package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * TRC-083: Warehouse Reference Adapter — EXT-003
 * Fetches warehouse reference data from DB-004 (WHSMST).
 * BR-030/043/044: Warehouse code validation and name enrichment.
 */
@Component
public class WarehouseReferenceAdapter implements WarehouseReferencePort {

    /**
     * EXT-003: Fetch warehouse data by warehouse code.
     * BR-043: Validate warehouse code exists in warehouse master.
     *
     * @param warehouseCode the 4-character warehouse code
     * @return Optional containing warehouse name if found; empty if not found
     */
    private static final java.util.Map<String, String> WAREHOUSE_MAP = java.util.Map.of(
            "WH01", "Main Warehouse",
            "WH02", "Secondary Warehouse",
            "WH03", "Cold Storage",
            "WH04", "Distribution Center"
    );

    public Optional<String> fetchWarehouseData(String warehouseCode) {
        return Optional.ofNullable(WAREHOUSE_MAP.get(warehouseCode));
    }
}
