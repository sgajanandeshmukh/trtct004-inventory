package com.trtct004.inventorymanagement.inventoryitemmanagement.repository;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity, Long> {

    List<InventoryTransactionEntity> findByItemIdOrderByTransactionDateDesc(String itemId);

    @Query("SELECT t FROM InventoryTransactionEntity t " +
           "WHERE (:itemId IS NULL OR t.itemId = :itemId) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "ORDER BY t.transactionDate DESC, t.sequenceId DESC")
    List<InventoryTransactionEntity> findByFilters(
            @Param("itemId") String itemId,
            @Param("transactionType") String transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM InventoryTransactionEntity t ORDER BY t.sequenceId DESC")
    List<InventoryTransactionEntity> findRecentTransactions();

    @Query("SELECT COUNT(t) FROM InventoryTransactionEntity t " +
           "WHERE t.transactionDate = :date AND t.transactionType = 'SC' " +
           "AND t.reference LIKE '%INACTIVE%'")
    long countDeactivationsOnDate(@Param("date") LocalDate date);
}
