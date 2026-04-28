/**
 * App.tsx — React Router v6 routes for Inventory screens.
 * R-08-E: Screen navigation map — SCR-001 to SCR-006 (Inventory).
 */

import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { createTheme, ThemeProvider, CssBaseline, GlobalStyles } from '@mui/material';
import InventoryMenu from './pages/InventoryMenu';
import InventoryList from './pages/InventoryList';
import InventoryItemDetail from './pages/InventoryItemDetail';
import ReorderReport from './pages/ReorderReport';
import ExitConfirmation from './pages/ExitConfirmation';

const theme = createTheme({
  palette: {
    primary:   { main: '#6366f1', light: '#818cf8', dark: '#4f46e5' },
    secondary: { main: '#8b5cf6', light: '#a78bfa', dark: '#7c3aed' },
    success:   { main: '#10b981', light: '#34d399', dark: '#059669' },
    warning:   { main: '#f59e0b', light: '#fbbf24', dark: '#d97706' },
    error:     { main: '#ef4444', light: '#f87171', dark: '#dc2626' },
    info:      { main: '#06b6d4', light: '#22d3ee', dark: '#0891b2' },
    background: { default: '#0f172a', paper: '#1e293b' },
    text: { primary: '#f1f5f9', secondary: '#94a3b8' },
  },
  typography: {
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h4: { fontWeight: 800, letterSpacing: '-0.03em' },
    h5: { fontWeight: 700, letterSpacing: '-0.02em' },
    h6: { fontWeight: 600, letterSpacing: '-0.01em' },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600, letterSpacing: '0.06em' },
    body2: { color: '#94a3b8' },
  },
  shape: { borderRadius: 16 },
  shadows: [
    'none',
    '0 1px 3px rgba(0,0,0,0.3)',
    '0 4px 6px -1px rgba(0,0,0,0.3)',
    '0 10px 15px -3px rgba(0,0,0,0.3)',
    '0 20px 25px -5px rgba(0,0,0,0.3)',
    '0 25px 50px -12px rgba(0,0,0,0.4)',
    '0 25px 50px -12px rgba(0,0,0,0.5)',
    ...Array(18).fill('0 25px 50px -12px rgba(0,0,0,0.5)'),
  ] as any,
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          borderRadius: 12,
          padding: '10px 24px',
          transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
          fontSize: '0.875rem',
        },
        contained: {
          background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
          boxShadow: '0 4px 15px rgba(99,102,241,0.4)',
          '&:hover': {
            background: 'linear-gradient(135deg, #818cf8 0%, #a78bfa 100%)',
            boxShadow: '0 8px 25px rgba(99,102,241,0.5)',
            transform: 'translateY(-2px)',
          },
        },
        outlined: {
          borderColor: 'rgba(148,163,184,0.3)',
          color: '#cbd5e1',
          '&:hover': {
            borderColor: 'rgba(148,163,184,0.5)',
            backgroundColor: 'rgba(148,163,184,0.08)',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: 'rgba(30,41,59,0.6)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(148,163,184,0.1)',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderColor: 'rgba(148,163,184,0.08)',
          color: '#e2e8f0',
        },
        head: {
          fontWeight: 700,
          fontSize: '0.7rem',
          textTransform: 'uppercase',
          letterSpacing: '0.08em',
          color: '#94a3b8',
          backgroundColor: 'rgba(15,23,42,0.5)',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          borderRadius: 8,
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 12,
            backgroundColor: 'rgba(15,23,42,0.4)',
            transition: 'all 0.2s',
            '& fieldset': {
              borderColor: 'rgba(148,163,184,0.15)',
            },
            '&:hover fieldset': {
              borderColor: 'rgba(148,163,184,0.3)',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#6366f1',
              boxShadow: '0 0 0 3px rgba(99,102,241,0.15)',
            },
          },
          '& .MuiInputLabel-root': {
            color: '#64748b',
          },
          '& .MuiOutlinedInput-input': {
            color: '#e2e8f0',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: 'rgba(30,41,59,0.6)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(148,163,184,0.1)',
          borderRadius: 20,
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 20,
          backgroundColor: 'rgba(30,41,59,0.95)',
          backdropFilter: 'blur(40px)',
          border: '1px solid rgba(148,163,184,0.15)',
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          backdropFilter: 'blur(20px)',
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: 'rgba(15,23,42,0.95)',
          backdropFilter: 'blur(10px)',
          borderRadius: 8,
          border: '1px solid rgba(148,163,184,0.1)',
          fontSize: '0.75rem',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          backgroundColor: 'rgba(148,163,184,0.1)',
        },
      },
    },
  },
});

const globalStyles = (
  <GlobalStyles styles={{
    '*': { boxSizing: 'border-box' },
    'html, body': {
      margin: 0,
      padding: 0,
    },
    body: {
      background: '#0f172a',
      minHeight: '100vh',
      position: 'relative',
      overflowX: 'hidden',
    },
    '#root': {
      position: 'relative',
      zIndex: 1,
      minHeight: '100vh',
    },
    '::-webkit-scrollbar': { width: 6, height: 6 },
    '::-webkit-scrollbar-track': { background: 'transparent' },
    '::-webkit-scrollbar-thumb': {
      background: 'rgba(148,163,184,0.2)',
      borderRadius: 3,
      '&:hover': { background: 'rgba(148,163,184,0.35)' },
    },
    '@keyframes fadeInUp': {
      from: { opacity: 0, transform: 'translateY(16px)' },
      to:   { opacity: 1, transform: 'translateY(0)' },
    },
    '@keyframes slideIn': {
      from: { opacity: 0, transform: 'translateX(-12px)' },
      to:   { opacity: 1, transform: 'translateX(0)' },
    },
    '@keyframes pulse': {
      '0%, 100%': { opacity: 1 },
      '50%': { opacity: 0.6 },
    },
    '@keyframes float': {
      '0%, 100%': { transform: 'translateY(0)' },
      '50%': { transform: 'translateY(-8px)' },
    },
    '@keyframes glow': {
      '0%, 100%': { opacity: 0.4 },
      '50%': { opacity: 0.8 },
    },
    '@keyframes shimmer': {
      '0%': { backgroundPosition: '-200% 0' },
      '100%': { backgroundPosition: '200% 0' },
    },
  }} />
);

const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {globalStyles}
      <BrowserRouter>
        <Routes>
          {/* Default redirect to main menu — BPF-001 entry point */}
          <Route path="/" element={<Navigate to="/inventory/menu" replace />} />

          {/* SCR-001: Inventory Management Main Menu */}
          <Route path="/inventory/menu" element={<InventoryMenu />} />

          {/* SCR-005: Exit Confirmation */}
          <Route path="/inventory/exit-confirm" element={<ExitConfirmation />} />

          {/* SCR-002: Inventory Item List */}
          <Route path="/inventory/list" element={<InventoryList />} />

          {/* SCR-003/004: Inventory Item Detail (Add / Edit / View) */}
          <Route path="/inventory/items/new" element={<InventoryItemDetail />} />
          <Route path="/inventory/items/:itemId" element={<InventoryItemDetail />} />

          {/* SCR-006: Inventory Reorder Report */}
          <Route path="/inventory/report/reorder" element={<ReorderReport />} />

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/inventory/menu" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
};

export default App;
