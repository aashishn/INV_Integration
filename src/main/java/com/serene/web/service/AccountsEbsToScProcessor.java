package com.serene.web.service;

import java.util.HashMap;
import java.util.Map;

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
import com.serene.job.util.ExpressionEvaluator;
import com.serene.job.util.JobUtils;
import com.serene.web.util.Constants;
import com.serene.ws.util.ConfigUtil;

@Service
@Lazy(true)
@Scope("prototype")
public class AccountsEbsToScProcessor implements ItemProcessor<Map<String,Object>, Map<String,Object>>{

	private static Logger log = LoggerFactory.getLogger(AccountsEbsToScProcessor.class); 

	@Resource
	protected JobUtils jobUtils ; 

	@Resource
	protected BatchJobContext batchJobContext ;

	@Resource
	private ExpressionEvaluator expressionEvaluator ;

	@Resource
	private GenericOscRestService genericOscRestService;
	
	@Resource
	private AccountOscWsClient accountOscWsClient; 

    private static final Map<String, String> salesChannelMap;
    private static final Map<String, Boolean> billToShipToMap;

    static
    {
    	salesChannelMap = new HashMap<String, String>();
    	salesChannelMap.put("HUB", "Hub");
    	salesChannelMap.put("INDIRECT", "Indirect");
    	salesChannelMap.put("DISTI", "Distributor");
    	salesChannelMap.put("PARENT", "Parent Customer");
    	salesChannelMap.put("CM", "CM");
    	salesChannelMap.put("DIRECT", "Direct");
    	salesChannelMap.put("INTERCO", "Intercompany");
    	
    	billToShipToMap = new HashMap<String, Boolean>();
    	billToShipToMap.put("Y", true);
    	billToShipToMap.put("P", true);
    	billToShipToMap.put("y", true);
    	billToShipToMap.put("p", true);
    }
    
