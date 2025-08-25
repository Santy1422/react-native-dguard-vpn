/**
 * Native module bridge for ATOM VPN SDK
 */

import { NativeModules, Platform } from 'react-native';
const LINKING_ERROR = `The package 'react-native-atom-vpn' doesn't seem to be linked. Make sure: \n\n` + Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';

// Define the native module interface based on platform

const AtomVpnModule = NativeModules.AtomVpn ? NativeModules.AtomVpn : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
export default AtomVpnModule;
//# sourceMappingURL=NativeAtomVpn.js.map