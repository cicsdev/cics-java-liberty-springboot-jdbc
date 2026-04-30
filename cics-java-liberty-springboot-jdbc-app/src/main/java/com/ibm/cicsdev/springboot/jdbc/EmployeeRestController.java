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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


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
	 * Root endpoint - returns date/time + usage information
	 * 
	 * @return the Usage information 
	 */
	@GetMapping("/")
	@ResponseBody
	public String Index()
	{    
		Date myDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss.SSSSSS");
		String myDateString = sdf.format(myDate);
		
		return "<h1>Spring Boot JDBC Employee REST sample. Date/Time: " + myDateString + "</h1>"
		+ "<h3>Usage:</h3>"
		+ "<b>/allEmployees</b> - return a list of employees using a classic SELECT statement <br>"
		+ "<b>/listEmployee/{empno}</b> - a list of employee records for the employee number provided <br>"
		+ "<br> --- Update operations --- <br>"
		+ "<b>/addEmployee/{firstName}/{lastName}</b> - add an employee <br>"				
		+ "<b>/deleteEmployee/{empNo}</b> - delete an employee <br>"
		+ "<b>/updateEmployee/{empNo}/{newSalary}</b> - update employee salary <br>"
		+ "<br> --- Update operations within a Global (XA) Transaction --- <br>"
		+ "<b>/addEmployeeTx/{firstName}/{lastName}</b> - add an employee using an XA transaction <br>"				
		+ "<b>/deleteEmployeeTx/{empNo}</b> - delete an employee using an XA transaction <br>"
		+ "<b>/updateEmployeeTx/{empNo}/{newSalary}</b> - update employee salary using an XA transaction";
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
	public List<Employee> listEmployee(@PathVariable String empno) 
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
	public String addEmp(@PathVariable String firstName , @PathVariable String lastName) 
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
	public String addEmpTx(@PathVariable String firstName , @PathVariable String lastName) 
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
	public String delEmployee(@PathVariable String empNo) 
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
	public String delEmployeeTx(@PathVariable String empNo) 
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
	public String updateEmp(@PathVariable String empNo, @PathVariable int newSalary) 
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
	public String updateEmpTx(@PathVariable String empNo, @PathVariable int newSalary) 
	{
		String result = employeeService.updateEmployee(newSalary, empNo);
		return result;
	}
	
}
