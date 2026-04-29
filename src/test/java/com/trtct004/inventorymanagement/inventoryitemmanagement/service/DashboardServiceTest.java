package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private InventoryItemRepository itemRepository;
    @Mock private InventoryTransactionService transactionService;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(itemRepository, transactionService);
    }

    private void stubRepositoryDefaults() {
        when(itemRepository.countByStatus(InventoryItemStatus.ACTIVE)).thenReturn(10L);
        when(itemRepository.countByStatus(InventoryItemStatus.INACTIVE)).thenReturn(2L);
        when(itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)).thenReturn(0L);
        when(itemRepository.countByStatus(InventoryItemStatus.DELETED)).thenReturn(0L);
        when(itemRepository.countItemsBelowReorderPoint()).thenReturn(3L);
        when(itemRepository.countOutOfStockItems()).thenReturn(1L);
        when(itemRepository.calculateTotalInventoryValue()).thenReturn(new BigDecimal("5000.00"));
        when(itemRepository.calculateAverageMargin()).thenReturn(new BigDecimal("40.5678"));
        when(itemRepository.getCategoryDistribution()).thenReturn(List.of());
        when(itemRepository.getWarehouseDistribution()).thenReturn(List.of());
        when(itemRepository.getStatusDistribution()).thenReturn(List.of());
        when(transactionService.getRecentTransactions(20)).thenReturn(List.of());
    }

    @Nested
    @DisplayName("getDashboardData — FR-001 KPIs")
    class KpiTests {

        @Test
        @DisplayName("returns all KPI fields with correct values")
        void getDashboardData_returnsKpis() {
            stubRepositoryDefaults();

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("totalItems")).isEqualTo(12L);
            assertThat(kpis.get("activeItems")).isEqualTo(10L);
            assertThat(kpis.get("inactiveItems")).isEqualTo(2L);
            assertThat(kpis.get("belowReorderPoint")).isEqualTo(3L);
            assertThat(kpis.get("outOfStock")).isEqualTo(1L);
            assertThat(kpis.get("totalInventoryValue")).isEqualTo(new BigDecimal("5000.00"));
            assertThat(kpis.get("averageMarginPercent")).isEqualTo(new BigDecimal("40.57"));
        }

        @Test
        @DisplayName("handles null totalInventoryValue from repository")
        void getDashboardData_nullInventoryValue_defaultsToZero() {
            stubRepositoryDefaults();
            when(itemRepository.calculateTotalInventoryValue()).thenReturn(null);

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("totalInventoryValue")).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("handles null averageMargin from repository")
        void getDashboardData_nullAverageMargin_defaultsToZero() {
            stubRepositoryDefaults();
            when(itemRepository.calculateAverageMargin()).thenReturn(null);

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("averageMarginPercent")).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("totalItems includes all status types")
        void getDashboardData_totalItemsIncludesAllStatuses() {
            when(itemRepository.countByStatus(InventoryItemStatus.ACTIVE)).thenReturn(5L);
            when(itemRepository.countByStatus(InventoryItemStatus.INACTIVE)).thenReturn(3L);
            when(itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)).thenReturn(1L);
            when(itemRepository.countByStatus(InventoryItemStatus.DELETED)).thenReturn(2L);
            when(itemRepository.countItemsBelowReorderPoint()).thenReturn(0L);
            when(itemRepository.countOutOfStockItems()).thenReturn(0L);
            when(itemRepository.calculateTotalInventoryValue()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.calculateAverageMargin()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.getCategoryDistribution()).thenReturn(List.of());
            when(itemRepository.getWarehouseDistribution()).thenReturn(List.of());
            when(itemRepository.getStatusDistribution()).thenReturn(List.of());
            when(transactionService.getRecentTransactions(20)).thenReturn(List.of());

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("totalItems")).isEqualTo(11L);
        }
    }

    @Nested
    @DisplayName("Health Score — FR-001 algorithm")
    class HealthScoreTests {

        @Test
        @DisplayName("perfect score when no issues")
        void healthScore_noIssues_returns100() {
            when(itemRepository.countByStatus(InventoryItemStatus.ACTIVE)).thenReturn(20L);
            when(itemRepository.countByStatus(InventoryItemStatus.INACTIVE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.DELETED)).thenReturn(0L);
            when(itemRepository.countItemsBelowReorderPoint()).thenReturn(0L);
            when(itemRepository.countOutOfStockItems()).thenReturn(0L);
            when(itemRepository.calculateTotalInventoryValue()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.calculateAverageMargin()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.getCategoryDistribution()).thenReturn(List.of());
            when(itemRepository.getWarehouseDistribution()).thenReturn(List.of());
            when(itemRepository.getStatusDistribution()).thenReturn(List.of());
            when(transactionService.getRecentTransactions(20)).thenReturn(List.of());

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("inventoryHealthScore")).isEqualTo(100);
        }

        @Test
        @DisplayName("score 100 when zero active items (no penalty possible)")
        void healthScore_zeroActiveItems_returns100() {
            when(itemRepository.countByStatus(InventoryItemStatus.ACTIVE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.INACTIVE)).thenReturn(5L);
            when(itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.DELETED)).thenReturn(0L);
            when(itemRepository.countItemsBelowReorderPoint()).thenReturn(0L);
            when(itemRepository.countOutOfStockItems()).thenReturn(0L);
            when(itemRepository.calculateTotalInventoryValue()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.calculateAverageMargin()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.getCategoryDistribution()).thenReturn(List.of());
            when(itemRepository.getWarehouseDistribution()).thenReturn(List.of());
            when(itemRepository.getStatusDistribution()).thenReturn(List.of());
            when(transactionService.getRecentTransactions(20)).thenReturn(List.of());

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("inventoryHealthScore")).isEqualTo(100);
        }

        @Test
        @DisplayName("all out of stock yields score of 50 (50% penalty)")
        void healthScore_allOutOfStock() {
            when(itemRepository.countByStatus(InventoryItemStatus.ACTIVE)).thenReturn(10L);
            when(itemRepository.countByStatus(InventoryItemStatus.INACTIVE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.DELETED)).thenReturn(0L);
            when(itemRepository.countItemsBelowReorderPoint()).thenReturn(0L);
            when(itemRepository.countOutOfStockItems()).thenReturn(10L);
            when(itemRepository.calculateTotalInventoryValue()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.calculateAverageMargin()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.getCategoryDistribution()).thenReturn(List.of());
            when(itemRepository.getWarehouseDistribution()).thenReturn(List.of());
            when(itemRepository.getStatusDistribution()).thenReturn(List.of());
            when(transactionService.getRecentTransactions(20)).thenReturn(List.of());

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            assertThat(kpis.get("inventoryHealthScore")).isEqualTo(50);
        }

        @Test
        @DisplayName("score never goes below 0")
        void healthScore_neverBelowZero() {
            when(itemRepository.countByStatus(InventoryItemStatus.ACTIVE)).thenReturn(5L);
            when(itemRepository.countByStatus(InventoryItemStatus.INACTIVE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.PENDING_DELETE)).thenReturn(0L);
            when(itemRepository.countByStatus(InventoryItemStatus.DELETED)).thenReturn(0L);
            when(itemRepository.countItemsBelowReorderPoint()).thenReturn(5L);
            when(itemRepository.countOutOfStockItems()).thenReturn(5L);
            when(itemRepository.calculateTotalInventoryValue()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.calculateAverageMargin()).thenReturn(BigDecimal.ZERO);
            when(itemRepository.getCategoryDistribution()).thenReturn(List.of());
            when(itemRepository.getWarehouseDistribution()).thenReturn(List.of());
            when(itemRepository.getStatusDistribution()).thenReturn(List.of());
            when(transactionService.getRecentTransactions(20)).thenReturn(List.of());

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            Map<String, Object> kpis = (Map<String, Object>) result.get("kpis");
            int score = (int) kpis.get("inventoryHealthScore");
            assertThat(score).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getDashboardData — distributions")
    class DistributionTests {

        @Test
        @DisplayName("maps category distribution rows correctly")
        void getDashboardData_categoryDistribution() {
            stubRepositoryDefaults();
            List<Object[]> catRows = new java.util.ArrayList<>();
            catRows.add(new Object[]{"ELECT", 5L, new BigDecimal("1000.00")});
            catRows.add(new Object[]{"MECH", 3L, new BigDecimal("750.00")});
            when(itemRepository.getCategoryDistribution()).thenReturn(catRows);

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> categories = (List<Map<String, Object>>) result.get("categoryDistribution");
            assertThat(categories).hasSize(2);
            assertThat(categories.get(0).get("categoryCode")).isEqualTo("ELECT");
            assertThat(categories.get(0).get("itemCount")).isEqualTo(5L);
            assertThat(categories.get(0).get("totalValue")).isEqualTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("maps warehouse distribution rows correctly")
        void getDashboardData_warehouseDistribution() {
            stubRepositoryDefaults();
            List<Object[]> whRows = new java.util.ArrayList<>();
            whRows.add(new Object[]{"WH01", 8L, 500L});
            when(itemRepository.getWarehouseDistribution()).thenReturn(whRows);

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> warehouses = (List<Map<String, Object>>) result.get("warehouseDistribution");
            assertThat(warehouses).hasSize(1);
            assertThat(warehouses.get(0).get("warehouseCode")).isEqualTo("WH01");
        }

        @Test
        @DisplayName("maps status distribution with null status handling")
        void getDashboardData_statusDistribution_nullStatus() {
            stubRepositoryDefaults();
            List<Object[]> statusRows = new java.util.ArrayList<>();
            statusRows.add(new Object[]{InventoryItemStatus.ACTIVE, 10L});
            statusRows.add(new Object[]{null, 1L});
            when(itemRepository.getStatusDistribution()).thenReturn(statusRows);

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> statuses = (List<Map<String, Object>>) result.get("statusDistribution");
            assertThat(statuses).hasSize(2);
            assertThat(statuses.get(1).get("status")).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("empty distributions return empty lists")
        void getDashboardData_emptyDistributions() {
            stubRepositoryDefaults();

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            List<?> categories = (List<?>) result.get("categoryDistribution");
            assertThat(categories).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDashboardData — recent activity")
    class RecentActivityTests {

        @Test
        @DisplayName("includes recent transactions and count")
        void getDashboardData_recentActivity() {
            stubRepositoryDefaults();
            InventoryTransactionEntity txn = InventoryTransactionEntity.builder()
                    .sequenceId(1L).itemId("ITM00001").transactionType("CR").build();
            when(transactionService.getRecentTransactions(20)).thenReturn(List.of(txn));

            Map<String, Object> result = service.getDashboardData();

            @SuppressWarnings("unchecked")
            List<?> activity = (List<?>) result.get("recentActivity");
            assertThat(activity).hasSize(1);
            assertThat(result.get("recentActivityCount")).isEqualTo(1);
        }

        @Test
        @DisplayName("empty activity returns 0 count")
        void getDashboardData_noRecentActivity() {
            stubRepositoryDefaults();

            Map<String, Object> result = service.getDashboardData();

            assertThat(result.get("recentActivityCount")).isEqualTo(0);
        }
    }
}
