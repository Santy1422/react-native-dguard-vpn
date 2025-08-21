# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2024-01-21

### ✨ Added
- **Enhanced `quickConnect` method** for both iOS and Android platforms
- **Smart protocol selection** - Prioritizes IKEv2 > OpenVPN UDP > Others
- **Optimized server selection** - Uses recommended locations when available
- **Multiple fallback strategies** - Ensures connection reliability
- **Platform-specific secret key support** - Better Android/iOS configuration

### 🛠️ Improved
- **Connection reliability** - Multiple fallback mechanisms for failed connections
- **Error handling** - More descriptive error messages and better debugging
- **Performance optimization** - Automatic protocol and server optimization
- **Smart dialing** - Enhanced connection speed and stability

### 🔧 Technical Enhancements
- **Android**: Robust `quickConnect` implementation with nested callbacks
- **iOS**: Optimized country selection with protocol prioritization
- **Both platforms**: Automatic port configuration and connection optimization

### 🐛 Bug Fixes
- Fixed "unable to establish connection" errors
- Improved credential handling and authentication
- Better handling of network state changes
- Enhanced SDK initialization reliability

### 📚 Documentation
- Updated implementation guide with latest features
- Added troubleshooting section for connection issues
- Improved API documentation for quickConnect method

## [1.0.0] - 2024-01-20

### 🎉 Initial Release
- **Complete VPN functionality** for React Native
- **Cross-platform support** (iOS & Android)
- **ATOM SDK integration** with full API coverage
- **Expo Development Builds** compatibility
- **Multi-brand architecture** support (DGuard, Telefónica, Santander)
- **TypeScript support** with comprehensive type definitions
- **Event-driven architecture** with real-time state monitoring
- **Redux integration** ready
- **Professional error handling** and logging
- **Multiple VPN protocols** support (OpenVPN, IKEv2, WireGuard)
- **Comprehensive documentation** and tutorials