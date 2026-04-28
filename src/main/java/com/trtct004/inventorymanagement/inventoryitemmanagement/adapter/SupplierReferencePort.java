package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import java.util.Optional;

/**
 * EXT-002: Contract for fetching supplier reference data (DB-003 / SUPLMST).
 * BR-029/041/042: Supplier code validation and name enrichment.
 */
public interface SupplierReferencePort {

    /**
     * Fetch supplier data by supplier code.
     * @param supplierCode the supplier code
     * @return Optional containing supplier name if found; empty if not found
     */
    Optional<String> fetchSupplierData(String supplierCode);
}
