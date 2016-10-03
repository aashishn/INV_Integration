package com.serene.web;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

import com.serene.job.JobBootLoader;

@Configuration
@Import({JobBootLoader.class})
@ComponentScan(basePackages={"com.serene.web"})
@EnableAutoConfiguration
@EnableCaching
@EnableAsync
//@EnableJpaRepositories(basePackages = { "com.serene.web.dao" })
//@ImportResource({"classpath:/META-INF/spring/**/*-context.xml"})
public class WebConfig {

	@Resource
	private Environment environment ;
	
	private Logger log = Logger.getLogger(WebConfig.class.getName());
    public static void main(String[] args) {
        //SpringApplication.run(WebConfig.class, args);
    }
    
	@Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<ConcurrentMapCache> caches = new ArrayList<ConcurrentMapCache>();
        caches.add(new ConcurrentMapCache("wsUtilCache"));
        caches.add(new ConcurrentMapCache("batchCache"));
		cacheManager.setCaches(caches);
        return cacheManager;
    }
	
    @Bean
    public UrlBasedViewResolver viewResolver(){
        log.info("Entering tiles configurer");
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(TilesView.class);
        return viewResolver;
    }
    
    @Bean
    public TilesConfigurer tilesConfigurer(){
    	log.info("Entering tiles configurer");
        String[] defs = {"/WEB-INF/layouts/layouts.xml","/WEB-INF/views/**/views.xml" };
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(defs);
        return tilesConfigurer;
    }
}