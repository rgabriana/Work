package com.enlightedinc.components.renderers
{
import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.text.TextField;
import mx.controls.CheckBox;
import mx.controls.dataGridClasses.DataGridListData;
import mx.controls.listClasses.ListBase;

/** 
 *  The Renderer.
 */
public class CheckBoxHeaderRenderer extends CheckBox
{

	public function CheckBoxHeaderRenderer()
	{
		focusEnabled = false;
	}

	override public function set data(value:Object):void
	{
		invalidateProperties();
	}
	
    private var addedListener:Boolean = false;
    private var partiallySelected:Boolean = false;

	override protected function commitProperties():void
	{
		super.commitProperties();
		if(owner!=null && ListBase(owner).dataProvider!=null)
		{
			if (owner is ListBase)
	        {
	            if (!addedListener)
	            {
	                addedListener = true;
	                owner.addEventListener("valueCommit", owner_changeHandler, false, 0, true);
	                owner.addEventListener("change", owner_changeHandler, false, 0, true);
	            }
	
	            if (ListBase(owner).dataProvider.length == ListBase(owner).selectedItems.length)
	            {
				    selected = true;
	                partiallySelected = false;
	            }
	            else if (ListBase(owner).selectedItems.length == 0)
	            {
	                selected = false;
	                partiallySelected = false;
	            }
	            else
	            {
	                selected = false;
	                partiallySelected = true;
	            }
	            invalidateDisplayList();
	        }
		}
	}

	/* eat keyboard events, the underlying list will handle them */
	override protected function keyDownHandler(event:KeyboardEvent):void
	{
	}

	/* eat keyboard events, the underlying list will handle them */
	override protected function keyUpHandler(event:KeyboardEvent):void
	{
	}

	override protected function clickHandler(event:MouseEvent):void
	{
        if (selected)
        {
            // uncheck everything
            ListBase(owner).selectedIndex = -1;
        }
        else
        {
            var n:int = ListBase(owner).dataProvider.length;
            var arr:Array = [];
            for (var i:int = i; i < n; i++)
                arr.push(i);
            ListBase(owner).selectedIndices = arr;
        }
	}

	/* center the checkbox if we're in a datagrid */
	override protected function updateDisplayList(w:Number, h:Number):void
	{
		super.updateDisplayList(w, h);

        graphics.clear();

		if (listData is DataGridListData)
		{
			var n:int = numChildren;
			for (var i:int = 0; i < n; i++)
			{
				var c:DisplayObject = getChildAt(i);
				if (!(c is TextField))
				{
					c.x = (w - c.width) / 2;
					c.y = 0;
                    c.alpha = 1;
                    if (partiallySelected)
                    {
                        graphics.beginFill(0x000000);
                        graphics.drawRect(c.x, c.y, c.width, c.height);
                        graphics.endFill();
                        c.alpha = 0.7;
                    }
				}
			}
		}
	}

    private function owner_changeHandler(event:Event):void
    {
        invalidateProperties();
    }

}

}