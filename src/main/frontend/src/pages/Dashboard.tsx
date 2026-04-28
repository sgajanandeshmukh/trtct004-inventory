import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Grid, Paper, Typography, Chip, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, CircularProgress, IconButton, Tooltip, LinearProgress,
} from '@mui/material';
import {
  Inventory2 as InventoryIcon, TrendingDown, TrendingUp, Warning,
  AttachMoney, Assessment, ListAlt, Add, BarChart, History,
  Upload, Download, ViewList,
} from '@mui/icons-material';
import AppLayout from '../components/AppLayout';
import { dashboardApi, DashboardResponse, TransactionRecord } from '../services/inventoryApi';

const txnTypeLabels: Record<string, { label: string; color: 'success' | 'info' | 'warning' | 'error' | 'default' }> = {
  CR: { label: 'Created', color: 'success' },
  UP: { label: 'Updated', color: 'info' },
  SC: { label: 'Status Change', color: 'warning' },
  QA: { label: 'Qty Adjusted', color: 'default' },
  SR: { label: 'Received', color: 'success' },
};

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardApi.getDashboard()
      .then((res) => setData(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <AppLayout title="Dashboard" subtitle="Loading...">
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 10 }}>
          <CircularProgress size={60} />
        </Box>
      </AppLayout>
    );
  }

  if (!data) {
    return (
      <AppLayout title="Dashboard" subtitle="Inventory Intelligence">
        <Typography color="error">Failed to load dashboard data.</Typography>
      </AppLayout>
    );
  }

  const { kpis } = data;

  const healthColor = kpis.inventoryHealthScore >= 80 ? '#10b981' :
    kpis.inventoryHealthScore >= 50 ? '#f59e0b' : '#ef4444';

  const navCards = [
    { title: 'Browse Inventory', icon: <ViewList />, path: '/inventory/list', color: '#3b82f6' },
    { title: 'Add New Item', icon: <Add />, path: '/inventory/items/new?mode=A', color: '#10b981' },
    { title: 'Reorder Report', icon: <Assessment />, path: '/inventory/report/reorder', color: '#f59e0b' },
    { title: 'Transaction History', icon: <History />, path: '/inventory/transactions', color: '#8b5cf6' },
    { title: 'Bulk Operations', icon: <Upload />, path: '/inventory/bulk', color: '#ec4899' },
    { title: 'API Documentation', icon: <BarChart />, path: '', color: '#6366f1', external: '/swagger-ui.html' },
  ];

  return (
    <AppLayout title="Dashboard" subtitle="Inventory Intelligence" badge="FR-001">
      {/* Health Score */}
      <Paper sx={{ p: 3, mb: 3, background: 'rgba(255,255,255,0.03)', backdropFilter: 'blur(10px)' }}>
        <Grid container spacing={3} alignItems="center">
          <Grid item xs={12} md={3}>
            <Box sx={{ textAlign: 'center' }}>
              <Box sx={{ position: 'relative', display: 'inline-flex' }}>
                <CircularProgress
                  variant="determinate"
                  value={kpis.inventoryHealthScore}
                  size={120}
                  thickness={6}
                  sx={{ color: healthColor }}
                />
                <Box sx={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
                  <Typography variant="h4" fontWeight={700} sx={{ color: healthColor }}>{kpis.inventoryHealthScore}</Typography>
                  <Typography variant="caption" color="text.secondary">Health</Typography>
                </Box>
              </Box>
            </Box>
          </Grid>
          <Grid item xs={12} md={9}>
            <Grid container spacing={2}>
              {[
                { label: 'Total Items', value: kpis.totalItems, icon: <InventoryIcon />, color: '#818cf8' },
                { label: 'Active', value: kpis.activeItems, icon: <TrendingUp />, color: '#34d399' },
                { label: 'Below Reorder', value: kpis.belowReorderPoint, icon: <TrendingDown />, color: '#fbbf24' },
                { label: 'Out of Stock', value: kpis.outOfStock, icon: <Warning />, color: '#f87171' },
                { label: 'Inventory Value', value: `$${Number(kpis.totalInventoryValue).toLocaleString()}`, icon: <AttachMoney />, color: '#2dd4bf' },
                { label: 'Avg Margin', value: `${Number(kpis.averageMarginPercent).toFixed(1)}%`, icon: <Assessment />, color: '#a78bfa' },
              ].map((kpi, idx) => (
                <Grid item xs={6} sm={4} md={2} key={idx}>
                  <Paper sx={{ p: 1.5, textAlign: 'center', background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.08)' }}>
                    <Box sx={{ color: kpi.color, mb: 0.5 }}>{kpi.icon}</Box>
                    <Typography variant="h6" fontWeight={700}>{kpi.value}</Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.65rem' }}>{kpi.label}</Typography>
                  </Paper>
                </Grid>
              ))}
            </Grid>
          </Grid>
        </Grid>
      </Paper>

      {/* Navigation Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {navCards.map((card, idx) => (
          <Grid item xs={6} sm={4} md={2} key={idx}>
            <Paper
              onClick={() => card.external ? window.open(card.external, '_blank') : navigate(card.path)}
              sx={{
                p: 2, textAlign: 'center', cursor: 'pointer',
                background: `linear-gradient(135deg, ${card.color}15, ${card.color}08)`,
                border: `1px solid ${card.color}30`,
                transition: 'all 0.3s',
                '&:hover': { transform: 'translateY(-4px)', boxShadow: `0 8px 25px ${card.color}20` },
              }}
            >
              <Box sx={{ color: card.color, mb: 1 }}>{card.icon}</Box>
              <Typography variant="body2" fontWeight={600}>{card.title}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Category Distribution */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, background: 'rgba(255,255,255,0.03)' }}>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
              <ListAlt sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
              Category Distribution
            </Typography>
            {data.categoryDistribution.map((cat) => (
              <Box key={cat.categoryCode} sx={{ mb: 1.5 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="body2">{cat.categoryCode}</Typography>
                  <Typography variant="body2" color="text.secondary">{cat.itemCount} items &middot; ${Number(cat.totalValue).toLocaleString()}</Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={Math.min(100, (cat.itemCount / Math.max(1, kpis.activeItems)) * 100)}
                  sx={{ height: 6, borderRadius: 3 }}
                />
              </Box>
            ))}
            {data.categoryDistribution.length === 0 && <Typography color="text.secondary" variant="body2">No category data available</Typography>}
          </Paper>
        </Grid>

        {/* Warehouse Distribution */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, background: 'rgba(255,255,255,0.03)' }}>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
              <BarChart sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
              Warehouse Distribution
            </Typography>
            {data.warehouseDistribution.map((wh) => (
              <Box key={wh.warehouseCode} sx={{ mb: 1.5 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="body2">{wh.warehouseCode}</Typography>
                  <Typography variant="body2" color="text.secondary">{wh.itemCount} items &middot; {Number(wh.totalQuantity).toLocaleString()} units</Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={Math.min(100, (wh.itemCount / Math.max(1, kpis.activeItems)) * 100)}
                  color="secondary"
                  sx={{ height: 6, borderRadius: 3 }}
                />
              </Box>
            ))}
            {data.warehouseDistribution.length === 0 && <Typography color="text.secondary" variant="body2">No warehouse data available</Typography>}
          </Paper>
        </Grid>

        {/* Recent Activity */}
        <Grid item xs={12}>
          <Paper sx={{ p: 2, background: 'rgba(255,255,255,0.03)' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="subtitle1" fontWeight={600}>
                <History sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
                Recent Activity
              </Typography>
              <Chip label={`${data.recentActivityCount} transactions`} size="small" variant="outlined" />
            </Box>
            {data.recentActivity.length > 0 ? (
              <TableContainer sx={{ maxHeight: 320 }}>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell>Type</TableCell>
                      <TableCell>Item ID</TableCell>
                      <TableCell>Quantity</TableCell>
                      <TableCell>Reference</TableCell>
                      <TableCell>Date</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.recentActivity.map((txn: TransactionRecord) => {
                      const typeInfo = txnTypeLabels[txn.transactionType] || { label: txn.transactionType, color: 'default' as const };
                      return (
                        <TableRow key={txn.sequenceId} hover>
                          <TableCell><Chip label={typeInfo.label} size="small" color={typeInfo.color} /></TableCell>
                          <TableCell sx={{ fontFamily: 'monospace' }}>{txn.itemId}</TableCell>
                          <TableCell>{txn.quantity}</TableCell>
                          <TableCell>{txn.reference}</TableCell>
                          <TableCell>{txn.transactionDate}</TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            ) : (
              <Typography color="text.secondary" variant="body2" sx={{ textAlign: 'center', py: 3 }}>
                No recent transactions. Activity will appear here as items are created, updated, or deactivated.
              </Typography>
            )}
          </Paper>
        </Grid>
      </Grid>
    </AppLayout>
  );
};

export default Dashboard;
