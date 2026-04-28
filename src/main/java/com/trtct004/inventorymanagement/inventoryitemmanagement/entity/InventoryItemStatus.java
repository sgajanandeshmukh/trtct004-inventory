package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

/**
 * STM-001: Inventory Item Lifecycle Status
 * Legal transitions:
 *   T-001: ACTIVE      → INACTIVE        (soft delete / deactivation)
 *   T-002: INACTIVE    → ACTIVE          (reactivation — SME-required; not currently in scope)
 *   T-003: ACTIVE      → PENDING_DELETE  (reserved for future delete workflow)
 * BR-020, BR-033, BR-034, BR-050
 */
public enum InventoryItemStatus {

    /** Item is available and actively managed. Default for new items (BR-050, HV-013). */
    ACTIVE,

    /** Item has been soft-deleted (deactivated). Physical record is retained (BR-020, HV-012). */
    INACTIVE,

    /** Item flagged for deletion — awaiting final approval. Reserved for future workflow. */
    PENDING_DELETE,

    /** Item permanently removed — logical terminal state (not used in current scope). */
    DELETED
}
