package com.serene.web.job.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.serene.job.repo.model.FieldDetail;
import com.serene.job.service.UpdateResponseService;
import com.serene.job.util.CacheUtil;
import com.serene.job.util.ExpressionEvaluator;
import com.serene.ws.service.FusionWebService;

@Service("qLIItemProcessor")
@Lazy(true)
@Scope("prototype")
public class QLIItemProcessor implements ItemProcessor<Object, Object> {
	private static final Log log = LogFactory.getLog(BacklogItemProcessor.class);
	@Resource
	protected UpdateResponseService updateResponseService;

	@Resource(name = "fusionWebServiceImpl")
	protected FusionWebService fusionWebService;
	@Resource
	protected BatchJobContext batchJobContext;

	@Resource
	private ExpressionEvaluator expressionEvaluator ;

	
	@Resource
	private CacheUtil cacheUtil;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process(Object item) throws Exception {
		Map<String, Object> data = (Map<String, Object>) item;
		List<Map<String, Object>> valuesMapArr = new ArrayList();
		Map<String, Object> dataValues = new HashMap<String, Object>();
		String objectName = this.batchJobContext.getCurrentObject().getItemWriter().getObjectName();
		String objectId = this.batchJobContext.getCurrentObject().getItemWriter().getObjectIdField();
		StringBuilder insert = new StringBuilder(" INSERT INTO ");
		insert.append(objectName).append(" (");
		StringBuilder values = new StringBuilder(" VALUES  (");
		StringBuilder update = new StringBuilder();
		StringBuilder select = new StringBuilder();
		List<FieldDetail> fieldMappings = this.batchJobContext.getCurrentObject().getFieldMapping();
		JXPathContext context = JXPathContext.newContext(data);
		
		
		
		for (FieldDetail m : fieldMappings) {
			Object fv = expressionEvaluator.getValue(data, context, m, this.batchJobContext.getCurrentObject());
			dataValues.put(m.getToField(), fv);
			System.out.println(m.getToField()+"------------->"+fv);
		}
		valuesMapArr.add(dataValues);
		

		for (FieldDetail  f : fieldMappings){
			if (BooleanUtils.isNotTrue(f.getStatus())) {
				continue ;
			}
			if (StringUtils.equalsIgnoreCase("insert", f.getOperationType())) {
				insert.append(f.getToField()).append(",");
				values.append(":").append(f.getToField()).append(",");
			} else	if (StringUtils.equalsIgnoreCase("update", f.getOperationType())) {
					update.append(f.getToField()).append(" = :").append(f.getToField()).append(",");	
			} else {
				insert.append(f.getToField()).append(",");
				values.append(":").append(f.getToField()).append(",");
				update.append(f.getToField()).append(" = :").append(f.getToField()).append(",");
			}
		}
		if (update.length() > 1) {
			insert.replace(insert.length()-1, insert.length(), ")");
			values.replace(values.length()-1, values.length(), ")");
			insert.append(values);
		}
		
		if (update.length() > 1) {
			update.replace(update.length()-1, update.length(), "");
		}
		update.insert(0," UPDATE "+objectName+" SET ") ;
		update.append(" where "+objectId+" = :"+objectId);
		select.append("SELECT count("+objectId+") from "+objectName +" where "+objectId+" = :"+objectId);
		
		System.out.println("insert is +++++++"+insert);
		System.out.println("update is +++++++"+update);
		System.out.println("select is +++++++"+select);
		
		for (Iterator i = dataValues.keySet().iterator(); i.hasNext();) {
			 String key = (String) i.next();
		     Object value = (Object) dataValues.get(key);
		}

		try {
			int doInsert =0;
			 NamedParameterJdbcTemplate jdbcTemplate = cacheUtil.getJdbcTemplate(this.batchJobContext.getCurrentObject().getItemWriter().getDataSource().getDataSourceName());
		     SqlParameterSource namedParameters = new MapSqlParameterSource(objectId, dataValues.get(objectId));
		     doInsert=jdbcTemplate.queryForObject(select.toString(), namedParameters, Integer.class);

			
	         System.out.println("doInsert Count is "+doInsert + " for id   +"+dataValues.get(objectId));
			// If this does not exist in EBS, delete it
			if(doInsert==0)		
			{
				jdbcTemplate.batchUpdate(insert.toString(), valuesMapArr.toArray(new Map[0]));
			}
			else
			{
				jdbcTemplate.batchUpdate(update.toString(), valuesMapArr.toArray(new Map[0]));

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
