package com.trtct004.inventorymanagement.inventoryitemmanagement.validator;

import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.CategoryReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.SupplierReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.WarehouseReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemRequestDto;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemValidatorTest {

    @Mock private InventoryItemRepository repository;
    @Mock private CategoryReferencePort categoryAdapter;
    @Mock private SupplierReferencePort supplierAdapter;
    @Mock private WarehouseReferencePort warehouseAdapter;

    private InventoryItemValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InventoryItemValidator(repository, categoryAdapter, supplierAdapter, warehouseAdapter);
    }

    private InventoryItemRequestDto validRequest() {
        return InventoryItemRequestDto.builder()
                .itemId("ITM00001")
                .itemName("Widget A")
                .itemDescription("Standard widget")
                .categoryCode("ELECT")
                .supplierCode("SUP001")
                .warehouseCode("WH01")
                .quantityOnHand(100)
                .reorderPoint(20)
                .reorderQuantity(50)
                .unitCost(new BigDecimal("10.00"))
                .unitPrice(new BigDecimal("15.00"))
                .build();
    }

    private void stubAllReferencesValid() {
        when(categoryAdapter.fetchCategoryData("ELECT")).thenReturn(Optional.of("Electronics"));
        when(supplierAdapter.fetchSupplierData("SUP001")).thenReturn(Optional.of("Acme Corp"));
        when(warehouseAdapter.fetchWarehouseData("WH01")).thenReturn(Optional.of("Main Warehouse"));
    }

    @Nested
    @DisplayName("validateItem — full validation (BR-036 to BR-048)")
    class FullValidationTests {

        @Test
        @DisplayName("valid request in Add mode returns no errors")
        void validateItem_validAddRequest_noErrors() {
            InventoryItemRequestDto request = validRequest();
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("valid request in Edit mode returns no errors")
        void validateItem_validEditRequest_noErrors() {
            InventoryItemRequestDto request = validRequest();
            stubAllReferencesValid();

            List<String> errors = validator.validateItem(request, "E");

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("accumulates all errors simultaneously (BR-048)")
        void validateItem_multipleErrors_allReported() {
            InventoryItemRequestDto request = InventoryItemRequestDto.builder()
                    .itemId(null)
                    .itemName(null)
                    .categoryCode(null)
                    .supplierCode(null)
                    .warehouseCode(null)
                    .unitCost(null)
                    .unitPrice(null)
                    .build();

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-001", "INV-003", "INV-004", "INV-005", "INV-006");
            assertThat(errors.size()).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("BR-036: Item ID Required (Add mode)")
    class ItemIdRequiredTests {

        @Test
        @DisplayName("INV-001 when itemId is null in Add mode")
        void validateItem_nullItemId_addMode() {
            InventoryItemRequestDto request = validRequest();
            request.setItemId(null);
            stubAllReferencesValid();

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-001");
        }

        @Test
        @DisplayName("INV-001 when itemId is blank in Add mode")
        void validateItem_blankItemId_addMode() {
            InventoryItemRequestDto request = validRequest();
            request.setItemId("   ");
            stubAllReferencesValid();

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-001");
        }

        @Test
        @DisplayName("no INV-001 in Edit mode even with null itemId")
        void validateItem_nullItemId_editMode_noError() {
            InventoryItemRequestDto request = validRequest();
            request.setItemId(null);
            stubAllReferencesValid();

            List<String> errors = validator.validateItem(request, "E");

            assertThat(errors).doesNotContain("INV-001");
        }
    }

    @Nested
    @DisplayName("BR-037: Item ID Uniqueness (Add mode)")
    class ItemIdUniquenessTests {

        @Test
        @DisplayName("INV-002 when itemId already exists")
        void validateItem_duplicateItemId() {
            InventoryItemRequestDto request = validRequest();
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(true);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-002");
        }

        @Test
        @DisplayName("no INV-002 when itemId is new")
        void validateItem_uniqueItemId() {
            InventoryItemRequestDto request = validRequest();
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).doesNotContain("INV-002");
        }
    }

    @Nested
    @DisplayName("BR-038: Item Name Required")
    class ItemNameRequiredTests {

        @Test
        @DisplayName("INV-003 when itemName is null")
        void validateItem_nullItemName() {
            InventoryItemRequestDto request = validRequest();
            request.setItemName(null);
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-003");
        }

        @Test
        @DisplayName("INV-003 when itemName is empty")
        void validateItem_emptyItemName() {
            InventoryItemRequestDto request = validRequest();
            request.setItemName("");
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-003");
        }
    }

    @Nested
    @DisplayName("BR-039: Category Code Validation")
    class CategoryCodeTests {

        @Test
        @DisplayName("INV-004 when category code is null")
        void validateItem_nullCategoryCode() {
            InventoryItemRequestDto request = validRequest();
            request.setCategoryCode(null);
            when(supplierAdapter.fetchSupplierData("SUP001")).thenReturn(Optional.of("Acme"));
            when(warehouseAdapter.fetchWarehouseData("WH01")).thenReturn(Optional.of("Main"));
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-004");
        }

        @Test
        @DisplayName("INV-004 when category code not found in reference")
        void validateItem_invalidCategoryCode() {
            InventoryItemRequestDto request = validRequest();
            request.setCategoryCode("BOGUS");
            when(categoryAdapter.fetchCategoryData("BOGUS")).thenReturn(Optional.empty());
            when(supplierAdapter.fetchSupplierData("SUP001")).thenReturn(Optional.of("Acme"));
            when(warehouseAdapter.fetchWarehouseData("WH01")).thenReturn(Optional.of("Main"));
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-004");
        }
    }

    @Nested
    @DisplayName("BR-041: Supplier Code Validation")
    class SupplierCodeTests {

        @Test
        @DisplayName("INV-005 when supplier code is null")
        void validateItem_nullSupplierCode() {
            InventoryItemRequestDto request = validRequest();
            request.setSupplierCode(null);
            when(categoryAdapter.fetchCategoryData("ELECT")).thenReturn(Optional.of("Electronics"));
            when(warehouseAdapter.fetchWarehouseData("WH01")).thenReturn(Optional.of("Main"));
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-005");
        }

        @Test
        @DisplayName("INV-005 when supplier code not found in reference")
        void validateItem_invalidSupplierCode() {
            InventoryItemRequestDto request = validRequest();
            request.setSupplierCode("NOSUP");
            when(categoryAdapter.fetchCategoryData("ELECT")).thenReturn(Optional.of("Electronics"));
            when(supplierAdapter.fetchSupplierData("NOSUP")).thenReturn(Optional.empty());
            when(warehouseAdapter.fetchWarehouseData("WH01")).thenReturn(Optional.of("Main"));
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-005");
        }
    }

    @Nested
    @DisplayName("BR-043: Warehouse Code Validation")
    class WarehouseCodeTests {

        @Test
        @DisplayName("INV-006 when warehouse code is null")
        void validateItem_nullWarehouseCode() {
            InventoryItemRequestDto request = validRequest();
            request.setWarehouseCode(null);
            when(categoryAdapter.fetchCategoryData("ELECT")).thenReturn(Optional.of("Electronics"));
            when(supplierAdapter.fetchSupplierData("SUP001")).thenReturn(Optional.of("Acme"));
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-006");
        }

        @Test
        @DisplayName("INV-006 when warehouse code not found in reference")
        void validateItem_invalidWarehouseCode() {
            InventoryItemRequestDto request = validRequest();
            request.setWarehouseCode("WH99");
            when(categoryAdapter.fetchCategoryData("ELECT")).thenReturn(Optional.of("Electronics"));
            when(supplierAdapter.fetchSupplierData("SUP001")).thenReturn(Optional.of("Acme"));
            when(warehouseAdapter.fetchWarehouseData("WH99")).thenReturn(Optional.empty());
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-006");
        }
    }

    @Nested
    @DisplayName("BR-045: Price Not Below Cost")
    class PriceBelowCostTests {

        @Test
        @DisplayName("INV-007 when price > 0 and price < cost")
        void validateItem_priceBelowCost() {
            InventoryItemRequestDto request = validRequest();
            request.setUnitPrice(new BigDecimal("5.00"));
            request.setUnitCost(new BigDecimal("10.00"));
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).contains("INV-007");
        }

        @Test
        @DisplayName("no INV-007 when price equals cost")
        void validateItem_priceEqualsCost() {
            InventoryItemRequestDto request = validRequest();
            request.setUnitPrice(new BigDecimal("10.00"));
            request.setUnitCost(new BigDecimal("10.00"));
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).doesNotContain("INV-007");
        }

        @Test
        @DisplayName("no INV-007 when price is zero (BR-045 only triggers when price > 0)")
        void validateItem_zeroPriceNoCostCheck() {
            InventoryItemRequestDto request = validRequest();
            request.setUnitPrice(BigDecimal.ZERO);
            request.setUnitCost(new BigDecimal("10.00"));
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).doesNotContain("INV-007");
        }

        @Test
        @DisplayName("no INV-007 when price or cost is null")
        void validateItem_nullPriceOrCost_noPriceCheck() {
            InventoryItemRequestDto request = validRequest();
            request.setUnitPrice(null);
            request.setUnitCost(null);
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).doesNotContain("INV-007");
        }

        @Test
        @DisplayName("no INV-007 when price > cost")
        void validateItem_priceAboveCost() {
            InventoryItemRequestDto request = validRequest();
            request.setUnitPrice(new BigDecimal("20.00"));
            request.setUnitCost(new BigDecimal("10.00"));
            stubAllReferencesValid();
            when(repository.existsById("ITM00001")).thenReturn(false);

            List<String> errors = validator.validateItem(request, "A");

            assertThat(errors).doesNotContain("INV-007");
        }
    }

    @Nested
    @DisplayName("assignValidationErrorCode — error code mapping")
    class ErrorCodeMappingTests {

        @Test
        void itemIdRequired_mapsTo_INV001() {
            assertThat(validator.assignValidationErrorCode("ITEM_ID_REQUIRED")).isEqualTo("INV-001");
        }

        @Test
        void itemIdDuplicate_mapsTo_INV002() {
            assertThat(validator.assignValidationErrorCode("ITEM_ID_DUPLICATE")).isEqualTo("INV-002");
        }

        @Test
        void itemNameRequired_mapsTo_INV003() {
            assertThat(validator.assignValidationErrorCode("ITEM_NAME_REQUIRED")).isEqualTo("INV-003");
        }

        @Test
        void categoryCodeInvalid_mapsTo_INV004() {
            assertThat(validator.assignValidationErrorCode("CATEGORY_CODE_INVALID")).isEqualTo("INV-004");
        }

        @Test
        void supplierCodeInvalid_mapsTo_INV005() {
            assertThat(validator.assignValidationErrorCode("SUPPLIER_CODE_INVALID")).isEqualTo("INV-005");
        }

        @Test
        void warehouseCodeInvalid_mapsTo_INV006() {
            assertThat(validator.assignValidationErrorCode("WAREHOUSE_CODE_INVALID")).isEqualTo("INV-006");
        }

        @Test
        void priceBelowCost_mapsTo_INV007() {
            assertThat(validator.assignValidationErrorCode("PRICE_BELOW_COST")).isEqualTo("INV-007");
        }

        @Test
        void unknownFailure_mapsTo_INV999() {
            assertThat(validator.assignValidationErrorCode("UNKNOWN_ERROR")).isEqualTo("INV-999");
        }
    }
}
