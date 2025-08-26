package com.atomvpn

import android.app.Activity
import android.content.Context
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson

// ATOM SDK imports - Using only available classes from SDK v6.0.4
import com.atom.sdk.android.AtomManager
import com.atom.sdk.android.VPNStateListener
import com.atom.sdk.android.VPNProperties
import com.atom.sdk.android.VPNCredentials
import com.atom.sdk.android.ConnectionDetails
import com.atom.sdk.android.Country
import com.atom.sdk.android.City

class AtomVpnModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext), VPNStateListener {
    
    companion object {
        const val MODULE_NAME = "AtomVpn"
        private var atomManager: AtomManager? = null
        private var isInitialized = false
    }
    
    private var connectionPromise: Promise? = null
    private var reactContext: ReactApplicationContext = reactContext
    
    override fun getName(): String = MODULE_NAME
    
    @ReactMethod
    fun initialize(secretKey: String, promise: Promise) {
        try {
            if (isInitialized) {
                promise.resolve(true)
                return
            }
            
            val activity = currentActivity
            if (activity == null) {
                promise.reject("NO_ACTIVITY", "Activity is null")
                return
            }
            
            // Initialize AtomManager with just the basic configuration
            atomManager = AtomManager.getInstance()
            atomManager?.let { manager ->
                manager.initialize(activity, secretKey, object : AtomManager.InitializeCallback {
                    override fun onInitialized() {
                        isInitialized = true
                        promise.resolve(true)
                    }
                })
            } ?: promise.reject("INIT_ERROR", "Failed to get AtomManager instance")
            
        } catch (e: Exception) {
            promise.reject("INIT_ERROR", "Failed to initialize: ${e.message}")
        }
    }
    
    @ReactMethod
    fun connect(countryCode: String, protocol: String, promise: Promise) {
        try {
            connectionPromise = promise
            
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val activity = currentActivity
            if (activity == null) {
                promise.reject("NO_ACTIVITY", "Activity is null")
                return
            }
            
            // Simple connection without Protocol enum
            val credentials = VPNCredentials("", "")
            atomManager?.connect(activity, credentials)
            
        } catch (e: Exception) {
            promise.reject("CONNECT_ERROR", "Failed to connect: ${e.message}")
        }
    }
    
    @ReactMethod
    fun disconnect(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            atomManager?.disconnect()
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("DISCONNECT_ERROR", "Failed to disconnect: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getCurrentStatus(promise: Promise) {
        try {
            val status = when {
                atomManager?.isConnected() == true -> "CONNECTED"
                atomManager?.isConnecting() == true -> "CONNECTING"
                else -> "DISCONNECTED"
            }
            promise.resolve(status)
        } catch (e: Exception) {
            promise.resolve("DISCONNECTED")
        }
    }
    
    @ReactMethod
    fun getCountries(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            // Return empty array for now since we don't have the proper callback interface
            val countries = WritableNativeArray()
            promise.resolve(countries)
            
        } catch (e: Exception) {
            promise.reject("GET_COUNTRIES_ERROR", "Failed to get countries: ${e.message}")
        }
    }
    
    // VPNStateListener implementation
    override fun onStateChange(vpnState: VPNStateListener.VPNState) {
        sendEvent("vpn-state-changed", vpnState.name)
    }
    
    override fun onConnected(connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-connected", connectionDetails?.toString() ?: "")
        connectionPromise?.resolve(true)
        connectionPromise = null
    }
    
    override fun onConnecting(vpnProperties: VPNProperties?, atomConfiguration: Any?) {
        sendEvent("vpn-connecting", "")
    }
    
    override fun onRedialing(atomException: Any?, connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-redialing", connectionDetails?.toString() ?: "")
    }
    
    override fun onDialError(atomException: Any?, connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-dial-error", "Connection failed")
        connectionPromise?.reject("DIAL_ERROR", "Failed to establish VPN connection")
        connectionPromise = null
    }
    
    override fun onDisconnected(connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-disconnected", connectionDetails?.toString() ?: "")
    }
    
    override fun onDisconnected(disconnectedBy: Any?, connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-disconnected", connectionDetails?.toString() ?: "")
    }
    
    override fun onPaused(atomException: Any?, connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-paused", connectionDetails?.toString() ?: "")
    }
    
    override fun onPacketsReceived(rx: String?, tx: String?) {
        val data = WritableNativeMap()
        data.putString("rx", rx ?: "0")
        data.putString("tx", tx ?: "0")
        sendEvent("vpn-packets", data)
    }
    
    override fun onUnableToAccessInternet(atomException: Any?, connectionDetails: ConnectionDetails?) {
        sendEvent("vpn-no-internet", "Unable to access internet")
    }
    
    override fun onConnectedLocation(location: Any?) {
        sendEvent("vpn-location", location?.toString() ?: "")
    }
    
    private fun sendEvent(eventName: String, data: Any) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, data)
    }
}