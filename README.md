
# react-native-nearby-api

## Getting started

`$ yarn react-native-nearby-api` or `$ npm install react-native-nearby-api --save`

### Mostly automatic installation

`$ react-native link react-native-nearby-api`

### Manual installation

#### iOS
- Coming Soon!

#### Android
1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.badfeatures.nearby.RNNearbyApiPackage;` to the imports at the top of the file
  - Add `new RNNearbyApiPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-nearby-api'
  	project(':react-native-nearby-api').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-nearby-api/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-nearby-api')
  	```


## Usage
See the [example app](https://github.com/badfeatures/react-native-nearby-api/tree/master/example) for more detail and code examples.

```javascript
import NearbyApi from 'react-native-nearby-api';

const nearbyAPI = new NearbyAPI()
nearbyAPI.onConnected(message => {
	console.log(message);
});
nearbyAPI.onDisconnected(message => {
	console.log(message);
});
nearbyAPI.onFound(message => {
	console.log("Message Found!");
	console.log(message);
});
nearbyAPI.onLost(message => {
	console.log("Message Lost!");
	console.log(message);
});
nearbyAPI.onDistanceChanged((message, value) => {
	console.log("Distance Changed!");
	console.log(message, value);
});
nearbyAPI.onPublishSuccess(message => {
	console.log(message);
});
nearbyAPI.onPublishFailed(message => {
	console.log(message);
});
nearbyAPI.onSubscribeSuccess(() => {
});
nearbyAPI.onSubscribeFailed(() => {
});
	
// To connect from Google API Client
nearbyAPI.connect()

// To disconnect later
nearbyAPI.disconnect()

// To publish to nearby devices
nearbyAPI.publish('Hello World!')

// To subscribe to nearby devices broadcasting
nearbyAPI.subscribe();
```
  