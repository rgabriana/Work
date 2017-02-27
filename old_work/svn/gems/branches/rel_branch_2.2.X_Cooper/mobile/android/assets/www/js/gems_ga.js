/* This function called when Logout button is clicked */
function gemsgalogout() {
	// $("[type='submit']").button('enable');
	
	logoutUrl = window.localStorage.getItem("serverBaseUrl");
	logoutUrl += "ems/j_spring_security_logout";

	$('#errorMessage').text("");
	$.mobile.changePage("index.html", {
				transition : "fade"
			});
	$("[type='submit']").button('enable');
	if ($('input[type=checkbox]').is(':checked') == false) {

		document.myform.username.value = "";

		document.myform.pword.value = "";
	}
	//console.log("Logging out using url " + logoutUrl);
	$.ajax({
				type : "GET",
				url : logoutUrl,
				cache : false,
				success : function sceneSucess(xml) {

					//console.log("Sucess Logging out of 3.0 server");

				},
				error : function(jqXHR, textStatus, errorThrown) {
					//console.log("error "+errorThrown);
				}

			});

	//window.localStorage.setItem("currentSessionId", null);

}

var switchListServerUrl = "";
var applySliderDimCmdServerUrl = "";

//TODO ; Removal of this variable as it does not play any role for relative dimming of 1.x sensors too.
var iDimVal = 50;


function setUrls() {
	//console.log("BASEURL used : "+ window.localStorage.getItem("serverBaseUrl"));
	// webservice used Switchservice
	switchListServerUrl = window.localStorage.getItem("serverBaseUrl");
	switchListServerUrl += "ems/services/org/switch/list/userfacilities/";
	switchListServerUrl += window.localStorage.getItem("userId");

	//console.log("Refreshing List for UserID : "+ window.localStorage.getItem("userId"));
	//console.log("switchListServerUrl used : " + switchListServerUrl);

}

/* This function is called when refresh button is clicked */
function gemgaRefreshSwitchList() {
	setUrls();
	var switchListString = "";

	$.ajax({
				type : "GET",
				url : switchListServerUrl,
				contentType : "text/xml",
				dataType : "xml",
				cache : false,
				headers : {
					Cookie : "JSESSIONID="
							+ window.localStorage.getItem("currentSessionId")
				},
				beforeSend : function(xhr) {
					xhr.withCredentials = true;
					// xhr.setRequestHeader("Set-Cookie: JSESSIONID=",
					// window.localStorage.getItem("currentSessionId")+ ";
					// HttpOnly");
					// console.log("Started ajax call with session id" +
					// window.localStorage.getItem("currentSessionId"));
					// console.log(xhr.responseText) ;

				},

				success : function switchSucess(xml) {

					//console.log("Sucess in gems3 refresh list webservice");

					switchxmlResult = xml;
					
					//console.log("gems3RefreshSwitchList : "+(new XMLSerializer()).serializeToString(switchxmlResult) );

					$(xml).find("switchDetail").each(function() {

						info = $(this).children("name").text();
						var switchId = $(this).children("id").text();
						var noOfScenes = $(this).find("scenecount").text();
						noOfScenes -= 2; // Don't consider "All On" & "All
						// Off" as attached scenes

						//console.log("Switch ID :" + switchId);
						//console.log("Switch Name :" + info);
						//console.log("scenecount : " + noOfScenes)

						/*if( noOfScenes == 0){
							
							switchListString += "<li><a href=\"emptyScenes.html?id="
								+ switchId + "\">" + info
								+ "<span class=\"ui-li-count\">" + noOfScenes
								+ "</span>" + "</a></li>";
							
						}else{
							
							switchListString += "<li><a href=\"scenes.html?id="
								+ switchId + "\">" + info
								+ "<span class=\"ui-li-count\">" + noOfScenes
								+ "</span>" + "</a></li>";
							
						}*/
						
						
						switchListString += "<li><a href=\"scenesgems_ga.html?id="
							+ switchId + "\">" + info
							+ "<span class=\"ui-li-count\">" + noOfScenes
							+ "</span>" + "</a></li>";
						
						
						/*switchListString += "<li><a onclick=\"fixtureList("+switchId+")\">" + info
								+ "<span class=\"ui-li-count\">" + noOfScenes
								+ "</span>" + "</a></li>";*/
						
						// switchListString += "<li><a href=\"scenes.html
						// \">"+"hi"+ "</a></li>";
						//console.log("URL Created " + switchListString);
					});
					
					if( switchListString == ""){
				  		switchListString = "There are no Switches Configured";
				  	}
					
					$('#switchList').empty().append(switchListString);
					$('#switchList').listview('refresh');
					//console.log("In switchSucess End " + xml);

				},
				error : function(jqXHR, textStatus, errorThrown) {
					//console.log('error');
					//console.log(errorThrown);
					//console.log(jqXHR);
					//console.log(textStatus);
					//console.log("Error while communicating to gems3 refresh list webservice")
					
					if(errorThrown == "Unauthorized"){
						
						$('#errorMessage').text("Session Expired.Please login");
						//gems3logout();
						gemsgalogout();
						
					}

				}

			});
}

