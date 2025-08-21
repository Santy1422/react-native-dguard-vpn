/**
 * VPN Redux Slice for DGuard Project
 * Manages VPN state in Redux store
 */

import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { VpnState, Country, Protocol, ConnectionDetails, VpnError } from 'react-native-dguard-vpn';
import { VPNService, VPNServiceConfig } from './VPNService';

interface VPNSliceState {
  // Initialization
  isInitialized: boolean;
  currentBrand: string;
  
  // Connection state
  status: VpnState;
  isConnecting: boolean;
  isDisconnecting: boolean;
  connectedIP: string | null;
  connectionDetails: ConnectionDetails | null;
  
  // Server data
  countries: Country[];
  protocols: Protocol[];
  cities: any[];
  recommendedLocation: Country | null;
  
  // UI state
  isLoading: boolean;
  error: string | null;
  lastError: VpnError | null;
  
  // Configuration
  selectedCountry: Country | null;
  selectedProtocol: Protocol | null;
  autoConnect: boolean;
}

const initialState: VPNSliceState = {
  isInitialized: false,
  currentBrand: '',
  status: 'DISCONNECTED',
  isConnecting: false,
  isDisconnecting: false,
  connectedIP: null,
  connectionDetails: null,
  countries: [],
  protocols: [],
  cities: [],
  recommendedLocation: null,
  isLoading: false,
  error: null,
  lastError: null,
  selectedCountry: null,
  selectedProtocol: null,
  autoConnect: false,
};

// Async Thunks
export const initializeVPN = createAsyncThunk(
  'vpn/initialize',
  async (config: VPNServiceConfig, { rejectWithValue }) => {
    try {
      const vpnService = VPNService.getInstance();
      const success = await vpnService.initialize(config);
      
      if (!success) {
        throw new Error('VPN initialization failed');
      }
      
      // Load server data
      const [countries, protocols, recommended, cities] = await Promise.all([
        vpnService.getCountries(),
        vpnService.getProtocols(),
        vpnService.getRecommendedLocation().catch(() => null),
        vpnService.getCities().catch(() => [])
      ]);
      
      return {
        brand: config.brand,
        countries,
        protocols,
        recommended,
        cities
      };
    } catch (error: any) {
      return rejectWithValue(error.message || 'VPN initialization failed');
    }
  }
);

export const connectVPN = createAsyncThunk(
  'vpn/connect',
  async (properties: { country?: string; protocol?: string; useRecommended?: boolean }, { getState, rejectWithValue }) => {
    try {
      const state = getState() as { vpn: VPNSliceState };
      const vpnService = VPNService.getInstance();
      
      let connectionProps;
      
      if (properties.useRecommended && state.vpn.recommendedLocation && state.vpn.protocols.length > 0) {
        connectionProps = {
          country: state.vpn.recommendedLocation.country,
          protocol: state.vpn.protocols[0].protocol,
          enableOptimization: true,
          enableSmartDialing: true
        };
      } else if (properties.country && properties.protocol) {
        connectionProps = {
          country: properties.country,
          protocol: properties.protocol,
          enableOptimization: true,
          enableSmartDialing: true
        };
      } else {
        throw new Error('Invalid connection properties');
      }
      
      await vpnService.connect(connectionProps);
      return connectionProps;
    } catch (error: any) {
      return rejectWithValue(error.message || 'VPN connection failed');
    }
  }
);

export const disconnectVPN = createAsyncThunk(
  'vpn/disconnect',
  async (_, { rejectWithValue }) => {
    try {
      const vpnService = VPNService.getInstance();
      await vpnService.disconnect();
      return true;
    } catch (error: any) {
      return rejectWithValue(error.message || 'VPN disconnection failed');
    }
  }
);

export const quickConnectVPN = createAsyncThunk(
  'vpn/quickConnect',
  async (_, { rejectWithValue }) => {
    try {
      const vpnService = VPNService.getInstance();
      await vpnService.quickConnect();
      return true;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Quick connect failed');
    }
  }
);

export const refreshServerData = createAsyncThunk(
  'vpn/refreshServerData',
  async (_, { rejectWithValue }) => {
    try {
      const vpnService = VPNService.getInstance();
      
      const [countries, protocols, recommended] = await Promise.all([
        vpnService.getCountries(),
        vpnService.getProtocols(),
        vpnService.getRecommendedLocation().catch(() => null)
      ]);
      
      return { countries, protocols, recommended };
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to refresh server data');
    }
  }
);

export const getVPNStatus = createAsyncThunk(
  'vpn/getStatus',
  async (_, { rejectWithValue }) => {
    try {
      const vpnService = VPNService.getInstance();
      const [status, connectedIP] = await Promise.all([
        vpnService.getCurrentStatus(),
        vpnService.getConnectedIP().catch(() => null)
      ]);
      
      return { status, connectedIP };
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to get VPN status');
    }
  }
);

