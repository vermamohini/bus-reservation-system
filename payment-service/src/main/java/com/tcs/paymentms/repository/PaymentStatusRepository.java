package com.tcs.paymentms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.paymentms.entities.PaymentStatus;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {

}
