package com.serene.web.service;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.serene.job.common.BatchJobContext;
import com.serene.job.repo.model.JobDetail;
import com.serene.web.util.CommonUtil;
import com.serene.web.util.Constants;
import com.serene.ws.util.ConfigUtil;

public class ProductsScReader implements ItemReader<Map<String,Object>> ,StepExecutionListener{
	private static Logger log = LoggerFactory.getLogger(ProductsScReader.class);

	private String queryCriteria ; 
	
	private Object[] queryParams;
	
	private Queue<Map<String,Object>> items  = new LinkedBlockingQueue<Map<String,Object>>();
	
	@Resource
	private BatchJobContext  batchJobContext ;	
	
	@Resource
	private GenericOscRestService genericOscRestService;

	private String startTime;
	
	@Override
	public ExitStatus afterStep(StepExecution arg0) {
		// cleanup resource
		items.clear();
		return null;
	}

	@Override
	public void beforeStep(StepExecution arg0) {
		JobDetail jobSchedulerMetadata = batchJobContext.getCurrentObject();
        queryCriteria = jobSchedulerMetadata.getItemReader().getQuery();
		startTime = jobSchedulerMetadata.getInterfaceLastPoolTime();
        DateFormat scDate    = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
        try{        	
			if (StringUtils.isNoneBlank(startTime)) {
				Date queryCriteriaDate = scDate.parse(startTime);	
				queryParams = new Object[]{queryCriteriaDate};
			}			
        }catch(Exception e){
			log.error("Error parsing date in before step",e);
		}
		items = new LinkedBlockingQueue<Map<String,Object>>();	
//		try {
//			items = CommonUtil.populateResultForReaderWriter(agilePlmService.getAllProductDelta(queryCriteria, queryParams));
//		} catch (Exception e) {
//			log.error("Error in while fetching records from Agile PLM ",e);
//		}
	}

	@Override
	public Map<String, Object> read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (items.isEmpty()) {			
			String url = ConfigUtil.getProperty(Constants.SC_PRODUCT_REST_URL);
			String resp = genericOscRestService.getByExternalSystemId(url, batchJobContext.getCurrentObject().getInterfaceLastUpdatedField(), startTime);//CommonUtil.populateResultForReaderWriter(agilePlmService.getAllProductDelta(queryCriteria, queryParams));
			List<Map> resultList = new ArrayList<Map>();
			Gson gson = new Gson();
			if(StringUtils.isNotBlank(resp)){
				JsonElement jelement = new JsonParser().parse(resp);
				JsonObject  jobject = jelement.getAsJsonObject();
				JsonArray jarray = jobject.getAsJsonArray("items");
				if(jarray!=null && jarray.size()>0){
					for(int i=1;i<=jarray.size();i++){
						Map<String,Object> map = new HashMap<String,Object>();
//						Type type = new TypeToken<Map<String, Object>>(){}.getType();
						map = gson.fromJson(jarray.get(i).getAsJsonObject(), map.getClass());
						resultList.add(map);
					}
				}
			}
			items = CommonUtil.populateResultForReaderWriter(resultList);
		}
		return items.poll();
	}
}
