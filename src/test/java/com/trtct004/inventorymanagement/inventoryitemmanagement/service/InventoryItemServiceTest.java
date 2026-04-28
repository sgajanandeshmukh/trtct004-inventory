package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.CategoryReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.SupplierReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.WarehouseReferencePort;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryItemRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.validator.InventoryItemValidatorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceTest {

    @Mock private InventoryItemRepository repository;
    @Mock private InventoryItemValidatorPort validator;
    @Mock private CategoryReferencePort categoryAdapter;
    @Mock private SupplierReferencePort supplierAdapter;
    @Mock private WarehouseReferencePort warehouseAdapter;

    private InventoryItemService service;

    @BeforeEach
    void setUp() {
        service = new InventoryItemService(repository, validator, categoryAdapter, supplierAdapter, warehouseAdapter);
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

    private InventoryItemEntity activeEntity(String itemId) {
        return InventoryItemEntity.builder()
                .itemId(itemId)
                .itemName("Widget A")
                .categoryCode("ELECT")
                .supplierCode("SUP001")
                .warehouseCode("WH01")
                .quantityOnHand(100)
                .quantityAllocated(0)
                .quantityOnOrder(0)
                .reorderPoint(20)
                .reorderQuantity(50)
                .unitCost(new BigDecimal("10.00"))
                .unitPrice(new BigDecimal("15.00"))
                .status(InventoryItemStatus.ACTIVE)
                .lastUpdatedDate(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("saveItem — BR-041 to BR-048, DT-007")
    class SaveItemTests {

        @Test
        @DisplayName("returns failure when validation errors exist (BR-048)")
        void saveItem_validationErrors_returnsFailure() {
            InventoryItemRequestDto request = validRequest();
            when(validator.validateItem(request, "A")).thenReturn(List.of("INV-001", "INV-003"));

            InventoryItemService.SaveResult result = service.saveItem(request, "A");

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).containsExactly("INV-001", "INV-003");
            assertThat(result.entity()).isNull();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("routes to createItem in Add mode when valid")
        void saveItem_addMode_createsItem() {
            InventoryItemRequestDto request = validRequest();
            when(validator.validateItem(request, "A")).thenReturn(List.of());
            when(repository.save(any(InventoryItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemService.SaveResult result = service.saveItem(request, "A");

            assertThat(result.success()).isTrue();
            assertThat(result.entity().getItemId()).isEqualTo("ITM00001");
            assertThat(result.entity().getStatus()).isEqualTo(InventoryItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("routes to updateItem in Edit mode when valid")
        void saveItem_editMode_updatesItem() {
            InventoryItemRequestDto request = validRequest();
            InventoryItemEntity existing = activeEntity("ITM00001");
            when(validator.validateItem(request, "E")).thenReturn(List.of());
            when(repository.findById("ITM00001")).thenReturn(Optional.of(existing));
            when(repository.save(any(InventoryItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemService.SaveResult result = service.saveItem(request, "E");

            assertThat(result.success()).isTrue();
            assertThat(result.entity().getItemName()).isEqualTo("Widget A");
        }
    }

    @Nested
    @DisplayName("createItem — BR-049/050/051")
    class CreateItemTests {

        @Test
        @DisplayName("initializes quantityAllocated to 0 (HV-023)")
        void createItem_setsAllocatedQtyToZero() {
            InventoryItemRequestDto request = validRequest();
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemEntity result = service.createItem(request);

            assertThat(result.getQuantityAllocated()).isEqualTo(0);
        }

        @Test
        @DisplayName("initializes quantityOnOrder to 0 (HV-024)")
        void createItem_setsOnOrderQtyToZero() {
            InventoryItemRequestDto request = validRequest();
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemEntity result = service.createItem(request);

            assertThat(result.getQuantityOnOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("sets status to ACTIVE (BR-050)")
        void createItem_setsStatusActive() {
            InventoryItemRequestDto request = validRequest();
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemEntity result = service.createItem(request);

            assertThat(result.getStatus()).isEqualTo(InventoryItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("sets lastUpdatedDate to today")
        void createItem_setsLastUpdatedDate() {
            InventoryItemRequestDto request = validRequest();
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemEntity result = service.createItem(request);

            assertThat(result.getLastUpdatedDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("defaults quantityOnHand to 0 when null in request")
        void createItem_nullQtyOnHand_defaultsToZero() {
            InventoryItemRequestDto request = validRequest();
            request.setQuantityOnHand(null);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemEntity result = service.createItem(request);

            assertThat(result.getQuantityOnHand()).isEqualTo(0);
        }

        @Test
        @DisplayName("maps all DTO fields to entity correctly")
        void createItem_mapsAllFields() {
            InventoryItemRequestDto request = validRequest();
            ArgumentCaptor<InventoryItemEntity> captor = ArgumentCaptor.forClass(InventoryItemEntity.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.createItem(request);
            InventoryItemEntity saved = captor.getValue();

            assertThat(saved.getItemId()).isEqualTo("ITM00001");
            assertThat(saved.getItemName()).isEqualTo("Widget A");
            assertThat(saved.getItemDescription()).isEqualTo("Standard widget");
            assertThat(saved.getCategoryCode()).isEqualTo("ELECT");
            assertThat(saved.getSupplierCode()).isEqualTo("SUP001");
            assertThat(saved.getWarehouseCode()).isEqualTo("WH01");
            assertThat(saved.getQuantityOnHand()).isEqualTo(100);
            assertThat(saved.getReorderPoint()).isEqualTo(20);
            assertThat(saved.getReorderQuantity()).isEqualTo(50);
            assertThat(saved.getUnitCost()).isEqualByComparingTo("10.00");
            assertThat(saved.getUnitPrice()).isEqualByComparingTo("15.00");
        }
    }

    @Nested
    @DisplayName("updateItem — BR-053")
    class UpdateItemTests {

        @Test
        @DisplayName("updates existing entity fields")
        void updateItem_updatesFields() {
            InventoryItemRequestDto request = validRequest();
            request.setItemName("Updated Widget");
            request.setQuantityOnHand(200);
            InventoryItemEntity existing = activeEntity("ITM00001");
            when(repository.findById("ITM00001")).thenReturn(Optional.of(existing));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryItemEntity result = service.updateItem(request);

            assertThat(result.getItemName()).isEqualTo("Updated Widget");
            assertThat(result.getQuantityOnHand()).isEqualTo(200);
            assertThat(result.getLastUpdatedDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("throws when item not found")
        void updateItem_notFound_throws() {
            InventoryItemRequestDto request = validRequest();
            request.setItemId("MISSING");
            when(repository.findById("MISSING")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateItem(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Item not found: MISSING");
        }
    }

    @Nested
    @DisplayName("calculateMargin — ALG-001, DT-006")
    class CalculateMarginTests {

        @Test
        @DisplayName("standard margin: (15-10)/15*100 = 33.33%")
        void calculateMargin_standardValues() {
            BigDecimal result = service.calculateMargin(new BigDecimal("15.00"), new BigDecimal("10.00"));
            assertThat(result).isEqualByComparingTo("33.33");
        }

        @Test
        @DisplayName("100% margin when cost is zero")
        void calculateMargin_zeroCost() {
            BigDecimal result = service.calculateMargin(new BigDecimal("10.00"), BigDecimal.ZERO);
            assertThat(result).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("zero margin when price equals cost")
        void calculateMargin_priceEqualsCost() {
            BigDecimal result = service.calculateMargin(new BigDecimal("10.00"), new BigDecimal("10.00"));
            assertThat(result).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("zero when price is null (BR-032)")
        void calculateMargin_nullPrice_returnsZero() {
            BigDecimal result = service.calculateMargin(null, new BigDecimal("10.00"));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("zero when price is zero (BR-032)")
        void calculateMargin_zeroPrice_returnsZero() {
            BigDecimal result = service.calculateMargin(BigDecimal.ZERO, new BigDecimal("10.00"));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("negative margin when cost exceeds price")
        void calculateMargin_costExceedsPrice() {
            BigDecimal result = service.calculateMargin(new BigDecimal("10.00"), new BigDecimal("15.00"));
            assertThat(result).isEqualByComparingTo("-50.00");
        }

        @Test
        @DisplayName("zero when price is negative")
        void calculateMargin_negativePrice_returnsZero() {
            BigDecimal result = service.calculateMargin(new BigDecimal("-5.00"), new BigDecimal("10.00"));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("high precision margin: (100-99)/100*100 = 1.00%")
        void calculateMargin_smallDifference() {
            BigDecimal result = service.calculateMargin(new BigDecimal("100.00"), new BigDecimal("99.00"));
            assertThat(result).isEqualByComparingTo("1.00");
        }
    }

    @Nested
    @DisplayName("checkDeleteEligibility — BR-017/019")
    class CheckDeleteEligibilityTests {

        @Test
        @DisplayName("returns empty when no allocation (deletion allowed)")
        void checkDeleteEligibility_noAllocation_returnsEmpty() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            entity.setQuantityAllocated(0);
            when(repository.findById("ITM00001")).thenReturn(Optional.of(entity));

            Optional<String> result = service.checkDeleteEligibility("ITM00001");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns error message when allocated qty > 0 (BR-019)")
        void checkDeleteEligibility_hasAllocation_returnsError() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            entity.setQuantityAllocated(5);
            when(repository.findById("ITM00001")).thenReturn(Optional.of(entity));

            Optional<String> result = service.checkDeleteEligibility("ITM00001");

            assertThat(result).isPresent();
            assertThat(result.get()).contains("allocated quantity > 0");
        }

        @Test
        @DisplayName("returns empty when null allocation")
        void checkDeleteEligibility_nullAllocation_returnsEmpty() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            entity.setQuantityAllocated(null);
            when(repository.findById("ITM00001")).thenReturn(Optional.of(entity));

            Optional<String> result = service.checkDeleteEligibility("ITM00001");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when item not found")
        void checkDeleteEligibility_itemNotFound_returnsEmpty() {
            when(repository.findById("MISSING")).thenReturn(Optional.empty());

            Optional<String> result = service.checkDeleteEligibility("MISSING");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("softDeleteItem — BR-016/020, STM-001 T-001")
    class SoftDeleteItemTests {

        @Test
        @DisplayName("transitions ACTIVE item to INACTIVE")
        void softDeleteItem_activeItem_setsInactive() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            when(repository.findById("ITM00001")).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.softDeleteItem("ITM00001");

            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.INACTIVE);
            assertThat(entity.getLastUpdatedDate()).isEqualTo(LocalDate.now());
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("throws when allocated quantity exists (BR-019)")
        void softDeleteItem_withAllocations_throws() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            entity.setQuantityAllocated(10);
            when(repository.findById("ITM00001")).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.softDeleteItem("ITM00001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("allocated quantity is 10");
        }

        @Test
        @DisplayName("throws when item not found")
        void softDeleteItem_itemNotFound_throws() {
            when(repository.findById("MISSING")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.softDeleteItem("MISSING"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Item not found: MISSING");
        }

        @Test
        @DisplayName("throws when item is already INACTIVE (STM-001)")
        void softDeleteItem_inactiveItem_throws() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            entity.setStatus(InventoryItemStatus.INACTIVE);
            when(repository.findById("ITM00001")).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.softDeleteItem("ITM00001"))
                    .isInstanceOf(com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("SaveResult record")
    class SaveResultTests {

        @Test
        @DisplayName("success factory creates valid result")
        void saveResult_success() {
            InventoryItemEntity entity = activeEntity("ITM00001");
            InventoryItemService.SaveResult result = InventoryItemService.SaveResult.success(entity);

            assertThat(result.success()).isTrue();
            assertThat(result.entity()).isEqualTo(entity);
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("failure factory creates invalid result")
        void saveResult_failure() {
            InventoryItemService.SaveResult result = InventoryItemService.SaveResult.failure(List.of("INV-001"));

            assertThat(result.success()).isFalse();
            assertThat(result.entity()).isNull();
            assertThat(result.errors()).containsExactly("INV-001");
        }
    }
}
