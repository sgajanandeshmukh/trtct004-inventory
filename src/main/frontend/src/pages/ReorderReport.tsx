/**
 * ReorderReport.tsx — SCR-006: Inventory Reorder Report
 * R-08-A: Filter actions -> HTTP GET; print action -> HTTP POST
 * R-08-G: BATCH-REPORT — Sort/Group by shortage quantity descending (DT-008)
 * R-08-D: shortageQuantity and itemReplenishmentValue are derived — display only
 * BR-055–070, DT-007, DT-008, ALG-002, ALG-003
 */

import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, TextField, Button, Table, TableHead, TableRow, TableCell,
  TableBody, Paper, Grid, Chip, Snackbar, Alert, Skeleton, Card, CardContent, TableContainer
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import RefreshIcon from '@mui/icons-material/Refresh';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import AppLayout from '../components/AppLayout';
import { reorderApi, ReorderItem } from '../services/inventoryApi';

const ReorderReport: React.FC = () => {
  const navigate = useNavigate();
  const [warehouseFilter, setWarehouseFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [items, setItems] = useState<ReorderItem[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [totalValue, setTotalValue] = useState(0);
  const [noItems, setNoItems] = useState(false);
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' }>({
    open: false, message: '', severity: 'success',
  });

  const loadReport = useCallback(async () => {
    setLoading(true);
    try {
      const response = await reorderApi.getReport({
        warehouseCode: warehouseFilter || undefined,
        categoryCode: categoryFilter || undefined,
      });
      const sorted = [...(response.data.items ?? [])].sort((a, b) => b.shortageQuantity - a.shortageQuantity);
      setItems(sorted);
      setTotalCount(response.data.totalReorderItemCount);
      setTotalValue(response.data.totalReplenishmentValue);
      setNoItems(response.data.noItemsFound);
    } catch {
      setSnackbar({ open: true, message: 'Failed to load report.', severity: 'error' });
      setNoItems(true);
    } finally {
      setLoading(false);
    }
  }, [warehouseFilter, categoryFilter]);

  useEffect(() => { loadReport(); }, [loadReport]);

  const handlePrint = async () => {
    try {
      await reorderApi.printReport({ warehouseCode: warehouseFilter || undefined, categoryCode: categoryFilter || undefined });
      setSnackbar({ open: true, message: 'Report sent to output queue.', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Print failed. Please try again.', severity: 'error' });
    }
  };

  const getUrgencyChip = (item: ReorderItem) => {
    const ratio = item.reorderPoint > 0 ? item.shortageQuantity / item.reorderPoint : 0;
    if (ratio >= 1) return (
      <Chip label="Critical" size="small"
        sx={{
          fontWeight: 700, fontSize: '0.63rem', height: 20, borderRadius: '6px',
          bgcolor: 'rgba(239,68,68,0.12)', color: '#f87171',
        }} />
    );
    if (ratio >= 0.5) return (
      <Chip label="High" size="small"
        sx={{
          fontWeight: 700, fontSize: '0.63rem', height: 20, borderRadius: '6px',
          bgcolor: 'rgba(245,158,11,0.12)', color: '#fbbf24',
        }} />
    );
    return (
      <Chip label="Low" size="small"
        sx={{
          fontSize: '0.63rem', height: 20, borderRadius: '6px',
          bgcolor: 'rgba(148,163,184,0.1)', color: '#94a3b8',
        }} />
    );
  };

  return (
    <AppLayout title="Reorder Report" subtitle="Items below reorder point requiring replenishment" backTo="/inventory/dashboard">
      {/* Filter bar */}
      <Paper elevation={0} sx={{
        p: 2.5, mb: 3, borderRadius: 3,
        bgcolor: 'rgba(30,41,59,0.6)',
        backdropFilter: 'blur(20px)',
        border: '1px solid rgba(148,163,184,0.1)',
      }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={6} sm={3}>
            <TextField label="Warehouse Code" value={warehouseFilter} onChange={e => setWarehouseFilter(e.target.value)}
              size="small" fullWidth inputProps={{ maxLength: 4 }} placeholder="All warehouses" />
          </Grid>
          <Grid item xs={6} sm={3}>
            <TextField label="Category Code" value={categoryFilter} onChange={e => setCategoryFilter(e.target.value)}
              size="small" fullWidth inputProps={{ maxLength: 6 }} placeholder="All categories" />
          </Grid>
          <Grid item xs="auto">
            <Button variant="outlined" startIcon={<RefreshIcon />} onClick={loadReport}
              sx={{
                borderColor: 'rgba(148,163,184,0.3)', color: '#cbd5e1',
                '&:hover': { borderColor: 'rgba(148,163,184,0.5)', bgcolor: 'rgba(99,102,241,0.08)' },
              }}>
              Refresh
            </Button>
          </Grid>
          <Grid item xs="auto">
            <Button variant="outlined" startIcon={<PrintIcon />} onClick={handlePrint}
              sx={{
                borderColor: 'rgba(139,92,246,0.4)', color: '#a78bfa',
                '&:hover': { borderColor: 'rgba(139,92,246,0.6)', bgcolor: 'rgba(139,92,246,0.08)' },
              }}>
              Print Report
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Summary stat cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6}>
          <Card elevation={0} sx={{
            borderRadius: 3, overflow: 'visible',
            bgcolor: 'rgba(30,41,59,0.6)',
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(148,163,184,0.1)',
          }}>
            <CardContent sx={{ py: 2.5, '&:last-child': { pb: 2.5 } }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{
                  width: 48, height: 48, borderRadius: '14px',
                  background: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  boxShadow: '0 4px 12px rgba(245,158,11,0.3)',
                }}>
                  <TrendingDownIcon sx={{ color: '#fff', fontSize: 24 }} />
                </Box>
                <Box>
                  <Typography variant="caption" sx={{ color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: '0.65rem', fontWeight: 600 }}>
                    Items Below Reorder Point
                  </Typography>
                  <Typography variant="h4" sx={{ fontWeight: 800, color: '#fbbf24', fontFamily: 'monospace', lineHeight: 1.2 }}>
                    {totalCount}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6}>
          <Card elevation={0} sx={{
            borderRadius: 3, overflow: 'visible',
            bgcolor: 'rgba(30,41,59,0.6)',
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(148,163,184,0.1)',
          }}>
            <CardContent sx={{ py: 2.5, '&:last-child': { pb: 2.5 } }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{
                  width: 48, height: 48, borderRadius: '14px',
                  background: 'linear-gradient(135deg, #6366f1 0%, #818cf8 100%)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  boxShadow: '0 4px 12px rgba(99,102,241,0.3)',
                }}>
                  <AttachMoneyIcon sx={{ color: '#fff', fontSize: 24 }} />
                </Box>
                <Box>
                  <Typography variant="caption" sx={{ color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: '0.65rem', fontWeight: 600 }}>
                    Total Replenishment Value
                  </Typography>
                  <Typography variant="h4" sx={{ fontWeight: 800, color: '#818cf8', fontFamily: 'monospace', lineHeight: 1.2 }}>
                    ${totalValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Report table */}
      <Paper elevation={0} sx={{
        borderRadius: 3, overflow: 'hidden',
        bgcolor: 'rgba(30,41,59,0.6)',
        backdropFilter: 'blur(20px)',
        border: '1px solid rgba(148,163,184,0.1)',
      }}>
        {loading ? (
          <Box sx={{ p: 3 }}>
            {[...Array(6)].map((_, i) => (
              <Skeleton key={i} height={44} sx={{ mb: 0.5, borderRadius: 1, bgcolor: 'rgba(148,163,184,0.08)' }} />
            ))}
          </Box>
        ) : noItems ? (
          <Box sx={{ p: 6, textAlign: 'center' }}>
            <WarningAmberIcon sx={{ fontSize: 48, color: '#64748b', mb: 2 }} />
            <Typography variant="h6" sx={{ color: '#94a3b8', mb: 1 }}>No items below reorder point</Typography>
            <Typography variant="body2" sx={{ color: '#64748b' }}>
              {warehouseFilter || categoryFilter ? 'Try adjusting your filter criteria.' : 'All stock levels are healthy.'}
            </Typography>
          </Box>
        ) : (
          <>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: 'rgba(15,23,42,0.5)' }}>
                    {['Item ID', 'Item Name', 'Category', 'Qty On Hand', 'Reorder Point', 'Shortage', 'Supplier', 'Repl. Value', 'Urgency'].map((header, idx) => (
                      <TableCell
                        key={header}
                        align={[3, 4, 5, 7].includes(idx) ? 'right' : 'left'}
                        sx={{
                          color: '#94a3b8',
                          borderBottom: '1px solid rgba(148,163,184,0.08)',
                          fontWeight: 600,
                          fontSize: '0.75rem',
                          textTransform: 'uppercase',
                          letterSpacing: '0.05em',
                        }}
                      >
                        {header}
                      </TableCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {items.map((row, i) => {
                    const isCritical = row.reorderPoint > 0 && row.shortageQuantity / row.reorderPoint >= 1;
                    return (
                      <TableRow key={row.itemId} hover sx={{
                        bgcolor: isCritical ? 'rgba(239,68,68,0.04)' : 'inherit',
                        '&:hover': { bgcolor: isCritical ? 'rgba(239,68,68,0.08)' : 'rgba(99,102,241,0.04)' },
                        animation: 'fadeInUp 0.3s ease-out',
                        animationDelay: `${i * 0.03}s`,
                        animationFillMode: 'both',
                        '& .MuiTableCell-root': {
                          borderBottom: '1px solid rgba(148,163,184,0.08)',
                        },
                      }}>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.82rem', fontWeight: 600, color: '#818cf8' }}>{row.itemId}</TableCell>
                        <TableCell sx={{ fontWeight: 500, color: '#f1f5f9' }}>{row.itemName}</TableCell>
                        <TableCell>
                          <Chip label={row.categoryCode} size="small" variant="outlined"
                            sx={{
                              fontFamily: 'monospace', fontSize: '0.7rem', height: 20, borderRadius: '6px',
                              borderColor: 'rgba(148,163,184,0.2)', color: '#94a3b8',
                            }} />
                        </TableCell>
                        <TableCell align="right" sx={{ fontFamily: 'monospace', color: '#f1f5f9' }}>{row.quantityOnHand.toLocaleString()}</TableCell>
                        <TableCell align="right" sx={{ fontFamily: 'monospace', color: '#f1f5f9' }}>{row.reorderPoint.toLocaleString()}</TableCell>
                        <TableCell align="right" sx={{ fontFamily: 'monospace', color: '#f87171', fontWeight: 700 }}>
                          {row.shortageQuantity.toLocaleString()}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.82rem', color: '#94a3b8' }}>{row.supplierCode}</TableCell>
                        <TableCell align="right" sx={{ fontFamily: 'monospace', fontWeight: 600, color: '#f1f5f9' }}>
                          ${row.itemReplenishmentValue?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00'}
                        </TableCell>
                        <TableCell>{getUrgencyChip(row)}</TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
            <Box sx={{
              px: 2.5, py: 1.5,
              bgcolor: 'rgba(15,23,42,0.5)',
              borderTop: '1px solid rgba(148,163,184,0.08)',
              display: 'flex', justifyContent: 'space-between',
            }}>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 500 }}>
                {items.length} item{items.length !== 1 ? 's' : ''} — Sorted by shortage (highest first)
              </Typography>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600 }}>
                Total: ${totalValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </Typography>
            </Box>
          </>
        )}
      </Paper>

      <Snackbar open={snackbar.open} autoHideDuration={4000}
        onClose={() => setSnackbar(s => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar(s => ({ ...s, open: false }))}
          sx={{ borderRadius: 2, boxShadow: '0 8px 24px rgba(0,0,0,0.4)' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </AppLayout>
  );
};

export default ReorderReport;
