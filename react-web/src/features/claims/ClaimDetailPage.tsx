import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Chip, CircularProgress,
  Grid, Divider
} from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { claimService } from '../../services/api';

const statusColors: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning'> = {
  SUBMITTED: 'info',
  IN_REVIEW: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  PAID: 'primary',
};

const ClaimDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [claim, setClaim] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    claimService.getClaimById(Number(id))
      .then(r => setClaim(r.data.data))
      .catch(() => navigate('/claims'))
      .finally(() => setLoading(false));
  }, [id, navigate]);

  if (loading) return <Box p={3} textAlign="center"><CircularProgress /></Box>;
  if (!claim) return null;

  return (
    <Box p={3} maxWidth={800} mx="auto">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Claim Details</Typography>
        <Chip label={claim.status} color={statusColors[claim.status] || 'default'} />
      </Box>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Claim Number</Typography>
            <Typography>{claim.claimNumber}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Policy Number</Typography>
            <Typography>{claim.policyNumber}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Policy Type</Typography>
            <Typography>{claim.policyType}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Claim Type</Typography>
            <Typography>{claim.claimType}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Incident Date</Typography>
            <Typography>{claim.incidentDate}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Claim Amount</Typography>
            <Typography variant="h6" color="primary">${claim.amount}</Typography>
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="subtitle2" color="textSecondary">Description</Typography>
            <Typography>{claim.description}</Typography>
          </Grid>

          {claim.reviewNotes && (
            <Grid item xs={12}>
              <Divider sx={{ my: 2 }} />
              <Typography variant="subtitle2" color="textSecondary">Review Notes</Typography>
              <Typography>{claim.reviewNotes}</Typography>
            </Grid>
          )}

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Submitted By</Typography>
            <Typography>{claim.submittedByName}</Typography>
            <Typography variant="caption">{claim.submittedByEmail}</Typography>
          </Grid>
          {claim.reviewedByName && (
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle2" color="textSecondary">Reviewed By</Typography>
              <Typography>{claim.reviewedByName}</Typography>
            </Grid>
          )}

          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">Submitted</Typography>
            <Typography>{new Date(claim.createdAt).toLocaleString()}</Typography>
          </Grid>
          {claim.updatedAt && (
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle2" color="textSecondary">Last Updated</Typography>
              <Typography>{new Date(claim.updatedAt).toLocaleString()}</Typography>
            </Grid>
          )}
        </Grid>

        <Box mt={3}>
          <Chip
            label="Back to Claims"
            onClick={() => navigate('/claims')}
            clickable
          />
        </Box>
      </Paper>
    </Box>
  );
};

export default ClaimDetailPage;
