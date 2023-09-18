package com.tcs.inventoryms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.inventoryms.entities.InventoryUpdateLog;

public interface InventoryUpdateLogRepository extends JpaRepository<InventoryUpdateLog, Integer> {

}
