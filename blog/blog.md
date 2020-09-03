# Spring Boot Java applications for CICS, Part 4: JDBC


## Learning Objectives
Accessing a relational database from your Spring Boot application is likely to be an essential requirement for many applications. This tutorial demonstrates how to create a Java application which accesses a relational database using Spring Boot's approach to JDBC. The application is designed to be deployed into a Liberty server, running in CICS. 

Spring Boot's JDBC support provides database related beans, such as `JdbcTemplate` and `DataSource`. These beans can be auto-wired into an application to facilitate an automatic JDBC connection to your database. Follow the steps in this article to create a Spring Boot web application that reads and updates the Db2 employee table. The application is designed to be built using Gradle or Maven, and deployed in a CICS Liberty JVM server using IBM Db2® for z/OS as the relational database. 

This tutorial will show you how to

1. Create and build a Spring Boot application that uses JDBC
1. Access the database using Spring's `JdbcTemplate` 
1. Use a `DataSource` bean as an alternative way of locating a dataSource reference.
1. Understand how to make JDBC updates transactional in CICS
1. Test the sample in CICS

The application is a web application and all requests can be made from a browser. The application uses the Spring Boot web interface to process GET REST requests. In a real world implementation, other types of REST interfaces, such as POST, would be more appropriate. GET requests are used here for simplicity.

The application source and build scripts are available in the [cicsdev/cics-java-liberty-springboot-jdbc](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/) repository.


## Prerequisites

- CICS TS V5.3 or later
- A configured Liberty JVM server in CICS
- Db2 for z/OS (or another relational database)
- Java SE 1.8 on the z/OS system
- Java SE 1.8 on the workstation
- An Eclipse development environment on the workstation (optional)
- Either Gradle or Apache Maven on the workstation (optional if using Wrappers)

## Estimated time

It should take you about 2 hours to complete this tutorial.


## Step 1: Create the Application

You can develop the code by following this tutorial step-by-step, or by downloading the [cics-java-liberty-springboot-jdbc](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc) example in GitHub.

If you are following step-by-step, generate and download a Spring Boot web application using the Spring initializr website tool. For further details on how to do this, refer to part 1 of this tutorial series [Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/technologies/java/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle). We use Eclipse as our preferred IDE.

Once your newly generated project has been imported into your IDE, you should have the `Application.java` and `ServletInitializer.java` classes which provide the basic framework of a Spring Boot web application.  

In the first part of this tutorial series we looked in-depth at how to use Gradle or Maven to build a Spring Boot web application for CICS. Using that knowledge you should be in a position to enhance the `build.gradle`, or `pom.xml` to include dependencies on additional libraries. In particular, we require Spring Boot JDBC support and it's implicit support for transactions. If you also need to use the JCICS API within your application, you will need to add further dependencies to your build as outlined in the previous tutorial.

For Gradle, your build file should have the additional dependency:

```gradle
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
 ```

For Maven, you'll need the following additonal dependency in your `pom.xml`
```xml 
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
```


## Step 2. Access the relational database.
In this section we will describe how to access the database using Spring's `JdbcTemplate`.

### Add a class to define the data object(s)

This example application makes use of a supplied Db2 table which contains employee data. The supplied table can be found on your Db2 for z/OS system in database DSN8D11A. The DDL for this table can be found in the [Db2 for z/OS Knowledge Center](https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/intro/src/tpc/db2z_sampletablesemployeemain.html)

We need a representation of this table in our application so the first item we add is a definition of the employee object. This is provided in our sample [`Employee.java`](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/Employee.java) class. It is a standard Java representation of our employee record which contains definitions for each column in the table, a constructor, plus getters and setters for each field.

### Add a REST controller

The REST controller is the code which will process requests received from the browser. It will direct the incoming requests to the appropriate service methods. Code for [EmployeeRestController.java](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/EmployeeRestController.java) is provided in the sample. The controller contains endpoints to perform the following functions:

* Simple end point to display usage information
* Display all Employees in the table
* Display one Employee in the table
* Add a new row (Employee) to the table
* Delete a row (Employee) from the table
* Update a row (Employee) in the table
* Add a new row (Employee) to the table under an XA transaction
* Delete a row (Employee) from the table under an XA transaction
* Update row (Employee) in the table under an XA transaction


The root endpoint providing usage details looks like the following: 

```java  
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
```

### Add a service class to using Spring's `JdbcTemplate`

The REST controller contains an `@Autowired` annotation for our employee service.

