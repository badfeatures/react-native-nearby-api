'use strict'
import { 
    NativeModules,
    DeviceEventEmitter,
} from 'react-native'

const { RNNearbyApi } = NativeModules

/**
 * Event Constants 
 */
export const CONNECTED = "CONNECTED"
export const CONNECTION_SUSPENDED = "CONNECTION_SUSPENDED"
export const CONNECTION_FAILED = "CONNECTION_FAILED"
export const MESSAGE_FOUND = "MESSAGE_FOUND"
export const MESSAGE_LOST = "MESSAGE_LOST"
export const DISTANCE_CHANGED = "DISTANCE_CHANGED"
export const BLE_SIGNAL_CHANGED = "BLE_SIGNAL_CHANGED"

export class NearbyAPI {

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

    unpublish = () => {
        this._nearbyAPI.unpublish()
    }

    unsubscribe = () => {
        this._nearbyAPI.unsubscribe()
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