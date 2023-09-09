package com.tcs.inventoryms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.inventoryms.entities.BusInventory;

public interface BusInventoryRepository extends JpaRepository<BusInventory, String> {

}
