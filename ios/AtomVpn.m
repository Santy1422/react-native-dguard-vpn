//
//  AtomVpn.m
//  react-native-dguard-vpn
//  DGuard VPN - Secure VPN Solution
//

#import "AtomVpn.h"
#import <AtomSDK/AtomSDK.h>
#import <AtomCore/AtomCore.h>

@interface AtomVpn () <AtomManagerDelegate>

@property (nonatomic, strong) AtomManager *atomManager;
@property (nonatomic, assign) BOOL isInitialized;

@end

@implementation AtomVpn

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        @"AtomVPN:onStateChange",
        @"AtomVPN:onConnected",
        @"AtomVPN:onConnectedWithDetails",
        @"AtomVPN:onDisconnected",
        @"AtomVPN:onDisconnectedWithDetails",
        @"AtomVPN:onError",
        @"AtomVPN:onPaused",
        @"AtomVPN:onPacketsTransmitted",
        @"AtomVPN:onConnecting",
        @"AtomVPN:onDisconnecting",
        @"AtomVPN:onRedialing"
    ];
}

#pragma mark - Initialization

RCT_EXPORT_METHOD(initialize:(NSString *)secretKey
                  vpnInterfaceName:(NSString * _Nullable)vpnInterfaceName
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (self.isInitialized) {
        resolve(@YES);
        return;
    }
    
    @try {
        AtomConfiguration *atomConfiguration = [[AtomConfiguration alloc] init];
        atomConfiguration.secretKey = secretKey;
        
        if (vpnInterfaceName && vpnInterfaceName.length > 0) {
            atomConfiguration.vpnInterfaceName = vpnInterfaceName;
        } else {
            atomConfiguration.vpnInterfaceName = @"DGuard VPN";
        }
        
        // Configure bundle identifiers for Network Extensions
        atomConfiguration.tunnelProviderBundleIdentifier = [NSString stringWithFormat:@"%@.packettunnelopenvpn", [[NSBundle mainBundle] bundleIdentifier]];
        atomConfiguration.wireGuardTunnelProviderBundleIdentifier = [NSString stringWithFormat:@"%@.packettunnelwireguard", [[NSBundle mainBundle] bundleIdentifier]];
        atomConfiguration.appGroupIdentifier = [NSString stringWithFormat:@"group.%@", [[NSBundle mainBundle] bundleIdentifier]];
        
        self.atomManager = [AtomManager sharedInstanceWithAtomConfiguration:atomConfiguration];
        self.atomManager.delegate = self;
        self.isInitialized = YES;
        
        // Install VPN profile
        [self.atomManager installVPNProfileWithCompletion:^(NSString *success) {
            NSLog(@"VPN profile installed successfully");
            resolve(@YES);
        } errorBlock:^(NSError *error) {
            NSLog(@"VPN profile installation error: %@", error.localizedDescription);
            // Still resolve as successful since the SDK is initialized
            resolve(@YES);
        }];
        
    } @catch (NSException *exception) {
        reject(@"INITIALIZATION_ERROR", exception.reason, nil);
    }
}

#pragma mark - Connection Management

RCT_EXPORT_METHOD(connect:(NSString *)propertiesJson
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!self.isInitialized || !self.atomManager) {
        reject(@"NOT_INITIALIZED", @"ATOM SDK not initialized", nil);
        return;
    }
    
    @try {
        NSError *error;
        NSDictionary *propertiesDict = [NSJSONSerialization JSONObjectWithData:[propertiesJson dataUsingEncoding:NSUTF8StringEncoding] options:0 error:&error];
        
        if (error) {
            reject(@"JSON_PARSE_ERROR", @"Failed to parse properties JSON", error);
            return;
        }
        
        AtomProperties *properties = [self createAtomPropertiesFromDict:propertiesDict];
        
        if (!properties) {
            reject(@"INVALID_PROPERTIES", @"Invalid connection properties", nil);
            return;
        }
        
        [self.atomManager connectWithProperties:properties completion:^(NSString *success) {
            resolve(nil);
        } errorBlock:^(NSError *error) {
            reject(@"CONNECTION_ERROR", error.localizedDescription, error);
        }];
        
    } @catch (NSException *exception) {
        reject(@"CONNECTION_ERROR", exception.reason, nil);
    }
}

RCT_EXPORT_METHOD(disconnect:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        [self.atomManager disconnectVPN];
        resolve(nil);
    } @catch (NSException *exception) {
        reject(@"DISCONNECT_ERROR", exception.reason, nil);
    }
}

RCT_EXPORT_METHOD(cancel:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        [self.atomManager cancelVPN];
        resolve(nil);
    } @catch (NSException *exception) {
        reject(@"CANCEL_ERROR", exception.reason, nil);
    }
}

