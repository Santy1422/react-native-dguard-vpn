module.exports = {
  dependency: {
    platforms: {
      android: {
        sourceDir: '../android/',
        javaPackageName: 'com.atomvpn',
        packageImportPath: 'import com.atomvpn.AtomVpnPackage;',
      },
      ios: {
        podspecPath: '../react-native-dguard-vpn.podspec',
      },
    },
  },
};