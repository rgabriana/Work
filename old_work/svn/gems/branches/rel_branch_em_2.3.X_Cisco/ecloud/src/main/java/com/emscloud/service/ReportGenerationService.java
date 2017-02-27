package com.emscloud.service;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.reports.BirtEngineFactory;
import com.emscloud.reports.ReportFormat;


@Service("reportGenerationService")
@Transactional(propagation = Propagation.REQUIRED)
public class ReportGenerationService {
	
	@Resource
	BirtEngineFactory birtEngineFactory;
	
	
	public File generateReport(Map<String, Object> context, String reportName, ReportFormat format){
		
		File file = null;
		IRunAndRenderTask task = null;
		try{
			
			ClassPathResource resource  = new ClassPathResource("META-INF/reports/" +reportName);
			InputStream inputStream = resource.getInputStream();

			
			IReportRunnable report = birtEngineFactory.getReportEngine().openReportDesign(inputStream);
			
			
			task = birtEngineFactory.getReportEngine().createRunAndRenderTask(report);
			
			//Set parent classloader for engine
            task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, Thread.currentThread().getContextClassLoader());
            
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                task.setParameterValue(entry.getKey(), entry.getValue());
            }
            
            IRenderOption options = new RenderOption();
            options.setOutputFormat(format.getExtension());
            
            file = File.createTempFile(reportName, "." + options.getOutputFormat());
            options.setOutputFileName(file.getAbsolutePath());
            
            if( options.getOutputFormat().equalsIgnoreCase("html")){
                final HTMLRenderOption htmlOptions = new HTMLRenderOption( options);
                htmlOptions.setImageDirectory("img");
                htmlOptions.setHtmlPagination(false);
                htmlOptions.setHtmlRtLFlag(false);
                htmlOptions.setEmbeddable(false);
                htmlOptions.setSupportedImageFormats("PNG");
 
                //set this if you want your image source url to be altered
                //If using the setBaseImageURL, make sure to set image handler to HTMLServerImageHandler
                //htmlOptions.setBaseImageURL("http://myhost/prependme?image=");
            }else if( options.getOutputFormat().equalsIgnoreCase("pdf")){
                final PDFRenderOption pdfOptions = new PDFRenderOption( options );
                pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
                pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES);
            }
 
            task.setRenderOption(options);
   
            
            //run and render report
            task.run();
 
           

			
		}catch(Exception e){
			
		}finally{
			 task.close();
		}
		
		return file;
	}

}
