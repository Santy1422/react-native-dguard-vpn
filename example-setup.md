# CRITICAL SETUP INSTRUCTIONS

## IMPORTANT: Before using this library

### 1. iOS Project Setup (REQUIRED)

Add this to your main app's `Podfile`:

```ruby
source 'https://github.com/CocoaPods/Specs.git'

# Add this BEFORE your target
install! 'cocoapods',
  :deterministic_uuids => false

target 'YourAppName' do
  # Your other dependencies
  
  # ATOM VPN Library
  pod 'react-native-atom-vpn', :path => '../node_modules/react-native-atom-vpn'
  
  # ATOM SDK source (REQUIRED)
  pod 'AtomSDKBySecure'
end

# Add post install script
post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['EXCLUDED_ARCHS[sdk=iphonesimulator*]'] = 'arm64'
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '12.0'
    end
  end
end
```

### 2. Android Project Setup (REQUIRED)

Add this to `android/gradle.properties`:
```
authToken=jp_l1hv3212tltdau845qago2l4e
```

Add this to `android/build.gradle` (project level):
```gradle
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        
        // REQUIRED FOR ATOM SDK
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

### 3. Replace YOUR_ATOM_SECRET_KEY

In your app code, replace `YOUR_ATOM_SECRET_KEY` with your actual ATOM SDK secret key.

### 4. Network Extension Setup (iOS - Advanced)

For full VPN functionality on iOS, you need to create Network Extension targets. This is optional but recommended for production apps.

### 5. Permissions (Android)

Make sure your `android/app/src/main/AndroidManifest.xml` includes:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

## Testing the Integration

1. Install dependencies
2. Run `cd ios && pod install` (iOS)
3. Run your app on device (not simulator for VPN testing)
4. Initialize ATOM SDK in your app
5. Test basic connection

## Common Issues

1. **iOS**: "AtomSDK not found" - Make sure you've added the pod source
2. **Android**: "SDK not found" - Check authToken and maven repositories  
3. **Both**: VPN not working - Make sure you're testing on physical device