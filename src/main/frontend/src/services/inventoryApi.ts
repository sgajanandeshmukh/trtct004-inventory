/**
 * inventoryApi.ts — Typed Axios API client for Inventory Management System
 * Maps to Spring REST endpoints defined in Group 9 controllers.
 * SCR-001 to SCR-006 screen data flows.
 */

import axios, { AxiosInstance, AxiosResponse } from 'axios';

// ─────────────────────────────────────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────────────────────────────────────

export interface InventoryMenuResponse {
  screen: string;
  options: Record<string, string>;
}

export interface InventoryListRow {
  itemId: string;
  itemName: string;
  categoryCode: string;
  quantityOnHand: number;
  unitPrice: number;
  status: string;
}

export interface InventoryListResponse {
  items: InventoryListRow[];
  noRecordsFound: boolean;
  deactivationSuccess: boolean;
  deleteBlockedByAllocation: boolean;
}

export interface InventoryItemRequest {
  itemId?: string;
  itemName: string;
  itemDescription?: string;
  categoryCode?: string;
  supplierCode?: string;
  warehouseCode?: string;
  quantityOnHand?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  unitCost?: number;
  unitPrice?: number;
  mode: 'A' | 'E' | 'D';
}

export interface InventoryItemResponse {
  itemId: string;
  itemName: string;
  itemDescription?: string;
  categoryCode?: string;
  categoryName?: string;
  supplierCode?: string;
  supplierName?: string;
  warehouseCode?: string;
  warehouseName?: string;
  quantityOnHand?: number;
  quantityAllocated?: number;
  quantityOnOrder?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  unitCost?: number;
  unitPrice?: number;
  marginPercentage?: number;
  status?: string;
  lastUpdatedDate?: string;
  currentMode?: string;
  saveSuccess?: boolean;
  recordNotFound?: boolean;
  validationFailed?: boolean;
  validationErrors?: string[];
}

export interface ReorderReportRequest {
  warehouseCode?: string;
  categoryCode?: string;
}

export interface ReorderItem {
  itemId: string;
  itemName: string;
  categoryCode: string;
  quantityOnHand: number;
  reorderPoint: number;
  shortageQuantity: number;
  supplierCode: string;
  itemReplenishmentValue: number;
}

export interface ReorderReportResponse {
  items: ReorderItem[];
  totalReorderItemCount: number;
  totalReplenishmentValue: number;
  noItemsFound: boolean;
}

// ─────────────────────────────────────────────────────────────────────────────
// API Client
// ─────────────────────────────────────────────────────────────────────────────

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// ─────────────────────────────────────────────────────────────────────────────
// Menu API — SCR-001/005
// ─────────────────────────────────────────────────────────────────────────────

export const menuApi = {
  /** BR-001: Get menu state */
  getMenu: (): Promise<AxiosResponse<InventoryMenuResponse>> =>
    apiClient.get('/api/inventory/menu'),

  /** BR-002–007: Submit menu selection (DT-001) */
  selectOption: (selection: string): Promise<AxiosResponse<{ redirect: string }>> =>
    apiClient.post('/api/inventory/menu/select', { selection }),

  /** BR-005: Get exit confirmation screen */
  getExitConfirm: (): Promise<AxiosResponse<{ screen: string; message: string }>> =>
    apiClient.get('/api/inventory/menu/exit-confirm'),

  /** BR-006/007: Confirm or cancel exit */
  confirmExit: (cancel: boolean): Promise<AxiosResponse<{ redirect?: string; action?: string }>> =>
    apiClient.post(`/api/inventory/menu/exit-confirm?cancel=${cancel}`),
};

// ─────────────────────────────────────────────────────────────────────────────
// List API — SCR-002
// ─────────────────────────────────────────────────────────────────────────────

export const listApi = {
  /** BR-008/009/011: Get item list with optional filters */
  getList: (categoryCode?: string, nameFilter?: string): Promise<AxiosResponse<InventoryListResponse>> =>
    apiClient.get('/api/inventory/list', { params: { categoryCode, nameFilter } }),

  /** DT-002: Perform row action (2=Edit, 4=Delete, 5=Display) */
  rowAction: (itemId: string, actionCode: string): Promise<AxiosResponse<unknown>> =>
    apiClient.post(`/api/inventory/list/${itemId}/action`, null, { params: { actionCode } }),
};

// ─────────────────────────────────────────────────────────────────────────────
// Item Detail API — SCR-003/004
// ─────────────────────────────────────────────────────────────────────────────

export const itemApi = {
  /** BR-023/024/025: Load item detail in specified mode */
  getItem: (itemId: string, mode: 'A' | 'E' | 'D'): Promise<AxiosResponse<InventoryItemResponse>> =>
    apiClient.get(`/api/inventory/items/${itemId}`, { params: { mode } }),

  /** BR-036–054: Save item (create or update) */
  saveItem: (request: InventoryItemRequest): Promise<AxiosResponse<InventoryItemResponse>> =>
    apiClient.post('/api/inventory/items', request),

  /** SCR-004: Get delete confirmation screen data */
  getDeleteConfirm: (itemId: string): Promise<AxiosResponse<unknown>> =>
    apiClient.get(`/api/inventory/items/${itemId}/delete-confirm`),
};

// ─────────────────────────────────────────────────────────────────────────────
// Reorder Report API — SCR-006
// ─────────────────────────────────────────────────────────────────────────────

export const reorderApi = {
  /** BR-055/056/058: Get reorder report with optional filters */
  getReport: (params: ReorderReportRequest): Promise<AxiosResponse<ReorderReportResponse>> =>
    apiClient.get('/api/inventory/report/reorder', { params }),

  /** BR-057: Print report */
  printReport: (params: ReorderReportRequest): Promise<AxiosResponse<string>> =>
    apiClient.post('/api/inventory/report/reorder/print', null, { params }),
};

export default apiClient;
