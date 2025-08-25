package com.atomvpn

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson

// ATOM SDK imports - Using actual available classes from SDK v6.0.4
import com.atom.sdk.android.AtomManager
import com.atom.sdk.android.VPNStateListener
import com.atom.sdk.android.VPNProperties
import com.atom.sdk.android.VPNCredentials
import com.atom.sdk.android.ConnectionDetails
import com.atom.sdk.android.PauseVPNTimer
import com.atom.sdk.android.data.callbacks.CollectionCallback
import com.atom.sdk.android.data.callbacks.Callback
import com.atom.core.exceptions.AtomException
import com.atom.core.exceptions.AtomValidationException
// Import specific core models that are available
import com.atom.core.models.Country
import com.atom.core.models.Protocol
import com.atom.core.models.Channel
import com.atom.core.models.City
import com.atom.core.models.Location
import com.atom.core.models.AtomConfiguration

class AtomVpnModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext), VPNStateListener {

    companion object {
        private var isInitialized = false
        private const val NOTIFICATION_ID = 1001
    }
    
    private val gson = Gson()
    private var currentVpnState = "DISCONNECTED"
    private var atomManager: AtomManager? = null

    override fun getName(): String {
        return "AtomVpn"
    }

    @ReactMethod
    fun initialize(secretKey: String, vpnInterfaceName: String?, promise: Promise) {
        if (isInitialized) {
            promise.resolve(true)
            return
        }

        try {
            if (TextUtils.isEmpty(secretKey)) {
                promise.reject("INIT_ERROR", "Secret key is required")
                return
            }

            // Initialize ATOM SDK using the actual available API from SDK v6.0.4
            // The SDK has a simplified initialize method that takes Application and secretKey
            AtomManager.initialize(
                reactApplicationContext.currentActivity?.application ?: reactApplicationContext as android.app.Application,
                secretKey,
                object : AtomManager.InitializeCallback {
                    override fun onInitialized(mAtomManager: AtomManager) {
                        atomManager = mAtomManager
                        isInitialized = true
                        AtomManager.addVPNStateListener(this@AtomVpnModule)
                        promise.resolve(true)
                    }
                }
            )
        } catch (e: AtomValidationException) {
            promise.reject("INITIALIZATION_ERROR", e.message, e)
        } catch (e: Exception) {
            promise.reject("INITIALIZATION_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setVPNCredentials(username: String, password: String, promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        try {
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                promise.reject("CREDENTIALS_ERROR", "Username and password are required")
                return
            }

            // Use the API method from AtomManager - it returns AtomManager for chaining
            val credentials = VPNCredentials(username, password)
            atomManager?.setVPNCredentials(credentials)
            promise.resolve(null)

        } catch (e: Exception) {
            promise.reject("CREDENTIALS_ERROR", "Failed to set credentials: ${e.message}")
        }
    }

    @ReactMethod
    fun setUUID(uuid: String, promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        try {
            if (TextUtils.isEmpty(uuid)) {
                promise.reject("UUID_ERROR", "UUID is required")
                return
            }

            atomManager?.setUUID(uuid)
            promise.resolve(null)

        } catch (e: Exception) {
            promise.reject("UUID_ERROR", "Failed to set UUID: ${e.message}")
        }
    }

