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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The nightly payment batch: picks up the pending payments and hands them over to
 * PayGrid. A failed PayGrid call propagates out of the task, so the run is reported as
 * failed and the payments stay pending for the next run.
 */
@Component
public class PaymentBatchJob {

	private static final Logger log = LoggerFactory.getLogger(PaymentBatchJob.class);

	private final PaymentRepository payments;

	private final PayGridClient payGrid;

	PaymentBatchJob(PaymentRepository payments, PayGridClient payGrid) {
		this.payments = payments;
		this.payGrid = payGrid;
	}

	@Scheduled(cron = "${payments.batch.cron}")
	public void processPendingPayments() {
		List<Payment> pending = this.payments.findByStatus(PaymentStatus.PENDING, Pageable.ofSize(2_000));
		log.info("picked up {} pending payments", pending.size());
		if (pending.isEmpty()) {
			return;
		}
		this.payGrid.processAll(pending);
		pending.forEach(Payment::markProcessed);
		this.payments.saveAll(pending);
		log.info("processed {} payments", pending.size());
	}

}