/*
 * Below is the code for Gems 3.0 verion to handle Scene activity and make calls
 * to change Scene and do other on/off related things.
 */

// var currentSessionId = ""
var applySwitchId = "";

var applySceneId = "";

var applyBrightnessValue = "";

var clickedScene = "";
var percentage = "";
var time = "";

function applyAttachedSceneForSwitch(switchId, sceneId,selectedSceneOrder,id) {
	//console.log("In applying");
	if (clickedScene != "" && clickedScene != sceneId) {
		$("#" + clickedScene + "  img.ui-li-icon").attr("src",
				"js/images/grey16x16.png");
	}

	if (clickedScene != sceneId) {
		$("#" + sceneId + "  img.ui-li-icon").attr("src",
				"js/images/green16x16.png");
	}
	//console.log("In applying done");
	clickedScene = sceneId;

	if (clickedButton != "") {
		$("#" + clickedButton + "  span.ui-icon").addClass("ui-icon-scene-off")
				.removeClass("ui-icon-scene-on");
		//console.log("Clicked Button is :" + clickedButton);
		clickedButton = "";
	}
	//console.log("");
	applySceneForSwitch(switchId, selectedSceneOrder);
}

function applySceneForSwitch(switchId, sceneId) {
	//console.log("Ïn apply scene");
	applySwitchId = switchId;
	if (sceneId == null) {
		applySceneId = "";
	} else
		applySceneId = sceneId;	
	
	applySceneServerUrl = window.localStorage.getItem("serverBaseUrl");
	applySceneServerUrl += "ems/services/org/switch/op/"
			+ applySwitchId + "/action/scene/argument/" + applySceneId 
	//console.log(applySceneServerUrl);
	$.ajax({
				type : "POST",
				url : applySceneServerUrl,
				contentType : "application/xml",
				dataType : "xml",
				cache : false,
				headers : {
					Cookie : "JSESSIONID="
							+ window.localStorage.getItem("currentSessionId")
				},
				beforeSend : function(xhr) {
					xhr.withCredentials = true;
				},
				success : function sceneSucess(xml) {

					/*
					 * $(xml).find("setSwitchResponse").each(function() {
					 * if($(this).children("result").text() == 1 ){
					 * console.log("The Scene is applied"); }});
					 */
					console.log("Sucess in  gemsga apply scene webservice");
					

				},
				error : function(jqXHR, textStatus, errorThrown) {
					//console.log('error');
					//console.log(errorThrown);
					//console.log(jqXHR);
					//console.log(textStatus);
					console.log("Error while communicating to gemsga apply scene webservice")
					
					if(errorThrown == "Unauthorized"){
						
						$('#errorMessage').text("Session Expired.Please login");
						//gems3logout();
						gemsgalogout();
						
					}
				}

			});

}


