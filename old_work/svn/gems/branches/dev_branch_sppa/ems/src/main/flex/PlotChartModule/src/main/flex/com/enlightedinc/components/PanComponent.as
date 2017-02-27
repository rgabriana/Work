package com.enlightedinc.components
{
	import flash.events.MouseEvent;
	
	import mx.containers.Canvas;
	import mx.controls.Image;
	
	import spark.components.Button;
	
	public class PanComponent extends Canvas
	{
		private var initStageX:Number;
		private var initStageY:Number;
		private var initHorizontalScrollPosition:Number;
		private var initVerticalScrollPosition:Number;
		
		[Bindable]
		public var panningEnabled:Boolean = false;
		
		public function PanComponent()
		{
			super();
		}
		
		override protected function createChildren():void {
			super.createChildren();
			this.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
		} 
		
		private function onMouseDown(event:MouseEvent):void
		{
			if(event.target is Image || event.target is Button || event.ctrlKey || (!panningEnabled && !(event.target is PanComponent)))
				return;
			
			initStageX = event.stageX;
			initStageY = event.stageY;
			
			initHorizontalScrollPosition = this.horizontalScrollPosition;
			initVerticalScrollPosition = this.verticalScrollPosition;
			
			systemManager.addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove, true);
			systemManager.addEventListener(MouseEvent.MOUSE_UP, onMouseUp, true);
		}
		
		private function onMouseMove(event:MouseEvent):void
		{
			event.stopImmediatePropagation();
			
			this.verticalScrollPosition = initVerticalScrollPosition - (event.stageY - initStageY);
			this.horizontalScrollPosition = initHorizontalScrollPosition - (event.stageX - initStageX);
		}
		
		private function onMouseUp(event:Event):void
		{
			systemManager.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseMove, true);
			systemManager.removeEventListener(MouseEvent.MOUSE_UP, onMouseUp, true);
		}
	}
}