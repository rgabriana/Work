//
//  MyClass.m
//  iOS
//
//  Created by Yogesh Chitnis on 01/11/11.
//  Copyright 2011 yogesh@enlightedinc.com. All rights reserved.
//


#import "MyClass.h" 

@implementation MyClass

@synthesize callbackID,receivedData;


NSMutableURLRequest *oRequest1;

NSURLConnection *xmlConnect;

-(void)nativeLogin:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options  
{
    //The first argument in the arguments parameter is the callbackID.
    //We use this to send data back to the successCallback or failureCallback
    //through PluginResult.   
    self.callbackID = [arguments pop];
    
        
    
    //[stringToReturn appendString: databuffer];
    
    NSString *databuffer = [arguments objectAtIndex:0];
    NSLog(@" %@",databuffer);
    
    NSString *serverUrl = [arguments objectAtIndex:1];
    
    NSURL *oRequestUrl = [NSURL URLWithString:serverUrl];
    
	//NSMutableURLRequest *oRequest = [[[NSMutableURLRequest alloc] init] autorelease];
	oRequest1 = [[[NSMutableURLRequest alloc] init] autorelease];
	[oRequest1 setValue:@"text/xml" forHTTPHeaderField:@"Content-Type"];
	[oRequest1 setHTTPMethod:@"POST"];
	[oRequest1 setURL:oRequestUrl];
	[oRequest1 setTimeoutInterval:60.0];
    //[oRequest1 set];
    
	//NSMutableData *oHttpBody = [NSMutableData data];
    
    receivedData = [[NSMutableData alloc]init];
	
	NSMutableData *oHttpBody = [[NSMutableData alloc]init];
	
     NSLog(@"log: %@ ",databuffer);
	[oHttpBody appendData:[databuffer dataUsingEncoding:NSUTF8StringEncoding]];
	[oRequest1 setHTTPBody:oHttpBody];
	[oRequest1 setValue:[NSString stringWithFormat:@"%d", [oHttpBody length]] forHTTPHeaderField:@"Content-Length"];
	
    
    
	
	xmlConnect = [[NSURLConnection alloc] initWithRequest:oRequest1 delegate:self startImmediately:YES];
	
	if (!xmlConnect)
	{
		NSLog(@"Error in connection");
	}
    
    if (xmlConnect)
	{
		NSLog(@"Sucess in connection");
	}
    
    [NSTimer scheduledTimerWithTimeInterval:15.0 target:self selector:@selector(cancelURLConnection:) userInfo:nil repeats:NO];    
    
     
    
}


-(void) cancelURLConnection:(NSTimer*)timer {
    NSLog(@"In Cancel Connection");
    [xmlConnect cancel];
    
    NSMutableString *stringToReturn = [NSMutableString stringWithString: @"105"];
    
    PluginResult* pluginResult = [PluginResult resultWithStatus:PGCommandStatus_OK messageAsString:                        [stringToReturn stringByStandardizingPath]];
    
    [self writeJavascript: [pluginResult toSuccessCallbackString:self.callbackID]];
    
}


-(void)connection:(NSURLConnection*)connection didReceiveResponse:(NSURLResponse*)response
{
	//receivedData = [[NSMutableData alloc] init]; // _data being an ivar
    NSLog(@"In didReceiveResponse");
    
}
-(void)connection:(NSURLConnection*)connection didReceiveData:(NSData*)data
{
	NSLog(@"Received %d bytes of data", [data length]);
	[receivedData appendData:data];
    NSString *dataStrreceivedData =[[[NSString alloc] initWithData:receivedData encoding:NSASCIIStringEncoding] autorelease];
    NSLog(@"ReceivedData :  %@ ", dataStrreceivedData );
    
}




- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
    //return [protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust];
    return YES;
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
	if ([challenge.protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust])
		//if ([trustedHosts containsObject:challenge.protectionSpace.host])
        [challenge.sender useCredential:[NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust] forAuthenticationChallenge:challenge];
	
	[challenge.sender continueWithoutCredentialForAuthenticationChallenge:challenge];
}


-(void)connection:(NSURLConnection*)connection didFailWithError:(NSError*)error
{
	
	NSLog(@"Error in Connecting to Server");
	    NSMutableString *stringToReturn = [NSMutableString stringWithString: @"105"];
    
    //Create Plugin Result
    
    PluginResult* pluginResult = [PluginResult resultWithStatus:PGCommandStatus_OK messageAsString:                        [stringToReturn stringByStandardizingPath]];
    
    
    //Call  the Success Javascript function
    [self writeJavascript: [pluginResult toSuccessCallbackString:self.callbackID]];
    

	
}

-(void)connectionDidFinishLoading:(NSURLConnection*)connection
{	
	
	NSString *dataStr=[[[NSString alloc] initWithData:receivedData encoding:NSASCIIStringEncoding] autorelease];
	NSLog(@"Succeeded! Received Data %@ :", dataStr);
	
	
    
    NSMutableString *stringToReturn = [NSMutableString stringWithString: @""];
    
    [stringToReturn appendString: dataStr];
    
    //Create Plugin Result
    
    PluginResult* pluginResult = [PluginResult resultWithStatus:PGCommandStatus_OK messageAsString:                        [stringToReturn stringByStandardizingPath]];
    
    
    
    //Call  the Success Javascript function
    [self writeJavascript: [pluginResult toSuccessCallbackString:self.callbackID]];
    
    
	
}	


@end