/*curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/{switchid}/action/{action}/argument/{argument}
{
	switchid (database ID for the switch, to which the switch group and the fixtures are associated)
	action (auto | scene | dimup | dimdown), default is auto
	argument (auto {0} | scene {0-8} | dimup {10} | dimdown {10})
}*/
function applyDimForSwitch(switchId)
{	
	///services/org/switch/op/
	applySwitchId = switchId;
	applySliderDimCmdServerUrl = window.localStorage.getItem("serverBaseUrl");
	
	applySliderDimCmdServerUrl += "ems/services/org/switch/op/"
		+ applySwitchId + "/action/dimdown/argument/"+iDimVal;
		
		$.ajax({
			type : "POST",
			url : applySliderDimCmdServerUrl,
			contentType : "application/xml",
			dataType : "xml",
			cache : false,
			headers : {
				Cookie : "JSESSIONID="
						+ window.localStorage.getItem("currentSessionId")
			},
			beforeSend : function(xhr) {
				xhr.withCredentials = true;
			},
			success : function sceneSucess(xml) {
				//console.log(xml);
				//console.log("Dim success");
				
				/*
				 * $(xml).find("setSwitchResponse").each(function() {
				 * if($(this).children("result").text() == 1 ){
				 * console.log("The Scene is applied"); }});
				 */
				//console.log("Sucess in gems3 apply slider webservice");
				

			},
			error : function(jqXHR, textStatus, errorThrown) {
				//console.log('error');
				//console.log(errorThrown);
				//console.log(jqXHR);
				//console.log(textStatus);
				//console.log("Error while communicating to gems3 apply slider webservice")
				
				if(errorThrown == "Unauthorized"){
					
					$('#errorMessage').text("Session Expired.Please login");
					//gems3logout();
					gemsgalogout();
					
				}
			}

		});
}

function applyBrightForSwitch(switchId)
{
	
	applySwitchId = switchId;
	applySliderDimCmdServerUrl = window.localStorage.getItem("serverBaseUrl");
	
	applySliderDimCmdServerUrl += "ems/services/org/switch/op/"
		+ applySwitchId + "/action/dimup/argument/"+iDimVal;
		
		$.ajax({
			type : "POST",
			url : applySliderDimCmdServerUrl,
			contentType : "application/xml",
			dataType : "xml",
			cache : false,
			headers : {
				Cookie : "JSESSIONID="
						+ window.localStorage.getItem("currentSessionId")
			},
			beforeSend : function(xhr) {
				xhr.withCredentials = true;
			},
			success : function sceneSucess(xml) {
				//console.log(xml);
				//console.log("Bright success");
				/*
				 * $(xml).find("setSwitchResponse").each(function() {
				 * if($(this).children("result").text() == 1 ){
				 * console.log("The Scene is applied"); }});
				 */
				//console.log("Sucess in gems3 apply slider webservice");
				

			},
			error : function(jqXHR, textStatus, errorThrown) {
				//console.log('error');
				//console.log(errorThrown);
				//console.log(jqXHR);
				//console.log(textStatus);
				//console.log("Error while communicating to gems3 apply slider webservice")
				
				if(errorThrown == "Unauthorized"){
					
					$('#errorMessage').text("Session Expired.Please login");
					//gems3logout();
					gemsgalogout();
					
				}
			}

		});

	
}

