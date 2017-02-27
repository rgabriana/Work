/**
 * Phonegap MyPlugin Instance plugin
 * 
 *
 */
var MyPlugin = {
    
nativeFunction: function(types, success, fail) {
    return PhoneGap.exec(success, fail, "PluginClass", "print", types);
    
}
};