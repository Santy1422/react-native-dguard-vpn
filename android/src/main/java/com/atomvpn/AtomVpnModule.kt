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
    fun initialize(secretKey: String, vpnInterfaceName: String? = null, promise: Promise) {
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
    fun connect(propertiesJson: String, promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val gson = Gson()
            val properties = gson.fromJson(propertiesJson, Map::class.java)
            val countryCode = properties["countryCode"] as? String ?: "US"
            val protocol = properties["protocol"] as? String ?: "AUTO"
            
            connectToVPN(countryCode, protocol, promise)
            
        } catch (e: Exception) {
            promise.reject("CONNECT_ERROR", "Failed to connect: ${e.message}")
        }
    }
    
    @ReactMethod
    fun connectToVPN(countryCode: String, protocol: String, promise: Promise) {
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
    
    // Additional API methods to match JavaScript interface
    @ReactMethod
    fun cancel(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            val cancelMethod = atomManagerClass.getMethod("cancel")
            cancelMethod.invoke(atomManager)
            
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("CANCEL_ERROR", "Failed to cancel: ${e.message}")
        }
    }
    
    @ReactMethod
    fun isVPNServicePrepared(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.resolve(false)
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            val isPreparedMethod = atomManagerClass.getMethod("isVPNServicePrepared", Context::class.java)
            val isPrepared = isPreparedMethod.invoke(atomManager, reactContext) as Boolean
            
            promise.resolve(isPrepared)
            
        } catch (e: Exception) {
            promise.resolve(false)
        }
    }
    
    @ReactMethod
    fun getProtocols(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            val getProtocolsMethod = atomManagerClass.getMethod("getProtocols",
                Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback"))
            
            val callbackInterface = Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback")
            val callback = Proxy.newProxyInstance(
                callbackInterface.classLoader,
                arrayOf(callbackInterface),
                GenericCollectionCallback(promise, "PROTOCOLS")
            )
            
            getProtocolsMethod.invoke(atomManager, callback)
            
        } catch (e: Exception) {
            promise.reject("GET_PROTOCOLS_ERROR", "Failed to get protocols: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getCountriesForSmartDialing(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            // Use regular getCountries method as fallback
            getCountries(promise)
            
        } catch (e: Exception) {
            promise.reject("GET_SMART_COUNTRIES_ERROR", "Failed to get smart dialing countries: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getRecommendedLocation(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            // For now, return the first available country as recommended
            getCountries(promise)
            
        } catch (e: Exception) {
            promise.reject("GET_RECOMMENDED_LOCATION_ERROR", "Failed to get recommended location: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getChannels(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            // Return empty array as channels might not be available
            val channels = WritableNativeArray()
            promise.resolve(channels)
            
        } catch (e: Exception) {
            promise.reject("GET_CHANNELS_ERROR", "Failed to get channels: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getCities(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            val getCitiesMethod = atomManagerClass.getMethod("getCities",
                Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback"))
            
            val callbackInterface = Class.forName("com.atom.sdk.android.data.callbacks.CollectionCallback")
            val callback = Proxy.newProxyInstance(
                callbackInterface.classLoader,
                arrayOf(callbackInterface),
                GenericCollectionCallback(promise, "CITIES")
            )
            
            getCitiesMethod.invoke(atomManager, callback)
            
        } catch (e: Exception) {
            promise.reject("GET_CITIES_ERROR", "Failed to get cities: ${e.message}")
        }
    }
    
    @ReactMethod
    fun pause(timer: String, promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            // For simplicity, just disconnect temporarily
            disconnect(promise)
            
        } catch (e: Exception) {
            promise.reject("PAUSE_ERROR", "Failed to pause VPN: ${e.message}")
        }
    }
    
    @ReactMethod
    fun resume(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            // Resume is essentially reconnecting with last properties
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("RESUME_ERROR", "Failed to resume VPN: ${e.message}")
        }
    }
    
    @ReactMethod
    fun setVPNCredentials(username: String, password: String, promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            val credentialsClass = Class.forName("com.atom.sdk.android.VPNCredentials")
            val credentials = credentialsClass.getConstructor(String::class.java, String::class.java)
                .newInstance(username, password)
            
            val setCredentialsMethod = atomManagerClass.getMethod("setVPNCredentials", credentialsClass)
            setCredentialsMethod.invoke(atomManager, credentials)
            
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("SET_CREDENTIALS_ERROR", "Failed to set VPN credentials: ${e.message}")
        }
    }
    
    @ReactMethod
    fun setUUID(uuid: String, promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "VPN Manager not initialized")
                return
            }
            
            val atomManagerClass = atomManager!!.javaClass
            val setUUIDMethod = atomManagerClass.getMethod("setUUID", String::class.java)
            setUUIDMethod.invoke(atomManager, uuid)
            
            promise.resolve(true)
            
        } catch (e: Exception) {
            promise.reject("SET_UUID_ERROR", "Failed to set UUID: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getConnectedIP(promise: Promise) {
        try {
            // Return null as this might require additional implementation
            promise.resolve(null)
            
        } catch (e: Exception) {
            promise.reject("GET_IP_ERROR", "Failed to get connected IP: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getAtomShieldData(promise: Promise) {
        try {
            // Return empty object as AtomShield might not be configured
            val shieldData = WritableNativeMap()
            shieldData.putInt("blockedAds", 0)
            shieldData.putInt("blockedMalware", 0)
            shieldData.putInt("blockedTrackers", 0)
            promise.resolve(shieldData)
            
        } catch (e: Exception) {
            promise.reject("GET_SHIELD_DATA_ERROR", "Failed to get AtomShield data: ${e.message}")
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
    
    // Generic collection callback for reusable API calls
    private inner class GenericCollectionCallback(
        private val promise: Promise,
        private val dataType: String
    ) : InvocationHandler {
        
        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
            when (method?.name) {
                "onSuccess" -> {
                    try {
                        val collection = args?.get(0) as? List<*>
                        val resultArray = WritableNativeArray()
                        
                        collection?.forEach { item ->
                            val itemMap = WritableNativeMap()
                            try {
                                // Extract common fields based on data type
                                when (dataType) {
                                    "PROTOCOLS" -> {
                                        val nameMethod = item?.javaClass?.getMethod("name")
                                        val name = nameMethod?.invoke(item) as? String
                                        itemMap.putString("name", name ?: "")
                                        itemMap.putString("value", name ?: "")
                                    }
                                    "CITIES" -> {
                                        val nameField = item?.javaClass?.getField("name")
                                        val codeField = item?.javaClass?.getField("code")
                                        itemMap.putString("name", nameField?.get(item) as? String ?: "")
                                        itemMap.putString("code", codeField?.get(item) as? String ?: "")
                                    }
                                    else -> {
                                        val nameField = item?.javaClass?.getField("name")
                                        val codeField = item?.javaClass?.getField("code")
                                        itemMap.putString("name", nameField?.get(item) as? String ?: "")
                                        itemMap.putString("code", codeField?.get(item) as? String ?: "")
                                    }
                                }
                                resultArray.pushMap(itemMap)
                            } catch (e: Exception) {
                                // Skip this item if fields are not accessible
                            }
                        }
                        
                        promise.resolve(resultArray)
                    } catch (e: Exception) {
                        promise.reject("GET_${dataType}_ERROR", "Error processing ${dataType.toLowerCase()}: ${e.message}")
                    }
                }
                "onError", "onNetworkError" -> {
                    promise.reject("GET_${dataType}_ERROR", "Failed to get ${dataType.toLowerCase()}")
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