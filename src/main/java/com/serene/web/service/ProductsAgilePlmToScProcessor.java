package com.serene.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.serene.job.common.BatchJobContext;
import com.serene.job.repo.model.FieldDetail;
import com.serene.job.util.ExpressionEvaluator;
import com.serene.job.util.JobUtils;
import com.serene.web.util.Constants;
import com.serene.ws.util.ConfigUtil;
@Service
@Lazy(true)
@Scope("prototype")
public class ProductsAgilePlmToScProcessor implements ItemProcessor<Map<String,Object>, Map<String,Object>>{

	private static Logger log = LoggerFactory.getLogger(ProductsAgilePlmToScProcessor.class); 
	
	@Resource
	protected JobUtils jobUtils ; 

	@Resource
	protected BatchJobContext batchJobContext ;

	@Resource
	private ExpressionEvaluator expressionEvaluator ;

	@Resource
	private GenericOscRestService genericOscRestService;

	@Override
	public Map<String, Object> process(Map<String, Object> item) throws Exception {
		Map<String,Object> value = new HashMap<String,Object>();
		try{
			List<FieldDetail> fieldMappings = batchJobContext.getCurrentObject().getFieldMapping();		
			String externalSystemIdFieldName = batchJobContext.getCurrentObject().getItemWriter().getExternalSysFieldId();
	
			for (FieldDetail fieldDetail : fieldMappings) {
				if(fieldDetail.getStatus()){
					Object fromFieldValue = null;
					if (StringUtils.isNotBlank(fieldDetail.getFromField())) {
						fromFieldValue = item.get(fieldDetail.getFromField());
					}else{
						fromFieldValue = fieldDetail.getDefaultValue();
					}
					value.put(fieldDetail.getToField(), fromFieldValue);
				}
			}
			String url = ConfigUtil.getProperty(Constants.SC_PRODUCT_REST_URL);
			String resp = genericOscRestService.getByExternalSystemId(url,externalSystemIdFieldName,String.valueOf(value.get(externalSystemIdFieldName)));
			Long inventoryItemId = null;
			if(StringUtils.isNotBlank(resp)){
				JsonElement jelement = new JsonParser().parse(resp);
				JsonObject  jobject = jelement.getAsJsonObject();
				JsonArray jarray = jobject.getAsJsonArray("items");
				if(jarray!=null && jarray.size()>0){
					jobject = jarray.get(0).getAsJsonObject();
					inventoryItemId = jobject.get(batchJobContext.getCurrentObject().getItemWriter().getObjectIdField()).getAsLong();
					if(inventoryItemId!=null){			
						value.put(batchJobContext.getCurrentObject().getItemWriter().getObjectIdField(), inventoryItemId);
						boolean updateInSc = false;
						for(String key: value.keySet()){
							if(!Objects.equals(value.get(key),jobject.get(key))){
								updateInSc = true;
								break;
							}
						}
						if(!updateInSc){
							log.debug("Skipping unchanged product- "+item.get(batchJobContext.getCurrentObject().getItemReader().getObjectIdField()));
							return null;
						}
					}
				}
			}
		}catch(Exception e){
			log.error("Error while processing data ",e);
            this.logErrorInDb(item, e);
            return null;
		}
		return value;
	}
	
    public void logErrorInDb(Map<String, Object> item, Exception e) {
        try {
            this.jobUtils.logItemErrorStatus(e, item);        }
        catch (Exception e1) {
            log.error("Error while writing error log ", (Throwable)e1);
        }
    }
}
