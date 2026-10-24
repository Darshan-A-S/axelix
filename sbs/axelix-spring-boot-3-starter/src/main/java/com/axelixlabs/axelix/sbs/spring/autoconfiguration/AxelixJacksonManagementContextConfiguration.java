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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.util.List;

import com.fasterxml.jackson.databind.json.JsonMapper;

import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axelixlabs.axelix.sbs.spring.core.web.AxelixJacksonHttpMessageConverter;

/**
 * Configures a dedicated {@link AxelixJacksonHttpMessageConverter} with an isolated Jackson mapper
 * in the context that serves Actuator (main context or separate management port context), ensuring Axelix
 * endpoint serialization is isolated from the application's Jackson configuration without exposing the
 * mapper to the host application context.
 *
 * @author Darshan A S
 */
@ManagementContextConfiguration(value = ManagementContextType.ANY, proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(
        name = {
            "org.springframework.web.servlet.config.annotation.WebMvcConfigurer",
            "com.fasterxml.jackson.databind.json.JsonMapper"
        })
public class AxelixJacksonManagementContextConfiguration {

    @Bean
    public WebMvcConfigurer axelixEndpointWebMvcConfigurer() {
        AxelixJacksonHttpMessageConverter converter = new AxelixJacksonHttpMessageConverter(
                JsonMapper.builder().findAndAddModules().build());
        return new WebMvcConfigurer() {
            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                converters.add(0, converter);
            }
        };
    }
}
