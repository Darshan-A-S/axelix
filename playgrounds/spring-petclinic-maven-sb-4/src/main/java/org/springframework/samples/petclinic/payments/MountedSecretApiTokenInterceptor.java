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
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Token interceptor for the datacenters that have no Vault: there the PayGrid API token
 * is mounted into the container as a secret file. Registered only when that file is
 * present; it is re-read on every request, so a rotated token is picked up without a
 * restart.
 */
@Component
@ConditionalOnResource(resources = "file:" + MountedSecretApiTokenInterceptor.TOKEN_FILE)
class MountedSecretApiTokenInterceptor implements ApiTokenInterceptor {

	static final String TOKEN_FILE = "/var/run/secrets/paygrid/api-token";

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getHeaders().setBearerAuth(Files.readString(Path.of(TOKEN_FILE)).strip());
		return execution.execute(request, body);
	}

}