#pragma mark - Status and Information

RCT_EXPORT_METHOD(getCurrentStatus:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        AtomVPNStatus status = [self.atomManager getCurrentVPNStatus];
        NSString *statusString = [self stringFromVPNStatus:status];
        resolve(statusString);
    } @catch (NSException *exception) {
        reject(@"STATUS_ERROR", exception.reason, nil);
    }
}

RCT_EXPORT_METHOD(isVPNServicePrepared:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        // In iOS, VPN service is generally prepared after profile installation
        BOOL isPrepared = self.isInitialized;
        resolve(@(isPrepared));
    } @catch (NSException *exception) {
        reject(@"SERVICE_CHECK_ERROR", exception.reason, nil);
    }
}

#pragma mark - Data Retrieval

RCT_EXPORT_METHOD(getCountries:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!self.isInitialized || !self.atomManager) {
        reject(@"NOT_INITIALIZED", @"ATOM SDK not initialized", nil);
        return;
    }
    
    [self.atomManager getCountriesWithSuccess:^(NSArray<AtomCountry *> *countries) {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[self countriesToDictArray:countries] options:0 error:&error];
        if (error) {
            reject(@"JSON_SERIALIZE_ERROR", @"Failed to serialize countries", error);
        } else {
            NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            resolve(jsonString);
        }
    } errorBlock:^(NSError *error) {
        reject(@"COUNTRIES_ERROR", error.localizedDescription, error);
    }];
}

RCT_EXPORT_METHOD(getProtocols:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!self.isInitialized || !self.atomManager) {
        reject(@"NOT_INITIALIZED", @"ATOM SDK not initialized", nil);
        return;
    }
    
    [self.atomManager getProtocolsWithSuccess:^(NSArray<AtomProtocol *> *protocols) {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[self protocolsToDictArray:protocols] options:0 error:&error];
        if (error) {
            reject(@"JSON_SERIALIZE_ERROR", @"Failed to serialize protocols", error);
        } else {
            NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            resolve(jsonString);
        }
    } errorBlock:^(NSError *error) {
        reject(@"PROTOCOLS_ERROR", error.localizedDescription, error);
    }];
}

RCT_EXPORT_METHOD(getCountriesForSmartDialing:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!self.isInitialized || !self.atomManager) {
        reject(@"NOT_INITIALIZED", @"ATOM SDK not initialized", nil);
        return;
    }
    
    [self.atomManager getCountriesForSmartDialing:^(NSArray<AtomCountry *> *countries) {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[self countriesToDictArray:countries] options:0 error:&error];
        if (error) {
            reject(@"JSON_SERIALIZE_ERROR", @"Failed to serialize smart countries", error);
        } else {
            NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            resolve(jsonString);
        }
    } errorBlock:^(NSError *error) {
        reject(@"SMART_COUNTRIES_ERROR", error.localizedDescription, error);
    }];
}

RCT_EXPORT_METHOD(getRecommendedLocation:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!self.isInitialized || !self.atomManager) {
        reject(@"NOT_INITIALIZED", @"ATOM SDK not initialized", nil);
        return;
    }
    
    [self.atomManager getOptimizedCountriesWithSuccess:^(NSArray<AtomCountry *> *countries) {
        if (countries && countries.count > 0) {
            AtomCountry *recommended = countries[0];
            NSDictionary *countryDict = [self countryToDict:recommended];
            NSError *error;
            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:countryDict options:0 error:&error];
            if (error) {
                reject(@"JSON_SERIALIZE_ERROR", @"Failed to serialize recommended location", error);
            } else {
                NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                resolve(jsonString);
            }
        } else {
            resolve(nil);
        }
    } errorBlock:^(NSError *error) {
        reject(@"RECOMMENDED_LOCATION_ERROR", error.localizedDescription, error);
    }];
}

RCT_EXPORT_METHOD(getChannels:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    // Channels might not be directly available in iOS SDK
    resolve(@"[]");
}

RCT_EXPORT_METHOD(getCities:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!self.isInitialized || !self.atomManager) {
        reject(@"NOT_INITIALIZED", @"ATOM SDK not initialized", nil);
        return;
    }
    
    [self.atomManager getCitiesWithSuccess:^(NSArray<AtomCity *> *cities) {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[self citiesToDictArray:cities] options:0 error:&error];
        if (error) {
            reject(@"JSON_SERIALIZE_ERROR", @"Failed to serialize cities", error);
        } else {
            NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            resolve(jsonString);
        }
    } errorBlock:^(NSError *error) {
        reject(@"CITIES_ERROR", error.localizedDescription, error);
    }];
}

