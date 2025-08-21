/**
 * DGuard VPN - React Native VPN Library
 * Powered by ATOM SDK technology
 * Main API interface for VPN functionality
 */

import { NativeEventEmitter, Platform } from 'react-native';
import NativeAtomVpn from './NativeAtomVpn';
import type {
  AtomVpnConfig,
  VpnProperties,
  VpnState,
  Country,
  Protocol,
  City,
  Channel,
  ConnectionDetails,
  VpnError,
  PauseTimer,
  AtomShieldData,
  AtomVpnSdk,
  StateChangeCallback,
  ConnectedCallback,
  DisconnectedCallback,
  ErrorCallback,
  PausedCallback,
  PacketsTransmittedCallback
} from './types';

/**
 * DGuard VPN SDK Implementation
 * ATOM SDK Integration Layer
 */
class AtomVpnSDK implements AtomVpnSdk {
  private eventEmitter: NativeEventEmitter;
  private isInitialized: boolean = false;

  constructor() {
    this.eventEmitter = new NativeEventEmitter(NativeAtomVpn);
  }

  /**
   * Initialize the ATOM VPN SDK
   * @param config Configuration object with secret key and optional interface name
   * @returns Promise resolving to initialization success
   */
  async initialize(config: AtomVpnConfig): Promise<boolean> {
    try {
      const result = await NativeAtomVpn.initialize(
        config.secretKey,
        config.vpnInterfaceName
      );
      this.isInitialized = result;
      return result;
    } catch (error) {
      console.error('ATOM VPN initialization failed:', error);
      throw error;
    }
  }

  /**
   * Connect to VPN with specified properties
   * @param properties Connection properties including country, protocol, etc.
   * @returns Promise resolving when connection starts
   */
  async connect(properties: VpnProperties): Promise<void> {
    if (!this.isInitialized) {
      throw new Error('DGuard VPN not initialized. Call initialize() first.');
    }

    try {
      const propertiesJson = JSON.stringify(properties);
      await NativeAtomVpn.connect(propertiesJson);
    } catch (error) {
      console.error('ATOM VPN connection failed:', error);
      throw error;
    }
  }

  /**
   * Disconnect from VPN
   * @returns Promise resolving when disconnection starts
   */
  async disconnect(): Promise<void> {
    try {
      await NativeAtomVpn.disconnect();
    } catch (error) {
      console.error('ATOM VPN disconnection failed:', error);
      throw error;
    }
  }

  /**
   * Cancel ongoing VPN connection
   * @returns Promise resolving when cancellation completes
   */
  async cancel(): Promise<void> {
    try {
      await NativeAtomVpn.cancel();
    } catch (error) {
      console.error('ATOM VPN cancellation failed:', error);
      throw error;
    }
  }

  /**
   * Get current VPN status
   * @returns Promise resolving to current VPN state
   */
  async getCurrentStatus(): Promise<VpnState> {
    try {
      const status = await NativeAtomVpn.getCurrentStatus();
      return status as VpnState;
    } catch (error) {
      console.error('Failed to get VPN status:', error);
      throw error;
    }
  }

  /**
   * Check if VPN service is prepared
   * @returns Promise resolving to service preparation status
   */
  async isVPNServicePrepared(): Promise<boolean> {
    try {
      return await NativeAtomVpn.isVPNServicePrepared();
    } catch (error) {
      console.error('Failed to check VPN service status:', error);
      throw error;
    }
  }

  /**
   * Get available countries
   * @returns Promise resolving to array of countries
   */
  async getCountries(): Promise<Country[]> {
    try {
      const countriesJson = await NativeAtomVpn.getCountries();
      return JSON.parse(countriesJson);
    } catch (error) {
      console.error('Failed to get countries:', error);
      throw error;
    }
  }

  /**
   * Get available protocols
   * @returns Promise resolving to array of protocols
   */
  async getProtocols(): Promise<Protocol[]> {
    try {
      const protocolsJson = await NativeAtomVpn.getProtocols();
      return JSON.parse(protocolsJson);
    } catch (error) {
      console.error('Failed to get protocols:', error);
      throw error;
    }
  }

