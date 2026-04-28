package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.CategoryReferenceAdapter;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.SupplierReferenceAdapter;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.WarehouseReferenceAdapter;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemResponseDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.InventoryItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TRC-008, TRC-093, TRC-098: Inventory Item Controller — SCR-003/004
 * UI-003: Item Detail Maintenance and Delete Confirmation screens.
 *
 * SCR-003: Multi-mode (Add/Edit/Display) item detail form.
 * SCR-004: Delete Confirmation dialog.
 *
 * BR-023: Add Mode Initialization
 * BR-024: Edit Mode Load
 * BR-025: Display Mode Read-Only
 * BR-026: Refresh Record on Demand (F5)
 * BR-027: Display Mode Blocks Save
 * BR-028–030: Enrich display with category/supplier/warehouse names
 * BR-031/032: Calculate gross margin (ALG-001)
 * BR-035: Record Not Found on Load
 * BR-036–048: Field validation and save block
 * BR-049–054: New item save, mode switch
 */
@RestController
@RequestMapping("/api/inventory/items")
public class InventoryItemController {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;
    private final CategoryReferenceAdapter categoryReferenceAdapter;
    private final SupplierReferenceAdapter supplierReferenceAdapter;
    private final WarehouseReferenceAdapter warehouseReferenceAdapter;

    public InventoryItemController(
            InventoryItemRepository inventoryItemRepository,
            InventoryItemService inventoryItemService,
            CategoryReferenceAdapter categoryReferenceAdapter,
            SupplierReferenceAdapter supplierReferenceAdapter,
            WarehouseReferenceAdapter warehouseReferenceAdapter) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemService = inventoryItemService;
        this.categoryReferenceAdapter = categoryReferenceAdapter;
        this.supplierReferenceAdapter = supplierReferenceAdapter;
        this.warehouseReferenceAdapter = warehouseReferenceAdapter;
    }

    /**
     * Load item detail screen — Add, Edit, or Display mode.
     * BR-023/024/025: Mode-based initialization.
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponseDto> handleItemDetail(
            @PathVariable String itemId,
            @RequestParam(defaultValue = "D") String mode) {

        if ("A".equals(mode)) {
            // BR-023: Add Mode — blank screen
            return ResponseEntity.ok(InventoryItemResponseDto.builder()
                    .currentMode("Add")
                    .build());
        }

        // BR-024/025: Edit/Display Mode — load existing record
        return inventoryItemRepository.findById(itemId)
                .map(item -> buildResponse(item, mode))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(InventoryItemResponseDto.builder()
                        .recordNotFound(true) // BR-035
                        .build()));
    }

    /**
     * Save item — Add or Edit mode.
     * BR-036–048: Validation and save block.
     * BR-049–054: Post-save state.
     */
    @PostMapping
    public ResponseEntity<InventoryItemResponseDto> handleSave(
            @Valid @RequestBody InventoryItemRequestDto request) {

        String mode = request.getMode() != null ? request.getMode() : "A";

        // BR-027: Display Mode Blocks Save
        if ("D".equals(mode)) {
            return ResponseEntity.ok(InventoryItemResponseDto.builder()
                    .validationFailed(false)
                    .build());
        }

        InventoryItemService.SaveResult result = inventoryItemService.saveItem(request, mode);

        if (!result.success()) {
            // BR-048: Save Blocked on Validation Failure
            return ResponseEntity.unprocessableEntity()
                    .body(InventoryItemResponseDto.builder()
                            .validationFailed(true)
                            .validationErrors(result.errors())
                            .build());
        }

        InventoryItemEntity saved = result.entity();
        // BR-051: Post-Add Mode Switch to Edit
        String newMode = "A".equals(mode) ? "Edit" : "Edit";

        return ResponseEntity.ok(buildResponse(saved, "E")
                .toBuilder()
                .saveSuccess(true) // BR-052
                .currentMode(newMode)
                .build());
    }

    /**
     * Render the item detail form.
     */
    @GetMapping("/{itemId}/render")
    public ResponseEntity<InventoryItemResponseDto> renderItemDetailForm(
            @PathVariable String itemId,
            @RequestParam(defaultValue = "D") String mode) {
        return handleItemDetail(itemId, mode);
    }

    /** Bind detail screen for frontend */
    @GetMapping("/{itemId}/bind")
    public ResponseEntity<InventoryItemResponseDto> bindDetailScreen(
            @PathVariable String itemId,
            @RequestParam(defaultValue = "D") String mode) {
        return handleItemDetail(itemId, mode);
    }

    /** Bind delete confirmation screen — SCR-004 */
    @GetMapping("/{itemId}/delete-confirm")
    public ResponseEntity<Map<String, Object>> bindDeleteConfirmScreen(@PathVariable String itemId) {
        return inventoryItemRepository.findById(itemId)
                .map(item -> {
                    Map<String, Object> body = new java.util.LinkedHashMap<>();
                    body.put("screen", "DELETE_CONFIRM");
                    body.put("itemId", item.getItemId());
                    body.put("itemName", item.getItemName());
                    body.put("quantityOnHand", item.getQuantityOnHand());
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().<Map<String, Object>>build());
    }

    private InventoryItemResponseDto buildResponse(InventoryItemEntity item, String mode) {
        // BR-028–030: Enrich display names
        String categoryName = null;
        try { categoryName = categoryReferenceAdapter.fetchCategoryData(item.getCategoryCode()).orElse(null); } catch (Exception ignored) {}
        String supplierName = null;
        try { supplierName = supplierReferenceAdapter.fetchSupplierData(item.getSupplierCode()).orElse(null); } catch (Exception ignored) {}
        String warehouseName = null;
        try { warehouseName = warehouseReferenceAdapter.fetchWarehouseData(item.getWarehouseCode()).orElse(null); } catch (Exception ignored) {}

        return InventoryItemResponseDto.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .categoryCode(item.getCategoryCode())
                .categoryName(categoryName)
                .supplierCode(item.getSupplierCode())
                .supplierName(supplierName)
                .warehouseCode(item.getWarehouseCode())
                .warehouseName(warehouseName)
                .quantityOnHand(item.getQuantityOnHand())
                .quantityAllocated(item.getQuantityAllocated())
                .quantityOnOrder(item.getQuantityOnOrder())
                .reorderPoint(item.getReorderPoint())
                .reorderQuantity(item.getReorderQuantity())
                .unitCost(item.getUnitCost())
                .unitPrice(item.getUnitPrice())
                // ALG-001: BR-031/032 — calculated gross margin
                .marginPercentage(inventoryItemService.calculateMargin(item.getUnitPrice(), item.getUnitCost()))
                // BR-033/034: Active or Inactive status display
                .status(item.isActive() ? "Active" : "Inactive")
                .lastUpdatedDate(item.getLastUpdatedDate())
                .currentMode("E".equals(mode) ? "Edit" : "D".equals(mode) ? "Display" : "Add")
                .build();
    }
}
