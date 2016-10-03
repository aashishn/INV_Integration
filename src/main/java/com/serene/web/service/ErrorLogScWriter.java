package com.serene.web.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.serene.job.common.BatchJobContext;
import com.serene.job.util.JobUtils;
import com.serene.web.email.EmailHtmlSender;
import com.serene.web.email.EmailStatus;

@Service("ERROR_LOG_EMAIL_WRITER")
@Lazy(true)
@Scope("prototype")
public class ErrorLogScWriter implements ItemWriter<Map<String,Object>> , StepExecutionListener  {

	private static Logger log = LoggerFactory.getLogger(ErrorLogScWriter.class); 

	@Resource
	private BatchJobContext batchJobContext ;

	@Resource
	private GenericOscRestService genericOscRestService;

	@Resource 
	protected JobUtils jobUtils ;

	@Resource
	private EmailHtmlSender emailHtmlSender;
	
    @Value("${email.to}")
    private String toAddresses;

	@Override
	public void write(List<? extends Map<String, Object>> items) throws Exception {
		log.debug(">write");
		try {
			Context context = new Context();
			context.setVariable("results", items);
//			String toAddresses=ConfigUtil.getProperty(Constants.EMAIL_TO_LIST);
			EmailStatus emailStatus = emailHtmlSender.send(toAddresses, "Integration error notification", "integration_notification_email", context);
		} catch (Exception e) {
			log.error("Error while writing data ",e);
			throw e;
		}finally{
			//				for (Map<String, Object> it : items) {
			//					it.put(batchJobContext.getCurrentObject().getItemReader().getObjectIdField(), it.get(batchJobContext.getCurrentObject().getItemWriter().getExternalSysFieldId()));
			//				}
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

