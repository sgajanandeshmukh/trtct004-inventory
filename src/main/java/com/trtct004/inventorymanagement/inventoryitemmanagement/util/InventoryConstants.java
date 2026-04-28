package com.trtct004.inventorymanagement.inventoryitemmanagement.util;

import java.math.BigDecimal;

/**
 * InventoryConstants — all hardcoded values (HV-IDs) externalized from
 * legacy RPG programs INVMNUR/INVLSTR/INVDTLR/INVRPTR.
 */
public final class InventoryConstants {

    private InventoryConstants() {
        // utility class — no instantiation
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HV-IDs: Inventory Management
    // ─────────────────────────────────────────────────────────────────────────

    /** HV-001: Menu option code — Browse Inventory List */
    public static final String MENU_OPTION_BROWSE = "1";

    /** HV-002: Menu option code — Add/Edit Item */
    public static final String MENU_OPTION_ITEM_DETAIL = "2";

    /** HV-003: Menu option code — Reorder Report */
    public static final String MENU_OPTION_REORDER_REPORT = "3";

    /** HV-004: Menu option code — Exit */
    public static final String MENU_OPTION_EXIT = "9";

    /** HV-005 / HV-027: Maximum list/report rows per browse session */
    public static final int MAX_LIST_SIZE = 200;

    /** HV-006: List action code — Edit */
    public static final String LIST_ACTION_EDIT = "2";

    /** HV-007: List action code — Delete (deactivate) */
    public static final String LIST_ACTION_DELETE = "4";

    /** HV-008: List action code — Display (read-only) */
    public static final String LIST_ACTION_DISPLAY = "5";

    /** HV-013 / HV-026: Active item status code */
    public static final String ITEM_STATUS_ACTIVE = "A";

    /** HV-012 / HV-014: Inactive item status code */
    public static final String ITEM_STATUS_INACTIVE = "I";

    /** HV-015: Operating mode — Add */
    public static final String MODE_ADD = "A";

    /** HV-016: Operating mode — Edit */
    public static final String MODE_EDIT = "E";

    /** HV-017: Operating mode — Display */
    public static final String MODE_DISPLAY = "D";

    /** HV-025: Gross margin percentage multiplier */
    public static final BigDecimal MARGIN_PERCENT_MULTIPLIER = new BigDecimal("100");

    /** HV-023: New item allocated quantity initial value */
    public static final int NEW_ITEM_ALLOCATED_QTY = 0;

    /** HV-024: New item on-order quantity initial value */
    public static final int NEW_ITEM_ON_ORDER_QTY = 0;
}
