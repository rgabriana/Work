package com.enlightedinc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.allcolor.yahp.converter.CYaHPConverter;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class ENLCommandLineUtils {

	private static Logger syslog = Logger.getLogger("SysLog");
	private static String readFile( String file ) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while( ( line = reader.readLine() ) != null ) {
	            stringBuilder.append( line );
	            stringBuilder.append( ls );
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
	public static void convertToPdf(final HtmlToPdfDTO dto) throws Exception{
		// new converter
		CYaHPConverter converter = new CYaHPConverter();
		// save pdf in outfile
		File fout = new File(dto.getOutputpdfpath());
		FileOutputStream out = new FileOutputStream(fout);
		// contains configuration properties
		Map properties = new HashMap();
		// list containing header/footer
		List headerFooterList = new ArrayList();
		// add header/footer
		if(dto.headerpath!=null && !dto.headerpath.trim().equals("")){
			final String headerStr = readFile(dto.headerpath);
			if(headerStr!=null && !headerStr.trim().equals("")){
				//System.out.println("headerString12222::"+headerStr);
				headerFooterList
				        .add(new IHtmlToPdfTransformer.CHeaderFooter(headerStr, IHtmlToPdfTransformer.CHeaderFooter.HEADER));
			}
		}
		if(dto.footerpath!=null && !dto.footerpath.trim().equals("")){
			final String footerStr = readFile(dto.footerpath);
			if(footerStr!=null && !footerStr.trim().equals("")){
				//System.out.println("footerStr::"+footerStr);
				headerFooterList.add(new IHtmlToPdfTransformer.CHeaderFooter(footerStr/*
				        "<table width=\"100%\"><tbody><tr><td align=\"left\">"
				                + "© 2016 enLighted Inc</td><td align=\"right\"><pagenumber>/<"
				                + "pagecount></td></tr></tbody></table>"*/,
				        IHtmlToPdfTransformer.CHeaderFooter.FOOTER));
			}
		}
		
		properties.put(IHtmlToPdfTransformer.PDF_RENDERER_CLASS,
		        IHtmlToPdfTransformer.FLYINGSAUCER_PDF_RENDERER);
//		properties.put(IHtmlToPdfTransformer.FOP_TTF_FONT_PATH,
//		 fontPath);
		/** A4 portrait size */
		final PageSize A4P = new PageSize(20.8d, 29.6d, 1d,1d,1.5d, 3.5d); // widht, height, lmargin, rrmargin, bmargin, tmargin
		converter.convertToPdf(new URL("file://"+dto.getInputhtmlpath()),
		        A4P, headerFooterList, out,
		        properties);
		out.flush();
		out.close();
	}
	
	private static class HtmlToPdfDTO{
		private String inputhtmlpath="";
		private String outputpdfpath="";
		private String headerpath="";
		private String footerpath="";
		public String getInputhtmlpath() {
			return inputhtmlpath;
		}
		public void setInputhtmlpath(String inputhtmlpath) {
			this.inputhtmlpath = inputhtmlpath;
		}
		public String getOutputpdfpath() {
			return outputpdfpath;
		}
		public void setOutputpdfpath(String outputpdfpath) {
			this.outputpdfpath = outputpdfpath;
		}
	}
	
	public static final Map<String, String> SYS_ENV = System.getenv();
	public static final String ENL_APP_HOME = SYS_ENV.get("ENL_APP_HOME");
	
	public static void initLog4j(){
		System.out.println("ENL_APP_HOME is :"+ ENL_APP_HOME+":");
		String log4jPath = ENL_APP_HOME + File.separator
				+ "Enlighted" + File.separator + "ems_log4j" + File.separator
				+ "log4j.properties";
		System.out.println("log4jPath is :"+ log4jPath+":");
		int intervalToWatchlogFile = 300000; // in ms
		try {
			PropertyConfigurator.configureAndWatch(log4jPath,
					intervalToWatchlogFile);
		} catch (Exception ed) {
			System.out
					.println("Could not enable the regular watching of log4j.properties file:"+ ed.getMessage());
		}
	}
	public static void main(String args[]){
		try {
			//initLog4j();
			//syslog.info("ENLCommandLineUtils called with arguments as :"+Arrays.deepToString(args)+":");
			String feature = "";
			String featureVal = "";
			String input = "";
			String output = "";
			KeyValBean b ;
			if (args != null && args.length >= 1){
				//Get the featureset
				b= getKeyValFromArg(args, 0);
				if (b != null){
					feature  = b.key;
					featureVal = b.val;
				}
				if (featureVal.equals("genpdf")){
					final HtmlToPdfDTO dto = new HtmlToPdfDTO();
					
					b= getKeyValFromArg(args, 1);
					if (b != null){
						if (b.key.equals("input")){
							input = b.val;
						}
					}
					b= getKeyValFromArg(args, 2);
					if (b != null){
						if (b.key.equals("output")){
							output = b.val;
						}
					}
					b= getKeyValFromArg(args, 3);
					if (b != null){
						if (b.key.equals("header")){
							dto.headerpath = b.val;
						}else if (b.key.equals("footer")){
							dto.footerpath = b.val;
						}
					}
					b= getKeyValFromArg(args, 4);
					if (b != null){
						if (b.key.equals("footer")){
							dto.footerpath = b.val;
						}else if (b.key.equals("header")){
							dto.headerpath = b.val;
						}
					}
					dto.setInputhtmlpath(input);
					dto.setOutputpdfpath(output);
					convertToPdf(dto);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception occured"+ e.getMessage());
			//syslog.error("**Exception occured:",e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	private static final String EQUAL_STR = "=";
	private static final String EMPTY_STR = "";
	private static KeyValBean getKeyValFromArg(final String arg[], int pos){
		if (arg == null || arg.length <= pos){
			return null;
		}
		final String temp[] = arg[pos].split(EQUAL_STR);
		if(temp.length < 2){
			return null;
		}
		return new KeyValBean(temp[0], temp[1]);
	}
	private static class KeyValBean{
		public final String key;
		public final String val;
		public KeyValBean(final String key, final String val){
			this.key = key;
			this.val = val;
		}
	}
}
