import React, { useState } from 'react';
import {
  Box, Grid, Paper, Typography, TextField, Button, MenuItem, Chip, Alert, Table,
  TableBody, TableCell, TableContainer, TableHead, TableRow, CircularProgress,
  IconButton, Tooltip, LinearProgress, Tabs, Tab,
} from '@mui/material';
import {
  Download, Upload, SwapHoriz, Description, CloudDownload, CheckCircle,
  Error as ErrorIcon, ContentCopy,
} from '@mui/icons-material';
import AppLayout from '../components/AppLayout';
import { bulkApi, listApi, BulkStatusResult, BulkImportResult } from '../services/inventoryApi';

interface TabPanelProps {
  children: React.ReactNode;
  value: number;
  index: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <Box role="tabpanel" hidden={value !== index} sx={{ pt: 3 }}>
    {value === index && children}
  </Box>
);

const BulkOperations: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);

  // Export state
  const [exportCategory, setExportCategory] = useState('');
  const [exportNameFilter, setExportNameFilter] = useState('');
  const [exporting, setExporting] = useState(false);
  const [exportMsg, setExportMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Status Update state
  const [statusItemIds, setStatusItemIds] = useState('');
  const [targetStatus, setTargetStatus] = useState('INACTIVE');
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [statusResult, setStatusResult] = useState<BulkStatusResult | null>(null);

  // Import state
  const [csvContent, setCsvContent] = useState('');
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<BulkImportResult | null>(null);
  const [importFile, setImportFile] = useState<File | null>(null);

  const handleExport = () => {
    setExporting(true);
    setExportMsg(null);
    bulkApi.exportCsv(exportCategory || undefined, exportNameFilter || undefined)
      .then((res) => {
        const blob = new Blob([res.data], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'inventory_export.csv';
        a.click();
        URL.revokeObjectURL(url);
        setExportMsg({ type: 'success', text: 'CSV exported successfully.' });
      })
      .catch(() => setExportMsg({ type: 'error', text: 'Export failed. Please try again.' }))
      .finally(() => setExporting(false));
  };

  const handleDownloadTemplate = () => {
    bulkApi.downloadTemplate()
      .then((res) => {
        const blob = new Blob([res.data], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'inventory_import_template.csv';
        a.click();
        URL.revokeObjectURL(url);
      })
      .catch(() => {});
  };

  const handleBulkStatusUpdate = () => {
    const ids = statusItemIds.split(/[\n,]+/).map(s => s.trim()).filter(Boolean);
    if (ids.length === 0) return;
    setStatusUpdating(true);
    setStatusResult(null);
    bulkApi.bulkStatusUpdate(ids, targetStatus)
      .then((res) => setStatusResult(res.data))
      .catch(() => setStatusResult({ success: false, successCount: 0, failCount: ids.length, failures: ['Request failed'], totalProcessed: ids.length }))
      .finally(() => setStatusUpdating(false));
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImportFile(file);
    const reader = new FileReader();
    reader.onload = (ev) => {
      setCsvContent(ev.target?.result as string || '');
    };
    reader.readAsText(file);
  };

  const handleImport = () => {
    if (!csvContent.trim()) return;
    setImporting(true);
    setImportResult(null);
    bulkApi.importCsv(csvContent)
      .then((res) => setImportResult(res.data))
      .catch(() => setImportResult({ success: false, created: 0, updated: 0, failed: 0, errors: ['Import request failed'] }))
      .finally(() => setImporting(false));
  };

  return (
    <AppLayout title="Bulk Operations" subtitle="Mass Data Management" showBack backTo="/inventory/dashboard" badge="FR-008">
      <Paper sx={{ p: 0, background: 'rgba(255,255,255,0.03)' }}>
        <Tabs
          value={activeTab}
          onChange={(_, v) => setActiveTab(v)}
          sx={{
            borderBottom: '1px solid rgba(148,163,184,0.1)',
            px: 2,
            '& .MuiTab-root': { textTransform: 'none', fontWeight: 600, minHeight: 56 },
          }}
        >
          <Tab icon={<Download />} iconPosition="start" label="Export CSV" />
          <Tab icon={<SwapHoriz />} iconPosition="start" label="Bulk Status Update" />
          <Tab icon={<Upload />} iconPosition="start" label="Import CSV" />
        </Tabs>

        {/* ─── Tab 0: Export ─── */}
        <TabPanel value={activeTab} index={0}>
          <Box sx={{ p: 3 }}>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
              <CloudDownload sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
              Export Inventory to CSV
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Download inventory data as a CSV file. Optionally filter by category or item name.
            </Typography>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={4}>
                <TextField fullWidth size="small" label="Category Code" value={exportCategory} onChange={e => setExportCategory(e.target.value)} placeholder="e.g. ELECT" />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField fullWidth size="small" label="Name Filter" value={exportNameFilter} onChange={e => setExportNameFilter(e.target.value)} placeholder="e.g. Widget" />
              </Grid>
              <Grid item xs={12} sm={4}>
                <Button
                  variant="contained"
                  startIcon={exporting ? <CircularProgress size={18} color="inherit" /> : <Download />}
                  onClick={handleExport}
                  disabled={exporting}
                  fullWidth
                >
                  {exporting ? 'Exporting...' : 'Export CSV'}
                </Button>
              </Grid>
            </Grid>
            {exportMsg && (
              <Alert severity={exportMsg.type} sx={{ mt: 2 }}>{exportMsg.text}</Alert>
            )}
          </Box>
        </TabPanel>

        {/* ─── Tab 1: Bulk Status Update ─── */}
        <TabPanel value={activeTab} index={1}>
          <Box sx={{ p: 3 }}>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
              <SwapHoriz sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
              Bulk Status Update
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Change the status of multiple items at once. Enter item IDs separated by commas or newlines.
              Only valid state transitions are allowed (Active &rarr; Inactive or Inactive &rarr; Active).
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={8}>
                <TextField
                  fullWidth
                  size="small"
                  label="Item IDs (comma or newline separated)"
                  multiline
                  rows={4}
                  value={statusItemIds}
                  onChange={e => setStatusItemIds(e.target.value)}
                  placeholder="ITM00001&#10;ITM00002&#10;ITM00003"
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  size="small"
                  select
                  label="Target Status"
                  value={targetStatus}
                  onChange={e => setTargetStatus(e.target.value)}
                  sx={{ mb: 2 }}
                >
                  <MenuItem value="INACTIVE">Inactive (Deactivate)</MenuItem>
                  <MenuItem value="ACTIVE">Active (Reactivate)</MenuItem>
                </TextField>
                <Button
                  variant="contained"
                  startIcon={statusUpdating ? <CircularProgress size={18} color="inherit" /> : <SwapHoriz />}
                  onClick={handleBulkStatusUpdate}
                  disabled={statusUpdating || !statusItemIds.trim()}
                  fullWidth
                  color={targetStatus === 'INACTIVE' ? 'warning' : 'success'}
                >
                  {statusUpdating ? 'Processing...' : 'Update Status'}
                </Button>
              </Grid>
            </Grid>

            {statusResult && (
              <Paper sx={{ mt: 3, p: 2, background: 'rgba(255,255,255,0.04)' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                  {statusResult.failCount === 0 ? (
                    <Chip icon={<CheckCircle />} label="All Succeeded" color="success" size="small" />
                  ) : statusResult.successCount === 0 ? (
                    <Chip icon={<ErrorIcon />} label="All Failed" color="error" size="small" />
                  ) : (
                    <Chip label="Partial Success" color="warning" size="small" />
                  )}
                  <Typography variant="body2" color="text.secondary">
                    {statusResult.successCount} succeeded, {statusResult.failCount} failed of {statusResult.totalProcessed} total
                  </Typography>
                </Box>
                {statusResult.successCount > 0 && (
                  <LinearProgress
                    variant="determinate"
                    value={(statusResult.successCount / statusResult.totalProcessed) * 100}
                    color="success"
                    sx={{ height: 6, borderRadius: 3, mb: 2 }}
                  />
                )}
                {statusResult.failures.length > 0 && (
                  <Box>
                    <Typography variant="caption" color="error" sx={{ fontWeight: 600 }}>Failures:</Typography>
                    {statusResult.failures.map((f, i) => (
                      <Typography key={i} variant="body2" sx={{ fontSize: '0.8rem', color: '#f87171', ml: 1 }}>
                        &bull; {f}
                      </Typography>
                    ))}
                  </Box>
                )}
              </Paper>
            )}
          </Box>
        </TabPanel>

        {/* ─── Tab 2: Import ─── */}
        <TabPanel value={activeTab} index={2}>
          <Box sx={{ p: 3 }}>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
              <Upload sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
              Import Inventory from CSV
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Upload a CSV file to create or update inventory items in bulk. Existing items (matched by Item ID) will be updated; new IDs will be created.
            </Typography>

            <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
              <Button
                variant="outlined"
                startIcon={<Description />}
                onClick={handleDownloadTemplate}
                size="small"
              >
                Download Template
              </Button>
              <Button
                variant="outlined"
                component="label"
                startIcon={<Upload />}
                size="small"
              >
                {importFile ? importFile.name : 'Select CSV File'}
                <input type="file" accept=".csv,text/csv" hidden onChange={handleFileSelect} />
              </Button>
            </Box>

            <TextField
              fullWidth
              size="small"
              label="CSV Content (paste or load from file)"
              multiline
              rows={8}
              value={csvContent}
              onChange={e => setCsvContent(e.target.value)}
              placeholder="Item ID,Item Name,Description,Category,Qty On Hand,Qty Allocated,Qty On Order,Reorder Point,Reorder Qty,Unit Cost,Unit Price,Supplier,Warehouse"
              sx={{ mb: 2, '& .MuiOutlinedInput-root': { fontFamily: 'monospace', fontSize: '0.8rem' } }}
            />

            <Button
              variant="contained"
              startIcon={importing ? <CircularProgress size={18} color="inherit" /> : <Upload />}
              onClick={handleImport}
              disabled={importing || !csvContent.trim()}
            >
              {importing ? 'Importing...' : 'Import CSV'}
            </Button>

            {importResult && (
              <Paper sx={{ mt: 3, p: 2, background: 'rgba(255,255,255,0.04)' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                  {importResult.failed === 0 ? (
                    <Chip icon={<CheckCircle />} label="Import Complete" color="success" size="small" />
                  ) : (
                    <Chip icon={<ErrorIcon />} label="Import Partial" color="warning" size="small" />
                  )}
                </Box>
                <Grid container spacing={2} sx={{ mb: 2 }}>
                  <Grid item xs={4}>
                    <Paper sx={{ p: 1.5, textAlign: 'center', background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.2)' }}>
                      <Typography variant="h5" fontWeight={700} sx={{ color: '#34d399' }}>{importResult.created}</Typography>
                      <Typography variant="caption" color="text.secondary">Created</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={4}>
                    <Paper sx={{ p: 1.5, textAlign: 'center', background: 'rgba(59,130,246,0.1)', border: '1px solid rgba(59,130,246,0.2)' }}>
                      <Typography variant="h5" fontWeight={700} sx={{ color: '#60a5fa' }}>{importResult.updated}</Typography>
                      <Typography variant="caption" color="text.secondary">Updated</Typography>
                    </Paper>
                  </Grid>
                  <Grid item xs={4}>
                    <Paper sx={{ p: 1.5, textAlign: 'center', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)' }}>
                      <Typography variant="h5" fontWeight={700} sx={{ color: '#f87171' }}>{importResult.failed}</Typography>
                      <Typography variant="caption" color="text.secondary">Failed</Typography>
                    </Paper>
                  </Grid>
                </Grid>
                {importResult.errors.length > 0 && (
                  <Box>
                    <Typography variant="caption" color="error" sx={{ fontWeight: 600 }}>Errors:</Typography>
                    {importResult.errors.map((err, i) => (
                      <Typography key={i} variant="body2" sx={{ fontSize: '0.8rem', color: '#f87171', ml: 1 }}>
                        &bull; {err}
                      </Typography>
                    ))}
                  </Box>
                )}
              </Paper>
            )}
          </Box>
        </TabPanel>
      </Paper>
    </AppLayout>
  );
};

export default BulkOperations;
