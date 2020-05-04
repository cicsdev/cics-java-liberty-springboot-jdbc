package com.ibm.cicsdev.springboot.jdbc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

	//	private DataSource dataSource;	

	@Autowired
	private JdbcTemplate jdbcTemplate;	

	@Autowired
	private DataSource datasource2;

	public List<Employee> selectAll() throws NamingException {
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

	public List<Employee> selectAllUsingBeanDataSource() throws NamingException {
		/*
		 * Select all rows from the emp table
		 * 
		 * Identical to preceding selectAll() method except that the 
		 *   datasource information comes injected Bean datasource2
		 *   this will override the setting in the application.properties file
		 */

		//set the data source from the injected bean
		jdbcTemplate = new JdbcTemplate(datasource2);	    

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

	public List<Employee> selectWhereEmpno(String empNo) {
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


	public String addEmployee(String fName, String lName) {
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
		LocalDateTime now = LocalDateTime.now();  
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
		} else
		{
			return "employee insert failed try again";
		}
	}


	public String deleteEmployee(String empNo)
	/*
	 *  Delete an employee based on the empNo passed in
	 *  
	 *    datasource information comes from the application.properties file in the resources directory
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
		} else
		{
			return "employee delete failed try again";
		}
	}


	public String updateEmployee(int newSalary, String empNo) {
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
		} else
		{
			return "employee update failed try again";
		}

	}
}
