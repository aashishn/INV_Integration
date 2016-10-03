package com.serene.web.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.serene.job.common.BatchJobContext;
import com.serene.job.util.JobUtils;
import com.serene.web.util.CommonUtil;
import com.serene.web.util.Constants;
import com.serene.ws.util.ConfigUtil;

@Service("PRODUCT_SC_WRITER")
@Lazy(true)
@Scope("prototype")
public class ProductScWriter implements ItemWriter<Map<String,Object>> , StepExecutionListener  {

	private static Logger log = LoggerFactory.getLogger(ProductScWriter.class); 

	@Resource
	private BatchJobContext batchJobContext ;
	
	@Resource
	private GenericOscRestService genericOscRestService;
	
	@Resource 
	protected JobUtils jobUtils ;
	
	@Override
	public void write(List<? extends Map<String, Object>> items) throws Exception {
		log.debug(">write");
		String url = ConfigUtil.getProperty(Constants.SC_PRODUCT_REST_URL);
		for (Map<String, Object> item : items) {
			try {
				Object id = item.get(batchJobContext.getCurrentObject().getItemWriter().getObjectIdField());
				String itemAsJsonString = new Gson().toJson(CommonUtil.getMapForJson(item));			
				String response = null;
				if (id == null){					 
					response = genericOscRestService.create(url, itemAsJsonString);
				}else{
					response = genericOscRestService.updateUsingPatch(url, itemAsJsonString);
				}	
				if(StringUtils.isNotBlank(response)){
					try{
					JsonElement jelement = new JsonParser().parse(response);
					JsonObject  jobject = jelement.getAsJsonObject();
						if(jobject.get(batchJobContext.getCurrentObject().getItemWriter().getObjectIdField())==null){
							throw new Exception("Error while processing "+batchJobContext.getCurrentObject().getItemWriter().getObjectName()+" in Sales Cloud. Response from Sales Cloud: "+response);
						}
					}catch(Exception e){
						throw new Exception("Error while processing "+batchJobContext.getCurrentObject().getItemWriter().getObjectName()+" in Sales Cloud. Response from Sales Cloud: "+response);
					}
				}
		        this.jobUtils.logItem(item, "", "COMPLETED", null);
			} catch (Exception e) {
				log.error("Error while writing data ",e);
                this.logErrorInDb(item, e);
			}finally{
//				for (Map<String, Object> it : items) {
//					it.put(batchJobContext.getCurrentObject().getItemReader().getObjectIdField(), it.get(batchJobContext.getCurrentObject().getItemWriter().getExternalSysFieldId()));
//				}
			}
		}
	}
	
    public void logErrorInDb(Map<String, Object> item, Exception e) {
        try {
            this.jobUtils.logItemErrorStatus(e, item);        }
        catch (Exception e1) {
            log.error("Error while writing error log ", (Throwable)e1);
        }
    }

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
