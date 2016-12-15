package com.serene.web.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import com.serene.ws.util.ConfigUtil;


public class CommonUtil {

	public static Queue<Map<String,Object>> populateResultForReaderWriter(Object response){
		Queue<Map<String,Object>> items  = new LinkedBlockingQueue<Map<String,Object>>();
		if (response instanceof AbstractMap) {
			Object data = ((Map<String, Object>) response).get("Value");
			if (data == null) {
				items.add((Map<String, Object>) response);
			} else if (data instanceof AbstractMap) {
				items.add((Map<String, Object>) data);
			} else if (((Map<String, Object>) response).get("Value")  instanceof AbstractList){
				List<Map<String,Object>> payload = (List<Map<String,Object>> )data;
				for (Map<String, Object> map : payload) {
					if (map.get("Value") instanceof AbstractList) {
						List<Map<String, Object>> values = (List) map.get("Value");
						for (Map<String, Object> value : values) {
							items.add(value);	
						}
					} else {
						items.add(map);
					}
				}
			}
		} else if (response  instanceof AbstractList){
			List<Map<String,Object>> payload = (List<Map<String,Object>> )response;
			for (Map<String, Object> map : payload) {
				if (map.get("Value") instanceof AbstractList) {
					List<Map<String, Object>> values = (List) map.get("Value");
					for (Map<String, Object> value : values) {
						items.add(value);	
					}
				} else {
					items.add(map);
				}
			}
		}
		return items;
	}

	public static String getAuthorizationStringFromCredentials(){
		String name = ConfigUtil.getProperty(Constants.SC_USERNAME);
		String password = ConfigUtil.getProperty(Constants.SC_PASSWORD);
		String authString = name + ":" + password;
		byte[] authEncBytes = Base64Utils.encode(authString.getBytes());
		return "Basic " + new String(authEncBytes);
	}

	public static String getJsonString(Map<String, Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if(item!=null){
			int count = 1;
			for(String key:item.keySet()){	
				String keyFinal = "\""+key+"\"";
				Object value = item.get(key);
				String valueFinal = value!=null ? (!(value instanceof String) && StringUtils.isNumeric(String.valueOf(value))? String.valueOf(value):"\""+String.valueOf(value)+"\""):null;
				if(count==item.size()){
					sb.append(keyFinal+" : "+valueFinal);
				}else{
					sb.append(keyFinal+" : "+valueFinal+",");
				}
				count++;
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static Map<String, Object> getMapForJson(Map<String, Object> fromMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for(String key:fromMap.keySet()){
			if(fromMap.get(key) instanceof Integer || fromMap.get(key) instanceof Long){
				resultMap.put(key, Long.valueOf(String.valueOf(fromMap.get(key))));
			}else{
				if(fromMap.get(key)!=null){
					resultMap.put(key, fromMap.get(key).toString());
				}else{
					resultMap.put(key, null);
				}
			}
		}
		return resultMap;
	}
	
	public static boolean checkIfDate(String value,String dateFormat){
		  DateFormat sdf    = new SimpleDateFormat(dateFormat);
		  try{
			  sdf.parse(value);
			  return true;
		  }catch(Exception e){
			  return false;
		  }
	}

}
