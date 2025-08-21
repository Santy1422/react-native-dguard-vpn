package com.atomvpn

import android.app.Activity
import android.text.TextUtils
import com.atom.core.exceptions.AtomAPIException
import com.atom.core.exceptions.AtomException
import com.atom.core.exceptions.AtomValidationException
import com.atom.core.models.AtomConfiguration
import com.atom.core.models.AtomNotification
import com.atom.core.models.City
import com.atom.core.models.Country
import com.atom.core.models.Protocol
import com.atom.sdk.android.AtomManager
import com.atom.sdk.android.ConnectionDetails
import com.atom.sdk.android.VPNCredentials
import com.atom.sdk.android.VPNProperties
import com.atom.sdk.android.VPNStateListener
import com.atom.sdk.android.VPNState
import com.atom.sdk.android.data.callbacks.CollectionCallback
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class AtomVpnModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext), VPNStateListener {

    private val gson = Gson()
    private var atomManager: AtomManager? = null
    private var isInitialized = false

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
            val atomConfigurationBuilder = AtomConfiguration.Builder(secretKey)
            
            if (!TextUtils.isEmpty(vpnInterfaceName)) {
                atomConfigurationBuilder.setVpnInterfaceName(vpnInterfaceName)
            } else {
                atomConfigurationBuilder.setVpnInterfaceName("DGuard VPN")
            }
            
            // Configure notification
            val atomNotificationBuilder = AtomNotification.Builder(
                1001,
                "DGuard VPN",
                "You are now secured with DGuard VPN",
                android.R.drawable.ic_menu_info_details,
                android.graphics.Color.BLUE
            )
            atomConfigurationBuilder.setNotification(atomNotificationBuilder.build())
            atomConfigurationBuilder.enableVPNPause()
            
            val atomConfiguration = atomConfigurationBuilder.build()
            
            AtomManager.initialize(
                reactApplicationContext,
                atomConfiguration,
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
    fun connect(propertiesJson: String, promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
            return
        }

        try {
            val propertiesMap = gson.fromJson<Map<String, Any>>(
                propertiesJson, 
                object : TypeToken<Map<String, Any>>() {}.type
            )
            
            val vpnPropertiesBuilder = createVPNPropertiesBuilder(propertiesMap)
            val activity = currentActivity
            
            if (activity != null) {
                atomManager?.connect(activity, vpnPropertiesBuilder.build())
                promise.resolve(null)
            } else {
                promise.reject("NO_ACTIVITY", "No current activity available")
            }
        } catch (e: AtomValidationException) {
            promise.reject("CONNECTION_ERROR", e.message, e)
        } catch (e: Exception) {
            promise.reject("CONNECTION_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun disconnect(promise: Promise) {
        try {
            val activity = currentActivity
            if (activity != null) {
                atomManager?.disconnect(activity)
                promise.resolve(null)
            } else {
                promise.reject("NO_ACTIVITY", "No current activity available")
            }
        } catch (e: Exception) {
            promise.reject("DISCONNECT_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun cancel(promise: Promise) {
        try {
            val activity = currentActivity
            if (activity != null) {
                atomManager?.cancel(activity)
                promise.resolve(null)
            } else {
                promise.reject("NO_ACTIVITY", "No current activity available")
            }
        } catch (e: Exception) {
            promise.reject("CANCEL_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getCurrentStatus(promise: Promise) {
        try {
            val activity = currentActivity
            if (activity != null) {
                val status = atomManager?.getCurrentVpnStatus(activity) ?: "DISCONNECTED"
                promise.resolve(status)
            } else {
                promise.reject("NO_ACTIVITY", "No current activity available")
            }
        } catch (e: Exception) {
            promise.reject("STATUS_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun isVPNServicePrepared(promise: Promise) {
        try {
            val activity = currentActivity
            if (activity != null) {
                val isPrepared = atomManager?.isVPNServicePrepared(activity) ?: false
                promise.resolve(isPrepared)
            } else {
                promise.reject("NO_ACTIVITY", "No current activity available")
            }
        } catch (e: Exception) {
            promise.reject("SERVICE_CHECK_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getCountries(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
            return
        }

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
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
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
    fun getCountriesForSmartDialing(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
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
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
            return
        }

        atomManager?.getRecommendedLocation(object : CollectionCallback<Country> {
            override fun onSuccess(countries: List<Country>?) {
                if (countries != null && countries.isNotEmpty()) {
                    val json = gson.toJson(countries[0])
                    promise.resolve(json)
                } else {
                    promise.resolve(null)
                }
            }

            override fun onError(exception: AtomException) {
                promise.reject("RECOMMENDED_LOCATION_ERROR", exception.message, exception)
            }

            override fun onNetworkError(exception: AtomException) {
                promise.reject("RECOMMENDED_LOCATION_NETWORK_ERROR", exception.message, exception)
            }
        })
    }

    @ReactMethod
    fun getChannels(promise: Promise) {
        // Note: Channels might not be directly available in all SDK versions
        // This is a placeholder implementation
        promise.resolve("[]")
    }

    @ReactMethod
    fun getCities(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
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
    fun pause(timerString: String, promise: Promise) {
        try {
            val timer = when (timerString) {
                "MINUTES_5" -> AtomManager.PauseTimer.MINUTES_5
                "MINUTES_10" -> AtomManager.PauseTimer.MINUTES_10
                "MINUTES_15" -> AtomManager.PauseTimer.MINUTES_15
                "MINUTES_30" -> AtomManager.PauseTimer.MINUTES_30
                "MINUTES_60" -> AtomManager.PauseTimer.MINUTES_60
                "MANUAL" -> AtomManager.PauseTimer.MANUAL
                else -> AtomManager.PauseTimer.MINUTES_5
            }
            
            atomManager?.pause(timer)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("PAUSE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun resume(promise: Promise) {
        try {
            atomManager?.resume()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("RESUME_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setVPNCredentials(username: String, password: String, promise: Promise) {
        try {
            val credentials = VPNCredentials(username, password)
            atomManager?.setVPNCredentials(credentials)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("CREDENTIALS_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setUUID(uuid: String, promise: Promise) {
        try {
            atomManager?.setUUID(uuid)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("UUID_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getConnectedIP(promise: Promise) {
        try {
            val connectedIP = atomManager?.connectedIP
            promise.resolve(connectedIP)
        } catch (e: Exception) {
            promise.reject("IP_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getAtomShieldData(promise: Promise) {
        // AtomShield data might not be directly available in all SDK versions
        // This is a placeholder implementation
        val data = mapOf(
            "counter" to 0,
            "trackerCount" to 0,
            "adCount" to 0
        )
        promise.resolve(gson.toJson(data))
    }

    @ReactMethod
    fun quickConnect(promise: Promise) {
        if (!isInitialized || atomManager == null) {
            promise.reject("NOT_INITIALIZED", "DGuard VPN SDK not initialized")
            return
        }

        try {
            // Get recommended location first
            atomManager?.getRecommendedLocation(object : CollectionCallback<Country> {
                override fun onSuccess(countries: List<Country>?) {
                    if (countries != null && countries.isNotEmpty()) {
                        // Use recommended country
                        val recommendedCountry = countries[0]
                        
                        // Get protocols to find best one
                        atomManager?.getProtocols(object : CollectionCallback<Protocol> {
                            override fun onSuccess(protocols: List<Protocol>?) {
                                if (protocols != null && protocols.isNotEmpty()) {
                                    // Prefer IKEv2, then OpenVPN UDP, then first available
                                    val bestProtocol = protocols.find { it.protocol == "ikev2" }
                                        ?: protocols.find { it.protocol == "openvpn_udp" }
                                        ?: protocols[0]
                                    
                                    // Build connection properties
                                    val vpnPropertiesBuilder = VPNProperties.Builder(recommendedCountry, bestProtocol)
                                        .withOptimization()
                                        .withSmartDialing()
                                        .withAutomaticPort()
                                    
                                    val activity = currentActivity
                                    if (activity != null) {
                                        atomManager?.connect(activity, vpnPropertiesBuilder.build())
                                        promise.resolve(null)
                                    } else {
                                        promise.reject("NO_ACTIVITY", "No current activity available")
                                    }
                                } else {
                                    promise.reject("NO_PROTOCOLS", "No protocols available")
                                }
                            }
                            
                            override fun onError(exception: AtomException) {
                                promise.reject("PROTOCOLS_ERROR", exception.message, exception)
                            }
                            
                            override fun onNetworkError(exception: AtomException) {
                                promise.reject("PROTOCOLS_NETWORK_ERROR", exception.message, exception)
                            }
                        })
                    } else {
                        // Fallback to first available country
                        atomManager?.getCountries(object : CollectionCallback<Country> {
                            override fun onSuccess(allCountries: List<Country>?) {
                                if (allCountries != null && allCountries.isNotEmpty()) {
                                    val fallbackCountry = allCountries[0]
                                    
                                    atomManager?.getProtocols(object : CollectionCallback<Protocol> {
                                        override fun onSuccess(protocols: List<Protocol>?) {
                                            if (protocols != null && protocols.isNotEmpty()) {
                                                val bestProtocol = protocols.find { it.protocol == "ikev2" }
                                                    ?: protocols.find { it.protocol == "openvpn_udp" }
                                                    ?: protocols[0]
                                                
                                                val vpnPropertiesBuilder = VPNProperties.Builder(fallbackCountry, bestProtocol)
                                                    .withOptimization()
                                                    .withSmartDialing()
                                                    .withAutomaticPort()
                                                
                                                val activity = currentActivity
                                                if (activity != null) {
                                                    atomManager?.connect(activity, vpnPropertiesBuilder.build())
                                                    promise.resolve(null)
                                                } else {
                                                    promise.reject("NO_ACTIVITY", "No current activity available")
                                                }
                                            } else {
                                                promise.reject("NO_PROTOCOLS", "No protocols available")
                                            }
                                        }
                                        
                                        override fun onError(exception: AtomException) {
                                            promise.reject("PROTOCOLS_ERROR", exception.message, exception)
                                        }
                                        
                                        override fun onNetworkError(exception: AtomException) {
                                            promise.reject("PROTOCOLS_NETWORK_ERROR", exception.message, exception)
                                        }
                                    })
                                } else {
                                    promise.reject("NO_COUNTRIES", "No countries available")
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
                }
                
                override fun onError(exception: AtomException) {
                    // Fallback to regular countries if recommended location fails
                    atomManager?.getCountries(object : CollectionCallback<Country> {
                        override fun onSuccess(allCountries: List<Country>?) {
                            if (allCountries != null && allCountries.isNotEmpty()) {
                                val fallbackCountry = allCountries[0]
                                
                                atomManager?.getProtocols(object : CollectionCallback<Protocol> {
                                    override fun onSuccess(protocols: List<Protocol>?) {
                                        if (protocols != null && protocols.isNotEmpty()) {
                                            val bestProtocol = protocols.find { it.protocol == "ikev2" }
                                                ?: protocols.find { it.protocol == "openvpn_udp" }
                                                ?: protocols[0]
                                            
                                            val vpnPropertiesBuilder = VPNProperties.Builder(fallbackCountry, bestProtocol)
                                                .withOptimization()
                                                .withSmartDialing()
                                                .withAutomaticPort()
                                            
                                            val activity = currentActivity
                                            if (activity != null) {
                                                atomManager?.connect(activity, vpnPropertiesBuilder.build())
                                                promise.resolve(null)
                                            } else {
                                                promise.reject("NO_ACTIVITY", "No current activity available")
                                            }
                                        } else {
                                            promise.reject("NO_PROTOCOLS", "No protocols available")
                                        }
                                    }
                                    
                                    override fun onError(exception: AtomException) {
                                        promise.reject("PROTOCOLS_ERROR", exception.message, exception)
                                    }
                                    
                                    override fun onNetworkError(exception: AtomException) {
                                        promise.reject("PROTOCOLS_NETWORK_ERROR", exception.message, exception)
                                    }
                                })
                            } else {
                                promise.reject("NO_COUNTRIES", "No countries available")
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
                
                override fun onNetworkError(exception: AtomException) {
                    promise.reject("RECOMMENDED_LOCATION_NETWORK_ERROR", exception.message, exception)
                }
            })
        } catch (e: Exception) {
            promise.reject("QUICK_CONNECT_ERROR", e.message, e)
        }
    }

    private fun createVPNPropertiesBuilder(properties: Map<String, Any>): VPNProperties.Builder {
        var builder: VPNProperties.Builder? = null

        // Handle dedicated IP connection
        val dedicatedIP = properties["dedicatedIP"] as? String
        if (!dedicatedIP.isNullOrEmpty()) {
            builder = VPNProperties.Builder(dedicatedIP)
        } else {
            // Handle country/city and protocol
            val countryCode = properties["country"] as? String
            val cityName = properties["city"] as? String
            val protocolName = properties["protocol"] as? String

            if (!protocolName.isNullOrEmpty()) {
                val protocol = Protocol().apply { 
                    protocol = protocolName 
                    name = protocolName
                }

                if (!cityName.isNullOrEmpty()) {
                    val city = City().apply { 
                        name = cityName
                        country = countryCode ?: ""
                    }
                    builder = VPNProperties.Builder(city, protocol)
                } else if (!countryCode.isNullOrEmpty()) {
                    val country = Country().apply { 
                        country = countryCode
                        name = countryCode
                    }
                    builder = VPNProperties.Builder(country, protocol)
                }
            }
        }

        if (builder == null) {
            throw IllegalArgumentException("Invalid connection properties")
        }

        // Configure additional properties
        val enableOptimization = properties["enableOptimization"] as? Boolean ?: false
        if (enableOptimization) {
            builder.withOptimization()
        }

        val enableSmartDialing = properties["enableSmartDialing"] as? Boolean ?: false
        if (enableSmartDialing) {
            builder.withSmartDialing()
        }

        val secondaryProtocol = properties["secondaryProtocol"] as? String
        if (!secondaryProtocol.isNullOrEmpty()) {
            val protocol = Protocol().apply { 
                protocol = secondaryProtocol
                name = secondaryProtocol
            }
            builder.withSecondaryProtocol(protocol)
        }

        val tertiaryProtocol = properties["tertiaryProtocol"] as? String
        if (!tertiaryProtocol.isNullOrEmpty()) {
            val protocol = Protocol().apply { 
                protocol = tertiaryProtocol
                name = tertiaryProtocol
            }
            builder.withTertiaryProtocol(protocol)
        }

        val useAutomaticPort = properties["useAutomaticPort"] as? Boolean ?: true
        if (useAutomaticPort) {
            builder.withAutomaticPort()
        }

        val manualPort = properties["manualPort"] as? Double
        if (manualPort != null) {
            builder.withManualPort(manualPort.toInt())
        }

        return builder
    }

    private fun sendEvent(eventName: String, params: WritableMap?) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    // VPNStateListener implementation
    override fun onConnected() {
        sendEvent("AtomVPN:onConnected", null)
    }

    override fun onConnected(connectionDetails: ConnectionDetails) {
        val params = Arguments.createMap().apply {
            putBoolean("isConnected", true)
            putString("serverIP", connectionDetails.serverIP)
        }
        sendEvent("AtomVPN:onConnectedWithDetails", params)
    }

    override fun onConnecting() {
        sendEvent("AtomVPN:onConnecting", null)
    }

    override fun onConnecting(vpnProperties: VPNProperties, atomConfiguration: AtomConfiguration) {
        sendEvent("AtomVPN:onConnecting", null)
    }

    override fun onRedialing(atomException: AtomException, connectionDetails: ConnectionDetails) {
        val params = Arguments.createMap().apply {
            putString("message", atomException.message)
            putInt("code", atomException.code)
        }
        sendEvent("AtomVPN:onRedialing", params)
    }

    override fun onDialError(atomException: AtomException, connectionDetails: ConnectionDetails) {
        val params = Arguments.createMap().apply {
            putString("message", atomException.message)
            putInt("code", atomException.code)
            if (atomException.exception is AtomAPIException) {
                val apiException = atomException.exception as AtomAPIException
                putString("details", apiException.errorMessage)
            }
        }
        sendEvent("AtomVPN:onError", params)
    }

    override fun onStateChange(state: String) {
        val params = Arguments.createMap().apply {
            putString("state", state)
        }
        sendEvent("AtomVPN:onStateChange", params)
    }

    override fun onPaused(atomException: AtomException?, connectionDetails: ConnectionDetails) {
        val params = Arguments.createMap().apply {
            if (atomException != null) {
                putString("error", atomException.message)
                putInt("errorCode", atomException.code)
            }
        }
        sendEvent("AtomVPN:onPaused", params)
    }

    override fun onDisconnecting(connectionDetails: ConnectionDetails) {
        sendEvent("AtomVPN:onDisconnecting", null)
    }

    override fun onDisconnected(isCancelled: Boolean) {
        val params = Arguments.createMap().apply {
            putBoolean("cancelled", isCancelled)
        }
        sendEvent("AtomVPN:onDisconnected", params)
    }

    override fun onDisconnected(connectionDetails: ConnectionDetails) {
        val params = Arguments.createMap().apply {
            putBoolean("cancelled", connectionDetails.isCancelled)
            putBoolean("isConnected", false)
        }
        sendEvent("AtomVPN:onDisconnectedWithDetails", params)
    }

    override fun onUnableToAccessInternet(atomException: AtomException, connectionDetails: ConnectionDetails) {
        val params = Arguments.createMap().apply {
            putString("message", atomException.message)
            putInt("code", atomException.code)
        }
        sendEvent("AtomVPN:onUnableToAccessInternet", params)
    }

    override fun onPacketsTransmitted(inBytes: String, outBytes: String, inSpeed: String, outSpeed: String) {
        val params = Arguments.createMap().apply {
            putString("inBytes", inBytes)
            putString("outBytes", outBytes)
            putString("inSpeed", inSpeed)
            putString("outSpeed", outSpeed)
        }
        sendEvent("AtomVPN:onPacketsTransmitted", params)
    }

    override fun onInvalidateVPNOnNetworkChange() {
        sendEvent("AtomVPN:onInvalidateVPNOnNetworkChange", null)
    }
}