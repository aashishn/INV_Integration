/**
 * @author gpatwa
 * This is template for writing custom Item Reader. The implementor should override write method 
 * The sample example can be seen in the integration.jar.  
 */
package com.serene.web.job.template;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.serene.job.common.BatchJobContext;

@Service
@Lazy(false)
public class CustomItemWriterTemplate  implements ItemWriter<Map<String,Object>> {

	@Resource
	private BatchJobContext batchJobContext ;
	
	private static Logger log = LoggerFactory.getLogger(CustomItemWriterTemplate.class); 
	
	@SuppressWarnings("unchecked")
	@Override
	public void write(List<? extends Map<String, Object>> items) throws Exception {
	}
}