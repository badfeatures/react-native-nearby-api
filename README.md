
# react-native-nearby-api

## Getting started

`$ npm install react-native-nearby-api --save`

### Mostly automatic installation

`$ react-native link react-native-nearby-api`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-nearby-api` and add `RNNearbyApi.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNNearbyApi.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNNearbyApiPackage;` to the imports at the top of the file
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
```javascript
import RNNearbyApi from 'react-native-nearby-api';

// TODO: What to do with the module?
RNNearbyApi;
```
  