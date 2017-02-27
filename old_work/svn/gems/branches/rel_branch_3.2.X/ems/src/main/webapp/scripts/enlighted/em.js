/**
 * Enlighted EM javascript like adding generic jquery validation or any common resource to be accessed in the templates.
 * 
 */
/**
	 * 
	 * Password Regular Expression Pattern

		((?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})
		Description
		
		(			# Start of group
		  (?=.*\d)		#   must contains one digit from 0-9
		  (?=.*[a-z])		#   must contains one lowercase characters
		  (?=.*[A-Z])		#   must contains one uppercase characters
		  (?=.*[@#$%])		#   must contains one special symbols in the list "@#$%"
		              .		#     match anything with previous condition checking
		                {6,20}	#        length at least 6 characters and maximum of 20	
		)			# End of group
		?= – means apply the assertion condition, meaningless by itself, always work with other combination
*/
$(document).ready(function() {
	
	// Added server side validation for the same as well.
	var patternPasswordValidate = /^.*(?=.*\d)(?=.*[a-zA-Z])(?=.*[@#$%&\-_]).*$/;
	jQuery.validator.addMethod("passwordValidate",
			function(value, element) {
			    return patternPasswordValidate.test(value);
			}, "Password policy not matching ");
});
