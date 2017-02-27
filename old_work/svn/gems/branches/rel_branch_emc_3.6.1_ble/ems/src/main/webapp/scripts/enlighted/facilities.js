//function to detect browser
//how to use e.g. if ($.browser.mozilla) {}
(function($) {     
	var userAgent = navigator.userAgent.toLowerCase();      
	$.browser = {         
		version: (userAgent.match( /.+(?:rv|it|ra|ie)[\/: ]([\d.]+)/ ) || [0,'0'])[1],        
		safari: /webkit/.test( userAgent ),         
		opera: /opera/.test( userAgent ),         
		msie: /msie/.test( userAgent ) && !/opera/.test( userAgent ),         
		mozilla: /mozilla/.test( userAgent ) && !/(compatible|webkit)/.test( userAgent )     
	};  
})(jQuery);  