package com.enlightedinc.common
{
	import mx.validators.ValidationResult;
	import mx.validators.Validator;
	/**
	 * The IPValidator class is used to validate IP Address range
	 * 
	 * @author Sharad K Mahajan Jan 10, 2012
	 */	
	public class IPValidator extends Validator
	{
		public function IPValidator()
		{
			super();
		}
		private var results:Array;
		
		// The doValidation() method.
		override protected function doValidation(value:Object):Array {
			results = [];
			results = super.doValidation(value);        
			if (results.length > 0)
			{
				return results;
			}
			var theStringValue:String = String(value);
			if (theStringValue.length == 0)
			{
				results.push(new ValidationResult(true, null, "NaN", "IP Address cannot be empty."));
				return results;
			}
			if (theStringValue.length < 7)
			{
				results.push(new ValidationResult(true, null, "NaN", "Your input does not match IP Address Requirements. \n 1) Characters and symbols are not allowed \n 2) Numbers should be between 0 and 255. \n 3) Wildcard ('*') allowed only at the end of the IP Address. \n Allowed IP Address examples : 9.9.9.9, 9.9.9.*, etc."));
				return results;
			}
			if ((theStringValue.indexOf('.') == -1) ) 
			{
				results.push(new ValidationResult(true, null, "NaN", "No '.' exists in your IP address.")); 
				return results; 
			} 
			var ipArr:Array = theStringValue.split('.'); 
			if (ipArr.length != 4) 
			{ 
				results.push(new ValidationResult(true, null, "NaN", "There should be 3, non-consecutive '.'s in IP address.")); 
				return results;
			}
			if(ipArr[0] == 0)
			{
				results.push(new ValidationResult(true, null, "NaN", "IP Address cannot start with a '0'.")); 
				return results;
			}
			var i:int;
			for(i=0;i<ipArr.length;i++)
			{
				var a:Number = ipArr[i];
				if(i==3 && ipArr[i]=='*')
					return results;
				else if(isNaN(a) || (ipArr[i] < 0)|| (ipArr[i] > 255))
				{
					results.push(new ValidationResult(true, null, "NaN", "1) Characters and symbols are not allowed \n 2) Numbers should be between 0 and 255. \n 3) Wildcard ('*') allowed only at the end of the IP Address.")); 
					return results;
				} 
			}
			return results;
		}
	}
}