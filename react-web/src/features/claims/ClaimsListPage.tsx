import React, { useEffect, useState } from 'react';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip, Button } from '@mui/material';
import { claimService } from '../../services/api';
import { useNavigate } from 'react-router-dom';

const ClaimsListPage: React.FC = () => {
  const [claims, setClaims] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadClaims();
  }, []);

  const loadClaims = async () => {
    try {
      const response = await claimService.getMyClaims();
      setClaims(response.data.data || []);
    } catch (error) {
      console.error('Failed to load claims:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning'> = {
      SUBMITTED: 'info',
      IN_REVIEW: 'warning',
      APPROVED: 'success',
      REJECTED: 'error',
      PAID: 'primary',
    };
    return colors[status] || 'default';
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">My Claims</Typography>
        <Button variant="contained" onClick={() => navigate('/claims/new')}>
          Submit New Claim
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Claim Number</TableCell>
              <TableCell>Policy</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Submitted</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {claims.map((claim) => (
              <TableRow key={claim.id} hover onClick={() => navigate(`/claims/${claim.id}`)} style={{ cursor: 'pointer' }}>
                <TableCell>{claim.claimNumber}</TableCell>
                <TableCell>{claim.policyNumber}</TableCell>
                <TableCell>{claim.claimType}</TableCell>
                <TableCell>${claim.amount}</TableCell>
                <TableCell>
                  <Chip label={claim.status} color={getStatusColor(claim.status)} size="small" />
                </TableCell>
                <TableCell>{new Date(claim.createdAt).toLocaleDateString()}</TableCell>
              </TableRow>
            ))}
            {claims.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={6} align="center">No claims found</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default ClaimsListPage;
