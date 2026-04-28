package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.ReorderReportRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.ReorderReportResponseDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReorderReportServiceTest {

    @Mock private InventoryItemRepository repository;

    private ReorderReportService service;

    @BeforeEach
    void setUp() {
        service = new ReorderReportService(repository);
    }

    private InventoryItemEntity reorderCandidate(String id, int qtyOnHand, int reorderPoint, BigDecimal unitCost) {
        return InventoryItemEntity.builder()
                .itemId(id)
                .itemName("Item " + id)
                .categoryCode("ELECT")
                .supplierCode("SUP001")
                .warehouseCode("WH01")
                .quantityOnHand(qtyOnHand)
                .quantityAllocated(0)
                .quantityOnOrder(0)
                .reorderPoint(reorderPoint)
                .reorderQuantity(50)
                .unitCost(unitCost)
                .unitPrice(new BigDecimal("20.00"))
                .status(InventoryItemStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("calculateShortage — ALG-002")
    class CalculateShortageTests {

        @Test
        @DisplayName("shortage = reorderPoint - qtyOnHand when positive")
        void calculateShortage_positive() {
            assertThat(service.calculateShortage(100, 30)).isEqualTo(70);
        }

        @Test
        @DisplayName("shortage = 0 when qtyOnHand exceeds reorderPoint (BR-064)")
        void calculateShortage_noShortage() {
            assertThat(service.calculateShortage(20, 50)).isEqualTo(0);
        }

        @Test
        @DisplayName("shortage = 0 when qtyOnHand equals reorderPoint")
        void calculateShortage_exactlyAtReorderPoint() {
            assertThat(service.calculateShortage(50, 50)).isEqualTo(0);
        }

        @Test
        @DisplayName("full shortage when qtyOnHand is zero")
        void calculateShortage_zeroOnHand() {
            assertThat(service.calculateShortage(100, 0)).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("calculateReplenishmentValue — ALG-003")
    class CalculateReplenishmentValueTests {

        @Test
        @DisplayName("value = shortage * unitCost (BR-065)")
        void calculateReplenishmentValue_standard() {
            BigDecimal result = service.calculateReplenishmentValue(10, new BigDecimal("25.50"));
            assertThat(result).isEqualByComparingTo("255.00");
        }

        @Test
        @DisplayName("value = 0 when shortage is zero")
        void calculateReplenishmentValue_zeroShortage() {
            BigDecimal result = service.calculateReplenishmentValue(0, new BigDecimal("25.50"));
            assertThat(result).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("handles fractional unit cost with correct scale")
        void calculateReplenishmentValue_fractionalCost() {
            BigDecimal result = service.calculateReplenishmentValue(3, new BigDecimal("7.33"));
            assertThat(result).isEqualByComparingTo("21.99");
        }
    }

    @Nested
    @DisplayName("evaluateReorderEligibility — DT-007, BR-059/062")
    class EvaluateReorderEligibilityTests {

        @Test
        @DisplayName("eligible: active item, qty below reorder point")
        void evaluateReorderEligibility_eligible() {
            InventoryItemEntity item = reorderCandidate("ITM001", 5, 20, new BigDecimal("10.00"));
            assertThat(service.evaluateReorderEligibility(item)).isTrue();
        }

        @Test
        @DisplayName("eligible: qty exactly at reorder point")
        void evaluateReorderEligibility_atReorderPoint() {
            InventoryItemEntity item = reorderCandidate("ITM001", 20, 20, new BigDecimal("10.00"));
            assertThat(service.evaluateReorderEligibility(item)).isTrue();
        }

        @Test
        @DisplayName("not eligible: inactive item (BR-059)")
        void evaluateReorderEligibility_inactiveItem() {
            InventoryItemEntity item = reorderCandidate("ITM001", 5, 20, new BigDecimal("10.00"));
            item.setStatus(InventoryItemStatus.INACTIVE);
            assertThat(service.evaluateReorderEligibility(item)).isFalse();
        }

        @Test
        @DisplayName("not eligible: reorder point is 0 (BR-062)")
        void evaluateReorderEligibility_zeroReorderPoint() {
            InventoryItemEntity item = reorderCandidate("ITM001", 5, 0, new BigDecimal("10.00"));
            item.setReorderPoint(0);
            assertThat(service.evaluateReorderEligibility(item)).isFalse();
        }

        @Test
        @DisplayName("not eligible: reorder point is null")
        void evaluateReorderEligibility_nullReorderPoint() {
            InventoryItemEntity item = reorderCandidate("ITM001", 5, 20, new BigDecimal("10.00"));
            item.setReorderPoint(null);
            assertThat(service.evaluateReorderEligibility(item)).isFalse();
        }

        @Test
        @DisplayName("not eligible: qty above reorder point")
        void evaluateReorderEligibility_aboveReorderPoint() {
            InventoryItemEntity item = reorderCandidate("ITM001", 50, 20, new BigDecimal("10.00"));
            assertThat(service.evaluateReorderEligibility(item)).isFalse();
        }

        @Test
        @DisplayName("not eligible: negative reorder point")
        void evaluateReorderEligibility_negativeReorderPoint() {
            InventoryItemEntity item = reorderCandidate("ITM001", 5, -1, new BigDecimal("10.00"));
            item.setReorderPoint(-1);
            assertThat(service.evaluateReorderEligibility(item)).isFalse();
        }
    }

    @Nested
    @DisplayName("aggregateReorderData — BR-055 to BR-070")
    class AggregateReorderDataTests {

        @Test
        @DisplayName("returns correct items with shortage and value calculations")
        void aggregateReorderData_standardScenario() {
            InventoryItemEntity item1 = reorderCandidate("ITM001", 5, 20, new BigDecimal("10.00"));
            InventoryItemEntity item2 = reorderCandidate("ITM002", 10, 30, new BigDecimal("5.00"));
            when(repository.findReorderItems(null, null)).thenReturn(List.of(item1, item2));

            ReorderReportResponseDto result = service.aggregateReorderData(new ReorderReportRequestDto(null, null));

            assertThat(result.getTotalReorderItemCount()).isEqualTo(2);
            assertThat(result.getItems()).hasSize(2);
            assertThat(result.getItems().get(0).getShortageQuantity()).isEqualTo(15);
            assertThat(result.getItems().get(0).getItemReplenishmentValue()).isEqualByComparingTo("150.00");
            assertThat(result.getItems().get(1).getShortageQuantity()).isEqualTo(20);
            assertThat(result.getItems().get(1).getItemReplenishmentValue()).isEqualByComparingTo("100.00");
            assertThat(result.getTotalReplenishmentValue()).isEqualByComparingTo("250.00");
            assertThat(result.isNoItemsFound()).isFalse();
        }

        @Test
        @DisplayName("returns empty report when no candidates found")
        void aggregateReorderData_noCandidates() {
            when(repository.findReorderItems(any(), any())).thenReturn(Collections.emptyList());

            ReorderReportResponseDto result = service.aggregateReorderData(new ReorderReportRequestDto("WH01", "ELECT"));

            assertThat(result.getTotalReorderItemCount()).isEqualTo(0);
            assertThat(result.getItems()).isEmpty();
            assertThat(result.isNoItemsFound()).isTrue();
            assertThat(result.getTotalReplenishmentValue()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("caps results at MAX_LIST_SIZE = 200 (BR-068)")
        void aggregateReorderData_capsAt200() {
            List<InventoryItemEntity> items = new ArrayList<>();
            for (int i = 0; i < 210; i++) {
                items.add(reorderCandidate(String.format("ITM%04d", i), 5, 20, new BigDecimal("1.00")));
            }
            when(repository.findReorderItems(null, null)).thenReturn(items);

            ReorderReportResponseDto result = service.aggregateReorderData(new ReorderReportRequestDto(null, null));

            assertThat(result.getTotalReorderItemCount()).isEqualTo(200);
            assertThat(result.getItems()).hasSize(200);
        }

        @Test
        @DisplayName("skips ineligible items among candidates")
        void aggregateReorderData_skipsIneligible() {
            InventoryItemEntity eligible = reorderCandidate("ITM001", 5, 20, new BigDecimal("10.00"));
            InventoryItemEntity ineligible = reorderCandidate("ITM002", 50, 20, new BigDecimal("10.00"));
            when(repository.findReorderItems(null, null)).thenReturn(List.of(eligible, ineligible));

            ReorderReportResponseDto result = service.aggregateReorderData(new ReorderReportRequestDto(null, null));

            assertThat(result.getTotalReorderItemCount()).isEqualTo(1);
            assertThat(result.getItems().get(0).getItemId()).isEqualTo("ITM001");
        }

        @Test
        @DisplayName("passes warehouse and category filters to repository")
        void aggregateReorderData_passesFilters() {
            when(repository.findReorderItems("WH01", "ELECT")).thenReturn(Collections.emptyList());

            service.aggregateReorderData(new ReorderReportRequestDto("WH01", "ELECT"));

            org.mockito.Mockito.verify(repository).findReorderItems("WH01", "ELECT");
        }
    }
}