  /**
   * Get countries optimized for smart dialing
   * @returns Promise resolving to array of optimized countries
   */
  async getCountriesForSmartDialing(): Promise<Country[]> {
    try {
      const countriesJson = await NativeAtomVpn.getCountriesForSmartDialing();
      return JSON.parse(countriesJson);
    } catch (error) {
      console.error('Failed to get smart dialing countries:', error);
      throw error;
    }
  }

  /**
   * Get recommended location based on optimization
   * @returns Promise resolving to recommended country
   */
  async getRecommendedLocation(): Promise<Country> {
    try {
      const locationJson = await NativeAtomVpn.getRecommendedLocation();
      if (!locationJson) {
        throw new Error('No recommended location available');
      }
      return JSON.parse(locationJson);
    } catch (error) {
      console.error('Failed to get recommended location:', error);
      throw error;
    }
  }

  /**
   * Get available channels
   * @returns Promise resolving to array of channels
   */
  async getChannels(): Promise<Channel[]> {
    try {
      const channelsJson = await NativeAtomVpn.getChannels();
      return JSON.parse(channelsJson);
    } catch (error) {
      console.error('Failed to get channels:', error);
      throw error;
    }
  }

  /**
   * Get available cities
   * @returns Promise resolving to array of cities
   */
  async getCities(): Promise<City[]> {
    try {
      const citiesJson = await NativeAtomVpn.getCities();
      return JSON.parse(citiesJson);
    } catch (error) {
      console.error('Failed to get cities:', error);
      throw error;
    }
  }

  /**
   * Pause VPN connection for specified duration
   * @param timer Pause duration
   * @returns Promise resolving when pause starts
   */
  async pause(timer: PauseTimer): Promise<void> {
    try {
      await NativeAtomVpn.pause(timer);
    } catch (error) {
      console.error('Failed to pause VPN:', error);
      throw error;
    }
  }

  /**
   * Resume paused VPN connection
   * @returns Promise resolving when resume starts
   */
  async resume(): Promise<void> {
    try {
      await NativeAtomVpn.resume();
    } catch (error) {
      console.error('Failed to resume VPN:', error);
      throw error;
    }
  }

  /**
   * Set VPN credentials
   * @param username VPN username
   * @param password VPN password
   * @returns Promise resolving when credentials are set
   */
  async setVPNCredentials(username: string, password: string): Promise<void> {
    try {
      await NativeAtomVpn.setVPNCredentials(username, password);
    } catch (error) {
      console.error('Failed to set VPN credentials:', error);
      throw error;
    }
  }

  /**
   * Set user UUID
   * @param uuid User UUID
   * @returns Promise resolving when UUID is set
   */
  async setUUID(uuid: string): Promise<void> {
    try {
      await NativeAtomVpn.setUUID(uuid);
    } catch (error) {
      console.error('Failed to set UUID:', error);
      throw error;
    }
  }

  /**
   * Get connected IP address
   * @returns Promise resolving to connected IP or null
   */
  async getConnectedIP(): Promise<string | null> {
    try {
      return await NativeAtomVpn.getConnectedIP();
    } catch (error) {
      console.error('Failed to get connected IP:', error);
      throw error;
    }
  }

  /**
   * Get AtomShield blocking statistics
   * @returns Promise resolving to shield data
   */
  async getAtomShieldData(): Promise<AtomShieldData> {
    try {
      const dataJson = await NativeAtomVpn.getAtomShieldData();
      return JSON.parse(dataJson);
    } catch (error) {
      console.error('Failed to get AtomShield data:', error);
      throw error;
    }
  }

  // Event Listeners

  /**
   * Listen for VPN state changes
   * @param callback Function to call when state changes
   * @returns Subscription object
   */
  onStateChange(callback: StateChangeCallback) {
    return this.eventEmitter.addListener('AtomVPN:onStateChange', (event) => {
      callback(event.state);
    });
  }

