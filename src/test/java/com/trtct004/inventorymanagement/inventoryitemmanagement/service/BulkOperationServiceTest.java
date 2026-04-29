package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationServiceTest {

    @Mock private InventoryItemRepository itemRepository;
    @Mock private InventoryTransactionService transactionService;

    private BulkOperationService service;

    @BeforeEach
    void setUp() {
        service = new BulkOperationService(itemRepository, transactionService);
    }

    private InventoryItemEntity buildItem(String itemId, String name, InventoryItemStatus status) {
        return InventoryItemEntity.builder()
                .itemId(itemId)
                .itemName(name)
                .itemDescription("Desc")
                .categoryCode("ELECT")
                .quantityOnHand(100)
                .quantityAllocated(0)
                .quantityOnOrder(0)
                .reorderPoint(20)
                .reorderQuantity(50)
                .unitCost(new BigDecimal("10.00"))
                .unitPrice(new BigDecimal("15.00"))
                .supplierCode("SUP001")
                .warehouseCode("WH01")
                .status(status)
                .lastUpdatedDate(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("exportToCsv — FR-008 CSV export")
    class ExportCsvTests {

        @Test
        @DisplayName("generates CSV header and data rows")
        void exportToCsv_generatesHeaderAndRows() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findByFilters(null, null, null)).thenReturn(List.of(item));

            String csv = service.exportToCsv(null, null);

            assertThat(csv).startsWith("Item ID,Item Name,");
            assertThat(csv).contains("ITM00001");
            assertThat(csv).contains("Widget");
        }

        @Test
        @DisplayName("returns header only when no items")
        void exportToCsv_noItems_headerOnly() {
            when(itemRepository.findByFilters(null, null, null)).thenReturn(List.of());

            String csv = service.exportToCsv(null, null);

            String[] lines = csv.split("\n");
            assertThat(lines).hasSize(1);
            assertThat(lines[0]).startsWith("Item ID,");
        }

        @Test
        @DisplayName("passes category and name filters to repository")
        void exportToCsv_passesFilters() {
            when(itemRepository.findByFilters("ELECT", "widget", null)).thenReturn(List.of());

            service.exportToCsv("ELECT", "widget");

            verify(itemRepository).findByFilters("ELECT", "widget", null);
        }

        @Test
        @DisplayName("escapes commas in field values")
        void exportToCsv_escapesCommas() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget, Large", InventoryItemStatus.ACTIVE);
            when(itemRepository.findByFilters(null, null, null)).thenReturn(List.of(item));

            String csv = service.exportToCsv(null, null);

            assertThat(csv).contains("\"Widget, Large\"");
        }

        @Test
        @DisplayName("handles null description field")
        void exportToCsv_nullDescription() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            item.setItemDescription(null);
            when(itemRepository.findByFilters(null, null, null)).thenReturn(List.of(item));

            String csv = service.exportToCsv(null, null);

            assertThat(csv).contains("ITM00001");
        }
    }

    @Nested
    @DisplayName("bulkStatusUpdate — FR-008 mass status change")
    class BulkStatusUpdateTests {

        @Test
        @DisplayName("deactivates active items successfully")
        void bulkStatusUpdate_deactivateActive_succeeds() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findById("ITM00001")).thenReturn(Optional.of(item));
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> result = service.bulkStatusUpdate(List.of("ITM00001"), "INACTIVE");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("successCount")).isEqualTo(1);
            assertThat(result.get("failCount")).isEqualTo(0);
            verify(transactionService).logStatusChange(eq("ITM00001"), eq("ACTIVE"), eq("INACTIVE"));
        }

        @Test
        @DisplayName("reactivates inactive items successfully")
        void bulkStatusUpdate_reactivateInactive_succeeds() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.INACTIVE);
            when(itemRepository.findById("ITM00001")).thenReturn(Optional.of(item));
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> result = service.bulkStatusUpdate(List.of("ITM00001"), "ACTIVE");

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("successCount")).isEqualTo(1);
        }

        @Test
        @DisplayName("fails for item not found")
        void bulkStatusUpdate_itemNotFound_fails() {
            when(itemRepository.findById("MISSING")).thenReturn(Optional.empty());

            Map<String, Object> result = service.bulkStatusUpdate(List.of("MISSING"), "INACTIVE");

            assertThat(result.get("failCount")).isEqualTo(1);
            @SuppressWarnings("unchecked")
            List<String> failures = (List<String>) result.get("failures");
            assertThat(failures.get(0)).contains("not found");
        }

        @Test
        @DisplayName("fails for invalid status string")
        void bulkStatusUpdate_invalidStatus() {
            Map<String, Object> result = service.bulkStatusUpdate(List.of("ITM00001"), "BOGUS");

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("error")).asString().contains("Invalid target status");
        }

        @Test
        @DisplayName("fails for illegal transition (ACTIVE -> ACTIVE)")
        void bulkStatusUpdate_illegalTransition_fails() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findById("ITM00001")).thenReturn(Optional.of(item));

            Map<String, Object> result = service.bulkStatusUpdate(List.of("ITM00001"), "ACTIVE");

            assertThat(result.get("failCount")).isEqualTo(1);
            @SuppressWarnings("unchecked")
            List<String> failures = (List<String>) result.get("failures");
            assertThat(failures.get(0)).contains("illegal transition");
        }

        @Test
        @DisplayName("processes multiple items with mixed results")
        void bulkStatusUpdate_mixedResults() {
            InventoryItemEntity active = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findById("ITM00001")).thenReturn(Optional.of(active));
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(itemRepository.findById("MISSING")).thenReturn(Optional.empty());

            Map<String, Object> result = service.bulkStatusUpdate(List.of("ITM00001", "MISSING"), "INACTIVE");

            assertThat(result.get("successCount")).isEqualTo(1);
            assertThat(result.get("failCount")).isEqualTo(1);
            assertThat(result.get("totalProcessed")).isEqualTo(2);
        }

        @Test
        @DisplayName("trims whitespace from item IDs")
        void bulkStatusUpdate_trimsItemIds() {
            InventoryItemEntity item = buildItem("ITM00001", "Widget", InventoryItemStatus.ACTIVE);
            when(itemRepository.findById("ITM00001")).thenReturn(Optional.of(item));
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.bulkStatusUpdate(List.of(" ITM00001 "), "INACTIVE");

            verify(itemRepository).findById("ITM00001");
        }
    }

    @Nested
    @DisplayName("importFromCsv — FR-008 CSV import")
    class ImportFromCsvTests {

        @Test
        @DisplayName("creates new item when ID does not exist")
        void importFromCsv_newItem_creates() {
            String csv = "Item ID,Item Name,Description,Category,Qty,Alloc,Order,Reorder Pt,Reorder Qty,Cost,Price,Supplier,Warehouse\n"
                       + "ITM00099,Test Item,Test desc,ELECT,100,0,0,20,50,10.00,15.00,SUP001,WH01\n";
            when(itemRepository.findById("ITM00099")).thenReturn(Optional.empty());
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> result = service.importFromCsv(csv);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("created")).isEqualTo(1);
            assertThat(result.get("updated")).isEqualTo(0);
            verify(transactionService).logItemCreated(eq("ITM00099"), eq(100));
        }

        @Test
        @DisplayName("updates existing item when ID exists")
        void importFromCsv_existingItem_updates() {
            String csv = "Header line\n"
                       + "ITM00001,Updated Name,Updated desc,MECH,200,0,0,30,60,12.00,18.00,SUP002,WH02\n";
            InventoryItemEntity existing = buildItem("ITM00001", "Old Name", InventoryItemStatus.ACTIVE);
            when(itemRepository.findById("ITM00001")).thenReturn(Optional.of(existing));
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> result = service.importFromCsv(csv);

            assertThat(result.get("updated")).isEqualTo(1);
            assertThat(result.get("created")).isEqualTo(0);
            assertThat(existing.getItemName()).isEqualTo("Updated Name");
            verify(transactionService).logItemUpdated(eq("ITM00001"), eq(200));
        }

        @Test
        @DisplayName("fails line with insufficient fields")
        void importFromCsv_insufficientFields_failsLine() {
            String csv = "Header\nITM00001,Name Only\n";

            Map<String, Object> result = service.importFromCsv(csv);

            assertThat(result.get("failed")).isEqualTo(1);
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) result.get("errors");
            assertThat(errors.get(0)).contains("insufficient fields");
        }

        @Test
        @DisplayName("returns error for empty CSV")
        void importFromCsv_emptyCsv() {
            Map<String, Object> result = service.importFromCsv("");

            assertThat(result.get("success")).isEqualTo(false);
        }

        @Test
        @DisplayName("skips blank lines")
        void importFromCsv_skipsBlankLines() {
            String csv = "Header\n\n\nITM00099,Test,Desc,ELECT,10,0,0,5,20,1.00,2.00,SUP001,WH01\n\n";
            when(itemRepository.findById("ITM00099")).thenReturn(Optional.empty());
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> result = service.importFromCsv(csv);

            assertThat(result.get("created")).isEqualTo(1);
        }

        @Test
        @DisplayName("handles non-numeric quantity gracefully (defaults to 0)")
        void importFromCsv_nonNumericQty_defaultsToZero() {
            String csv = "Header\nITM00099,Test,Desc,ELECT,abc,0,0,5,20,1.00,2.00,SUP001,WH01\n";
            when(itemRepository.findById("ITM00099")).thenReturn(Optional.empty());
            when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> result = service.importFromCsv(csv);

            assertThat(result.get("created")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getImportTemplate — FR-008 template download")
    class ImportTemplateTests {

        @Test
        @DisplayName("returns CSV with header and sample row")
        void getImportTemplate_headerAndSample() {
            String template = service.getImportTemplate();

            assertThat(template).contains("Item ID,Item Name");
            assertThat(template).contains("ITM00099");
            String[] lines = template.split("\n");
            assertThat(lines.length).isGreaterThanOrEqualTo(2);
        }
    }
}
