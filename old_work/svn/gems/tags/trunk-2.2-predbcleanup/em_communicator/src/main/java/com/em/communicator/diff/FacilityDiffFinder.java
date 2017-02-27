package com.em.communicator.diff;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.stereotype.Service;

import com.communicator.communication.Globals;
import com.communicator.model.vo.AreaVO;
import com.communicator.model.vo.BuildingVO;
import com.communicator.model.vo.CampusVO;
import com.communicator.model.vo.CompanyVO;
import com.communicator.model.vo.FixtureVO;
import com.communicator.model.vo.FloorVO;
import com.communicator.model.vo.GatewayVO;

@Service("facilityDiffFinder")
public class FacilityDiffFinder implements IFinder {
	
	private String dataPath = "/home/enlighted/clouddata/";

	@Override
	public void getChangedData(List<CompanyVO> company, List<CampusVO> campus,
			List<BuildingVO> building, List<FloorVO> floor, List<AreaVO> area,
			List<FixtureVO> fixture, List<GatewayVO> gateway) {
		
			BufferedReader br = null;
			Runtime rt = Runtime.getRuntime();
			Process pr = null;
			String line = "";
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			    
			try {
				pr = rt.exec("get_cloud_data.sh");
				pr.waitFor();
				
				//COMPANY
				pr = rt.exec("cat " + dataPath + "final_cloud_company");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						company.add(new CompanyVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[3].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[4].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[5], "".equals(arr[6]) || "\\N".equals(arr[6]) ? null : new Integer(arr[6]), "t".equals("".equals(arr[7]) || "\\N".equals(arr[7]) ? "f" : arr[7]), 
								arr[8].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[9].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[10], "".equals(arr[11]) || "\\N".equals(arr[11]) ? null : new Long(arr[11]), 
								"".equals(arr[12]) || "\\N".equals(arr[12]) ? null : new Float(arr[12]), arr[13]));
					}
				}
				
				//CAMPUS
				pr = rt.exec("cat " + dataPath + "final_cloud_campus");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						campus.add(new CampusVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[3].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[4].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								"".equals(arr[5]) || "\\N".equals(arr[5]) ? null : new Long(arr[5]) ));
					}
				}
				
				//BUILDING
				pr = rt.exec("cat " + dataPath + "final_cloud_building");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						building.add(new BuildingVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								"".equals(arr[3]) || "\\N".equals(arr[3]) ? null : new Long(arr[3]) ));
					}
				}
				
				//FlOOR
				pr = rt.exec("cat " + dataPath + "final_cloud_floor");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						floor.add(new FloorVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[3].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								"".equals(arr[4]) || "\\N".equals(arr[4]) ? null : new Long(arr[4]), arr[5], 
								"".equals(arr[6]) || "\\N".equals(arr[6]) ? null : new Integer(arr[6]), 
								"".equals(arr[7]) || "\\N".equals(arr[7]) ? null : new Integer(arr[7]), 
								"".equals(arr[8]) || "\\N".equals(arr[8]) ? null : formatter.parse(arr[8])));
					}
				}
				
				//AREA
				pr = rt.exec("cat " + dataPath + "final_cloud_area");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						area.add(new AreaVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[3].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								"".equals(arr[4]) || "\\N".equals(arr[4]) ? null : new Long(arr[4]), arr[5] ));
					}
				}
				
				//FIXTURE
				pr = rt.exec("cat " + dataPath + "final_cloud_fixture");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						fixture.add(new FixtureVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								arr[3],	"".equals(arr[4]) || "\\N".equals(arr[4]) ? null : new Long(arr[4]), 
								"".equals(arr[5]) || "\\N".equals(arr[5]) ? null : new Long(arr[5]), arr[6], 
								"".equals(arr[7]) || "\\N".equals(arr[7]) ? null : new Integer(arr[7]),
								"".equals(arr[8]) || "\\N".equals(arr[8]) ? null : new Integer(arr[8]), arr[9], 
								"".equals(arr[10]) || "\\N".equals(arr[10]) ? null : new Long(arr[10]), arr[11], 
								arr[12], arr[13],
								"".equals(arr[14]) || "\\N".equals(arr[14]) ? null : new Integer(arr[14]), 
								"".equals(arr[15]) || "\\N".equals(arr[15]) ? null : formatter.parse(arr[15]), 
								"".equals(arr[16]) || "\\N".equals(arr[16]) ? null : formatter.parse(arr[16]), arr[17], 
								"".equals(arr[18]) || "\\N".equals(arr[18]) ? null : new java.math.BigDecimal(arr[18]), arr[19]));
					}
				}
	    		
				//GATEWAY
				pr = rt.exec("cat " + dataPath + "final_cloud_gateway");
				pr.waitFor();
				br = new BufferedReader(new InputStreamReader(
						pr.getInputStream()));
				while (true) {
					line = br.readLine();
					if (line == null) {
						break;
					}
					String[] arr = line.split(Globals.separator_string);
					if(arr.length > 1) {
						gateway.add(new GatewayVO(arr[0], new Long(arr[1]), 
								arr[2].replaceAll(Globals.separator_encode_string, Globals.separator_string),
								"".equals(arr[3]) || "\\N".equals(arr[3]) ? null : new Long(arr[3]), 
								"".equals(arr[4]) || "\\N".equals(arr[4]) ? null : new Integer(arr[4]), 
								"".equals(arr[5]) || "\\N".equals(arr[5]) ? null : new Integer(arr[5]), arr[6], 
								arr[7], arr[8], arr[9], arr[10], arr[11],
								"".equals(arr[12]) || "\\N".equals(arr[12]) ? null : new Integer(arr[12])));
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		
	}

}
