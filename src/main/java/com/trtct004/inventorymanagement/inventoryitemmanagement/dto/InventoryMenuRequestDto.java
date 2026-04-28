package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for SCR-001: Inventory Management Main Menu.
 * F-001-09: Selection Entry (MNSEL) — accepts values: 1, 2, 3, 9.
 * BR-002–007: Menu option routing and validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMenuRequestDto {

    /** F-001-09: Menu selection — 1=Browse, 2=Item Detail, 3=Reorder, 9=Exit (DT-001) */
    @NotBlank
    private String selection;
}
