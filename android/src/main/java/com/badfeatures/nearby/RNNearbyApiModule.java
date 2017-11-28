
package com.badfeatures.nearby;

import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

    private static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private enum RNNearbyApiEvent {
        CONNECTED("CONNECTED"),
        CONNECTION_SUSPENDED("CONNECTION_SUSPENDED"),
        CONNECTION_FAILED("CONNECTION_FAILED"),
        DISCONNECTED("DISCONNECTED"),
        MESSAGE_FOUND("MESSAGE_FOUND"),
        MESSAGE_LOST("MESSAGE_LOST"),
        DISTANCE_CHANGED("DISTANCE_CHANGED"),
        BLE_SIGNAL_CHANGED("BLE_SIGNAL_CHANGED"),
        PUBLISH_SUCCESS("PUBLISH_SUCCESS"),
        PUBLISH_FAILED("PUBLISH_FAILED"),
        SUBSCRIBE_SUCCESS("SUBSCRIBE_SUCCESS"),
        SUBSCRIBE_FAILED("SUBSCRIBE_FAILED")
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
    @NonNull private volatile Boolean _isPublishing = false;
    @NonNull private volatile Boolean _isSubscribing = false;
    @NonNull private Boolean _isBLEOnly = false;
    @NonNull private MessageListener _messageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            super.onFound(message);
            String messageAsString = new String(message.getContent());
            Log.d(getName(), "Message Found: " + messageAsString);
            emitEvent(RNNearbyApiEvent.MESSAGE_FOUND, messageAsString);
        }

        @Override
        public void onLost(Message message) {
            super.onLost(message);
            String messageAsString = new String(message.getContent());
            Log.d(getName(), "Message Lost: " + messageAsString);
            emitEvent(RNNearbyApiEvent.MESSAGE_LOST, messageAsString);
        }

        @Override
        public void onDistanceChanged(Message message, Distance distance) {
            super.onDistanceChanged(message, distance);
            //TODO: Combine both message + distance
            Log.d(getName(), "Distance Changed: " + message.toString() + " " + distance.getMeters() + "m");
            emitEvent(RNNearbyApiEvent.DISTANCE_CHANGED, message, (int)distance.getMeters());
        }

        @Override
        public void onBleSignalChanged(Message message, BleSignal bleSignal) {
            super.onBleSignalChanged(message, bleSignal);
            //TODO: Combine both message + bleSignal
            Log.d(getName(), "Distance Changed: " + message.toString() + " " + bleSignal.getRssi() + " rssi");
            emitEvent(RNNearbyApiEvent.BLE_SIGNAL_CHANGED, message, bleSignal.getRssi());
        }
    };

    public RNNearbyApiModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this._reactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
    }

    @Nullable
    private String getPublishedMessageString() {
        return _publishedMessage == null ? null : new String(_publishedMessage.getContent());
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

    private SubscribeOptions createSubscribeOptions(int ttlSeconds, boolean bleOnly) {
        Strategy pubSubStrategy;
        if(bleOnly) {
            pubSubStrategy = Strategy.BLE_ONLY;
        } else {
            pubSubStrategy = new Strategy.Builder().setTtlSeconds(ttlSeconds).build();
        }

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

    private boolean isMinimumAndroidVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    private boolean isGooglePlayServicesAvailable(boolean showErrorDialog) {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int availability = googleApi.isGooglePlayServicesAvailable(getReactApplicationContext());
        boolean result = availability == ConnectionResult.SUCCESS;
        if(!result &&
           showErrorDialog &&
           googleApi.isUserResolvableError(availability)
        ) {
            googleApi.getErrorDialog(getCurrentActivity(), availability, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        }
        return result;
    }

    /**
     *
     * @param apiKey - Note: API is unused for Android. Must be set in the AndroidManifest.
     */
    @ReactMethod
    public void connect(String apiKey, boolean bleOnly) {
        if(!isMinimumAndroidVersion()) {
            emitEvent(RNNearbyApiEvent.CONNECTION_FAILED, "Current Android version is too low: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        if(!isGooglePlayServicesAvailable(true)) {
            emitEvent(RNNearbyApiEvent.CONNECTION_FAILED, "Google Play Services is not available on this device.");
            return;
        }
        _isBLEOnly = bleOnly;
        GoogleApiClient client = getGoogleAPIInstance();
        if(client.isConnected()) {
            Log.w(getName(), "Google API Client is already connected.");
            emitEvent(RNNearbyApiEvent.CONNECTED, "Google API Client is already connected.");
            return;
        }
        client.connect();
    }

    @ReactMethod
    public void disconnect() {
        GoogleApiClient client = getGoogleAPIInstance();
        client.disconnect();
        Log.d(getName(), "Google API Client disconnected.");
        emitEvent(RNNearbyApiEvent.DISCONNECTED, "Google API Client is disconnected.");
    }

    @ReactMethod
    public void isConnected(Callback callback) {
        boolean connected = getGoogleAPIInstance().isConnected();
        callback.invoke(connected);
    }

    @ReactMethod
    public void isPublishing(Callback callback) {
        callback.invoke(_isPublishing);
    }

    @ReactMethod
    public void publish(String message) {
        Log.i(getName(), "Attempting to publish: " + message);
        GoogleApiClient client = getGoogleAPIInstance();
        if(client.isConnected()) {
            final Message publishMessage = new Message(message.getBytes());
            _publishedMessage = publishMessage;
            PublishOptions options = createPublishOptions(180);
            Log.i(getName(), "Publishing message: " + new String(publishMessage.getContent()));
            Nearby.Messages.publish(client, publishMessage, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.i(getName(), "Published message.");
                                _isPublishing = true;
                                emitEvent(RNNearbyApiEvent.PUBLISH_SUCCESS, publishMessage);
                            } else {
                                Log.e(getName(), "Publish failed");
                                Log.e(getName(), status.getStatusMessage());
                                _isPublishing = false;
                                emitEvent(RNNearbyApiEvent.PUBLISH_FAILED, "Publish failed: " + status.getStatusMessage());
                            }
                        }
                    });
        } else {
            Log.e(getName(), "Google API Client not connected. Call " + getName() + ".connect() before publishing.");
            emitEvent(RNNearbyApiEvent.PUBLISH_FAILED, "Google API Client not connected. Call " + getName() + ".connect() before publishing.");
        }
    }

    @ReactMethod
    public void unpublish() {
        GoogleApiClient client = getGoogleAPIInstance();
        if(client.isConnected() && (_publishedMessage != null || _isPublishing)) {
            Nearby.Messages.unpublish(client, _publishedMessage);
            _publishedMessage = null;
            _isPublishing = false;
            Log.i(getName(), "Unpublished message.");
        }
    }

    @ReactMethod
    public void isSubscribing(Callback callback) {
        callback.invoke(_isSubscribing);
    }

    @ReactMethod
    public void subscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if(client.isConnected()) {
            SubscribeOptions options = createSubscribeOptions(180, _isBLEOnly);
            Nearby.Messages.subscribe(client, _messageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.i(getName(), "Subscribe success.");
                                _isSubscribing = true;
                                emitEvent(RNNearbyApiEvent.SUBSCRIBE_SUCCESS, "Subscribe success. Is currently subscribing.");
                            } else {
                                Log.e(getName(), "Subscribe failed");
                                Log.e(getName(), status.getStatusMessage());
                                _isSubscribing = false;
                                emitEvent(RNNearbyApiEvent.SUBSCRIBE_FAILED, "Subscribe failed: " + status.getStatusMessage());
                            }
                        }
                    });
        } else {
            Log.e(getName(), "Google API Client not connected. Call " + getName() + ".connect() before subscribing.");
            emitEvent(RNNearbyApiEvent.SUBSCRIBE_FAILED, "Google API Client not connected. Call " + getName() + ".connect() before subscribing.");
        }
    }

    @ReactMethod
    public void unsubscribe() {
        GoogleApiClient client = getGoogleAPIInstance();
        if(client.isConnected())
            Nearby.Messages.unsubscribe(client, _messageListener);
        Log.i(getName(), "Unsubscribe listener.");
        _isSubscribing = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(getGoogleAPIInstance().isConnected()) {
            Log.d(getName(), "Google API Client connected.");
            emitEvent(RNNearbyApiEvent.CONNECTED, "Google API Client is connected.");
        } else {
            Log.e(getName(), "Google API Client not connected.");
        }
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
        if(connectionResult.hasResolution()) {
            try {
                PendingIntent pi = connectionResult.getResolution();
                Log.d(getName(), "Attempting to launch permission modal after failure.");
                pi.send();
            } catch (PendingIntent.CanceledException exception) {
                Log.e(getName(), exception.getMessage());
            }
        }
    }

    @Override
    public void onHostResume() {
        Log.i(getName(), "onHostResume");
        String pubMsg = getPublishedMessageString();
        if(_isPublishing && pubMsg != null) {
            publish(pubMsg);
        }
        if(_isSubscribing) {
            subscribe();
        }

    }

    @Override
    public void onHostPause() {
        Log.i(getName(), "onHostPause");
        unpublish();
        unsubscribe();
    }

    @Override
    public void onHostDestroy() {
        Log.i(getName(), "onHostDestroy");
        unpublish();
        unsubscribe();
    }

    private void emitEvent(RNNearbyApiEvent event, Message message) {
        emitEvent(event, new String(message.getContent()));
    }

    private void emitEvent(RNNearbyApiEvent event, Message message, Integer value) {
        emitEvent(event, new String(message.getContent()), value);
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