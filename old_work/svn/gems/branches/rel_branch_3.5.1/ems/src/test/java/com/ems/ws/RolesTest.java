package com.ems.ws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.User;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.sun.jersey.api.client.ClientResponse;

public class RolesTest extends AbstractEnlightedWSTest {

	@Resource(name = "userManager")
	private UserManager userManager;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;

	@Test
	@Transactional
	public void testSwitchApiDifferentVersionDifferentUser() {
		try {
			//select u.email, r.name, u.no_login_attempts, u.status from users u, roles r where u.role_id = r.id;
			String urlAccess = HOST_ADDR + "/ems/api/org/switch/v1/details/floor/1/ERC";
			String email = "sharad";
			User user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			checkAccessToAPI(urlAccess, user, true, ClientResponse.Status.OK.getStatusCode(), false);

			email = "sharad_aud";
			user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			checkAccessToAPI(urlAccess, user, false, ClientResponse.Status.OK.getStatusCode(), false);

			email = "sharad_admin";
			user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			checkAccessToAPI(urlAccess, user, true, ClientResponse.Status.OK.getStatusCode(), false);

			urlAccess = HOST_ADDR + "/ems/api/org/switch/details/floor/1/ERC";
			email = "sharad";
			user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			checkAccessToAPI(urlAccess, user, true, ClientResponse.Status.OK.getStatusCode(), false);

			email = "sharad_aud";
			user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			checkAccessToAPI(urlAccess, user, true, ClientResponse.Status.OK.getStatusCode(), false);

			email = "sharad_admin";
			user = userManager.loadUserByUserName(email);
			Assert.assertEquals(email, user.getEmail());
			checkAccessToAPI(urlAccess, user, true, ClientResponse.Status.OK.getStatusCode(), false);

		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}

	}

	public void checkAccessToAPI(final String urlAccess, final User user, final boolean statusConditionFlagToRaiseErr, final int statusCode, final boolean isGet ) throws Exception{
		String key = user.getSecretKey();
		ClientResponse res = getAPIResponse(urlAccess, user.getEmail(), key, isGet);
		if(!statusConditionFlagToRaiseErr){
			if (res.getStatus() == statusCode) {
				final String err = res.getEntity(String.class);
				logger.error(err + " Observed. Status is: " + res.getStatus() +" status code should not be equal to "+statusCode);
				Assert.fail(err);
			}
		}else{
			if (res.getStatus() != statusCode) {
				final String err = res.getEntity(String.class);
				logger.error(err + " Observed. Status is: " + res.getStatus() +" status code should be equal to "+statusCode);
				Assert.fail(err);
			}
		}

	}

