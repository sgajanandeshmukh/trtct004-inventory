package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.service.BulkOperationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BulkOperationController.class)
class BulkOperationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private BulkOperationService bulkOperationService;

    @Test
    @DisplayName("GET /api/inventory/bulk/export returns CSV file")
    void exportCsv_returns200WithCsv() throws Exception {
        when(bulkOperationService.exportToCsv(isNull(), isNull()))
                .thenReturn("Item ID,Item Name\nITM00001,Widget\n");

        mockMvc.perform(get("/api/inventory/bulk/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=inventory_export.csv"))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    @DisplayName("GET /api/inventory/bulk/export with filters")
    void exportCsv_withFilters() throws Exception {
        when(bulkOperationService.exportToCsv("ELECT", "widget"))
                .thenReturn("Item ID\n");

        mockMvc.perform(get("/api/inventory/bulk/export")
                        .param("categoryCode", "ELECT")
                        .param("nameFilter", "widget"))
                .andExpect(status().isOk());

        verify(bulkOperationService).exportToCsv("ELECT", "widget");
    }

    @Test
    @DisplayName("POST /api/inventory/bulk/status-update returns result")
    void bulkStatusUpdate_returns200() throws Exception {
        Map<String, Object> serviceResult = new LinkedHashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("successCount", 2);
        serviceResult.put("failCount", 0);
        serviceResult.put("failures", List.of());
        serviceResult.put("totalProcessed", 2);
        when(bulkOperationService.bulkStatusUpdate(anyList(), eq("INACTIVE")))
                .thenReturn(serviceResult);

        mockMvc.perform(post("/api/inventory/bulk/status-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemIds\":[\"ITM00001\",\"ITM00002\"],\"targetStatus\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(2));
    }

    @Test
    @DisplayName("POST /api/inventory/bulk/status-update returns 400 when missing fields")
    void bulkStatusUpdate_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/inventory/bulk/status-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"INACTIVE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/inventory/bulk/status-update returns 400 when empty itemIds")
    void bulkStatusUpdate_emptyItemIds_returns400() throws Exception {
        mockMvc.perform(post("/api/inventory/bulk/status-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemIds\":[],\"targetStatus\":\"INACTIVE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/inventory/bulk/import returns import result")
    void importCsv_returns200() throws Exception {
        Map<String, Object> serviceResult = new LinkedHashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("created", 1);
        serviceResult.put("updated", 0);
        serviceResult.put("failed", 0);
        serviceResult.put("errors", List.of());
        when(bulkOperationService.importFromCsv(anyString())).thenReturn(serviceResult);

        mockMvc.perform(post("/api/inventory/bulk/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"csvContent\":\"Header\\nITM00099,Test,Desc,ELECT,10,0,0,5,20,1.00,2.00,SUP001,WH01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.created").value(1));
    }

    @Test
    @DisplayName("POST /api/inventory/bulk/import returns 400 when csvContent missing")
    void importCsv_missingContent_returns400() throws Exception {
        mockMvc.perform(post("/api/inventory/bulk/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/inventory/bulk/import returns 400 when csvContent blank")
    void importCsv_blankContent_returns400() throws Exception {
        mockMvc.perform(post("/api/inventory/bulk/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"csvContent\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/inventory/bulk/template returns CSV template")
    void downloadTemplate_returns200() throws Exception {
        when(bulkOperationService.getImportTemplate())
                .thenReturn("Item ID,Item Name\nITM00099,Sample\n");

        mockMvc.perform(get("/api/inventory/bulk/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=inventory_import_template.csv"));
    }
}
