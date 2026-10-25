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
package com.axelixlabs.axelix.master.service.convert.response;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse.InstanceShortProfile;
import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse.InstanceStatus;
import com.axelixlabs.axelix.master.domain.instance.Instance;

/**
 * Converter that is capable to translate the given {@link Instance} to the {@link InstanceShortProfile}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class InstancesToShortProfileConverter implements Converter<Instance, InstanceShortProfile> {

    @Override
    public @NonNull InstanceShortProfile convertNonNull(@NonNull Instance instance) {
        return new InstanceShortProfile(
                instance.id().instanceId(),
                instance.name(),
                instance.serviceVersion(),
                instance.commitShaShort(),
                switch (instance.status()) {
                    case UP -> InstanceStatus.UP;
                    case DOWN -> InstanceStatus.DOWN;
                    case UNKNOWN -> InstanceStatus.UNKNOWN;
                },
                instance.deployedAt() != null ? instance.deployedAt().getEpochSecond() : null,
                instance.javaVersion().render(),
                instance.springBootVersion(),
                instance.springFrameworkVersion(),
                instance.kotlinVersion());
    }
}
