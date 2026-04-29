/**
 * InventoryList.tsx — SCR-002: Inventory Item List
 * R-08-A: HTTP actions via typed API client
 * R-08-C: Action buttons replace legacy "2=Edit 4=Delete 5=Display" Op column
 * R-08-G: READ-ONLY list wired to repository query
 * BR-008–022, DT-002, DT-003
 */

import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, TextField, Button, Table, TableHead, TableRow, TableCell,
  TableBody, Paper, IconButton, Tooltip, Chip, InputAdornment,
  Snackbar, Alert, Skeleton, Stack, TableContainer, ToggleButtonGroup, ToggleButton,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import InventoryIcon from '@mui/icons-material/Inventory2';
import AppLayout from '../components/AppLayout';
import { listApi, InventoryListRow } from '../services/inventoryApi';

const InventoryList: React.FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<InventoryListRow[]>([]);
  const [categoryFilter, setCategoryFilter] = useState('');
  const [nameFilter, setNameFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  const [noRecords, setNoRecords] = useState(false);
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' }>({
    open: false, message: '', severity: 'success',
  });

  const showSnack = (message: string, severity: 'success' | 'error' | 'info' = 'success') =>
    setSnackbar({ open: true, message, severity });

  const loadList = useCallback(async () => {
    setLoading(true);
    try {
      const statusParam = statusFilter === 'ALL' ? undefined : statusFilter;
      const response = await listApi.getList(categoryFilter || undefined, nameFilter || undefined, statusParam);
      setItems(response.data.items);
      setNoRecords(response.data.noRecordsFound);
    } catch {
      showSnack('Failed to load inventory list.', 'error');
      setNoRecords(true);
    } finally {
      setLoading(false);
    }
  }, [categoryFilter, nameFilter, statusFilter]);

  useEffect(() => { loadList(); }, [loadList]);

  const handleEdit = (itemId: string) => navigate(`/inventory/items/${itemId}?mode=E`);
  const handleView = (itemId: string) => navigate(`/inventory/items/${itemId}?mode=D`);

  const handleDelete = async (itemId: string) => {
    try {
      const resp = await listApi.rowAction(itemId, '4');
      const data = resp.data as { deleteBlockedByAllocation?: boolean };
      if (data.deleteBlockedByAllocation) {
        showSnack('Cannot delete — item has active allocations.', 'error');
      } else {
        showSnack('Item deactivated successfully.');
        loadList();
      }
    } catch {
      showSnack('Delete failed. Please try again.', 'error');
    }
  };

  const getStatusChip = (status: string) => {
    const config: Record<string, { bg: string; color: string; border: string }> = {
      ACTIVE: { bg: 'rgba(16,185,129,0.12)', color: '#34d399', border: 'rgba(16,185,129,0.25)' },
      INACTIVE: { bg: 'rgba(148,163,184,0.12)', color: '#94a3b8', border: 'rgba(148,163,184,0.25)' },
      PENDING_DELETE: { bg: 'rgba(245,158,11,0.12)', color: '#fbbf24', border: 'rgba(245,158,11,0.25)' },
      DELETED: { bg: 'rgba(239,68,68,0.12)', color: '#f87171', border: 'rgba(239,68,68,0.25)' },
    };
    const c = config[status] ?? config.INACTIVE;
    return (
      <Chip
        label={status ?? 'ACTIVE'}
        size="small"
        sx={{
          fontWeight: 600, fontSize: '0.65rem', height: 22, borderRadius: '6px',
          bgcolor: c.bg, color: c.color, border: `1px solid ${c.border}`,
        }}
      />
    );
  };

  return (
    <AppLayout title="Inventory Item List" subtitle="Browse and manage inventory records" backTo="/inventory/dashboard">
      <Paper
        elevation={0}
        sx={{
          borderRadius: 3,
          overflow: 'hidden',
        }}
      >
        {/* Toolbar */}
        <Box sx={{
          p: 2.5,
          display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center',
          borderBottom: '1px solid rgba(148,163,184,0.08)',
        }}>
          <TextField
            label="Category"
            value={categoryFilter}
            onChange={e => setCategoryFilter(e.target.value)}
            size="small"
            inputProps={{ maxLength: 6 }}
            sx={{ width: 150 }}
            InputProps={{
              startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" sx={{ color: '#64748b' }} /></InputAdornment>,
            }}
          />
          <TextField
            label="Item Name"
            value={nameFilter}
            onChange={e => setNameFilter(e.target.value)}
            size="small"
            inputProps={{ maxLength: 15 }}
            sx={{ width: 220 }}
            InputProps={{
              startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" sx={{ color: '#64748b' }} /></InputAdornment>,
            }}
          />
          <ToggleButtonGroup
            value={statusFilter}
            exclusive
            onChange={(_, val) => { if (val) setStatusFilter(val); }}
            size="small"
            sx={{
              '& .MuiToggleButton-root': {
                textTransform: 'none', fontSize: '0.75rem', px: 1.5, py: 0.5,
                color: '#94a3b8', borderColor: 'rgba(148,163,184,0.2)',
                '&.Mui-selected': { bgcolor: 'rgba(99,102,241,0.15)', color: '#818cf8', borderColor: 'rgba(99,102,241,0.3)' },
              },
            }}
          >
            <ToggleButton value="ALL">All</ToggleButton>
            <ToggleButton value="ACTIVE">Active</ToggleButton>
            <ToggleButton value="INACTIVE">Inactive</ToggleButton>
          </ToggleButtonGroup>
          <Tooltip title="Refresh list">
            <IconButton
              onClick={loadList}
              size="small"
              sx={{
                bgcolor: 'rgba(148,163,184,0.08)',
                border: '1px solid rgba(148,163,184,0.1)',
                '&:hover': { bgcolor: 'rgba(148,163,184,0.15)' },
                color: '#94a3b8',
              }}
            >
              <RefreshIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Box sx={{ flexGrow: 1 }} />
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/inventory/items/new?mode=A')}
          >
            Add New Item
          </Button>
        </Box>

        {/* Table */}
        {loading ? (
          <Box sx={{ p: 3 }}>
            {[...Array(6)].map((_, i) => <Skeleton key={i} height={44} sx={{ mb: 0.5, borderRadius: 1 }} />)}
          </Box>
        ) : noRecords ? (
          <Box sx={{ p: 8, textAlign: 'center' }}>
            <Box sx={{
              width: 72, height: 72, borderRadius: '50%', mx: 'auto', mb: 3,
              background: 'rgba(148,163,184,0.08)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <InventoryIcon sx={{ fontSize: 36, color: '#475569' }} />
            </Box>
            <Typography variant="h6" sx={{ color: '#94a3b8', mb: 1 }}>No items found</Typography>
            <Typography variant="body2" sx={{ color: '#64748b' }}>
              No records match the current filters. Try adjusting your search criteria.
            </Typography>
          </Box>
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ width: 110 }}>Actions</TableCell>
                  <TableCell>Item ID</TableCell>
                  <TableCell>Item Name</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell align="right">Qty On Hand</TableCell>
                  <TableCell align="right">Unit Price</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {items.map((row, i) => (
                  <TableRow
                    key={row.itemId}
                    hover
                    sx={{
                      transition: 'background-color 0.15s',
                      '&:hover': { bgcolor: 'rgba(99,102,241,0.04)' },
                      animation: 'fadeInUp 0.3s ease-out',
                      animationDelay: `${i * 0.03}s`,
                      animationFillMode: 'both',
                    }}
                  >
                    <TableCell sx={{ py: 0.75 }}>
                      <Stack direction="row" spacing={0.5}>
                        <Tooltip title="Edit item" arrow>
                          <IconButton
                            size="small"
                            onClick={() => handleEdit(row.itemId)}
                            sx={{
                              bgcolor: 'rgba(59,130,246,0.1)',
                              border: '1px solid rgba(59,130,246,0.15)',
                              '&:hover': { bgcolor: 'rgba(59,130,246,0.2)' },
                            }}
                          >
                            <EditIcon sx={{ fontSize: 14, color: '#60a5fa' }} />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="View details" arrow>
                          <IconButton
                            size="small"
                            onClick={() => handleView(row.itemId)}
                            sx={{
                              bgcolor: 'rgba(148,163,184,0.08)',
                              border: '1px solid rgba(148,163,184,0.12)',
                              '&:hover': { bgcolor: 'rgba(148,163,184,0.15)' },
                            }}
                          >
                            <VisibilityIcon sx={{ fontSize: 14, color: '#94a3b8' }} />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete item" arrow>
                          <IconButton
                            size="small"
                            onClick={() => handleDelete(row.itemId)}
                            sx={{
                              bgcolor: 'rgba(239,68,68,0.08)',
                              border: '1px solid rgba(239,68,68,0.12)',
                              '&:hover': { bgcolor: 'rgba(239,68,68,0.15)' },
                            }}
                          >
                            <DeleteIcon sx={{ fontSize: 14, color: '#f87171' }} />
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </TableCell>
                    <TableCell sx={{ fontFamily: '"JetBrains Mono", monospace', fontSize: '0.82rem', fontWeight: 600, color: '#818cf8' }}>
                      {row.itemId}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 500, color: '#e2e8f0' }}>{row.itemName}</TableCell>
                    <TableCell>
                      <Chip
                        label={row.categoryCode}
                        size="small"
                        variant="outlined"
                        sx={{
                          fontFamily: 'monospace', fontSize: '0.7rem', height: 22, borderRadius: '6px',
                          borderColor: 'rgba(148,163,184,0.2)', color: '#94a3b8',
                        }}
                      />
                    </TableCell>
                    <TableCell align="right" sx={{ fontFamily: 'monospace', fontWeight: 500, color: '#e2e8f0' }}>
                      {row.quantityOnHand.toLocaleString()}
                    </TableCell>
                    <TableCell align="right" sx={{ fontFamily: 'monospace', fontWeight: 600, color: '#34d399' }}>
                      ${row.unitPrice?.toFixed(2) ?? '—'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {!loading && !noRecords && items.length > 0 && (
          <Box sx={{
            px: 2.5, py: 1.5,
            borderTop: '1px solid rgba(148,163,184,0.08)',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          }}>
            <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 500 }}>
              {items.length} record{items.length !== 1 ? 's' : ''} found
            </Typography>
            <Chip
              label="Inventory"
              size="small"
              variant="outlined"
              sx={{ fontSize: '0.62rem', height: 20, borderColor: 'rgba(148,163,184,0.15)', color: '#64748b' }}
            />
          </Box>
        )}
      </Paper>

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar(s => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert
          severity={snackbar.severity}
          onClose={() => setSnackbar(s => ({ ...s, open: false }))}
          sx={{ borderRadius: 2, boxShadow: '0 12px 30px rgba(0,0,0,0.3)' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </AppLayout>
  );
};

export default InventoryList;
