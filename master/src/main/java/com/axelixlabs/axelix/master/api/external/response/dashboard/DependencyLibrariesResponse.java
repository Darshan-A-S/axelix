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
package com.axelixlabs.axelix.master.api.external.response.dashboard;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.ProjectReference;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Succession;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

/**
 * A single page of the libraries used across the applications.
 *
 * @param libraries         the libraries on the requested page.
 * @param matchingLibraries the number of libraries matching the filters, across all pages.
 * @param page              the zero-based number of the page.
 * @param size              the maximum number of libraries on a page.
 *
 * @author Mikhail Polivakha
 */
public record DependencyLibrariesResponse(List<UsedLibrary> libraries, long matchingLibraries, int page, int size) {

    /**
     * A library used by at least one application. Everything below {@code versions} comes from the curated catalog and
     * is {@code null} when Axelix does not curate the library, e.g. for an organization's internal library.
     *
     * @param library          the {@code groupId:artifactId} of the library.
     * @param applicationCount the number of applications that use the library.
     * @param share            the percentage of all the applications that use the library.
     * @param versions         the versions in use, the newest first.
     * @param status           what the authors are still doing with the library.
     * @param ecosystem        the area of a running application the library belongs to.
     * @param summary          what happened to the library, in prose.
     * @param succession       where to go instead, if anywhere.
     * @param reference        the upstream page backing the {@code status}.
     */
    public record UsedLibrary(
            Library library,
            long applicationCount,
            double share,
            List<VersionUsage> versions,
            @Nullable SupportStatus status,
            @Nullable Ecosystem ecosystem,
            @Nullable String summary,
            @Nullable Succession succession,
            @Nullable ProjectReference reference) {}

    /**
     * @param version          the resolved version.
     * @param applicationCount the number of applications that resolve exactly this version.
     */
    public record VersionUsage(String version, long applicationCount) {}
}
