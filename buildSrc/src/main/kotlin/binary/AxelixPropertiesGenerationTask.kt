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
package binary

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.Properties

/**
 * Gradle task for generating the {@code axelix.properties} file.
 *
 * @author Mikhail Polivakha
 */
abstract class AxelixPropertiesGenerationTask : DefaultTask() {

    /**
     * The version of Axelix distribution.
     */
    @get:Input
    abstract val properties: MapProperty<String, String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generatePropertiesFile() {
        val props = Properties()
        props.putAll(properties.get())

        val propertiesFile = outputFile.get().asFile
        propertiesFile.parentFile.mkdirs()

        propertiesFile.outputStream().use {
            props.store(it, null)
        }
    }
}