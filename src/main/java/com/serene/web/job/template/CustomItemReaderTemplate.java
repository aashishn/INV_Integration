/**
 * @author gpatwa
 * This is template for reader custom Item Reader. The implementor should override read method 
 * and return the data in the map format. The sample example can be seen in the integration.jar  
 */
package com.serene.web.job.template;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.serene.job.common.BatchJobContext;

@Service
@Lazy(true)
@Scope("prototype")
public class CustomItemReaderTemplate implements ItemReader<Map<String,Object>>  {

	@Resource
	private BatchJobContext batchJobContext ;  
	
	@Override
	public Map<String,Object> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		// TODO Auto-generated method stub
		return null;
	}
}
