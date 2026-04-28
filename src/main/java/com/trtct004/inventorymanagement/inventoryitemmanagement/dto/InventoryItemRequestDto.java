package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO for SCR-003/004: Inventory Item Detail.
 * Maps to item detail form fields (DOC-06 SCR-003).
 * Used for Add (mode=A) and Edit (mode=E) operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemRequestDto {

    /** F-003-05: Item ID — required in Add mode; protected in Edit mode (BR-036/037) */
    private String itemId;

    /** F-003-06: Item Name — mandatory (BR-038) */
    @NotBlank
    private String itemName;

    /** F-003-07: Item Description */
    private String itemDescription;

    /** F-003-08: Category Code — validated against CTGMST (BR-039) */
    private String categoryCode;

    /** F-003-10: Supplier Code — validated against SUPLMST (BR-041) */
    private String supplierCode;

    /** F-003-12: Warehouse Code — validated against WHSMST (BR-043) */
    private String warehouseCode;

    /** F-003-14: Quantity on Hand */
    private Integer quantityOnHand;

    /** F-003-17: Reorder Point */
    private Integer reorderPoint;

    /** F-003-18: Reorder Quantity */
    private Integer reorderQuantity;

    /** F-003-19: Unit Cost — FINANCIAL */
    private BigDecimal unitCost;

    /** F-003-20: Unit Price — FINANCIAL; must be >= unitCost when > 0 (BR-045) */
    private BigDecimal unitPrice;

    /** Operating mode: "A" (Add), "E" (Edit), "D" (Display) */
    private String mode;

    // Getters
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public String getCategoryCode() { return categoryCode; }
    public String getSupplierCode() { return supplierCode; }
    public String getWarehouseCode() { return warehouseCode; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public Integer getReorderPoint() { return reorderPoint; }
    public Integer getReorderQuantity() { return reorderQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getMode() { return mode; }

    // Setters
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public void setSupplierCode( String supplierCode) { this.supplierCode = supplierCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setMode(String mode) { this.mode = mode; }

    // Builder
    public static InventoryItemRequestDtoBuilder builder() {
        return new InventoryItemRequestDtoBuilder();
    }

    public static class InventoryItemRequestDtoBuilder {
        private String itemId;
        private String itemName;
        private String itemDescription;
        private String categoryCode;
        private String supplierCode;
        private String warehouseCode;
        private Integer quantityOnHand;
        private Integer reorderPoint;
        private Integer reorderQuantity;
        private BigDecimal unitCost;
        private BigDecimal unitPrice;
        private String mode;

        public InventoryItemRequestDtoBuilder itemId(String itemId) { this.itemId = itemId; return this; }
        public InventoryItemRequestDtoBuilder itemName(String itemName) { this.itemName = itemName; return this; }
        public InventoryItemRequestDtoBuilder itemDescription(String itemDescription) { this.itemDescription = itemDescription; return this; }
        public InventoryItemRequestDtoBuilder categoryCode(String categoryCode) { this.categoryCode = categoryCode; return this; }
        public InventoryItemRequestDtoBuilder supplierCode(String supplierCode) { this.supplierCode = supplierCode; return this; }
        public InventoryItemRequestDtoBuilder warehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; return this; }
        public InventoryItemRequestDtoBuilder quantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; return this; }
        public InventoryItemRequestDtoBuilder reorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public InventoryItemRequestDtoBuilder reorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; return this; }
        public InventoryItemRequestDtoBuilder unitCost(BigDecimal unitCost) { this.unitCost = unitCost; return this; }
        public InventoryItemRequestDtoBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public InventoryItemRequestDtoBuilder mode(String mode) { this.mode = mode; return this; }

        public InventoryItemRequestDto build() {
            InventoryItemRequestDto dto = new InventoryItemRequestDto();
            dto.itemId = this.itemId;
            dto.itemName = this.itemName;
            dto.itemDescription = this.itemDescription;
            dto.categoryCode = this.categoryCode;
            dto.supplierCode = this.supplierCode;
            dto.warehouseCode = this.warehouseCode;
            dto.quantityOnHand = this.quantityOnHand;
            dto.reorderPoint = this.reorderPoint;
            dto.reorderQuantity = this.reorderQuantity;
            dto.unitCost = this.unitCost;
            dto.unitPrice = this.unitPrice;
            dto.mode = this.mode;
            return dto;
        }
    }
}
