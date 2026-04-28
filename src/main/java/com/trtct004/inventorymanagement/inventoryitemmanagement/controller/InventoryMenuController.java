package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.InventoryMenuRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.orchestrator.InventoryApplicationFacade;
import com.trtct004.inventorymanagement.inventoryitemmanagement.util.InventoryConstants;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TRC-006, TRC-015, TRC-017, TRC-045, TRC-091, TRC-094, TRC-096, TRC-099:
 * Inventory Menu Controller — SCR-001/SCR-005
 * UI-001: Main menu and exit confirmation screens.
 *
 * BR-001: Menu Session Loop
 * BR-002: Navigate to Inventory List (HV-001: option '1')
 * BR-003: Navigate to Item Detail (HV-002: option '2')
 * BR-004: Navigate to Reorder Report (HV-003: option '3')
 * BR-005: Exit Confirmation Required (HV-004: option '9')
 * BR-006: Exit Cancelled — Resume Menu
 * BR-007: Invalid Menu Selection
 * DT-001: Menu Option Routing
 * DT-002: Exit confirmation routing
 */
@RestController
@RequestMapping("/api/inventory/menu")
public class InventoryMenuController {

    private final InventoryApplicationFacade inventoryApplicationFacade;

    public InventoryMenuController(InventoryApplicationFacade inventoryApplicationFacade) {
        this.inventoryApplicationFacade = inventoryApplicationFacade;
    }

    /**
     * Initialize and render the main menu.
     * BR-001: Menu Session Loop — GET always returns menu state.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> renderMenu() {
        return ResponseEntity.ok(Map.of(
                "screen", "MAIN_MENU",
                "options", Map.of(
                        InventoryConstants.MENU_OPTION_BROWSE,         "Browse Inventory Items",
                        InventoryConstants.MENU_OPTION_ITEM_DETAIL,    "Add / Edit Item",
                        InventoryConstants.MENU_OPTION_REORDER_REPORT, "Reorder Report",
                        InventoryConstants.MENU_OPTION_EXIT,           "Exit"
                )
        ));
    }

    /**
     * Handle a menu selection.
     * BR-002–007, DT-001: Route based on selection value.
     */
    @PostMapping("/select")
    public ResponseEntity<Map<String, String>> routeMenuOption(
            @Valid @RequestBody InventoryMenuRequestDto request) {

        String selection = request.getSelection();
        String route = switch (selection) {
            // BR-002: Navigate to Inventory List (HV-001)
            case "1" -> "/api/inventory/list";
            // BR-003: Navigate to Item Detail (HV-002)
            case "2" -> "/api/inventory/items";
            // BR-004: Navigate to Reorder Report (HV-003)
            case "3" -> "/api/inventory/report/reorder";
            // BR-005: Exit Confirmation Required (HV-004)
            case "9" -> "/api/inventory/menu/exit-confirm";
            // BR-007: Invalid Menu Selection
            default  -> null;
        };

        if (route == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid selection: " + selection,
                                 "indicator", "INVALID_SELECTION"));
        }

        return ResponseEntity.ok(Map.of("redirect", route));
    }

    /**
     * SCR-005: Render exit confirmation screen.
     * BR-005: Exit Confirmation Required.
     */
    @GetMapping("/exit-confirm")
    public ResponseEntity<Map<String, String>> renderExitConfirmation() {
        return ResponseEntity.ok(Map.of(
                "screen", "EXIT_CONFIRMATION",
                "message", "Press Enter to confirm exit, F12=Cancel"
        ));
    }

    /**
     * Confirm exit.
     * BR-006: F12 → Exit Cancelled; Enter → Terminate Session.
     */
    @PostMapping("/exit-confirm")
    public ResponseEntity<Map<String, String>> confirmExit(
            @RequestParam(defaultValue = "false") boolean cancel) {

        if (cancel) {
            // BR-006: Exit Cancelled — Resume Menu
            return ResponseEntity.ok(Map.of("redirect", "/api/inventory/menu"));
        }

        // Exit confirmed — terminate session
        return ResponseEntity.ok(Map.of("action", "SESSION_TERMINATED"));
    }

    /**
     * Terminate session — release all resources.
     */
    @PostMapping("/terminate")
    public ResponseEntity<Map<String, String>> terminateSession() {
        return ResponseEntity.ok(Map.of("status", "SESSION_CLOSED"));
    }

    /**
     * Initialize the menu screen state (called on session start).
     * BPF-001 Step 1.
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeMenuScreen() {
        InventoryApplicationFacade.InventorySessionContext ctx =
                new InventoryApplicationFacade.InventorySessionContext();
        ctx.setCurrentAction("INIT");
        String screen = inventoryApplicationFacade.orchestrateInventoryProcess(ctx);
        return ResponseEntity.ok(Map.of("screen", screen, "status", "INITIALIZED"));
    }

    /** Bind screen for main menu — returns current menu state for frontend binding */
    @GetMapping("/bind")
    public ResponseEntity<Map<String, Object>> bindMenuScreen() {
        return renderMenu();
    }

    /** Bind exit confirmation screen */
    @GetMapping("/exit-confirm/bind")
    public ResponseEntity<Map<String, String>> bindExitConfirmScreen() {
        return renderExitConfirmation();
    }
}
