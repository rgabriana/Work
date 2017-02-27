/* This function called when Logout button is clicked */
function gems3logout() {
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


function setUrls() {
	//console.log("BASEURL used : "+ window.localStorage.getItem("serverBaseUrl"));
	// webservice used Switchservice
	switchListServerUrl = window.localStorage.getItem("serverBaseUrl");
	switchListServerUrl += "ems/services/org/switch/list/user/";
	switchListServerUrl += window.localStorage.getItem("userId");

	//console.log("Refreshing List for UserID : "+ window.localStorage.getItem("userId"));
	//console.log("switchListServerUrl used : " + switchListServerUrl);

}

/* This function is called when refresh button is clicked */
function gems3RefreshSwitchList() {
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
						
						
						switchListString += "<li><a href=\"scenes.html?id="
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
						gems3logout();
						
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

function applyAttachedScene(switchId, sceneId, brigtnessValue) {

	if (clickedScene != "" && clickedScene != sceneId) {
		$("#" + clickedScene + "  img.ui-li-icon").attr("src",
				"js/images/grey16x16.png");
	}

	if (clickedScene != sceneId) {
		$("#" + sceneId + "  img.ui-li-icon").attr("src",
				"js/images/green16x16.png");
	}

	clickedScene = sceneId;

	if (clickedButton != "") {
		$("#" + clickedButton + "  span.ui-icon").addClass("ui-icon-scene-off")
				.removeClass("ui-icon-scene-on");
		//console.log("Clicked Button is :" + clickedButton);
		clickedButton = "";
	}

	applyScene(switchId, sceneId, brigtnessValue);

}

function applyScene(switchId, sceneId, brigtnessValue) {

	applySwitchId = switchId;
	if (sceneId == null) {
		applySceneId = "";
	} else
		applySceneId = sceneId;
	var time = 60;
	if (brigtnessValue == null) {
		percentage = "";
	} else {
		percentage = brigtnessValue;
	}
	applySceneServerUrl = window.localStorage.getItem("serverBaseUrl");
	applySceneServerUrl += "ems/services/org/switch/op/dim/switch/"
			+ applySwitchId + "/scene/" + applySceneId + "/" + percentage + "/"
			+ time
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
					//console.log("Sucess in  gems3 apply scene webservice");


				},
				error : function(jqXHR, textStatus, errorThrown) {
					//console.log('error');
					//console.log(errorThrown);
					//console.log(jqXHR);
					//console.log(textStatus);
					//console.log("Error while communicating to gems3 apply scene webservice")
					
					if(errorThrown == "Unauthorized"){
						
						$('#errorMessage').text("Session Expired.Please login");
						gems3logout();
						
				}
				}

			});

}

function applySlider(switchId, slidertime, brigtnessValue) {

	applySwitchId = switchId;
	percentage = brigtnessValue;
	time = slidertime;
	applySliderDimCmdServerUrl = window.localStorage.getItem("serverBaseUrl");
	applySliderDimCmdServerUrl += "ems/services/org/switch/op/dim/switch/"
			+ applySwitchId + "/" + percentage + "/" + time

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
						gems3logout();
						
				}
				}

			});

}

var sliderBrightness = "";

var clickedButton = "";

/* Event Handler when scenepage is Loaded */
$('#scenePage').live('pageshow', function(event) {

	if(version == 3){
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

			defaultScenes += '<a data-role="button" onclick= "applyScene('
					+ switchId + ',null,101)">Auto</a>';

			$('#autoButton').bind("click", function() {

				if (clickedButton != "" && clickedButton != "autoButton") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					// console.log("Clicked Button is empty or u clicked plus
					// again :"+clickedButton);
				}

				if (clickedButton != "autoButton") {

					$('#autoButton span.ui-icon').addClass("ui-icon-scene-on")
							.removeClass("ui-icon-scene-off");
				}

				clickedButton = "autoButton";

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				applySlider(switchId, 60, 101);
			});

			$(this).find("scene").each(function() {
				var selectedSceneName = $(this).children("scenename").text();
				var selectedSceneId = $(this).children("sceneid").text();

				// For Switches other than Auto brightness is null
				if (selectedSceneName == "All On") {
					defaultScenes += '<a data-role="button"  id="'
							+ selectedSceneId + '" onclick= "applyScene('
							+ switchId + ',' + selectedSceneId
							+ ',null)">On</a>';

					$('#allOnButton').bind("click", function() {

						if (clickedButton != ""
								&& clickedButton != "allOnButton") {
							$("#" + clickedButton + "  span.ui-icon")
									.addClass("ui-icon-scene-off")
									.removeClass("ui-icon-scene-on");
							// console.log("Clicked Button is :"+clickedButton);
						}

						if (clickedButton != "allOnButton") {
							$('#allOnButton span.ui-icon')
									.addClass("ui-icon-scene-on")
									.removeClass("ui-icon-scene-off");
						}

						clickedButton = "allOnButton";

						if (clickedScene != "") {
							$("#" + clickedScene + "  img.ui-li-icon").attr(
									"src", "js/images/grey16x16.png");
							clickedScene = "";
						}

						applySlider(switchId, 60, 100);
					});

				} else if (selectedSceneName == "All Off") {
					defaultScenes += '<a data-role="button"  id="'
							+ selectedSceneId + '" onclick= "applyScene('
							+ switchId + ',' + selectedSceneId
							+ ',null)">Off</a>';

					$('#allOffButton').bind("click", function() {

						if (clickedButton != ""
								&& clickedButton != "allOffButton") {
							$("#" + clickedButton + "  span.ui-icon")
									.addClass("ui-icon-scene-off")
									.removeClass("ui-icon-scene-on");
							// console.log("Clicked Button is :"+clickedButton);
						}

						if (clickedButton != "allOffButton") {
							$('#allOffButton span.ui-icon')
									.addClass("ui-icon-scene-on")
									.removeClass("ui-icon-scene-off");
						}

						clickedButton = "allOffButton";

						if (clickedScene != "") {
							$("#" + clickedScene + "  img.ui-li-icon").attr(
									"src", "js/images/grey16x16.png");
							clickedScene = "";
						}

						applySlider(switchId, 60, 0);
					});

				} else {
					noOfattachedScenes++;
					// 102 (for picking the light level from the respective
					// scene+switch combo)
					attachedScenes += '<li><a data-role="button" id="'
							+ selectedSceneId
							+ '" data-icon="arrow-d" onclick= "applyAttachedScene('
							+ switchId
							+ ','
							+ selectedSceneId
							+ ',102)"><img src="js/images/grey16x16.png" class="ui-li-icon" >'
							+ selectedSceneName + '</a></li>';
				}
			});

			$('#scenesList').empty().append(defaultScenes);
			$('#scenesList').controlgroup('refresh');

			if (noOfattachedScenes > 0) {
				attachedScenes = '<li data-role="list-divider">Scenes</li>'
						+ attachedScenes;
				$('#attachedSceneList').append(attachedScenes);
				$('#attachedSceneList').listview('refresh');
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
