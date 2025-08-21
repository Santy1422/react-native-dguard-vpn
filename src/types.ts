/**
 * DGuard VPN SDK Types and Interfaces for React Native
 * Based on the official ATOM SDK Android and iOS implementations
 * Secure VPN solution powered by ATOM technology
 */

import type { EmitterSubscription } from 'react-native';

/**
 * Configuration for initializing the ATOM VPN SDK
 */
export interface AtomVpnConfig {
  /** Your ATOM SDK secret key */
  secretKey: string;
  /** Custom VPN interface name (optional) */
  vpnInterfaceName?: string;
}

/**
 * VPN connection properties
 */
export interface VpnProperties {
  // Basic connection parameters
  /** Country code or slug to connect to */
  country?: string;
  /** Protocol to use for connection */
  protocol?: string;
  
  // Dedicated IP connection
  /** Dedicated IP address for connection */
  dedicatedIP?: string;
  
  // Authentication credentials
  /** VPN username */
  username?: string;
  /** VPN password */
  password?: string;
  /** User UUID for authentication */
  uuid?: string;
  
  // Advanced connection options
  /** Enable connection optimization */
  enableOptimization?: boolean;
  /** Enable smart dialing mechanism */
  enableSmartDialing?: boolean;
  /** Enable protocol switching on failure */
  enableProtocolSwitch?: boolean;
  /** Use failover servers */
  useFailover?: boolean;
  
  // Multiple protocol support
  /** Secondary protocol for fallback */
  secondaryProtocol?: string;
  /** Tertiary protocol for additional fallback */
  tertiaryProtocol?: string;
  
  // AtomShield features
  /** Enable tracker blocking */
  enableTrackerBlocker?: boolean;
  /** Enable ad blocking */
  enableAdBlocker?: boolean;
  
  // Network settings
  /** Allow local network traffic */
  allowLocalNetworkTraffic?: boolean;
  
  // Port configuration
  /** Use automatic port selection */
  useAutomaticPort?: boolean;
  /** Manual port number */
  manualPort?: number;
  
  // Location specifics
  /** Specific city to connect to */
  city?: string;
  /** Specific channel to connect to */
  channel?: string;
}

/**
 * Country information
 */
export interface Country {
  /** Country display name */
  name: string;
  /** Country code (2-letter) */
  code: string;
  /** Country slug identifier */
  country: string;
  /** Available protocols for this country */
  protocols?: Protocol[];
  /** Connection latency to this country */
  latency?: number;
  /** Country ID */
  countryId?: number;
}

/**
 * VPN protocol information
 */
export interface Protocol {
  /** Protocol display name */
  name: string;
  /** Protocol type (e.g., 'udp', 'tcp', 'ikev2', 'wireguard') */
  type: string;
  /** Protocol identifier */
  protocol: string;
}

/**
 * City information
 */
export interface City {
  /** City ID */
  id: number;
  /** City name */
  name: string;
  /** Country this city belongs to */
  country: string;
  /** Available protocols for this city */
  protocols?: Protocol[];
  /** City identifier */
  cityId?: number;
}

/**
 * Channel information for advanced routing
 */
export interface Channel {
  /** Channel ID */
  id: number;
  /** Channel name */
  name: string;
  /** Available protocols for this channel */
  protocols?: Protocol[];
}

/**
 * Connection details provided in callbacks
 */
export interface ConnectionDetails {
  /** Whether VPN is connected */
  isConnected: boolean;
  /** Server IP address */
  serverIP?: string;
  /** Protocol used for connection */
  protocol?: Protocol;
  /** Connected country */
  country?: Country;
  /** Connected city */
  city?: City;
  /** Connected channel */
  channel?: Channel;
  /** Connection time in seconds */
  connectionTime?: number;
  /** Whether connection was cancelled */
  cancelled?: boolean;
  /** Pause timer if VPN is paused */
  pauseVPNTimer?: PauseTimer;
  /** Whether VPN was auto-resumed */
  isVPNAutoResumed?: boolean;
  /** Whether tracker blocker was requested */
  isTrackerBlockerRequested?: boolean;
  /** Whether ad blocker was requested */
  isAdBlockerRequested?: boolean;
  /** Whether local network traffic is allowed */
  isAllowLocalNetworkTrafficRequested?: boolean;
}

/**
 * VPN connection states
 */
export type VpnState = 
  | 'DISCONNECTED'
  | 'CONNECTING' 
  | 'CONNECTED'
  | 'DISCONNECTING'
  | 'PAUSED'
  | 'PAUSING'
  | 'RECONNECTING'
  | 'VALIDATING'
  | 'AUTHENTICATING'
  | 'REASSERTING'
  | 'VERIFYING_HOSTNAME'
  | 'GETTING_FASTEST_SERVER'
  | 'OPTIMIZING_CONNECTION'
  | 'GENERATING_CREDENTIALS';

/**
 * Pause timer options
 */
