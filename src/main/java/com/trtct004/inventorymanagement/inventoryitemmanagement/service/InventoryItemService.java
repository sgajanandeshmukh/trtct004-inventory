package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.CategoryReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.SupplierReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.WarehouseReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemResponseDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.validator.InventoryItemValidatorPort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.util.InventoryConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TRC-016, TRC-020, TRC-021, TRC-029, TRC-048: Inventory Item Service
 * Implements domain logic for inventory item lifecycle management.
 *
 * BR-016 to BR-019: Soft delete / deactivation (INVLSTR)
 * BR-040: Save pre-conditions
 * BR-041 to BR-048: Create item, update item
 * ALG-001: Calculate margin percentage
 * DT-007: Decide save or reject based on validation
 */
@Service
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemValidatorPort inventoryItemValidator;
    private final CategoryReferencePort categoryReferenceAdapter;
    private final SupplierReferencePort supplierReferenceAdapter;
    private final WarehouseReferencePort warehouseReferenceAdapter;

    public InventoryItemService(
            InventoryItemRepository inventoryItemRepository,
            InventoryItemValidatorPort inventoryItemValidator,
            CategoryReferencePort categoryReferenceAdapter,
            SupplierReferencePort supplierReferenceAdapter,
            WarehouseReferencePort warehouseReferenceAdapter) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemValidator = inventoryItemValidator;
        this.categoryReferenceAdapter = categoryReferenceAdapter;
        this.supplierReferenceAdapter = supplierReferenceAdapter;
        this.warehouseReferenceAdapter = warehouseReferenceAdapter;
    }

    /**
     * BR-041 to BR-048: Save an item (create or update based on mode).
     * DT-007: Decide save or reject.
     *
     * @param request item request DTO
     * @param mode    "A" (Add) or "E" (Edit)
     * @return SaveResult with saved entity or validation errors
     */
    @Transactional
    public SaveResult saveItem(InventoryItemRequestDto request, String mode) {
        List<String> errors = inventoryItemValidator.validateItem(request, mode);

        // BR-048: Save Blocked on Validation Failure — DT-007 reject path
        if (!errors.isEmpty()) {
            return SaveResult.failure(errors);
        }

        return decideSaveOrReject(request, mode);
    }

    /**
     * DT-007: Decide whether to proceed with save or reject after validation.
     * Routes to createItem for Add mode or updateItem for Edit mode.
     */
    public SaveResult decideSaveOrReject(InventoryItemRequestDto request, String mode) {
        if (InventoryConstants.MODE_ADD.equals(mode)) {
            return SaveResult.success(createItem(request));
        } else {
            return SaveResult.success(updateItem(request));
        }
    }

    /**
     * BR-049/050/051: Create a new inventory item.
     * Initializes allocated qty = 0 (HV-023), on-order qty = 0 (HV-024), status = ACTIVE (HV-013).
     */
    public InventoryItemEntity createItem(InventoryItemRequestDto request) {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setItemId(request.getItemId());
        entity.setItemName(request.getItemName());
        entity.setItemDescription(request.getItemDescription());
        entity.setCategoryCode(request.getCategoryCode());
        entity.setQuantityOnHand(request.getQuantityOnHand() != null ? request.getQuantityOnHand() : 0);
        // BR-049: Initialize allocation and order quantities to zero
        entity.setQuantityAllocated(InventoryConstants.NEW_ITEM_ALLOCATED_QTY);
        entity.setQuantityOnOrder(InventoryConstants.NEW_ITEM_ON_ORDER_QTY);
        entity.setReorderPoint(request.getReorderPoint());
        entity.setReorderQuantity(request.getReorderQuantity());
        entity.setUnitCost(request.getUnitCost());
        entity.setUnitPrice(request.getUnitPrice());
        entity.setSupplierCode(request.getSupplierCode());
        entity.setWarehouseCode(request.getWarehouseCode());
        // BR-050: New item status = ACTIVE
        entity.setStatus(InventoryItemStatus.ACTIVE);
        entity.setLastUpdatedDate(LocalDate.now());
        return inventoryItemRepository.save(entity);
    }

    /**
     * BR-053: Update an existing inventory item.
     */
    public InventoryItemEntity updateItem(InventoryItemRequestDto request) {
        InventoryItemEntity entity = inventoryItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalStateException("Item not found: " + request.getItemId()));

        entity.setItemName(request.getItemName());
        entity.setItemDescription(request.getItemDescription());
        entity.setCategoryCode(request.getCategoryCode());
        entity.setQuantityOnHand(request.getQuantityOnHand());
        entity.setReorderPoint(request.getReorderPoint());
        entity.setReorderQuantity(request.getReorderQuantity());
        entity.setUnitCost(request.getUnitCost());
        entity.setUnitPrice(request.getUnitPrice());
        entity.setSupplierCode(request.getSupplierCode());
        entity.setWarehouseCode(request.getWarehouseCode());
        entity.setLastUpdatedDate(LocalDate.now());

        return inventoryItemRepository.save(entity);
    }

    /**
     * ALG-001: Calculate gross margin percentage.
     * DT-006: If unit price > 0: ((price − cost) / price) × 100; else 0.
     *
     * @param unitPrice selling price per unit
     * @param unitCost  purchase cost per unit
     * @return gross margin percentage (BigDecimal, 2 decimal places)
     */
    public BigDecimal calculateMargin(BigDecimal unitPrice, BigDecimal unitCost) {
        // BR-032/047: Zero margin when no unit price
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // BR-031/046: ALG-001 formula
        return unitPrice.subtract(unitCost)
                .divide(unitPrice, 4, RoundingMode.HALF_UP)
                .multiply(InventoryConstants.MARGIN_PERCENT_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * BR-017/019: Check delete eligibility — item must have no allocated quantity.
     * BR-019: Block deletion when allocated quantity exists.
     *
     * @param itemId the item to check
     * @return empty Optional if deletion is allowed; error message if blocked
     */
    public Optional<String> checkDeleteEligibility(String itemId) {
        return inventoryItemRepository.findById(itemId).map(item -> {
            if (item.getQuantityAllocated() != null && item.getQuantityAllocated() > 0) {
                // BR-019: Block deletion
                return "Item cannot be deactivated — allocated quantity > 0";
            }
            return null;
        });
    }

    /**
     * BR-016/020: Soft delete — set item status to INACTIVE.
     * STM-001 T-001: ACTIVE → INACTIVE.
     *
     * @param itemId the item to deactivate
     */
    @Transactional
    public void softDeleteItem(String itemId) {
        InventoryItemEntity item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalStateException("Item not found: " + itemId));

        // BR-019: Block if allocated quantity exists
        if (item.getQuantityAllocated() != null && item.getQuantityAllocated() > 0) {
            throw new IllegalStateException(
                    "Cannot deactivate item '" + itemId + "' — allocated quantity is " + item.getQuantityAllocated());
        }

        // BR-020: Set status to Inactive (STM-001 T-001)
        item.softDelete();
        item.setLastUpdatedDate(LocalDate.now());
        inventoryItemRepository.save(item);
    }

    /**
     * Result of a save operation.
     */
    public record SaveResult(InventoryItemEntity entity, List<String> errors, boolean success) {
        public static SaveResult success(InventoryItemEntity entity) {
            return new SaveResult(entity, List.of(), true);
        }
        public static SaveResult failure(List<String> errors) {
            return new SaveResult(null, errors, false);
        }
    }
}
