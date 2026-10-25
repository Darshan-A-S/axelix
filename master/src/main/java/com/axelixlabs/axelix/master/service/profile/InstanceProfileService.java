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
package com.axelixlabs.axelix.master.service.profile;

import com.axelixlabs.axelix.master.api.external.response.profile.InstanceProfileResponse;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

/**
 * Assembles the profile of a managed instance.
 *
 * @author Mikhail Polivakha
 */
public interface InstanceProfileService {

    /**
     * @param instanceId the id of the instance to assemble the profile for.
     * @return the profile of the instance.
     *
     * @throws InstanceNotFoundException when the instance is not known to this Axelix Master.
     */
    InstanceProfileResponse getProfile(InstanceId instanceId);
}
