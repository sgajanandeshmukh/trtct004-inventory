package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.service.InventoryTransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionHistoryController.class)
class TransactionHistoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private InventoryTransactionService transactionService;

    private InventoryTransactionEntity sampleTxn() {
        return InventoryTransactionEntity.builder()
                .sequenceId(1L)
                .transactionId(100L)
                .itemId("ITM00001")
                .transactionType("CR")
                .quantity(50)
                .transactionDate(LocalDate.of(2026, 4, 28))
                .userId("SYSTEM")
                .reference("NEW_ITEM")
                .build();
    }

    @Test
    @DisplayName("GET /api/inventory/transactions returns search results")
    void searchTransactions_returns200() throws Exception {
        when(transactionService.searchTransactions(any(), any(), any(), any()))
                .thenReturn(List.of(sampleTxn()));

        mockMvc.perform(get("/api/inventory/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.noRecordsFound").value(false))
                .andExpect(jsonPath("$.transactions[0].itemId").value("ITM00001"));
    }

    @Test
    @DisplayName("GET /api/inventory/transactions with filters passes params")
    void searchTransactions_withFilters() throws Exception {
        when(transactionService.searchTransactions(eq("ITM00001"), eq("CR"), any(), any()))
                .thenReturn(List.of(sampleTxn()));

        mockMvc.perform(get("/api/inventory/transactions")
                        .param("itemId", "ITM00001")
                        .param("transactionType", "CR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1));

        verify(transactionService).searchTransactions(eq("ITM00001"), eq("CR"), isNull(), isNull());
    }

    @Test
    @DisplayName("GET /api/inventory/transactions returns empty when no records")
    void searchTransactions_noRecords() throws Exception {
        when(transactionService.searchTransactions(any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/inventory/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.noRecordsFound").value(true));
    }

    @Test
    @DisplayName("GET /api/inventory/transactions/item/{itemId} returns item history")
    void getItemHistory_returns200() throws Exception {
        when(transactionService.getTransactionHistory("ITM00001"))
                .thenReturn(List.of(sampleTxn()));

        mockMvc.perform(get("/api/inventory/transactions/item/ITM00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value("ITM00001"))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.transactions").isArray());
    }

    @Test
    @DisplayName("GET /api/inventory/transactions/recent returns recent with default limit")
    void getRecentTransactions_defaultLimit() throws Exception {
        when(transactionService.getRecentTransactions(20))
                .thenReturn(List.of(sampleTxn()));

        mockMvc.perform(get("/api/inventory/transactions/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1));

        verify(transactionService).getRecentTransactions(20);
    }

    @Test
    @DisplayName("GET /api/inventory/transactions/recent with custom limit")
    void getRecentTransactions_customLimit() throws Exception {
        when(transactionService.getRecentTransactions(5))
                .thenReturn(List.of(sampleTxn()));

        mockMvc.perform(get("/api/inventory/transactions/recent")
                        .param("limit", "5"))
                .andExpect(status().isOk());

        verify(transactionService).getRecentTransactions(5);
    }

    @Test
    @DisplayName("GET /api/inventory/transactions with date params parses ISO dates")
    void searchTransactions_withDates() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        when(transactionService.searchTransactions(isNull(), isNull(), eq(start), eq(end)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/inventory/transactions")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk());

        verify(transactionService).searchTransactions(isNull(), isNull(), eq(start), eq(end));
    }
}
