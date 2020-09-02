# Spring Boot Java applications for CICS, Part 3: JDBC


## Learning Objectives
Accessing a relational database from your Spring Boot application is likely to be an essential requirement for your application. This tutorial demonstrates how to create a Java application which accesses a relational database using Spring Boot's approach to JDBC. The application is destined to be deployed into a Liberty server, running in CICS. 

JDBC is a Java API which allows a Java applications to access data stored in a relational database. In this tutorial we will be using IBM Db2® for z/OS as our relational database. The application will use the supplied EMP table which is supplied with DB2. 

Spring Boot's JDBC support provides database related beans, such as `DataSource` and `JdbcTemplate`. These beans can be Autowired into an application to facilitate an automatic JDBC connection to your Database. Follow the steps in this article to generate a Spring Boot web application used to update the Db2 employee table. The application is built using Gradle or Maven, and deployed in a CICS Liberty JVM server.

This tutorial will show you how to

1. Create and build Spring Boot application that uses JDBC
1. ...
1. Use Spring's JDBC Template to execute an SQL statement
1. Use a DataSource bean
1. Understand how to make JDBC updates transactional in CICS
1. ...

The application is a web application where all requests can be made from a browser. The application uses the Spring Boot web interface to process GET REST requests only. In a real world implementation of this other types of REST interfaces, such as POST, would be more appropriate. GET requests are used here for simplicity.

The application source and build scripts are available in the (cicsdev/cics-java-liberty-springboot-jdbc)[https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/] repository.


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

You can develop the code by following this tutorial step-by-step, or by downloading or cloning the [cics-java-liberty-springboot-jdbc](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc) example in GitHub.

