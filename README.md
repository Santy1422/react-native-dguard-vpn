# DGuard VPN - React Native

A complete React Native VPN library powered by ATOM SDK technology, providing secure VPN connectivity with full TypeScript support and Expo Development Builds compatibility.

## Features

- ✅ **Full TypeScript Support** - Complete type definitions for all SDK features
- ✅ **Expo Development Builds** - Compatible with Expo managed workflow
- ✅ **Cross Platform** - Support for both iOS and Android
- ✅ **Complete API Coverage** - All ATOM SDK features exposed
- ✅ **Event-Driven Architecture** - Real-time VPN status and data monitoring
- ✅ **Smart Dialing** - Automatic server optimization
- ✅ **Multiple Protocols** - Support for OpenVPN, IKEv2, WireGuard
- ✅ **AtomShield** - Built-in ad and tracker blocking
- ✅ **Pause/Resume** - VPN connection management
- ✅ **Dedicated IP** - Support for dedicated IP connections

## Installation

```bash
npm install react-native-dguard-vpn
# or
yarn add react-native-dguard-vpn
```

### iOS Setup

1. **Install CocoaPods dependencies:**
   ```bash
   cd ios && pod install
   ```

2. **Configure App Groups and Network Extensions:**
   
   Add the following to your `Info.plist`:
   ```xml
   <key>NSAppTransportSecurity</key>
   <dict>
       <key>NSAllowsArbitraryLoads</key>
       <true/>
   </dict>
   ```

3. **Add Network Extension capability** in Xcode:
   - Select your app target
   - Go to "Signing & Capabilities"
   - Add "Network Extensions" capability
   - Add "App Groups" capability

4. **Create Network Extension targets** (for advanced VPN functionality):
   
   Create two new Network Extension targets:
   - `PacketTunnelOpenVPN` (for OpenVPN/IKEv2)
   - `PacketTunnelWireGuard` (for WireGuard)

### Android Setup

1. **Add to `android/settings.gradle`:**
   ```gradle
   include ':react-native-dguard-vpn'
   project(':react-native-dguard-vpn').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-dguard-vpn/android')
   ```

2. **Add to `android/app/build.gradle`:**
   ```gradle
   dependencies {
       implementation project(':react-native-dguard-vpn')
   }
   ```

3. **Add to `MainApplication.java`:**
   ```java
   import com.atomvpn.AtomVpnPackage;
   
   @Override
   protected List<ReactPackage> getPackages() {
       return Arrays.<ReactPackage>asList(
           new MainReactPackage(),
           new AtomVpnPackage()  // Add this line
       );
   }
   ```

4. **Add to `android/gradle.properties`:**
   ```properties
   authToken=jp_l1hv3212tltdau845qago2l4e
   ```

### Expo Development Builds Setup

1. **Create development build:**
   ```bash
   expo install expo-dev-client
   expo run:ios --device
   # or
   expo run:android --device
   ```

2. **Add to `app.json`:**
   ```json
   {
     "expo": {
       "plugins": [
         [
           "expo-build-properties",
           {
             "ios": {
               "deploymentTarget": "12.0"
             },
             "android": {
               "minSdkVersion": 22
             }
           }
         ]
       ]
     }
   }
   ```

## Usage

### Basic Example

```typescript
import DGuardVPN from 'react-native-dguard-vpn';

const App = () => {
  const initializeVPN = async () => {
    try {
      const success = await DGuardVPN.initialize({
        secretKey: 'YOUR_ATOM_SECRET_KEY',
        vpnInterfaceName: 'My VPN App'
      });
      
      if (success) {
        console.log('DGuard VPN initialized successfully');
      }
    } catch (error) {
      console.error('VPN initialization failed:', error);
    }
  };

  const connectToVPN = async () => {
    try {
      await DGuardVPN.connect({
        country: 'US',
        protocol: 'ikev2',
        enableOptimization: true,
        enableSmartDialing: true
      });
    } catch (error) {
      console.error('VPN connection failed:', error);
    }
  };

  const disconnectVPN = async () => {
    try {
      await DGuardVPN.disconnect();
    } catch (error) {
      console.error('VPN disconnection failed:', error);
    }
  };

  return (
    // Your app UI
  );
};
```

