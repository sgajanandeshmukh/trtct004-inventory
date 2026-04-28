package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import java.util.Optional;

/**
 * EXT-003: Contract for fetching warehouse reference data (DB-004 / WHSMST).
 * BR-030/043/044: Warehouse code validation and name enrichment.
 */
public interface WarehouseReferencePort {

    /**
     * Fetch warehouse data by warehouse code.
     * @param warehouseCode the warehouse code
     * @return Optional containing warehouse name if found; empty if not found
     */
    Optional<String> fetchWarehouseData(String warehouseCode);
}
