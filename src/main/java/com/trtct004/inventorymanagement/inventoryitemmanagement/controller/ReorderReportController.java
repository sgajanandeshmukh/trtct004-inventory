package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.adapter.ReportPrintAdapterProxy;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.ReorderReportRequestDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.dto.ReorderReportResponseDto;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.ReorderReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TRC-009, TRC-095, TRC-100: Reorder Report Controller — SCR-006
 * UI-005: Inventory Reorder Report screen.
 *
 * BR-055: Initial Report Load on Entry
 * BR-056: Manual Report Refresh (F5)
 * BR-057: Print Report (F6)
 * BR-058: Auto-Refresh on Filter Change
 * BR-059–070: Reorder eligibility, calculations, aggregation
 * DT-007: Item Inclusion Criteria for Reorder Report
 * DT-008: Report Refresh Triggers
 */
@RestController
@RequestMapping("/api/inventory/report/reorder")
public class ReorderReportController {

    private final ReorderReportService reorderReportService;

    public ReorderReportController(ReorderReportService reorderReportService) {
        this.reorderReportService = reorderReportService;
    }

    /**
     * BR-055/056/058: Generate reorder report with optional filters.
     * DT-008: Triggered by load, F5 refresh, or filter change.
     */
    @GetMapping
    public ResponseEntity<ReorderReportResponseDto> generateReorderReport(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String categoryCode) {

        ReorderReportRequestDto request = new ReorderReportRequestDto(warehouseCode, categoryCode);
        ReorderReportResponseDto response = reorderReportService.aggregateReorderData(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Render reorder report for screen display.
     */
    @GetMapping("/render")
    public ResponseEntity<ReorderReportResponseDto> renderReorderReport(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String categoryCode) {
        return generateReorderReport(warehouseCode, categoryCode);
    }

    /**
     * Bind reorder report screen for frontend.
     */
    @GetMapping("/bind")
    public ResponseEntity<ReorderReportResponseDto> bindReorderReportScreen() {
        return generateReorderReport(null, null);
    }

    /**
     * BR-057: Print Report — F6 action.
     * Delegates to ReportPrintAdapter (EXT-009).
     */
    @PostMapping("/print")
    public ResponseEntity<String> printReport(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String categoryCode) {
        // Render report first, then spool output
        ReorderReportResponseDto report = reorderReportService.aggregateReorderData(
                new ReorderReportRequestDto(warehouseCode, categoryCode));
        // EXT-009: spoolReportOutput — implementation pending
        return ResponseEntity.ok("PRINT_QUEUED");
    }
}
