package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.service.BulkOperationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/bulk")
public class BulkOperationController {

    private final BulkOperationService bulkOperationService;

    public BulkOperationController(BulkOperationService bulkOperationService) {
        this.bulkOperationService = bulkOperationService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String nameFilter) {

        String csv = bulkOperationService.exportToCsv(categoryCode, nameFilter);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    @PostMapping("/status-update")
    public ResponseEntity<Map<String, Object>> bulkStatusUpdate(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> itemIds = (List<String>) request.get("itemIds");
        String targetStatus = (String) request.get("targetStatus");

        if (itemIds == null || itemIds.isEmpty() || targetStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "itemIds and targetStatus required"));
        }

        return ResponseEntity.ok(bulkOperationService.bulkStatusUpdate(itemIds, targetStatus));
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCsv(@RequestBody Map<String, String> request) {
        String csvContent = request.get("csvContent");
        if (csvContent == null || csvContent.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "csvContent required"));
        }
        return ResponseEntity.ok(bulkOperationService.importFromCsv(csvContent));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String template = bulkOperationService.getImportTemplate();
        byte[] bytes = template.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_import_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
