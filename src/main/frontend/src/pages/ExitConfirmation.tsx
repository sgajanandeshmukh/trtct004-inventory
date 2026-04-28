/**
 * ExitConfirmation.tsx — SCR-005: Exit Confirmation
 * BR-005/006: Confirm exit dialog
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Button, Paper } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { menuApi } from '../services/inventoryApi';

const ExitConfirmation: React.FC = () => {
  const navigate = useNavigate();

  const handleConfirm = async () => {
    // BR-005: Exit confirmed — terminate session
    await menuApi.confirmExit(false);
    navigate('/');
  };

  const handleCancel = async () => {
    // BR-006: Exit Cancelled — Resume Menu
    await menuApi.confirmExit(true);
    navigate('/inventory/menu');
  };

  return (
    <Box sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: '#0f172a',
      position: 'relative',
      overflow: 'hidden',
      p: 3,
      '&::before': {
        content: '""',
        position: 'absolute',
        top: '-20%',
        left: '-10%',
        width: '60%',
        height: '60%',
        background: 'radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%)',
        animation: 'orbFloat1 8s ease-in-out infinite',
      },
      '&::after': {
        content: '""',
        position: 'absolute',
        bottom: '-20%',
        right: '-10%',
        width: '60%',
        height: '60%',
        background: 'radial-gradient(circle, rgba(139,92,246,0.1) 0%, transparent 70%)',
        animation: 'orbFloat2 10s ease-in-out infinite',
      },
      '@keyframes orbFloat1': {
        '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
        '50%': { transform: 'translate(30px, 20px) scale(1.1)' },
      },
      '@keyframes orbFloat2': {
        '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
        '50%': { transform: 'translate(-30px, -20px) scale(1.1)' },
      },
      '@keyframes fadeInUp': {
        '0%': { opacity: 0, transform: 'translateY(30px)' },
        '100%': { opacity: 1, transform: 'translateY(0)' },
      },
    }}>
      <Paper
        elevation={0}
        sx={{
          p: 5,
          textAlign: 'center',
          maxWidth: 420,
          width: '100%',
          borderRadius: 4,
          position: 'relative',
          zIndex: 1,
          background: 'rgba(30,41,59,0.8)',
          backdropFilter: 'blur(30px)',
          border: '1px solid rgba(148,163,184,0.1)',
          animation: 'fadeInUp 0.4s ease-out',
        }}
      >
        <Box sx={{
          width: 64, height: 64, borderRadius: '50%',
          background: 'linear-gradient(135deg, #ef4444 0%, #f87171 100%)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          mx: 'auto', mb: 3,
          boxShadow: '0 8px 25px rgba(239,68,68,0.4)',
        }}>
          <LogoutIcon sx={{ fontSize: 30, color: '#fff' }} />
        </Box>

        {/* F-005-01: Screen Title */}
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1, color: '#f1f5f9' }}>
          Exit Application?
        </Typography>

        {/* F-005-02: Exit Prompt — BR-005 */}
        <Typography variant="body2" sx={{ color: '#94a3b8', mb: 4 }}>
          Are you sure you want to exit the Inventory Management System? Any unsaved changes will be lost.
        </Typography>

        <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={handleCancel}
            sx={{
              borderRadius: 2,
              px: 3,
              borderColor: 'rgba(148,163,184,0.3)',
              color: '#cbd5e1',
              '&:hover': {
                borderColor: 'rgba(148,163,184,0.5)',
                background: 'rgba(148,163,184,0.08)',
              },
            }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            startIcon={<LogoutIcon />}
            onClick={handleConfirm}
            sx={{
              borderRadius: 2,
              px: 3,
              background: 'linear-gradient(135deg, #ef4444 0%, #f87171 100%)',
              boxShadow: '0 8px 25px rgba(239,68,68,0.4)',
              '&:hover': {
                background: 'linear-gradient(135deg, #dc2626 0%, #ef4444 100%)',
                boxShadow: '0 10px 30px rgba(239,68,68,0.5)',
              },
            }}
          >
            Confirm Exit
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default ExitConfirmation;
