"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _reactNative = require("react-native");
/**
 * Native module bridge for ATOM VPN SDK
 */

const LINKING_ERROR = `The package 'react-native-atom-vpn' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo Go\n';

// Define the native module interface based on platform

const AtomVpnModule = _reactNative.NativeModules.AtomVpn ? _reactNative.NativeModules.AtomVpn : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
var _default = exports.default = AtomVpnModule;
//# sourceMappingURL=NativeAtomVpn.js.map