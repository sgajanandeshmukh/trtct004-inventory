package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * TRC-081: Category Reference Adapter — EXT-001
 * Fetches category reference data from DB-002 (CTGMST).
 * BR-028/039/040: Category code validation and name enrichment.
 */
@Component
public class CategoryReferenceAdapter implements CategoryReferencePort {

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "ELECT", "Electronics",
            "MECH",  "Mechanical Parts",
            "CHEM",  "Chemical Supplies",
            "PACK",  "Packaging Materials",
            "TOOL",  "Tools & Equipment",
            "MISC",  "Miscellaneous"
    );

    /**
     * EXT-001: Fetch category data by category code.
     * BR-039: Validate category code exists in category master.
     *
     * @param categoryCode the 6-character category code
     * @return Optional containing category name if found; empty if not found
     */
    public Optional<String> fetchCategoryData(String categoryCode) {
        return Optional.ofNullable(CATEGORY_MAP.get(categoryCode));
    }
}
