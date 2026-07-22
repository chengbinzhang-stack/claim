import React, { useState } from 'react';
import {
  Box, Typography, TextField, Button, Paper,
  MenuItem, Alert, CircularProgress
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { claimService } from '../../services/api';

const CLAIM_TYPES = ['ACCIDENT', 'THEFT', 'FIRE', 'WATER_DAMAGE', 'MEDICAL', 'OTHER'];

const ClaimFormPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    policyNumber: '',
    claimType: '',
    incidentDate: '',
    description: '',
    amount: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const payload = {
        policyNumber: form.policyNumber,
        claimType: form.claimType,
        policyType: form.policyType,
        incidentDate: form.incidentDate,
        description: form.description,
        amount: parseFloat(form.amount),
      };
      await claimService.submitClaim(payload);
      navigate('/claims');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit claim');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box p={3} maxWidth={600} mx="auto">
      <Typography variant="h5" mb={3}>Submit New Claim</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Paper sx={{ p: 3 }}>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth label="Policy Number" name="policyNumber"
            value={form.policyNumber} onChange={handleChange} required margin="normal"
          />
          <TextField
            fullWidth select label="Claim Type" name="claimType"
            value={form.claimType} onChange={handleChange} required margin="normal"
          >
            {CLAIM_TYPES.map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
          </TextField>
          <TextField
            fullWidth label="Incident Date" name="incidentDate" type="date"
            value={form.incidentDate} onChange={handleChange} required margin="normal"
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            fullWidth label="Claim Amount ($)" name="amount" type="number"
            value={form.amount} onChange={handleChange} required margin="normal"
            inputProps={{ min: 0.01, step: 0.01 }}
          />
          <TextField
            fullWidth label="Description" name="description"
            value={form.description} onChange={handleChange} required margin="normal"
            multiline rows={4}
            helperText="Describe the incident in detail (min 10 characters)"
          />

          <Box mt={3} display="flex" gap={2}>
            <Button type="submit" variant="contained" disabled={loading}>
              {loading ? <CircularProgress size={24} /> : 'Submit Claim'}
            </Button>
            <Button variant="outlined" onClick={() => navigate('/claims')}>
              Cancel
            </Button>
          </Box>
        </form>
      </Paper>
    </Box>
  );
};

export default ClaimFormPage;
