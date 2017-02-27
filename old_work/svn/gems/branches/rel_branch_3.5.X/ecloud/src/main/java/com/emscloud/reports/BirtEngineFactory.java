package com.emscloud.reports;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("birtEngineFactory")
public class BirtEngineFactory  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    IReportEngine reportEngine = null;
    EngineConfig enginerConfig = null;
    IDesignEngine designEngine = null;
    
    @Resource
    ApplicationContext applicationContext;

    @PostConstruct
    private void initialize() throws Exception {
        enginerConfig = new EngineConfig();
      //  config.setBIRTHome("");
        
    //    ClassPathResource resource = new ClassPathResource("META-INF/reports");
    //    enginerConfig.setResourcePath(resource.getFile().getAbsoluteFile().toString());
        enginerConfig.getAppContext().put("PARENT_CLASSLOADER", BirtEngineFactory.class.getClassLoader());
        enginerConfig.getAppContext().put("spring", applicationContext);
        
        Platform.startup(enginerConfig);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        reportEngine = factory.createReportEngine(enginerConfig);
        DesignConfig dConfig = new DesignConfig();
        IDesignEngineFactory designEngineFactory =
                (IDesignEngineFactory) Platform.createFactoryObject
                        (IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
        designEngine = designEngineFactory.createDesignEngine(dConfig);
    }

    @PreDestroy
    private void destroy() {
        reportEngine.destroy();
        Platform.shutdown();
        RegistryProviderFactory.releaseDefault(); 
    }

	public IReportEngine getReportEngine() {
		return reportEngine;
	}

    

}
