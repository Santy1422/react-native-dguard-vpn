/**
 * VPN Service for DGuard Project
 * Integrates DGuard VPN library with existing architecture
 */

import DGuardVPN, { 
  VpnState, 
  AtomVpnConfig, 
  VpnProperties,
  Country,
  Protocol,
  ConnectionDetails,
  VpnError
} from 'react-native-dguard-vpn';
import { EmitterSubscription } from 'react-native';

export interface VPNServiceConfig {
  secretKey: string;
  interfaceName: string;
  brand: 'dguard' | 'telefonica' | 'santander';
}

export class VPNService {
  private static instance: VPNService;
  private isInitialized: boolean = false;
  private currentBrand: string = '';
  private subscriptions: EmitterSubscription[] = [];
  private listeners: {
    onStateChange?: (state: VpnState) => void;
    onConnected?: (details: ConnectionDetails) => void;
    onDisconnected?: (details: ConnectionDetails) => void;
    onError?: (error: VpnError) => void;
  } = {};

  static getInstance(): VPNService {
    if (!VPNService.instance) {
      VPNService.instance = new VPNService();
    }
    return VPNService.instance;
  }

  /**
   * Initialize VPN with brand-specific configuration
   */
  async initialize(config: VPNServiceConfig): Promise<boolean> {
    if (this.isInitialized && this.currentBrand === config.brand) {
      console.log(`✅ VPN already initialized for brand: ${config.brand}`);
      return true;
    }

    try {
      console.log(`🚀 Initializing VPN for brand: ${config.brand}`);
      
      const vpnConfig: AtomVpnConfig = {
        secretKey: config.secretKey,
        vpnInterfaceName: config.interfaceName
      };

      const success = await DGuardVPN.initialize(vpnConfig);
      
      if (success) {
        this.isInitialized = true;
        this.currentBrand = config.brand;
        this.setupEventListeners();
        console.log(`✅ VPN initialized successfully for ${config.brand}`);
        return true;
      } else {
        console.error(`❌ VPN initialization failed for ${config.brand}`);
        return false;
      }
    } catch (error) {
      console.error(`❌ VPN initialization error for ${config.brand}:`, error);
      return false;
    }
  }

  /**
   * Setup event listeners for VPN state changes
   */
  private setupEventListeners(): void {
    // Clear existing subscriptions
    this.subscriptions.forEach(sub => sub.remove());
    this.subscriptions = [];

    // State change listener
    const stateSubscription = DGuardVPN.onStateChange((state: VpnState) => {
      console.log(`📡 VPN State Change: ${state}`);
      this.listeners.onStateChange?.(state);
    });

    // Connection listener
    const connectedSubscription = DGuardVPN.onConnected((details: ConnectionDetails) => {
      console.log(`🌐 VPN Connected:`, details);
      this.listeners.onConnected?.(details);
    });

    // Disconnection listener
    const disconnectedSubscription = DGuardVPN.onDisconnected((details: ConnectionDetails) => {
      console.log(`❌ VPN Disconnected:`, details);
      this.listeners.onDisconnected?.(details);
    });

    // Error listener
    const errorSubscription = DGuardVPN.onError((error: VpnError) => {
      console.error(`🚨 VPN Error [${error.code}]:`, error.message);
      this.listeners.onError?.(error);
    });

    this.subscriptions = [
      stateSubscription,
      connectedSubscription,
      disconnectedSubscription,
      errorSubscription
    ];
  }

  /**
   * Set event listeners
   */
  setListeners(listeners: {
    onStateChange?: (state: VpnState) => void;
    onConnected?: (details: ConnectionDetails) => void;
    onDisconnected?: (details: ConnectionDetails) => void;
    onError?: (error: VpnError) => void;
  }): void {
    this.listeners = listeners;
  }

  /**
   * Connect to VPN with specified properties
   */
  async connect(properties: VpnProperties): Promise<void> {
    if (!this.isInitialized) {
      throw new Error('VPN not initialized. Call initialize() first.');
    }

    console.log(`🔌 Connecting VPN with properties:`, properties);
    return DGuardVPN.connect(properties);
  }

