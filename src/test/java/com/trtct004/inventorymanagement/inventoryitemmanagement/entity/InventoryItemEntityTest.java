package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryItemEntityTest {

    private InventoryItemEntity buildEntity(InventoryItemStatus status) {
        return InventoryItemEntity.builder()
                .itemId("ITM00001")
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
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("Builder defaults")
    class BuilderDefaultTests {

        @Test
        @DisplayName("quantityAllocated defaults to 0")
        void builder_quantityAllocated_defaultsToZero() {
            InventoryItemEntity entity = InventoryItemEntity.builder()
                    .itemId("ITM00001")
                    .itemName("Test")
                    .categoryCode("ELECT")
                    .supplierCode("SUP001")
                    .warehouseCode("WH01")
                    .quantityOnHand(10)
                    .unitCost(BigDecimal.TEN)
                    .unitPrice(BigDecimal.TEN)
                    .build();
            assertThat(entity.getQuantityAllocated()).isEqualTo(0);
        }

        @Test
        @DisplayName("quantityOnOrder defaults to 0")
        void builder_quantityOnOrder_defaultsToZero() {
            InventoryItemEntity entity = InventoryItemEntity.builder()
                    .itemId("ITM00001")
                    .itemName("Test")
                    .categoryCode("ELECT")
                    .supplierCode("SUP001")
                    .warehouseCode("WH01")
                    .quantityOnHand(10)
                    .unitCost(BigDecimal.TEN)
                    .unitPrice(BigDecimal.TEN)
                    .build();
            assertThat(entity.getQuantityOnOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("status defaults to ACTIVE (BR-050)")
        void builder_status_defaultsToActive() {
            InventoryItemEntity entity = InventoryItemEntity.builder()
                    .itemId("ITM00001")
                    .itemName("Test")
                    .categoryCode("ELECT")
                    .supplierCode("SUP001")
                    .warehouseCode("WH01")
                    .quantityOnHand(10)
                    .unitCost(BigDecimal.TEN)
                    .unitPrice(BigDecimal.TEN)
                    .build();
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("softDelete — T-001: ACTIVE -> INACTIVE")
    class SoftDeleteTests {

        @Test
        @DisplayName("transitions ACTIVE to INACTIVE (BR-020)")
        void softDelete_fromActive_setsInactive() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            entity.softDelete();
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.INACTIVE);
        }

        @Test
        @DisplayName("throws from INACTIVE")
        void softDelete_fromInactive_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            assertThatThrownBy(entity::softDelete)
                    .isInstanceOf(InventoryItemStateTransitionException.class)
                    .hasMessageContaining("T-001");
        }

        @Test
        @DisplayName("throws from PENDING_DELETE")
        void softDelete_fromPendingDelete_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.PENDING_DELETE);
            assertThatThrownBy(entity::softDelete)
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("throws from DELETED")
        void softDelete_fromDeleted_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.DELETED);
            assertThatThrownBy(entity::softDelete)
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("reactivate — T-002: INACTIVE -> ACTIVE")
    class ReactivateTests {

        @Test
        @DisplayName("transitions INACTIVE to ACTIVE")
        void reactivate_fromInactive_setsActive() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            entity.reactivate();
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("throws from ACTIVE")
        void reactivate_fromActive_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            assertThatThrownBy(entity::reactivate)
                    .isInstanceOf(InventoryItemStateTransitionException.class)
                    .hasMessageContaining("T-002");
        }

        @Test
        @DisplayName("throws from PENDING_DELETE")
        void reactivate_fromPendingDelete_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.PENDING_DELETE);
            assertThatThrownBy(entity::reactivate)
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("flagForDeletion — T-003: ACTIVE -> PENDING_DELETE")
    class FlagForDeletionTests {

        @Test
        @DisplayName("transitions ACTIVE to PENDING_DELETE")
        void flagForDeletion_fromActive_setsPendingDelete() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            entity.flagForDeletion();
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.PENDING_DELETE);
        }

        @Test
        @DisplayName("throws from INACTIVE")
        void flagForDeletion_fromInactive_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            assertThatThrownBy(entity::flagForDeletion)
                    .isInstanceOf(InventoryItemStateTransitionException.class)
                    .hasMessageContaining("T-003");
        }

        @Test
        @DisplayName("throws from DELETED")
        void flagForDeletion_fromDeleted_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.DELETED);
            assertThatThrownBy(entity::flagForDeletion)
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("isActive — BR-033/034")
    class IsActiveTests {

        @Test
        @DisplayName("returns true for ACTIVE status")
        void isActive_activeStatus_true() {
            assertThat(buildEntity(InventoryItemStatus.ACTIVE).isActive()).isTrue();
        }

        @Test
        @DisplayName("returns false for INACTIVE status")
        void isActive_inactiveStatus_false() {
            assertThat(buildEntity(InventoryItemStatus.INACTIVE).isActive()).isFalse();
        }

        @Test
        @DisplayName("returns false for PENDING_DELETE status")
        void isActive_pendingDeleteStatus_false() {
            assertThat(buildEntity(InventoryItemStatus.PENDING_DELETE).isActive()).isFalse();
        }

        @Test
        @DisplayName("returns false for DELETED status")
        void isActive_deletedStatus_false() {
            assertThat(buildEntity(InventoryItemStatus.DELETED).isActive()).isFalse();
        }
    }
}
