package com.emscloud.communication.adaptor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.emscloud.communication.ConnectionTemlpate;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.model.EmInstance;
import com.emscloud.model.ReplicaServer;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource.Builder;

@Component("gemAdapter")
public class CloudAdapter implements Adapter {
	private static final String CONTEXT_URL = "/em_cloud_instance" ;
	
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
	
	public <T> ResponseWrapper<T> executeGet(String ip, String urls, String mediaType, Class<?> responseObject) {
		
		ResponseWrapper<T> newResponse = new ResponseWrapper<T>();
		newResponse.setEm(null);
		try {			
			if(ip == null || ip.isEmpty()) {
				logger.error(" Not able to communicate with Server");
			}		
			System.out.println("REQ: " + ip + " " + urls);
			Builder br = connectionTemlpate.executeGet(ip, urls, mediaType);
			if (br != null) {
				ClientResponse response = br.get(ClientResponse.class);
				System.out.println("response - " + response.getClientResponseStatus().getStatusCode());
				if (response.getClientResponseStatus() != null) {
					newResponse.setStatus(response.getClientResponseStatus().getStatusCode());
				} else {
					newResponse.setStatus(-1); // status to symbolize that we got a null status code.
				}
				if (Response.Status.OK.getStatusCode() == response.getClientResponseStatus().getStatusCode()) {
					newResponse.setItems((T) response.getEntity(responseObject));
				} else {
					newResponse.setItems((T) responseObject.newInstance());
				}
				System.out.println("RES: " + ip +  " " + urls + " " + newResponse.getStatus());							
			} else {
				System.out.println("no, br");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return newResponse;
		
	} //end of method executeGet
	
	public <T> ResponseWrapper<T> executeGetList(String ip, String urls, String mediaType, GenericType type) {
		
		ResponseWrapper<T> newResponse = new ResponseWrapper<T>();
		newResponse.setEm(null);
		try {			
			if(ip == null || ip.isEmpty()) {
				logger.error(" Not able to communicate with Server");
			}		
			System.out.println("REQ: " + ip + " " + urls);
			Builder br = connectionTemlpate.executeGet(ip, urls, mediaType);
			if (br != null) {
				ClientResponse response = br.get(ClientResponse.class);
				System.out.println("response - " + response.getClientResponseStatus().getStatusCode());
				if (response.getClientResponseStatus() != null) {
					newResponse.setStatus(response.getClientResponseStatus().getStatusCode());
				} else {
					newResponse.setStatus(-1); // status to symbolize that we got a null status code.
				}				
				if (Response.Status.OK.getStatusCode() == response.getClientResponseStatus().getStatusCode()) {
					newResponse.setItems((T) response.getEntity(type));					
				} else {
					newResponse.setItems((T) type);
				}
				System.out.println("RES: " + ip +  " " + urls + " " + newResponse.getStatus());							
			} else {
				System.out.println("no, br");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return newResponse;
		
	} //end of method executeGetList

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

		@Override
		public String getContextUrl() {
			return CONTEXT_URL;
		}
		
		
		
		public InputStream downloadFile(EmInstance em, String urls) {
			try {
				Builder br = connectionTemlpate.executePost(em, urls,
						MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM);
				if (br != null) {
					ClientResponse cr = br.post(ClientResponse.class);
					if (cr.getClientResponseStatus() != null && Response.Status.OK.getStatusCode() == cr.getClientResponseStatus().getStatusCode()) {
						final InputStream stream = cr.getEntity(InputStream.class);
						return stream;
					}
					else {
						logger.error("ERROR getting file");
					}
				} else {
					logger.error("Not able to communicate with replica");
				}

			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			return null;
		}
		
		
		
		@SuppressWarnings("unchecked")
		public <T> ResponseWrapper<T> executePost(ReplicaServer rs,
				String urls, String inputMediaType, String outputMediaType,
				Class<?> responseObject, Object requestObject) {
			List<ResponseWrapper<T>> responses = null;
			try {
				responses = new ArrayList<ResponseWrapper<T>>();
				
				ResponseWrapper<T> newResponse = new ResponseWrapper<T>();

				Builder br = connectionTemlpate.executePost(rs, urls,
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
				} else {
					logger.error(" Not able to communicate with replica " + rs.getName());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			if(responses != null && responses.size() > 0) {
				return responses.get(0);
			}
			return null;
		}
		


}
