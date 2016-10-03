package com.serene.web.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.serene.web.util.CommonUtil;

@Service
@Scope("prototype")
public class GenericOscRestServiceImpl implements GenericOscRestService{

	private static Logger log = LoggerFactory.getLogger(GenericOscRestServiceImpl.class);
	
	private String USER_AGENT="Mozilla/5.0";//TODO Get from properties file

	public String create(String url, String jsonString) throws ClientProtocolException, IOException{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("User-Agent", USER_AGENT);
		httpPost.addHeader("Authorization", CommonUtil.getAuthorizationStringFromCredentials());

		StringEntity input = new StringEntity(jsonString);
		input.setContentType("application/vnd.oracle.adf.resourceitem+json");
		httpPost.setEntity(input);
		CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

		log.debug("POST Response Status:: "
				+ httpResponse.getStatusLine().getStatusCode());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();

		// print result
		log.debug(response.toString());
		httpClient.close();
		return response.toString();		
	}
	
	public String updateUsingPatch(String url, String jsonString) throws ClientProtocolException, IOException{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPatch httpPatch = new HttpPatch(url);
		httpPatch.addHeader("User-Agent", USER_AGENT);
		httpPatch.addHeader("Authorization", CommonUtil.getAuthorizationStringFromCredentials());

		StringEntity input = new StringEntity(jsonString);
		input.setContentType("application/vnd.oracle.adf.resourceitem+json");
		httpPatch.setEntity(input);
		CloseableHttpResponse httpResponse = httpClient.execute(httpPatch);

		log.debug("POST Response Status:: "
				+ httpResponse.getStatusLine().getStatusCode());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();

		// print result
		log.debug(response.toString());
		httpClient.close();
		return response.toString();		
	}
	
	public String getById(String url, Long id) throws ClientProtocolException, IOException{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		url = url + "/" + String.valueOf(id);
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("User-Agent", USER_AGENT);
		httpGet.addHeader("Authorization", CommonUtil.getAuthorizationStringFromCredentials());
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

		log.debug("GET Response Status:: "
				+ httpResponse.getStatusLine().getStatusCode());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();

		// print result
		log.debug(response.toString());
		httpClient.close();
		return response.toString();		
	}
	
	public String getAll(String url) throws ClientProtocolException, IOException{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("User-Agent", USER_AGENT);
		httpGet.addHeader("Authorization", CommonUtil.getAuthorizationStringFromCredentials());
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

		log.debug("GET Response Status:: "
				+ httpResponse.getStatusLine().getStatusCode());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();

		// print result
		log.debug(response.toString());
		httpClient.close();
		return response.toString();		
	}

	@Override
	public String updateUsingPut(String url, String jsonString) throws ClientProtocolException, IOException {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getByExternalSystemId(String url, String externalSystemIdFieldName, Object externalSystemIdFieldValue)
			throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		if(!(externalSystemIdFieldValue instanceof String))
			externalSystemIdFieldValue = externalSystemIdFieldValue.toString();
		externalSystemIdFieldValue=URLEncoder.encode((String)externalSystemIdFieldValue, "UTF-8");
		url = url + "/?q=" + externalSystemIdFieldName + "=" +externalSystemIdFieldValue;
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("User-Agent", USER_AGENT);
		httpGet.addHeader("Authorization", CommonUtil.getAuthorizationStringFromCredentials());
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

		log.debug("GET Response Status:: "
				+ httpResponse.getStatusLine().getStatusCode());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();

		// print result
		log.debug(response.toString());
		httpClient.close();
		return response.toString();		
	}
}