	@Override
	public Map<String, Object> process(Map<String, Object> item) throws Exception {
		try{
			String url = ConfigUtil.getProperty(Constants.SC_ACCOUNT_REST_URL);
			Long parentAccountPartyId = null;		
			String externalSystemIdFieldName = batchJobContext.getCurrentObject().getItemWriter().getExternalSysFieldId();	
			String parentAccountEbsNumber = item.get(Constants.EBS_ACCOUNT_NUMBER)+"-"+Constants.EBS_SITE_NUMBER_PARENT_ACC;
			String ebsOscPartyId = null;
			
			if(item.get(Constants.EBS_PARENT_CUSTOMER_NAME)!=null  && StringUtils.isNotBlank((String)item.get(Constants.EBS_PARENT_CUSTOMER_NAME))){	
				if(item.get(Constants.EBS_SITE_STATUS_FIELD)!=null && ((String)item.get(Constants.EBS_SITE_STATUS_FIELD)).equalsIgnoreCase(Constants.EBS_ACCOUNT_STATUS_INACTIVE)){
					item.put(Constants.SC_EBS_STATUS, Constants.SC_ACCOUNT_STATUS_INACTIVE);
				}
				ebsOscPartyId = (String)item.get(Constants.EBS_SITE_OSC_PARTY_ID);
				item.put(Constants.EBS_SALES_CHANNEL_FIELD,null);
/*				String resp = genericOscRestService.getByExternalSystemId(url,externalSystemIdFieldName,parentAccountEbsNumber);
				if(StringUtils.isNotBlank(resp)){
					JsonElement jelement = new JsonParser().parse(resp);
					JsonObject  jobject = jelement.getAsJsonObject();
					JsonArray jarray = jobject.getAsJsonArray("items");
					if(jarray!=null && jarray.size()>0){
						jobject = jarray.get(0).getAsJsonObject();
						parentAccountPartyId = jobject.get(Constants.SC_ACCOUNT_PARTY_ID).getAsLong();
						item.put(Constants.SC_PARENT_ACCOUNT_PARTY_ID,parentAccountPartyId);
					}else{
						throw new Exception("Error inserting/updating account. Parent Account "+parentAccountEbsNumber+ " does not exist in Sales Cloud.");
					}
				}else{
					throw new Exception("Error inserting/updating account. Parent Account "+parentAccountEbsNumber+ " does not exist in Sales Cloud.");
				} */
				parentAccountPartyId = accountOscWsClient.findAccountOscByExternalId(externalSystemIdFieldName,parentAccountEbsNumber);
				item.put(Constants.SC_PARENT_ACCOUNT_PARTY_ID,parentAccountPartyId);
				if(parentAccountPartyId==null){
					throw new Exception("Error inserting/updating account. Parent Account "+parentAccountEbsNumber+ " does not exist in Sales Cloud.");
				}
			}else{
				if(item.get(Constants.EBS_ACCOUNT_STATUS_FIELD)!=null && ((String)item.get(Constants.EBS_ACCOUNT_STATUS_FIELD)).equalsIgnoreCase(Constants.EBS_ACCOUNT_STATUS_INACTIVE)){
					item.put(Constants.SC_EBS_STATUS, Constants.SC_ACCOUNT_STATUS_INACTIVE);
				}
				//ebsOscPartyId = (String)item.get(Constants.EBS_ACCOUNT_OSC_PARTY_ID);
			}
			Long partyId = null;
			
			if(ebsOscPartyId!=null){
				partyId = Long.valueOf(ebsOscPartyId);
			}
			if(partyId==null){	
/*				String resp = genericOscRestService.getByExternalSystemId(url,externalSystemIdFieldName,String.valueOf(item.get(Constants.EBS_ORACLE_CUSTOMER_NUMBER)));
				
				if(StringUtils.isNotBlank(resp)){
					JsonElement jelement = new JsonParser().parse(resp);
					JsonObject  jobject = jelement.getAsJsonObject();
					JsonArray jarray = jobject.getAsJsonArray("items");
					if(jarray!=null && jarray.size()>0){
						jobject = jarray.get(0).getAsJsonObject();
						partyId = jobject.get(batchJobContext.getCurrentObject().getItemWriter().getObjectIdField()).getAsLong();
					}
				} */
				partyId = accountOscWsClient.findAccountOscByExternalId(externalSystemIdFieldName,String.valueOf(item.get(Constants.EBS_ORACLE_CUSTOMER_NUMBER)));
			}
			if(partyId!=null){			
				item.put(batchJobContext.getCurrentObject().getItemWriter().getObjectIdField(), partyId);
			}
			
			if(item.get(Constants.EBS_SALES_CHANNEL_FIELD)!=null && salesChannelMap.get(item.get(Constants.EBS_SALES_CHANNEL_FIELD))!=null){
				item.put(Constants.EBS_SALES_CHANNEL_FIELD, salesChannelMap.get(item.get(Constants.EBS_SALES_CHANNEL_FIELD)));
			}			
			if(item.get(Constants.EBS_BILL_TO_FIELD)!=null && billToShipToMap.get(item.get(Constants.EBS_BILL_TO_FIELD))!=null){
				item.put(Constants.EBS_BILL_TO_FIELD, billToShipToMap.get(item.get(Constants.EBS_BILL_TO_FIELD)));
			}else{
				item.put(Constants.EBS_BILL_TO_FIELD, false);				
			}
				
			if(item.get(Constants.EBS_SHIP_TO_FIELD)!=null && billToShipToMap.get(item.get(Constants.EBS_SHIP_TO_FIELD))!=null){
				item.put(Constants.EBS_SHIP_TO_FIELD, billToShipToMap.get(item.get(Constants.EBS_SHIP_TO_FIELD)));
			}else{
				item.put(Constants.EBS_SHIP_TO_FIELD, false);				
			}
			
		}catch(Exception e){
			log.error("Error while processing data ",e);
            this.logErrorInDb(item, e);
            return null;			
		}
		return item;
	}	
	
    public void logErrorInDb(Map<String, Object> item, Exception e) {
        try {
            this.jobUtils.logItemErrorStatus(e, item);        }
        catch (Exception e1) {
            log.error("Error while writing error log ", (Throwable)e1);
        }
    }
}