```java
@Autowired  
private EmployeeService employeeService;
```
This enables the controller to call methods which service the incoming requests. This service class makes calls to the database using the `JdbcTemplate` class supplied by Spring. It also calls the dao (data access object) class to construct an object representation of the EMP table row. 

`JdbcTemplate` is the central class in Spring's JDBC core package. It simplifies the use of JDBC and helps to avoid common errors. `JdbcTemplate` executes core JDBC workflow, leaving application code to provide the SQL and extract results. The class also executes SQL queries or updates, initiates iteration over ResultSets, catches JDBC exceptions and translates them to the generic, more informative exception hierarchy defined in the `org.springframework.dao package`. In addition when you don't have a Spring managed transaction then the `JdbcTemplate `will also call the `close()` method on the data source Connection to ensure connections are returned to the pool.

Our example application uses the query and update methods of the `JdbcTemplate` class. In each case the `JdbcTemplate` object is passed a piece of SQL as a string and any result sets are processed and returned in the appropriate object. In the case of queries using the update method, `JdbcTemplate.update()` returns an integer indicating the number of rows which have been affected by the update. 
 
The service class for this application `EmployeeService.java` can be viewed [here](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/EmployeeService.java) in the sample application.

The following snippet of code shows the `JdbcTemplate` being used to query all rows in the employee table

```java
    public List<Employee> selectAll() throws NamingException 
    {
        // setup the select SQL
        String sql = "SELECT * FROM emp";

        // run the query
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
```

### Configure application.properties
Next, in the `src/main/resources` folder create an `application.properties` file to contain the property shown below. The value should match exactly the JNDI name specified in the dataSource definition (which we add to our Liberty `server.xml` later in the tutorial). This property is used by the JdbcTemplate to locate the required dataSource definition using JNDI, and to create an implicit DataSource object representing that dataSource.
```
spring.datasource.jndi-name=jdbc/jdbcDataSource
```

## Step 3. Using a DataSource bean to locate a dataSource definition
Instead of using the `spring.datasource.jndi-name` property as a default way to find a JNDI reference for a dataSource, you can provide a custom DataSource bean to specifiy the JNDI name. One way to do this, is for the `Application` class to provide a method, annotated with `@Bean`, that lookups up the dataSource using the standard Java API `InitialContext.doLookup()`. 

```java
@SpringBootApplication
public class Application 
{
    private static final String DATA_SOURCE = "jdbc/jdbcDataSource";

    public static void main(String args[]) throws NamingException 
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public static DataSource dataSource() 
    {
        try 
	{
            DataSource ds = InitialContext.doLookup(DATA_SOURCE);
            return ds;
        } 
	catch (NamingException e) 
	{
            e.printStackTrace();
            return null;
        }
    }
}
```
You could also use Spring's `JndiDataSourceLookup` class, calling `JndiDataSourceLookup.getDataSource(String jndi)` to achieve the same result.

> **Note:** If your application needs to access multiple data sources then the DataSource bean technique is very useful. A working example for this technique is provided in the CICSDev git repository[cics-java-liberty-springboot-jdbc-multi](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc-multi).

## Step 4. Add transaction support

Transactional support is a key part of using JDBC within CICS. Our sample can be used either within the default transactional scope of a CICS unit-of-work, or within the scope of a global transaction. The latter is achieved by suffixing the existing REST endpoints with `Tx`, such as `addEmployeeTx/{firstName}/{lastName}`

There are three types of Db2 dataSource definition that can be used in CICS Liberty, all use the same Db2 JDBC driver (JCC) but have slightly different transactional behaviours. They are as follows:
- The original `cicsts_dataSource` using type 2 connectivity and a CICS DB2CONN resource.
- A Liberty `dataSource` with type 2 connectivity and a CICS DB2CONN resource.
- A Liberty `dataSource` with type 4 connectivity and using a remote TCP/IP connection managed by Liberty. 

