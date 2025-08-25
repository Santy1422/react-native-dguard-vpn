/**
 * Native module bridge for ATOM VPN SDK
 */

import { NativeModules, Platform } from 'react-native';
import type { 
  AtomVpnNativeModuleAndroid, 
  AtomVpnNativeModuleIOS 
} from './types';

const LINKING_ERROR =
  `The package 'react-native-atom-vpn' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// Define the native module interface based on platform
type AtomVpnNativeModule = typeof Platform.OS extends 'android' 
  ? AtomVpnNativeModuleAndroid 
  : AtomVpnNativeModuleIOS;

const AtomVpnModule = NativeModules.AtomVpn
  ? NativeModules.AtomVpn as AtomVpnNativeModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    ) as AtomVpnNativeModule;

export default AtomVpnModule;