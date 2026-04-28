package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.ReorderReportRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.ReorderReportResponseDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.util.InventoryConstants;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * TRC-022, TRC-023, TRC-030, TRC-031, TRC-049: Reorder Report Service
 * ALG-002: Quantity Short Calculation
 * ALG-003: Item Replenishment Value Calculation
 * DT-007: Item Inclusion Criteria for Reorder Report
 * DT-008: Report Refresh Triggers
 *
 * BR-049 to BR-058: Reorder eligibility
 * BR-059 to BR-070: Aggregation
 */
@Service
public class ReorderReportService {

    private final InventoryItemRepository inventoryItemRepository;

    public ReorderReportService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Aggregate reorder data for all qualifying items.
     * ALG-002/003, BR-055 to BR-070, DT-007.
     *
     * @param request filter parameters (warehouse, category)
     * @return aggregated reorder report response
     */
    public ReorderReportResponseDto aggregateReorderData(ReorderReportRequestDto request) {
        List<InventoryItemEntity> candidates = inventoryItemRepository.findReorderItems(
                request.getWarehouseCode(),
                request.getCategoryCode());

        List<ReorderReportResponseDto.ReorderItem> items = new ArrayList<>();
        BigDecimal totalReplenishmentValue = BigDecimal.ZERO;
        int totalCount = 0;

        for (InventoryItemEntity item : candidates) {
            // BR-068: Maximum report size cap (HV-027: 200)
            if (totalCount >= InventoryConstants.MAX_LIST_SIZE) {
                break;
            }

            // BR-062: Only include items where reorder point > 0 and qty on hand <= reorder point
            if (!evaluateReorderEligibility(item)) {
                continue;
            }

            // BR-063/064: ALG-002 — Calculate Quantity Short
            int shortageQty = calculateShortage(item.getReorderPoint(), item.getQuantityOnHand());

            // BR-065: ALG-003 — Calculate Item Replenishment Value
            BigDecimal itemValue = calculateReplenishmentValue(shortageQty, item.getUnitCost());

            // BR-066: Accumulate Total Replenishment Value
            totalReplenishmentValue = totalReplenishmentValue.add(itemValue);

            // BR-067: Accumulate Total Reorder Item Count
            totalCount++;

            items.add(ReorderReportResponseDto.ReorderItem.builder()
                    .itemId(item.getItemId())
                    .itemName(item.getItemName())
                    .categoryCode(item.getCategoryCode())
                    .quantityOnHand(item.getQuantityOnHand())
                    .reorderPoint(item.getReorderPoint())
                    .shortageQuantity(shortageQty)
                    .supplierCode(item.getSupplierCode())
                    .itemReplenishmentValue(itemValue)
                    .build());
        }

        // BR-069: Display Report Summary Totals
        return ReorderReportResponseDto.builder()
                .items(items)
                .totalReorderItemCount(totalCount)
                .totalReplenishmentValue(totalReplenishmentValue)
                .noItemsFound(totalCount == 0)
                .build();
    }

    /**
     * DT-007: Evaluate whether an item qualifies for the reorder report.
     * BR-059: Exclude inactive items.
     * BR-062: Include only if qty on hand <= reorder point AND reorder point > 0.
     */
    public boolean evaluateReorderEligibility(InventoryItemEntity item) {
        // BR-059: Must be active
        if (!item.isActive()) {
            return false;
        }
        // BR-062: Reorder point must be defined and positive
        if (item.getReorderPoint() == null || item.getReorderPoint() <= 0) {
            return false;
        }
        // BR-062: Qty on hand must be at or below reorder point
        return item.getQuantityOnHand() <= item.getReorderPoint();
    }

    /**
     * ALG-002: Calculate shortage quantity.
     * BR-063: shortage = reorder point − qty on hand
     * BR-064: floor to zero if negative
     *
     * @param reorderPoint   minimum stock threshold
     * @param quantityOnHand current stock count
     * @return shortage quantity (>= 0)
     */
    public int calculateShortage(int reorderPoint, int quantityOnHand) {
        return Math.max(0, reorderPoint - quantityOnHand);
    }

    /**
     * ALG-003: Calculate item replenishment value.
     * BR-065: item value = shortage quantity × unit cost
     *
     * @param shortageQty shortage quantity
     * @param unitCost    purchase cost per unit
     * @return estimated replenishment spend for this item
     */
    public BigDecimal calculateReplenishmentValue(int shortageQty, BigDecimal unitCost) {
        return BigDecimal.valueOf(shortageQty)
                .multiply(unitCost)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Format reorder report for display.
     */
    public ReorderReportResponseDto formatReorderReport(ReorderReportRequestDto request) {
        return aggregateReorderData(request);
    }
}
