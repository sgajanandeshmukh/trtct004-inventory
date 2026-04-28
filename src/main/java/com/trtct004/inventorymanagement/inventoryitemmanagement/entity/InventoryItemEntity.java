package com.trtct004.inventorymanagement.inventoryitemmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TRC-071, TRC-074: Inventory Item Entity — maps to DB-001 (ITMMST / ITMMSTP).
 * Central catalog of all inventory items with lifecycle state machine (STM-001).
 *
 * State machine transitions (STM-001):
 *   T-001: ACTIVE → INACTIVE (soft delete — BR-020)
 *   T-002: INACTIVE → ACTIVE (reactivation — SME-required)
 *   T-003: ACTIVE → PENDING_DELETE (reserved)
 *
 * PII: No PII fields on this entity.
 * Financial fields: unitCost (FLD-DB001-010), unitPrice (FLD-DB001-011) — BigDecimal precision.
 */
@Entity
@Table(name = "itmmst", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemEntity {

    /** FLD-DB001-001: Item ID — primary key, 8-char unique identifier */
    @Id
    @Column(name = "imitid", length = 8, nullable = false)
    private String itemId;

    /** FLD-DB001-002: Item Name — mandatory, must be unique (BR-038) */
    @Column(name = "imitnm", length = 30, nullable = false)
    private String itemName;

    /** FLD-DB001-003: Item Description */
    @Column(name = "imitds", length = 50)
    private String itemDescription;

    /** FLD-DB001-004: Category Code — FK → DB-002.CGCTID (inferred) */
    @Column(name = "imctgy", length = 6, nullable = false)
    private String categoryCode;

    /** FLD-DB001-005: Quantity On Hand */
    @Column(name = "imqtoh", nullable = false)
    private Integer quantityOnHand;

    /** FLD-DB001-006: Quantity Allocated — initialized to 0 for new items (HV-023, BR-049) */
    @Column(name = "imqtal")
    @Builder.Default
    private Integer quantityAllocated = 0;

    /** FLD-DB001-007: Quantity On Order — initialized to 0 for new items (HV-024, BR-049) */
    @Column(name = "imqtor")
    @Builder.Default
    private Integer quantityOnOrder = 0;

    /** FLD-DB001-008: Reorder Point */
    @Column(name = "imropl")
    private Integer reorderPoint;

    /** FLD-DB001-009: Reorder Quantity */
    @Column(name = "improq")
    private Integer reorderQuantity;

    /** FLD-DB001-010: Unit Cost — FINANCIAL; BigDecimal precision (BR-045) */
    @Column(name = "imuncs", precision = 9, scale = 2, nullable = false)
    private BigDecimal unitCost;

    /** FLD-DB001-011: Unit Price — FINANCIAL; must be >= unitCost when > 0 (BR-045) */
    @Column(name = "imunpr", precision = 9, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /** FLD-DB001-012: Supplier Code — FK → DB-003.SPSPID (inferred) */
    @Column(name = "imsupl", length = 6, nullable = false)
    private String supplierCode;

    /** FLD-DB001-013: Warehouse Code — FK → DB-004.WHWHID (inferred) */
    @Column(name = "imwhse", length = 4, nullable = false)
    private String warehouseCode;

    /**
     * FLD-DB001-014: Status — item lifecycle status (STM-001).
     * Default = ACTIVE for new items (BR-050, HV-013).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "imstat", length = 1, nullable = false)
    @Builder.Default
    private InventoryItemStatus status = InventoryItemStatus.ACTIVE;

    /** FLD-DB001-015: Last Updated Date — YYYYMMDD in legacy; native DATE in modernized system */
    @Column(name = "imlupd")
    private LocalDate lastUpdatedDate;

    // ─────────────────────────────────────────────────────────────────────────
    // STM-001: State machine transition methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * T-001: Transition ACTIVE → INACTIVE (soft delete).
     * BR-020: Set item status to inactive in the item master.
     * Throws InventoryItemStateTransitionException if current state is not ACTIVE.
     */
    public void softDelete() {
        if (this.status != InventoryItemStatus.ACTIVE) {
            throw new com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStateTransitionException(
                    "T-001", this.status, InventoryItemStatus.INACTIVE, itemId);
        }
        this.status = InventoryItemStatus.INACTIVE;
    }

    /**
     * T-002: Transition INACTIVE → ACTIVE (reactivation).
     * SME-required: not in current scope; included for completeness.
     */
    public void reactivate() {
        if (this.status != InventoryItemStatus.INACTIVE) {
            throw new com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStateTransitionException(
                    "T-002", this.status, InventoryItemStatus.ACTIVE, itemId);
        }
        this.status = InventoryItemStatus.ACTIVE;
    }

    /**
     * T-003: Transition ACTIVE → PENDING_DELETE.
     * Reserved for future delete workflow.
     */
    public void flagForDeletion() {
        if (this.status != InventoryItemStatus.ACTIVE) {
            throw new com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStateTransitionException(
                    "T-003", this.status, InventoryItemStatus.PENDING_DELETE, itemId);
        }
        this.status = InventoryItemStatus.PENDING_DELETE;
    }

    /**
     * BR-033/BR-034: Returns true if the item is Active.
     */
    public boolean isActive() {
        return InventoryItemStatus.ACTIVE == this.status;
    }
}
