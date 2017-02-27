package com.enlightedinc.components
{
	import spark.components.SkinnableContainer;

	public class GroupBox extends SkinnableContainer
	{
	   private var _label : String; 
	   public function GroupBox() 
	   { 
		    super(); 
	   } 
	   public function get label():String 
	   { 
		    return _label; 
	   } 
	   public function set label(value:String):void 
	   { 
		    _label = value; 
	   } 
	}
}