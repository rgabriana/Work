package com.emscloud.communication;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.vos.Wrapper;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.types.FacilityType;



@Service("communicationUtils")
@Transactional(propagation = Propagation.REQUIRED)
public class CommunicationUtils {

	static final Logger logger = Logger.getLogger(CommunicationUtils.class
			.getName());
	@Resource
	private FacilityEmMappingManager facilityEmMappingManager;
	@Resource
	private EmInstanceManager emInstanceManager;
	

	public ArrayList<EmInstance> getEmMap(Facility facility) {
		ArrayList<EmInstance> emList = new ArrayList<EmInstance>();
		ArrayList<Facility> floorFacilitys = (ArrayList<Facility>) getFloor(facility);
		Facility floor = null;
		FacilityEmMapping facEmMap = null;
		EmInstance em = null;
		try {
			if (floorFacilitys != null && !floorFacilitys.isEmpty()) {
				Iterator<Facility> itr = floorFacilitys.iterator();
				while (itr.hasNext()) {
					floor = itr.next();
					facEmMap = facilityEmMappingManager
							.getFacilityEmMappingOnFacilityId(floor.getId());
					if (facEmMap != null) {
						em = emInstanceManager
								.getEmInstance(facEmMap.getEmId());
						emList.add(em);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return emList;
	}

	public List<Facility> getFloor(Facility facility) {

		ArrayList<Facility> floorFac = new ArrayList<Facility>();
		if (facility.getType().intValue() == FacilityType.FLOOR.ordinal()) {
			floorFac.add(facility);
		} else {
			Set<Facility> childFac = facility.getChildFacilities();

			Iterator<Facility> itr = childFac.iterator();
			while (itr.hasNext()) {
				Facility next = itr.next();
				if (next.getType().intValue() == FacilityType.FLOOR.ordinal()) {
					floorFac.add(next);
				} else {
					floorFac.addAll(getFloor(next));
				}
			}
		}
		return floorFac;
	}

	/**
	 * Unmarshal XML to Wrapper and return List value.
	 */
	public static <T> List<T> unmarshal(Unmarshaller unmarshaller,
			Class<T> clazz, String xml) throws JAXBException {
		StreamSource xmlSource = new StreamSource(new StringReader(xml));
		Wrapper<T> wrapper = (Wrapper<T>) unmarshaller.unmarshal(xmlSource,
				Wrapper.class).getValue();
		return wrapper.getItems();
	}

	/**
	 * Wrap List in Wrapper, then leverage JAXBElement to supply root element
	 * information.
	 */
	public static String marshal(Marshaller marshaller, List<?> list,
			String name) throws JAXBException {
		StringWriter sw = new StringWriter();
		QName qName = new QName(name);
		Wrapper wrapper = new Wrapper(list);
		JAXBElement<Wrapper> jaxbElement = new JAXBElement<Wrapper>(qName,
				Wrapper.class, wrapper);
		marshaller.marshal(jaxbElement, sw);
		return sw.toString();
	}

	public <T> String convertModelListToString(List<?> list, Class<?> modelClass) {
		String result = null;
		try {
			JAXBContext jc = JAXBContext.newInstance(Wrapper.class, modelClass);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			result = marshal(marshaller, list, modelClass.getName());

		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public <T> String convertModelToString(T model) {
		String result = null;
		StringWriter sw = new StringWriter();
		try {
			JAXBContext jc = JAXBContext.newInstance(model.getClass());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			// QName qName = new QName(modelClass.getName());

			/*
			 * JAXBElement<modelClass> jaxbElement = new
			 * JAXBElement<modelClass>(qName, modelClass, model);
			 */
			marshaller.marshal(model, sw);
			result = sw.toString();

		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public static String getJSONString(Object object) {

		String output = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			output = mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

}
