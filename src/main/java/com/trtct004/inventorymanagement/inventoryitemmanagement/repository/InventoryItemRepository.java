package com.trtct004.inventorymanagement.inventoryitemmanagement.repository;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryItemEntity (DB-001 ITMMST).
 * TRC-071, TRC-074
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, String> {

    /**
     * BR-012: Filter by category code (exact match).
     * BR-013: Filter by name (partial/contains match).
     * BR-014: Maximum list size cap (HV-005).
     */
    @Query(value = "SELECT * FROM inventory.itmmst i " +
           "WHERE (:categoryCode IS NULL OR i.imctgy = :categoryCode) " +
           "AND (:nameFilter IS NULL OR i.imitnm ILIKE CONCAT('%', CAST(:nameFilter AS TEXT), '%')) " +
           "ORDER BY i.imitnm",
           nativeQuery = true)
    List<InventoryItemEntity> findByFilters(
            @Param("categoryCode") String categoryCode,
            @Param("nameFilter") String nameFilter);

    /**
     * BR-059: Find active items at or below reorder point with positive reorder point.
     * BR-060: Warehouse filter. BR-061: Category filter.
     * ALG-002, ALG-003: Reorder report data.
     */
    @Query("SELECT i FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE " +
           "AND i.reorderPoint > 0 " +
           "AND i.quantityOnHand <= i.reorderPoint " +
           "AND (:warehouseCode IS NULL OR i.warehouseCode = :warehouseCode) " +
           "AND (:categoryCode IS NULL OR i.categoryCode = :categoryCode) " +
           "ORDER BY i.itemId")
    List<InventoryItemEntity> findReorderItems(
            @Param("warehouseCode") String warehouseCode,
            @Param("categoryCode") String categoryCode);

    /** Check item name uniqueness for add mode (BR-037 — item ID uniqueness is PK). */
    boolean existsByItemName(String itemName);

    long countByStatus(com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus status);

    @Query("SELECT COUNT(i) FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE " +
           "AND i.reorderPoint > 0 AND i.quantityOnHand <= i.reorderPoint")
    long countItemsBelowReorderPoint();

    @Query("SELECT COUNT(i) FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE " +
           "AND i.quantityOnHand = 0")
    long countOutOfStockItems();

    @Query("SELECT COALESCE(SUM(i.quantityOnHand * i.unitCost), 0) FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE")
    java.math.BigDecimal calculateTotalInventoryValue();

    @Query("SELECT COALESCE(AVG(CASE WHEN i.unitPrice > 0 THEN ((i.unitPrice - i.unitCost) / i.unitPrice) * 100 ELSE 0 END), 0) " +
           "FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE")
    java.math.BigDecimal calculateAverageMargin();

    @Query("SELECT i.categoryCode, COUNT(i), COALESCE(SUM(i.quantityOnHand * i.unitCost), 0) " +
           "FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE " +
           "GROUP BY i.categoryCode ORDER BY i.categoryCode")
    List<Object[]> getCategoryDistribution();

    @Query("SELECT i.warehouseCode, COUNT(i), COALESCE(SUM(i.quantityOnHand), 0) " +
           "FROM InventoryItemEntity i " +
           "WHERE i.status = com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryItemStatus.ACTIVE " +
           "GROUP BY i.warehouseCode ORDER BY i.warehouseCode")
    List<Object[]> getWarehouseDistribution();

    @Query("SELECT i.status, COUNT(i) FROM InventoryItemEntity i GROUP BY i.status")
    List<Object[]> getStatusDistribution();
}
