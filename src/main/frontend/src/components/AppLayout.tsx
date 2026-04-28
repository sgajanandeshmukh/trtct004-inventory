/**
 * AppLayout.tsx — Shared application shell
 * R-08-E: Screen navigation — persistent header with breadcrumb
 * Dark theme with animated gradient background and glassmorphic header.
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar, Toolbar, Typography, IconButton, Box, Breadcrumbs, Link, Chip, Button
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import InventoryIcon from '@mui/icons-material/Inventory2';
import HomeIcon from '@mui/icons-material/Home';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';

interface AppLayoutProps {
  title: string;
  subtitle?: string;
  showBack?: boolean;
  backTo?: string;
  children: React.ReactNode;
  badge?: string | { label: string; color: 'default' | 'primary' | 'secondary' | 'warning' | 'error' | 'success' | 'info' };
}

const ROUTE_LABELS: Record<string, string> = {
  '/inventory/menu': 'Menu',
  '/inventory/dashboard': 'Dashboard',
  '/inventory/list': 'Item List',
  '/inventory/report/reorder': 'Reorder Report',
  '/inventory/transactions': 'Transaction History',
  '/inventory/bulk': 'Bulk Operations',
};

const AppLayout: React.FC<AppLayoutProps> = ({ title, subtitle, showBack = true, backTo, children, badge: badgeProp }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleBack = () => {
    if (backTo) navigate(backTo);
    else navigate(-1);
  };

  const badge = typeof badgeProp === 'string'
    ? { label: badgeProp, color: 'default' as const }
    : badgeProp;

  const isMenu = location.pathname === '/inventory/menu';
  const isDashboard = location.pathname === '/inventory/dashboard';

  return (
    <Box sx={{ minHeight: '100vh', position: 'relative', overflow: 'hidden' }}>
      {/* Animated background orbs */}
      <Box sx={{
        position: 'fixed', inset: 0, zIndex: 0, pointerEvents: 'none',
        background: '#0f172a',
      }}>
        <Box sx={{
          position: 'absolute', top: '-10%', right: '-5%',
          width: '45vw', height: '45vw', maxWidth: 700, maxHeight: 700,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%)',
          animation: 'float 20s ease-in-out infinite',
        }} />
        <Box sx={{
          position: 'absolute', bottom: '-15%', left: '-10%',
          width: '50vw', height: '50vw', maxWidth: 800, maxHeight: 800,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(139,92,246,0.1) 0%, transparent 70%)',
          animation: 'float 25s ease-in-out infinite reverse',
        }} />
        <Box sx={{
          position: 'absolute', top: '40%', left: '50%',
          width: '30vw', height: '30vw', maxWidth: 500, maxHeight: 500,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(6,182,212,0.08) 0%, transparent 70%)',
          animation: 'float 18s ease-in-out infinite 3s',
        }} />
        {/* Subtle grid pattern */}
        <Box sx={{
          position: 'absolute', inset: 0,
          backgroundImage: `
            linear-gradient(rgba(148,163,184,0.03) 1px, transparent 1px),
            linear-gradient(90deg, rgba(148,163,184,0.03) 1px, transparent 1px)
          `,
          backgroundSize: '60px 60px',
        }} />
      </Box>

      {/* Glass AppBar */}
      <AppBar
        position="sticky"
        elevation={0}
        sx={{
          background: 'rgba(15,23,42,0.7)',
          backdropFilter: 'blur(20px)',
          borderBottom: '1px solid rgba(148,163,184,0.08)',
          zIndex: 10,
        }}
      >
        <Toolbar sx={{ gap: 1.5, minHeight: { xs: 60, sm: 68 } }}>
          {showBack && !isMenu && !isDashboard && (
            <IconButton
              color="inherit"
              onClick={handleBack}
              size="small"
              sx={{
                mr: 0.5,
                bgcolor: 'rgba(148,163,184,0.08)',
                border: '1px solid rgba(148,163,184,0.1)',
                '&:hover': { bgcolor: 'rgba(148,163,184,0.15)', borderColor: 'rgba(148,163,184,0.2)' },
                transition: 'all 0.2s',
              }}
            >
              <ArrowBackIcon fontSize="small" />
            </IconButton>
          )}

          {/* Logo */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mr: 1 }}>
            <Box sx={{
              width: 38, height: 38,
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: '0 4px 15px rgba(99,102,241,0.4)',
              position: 'relative',
              '&::after': {
                content: '""',
                position: 'absolute', inset: -2,
                borderRadius: '14px',
                background: 'linear-gradient(135deg, rgba(99,102,241,0.4), rgba(139,92,246,0.4))',
                zIndex: -1,
                filter: 'blur(8px)',
                animation: 'glow 3s ease-in-out infinite',
              },
            }}>
              <InventoryIcon sx={{ fontSize: 20, color: '#fff' }} />
            </Box>
          </Box>

          <Box sx={{ flexGrow: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography
                variant="h6"
                sx={{
                  fontWeight: 700,
                  lineHeight: 1.2,
                  letterSpacing: '-0.01em',
                  fontSize: { xs: '0.95rem', sm: '1.1rem' },
                  color: '#f1f5f9',
                }}
              >
                {title}
              </Typography>
              {badge && (
                <Chip
                  label={badge.label}
                  color={badge.color}
                  size="small"
                  sx={{
                    fontWeight: 700,
                    fontSize: '0.62rem',
                    height: 22,
                    borderRadius: '6px',
                    letterSpacing: '0.04em',
                    bgcolor: badge.color === 'primary' ? 'rgba(99,102,241,0.2)' :
                             badge.color === 'secondary' ? 'rgba(139,92,246,0.2)' :
                             'rgba(148,163,184,0.15)',
                    color: badge.color === 'primary' ? '#818cf8' :
                           badge.color === 'secondary' ? '#a78bfa' :
                           '#94a3b8',
                    border: '1px solid',
                    borderColor: badge.color === 'primary' ? 'rgba(99,102,241,0.3)' :
                                 badge.color === 'secondary' ? 'rgba(139,92,246,0.3)' :
                                 'rgba(148,163,184,0.2)',
                  }}
                />
              )}
            </Box>
            {subtitle && (
              <Typography variant="caption" sx={{ color: '#64748b', letterSpacing: 0.2, fontSize: '0.72rem' }}>
                {subtitle}
              </Typography>
            )}
          </Box>

          {/* Right side */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            {!isMenu && !isDashboard && (
              <IconButton
                size="small"
                sx={{
                  color: '#64748b',
                  '&:hover': { color: '#e2e8f0', bgcolor: 'rgba(148,163,184,0.08)' },
                  transition: 'all 0.2s',
                }}
                onClick={() => navigate('/inventory/dashboard')}
              >
                <HomeIcon fontSize="small" />
              </IconButton>
            )}
            <Box sx={{
              px: 2, py: 0.75,
              borderRadius: '10px',
              background: 'rgba(99,102,241,0.1)',
              border: '1px solid rgba(99,102,241,0.2)',
            }}>
              <Typography variant="caption" sx={{
                fontWeight: 700,
                fontSize: '0.65rem',
                letterSpacing: '0.12em',
                color: '#818cf8',
                fontFamily: '"Inter", monospace',
              }}>
                WorkStream
              </Typography>
            </Box>
          </Box>
        </Toolbar>

        {/* Breadcrumb — R-08-E */}
        {!isMenu && !isDashboard && (
          <Box sx={{
            px: 2.5, pb: 1.5, pt: 0,
            display: 'flex', alignItems: 'center',
          }}>
            <Breadcrumbs
              separator={<NavigateNextIcon sx={{ fontSize: 14, color: 'rgba(148,163,184,0.3)' }} />}
              sx={{ '& *': { fontSize: '0.72rem' } }}
            >
              <Link
                underline="hover"
                sx={{
                  cursor: 'pointer',
                  color: '#64748b',
                  '&:hover': { color: '#818cf8' },
                  display: 'flex', alignItems: 'center', gap: 0.5,
                  transition: 'color 0.15s',
                }}
                onClick={() => navigate('/inventory/dashboard')}
              >
                Inventory
              </Link>
              {ROUTE_LABELS[location.pathname]
                ? <Typography sx={{ color: '#e2e8f0', fontSize: '0.72rem', fontWeight: 600 }}>
                    {ROUTE_LABELS[location.pathname]}
                  </Typography>
                : <Typography sx={{ color: '#e2e8f0', fontSize: '0.72rem', fontWeight: 600 }}>
                    Item Detail
                  </Typography>
              }
            </Breadcrumbs>
          </Box>
        )}
      </AppBar>

      {/* Main Content */}
      <Box sx={{
        position: 'relative',
        zIndex: 1,
        p: { xs: 2, sm: 3 },
        maxWidth: 1400,
        mx: 'auto',
        animation: 'fadeInUp 0.4s ease-out',
      }}>
        {children}
      </Box>
    </Box>
  );
};

export default AppLayout;
