/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from "react";
import {
  Platform,
  StyleSheet,
  Text,
  View,
  TouchableOpacity
} from "react-native";
import { NearbyAPI } from "react-native-nearby-api";

const instructions = Platform.select({
  ios: "Press Cmd+R to reload,\n" + "Cmd+D or shake for dev menu",
  android:
    "Double tap R on your keyboard to reload,\n" +
    "Shake or press menu button for dev menu"
});

const nearbyAPI = new NearbyAPI();

export default class App extends Component {
  constructor() {
    super();
    this.state = {
      isConnected: false,
      nearbyMessage: null
    };
  }

  componentDidMount() {
    console.log("Mounting ", NearbyAPI);
    nearbyAPI.onConnected(message => {
      console.log(message);
      this.setState({
        isConnected: true,
        nearbyMessage: `Connected - ${message}`
      });
    });
    nearbyAPI.onDisconnected(message => {
      console.log(message);
      this.setState({
        isConnected: false,
        nearbyMessage: `Disconnected - ${message}`
      });
    });
    nearbyAPI.onFound(message => {
      console.log("Message Found!");
      console.log(message);
      this.setState({ nearbyMessage: `Message Found - ${message}` });
    });
    nearbyAPI.onLost(message => {
      console.log("Message Lost!");
      console.log(message);
      this.setState({ nearbyMessage: `Message Lost - ${message}` });
    });
    nearbyAPI.onDistanceChanged((message, value) => {
      console.log("Distance Changed!");
      console.log(message, value);
      this.setState({
        nearbyMessage: `Distance Changed - ${message} - ${value}`
      });
    });
    nearbyAPI.onPublishSuccess(message => {
      console.log(message);
      this.setState({ nearbyMessage: `Publish Success - ${message}` });
    });
    nearbyAPI.onPublishFailed(message => {
      console.log(message);
      this.setState({ nearbyMessage: `Publish Failed - ${message}` });
    });
    nearbyAPI.onSubscribeSuccess(() => {
      this.setState({ nearbyMessage: `Subscribe Success` });
    });
    nearbyAPI.onSubscribeFailed(() => {
      this.setState({ nearbyMessage: `Subscribe Failed` });
    });
  }

  _connectPress = () => {
    if (this.state.isConnected) {
      nearbyAPI.disconnect();
    } else {
      nearbyAPI.connect();
    }
  };

  _publishPress = () => {
    nearbyAPI.publish(`Hello World! - ${Math.random()}`);
  };

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to react-native-nearby-api test app!
        </Text>
        <Text style={styles.instructions}>
          Is Connected: {`${this.state.isConnected}`}
        </Text>
        <Text style={styles.instructions}>{this.state.nearbyMessage}</Text>
        <TouchableOpacity onPress={this._connectPress}>
          <Text style={styles.connectButton}>
            {this.state.isConnected ? "DISCONNECT" : "CONNECT"}
          </Text>
        </TouchableOpacity>
        <View style={{ height: 32 }} />
        <TouchableOpacity onPress={this._publishPress}>
          <Text style={styles.connectButton}>PUBLISH</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  connectButton: {
    fontSize: 30,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  }
});
