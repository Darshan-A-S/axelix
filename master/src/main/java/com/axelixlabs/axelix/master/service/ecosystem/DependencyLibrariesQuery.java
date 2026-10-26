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
package com.axelixlabs.axelix.master.service.ecosystem;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

/**
 * Which page of the used libraries to return, and how to filter them. Libraries Axelix does not curate have neither
 * a status nor an ecosystem, so they never match a non-empty {@code statuses} or {@code ecosystems} filter.
 *
 * @param search     a case-insensitive part of the {@code groupId:artifactId}, or {@code null} to match all.
 * @param statuses   the statuses to keep, or empty to keep all.
 * @param ecosystems the ecosystems to keep, or empty to keep all.
 * @param page       the zero-based number of the page.
 * @param size       the maximum number of libraries on a page.
 *
 * @author Mikhail Polivakha
 */
public record DependencyLibrariesQuery(
        @Nullable String search, Set<SupportStatus> statuses, Set<Ecosystem> ecosystems, int page, int size) {}
