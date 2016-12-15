package com.serene.web.service;

import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.hibernate.internal.jaxb.mapping.hbm.JaxbElementElement;
import org.hibernate.internal.jaxb.mapping.orm.JaxbElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.serene.web.stubs.account.Account;
import com.serene.web.stubs.account.AccountService;
import com.serene.web.stubs.account.AccountService_Service;
import com.serene.web.stubs.account.ChildFindCriteria;
import com.serene.web.stubs.account.Conjunction;
import com.serene.web.stubs.account.DataObjectResult;
import com.serene.web.stubs.account.DeleteAccount;
import com.serene.web.stubs.account.FindAccount;
import com.serene.web.stubs.account.FindControl;
import com.serene.web.stubs.account.FindCriteria;
import com.serene.web.stubs.account.MethodResult;
import com.serene.web.stubs.account.ObjectFactory;
import com.serene.web.stubs.account.SortAttribute;
import com.serene.web.stubs.account.SortOrder;
import com.serene.web.stubs.account.ViewCriteria;
import com.serene.web.stubs.account.ViewCriteriaItem;
import com.serene.web.stubs.account.ViewCriteriaRow;
import com.serene.web.util.Constants;
import com.serene.ws.util.ConfigUtil;



@Service
public class AccountOscWsClient {

	Logger log = LoggerFactory.getLogger(AccountOscWsClient.class);
	
    protected static AccountService_Service accountService_Service;
    protected AccountService accountService;
    protected ObjectFactory objectFactory;
	
    public AccountOscWsClient() {
	    init();
    }

    protected void init() {	    
	    String username = ConfigUtil.getProperty(Constants.SC_USERNAME);
	    String password = ConfigUtil.getProperty(Constants.SC_PASSWORD);
	   
	    accountService_Service = new AccountService_Service();
	    accountService = accountService_Service.getAccountServiceSoapHttpPort();
	    
	    Map<String, Object> reqContext =
	    ((BindingProvider)accountService).getRequestContext();
	    reqContext.put(BindingProvider.USERNAME_PROPERTY, username);
	    reqContext.put(BindingProvider.PASSWORD_PROPERTY, password);
			
	    objectFactory = new ObjectFactory();
    }
    
	public List<Object> findAccountInOsc(int fetchStart, int fetchSize, ViewCriteria filter, List<String> findAttributes, 
			boolean excludeAttribute, List<ChildFindCriteria> childFindCriteriaList, SortOrder sortOrder) throws Exception{
		log.debug("findAccountOsc >>");
		
		FindCriteria findCriteria = objectFactory.createFindCriteria();
		
		findCriteria.setFetchStart(fetchStart);
		findCriteria.setFetchSize(fetchSize);
		if(findAttributes!=null && !findAttributes.isEmpty()){
			findCriteria.getFindAttribute().addAll(findAttributes);
		}
		findCriteria.setExcludeAttribute(excludeAttribute);
		
		//Filter
		if(filter!=null){
			findCriteria.setFilter(filter);
		}
		
		//SortOrder
		if(sortOrder !=null){
			findCriteria.setSortOrder(sortOrder);
		}
		
		FindControl findControl = objectFactory.createFindControl();
		findControl.setRetrieveAllTranslations(true);
				
		FindAccount findAccount = objectFactory.createFindAccount();
		findAccount.setFindCriteria(findCriteria);
		findAccount.setFindControl(findControl);
		DataObjectResult findAccountResponse = null;
		try{
			findAccountResponse = accountService.findAccount(findCriteria, findControl);			
		}catch(Exception e){
			throw e;
		}
		List<Object> accounts = null;
	
		if (findAccountResponse.getValue() != null) {
			log.debug("findAccountOsc successful");
			accounts = findAccountResponse.getValue();
		}
		
		
		log.debug("findAccountOsc <<");
		return accounts;		
	}
	
	public Long findAccountOscByExternalId(String fieldName, String fieldValue) throws Exception{
		log.debug("findAccountOscByExternalId >>");
		Long partyId = null;
		String externalIdValue = fieldValue;
		
		ViewCriteria filter = new ViewCriteria();			
		ViewCriteria nested1 = new ViewCriteria();
		
		ViewCriteriaRow group =  new ViewCriteriaRow();	
		group.setConjunction(Conjunction.AND);
		group.getItem().add(getAccountViewCriteriaItem(Conjunction.AND, fieldName, "=", externalIdValue, null));
		filter.setConjunction(Conjunction.AND);
		filter.getGroup().add(group);
		
		List<Object> accounts = findAccountInOsc(0, 1, filter, null, false, null, null);
		if(accounts!=null && !accounts.isEmpty()){
			log.debug("findAccountOscByExternalId successful");
			partyId = Long.valueOf(((Account)accounts.get(0)).getPartyId());
		}
		
		log.debug("findAccountOscByExternalId <<");
		return partyId;		
	}
	
	private ViewCriteriaItem getAccountViewCriteriaItem(Conjunction conjunction,String atrribute, String operator, String value, ViewCriteria nested) {
		log.debug("getAccountViewCriteriaItem >>");
		ViewCriteriaItem item = new ViewCriteriaItem();
		item.setConjunction(conjunction);
		item.setAttribute(atrribute);
		item.setOperator(operator);
		if(!"EXISTS".equals(operator)){
			item.getValue().add(value);
		}
		// create Nested ViewCriteria
		if(nested!=null)
			item.setNested(nested);
		log.debug("getAccountViewCriteriaItem <<");
		return item;
	}
	
	public static void main(String[] args) throws Exception{
		AccountOscWsClient accClient = new AccountOscWsClient();
		ViewCriteria filter = new ViewCriteria();			
		ViewCriteria nested1 = new ViewCriteria();
		
		ViewCriteriaRow group =  new ViewCriteriaRow();	
		group.setConjunction(Conjunction.AND);
		group.getItem().add(accClient.getAccountViewCriteriaItem(Conjunction.AND, "CreatedBy", "=", "IUSER", null));
		filter.setConjunction(Conjunction.AND);
		filter.getGroup().add(group);
		
		SortOrder sortOrder = new SortOrder();
		SortAttribute sortAttribute = new SortAttribute();
		sortAttribute.setName("OrganizationDEO_OracleCustomerNumber_c");
		sortAttribute.setDescending(false);
		sortOrder.getSortAttribute().add(sortAttribute);
		List<Object> accounts = null;
		int i=1;
		do{
			accounts = accClient.findAccountInOsc(0, 100, filter, null, false, null, sortOrder);
			if(accounts!=null && !accounts.isEmpty()){
				for(Object account: accounts){
					Long partyId = Long.valueOf(((Account)account).getPartyId());
					String ebsId = String.valueOf(((Account)account).getOrganizationDEOOracleCustomerNumberC().getValue());
					System.out.println(i++ +" Deleting Account PartyId="+partyId+" OrganizationDEO_OracleCustomerNumber_c="+ebsId);		
					Account accreq =  accClient.objectFactory.createAccount();
					accreq.setPartyId(partyId);
					MethodResult result = accClient.accountService.deleteAccount(accreq);
					System.out.println(result.getMessage());					
				}
			}else{
				System.out.println("No accounts found.");
			}
		}while(accounts!=null && !accounts.isEmpty());
	}
}
