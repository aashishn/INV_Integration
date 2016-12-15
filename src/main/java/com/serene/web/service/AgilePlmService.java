package com.serene.web.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IQuery;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.api.ItemConstants;
import com.serene.web.util.Constants;
import com.serene.ws.util.ConfigUtil;


@Service
public class AgilePlmService {

	private static Logger log = LoggerFactory.getLogger(AgilePlmService.class);
	public static IAgileSession       session  = null;
	public static AgileSessionFactory factory;
	public static IQuery              query;
	public static IAttribute[]        attrs;

	public List<Map> getAllProductDelta(String queryCriteria, Object[] params, List<Integer> attributes) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
		List<Map> resultList = new ArrayList<Map>();
		try {
			// Create an IAgileSession instance.
			log.trace("Start Time: "+new Date());
			session = connect(session);
//			query   = (IQuery)session.createObject(IQuery.OBJECT_TYPE,
//					ItemConstants.CLASS_PART);
			
	         IAdmin admin = session.getAdminInstance();
	         IAgileClass cls = admin.getAgileClass("FinishedGood");
	         query = (IQuery)session.createObject(IQuery.OBJECT_TYPE, cls);
	         
			query.setCaseSensitive(true);
			
			if(params!=null && params.length>0){
				query.setCriteria(queryCriteria,params);
			}else{
				query.setCriteria(queryCriteria);
			}
 
			IAttribute[] attributesArray = null;//new IAttribute[16];
			List<IAttribute> attributesList = new LinkedList<IAttribute>();
			int i=0;
			for(Integer attribute:attributes){
				attributesList.add(cls.getAttribute(attribute));
			}
			attributesList.add(cls.getAttribute(2002));
			attributesArray = attributesList!=null?attributesList.toArray(new IAttribute[attributes.size()]):null;
//			attributesArray[i++] = cls.getAttribute(1016);
//			attributesArray[i++] = cls.getAttribute(1001); //Title Block Number
//			attributesArray[i++] = cls.getAttribute(1002); //Title Block Description
//			attributesArray[i++] = cls.getAttribute(1082); //Title Block Business Unit
//			attributesArray[i++] = cls.getAttribute(1004); //Title Block Projects/Family	         
//			attributesArray[i++] = cls.getAttribute(1539); //Page Three List01
//			attributesArray[i++] = cls.getAttribute(1541); //Page Three List03
//			attributesArray[i++] = cls.getAttribute(1542); //Page Three List04
//			attributesArray[i++] = cls.getAttribute(1549); //Page Three List06
//			attributesArray[i++] = cls.getAttribute(1550); //Page Three List12
//			attributesArray[i++] = cls.getAttribute(1084); //Title Lifecycle phase
//			attributesArray[i++] = cls.getAttribute(1081); //Title Block Subclass
//			attributesArray[i++] = cls.getAttribute(1016); //Title Block Date Released
//			attributesArray[i++] = cls.getAttribute(12089); //Title Block Date Effective
//			attributesArray[i++] = cls.getAttribute(1017); //Title Block Date Inc
			//				attr[i++] = cls.getAttribute(2000008520); //Title Block Item Groups
			//				attr[i++] = cls.getAttribute(2000009468); //Title Block Part Subtype
			//				attr[i++] = cls.getAttribute(2000009494); //Title Block Base Model
//			attributesArray[i++] = cls.getAttribute(2002); // Page Two Create Date
//			attr[i++] = cls.getAttribute(1129);
			
//			for(IAttribute iatrr:query.getResultAttributes(true)){
//				System.out.println("APINAME="+iatrr.getFullyQualifiedAPIName()+"\n"+"ID="+iatrr.getId());
//			}
			query.setResultAttributes(attributesArray);



			ITable result = query.execute();
			log.trace("End Time: "+new Date());
			
			log.trace("Size: "+result.size());
			Iterator iterator = result.iterator();
			int count=1;
			while (iterator.hasNext()) {
				IRow   row    = (IRow)iterator.next();
			    Map rowMap = row.getValues();
			    Map resultRowMap = new HashMap<String,Object>();
			    for(Object key:rowMap.keySet()){
			    	if((key.equals(1016)||key.equals(2002)) && rowMap.get(key) instanceof Date){
			    		resultRowMap.put(String.valueOf(key), sdf.format(rowMap.get(key)));
			    	}else{
			    		resultRowMap.put(String.valueOf(key), rowMap.get(key));
			    	}
			    }
				resultList.add(resultRowMap);
				log.trace("Result row "+ count++ +" : "+resultRowMap);
			}
		} catch (APIException e){
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if(session!=null)
				session.close();
		}
		return resultList;
	}

	/**
	 * <p> Execute and print query results </p>
	 *
	 * @throws APIException
	 */
	private static void printQueryResult() throws APIException {
		ITable result = query.execute();

		log.debug("result Size: "+result.size());
		Iterator i = result.iterator();

		while (i.hasNext()) {
			IRow   row    = (IRow)i.next();
			String number = (String)row.getValue(
					ItemConstants.ATT_TITLE_BLOCK_NUMBER);

			log.debug(number);
		}
	}

	/**
	 * <p> Add attribute to the defult result fields </p>
	 *
	 * @param attribute
	 * @throws APIException
	 */
	private static void addResultAttr(Integer attribute) throws APIException {
		IAttribute[] all = query.getResultAttributes(true);
		IAttribute[] selected = query.getResultAttributes(false);
		ArrayList    attrIdList = new ArrayList(Arrays.asList(selected));

		for (int i = 0; i < all.length; ++i) {
			if (all[i].getId().equals(attribute)) {
				attrIdList.add(all[i]);
			}
		}

		query.setResultAttributes(
				(IAttribute[])attrIdList.toArray(new IAttribute[0]));
	}

	/**
	 * <p> Print result attributes
	 *
	 * @return IAttribute[]
	 */
	private static IAttribute[] printResultAttr() throws APIException {
		IAttribute[] resultAttrs = query.getResultAttributes(false);

		for (int i = 0; i < resultAttrs.length; i++) {
			IAttribute attr = resultAttrs[i];

			log.debug("Result Attribute" + i + ", " +
					attr.getFullName());
		}

		return resultAttrs;
	}

	/**
	 * <p> Print searchable attributes </p>
	 *
	 * @return IAttribute[]
	 */
	private static IAttribute[] printSearchableAttr() throws APIException {
		IAttribute[] attrs = query.getSearchableAttributes();

		for (int i = 0; i < attrs.length; i++) {
			IAttribute attr = attrs[i];

			log.debug(
					"Searchable Attribute" + i + ", " + attr.getFullName());
		}

		return attrs;
	}

	/**
	 * <p> Create an IAgileSession instance </p>
	 *
	 * @return IAgileSession
	 * @throws APIException
	 */
	private static IAgileSession connect(IAgileSession session)
			throws APIException {
		System.setProperty("disable.agile.sessionID.generation", "true");
		HashMap params = new HashMap();

		params.put(AgileSessionFactory.USERNAME, ConfigUtil.getProperty(Constants.AGILE_PLM_USERNAME));
		params.put(AgileSessionFactory.PASSWORD, ConfigUtil.getProperty(Constants.AGILE_PLM_PASSWORD));
		factory = AgileSessionFactory.getInstance(ConfigUtil.getProperty(Constants.AGILE_PLM_URL));
		session = factory.createSession(params);
		return session;
	}
	
	public static void main(String[] args) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		new AgilePlmService().getAllProductDelta("[1001] == 'Test demo product demo 2'", null,null);
	}
}
