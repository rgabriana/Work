package com.emscloud.communication.adaptor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.emscloud.communication.ConnectionTemlpate;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.model.EmInstance;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource.Builder;

@Component("uemAdapter")
public class CloudAdapter implements Adapter {
	@Resource
	ConnectionTemlpate<?> connectionTemlpate;
	static final Logger logger = Logger.getLogger(CloudAdapter.class.getName());

	public <T> List<ResponseWrapper<T>> executeGet(List<EmInstance> emList,
			String urls, String mediaType, Class<?> responseObject) {
		List<ResponseWrapper<T>> responses = null;
		try {
			responses = new ArrayList<ResponseWrapper<T>>();
			if (!emList.isEmpty() && emList != null) {
				for (EmInstance em : emList) {
					ResponseWrapper<T> newResponse = new ResponseWrapper<T>();
					newResponse.setEm(em);
					logger.debug("REQ: " + em.getMacId() 
							+ " " + urls);
					Builder br = connectionTemlpate.executeGet(em, urls,
							mediaType);
					if (br != null) {
						ClientResponse response = br.get(ClientResponse.class);
						if (response.getClientResponseStatus() != null)
							newResponse.setStatus(response
									.getClientResponseStatus().getStatusCode());
						else
							newResponse.setStatus(-1); // status to symbolize
														// that we got a null
														// status code.
						if (Response.Status.OK.getStatusCode() == response
								.getClientResponseStatus().getStatusCode()) {
							newResponse.setItems((T) response
									.getEntity(responseObject));
						} else {
							newResponse.setItems((T) responseObject
									.newInstance());
						}
						responses.add(newResponse);
						logger.debug("RES: " + em.getMacId() +  " " + urls + " "
								+ newResponse.getStatus());
					} else {
						logger.error(em.getMacId() + " Not able to communicate with EM ");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return responses;
	}

	public <T> List<ResponseWrapper<List<T>>> executeGet(
			List<EmInstance> emList, String urls, String mediaType,
			GenericType<List<T>> genericType) {
		List<ResponseWrapper<List<T>>> responses = null;
		try {
			responses = new ArrayList<ResponseWrapper<List<T>>>();
			if (!emList.isEmpty() && emList != null) {
				for (EmInstance em : emList) {
					List<T> result = null;
					ResponseWrapper<List<T>> newResponse = new ResponseWrapper<List<T>>();
					newResponse.setEm(em);
					logger.debug("REQ: " + em.getMacId() + " " + urls);

					Builder br = connectionTemlpate.executeGet(em, urls,
							mediaType);
					if (br != null) {
						ClientResponse response = br.get(ClientResponse.class);
						if (response.getClientResponseStatus() != null)
							newResponse.setStatus(response
									.getClientResponseStatus().getStatusCode());
						else
							newResponse.setStatus(-1); // status to symbolize
														// that we got a null
														// status code.
						if (Response.Status.OK.getStatusCode() == response
								.getClientResponseStatus().getStatusCode()) {
							newResponse.setItems((List<T>) response
									.getEntity(genericType));
						} else {
							newResponse.setItems((List<T>) new ArrayList());
						}
						responses.add(newResponse);
						logger.debug("RES: " + em.getMacId() + " " + urls + " "
								+ newResponse.getStatus());
					} else {
						logger.error(em.getMacId() + " Not able to communicate with EM ");
					}

				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return responses;
	}

	public <T> List<ResponseWrapper<T>> executePost(List<EmInstance> emList,
			String urls, String inputMediaType, String outputMediaType,
			Class<?> responseObject, Object requestObject) {
		List<ResponseWrapper<T>> responses = null;
		try {
			responses = new ArrayList<ResponseWrapper<T>>();
			if (!emList.isEmpty() && emList != null) {
				for (EmInstance em : emList) {
					ResponseWrapper<T> newResponse = new ResponseWrapper<T>();
					newResponse.setEm(em);
					logger.debug("REQ: " + em.getMacId() + " " + urls);

					Builder br = connectionTemlpate.executePost(em, urls,
							inputMediaType, outputMediaType);
					if (br != null) {
						ClientResponse cr = br.post(ClientResponse.class,
								requestObject);
						if (cr.getClientResponseStatus() != null)
							newResponse.setStatus(cr.getClientResponseStatus()
									.getStatusCode());
						else
							newResponse.setStatus(-1); // status to symbolize
														// that we got a null
														// status code.
						if (Response.Status.OK.getStatusCode() == cr
								.getClientResponseStatus().getStatusCode()) {
							newResponse.setItems((T) cr
									.getEntity(responseObject));
						} else {
							newResponse.setItems((T) responseObject
									.newInstance());
						}
						responses.add(newResponse);
						logger.debug("RES: " + em.getMacId() + " " + urls + " "
								+ newResponse.getStatus());
					} else {
						logger.error(em.getMacId() + " Not able to communicate with EM ");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return responses;
	}

	public <T> List<ResponseWrapper<List<T>>> executePost(
			List<EmInstance> emList, String urls, String inputMediaType,
			String outputMediaType, GenericType<List<T>> responseObject,
			Object requestObject) {
		List<ResponseWrapper<List<T>>> responses = null;
		try {
			responses = new ArrayList<ResponseWrapper<List<T>>>();
			if (!emList.isEmpty() && emList != null) {
				for (EmInstance em : emList) {
					List<T> result = null;
					ResponseWrapper<List<T>> newResponse = new ResponseWrapper<List<T>>();
					newResponse.setEm(em);
					logger.debug("REQ: " + em.getMacId() + " " + urls);

					Builder br = connectionTemlpate.executePost(em, urls,
							inputMediaType, outputMediaType);
					if (br != null) {
						ClientResponse cr = br.post(ClientResponse.class,
								requestObject);
						if (cr.getClientResponseStatus() != null)
							newResponse.setStatus(cr.getClientResponseStatus()
									.getStatusCode());
						else
							newResponse.setStatus(-1); // status to symbolize
														// that we got a null
														// status code.
						if (Response.Status.OK.getStatusCode() == cr
								.getClientResponseStatus().getStatusCode()) {
							newResponse.setItems((List<T>) cr
									.getEntity(responseObject));
						} else {
							newResponse.setItems((List<T>) new ArrayList());
						}
						responses.add(newResponse);
						logger.debug("RES: " + em.getMacId() + " " + urls + " "
								+ newResponse.getStatus());
					} else {
						logger.error(em.getMacId() + " Not able to communicate with EM ");
					}

				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return responses;
	}

	public <T> ResponseWrapper<List<T>> executeGet(EmInstance em, String urls,
			String mediaType, GenericType<List<T>> responseObject) {
		ResponseWrapper<List<T>> sendResponse = new ResponseWrapper<List<T>>();
		ArrayList<EmInstance> temp = new ArrayList<EmInstance>();
		try {
			temp.add(em);
			logger.debug("REQ: " + em.getMacId() + " " + urls);
			List<ResponseWrapper<List<T>>> reponse = executeGet(temp, urls,
					mediaType, responseObject);
			sendResponse = reponse.get(0);
			if (sendResponse != null) {
				logger.debug("RES: " + em.getMacId() + " " + urls + " "
						+ sendResponse.getStatus());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			temp = null;
		}
		return sendResponse;
	}
	
	
	public <T> ResponseWrapper<T> executeGet(EmInstance em,
			String urls, String mediaType, Class<?> responseObject) {
		
		ResponseWrapper<T> sendResponse = new ResponseWrapper<T>();
		ArrayList<EmInstance> temp = new ArrayList<EmInstance>();
		try {
			temp.add(em);
			logger.debug("REQ: " + em.getMacId() + " " + urls);
			List<ResponseWrapper<T>> reponse = executeGet(temp, urls,
					mediaType, responseObject);
			sendResponse = reponse.get(0);
			if (sendResponse != null) {
				logger.debug("RES: " + em.getMacId()+ " " + urls + " "
						+ sendResponse.getStatus());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			temp = null;
		}
		return sendResponse;
	}
	
	
	public <T> ResponseWrapper<List<T>> executePost(EmInstance em, String urls, String inputMediaType,
			String outputMediaType, GenericType<List<T>> responseObject,
			Object requestObject) {
		ResponseWrapper<List<T>> sendResponse = new ResponseWrapper<List<T>>();
		ArrayList<EmInstance> temp = new ArrayList<EmInstance>();
		try {
			temp.add(em);
			logger.debug("REQ: " + em.getMacId() + " " + urls);

			List<ResponseWrapper<List<T>>> reponse = executePost(temp, urls,
					inputMediaType,outputMediaType, responseObject,requestObject);
			sendResponse = reponse.get(0);
			if (sendResponse != null) {
				logger.debug("RES: " + em.getMacId() + " " + urls + " "
						+ sendResponse.getStatus());
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			temp = null;
		}
		return sendResponse;
	}
	
	
	   public <T> ResponseWrapper<T> executePost(EmInstance em, String urls, String inputMediaType, String outputMediaType,
	            Class<?> responseObject, Object requestObject) {
	        ResponseWrapper<T> sendResponse = new ResponseWrapper<T>();
	        ArrayList<EmInstance> temp = new ArrayList<EmInstance>();
	        try {
	            temp.add(em);
	            logger.debug("REQ: " + em.getMacId() + " " + urls);

	            List<ResponseWrapper<T>> reponse = executePost(temp, urls,
	                    inputMediaType,outputMediaType, responseObject,requestObject);
	            sendResponse = reponse.get(0);
	            if (sendResponse != null) {
	                logger.debug("RES: " + em.getMacId() + " " + urls + " "
	                        + sendResponse.getStatus());
	            }
	        } catch (Exception ex) {
	            logger.error(ex.getMessage());
	        } finally {
	            temp = null;
	        }
	        return sendResponse;
	    }




}
