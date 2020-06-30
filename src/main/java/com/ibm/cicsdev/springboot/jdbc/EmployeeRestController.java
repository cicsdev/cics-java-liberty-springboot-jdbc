package com.ibm.cicsdev.springboot.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author tony
 *
 */
@RestController
public class EmployeeRestController {
	/*    
	 *  REST controller used to direct incoming requests to the correct business service.
	 *  
	 *  In a real world application some of these functions would most likely be done by a POST
	 *    request. For simplicity all requests to this sample application are done with a GET request
	 *    
	 */

	@Autowired  
	private EmployeeService employeeService;

	/**
	 * Simple endpoint - returns date and time - simple test of the application
	 * 
	 * @return  a Hello message 
	 */
	@GetMapping("/")
	@ResponseBody
	public String Index()
	{    
		Date myDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss.SSSSSS");
		String myDateString = sdf.format(myDate);
		return "Hello from employee service controller. Date/Time: " + myDateString;
	}

	/**
	 *  example url http://<server>:<port>/allRows
	 *  
	 * @return a list of employees
	 * @throws NamingException
	 */
	@GetMapping({"/allRows","/allRows/"})
	public List<Employee> getAllRows() throws NamingException {
		return employeeService.selectAll();
	}
	
	/**
	 *  example url http://<server>:<port>/allRows2
	 *  
	 * @return a list of employees
	 * @throws NamingException
	 */
	@GetMapping({"/allRows2","/allRows2/"})
	public List<Employee> getAllRows2() throws NamingException {
		return employeeService.selectAllUsingBeanDataSource();
	}
	/**
	 * example url http://<server>:<port>/oneEmployee/000100
	 * 
	 * @param empno - employee number
	 * @return a list of employee records for the passed parameter number
	 */
	@GetMapping("/oneEmployee/{empno}")
	public List<Employee> oneEmployee(@PathVariable String empno) {
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
	public String addEmp(@PathVariable String firstName , @PathVariable String lastName) {
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
	public String delEmployee(@PathVariable String empNo) {
		String result = employeeService.deleteEmployee(empNo);
		return result;
	}
	
	/**
	 * example url http://<server>:<port>/updateEmployee/368620/33333
	 * 
	 * @param empNo - employee number to be updated
	 * @param newSalary - the new salary to be given to the employee
	 * @return a message indicating success or failure of the update operation
	 */
	@GetMapping("/updateEmployee/{empNo}/{newSalary}")
	@ResponseBody
	public String updateEmp(@PathVariable String empNo, @PathVariable int newSalary) {
		String result = employeeService.updateEmployee(newSalary, empNo);
		return result;
	}

	
}
