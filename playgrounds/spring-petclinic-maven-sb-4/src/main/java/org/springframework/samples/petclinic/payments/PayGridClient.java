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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Hands payments over to PayGrid, the external provider that executes them.
 */
@Component
class PayGridClient {

	private static final Logger log = LoggerFactory.getLogger(PayGridClient.class);

	private final RestClient payGridRestClient;

	PayGridClient(RestClient payGridRestClient) {
		this.payGridRestClient = payGridRestClient;
	}

	void processAll(List<Payment> payments) {
		List<PaymentInstruction> instructions = payments.stream()
			.map(payment -> new PaymentInstruction(payment.getId(), payment.getPayee(), payment.getAmount(),
					payment.getCurrency(), payment.getInvoices()))
			.toList();
		try {
			this.payGridRestClient.post().uri("/v1/payments/batch").body(instructions).retrieve().toBodilessEntity();
		}
		catch (RestClientException ex) {
			log.error("request to PayGrid failed: {}", ex.getMessage());
			throw ex;
		}
	}

	record PaymentInstruction(Long paymentId, String payee, BigDecimal amount, String currency, Set<String> invoices) {
	}

}
