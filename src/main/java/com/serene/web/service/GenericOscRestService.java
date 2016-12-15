package com.serene.web.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

public interface GenericOscRestService {

	public String create(String url, String jsonString) throws ClientProtocolException, IOException, Exception;
	
	public String updateUsingPatch(String url, String jsonString) throws ClientProtocolException, IOException, Exception;
	
	public String updateUsingPut(String url, String jsonString) throws ClientProtocolException, IOException;
	
	public String getById(String url, Long id) throws ClientProtocolException, IOException, Exception;
	
	public String getAll(String url) throws ClientProtocolException, IOException, Exception;
	
	public String getByExternalSystemId(String url, String externalSystemIdFieldName, Object externalSystemIdFieldValue) throws ClientProtocolException, IOException, Exception;
}
