package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for SCR-006: Inventory Reorder Report filter parameters.
 * F-006-11: Warehouse Filter (RPWHSE) — optional (BR-060)
 * F-006-12: Category Filter (RPCTGY) — optional (BR-061)
 * DT-008: Report refresh triggers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderReportRequestDto {

    /** F-006-11: Warehouse filter — 4-char warehouse code; null/blank = all warehouses (BR-060) */
    private String warehouseCode;

    /** F-006-12: Category filter — 6-char category code; null/blank = all categories (BR-061) */
    private String categoryCode;
}