#pragma mark - Pause/Resume

RCT_EXPORT_METHOD(pause:(NSString *)timerString
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        // iOS SDK might not have pause functionality
        // This is a placeholder implementation
        resolve(nil);
    } @catch (NSException *exception) {
        reject(@"PAUSE_ERROR", exception.reason, nil);
    }
}

RCT_EXPORT_METHOD(resume:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        // iOS SDK might not have resume functionality
        // This is a placeholder implementation
        resolve(nil);
    } @catch (NSException *exception) {
        reject(@"RESUME_ERROR", exception.reason, nil);
    }
}

#pragma mark - Credentials

RCT_EXPORT_METHOD(setVPNCredentials:(NSString *)username
                  password:(NSString *)password
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        AtomCredential *credential = [[AtomCredential alloc] initWithUsername:username password:password];
        [self.atomManager setAtomCredential:credential];
        resolve(nil);
    } @catch (NSException *exception) {
        reject(@"CREDENTIALS_ERROR", exception.reason, nil);
    }
}

RCT_EXPORT_METHOD(setUUID:(NSString *)uuid
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        // UUID setting might be handled differently in iOS
        resolve(nil);
    } @catch (NSException *exception) {
        reject(@"UUID_ERROR", exception.reason, nil);
    }
}

#pragma mark - Utility

RCT_EXPORT_METHOD(getConnectedIP:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSString *connectedIP = [self.atomManager getConnectedIP];
        resolve(connectedIP);
    } @catch (NSException *exception) {
        reject(@"IP_ERROR", exception.reason, nil);
    }
}

RCT_EXPORT_METHOD(getAtomShieldData:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    // AtomShield data might not be directly available in iOS SDK
    NSDictionary *data = @{
        @"counter": @0,
        @"trackerCount": @0,
        @"adCount": @0
    };
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:&error];
    if (error) {
        reject(@"JSON_SERIALIZE_ERROR", @"Failed to serialize shield data", error);
    } else {
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        resolve(jsonString);
    }
}

#pragma mark - Helper Methods

- (AtomProperties *)createAtomPropertiesFromDict:(NSDictionary *)dict
{
    AtomProperties *properties = nil;
    
    // Handle dedicated IP
    NSString *dedicatedIP = dict[@"dedicatedIP"];
    if (dedicatedIP && dedicatedIP.length > 0) {
        properties = [[AtomProperties alloc] initWithDedicatedHostName:dedicatedIP];
    } else {
        // Handle country/city and protocol
        NSString *countryCode = dict[@"country"];
        NSString *cityName = dict[@"city"];
        NSString *protocolName = dict[@"protocol"];
        
        if (protocolName && protocolName.length > 0) {
            AtomProtocol *protocol = [[AtomProtocol alloc] init];
            protocol.protocol = protocolName;
            protocol.name = protocolName;
            
            if (cityName && cityName.length > 0) {
                AtomCity *city = [[AtomCity alloc] init];
                city.name = cityName;
                city.country = countryCode ?: @"";
                properties = [[AtomProperties alloc] initWithCity:city protocol:protocol];
            } else if (countryCode && countryCode.length > 0) {
                AtomCountry *country = [[AtomCountry alloc] init];
                country.country = countryCode;
                country.name = countryCode;
                properties = [[AtomProperties alloc] initWithCountry:country protocol:protocol];
            }
        }
    }
    
    if (!properties) {
        return nil;
    }
    
    // Configure additional properties
    NSNumber *enableOptimization = dict[@"enableOptimization"];
    if (enableOptimization && enableOptimization.boolValue) {
        properties.useOptimization = YES;
    }
    
    NSNumber *enableSmartDialing = dict[@"enableSmartDialing"];
    if (enableSmartDialing && enableSmartDialing.boolValue) {
        properties.useSmartDialing = YES;
    }
    
    NSString *secondaryProtocol = dict[@"secondaryProtocol"];
    if (secondaryProtocol && secondaryProtocol.length > 0) {
        AtomProtocol *protocol = [[AtomProtocol alloc] init];
        protocol.protocol = secondaryProtocol;
        protocol.name = secondaryProtocol;
        properties.secondaryProtocol = protocol;
    }
    
    NSString *tertiaryProtocol = dict[@"tertiaryProtocol"];
    if (tertiaryProtocol && tertiaryProtocol.length > 0) {
        AtomProtocol *protocol = [[AtomProtocol alloc] init];
        protocol.protocol = tertiaryProtocol;
        protocol.name = tertiaryProtocol;
        properties.tertiaryProtocol = protocol;
    }
    
    return properties;
}

