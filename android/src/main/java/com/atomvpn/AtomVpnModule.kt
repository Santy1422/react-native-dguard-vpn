package com.atomvpn

import android.app.Activity
import android.content.Context
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import java.lang.reflect.Method
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * Real VPN Module using ATOM SDK with reflection
 * This implementation uses reflection to call ATOM SDK methods
 * without depending on missing core interface classes
 */
class AtomVpnModule(private val reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext) {
    
    companion object {
        const val MODULE_NAME = "AtomVpn"
        private var atomManager: Any? = null
        private var isInitialized = false
    }
    
    private var connectionPromise: Promise? = null
    
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
            
            // Use reflection to get AtomManager instance
            val atomManagerClass = Class.forName("com.atom.sdk.android.AtomManager")
            val getInstanceMethod = atomManagerClass.getMethod("getInstance")
            atomManager = getInstanceMethod.invoke(null)
            
            if (atomManager == null) {
                promise.reject("INIT_ERROR", "Failed to get AtomManager instance")
                return
            }
            
            // Create AtomConfiguration using reflection
            val configClass = Class.forName("com.atom.core.models.AtomConfiguration\$Builder")
            val configBuilder = configClass.getConstructor(String::class.java).newInstance(secretKey)
            val buildMethod = configClass.getMethod("build")
            val atomConfiguration = buildMethod.invoke(configBuilder)
            
            // Initialize with reflection
            val initMethod = atomManagerClass.getMethod("initialize", 
                Context::class.java, 
                Class.forName("com.atom.core.models.AtomConfiguration"))
            initMethod.invoke(atomManager, activity, atomConfiguration)
            
