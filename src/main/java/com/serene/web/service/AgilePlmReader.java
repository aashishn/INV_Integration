package com.serene.web.service;

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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.serene.job.common.BatchJobContext;
import com.serene.job.repo.model.FieldDetail;
import com.serene.job.repo.model.JobDetail;
import com.serene.web.util.CommonUtil;

@Service("PRODUCT_AGILEPLM_READER")
@Lazy(true)
@Scope("prototype")
public class AgilePlmReader implements ItemReader<Map<String,Object>> ,StepExecutionListener {

	private static Logger log = LoggerFactory.getLogger(AgilePlmReader.class);

	private String queryCriteria ; 
	
	private Object[] queryParams;
	
	private Queue<Map<String,Object>> items  = new LinkedBlockingQueue<Map<String,Object>>();
	
	@Resource
	private BatchJobContext  batchJobContext ;	
	
	@Resource
	private AgilePlmService agilePlmService;
	
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
		String startTime = jobSchedulerMetadata.getInterfaceLastPoolTime();
        DateFormat agilePlmDf    = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
        try{        	
			if (StringUtils.isNoneBlank(startTime)) {
				Date queryCriteriaDate = agilePlmDf.parse(startTime);	
				queryParams = new Object[]{queryCriteriaDate};
			}			
        }catch(Exception e){
			log.error("Error parsing date in before step",e);
		}
		items = new LinkedBlockingQueue<Map<String,Object>>();	
		try {
			List<FieldDetail> fieldMappings = batchJobContext.getCurrentObject().getFieldMapping();		
			List<Integer> attributesList = new ArrayList<Integer>();
			for (FieldDetail fieldDetail : fieldMappings) {
				if(fieldDetail.getStatus()){
					if (StringUtils.isNotBlank(fieldDetail.getFromField()) && StringUtils.isNumeric(fieldDetail.getFromField())){
						attributesList.add(Integer.valueOf(fieldDetail.getFromField()));
					}
				}
			}
			items = CommonUtil.populateResultForReaderWriter(agilePlmService.getAllProductDelta(queryCriteria, queryParams, attributesList));
		} catch (Exception e) {
			log.error("Error in while fetching records from Agile PLM ",e);
		}
	}

	@Override
	public Map<String, Object> read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//		if (items.isEmpty()) {			
//			items = CommonUtil.populateResultForReaderWriter(agilePlmService.getAllProductDelta(queryCriteria, queryParams));
//		}
		return items.poll();
	}

}
