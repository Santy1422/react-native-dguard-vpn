import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import DGuardVPN, { VpnState, Country, Protocol } from 'react-native-dguard-vpn';

const DGuardVPNExample = () => {
  const [vpnState, setVpnState] = useState<VpnState>('DISCONNECTED');
  const [countries, setCountries] = useState<Country[]>([]);
  const [protocols, setProtocols] = useState<Protocol[]>([]);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    initializeVPN();
    setupEventListeners();
    
    return () => {
      // Cleanup listeners if needed
    };
  }, []);

  const initializeVPN = async () => {
    try {
      console.log('Initializing DGuard VPN...');
      
      const success = await DGuardVPN.initialize({
        secretKey: 'YOUR_ATOM_SECRET_KEY', // Replace with your actual key
        vpnInterfaceName: 'My VPN App'
      });
      
      if (success) {
        console.log('✅ DGuard VPN initialized successfully');
        setIsInitialized(true);
        
        // Load countries and protocols
        await loadServerData();
      } else {
        console.error('❌ DGuard VPN initialization failed');
        Alert.alert('Error', 'VPN initialization failed');
      }
    } catch (error) {
      console.error('❌ VPN initialization error:', error);
      Alert.alert('Error', `VPN initialization failed: ${error}`);
    }
  };

  const loadServerData = async () => {
    try {
      // Get available countries
      const countriesList = await DGuardVPN.getCountries();
      setCountries(countriesList);
      console.log(`📍 Loaded ${countriesList.length} countries`);
      
      // Get available protocols
      const protocolsList = await DGuardVPN.getProtocols();
      setProtocols(protocolsList);
      console.log(`🔐 Loaded ${protocolsList.length} protocols`);
      
      // Get recommended location
      const recommended = await DGuardVPN.getRecommendedLocation();
      console.log(`🎯 Recommended location: ${recommended.name}`);
    } catch (error) {
      console.error('❌ Failed to load server data:', error);
    }
  };

  const setupEventListeners = () => {
    // Listen for state changes
    const stateSubscription = DGuardVPN.onStateChange((state) => {
      console.log(`🔄 VPN State changed: ${state}`);
      setVpnState(state);
      
      switch (state) {
        case 'CONNECTING':
          console.log('⏳ Connecting to VPN...');
          break;
        case 'CONNECTED':
          console.log('✅ VPN Connected!');
          Alert.alert('Success', 'VPN Connected!');
          break;
        case 'DISCONNECTED':
          console.log('❌ VPN Disconnected');
          break;
        case 'RECONNECTING':
          console.log('🔄 VPN Reconnecting...');
          break;
      }
    });

    // Listen for connection details
    const connectedSubscription = DGuardVPN.onConnected((details) => {
      console.log('🌐 Connected to server:', details.serverIP);
      console.log('🔐 Protocol:', details.protocol?.name);
      console.log('🌍 Country:', details.country?.name);
    });

    // Listen for disconnection
    const disconnectedSubscription = DGuardVPN.onDisconnected((details) => {
      console.log('❌ Disconnected:', details.cancelled ? 'Cancelled' : 'Normal');
    });

    // Listen for errors
    const errorSubscription = DGuardVPN.onError((error) => {
      console.error(`❌ VPN Error [${error.code}]:`, error.message);
      Alert.alert('VPN Error', `${error.message} (Code: ${error.code})`);
    });

    // Listen for data transmission
    const dataSubscription = DGuardVPN.onPacketsTransmitted((inBytes, outBytes, inSpeed, outSpeed) => {
      console.log(`📊 Data: ↓${inBytes} ↑${outBytes} Speed: ↓${inSpeed} ↑${outSpeed}`);
    });
  };

  const connectToVPN = async () => {
    if (!isInitialized) {
      Alert.alert('Error', 'VPN not initialized');
      return;
    }

    try {
      console.log('🚀 Connecting to VPN...');
      
      // Use first available country and protocol
      const country = countries[0];
      const protocol = protocols[0];
      
      if (!country || !protocol) {
        Alert.alert('Error', 'No countries or protocols available');
        return;
      }

      await DGuardVPN.connect({
        country: country.country,
        protocol: protocol.protocol,
        enableOptimization: true,
        enableSmartDialing: true
      });
      
      console.log(`🌍 Connecting to ${country.name} via ${protocol.name}`);
    } catch (error) {
      console.error('❌ Connection failed:', error);
      Alert.alert('Connection Error', `Failed to connect: ${error}`);
    }
  };

  const disconnectVPN = async () => {
    try {
      console.log('🛑 Disconnecting VPN...');
      await DGuardVPN.disconnect();
    } catch (error) {
      console.error('❌ Disconnection failed:', error);
      Alert.alert('Disconnection Error', `Failed to disconnect: ${error}`);
    }
  };

  const getVPNStatus = async () => {
    try {
      const status = await DGuardVPN.getCurrentStatus();
      console.log(`📊 Current VPN Status: ${status}`);
      Alert.alert('VPN Status', status);
    } catch (error) {
      console.error('❌ Failed to get status:', error);
    }
  };

  const connectWithCredentials = async () => {
    try {
      // Set credentials first
      await DGuardVPN.setVPNCredentials('your_username', 'your_password');
      
      // Then connect
      await DGuardVPN.connect({
        country: 'US',
        protocol: 'ikev2',
        username: 'your_username',
        password: 'your_password'
      });
    } catch (error) {
      console.error('❌ Credential connection failed:', error);
      Alert.alert('Error', `Credential connection failed: ${error}`);
    }
  };

  return (
    <View style={{ flex: 1, padding: 20, justifyContent: 'center' }}>
      <Text style={{ fontSize: 24, textAlign: 'center', marginBottom: 20 }}>
        DGuard VPN Demo
      </Text>
      
      <Text style={{ textAlign: 'center', marginBottom: 20 }}>
        Status: {vpnState}
      </Text>
      
      <Text style={{ textAlign: 'center', marginBottom: 20 }}>
        Countries: {countries.length} | Protocols: {protocols.length}
      </Text>
      
      <TouchableOpacity
        style={{
          backgroundColor: vpnState === 'CONNECTED' ? '#ff4444' : '#4CAF50',
          padding: 15,
          borderRadius: 5,
          marginBottom: 10
        }}
        onPress={vpnState === 'CONNECTED' ? disconnectVPN : connectToVPN}
      >
        <Text style={{ color: 'white', textAlign: 'center', fontWeight: 'bold' }}>
          {vpnState === 'CONNECTED' ? 'DISCONNECT' : 'CONNECT'}
        </Text>
      </TouchableOpacity>
      
      <TouchableOpacity
        style={{
          backgroundColor: '#2196F3',
          padding: 15,
          borderRadius: 5,
          marginBottom: 10
        }}
        onPress={getVPNStatus}
      >
        <Text style={{ color: 'white', textAlign: 'center' }}>
          Get Status
        </Text>
      </TouchableOpacity>
      
      <TouchableOpacity
        style={{
          backgroundColor: '#FF9800',
          padding: 15,
          borderRadius: 5,
          marginBottom: 10
        }}
        onPress={connectWithCredentials}
      >
        <Text style={{ color: 'white', textAlign: 'center' }}>
          Connect with Credentials
        </Text>
      </TouchableOpacity>
      
      <Text style={{ textAlign: 'center', marginTop: 20, fontSize: 12, color: '#666' }}>
        {isInitialized ? '✅ SDK Initialized' : '❌ SDK Not Initialized'}
      </Text>
    </View>
  );
};

export default DGuardVPNExample;