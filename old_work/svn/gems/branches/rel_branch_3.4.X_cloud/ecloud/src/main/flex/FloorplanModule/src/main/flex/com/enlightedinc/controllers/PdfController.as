package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.components.PanComponent;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.events.IOErrorEvent;
	import flash.events.MouseEvent;
	import flash.net.FileReference;
	import flash.utils.ByteArray;
	
	import mx.controls.Alert;
	import mx.controls.Image;
	
	import org.alivepdf.colors.RGBColor;
	import org.alivepdf.display.Display;
	import org.alivepdf.fonts.FontFamily;
	import org.alivepdf.fonts.Style;
	import org.alivepdf.images.ImageFormat;
	import org.alivepdf.images.ResizeMode;
	import org.alivepdf.layout.Layout;
	import org.alivepdf.layout.Orientation;
	import org.alivepdf.layout.Size;
	import org.alivepdf.layout.Unit;
	import org.alivepdf.pdf.PDF;
	import org.alivepdf.saving.Method;

	public class PdfController
	{
		
		public function PdfController()
		{
		}
		
		public var fpModel:FPModel;
		
		public var panCanvas:PanComponent;
		
		public var img:Image;
		
		public function saveAsPdf(e:MouseEvent):void
		{
			var printPDF:PDF = new PDF( Orientation.LANDSCAPE, Unit.MM, Size.LETTER );
			var printFileName :String ="";
			if(fpModel.m_propertyType == Constants.FLOOR || fpModel.m_propertyType == Constants.AREA)
			{
				var plotChart:PanComponent = null;
				plotChart = panCanvas;
				var height:Number = img.measuredHeight;
				var width:Number = img.measuredWidth;
				var factor:Number = GlobalUtils.GetFactorValue(height, width);
				var strLocation:String = "";
				if(fpModel.m_propertyMode == Constants.FLOORPLAN)
				{
					strLocation = "FloorPlan";
					printFileName = "FloorPlan.pdf";
				}else
				{
					strLocation = "Outage Report ";
					printFileName = "Outage_Report.pdf";
				}
				printPDF.addPage();
				printPDF.setDisplayMode(Display.REAL); 
				printPDF.setFont(FontFamily.ARIAL, Style.NORMAL, 10);
				printPDF.textStyle(new RGBColor (0x000000));
				printPDF.addText(strLocation,120,20);
				printPDF.addImage(plotChart,5 , 24, plotChart.width/factor, plotChart.height/factor, ImageFormat.JPG,100,1,ResizeMode.FIT_TO_PAGE);
				
			}
			printPDF.setDisplayMode( Display.FULL_PAGE, Layout.SINGLE_PAGE);
			
			var fileRefObj:FileReference = new FileReference();
			fileRefObj.addEventListener(IOErrorEvent.IO_ERROR,ioErrorHander,false,0,true);
			var gridBytesArray:ByteArray = printPDF.save(Method.LOCAL);
			fileRefObj.save(gridBytesArray, printFileName);
		}
		
		private function ioErrorHander(e:IOErrorEvent):void
		{
			Alert.show("Error Occured while saving, Please close file if open any");
		}
	}
}