  /**
   * Disconnect from VPN
   */
  async disconnect(): Promise<void> {
    console.log(`🔌 Disconnecting VPN`);
    return DGuardVPN.disconnect();
  }

  /**
   * Cancel ongoing VPN connection
   */
  async cancel(): Promise<void> {
    console.log(`🚫 Cancelling VPN connection`);
    return DGuardVPN.cancel();
  }

  /**
   * Get current VPN status
   */
  async getCurrentStatus(): Promise<VpnState> {
    return DGuardVPN.getCurrentStatus();
  }

  /**
   * Check if VPN service is prepared
   */
  async isVPNServicePrepared(): Promise<boolean> {
    return DGuardVPN.isVPNServicePrepared();
  }

  /**
   * Get available countries
   */
  async getCountries(): Promise<Country[]> {
    if (!this.isInitialized) {
      throw new Error('VPN not initialized');
    }
    return DGuardVPN.getCountries();
  }

  /**
   * Get available protocols
   */
  async getProtocols(): Promise<Protocol[]> {
    if (!this.isInitialized) {
      throw new Error('VPN not initialized');
    }
    return DGuardVPN.getProtocols();
  }

  /**
   * Get recommended location
   */
  async getRecommendedLocation(): Promise<Country> {
    if (!this.isInitialized) {
      throw new Error('VPN not initialized');
    }
    return DGuardVPN.getRecommendedLocation();
  }

  /**
   * Get cities
   */
  async getCities() {
    if (!this.isInitialized) {
      throw new Error('VPN not initialized');
    }
    return DGuardVPN.getCities();
  }

  /**
   * Set VPN credentials
   */
  async setVPNCredentials(username: string, password: string): Promise<void> {
    return DGuardVPN.setVPNCredentials(username, password);
  }

  /**
   * Set user UUID
   */
  async setUUID(uuid: string): Promise<void> {
    return DGuardVPN.setUUID(uuid);
  }

  /**
   * Pause VPN connection
   */
  async pause(timer: 'MINUTES_5' | 'MINUTES_10' | 'MINUTES_15' | 'MINUTES_30' | 'MINUTES_60' | 'MANUAL'): Promise<void> {
    return DGuardVPN.pause(timer);
  }

  /**
   * Resume VPN connection
   */
  async resume(): Promise<void> {
    return DGuardVPN.resume();
  }

  /**
   * Get connected IP address
   */
  async getConnectedIP(): Promise<string | null> {
    return DGuardVPN.getConnectedIP();
  }

  /**
   * Get current brand
   */
  getCurrentBrand(): string {
    return this.currentBrand;
  }

  /**
   * Check if initialized
   */
  getIsInitialized(): boolean {
    return this.isInitialized;
  }

  /**
   * Clean up subscriptions
   */
  cleanup(): void {
    console.log('🧹 Cleaning up VPN service');
    this.subscriptions.forEach(sub => sub.remove());
    this.subscriptions = [];
    this.listeners = {};
  }

  /**
   * Quick connect to recommended server
   */
  async quickConnect(): Promise<void> {
    if (!this.isInitialized) {
      throw new Error('VPN not initialized');
    }

    try {
      const [recommended, protocols] = await Promise.all([
        this.getRecommendedLocation(),
        this.getProtocols()
      ]);

      if (!recommended || protocols.length === 0) {
        throw new Error('No servers or protocols available');
      }

      await this.connect({
        country: recommended.country,
        protocol: protocols[0].protocol,
        enableOptimization: true,
        enableSmartDialing: true
      });
    } catch (error) {
      console.error('Quick connect failed:', error);
      throw error;
    }
  }

  /**
   * Get VPN statistics for the current brand
   */
  getVPNStats() {
    return {
      brand: this.currentBrand,
      isInitialized: this.isInitialized,
      hasActiveListeners: this.subscriptions.length > 0
    };
  }
}

// Export singleton instance
export const vpnService = VPNService.getInstance();