package com.trtct004.inventorymanagement.inventoryitemmanagement.validator;

import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemRequestDto;

import java.util.List;

/**
 * Contract for validating inventory item save requests.
 * BR-036 through BR-048, DT-005.
 */
public interface InventoryItemValidatorPort {

    /**
     * Validate a complete item save request for Add or Edit mode.
     * @param request the item request DTO
     * @param mode    operating mode: "A" (Add) or "E" (Edit)
     * @return list of validation error codes (empty = valid)
     */
    List<String> validateItem(InventoryItemRequestDto request, String mode);
}
