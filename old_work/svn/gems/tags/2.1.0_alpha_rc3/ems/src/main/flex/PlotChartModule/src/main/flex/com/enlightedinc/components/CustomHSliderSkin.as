package com.enlightedinc.components
{
	import spark.components.Label;
	import spark.skins.spark.HSliderSkin;

	public class CustomHSliderSkin extends HSliderSkin
	{
		public function CustomHSliderSkin()
		{
			var rightLabel:Label = new Label();
			rightLabel.right = 0;
			rightLabel.top = -10;
			rightLabel.text = "100";
			this.addElementAt(rightLabel, 0);
			
			var leftLabel:Label = new Label();
			leftLabel.left = 0;
			leftLabel.top = -10;
			leftLabel.text = "-100";
			this.addElementAt(leftLabel, 0);
		}
	}
}