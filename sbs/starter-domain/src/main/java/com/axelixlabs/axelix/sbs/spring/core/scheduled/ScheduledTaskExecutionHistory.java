/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.log.Logger;

/**
 * Bounded in-memory history of the scheduled task executions, kept per task until they are delivered to master.
 * The delivery follows the {@link #mark()} - {@link #commit()} / {@link #rollback()} protocol, so that the executions
 * are dropped only once they have been delivered.
 *
 * @author Vyacheslav Yanin
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class ScheduledTaskExecutionHistory {

    private final Map<String, SlidingWindow<ScheduledTaskExecution>> history = new ConcurrentHashMap<>();
    // compared by identity, so that equal executions recorded after the mark are not dropped by the commit
    private final AtomicReference<@Nullable Set<ScheduledTaskExecution>> marked = new AtomicReference<>();
    private final ScheduledTaskHistoryConfigurationProperties properties;
    private final Logger logger;

    public ScheduledTaskExecutionHistory(ScheduledTaskHistoryConfigurationProperties properties, Logger logger) {
        this.properties = properties;
        this.logger = logger;
    }

    public void record(ScheduledTaskExecution execution) {
        history.computeIfAbsent(execution.getTaskId(), taskId -> new SlidingWindow<>(properties.getHistoryMaxSize()))
                .put(execution);
    }

    /**
     * Returns the recorded executions and remembers them, so that {@link #commit()} drops them once they are
     * delivered. A pending mark is kept as is, since replacing it would let {@link #commit()} drop the executions
     * that have never been delivered.
     */
    public List<ScheduledTaskExecution> mark() {
        List<ScheduledTaskExecution> executions = history.values().stream()
                .flatMap(window -> window.get().stream())
                .collect(Collectors.toUnmodifiableList());

        Set<ScheduledTaskExecution> snapshot = Collections.newSetFromMap(new IdentityHashMap<>());
        snapshot.addAll(executions);

        if (!marked.compareAndSet(null, snapshot)) {
            logger.info("The scheduled task executions are marked while the previous mark is still pending, "
                    + "so the executions recorded since then are going to be delivered once again");
        }
        return executions;
    }

    /**
     * Drops the executions remembered by the last {@link #mark()}, once they have been delivered.
     */
    public void commit() {
        Set<ScheduledTaskExecution> delivered = marked.getAndSet(null);
        if (delivered != null) {
            history.values().forEach(window -> window.removeIf(delivered::contains));
        }
    }

    /**
     * Keeps the executions remembered by the last {@link #mark()} for the next delivery, since this one has failed.
     */
    public void rollback() {
        marked.set(null);
    }
}