### Advanced Connection with Dedicated IP

```typescript
const connectWithDedicatedIP = async () => {
  try {
    await DGuardVPN.setVPNCredentials('username', 'password');
    
    await DGuardVPN.connect({
      dedicatedIP: '123.456.789.123',
      protocol: 'openvpn_udp',
      enableTrackerBlocker: true,
      enableAdBlocker: true
    });
  } catch (error) {
    console.error('Dedicated IP connection failed:', error);
  }
};
```

### Event Monitoring

```typescript
import { useEffect } from 'react';
import DGuardVPN, { VpnState, ConnectionDetails, VpnError } from 'react-native-atom-vpn';

const VPNMonitor = () => {
  useEffect(() => {
    // Listen for state changes
    const stateSubscription = DGuardVPN.onStateChange((state: VpnState) => {
      console.log('VPN State:', state);
      switch (state) {
        case 'CONNECTING':
          console.log('Connecting to VPN...');
          break;
        case 'CONNECTED':
          console.log('VPN Connected!');
          break;
        case 'DISCONNECTED':
          console.log('VPN Disconnected');
          break;
      }
    });

    // Listen for connection details
    const connectedSubscription = DGuardVPN.onConnected((details: ConnectionDetails) => {
      console.log('Connected to server:', details.serverIP);
      console.log('Protocol:', details.protocol?.name);
      console.log('Country:', details.country?.name);
    });

    // Listen for errors
    const errorSubscription = DGuardVPN.onError((error: VpnError) => {
      console.error('VPN Error:', error.message, 'Code:', error.code);
    });

    // Listen for data transmission
    const dataSubscription = DGuardVPN.onPacketsTransmitted((inBytes, outBytes, inSpeed, outSpeed) => {
      console.log(`Data: ↓${inBytes} ↑${outBytes} Speed: ↓${inSpeed} ↑${outSpeed}`);
    });

    return () => {
      stateSubscription.remove();
      connectedSubscription.remove();
      errorSubscription.remove();
      dataSubscription.remove();
    };
  }, []);

  return null; // Your monitoring UI
};
```

### Server Selection

```typescript
const selectOptimalServer = async () => {
  try {
    // Get all available countries
    const countries = await DGuardVPN.getCountries();
    console.log('Available countries:', countries.length);

    // Get recommended location
    const recommended = await DGuardVPN.getRecommendedLocation();
    console.log('Recommended:', recommended.name, 'Latency:', recommended.latency);

    // Get available protocols
    const protocols = await DGuardVPN.getProtocols();
    console.log('Available protocols:', protocols.map(p => p.name));

    // Connect to recommended location
    await DGuardVPN.connect({
      country: recommended.country,
      protocol: protocols[0].protocol,
      enableOptimization: true
    });
  } catch (error) {
    console.error('Server selection failed:', error);
  }
};
```

### Pause and Resume

```typescript
const pauseVPN = async () => {
  try {
    await DGuardVPN.pause('MINUTES_30'); // Pause for 30 minutes
    console.log('VPN paused for 30 minutes');
  } catch (error) {
    console.error('Failed to pause VPN:', error);
  }
};

const resumeVPN = async () => {
  try {
    await DGuardVPN.resume();
    console.log('VPN resumed');
  } catch (error) {
    console.error('Failed to resume VPN:', error);
  }
};
```

## API Reference

### Initialization

#### `initialize(config: AtomVpnConfig): Promise<boolean>`
Initialize the ATOM VPN SDK with your secret key.

**Parameters:**
- `config.secretKey: string` - Your ATOM SDK secret key
- `config.vpnInterfaceName?: string` - Custom VPN interface name

### Connection Management

#### `connect(properties: VpnProperties): Promise<void>`
Connect to VPN with specified properties.

#### `disconnect(): Promise<void>`
Disconnect from VPN.

