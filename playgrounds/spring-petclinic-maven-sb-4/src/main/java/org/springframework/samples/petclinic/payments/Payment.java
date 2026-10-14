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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * A payment the nightly {@link PaymentBatchJob} hands over to PayGrid for execution.
 */
@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String payee;

	private BigDecimal amount;

	private String currency;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	@ElementCollection
	@CollectionTable(name = "payment_invoices", joinColumns = @JoinColumn(name = "payment_id"))
	@Column(name = "invoice_number")
	private Set<String> invoices = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "payment_risk_flags", joinColumns = @JoinColumn(name = "payment_id"),
			inverseJoinColumns = @JoinColumn(name = "risk_flag_id"))
	private List<RiskFlag> riskFlags = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public String getPayee() {
		return payee;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public Set<String> getInvoices() {
		return invoices;
	}

	public List<RiskFlag> getRiskFlags() {
		return riskFlags;
	}

	void markProcessed() {
		this.status = PaymentStatus.PROCESSED;
	}

}
