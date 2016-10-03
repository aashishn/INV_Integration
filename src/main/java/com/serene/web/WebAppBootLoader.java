package com.serene.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.context.ApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class WebAppBootLoader extends  SpringBootServletInitializer  {
	public static ApplicationContext applicationContext ;
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	SpringApplicationBuilder builder = application.sources(WebConfig.class);
    	return builder ;
    }
    
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		servletContext.addFilter("openEntityManagerInViewFilter", OpenEntityManagerInViewFilter.class);
	}
	
}
