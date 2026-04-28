package com.trtct004.inventorymanagement.inventoryitemmanagement.exception;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;

/**
 * Thrown when an illegal STM-001 inventory item state transition is attempted.
 * Referenced by InventoryItemStateMachine and InventoryItemEntity.
 */
public class InventoryItemStateTransitionException extends RuntimeException {

    private final String transitionId;
    private final InventoryItemStatus fromState;
    private final InventoryItemStatus toState;
    private final String itemId;

    public InventoryItemStateTransitionException(
            String transitionId,
            InventoryItemStatus fromState,
            InventoryItemStatus toState,
            String itemId) {
        super(String.format(
                "Illegal inventory item state transition [%s]: cannot transition from %s to %s for item '%s'",
                transitionId, fromState, toState, itemId));
        this.transitionId = transitionId;
        this.fromState = fromState;
        this.toState = toState;
        this.itemId = itemId;
    }

    public String getTransitionId() { return transitionId; }
    public InventoryItemStatus getFromState() { return fromState; }
    public InventoryItemStatus getToState() { return toState; }
    public String getItemId() { return itemId; }
}
