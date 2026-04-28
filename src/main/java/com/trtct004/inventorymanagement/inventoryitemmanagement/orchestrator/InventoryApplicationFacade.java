package com.trtct004.inventorymanagement.inventoryitemmanagement.orchestrator;

import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.*;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.InventoryItemService;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.ReorderReportService;
import org.springframework.stereotype.Component;

/**
 * TRC-002, TRC-004, TRC-069, TRC-079: Inventory Application Facade
 * BPF-001: Inventory Item Management business process flow.
 * SEQ-001: Inventory Item Management Session sequence.
 *
 * Orchestrates the inventory management application session, routing user actions
 * to the appropriate domain services. Acts as the entry-point orchestrator for
 * the DD-L4-001 domain cluster.
 */
@Component
public class InventoryApplicationFacade {

    private final InventoryItemService inventoryItemService;
    private final ReorderReportService reorderReportService;

    public InventoryApplicationFacade(
            InventoryItemService inventoryItemService,
            ReorderReportService reorderReportService) {
        this.inventoryItemService = inventoryItemService;
        this.reorderReportService = reorderReportService;
    }

    /**
     * BPF-001: Orchestrate the inventory management process for a session.
     * SEQ-001 Step 1: Root domain entry.
     *
     * @param sessionContext session state (user preferences, current selections)
     * @return root domain response
     */
    public String orchestrateInventoryProcess(InventorySessionContext sessionContext) {
        return rootDomain(sessionContext);
    }

    /**
     * SEQ-001: Orchestrate session flow — dispatch to functional domain based on session action.
     * BR-002/003/004/005: Menu routing.
     */
    public String orchestrateSessionFlow(InventorySessionContext sessionContext) {
        return switch (sessionContext.getCurrentAction()) {
            case "BROWSE_LIST"    -> inventoryDomain(sessionContext);
            case "ITEM_DETAIL"    -> itemManagement(sessionContext);
            case "REORDER_REPORT" -> reorderReport(sessionContext);
            case "EXIT"           -> "EXIT_CONFIRMED";
            default               -> "INVALID_SELECTION";
        };
    }

    /**
     * Root domain — entry point for inventory management session.
     * BPF-001 Step 1: Display main menu.
     */
    public String rootDomain(InventorySessionContext sessionContext) {
        sessionContext.setCurrentScreen("MAIN_MENU");
        return "MAIN_MENU";
    }

    /**
     * Inventory domain — item list browsing.
     * BPF-001 Step 2: Browse Inventory Items.
     * BR-008 to BR-022.
     */
    public String inventoryDomain(InventorySessionContext sessionContext) {
        sessionContext.setCurrentScreen("ITEM_LIST");
        return "ITEM_LIST";
    }

    /**
     * Item management — create/edit/display/delete operations.
     * BPF-001 Steps 3–6.
     * BR-023 to BR-054.
     */
    public String itemManagement(InventorySessionContext sessionContext) {
        sessionContext.setCurrentScreen("ITEM_DETAIL");
        return "ITEM_DETAIL";
    }

    /**
     * Reorder report — display reorder items.
     * BPF-001 Step 7.
     * BR-055 to BR-070.
     */
    private String reorderReport(InventorySessionContext sessionContext) {
        sessionContext.setCurrentScreen("REORDER_REPORT");
        return "REORDER_REPORT";
    }

    /**
     * Simple session context holder — replaces legacy WS-001 working storage.
     */
    public static class InventorySessionContext {
        private String currentAction;
        private String currentScreen;
        private String selectedItemId;

        public String getCurrentAction() { return currentAction; }
        public void setCurrentAction(String action) { this.currentAction = action; }
        public String getCurrentScreen() { return currentScreen; }
        public void setCurrentScreen(String screen) { this.currentScreen = screen; }
        public String getSelectedItemId() { return selectedItemId; }
        public void setSelectedItemId(String itemId) { this.selectedItemId = itemId; }
    }
}
