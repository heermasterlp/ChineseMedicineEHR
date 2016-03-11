package com.um.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mathworks.toolbox.javabuilder.MWException;

import newpredictum.Predictum;

/**
 * Database conncetions bean 
 *  
 * @author heermaster
 *
 */
@Configuration
public class DaoConfig {
	
	
	@Bean 
	public Predictum predictum() throws MWException {
		return new Predictum();
	}
	
	@Bean
	public DataBaseBean dataBaseBean() {
		return new DataBaseBean();
	}
}
