package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

import com.trtct004.inventorymanagement.inventoryitemmanagement.exception.InventoryItemStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * STM-001: Inventory Item Lifecycle State Machine.
 * Enforces legal transitions T-001 through T-003 for InventoryItemStatus.
 *
 * Legal transitions:
 *   T-001: ACTIVE      → INACTIVE        (soft delete — BR-020)
 *   T-002: INACTIVE    → ACTIVE          (reactivation — SME-required; not in current scope)
 *   T-003: ACTIVE      → PENDING_DELETE  (reserved for future delete workflow)
 */
@Component
public class InventoryItemStateMachine {

    private static final Map<InventoryItemStatus, Set<InventoryItemStatus>> LEGAL_TRANSITIONS;

    static {
        LEGAL_TRANSITIONS = new EnumMap<>(InventoryItemStatus.class);
        // T-001: ACTIVE → INACTIVE
        // T-003: ACTIVE → PENDING_DELETE
        LEGAL_TRANSITIONS.put(InventoryItemStatus.ACTIVE,
                EnumSet.of(InventoryItemStatus.INACTIVE, InventoryItemStatus.PENDING_DELETE));
        // T-002: INACTIVE → ACTIVE (reactivation — SME required)
        LEGAL_TRANSITIONS.put(InventoryItemStatus.INACTIVE,
                EnumSet.of(InventoryItemStatus.ACTIVE));
        // PENDING_DELETE → DELETED (terminal)
        LEGAL_TRANSITIONS.put(InventoryItemStatus.PENDING_DELETE,
                EnumSet.of(InventoryItemStatus.DELETED));
        // DELETED: no further transitions
        LEGAL_TRANSITIONS.put(InventoryItemStatus.DELETED, EnumSet.noneOf(InventoryItemStatus.class));
    }

    /**
     * Validate and execute a state transition on an inventory item entity.
     * Throws InventoryItemStateTransitionException if the transition is not legal.
     *
     * @param entity       the item to transition
     * @param targetStatus the desired new status
     * @param transitionId the transition identifier (T-001, T-002, T-003)
     */
    public void transition(InventoryItemEntity entity, InventoryItemStatus targetStatus, String transitionId) {
        InventoryItemStatus current = entity.getStatus();
        Set<InventoryItemStatus> allowed = LEGAL_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(InventoryItemStatus.class));

        if (!allowed.contains(targetStatus)) {
            throw new InventoryItemStateTransitionException(transitionId, current, targetStatus, entity.getItemId());
        }
        entity.setStatus(targetStatus);
    }

    /**
     * Return true if the transition from current to target is legal.
     */
    public boolean isLegalTransition(InventoryItemStatus from, InventoryItemStatus to) {
        return LEGAL_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(InventoryItemStatus.class)).contains(to);
    }
}
