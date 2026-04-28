package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

/**
 * Thrown when an invalid state transition is attempted on an InventoryItemEntity (STM-001).
 */
public class InventoryItemStateTransitionException extends RuntimeException {

    public InventoryItemStateTransitionException(String transitionCode,
                                                  InventoryItemStatus currentStatus,
                                                  InventoryItemStatus targetStatus,
                                                  String itemId) {
        super(String.format("Invalid state transition %s: cannot move from %s to %s for item [%s]",
                transitionCode, currentStatus, targetStatus, itemId));
    }
}
