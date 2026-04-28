package com.trtct004.inventorymanagement.inventoryitemmanagement.adapter;

import org.springframework.stereotype.Component;

/**
 * Proxy adapter to bridge ReportPrintAdapter into the inventory domain.
 * Avoids circular dependency between inventory controller and payroll adapter package.
 */
@Component
public class ReportPrintAdapterProxy {
    public void spoolReport(String reportName, String content) {
        // Delegates to EXT-009 implementation when available
    }
}
