package com.tcs.paymentms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.paymentms.entities.PaymentDetails;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Integer> {

}
