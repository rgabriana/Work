package com.enlightedinc.components
{
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.controls.Button;
	import mx.controls.Label;
	import mx.core.UIComponent;
	/**
	 * The BusyStateManager class is used to show loading status for individual custom components
	 * 
	 * @author Sharad K Mahajan Sep 23, 2011
	 */	
	public class BusyStateManager extends EventDispatcher
	{
		private var availableComponents:Dictionary = new Dictionary(true);
		
		private static var instance : BusyStateManager; 
		
		public static function getInstance():BusyStateManager  
		{
			if ( instance == null ) 
			{
				instance = new BusyStateManager();
			}
			return instance;
		}
		/**
		 * Constructor
		 */
		public function BusyStateManager() 
		{
			if ( instance != null )
			{
				throw new Error("Cant Instantiate BusyStateManager. " + 
					"Call BusyStateManager.getInstance()");
				return;
			}
		}
		
		public function registerComponent(component:UIComponent):void {
			availableComponents[component.id] = component;
		}
		/**
		 * @public
		 * executes when user wants to show Loading cursor for given components. 
		 */
		public function showAsBusy(key:String):void {
			var value:UIComponent = availableComponents[key];
			value.enabled=false;
			if(value.getChildByName("image"))
			//(value.getChildByName("image") as Image).visible=true;
			(value.getChildByName("image") as LoadingLabel).visible=true;
		}
		/**
		 * @public
		 * executes when user wants to Hide Loading cursor for given components. 
		 */
		public function removeBusyCursor(key:String):void {
			var value:UIComponent = availableComponents[key];
			value.enabled=true;
			if(value.getChildByName("image"))
			//(value.getChildByName("image") as Image).visible=false;
			(value.getChildByName("image") as LoadingLabel).visible=false;
		}
	}

}