export type PauseTimer = 
  | 'MINUTES_5'
  | 'MINUTES_10'
  | 'MINUTES_15'
  | 'MINUTES_30'
  | 'MINUTES_60'
  | 'MANUAL';

/**
 * VPN error information
 */
export interface VpnError {
  /** Error code */
  code: number;
  /** Error message */
  message: string;
  /** Additional error details */
  details?: string;
}

/**
 * AtomShield statistics
 */
export interface AtomShieldData {
  /** Total blocked items counter */
  counter: number;
  /** Number of trackers blocked */
  trackerCount?: number;
  /** Number of ads blocked */
  adCount?: number;
}

/**
 * VPN credentials
 */
export interface VpnCredentials {
  /** VPN username */
  username: string;
  /** VPN password */
  password: string;
}

/**
 * Event listener callback types
 */
export type StateChangeCallback = (state: VpnState) => void;
export type ConnectedCallback = (details: ConnectionDetails) => void;
export type DisconnectedCallback = (details: ConnectionDetails) => void;
export type ErrorCallback = (error: VpnError) => void;
export type PausedCallback = (details: ConnectionDetails) => void;
export type PacketsTransmittedCallback = (
  inBytes: string,
  outBytes: string,
  inSpeed: string,
  outSpeed: string
) => void;

/**
 * Main ATOM VPN SDK interface
 */
export interface AtomVpnSdk {
  // Initialization
  initialize(config: AtomVpnConfig): Promise<boolean>;
  
  // Connection management
  connect(properties: VpnProperties): Promise<void>;
  disconnect(): Promise<void>;
  cancel(): Promise<void>;
  
  // Status checking
  getCurrentStatus(): Promise<VpnState>;
  isVPNServicePrepared(): Promise<boolean>;
  
  // Data retrieval
  getCountries(): Promise<Country[]>;
  getProtocols(): Promise<Protocol[]>;
  getCountriesForSmartDialing(): Promise<Country[]>;
  getRecommendedLocation(): Promise<Country>;
  getChannels(): Promise<Channel[]>;
  getCities(): Promise<City[]>;
  
  // Pause/Resume functionality
  pause(timer: PauseTimer): Promise<void>;
  resume(): Promise<void>;
  
  // Credentials management
  setVPNCredentials(username: string, password: string): Promise<void>;
  setUUID(uuid: string): Promise<void>;
  
  // Event listeners
  onStateChange(callback: StateChangeCallback): EmitterSubscription;
  onConnected(callback: ConnectedCallback): EmitterSubscription;
  onDisconnected(callback: DisconnectedCallback): EmitterSubscription;
  onError(callback: ErrorCallback): EmitterSubscription;
  onPaused(callback: PausedCallback): EmitterSubscription;
  onPacketsTransmitted(callback: PacketsTransmittedCallback): EmitterSubscription;
  
  // Utility methods
  getConnectedIP(): Promise<string | null>;
  getAtomShieldData(): Promise<AtomShieldData>;
}

/**
 * Native module interface for Android
 */
export interface AtomVpnNativeModuleAndroid {
  initialize(secretKey: string, vpnInterfaceName?: string): Promise<boolean>;
  connect(properties: string): Promise<void>;
  disconnect(): Promise<void>;
  cancel(): Promise<void>;
  getCurrentStatus(): Promise<string>;
  isVPNServicePrepared(): Promise<boolean>;
  getCountries(): Promise<string>;
  getProtocols(): Promise<string>;
  getCountriesForSmartDialing(): Promise<string>;
  getRecommendedLocation(): Promise<string>;
  getChannels(): Promise<string>;
  getCities(): Promise<string>;
  pause(timer: string): Promise<void>;
  resume(): Promise<void>;
  setVPNCredentials(username: string, password: string): Promise<void>;
  setUUID(uuid: string): Promise<void>;
  getConnectedIP(): Promise<string | null>;
  getAtomShieldData(): Promise<string>;
}

/**
 * Native module interface for iOS
 */
export interface AtomVpnNativeModuleIOS {
  initialize(secretKey: string, vpnInterfaceName?: string): Promise<boolean>;
  connect(properties: string): Promise<void>;
  disconnect(): Promise<void>;
  cancel(): Promise<void>;
  getCurrentStatus(): Promise<string>;
  isVPNServicePrepared(): Promise<boolean>;
  getCountries(): Promise<string>;
  getProtocols(): Promise<string>;
  getCountriesForSmartDialing(): Promise<string>;
  getRecommendedLocation(): Promise<string>;
  getChannels(): Promise<string>;
  getCities(): Promise<string>;
  pause(timer: string): Promise<void>;
  resume(): Promise<void>;
  setVPNCredentials(username: string, password: string): Promise<void>;
  setUUID(uuid: string): Promise<void>;
  getConnectedIP(): Promise<string | null>;
  getAtomShieldData(): Promise<string>;
}