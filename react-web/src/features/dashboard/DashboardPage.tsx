import React, { useEffect, useState } from 'react';
import { Box, Typography, Paper, Grid, Button } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardService } from '../../services/api';
import { useNavigate } from 'react-router-dom';

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [role, setRole] = useState<string | null>(() => {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    try { return JSON.parse(userStr).roleName || null; } catch { return null; }
  });

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const response = await dashboardService.getStats();
      setStats(response.data.data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    } finally {
      setLoading(false);
    }
  };

  const chartData = stats ? [
    { name: 'Submitted', value: stats.submitted },
    { name: 'In Review', value: stats.inReview },
    { name: 'Approved', value: stats.approved },
    { name: 'Rejected', value: stats.rejected },
    { name: 'Paid', value: stats.paid },
  ] : [];

  if (loading) return <Box p={3}>Loading...</Box>;

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Dashboard</Typography>
        <Box display="flex" gap={1}>
          <Button variant="outlined" onClick={() => navigate('/claims')}>My Claims</Button>
          <Button variant="contained" onClick={() => navigate('/claims/new')}>Submit Claim</Button>
          {(role === 'ADJUSTER' || role === 'ADMIN') && (
            <Button variant="outlined" color="warning" onClick={() => navigate('/adjuster/claims')}>
              Adjuster View
            </Button>
          )}
        </Box>
      </Box>
      <Grid container spacing={3}>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h3">{stats?.totalClaims || 0}</Typography>
            <Typography color="textSecondary">Total Claims</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h3" color="info.main">{stats?.submitted || 0}</Typography>
            <Typography color="textSecondary">Submitted</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h3" color="success.main">{stats?.approved || 0}</Typography>
            <Typography color="textSecondary">Approved</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h3" color="error.main">{stats?.rejected || 0}</Typography>
            <Typography color="textSecondary">Rejected</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>Claims Overview</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" fill="#1976d2" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;