	@Test
	public void testRolesInputAndRolesInJavaFiles(){
		try{
			final ConcurrentHashMap<String, ReflectionDTO> mout = generateJavaReflectionMapOfAllClasses();
			final File f = new File("src/test/java/referencefiles/roles-input-files-pm.txt");
			final ConcurrentHashMap<String, ReflectionDTO> min = generateRolesInputFromFile(f);
			final Set<String> keys = min.keySet();
			for (final String key : keys){
				final ReflectionDTO din = min.get(key);
				final ReflectionDTO dout = mout.get(key);
				if( (din==null || StringUtils.isEmpty(din.getInputRolesByPM())) ||
						(dout == null || StringUtils.isEmpty(dout.authVal)) ||
						(!din.getInputRolesByPM().equals(dout.authVal)) ){
					Assert.fail("failed for service "+din.getPathVal() +" Probably for class "+ dout.getParentClassName()+" and method "+ dout.getMethodName());
				}
			}
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}
	//Comment out only if need to update the java files for roles
	@Test
	public void updateAllJavaSourceFilesForRoles(){
		try{

			final ConcurrentHashMap<String, ReflectionDTO> m = generateJavaReflectionMapOfAllClasses();
			final File f = new File("src/test/java/referencefiles/roles-input-files-pm.txt");
			final ConcurrentHashMap<String, ReflectionDTO> mRef = generateRolesInputFromFile(f);
			final Set<String> keys = m.keySet();
			for (final String key : keys){
				final ReflectionDTO d = m.get(key);
//				if(!d.parentClassName.equals(FacilitiesService.class.getName())){
//					continue;
//				}
				final String parentClassFilePath = d.parentClassName.replaceAll("\\.","/");
				final File file = new File("src/main/java/"+parentClassFilePath.trim()+".java");
				FileReader fr = new FileReader(file);
	            BufferedReader br = new BufferedReader(fr);
	            final StringBuffer buf = new StringBuffer();
	            String line = null;
	            String prevLine = null;
	            boolean isImportThere = false;
	            while ((line = br.readLine()) != null)
	            {
	            	isImportThere = isImportThere ? true : line.contains("import org.springframework.security.access.prepost.PreAuthorize;");
	            	if(prevLine != null && prevLine.contains("package com.") && !isImportThere){
	            		buf.append("import org.springframework.security.access.prepost.PreAuthorize;\n");
	            		isImportThere = true;
	            	}
	                if (line.trim().equals("@Path(\""+d.getMethodPathVal()+"\")") && !prevLine.contains("@PreAuthorize")){
	                	final ReflectionDTO dRef = mRef.get(d.getPathVal());
	                	if(dRef != null && d.getMethodName().equals(dRef.getMethodName()) && d.getParentClassName().equals(dRef.getParentClassName())){
	                		final String rolesStr = dRef.getAuthVal();
	                		buf.append("\t"+rolesStr+"\n");
	                	}
	                }
	                buf.append(line+"\n");
	                prevLine = line;
	            }
	            FileWriter fw = new FileWriter(file);
	            BufferedWriter out = new BufferedWriter(fw);
	            out.write(buf.toString());
	            out.close();
	            br.close();
			}
		} catch (Exception e) {
			logger.error("***FAILED***", e);
			Assert.fail(e.getMessage());
		}
	}

	public ConcurrentHashMap<String, ReflectionDTO> generateRolesInputFromFile(final File file) throws Exception{
		final ConcurrentHashMap<String, ReflectionDTO> map = new ConcurrentHashMap<String, ReflectionDTO>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
        while ((line = br.readLine()) != null){
        	final String lineArr[] = line.split("\t");
        	final ReflectionDTO d = new ReflectionDTO();
        	d.setPathVal(lineArr[2]);
        	if(!map.containsKey(d.getPathVal())){
        		map.put(d.getPathVal(), d);
        	}else{
        		throw new Exception("Duplicate key exits in the input file: "+ d.getPathVal());
        	}
        	d.setHttpMethodVal(lineArr[1]);

        	final String inputRolesArr[] = lineArr[3].split(",");
        	final StringBuffer javaCodeAuthVal = new StringBuffer("@PreAuthorize(\"hasAnyRole(");
        	final StringBuffer inputRolesByPM = new StringBuffer();
        	int cnt = 1;
        	for(final String inputRole : inputRolesArr){
        		if(cnt == inputRolesArr.length){
        			javaCodeAuthVal.append("'"+inputRole.trim()+"')\")");
        			inputRolesByPM.append(inputRole.trim());
        		}else{
        			javaCodeAuthVal.append("'"+inputRole.trim()+"',");
        			inputRolesByPM.append(inputRole.trim()+",");
        		}
        		cnt++;
        	}
        	d.setAuthVal(javaCodeAuthVal.toString());
        	d.setInputRolesByPM(inputRolesByPM.toString());
        }
        return map;
	}
	/**
	 * Map structure: /org/ems/list  vs DTO of {actual class with package name, method name,  }
	 * methodName : ReflectionDTO
	 */
	public ConcurrentHashMap<String, ReflectionDTO> generateJavaReflectionMapOfAllClasses() throws Exception{
		final ConcurrentHashMap<String, ReflectionDTO> map = new ConcurrentHashMap<String, ReflectionDTO>();

		// create scanner and disable default filters (that is the 'false' argument)
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		// add include filters which matches all the classes (or use your own)
		provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
		// get matching classes defined in the package
		final Set<BeanDefinition> classes = provider.findCandidateComponents("com.ems");
		 int cnt = 0;
		for( BeanDefinition bean : classes ) {
		     Class<?> cls = Class.forName(bean.getBeanClassName());
//		     if(cls != UserService.class){
//		    	 continue;
//		     }
		     final javax.ws.rs.Path p = cls.getAnnotation(javax.ws.rs.Path.class);
			     if(p != null){
				     final String pathVal = p.value();
				     final Method methods[] = cls.getMethods();
				     for(final Method m : methods){
				    	 final ReflectionDTO dto = new ReflectionDTO();
				    	 final javax.ws.rs.Path pm = m.getAnnotation(javax.ws.rs.Path.class);
				    	 if(pm != null){
				    		 final String methodPathval = pathVal+"/"+pm.value();
				    		 dto.setParentPathVal(pathVal);
				    		 dto.setMethodPathVal(pm.value());
				    		 dto.setPathVal(methodPathval);
				    		 dto.setParentClassName(cls.getName());
				    		 dto.setMethodName(m.getName());
				    		 if(map.containsKey(methodPathval)){
						    	 map.put(methodPathval+"_DUPLICATE", dto);
						     }else{
						    	 map.put(methodPathval, dto);
						     }
				    		 Produces produces = m.getAnnotation(Produces.class);
				    		 if(produces!=null){
				    			 final String str[] = produces.value();
				    			 final StringBuffer b = new StringBuffer();
				    			 for (final String s : str){
				    				 b.append(s+",");
				    			 }
				    			 dto.setProducesVal(b.toString());
				    		 }

				    		 Consumes consumes = m.getAnnotation(Consumes.class);
				    		 if(consumes!=null){
				    			 final String str[] = consumes.value();
				    			 final StringBuffer b = new StringBuffer();
				    			 for (final String s : str){
				    				 b.append(s+",");
				    			 }
				    			 dto.setConsumesVal(b.toString());
				    		 }

				    		 PreAuthorize auth = cls.getAnnotation(PreAuthorize.class);
				    		 if(auth!=null){
				    			 String temp = StringUtils.isEmpty(dto.getAuthVal())?auth.value():dto.getAuthVal()+";"+auth.value();
				    			 temp = temp.replaceAll("hasAnyRole\\(", "");
				    			 temp = temp.replaceAll("\\)", "");
				    			 temp = temp.replaceAll("'", "");
				    			 dto.setAuthVal(temp);
				    		 }

				    		 auth = m.getAnnotation(PreAuthorize.class);
				    		 if(auth!=null){
				    			 String temp = StringUtils.isEmpty(dto.getAuthVal())?auth.value():dto.getAuthVal()+";"+auth.value();
				    			 temp = temp.replaceAll("hasAnyRole\\(", "");
				    			 temp = temp.replaceAll("\\)", "");
				    			 temp = temp.replaceAll("'", "");
				    			 dto.setAuthVal(temp);
				    		 }

				    		 //method params
				    		 final Annotation paArr[][] = m.getParameterAnnotations();
				    		 final Class paramTypes[] = m.getParameterTypes();
				    		 int paramCnt = 0;
				    		 for (final Annotation[] pa1 : paArr){
				    			 final Class paramTypeClass = paramTypes[paramCnt++];
				    			 for(final Annotation pa : pa1){
				    				 System.out.println("ClassName:"+pa.getClass());
					    			 //if(pa.getClass() == PathParam.class){
				    				 	if(pa.annotationType() == PathParam.class){
						    				 final PathParam pp = (PathParam)pa;
						    				 final String pathParamVal = pp.value();
						    				 System.out.println("ParamName:"+pathParamVal + ":Type:"+paramTypeClass);
						    				 dto.paramList.put(pathParamVal, paramTypeClass.getName());
				    				 	}
					    			 //}
				    			 }
				    		 }

				    		 String httpMethodVal = "";
				    		 final Annotation ann[] = m.getDeclaredAnnotations();
				    		 for (final Annotation a : ann){
				    			 Class c = a.annotationType();
				    			 javax.ws.rs.HttpMethod methodType = (HttpMethod) c.getAnnotation(javax.ws.rs.HttpMethod.class);
				    			 if(methodType!=null){
				    				 httpMethodVal = methodType.value();
				    				 dto.setHttpMethodVal(httpMethodVal);
				    				 break;
				    			 }
				    		 }

				    		cnt++;
				    	 }
				     }
		     }
		}
		System.out.println("Count is "+cnt);
		return map;

	}

	private class ReflectionDTO{
		private String parentClassName;
		private String methodName;
		private String pathVal;
		private String parentPathVal;
		private String methodPathVal; // This is the entire absolute path of the ws including path on parent class
		private String httpMethodVal;
		private String producesVal; //  javax.ws.rs.Produces
		private String consumesVal; // javax.ws.rs.Consumes
		private String authVal; //org.springframework.security.access.prepost.PreAuthorize
		private String inputRolesByPM;
		private ConcurrentHashMap<String,String> paramList = new ConcurrentHashMap<String,String>();


		public String getInputRolesByPM() {
			return inputRolesByPM;
		}
		public void setInputRolesByPM(String inputRolesByPM) {
			this.inputRolesByPM = inputRolesByPM;
		}
		public String getParentPathVal() {
			return parentPathVal;
		}
		public void setParentPathVal(String parentPathVal) {
			this.parentPathVal = parentPathVal;
		}
		public String getMethodPathVal() {
			return methodPathVal;
		}
		public void setMethodPathVal(String methodPathVal) {
			this.methodPathVal = methodPathVal;
		}
		public ConcurrentHashMap<String, String> getParamList() {
			return paramList;
		}
		public void setParamList(ConcurrentHashMap<String, String> paramList) {
			this.paramList = paramList;
		}
		public String getParentClassName() {
			return parentClassName;
		}
		public void setParentClassName(String parentClassName) {
			this.parentClassName = parentClassName;
		}
		public String getMethodName() {
			return methodName;
		}
		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}
		public String getPathVal() {
			return pathVal;
		}
		public void setPathVal(String pathVal) {
			this.pathVal = pathVal;
		}
		public String getHttpMethodVal() {
			return httpMethodVal;
		}
		public void setHttpMethodVal(String httpMethodVal) {
			this.httpMethodVal = httpMethodVal;
		}
		public String getProducesVal() {
			return producesVal;
		}
		public void setProducesVal(String producesVal) {
			this.producesVal = producesVal;
		}
		public String getConsumesVal() {
			return consumesVal;
		}
		public void setConsumesVal(String consumesVal) {
			this.consumesVal = consumesVal;
		}
		public String getAuthVal() {
			return authVal;
		}
		public void setAuthVal(String authVal) {
			this.authVal = authVal;
		}



	}
}

