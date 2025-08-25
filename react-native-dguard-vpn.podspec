require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'

Pod::Spec.new do |s|
  s.name         = "react-native-dguard-vpn"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  DGuard VPN - React Native library for secure VPN connection with ATOM SDK integration.
                  Provides complete TypeScript interface for VPN functionality including connection management,
                  server selection, protocol switching, and real-time status monitoring with Expo Development Builds support.
                   DESC
  s.homepage     = "https://github.com/username/react-native-dguard-vpn"
  s.license      = "MIT"
  s.authors      = { "DGuard Team" => "contact@dguard.com" }
  s.platforms    = { :ios => "12.0" }
  s.source       = { :git => "https://github.com/username/react-native-dguard-vpn.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,cc,cpp,m,mm,swift}"
  s.requires_arc = true

  # iOS deployment target
  s.ios.deployment_target = "12.0"

  # Dependencies
  s.dependency "React-Core"
  
  # ATOM SDK Dependencies
  s.dependency "AtomSDKBySecure"

  # React Native compatibility
  if respond_to?(:install_modules_dependencies, true)
    install_modules_dependencies(s)
  else
    s.dependency "React-Core"

    # Don't install the dependencies when we run `pod install` in the old architecture.
    if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
      s.compiler_flags = folly_compiler_flags + " -DRCT_NEW_ARCH_ENABLED=1"
      s.pod_target_xcconfig    = {
        "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\"",
        "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
        "CLANG_CXX_LANGUAGE_STANDARD" => "c++17"
      }
      s.dependency "React-Codegen"
      s.dependency "RCT-Folly"
      s.dependency "RCTRequired"
      s.dependency "RCTTypeSafety"
      s.dependency "ReactCommon/turbomodule/core"
    end
  end

  # Framework search paths for ATOM SDK
  s.frameworks = 'Foundation', 'UIKit', 'NetworkExtension', 'SystemConfiguration'
  
  # Required for VPN functionality
  s.weak_frameworks = 'Network'
  
  # Compiler flags
  s.compiler_flags = '-DDGUARD_VPN_SDK=1'
  
  # Pod configuration
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }
  
  # Swift version for mixed projects
  s.swift_version = "5.0"
end