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
package com.axelixlabs.axelix.sbs.spring.core.master.insights;

import java.util.List;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.Insights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.TransactionAggregatedProfile;

/**
 * Provides insights discovered for the current service instance.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public interface InsightsInfoProvider {

    /**
     * @return the actual insight information.
     */
    Insights getInsight();

    /**
     * @return the aggregated information about the transactions inside the instance.
     */
    List<TransactionAggregatedProfile> getTransactions();

    /**
     * IMPORTANT: this is not a plain getter. The returned executions are marked as being delivered, so every call
     * must be followed by {@link com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory#commit()}
     * or {@link com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory#rollback()}, depending
     * on the delivery outcome. Otherwise, the executions are going to be delivered to master once again.
     *
     * @return the scheduled task executions that are not yet delivered to master.
     */
    List<ScheduledTaskExecution> getScheduledTaskExecutions();
}
