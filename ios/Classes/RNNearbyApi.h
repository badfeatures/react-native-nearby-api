#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <GNSMessages.h>

@interface RNNearbyApi : RCTEventEmitter <RCTBridgeModule>

@property(nonatomic, strong) id<GNSPublication> publication;
@property(nonatomic, strong) id<GNSSubscription> subscription;

@end
  
 
