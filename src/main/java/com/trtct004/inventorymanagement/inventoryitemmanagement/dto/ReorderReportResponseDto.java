package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for SCR-006: Inventory Reorder Report response.
 */
public class ReorderReportResponseDto {

    private List<ReorderItem> items;
    private int totalReorderItemCount;
    private BigDecimal totalReplenishmentValue;
    private boolean noItemsFound;

    public ReorderReportResponseDto() {}

    public List<ReorderItem> getItems() { return items; }
    public void setItems(List<ReorderItem> items) { this.items = items; }

    public int getTotalReorderItemCount() { return totalReorderItemCount; }
    public void setTotalReorderItemCount(int totalReorderItemCount) { this.totalReorderItemCount = totalReorderItemCount; }

    public BigDecimal getTotalReplenishmentValue() { return totalReplenishmentValue; }
    public void setTotalReplenishmentValue(BigDecimal totalReplenishmentValue) { this.totalReplenishmentValue = totalReplenishmentValue; }

    public boolean isNoItemsFound() { return noItemsFound; }
    public void setNoItemsFound(boolean noItemsFound) { this.noItemsFound = noItemsFound; }

    public static ReorderReportResponseDtoBuilder builder() {
        return new ReorderReportResponseDtoBuilder();
    }

    public static class ReorderReportResponseDtoBuilder {
        private List<ReorderItem> items;
        private int totalReorderItemCount;
        private BigDecimal totalReplenishmentValue;
        private boolean noItemsFound;

        public ReorderReportResponseDtoBuilder items(List<ReorderItem> items) { this.items = items; return this; }
        public ReorderReportResponseDtoBuilder totalReorderItemCount(int v) { this.totalReorderItemCount = v; return this; }
        public ReorderReportResponseDtoBuilder totalReplenishmentValue(BigDecimal v) { this.totalReplenishmentValue = v; return this; }
        public ReorderReportResponseDtoBuilder noItemsFound(boolean v) { this.noItemsFound = v; return this; }

        public ReorderReportResponseDto build() {
            ReorderReportResponseDto dto = new ReorderReportResponseDto();
            dto.items = this.items;
            dto.totalReorderItemCount = this.totalReorderItemCount;
            dto.totalReplenishmentValue = this.totalReplenishmentValue;
            dto.noItemsFound = this.noItemsFound;
            return dto;
        }
    }

    public static class ReorderItem {
        private String itemId;
        private String itemName;
        private String categoryCode;
        private int quantityOnHand;
        private int reorderPoint;
        private int shortageQuantity;
        private String supplierCode;
        private BigDecimal itemReplenishmentValue;

        public ReorderItem() {}

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

        public int getQuantityOnHand() { return quantityOnHand; }
        public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }

        public int getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }

        public int getShortageQuantity() { return shortageQuantity; }
        public void setShortageQuantity(int shortageQuantity) { this.shortageQuantity = shortageQuantity; }

        public String getSupplierCode() { return supplierCode; }
        public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

        public BigDecimal getItemReplenishmentValue() { return itemReplenishmentValue; }
        public void setItemReplenishmentValue(BigDecimal itemReplenishmentValue) { this.itemReplenishmentValue = itemReplenishmentValue; }

        public static ReorderItemBuilder builder() {
            return new ReorderItemBuilder();
        }

        public static class ReorderItemBuilder {
            private String itemId;
            private String itemName;
            private String categoryCode;
            private int quantityOnHand;
            private int reorderPoint;
            private int shortageQuantity;
            private String supplierCode;
            private BigDecimal itemReplenishmentValue;

            public ReorderItemBuilder itemId(String v) { this.itemId = v; return this; }
            public ReorderItemBuilder itemName(String v) { this.itemName = v; return this; }
            public ReorderItemBuilder categoryCode(String v) { this.categoryCode = v; return this; }
            public ReorderItemBuilder quantityOnHand(int v) { this.quantityOnHand = v; return this; }
            public ReorderItemBuilder reorderPoint(int v) { this.reorderPoint = v; return this; }
            public ReorderItemBuilder shortageQuantity(int v) { this.shortageQuantity = v; return this; }
            public ReorderItemBuilder supplierCode(String v) { this.supplierCode = v; return this; }
            public ReorderItemBuilder itemReplenishmentValue(BigDecimal v) { this.itemReplenishmentValue = v; return this; }

            public ReorderItem build() {
                ReorderItem item = new ReorderItem();
                item.itemId = this.itemId;
                item.itemName = this.itemName;
                item.categoryCode = this.categoryCode;
                item.quantityOnHand = this.quantityOnHand;
                item.reorderPoint = this.reorderPoint;
                item.shortageQuantity = this.shortageQuantity;
                item.supplierCode = this.supplierCode;
                item.itemReplenishmentValue = this.itemReplenishmentValue;
                return item;
            }
        }
    }
}