function applyAutoForSwitch(switchId)
{
	applySwitchId = switchId;
	applySliderDimCmdServerUrl = window.localStorage.getItem("serverBaseUrl");	
	applySliderDimCmdServerUrl += "ems/services/org/switch/op/"
		+ applySwitchId + "/action/auto/argument/101"
		
		$.ajax({
			type : "POST",
			url : applySliderDimCmdServerUrl,
			contentType : "application/xml",
			dataType : "xml",
			cache : false,
			headers : {
				Cookie : "JSESSIONID="
						+ window.localStorage.getItem("currentSessionId")
			},
			beforeSend : function(xhr) {
				xhr.withCredentials = true;
			},
			success : function sceneSucess(xml) {

				/*
				 * $(xml).find("setSwitchResponse").each(function() {
				 * if($(this).children("result").text() == 1 ){
				 * console.log("The Scene is applied"); }});
				 */
				//console.log("Sucess in gems3 apply slider webservice");
				

			},
			error : function(jqXHR, textStatus, errorThrown) {
				//console.log('error');
				//console.log(errorThrown);
				//console.log(jqXHR);
				//console.log(textStatus);
				//console.log("Error while communicating to gems3 apply slider webservice")
				
				if(errorThrown == "Unauthorized"){
					
					$('#errorMessage').text("Session Expired.Please login");
					//gems3logout();
					gemsgalogout();
				}
			}

		});

	
}

var sliderBrightness = "";

var clickedButton = "";




