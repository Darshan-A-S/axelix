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
package com.axelixlabs.axelix.master.repository;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.ApplicationDependencies;

/**
 * Repository for the {@link ApplicationDependencies} aggregate.
 *
 * @author Nikita Kirillov
 */
public interface ApplicationDependenciesRepository extends ListCrudRepository<ApplicationDependencies, String> {

    @Query("SELECT * FROM application_dependencies WHERE group_id = :groupId AND artifact_id = :artifactId")
    Optional<ApplicationDependencies> findByApplication(
            @Param("groupId") String groupId, @Param("artifactId") String artifactId);
}