- (NSString *)stringFromVPNStatus:(AtomVPNStatus)status
{
    switch (status) {
        case AtomStatusInvalid:
            return @"INVALID";
        case AtomStatusConnected:
            return @"CONNECTED";
        case AtomStatusConnecting:
            return @"CONNECTING";
        case AtomStatusValidating:
            return @"VALIDATING";
        case AtomStatusReasserting:
            return @"REASSERTING";
        case AtomStatusDisconnected:
            return @"DISCONNECTED";
        case AtomStatusDisconnecting:
            return @"DISCONNECTING";
        case AtomStatusAuthenticating:
            return @"AUTHENTICATING";
        case AtomStatusVerifyingHostName:
            return @"VERIFYING_HOSTNAME";
        case AtomStatusGettingFastestServer:
            return @"GETTING_FASTEST_SERVER";
        case AtomStatusOptimizingConnection:
            return @"OPTIMIZING_CONNECTION";
        case AtomStatusGeneratingCredentials:
            return @"GENERATING_CREDENTIALS";
        default:
            return @"DISCONNECTED";
    }
}

- (NSArray<NSDictionary *> *)countriesToDictArray:(NSArray<AtomCountry *> *)countries
{
    NSMutableArray *result = [NSMutableArray array];
    for (AtomCountry *country in countries) {
        [result addObject:[self countryToDict:country]];
    }
    return result;
}

- (NSDictionary *)countryToDict:(AtomCountry *)country
{
    NSMutableArray *protocols = [NSMutableArray array];
    for (AtomProtocol *protocol in country.protocols) {
        [protocols addObject:@{
            @"name": protocol.name ?: @"",
            @"protocol": protocol.protocol ?: @"",
            @"type": protocol.protocol ?: @""
        }];
    }
    
    return @{
        @"name": country.name ?: @"",
        @"country": country.country ?: @"",
        @"code": country.country ?: @"",
        @"countryId": @(country.countryId),
        @"latency": @(country.latency),
        @"protocols": protocols
    };
}

- (NSArray<NSDictionary *> *)protocolsToDictArray:(NSArray<AtomProtocol *> *)protocols
{
    NSMutableArray *result = [NSMutableArray array];
    for (AtomProtocol *protocol in protocols) {
        [result addObject:@{
            @"name": protocol.name ?: @"",
            @"protocol": protocol.protocol ?: @"",
            @"type": protocol.protocol ?: @""
        }];
    }
    return result;
}

- (NSArray<NSDictionary *> *)citiesToDictArray:(NSArray<AtomCity *> *)cities
{
    NSMutableArray *result = [NSMutableArray array];
    for (AtomCity *city in cities) {
        NSMutableArray *protocols = [NSMutableArray array];
        for (AtomProtocol *protocol in city.protocols) {
            [protocols addObject:@{
                @"name": protocol.name ?: @"",
                @"protocol": protocol.protocol ?: @"",
                @"type": protocol.protocol ?: @""
            }];
        }
        
        [result addObject:@{
            @"id": @(city.cityId),
            @"cityId": @(city.cityId),
            @"name": city.name ?: @"",
            @"country": city.country ?: @"",
            @"protocols": protocols
        }];
    }
    return result;
}

#pragma mark - AtomManagerDelegate

- (void)atomManagerDidConnect:(AtomConnectionDetails *)atomConnectionDetails
{
    NSDictionary *details = @{
        @"isConnected": @YES,
        @"serverIP": [self.atomManager getConnectedIP] ?: @""
    };
    [self sendEventWithName:@"AtomVPN:onConnectedWithDetails" body:details];
}

- (void)atomManagerDidDisconnect:(AtomConnectionDetails *)atomConnectionDetails
{
    NSDictionary *details = @{
        @"isConnected": @NO,
        @"cancelled": @(atomConnectionDetails.cancelled)
    };
    [self sendEventWithName:@"AtomVPN:onDisconnectedWithDetails" body:details];
}

- (void)atomManagerOnRedialing:(AtomConnectionDetails *)atomConnectionDetails withError:(NSError *)error
{
    NSDictionary *errorDict = @{
        @"message": error.localizedDescription ?: @"",
        @"code": @(error.code)
    };
    [self sendEventWithName:@"AtomVPN:onRedialing" body:errorDict];
}

- (void)atomManagerDialErrorReceived:(NSError *)error withConnectionDetails:(AtomConnectionDetails *)atomConnectionDetails
{
    NSDictionary *errorDict = @{
        @"message": error.localizedDescription ?: @"",
        @"code": @(error.code),
        @"details": error.userInfo[@"NSLocalizedFailureReason"] ?: @""
    };
    [self sendEventWithName:@"AtomVPN:onError" body:errorDict];
}

@end