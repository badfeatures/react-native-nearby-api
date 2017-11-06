require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-nearby-api"
  s.version      = package['version']
  s.summary      = "React Native Nearby API wrapper around NearbyMessages API"

  s.authors      = { "Bad Features, Inc" => "info@badfeatures.com" }
  s.homepage     = "https://github.com/badfeatures/react-native-nearby-api"
  s.license      = "MIT"
  s.platform     = :ios, "9.0"

  s.source       = { :git => "https://github.com/badfeatures/react-native-nearby-api", :tag => "#{s.version}" }
  s.source_files  = "ios/Classes/**/*.{h,m}"

  s.dependency 'React'
  s.dependency 'NearbyMessages', '1.1.0'
end