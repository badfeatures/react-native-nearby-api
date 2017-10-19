
package com.badfeatures.nearby;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

public class RNNearbyApiModule extends ReactContextBaseJavaModule implements LifecycleEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private enum RNNearbyApiEvent {
        CONNECTED("CONNECTED"),
        CONNECTION_SUSPENDED("CONNECTION_SUSPENDED"),
        CONNECTION_FAILED("CONNECTION_FAILED"),
        MESSAGE_FOUND("MESSAGE_FOUND"),
        MESSAGE_LOST("MESSAGE_LOST"),
        DISTANCE_CHANGED("DISTANCE_CHANGED"),
        BLE_SIGNAL_CHANGED("BLE_SIGNAL_CHANGED")
        ;

        private final String _type;

        RNNearbyApiEvent(String type) {
            _type = type;
        }

        @Override
        public String toString() {
            return _type;
        }
    }

    private final ReactApplicationContext _reactContext;
    private GoogleApiClient _googleAPIClient;
    @Nullable private Message _publishedMessage;
    @NonNull private MessageListener _messageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            super.onFound(message);
            Log.i(getName(), "Message Found: " + message.toString());
            emitEvent(RNNearbyApiEvent.MESSAGE_FOUND, message);
        }

        @Override
        public void onLost(Message message) {
            super.onLost(message);
            Log.i(getName(), "Message Lost: " + message.toString());
            emitEvent(RNNearbyApiEvent.MESSAGE_LOST, message);
        }

        @Override
        public void onDistanceChanged(Message message, Distance distance) {
            super.onDistanceChanged(message, distance);
            //TODO: Combine both message + distance
            Log.i(getName(), "Distance Changed: " + message.toString() + " " + distance.getMeters() + "m");
            emitEvent(RNNearbyApiEvent.DISTANCE_CHANGED, message, (int)distance.getMeters());
        }

        @Override
        public void onBleSignalChanged(Message message, BleSignal bleSignal) {
            super.onBleSignalChanged(message, bleSignal);
            //TODO: Combine both message + bleSignal
            Log.i(getName(), "Distance Changed: " + message.toString() + " " + bleSignal.getRssi() + " rssi");
            emitEvent(RNNearbyApiEvent.BLE_SIGNAL_CHANGED, message, bleSignal.getRssi());
        }
    };

    public RNNearbyApiModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this._reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNNearbyApi";
    }

    private synchronized GoogleApiClient getGoogleAPIInstance() {
        if (_googleAPIClient == null) {
            _googleAPIClient = new GoogleApiClient
                    .Builder(this.getReactApplicationContext())
                    .addApi(Nearby.MESSAGES_API)
                    //TODO: Add more functionality (Currently only: Messages API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        return _googleAPIClient;
    }

    private PublishOptions createPublishOptions(int ttlSeconds) {
        Strategy pubSubStrategy = new Strategy.Builder().setTtlSeconds(ttlSeconds).build();

        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(pubSubStrategy)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                    }
                }).build();
        return options;
    }

    private SubscribeOptions createSubscribeOptions(int ttlSeconds) {
        Strategy pubSubStrategy = new Strategy.Builder().setTtlSeconds(ttlSeconds).build();

        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(pubSubStrategy)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                    }
                })
                .build();
        return options;
    }

    @ReactMethod
    public void publish(String message) {
        GoogleApiClient client = getGoogleAPIInstance();
        Message publishMessage = new Message(message.getBytes());
        _publishedMessage = publishMessage;

        if(client.isConnected()) {
            PublishOptions options = createPublishOptions(180);
            Log.i(getName(), "Publishing message: " + publishMessage.toString());
            Nearby.Messages.publish(client, publishMessage, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(getName(), "Published message.");
                        } else {
                            Log.e(getName(), "Publish failed");
                            Log.e(getName(), status.getStatusMessage());
                        }
                    }
                });
        }
    }

    @ReactMethod
    public void unpublish() {
        GoogleApiClient client = getGoogleAPIInstance();
        if(_publishedMessage != null) {
            Nearby.Messages.unpublish(client, _publishedMessage);
            _publishedMessage = null;
            Log.i(getName(), "Unpublished message.");
        }
    }

    @ReactMethod
    public void subscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if(client.isConnected()) {
            SubscribeOptions options = createSubscribeOptions(180);
            Nearby.Messages.subscribe(client, _messageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.i(getName(), "Subscribe success.");
                            } else {
                                Log.e(getName(), "Subscribe failed");
                                Log.e(getName(), status.getStatusMessage());
                            }
                        }
                    });
        }
    }

    @ReactMethod
    public void unsubscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        Nearby.Messages.unsubscribe(client, _messageListener);
        Log.i(getName(), "Unsubscribed listener.");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(getName(), "Google API Client connected.");
        if(_publishedMessage != null) {
            publish(_publishedMessage.toString());
        }
        subscribe();
        emitEvent(RNNearbyApiEvent.CONNECTED, "Nearby Messages is publishing and subscribing.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(getName(), RNNearbyApiEvent.CONNECTION_SUSPENDED.toString() + " " + i);
        emitEvent(RNNearbyApiEvent.CONNECTION_SUSPENDED, "Google Client connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(getName(), RNNearbyApiEvent.CONNECTION_FAILED.toString() + " " + connectionResult.toString());
        emitEvent(RNNearbyApiEvent.CONNECTION_FAILED, "Google Client connection failed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onHostResume() {
        Log.i(getName(), "onHostResume");
        GoogleApiClient client = getGoogleAPIInstance();
        client.connect();
    }

    @Override
    public void onHostPause() {
        Log.i(getName(), "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        Log.i(getName(), "onHostDestroy");
        unpublish();
        unsubscribe();

        GoogleApiClient client = getGoogleAPIInstance();
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    private void emitEvent(RNNearbyApiEvent event, Message message) {
        emitEvent(event, message.toString());
    }

    private void emitEvent(RNNearbyApiEvent event, Message message, Integer value) {
        emitEvent(event, message.toString(), value);
    }

    private void emitEvent(RNNearbyApiEvent event, String message) {
        emitEvent(event, message, null);
    }

    private void emitEvent(RNNearbyApiEvent event, String message, Integer value) {
        WritableMap params = Arguments.createMap();
        params.putString("event", event.toString());
        params.putString("message", message);
        if(value != null) {
            params.putInt("value", value);
        }
        RNNearbyApiModule.emitEvent(getReactApplicationContext(), "subscribe", params);
    }

    private static void emitEvent(ReactContext context, String event, Object object) {
        if (context != null) {
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(event, object);
        } else {
            Log.e("eventEmit", "Null context");
        }

    }
}