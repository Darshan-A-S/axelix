/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.payments;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Staging token interceptor: it attaches the fixed, shared token PayGrid issues for its
 * staging tenant. Registered only when {@code environment=staging}.
 */
@Component
@ConditionalOnProperty(name = "deploy.env", havingValue = "staging")
class StaticApiTokenInterceptor implements ApiTokenInterceptor {

	private final String apiToken;

	StaticApiTokenInterceptor(@Value("${paygrid.staging.token}") String apiToken) {
		this.apiToken = apiToken;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getHeaders().setBearerAuth(this.apiToken);
		return execution.execute(request, body);
	}

}
