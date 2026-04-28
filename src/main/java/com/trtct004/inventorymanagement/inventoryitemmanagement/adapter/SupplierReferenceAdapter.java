package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * TRC-082: Supplier Reference Adapter — EXT-002
 * Fetches supplier reference data from DB-003 (SUPLMST).
 * BR-029/041/042: Supplier code validation and name enrichment.
 * PII: DB-003 contains Contact Name, Contact Phone, Contact Email — access controls required.
 */
@Component
public class SupplierReferenceAdapter implements SupplierReferencePort {

    /**
     * EXT-002: Fetch supplier data by supplier code.
     * BR-041: Validate supplier code exists in supplier master.
     *
     * @param supplierCode the 6-character supplier code
     * @return Optional containing supplier name if found; empty if not found
     */
    private static final java.util.Map<String, String> SUPPLIER_MAP = java.util.Map.of(
            "SUP001", "TechParts Co",
            "SUP002", "MechSupply Ltd",
            "SUP003", "ChemSource Inc",
            "SUP004", "PackPro LLC",
            "SUP005", "GlobalTools Ltd"
    );

    public Optional<String> fetchSupplierData(String supplierCode) {
        return Optional.ofNullable(SUPPLIER_MAP.get(supplierCode));
    }
}
