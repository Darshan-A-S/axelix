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
package com.axelixlabs.axelix.master.service.transport;

import java.util.List;

/**
 * Indicates that a bulk endpoint invocation completed only for a subset of requested instances.
 *
 * @author Sergey Cherkasov
 */
public class PartiallyUpdatedException extends RuntimeException {

    private final List<String> failedInstanceIds;

    /**
     * @param failedInstanceIds the IDs of the instances for which the invocation has failed
     */
    public PartiallyUpdatedException(List<String> failedInstanceIds) {
        super("The operation has failed for the following instances: " + String.join(", ", failedInstanceIds));
        this.failedInstanceIds = List.copyOf(failedInstanceIds);
    }

    public List<String> failedInstanceIds() {
        return failedInstanceIds;
    }
}
