// Updates needed for DGuard app.config.js

// 1. Add VPN permissions to Android configuration
const androidPermissionsUpdate = [
  'CAMERA',
  'READ_EXTERNAL_STORAGE',
  'WRITE_EXTERNAL_STORAGE',
  'RECORD_AUDIO',
  'INTERNET',
  'ACCESS_NETWORK_STATE',
  'VIBRATE',
  'WAKE_LOCK',
  'RECEIVE_BOOT_COMPLETED',
  'POST_NOTIFICATIONS',
  'ACCESS_COARSE_LOCATION',
  'ACCESS_FINE_LOCATION',
  'ACCESS_BACKGROUND_LOCATION',
  // ADD THESE VPN PERMISSIONS:
  'ACCESS_WIFI_STATE',
  'CHANGE_WIFI_STATE',
  'FOREGROUND_SERVICE'
];

// 2. Add iOS VPN capabilities
const iosInfoPlistUpdate = {
  CFBundleDisplayName: brand.displayName,
  LSApplicationQueriesSchemes: [brand.scheme],
  NSCameraUsageDescription: "Esta aplicación necesita acceso a la cámara para tomar fotos y escanear documentos.",
  NSPhotoLibraryUsageDescription: "Esta aplicación necesita acceso a la galería para seleccionar imágenes.",
  NSLocationWhenInUseUsageDescription: "Esta aplicación necesita acceso a tu ubicación para verificar transacciones y mejorar la seguridad.",
  NSLocationAlwaysAndWhenInUseUsageDescription: "Esta aplicación necesita acceso a tu ubicación para verificar transacciones y mejorar la seguridad.",
  NSLocationAlwaysUsageDescription: "Esta aplicación necesita acceso a tu ubicación para verificar transacciones y mejorar la seguridad en segundo plano.",
  // ADD THESE VPN DESCRIPTIONS:
  NSNetworkExtensionDescription: "Esta aplicación utiliza VPN para proteger tu conexión y datos financieros."
};

// 3. Add VPN configuration to brand configs
const brandConfigsWithVPN = {
  dguard: {
    name: 'DGuard',
    displayName: 'DGuard',
    slug: 'dguard',
    scheme: 'dguard',
    iconPath: './assets/brands/dguard/icon.png',
    splashPath: './assets/brands/dguard/splash.png',
    bundleId: nodeEnv === 'development' ? 'com.dguard.app.dev' : 
              nodeEnv === 'staging' ? 'com.dguard.app.staging' : 'com.dguard.app',
    googleServicesFile: './google-services-dguard.json',
    projectId: '09d4a6cf-8fe8-4611-b32c-0c62fe6faffa',
    firebaseConfig: firebaseConfig,
    privacyPolicyUrl: 'https://gist.github.com/Santy1422/4475f562fe3601ce744805737f787444',
    // ADD VPN CONFIG:
    vpn: {
      secretKey: process.env.DGUARD_VPN_SECRET_KEY || 'YOUR_DGUARD_VPN_SECRET_KEY',
      interfaceName: 'DGuard VPN',
      enabled: true
    }
  },
  telefonica: {
    name: 'Telefónica Segura',
    displayName: 'Telefónica Segura',
    slug: 'dguard-telefonica',
    scheme: 'telefonica-dguard',
    iconPath: './assets/brands/telefonica/icon.png',
    splashPath: './assets/brands/telefonica/splash.png',
    bundleId: nodeEnv === 'development' ? 'com.telefonica.dguard.dev' : 
              nodeEnv === 'staging' ? 'com.telefonica.dguard.staging' : 'com.telefonica.dguard',
    googleServicesFile: './google-services-telefonica.json',
    projectId: '09d4a6cf-8fe8-4611-b32c-0c62fe6faffa',
    firebaseConfig: firebaseConfig,
    privacyPolicyUrl: 'https://gist.github.com/Santy1422/4475f562fe3601ce744805737f787444',
    // ADD VPN CONFIG:
    vpn: {
      secretKey: process.env.TELEFONICA_VPN_SECRET_KEY || 'YOUR_TELEFONICA_VPN_SECRET_KEY',
      interfaceName: 'Telefónica Segura VPN',
      enabled: true
    }
  },
  santander: {
    name: 'Santander Protege',
    displayName: 'Santander Protege',
    slug: 'dguard-santander',
    scheme: 'santander-dguard',
    iconPath: './assets/brands/santander/icon.png',
    splashPath: './assets/brands/santander/splash.png',
    bundleId: nodeEnv === 'development' ? 'com.santander.dguard.dev' : 
              nodeEnv === 'staging' ? 'com.santander.dguard.staging' : 'com.santander.dguard',
    googleServicesFile: './google-services-santander.json',
    projectId: '09d4a6cf-8fe8-4611-b32c-0c62fe6faffa',
    firebaseConfig: firebaseConfig,
    privacyPolicyUrl: 'https://gist.github.com/Santy1422/4475f562fe3601ce744805737f787444',
    // ADD VPN CONFIG:
    vpn: {
      secretKey: process.env.SANTANDER_VPN_SECRET_KEY || 'YOUR_SANTANDER_VPN_SECRET_KEY',
      interfaceName: 'Santander Protege VPN',
      enabled: true
    }
  }
};

// 4. Add VPN config to extra section
const extraConfigUpdate = {
  brandId: process.env.EXPO_PUBLIC_BRAND_ID || 'telefonica',
  brandConfig: {
    name: process.env.EXPO_PUBLIC_BRAND_ID || 'telefonica',
    displayName: brand.displayName,
    bundleId: brand.bundleId,
    appName: brand.displayName,
    scheme: brand.scheme,
    buildVariant: process.env.EXPO_PUBLIC_BRAND_ID || 'telefonica',
    iconPath: brand.iconPath,
    splashPath: brand.splashPath,
    googleServicesFile: brand.googleServicesFile,
    firebaseConfig: brand.firebaseConfig,
    appleTeamId: 'TELEFONICA_TEAM_ID',
    privacyPolicyUrl: brand.privacyPolicyUrl,
    // ADD VPN CONFIG:
    vpn: brand.vpn,
    environment: {
      EXPO_PUBLIC_BRAND_ID: process.env.EXPO_PUBLIC_BRAND_ID || 'telefonica',
      EXPO_PUBLIC_API_URL: process.env.EXPO_PUBLIC_API_URL,
      EXPO_PUBLIC_APP_NAME: process.env.EXPO_PUBLIC_APP_NAME || brand.displayName,
      // ADD VPN ENVIRONMENT VARIABLES:
      VPN_SECRET_KEY: brand.vpn.secretKey,
      VPN_INTERFACE_NAME: brand.vpn.interfaceName
    }
  },
  eas: {
    projectId: brand.projectId
  }
};

module.exports = {
  androidPermissionsUpdate,
  iosInfoPlistUpdate,
  brandConfigsWithVPN,
  extraConfigUpdate
};