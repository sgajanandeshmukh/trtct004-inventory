package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for inventory item detail response.
 * Includes enriched name fields (category, supplier, warehouse names) populated
 * by adapter lookups (BR-028/029/030/040/042/044).
 * Includes calculated margin (ALG-001).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponseDto {

    private String itemId;
    private String itemName;
    private String itemDescription;
    private String categoryCode;
    private String categoryName;      // enriched from CTGMST (BR-028/040)
    private String supplierCode;
    private String supplierName;      // enriched from SUPLMST (BR-029/042)
    private String warehouseCode;
    private String warehouseName;     // enriched from WHSMST (BR-030/044)
    private Integer quantityOnHand;
    private Integer quantityAllocated;
    private Integer quantityOnOrder;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private BigDecimal unitCost;
    private BigDecimal unitPrice;
    private BigDecimal marginPercentage; // ALG-001: calculated at runtime, not persisted
    private String status;             // "Active" or "Inactive" (BR-033/034)
    private LocalDate lastUpdatedDate;
    private String currentMode;        // "Add", "Edit", or "Display"
    private boolean saveSuccess;       // BR-052: save success indicator
    private boolean recordNotFound;    // BR-035/053: record-not-found indicator
    private boolean validationFailed;  // BR-048: validation-failed indicator
    private List<String> validationErrors; // BR-036–045: field-level errors

    // Getters
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public String getCategoryCode() { return categoryCode; }
    public String getCategoryName() { return categoryName; }
    public String getSupplierCode() { return supplierCode; }
    public String getSupplierName() { return supplierName; }
    public String getWarehouseCode() { return warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public Integer getQuantityAllocated() { return quantityAllocated; }
    public Integer getQuantityOnOrder() { return quantityOnOrder; }
    public Integer getReorderPoint() { return reorderPoint; }
    public Integer getReorderQuantity() { return reorderQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getMarginPercentage() { return marginPercentage; }
    public String getStatus() { return status; }
    public LocalDate getLastUpdatedDate() { return lastUpdatedDate; }
    public String getCurrentMode() { return currentMode; }
    public boolean isSaveSuccess() { return saveSuccess; }
    public boolean isRecordNotFound() { return recordNotFound; }
    public boolean isValidationFailed() { return validationFailed; }
    public List<String> getValidationErrors() { return validationErrors; }

    // Setters
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public void setQuantityAllocated(Integer quantityAllocated) { this.quantityAllocated = quantityAllocated; }
    public void setQuantityOnOrder(Integer quantityOnOrder) { this.quantityOnOrder = quantityOnOrder; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setMarginPercentage(BigDecimal marginPercentage) { this.marginPercentage = marginPercentage; }
    public void setStatus(String status) { this.status = status; }
    public void setLastUpdatedDate(LocalDate lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
    public void setCurrentMode(String currentMode) { this.currentMode = currentMode; }
    public void setSaveSuccess(boolean saveSuccess) { this.saveSuccess = saveSuccess; }
    public void setRecordNotFound(boolean recordNotFound) { this.recordNotFound = recordNotFound; }
    public void setValidationFailed(boolean validationFailed) { this.validationFailed = validationFailed; }
    public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }

    // Builder
    public static InventoryItemResponseDtoBuilder builder() {
        return new InventoryItemResponseDtoBuilder();
    }

    public InventoryItemResponseDtoBuilder toBuilder() {
        return new InventoryItemResponseDtoBuilder(this);
    }

    public static class InventoryItemResponseDtoBuilder {
        private String itemId;
        private String itemName;
        private String itemDescription;
        private String categoryCode;
        private String categoryName;
        private String supplierCode;
        private String supplierName;
        private String warehouseCode;
        private String warehouseName;
        private Integer quantityOnHand;
        private Integer quantityAllocated;
        private Integer quantityOnOrder;
        private Integer reorderPoint;
        private Integer reorderQuantity;
        private BigDecimal unitCost;
        private BigDecimal unitPrice;
        private BigDecimal marginPercentage;
        private String status;
        private LocalDate lastUpdatedDate;
        private String currentMode;
        private boolean saveSuccess;
        private boolean recordNotFound;
        private boolean validationFailed;
        private List<String> validationErrors;

        public InventoryItemResponseDtoBuilder() {}

        public InventoryItemResponseDtoBuilder(InventoryItemResponseDto dto) {
            this.itemId = dto.itemId;
            this.itemName = dto.itemName;
            this.itemDescription = dto.itemDescription;
            this.categoryCode = dto.categoryCode;
            this.categoryName = dto.categoryName;
            this.supplierCode = dto.supplierCode;
            this.supplierName = dto.supplierName;
            this.warehouseCode = dto.warehouseCode;
            this.warehouseName = dto.warehouseName;
            this.quantityOnHand = dto.quantityOnHand;
            this.quantityAllocated = dto.quantityAllocated;
            this.quantityOnOrder = dto.quantityOnOrder;
            this.reorderPoint = dto.reorderPoint;
            this.reorderQuantity = dto.reorderQuantity;
            this.unitCost = dto.unitCost;
            this.unitPrice = dto.unitPrice;
            this.marginPercentage = dto.marginPercentage;
            this.status = dto.status;
            this.lastUpdatedDate = dto.lastUpdatedDate;
            this.currentMode = dto.currentMode;
            this.saveSuccess = dto.saveSuccess;
            this.recordNotFound = dto.recordNotFound;
            this.validationFailed = dto.validationFailed;
            this.validationErrors = dto.validationErrors;
        }

        public InventoryItemResponseDtoBuilder itemId(String itemId) { this.itemId = itemId; return this; }
        public InventoryItemResponseDtoBuilder itemName(String itemName) { this.itemName = itemName; return this; }
        public InventoryItemResponseDtoBuilder itemDescription(String itemDescription) { this.itemDescription = itemDescription; return this; }
        public InventoryItemResponseDtoBuilder categoryCode(String categoryCode) { this.categoryCode = categoryCode; return this; }
        public InventoryItemResponseDtoBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public InventoryItemResponseDtoBuilder supplierCode(String supplierCode) { this.supplierCode = supplierCode; return this; }
        public InventoryItemResponseDtoBuilder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public InventoryItemResponseDtoBuilder warehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; return this; }
        public InventoryItemResponseDtoBuilder warehouseName(String warehouseName) { this.warehouseName = warehouseName; return this; }
        public InventoryItemResponseDtoBuilder quantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; return this; }
        public InventoryItemResponseDtoBuilder quantityAllocated(Integer quantityAllocated) { this.quantityAllocated = quantityAllocated; return this; }
        public InventoryItemResponseDtoBuilder quantityOnOrder(Integer quantityOnOrder) { this.quantityOnOrder = quantityOnOrder; return this; }
        public InventoryItemResponseDtoBuilder reorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public InventoryItemResponseDtoBuilder reorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; return this; }
        public InventoryItemResponseDtoBuilder unitCost(BigDecimal unitCost) { this.unitCost = unitCost; return this; }
        public InventoryItemResponseDtoBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public InventoryItemResponseDtoBuilder marginPercentage(BigDecimal marginPercentage) { this.marginPercentage = marginPercentage; return this; }
        public InventoryItemResponseDtoBuilder status(String status) { this.status = status; return this; }
        public InventoryItemResponseDtoBuilder lastUpdatedDate(LocalDate lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; return this; }
        public InventoryItemResponseDtoBuilder currentMode(String currentMode) { this.currentMode = currentMode; return this; }
        public InventoryItemResponseDtoBuilder saveSuccess(boolean saveSuccess) { this.saveSuccess = saveSuccess; return this; }
        public InventoryItemResponseDtoBuilder recordNotFound(boolean recordNotFound) { this.recordNotFound = recordNotFound; return this; }
        public InventoryItemResponseDtoBuilder validationFailed(boolean validationFailed) { this.validationFailed = validationFailed; return this; }
        public InventoryItemResponseDtoBuilder validationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; return this; }

        public InventoryItemResponseDto build() {
            InventoryItemResponseDto dto = new InventoryItemResponseDto();
            dto.itemId = this.itemId;
            dto.itemName = this.itemName;
            dto.itemDescription = this.itemDescription;
            dto.categoryCode = this.categoryCode;
            dto.categoryName = this.categoryName;
            dto.supplierCode = this.supplierCode;
            dto.supplierName = this.supplierName;
            dto.warehouseCode = this.warehouseCode;
            dto.warehouseName = this.warehouseName;
            dto.quantityOnHand = this.quantityOnHand;
            dto.quantityAllocated = this.quantityAllocated;
            dto.quantityOnOrder = this.quantityOnOrder;
            dto.reorderPoint = this.reorderPoint;
            dto.reorderQuantity = this.reorderQuantity;
            dto.unitCost = this.unitCost;
            dto.unitPrice = this.unitPrice;
            dto.marginPercentage = this.marginPercentage;
            dto.status = this.status;
            dto.lastUpdatedDate = this.lastUpdatedDate;
            dto.currentMode = this.currentMode;
            dto.saveSuccess = this.saveSuccess;
            dto.recordNotFound = this.recordNotFound;
            dto.validationFailed = this.validationFailed;
            dto.validationErrors = this.validationErrors;
            return dto;
        }
    }
}
