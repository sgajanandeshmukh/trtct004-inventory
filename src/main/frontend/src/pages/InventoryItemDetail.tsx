/**
 * InventoryItemDetail.tsx — SCR-003/004: Item Maintenance
 * R-08-B: React Hook Form + Zod schema validation (replaces manual state + catch-all alerts)
 * R-08-C: INQ/ADD/MOD function modes with field-level access control
 * R-08-D: Gross margin is a derived field — computed client-side, never submitted
 * R-08-G: WRITE-ON-SAVE wired to saveItem repository call
 * BR-023–054, DT-004, DT-005, DT-006, ALG-001
 */

import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box, Typography, TextField, Button, Paper, Grid, Chip,
  Snackbar, Alert, Skeleton, Stack, Tooltip
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import EditIcon from '@mui/icons-material/Edit';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import BadgeIcon from '@mui/icons-material/Badge';
import CategoryIcon from '@mui/icons-material/Category';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import AppLayout from '../components/AppLayout';
import { itemApi, InventoryItemRequest, InventoryItemResponse } from '../services/inventoryApi';

// ─── R-08-B: Zod validation schema ───────────────────────────────────────────
const itemSchema = z.object({
  itemId: z.string().min(1, 'Item ID is required').max(8, 'Maximum 8 characters'),
  itemName: z.string().min(1, 'Item Name is required').max(30, 'Maximum 30 characters'),
  itemDescription: z.string().max(50, 'Maximum 50 characters').optional().or(z.literal('')),
  categoryCode: z.string().max(6, 'Maximum 6 characters').optional().or(z.literal('')),
  supplierCode: z.string().max(6, 'Maximum 6 characters').optional().or(z.literal('')),
  warehouseCode: z.string().max(4, 'Maximum 4 characters').optional().or(z.literal('')),
  quantityOnHand: z.coerce.number().int('Must be a whole number').min(0, 'Cannot be negative').optional(),
  reorderPoint: z.coerce.number().int('Must be a whole number').min(0, 'Cannot be negative').optional(),
  reorderQuantity: z.coerce.number().int('Must be a whole number').min(0, 'Cannot be negative').optional(),
  unitCost: z.coerce.number().min(0, 'Must be >= 0').optional(),
  unitPrice: z.coerce.number().min(0, 'Must be >= 0').optional(),
}).refine(
  data => {
    if (data.unitPrice !== undefined && data.unitCost !== undefined) {
      return data.unitPrice >= data.unitCost;
    }
    return true;
  },
  { message: 'Unit Price must be greater than or equal to Unit Cost', path: ['unitPrice'] }
);

type ItemFormValues = z.infer<typeof itemSchema>;

// ─── Mode config ─────────────────────────────────────────────────────────────
const MODE_CONFIG = {
  A: { label: 'Add Mode',     color: 'primary'   as const, editable: true,  showSave: true,  gradient: 'linear-gradient(135deg, #6366f1 0%, #818cf8 100%)' },
  E: { label: 'Edit Mode',    color: 'secondary' as const, editable: true,  showSave: true,  gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)' },
  D: { label: 'View Mode',    color: 'default'   as const, editable: false, showSave: false, gradient: 'linear-gradient(135deg, #475569 0%, #64748b 100%)' },
};

