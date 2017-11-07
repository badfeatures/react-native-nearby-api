
#import "RNNearbyApi.h"

typedef NS_ENUM(NSInteger, RNNearbyApiEvent) {
    CONNECTED,
    CONNECTION_SUSPENDED,
    CONNECTION_FAILED,
    DISCONNECTED,
    MESSAGE_FOUND,
    MESSAGE_LOST,
    DISTANCE_CHANGED,
    BLE_SIGNAL_CHANGED,
    PUBLISH_SUCCESS,
    PUBLISH_FAILED,
    SUBSCRIBE_SUCCESS,
    SUBSCRIBE_FAILED,
};

static GNSMessageManager *_messageManager = nil;

@implementation RNNearbyApi

@synthesize publication;
@synthesize subscription;

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (nonnull NSString *)stringForAPIEvent:(RNNearbyApiEvent)event {
    switch(event) {
        case CONNECTED: return @"CONNECTED";
        case CONNECTION_SUSPENDED: return @"CONNECTION_SUSPENDED";
        case CONNECTION_FAILED: return @"CONNECTION_FAILED";
        case DISCONNECTED: return @"DISCONNECTED";
        case MESSAGE_FOUND: return @"MESSAGE_FOUND";
        case MESSAGE_LOST: return @"MESSAGE_LOST";
        case DISTANCE_CHANGED: return @"DISTANCE_CHANGED";
        case BLE_SIGNAL_CHANGED: return @"BLE_SIGNAL_CHANGED";
        case PUBLISH_SUCCESS: return @"PUBLISH_SUCCESS";
        case PUBLISH_FAILED: return @"PUBLISH_FAILED";
        case SUBSCRIBE_SUCCESS: return @"SUBSCRIBE_SUCCESS";
        case SUBSCRIBE_FAILED: return @"SUBSCRIBE_FAILED";
        default: return @"CONNECTED";
    }
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"subscribe"];
//    return @[@"CONNECTED",
//             @"CONNECTION_SUSPENDED",
//             @"CONNECTION_FAILED",
//             @"DISCONNECTED",
//             @"MESSAGE_FOUND",
//             @"MESSAGE_LOST",
//             @"DISTANCE_CHANGED",
//             @"BLE_SIGNAL_CHANGED",
//             @"PUBLISH_SUCCESS",
//             @"PUBLISH_FAILED",
//             @"SUBSCRIBE_SUCCESS",
//             @"SUBSCRIBE_FAILED"];
}

- (void)sendEvent:(RNNearbyApiEvent)event withMessage:(GNSMessage *)message {
    NSString *eventString = [self stringForAPIEvent:event];
    NSString *messageString = [[NSString alloc] initWithData:message.content encoding: NSUTF8StringEncoding];
    NSDictionary *body = @{
                           @"event": eventString,
                           @"message": messageString
                           };
    [self sendEventWithName:@"subscribe" body:body];
}

- (void)sendEvent:(RNNearbyApiEvent)event withBody:(id)body {
    [self sendEventWithName:@"subscribe" body:body];
}

- (id)sharedMessageManager {
    @synchronized(self) {
        if(_messageManager == nil)
            _messageManager = [[GNSMessageManager alloc] initWithAPIKey:@"AIzaSyA0syu9nNgkHszm7OSWUsU47dowXAkv8LA"];
    }
    return _messageManager;
}

RCT_EXPORT_METHOD(connect) {
    // iOS Doesn't have a connect: method
    [self sharedMessageManager];
    [self sendEvent:CONNECTED withBody:@"Successfully connected."];
}

RCT_EXPORT_METHOD(disconnect) {
    // iOS Doesn't have a disconnect: method
    // Try setting messageManager to nil
    _messageManager = nil;
    [self sendEvent:DISCONNECTED withBody:@"Successfully disconnected."];
}

RCT_EXPORT_METHOD(publish:(nonnull NSString *)messageString) {
    if(messageString == nil) {
        [self sendEvent:PUBLISH_FAILED withBody:@"Cannot publish an empty message"];
    }
    // Release old publication
    [self unpublish];
    // Create new message
    GNSMessage *message = [GNSMessage messageWithContent: [messageString dataUsingEncoding: NSUTF8StringEncoding]];
    publication = [[self sharedMessageManager] publicationWithMessage: message];
    [self sendEvent:PUBLISH_SUCCESS withBody:[NSString stringWithFormat:@"Successfully published: %@", messageString]];
}

RCT_EXPORT_METHOD(unpublish) {
    publication = nil;
}

RCT_EXPORT_METHOD(subscribe) {
    // Release old subscription
    [self unsubscribe];
    // Create subscription object
    __weak RNNearbyApi *welf = self;
    subscription = [[self sharedMessageManager] subscriptionWithMessageFoundHandler:^(GNSMessage *message) {
        [welf sendEvent:MESSAGE_FOUND withMessage:message];
    } messageLostHandler:^(GNSMessage *message) {
        [welf sendEvent:MESSAGE_LOST withMessage:message];
    }];
    [self sendEvent:SUBSCRIBE_SUCCESS withBody:@"Successfully Subscribed."];
}

RCT_EXPORT_METHOD(unsubscribe) {
    subscription = nil;
}

@end
  
