/**
 * @author gpatwa
 * Job Test case
 */
package com.serene.web;

import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.serene.job.InitializeJobFromDbMetadata;
import com.serene.job.repo.dao.IntermidiateTableDao;
import com.serene.job.repo.dao.JobSchedulerMetadataDao;
import com.serene.job.repo.model.JobDetail;
import com.serene.job.scheduler.AbstractJobScheduler;
import com.serene.job.util.JobUtils;
import com.serene.web.email.EmailHtmlSender;
import com.serene.ws.service.FusionWebService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class JobTests {

	private static Logger log = Logger.getLogger(JobTests.class.getName()); 
	
	@Resource
	private FusionWebService fusionWebService;  
	
	@Resource
	private JobSchedulerMetadataDao jobSchedulerMetadataDao ;  
	
	@Resource
	protected ApplicationContext applicationContext;
	
	@Resource
	private JobUtils  jobUtils ;
	
	@Resource
	private EmailHtmlSender emailHtmlSender;
	
	@Resource
	private IntermidiateTableDao intermidiateTableDao;

	@Test
	@SuppressWarnings("unchecked")
	public void runJobByName() throws Exception{
		JobDetail j = jobSchedulerMetadataDao.findOne("PRODUCTS_AGILEPLM_TO_SC");
		
		AbstractJobScheduler jobScheduler =  (AbstractJobScheduler) applicationContext.getBean(InitializeJobFromDbMetadata.get(j.getInterfaceType()));
		jobScheduler.setJobName(j.getJobName());
		jobScheduler.loadJob();
		jobScheduler.run();
		log.info(" Test Completed");
	}
	
	@Test
	public void fqlWithChildColumns() throws Exception{
		Map<String,Object> response = fusionWebService.query("Select ParentAccountPartyId,PartyId,OrganizationName,OrganizationDEO_CustomerType_c,OwnerName,OwnerPartyId,SalesTeamMember.AccessLevelCode,SalesTeamMember.CreationDate,SalesTeamMember.LastUpdateDate,SalesTeamMember.ResourceId,SalesTeamMember.ResourceRoleName,SalesTeamMember.TeamMemberId from Account where LastUpdateDate AFTER '1800-10-10T12:34:06.042Z' order by LastUpdateDate asc");   
		log.info(response.toString());
	}
}
