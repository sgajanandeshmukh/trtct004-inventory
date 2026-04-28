package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryTransactionRepository;
import com.trtct004.inventorymanagement.inventoryitemmanagement.util.InventoryConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final AtomicLong transactionIdGenerator = new AtomicLong(System.currentTimeMillis());

    public InventoryTransactionService(InventoryTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public InventoryTransactionEntity logTransaction(String itemId, String type, int quantity, String reference) {
        InventoryTransactionEntity txn = InventoryTransactionEntity.builder()
                .transactionId(transactionIdGenerator.incrementAndGet())
                .itemId(itemId)
                .transactionType(type)
                .quantity(quantity)
                .transactionDate(LocalDate.now())
                .userId("SYSTEM")
                .reference(truncate(reference, 20))
                .build();
        return transactionRepository.save(txn);
    }

    public void logItemCreated(String itemId, int quantityOnHand) {
        logTransaction(itemId, "CR", quantityOnHand, "NEW_ITEM");
    }

    public void logItemUpdated(String itemId, int quantityOnHand) {
        logTransaction(itemId, "UP", quantityOnHand, "EDIT");
    }

    public void logStatusChange(String itemId, String fromStatus, String toStatus) {
        logTransaction(itemId, "SC", 0, fromStatus + "->" + toStatus);
    }

    public void logQuantityAdjustment(String itemId, int oldQty, int newQty) {
        logTransaction(itemId, "QA", newQty - oldQty, "ADJ:" + oldQty + "->" + newQty);
    }

    public List<InventoryTransactionEntity> getTransactionHistory(String itemId) {
        return transactionRepository.findByItemIdOrderByTransactionDateDesc(itemId);
    }

    public List<InventoryTransactionEntity> searchTransactions(
            String itemId, String transactionType, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByFilters(itemId, transactionType, startDate, endDate);
    }

    public List<InventoryTransactionEntity> getRecentTransactions(int limit) {
        List<InventoryTransactionEntity> all = transactionRepository.findRecentTransactions();
        return all.stream().limit(limit).toList();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
