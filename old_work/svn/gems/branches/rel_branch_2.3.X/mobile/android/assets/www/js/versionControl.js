/**
 * @author Sameer Surjikar A version control system in this js file will load
 *         appropriate gem version of js file which is needed by that Gem's
 *         version. Each function that will be used in UI should have a js
 *         function layer in this js file and we use this js layer function in
 *         UI.
 */
/*function loadjscssfile(filename, filetype) {
	if (filetype == "js") { // if filename is a external JavaScript file
		var fileref = document.createElement('script')
		fileref.setAttribute("type", "text/javascript")
		fileref.setAttribute("src", filename)
	} else if (filetype == "css") { // if filename is an external CSS file
		var fileref = document.createElement("link")
		fileref.setAttribute("rel", "stylesheet")
		fileref.setAttribute("type", "text/css")
		fileref.setAttribute("href", filename)
	}
	if (typeof fileref != "undefined")
		document.getElementsByTagName("head")[0].appendChild(fileref)

	console.log(document.getElementsByTagName("head")[0]);
}*/

var version = "";

/*function setdefaultJS() {
	// code commented because dynamic loading of javascript file loading was not
	// sucessfull.
	// loadjscssfile("js/gems2.js", "js");
	
	 * $.getScript('js/gems2.js', function() { console.log("In get script")
	 * refreshSwitchList() ; }); if(window.localStorage.getItem("version")==0 ) {
	 *  }
	 

}*/

function setVersion() {
	//console.log("Printing the version"+window.localStorage.getItem("version"));	
	var mStr = window.localStorage.getItem("version");
	var mVersion = "";
		var formValues = mStr.split(".");
		for ( var j = 0; j < formValues.length; j++) {		
			mVersion = mVersion + formValues[j];
		}	
	if(Number(mVersion.slice(0,2)) >= 22)
		{
		version = 5;
		return;
		}
	if (window.localStorage.getItem("version") == "") {
		version = 0;
	}else{
		version = 3;
	}		
}

function refreshSwitchListLayer() {
	// setdefaultJS();
	setVersion();
	//console.log("Version Layer : " + window.localStorage.getItem("version"));
	switch (version) {
		case 0 :
		    //console.log(" Gem 2.0 js is called")
			gems2RefreshSwitchList();
			break;
		case 3 :
			//console.log(" Gem 3.0 js is called")
			gems3RefreshSwitchList();
			break;
		case 5:
			//console.log("Getting in ga");
			gemgaRefreshSwitchList();
			break;
		default :
			//console.log("In default of refreshSwitchListLayer no conditions matched. Gems version not correct.");
	}

}
function logoutLayer() {
	// setdefaultJS();
	setVersion();
	//console.log("Version Layer : " + window.localStorage.getItem("version"));
	switch (version) {
		case 0 :
		    //console.log("Logout using Gem 2.0 js ")
			gems2logout()
			break;
		case 3 :
			//console.log("Logout using Gem 3.0 js")
			gems3logout()
			break;
		case 5:
			//console.log("Gems ga logout");
			gemsgalogout();
			break;		
		default :
			//console.log("In default of refreshSwitchListLayer no conditions matched. Gems version not correct.");
	}

}