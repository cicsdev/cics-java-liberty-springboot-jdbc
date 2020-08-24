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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class which retrieves the data requested by the REST controller
 *    makes use of jdbcTemplate to retrieve the data from table EMP
 * 
 * @Autowired Marks a constructor, field, setter method, or config method as to be autowired by Spring's dependency injection facilities
 * @Service Marks a class as providing business logic
 */

@Service
public class EmployeeService 
{
	// The autowired JbdcTemplate gets its data-source definition from application.properties by default	
	@Autowired
	private JdbcTemplate jdbcTemplate;	

	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 
	private LocalDateTime now = LocalDateTime.now();
	
	
	/**
	 * @return a list of employees
	 * @throws NamingException
	 */
	public List<Employee> selectAll() 
	{
		/*
		 * Select all rows from the emp table
		 * 
		 *   datasource information comes from the application.properties file in the resources directory
		 *   
		 */

		//setup the select SQL
		String sql = "SELECT * FROM emp";

		//run the query
		return jdbcTemplate.query(
				sql,
				(rs, rowNum) ->
				new Employee(
						rs.getString("EMPNO"),
						rs.getString("FIRSTNME"),
						rs.getString("MIDINIT"),
						rs.getString("LASTNAME"),
						rs.getString("WORKDEPT"),
						rs.getString("PHONENO"),
						rs.getDate("HIREDATE"),
						rs.getString("JOB"),
						rs.getInt("EDLEVEL"),
						rs.getString("SEX"),
						rs.getString("BIRTHDATE"),
						rs.getLong("SALARY"),
						rs.getLong("BONUS"),
						rs.getLong("COMM")));
	}

	
	/**
	 * @return a list of employees
	 * @throws NamingException
	 */
	public List<Employee> selectAllUsingBeanDataSource()
	{
		/*
		 * Select all rows from the emp table
		 * 
		 * Identical to preceding selectAll() method except that the 
		 *   dataSource information comes injected Bean datasource2
		 *   this will override the setting in the application.properties file
		 */

		//set up the select SQL
		String sql = "SELECT * FROM emp";

		//run the query
		return jdbcTemplate.query(
				sql,
				(rs, rowNum) ->
				new Employee(
						rs.getString("EMPNO"),
						rs.getString("FIRSTNME"),
						rs.getString("MIDINIT"),
						rs.getString("LASTNAME"),
						rs.getString("WORKDEPT"),
						rs.getString("PHONENO"),
						rs.getDate("HIREDATE"),
						rs.getString("JOB"),
						rs.getInt("EDLEVEL"),
						rs.getString("SEX"),
						rs.getString("BIRTHDATE"),
						rs.getLong("SALARY"),
						rs.getLong("BONUS"),
						rs.getLong("COMM")));
	}

	
	/**
	 * @param empNo
	 * @return a list of employee records for a specific employee number
	 */
	public List<Employee> selectWhereEmpno(String empNo) 
	{
		/*
		 * Return all rows for a specific employee number
		 * 
		 *   datasource information comes from the application.properties file in the resources directory
		 *   
		 */

		String sql = "SELECT * FROM emp where empno = ?";

		return jdbcTemplate.query(
				sql,
				new Object [] {empNo},
				(rs, rowNum) ->
				new Employee(
						rs.getString("EMPNO"),
						rs.getString("FIRSTNME"),
						rs.getString("MIDINIT"),
						rs.getString("LASTNAME"),
						rs.getString("WORKDEPT"),
						rs.getString("PHONENO"),
						rs.getDate("HIREDATE"),
						rs.getString("JOB"),
						rs.getInt("EDLEVEL"),
						rs.getString("SEX"),
						rs.getString("BIRTHDATE"),
						rs.getLong("SALARY"),
						rs.getLong("BONUS"),
						rs.getLong("COMM")));
	}


	/**
	 * @param fName - first name 
	 * @param lName - last name
	 * @return a string indicating the result of the add operation
	 */
	public String addEmployee(String fName, String lName) 
	{
		/*
		 *  Add a new employee.
		 *      Firstname and lastname are passed in 
		 *      
		 *      for demo purposes all the other fields are set by this method
		 *      
		 *  datasource information comes from the application.properties file in the resources directory
		 *  
		 */

		//generate an empNo between 300000 and 999999
		int max = 999999;
		int min = 300000;
		String empno = String.valueOf((int) Math.round((Math.random()*((max-min)+1))+min));

		//for demo purposes hard code all the remaining fields (except first name and last name) 
		String midInit = "A";
		String workdept = "E21";
		String phoneNo = "1234";

		//get today's date and set as hiredate
		String hireDate= dtf.format(now);  

		String job = "Engineer";
		int edLevel =3 ;
		String sex ="M";
		String birthDate = "1999-01-01" ;
		long salary = 20000;
		long bonus= 1000;
		long comm = 1000;

		//setup the SQL
		String sql = "insert into emp (EMPNO, FIRSTNME, MIDINIT,LASTNAME,WORKDEPT,PHONENO,HIREDATE,JOB,EDLEVEL,SEX,BIRTHDATE,SALARY,BONUS,COMM) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		//do the insert
		int numRows =  jdbcTemplate.update (sql,
				empno,
				fName,
				midInit,
				lName,
				workdept,
				phoneNo,
				hireDate,
				job,
				edLevel,
				sex,
				birthDate,
				salary,
				bonus,
				comm);

		//numRows is the number of rows inserted - will be zero if the insert fails
		if (numRows > 0) 
		{
			return "employee " + empno + " added";
		} 
		else
		{
			return "employee insert failed try again";
		}
	}


	/**
	 * @param empNo - employee number to be deleted
	 * @return - a message to indicate success or failure of the delete operation
	 */
	public String deleteEmployee(String empNo)
	/*
	 *  Delete an employee based on the empNo passed in
	 *  
	 *    dataSource information comes from the application.properties file in the resources directory
	 *    
	 */
	{
		//set up the delete SQL
		String sql = "DELETE FROM emp WHERE empno =?";

		//do the delete
		int numRows = jdbcTemplate.update(sql, empNo);

		//numRows is the number of rows deleted - will be zero if the delete fails
		if (numRows > 0) 
		{
			return "employee " + empNo + " deleted";
		} 
		else
		{
			return "employee delete failed try again";
		}
	}


	/**
	 * @param newSalary - update the employee record with this salary amount
	 * @param empNo - the employee number which is to be uodated
	 * @return a message to indicate success or failure of the update operation
	 */
	public String updateEmployee(int newSalary, String empNo) 
	{
		/*
		 * Update a specified employee's salary based on the empNo passed to the salary passed in.
		 * 
		 *   datasource information comes from the application.properties file in the resources directory
		 *   
		 */

		//set up the update SQL
		String sql = "update emp set salary =? where empNo = ?";

		//do the update
		int numRows = jdbcTemplate.update(sql, newSalary, empNo);

		//numRows is the number of rows updated - will be zero if the update fails   
		if (numRows > 0) 
		{
			return "employee " + empNo + " salary changed to " + newSalary;
		}
		else
		{
			return "employee update failed try again";
		}
	}
	
}
