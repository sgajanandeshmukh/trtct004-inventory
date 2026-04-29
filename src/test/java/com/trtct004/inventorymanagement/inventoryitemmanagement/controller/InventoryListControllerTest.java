package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.InventoryItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryListController.class)
class InventoryListControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private InventoryItemRepository itemRepository;
    @MockBean private InventoryItemService itemService;

    private InventoryItemEntity buildItem(String itemId, String name, InventoryItemStatus status) {
        return InventoryItemEntity.builder()
                .itemId(itemId)
                .itemName(name)
                .itemDescription("Test")
                .categoryCode("ELECT")
                .quantityOnHand(50)
                .quantityAllocated(0)
                .quantityOnOrder(0)
                .reorderPoint(10)
                .reorderQuantity(25)
                .unitCost(new BigDecimal("10.00"))
                .unitPrice(new BigDecimal("15.00"))
                .supplierCode("SUP001")
                .warehouseCode("WH01")
                .status(status)
                .lastUpdatedDate(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/inventory/list — status filter")
    class StatusFilterTests {

        @Test
        @DisplayName("returns all items when no status filter")
        void noStatusFilter_returnsAll() throws Exception {
            InventoryItemEntity active = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            InventoryItemEntity inactive = buildItem("ITM00002", "Gadget", InventoryItemStatus.INACTIVE);
            when(itemRepository.findByFilters(isNull(), isNull(), isNull()))
                    .thenReturn(List.of(active, inactive));

            mockMvc.perform(get("/api/inventory/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(2))
                    .andExpect(jsonPath("$.noRecordsFound").value(false));
        }

        @Test
        @DisplayName("filters by ACTIVE status")
        void activeStatusFilter_returnsActiveOnly() throws Exception {
            InventoryItemEntity active = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findByFilters(isNull(), isNull(), eq("ACTIVE")))
                    .thenReturn(List.of(active));

            mockMvc.perform(get("/api/inventory/list").param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].itemId").value("ITM00001"))
                    .andExpect(jsonPath("$.items[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("filters by INACTIVE status")
        void inactiveStatusFilter_returnsInactiveOnly() throws Exception {
            InventoryItemEntity inactive = buildItem("ITM00002", "Gadget", InventoryItemStatus.INACTIVE);
            when(itemRepository.findByFilters(isNull(), isNull(), eq("INACTIVE")))
                    .thenReturn(List.of(inactive));

            mockMvc.perform(get("/api/inventory/list").param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].status").value("INACTIVE"));
        }

        @Test
        @DisplayName("returns empty list with noRecordsFound flag")
        void statusFilter_noMatch_returnsEmpty() throws Exception {
            when(itemRepository.findByFilters(isNull(), isNull(), eq("INACTIVE")))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/inventory/list").param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(0))
                    .andExpect(jsonPath("$.noRecordsFound").value(true));
        }

        @Test
        @DisplayName("combines category and status filters")
        void combinedFilters_passedToRepository() throws Exception {
            when(itemRepository.findByFilters(eq("ELECT"), isNull(), eq("ACTIVE")))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/inventory/list")
                            .param("categoryCode", "ELECT")
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.noRecordsFound").value(true));

            verify(itemRepository).findByFilters("ELECT", null, "ACTIVE");
        }

        @Test
        @DisplayName("combines all three filters")
        void allThreeFilters_passedToRepository() throws Exception {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findByFilters(eq("ELECT"), eq("Widget"), eq("ACTIVE")))
                    .thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/list")
                            .param("categoryCode", "ELECT")
                            .param("nameFilter", "Widget")
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].itemName").value("Widget"));
        }
    }

    @Nested
    @DisplayName("GET /api/inventory/list — general behavior")
    class GeneralListTests {

        @Test
        @DisplayName("BR-014: caps result to MAX_LIST_SIZE (200)")
        void listCapAt200() throws Exception {
            List<InventoryItemEntity> manyItems = java.util.stream.IntStream.rangeClosed(1, 210)
                    .mapToObj(i -> buildItem(String.format("ITM%05d", i), "Item " + i, InventoryItemStatus.ACTIVE))
                    .toList();
            when(itemRepository.findByFilters(isNull(), isNull(), isNull()))
                    .thenReturn(manyItems);

            mockMvc.perform(get("/api/inventory/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(200));
        }

        @Test
        @DisplayName("returns correct JSON structure")
        void returnsCorrectStructure() throws Exception {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findByFilters(isNull(), isNull(), isNull()))
                    .thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].itemId").value("ITM00001"))
                    .andExpect(jsonPath("$.items[0].itemName").value("Widget"))
                    .andExpect(jsonPath("$.items[0].categoryCode").value("ELECT"))
                    .andExpect(jsonPath("$.items[0].quantityOnHand").value(50))
                    .andExpect(jsonPath("$.items[0].unitPrice").value(15.00))
                    .andExpect(jsonPath("$.items[0].status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/inventory/list/{itemId}/action — DT-002")
    class RowActionTests {

        @Test
        @DisplayName("action '2' returns EDIT redirect")
        void editAction_returnsRedirect() throws Exception {
            mockMvc.perform(post("/api/inventory/list/ITM00001/action")
                            .param("actionCode", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.action").value("EDIT"))
                    .andExpect(jsonPath("$.redirect").value("/api/inventory/items/ITM00001?mode=E"));
        }

        @Test
        @DisplayName("action '5' returns DISPLAY redirect")
        void displayAction_returnsRedirect() throws Exception {
            mockMvc.perform(post("/api/inventory/list/ITM00001/action")
                            .param("actionCode", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.action").value("DISPLAY"))
                    .andExpect(jsonPath("$.redirect").value("/api/inventory/items/ITM00001?mode=D"));
        }

        @Test
        @DisplayName("action '4' with eligible item returns deactivation success")
        void deleteAction_eligible_deactivates() throws Exception {
            when(itemService.checkDeleteEligibility("ITM00001")).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/inventory/list/ITM00001/action")
                            .param("actionCode", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deactivationSuccess").value(true));

            verify(itemService).softDeleteItem("ITM00001");
        }

        @Test
        @DisplayName("action '4' with allocation blocks deletion")
        void deleteAction_allocated_blocked() throws Exception {
            when(itemService.checkDeleteEligibility("ITM00001"))
                    .thenReturn(Optional.of("Item has allocated quantity"));

            mockMvc.perform(post("/api/inventory/list/ITM00001/action")
                            .param("actionCode", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deleteBlockedByAllocation").value(true));

            verify(itemService, never()).softDeleteItem(anyString());
        }

        @Test
        @DisplayName("unknown action returns IGNORED")
        void unknownAction_returnsIgnored() throws Exception {
            mockMvc.perform(post("/api/inventory/list/ITM00001/action")
                            .param("actionCode", "9"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.action").value("IGNORED"));
        }
    }

    @Nested
    @DisplayName("GET /api/inventory/list/render and /bind")
    class AliasEndpointTests {

        @Test
        @DisplayName("/render delegates to displayItemList with status")
        void renderWithStatus() throws Exception {
            when(itemRepository.findByFilters(isNull(), isNull(), eq("ACTIVE")))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/inventory/list/render").param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.noRecordsFound").value(true));

            verify(itemRepository).findByFilters(null, null, "ACTIVE");
        }

        @Test
        @DisplayName("/bind returns unfiltered list")
        void bindReturnsAll() throws Exception {
            when(itemRepository.findByFilters(isNull(), isNull(), isNull()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/inventory/list/bind"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.noRecordsFound").value(true));

            verify(itemRepository).findByFilters(null, null, null);
        }
    }
}
