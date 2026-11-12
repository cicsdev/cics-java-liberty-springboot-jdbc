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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;


/**
 * 
 * Employee REST controller
 * 
 * A REST controller used to direct incoming REST requests to the correct business service.
 *  
 * In a real world application some of these functions would most likely be done by a POST
 * request. For simplicity all requests to this sample application are done with a GET request
 */
@RestController
public class EmployeeRestController
{	
	@Autowired  
	private EmployeeService employeeService;

	
	/**
	 * Root endpoint - returns the static usage information page, with the Spring Boot version filled in
	 *
	 * @return the static index.html page
	 * @throws IOException if the page cannot be read
	 */
	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String Index() throws IOException
	{
		String page = new String(new ClassPathResource("static/index.html").getContentAsByteArray(), StandardCharsets.UTF_8);
		return page.replace("${springBootVersion}", SpringBootVersion.getVersion());
	}

	
	/**
	 *  example url http://<server>:<port>/allEmployees
	 *  
	 * @return a list of employees
	 */
	@GetMapping({"/allEmployees","/allEmployees/"})
	public List<Employee> getAllRows() 
	{
		return employeeService.selectAll();
	}
	
	
	/**
	 * example url http://<server>:<port>/listEmployee/000100
	 * 
	 * @param empno - employee number
	 * @return a list of employee records for the passed parameter number
	 */
	@GetMapping("/listEmployee/{empno}")
	public List<Employee> listEmployee(@PathVariable("empno") String empno)
	{
		return employeeService.selectWhereEmpno(empno);
	}
	
	
	/**
	 *  example url http://<server>:<port>/addEmployee/Tony/Fitzgerald
	 *  
	 * @param firstName - employee first name
	 * @param lastName - employee last name
	 * @return a message indicating success or failure of the add operation
	 */
	@GetMapping("/addEmployee/{firstName}/{lastName}")
	@ResponseBody
	public String addEmp(@PathVariable("firstName") String firstName , @PathVariable("lastName") String lastName)
	{
		String result = employeeService.addEmployee(firstName,lastName);
		return result;
	}
	
	
	/**
	 *  example url http://<server>:<port>/addEmployeeTx/Tony/Fitzgerald
	 *  Add Employee within a Global (XA) transaction
	 *  
	 * @param firstName - employee first name
	 * @param lastName - employee last name
	 * @return a message indicating success or failure of the add operation
	 */
	@GetMapping("/addEmployeeTx/{firstName}/{lastName}")
	@ResponseBody
	@Transactional
	public String addEmpTx(@PathVariable("firstName") String firstName , @PathVariable("lastName") String lastName)
	{
		String result = employeeService.addEmployee(firstName,lastName);
		return result;
	}
	
	
	/**
	 *  example url http://<server>:<port>/deleteEmployee/368620
	 *  
	 * @param empNo - employee number to be deleted
	 * @return a message indicating success or failure of the delete operation
	 */
	@GetMapping("/deleteEmployee/{empNo}")
	@ResponseBody
	public String delEmployee(@PathVariable("empNo") String empNo)
	{
		String result = employeeService.deleteEmployee(empNo);
		return result;
	}
	
	
	/**
	 *  example url http://<server>:<port>/deleteEmployee/368620
	 *  Delete Employee within a Global (XA) transaction
	 *  
	 * @param empNo - employee number to be deleted
	 * @return a message indicating success or failure of the delete operation
	 */
	@GetMapping("/deleteEmployeeTx/{empNo}")
	@ResponseBody
	@Transactional
	public String delEmployeeTx(@PathVariable("empNo") String empNo)
	{
		String result = employeeService.deleteEmployee(empNo);
		return result;
	}
	
	
	/**
	 * example url http://<server>:<port>/updateEmployee/368620/33333
	 * Update the salary of an employee
	 * 
	 * @param empNo - employee number to be updated
	 * @param newSalary - the new salary to be given to the employee
	 * @return a message indicating success or failure of the update operation
	 */
	@GetMapping("/updateEmployee/{empNo}/{newSalary}")
	@ResponseBody
	public String updateEmp(@PathVariable("empNo") String empNo, @PathVariable("newSalary") int newSalary) 
	{
		String result = employeeService.updateEmployee(newSalary, empNo);
		return result;
	}
	
	
	/**
	 * example url http://<server>:<port>/updateEmployeeTx/368620/33333
	 * Update the salary of an employee within a Global (XA) transaction
	 * 
	 * @param empNo - employee number to be updated
	 * @param newSalary - the new salary to be given to the employee
	 * @return a message indicating success or failure of the update operation
	 */
	@GetMapping("/updateEmployeeTx/{empNo}/{newSalary}")
	@ResponseBody
	@Transactional
	public String updateEmpTx(@PathVariable("empNo") String empNo, @PathVariable("newSalary") int newSalary)
	{
		String result = employeeService.updateEmployee(newSalary, empNo);
		return result;
	}
	
}