const SectionHeader: React.FC<{ icon: React.ReactNode; title: string }> = ({ icon, title }) => (
  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2.5 }}>
    <Box sx={{
      width: 32, height: 32, borderRadius: '8px',
      bgcolor: 'rgba(148,163,184,0.06)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      {icon}
    </Box>
    <Typography variant="subtitle2" sx={{
      textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: '0.7rem', color: '#94a3b8',
    }}>
      {title}
    </Typography>
  </Box>
);

const InventoryItemDetail: React.FC = () => {
  const { itemId } = useParams<{ itemId: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const mode = (searchParams.get('mode') ?? 'D') as 'A' | 'E' | 'D';
  const modeConfig = MODE_CONFIG[mode];

  const [serverData, setServerData] = useState<InventoryItemResponse>({} as InventoryItemResponse);
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false, message: '', severity: 'success',
  });

  const { control, handleSubmit, watch, reset, formState: { errors, isSubmitting } } = useForm<ItemFormValues>({
    resolver: zodResolver(itemSchema),
    defaultValues: {
      itemId: '', itemName: '', itemDescription: '', categoryCode: '',
      supplierCode: '', warehouseCode: '', quantityOnHand: 0,
      reorderPoint: 0, reorderQuantity: 0, unitCost: 0, unitPrice: 0,
    },
  });

  const watchedCost = watch('unitCost') ?? 0;
  const watchedPrice = watch('unitPrice') ?? 0;

  // ALG-001: Dynamic gross margin
  const grossMargin = watchedPrice > 0
    ? (((watchedPrice - watchedCost) / watchedPrice) * 100).toFixed(2)
    : '0.00';

  const marginColor = parseFloat(grossMargin) >= 20 ? '#34d399'
    : parseFloat(grossMargin) >= 0 ? '#fbbf24' : '#f87171';

  useEffect(() => {
    if (mode !== 'A' && itemId && itemId !== 'new') {
      setLoading(true);
      itemApi.getItem(itemId, mode)
        .then(res => {
          setServerData(res.data);
          reset({
            itemId: res.data.itemId ?? '', itemName: res.data.itemName ?? '',
            itemDescription: res.data.itemDescription ?? '', categoryCode: res.data.categoryCode ?? '',
            supplierCode: res.data.supplierCode ?? '', warehouseCode: res.data.warehouseCode ?? '',
            quantityOnHand: res.data.quantityOnHand ?? 0, reorderPoint: res.data.reorderPoint ?? 0,
            reorderQuantity: res.data.reorderQuantity ?? 0, unitCost: res.data.unitCost ?? 0,
            unitPrice: res.data.unitPrice ?? 0,
          });
        })
        .finally(() => setLoading(false));
    }
  }, [itemId, mode, reset]);

  const onSubmit = async (values: ItemFormValues) => {
    const request: InventoryItemRequest = {
      itemId: mode === 'A' ? values.itemId : itemId,
      itemName: values.itemName, itemDescription: values.itemDescription,
      categoryCode: values.categoryCode, supplierCode: values.supplierCode,
      warehouseCode: values.warehouseCode, quantityOnHand: values.quantityOnHand,
      reorderPoint: values.reorderPoint, reorderQuantity: values.reorderQuantity,
      unitCost: values.unitCost, unitPrice: values.unitPrice, mode,
    };

    try {
      const response = await itemApi.saveItem(request);
      if (response.data.validationFailed) {
        const msgs = response.data.validationErrors?.join(' | ') ?? 'Validation failed.';
        setSnackbar({ open: true, message: msgs, severity: 'error' });
      } else {
        setSnackbar({ open: true, message: 'Record saved successfully.', severity: 'success' });
        if (mode === 'A' && response.data.itemId) {
          navigate(`/inventory/items/${response.data.itemId}?mode=E`);
        }
      }
    } catch (err: any) {
      const msg = err.response?.data?.validationErrors?.join(' | ') ?? 'Save failed. Please try again.';
      setSnackbar({ open: true, message: msg, severity: 'error' });
    }
  };

  const badgeColor = modeConfig.color === 'primary' ? 'primary'
    : modeConfig.color === 'secondary' ? 'secondary' : 'default';

  const pageTitle = mode === 'A' ? 'Add New Item'
    : mode === 'E' ? `Edit Item — ${itemId}` : `View Item — ${itemId}`;

  if (loading) {
    return (
      <AppLayout title={pageTitle} backTo="/inventory/list" badge={{ label: modeConfig.label, color: badgeColor }}>
        <Paper elevation={0} sx={{
          p: 4, borderRadius: 3,
          background: 'rgba(30,41,59,0.6)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(148,163,184,0.1)',
        }}>
          {[...Array(8)].map((_, i) => (
            <Skeleton key={i} height={50} sx={{
              mb: 1, borderRadius: 1,
              bgcolor: 'rgba(148,163,184,0.08)',
            }} />
          ))}
        </Paper>
      </AppLayout>
    );
  }

  if (serverData.recordNotFound) {
    return (
      <AppLayout title="Item Not Found" backTo="/inventory/list">
        <Alert severity="error" sx={{
          mt: 2, borderRadius: 2,
          bgcolor: 'rgba(239,68,68,0.12)',
          color: '#f87171',
          border: '1px solid rgba(239,68,68,0.25)',
          '& .MuiAlert-icon': { color: '#f87171' },
        }}>
          Item record <strong>{itemId}</strong> does not exist.
        </Alert>
      </AppLayout>
    );
  }

  // ─── Glassmorphic Paper shared styles ──────────────────────────────────────
  const glassPaperSx = {
    p: 3,
    borderRadius: 3,
    mb: 2,
    background: 'rgba(30,41,59,0.6)',
    backdropFilter: 'blur(20px)',
    border: '1px solid rgba(148,163,184,0.1)',
    animation: 'fadeInUp 0.5s ease-out both',
  };

  // ─── Disabled field styling for dark theme ─────────────────────────────────
  const disabledFieldSx = {
    '& .MuiInputBase-input.Mui-disabled': {
      color: '#94a3b8',
      WebkitTextFillColor: '#94a3b8',
    },
  };

  return (
    <AppLayout title={pageTitle} backTo="/inventory/list" badge={{ label: modeConfig.label, color: badgeColor }}>
      {serverData.status === 'INACTIVE' && (
        <Alert severity="warning" sx={{
          mb: 2, borderRadius: 2,
          bgcolor: 'rgba(245,158,11,0.12)',
          color: '#fbbf24',
          border: '1px solid rgba(245,158,11,0.25)',
          '& .MuiAlert-icon': { color: '#fbbf24' },
        }}>
          This item is <strong>Inactive</strong>. It is not available for new transactions.
        </Alert>
      )}

      <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
        {/* ── Section 1: Item Identity ─────────────────────────────────── */}
        <Paper elevation={0} sx={glassPaperSx}>
          <SectionHeader icon={<BadgeIcon sx={{ fontSize: 18, color: '#818cf8' }} />} title="Item Identity" />
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <Controller name="itemId" control={control} render={({ field }) => (
                <TextField {...field} label="Item ID *" fullWidth size="small" disabled={mode !== 'A'}
                  error={!!errors.itemId} helperText={errors.itemId?.message}
                  inputProps={{ maxLength: 8, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={12} sm={8}>
              <Controller name="itemName" control={control} render={({ field }) => (
                <TextField {...field} label="Item Name *" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.itemName} helperText={errors.itemName?.message} inputProps={{ maxLength: 30 }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="itemDescription" control={control} render={({ field }) => (
                <TextField {...field} label="Description" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.itemDescription} helperText={errors.itemDescription?.message} inputProps={{ maxLength: 50 }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
          </Grid>
        </Paper>

        {/* ── Section 2: Classification ────────────────────────────────── */}
        <Paper elevation={0} sx={{ ...glassPaperSx, animationDelay: '0.05s' }}>
          <SectionHeader icon={<CategoryIcon sx={{ fontSize: 18, color: '#818cf8' }} />} title="Classification" />
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <Controller name="categoryCode" control={control} render={({ field }) => (
                <TextField {...field} label="Category Code" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.categoryCode} helperText={errors.categoryCode?.message}
                  inputProps={{ maxLength: 6, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={12} sm={8}>
              <TextField label="Category Name" fullWidth size="small" value={serverData.categoryName ?? ''} disabled
                sx={disabledFieldSx} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <Controller name="supplierCode" control={control} render={({ field }) => (
                <TextField {...field} label="Supplier Code" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.supplierCode} helperText={errors.supplierCode?.message}
                  inputProps={{ maxLength: 6, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={12} sm={8}>
              <TextField label="Supplier Name" fullWidth size="small" value={serverData.supplierName ?? ''} disabled
                sx={disabledFieldSx} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <Controller name="warehouseCode" control={control} render={({ field }) => (
                <TextField {...field} label="Warehouse Code" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.warehouseCode} helperText={errors.warehouseCode?.message}
                  inputProps={{ maxLength: 4, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={12} sm={8}>
              <TextField label="Warehouse Name" fullWidth size="small" value={serverData.warehouseName ?? ''} disabled
                sx={disabledFieldSx} />
            </Grid>
          </Grid>
        </Paper>

        {/* ── Section 3: Stock Quantities ──────────────────────────────── */}
        <Paper elevation={0} sx={{ ...glassPaperSx, animationDelay: '0.1s' }}>
          <SectionHeader icon={<WarehouseIcon sx={{ fontSize: 18, color: '#818cf8' }} />} title="Stock Quantities" />
          <Grid container spacing={2}>
            <Grid item xs={6} sm={3}>
              <Controller name="quantityOnHand" control={control} render={({ field }) => (
                <TextField {...field} label="Qty On Hand" type="number" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.quantityOnHand} helperText={errors.quantityOnHand?.message}
                  inputProps={{ min: 0, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <TextField label="Qty Allocated" fullWidth size="small" value={serverData.quantityAllocated ?? 0} disabled
                InputProps={{ endAdornment: (
                  <Tooltip title="Calculated by allocation system"><InfoOutlinedIcon sx={{ fontSize: 14, color: '#64748b' }} /></Tooltip>
                )}}
                sx={disabledFieldSx} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <TextField label="Qty On Order" fullWidth size="small" value={serverData.quantityOnOrder ?? 0} disabled
                InputProps={{ endAdornment: (
                  <Tooltip title="Calculated by procurement system"><InfoOutlinedIcon sx={{ fontSize: 14, color: '#64748b' }} /></Tooltip>
                )}}
                sx={disabledFieldSx} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <Controller name="reorderPoint" control={control} render={({ field }) => (
                <TextField {...field} label="Reorder Point" type="number" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.reorderPoint} helperText={errors.reorderPoint?.message}
                  inputProps={{ min: 0, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <Controller name="reorderQuantity" control={control} render={({ field }) => (
                <TextField {...field} label="Reorder Qty" type="number" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.reorderQuantity} helperText={errors.reorderQuantity?.message}
                  inputProps={{ min: 0, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
          </Grid>
        </Paper>

        {/* ── Section 4: Pricing ───────────────────────────────────────── */}
        <Paper elevation={0} sx={{ ...glassPaperSx, animationDelay: '0.15s' }}>
          <SectionHeader icon={<AttachMoneyIcon sx={{ fontSize: 18, color: '#818cf8' }} />} title="Pricing" />
          <Grid container spacing={2} alignItems="flex-start">
            <Grid item xs={6} sm={3}>
              <Controller name="unitCost" control={control} render={({ field }) => (
                <TextField {...field} label="Unit Cost ($)" type="number" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.unitCost} helperText={errors.unitCost?.message}
                  inputProps={{ step: '0.01', min: 0, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <Controller name="unitPrice" control={control} render={({ field }) => (
                <TextField {...field} label="Unit Price ($)" type="number" fullWidth size="small" disabled={!modeConfig.editable}
                  error={!!errors.unitPrice} helperText={errors.unitPrice?.message ?? 'Must be >= Unit Cost'}
                  inputProps={{ step: '0.01', min: 0, style: { fontFamily: 'monospace' } }}
                  sx={disabledFieldSx} />
              )} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <TextField label="Gross Margin %" value={grossMargin} fullWidth size="small" disabled
                InputProps={{
                  endAdornment: (
                    <Tooltip title="Derived: (Price - Cost) / Price x 100">
                      <InfoOutlinedIcon sx={{ fontSize: 14, color: '#64748b' }} />
                    </Tooltip>
                  ),
                  style: { color: marginColor, fontFamily: 'monospace', fontWeight: 700 },
                }}
                sx={{ '& .MuiInputBase-input.Mui-disabled': { WebkitTextFillColor: marginColor } }} />
            </Grid>
            <Grid item xs={6} sm={3}>
              <TextField label="Status" value={serverData.status ?? (mode === 'A' ? 'ACTIVE' : '—')} fullWidth size="small" disabled
                sx={disabledFieldSx} />
            </Grid>
          </Grid>
        </Paper>

        {/* ── Action buttons ────────────── */}
        <Paper elevation={0} sx={{
          p: 2, borderRadius: 3,
          background: 'rgba(30,41,59,0.6)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(148,163,184,0.1)',
          animation: 'fadeInUp 0.5s ease-out both',
          animationDelay: '0.2s',
        }}>
          <Stack direction="row" spacing={1.5}>
            {modeConfig.showSave && (
              <Button type="submit" variant="contained" startIcon={<SaveIcon />} disabled={isSubmitting}
                sx={{
                  minWidth: 120,
                  background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                  boxShadow: '0 4px 15px rgba(99,102,241,0.3)',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #818cf8 0%, #a78bfa 100%)',
                    boxShadow: '0 6px 20px rgba(99,102,241,0.4)',
                  },
                }}>
                {isSubmitting ? 'Saving...' : 'Save'}
              </Button>
            )}
            <Button variant="outlined" startIcon={<CancelIcon />} onClick={() => navigate('/inventory/list')}
              sx={{
                borderColor: 'rgba(148,163,184,0.3)',
                color: '#94a3b8',
                '&:hover': {
                  borderColor: 'rgba(148,163,184,0.5)',
                  bgcolor: 'rgba(148,163,184,0.08)',
                },
              }}>
              {modeConfig.showSave ? 'Cancel' : 'Back to List'}
            </Button>
            {mode === 'D' && (
              <Button variant="outlined" color="secondary" startIcon={<EditIcon />}
                onClick={() => navigate(`/inventory/items/${itemId}?mode=E`)}
                sx={{
                  borderColor: 'rgba(139,92,246,0.4)',
                  color: '#a78bfa',
                  '&:hover': {
                    borderColor: 'rgba(139,92,246,0.6)',
                    bgcolor: 'rgba(139,92,246,0.08)',
                  },
                }}>
                Edit This Item
              </Button>
            )}
          </Stack>
        </Paper>
      </Box>

      <Snackbar open={snackbar.open} autoHideDuration={4000}
        onClose={() => setSnackbar(s => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar(s => ({ ...s, open: false }))}
          sx={{
            borderRadius: 2,
            boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
            ...(snackbar.severity === 'success' ? {
              bgcolor: 'rgba(16,185,129,0.15)',
              color: '#34d399',
              border: '1px solid rgba(16,185,129,0.25)',
              '& .MuiAlert-icon': { color: '#34d399' },
            } : {
              bgcolor: 'rgba(239,68,68,0.15)',
              color: '#f87171',
              border: '1px solid rgba(239,68,68,0.25)',
              '& .MuiAlert-icon': { color: '#f87171' },
            }),
          }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </AppLayout>
  );
};

export default InventoryItemDetail;