If you are following step-by-step, generate and download a Spring Boot web application using the Spring initializr website tool. For further details on how to do this, and how to deploy bundles to CICS, see this tutorial - [spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle](https://developer.ibm.com/technologies/java/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle). We use Eclipse as our preferred IDE.

Once your newly generated project has been imported into your IDE, you should have the `Application.java` and `ServletInitializer.java` classes which provide the basic framework of a Spring Boot web application.  

In the [first part](https://developer.ibm.com/technologies/spring/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle/) of this tutorial series we looked in-depth at how to use Gradle or Maven to build a Spring Boot web application. Using that knowledge you should now be in a position to enhance the `build.gradle`, or `pom.xml` to include the necessary dependencies to compile against the additinal Spring Boot librariesy. In particular we require the libraries that provide the Spring JDBC and Spring Transaction support. 

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
In this section blah blah...

### Add a class to define the data object(s)

This example application will make use of a supplied Db2 table which contains employee data. The supplied table can be found on your Db2 for z/OS system in database DSN8D11A. The DDL for this table can be found in the (Db2 for z/OS Knowledge Center)[https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/intro/src/tpc/db2z_sampletablesemployeemain.html]

We need to have a representation of this table in our application so the first item we add is a definition of an employee object. This is provided in the [`Employee.java`](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/Employee.java) class. This is a standard Java representation of our employee record which contains definitions for each column in the table, a constructor plus getters and setters for each field.

### Add a REST controller

The REST controller is the code which will process the requests coming in from the browser. It will direct the incoming requests to the appropriate service method to complete the request. Code for `EmployeeRestController.java` is provided in the [sample](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/EmployeeRestController.java). The controller contains endpoints to perform the following functions:

* simple end point to display usage information
* display all rows int the table
* display one row in the table
* add a new row (Employee) to the table
* delete a row (Employee) from the table
* update row (Employee) in the table
* add a new row (Employee) to the table under an XA transaction
* delete a row (Employee) from the table under an XA transaction
* update row (Employee) in the table under an XA transaction

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

### Add service class which uses Spring's `JdbcTemplate`

The REST controller contains an `@Autowired` annotation for our employee service.

```java
@Autowired  
private EmployeeService employeeService;
```
This enables the controller to call methods which service the incoming requests. This service class makes calls to the database using the `JdbcTemplate` class supplied by Spring. It also calls the dao(data access object) class to construct an object representation of the EMP table row. 

`JdbcTemplate` is the central class in Spring's JDBC core package. It simplifies the use of JDBC and helps to avoid common errors. `JdbcTemplate` executes core JDBC workflow, leaving application code to provide the SQL and extract results. The class also executes SQL queries or updates, initiates iteration over ResultSets, catches JDBC exceptions and translates them to the generic, more informative exception hierarchy defined in the `org.springframework.dao package`.

JdbcTemplate in this example application uses the query and update methods of that class. The jdbcTemplate in each case is passed a piece of SQL as a string and any result sets are processed by jdbcTemplate and returned in the appropriate object. In the case of the queries using the update method the jdbcTemplate.update returns an integer indicating the number of rows which have been affected by the update. 
 
The service class for this application EmployeeService.java can be viewed [here](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/blob/master/src/main/java/com/ibm/cicsdev/springboot/jdbc/EmployeeService.java) in the sample application.

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
Next in the `src/main/resources` directory edit the `application.properties` file to contain the following property. The value should match exactly the JNDI name which will be specified in the dataSource definition which we will add to our Liberty `server.xml` later in this tutorial.
```
spring.datasource.jndi-name=jdbc/jdbcDataSource
```


## Usiong a datasource bean
Instead of using the `spring.datasource.jndi-name` property to name the JNDI reference for the data source, the JNDI name can alternatively be set using a datasource bean in the application code. In order to do this you would define the bean in the application. To do this, the `Application` class would need to create the DataSource object using `InitialContext.doLookup();` as shown.

```
@SpringBootApplication
public class Application 
{
	// name the dataSource jndi name
	private static final String DATA_SOURCE = "jdbc/jdbcDataSource";

	/**
	 * @param args
	 * @throws NamingException 
	 */
	public static void main(String args[]) throws NamingException 
    {
		SpringApplication.run(Application.class, args);		
	}
	
    
	/**
	 * @return a data Source
	 */
	@Bean
	public static DataSource dataSource() 
    {		
		try 
        {
			// Look up the connection factory from Liberty
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

Then in the `EmployeeService` class the bean must be Autowired as follows:
```java
@Autowired
private DataSource myDatasource
```


## Step 3. Add transaction support

There are three types of Db2 DataSource definition that can be used in CICS Liberty, all use the Db2 JDBC driver (JCC). They are:
- the original `cicsts_dataSource` using type 2 connectivity (DB2CONN) and supporting the DriverManager interface. 
- a Liberty `dataSource` with type 2 connectivity (using CICS DB2CONN for connection management)
- a Liberty `dataSource` with type 4 connectivity (using TCP/IP and Liberty for connection management)

Data sources are defined in server.xml, and JNDI is used by this application to autowire to the specified data source given by the URL in `application.properties`.   
It is important to note that when the Db2 JDBC driver is operating in a CICS environment with type 2 connectivity, the autocommit property is <i>forced</i> to 'false' and by default the `commitOrRollbackOnCleanup` property is set to 'rollback'. Traditionally this has been because the driver defers to CICS UOW processing to demark transactions in a CICS application. Conversely, JDBC type 4 connectivity defaults to 'autocommit=true' as this is more standard in a distributed environment. Additionally the `commitOrRollbackOnCleanup` property does <b>not</b> apply if autocommit is on, AND autocommit does not apply if using a global txn.

The differing values of these properties for different DataSource types, give rise to different transactional behaviour when used in CICS Liberty. For example, calling the `/addEmployee` endpoint in this sample with a Liberty type 4 DataSource will result in an automatic commit, the same call using a Liberty type 2 DataSource will result in rollback, because autocommit=false (forced by JCC driver) and the clean-up behaviour (if there is no explicit transaction) is to rollback.

For the `cicsts_dataSource` which uses type 2 connectivity, the behaviour is similar to Liberty type 4 but this DataSource implementation does not involve the Liberty transaction manager by default and so the clean-up behaviour does not apply. Thus when the transaction finishes, CICS will implicitly commit the UOW, and the database updates are committed. 

You can emulate the autocommit behaviour for a Liberty DataSource with type 2 connectivity by setting the `commitOrRollbackOnCleanUp` property to 'commit'. However, should the application then cause an exception or abend, the CICS UOW containing the Db2 update, has already been committed and only a second new (empty) UOW is rolled back.

Thus, for each update operation in this sample we provide a second end-point version (post-fix 'Tx') which wraps the call in an XA (global) transaction and in all environments the behaviour remains fully transactional and consistent. You can observe the differences in behaviour by defining different DataSource types in your server.xml and driving the different local vs global transaction endpoints.

For more details about using the @Transactional annotation and XA transactions see [Spring Boot Java applications for CICS - Part 3 - Transactions](https://github.com/cicsdev/cics-java-liberty-springboot-transactions/blob/master/blog/Blog.md)


## Step 3 : Configure Liberty

> **Note:** If application security is enabled in the target Liberty server, you will also need to enable an authentication method, and authorisation roles. To do this you will need to create a Jave EE `web.xml file`, and place this in the src/main/webapp/WEB-INF/ folder. A sample `web.xml` file that supports basic authentication is provided in the associated Git repository. For further details on enabling security refer to the previous tutorial [Spring Boot Java applications for CICS, Part 2: Security](https://developer.ibm.com/technologies/spring/tutorials/spring-boot-java-applications-for-cics-part-2-security/)

To deploy the sample into a CICS Liberty JVM server you will need to build the application as a WAR. Gradle [build.gradle]() and Maven [pom.xml]() files are provided in the sample Git repository to simplify this task. You will need to:

* Configure your CICS Liberty JVM server 
* Define the application to the Liberty server either in CICS bundle, or using an <application> element in the Liberty server.xml
* Start the Liberty server 
* Invoke the application using the following example URL
   `http://hostname:port/cics-java-liberty-springboot-jdbc-0.1.0/`
  
This will return a response which looks similar to the following:
  
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


### Deploy the WAR into a CICS Liberty JVM server
There are two ways to deploy the WAR. 
1. You can add an <application> element to your server.xml which points to your uploaded WAR file location.
2. You can deploy the Spring Boot WAR as a WAR bundlepart within a CICS bundle.
  
Both methods are described in [Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle/


### Add Datasource defintion to server.xml

The server.xml requires a datasource defintion to enable the connection to DB2. Either JDBC type 2 connectivity, or JDBC type 4 connectivity can be used:

### Type 4
An example of a DataSource using type 4 connectivity follows:

```xml
  <dataSource id="t4"  jndiName="jdbc/jdbcDataSource" type="javax.sql.DataSource">
         <jdbcDriver libraryRef="db2Lib"/>
        <properties.db2.jcc currentSchema="DSN81110" databaseName="DSNV11P2" driverType="4" password="+++++++++++++"
                      portNumber="<port num>" serverName="<your server name>" user="<user id"/>
  </dataSource>
 
  <library id="db2Lib">
        <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
        <fileset dir="/usr/lpp/db2v11/jdbc/lib"/>
   </library>
```

A DataSource using JDBC type 4 connectivity does not require CICS to have an installed DB2CONN definition.

### Type 2
An example of a DataSource using JDBC type 2 connectivity follows:

```xml
   <dataSource id="t2" jndiName="jdbc/jdbcDataSource" transactional="false">
        <jdbcDriver libraryRef="db2Lib"/>
        <properties.db2.jcc currentSchema="DSN81110" driverType="2"/>
        <connectionManager agedTimeout="0"/>
   </dataSource>
   
   <library id="db2Lib">
        <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
        <fileset dir="/usr/lpp/db2v11/jdbc/lib"/>
   </library>
```

For JDBC type 2 connectivity to Db2, an active CICS DB2CONN resource must be installed and available in the CICS region. 



## Step 4: Trying out the sample


#Trying out the sample

1. Find the base URL for the application in the Liberty messages.log to view the Usage instructions:
    e.g. `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/`


2. Paste the base URL along with the REST service suffix 'allEmployees' into the browser to view all Employees
    e.g. `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/allEmployees`


##References
https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html
