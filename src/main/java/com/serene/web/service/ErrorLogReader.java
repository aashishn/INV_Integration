package com.serene.web.service;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serene.job.common.BatchJobContext;
import com.serene.job.common.ResultsetToHashMap;
import com.serene.job.repo.dao.JobSchedulerMetadataDao;
import com.serene.job.repo.model.JobDetail;
import com.serene.job.util.CacheUtil;
import com.serene.web.util.Constants;


@Service("ERROR_LOG_READER")
@Lazy(true)
@Scope("prototype")
public class ErrorLogReader<T> extends JdbcCursorItemReader<T>
implements StepExecutionListener {
    @Resource
    private BatchJobContext batchJobContext;
    @Resource
    private JobSchedulerMetadataDao jobSchedulerMetadataDao;
    private String jobName;
    
    @Resource
    private CacheUtil cacheUtil;

    @Resource
    protected ResultsetToHashMap resultsetToHashMap;

    public String getJobName() {
        return this.jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Transactional(value="jobTransactionManager")
    public void beforeStep(StepExecution stepExecution) {
    	JobDetail jobSchedulerMetadata = batchJobContext.getCurrentObject();
        String sql = jobSchedulerMetadata.getItemReader().getQuery();
        if (StringUtils.contains((CharSequence)sql, (CharSequence)Constants.LAST_UPDATED_DATE)) {
            String startTime = jobSchedulerMetadata.getInterfaceLastPoolTime();
            sql = StringUtils.isNoneBlank((CharSequence[])new CharSequence[]{startTime}) ? sql.replaceAll(Constants.LAST_UPDATED_DATE, startTime) : sql.replaceAll(Constants.LAST_UPDATED_DATE, Constants.MIN_QUERY_DATE);
        }
        DataSource dataSource = this.cacheUtil.getJdbcDataSource(jobSchedulerMetadata.getItemReader().getDataSource().getDataSourceName());
        this.setDataSource(dataSource);
        this.setRowMapper((RowMapper)this.resultsetToHashMap);
        this.setSql(sql);
        this.setFetchSize(this.batchJobContext.getCurrentObject().getItemReader().getFetchSize().intValue());
    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    public void afterPropertiesSet() throws Exception {
    }
}
