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
import type { EInstanceFeature, IInstanceCard } from "@/models";

/**
 * The profile of a single managed instance, i.e. everything the instance view renders around its pages.
 */
export interface IInstanceProfileResponse {
    /** The short profile of the instance, the same as the wallboard renders */
    instance: IInstanceCard;

    /** The number of the other instances of the same application, i.e. excluding this one */
    siblingsCount: number;

    /**
     * The features the instance has. The UI renders a feature (its menu entry, its page) only when its id is present.
     * For starters older than 1.3, which do not detect their features, Axelix Master assumes all of them.
     */
    features: EInstanceFeature[];
}
