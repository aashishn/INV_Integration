package com.serene.web;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@Configuration
//@EnableTransactionManagement(proxyTargetClass=true)
//@EnableJpaRepositories(
//       entityManagerFactoryRef = "entityManagerFactory", 
//        transactionManagerRef = "transactionManager",
//        basePackages = { "com.serene.dao" })
public class RepositoryConfigure {

	@Resource
	private Environment environment ;
	
	@Bean(name="jpaVendorAdapter")
	@Primary
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter adaptor = new HibernateJpaVendorAdapter();
		adaptor.setShowSql(true);
		adaptor.setGenerateDdl(true);
		adaptor.setDatabase(Database.MYSQL);
		return adaptor;
	}
	
	@Bean(name="source_db")
	@Primary
	public DataSource dataSource() {
		return DataSourceBuilder.create()
		.driverClassName(environment.getProperty("spring.datasource.source.driverClassName"))
		.url(environment.getProperty("spring.datasource.source.url"))
		.username(environment.getProperty("spring.datasource.source.username"))
		.password(environment.getProperty("spring.datasource.source.password"))
		.build();
	}
	
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[]{"com.serene.model"});
        em.setPersistenceUnitName("persistenceUnit");
        em.setJpaVendorAdapter(jpaVendorAdapter());
        em.afterPropertiesSet();
        return em;
    }
	
	@Bean(name="transactionManager")
	@Primary
	public PlatformTransactionManager transactionManager() {
      JpaTransactionManager transactionManager = new JpaTransactionManager();
      transactionManager.setDataSource(dataSource());
      transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
      return transactionManager;
	}
}