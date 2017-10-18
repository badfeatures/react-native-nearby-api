'use strict'
import { 
    NativeModules,
    DeviceEventEmitter,
} from 'react-native'

const { RNNearbyApi } = NativeModules

/**
 * Event Constants 
 */
const CONNECTED = "CONNECTED"
const CONNECTION_SUSPENDED = "CONNECTION_SUSPENDED"
const CONNECTION_FAILED = "CONNECTION_FAILED"
const MESSAGE_FOUND = "MESSAGE_FOUND"
const MESSAGE_LOST = "MESSAGE_LOST"
const DISTANCE_CHANGED = "DISTANCE_CHANGED"
const BLE_SIGNAL_CHANGED = "BLE_SIGNAL_CHANGED"

class NearbyAPI {

    constructor() {
        this._nearbyAPI = RNNearbyApi
        //TODO: Change according to platform, (DeviceEventEmitter for Android)
        this._eventEmitter = DeviceEventEmitter
        this._handlers = {}
        this._deviceEventSubscription = this._eventEmitter.addListener('subscribe', this._eventHandler.bind(this));
    }

    publish = message => {
        if(message !== null) {
            this._nearbyAPI.publish(message)
        } else {
            throw 'Unable to publish a null message.'
        }
    }

    subscribe = () => {
        this._nearbyAPI.subscribe()
    }

    /**
     * Handler Helper Functions.
     */

    onConnected = handler => {
        this._setHandler(CONNECTED, handler)
    }

    onConnectionFailure = handler => {
        this._setHandler(CONNECTION_FAILED, handler)
    }

    onConnectionSuspended = handler => {
        this._setHandler(CONNECTION_SUSPENDED, handler)
    }

    onFound = handler => {
        this._setHandler(MESSAGE_FOUND, handler)
    }

    onLost = handler => {
        this._setHandler(MESSAGE_LOST, handler)
    }

    onDistanceChanged = handler => {
        this._setHandler(DISTANCE_CHANGED, handler)
    }

    onBLESignalChanged = handler => {
        this._setHandler(BLE_SIGNAL_CHANGED, handler)
    }

    _setHandler = (event, handler) => {
        this._handlers[event] = handler
    }

    _eventHandler = event => {
        if (this._handlers.hasOwnProperty(event.event)) {
            this._handlers[event.event](
              event.hasOwnProperty('message') ? event.message : null,
              event.hasOwnProperty('value') ? event.value : null
            );
          }
    }
}

export default RNNearbyApi;
