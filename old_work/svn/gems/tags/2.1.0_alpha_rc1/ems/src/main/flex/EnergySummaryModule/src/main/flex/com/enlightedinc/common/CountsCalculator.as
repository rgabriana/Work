/**
 * @author Sharad.Mahajan Feb 14, 2012 
 * @Purpose
 */
package com.enlightedinc.common
{
	import com.enlightedinc.events.CurrentSavingChartUpdate;
	
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	
	[Event(name="chartupdate", type="com.enlightedinc.events.CurrentSavingChartUpdate")]
	public class CountsCalculator extends EventDispatcher
	{
		/**
		 * The static variable for CountsCalculator to implement singleton.
		 * @default
		 */
		private static var countsCalculator:CountsCalculator;
		/**
		 * varibles Binded on UI.
		 * @default
		 */
		[Bindable]
		public var savingSoFarPercVal:Number=0;
		[Bindable]
		public var chartDataCollection:ArrayCollection = new ArrayCollection();
		[Bindable]
		public var totalSavedCost:Number =0;
		[Bindable]
		public var totalSavedPower:Number=0;
		
		
		[Bindable]
		public var savingRightNowPercVal:Number=0;
		[Bindable]
		public var currentLoad:Number=0;
		[Bindable]
		public var savingMeterData:ArrayCollection = new ArrayCollection();
		
		/**
		 * Local varibles used for calculation purpose
		 * @default
		 */
		private var totalBasePower:Number=0;
		private var totalPowerUsed:Number=0;
		private var totalOccSaving:Number=0;
		private var totalAmbientSaving:Number=0;
		private var totalTaskTuneupSaving:Number=0;
		private var totalManualSaving:Number=0;
		
		
		/**
		 * The default constructor to make sure that the class can be instantiated only once.
		 * @throws Error
		 */
		public function CountsCalculator()
		{
			if (CountsCalculator.countsCalculator != null)
			{
				throw new Error("Only one CountsCalculator instance should be instantiated");
			}
		}
		
		/**
		 * Singleton implementation of the CountCalculator class.
		 * The method checks the instance frist before instantiating the CountCalculator.
		 * @return The CountCalculator
		 */
		public static function getInstance():CountsCalculator
		{
			if (countsCalculator == null)
			{
				countsCalculator=new CountsCalculator();
			}
			return countsCalculator;
		}
		
		public function calculateCounts(_dataProvider:ArrayCollection):void
		{
			flushCounts();
			if(_dataProvider!=null && _dataProvider.length>0)
			{
				var SavedCost:Number =0;
					for(var j:int=0; j<_dataProvider.length; j++)
					{
						totalSavedPower+=checkNaN(_dataProvider[j].savedpower);
						totalBasePower+=checkNaN(_dataProvider[j].basepowerused);
						SavedCost+=checkNaN(_dataProvider[j].savedcost);
						// Derive Perncentage Values of Ambient, Manual, Tasktuning, Occupancy Saving - To show series in the Stack chart 
						var object:Object = new Object();
						if(_dataProvider[j].basepowerused!=null && (!isNaN(_dataProvider[j].basepowerused)) && (_dataProvider[j].basepowerused>0))
						{
							totalAmbientSaving = (checkNaN(_dataProvider[j].ambientsaving)/checkNaN(_dataProvider[j].basepowerused))*100;
							totalOccSaving = (checkNaN(_dataProvider[j].occsaving)/checkNaN(_dataProvider[j].basepowerused))*100;
							totalTaskTuneupSaving = ((checkNaN(_dataProvider[j].tasktuneupsaving)+checkNaN(_dataProvider[j].manualsaving))/_dataProvider[j].basepowerused)*100;
							//totalManualSaving = (_dataProvider[j].manualsaving/_dataProvider[j].basepowerused)*100;
							totalPowerUsed =  (checkNaN(_dataProvider[j].powerused)/_dataProvider[j].basepowerused)*100;
						}else
						{
							totalAmbientSaving =0;
							totalOccSaving =0;
							totalTaskTuneupSaving =0
							//totalManualSaving =0;
							totalPowerUsed =0;
						}
						object.tasktuneupsaving = Math.round(totalTaskTuneupSaving);
						object.ambientsaving =Math.round(totalAmbientSaving);
						//object.manualsaving = Math.round(totalManualSaving);
						object.occsaving = Math.round(totalOccSaving);
						//object.powerused = Math.round(totalPowerUsed);
						object.powerused = 100 - (object.tasktuneupsaving + object.ambientsaving + object.occsaving);
						object.avgload=_dataProvider[j].avgload;
						object.basepowerused=_dataProvider[j].basepowerused;
						object.captureon=_dataProvider[j].captureon;
						object.cost=_dataProvider[j].cost;
						object.minload=_dataProvider[j].minload;
						object.peakload=_dataProvider[j].peakload;
						object.price=_dataProvider[j].price;
						object.savedcost=_dataProvider[j].savedcost;
						object.savedpower=_dataProvider[j].savedpower;
						chartDataCollection.addItem(object);
					}
					if(isNaN(totalSavedPower))
						totalSavedPower=0;
					
					if(isNaN(totalBasePower))
						totalBasePower = 0;
					savingSoFarPercVal = (totalSavedPower/totalBasePower)*100;
					savingSoFarPercVal= Math.round(savingSoFarPercVal) ;
					totalSavedCost = SavedCost;
			}
		}
		
		/**
		 * Method to reset all the counter.
		 * 
		 */
		public function flushCounts():void
		{
			if(chartDataCollection!=null && chartDataCollection.length>0)
			{
				chartDataCollection.removeAll();
			}
			totalSavedCost =0;
			savingSoFarPercVal=0;
			totalSavedPower=0;
			totalBasePower=0;
			totalPowerUsed=0;
			totalOccSaving=0;
			totalAmbientSaving=0;
			totalTaskTuneupSaving=0;
			totalManualSaving=0;
		}
		
		/**
		 * Calculate Current Counts
		 * 
		 */
		public function calculateCurrentCount(_dataProvider:ArrayCollection):void
		{
			flushCurrentCount();
			var currrentPowerUsed:Number =0;
			var currrentSavedPower:Number=0;
			var currentBasePower:Number =0;
			var ambientSaving:Number=0;
			var occSaving:Number=0;
			var taskTuneupSaving:Number=0;
			var powerUsed:Number=0;
			if(_dataProvider!=null && _dataProvider.length>0)
			{
				for(var j:int=0; j<_dataProvider.length; j++)
				{
					currrentPowerUsed+=checkNaN(_dataProvider[j].powerused);
					currentBasePower+=checkNaN(_dataProvider[j].basepowerused);
					//currrentSavedPower+=checkNaN(_dataProvider[j].savedpower);
					currrentSavedPower+=checkNaN(_dataProvider[j].ambientsaving) + checkNaN(_dataProvider[j].occsaving) + checkNaN(_dataProvider[j].tasktuneupsaving) + checkNaN(_dataProvider[j].manualsaving);
					
					// Derive Perncentage Values of Ambient, Manual, Tasktuning, Occupancy Saving - To show series in the Stack chart 
					var object:Object = new Object();
					if(_dataProvider[j].basepowerused!=null && (!isNaN(_dataProvider[j].basepowerused)) && (_dataProvider[j].basepowerused>0))
					{
						ambientSaving = (checkNaN(_dataProvider[j].ambientsaving)/checkNaN(_dataProvider[j].basepowerused))*100;
						occSaving = (checkNaN(_dataProvider[j].occsaving)/checkNaN(_dataProvider[j].basepowerused))*100;
						taskTuneupSaving = ((checkNaN(_dataProvider[j].tasktuneupsaving)+checkNaN(_dataProvider[j].manualsaving))/_dataProvider[j].basepowerused)*100;
						//totalManualSaving = (_dataProvider[j].manualsaving/_dataProvider[j].basepowerused)*100;
						powerUsed =  (checkNaN(_dataProvider[j].powerused)/_dataProvider[j].basepowerused)*100;
					}else
					{
						ambientSaving =0;
						occSaving =0;
						taskTuneupSaving =0
						//totalManualSaving =0;
						powerUsed =0;
					}
					object.tasktuneupsaving = Math.round(taskTuneupSaving);
					object.ambientsaving =Math.round(ambientSaving);
					object.occsaving = Math.round(occSaving);
					object.powerused = 100 - (object.tasktuneupsaving + object.ambientsaving + object.occsaving);
					object.avgload=_dataProvider[j].avgload;
					object.basepowerused=_dataProvider[j].basepowerused;
					object.captureon=_dataProvider[j].captureon;
					object.cost=_dataProvider[j].cost;
					object.minload=_dataProvider[j].minload;
					object.peakload=_dataProvider[j].peakload;
					object.price=_dataProvider[j].price;
					object.savedcost=_dataProvider[j].savedcost;
					object.savedpower=_dataProvider[j].savedpower;
					savingMeterData.addItem(object);
				}
				currentLoad = Math.round(currrentPowerUsed) ; 
				savingRightNowPercVal = (currrentSavedPower/currentBasePower)*100;
				savingRightNowPercVal= Math.round(savingRightNowPercVal) ;
				
				//Update the Current Saving Chart
				dispatchEvent(new CurrentSavingChartUpdate(CurrentSavingChartUpdate.CHART_UPDATE)); 
			
			}
		}
		public function flushCurrentCount():void
		{
			if(savingMeterData!=null && savingMeterData.length>0)
			{
				savingMeterData.removeAll();
			}
			savingRightNowPercVal =0;
			currentLoad =0;
		}
		
		public function checkNaN(value:*):Number
		{
			if(isNaN(value))
				return 0;
			else
			return value;
		}
	}
}