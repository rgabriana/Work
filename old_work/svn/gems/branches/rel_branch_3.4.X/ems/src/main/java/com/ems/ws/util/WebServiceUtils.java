package com.ems.ws.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;

import com.ems.model.DashboardRecord;
import com.ems.model.PlugloadProfileHandler;
import com.ems.model.ProfileHandler;
import com.ems.service.EnergyConsumptionManager;

/**
 * @author Sameer Surjikar
 * 
 */
public class WebServiceUtils {
	static final Logger logger = Logger.getLogger(WebServiceUtils.class
			.getName());

	/**
	 * @param Takes
	 *            in oRecords (DashboardRecord ) with local timezone.
	 * @return List of Records ( DashboardRecord ) with GMT timezone
	 */
	@SuppressWarnings("unused")
	public static List<DashboardRecord> convertToGMT(
			List<DashboardRecord> oRecords) {
		if ((!oRecords.isEmpty()) || (oRecords != null)) {
			List<DashboardRecord> gmtRecords = new ArrayList<DashboardRecord>();
			TimeZone localTimeZone = getServerTimeZone();
			Calendar oldCal = Calendar.getInstance(getServerTimeZone()) ;		
			Calendar newCal = Calendar.getInstance(new SimpleTimeZone(0, "GMT")) ;
			DashboardRecord record = null;
			Date recordDate = null;
			Iterator<DashboardRecord> itr = oRecords.iterator();
			while (itr.hasNext()) {
				record = itr.next();
				recordDate = record.getCaptureOn();			
				oldCal.setTime(recordDate) ;
				newCal.setTimeInMillis(oldCal.getTimeInMillis() + TimeZone.getTimeZone("GMT").getOffset(oldCal.getTimeInMillis()) - localTimeZone.getOffset(oldCal.getTimeInMillis())) ;
				record.setCaptureOn(newCal.getTime());
				gmtRecords.add(record);

			}

			return gmtRecords;
		} else {
			return oRecords;
		}
	}

	/**
	 * @return Server Time Zone.
	 */
	public static TimeZone getServerTimeZone() {
		final TimeZone timeZone = TimeZone.getDefault();
		
		return timeZone;
	}
	
	public static <T> String convertModelToString(T model) {
        String result = null;
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(model.getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(model, sw);
            result = sw.toString();
        } catch (JAXBException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
	}
	
	public static <T> ProfileHandler convertPhStringToModel(String content) throws JAXBException, XMLStreamException { 
        JAXBContext jc = JAXBContext.newInstance(ProfileHandler.class);
        XMLInputFactory xif = XMLInputFactory.newInstance();  
        StreamSource source = new StreamSource(new StringReader(content));
        XMLStreamReader xsr = xif.createXMLStreamReader(source);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ProfileHandler phObj = (ProfileHandler) unmarshaller.unmarshal(xsr);
        return phObj;
	}
	
	public static <T> PlugloadProfileHandler convertPlPhStringToModel(String content) throws JAXBException, XMLStreamException { 
        JAXBContext jc = JAXBContext.newInstance(PlugloadProfileHandler.class);
        XMLInputFactory xif = XMLInputFactory.newInstance();  
        StreamSource source = new StreamSource(new StringReader(content));
        XMLStreamReader xsr = xif.createXMLStreamReader(source);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        PlugloadProfileHandler plphObj = (PlugloadProfileHandler) unmarshaller.unmarshal(xsr);
        return plphObj;
	}
}
