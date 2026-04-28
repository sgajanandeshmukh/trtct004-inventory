package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DashboardService {

    private final InventoryItemRepository itemRepository;
    private final InventoryTransactionService transactionService;

    public DashboardService(InventoryItemRepository itemRepository,
                            InventoryTransactionService transactionService) {
        this.itemRepository = itemRepository;
        this.transactionService = transactionService;
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        long totalActive = itemRepository.countByStatus(InventoryItemStatus.ACTIVE);
        long totalInactive = itemRepository.countByStatus(InventoryItemStatus.INACTIVE);
        long totalItems = totalActive + totalInactive
                + itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)
                + itemRepository.countByStatus(InventoryItemStatus.DELETED);
        long belowReorder = itemRepository.countItemsBelowReorderPoint();
        long outOfStock = itemRepository.countOutOfStockItems();
        BigDecimal totalValue = itemRepository.calculateTotalInventoryValue();
        BigDecimal avgMargin = itemRepository.calculateAverageMargin();

        // KPI cards
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalItems", totalItems);
        kpis.put("activeItems", totalActive);
        kpis.put("inactiveItems", totalInactive);
        kpis.put("belowReorderPoint", belowReorder);
        kpis.put("outOfStock", outOfStock);
        kpis.put("totalInventoryValue", totalValue != null ? totalValue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        kpis.put("averageMarginPercent", avgMargin != null ? avgMargin.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Inventory health score (0-100)
        int healthScore = calculateHealthScore(totalActive, outOfStock, belowReorder);
        kpis.put("inventoryHealthScore", healthScore);

        dashboard.put("kpis", kpis);

        // Category distribution
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Object[] row : itemRepository.getCategoryDistribution()) {
            Map<String, Object> cat = new LinkedHashMap<>();
            cat.put("categoryCode", row[0]);
            cat.put("itemCount", row[1]);
            cat.put("totalValue", row[2]);
            categories.add(cat);
        }
        dashboard.put("categoryDistribution", categories);

        // Warehouse distribution
        List<Map<String, Object>> warehouses = new ArrayList<>();
        for (Object[] row : itemRepository.getWarehouseDistribution()) {
            Map<String, Object> wh = new LinkedHashMap<>();
            wh.put("warehouseCode", row[0]);
            wh.put("itemCount", row[1]);
            wh.put("totalQuantity", row[2]);
            warehouses.add(wh);
        }
        dashboard.put("warehouseDistribution", warehouses);

        // Status distribution
        List<Map<String, Object>> statuses = new ArrayList<>();
        for (Object[] row : itemRepository.getStatusDistribution()) {
            Map<String, Object> st = new LinkedHashMap<>();
            st.put("status", row[0] != null ? row[0].toString() : "UNKNOWN");
            st.put("count", row[1]);
            statuses.add(st);
        }
        dashboard.put("statusDistribution", statuses);

        // Recent activity (last 20 transactions)
        List<InventoryTransactionEntity> recent = transactionService.getRecentTransactions(20);
        dashboard.put("recentActivity", recent);
        dashboard.put("recentActivityCount", recent.size());

        return dashboard;
    }

    private int calculateHealthScore(long totalActive, long outOfStock, long belowReorder) {
        if (totalActive == 0) return 100;
        double outOfStockPenalty = ((double) outOfStock / totalActive) * 50;
        double reorderPenalty = ((double) belowReorder / totalActive) * 30;
        int score = (int) Math.max(0, Math.min(100, 100 - outOfStockPenalty - reorderPenalty));
        return score;
    }
}
