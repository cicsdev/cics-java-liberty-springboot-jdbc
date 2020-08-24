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
 * Spring boot application entry-point (including main method and @SpringBootApplication annotation).
 * 
 * The @SpringBootApplication annotation is equivalent to:
 *
 *   @EnableAutoConfiguration: enable Spring Boot’s auto-configuration mechanism
 *   @ComponentScan: scan all the beans and package declarations when the application initializes.
 *   @Configuration: allow to register extra beans in the context or import additional configuration classes
 * 
 */
@SpringBootApplication
public class Application 
{
<<<<<<< HEAD
=======
	// The dataSource jndi name
	private static final String DATA_SOURCE = "jdbc/jdbcDataSource-bean";


>>>>>>> refs/remotes/origin/master
	/**
	 * @param args - inputs
	 */
	public static void main(String args[]) 
	{
		SpringApplication.run(Application.class, args);
	}
	
<<<<<<< HEAD
=======
	/**
	 * @return the data Source
	 */
	@Bean
	public DataSource dataSource() 
	{		
		try 
		{
			// Look up the Liberty DataSource using JNDI
			DataSource ds = InitialContext.doLookup(DATA_SOURCE);
			return ds;
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
			return null;
		}
	} 
>>>>>>> refs/remotes/origin/master
}
