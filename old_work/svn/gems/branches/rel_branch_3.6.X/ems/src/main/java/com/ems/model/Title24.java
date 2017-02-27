package com.ems.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Title24 model that will be used to retrieve update from json string in the
 * system-configuration 'title24.json.key' as key
 * 
 * @author enlighted
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Title24 implements Serializable {

	private static Logger syslog = Logger.getLogger("SysLog");
	private static final long serialVersionUID = -8346640157041015941L;
	// title24.compliance.flag
	@XmlElement(name = "compliance")
	private Compliance compliance = new Compliance();

	@XmlElement(name = "lightcontrolaccepatance")
	private LightingControlAcceptance lightcontrolaccepatance = new LightingControlAcceptance();
	
	@XmlElement(name = "autodlcontrolaccepatance")
	private AutoDLControlAcceptance autodlcontrolaccepatance = new AutoDLControlAcceptance();
	@XmlElement(name = "dracceptance")
	private DRAcceptance dracceptance = new DRAcceptance();
	
	@XmlElement(name = "olc")
	private OutdoorLightingControl olc = new OutdoorLightingControl();
	
	@XmlElement(name = "formtype")
	public String formtype = "";
	
	public String getFormtype() {
		return formtype;
	}

	public void setFormtype(String formtype) {
		this.formtype = formtype;
	}

	@XmlAccessorType(XmlAccessType.NONE)
	public static class OutdoorLightingControl implements Serializable{
		private static final long serialVersionUID = -8346670157043316052L;
		@XmlElement(name = "constructioninspection")
		private List<String> constructioninspection= Arrays.asList(new String[]{""});
		@XmlElement(name = "ftmotion")
		private List<String> ftmotion= Arrays.asList(new String[]{""});
		@XmlElement(name = "ftnomotion")
		private List<String> ftnomotion= Arrays.asList(new String[]{""});
		@XmlElement(name = "cidayoff")
		private List<String> cidayoff= Arrays.asList(new String[]{""});
		@XmlElement(name = "cinightoff")
		private List<String> cinightoff= Arrays.asList(new String[]{""});
		@XmlElement(name = "lscriteriameet")
		private List<String> lscriteriameet= Arrays.asList(new String[]{""});
		@XmlElement(name = "ftdayoff")
		private List<String> ftdayoff= Arrays.asList(new String[]{""});
		
		@XmlElement(name = "ciatest")
		private String ciatest= "Fail";
		
		
		@XmlElement(name = "fttest1")
		private String fttest1= "Fail";
		
		@XmlElement(name = "fttest2")
		private String fttest2= "Fail";
		
		@XmlElement(name = "citest1")
		private String citest1= "Fail";
		
		@XmlElement(name = "citest2")
		private String citest2= "Fail";
		
		@XmlElement(name = "citest3")
		private String citest3= "Fail";
		
		@XmlElement(name = "fttestd")
		private String fttestd= "Fail";
		
		@XmlElement(name = "evaluation")
		private String evaluation= "Fail";
		
		
		public String getCiatest() {
			return ciatest;
		}
		public void setCiatest(String ciatest) {
			this.ciatest = ciatest;
		}
		public String getFttest1() {
			return fttest1;
		}
		public void setFttest1(String fttest1) {
			this.fttest1 = fttest1;
		}
		public String getFttest2() {
			return fttest2;
		}
		public void setFttest2(String fttest2) {
			this.fttest2 = fttest2;
		}
		public String getCitest1() {
			return citest1;
		}
		public void setCitest1(String citest1) {
			this.citest1 = citest1;
		}
		public String getCitest2() {
			return citest2;
		}
		public void setCitest2(String citest2) {
			this.citest2 = citest2;
		}
		public String getCitest3() {
			return citest3;
		}
		public void setCitest3(String citest3) {
			this.citest3 = citest3;
		}
		public String getFttestd() {
			return fttestd;
		}
		public void setFttestd(String fttestd) {
			this.fttestd = fttestd;
		}
		public String getEvaluation() {
			return evaluation;
		}
		public void setEvaluation(String evaluation) {
			this.evaluation = evaluation;
		}
		public List<String> getConstructioninspection() {
			return constructioninspection;
		}
		public void setConstructioninspection(List<String> constructioninspection) {
			this.constructioninspection = constructioninspection;
		}
		public List<String> getFtmotion() {
			return ftmotion;
		}
		public void setFtmotion(List<String> ftmotion) {
			this.ftmotion = ftmotion;
		}
		public List<String> getFtnomotion() {
			return ftnomotion;
		}
		public void setFtnomotion(List<String> ftnomotion) {
			this.ftnomotion = ftnomotion;
		}
		public List<String> getCidayoff() {
			return cidayoff;
		}
		public void setCidayoff(List<String> cidayoff) {
			this.cidayoff = cidayoff;
		}
		public List<String> getCinightoff() {
			return cinightoff;
		}
		public void setCinightoff(List<String> cinightoff) {
			this.cinightoff = cinightoff;
		}
		public List<String> getLscriteriameet() {
			return lscriteriameet;
		}
		public void setLscriteriameet(List<String> lscriteriameet) {
			this.lscriteriameet = lscriteriameet;
		}
		public List<String> getFtdayoff() {
			return ftdayoff;
		}
		public void setFtdayoff(List<String> ftdayoff) {
			this.ftdayoff = ftdayoff;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.NONE)
	public static class DRAcceptance implements Serializable{
		private static final long serialVersionUID = -8346670157042216052L;
		@XmlElement(name = "constructioninspection")
		private List<String> constructioninspection= Arrays.asList(new String[]{""});
		
		@XmlElement(name = "drtest")
		private List<String> drtest= Arrays.asList(new String[]{""});
		@XmlElement(name = "functionaltestcheckbox")
		private List<String> functionaltestcheckbox= Arrays.asList(new String[]{""});
		@XmlElement(name = "methodofmeasurement")
		private String methodofmeasurement= "Method2";
		@XmlElement(name = "method1data")
		private String method1data="[]";
		@XmlElement(name = "method2data")
		private String method2data="[]";
		@XmlElement(name = "drspacedata")
		private String drspacedata="[]";
		
		public List<String> getDrtest() {
			return drtest;
		}
		public void setDrtest(List<String> drtest) {
			this.drtest = drtest;
		}
		@Transient
		@JsonIgnore
		private  JSONArray method1datajsonarray;
		@Transient
		@JsonIgnore
		private  JSONArray method2datajsonarray;
		@Transient
		@JsonIgnore
		private  JSONArray drspacedatajsonarray;
		@JsonIgnore
		public JSONArray getMethod1datajsonarray() {
			return method1datajsonarray;
		}
		public void setMethod1datajsonarray(JSONArray method1datajsonarray) {
			this.method1datajsonarray = method1datajsonarray;
		}
		@JsonIgnore
		public JSONArray getMethod2datajsonarray() {
			return method2datajsonarray;
		}
		public void setMethod2datajsonarray(JSONArray method2datajsonarray) {
			this.method2datajsonarray = method2datajsonarray;
		}
		@JsonIgnore
		public JSONArray getDrspacedatajsonarray() {
			return drspacedatajsonarray;
		}
		public void setDrspacedatajsonarray(JSONArray drspacedatajsonarray) {
			this.drspacedatajsonarray = drspacedatajsonarray;
		}
		public List<String> getConstructioninspection() {
			return constructioninspection;
		}
		public void setConstructioninspection(List<String> constructioninspection) {
			this.constructioninspection = constructioninspection;
		}
		public List<String> getFunctionaltestcheckbox() {
			return functionaltestcheckbox;
		}
		public void setFunctionaltestcheckbox(List<String> functionaltestcheckbox) {
			this.functionaltestcheckbox = functionaltestcheckbox;
		}
		public String getMethod1data() {
			return method1data;
		}
		public void setMethod1data(String method1data) {
			try {
				this.method1datajsonarray = new JSONArray(method1data);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.method1data = method1data;
		}
		public String getDrspacedata() {
			return drspacedata;
		}
		public void setDrspacedata(String drspacedata) {
			try {
				this.drspacedatajsonarray = new JSONArray(drspacedata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.drspacedata = drspacedata;
		}
		public String getMethodofmeasurement() {
			return methodofmeasurement;
		}
		public void setMethodofmeasurement(String methodofmeasurement) {
			this.methodofmeasurement = methodofmeasurement;
		}
		public String getMethod2data() {
			return method2data;
		}
		public void setMethod2data(String method2data) {
			try {
				this.method2datajsonarray = new JSONArray(method2data);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.method2data = method2data;
		}
		
	}
	@XmlAccessorType(XmlAccessType.NONE)
	public static class AutoDLControlAcceptance implements Serializable{
		private static final long serialVersionUID = -8346670157042215952L;
		@XmlElement(name = "nrcalti03a")
		private List<String> nrcalti03a= Arrays.asList(new String[]{""});
		@XmlElement(name = "drawdlzone")
		private List<String> drawdlzone= Arrays.asList(new String[]{""});
		@XmlElement(name = "autodlfunctionaltestingcdsdata")
		private String autodlfunctionaltestingcdsdata= "[]";
		
		@XmlElement(name = "autodlfunctionaltestingsdsdata")
		private String autodlfunctionaltestingsdsdata= "[]";
		
		@XmlElement(name = "autodlfunctionaltestingcdslmmdata")
		private String autodlfunctionaltestingcdslmmdata= "[]";
		@XmlElement(name = "autodlfunctionaltestingsdslmmdata")
		private String autodlfunctionaltestingsdslmmdata="[]";
		@XmlElement(name = "drawdlzoneshownpages")
		private String drawdlzoneshownpages = "";
		@XmlElement(name = "drawdlzonebuiltplanpages")
		private String drawdlzonebuiltplanpages = "";
		@XmlElement(name = "autodlcontrolsystemgriddata")
		private String autodlcontrolsystemgriddata="[]";
		@XmlElement(name = "sensorcontroldata")
		private String sensorcontroldata="[]";
		
		@Transient
		@JsonIgnore
		private  JSONArray autodlcontrolsystemgriddatajsonarray;
		@Transient
		@JsonIgnore
		private  JSONArray sensorcontroldatajsonarray;
		
		@Transient
		@JsonIgnore
		private  JSONArray autodlfunctionaltestingsdsdatajsonarray;
		
		@Transient
		@JsonIgnore
		private  JSONArray autodlfunctionaltestingcdslmmdatajsonarray;
		@Transient
		@JsonIgnore
		private  JSONArray autodlfunctionaltestingsdslmmdatajsonarray;
		
		@Transient
		@JsonIgnore
		private JSONArray autodlfunctionaltestingcdsdatajsonarray;
		
		@JsonIgnore
		public JSONArray getAutodlfunctionaltestingsdsdatajsonarray() {
			return autodlfunctionaltestingsdsdatajsonarray;
		}
		public void setAutodlfunctionaltestingsdsdatajsonarray(
				JSONArray autodlfunctionaltestingsdsdatajsonarray) {
			this.autodlfunctionaltestingsdsdatajsonarray = autodlfunctionaltestingsdsdatajsonarray;
		}
		@JsonIgnore
		public JSONArray getAutodlfunctionaltestingcdslmmdatajsonarray() {
			return autodlfunctionaltestingcdslmmdatajsonarray;
		}
		public void setAutodlfunctionaltestingcdslmmdatajsonarray(
				JSONArray autodlfunctionaltestingcdslmmdatajsonarray) {
			this.autodlfunctionaltestingcdslmmdatajsonarray = autodlfunctionaltestingcdslmmdatajsonarray;
		}
		@JsonIgnore
		public JSONArray getAutodlfunctionaltestingsdslmmdatajsonarray() {
			return autodlfunctionaltestingsdslmmdatajsonarray;
		}
		public void setAutodlfunctionaltestingsdslmmdatajsonarray(
				JSONArray autodlfunctionaltestingsdslmmdatajsonarray) {
			this.autodlfunctionaltestingsdslmmdatajsonarray = autodlfunctionaltestingsdslmmdatajsonarray;
		}
		@JsonIgnore
		public JSONArray getAutodlfunctionaltestingcdsdatajsonarray() {
			return autodlfunctionaltestingcdsdatajsonarray;
		}
		public void setAutodlfunctionaltestingcdsdatajsonarray(
				JSONArray autodlfunctionaltestingcdsdatajsonarray) {
			this.autodlfunctionaltestingcdsdatajsonarray = autodlfunctionaltestingcdsdatajsonarray;
		}
		@JsonIgnore
		public JSONArray getAutodlcontrolsystemgriddatajsonarray() {
			return autodlcontrolsystemgriddatajsonarray;
		}
		public void setAutodlcontrolsystemgriddatajsonarray(
				JSONArray autodlcontrolsystemgriddatajsonarray) {
			this.autodlcontrolsystemgriddatajsonarray = autodlcontrolsystemgriddatajsonarray;
		}
		@JsonIgnore
		public JSONArray getSensorcontroldatajsonarray() {
			return sensorcontroldatajsonarray;
		}
		public void setSensorcontroldatajsonarray(JSONArray sensorcontroldatajsonarray) {
			this.sensorcontroldatajsonarray = sensorcontroldatajsonarray;
		}
		public String getSensorcontroldata() {
			return sensorcontroldata;
		}
		public void setSensorcontroldata(String sensorcontroldata) {
			try {
				this.sensorcontroldatajsonarray = new JSONArray(sensorcontroldata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.sensorcontroldata = sensorcontroldata;
		}
		public String getDrawdlzoneshownpages() {
			return drawdlzoneshownpages;
		}
		public void setDrawdlzoneshownpages(String drawdlzoneshownpages) {
			this.drawdlzoneshownpages = drawdlzoneshownpages;
		}
		public String getDrawdlzonebuiltplanpages() {
			return drawdlzonebuiltplanpages;
		}
		public void setDrawdlzonebuiltplanpages(String drawdlzonebuiltplanpages) {
			this.drawdlzonebuiltplanpages = drawdlzonebuiltplanpages;
		}
		public String getAutodlcontrolsystemgriddata() {
			return autodlcontrolsystemgriddata;
		}
		public void setAutodlcontrolsystemgriddata(String autodlcontrolsystemgriddata) {
			try {
				this.autodlcontrolsystemgriddatajsonarray = new JSONArray(autodlcontrolsystemgriddata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.autodlcontrolsystemgriddata = autodlcontrolsystemgriddata;
		}
		public List<String> getNrcalti03a() {
			return nrcalti03a;
		}
		public void setNrcalti03a(List<String> nrcalti03a) {
			this.nrcalti03a = nrcalti03a;
		}
		public List<String> getDrawdlzone() {
			return drawdlzone;
		}
		public void setDrawdlzone(List<String> drawdlzone) {
			this.drawdlzone = drawdlzone;
		}
		public String getAutodlfunctionaltestingcdsdata() {
			return autodlfunctionaltestingcdsdata;
		}
		public void setAutodlfunctionaltestingcdsdata(
				String autodlfunctionaltestingcdsdata) {
			this.autodlfunctionaltestingcdsdata = autodlfunctionaltestingcdsdata;
			try {
				this.autodlfunctionaltestingcdsdatajsonarray =new JSONArray(autodlfunctionaltestingcdsdata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
		}
		public String getAutodlfunctionaltestingsdsdata() {
			return autodlfunctionaltestingsdsdata;
		}
		public void setAutodlfunctionaltestingsdsdata(
				String autodlfunctionaltestingsdsdata) {
			this.autodlfunctionaltestingsdsdata = autodlfunctionaltestingsdsdata;
			try {
				this.autodlfunctionaltestingsdsdatajsonarray = new JSONArray(autodlfunctionaltestingsdsdata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
		}
		public String getAutodlfunctionaltestingcdslmmdata() {
			return autodlfunctionaltestingcdslmmdata;
		}
		public void setAutodlfunctionaltestingcdslmmdata(
				String autodlfunctionaltestingcdslmmdata) {
			this.autodlfunctionaltestingcdslmmdata = autodlfunctionaltestingcdslmmdata;
			try {
				this.autodlfunctionaltestingcdslmmdatajsonarray = new JSONArray(autodlfunctionaltestingcdslmmdata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
		}
		public String getAutodlfunctionaltestingsdslmmdata() {
			return autodlfunctionaltestingsdslmmdata;
		}
		public void setAutodlfunctionaltestingsdslmmdata(
				String autodlfunctionaltestingsdslmmdata) {
			this.autodlfunctionaltestingsdslmmdata = autodlfunctionaltestingsdslmmdata;
			try {
				this.autodlfunctionaltestingsdslmmdatajsonarray = new JSONArray(autodlfunctionaltestingsdslmmdata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
		}
		
		
	}
	
	@XmlAccessorType(XmlAccessType.NONE)
	public static class LightingControlAcceptance implements Serializable{
		private static final long serialVersionUID = -8346670157042215941L;
		@XmlElement(name = "autotimeswitchcontrolprogrammed")
		private List<String> autotimeswitchcontrolprogrammed= Arrays.asList(new String[]{""});
		@XmlElement(name = "ownerautoswitchprogramming")
		private List<String> ownerautoswitchprogramming= Arrays.asList(new String[]{""});
		@XmlElement(name = "occsensorconstructioninspection")
		private List<String> occsensorconstructioninspection= Arrays.asList(new String[]{""});
		@XmlElement(name = "evaluation")
		private List<String> evaluation= Arrays.asList(new String[]{""});
		@XmlElement(name = "submitflag")
		private boolean submitflag = false;
		@XmlElement(name = "spacedetaildata")
		private String spacedetaildata="[]";
		@Transient
		@JsonIgnore
		private  JSONArray spacedetaildatajsonarray;
		
		@XmlElement(name = "functionaltestdata")
		private String functionaltestdata="[]";
		
		@Transient
		@JsonIgnore
		private JSONArray functionaltestdatajsonarray;
		
		@XmlElement(name = "resultdata")
		private String resultdata="[]";
		
		@Transient
		@JsonIgnore
		private  JSONArray resultdatajsonarray;
		
		@JsonIgnore
		public JSONArray getResultdatajsonarray() {
			return resultdatajsonarray;
		}
		public void setResultdatajsonarray(JSONArray resultdatajsonarray) {
			this.resultdatajsonarray = resultdatajsonarray;
		}
		public String getResultdata() {
			return resultdata;
		}
		public void setResultdata(String resultdata) {
			try {
				this.resultdatajsonarray = new JSONArray(resultdata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.resultdata = resultdata;
		}
		public List<String> getAutotimeswitchcontrolprogrammed() {
			return autotimeswitchcontrolprogrammed;
		}
		public void setAutotimeswitchcontrolprogrammed(
				List<String> autotimeswitchcontrolprogrammed) {
			this.autotimeswitchcontrolprogrammed = autotimeswitchcontrolprogrammed;
		}
		public List<String> getOwnerautoswitchprogramming() {
			return ownerautoswitchprogramming;
		}
		public void setOwnerautoswitchprogramming(
				List<String> ownerautoswitchprogramming) {
			this.ownerautoswitchprogramming = ownerautoswitchprogramming;
		}
		public boolean isSubmitflag() {
			return submitflag;
		}
		public void setSubmitflag(boolean submitflag) {
			this.submitflag = submitflag;
		}
		public String getSpacedetaildata() {
			return spacedetaildata;
		}
		public void setSpacedetaildata(String spacedetaildata) {
			try {
				this.spacedetaildatajsonarray = new JSONArray(spacedetaildata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.spacedetaildata = spacedetaildata;
		}
		public String getFunctionaltestdata() {
			return functionaltestdata;
		}
		public void setFunctionaltestdata(String functionaltestdata) {
			try {
				this.functionaltestdatajsonarray = new JSONArray(functionaltestdata);
			} catch (JSONException e) {
				syslog.error("While generating json array from data string",e);
			}
			this.functionaltestdata = functionaltestdata;
		}
		public List<String> getOccsensorconstructioninspection() {
			return occsensorconstructioninspection;
		}
		public void setOccsensorconstructioninspection(
				List<String> occsensorconstructioninspection) {
			this.occsensorconstructioninspection = occsensorconstructioninspection;
		}
		@JsonIgnore
		public JSONArray getSpacedetaildatajsonarray() {
			
			return spacedetaildatajsonarray;
		}
		@JsonIgnore
		public JSONArray getFunctionaltestdatajsonarray() {
			return functionaltestdatajsonarray;
		}
		public void setSpacedetaildatajsonarray(JSONArray spacedetaildatajsonarray) {
			this.spacedetaildatajsonarray = spacedetaildatajsonarray;
		}
		public void setFunctionaltestdatajsonarray(JSONArray functionaltestdatajsonarray) {
			this.functionaltestdatajsonarray = functionaltestdatajsonarray;
		}
		public List<String> getEvaluation() {
			return evaluation;
		}
		public void setEvaluation(List<String> evaluation) {
			this.evaluation = evaluation;
		}
	}
//	@XmlAccessorType(XmlAccessType.NONE)
//	public static class SelectedNameStatus{
//		@XmlElement(name = "status")
//		private String name="";
//		@XmlElement(name = "status")
//		private String status="";
//		
//		public SelectedNameStatus(final String name, final String status){
//			this.name = name;
//			this.status = status;
//		}
//		public String getName() {
//			return name;
//		}
//		public void setName(String name) {
//			this.name = name;
//		}
//		public String getStatus() {
//			return status;
//		}
//		public void setStatus(String status) {
//			this.status = status;
//		}
//		
//	}
	
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Compliance implements Serializable {
		private static final long serialVersionUID = -8346640157042215941L;
		
		@XmlElement(name = "flag")
		private String flag="No";

		public String getFlag() {
			return flag;
		}

		public void setFlag(String flag) {
			this.flag = flag;
		}
	}

	public Compliance getCompliance() {
		return compliance;
	}

	public void setCompliance(Compliance compliance) {
		this.compliance = compliance;
	}

	public LightingControlAcceptance getLightcontrolaccepatance() {
		return lightcontrolaccepatance;
	}

	public void setLightcontrolaccepatance(
			LightingControlAcceptance lightcontrolaccepatance) {
		this.lightcontrolaccepatance = lightcontrolaccepatance;
	}

	public DRAcceptance getDracceptance() {
		return dracceptance;
	}

	public void setDracceptance(DRAcceptance dracceptance) {
		this.dracceptance = dracceptance;
	}

	public AutoDLControlAcceptance getAutodlcontrolaccepatance() {
		return autodlcontrolaccepatance;
	}

	public void setAutodlcontrolaccepatance(
			AutoDLControlAcceptance autodlcontrolaccepatance) {
		this.autodlcontrolaccepatance = autodlcontrolaccepatance;
	}

	public OutdoorLightingControl getOlc() {
		return olc;
	}

	public void setOlc(OutdoorLightingControl olc) {
		this.olc = olc;
	}
}

