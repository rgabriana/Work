function gotFS(fileSystem) {
        fileSystem.root.getFile("/sdcard/enlightedMobile.txt", {create: false}, gotFileEntry, fail);
}

function gotFileEntry(fileEntry) {
	//console.log("In gotFileEntry Function");    
	fileEntry.file(gotFile, fail);
}

function gotFile(file){
	    //console.log("In GotFile Function");
        //readDataUrl(file);
        readAsText(file);
}

/*function readDataUrl(file) {
    var reader = new FileReader();
    reader.onloadend = function(evt) {
        console.log("Read as data URL");
        console.log(evt.target.result);
    };
    reader.readAsDataURL(file);
}*/

function readAsText(file) {
    var reader = new FileReader();
    reader.onloadend = function(evt) {
        //console.log("Read as text");
        //console.log(evt.target.result);
        completeVersionString = evt.target.result;
    };
    reader.readAsText(file);
}

function fail(evt) {
	console.log("fail");
	//console.log(evt.target.error.code);
}


function onBodyLoad() {
	
	document.addEventListener("deviceready", onDeviceReady, true);
	
	//window.localStorage.setItem("currentSessionId", null);
}

/*
 * When this function is called, PhoneGap has been initialized and is ready to
 * roll
 */

function onDeviceReady() {

	
	if (navigator.userAgent.toLowerCase().match(/android/)) {
		window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFS, fail);
	}
	else if (navigator.userAgent.toLowerCase().match(/iphone/) || navigator.userAgent.toLowerCase().match(/ipad/)){
			$.ajax({

	           type: "GET",
	           url: "js/iosRevision.txt",
	           contentType : "text",
	           dataType: "xml",
	           success: function parseXml (xml){
	        	   $(xml).find("revisionString").each(function()
	                                              {
	                                                //console.log($(this).text());
	        		   								completeVersionString = "2.1."+$(this).text();
                                                  });
	           },
	           error : function(){
	        	   console.log("iosRevision text file not found");
	           }
			});
	}
	
	
	// navigator.notification.alert("Welcome to EnLighted Mobile App");
	
	$('#settingsErrorMessage').text("") ;
	document.addEventListener("backbutton", backKeyDown, true);
	
	un = window.localStorage.getItem("usernamekey");
	pw = window.localStorage.getItem("pwordkey");

	/*
	 * document.myform.username.value = un; document.myform.pword.value = pw;
	 */

	if ($('input[type=checkbox]').is(':checked') == true) {

		document.myform.username.value = un;
		document.myform.pword.value = pw;

	} else {

		document.myform.username.value = "";
		document.myform.pword.value ="";

	}

}

/* This function is called when the back key is pressed. */

function backKeyDown() {
	// do something

}

$(document).bind("mobileinit", function() {
			// console.log("In mobileinit method");
			$.event.special.swipe.durationThreshold = 4000;
			// console.log("Duration Thresold :
			// "+$.event.special.swipe.durationThreshold);
			$.mobile.fixedToolbars.setTouchToggleEnabled(false);
			$.mobile.fixedToolbars.show(true);

			if (navigator.userAgent.toLowerCase().match(/android/)) {
				//$.mobile.defaultPageTransition = 'none';
				// console.log("Android Mobile");
			}

		});

$("#loginform").live("submit", function(event) {
			event.preventDefault();
			// console.log("in loginform submit function");
			login();
		});

$("#settingsForm").live("submit", function(event) {
			event.preventDefault();
			// console.log("in settingsForm submit function");
			applySettings();
		});

/* Event handler function when Settings Page is Loaded */

$('#settingsPage').live('pageshow', function(event) {

	document.mySettings.serverip.value = window.localStorage
			.getItem("srvipkey");
	document.mySettings.serverport.value = window.localStorage
			.getItem("srvportkey");

});

var completeVersionString = "";

/* This function called when About button is clicked */
function infoDialog() {

	/*navigator.notification.alert("EnLighted Mobile App 2.1", alertDismissed,
			'Enlighted', 'Ok');*/
	
	navigator.notification.alert("EnLighted Mobile App "+completeVersionString , alertDismissed,
			'Enlighted', 'Ok');

}