  /**
   * Listen for VPN connection events
   * @param callback Function to call when connected
   * @returns Subscription object
   */
  onConnected(callback: ConnectedCallback) {
    return this.eventEmitter.addListener('AtomVPN:onConnectedWithDetails', (event) => {
      callback(event);
    });
  }

  /**
   * Listen for VPN disconnection events
   * @param callback Function to call when disconnected
   * @returns Subscription object
   */
  onDisconnected(callback: DisconnectedCallback) {
    return this.eventEmitter.addListener('AtomVPN:onDisconnectedWithDetails', (event) => {
      callback(event);
    });
  }

  /**
   * Listen for VPN errors
   * @param callback Function to call when error occurs
   * @returns Subscription object
   */
  onError(callback: ErrorCallback) {
    return this.eventEmitter.addListener('AtomVPN:onError', (event) => {
      const error: VpnError = {
        code: event.code,
        message: event.message,
        details: event.details
      };
      callback(error);
    });
  }

  /**
   * Listen for VPN pause events
   * @param callback Function to call when VPN is paused
   * @returns Subscription object
   */
  onPaused(callback: PausedCallback) {
    return this.eventEmitter.addListener('AtomVPN:onPaused', (event) => {
      callback(event);
    });
  }

  /**
   * Listen for packet transmission data
   * @param callback Function to call with transmission data
   * @returns Subscription object
   */
  onPacketsTransmitted(callback: PacketsTransmittedCallback) {
    return this.eventEmitter.addListener('AtomVPN:onPacketsTransmitted', (event) => {
      callback(event.inBytes, event.outBytes, event.inSpeed, event.outSpeed);
    });
  }
}

// Create and export singleton instance
const DGuardVPN = new AtomVpnSDK();

// Export as DGuardVPN and AtomVPN for compatibility
export default DGuardVPN;
export { DGuardVPN };

// Export types for TypeScript users
export type {
  AtomVpnConfig,
  VpnProperties,
  VpnState,
  Country,
  Protocol,
  City,
  Channel,
  ConnectionDetails,
  VpnError,
  PauseTimer,
  AtomShieldData,
  StateChangeCallback,
  ConnectedCallback,
  DisconnectedCallback,
  ErrorCallback,
  PausedCallback,
  PacketsTransmittedCallback
} from './types';

// Export class for advanced usage
export { AtomVpnSDK };

/**
 * Utility function to check if DGuard VPN is supported on current platform
 * @returns Boolean indicating platform support
 */
export const isDGuardVpnSupported = (): boolean => {
  return Platform.OS === 'ios' || Platform.OS === 'android';
};

// Legacy alias for backward compatibility
export const isAtomVpnSupported = isDGuardVpnSupported;

/**
 * Get platform-specific configuration requirements
 * @returns Object with platform-specific requirements
 */
export const getPlatformRequirements = () => {
  if (Platform.OS === 'ios') {
    return {
      minimumVersion: '12.0',
      requiredCapabilities: ['VPN Configuration', 'Network Extensions'],
      requiredEntitlements: [
        'com.apple.developer.networking.networkextension',
        'com.apple.developer.networking.vpn.api'
      ],
      bundleIdentifiers: {
        tunnelProvider: '$(PRODUCT_BUNDLE_IDENTIFIER).packettunnelopenvpn',
        wireGuardTunnel: '$(PRODUCT_BUNDLE_IDENTIFIER).packettunnelwireguard',
        appGroup: 'group.$(PRODUCT_BUNDLE_IDENTIFIER)'
      }
    };
  } else if (Platform.OS === 'android') {
    return {
      minimumSdkVersion: 22,
      targetSdkVersion: 34,
      requiredPermissions: [
        'android.permission.INTERNET',
        'android.permission.ACCESS_NETWORK_STATE',
        'android.permission.ACCESS_WIFI_STATE',
        'android.permission.CHANGE_WIFI_STATE',
        'android.permission.WAKE_LOCK',
        'android.permission.FOREGROUND_SERVICE'
      ],
      dependencies: [
        'org.bitbucket.purevpn:purevpn-sdk-android:6.0.4'
      ]
    };
  } else {
    return {
      supported: false,
      reason: 'Platform not supported'
    };
  }
};