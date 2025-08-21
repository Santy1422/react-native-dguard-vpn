# Troubleshooting Guide

## Common Issues and Solutions

### iOS Issues

#### 1. "AtomSDK/AtomSDK.h file not found"
**Solution:**
```bash
cd ios
pod install --repo-update
```
Make sure your Podfile includes:
```ruby
pod 'AtomSDKBySecure'
```

#### 2. "Network Extension capability missing"
**Solution:**
1. Open Xcode project
2. Select your target
3. Go to "Signing & Capabilities"
4. Add "Network Extensions" capability
5. Add "App Groups" capability

#### 3. "VPN profile installation failed"
**Solution:**
- Test on physical device (not simulator)
- Check that your secret key is valid
- Ensure app is signed with development/distribution profile

#### 4. "Module 'react-native-atom-vpn' not found"
**Solution:**
```bash
cd ios && pod install
npx react-native clean
npx react-native start --reset-cache
```

### Android Issues

#### 1. "Could not resolve: org.bitbucket.purevpn:purevpn-sdk-android:6.0.4"
**Solution:**
Add to `android/gradle.properties`:
```
authToken=jp_l1hv3212tltdau845qago2l4e
```

Add to `android/build.gradle` (project level):
```gradle
allprojects {
    repositories {
        maven { 
            url "https://bitbucket.org/purevpn/atom-android-releases/raw/master"
        }
        maven { 
            url 'https://jitpack.io'
            credentials { username authToken }
        }
    }
}
```

#### 2. "AtomVpnModule not found"
**Solution:**
Add to `MainApplication.java`:
```java
import com.atomvpn.AtomVpnPackage;

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new AtomVpnPackage() // Add this
    );
}
```

#### 3. "VPN permission denied"
**Solution:**
Make sure these permissions are in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

#### 4. "Multidex issue"
**Solution:**
Add to `android/app/build.gradle`:
```gradle
android {
    defaultConfig {
        multiDexEnabled true
    }
}

dependencies {
    implementation 'com.android.support:multidex:1.0.3'
}
```

### React Native Issues

#### 1. "AtomVPN.initialize is not a function"
**Solution:**
Check import:
```typescript
import AtomVPN from 'react-native-atom-vpn'; // Default import
// NOT: import { AtomVPN } from 'react-native-atom-vpn';
```

#### 2. "Promise rejected with no error"
**Solution:**
Add proper error handling:
```typescript
try {
    await AtomVPN.initialize({ secretKey: 'your_key' });
} catch (error) {
    console.error('Detailed error:', error);
}
```

#### 3. "Event listeners not working"
**Solution:**
Make sure to remove listeners in cleanup:
```typescript
useEffect(() => {
    const subscription = AtomVPN.onStateChange(callback);
    return () => subscription.remove(); // Important!
}, []);
```

### VPN Connection Issues

#### 1. "Connection timeout"
**Possible causes:**
- Invalid secret key
- Network connectivity issues
- Server unavailable
- Firewall blocking VPN protocols

**Solutions:**
- Verify secret key with ATOM support
- Try different protocol (OpenVPN, IKEv2, WireGuard)
- Test on different network
- Check with different server location

#### 2. "Authentication failed"
**Solutions:**
- Check username/password if using credentials
- Verify UUID format if using UUID authentication
- Contact ATOM support for account issues

#### 3. "VPN connects but no internet"
**Solutions:**
- Try different DNS servers
- Check `allowLocalNetworkTraffic` setting
- Test with different protocols
- Check device network settings

### Expo Issues

#### 1. "VPN not working in Expo Go"
**Solution:**
VPN functionality requires a development build. Expo Go cannot run VPN apps.
```bash
expo install expo-dev-client
expo run:ios --device
# or
expo run:android --device
```

#### 2. "Development build fails"
**Solution:**
Make sure `app.json` includes:
```json
{
  "expo": {
    "plugins": [
      [
        "expo-build-properties",
        {
          "ios": { "deploymentTarget": "12.0" },
          "android": { "minSdkVersion": 22 }
        }
      ]
    ]
  }
}
```

### Performance Issues

#### 1. "App crashes on VPN connect"
**Solution:**
- Check device memory
- Test on different device
- Add crash reporting to identify specific issue
- Check ProGuard rules for Android

#### 2. "High battery usage"
**Solution:**
- Use pause/resume functionality when app is backgrounded
- Optimize connection frequency
- Use appropriate protocols for use case

## Getting Help

1. **Check logs:** Enable verbose logging in development
2. **Test environment:** Always test VPN on physical devices
3. **Network conditions:** Test on different networks (WiFi, cellular)
4. **SDK version:** Ensure you're using compatible ATOM SDK version
5. **React Native version:** Check compatibility with your RN version

## Diagnostic Commands

### iOS
```bash
# Check pod installation
pod --version
pod repo update

# Check iOS deployment target
xcrun simctl list

# Check signing
codesign -dv YourApp.app
```

### Android
```bash
# Check Gradle configuration
./gradlew dependencies

# Check SDK installation
./gradlew assembleDebug --info

# Check permissions
adb shell dumpsys package com.yourapp | grep permission
```

### React Native
```bash
# Reset Metro cache
npx react-native start --reset-cache

# Clean builds
npx react-native clean

# Check linking
npx react-native config
```

If you're still having issues, please provide:
1. Platform (iOS/Android)
2. React Native version
3. Device model and OS version
4. Complete error logs
5. Steps to reproduce