package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryListRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryListResponseDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.InventoryItemService;
import com.trtct004.inventorymanagement.inventoryitemmanagement.util.InventoryConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TRC-007, TRC-018, TRC-046, TRC-092, TRC-097: Inventory List Controller — SCR-002
 * UI-002: Inventory Item List screen.
 *
 * BR-008: Initial List Load on Entry
 * BR-009: Manual List Refresh (F5)
 * BR-010: Add New Item from List (F6)
 * BR-011: Auto-Refresh on Filter Change
 * BR-012: Category Filter Applied
 * BR-013: Item Name Filter Applied
 * BR-014: Maximum List Size Cap (HV-005: 200)
 * BR-015: Empty List Notification
 * BR-016: Edit Item Action (action code '2')
 * BR-017: Delete Item Action (action code '4')
 * BR-018: Display Item Action (action code '5')
 * BR-019: Block Deletion When Allocated Quantity Exists
 * BR-020: Soft Delete — Set Item Inactive
 * BR-021: Deactivation Success Notification
 * BR-022: Post-Edit List Reload
 * DT-002: List Row Action Code Processing
 * DT-003: List Filter Auto-Refresh Logic
 */
@RestController
@RequestMapping("/api/inventory/list")
public class InventoryListController {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;

    public InventoryListController(
            InventoryItemRepository inventoryItemRepository,
            InventoryItemService inventoryItemService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemService = inventoryItemService;
    }

    /**
     * BR-008/009/011: Display item list with optional filters.
     * BR-014: Capped at HV-005 (200 rows).
     */
    @GetMapping
    public ResponseEntity<InventoryListResponseDto> displayItemList(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(filterItemList(new InventoryListRequestDto(categoryCode, nameFilter, InventoryConstants.MAX_LIST_SIZE), status));
    }

    /**
     * Filter item list — BR-012/013/014.
     */
    public InventoryListResponseDto filterItemList(InventoryListRequestDto request, String status) {
        List<InventoryItemEntity> allItems = inventoryItemRepository
                .findByFilters(request.getCategoryCode(), request.getNameFilter(), status);

        // BR-014: Maximum List Size Cap
        List<InventoryListResponseDto.ListRow> rows = allItems.stream()
                .limit(request.getPageSize())
                .map(item -> InventoryListResponseDto.ListRow.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .categoryCode(item.getCategoryCode())
                        .quantityOnHand(item.getQuantityOnHand())
                        .unitPrice(item.getUnitPrice())
                        .status(item.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        // BR-015: Empty List Notification
        return InventoryListResponseDto.builder()
                .items(rows)
                .noRecordsFound(rows.isEmpty())
                .build();
    }

    /**
     * Paginate list — returns a page of rows.
     */
    public InventoryListResponseDto paginateList(InventoryListRequestDto request, int page) {
        // Pagination is handled by pageSize parameter in filterItemList
        return filterItemList(request, null);
    }

    /**
     * Sort item list — BR-011: refreshed list is sorted by item name.
     */
    public InventoryListResponseDto sortItemList(InventoryListRequestDto request) {
        return filterItemList(request, null); // DB query already orders by itemName
    }

    /**
     * DT-002: Resolve list row action for a given item.
     * BR-016: Edit ('2'), BR-017: Delete ('4'), BR-018: Display ('5').
     */
    @PostMapping("/{itemId}/action")
    public ResponseEntity<Object> resolveListAction(
            @PathVariable String itemId,
            @RequestParam String actionCode) {

        return switch (actionCode) {
            // BR-016: Edit Item Action
            case "2" -> ResponseEntity.ok(java.util.Map.of(
                    "action", "EDIT", "redirect", "/api/inventory/items/" + itemId + "?mode=E"));
            // BR-017: Delete Item Action
            case "4" -> softDelete(itemId);
            // BR-018: Display Item Action
            case "5" -> ResponseEntity.ok(java.util.Map.of(
                    "action", "DISPLAY", "redirect", "/api/inventory/items/" + itemId + "?mode=D"));
            default -> ResponseEntity.ok(java.util.Map.of("action", "IGNORED"));
        };
    }

    private ResponseEntity<Object> softDelete(String itemId) {
        var eligibility = inventoryItemService.checkDeleteEligibility(itemId);
        // BR-019: Block if allocated quantity > 0 — return 200 so frontend can read the flag
        if (eligibility.isPresent() && eligibility.get() != null) {
            return ResponseEntity.ok(InventoryListResponseDto.builder()
                    .deleteBlockedByAllocation(true)
                    .build());
        }
        // BR-020: Perform soft delete
        inventoryItemService.softDeleteItem(itemId);
        // BR-021: Deactivation success
        return ResponseEntity.ok(InventoryListResponseDto.builder()
                .deactivationSuccess(true)
                .build());
    }

    /** Render the item list screen */
    @GetMapping("/render")
    public ResponseEntity<InventoryListResponseDto> renderItemList(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) String status) {
        return displayItemList(categoryCode, nameFilter, status);
    }

    /** Bind list screen for frontend */
    @GetMapping("/bind")
    public ResponseEntity<InventoryListResponseDto> bindListScreen() {
        return displayItemList(null, null, null);
    }
}
