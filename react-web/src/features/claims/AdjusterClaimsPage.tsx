import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, Button, Dialog,
  DialogTitle, DialogContent, DialogActions, TextField,
  CircularProgress, Alert
} from '@mui/material';
import { claimService } from '../../services/api';
import { useNavigate } from 'react-router-dom';

const statusColors: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning'> = {
  SUBMITTED: 'info',
  IN_REVIEW: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  PAID: 'primary',
};

const AdjusterClaimsPage: React.FC = () => {
  const navigate = useNavigate();
  const [claims, setClaims] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedClaim, setSelectedClaim] = useState<any>(null);
  const [action, setAction] = useState<'APPROVED' | 'REJECTED' | null>(null);
  const [reviewNotes, setReviewNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadAllClaims();
  }, []);

  const loadAllClaims = async () => {
    try {
      const res = await claimService.getAllClaims();
      setClaims(res.data.data || []);
    } catch (err: any) {
      setLoadError(err.response?.data?.message || err.message || 'Failed to load claims');
    } finally {
      setLoading(false);
    }
  };

  const openDialog = (claim: any, act: 'APPROVED' | 'REJECTED') => {
    setSelectedClaim(claim);
    setAction(act);
    setReviewNotes('');
    setDialogOpen(true);
  };

  const handleConfirm = async () => {
    if (!selectedClaim || !action) return;
    setSubmitting(true);
    setError('');
    try {
      await claimService.updateClaimStatus(selectedClaim.id, action, reviewNotes);
      setDialogOpen(false);
      loadAllClaims();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update claim');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Box p={3} textAlign="center"><CircularProgress /></Box>;

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">All Claims (Adjuster View)</Typography>
        {loadError && <Alert severity="error">{loadError}</Alert>}
        <Button variant="text" onClick={() => navigate('/dashboard')}>Dashboard</Button>
        <Button variant="outlined" onClick={() => navigate('/claims')}>
          My Claims
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Claim Number</TableCell>
              <TableCell>Policy</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Claimant</TableCell>
              <TableCell>Submitted</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {claims.map((claim) => (
              <TableRow key={claim.id} hover>
                <TableCell>{claim.id}</TableCell>
                <TableCell>{claim.claimNumber}</TableCell>
                <TableCell>{claim.policyNumber}</TableCell>
                <TableCell>{claim.claimType}</TableCell>
                <TableCell>${claim.amount}</TableCell>
                <TableCell>
                  <Chip label={claim.status} color={statusColors[claim.status] || 'default'} size="small" />
                </TableCell>
                <TableCell>{claim.submittedByName}</TableCell>
                <TableCell>{new Date(claim.createdAt).toLocaleDateString()}</TableCell>
                <TableCell>
                  {claim.status === 'SUBMITTED' || claim.status === 'IN_REVIEW' ? (
                    <Box display="flex" gap={1}>
                      <Button
                        size="small" color="success" variant="outlined"
                        onClick={() => openDialog(claim, 'APPROVED')}
                      >
                        Approve
                      </Button>
                      <Button
                        size="small" color="error" variant="outlined"
                        onClick={() => openDialog(claim, 'REJECTED')}
                      >
                        Reject
                      </Button>
                    </Box>
                  ) : (
                    <Button
                      size="small" variant="text"
                      onClick={() => navigate(`/claims/${claim.id}`)}
                    >
                      View
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {claims.length === 0 && (
              <TableRow>
                <TableCell colSpan={9} align="center">No claims found</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Approve/Reject Dialog */}
      <Dialog open={dialogOpen} onClose={() => !submitting && setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {action === 'APPROVED' ? 'Approve' : 'Reject'} Claim #{selectedClaim?.claimNumber}
        </DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Box pt={1}>
            <Typography variant="body2" mb={2}>
              Claim Amount: <strong>${selectedClaim?.amount}</strong> —{' '}
              {selectedClaim?.claimType} on policy <strong>{selectedClaim?.policyNumber}</strong>
            </Typography>
            <TextField
              fullWidth multiline rows={4} label="Review Notes"
              value={reviewNotes} onChange={e => setReviewNotes(e.target.value)}
              placeholder="Enter your review notes..."
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)} disabled={submitting}>Cancel</Button>
          <Button
            variant="contained"
            color={action === 'APPROVED' ? 'success' : 'error'}
            onClick={handleConfirm}
            disabled={submitting}
          >
            {submitting ? <CircularProgress size={20} /> : action === 'APPROVED' ? 'Confirm Approval' : 'Confirm Rejection'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdjusterClaimsPage;
