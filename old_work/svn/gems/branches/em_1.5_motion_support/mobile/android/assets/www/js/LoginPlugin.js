var LoginPlugin = {

		nativeLoginFunction : function(types,success, fail) {
			 return PhoneGap.exec(success,    //Success callback from the plugin
			       fail,     //Error callback from the plugin
			    'NativeLoginPlugin',  //Tell PhoneGap to run "PluginClass" Plugin
			     'nativeLogin',              //Tell plugin, which action we want to perform
			       types	//Passing list of args to the plugin
			      );        
			}
};