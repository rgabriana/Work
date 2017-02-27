//
//  MyClass.h
//  iOS
//
//  Created by Yogesh Chitnis on 01/11/11.
//  Copyright 2011 yogesh@enlightedinc.com. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <PhoneGap/PGPlugin.h>

//NSMutableData *receivedData;

@interface MyClass : PGPlugin {
    
    NSString* callbackID;
    NSMutableData *receivedData;
}

@property (nonatomic, copy) NSString* callbackID;
@property (nonatomic, retain) NSMutableData *receivedData;

//Instance Method  
- (void) nativeLogin:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;

@end