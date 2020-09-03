# Spring Boot Java applications for CICS, Part 4: JDBC


## Learning Objectives
Accessing a relational database from your Springboot application is likely to be an essential requirement for your application. This tutorial demonstrates how to create a Java application which accesses a relational database using Spring Boot's approach to JDBC. The application is designed to be deployed into a Liberty server, running in CICS. 

Spring Boot's JDBC support provides database related beans, such as `JdbcTemplate` and `DataSource`. These beans can be auto-wired into an application to facilitate an automatic JDBC connection to your database. Follow the steps in this article to create a Spring Boot web application that reads and updates the Db2 employee table. The application is designed to be built using Gradle or Maven, and deployed in a CICS Liberty JVM server using IBM Db2® for z/OS as the relational database. 

This tutorial will show you how to

1. Create and build a Spring Boot application that uses JDBC
1. Access the data base using Spring's `JdbcTemplate` 
1. Use a `DataSource` bean as an alternative way of locating the dataSource reference.
1. Understand how to make JDBC updates transactional in CICS
1. Test the sample in CICS

The application is a web application where all requests can be made from a browser. The application uses the Spring Boot web interface to process GET REST requests. In a real world implementation of this other types of REST interfaces, such as POST, would be more appropriate. GET requests are used here for simplicity.

The application source and build scripts are available in the [cicsdev/cics-java-liberty-springboot-jdbc](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/) repository.


## Prerequisites

- CICS TS V5.3 or later
- A configured Liberty JVM server in CICS
- Db2 for z/OS (or another relational database)
- Java SE 1.8 on the z/OS system
- Java SE 1.8 on the workstation
- An Eclipse development environment on the workstation
- Either Gradle or Apache Maven on the workstation

## Estimated time

It should take you about 2 hours to complete this tutorial.


## Step 1: Create the Application

You can develop the code by following this tutorial step-by-step, or by downloading the [cics-java-liberty-springboot-jdbc](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc) example in GitHub.

If you are following step-by-step, generate and download a Spring Boot web application using the Spring initializr website tool. For further details on how to do this, and how to deploy bundles to CICS, see this tutorial [Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/technologies/java/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle). We use Eclipse as our preferred IDE.

Once your newly generated project has been imported into your IDE, you should have the `Application.java` and `ServletInitializer.java` classes which provide the basic framework of a Spring Boot web application.  

