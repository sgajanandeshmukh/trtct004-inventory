import React, { useEffect, useState } from 'react';
import {
  Box, Grid, Paper, Typography, TextField, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, CircularProgress, Chip, Button, MenuItem, IconButton, Tooltip,
} from '@mui/material';
import { Search, Refresh, Download, History } from '@mui/icons-material';
import AppLayout from '../components/AppLayout';
import { transactionApi, TransactionRecord } from '../services/inventoryApi';

const txnTypes = [
  { value: '', label: 'All Types' },
  { value: 'CR', label: 'Created' },
  { value: 'UP', label: 'Updated' },
  { value: 'SC', label: 'Status Change' },
  { value: 'QA', label: 'Qty Adjusted' },
  { value: 'SR', label: 'Stock Received' },
  { value: 'TO', label: 'Transfer Out' },
  { value: 'TI', label: 'Transfer In' },
  { value: 'CC', label: 'Cycle Count' },
];

const typeColors: Record<string, 'success' | 'info' | 'warning' | 'error' | 'default'> = {
  CR: 'success', UP: 'info', SC: 'warning', QA: 'default', SR: 'success', TO: 'error', TI: 'info', CC: 'default',
};

const TransactionHistory: React.FC = () => {
  const [transactions, setTransactions] = useState<TransactionRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [itemIdFilter, setItemIdFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [totalCount, setTotalCount] = useState(0);

  const fetchData = () => {
    setLoading(true);
    transactionApi.search({
      itemId: itemIdFilter || undefined,
      transactionType: typeFilter || undefined,
      startDate: startDate || undefined,
      endDate: endDate || undefined,
    })
      .then((res) => {
        setTransactions(res.data.transactions || []);
        setTotalCount(res.data.totalCount || 0);
      })
      .catch(() => setTransactions([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleSearch = () => fetchData();

  const handleExportCsv = () => {
    const header = 'Seq,Transaction ID,Item ID,Type,Quantity,Date,User,Reference\n';
    const rows = transactions.map(t =>
      `${t.sequenceId},${t.transactionId},${t.itemId},${t.transactionType},${t.quantity},${t.transactionDate},${t.userId},${t.reference}`
    ).join('\n');
    const blob = new Blob([header + rows], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'transaction_history.csv';
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <AppLayout title="Transaction History" subtitle="Audit Trail" showBack backTo="/inventory/dashboard" badge="FR-005">
      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3, background: 'rgba(255,255,255,0.03)' }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={3}>
            <TextField fullWidth size="small" label="Item ID" value={itemIdFilter} onChange={e => setItemIdFilter(e.target.value)} />
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField fullWidth size="small" select label="Transaction Type" value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
              {txnTypes.map(t => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
            </TextField>
          </Grid>
          <Grid item xs={6} sm={2}>
            <TextField fullWidth size="small" type="date" label="Start Date" InputLabelProps={{ shrink: true }} value={startDate} onChange={e => setStartDate(e.target.value)} />
          </Grid>
          <Grid item xs={6} sm={2}>
            <TextField fullWidth size="small" type="date" label="End Date" InputLabelProps={{ shrink: true }} value={endDate} onChange={e => setEndDate(e.target.value)} />
          </Grid>
          <Grid item xs={12} sm={2}>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" startIcon={<Search />} onClick={handleSearch} fullWidth>Search</Button>
              <Tooltip title="Export CSV">
                <IconButton onClick={handleExportCsv} color="primary"><Download /></IconButton>
              </Tooltip>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Results */}
      <Paper sx={{ p: 2, background: 'rgba(255,255,255,0.03)' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="subtitle1" fontWeight={600}>
            <History sx={{ verticalAlign: 'middle', mr: 1 }} fontSize="small" />
            Transactions
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip label={`${totalCount} records`} size="small" variant="outlined" />
            <Tooltip title="Refresh"><IconButton size="small" onClick={fetchData}><Refresh /></IconButton></Tooltip>
          </Box>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 5 }}><CircularProgress /></Box>
        ) : transactions.length === 0 ? (
          <Typography color="text.secondary" sx={{ textAlign: 'center', py: 5 }}>No transactions found</Typography>
        ) : (
          <TableContainer sx={{ maxHeight: 500 }}>
            <Table size="small" stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Item ID</TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Reference</TableCell>
                  <TableCell>User</TableCell>
                  <TableCell>Date</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {transactions.map((txn) => (
                  <TableRow key={txn.sequenceId} hover>
                    <TableCell sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>{txn.sequenceId}</TableCell>
                    <TableCell>
                      <Chip
                        label={txnTypes.find(t => t.value === txn.transactionType)?.label || txn.transactionType}
                        size="small"
                        color={typeColors[txn.transactionType] || 'default'}
                      />
                    </TableCell>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 600 }}>{txn.itemId}</TableCell>
                    <TableCell>{txn.quantity !== 0 ? txn.quantity : '—'}</TableCell>
                    <TableCell sx={{ fontSize: '0.8rem' }}>{txn.reference}</TableCell>
                    <TableCell>{txn.userId}</TableCell>
                    <TableCell>{txn.transactionDate}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
    </AppLayout>
  );
};

export default TransactionHistory;