    @ReactMethod
    fun getCountries(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        // Use the correct callback interface for SDK v6.0.4
        atomManager?.getCountries(object : CollectionCallback<Country> {
            override fun onSuccess(countries: List<Country>?) {
                if (countries != null) {
                    val json = gson.toJson(countries)
                    promise.resolve(json)
                } else {
                    promise.resolve("[]")
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("COUNTRIES_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("COUNTRIES_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun getProtocols(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        atomManager?.getProtocols(object : CollectionCallback<Protocol> {
            override fun onSuccess(protocols: List<Protocol>?) {
                if (protocols != null) {
                    val json = gson.toJson(protocols)
                    promise.resolve(json)
                } else {
                    promise.resolve("[]")
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("PROTOCOLS_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("PROTOCOLS_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun connect(properties: String, promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        try {
            val propertiesMap = gson.fromJson<Map<String, Any>>(
                properties, 
                object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            )
            
            val vpnPropertiesBuilder = createVPNPropertiesBuilder(propertiesMap)
            val activity = currentActivity
            
            if (activity != null) {
                // Use Context instead of Activity - AtomManager.connect takes Context
                atomManager?.connect(activity as Context, vpnPropertiesBuilder.build())
                promise.resolve(null)
            } else {
                // Fallback to using reactApplicationContext
                atomManager?.connect(reactApplicationContext, vpnPropertiesBuilder.build())
                promise.resolve(null)
            }

        } catch (e: Exception) {
            promise.reject("CONNECTION_ERROR", "Failed to connect: ${e.message}")
        }
    }

    @ReactMethod
    fun disconnect(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        try {
            // AtomManager.disconnect() takes Context parameter in SDK v6.0.4
            atomManager?.disconnect(reactApplicationContext)
            promise.resolve(null)

        } catch (e: Exception) {
            promise.reject("DISCONNECT_ERROR", "Failed to disconnect: ${e.message}")
        }
    }

    @ReactMethod
    fun getCurrentStatus(promise: Promise) {
        try {
            if (!isInitialized) {
                promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
                return
            }

            promise.resolve(currentVpnState)

        } catch (e: Exception) {
            promise.reject("STATE_ERROR", "Failed to get state: ${e.message}")
        }
    }

    @ReactMethod
    fun cancel(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
                return
            }
            
            // AtomManager.cancel() takes Context parameter in SDK v6.0.4
            atomManager?.cancel(reactApplicationContext)
            promise.resolve(null)
            
        } catch (e: Exception) {
            promise.reject("CANCEL_ERROR", "Failed to cancel: ${e.message}")
        }
    }

    @ReactMethod
    fun isVPNServicePrepared(promise: Promise) {
        try {
            // isVPNServicePrepared takes Context parameter
            val isPrepared = atomManager?.isVPNServicePrepared(reactApplicationContext) ?: false
            promise.resolve(isPrepared)
        } catch (e: Exception) {
            promise.reject("SERVICE_ERROR", "Failed to check service: ${e.message}")
        }
    }

    @ReactMethod
    fun getCountriesForSmartDialing(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        atomManager?.getCountriesForSmartDialing(object : CollectionCallback<Country> {
            override fun onSuccess(countries: List<Country>?) {
                if (countries != null) {
                    val json = gson.toJson(countries)
                    promise.resolve(json)
                } else {
                    promise.resolve("[]")
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("SMART_COUNTRIES_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("SMART_COUNTRIES_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun getRecommendedLocation(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        // SDK v6.0.4 has getRecommendedLocation that returns Location, not Country
        atomManager?.getRecommendedLocation(object : Callback<Location> {
            override fun onSuccess(location: Location?) {
                if (location != null) {
                    val json = gson.toJson(location)
                    promise.resolve(json)
                } else {
                    promise.resolve(null)
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("LOCATION_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("LOCATION_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun getChannels(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        atomManager?.getChannels(object : CollectionCallback<Channel> {
            override fun onSuccess(channels: List<Channel>?) {
                if (channels != null) {
                    val json = gson.toJson(channels)
                    promise.resolve(json)
                } else {
                    promise.resolve("[]")
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("CHANNELS_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("CHANNELS_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun getCities(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
            return
        }

        atomManager?.getCities(object : CollectionCallback<City> {
            override fun onSuccess(cities: List<City>?) {
                if (cities != null) {
                    val json = gson.toJson(cities)
                    promise.resolve(json)
                } else {
                    promise.resolve("[]")
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("CITIES_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("CITIES_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun pause(timer: String, promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
                return
            }

            val pauseTimer = when (timer) {
                "5" -> PauseVPNTimer.MINUTES_5
                "10" -> PauseVPNTimer.MINUTES_10
                "15" -> PauseVPNTimer.MINUTES_15
                "30" -> PauseVPNTimer.MINUTES_30
                "60" -> PauseVPNTimer.MINUTES_60
                else -> PauseVPNTimer.MINUTES_5
            }

            atomManager?.pause(pauseTimer)
            promise.resolve(null)

        } catch (e: Exception) {
            promise.reject("PAUSE_ERROR", "Failed to pause: ${e.message}")
        }
    }

    @ReactMethod
    fun resume(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
                return
            }

            atomManager?.resume()
            promise.resolve(null)

        } catch (e: Exception) {
            promise.reject("RESUME_ERROR", "Failed to resume: ${e.message}")
        }
    }

    @ReactMethod
    fun getConnectedIP(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
                return
            }

            // Use the simplified getConnectedIp method from SDK v6.0.4
            try {
                val connectedIP = atomManager?.getConnectedIp()
                promise.resolve(connectedIP)
            } catch (e: Exception) {
                promise.reject("IP_ERROR", "Failed to get IP: ${e.message}")
            }

        } catch (e: Exception) {
            promise.reject("IP_ERROR", "Failed to get connected IP: ${e.message}")
        }
    }

    @ReactMethod
    fun getAtomShieldData(promise: Promise) {
        try {
            if (!isInitialized || atomManager == null) {
                promise.reject("NOT_INITIALIZED", "ATOM SDK not initialized")
                return
            }

            // Return basic shield data structure for now
            // TODO: Implement real shield data when available in SDK
            val shieldData = mapOf(
                "adsBlocked" to 0,
                "trackersBlocked" to 0,
                "malwareBlocked" to 0
            )
            promise.resolve(gson.toJson(shieldData))

        } catch (e: Exception) {
            promise.reject("SHIELD_ERROR", "Failed to get shield data: ${e.message}")
        }
    }

    // Helper method to create VPN properties from JSON
    private fun createVPNPropertiesBuilder(propertiesMap: Map<String, Any>): VPNProperties.Builder {
        return when {
            propertiesMap.containsKey("country") && propertiesMap.containsKey("protocol") -> {
                // Country and protocol connection
                val country = propertiesMap["country"] as String
                val protocol = propertiesMap["protocol"] as String
                VPNProperties.Builder(country, Protocol.valueOf(protocol))
            }
            propertiesMap.containsKey("enableSmartDialing") && 
            propertiesMap["enableSmartDialing"] == true -> {
                // Smart dialing connection
                VPNProperties.Builder().withSmartDialing()
            }
            else -> {
                // Default connection
                VPNProperties.Builder()
            }
        }
    }

    // VPN State Listener implementation - Using actual SDK v6.0.4 interface
    override fun onConnected() {
        currentVpnState = "CONNECTED"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "CONNECTED"))
    }

    override fun onConnected(connectionDetails: ConnectionDetails?) {
        currentVpnState = "CONNECTED"
        if (connectionDetails != null) {
            val detailsMap = mapOf(
                "country" to (connectionDetails.country ?: ""),
                "protocol" to (connectionDetails.protocol ?: ""),
                "ip" to (connectionDetails.ip ?: ""),
                "duration" to 0,
                "state" to "CONNECTED"
            )
            sendEvent("AtomVPN:onConnectedWithDetails", detailsMap)
        }
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "CONNECTED"))
    }

    override fun onConnecting() {
        currentVpnState = "CONNECTING"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "CONNECTING"))
    }

    override fun onConnecting(vpnProperties: VPNProperties?, atomConfiguration: AtomConfiguration?) {
        currentVpnState = "CONNECTING"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "CONNECTING"))
    }

    override fun onRedialing(atomException: AtomException?, connectionDetails: ConnectionDetails?) {
        currentVpnState = "RECONNECTING"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "RECONNECTING"))
    }

    override fun onDialError(atomException: AtomException?, connectionDetails: ConnectionDetails?) {
        if (atomException != null) {
            val errorMap = mapOf(
                "code" to (atomException.code ?: "DIAL_ERROR"),
                "message" to (atomException.message ?: "Dial error"),
                "details" to (atomException.localizedMessage ?: "")
            )
            sendEvent("AtomVPN:onError", errorMap)
        }
    }

    override fun onStateChange(state: String?) {
        val stateString = state ?: "UNKNOWN"
        currentVpnState = stateString
        sendEvent("AtomVPN:onStateChange", mapOf("state" to stateString))
    }

    override fun onPaused(atomException: AtomException?, connectionDetails: ConnectionDetails?) {
        currentVpnState = "PAUSED"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "PAUSED"))
    }

    override fun onDisconnecting(connectionDetails: ConnectionDetails?) {
        currentVpnState = "DISCONNECTING"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "DISCONNECTING"))
    }

    override fun onDisconnected(hasError: Boolean) {
        currentVpnState = "DISCONNECTED"
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "DISCONNECTED", "hasError" to hasError))
    }

    override fun onDisconnected(connectionDetails: ConnectionDetails?) {
        currentVpnState = "DISCONNECTED"
        val detailsMap = if (connectionDetails != null) {
            mapOf("duration" to (connectionDetails.duration ?: 0), "state" to "DISCONNECTED")
        } else {
            mapOf("duration" to 0, "state" to "DISCONNECTED")
        }
        sendEvent("AtomVPN:onDisconnectedWithDetails", detailsMap)
        sendEvent("AtomVPN:onStateChange", mapOf("state" to "DISCONNECTED"))
    }

    override fun onUnableToAccessInternet(atomException: AtomException?, connectionDetails: ConnectionDetails?) {
        val errorMap = mapOf(
            "code" to (atomException?.code ?: "INTERNET_ERROR"),
            "message" to (atomException?.message ?: "Unable to access internet"),
            "details" to (atomException?.localizedMessage ?: "")
        )
        sendEvent("AtomVPN:onError", errorMap)
    }

    override fun onPacketsTransmitted(p0: String?, p1: String?, p2: String?, p3: String?) {
        // Optional: Handle packet transmission stats if needed
    }

    private fun sendEvent(eventName: String, data: Any?) {
        val params = Arguments.createMap()
        when (data) {
            is Map<*, *> -> {
                data.forEach { (key, value) ->
                    when (value) {
                        is String -> params.putString(key.toString(), value)
                        is Int -> params.putInt(key.toString(), value)
                        is Double -> params.putDouble(key.toString(), value)
                        is Boolean -> params.putBoolean(key.toString(), value)
                        else -> params.putString(key.toString(), value.toString())
                    }
                }
            }
            is String -> params.putString("data", data)
            else -> params.putString("data", data.toString())
        }
        
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        if (isInitialized) {
            AtomManager.removeVPNStateListener(this)
            atomManager?.disconnect(reactApplicationContext)
        }
    }
}