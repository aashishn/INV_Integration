package com.serene.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.serene.job.common.BatchJobContext;
import com.serene.job.repo.model.FieldDetail;
import com.serene.job.util.ExpressionEvaluator;
import com.serene.job.util.JobUtils;

public class ProductsScToMapProcessor  implements ItemProcessor<Map<String,Object>, Map<String,Object>>{


	private static Logger log = LoggerFactory.getLogger(ProductsScToMapProcessor.class); 
	
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

