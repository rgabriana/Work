package com.emscloud.communication.adaptor;

import java.util.List;

import com.emscloud.communication.ResponseWrapper;
import com.emscloud.model.EmInstance;
import com.sun.jersey.api.client.GenericType;

public interface Adapter {
	public String getContextUrl();
	
	public <T> List<ResponseWrapper<T>> executeGet(List<EmInstance> emList,
			String urls, String mediaType, Class<?> responseObject);

	public <T> List<ResponseWrapper<List<T>>> executeGet(
			List<EmInstance> emList, String urls, String mediaType,
			GenericType<List<T>> genericType);

	public <T> List<ResponseWrapper<T>> executePost(List<EmInstance> emList,
			String urls, String inputMediaType, String outputMediaType,
			Class<?> responseObject, Object requestObject);

	public <T> List<ResponseWrapper<List<T>>> executePost(
			List<EmInstance> emList, String urls, String inputMediaType,
			String outputMediaType, GenericType<List<T>> responseObject,
			Object requestObject);

	public <T> ResponseWrapper<List<T>> executeGet(EmInstance em, String urls,
			String mediaType, GenericType<List<T>> responseObject);

	public <T> ResponseWrapper<T> executeGet(EmInstance em, String urls,
			String mediaType, Class<?> responseObject);

	public <T> ResponseWrapper<List<T>> executePost(EmInstance em, String urls,
			String inputMediaType, String outputMediaType,
			GenericType<List<T>> responseObject, Object requestObject);

	public <T> ResponseWrapper<T> executePost(EmInstance em, String urls,
			String inputMediaType, String outputMediaType,
			Class<?> responseObject, Object requestObject);
}