            isInitialized = true
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("INIT_ERROR", "Failed to initialize: ${e.message}")
        }
    }
    
    @ReactMethod
    fun connect(countryCode: String, protocol: String, promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            connectionPromise = promise
            
            val activity = currentActivity
            if (activity == null) {
                promise.reject("NO_ACTIVITY", "Activity is null")
                return
            }
            
            // Get a country by code using reflection
            val atomManagerClass = atomManager!!.javaClass
            val getCountriesMethod = atomManagerClass.getMethod("getCountries", 
                Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback"))
            
            // Create callback proxy for getting countries
            val callbackInterface = Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback")
            val callback = Proxy.newProxyInstance(
                callbackInterface.classLoader,
                arrayOf(callbackInterface),
                CountriesCallback(countryCode, protocol, promise)
            )
            
            getCountriesMethod.invoke(atomManager, callback)
            
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
            
            val atomManagerClass = atomManager!!.javaClass
            val disconnectMethod = atomManagerClass.getMethod("disconnect")
            disconnectMethod.invoke(atomManager)
            
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("DISCONNECT_ERROR", "Failed to disconnect: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getCurrentStatus(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.resolve("DISCONNECTED")
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            
            // Check if connected using reflection
            val isConnectedMethod = atomManagerClass.getMethod("isConnected")
            val isConnected = isConnectedMethod.invoke(atomManager) as Boolean
            
            if (isConnected) {
                promise.resolve("CONNECTED")
            } else {
                val isConnectingMethod = atomManagerClass.getMethod("isConnecting")
                val isConnecting = isConnectingMethod.invoke(atomManager) as Boolean
                promise.resolve(if (isConnecting) "CONNECTING" else "DISCONNECTED")
            }
            
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
            
            val atomManagerClass = atomManager!!.javaClass
            val getCountriesMethod = atomManagerClass.getMethod("getCountries", 
                Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback"))
            
            val callbackInterface = Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback")
            val callback = Proxy.newProxyInstance(
                callbackInterface.classLoader,
                arrayOf(callbackInterface),
                GetCountriesCallback(promise)
            )
            
            getCountriesMethod.invoke(atomManager, callback)
            
        } catch (e: Exception) {
            promise.reject("GET_COUNTRIES_ERROR", "Failed to get countries: ${e.message}")
        }
    }
    
    // Inner class for countries callback
    private inner class CountriesCallback(
        private val countryCode: String,
        private val protocol: String,
        private val promise: Promise?
    ) : InvocationHandler {
        
        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
            when (method?.name) {
                "onSuccess" -> {
                    try {
                        val countries = args?.get(0) as? List<*>
                        val targetCountry = countries?.find { country ->
                            val codeField = country?.javaClass?.getField("code")
                            codeField?.get(country) == countryCode
                        }
                        
                        if (targetCountry != null) {
                            // Connect to the country
                            connectToCountry(targetCountry, protocol)
                        } else {
                            promise?.reject("COUNTRY_NOT_FOUND", "Country $countryCode not found")
                        }
                    } catch (e: Exception) {
                        promise?.reject("CONNECT_ERROR", "Error processing countries: ${e.message}")
                    }
                }
                "onError", "onNetworkError" -> {
                    promise?.reject("CONNECT_ERROR", "Failed to get countries")
                }
            }
            return null
        }
    }
    
    // Inner class for get countries callback
    private inner class GetCountriesCallback(private val promise: Promise) : InvocationHandler {
        
        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
            when (method?.name) {
                "onSuccess" -> {
                    try {
                        val countries = args?.get(0) as? List<*>
                        val countriesArray = WritableNativeArray()
                        
                        countries?.forEach { country ->
                            val countryMap = WritableNativeMap()
                            try {
                                val nameField = country?.javaClass?.getField("name")
                                val codeField = country?.javaClass?.getField("code")
                                
                                countryMap.putString("name", nameField?.get(country) as? String ?: "")
                                countryMap.putString("code", codeField?.get(country) as? String ?: "")
                                countriesArray.pushMap(countryMap)
                            } catch (e: Exception) {
                                // Skip this country if fields are not accessible
                            }
                        }
                        
                        promise.resolve(countriesArray)
                    } catch (e: Exception) {
                        promise.reject("GET_COUNTRIES_ERROR", "Error processing countries: ${e.message}")
                    }
                }
                "onError", "onNetworkError" -> {
                    promise.reject("GET_COUNTRIES_ERROR", "Failed to get countries")
                }
            }
            return null
        }
    }
    
    private fun connectToCountry(country: Any, protocol: String) {
        try {
            if (atomManager == null) return
            
            val activity = currentActivity ?: return
            
            // Create VPNProperties using reflection
            val vpnPropertiesClass = Class.forName("com.atom.sdk.android.VPNProperties\$Builder")
            val protocolClass = Class.forName("com.atom.core.models.Protocol")
            val protocolEnum = protocolClass.enumConstants?.find { 
                it.toString() == protocol 
            } ?: protocolClass.enumConstants?.get(0) // Default to first protocol
            
            val vpnPropsBuilder = vpnPropertiesClass.getConstructor(country.javaClass, protocolClass)
                .newInstance(country, protocolEnum)
            val buildMethod = vpnPropertiesClass.getMethod("build")
            val vpnProperties = buildMethod.invoke(vpnPropsBuilder)
            
            // Connect using reflection
            val atomManagerClass = atomManager!!.javaClass
            val connectMethod = atomManagerClass.getMethod("connect", 
                Activity::class.java, 
                vpnProperties.javaClass)
            connectMethod.invoke(atomManager, activity, vpnProperties)
            
            // Success
            sendEvent("vpn-connecting", "")
            connectionPromise?.resolve(true)
            connectionPromise = null
            
        } catch (e: Exception) {
            connectionPromise?.reject("CONNECT_ERROR", "Failed to connect: ${e.message}")
            connectionPromise = null
        }
    }
    
    @ReactMethod
    fun addListener(eventName: String) {
        // Keep track of listeners - required for NativeEventEmitter
    }
    
    @ReactMethod
    fun removeListeners(count: Int) {
        // Remove listeners - required for NativeEventEmitter
    }
    
    private fun sendEvent(eventName: String, data: Any) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, data)
    }
}