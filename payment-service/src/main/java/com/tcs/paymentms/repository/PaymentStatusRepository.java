package com.tcs.paymentms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.paymentms.entities.PaymentStatus;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {
	
	public List<PaymentStatus> findByPaymentNumberOrderByCreatedDateDesc(Integer paymentNumber);

}
