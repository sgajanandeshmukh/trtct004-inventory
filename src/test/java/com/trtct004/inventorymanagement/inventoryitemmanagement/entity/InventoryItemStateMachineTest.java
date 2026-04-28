package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

import com.trtct004.inventorymanagement.inventoryitemmanagement.exception.InventoryItemStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryItemStateMachineTest {

    private InventoryItemStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new InventoryItemStateMachine();
    }

    private InventoryItemEntity buildEntity(InventoryItemStatus status) {
        return InventoryItemEntity.builder()
                .itemId("ITM00001")
                .itemName("Test Item")
                .categoryCode("ELECT")
                .supplierCode("SUP001")
                .warehouseCode("WH01")
                .quantityOnHand(10)
                .unitCost(BigDecimal.TEN)
                .unitPrice(BigDecimal.TEN)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("Legal transitions — transition()")
    class LegalTransitionTests {

        @Test
        @DisplayName("T-001: ACTIVE -> INACTIVE")
        void transition_activeToInactive() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            stateMachine.transition(entity, InventoryItemStatus.INACTIVE, "T-001");
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.INACTIVE);
        }

        @Test
        @DisplayName("T-002: INACTIVE -> ACTIVE")
        void transition_inactiveToActive() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            stateMachine.transition(entity, InventoryItemStatus.ACTIVE, "T-002");
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.ACTIVE);
        }

        @Test
        @DisplayName("T-003: ACTIVE -> PENDING_DELETE")
        void transition_activeToPendingDelete() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            stateMachine.transition(entity, InventoryItemStatus.PENDING_DELETE, "T-003");
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.PENDING_DELETE);
        }

        @Test
        @DisplayName("PENDING_DELETE -> DELETED (terminal)")
        void transition_pendingDeleteToDeleted() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.PENDING_DELETE);
            stateMachine.transition(entity, InventoryItemStatus.DELETED, "T-004");
            assertThat(entity.getStatus()).isEqualTo(InventoryItemStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("Illegal transitions — transition()")
    class IllegalTransitionTests {

        @Test
        @DisplayName("ACTIVE -> ACTIVE is illegal")
        void transition_activeToActive_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.ACTIVE, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("ACTIVE -> DELETED is illegal (must go through PENDING_DELETE)")
        void transition_activeToDeleted_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.ACTIVE);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.DELETED, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("INACTIVE -> INACTIVE is illegal")
        void transition_inactiveToInactive_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.INACTIVE, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("INACTIVE -> PENDING_DELETE is illegal")
        void transition_inactiveToPendingDelete_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.PENDING_DELETE, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("INACTIVE -> DELETED is illegal")
        void transition_inactiveToDeleted_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.INACTIVE);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.DELETED, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("PENDING_DELETE -> ACTIVE is illegal")
        void transition_pendingDeleteToActive_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.PENDING_DELETE);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.ACTIVE, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }

        @Test
        @DisplayName("DELETED -> any is illegal (terminal state)")
        void transition_deletedToAnything_throws() {
            InventoryItemEntity entity = buildEntity(InventoryItemStatus.DELETED);
            assertThatThrownBy(() -> stateMachine.transition(entity, InventoryItemStatus.ACTIVE, "X"))
                    .isInstanceOf(InventoryItemStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("isLegalTransition — predicate")
    class IsLegalTransitionTests {

        @ParameterizedTest
        @CsvSource({
                "ACTIVE, INACTIVE, true",
                "ACTIVE, PENDING_DELETE, true",
                "INACTIVE, ACTIVE, true",
                "PENDING_DELETE, DELETED, true",
                "ACTIVE, ACTIVE, false",
                "ACTIVE, DELETED, false",
                "INACTIVE, INACTIVE, false",
                "INACTIVE, PENDING_DELETE, false",
                "INACTIVE, DELETED, false",
                "PENDING_DELETE, ACTIVE, false",
                "PENDING_DELETE, INACTIVE, false",
                "DELETED, ACTIVE, false",
                "DELETED, INACTIVE, false",
                "DELETED, PENDING_DELETE, false",
                "DELETED, DELETED, false"
        })
        @DisplayName("validates all state combinations")
        void isLegalTransition(InventoryItemStatus from, InventoryItemStatus to, boolean expected) {
            assertThat(stateMachine.isLegalTransition(from, to)).isEqualTo(expected);
        }
    }
}
