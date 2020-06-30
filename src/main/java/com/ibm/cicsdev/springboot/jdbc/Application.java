package com.ibm.cicsdev.springboot.jdbc;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

/**
 * @author Tony Fitzgerald
 * 
 * Main application class
 *    also sets up a DataSource bean 
 */
@SpringBootApplication
@ServletComponentScan
public class Application extends SpringBootServletInitializer  {
	
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
