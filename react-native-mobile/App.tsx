import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, FlatList, ScrollView } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

// ⚠️ 改成你实际的后端地址
const API_BASE_URL = 'https://claim-api-7mi9.onrender.com';

// ============== 登录页面 ==============
const LoginScreen = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (!username || !password) {
      Alert.alert('Error', 'Please enter username and password');
      return;
    }
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });
      const data = await response.json();
      if (data.success) {
        await AsyncStorage.setItem('token', data.data.token);
        await AsyncStorage.setItem('user', JSON.stringify(data.data.user));
        onLogin();
      } else {
        Alert.alert('Error', data.message || 'Login failed');
      }
    } catch (error) {
      Alert.alert('Error', 'Network error. Make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Insurance Claim</Text>
      <Text style={styles.subtitle}>Login</Text>
      <TextInput style={styles.input} placeholder="Username" value={username} onChangeText={setUsername} autoCapitalize="none" />
      <TextInput style={styles.input} placeholder="Password" secureTextEntry value={password} onChangeText={setPassword} />
      <TouchableOpacity style={styles.button} onPress={handleLogin} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Sign In</Text>}
      </TouchableOpacity>
    </View>
  );
};

// ============== 理赔列表页 ==============
const ClaimsListScreen = ({ token, userRole, onLogout }) => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedClaim, setSelectedClaim] = useState(null);

  const fetchClaims = useCallback(async () => {
    try {
      const endpoint = (userRole === 'CUSTOMER') ? '/claims/my' : '/claims/all';
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await response.json();
      setClaims(data.data?.content || data.data || []);
    } catch (error) {
      console.error(error);
      Alert.alert('Error', 'Failed to fetch claims');
    } finally {
      setLoading(false);
    }
  }, [token, userRole]);

  useEffect(() => { fetchClaims(); }, [fetchClaims]);

  const handleUpdateStatus = async (claimId, status) => {
    try {
      const response = await fetch(`${API_BASE_URL}/claims/${claimId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status, reviewNotes: 'Reviewed via mobile' }),
      });
      const data = await response.json();
      if (data.success) {
        Alert.alert('Success', `Claim ${status}`);
        setSelectedClaim(null);
        fetchClaims();
      } else {
        Alert.alert('Error', data.message);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to update claim');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUBMITTED': return '#f57c00';
      case 'APPROVED': return '#388e3c';
      case 'REJECTED': return '#d32f2f';
      case 'UNDER_REVIEW': return '#1976d2';
      default: return '#666';
    }
  };

  if (selectedClaim) {
    return (
      <ScrollView style={styles.container}>
        <Text style={styles.title}>Claim Detail</Text>
        <Text style={styles.claimNumber}>{selectedClaim.claimNumber}</Text>
        <View style={styles.detailRow}><Text style={styles.label}>Type:</Text><Text>{selectedClaim.claimType}</Text></View>
        <View style={styles.detailRow}><Text style={styles.label}>Amount:</Text><Text>${selectedClaim.amount}</Text></View>
        <View style={styles.detailRow}>
          <Text style={styles.label}>Status:</Text>
          <Text style={{ color: getStatusColor(selectedClaim.status), fontWeight: 'bold' }}>{selectedClaim.status}</Text>
        </View>
        <View style={styles.detailRow}><Text style={styles.label}>Description:</Text><Text>{selectedClaim.description}</Text></View>

        {(userRole === 'ADJUSTER' || userRole === 'ADMIN') && selectedClaim.status === 'SUBMITTED' && (
          <View style={{ marginTop: 20 }}>
            <Text style={styles.subtitle}>Actions</Text>
            <TouchableOpacity style={[styles.button, { backgroundColor: '#388e3c', marginBottom: 10 }]} onPress={() => handleUpdateStatus(selectedClaim.id, 'APPROVED')}>
              <Text style={styles.buttonText}>Approve</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.button, { backgroundColor: '#d32f2f', marginBottom: 10 }]} onPress={() => handleUpdateStatus(selectedClaim.id, 'REJECTED')}>
              <Text style={styles.buttonText}>Reject</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.button, { backgroundColor: '#666' }]} onPress={() => handleUpdateStatus(selectedClaim.id, 'UNDER_REVIEW')}>
              <Text style={styles.buttonText}>Under Review</Text>
            </TouchableOpacity>
          </View>
        )}

        <TouchableOpacity style={[styles.button, { backgroundColor: '#666', marginTop: 20 }]} onPress={() => setSelectedClaim(null)}>
          <Text style={styles.buttonText}>Back to List</Text>
        </TouchableOpacity>
      </ScrollView>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>My Claims</Text>
      <Text style={styles.subtitle}>Role: {userRole}</Text>
      {loading ? (
        <ActivityIndicator size="large" color="#1976d2" />
      ) : (
        <FlatList
          data={claims}
          keyExtractor={(item) => String(item.id)}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.claimCard} onPress={() => setSelectedClaim(item)}>
              <View style={styles.cardHeader}>
                <Text style={styles.claimNumber}>{item.claimNumber}</Text>
                <Text style={[styles.statusBadge, { backgroundColor: getStatusColor(item.status) }]}>
                  {item.status}
                </Text>
              </View>
              <Text>{item.claimType} - ${item.amount}</Text>
              {item.submittedByName && <Text style={styles.submitter}>By: {item.submittedByName}</Text>}
            </TouchableOpacity>
          )}
          ListEmptyComponent={<Text style={styles.empty}>No claims found</Text>}
        />
      )}
      <TouchableOpacity style={[styles.button, { backgroundColor: '#d32f2f', marginTop: 20 }]} onPress={onLogout}>
        <Text style={styles.buttonText}>Logout</Text>
      </TouchableOpacity>
    </View>
  );
};

// ============== 主 App ==============
export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkLogin = async () => {
      const savedToken = await AsyncStorage.getItem('token');
      const userStr = await AsyncStorage.getItem('user');
      if (savedToken && userStr) {
        setToken(savedToken);
        setUser(JSON.parse(userStr));
        setIsLoggedIn(true);
      }
      setLoading(false);
    };
    checkLogin();
  }, []);

  const handleLogout = async () => {
    await AsyncStorage.removeItem('token');
    await AsyncStorage.removeItem('user');
    setIsLoggedIn(false);
    setUser(null);
    setToken(null);
  };

  if (loading) {
    return <View style={[styles.container, { justifyContent: 'center' }]}><ActivityIndicator size="large" color="#1976d2" /></View>;
  }

  if (!isLoggedIn) {
    return <LoginScreen onLogin={async () => {
      const [savedToken, userStr] = await Promise.all([
        AsyncStorage.getItem('token'),
        AsyncStorage.getItem('user'),
      ]);
      if (savedToken) setToken(savedToken);
      if (userStr) setUser(JSON.parse(userStr));
      setIsLoggedIn(true);
    }} />;
  }

  return (
    <ClaimsListScreen
      token={token || ''}
      userRole={user?.roleName || 'CUSTOMER'}
      onLogout={handleLogout}
    />
  );
}

// ============== 样式 ==============
const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#f5f5f5' },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 5, textAlign: 'center', color: '#1976d2', marginTop: 40 },
  subtitle: { fontSize: 16, color: '#666', marginBottom: 20, textAlign: 'center' },
  input: { backgroundColor: '#fff', padding: 15, borderRadius: 8, marginBottom: 12, fontSize: 16, borderWidth: 1, borderColor: '#ddd' },
  button: { backgroundColor: '#1976d2', padding: 15, borderRadius: 8, alignItems: 'center', marginTop: 5 },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  claimCard: { backgroundColor: '#fff', padding: 15, borderRadius: 8, marginBottom: 12, borderLeftWidth: 4, borderLeftColor: '#1976d2' },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  claimNumber: { fontSize: 16, fontWeight: 'bold', color: '#333' },
  statusBadge: { color: '#fff', fontSize: 11, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 12, overflow: 'hidden', fontWeight: 'bold' },
  submitter: { color: '#888', fontSize: 12, marginTop: 4 },
  detailRow: { flexDirection: 'row', marginBottom: 10, paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: '#eee' },
  label: { fontWeight: 'bold', width: 100, color: '#333' },
  empty: { textAlign: 'center', color: '#888', marginTop: 40 },
});