var serverUrl = "";

var srvip = "";

var srvport = "";

var info = "";

var switchxmlResult = "";

function toSettings() {
	$.mobile.changePage("settings.html", {
				transition : "fade"
			});
}

/* This function called when Settings button is clicked */
function applySettings() {

	// console.log("In applySettings page");
	
	$('#settingsErrorMessage').text("");

	srvip = document.mySettings.serverip.value;

	srvport = document.mySettings.serverport.value;

	if (srvip == "") {
		$('#settingsErrorMessage').text("Please enter the Server IP");

	} else {

		if (srvport == "") {
			$('#settingsErrorMessage').text("Please enter the Server Port");

		} else {
			$.mobile.changePage("index.html", {
						transition : "fade"
					});

			window.localStorage.setItem("srvipkey",
					document.mySettings.serverip.value);

			window.localStorage.setItem("srvportkey",
					document.mySettings.serverport.value);

			srvip = document.mySettings.serverip.value;

			srvport = document.mySettings.serverport.value;
			// For 3.0 and above version
			var serverBaseUrl =  "https://" + srvip + ":" + srvport
					+ "/" ;
				window.localStorage.setItem("serverBaseUrl", serverBaseUrl );
			// For 2.0 gems version
			serverUrl = "https://" + srvip + ":" + srvport
					+ "/ems/wsaction.action";
					window.localStorage.setItem("serverUrl", serverUrl);

			$('#errorMessage').text("Settings saved");
		}

	}

}

/* This function called when Cancel button in Settings page is clicked */
function cancelSettings() {

	$('#settingsErrorMessage').text("");

	$('#errorMessage').text("");

	srvip = window.localStorage.getItem("srvipkey");

	srvport = window.localStorage.getItem("srvportkey");

	serverUrl = "https://" + srvip + ":" + srvport + "/ems/wsaction.action";

	$.mobile.changePage("index.html", {
				transition : "fade"
			});
}


/* This function is called when back to switches button is clicked */
function backToSwitches() {
	$.mobile.changePage("switchList.html", {
				transition : "fade"
			});
	 refreshSwitchListLayer() ;
}


// To Quit App in Android
/*
 * function quitApp(){ navigator.app.exitApp(); //alert("in quit app");
 * //device.exitApp(); }
 */






/* This function is called when refresh button is clicked */
/* refreshSwitchList()
function refreshSwitchList() {

	var switchListString = "";

	$.ajax({
				type : "POST",
				url : serverUrl,
				data : getSwitchXml,
				contentType : "text/xml",
				dataType : "xml",
				success : function switchSucess(xml) {

					// console.log("In switchSucess Start "+xml);
					$(xml).find("response").each(function() {
								if ($(this).children("errorCode").text() == 003) {
									// console.log($(this).children("errorMessage").text());
									$.mobile.changePage("index.html", {
												transition : "fade"
											});
								}

							});

					switchxmlResult = xml;

					$(xml).find("switch").each(function() {

						info = $(this).children("name").text();
						var switchId = $(this).children("id").text();

						// console.log("Switch Name :"+info);
						var noOfScenes = $(this).find("scene").length;
						noOfScenes -= 2; // Don't consider "All On" & "All
						// Off" as attached scenes

						switchListString += "<li><a href=\"scenes.html?id="
								+ switchId + "\">" + info
								+ "<span class=\"ui-li-count\">" + noOfScenes
								+ "</span>" + "</a></li>";
					});
					$('#switchList').empty().append(switchListString);
					$('#switchList').listview('refresh');
					// console.log("In switchSucess End "+xml);

				}

			});

}*/

var getSwitchXml = "";

var currentSessionId = "";

var un = "";
var pw = "";

function alertDismissed() {

}