When using the default transactional scope of the CICS unit-of-work with a T2 Liberty JDBC connection you may notice that methods in the sample that perform database updates will rollback by default (and therefore also rollback the CICS UOW). This is because the JdbcTemplate **closes** connections after use. Closing a connection will cause the Liberty connection factory to *cleanup* outstanding requests **if** they are not autocommited or not in a global transaction. Since the default Liberty dataSource setting for the `commitOrRollbackOnCleanup`](https://www.ibm.com/support/knowledgecenter/en/SS7K4U_liberty/com.ibm.websphere.liberty.autogen.zos.doc/ae/rwlp_config_dataSource.html) property is `rollback`, and autocommit is not supported for T2 connections then requests to a T2 JDBC connection that use a Liberty dataSource will rollback by default.

However, the same is not true of the cicsts_dataSource. It does not use the Liberty DataSource connection manager, so there is no opportunity for the Liberty cleanup behaviour to take effect. Instead it is the CICS UOW behaviour that is respected, which means an implicit commit at Task end.

By default, commit behaviour is also exhibited by T4 JDBC connections. T4 JDBC connections default to autocommit=true, and each JDBC request will be auto-committed after use. 

The following table summarises the different behaviours for each type of dataSource.


|dataSource         |type     |autocommit    |autocommit default  |Default commit behaviour         |
|-----------------  |---------|--------------|--------------------|---------------------------------|
|cicsts_dataSource  |T2       |false         |false               |commit CICS UOW                  |
|Liberty datasource |T2       |false         |false               |rollback CICS UOW                |
|Liberty dataSource |T4       |true or false |true                |commit database update           |

To avoid differences and provide consistent behaviour, a global transaction can be used to control the transactional scope of all updates. Our sample contains a set of transactional service endpoints, such as `/addEmployeeTx` that map to service methods that create a global transaction using the Spring `@Transactional` annotation, as shown below. This ensures all the work performed within the scope of that method is part of a single global transaction coordinated by Libety. That work includes the CICS UOW, and any resources it controls, such as JDBC type 2 connections - as well as any requests to Liberty managed resources such as JDBC with type 4 connectivity.

```java
    @GetMapping("/addEmployeeTx/{firstName}/{lastName}")
    @ResponseBody
    @Transactional
    public String addEmpTx(@PathVariable String firstName , @PathVariable String lastName) 
    {
        String result = employeeService.addEmployee(firstName,lastName);
        return result;
    }
```

For further details on using Spring Transactions within CICS refer to the previous tutorial [Spring Boot Java applications for CICS, Part 3: Transactions](https://github.com/cicsdev/cics-java-liberty-springboot-transactions/blob/master/blog/Blog.md)

> **Note:** If application security is enabled in the target Liberty server, you will need to enable an authentication method, and authorisation roles. To do this, create a Jave EE `web.xml file`, and place it in the src/main/webapp/WEB-INF/ folder. A sample `web.xml` file that supports basic authentication is provided in the associated Git repository. For further details on enabling security, refer to the previous tutorial [Spring Boot Java applications for CICS, Part 2: Security](https://developer.ibm.com/technologies/spring/tutorials/spring-boot-java-applications-for-cics-part-2-security/)

## Step 5 : Deploying and running the sample

To deploy the sample into a CICS Liberty JVM server, you need to build the application as a WAR. Gradle [build.gradle](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/build.gradle) and Maven [pom.xml](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/pom.xml) files are provided in the sample Git repository to simplify this task. Once built, there are a couple of ways of deploying the application, either:

- Add an <application> element to the Liberty server.xml that points directly to the WAR
- Add the WAR to a CICS bundle project, exporting the project to zFS, and install it using a CICS BUNDLE resource definition

Ensure that you have defined a `dataSource` definition in Liberty server.xml. For details on configuring a dataSource and further information on deploying the sample to CICS, see the [README](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/README.md) in the Git repository.

To invoke the application you can use the following example URL
   `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/`
  
This will return a response which looks similar to the following and lists the different services available.
  
---
``` 
Spring Boot JDBC Employee REST sample. Date/Time: 2020-08-27:16-26-33.000197
Usage:
/allEmployees - return a list of employees using a classic SELECT statement
/listEmployee/{empno} - a list of employee records for the employee number provided

--- Update operations ---
/addEmployee/{firstName}/{lastName} - add an employee
/deleteEmployee/{empNo} - delete an employee
/updateEmployee/{empNo}/{newSalary} - update employee salary

--- Update operations within a Global (XA) Transaction ---
/addEmployeeTx/{firstName}/{lastName} - add an employee using an XA transaction
/deleteEmployeeTx/{empNo} - delete an employee using an XA transaction
/updateEmployeeTx/{empNo}/{newSalary} - update employee salary using an XA transaction
```
---



## Summary
Using JDBC to access relational databases is made easy in Spring using the JdbcTemplate. After completing this tutorial, you should be able to start to build fully functional Java based business applications in CICS using Spring Boot. Watch out for further [samples](https://github.com/cicsdev?q=springboot&type=&language=java)  on other Spring Boot technology from the CICS Java development team. 


## References
- [Spring JdbcTemplate class](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html)
- [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)
