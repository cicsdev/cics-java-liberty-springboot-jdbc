/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2020 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */

package com.ibm.cicsdev.springboot.jdbc;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 
 * This class is the entry point of the spring boot application which contains @SpringBootApplication annotation and the main method to run the Spring Boot application.
 * 
 * A single @SpringBootApplication annotation can be used to enable those three features, that is:
 *
 *   @EnableAutoConfiguration: enable Spring Boot’s auto-configuration mechanism
 *   @ComponentScan: scan all the beans and package declarations when the application initializes.
 *   @Configuration: allow to register extra beans in the context or import additional configuration classes
 * 
 */

@SpringBootApplication
public class Application {
	
	// name the dataSource jndi name
	private static final String DATA_SOURCE = "jdbc/jdbcDataSource-bean";


	/**
	 * @param args
	 */
	public static void main(String args[]) {
		SpringApplication.run(Application.class, args);
	}
	
	/**
	 * @return a data Source
	 */
	@Bean
	public DataSource dataSource() {		
		try {
			// Look up the connection factory from Liberty
			DataSource ds = InitialContext.doLookup(DATA_SOURCE);
			return ds;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	} 
}