/* Event Handler when scenepage is Loaded */
$('#scenePage').live('pageshow', function(event) {

	if(version == 5){
	//console.log("Scene Page of version 2.1"); 
	// $.mobile.loadingMessage = "Scenes Page is Loading";
	// $.mobile.pageLoading();
	// $.mobile.showPageLoadingMsg();

	/*------- CSS Changes to set slider image starts here-------- */

	$('.ui-slider').removeClass('ui-btn-corner-all');
	$('a.ui-slider-handle')
			.removeClass('ui-btn ui-btn-up-c ui-btn-corner-all ui-shadow');

	/*------- CSS Changes to set slider image ends here-------- */

	var switchId = getUrlVars()["id"];

	$("input[name=slider]").bind("change", function() {
		 
		 //console.log("Change Slid: "+$(this).val());
		 $("#slider_value").html($(this).val() + "%");
	 });

	$("input[name=slider]").siblings('.ui-slider').bind("tap",
			function(event, ui) {

				// console.log("Tap : "+$(this).siblings('input').val());
				sliderBrightness = $(this).siblings('input').val();

				$("#slider_value").html(sliderBrightness + "%");
				$("#slider_value").css(
						"left",
						(sliderBrightness * 3 - (sliderBrightness < 10
								? 15
								: 20))
								+ "px");

				applySlider(switchId, 60, sliderBrightness);

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				if (clickedButton != "") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					// console.log("Clicked Button is :"+clickedButton);
					clickedButton = "";
				}

			});

	$("input[name=slider]").siblings('.ui-slider').bind("taphold",
			function(event, ui) {

				// console.log("Taphold : "+$(this).siblings('input').val());
				sliderBrightness = $(this).siblings('input').val();

				$("#slider_value").html(sliderBrightness + "%");
				$("#slider_value").css(
						"left",
						(sliderBrightness * 3 - (sliderBrightness < 10
								? 15
								: 20))
								+ "px");

				applySlider(switchId, 60, sliderBrightness);

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				if (clickedButton != "") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					// console.log("Clicked Button is :"+clickedButton);
					clickedButton = "";
				}

			});

	$("input[name=slider]").siblings('.ui-slider').bind('swipe',
			function(event, ui) {

				// console.log("Swipe : "+$(this).siblings('input').val());

				sliderBrightness = $(this).siblings('input').val();

				$("#slider_value").html(sliderBrightness + "%");
				$("#slider_value").css(
						"left",
						(sliderBrightness * 3 - (sliderBrightness < 10
								? 15
								: 20))
								+ "px");

				applySlider(switchId, 60, sliderBrightness);

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				if (clickedButton != "") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					// console.log("Clicked Button is :"+clickedButton);
					clickedButton = "";
				}

			});

	$(switchxmlResult).find("switchDetail").each(function() {
		if ($(this).find("id").text() == switchId) {
			var selectedSwitchName = $(this).children("name").text();
			var selectedSwitchBgt = $(this).children("currentLightLevel")
					.text();
			$('#switchName').text(selectedSwitchName);
			$("input[name=slider]").val(selectedSwitchBgt).slider("refresh");

			$("#slider_value").html(selectedSwitchBgt + "%");
			$("#slider_value")
					.css(
							"left",
							(selectedSwitchBgt * 3 - (selectedSwitchBgt < 10
									? 15
									: 20))
									+ "px");

			var defaultScenes = "";
			var attachedScenes = "";
			var noOfattachedScenes = 0;

			// For Auto Switch sceneid is null and brightness is 101		

			$('#autoButtonga').bind("click", function() {	

				applyAutoForSwitch(switchId);
			});
			
			$('#dimButtonga').bind("click", function() {
				//Call dim url		
				//console.log("before dim call");
				applyDimForSwitch(switchId);
				//console.log("after dim call");
				
			});
			
			$('#brightButtonga').bind("click", function() {				
				//Call brighten url
				//console.log("before bright call");
				applyBrightForSwitch(switchId);			
				//console.log("after bright call");
			});


			$(this).find("scene").each(function() {
				var selectedSceneName = $(this).children("scenename").text();
				var selectedSceneId = $(this).children("sceneid").text();
				var selectedSceneOrder = $(this).children("sceneorder").text();
				console.log("Scene Order : "+selectedSceneOrder);

				// For Switches other than Auto brightness is null
				if (selectedSceneName == "All On") {
					console.log("Adding +++"+selectedSceneOrder);
					noOfattachedScenes++;
					// 102 (for picking the light level from the respective
					// scene+switch combo)	
					
					attachedScenes += '<li><a data-role="button" id="'
						+ selectedSceneId
						+ '" data-icon="arrow-d" onclick= "applyAttachedSceneForSwitch('
						+ switchId
						+ ','
						+ selectedSceneId
						+ ','
						+ selectedSceneOrder
						+ ',102)"><img src="js/images/grey16x16.png" class="ui-li-icon" >'
						+ selectedSceneName + '</a></li>';

					

				} else if (selectedSceneName == "All Off") {
					console.log("Adding");
					noOfattachedScenes++;
					// 102 (for picking the light level from the respective
					// scene+switch combo)
					attachedScenes += '<li><a data-role="button" id="'
						+ selectedSceneId
						+ '" data-icon="arrow-d" onclick= "applyAttachedSceneForSwitch('
						+ switchId
						+ ','
						+ selectedSceneId
						+ ','
						+ selectedSceneOrder
						+ ',102)"><img src="js/images/grey16x16.png" class="ui-li-icon" >'
						+ selectedSceneName + '</a></li>';			
					
					
				} else {
					console.log("Adding can call");
					noOfattachedScenes++;
					// 102 (for picking the light level from the respective
					// scene+switch combo)
					attachedScenes += '<li><a data-role="button" id="'
						+ selectedSceneId
						+ '" data-icon="arrow-d" onclick= "applyAttachedSceneForSwitch('
						+ switchId
						+ ','
						+ selectedSceneId
						+ ','
						+ selectedSceneOrder
						+ ',102)"><img src="js/images/grey16x16.png" class="ui-li-icon" >'
						+ selectedSceneName + '</a></li>';
				}
			});


			if (noOfattachedScenes > 0) {
				attachedScenes = '<li data-role="list-divider">Scenes</li>'
						+ attachedScenes;
				$('#attachedSceneListga').append(attachedScenes);
				$('#attachedSceneListga').listview('refresh');
			}
		}

	});

		// $.mobile.hidePageLoadingMsg();
	}
});

function getUrlVars() {
	var vars = [], hash;
	var hashes = window.location.href.slice(window.location.href.indexOf('?')
			+ 1).split('&');
	for (var i = 0; i < hashes.length; i++) {
		hash = hashes[i].split('=');
		vars.push(hash[0]);
		vars[hash[0]] = hash[1];
	}
	return vars;
}