#### `cancel(): Promise<void>`
Cancel ongoing VPN connection attempt.

### Status and Information

#### `getCurrentStatus(): Promise<VpnState>`
Get current VPN connection state.

#### `isVPNServicePrepared(): Promise<boolean>`
Check if VPN service is prepared and ready.

#### `getConnectedIP(): Promise<string | null>`
Get the current connected IP address.

### Data Retrieval

#### `getCountries(): Promise<Country[]>`
Get list of available countries.

#### `getProtocols(): Promise<Protocol[]>`
Get list of available protocols.

#### `getCities(): Promise<City[]>`
Get list of available cities.

#### `getRecommendedLocation(): Promise<Country>`
Get recommended server location.

#### `getCountriesForSmartDialing(): Promise<Country[]>`
Get optimized countries for smart dialing.

### Credentials Management

#### `setVPNCredentials(username: string, password: string): Promise<void>`
Set VPN authentication credentials.

#### `setUUID(uuid: string): Promise<void>`
Set user UUID for authentication.

### Event Listeners

#### `onStateChange(callback: (state: VpnState) => void): EmitterSubscription`
Listen for VPN state changes.

#### `onConnected(callback: (details: ConnectionDetails) => void): EmitterSubscription`
Listen for successful connections.

#### `onDisconnected(callback: (details: ConnectionDetails) => void): EmitterSubscription`
Listen for disconnections.

#### `onError(callback: (error: VpnError) => void): EmitterSubscription`
Listen for VPN errors.

#### `onPacketsTransmitted(callback: (inBytes, outBytes, inSpeed, outSpeed) => void): EmitterSubscription`
Listen for data transmission statistics.

## TypeScript Types

### VpnState
```typescript
type VpnState = 
  | 'DISCONNECTED'
  | 'CONNECTING' 
  | 'CONNECTED'
  | 'DISCONNECTING'
  | 'PAUSED'
  | 'RECONNECTING'
  | 'VALIDATING'
  | 'AUTHENTICATING';
```

### VpnProperties
```typescript
interface VpnProperties {
  country?: string;
  protocol?: string;
  dedicatedIP?: string;
  username?: string;
  password?: string;
  uuid?: string;
  enableOptimization?: boolean;
  enableSmartDialing?: boolean;
  secondaryProtocol?: string;
  tertiaryProtocol?: string;
  enableTrackerBlocker?: boolean;
  enableAdBlocker?: boolean;
  city?: string;
  // ... and more
}
```

## Platform Requirements

### iOS
- iOS 12.0+
- Xcode 15.3+
- Network Extensions capability
- App Groups capability

### Android
- Android API Level 22+ (Android 5.1)
- Kotlin 1.9.22
- compileSdk 34

## Error Codes

Common ATOM SDK error codes:

- `5039` - Connection cancelled
- `5194-5201` - Various connection errors
- `5177-5183` - Authentication errors
- `5190` - Network errors

## Troubleshooting

### iOS Issues

1. **Profile Installation Failed:**
   - Ensure Network Extensions capability is added
   - Check App Groups configuration
   - Verify bundle identifiers match

2. **Connection Timeout:**
   - Check network connectivity
   - Verify secret key is correct
   - Ensure VPN profile is installed

### Android Issues

1. **SDK Not Found:**
   - Verify authToken in gradle.properties
   - Check build.gradle repository configuration
   - Clean and rebuild project

2. **Permission Denied:**
   - Ensure all required permissions are added
   - Check VPN preparation status
   - Request VPN permission from user

### Common Issues

1. **Initialization Failed:**
   - Verify secret key is valid
   - Check network connectivity
   - Ensure platform requirements are met

2. **Connection Refused:**
   - Try different server/protocol
   - Check credentials if using dedicated IP
   - Verify account has active subscription

## Support

For issues related to:
- **React Native Library:** Open an issue on GitHub
- **ATOM SDK:** Contact ATOM SDK support
- **Account/Billing:** Contact your ATOM VPN provider

## License

MIT License - see LICENSE file for details.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request