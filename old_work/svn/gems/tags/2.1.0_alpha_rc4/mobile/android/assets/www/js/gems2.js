
/* This function called when Logout button is clicked */
function gems2logout() {
	// $("[type='submit']").button('enable');
	$('#errorMessage').text("");
	$.mobile.changePage("index.html", {
				transition : "fade"
			});
	$("[type='submit']").button('enable');
	if ($('input[type=checkbox]').is(':checked') == false) {

		document.myform.username.value = "";

		document.myform.pword.value = "";
	}
//	window.localStorage.setItem("currentSessionId", "");

}

var getSwitchXml = "";


/* This function is called when refresh button is clicked */
function gems2RefreshSwitchList() {
	getSwitchXml = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>2</messageType><sessionId>"
		+ window.localStorage.getItem("currentSessionId")
		+ "</sessionId><body><getSwitchesRequest /></body></request></root>";
	//console.log(getSwitchXml);
	//console.log("SESSION ID" +  window.localStorage.getItem("currentSessionId")) ;
	var switchListString = "";
	/*console.log("URL used to get switch list"
			+ window.localStorage.getItem("serverUrl"));*/

	$.ajax({
				type : "POST",
				cache : false,
				url : window.localStorage.getItem("serverUrl"),
				data : getSwitchXml,
				contentType : "text/xml",
				dataType : "xml",
				success : function switchSucess(xml) {

					// console.log("In switchSucess Start "+xml);
					$(xml).find("response").each(function() {
						if ($(this).children("errorCode").text() == 003) {
							/*console.log("Error in login respnse"
									+ $(this).children("errorMessage").text());
							console.log("Session ID "
									+ window.localStorage
											.getItem("currentSessionId"))
							console.log("Server Url used "
									+ window.localStorage.getItem("serverUrl"))
							console
									.log(window.localStorage
											.getItem("srvipkey")
											+ " "
											+ window.localStorage
													.getItem("srvportkey"));
							console.log("( print only if remember me check box is on ) Username:- "
									+ window.localStorage
											.getItem("usernamekey")
									+ "Password:- "
									+ window.localStorage.getItem("pwordkey"))*/
							$.mobile.changePage("index.html", {
										transition : "fade"
									});
							$('#settingsErrorMessage').text($(this)
									.children("errorMessage").text());

						}

					});

					/*console.log("Login Sucess")
					console.log("Session ID "
							+ window.localStorage.getItem("currentSessionId"))
					console.log("Server Url used "
							+ window.localStorage.getItem("serverUrl"))
					console.log(window.localStorage.getItem("srvipkey") + " "
							+ window.localStorage.getItem("srvportkey"));
					console.log("  ( print only if remember me check box is on ) Username:- "
							+ window.localStorage.getItem("usernamekey")
							+ "Password:- "
							+ window.localStorage.getItem("pwordkey"))*/

					switchxmlResult = xml;
					
					console.log("gems2RefreshSwitchList : "+(new XMLSerializer()).serializeToString(switchxmlResult) );
					
					
					$(xml).find("switch").each(function() {

						info = $(this).children("name").text();
						var switchId = $(this).children("id").text();

						//console.log("Switch Name :" + info);
						var noOfScenes = $(this).find("scene").length;
						//console.log("Total no of Scenes : "+noOfScenes);
						noOfScenes -= 2; // Don't consider "All On" & "All
						// Off" as attached scenes

						// switchListString += "<li><a href=\"scenes.html?id="
						// + switchId + "\">" + info
						// + "<span class=\"ui-li-count\">" + noOfScenes
						// + "</span>" + "</a></li>";
						
						/*if(noOfScenes == 0){
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
						
						//console.log(switchListString);

					});
					
					if( switchListString == ""){
				  		switchListString = "There are no Switches Configured";
				  	}
					
					$('#switchList').empty().append(switchListString);
					$('#switchList').listview('refresh');
					// console.log("In switchSucess End "+xml);

				}

			});

}

/*
 * Below is the code for Gems 2.0 verion to handle Scene activity and make calls
 * to change Scene and do other on/off related things.
 */

var currentSessionId = "";

var applySwitchId = "";

var applySceneId = "";

var applyBrightnessValue = "";

var clickedScene = "";

function gems2applyAttachedScene(switchId, sceneId, brigtnessValue) {

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
		//console.log("Clicked Button is :"+clickedButton);
		clickedButton = "";
	}

	gems2applyScene(switchId, sceneId, brigtnessValue);

}

function gems2applyScene(switchId, sceneId, brigtnessValue) {

	applySwitchId = switchId;

	if (sceneId == null) {
		applySceneId = "";
	} else {
		applySceneId = sceneId;

	}

	if (brigtnessValue == null) {
		applyBrightnessValue = "";
	} else {
		applyBrightnessValue = brigtnessValue;
	}

	var setSceneRequestXml = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root><request><messageType>3</messageType><sessionId>"
			+ currentSessionId
			+ "</sessionId><body><setSwitchRequest><switch><id>"
			+ applySwitchId
			+ "</id><state>1</state><brightness>"
			+ applyBrightnessValue
			+ "</brightness><scenes><scene><id>"
			+ applySceneId
			+ "</id></scene></scenes></switch></setSwitchRequest></body></request></root>";

	$.ajax({
				type : "POST",
				url : serverUrl,
				data : setSceneRequestXml,
				contentType : "text/xml",
				dataType : "xml",
				success : function sceneSucess(xml) {

					/*
					 * $(xml).find("setSwitchResponse").each(function() {
					 * if($(this).children("result").text() == 1 ){
					 * console.log("The Scene is applied"); }});
					 */

					$(xml).find("response").each(function() {
								if ($(this).children("errorCode").text() == 003) {
									// console.log($(this).children("errorMessage").text());
									$.mobile.changePage("index.html", {
												transition : "fade"
											});
									$('#errorMessage').text("");
								}

							});

				}

			});

}

var sliderBrightness = "";

var clickedButton = "";

/* Event Handler when scenepage is Loaded */
$('#scenePage').live('pageshow', function(event) {

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

				//console.log("Tap : "+$(this).siblings('input').val());
				sliderBrightness = $(this).siblings('input').val();

				$("#slider_value").html(sliderBrightness + "%");
				$("#slider_value").css(
						"left",
						(sliderBrightness * 3 - (sliderBrightness < 10
								? 15
								: 20))
								+ "px");

				gems2applyScene(switchId, null, sliderBrightness);

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				if (clickedButton != "") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					//console.log("Clicked Button is :"+clickedButton);
					clickedButton = "";
				}

			});

	$("input[name=slider]").siblings('.ui-slider').bind("taphold",
			function(event, ui) {

				//console.log("Taphold : "+$(this).siblings('input').val());
				sliderBrightness = $(this).siblings('input').val();

				$("#slider_value").html(sliderBrightness + "%");
				$("#slider_value").css(
						"left",
						(sliderBrightness * 3 - (sliderBrightness < 10
								? 15
								: 20))
								+ "px");

				gems2applyScene(switchId, null, sliderBrightness);

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				if (clickedButton != "") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					//console.log("Clicked Button is :"+clickedButton);
					clickedButton = "";
				}

			});

	$("input[name=slider]").siblings('.ui-slider').bind('swipe',
			function(event, ui) {

				//console.log("Swipe : "+$(this).siblings('input').val());

				sliderBrightness = $(this).siblings('input').val();

				$("#slider_value").html(sliderBrightness + "%");
				$("#slider_value").css(
						"left",
						(sliderBrightness * 3 - (sliderBrightness < 10
								? 15
								: 20))
								+ "px");

				gems2applyScene(switchId, null, sliderBrightness);

				if (clickedScene != "") {
					$("#" + clickedScene + "  img.ui-li-icon").attr("src",
							"js/images/grey16x16.png");
					clickedScene = "";
				}

				if (clickedButton != "") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					//console.log("Clicked Button is :"+clickedButton);
					clickedButton = "";
				}

			});

	$(switchxmlResult).find("switch").each(function() {
		if ($(this).children("id").text() == switchId) {
			var selectedSwitchName = $(this).children("name").text();
			var selectedSwitchBgt = $(this).children("brightness").text();
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

			defaultScenes += '<a data-role="button" onclick= "gems2applyScene('
					+ switchId + ',null,101)">Auto</a>';

			$('#autoButton').bind("click", function() {

				if (clickedButton != "" && clickedButton != "autoButton") {
					$("#" + clickedButton + "  span.ui-icon")
							.addClass("ui-icon-scene-off")
							.removeClass("ui-icon-scene-on");
					//console.log("Clicked Button is empty or u clicked plus again :"+clickedButton);
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

				gems2applyScene(switchId, null, 101);
			});

			$(this).find("scene").each(function() {
				var selectedSceneName = $(this).children("name").text();
				var selectedSceneId = $(this).children("id").text();

				// For Switches other than Auto brightness is null
				if (selectedSceneName == "All On") {
					defaultScenes += '<a data-role="button"  id="'
							+ selectedSceneId + '" onclick= "gems2applyScene('
							+ switchId + ',' + selectedSceneId
							+ ',null)">On</a>';

					$('#allOnButton').bind("click", function() {

						if (clickedButton != ""
								&& clickedButton != "allOnButton") {
							$("#" + clickedButton + "  span.ui-icon")
									.addClass("ui-icon-scene-off")
									.removeClass("ui-icon-scene-on");
							//console.log("Clicked Button is :"+clickedButton);
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

						gems2applyScene(switchId, selectedSceneId, null);
					});

				} else if (selectedSceneName == "All Off") {
					defaultScenes += '<a data-role="button"  id="'
							+ selectedSceneId + '" onclick= "gems2applyScene('
							+ switchId + ',' + selectedSceneId
							+ ',null)">Off</a>';

					$('#allOffButton').bind("click", function() {

						if (clickedButton != ""
								&& clickedButton != "allOffButton") {
							$("#" + clickedButton + "  span.ui-icon")
									.addClass("ui-icon-scene-off")
									.removeClass("ui-icon-scene-on");
							//console.log("Clicked Button is :"+clickedButton);
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

						gems2applyScene(switchId, selectedSceneId, null);
					});

				} else {
					noOfattachedScenes++;
					attachedScenes += '<li><a data-role="button" id="'
							+ selectedSceneId
							+ '" data-icon="arrow-d" onclick= "gems2applyAttachedScene('
							+ switchId
							+ ','
							+ selectedSceneId
							+ ',null)"><img src="js/images/grey16x16.png" class="ui-li-icon" >'
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