In the [first part](https://developer.ibm.com/technologies/spring/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle/) of this tutorial series we looked in-depth at how to use Gradle or Maven to build a Spring Boot web application for CICS. Using that knowledge you should now be in a position to enhance the `build.gradle`, or `pom.xml` to include the necessary dependencies to compile against the additinal Spring Boot libraries. In particular we require the libraries that provide the Spring JDBC and Spring Transaction support. If you also need to use the JCICS API within your application to invoke CICS commands, you will need to add further dependencies to your build as outlined in the previous tutorial.

For Gradle, your build file should have the additional following dependencies:

```gradle
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation ("org.springframework:spring-tx")     
 ```

For Maven, you'll need the following additonal dependencies in your `pom.xml`
```xml 
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>
<dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-tx</artifactId>
</dependency>
```


## Step 2. Access the relational data base.
In this section we will describe how to access the data base using Spring's `JdbcTemplate`.

### Add a class to define the data object(s)

This example application makes use of a supplied Db2 table which contains employee data. The supplied table can be found on your Db2 for z/OS system in database DSN8D11A. The DDL for this table can be found in the [Db2 for z/OS Knowledge Center](https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/intro/src/tpc/db2z_sampletablesemployeemain.html)

We need to have a representation of this table in our application so the first item we add is a definition of the employee object. This is provided in our sample [`Employee.java`](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/Employee.java) class. This is a standard Java representation of our employee record which contains definitions for each column in the table, a constructor plus getters and setters for each field.

### Add a REST controller

The REST controller is the code which will process the requests coming in from the browser. It will direct the incoming requests to the appropriate service methods to complete the request. Code for [EmployeeRestController.java](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/EmployeeRestController.java) is provided in the sample. The controller contains endpoints to perform the following functions:

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

### Add service class using Spring's `JdbcTemplate`

The REST controller contains an `@Autowired` annotation for our employee service.

```java
@Autowired  
private EmployeeService employeeService;
```
This enables the controller to call methods which service the incoming requests. This service class makes calls to the database using the `JdbcTemplate` class supplied by Spring. It also calls the dao(data access object) class to construct an object representation of the EMP table row. 

`JdbcTemplate` is the central class in Spring's JDBC core package. It simplifies the use of JDBC and helps to avoid common errors. `JdbcTemplate` executes core JDBC workflow, leaving application code to provide the SQL and extract results. The class also executes SQL queries or updates, initiates iteration over ResultSets, catches JDBC exceptions and translates them to the generic, more informative exception hierarchy defined in the `org.springframework.dao package`. In addition when you don't have a Spring managed transaction then the `JdbcTemplate `will also call the `close()` method on the data source Connection to ensure connections are returned to the pool.

Our example application uses the query and update methods of JdbcTemplate class. In each case JdbcTemplate is passed a piece of SQL as a string and any result sets are processed by JdbcTemplate and returned in the appropriate object. In the case of the queries using the update method the JdbcTemplate.update returns an integer indicating the number of rows which have been affected by the update. 
 
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
Next in the `src/main/resources` folder create an `application.properties` file to contain the following property. The value should match exactly the JNDI name which will be specified in the data source definition which we will add to our Liberty `server.xml` later in this tutorial. This enables the JdbcTemplate to locate the JNDI reference for the required data source.
```
spring.datasource.jndi-name=jdbc/jdbcDataSource
```
> **Note:** If application security is enabled in the target Liberty server, you will also need to enable an authentication method, and authorisation roles. To do this you will need to create a Jave EE `web.xml file`, and place this in the src/main/webapp/WEB-INF/ folder. A sample `web.xml` file that supports basic authentication is provided in the associated Git repository. For further details on enabling security refer to the previous tutorial [Spring Boot Java applications for CICS, Part 2: Security](https://developer.ibm.com/technologies/spring/tutorials/spring-boot-java-applications-for-cics-part-2-security/)

## Step 3. Using a DataSource bean to locate the data source
Instead of using the `spring.datasource.jndi-name` property to name the JNDI reference for the data source, the JNDI name can alternatively be set using a DataSource bean within the application. To do this, the `Application` class would need to provide a method annotated with the `@Bean` annotation that lookups up the data source using the JNDI method `InitialContext.doLookup()` and supplies this as a return value. 

```java
@SpringBootApplication
public class Application {
    private static final String DATA_SOURCE = "jdbc/jdbcDataSource";

    public static void main(String args[]) throws NamingException {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public static DataSource dataSource() {
        try {
            DataSource ds = InitialContext.doLookup(DATA_SOURCE);
            return ds;
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

Then in the `EmployeeService` class the bean must be auto-wired as follows:
```java
@Autowired
private DataSource myDatasource
```
This method removes the ability to easily modify the JNDI reference using the property, so is not used in our supplied example.

> **Note:** If your application needs to access multiple data sources then...... 
A working example for this technique is provided in the CICSDev git repository[cics-java-liberty-springboot-jdbc-multi](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc-multi).

## Step 4. Add transaction support

Transactional support is a key part of using JDBC within CICS. Our sample can be used either within the default transactional scope of a CICS unit-of-work, or within the scope of global JTA transaction by using the REST endpoints prefixed with `Tx` such as `addEmployeeTx/{firstName}/{lastName}`

There are three types of Db2 DataSource definition that can be used in CICS Liberty, all use the same Db2 JDBC driver (JCC). They are as follows:
- The original `cicsts_dataSource` using type 2 connectivity and a CICS DB2CONN resource.
- A Liberty `dataSource` with type 2 connectivity and a CICS DB2CONN resource.
- A Liberty `dataSource` with type 4 connectivity and using remote TCP/IP connection managed by Liberty. 

When using the default transactional scope of the CICS unit-of-work with a T2 JDBC connection you may notice that without setting the [`commitOrRollbackOnCleanup=commit`](https://www.ibm.com/support/knowledgecenter/en/SS7K4U_liberty/com.ibm.websphere.liberty.autogen.zos.doc/ae/rwlp_config_oauthProvider.html#databaseStore/dataSource) property on the Liberty dataSource, that methods that perform a data base updates will rollback (and not update). This is because the JdbcTemplate closes connections after usage. Closing a connection will cause the Liberty connection factory to cleanup outstanding requests if they are not autocommited, and are not in a global transaction. Since the default setting for `commitOrRollbackOnCleanup` is `rollback` and autocommit is not supported for T2 connections then requests will rollback by default. This can also apply to T4 JDBC connections, but since T4 JDBC connections default to using autocommit, this does not apply as each JDBC request will be auto committed after usage.


|data source      |type     |autocommit    |autocommit default  |commitOrRollbackOnCleanup default|
|-----------------|---------|--------------|--------------------|---------------------------------|
|cicsts_dataSource  |T2     |false         |false               |n/a     |
|Liberty datasource |T2     |false         |false               |rollback|
|Liberty dataSource |T4     |true or false |true                |commit  |

To avoid this situation a JTA transactiona scope can be used to control the transactional scope. If a transactional service endpoint such as `/addEmployeeTx` is used then the service method it maps to creates a global transaction using the Spring `@Transactional` annotation as shown. This ensures all the work called from this methods is part of a single global transaction coordinated by Libety. This includes the CICS UOW, and any requests to Liberty managed resources sucha as JDBC type 4 connections. 

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


## Step 5 : Running the sample

To deploy the sample into a CICS Liberty JVM server, you need to first build the application as a WAR. Gradle [build.gradle](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/build.gradle) and Maven [pom.xml](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/pom.xml) files are provided in the sample Git repository to simplify this task. You will then need to deploy the WAR into a CICS Liberty JVM server. There are a couple of ways of doing this, by either:

- Adding an <application> element to the Liberty server.xml that points directly to the WAR
- Adding the WAR to a CICS bundle project, export this to CICS, and install using a CICS BUNDLE resource definition

Further instructions on deploying the sample to CICS and creating the required `dataSource` definitions can be found in the Git repository [README](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/README.md)

To invoke the application you can use the following example URL
   `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/`
  
This will return a response which looks similar to the following which lists the different services available.
  
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


For example to list all employees copy the base URL along with the REST service suffix `allEmployees` into the browser to view all employees
`http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/allEmployees`

## Summary
Using JDBC to access relataional database is made easy in Spring using the JdbcTemplate. After completing this tutorial, you should be able to start to build fully functional Java based business applications in CICS using Spring Boot. Watch out for further [samples](https://github.com/cicsdev?q=springboot&type=&language=java)  on other Spring Boot technology from the CICS Java development team. 


## References
- [Spring JdbcTemplate class](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html)
- [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)
