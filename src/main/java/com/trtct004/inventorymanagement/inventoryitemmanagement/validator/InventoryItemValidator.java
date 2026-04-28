package com.trtct004.inventorymanagement.inventoryitemmanagement.validator;

import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.CategoryReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.SupplierReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.WarehouseReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.exception.InventoryItemStateTransitionException;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.util.InventoryConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * TRC-019, TRC-047, TRC-077: Inventory Item Validator
 * Implements field-level validation rules from DOC-03 INVDTLR (PGM-003).
 *
 * All errors are surfaced simultaneously (BR-048) — the caller receives a full list
 * of validation failures rather than failing on the first error encountered.
 *
 * DT-005: Field Validation Summary (Save Attempt)
 * DT-006: Gross Margin Calculation Conditions
 */
@Component
public class InventoryItemValidator implements InventoryItemValidatorPort {

    private final InventoryItemRepository inventoryItemRepository;
    private final CategoryReferencePort categoryReferenceAdapter;
    private final SupplierReferencePort supplierReferenceAdapter;
    private final WarehouseReferencePort warehouseReferenceAdapter;

    public InventoryItemValidator(
            InventoryItemRepository inventoryItemRepository,
            CategoryReferencePort categoryReferenceAdapter,
            SupplierReferencePort supplierReferenceAdapter,
            WarehouseReferencePort warehouseReferenceAdapter) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.categoryReferenceAdapter = categoryReferenceAdapter;
        this.supplierReferenceAdapter = supplierReferenceAdapter;
        this.warehouseReferenceAdapter = warehouseReferenceAdapter;
    }

    /**
     * Validate a complete item save request for Add or Edit mode.
     * BR-036 through BR-048, DT-005.
     *
     * @param request    the item request DTO
     * @param mode       operating mode: "A" (Add) or "E" (Edit)
     * @return list of validation error codes (empty = valid)
     */
    public List<String> validateItem(InventoryItemRequestDto request, String mode) {
        List<String> errors = new ArrayList<>();
        validateItemFields(request, mode, errors);
        return errors;
    }

    /**
     * Perform all field-level validation checks, accumulating errors.
     * BR-036 to BR-048 — all checks run regardless of prior failures (BR-048).
     */
    public void validateItemFields(InventoryItemRequestDto request, String mode, List<String> errors) {

        // BR-036: Validate Item ID Mandatory (Add mode only)
        if (InventoryConstants.MODE_ADD.equals(mode)) {
            if (!StringUtils.hasText(request.getItemId())) {
                errors.add(assignValidationErrorCode("ITEM_ID_REQUIRED"));
            } else {
                // BR-037: Validate Item ID Uniqueness (Add mode only)
                if (inventoryItemRepository.existsById(request.getItemId())) {
                    errors.add(assignValidationErrorCode("ITEM_ID_DUPLICATE"));
                }
            }
        }

        // BR-038: Validate Item Name Mandatory
        if (!StringUtils.hasText(request.getItemName())) {
            errors.add(assignValidationErrorCode("ITEM_NAME_REQUIRED"));
        }

        // BR-039: Validate Category Code — mandatory and must exist in reference table (DEF-008)
        String categoryCode = request.getCategoryCode() != null ? request.getCategoryCode().trim() : null;
        if (!StringUtils.hasText(categoryCode)) {
            errors.add(assignValidationErrorCode("CATEGORY_CODE_INVALID"));
        } else if (!categoryReferenceAdapter.fetchCategoryData(categoryCode).isPresent()) {
            errors.add(assignValidationErrorCode("CATEGORY_CODE_INVALID"));
        }

        // BR-041: Validate Supplier Code — mandatory and must exist in reference table (DEF-008)
        String supplierCode = request.getSupplierCode() != null ? request.getSupplierCode().trim() : null;
        if (!StringUtils.hasText(supplierCode)) {
            errors.add(assignValidationErrorCode("SUPPLIER_CODE_INVALID"));
        } else if (!supplierReferenceAdapter.fetchSupplierData(supplierCode).isPresent()) {
            errors.add(assignValidationErrorCode("SUPPLIER_CODE_INVALID"));
        }

        // BR-043: Validate Warehouse Code — mandatory and must exist in reference table (DEF-008)
        String warehouseCode = request.getWarehouseCode() != null ? request.getWarehouseCode().trim() : null;
        if (!StringUtils.hasText(warehouseCode)) {
            errors.add(assignValidationErrorCode("WAREHOUSE_CODE_INVALID"));
        } else if (!warehouseReferenceAdapter.fetchWarehouseData(warehouseCode).isPresent()) {
            errors.add(assignValidationErrorCode("WAREHOUSE_CODE_INVALID"));
        }

        // BR-045: Validate Price Not Below Cost
        if (request.getUnitPrice() != null && request.getUnitCost() != null) {
            if (request.getUnitPrice().compareTo(BigDecimal.ZERO) > 0
                    && request.getUnitPrice().compareTo(request.getUnitCost()) < 0) {
                errors.add(assignValidationErrorCode("PRICE_BELOW_COST"));
            }
        }
    }

    /**
     * Assign a validation error code string for a given failure type.
     * Maps logical failure names to error code strings returned in the response.
     * PT-001 to PT-003: validation error code assignment.
     */
    public String assignValidationErrorCode(String failureType) {
        return switch (failureType) {
            case "ITEM_ID_REQUIRED"    -> "INV-001";
            case "ITEM_ID_DUPLICATE"   -> "INV-002";
            case "ITEM_NAME_REQUIRED"  -> "INV-003";
            case "CATEGORY_CODE_INVALID"  -> "INV-004";
            case "SUPPLIER_CODE_INVALID"  -> "INV-005";
            case "WAREHOUSE_CODE_INVALID" -> "INV-006";
            case "PRICE_BELOW_COST"    -> "INV-007";
            default -> "INV-999";
        };
    }

    /**
     * Reject an illegal state transition for an inventory item.
     * STM-001: enforces T-001 through T-003 legal transitions.
     */
    public void rejectIllegalTransition(
            String itemId,
            InventoryItemStatus fromState,
            InventoryItemStatus toState,
            String transitionId) {
        throw new InventoryItemStateTransitionException(transitionId, fromState, toState, itemId);
    }
}
