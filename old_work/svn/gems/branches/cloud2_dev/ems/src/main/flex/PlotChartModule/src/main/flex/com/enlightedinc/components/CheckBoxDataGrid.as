package com.enlightedinc.components
{
import flash.display.Sprite;
import flash.events.KeyboardEvent;
import mx.controls.CheckBox;
import mx.controls.DataGrid;
import mx.controls.listClasses.IListItemRenderer;

/** 
 *  DataGrid that uses checkboxes for multiple selection
 */
public class CheckBoxDataGrid extends DataGrid
{

	override protected function selectItem(item:IListItemRenderer,
                                  shiftKey:Boolean, ctrlKey:Boolean,
                                  transition:Boolean = true):Boolean
	{
		// only run selection code if a checkbox was hit and always
		// pretend we're using ctrl selection
		if (item is CheckBox)
			return super.selectItem(item, false, true, transition);
		return false;
	}

	// turn off selection indicator
    override protected function drawSelectionIndicator(
                                indicator:Sprite, x:Number, y:Number,
                                width:Number, height:Number, color:uint,
                                itemRenderer:IListItemRenderer):void
    {
	}

	// whenever we draw the renderer, make sure we re-eval the checked state
    override protected function drawItem(item:IListItemRenderer,
                                selected:Boolean = false,
                                highlighted:Boolean = false,
                                caret:Boolean = false,
                                transition:Boolean = false):void
    {
		CheckBox(item).invalidateProperties();
		if(selected)
			highlighted=true;
		super.drawItem(item, selected, highlighted, caret, transition);
	}

	// fake all keyboard interaction as if it had the ctrl key down
	override protected function keyDownHandler(event:KeyboardEvent):void
	{
		// this is technically illegal, but works
		event.ctrlKey = true;
		event.shiftKey = false;
		super.keyDownHandler(event);
	}

}

}