/**
 * DGuard VPN - React Native VPN Library
 * Powered by ATOM SDK technology
 * Main API interface for VPN functionality
 */
import type { AtomVpnConfig, VpnProperties, VpnState, Country, Protocol, City, Channel, PauseTimer, AtomShieldData, AtomVpnSdk, StateChangeCallback, ConnectedCallback, DisconnectedCallback, ErrorCallback, PausedCallback, PacketsTransmittedCallback } from './types';
/**
 * DGuard VPN SDK Implementation
 * ATOM SDK Integration Layer
 */
declare class AtomVpnSDK implements AtomVpnSdk {
    private eventEmitter;
    private isInitialized;
    constructor();
    /**
     * Initialize the ATOM VPN SDK
     * @param config Configuration object with secret key and optional interface name
     * @returns Promise resolving to initialization success
     */
    initialize(config: AtomVpnConfig): Promise<boolean>;
    /**
     * Connect to VPN with specified properties
     * @param properties Connection properties including country, protocol, etc.
     * @returns Promise resolving when connection starts
     */
    connect(properties: VpnProperties): Promise<void>;
    /**
     * Disconnect from VPN
     * @returns Promise resolving when disconnection starts
     */
    disconnect(): Promise<void>;
    /**
     * Cancel ongoing VPN connection
     * @returns Promise resolving when cancellation completes
     */
    cancel(): Promise<void>;
    /**
     * Get current VPN status
     * @returns Promise resolving to current VPN state
     */
    getCurrentStatus(): Promise<VpnState>;
    /**
     * Check if VPN service is prepared
     * @returns Promise resolving to service preparation status
     */
    isVPNServicePrepared(): Promise<boolean>;
    /**
     * Get available countries
     * @returns Promise resolving to array of countries
     */
    getCountries(): Promise<Country[]>;
    /**
     * Get available protocols
     * @returns Promise resolving to array of protocols
     */
    getProtocols(): Promise<Protocol[]>;
    /**
     * Get countries optimized for smart dialing
     * @returns Promise resolving to array of optimized countries
     */
    getCountriesForSmartDialing(): Promise<Country[]>;
    /**
     * Get recommended location based on optimization
     * @returns Promise resolving to recommended country
     */
    getRecommendedLocation(): Promise<Country>;
    /**
     * Get available channels
     * @returns Promise resolving to array of channels
     */
    getChannels(): Promise<Channel[]>;
    /**
     * Get available cities
     * @returns Promise resolving to array of cities
     */
    getCities(): Promise<City[]>;
    /**
     * Pause VPN connection for specified duration
     * @param timer Pause duration
     * @returns Promise resolving when pause starts
     */
    pause(timer: PauseTimer): Promise<void>;
    /**
     * Resume paused VPN connection
     * @returns Promise resolving when resume starts
     */
    resume(): Promise<void>;
    /**
     * Set VPN credentials
     * @param username VPN username
     * @param password VPN password
     * @returns Promise resolving when credentials are set
     */
    setVPNCredentials(username: string, password: string): Promise<void>;
    /**
     * Set user UUID
     * @param uuid User UUID
     * @returns Promise resolving when UUID is set
     */
    setUUID(uuid: string): Promise<void>;
    /**
     * Get connected IP address
     * @returns Promise resolving to connected IP or null
     */
    getConnectedIP(): Promise<string | null>;
    /**
     * Get AtomShield blocking statistics
     * @returns Promise resolving to shield data
     */
    getAtomShieldData(): Promise<AtomShieldData>;
    /**
     * Listen for VPN state changes
     * @param callback Function to call when state changes
     * @returns Subscription object
     */
    onStateChange(callback: StateChangeCallback): import("react-native").EmitterSubscription;
    /**
     * Listen for VPN connection events
     * @param callback Function to call when connected
     * @returns Subscription object
     */
    onConnected(callback: ConnectedCallback): import("react-native").EmitterSubscription;
    /**
     * Listen for VPN disconnection events
     * @param callback Function to call when disconnected
     * @returns Subscription object
     */
    onDisconnected(callback: DisconnectedCallback): import("react-native").EmitterSubscription;
    /**
     * Listen for VPN errors
     * @param callback Function to call when error occurs
     * @returns Subscription object
     */
    onError(callback: ErrorCallback): import("react-native").EmitterSubscription;
    /**
     * Listen for VPN pause events
     * @param callback Function to call when VPN is paused
     * @returns Subscription object
     */
    onPaused(callback: PausedCallback): import("react-native").EmitterSubscription;
    /**
     * Listen for packet transmission data
     * @param callback Function to call with transmission data
     * @returns Subscription object
     */
    onPacketsTransmitted(callback: PacketsTransmittedCallback): import("react-native").EmitterSubscription;
}
declare const DGuardVPN: AtomVpnSDK;
export default DGuardVPN;
export { DGuardVPN };
export type { AtomVpnConfig, VpnProperties, VpnState, Country, Protocol, City, Channel, ConnectionDetails, VpnError, PauseTimer, AtomShieldData, StateChangeCallback, ConnectedCallback, DisconnectedCallback, ErrorCallback, PausedCallback, PacketsTransmittedCallback } from './types';
export { AtomVpnSDK };
/**
 * Utility function to check if DGuard VPN is supported on current platform
 * @returns Boolean indicating platform support
 */
export declare const isDGuardVpnSupported: () => boolean;
export declare const isAtomVpnSupported: () => boolean;
/**
 * Get platform-specific configuration requirements
 * @returns Object with platform-specific requirements
 */
export declare const getPlatformRequirements: () => {
    minimumVersion: string;
    requiredCapabilities: string[];
    requiredEntitlements: string[];
    bundleIdentifiers: {
        tunnelProvider: string;
        wireGuardTunnel: string;
        appGroup: string;
    };
    minimumSdkVersion?: undefined;
    targetSdkVersion?: undefined;
    requiredPermissions?: undefined;
    dependencies?: undefined;
    supported?: undefined;
    reason?: undefined;
} | {
    minimumSdkVersion: number;
    targetSdkVersion: number;
    requiredPermissions: string[];
    dependencies: string[];
    minimumVersion?: undefined;
    requiredCapabilities?: undefined;
    requiredEntitlements?: undefined;
    bundleIdentifiers?: undefined;
    supported?: undefined;
    reason?: undefined;
} | {
    supported: boolean;
    reason: string;
    minimumVersion?: undefined;
    requiredCapabilities?: undefined;
    requiredEntitlements?: undefined;
    bundleIdentifiers?: undefined;
    minimumSdkVersion?: undefined;
    targetSdkVersion?: undefined;
    requiredPermissions?: undefined;
    dependencies?: undefined;
};
//# sourceMappingURL=index.d.ts.map