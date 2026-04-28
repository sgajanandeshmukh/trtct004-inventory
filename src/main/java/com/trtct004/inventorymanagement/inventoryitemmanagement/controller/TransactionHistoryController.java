package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.InventoryTransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/transactions")
public class TransactionHistoryController {

    private final InventoryTransactionService transactionService;

    public TransactionHistoryController(InventoryTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> searchTransactions(
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<InventoryTransactionEntity> transactions = transactionService.searchTransactions(
                itemId, transactionType, startDate, endDate);

        return ResponseEntity.ok(Map.of(
                "transactions", transactions,
                "totalCount", transactions.size(),
                "noRecordsFound", transactions.isEmpty()
        ));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Map<String, Object>> getItemHistory(@PathVariable String itemId) {
        List<InventoryTransactionEntity> transactions = transactionService.getTransactionHistory(itemId);
        return ResponseEntity.ok(Map.of(
                "itemId", itemId,
                "transactions", transactions,
                "totalCount", transactions.size()
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentTransactions(
            @RequestParam(defaultValue = "20") int limit) {
        List<InventoryTransactionEntity> transactions = transactionService.getRecentTransactions(limit);
        return ResponseEntity.ok(Map.of(
                "transactions", transactions,
                "totalCount", transactions.size()
        ));
    }
}
