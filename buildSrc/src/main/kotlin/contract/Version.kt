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
package contract

// TODO:
//  We have the SemanticVersion already in the common. I am not sure whether or not we should re-use it somehow.
//  That would require a lot of machinery that I think right now is unnecessary.
/**
 * An Axelix release version. The qualifier (e.g. -SNAPSHOT) is irrelevant to every compatibility
 * rule and is dropped at parsing.
 *
 * @author Mikhail Polivakha
 */
data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    override fun toString(): String = "$major.$minor.$patch"

    companion object {

        private val FORMAT = Regex("""(\d+)\.(\d+)\.(\d+)""")

        fun parse(value: String): Version? {
            val match = FORMAT.matchAt(value, 0) ?: return null
            val (major, minor, patch) = match.destructured
            return Version(major.toInt(), minor.toInt(), patch.toInt())
        }
    }
}
