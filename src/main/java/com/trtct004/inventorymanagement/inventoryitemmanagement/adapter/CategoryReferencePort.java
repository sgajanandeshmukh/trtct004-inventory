package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import java.util.Optional;

/**
 * EXT-001: Contract for fetching category reference data (DB-002 / CTGMST).
 * BR-028/039/040: Category code validation and name enrichment.
 */
public interface CategoryReferencePort {

    /**
     * Fetch category data by category code.
     * @param categoryCode the category code
     * @return Optional containing category name if found; empty if not found
     */
    Optional<String> fetchCategoryData(String categoryCode);
}
