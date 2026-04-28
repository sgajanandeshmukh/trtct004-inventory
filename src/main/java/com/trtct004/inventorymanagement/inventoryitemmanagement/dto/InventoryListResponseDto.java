package com.trtct004.inventorymanagement.inventoryitemmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for SCR-002: Inventory Item List response.
 */
public class InventoryListResponseDto {

    private List<ListRow> items;
    private boolean noRecordsFound;
    private boolean deactivationSuccess;
    private boolean deleteBlockedByAllocation;

    public InventoryListResponseDto() {}

    public List<ListRow> getItems() { return items; }
    public void setItems(List<ListRow> items) { this.items = items; }

    public boolean isNoRecordsFound() { return noRecordsFound; }
    public void setNoRecordsFound(boolean noRecordsFound) { this.noRecordsFound = noRecordsFound; }

    public boolean isDeactivationSuccess() { return deactivationSuccess; }
    public void setDeactivationSuccess(boolean deactivationSuccess) { this.deactivationSuccess = deactivationSuccess; }

    public boolean isDeleteBlockedByAllocation() { return deleteBlockedByAllocation; }
    public void setDeleteBlockedByAllocation(boolean deleteBlockedByAllocation) { this.deleteBlockedByAllocation = deleteBlockedByAllocation; }

    public static InventoryListResponseDtoBuilder builder() {
        return new InventoryListResponseDtoBuilder();
    }

    public static class InventoryListResponseDtoBuilder {
        private List<ListRow> items;
        private boolean noRecordsFound;
        private boolean deactivationSuccess;
        private boolean deleteBlockedByAllocation;

        public InventoryListResponseDtoBuilder items(List<ListRow> items) { this.items = items; return this; }
        public InventoryListResponseDtoBuilder noRecordsFound(boolean v) { this.noRecordsFound = v; return this; }
        public InventoryListResponseDtoBuilder deactivationSuccess(boolean v) { this.deactivationSuccess = v; return this; }
        public InventoryListResponseDtoBuilder deleteBlockedByAllocation(boolean v) { this.deleteBlockedByAllocation = v; return this; }

        public InventoryListResponseDto build() {
            InventoryListResponseDto dto = new InventoryListResponseDto();
            dto.items = this.items;
            dto.noRecordsFound = this.noRecordsFound;
            dto.deactivationSuccess = this.deactivationSuccess;
            dto.deleteBlockedByAllocation = this.deleteBlockedByAllocation;
            return dto;
        }
    }

    public static class ListRow {
        private String itemId;
        private String itemName;
        private String categoryCode;
        private Integer quantityOnHand;
        private BigDecimal unitPrice;
        private String status;

        public ListRow() {}

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public String getCategoryCode() { return categoryCode; }
        public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

        public Integer getQuantityOnHand() { return quantityOnHand; }
        public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public static ListRowBuilder builder() {
            return new ListRowBuilder();
        }

        public static class ListRowBuilder {
            private String itemId;
            private String itemName;
            private String categoryCode;
            private Integer quantityOnHand;
            private BigDecimal unitPrice;
            private String status;

            public ListRowBuilder itemId(String v) { this.itemId = v; return this; }
            public ListRowBuilder itemName(String v) { this.itemName = v; return this; }
            public ListRowBuilder categoryCode(String v) { this.categoryCode = v; return this; }
            public ListRowBuilder quantityOnHand(Integer v) { this.quantityOnHand = v; return this; }
            public ListRowBuilder unitPrice(BigDecimal v) { this.unitPrice = v; return this; }
            public ListRowBuilder status(String v) { this.status = v; return this; }

            public ListRow build() {
                ListRow row = new ListRow();
                row.itemId = this.itemId;
                row.itemName = this.itemName;
                row.categoryCode = this.categoryCode;
                row.quantityOnHand = this.quantityOnHand;
                row.unitPrice = this.unitPrice;
                row.status = this.status;
                return row;
            }
        }
    }
}
