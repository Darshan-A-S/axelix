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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

/**
 * Internal plugin for various Axelix modules components build.
 *
 * @author Mikhail Polivakha
 */
class AxelixPropertiesPlugin : Plugin<Project> {

    companion object {
        private const val AXELIX_DSL_CONFIG = "axelix"

        private const val AXELIX_PROPERTIES_FILE_NAME = "axelix.properties"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create<AxelixPluginExtension>(AXELIX_DSL_CONFIG)

        val axelixGenResourcesDir = project.layout.buildDirectory.dir("generated-resources/axelix")

        val genTask = project.tasks.register("generatePropsFile", AxelixPropertiesGenerationTask::class.java) {
            properties.set(extension.properties)

            outputFile.set(axelixGenResourcesDir.map { it.file("META-INF/$AXELIX_PROPERTIES_FILE_NAME") })
        }

        project.plugins.withId("java") {
            project.extensions.configure<SourceSetContainer> {
                named("main") {
                    // we want to put the entire META-INF directory's content into final resources
                    // bundle, that is why we have two parent calls below
                    resources.srcDir(genTask.map { it.outputFile.get().asFile.parentFile.parent })
                }
            }
        }
    }

    /**
     * Interface that provides the DSL extension for the [AxelixPropertiesPlugin].
     *
     * @author Mikhail Polivakha
     */
    interface AxelixPluginExtension {

        val properties: MapProperty<String, String>
    }
}