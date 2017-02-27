package com.enlightedinc.components.itemrenderer
{
	import com.enlightedinc.events.EnergySummaryEvent;
	import com.enlightedinc.assets.images.Images;
	
	import flash.events.MouseEvent;
	
	import mx.collections.*;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.treeClasses.*;

	[Event(type="EnergySummaryEvent", name="openurl")]
	
	public class CustomTreeItemRenderer extends TreeItemRenderer
	{
		private var customItem:Button;
		
		public function CustomTreeItemRenderer()
		{
			super();
		}
		private function openURLEventHandler(event:MouseEvent):void
		{
			var e:EnergySummaryEvent = new EnergySummaryEvent(EnergySummaryEvent.OPENURL);
			dispatchEvent(e);
		}
		override protected function createChildren():void
		{
			super.createChildren();
			customItem=new Button();
			customItem.setStyle("upSkin", Images.PopUpArrow);
			customItem.setStyle("overSkin", Images.PopUpArrow);
			customItem.setStyle("downSkin", Images.PopUpArrow);
			customItem.buttonMode = true;
			customItem.width=15;
			customItem.height=15;
			customItem.addEventListener(MouseEvent.CLICK,openURLEventHandler,false,0,true);
			this.addChild(customItem);
		}
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			var treeListData:TreeListData=TreeListData(listData);
			super.updateDisplayList(unscaledWidth,unscaledHeight);
			if(treeListData.hasChildren)
			{
				this.setStyle("fontWeight","bold");
				this.label.text = this.label.text.toUpperCase();
				customItem.visible=false;
			}
			else{
				this.setStyle("fontWeight","normal");
				icon.visible=true;
				customItem.visible=true;
				customItem.width=15;
				customItem.height=15;
				customItem.x=label.textWidth+label.x+10;
			}
		}
	}
}
