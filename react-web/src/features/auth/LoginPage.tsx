import React, { useState } from 'react';
import { TextField, Button, Box, Typography, Alert, Card, CardContent, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/api';

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authService.login(username, password);
      const loginData = response.data;
      if (!loginData.data?.token) {
        setError('Invalid response: ' + JSON.stringify(loginData));
        return;
      }
      localStorage.setItem('token', loginData.data.token);
      localStorage.setItem('user', JSON.stringify(loginData.data.user));
      navigate('/dashboard');
    } catch (err: any) {
      alert('Login error: ' + JSON.stringify(err.response?.data || err.message || err));
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#f5f5f5' }}>
      <Card sx={{ maxWidth: 400, p: 2 }}>
        <CardContent>
          <Typography variant="h5" component="h1" gutterBottom textAlign="center">
            Insurance Claim System
          </Typography>
          <Typography variant="body2" color="textSecondary" textAlign="center" mb={3}>
            Sign in to continue
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <form onSubmit={handleLogin}>
            <TextField
              fullWidth
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              required
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3 }}
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>

          <Box mt={2} textAlign="center">
            <Link href="/register" underline="hover" variant="body2">
              Don't have an account? Register
            </Link>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default LoginPage;
