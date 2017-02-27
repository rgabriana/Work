/**
 * @author Sharad.Mahajan Feb 14, 2012 
 * @Purpose
 */
package com.enlightedinc.common
{
	import com.enlightedinc.components.Constants;
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
		
		public var energyConsumptionRawDataCollection:ArrayCollection = new ArrayCollection();
		
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
		public var baseLine:Number=0;
		[Bindable]
		public var periodPeak:Number=0;
		
		[Bindable]
		public var savingMeterData:ArrayCollection = new ArrayCollection();
		
		// CODE FOR DRILL DOWN CHART
		//[Bindable]
		//public var drillDownChartData:ArrayCollection = new ArrayCollection();
		// DRILL DOWN CHART CODE END
		
		public var currentUnitSelection:String = Constants.POWER_UNIT;
		
		public var YAxisMaxVaue:Number = 0;
		
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
		private var maxPeakLoad:Number =0;
		
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
		
		/**
		 * The method calculates Kwh counts - All counts are derived from Raw Data coming from webservice.
		 * @return void
		 */
		
		public function calculateKwhCounts(_dataProvider:ArrayCollection):void
		{
			flushCounts();
			
			if(_dataProvider!=null && _dataProvider.length>0)
			{
					var savedCost:Number =0;
					var savedPower:Number =0;
					for(var j:int=0; j<_dataProvider.length; j++)
					{
					
						if(	checkNaN(_dataProvider[j].peakload) > maxPeakLoad)
						{
							maxPeakLoad = checkNaN(_dataProvider[j].peakload) ;
						}
					
						var basePower:Number =0;
						var object:Object = new Object();
						if(_dataProvider[j].basepowerused!=null && (!isNaN(_dataProvider[j].basepowerused)) && (_dataProvider[j].basepowerused>0))
						{
							// AS PER NEW DASHBOARD REVIEW CHNAGES
							// Using Raw Values of Ambient, Manual, Tasktuning, Occupancy Saving - To show series in the Stack chart
							totalAmbientSaving = checkNaN(_dataProvider[j].ambientsaving);
							totalOccSaving = checkNaN(_dataProvider[j].occsaving);
							totalTaskTuneupSaving = checkNaN(_dataProvider[j].tasktuneupsaving)+checkNaN(_dataProvider[j].manualsaving);
							totalPowerUsed = checkNaN(_dataProvider[j].powerused);
							
							basePower = checkNaN(_dataProvider[j].basepowerused);
						}else
						{
							totalAmbientSaving =0;
							totalOccSaving =0;
							totalTaskTuneupSaving =0
							totalPowerUsed =0;
							basePower =0;
						} 
						
						object.tasktuneupsaving = checkNaN(totalTaskTuneupSaving);
						object.ambientsaving =checkNaN(totalAmbientSaving);
						object.occsaving = checkNaN(totalOccSaving);
						object.powerused = checkNaN(totalPowerUsed);
						object.avgload=_dataProvider[j].avgload;
						object.basepowerused=checkNaN(basePower);
						object.captureon=_dataProvider[j].captureon;
						object.cost=_dataProvider[j].cost;
						object.minload=_dataProvider[j].minload;
						object.peakload=_dataProvider[j].peakload;
						object.price=_dataProvider[j].price;
						object.savedcost=checkNaN(_dataProvider[j].savedcost);
						object.savedpower=_dataProvider[j].savedpower;
						chartDataCollection.addItem(object);
						
						savedPower+= (checkNaN(totalAmbientSaving + totalOccSaving + totalTaskTuneupSaving));
						totalBasePower+=checkNaN(_dataProvider[j].basepowerused);
						savedCost+=checkNaN(_dataProvider[j].savedcost);						
					}
					
					if(isNaN(savedPower))
						savedPower=0;
					
					if(isNaN(totalBasePower))
						totalBasePower = 0;
					if(totalBasePower>0)
					{
						var savingSoFarTemp:Number= (savedPower * 100 )/(totalBasePower);
						savingSoFarTemp = Math.round(savingSoFarTemp) ;
						savingSoFarPercVal= savingSoFarTemp
					}else
					{
						savingSoFarPercVal =0;
					}
					totalSavedPower = savedPower;
					totalSavedCost = savedCost;
					periodPeak = maxPeakLoad;
					calculatePeak();
					calculateYAxisMax(chartDataCollection);
			}else
			{
				YAxisMaxVaue = 1;
			}
			dispatchEvent(new CurrentSavingChartUpdate(CurrentSavingChartUpdate.CHART_UPDATE)); 
		}
		
		/**
		 * The method calculates Co2 counts - All counts are derived from Raw Data coming from webservice by multiplying carbon factor.
		 * @return void
		 */
		public function calculateCO2Counts(_dataProvider:ArrayCollection):void
		{
			flushCounts();
			if(_dataProvider!=null && _dataProvider.length>0)
			{
				var savedCost:Number =0;
				var savedPower:Number =0;
				for(var j:int=0; j<_dataProvider.length; j++)
				{
					if(	checkNaN(_dataProvider[j].peakload) > maxPeakLoad)
					{
						maxPeakLoad = checkNaN(_dataProvider[j].peakload) ;
					}
					var basePower:Number =0;
					var object:Object = new Object();
					if(_dataProvider[j].basepowerused!=null && (!isNaN(_dataProvider[j].basepowerused)) && (_dataProvider[j].basepowerused>0))
					{
						// AS PER NEW DASHBOARD REVIEW CHNAGES
						// Using Raw Values of Ambient, Manual, Tasktuning, Occupancy Saving - To show series in the Stack chart
						totalAmbientSaving = checkNaN(_dataProvider[j].ambientsaving);
						totalOccSaving = checkNaN(_dataProvider[j].occsaving);
						totalTaskTuneupSaving = checkNaN(_dataProvider[j].tasktuneupsaving)+checkNaN(_dataProvider[j].manualsaving);
						totalPowerUsed = checkNaN(_dataProvider[j].powerused);
						basePower = checkNaN(_dataProvider[j].basepowerused);
						
					}else
					{
						totalAmbientSaving =0;
						totalOccSaving =0;
						totalTaskTuneupSaving =0
						totalPowerUsed =0;
						basePower =0;
					} 
					
					object.tasktuneupsaving = checkNaN(totalTaskTuneupSaving * Constants.CARBON_FACTOR);
					object.ambientsaving =checkNaN(totalAmbientSaving * Constants.CARBON_FACTOR);
					object.occsaving = checkNaN(totalOccSaving *Constants.CARBON_FACTOR);
					object.powerused = checkNaN(totalPowerUsed * Constants.CARBON_FACTOR);
					object.avgload=_dataProvider[j].avgload;
					object.basepowerused= checkNaN(basePower * Constants.CARBON_FACTOR);
					object.captureon=_dataProvider[j].captureon;
					object.cost=_dataProvider[j].cost;
					object.minload=_dataProvider[j].minload;
					object.peakload=_dataProvider[j].peakload;
					object.price=_dataProvider[j].price;
					object.savedcost=_dataProvider[j].savedcost;
					object.savedpower=checkNaN(_dataProvider[j].savedpower * Constants.CARBON_FACTOR);
					chartDataCollection.addItem(object);
					
					savedPower+= (checkNaN(totalAmbientSaving + totalOccSaving + totalTaskTuneupSaving));
					//We will derive SavePower as : savePower = basePower - PowerUsed ; 
					//savedPower+= (checkNaN(_dataProvider[j].basepowerused) - checkNaN(_dataProvider[j].powerused));
					totalBasePower+=checkNaN(_dataProvider[j].basepowerused);
					savedCost+=checkNaN(_dataProvider[j].savedcost);
					
				}
				if(isNaN(savedPower))
					savedPower=0;
				
				if(totalBasePower>0)
				{
					var savingSoFarTemp:Number = (savedPower * 100)/(totalBasePower );
					savingSoFarTemp =  Math.round(savingSoFarTemp) ;
					savingSoFarPercVal= savingSoFarTemp;
				}else
				{
					savingSoFarPercVal =0;
				}
				totalSavedPower = savedPower;
				totalSavedCost = savedCost;
				periodPeak =  maxPeakLoad;
				calculatePeak();
				calculateYAxisMax(chartDataCollection);
			}else
			{
				YAxisMaxVaue = 1;
			}
			dispatchEvent(new CurrentSavingChartUpdate(CurrentSavingChartUpdate.CHART_UPDATE)); 
		}
		
		/**
		 * The method calculates Money($) counts - All counts are derived from Raw Data coming from webservice by considering Saved Cost in Calculation.
		 * @return void
		 */
		public function calculateMoneyCounts(_dataProvider:ArrayCollection):void
		{
			flushCounts();
			if(_dataProvider!=null && _dataProvider.length>0)
			{
				var tSavedCost:Number =0;
				var tBaseCost:Number =0;
				var tSavedPower:Number =0;
				for(var j:int=0; j<_dataProvider.length; j++)
				{
					
					//tBaseCost += tSavedCost + checkNaNAndRound(_dataProvider[j].cost);
					tBaseCost += checkNaN(_dataProvider[j].basecost);
					
					if(	checkNaN(_dataProvider[j].peakload) > maxPeakLoad)
					{
						maxPeakLoad = checkNaN(_dataProvider[j].peakload) ;
					}
					var basePower:Number =0;
					var SavedCost:Number =0;
					var cost:Number =0;
					var totalSavingInMoney:Number =0;
					var object:Object = new Object();
					if(_dataProvider[j].basepowerused!=null && (!isNaN(_dataProvider[j].basepowerused)) && (_dataProvider[j].basepowerused>0))
					{
						// AS PER NEW DASHBOARD REVIEW CHNAGES
						// Using Raw Values of Ambient, Manual, Tasktuning, Occupancy Saving - To show series in the Stack chart
						totalAmbientSaving = checkNaN(_dataProvider[j].ambientsaving);
						totalOccSaving = checkNaN(_dataProvider[j].occsaving);
						totalTaskTuneupSaving = checkNaN(_dataProvider[j].tasktuneupsaving)+checkNaN(_dataProvider[j].manualsaving);
						totalPowerUsed = checkNaN(_dataProvider[j].powerused);
						SavedCost = checkNaN(_dataProvider[j].savedcost);
						cost = checkNaN(_dataProvider[j].cost);
						basePower = checkNaN(_dataProvider[j].basepowerused);
					}else
					{
						totalAmbientSaving =0;
						totalOccSaving =0;
						totalTaskTuneupSaving =0
						totalPowerUsed =0;
						basePower =0;
					} 
					
					totalSavingInMoney = totalAmbientSaving + totalOccSaving + totalTaskTuneupSaving;
					
					if(totalSavingInMoney>0)
					{
						object.tasktuneupsaving = checkNaN((SavedCost * totalTaskTuneupSaving)/totalSavingInMoney);
						object.ambientsaving = checkNaN((SavedCost * totalAmbientSaving)/totalSavingInMoney);
						object.occsaving = checkNaN((SavedCost * totalOccSaving)/totalSavingInMoney);
					}else
					{
						object.tasktuneupsaving = 0;
						object.ambientsaving = 0;
						object.occsaving = 0;
					}
					object.powerused = checkNaN(cost);
					object.avgload=_dataProvider[j].avgload;
					object.basepowerused=checkNaN(basePower);
					object.captureon=_dataProvider[j].captureon;
					object.cost=_dataProvider[j].cost;
					object.minload=_dataProvider[j].minload;
					object.peakload=_dataProvider[j].peakload;
					object.price=_dataProvider[j].price;
					object.savedcost=_dataProvider[j].savedcost;
					object.savedpower=checkNaN(_dataProvider[j].savedpower);
					chartDataCollection.addItem(object);
					
					tSavedPower+= (totalAmbientSaving + totalOccSaving + totalTaskTuneupSaving);
					//We will derive SavePower as : savePower = basePower - PowerUsed ; 
					//tSavedPower+= (checkNaN(_dataProvider[j].basepowerused) - checkNaN(_dataProvider[j].powerused));
					totalBasePower+=checkNaN(_dataProvider[j].basepowerused);
					tSavedCost+=checkNaN(_dataProvider[j].savedcost);
					
				}
				if(isNaN(tSavedPower))
					tSavedPower=0;
				
				if(tBaseCost>0)
				{
					var savingSoFarTemp:Number= (tSavedCost * 100)/(tBaseCost);
					savingSoFarTemp = Math.round(savingSoFarTemp) ;
					savingSoFarPercVal = savingSoFarTemp;
				}else
				{
					savingSoFarPercVal =0;
				}
				totalSavedPower = tSavedPower;
				totalSavedCost = tSavedCost;
				periodPeak = maxPeakLoad;
				calculatePeak();
				calculateYAxisMax(chartDataCollection);
			}else
			{
				YAxisMaxVaue = 1;
			}
			dispatchEvent(new CurrentSavingChartUpdate(CurrentSavingChartUpdate.CHART_UPDATE)); 
		}
		
		/**
		 * Calculate Current Counts
		 * 
		 */
		public function calculateCurrentCount(_dataProvider:ArrayCollection):void
		{
			flushCurrentCount();
			var currrentPowerUsed:Number =0;
			var currentPeriodPeak:Number =0;
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
					currrentSavedPower+=checkNaN(_dataProvider[j].ambientsaving) + checkNaN(_dataProvider[j].occsaving) + checkNaN(_dataProvider[j].tasktuneupsaving) + checkNaN(_dataProvider[j].manualsaving);
					
					// Derive Perncentage Values of Ambient, Manual, Tasktuning, Occupancy Saving - To show series in the Stack chart 
					var object:Object = new Object();
					if(_dataProvider[j].basepowerused!=null && (!isNaN(_dataProvider[j].basepowerused)) && (_dataProvider[j].basepowerused>0))
					{
						ambientSaving = (checkNaN(_dataProvider[j].ambientsaving)/checkNaN(_dataProvider[j].basepowerused))*100;
						occSaving = (checkNaN(_dataProvider[j].occsaving)/checkNaN(_dataProvider[j].basepowerused))*100;
						taskTuneupSaving = ((checkNaN(_dataProvider[j].tasktuneupsaving)+checkNaN(_dataProvider[j].manualsaving))/_dataProvider[j].basepowerused)*100;
						powerUsed =  (checkNaN(_dataProvider[j].powerused)/_dataProvider[j].basepowerused)*100;
					}else
					{
						ambientSaving =0;
						occSaving =0;
						taskTuneupSaving =0
						powerUsed =0;
					}
					object.tasktuneupsaving = checkNaN(taskTuneupSaving);
					object.ambientsaving =checkNaN(ambientSaving);
					object.occsaving = checkNaN(occSaving);
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
				currentLoad = currrentPowerUsed; 
				if(currentBasePower>0)
				{
					baseLine = currentBasePower;
					savingRightNowPercVal = (currrentSavedPower * 100) /( currentBasePower);
					savingRightNowPercVal= Math.round(savingRightNowPercVal) ;
				}else
				{
					baseLine =0;
					savingRightNowPercVal =0;
				}
				
				//Update the Current Saving Chart
				dispatchEvent(new CurrentSavingChartUpdate(CurrentSavingChartUpdate.CHART_UPDATE)); 
				
				calculatePeak();
			
			}
		}
		
		/**
		 * Method to Flush all the counter.
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
			periodPeak =0;
			maxPeakLoad =0;		
			
		}
		
		/**
		 * Method to reset Global Energy Consumption ArrayCollection.
		 * 
		 */
		public function flushGlobalCollection():void
		{
			if(energyConsumptionRawDataCollection!=null && energyConsumptionRawDataCollection.length>0)
			{
				energyConsumptionRawDataCollection.removeAll();
			}
		}
		
		/**
		 * Method to Flush all the Current counts
		 * 
		 */
		public function flushCurrentCount():void
		{
			if(savingMeterData!=null && savingMeterData.length>0)
			{
				savingMeterData.removeAll();
			}
			savingRightNowPercVal =0;
			currentLoad =0;
			baseLine=0;
		}
		
		/**
		 * Method to Checks for Not a Number
		 * @Returns Number
		 */
		public function checkNaN(value:*):Number
		{
			if(isNaN(value)){
				return 0;
			}
			return value;
		}
		
		/**
		 * Method adjust the Period Peak Value if current load exceeds Period Peak
		 * @Returns void
		 */
		public function calculatePeak():void
		{
			if(currentLoad>periodPeak)
			{
				periodPeak = currentLoad;
			}
		}
		
		private function calculateYAxisMax(chartData:ArrayCollection):void{
			YAxisMaxVaue = 0;
			for each (var obj:Object in chartData)
			{
				try
				{
					if(currentUnitSelection == Constants.POWER_UNIT)
					{
						if(obj.basepowerused > this.YAxisMaxVaue)
							this.YAxisMaxVaue = obj.basepowerused;
						
						if(obj.basepowerused ==0 && this.YAxisMaxVaue ==0)
						{
							YAxisMaxVaue = 1;
						}
						
					}else if(currentUnitSelection == Constants.CARBON_UNIT)
					{
						if(obj.basepowerused > this.YAxisMaxVaue)
							this.YAxisMaxVaue = obj.basepowerused;
						
						if(obj.basepowerused ==0 && this.YAxisMaxVaue ==0)
						{
							YAxisMaxVaue = 1;
						}
						
					}else if(currentUnitSelection == Constants.CURRENCY_UNIT)
					{
						if(obj.savedcost > this.YAxisMaxVaue)
							this.YAxisMaxVaue = obj.savedcost;
						
						if(obj.savedcost ==0 && this.YAxisMaxVaue ==0)
						{
							YAxisMaxVaue = 1;
						}
					}
				}
				catch(e:Error){
				}
			}
		}
	}
}