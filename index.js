"use strict";
import {
  Platform,
  NativeModules,
  DeviceEventEmitter,
  NativeEventEmitter
} from "react-native";

const { RNNearbyApi } = NativeModules;

/**
 * Event Constants
 */
export const CONNECTED = "CONNECTED";
export const CONNECTION_SUSPENDED = "CONNECTION_SUSPENDED";
export const CONNECTION_FAILED = "CONNECTION_FAILED";
export const DISCONNECTED = "DISCONNECTED";
export const MESSAGE_FOUND = "MESSAGE_FOUND";
export const MESSAGE_LOST = "MESSAGE_LOST";
export const DISTANCE_CHANGED = "DISTANCE_CHANGED";
export const BLE_SIGNAL_CHANGED = "BLE_SIGNAL_CHANGED";
export const PUBLISH_SUCCESS = "PUBLISH_SUCCESS";
export const PUBLISH_FAILED = "PUBLISH_FAILED";
export const SUBSCRIBE_SUCCESS = "SUBSCRIBE_SUCCESS";
export const SUBSCRIBE_FAILED = "SUBSCRIBE_FAILED";

export class NearbyAPI {
  /**
   * Initializer for the RNNearbyApi wrapper.
   * @param {Boolean} bleOnly Only utilizes bluetooth through the Google Nearby SDK. Defaults to `true`.
   */
  constructor(bleOnly) {
    this._nearbyAPI = RNNearbyApi;
    this._eventEmitter =
      Platform.OS === "android"
        ? DeviceEventEmitter
        : new NativeEventEmitter(this._nearbyAPI);
    this._handlers = {};
    this._deviceEventSubscription = this._eventEmitter.addListener(
      "subscribe",
      Platform.OS === "android"
        ? this._eventHandler.bind(this)
        : this._eventHandler.bind(this)
    );
    this._isBLEOnly = !!bleOnly;
  }

  connect = apiKey => {
    this._nearbyAPI.connect(apiKey, this._isBLEOnly);
  };

  disconnect = () => {
    this._nearbyAPI.disconnect();
  };

  isConnected = cb => {
    this._nearbyAPI.isConnected(cb);
  };

  publish = message => {
    if (message !== null) {
      this._nearbyAPI.publish(message);
    } else {
      throw "Unable to publish a null message.";
    }
  };

  isPublishing = cb => {
    this._nearbyAPI.isPublishing(cb);
  };

  subscribe = () => {
    this._nearbyAPI.subscribe();
  };

  isSubscribing = cb => {
    this._nearbyAPI.isSubscribing(cb);
  };

  unpublish = () => {
    this._nearbyAPI.unpublish();
  };

  unsubscribe = () => {
    this._nearbyAPI.unsubscribe();
  };

  /**
   * Handler Helper Functions.
   */

  onConnected = handler => {
    this._setHandler(CONNECTED, handler);
  };

  onConnectionFailure = handler => {
    this._setHandler(CONNECTION_FAILED, handler);
  };

  onConnectionSuspended = handler => {
    this._setHandler(CONNECTION_SUSPENDED, handler);
  };

  onDisconnected = handler => {
    this._setHandler(DISCONNECTED, handler);
  };

  onFound = handler => {
    this._setHandler(MESSAGE_FOUND, handler);
  };

  onLost = handler => {
    this._setHandler(MESSAGE_LOST, handler);
  };

  onDistanceChanged = handler => {
    this._setHandler(DISTANCE_CHANGED, handler);
  };

  onBLESignalChanged = handler => {
    this._setHandler(BLE_SIGNAL_CHANGED, handler);
  };

  onPublishSuccess = handler => {
    this._setHandler(PUBLISH_SUCCESS, handler);
  };

  onPublishFailed = handler => {
    this._setHandler(PUBLISH_FAILED, handler);
  };

  onSubscribeSuccess = handler => {
    this._setHandler(SUBSCRIBE_SUCCESS, handler);
  };

  onSubscribeFailed = handler => {
    this._setHandler(SUBSCRIBE_FAILED, handler);
  };

  _setHandler = (event, handler) => {
    this._handlers[event] = handler;
  };

  _eventHandler = event => {
    if (this._handlers.hasOwnProperty(event.event)) {
      this._handlers[event.event](
        event.hasOwnProperty("message") ? event.message : null,
        event.hasOwnProperty("value") ? event.value : null
      );
    }
  };
}
