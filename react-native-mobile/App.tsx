import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

const API_BASE_URL = 'http://localhost:8082';

const LoginScreen: React.FC<{ navigation: any }> = ({ navigation }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
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
        navigation.navigate('Home');
      } else {
        Alert.alert('Error', data.message || 'Login failed');
      }
    } catch (error) {
      Alert.alert('Error', 'Network error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Insurance Claim</Text>
      <TextInput style={styles.input} placeholder="Username" value={username} onChangeText={setUsername} />
      <TextInput style={styles.input} placeholder="Password" secureTextEntry value={password} onChangeText={setPassword} />
      <TouchableOpacity style={styles.button} onPress={handleLogin} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Sign In</Text>}
      </TouchableOpacity>
    </View>
  );
};

const HomeScreen: React.FC<{ navigation: any }> = ({ navigation }) => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  React.useEffect(() => {
    fetchClaims();
  }, []);

  const fetchClaims = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      const response = await fetch(`${API_BASE_URL}/claims/my`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await response.json();
      setClaims(data.data || []);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>My Claims</Text>
      {loading ? <ActivityIndicator /> : claims.map((claim: any) => (
        <TouchableOpacity key={claim.id} style={styles.claimCard} onPress={() => navigation.navigate('ClaimDetail', { claim })}>
          <Text style={styles.claimNumber}>{claim.claimNumber}</Text>
          <Text>{claim.claimType} - ${claim.amount}</Text>
          <Text style={styles.status}>{claim.status}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#f5f5f5' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  input: { backgroundColor: '#fff', padding: 15, borderRadius: 5, marginBottom: 10 },
  button: { backgroundColor: '#1976d2', padding: 15, borderRadius: 5, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold' },
  claimCard: { backgroundColor: '#fff', padding: 15, borderRadius: 5, marginBottom: 10 },
  claimNumber: { fontSize: 16, fontWeight: 'bold' },
  status: { color: '#666', marginTop: 5 },
});

export default function App() {
  return (
    <View style={{ flex: 1 }}>
      <LoginScreen navigation={{ navigate: () => {} }} />
    </View>
  );
}