// Slice
const vpnSlice = createSlice({
  name: 'vpn',
  initialState,
  reducers: {
    // Real-time state updates from VPN service
    setVPNStatus: (state, action: PayloadAction<VpnState>) => {
      state.status = action.payload;
      
      // Update connecting/disconnecting flags based on status
      if (action.payload === 'CONNECTING') {
        state.isConnecting = true;
        state.isDisconnecting = false;
      } else if (action.payload === 'DISCONNECTING') {
        state.isConnecting = false;
        state.isDisconnecting = true;
      } else {
        state.isConnecting = false;
        state.isDisconnecting = false;
      }
      
      // Clear error on successful connection
      if (action.payload === 'CONNECTED') {
        state.error = null;
        state.lastError = null;
      }
    },
    
    setConnectionDetails: (state, action: PayloadAction<ConnectionDetails>) => {
      state.connectionDetails = action.payload;
      state.connectedIP = action.payload.serverIP || null;
    },
    
    setVPNError: (state, action: PayloadAction<VpnError>) => {
      state.lastError = action.payload;
      state.error = action.payload.message;
      state.isConnecting = false;
      state.isDisconnecting = false;
    },
    
    clearVPNError: (state) => {
      state.error = null;
      state.lastError = null;
    },
    
    setSelectedCountry: (state, action: PayloadAction<Country | null>) => {
      state.selectedCountry = action.payload;
    },
    
    setSelectedProtocol: (state, action: PayloadAction<Protocol | null>) => {
      state.selectedProtocol = action.payload;
    },
    
    setAutoConnect: (state, action: PayloadAction<boolean>) => {
      state.autoConnect = action.payload;
    },
    
    resetVPNState: (state) => {
      return { ...initialState };
    },
    
    setConnectedIP: (state, action: PayloadAction<string | null>) => {
      state.connectedIP = action.payload;
    }
  },
  extraReducers: (builder) => {
    // Initialize VPN
    builder
      .addCase(initializeVPN.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(initializeVPN.fulfilled, (state, action) => {
        state.isInitialized = true;
        state.currentBrand = action.payload.brand;
        state.countries = action.payload.countries;
        state.protocols = action.payload.protocols;
        state.recommendedLocation = action.payload.recommended;
        state.cities = action.payload.cities;
        state.isLoading = false;
        state.error = null;
      })
      .addCase(initializeVPN.rejected, (state, action) => {
        state.isInitialized = false;
        state.error = action.payload as string;
        state.isLoading = false;
      });
    
    // Connect VPN
    builder
      .addCase(connectVPN.pending, (state) => {
        state.isConnecting = true;
        state.error = null;
      })
      .addCase(connectVPN.fulfilled, (state, action) => {
        // Status will be updated via setVPNStatus from real-time listener
        state.error = null;
      })
      .addCase(connectVPN.rejected, (state, action) => {
        state.isConnecting = false;
        state.error = action.payload as string;
      });
    
    // Disconnect VPN
    builder
      .addCase(disconnectVPN.pending, (state) => {
        state.isDisconnecting = true;
        state.error = null;
      })
      .addCase(disconnectVPN.fulfilled, (state) => {
        // Status will be updated via setVPNStatus from real-time listener
        state.error = null;
      })
      .addCase(disconnectVPN.rejected, (state, action) => {
        state.isDisconnecting = false;
        state.error = action.payload as string;
      });
    
    // Quick Connect
    builder
      .addCase(quickConnectVPN.pending, (state) => {
        state.isConnecting = true;
        state.error = null;
      })
      .addCase(quickConnectVPN.fulfilled, (state) => {
        state.error = null;
      })
      .addCase(quickConnectVPN.rejected, (state, action) => {
        state.isConnecting = false;
        state.error = action.payload as string;
      });
    
    // Refresh Server Data
    builder
      .addCase(refreshServerData.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(refreshServerData.fulfilled, (state, action) => {
        state.countries = action.payload.countries;
        state.protocols = action.payload.protocols;
        state.recommendedLocation = action.payload.recommended;
        state.isLoading = false;
      })
      .addCase(refreshServerData.rejected, (state, action) => {
        state.error = action.payload as string;
        state.isLoading = false;
      });
    
    // Get VPN Status
    builder
      .addCase(getVPNStatus.fulfilled, (state, action) => {
        state.status = action.payload.status;
        state.connectedIP = action.payload.connectedIP;
      });
  }
});

// Actions
export const {
  setVPNStatus,
  setConnectionDetails,
  setVPNError,
  clearVPNError,
  setSelectedCountry,
  setSelectedProtocol,
  setAutoConnect,
  resetVPNState,
  setConnectedIP
} = vpnSlice.actions;

// Selectors
export const selectVPNState = (state: { vpn: VPNSliceState }) => state.vpn;
export const selectVPNStatus = (state: { vpn: VPNSliceState }) => state.vpn.status;
export const selectIsVPNConnected = (state: { vpn: VPNSliceState }) => state.vpn.status === 'CONNECTED';
export const selectVPNCountries = (state: { vpn: VPNSliceState }) => state.vpn.countries;
export const selectVPNProtocols = (state: { vpn: VPNSliceState }) => state.vpn.protocols;
export const selectVPNError = (state: { vpn: VPNSliceState }) => state.vpn.error;
export const selectIsVPNInitialized = (state: { vpn: VPNSliceState }) => state.vpn.isInitialized;

export default vpnSlice.reducer;