function login() {

	// console.log("In Login function");

	$('#errorMessage').text("");

	// $('#errorMessage').html("&nbsp;<br/>&nbsp;");

	/*
	 * window.localStorage.setItem("usernamekey",
	 * document.myform.username.value);
	 * 
	 * window.localStorage.setItem("pwordkey", document.myform.pword.value);
	 */

	if ($('input[type=checkbox]').is(':checked') == true) {

		window.localStorage.setItem("usernamekey",
				document.myform.username.value);

		window.localStorage.setItem("pwordkey", document.myform.pword.value);

	} else {

		//window.localStorage.setItem("usernamekey", "yogesh@enlightedinc.com");
		window.localStorage.setItem("usernamekey", "");
		window.localStorage.setItem("pwordkey", "");
		//window.localStorage.setItem("pwordkey", "chitnis");
	}

	/*
	 * un = window.localStorage.getItem("usernamekey"); pw =
	 * window.localStorage.getItem("pwordkey");
	 */

	un = document.myform.username.value;
	pw = document.myform.pword.value;

	srvip = window.localStorage.getItem("srvipkey");

	srvport = window.localStorage.getItem("srvportkey");

	if (srvip == null || srvport == null) {
		$('#errorMessage').text("Please enter the server details");

	} else {

		if (un == "") {
			$('#errorMessage').text("Please enter the Username");
		} else {

			if (pw == "") {
				$('#errorMessage').text("Please enter the Password");
			} else {

				$('#errorMessage').text("Please wait...");

				$("[type='submit']").button('disable');

				serverUrl = "https://" + srvip + ":" + srvport
						+ "/ems/wsaction.action";

				var loginXML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>1</messageType><body><loginRequest><userName>"
						+ un
						+ "</userName><password>"
						+ pw
						+ "</password></loginRequest></body></request></root>";

				// var loginResponceString =
				// window.enlightedLoginJS.login(loginXML,serverUrl);

				LoginPlugin.nativeLoginFunction([loginXML, serverUrl],

				function(result) {

					var loginResponceString = result;
					console.log(result);
					if (loginResponceString == "101"
							|| loginResponceString == "102"
							|| loginResponceString == "103"
							|| loginResponceString == "104"
							|| loginResponceString == "105") {

						// navigator.notification.alert(
						// 'Please check the server details',
						// alertDismissed, 'Enlighted', 'Ok');
						// console.log("Server Error Code : "
						// + loginResponceString);
						$('#errorMessage')
								.text("Please check the server details");

						// console.log("Login Responce String :
						// "+loginResponceString);
						$("[type='submit']").button('enable');

					} else {
						// $('#errorMessage')
						// .text("this works");
						// console.log("Able to connect to server");
						var parser = new DOMParser();

						var loginResponceXML = parser.parseFromString(
								loginResponceString, 'text/xml');
						
						//console.log("Login xml:"+ $(loginResponceXML).find("loginResponse").text());
						
						//To handle the responce for authentication failure in gems 3.0
						if( $(loginResponceXML).find("loginResponse").text() == "" ){
							$('#errorMessage').text("Please check userid and password");
						}
						
						
						$(loginResponceXML).find("loginResponse").each(
								function() {

									if ($(this).find("result").text() == 1) {
										currentSessionId = $(this)
												.find("sessionId").text();
										
							// Version control and gemsversion above 2.0 support start from here.  
							// depending on version we start calling respective function 
												window.localStorage.setItem("version", $(loginResponceXML).find("version").text() );
												window.localStorage.setItem("currentSessionId", currentSessionId);
												//console.log("SESSIONID Orignal: " +currentSessionId)
												window.localStorage.setItem("userId", $(loginResponceXML).find("userId").text() );
												
												$.mobile.changePage("switchList.html",
														{
															transition : "fade"
														});
														
												// This function is defined in version layer which decide on the version of the gem
												// which gems.js refreshswitchlist function to call. 
												refreshSwitchListLayer() ;
										
										

									} else {

										// console.log("Authentication
										// failure");
										// navigator.notification.alert('Please
										// check userid and
										// password',alertDismissed,'Enlighted','Ok'
										// );
										$('#errorMessage')
												.text("Please check userid and password");

										$("[type='submit']").button('enable');
									}
								});

						$("[type='submit']").button('enable');

					}

				},

				function(error) {

					// console.log("Native Method Error");
					$('#errorMessage').text(error);
					$("[type='submit']").button('enable');

				});

				// console.log("Server Detils are not empty");
			}

		}

	}

}