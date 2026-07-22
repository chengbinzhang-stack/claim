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
  const [globalError, setGlobalError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [form, setForm] = useState({
    policyNumber: '',
    claimType: '',
    incidentDate: '',
    description: '',
    amount: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    // Clear field error when user edits the field
    if (fieldErrors[e.target.name]) {
      setFieldErrors({ ...fieldErrors, [e.target.name]: '' });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalError('');
    setFieldErrors({});
    setLoading(true);

    try {
      const payload = {
        policyNumber: form.policyNumber,
        claimType: form.claimType,
        incidentDate: form.incidentDate,
        description: form.description,
        amount: parseFloat(form.amount),
      };
      await claimService.submitClaim(payload);
      navigate('/claims');
    } catch (err: any) {
      const data = err.response?.data;
      if (data?.data && typeof data.data === 'object') {
        // Field-level validation errors
        setFieldErrors(data.data as Record<string, string>);
        setGlobalError(data.message || 'Validation failed');
      } else {
        setGlobalError(data?.message || 'Failed to submit claim');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box p={3} maxWidth={600} mx="auto">
      <Typography variant="h5" mb={3}>Submit New Claim</Typography>

      {globalError && <Alert severity="error" sx={{ mb: 2 }}>{globalError}</Alert>}

      <Paper sx={{ p: 3 }}>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth label="Policy Number" name="policyNumber"
            value={form.policyNumber} onChange={handleChange} required margin="normal"
            error={!!fieldErrors.policyNumber}
            helperText={fieldErrors.policyNumber}
          />
          <TextField
            fullWidth select label="Claim Type" name="claimType"
            value={form.claimType} onChange={handleChange} required margin="normal"
            error={!!fieldErrors.claimType}
            helperText={fieldErrors.claimType}
          >
            {CLAIM_TYPES.map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
          </TextField>
          <TextField
            fullWidth label="Incident Date" name="incidentDate" type="date"
            value={form.incidentDate} onChange={handleChange} required margin="normal"
            InputLabelProps={{ shrink: true }}
            error={!!fieldErrors.incidentDate}
            helperText={fieldErrors.incidentDate}
          />
          <TextField
            fullWidth label="Claim Amount ($)" name="amount" type="number"
            value={form.amount} onChange={handleChange} required margin="normal"
            inputProps={{ min: 0.01, step: 0.01 }}
            error={!!fieldErrors.amount}
            helperText={fieldErrors.amount}
          />
          <TextField
            fullWidth label="Description" name="description"
            value={form.description} onChange={handleChange} required margin="normal"
            multiline rows={4}
            error={!!fieldErrors.description}
            helperText={fieldErrors.description || 'Describe the incident in detail (min 10 characters)'}
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
