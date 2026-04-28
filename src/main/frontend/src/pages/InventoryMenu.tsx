/**
 * InventoryMenu.tsx — SCR-001: Inventory Management Main Menu
 * R-08-C: Function modes — navigation cards replace legacy selection-input paradigm
 * R-08-E: React Router v6 navigation
 * BR-002–007, DT-001
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Card, CardActionArea, CardContent, Grid
} from '@mui/material';
import ListAltIcon from '@mui/icons-material/ListAlt';
import AddBoxIcon from '@mui/icons-material/AddBox';
import AssessmentIcon from '@mui/icons-material/Assessment';
import InventoryIcon from '@mui/icons-material/Inventory2';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import AppLayout from '../components/AppLayout';
import { menuApi } from '../services/inventoryApi';

interface NavCard {
  icon: React.ReactNode;
  title: string;
  description: string;
  route: string;
  actionCode: string;
  gradient: string;
  glowColor: string;
  accentColor: string;
}

const NAV_CARDS: NavCard[] = [
  {
    icon: <ListAltIcon sx={{ fontSize: 28, color: '#fff' }} />,
    title: 'Browse Inventory',
    description: 'Search, view, edit, and manage all inventory item records in the system.',
    route: '/inventory/list',
    actionCode: '1',
    gradient: 'linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%)',
    glowColor: 'rgba(59,130,246,0.4)',
    accentColor: '#3b82f6',
  },
  {
    icon: <AddBoxIcon sx={{ fontSize: 28, color: '#fff' }} />,
    title: 'Add New Item',
    description: 'Create a new inventory item with pricing, stock levels, and supplier details.',
    route: '/inventory/items/new?mode=A',
    actionCode: '2',
    gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)',
    glowColor: 'rgba(16,185,129,0.4)',
    accentColor: '#10b981',
  },
  {
    icon: <AssessmentIcon sx={{ fontSize: 28, color: '#fff' }} />,
    title: 'Reorder Report',
    description: 'View items below reorder point and calculate replenishment values.',
    route: '/inventory/report/reorder',
    actionCode: '3',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)',
    glowColor: 'rgba(245,158,11,0.4)',
    accentColor: '#f59e0b',
  },
];

const InventoryMenu: React.FC = () => {
  const navigate = useNavigate();

  const handleSelect = (card: NavCard) => {
    menuApi.selectOption(card.actionCode).catch(() => {});
    navigate(card.route);
  };

  return (
    <AppLayout title="Inventory Management" showBack={false}>
      {/* Hero Section */}
      <Box sx={{
        mt: 1, mb: 4,
        p: { xs: 4, sm: 5 },
        borderRadius: 4,
        background: 'rgba(30,41,59,0.5)',
        backdropFilter: 'blur(20px)',
        border: '1px solid rgba(148,163,184,0.08)',
        position: 'relative',
        overflow: 'hidden',
      }}>
        {/* Decorative elements */}
        <Box sx={{
          position: 'absolute', top: -60, right: -60,
          width: 250, height: 250, borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%)',
        }} />
        <Box sx={{
          position: 'absolute', bottom: -40, left: '20%',
          width: 200, height: 200, borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(139,92,246,0.1) 0%, transparent 70%)',
        }} />
        <Box sx={{
          position: 'absolute', top: '50%', right: '15%',
          width: 120, height: 120, borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(6,182,212,0.08) 0%, transparent 70%)',
        }} />

        <Box sx={{ position: 'relative' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2.5, mb: 1.5 }}>
            <Box sx={{
              width: 56, height: 56,
              borderRadius: '16px',
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: '0 8px 25px rgba(99,102,241,0.4)',
              position: 'relative',
              '&::after': {
                content: '""', position: 'absolute', inset: -3,
                borderRadius: '19px',
                background: 'linear-gradient(135deg, rgba(99,102,241,0.3), rgba(139,92,246,0.3))',
                zIndex: -1, filter: 'blur(10px)',
              },
            }}>
              <InventoryIcon sx={{ fontSize: 28, color: '#fff' }} />
            </Box>
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 800, letterSpacing: '-0.03em', color: '#f1f5f9', fontSize: { xs: '1.5rem', sm: '1.85rem' } }}>
                Inventory Management
              </Typography>
              <Typography variant="body2" sx={{ color: '#64748b', mt: 0.5, fontSize: '0.9rem' }}>
                Select a function below to get started
              </Typography>
            </Box>
          </Box>
        </Box>
      </Box>

      {/* Navigation Cards */}
      <Grid container spacing={3}>
        {NAV_CARDS.map((card, index) => (
          <Grid item xs={12} sm={6} md={4} key={card.actionCode}>
            <Card
              elevation={0}
              sx={{
                height: '100%',
                overflow: 'visible',
                position: 'relative',
                transition: 'all 0.35s cubic-bezier(0.4, 0, 0.2, 1)',
                animation: 'fadeInUp 0.5s ease-out',
                animationDelay: `${index * 0.12}s`,
                animationFillMode: 'both',
                '&:hover': {
                  transform: 'translateY(-8px)',
                  boxShadow: `0 25px 50px -12px ${card.glowColor}`,
                  borderColor: `${card.accentColor}40`,
                  '& .card-icon': {
                    transform: 'scale(1.1)',
                    boxShadow: `0 8px 30px ${card.glowColor}`,
                  },
                  '& .card-arrow': {
                    transform: 'translateX(6px)',
                    color: card.accentColor,
                  },
                },
              }}
            >
              <CardActionArea onClick={() => handleSelect(card)} sx={{ height: '100%', p: 0.5 }}>
                <CardContent sx={{ p: 3.5 }}>
                  <Box className="card-icon" sx={{
                    width: 56, height: 56,
                    borderRadius: '16px',
                    background: card.gradient,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    mb: 3,
                    boxShadow: `0 6px 20px ${card.glowColor}`,
                    transition: 'all 0.35s cubic-bezier(0.4, 0, 0.2, 1)',
                  }}>
                    {card.icon}
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, mb: 1, fontSize: '1.1rem', color: '#f1f5f9' }}>
                    {card.title}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#94a3b8', lineHeight: 1.7, mb: 2.5 }}>
                    {card.description}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', fontWeight: 600, fontSize: '0.82rem', color: '#818cf8' }}>
                    Open
                    <ArrowForwardIcon className="card-arrow" sx={{ fontSize: 16, ml: 0.5, transition: 'all 0.25s' }} />
                  </Box>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </AppLayout>
  );
};

export default InventoryMenu;
