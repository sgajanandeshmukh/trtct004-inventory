package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class BulkOperationService {

    private final InventoryItemRepository itemRepository;
    private final InventoryTransactionService transactionService;

    public BulkOperationService(InventoryItemRepository itemRepository,
                                InventoryTransactionService transactionService) {
        this.itemRepository = itemRepository;
        this.transactionService = transactionService;
    }

    public String exportToCsv(String categoryCode, String nameFilter) {
        List<InventoryItemEntity> items = itemRepository.findByFilters(categoryCode, nameFilter, null);
        StringBuilder sb = new StringBuilder();
        sb.append("Item ID,Item Name,Description,Category,Qty On Hand,Qty Allocated,Qty On Order,");
        sb.append("Reorder Point,Reorder Qty,Unit Cost,Unit Price,Supplier,Warehouse,Status,Last Updated\n");

        for (InventoryItemEntity item : items) {
            sb.append(csvEscape(item.getItemId())).append(",");
            sb.append(csvEscape(item.getItemName())).append(",");
            sb.append(csvEscape(item.getItemDescription())).append(",");
            sb.append(csvEscape(item.getCategoryCode())).append(",");
            sb.append(item.getQuantityOnHand()).append(",");
            sb.append(item.getQuantityAllocated()).append(",");
            sb.append(item.getQuantityOnOrder()).append(",");
            sb.append(item.getReorderPoint()).append(",");
            sb.append(item.getReorderQuantity()).append(",");
            sb.append(item.getUnitCost()).append(",");
            sb.append(item.getUnitPrice()).append(",");
            sb.append(csvEscape(item.getSupplierCode())).append(",");
            sb.append(csvEscape(item.getWarehouseCode())).append(",");
            sb.append(item.getStatus()).append(",");
            sb.append(item.getLastUpdatedDate()).append("\n");
        }
        return sb.toString();
    }

    @Transactional
    public Map<String, Object> bulkStatusUpdate(List<String> itemIds, String targetStatus) {
        InventoryItemStatus target;
        try {
            target = InventoryItemStatus.valueOf(targetStatus);
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "error", "Invalid target status: " + targetStatus);
        }

        int successCount = 0;
        int failCount = 0;
        List<String> failures = new ArrayList<>();

        for (String itemId : itemIds) {
            try {
                Optional<InventoryItemEntity> opt = itemRepository.findById(itemId.trim());
                if (opt.isEmpty()) {
                    failures.add(itemId + ": not found");
                    failCount++;
                    continue;
                }
                InventoryItemEntity item = opt.get();
                String fromStatus = item.getStatus().name();

                if (target == InventoryItemStatus.INACTIVE && item.getStatus() == InventoryItemStatus.ACTIVE) {
                    item.softDelete();
                } else if (target == InventoryItemStatus.ACTIVE && item.getStatus() == InventoryItemStatus.INACTIVE) {
                    item.reactivate();
                } else {
                    failures.add(itemId + ": illegal transition " + item.getStatus() + " -> " + target);
                    failCount++;
                    continue;
                }

                item.setLastUpdatedDate(LocalDate.now());
                itemRepository.save(item);
                transactionService.logStatusChange(itemId.trim(), fromStatus, item.getStatus().name());
                successCount++;
            } catch (Exception e) {
                failures.add(itemId + ": " + e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failures", failures);
        result.put("totalProcessed", itemIds.size());
        return result;
    }

    @Transactional
    public Map<String, Object> importFromCsv(String csvContent) {
        int created = 0;
        int updated = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String header = reader.readLine();
            if (header == null) {
                return Map.of("success", false, "error", "Empty CSV");
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 13) {
                        errors.add("Line " + lineNum + ": insufficient fields (" + fields.length + ")");
                        failed++;
                        continue;
                    }

                    String itemId = fields[0].trim();
                    Optional<InventoryItemEntity> existing = itemRepository.findById(itemId);

                    if (existing.isPresent()) {
                        InventoryItemEntity entity = existing.get();
                        entity.setItemName(fields[1].trim());
                        entity.setItemDescription(fields[2].trim());
                        entity.setCategoryCode(fields[3].trim());
                        entity.setQuantityOnHand(parseIntSafe(fields[4]));
                        entity.setReorderPoint(parseIntSafe(fields[7]));
                        entity.setReorderQuantity(parseIntSafe(fields[8]));
                        entity.setUnitCost(parseDecimalSafe(fields[9]));
                        entity.setUnitPrice(parseDecimalSafe(fields[10]));
                        entity.setSupplierCode(fields[11].trim());
                        entity.setWarehouseCode(fields[12].trim());
                        entity.setLastUpdatedDate(LocalDate.now());
                        itemRepository.save(entity);
                        transactionService.logItemUpdated(itemId, entity.getQuantityOnHand());
                        updated++;
                    } else {
                        InventoryItemEntity entity = InventoryItemEntity.builder()
                                .itemId(itemId)
                                .itemName(fields[1].trim())
                                .itemDescription(fields[2].trim())
                                .categoryCode(fields[3].trim())
                                .quantityOnHand(parseIntSafe(fields[4]))
                                .quantityAllocated(0)
                                .quantityOnOrder(0)
                                .reorderPoint(parseIntSafe(fields[7]))
                                .reorderQuantity(parseIntSafe(fields[8]))
                                .unitCost(parseDecimalSafe(fields[9]))
                                .unitPrice(parseDecimalSafe(fields[10]))
                                .supplierCode(fields[11].trim())
                                .warehouseCode(fields[12].trim())
                                .status(InventoryItemStatus.ACTIVE)
                                .lastUpdatedDate(LocalDate.now())
                                .build();
                        itemRepository.save(entity);
                        transactionService.logItemCreated(itemId, entity.getQuantityOnHand());
                        created++;
                    }
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                    failed++;
                }
            }
        } catch (Exception e) {
            return Map.of("success", false, "error", "CSV parsing error: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("created", created);
        result.put("updated", updated);
        result.put("failed", failed);
        result.put("errors", errors);
        return result;
    }

    public String getImportTemplate() {
        return "Item ID,Item Name,Description,Category,Qty On Hand,Qty Allocated,Qty On Order," +
               "Reorder Point,Reorder Qty,Unit Cost,Unit Price,Supplier,Warehouse\n" +
               "ITM00099,Sample Item,Sample description,ELECT,100,0,0,20,50,10.00,15.00,SUP001,WH01\n";
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private int parseIntSafe(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return 0; }
    }

    private BigDecimal parseDecimalSafe(String value) {
        try { return new BigDecimal(value.trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
