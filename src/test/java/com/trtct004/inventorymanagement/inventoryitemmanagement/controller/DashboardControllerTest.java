package com.trtct004.inventorymanagement.inventoryitemmanagement.controller;

import com.trtct004.inventorymanagement.inventoryitemmanagement.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DashboardService dashboardService;

    private Map<String, Object> buildDashboardData() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalItems", 12L);
        kpis.put("activeItems", 10L);
        kpis.put("inactiveItems", 2L);
        kpis.put("belowReorderPoint", 3L);
        kpis.put("outOfStock", 1L);
        kpis.put("totalInventoryValue", new BigDecimal("5000.00"));
        kpis.put("averageMarginPercent", new BigDecimal("40.57"));
        kpis.put("inventoryHealthScore", 76);
        dashboard.put("kpis", kpis);
        dashboard.put("categoryDistribution", List.of());
        dashboard.put("warehouseDistribution", List.of());
        dashboard.put("statusDistribution", List.of());
        dashboard.put("recentActivity", List.of());
        dashboard.put("recentActivityCount", 0);
        return dashboard;
    }

    @Test
    @DisplayName("GET /api/inventory/dashboard returns 200 with KPIs")
    void getDashboard_returns200WithKpis() throws Exception {
        when(dashboardService.getDashboardData()).thenReturn(buildDashboardData());

        mockMvc.perform(get("/api/inventory/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalItems").value(12))
                .andExpect(jsonPath("$.kpis.activeItems").value(10))
                .andExpect(jsonPath("$.kpis.inventoryHealthScore").value(76))
                .andExpect(jsonPath("$.categoryDistribution").isArray())
                .andExpect(jsonPath("$.recentActivityCount").value(0));
    }

    @Test
    @DisplayName("GET /api/inventory/dashboard returns correct content type")
    void getDashboard_returnsJsonContentType() throws Exception {
        when(dashboardService.getDashboardData()).thenReturn(buildDashboardData());

        mockMvc.perform(get("/api/inventory/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @DisplayName("GET /api/inventory/dashboard returns inventory value as number")
    void getDashboard_inventoryValueAsNumber() throws Exception {
        when(dashboardService.getDashboardData()).thenReturn(buildDashboardData());

        mockMvc.perform(get("/api/inventory/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalInventoryValue").value(5000.00));
    }
}
