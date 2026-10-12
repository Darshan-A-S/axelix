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

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stand-in for the external PayGrid API, hosted by the playground itself so the demo
 * needs no outside dependency - point {@code paygrid.host} at it. Like the real PayGrid
 * edge, it never answers a request that carries no API token in time: the caller only
 * sees a read timeout, not the reason.
 */
@RestController
@RequestMapping("/paygrid-sandbox")
class PayGridSandboxController {

	/**
	 * Must exceed the client's {@code spring.http.clients.read-timeout}.
	 */
	private static final Duration UNAUTHENTICATED_STALL = Duration.ofSeconds(5);

	@PostMapping("/v1/payments/batch")
	ResponseEntity<Void> acceptBatch(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization)
			throws InterruptedException {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			Thread.sleep(UNAUTHENTICATED_STALL.toMillis());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.accepted().build();
	}

}
