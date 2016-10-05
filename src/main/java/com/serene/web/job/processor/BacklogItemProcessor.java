package com.serene.web.job.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import com.serene.job.common.BatchJobContext;
import com.serene.job.repo.dao.JobSchedulerMetadataDao;
import com.serene.job.repo.model.JobSchedulerMetadata;
import com.serene.job.service.UpdateResponseService;
import com.serene.job.util.CacheUtil;
import com.serene.job.util.JobUtils;
import com.serene.job.util.PayloadBuilder;
import com.serene.ws.service.FusionWebService;

@Service
@Lazy(true)
@Scope("prototype")
public class BacklogItemProcessor implements ItemProcessor<Object, Object> {
	private static final Log log = LogFactory.getLog(BacklogItemProcessor.class);
	@Resource
	protected UpdateResponseService updateResponseService;
	
	@Resource 
	protected JobUtils jobUtils ;
	
	@Resource(name = "fusionWebServiceImpl")
	protected FusionWebService fusionWebService;
	@Resource
	protected BatchJobContext batchJobContext;
	protected List<String> operationTypes = new ArrayList();
	@Resource
	protected PayloadBuilder payloadBuilder;
	@Resource
	private CacheUtil cacheUtil;
	@Resource
	private JobSchedulerMetadataDao jobSchedulerMetadataDao;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process(Object item) throws Exception {
		String id=null;
		String recordName=null;
		Map<String, Object> data = (Map<String, Object>) item;
		for (Iterator i = data.keySet().iterator(); i.hasNext();) {
			 String key = (String) i.next();
		     Object value = (Object) data.get(key);
			if(key.equalsIgnoreCase("Id"))
				id=value.toString();
			if(key.equalsIgnoreCase("RecordName"))
				recordName=value.toString();
		}

		try {
			int backlogCount =0;
			if(NumberUtils.isNumber(recordName))
			 {
				 NamedParameterJdbcTemplate jdbcTemplate = cacheUtil.getJdbcTemplate(this.batchJobContext.getCurrentObject().getItemWriter().getDataSource().getDataSourceName());
		         String sql = "select count(EBS_TRANSACTION_ID)  from XXOE_SALES_ORDER_BACKLOGS_V where EBS_TRANSACTION_ID= :EBS_TRANSACTION_ID";
		         SqlParameterSource namedParameters = new MapSqlParameterSource("EBS_TRANSACTION_ID", recordName);
		         backlogCount=jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
			 }

			// If this does not exist in EBS, delete it
			if(backlogCount==0)		
			{
				this.operationTypes.add("DELETE");
				Map<String, Object> payload = this.payloadBuilder.getPayload(data, this.batchJobContext.getCurrentObject());
				Map<String, Object> response = this.fusionWebService.delete(payload,
						this.batchJobContext.getCurrentObject().getItemReader().getObjectName());

				this.updateResponseService.updateResponse(data, operationTypes, response);
		        this.jobUtils.logItem(data, "", "COMPLETED", "DELETED SUCCESSFULLY FROM SALES CLOUD");
			}
			else
	        this.jobUtils.logItem(data, "", "COMPLETED", null);
			
		} catch (Exception e) {
			log.error("Error while deleting data ",e);
            this.logErrorInDb(data,e);
		}
		updateMetadata(data);
		return null;
	}
	
	  public void updateMetadata(Map<String, Object> data)
	  {
		  JobSchedulerMetadata jobSchedulerMetadata = (JobSchedulerMetadata)this.jobSchedulerMetadataDao.findOne(this.batchJobContext.getCurrentObject().getJobName());
		    Object lastUpdatedTime = data.get(jobSchedulerMetadata.getInterfaceLastUpdatedField());
		    if (lastUpdatedTime != null) {
		         jobSchedulerMetadata.setInterfaceLastPoolTime(lastUpdatedTime.toString());
		     }
		     this.jobSchedulerMetadataDao.save(jobSchedulerMetadata);
	  }
	  
	   public void logErrorInDb(Map<String, Object> item, Exception e) {
	        try {
	            this.jobUtils.logItemErrorStatus(e, item);        }
	        catch (Exception e1) {
	            log.error("Error while writing error log ", (Throwable)e1);
	        }
	    }
}
