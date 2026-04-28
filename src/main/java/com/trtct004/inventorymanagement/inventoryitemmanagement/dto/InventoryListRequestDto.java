package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for SCR-002: Inventory Item List request parameters.
 * F-002-11: Category Search Filter (SRCTGY) — exact match (BR-012)
 * F-002-12: Name Search Filter (SRNAME) — partial match (BR-013)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryListRequestDto {

    /** Category filter — exact match on category code; null/blank = no filter (BR-012) */
    private String categoryCode;

    /** Name filter — partial/contains match on item name; null/blank = no filter (BR-013) */
    private String nameFilter;

    /** Page size — maximum rows to return; defaults to HV-005 (200) */
    private int pageSize = 200;
}
