package com.enlightedinc.assets.images
{
	public class Images
	{
		public function Images()
		{
		}
		
		[Embed(source="gems.png")]
		[Bindable]
		public static var Gems:Class;
		
		[Embed(source="popUpArrow.png")]
		[Bindable]
		public static var PopUpArrow:Class;
		
		[Embed(source="refresh.png")]
		[Bindable]
		public static var Refresh:Class;
	}
}