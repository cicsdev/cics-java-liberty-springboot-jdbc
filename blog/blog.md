# Learning Objectives
Accessing a relational database from your Springboot application is likely to be an essential requirement for your application. This tutorial demonstrates how to create a Java application which accesses a relational database using Spring Boot's approach to JDBC. The application is destined to be deployed into a Liberty server, running in CICS. 

1. We will Create a Spring Boot app that uses JDBC and setup a Maven/Gradle build for this
1. Use JDBC Template to access a data source
1. Configure a Datasource to define the connection to the database
1. Deploy and test the app in CICS/Liberty

JDBC is a Java API which allows a Java applications to access data stored in a relational database. In this tutorial we will be using IBM Db2® for z/OS as our relational database. The application will use the supplied EMP table which is supplied with DB2. 

Spring Boot JDBC supplies database related beans such as DataSource and JdbcTemplate,  which can be Autowired into an application to facilitate the usage of JDBC in the application. Follow the steps in this article to generate a Spring Boot web application which can then be built using either Gradle or Maven and deployed in a CICS Liberty JVM server and used to update the Db2 employee table  

The application will allow you to:

1. add an employee to the EMP table
1. list all or a single employee 
1. update an existing employee
1. delete an existing employee.  

## Step 1: Create the Application

You can develop the code by following this tutorial step-by-step, or by downloading or cloning the [cics-java-liberty-springboot-transactions](https://github.com/cicsdev/cics-java-liberty-springboot-transactions) example in GitHub.

If you are following step-by-step, generate and download a Spring Boot web application using the Spring initializr website tool. For further details on how to do this, and how to deploy bundles to CICS, see this tutorial - [spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle](https://developer.ibm.com/technologies/java/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle). We use Eclipse as our preferred IDE.

Once your newly generated project has been imported into your IDE, you should have the `Application.java` and `ServletInitializer.java` classes which provide the basic framework of a Spring Boot web application. 


## The Application
The application is a web application where all requests can be made from a browser. The application uses the Spring Boot web interface to process GET REST requests only. In a real world implementation of this other types of REST interfaces, such as POST, would be more appropriate. GET requests are used here for simplicity.

The application source and build scripts are available from github at <<<insert link>>>. 

## Construct the application

We will now add the various pieces of code to the application which will allow us to access the data in the EMP table in Db2.


### Add a class to define the data object(s)


This example application will make use of a supplied Db2 table which contains employee data. The supplied table should be found on your Db2 for z/OS system in database DSN8D11A. The DDL for this table can be found in the Db2 for z/OS Knowledge Center at the folloiwng location https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/intro/src/tpc/db2z_sampletablesemployeemain.html


We need to have a representation of this table in our application so the first item we need to add is a definition of an employee object. This is done in the Employee.java class. The Employee.class can be found in the git sample [here](https:\\link.here.com)

This is a standard java representation of our employee record which contains definitions for each column in the table, a constructor plus getters and setters for each field.


### Add a REST Controller


The REST controller is the code which will process the requests coming in from the browser. It will direct the incoming requests to the appropriate service method to complete the request.


Code for EmployeeRestController.java can be seen in the git sample [here](https:\\link.here.com). The controller contains endpoints to perform the following functions
* simple end point to display an informational message
* display all rows int the table
* display one row in the table
* add a new row (Employee) to the table
* delete a row (Employee) from the table
* updata row (Employee) in the table

the simple endpoint will look something like the following: 

```  
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
		+ "<b>/allRows</b> - return a list of employees using a classic SELECT statement<br>"
		+ "<b>/oneEmployee/{empno}</b> - a list of employee records for the employee number provided<br>"
		+ "<b>/addEmployee/{firstName}/{lastName}</b> - add an employee<br>"				
		+ "<b>/deleteEmployee/{empNo}</b> - delete an employee<br>"
		+ "<b>/updateEmployee/{empNo}/{newSalary}</b> - update employee salary";
	}
```

### Add Service class

The REST controller contains an @Autowired annotation:

```
@Autowired  
private EmployeeService employeeService;
```

which enables the controller methods to call methods which service the incoming requests.  This service class makes the calls to the database using the jdbcTemplate class supplied by Spring. It is also often calls the dao(data access object) class. 

jdbcTemplate _"is the central class in the JDBC core package. It simplifies the use of JDBC and helps to avoid common errors. It executes core JDBC workflow, leaving application code to provide SQL and extract results. This class executes SQL queries or updates, initiating iteration over ResultSets and catching JDBC exceptions and translating them to the generic, more informative exception hierarchy defined in the org.springframework.dao package"._    

(from https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html  )


jdbcTemplate in this example application uses the query and update methods of that class. The jdbcTemplate in each case is passed a piece of SQL as a string and any result sets are processed by jdbcTemplate and returned in the appropriate object. In the case of the queries using the update method the jdbcTemplate.update returns an integer indicating the number of rows which have been affected by the update. 
 
The service class for this application EmployeeService.java can bee viewed [here](https:\\link.here.com) in the git sample application

The following snippet of code shows the jdbcTemplace being used to access all rows in the table

```
    public List<Employee> selectAll() throws NamingException {
        /*
         * Select all rows from the emp table
         * 
         *   datasource information comes from the application.properties file in the resources directory
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

```

### Configure application.properties
In the src/main/resources directory edit the application.properties file to contain the following line

```
spring.datasource.jndi-name=jdbc/jdbcDataSource
```

the name value should match exactly the jndi name which will be specified in the dataSource definition which we will add to server.xml later in this blog.

### Add a web.xml to the application
To avoid the warnings about using BLANK and default CICS userid, you need to include a simple web.xml to the application to enable basic authentication. This is described in the blog [Spring Boot Java applications for CICS - Part 2 - Security](https://github.com/cicsdev/cics-java-liberty-springboot-security/blob/master/blog/blog.md)


## Step 2: Build the application

We have now completed all the tasks required to develop our application. The next thing we must do is to build the application and deploy it to CICS.


Building the application can be done using Maven or alternatively Gradle. The use of these two methods was discussed in detail in the first part of this blog series[Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle/).

Using that knowledge you should now be in a position to enhance the *build.gradle*, or *pom.xml* to include the necessary dependencies to compile against this Springboot JDBC application

For Gradle, your build file should have the following dependencies.

```Gradle
dependencies 
{
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Don't include TomCat in the runtime build, but do put it in WEB-INF so it can be run standalone a well as embedded
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")

    // CICS BOM (as of May 2020)
    compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:5.5-20200519131930-PH25409")

    // Don't include JCICS in the final build (no need for version because we have BOM)
    compileOnly("com.ibm.cics:com.ibm.cics.server")          

    // Spring JDBC Support    
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
```

For Maven, you'll need the following dependencies in your pom.xml

```XML
  <!-- CICS BOM (as of MAy 2020) -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>com.ibm.cics</groupId>
        <artifactId>com.ibm.cics.ts.bom</artifactId>
        <version>5.5-20200519131930-PH25409</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Compile against, but don't include JCICS in the final build (version and scope are from BOM) -->
    <dependency>
      <groupId>com.ibm.cics</groupId>
      <artifactId>com.ibm.cics.server</artifactId>
    </dependency>

    <!-- Spring Boot web support -->   
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Integration JDBC Support  -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>

    <!-- Compile against, but don't include TomCat in the runtime build --> 
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
      <scope>provided</scope>
    </dependency>
  </dependencies>
```
If you subsequently add JCICS calls to this application then you will need to add the appropriate dependancies to your pom.xml or build.gradle files. This are described in the blog [Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle/)

## Step 3 : Configure Liberty

To deploy the sample into a CICS Liberty JVM server you will need to first build build the application as a WAR. Maven pom.xml and Gradle build.gradle files are provided in the sample Git repository to simplify this task. You will then need to

* Configure your CICS Liberty JVM server to use a SAF user registry
* Define the application to the Liberty server using an <application> element in the Liberty server.xml just as we did for scenario 1
* Configure an authorization method, using either Java EE roles or EJBROLE profiles defined in SAF.
* Start the Liberty server and start the application using the following example url 
   * http://*your host name*:*your port number*/com.ibm.cicsdev.springboot.jdbc-0.1.0/
  
  this will display a screen which looks as follows:
  
---
``` 
Spring Boot JDBC Employee REST sample. Date/Time: 2020-08-25:11-14-31.000188       
Usage: 

/allRows - return a list of employees using a classic SELECT statement 
/oneEmployee/{empno} - a list of employee records for the employee number provided 
/addEmployee/{firstName}/{lastName} - add an employee 
/deleteEmployee/{empNo} - delete an employee 
/updateEmployee/{empNo}/{newSalary} - update employee salary 
```
---


### Deploy the WAR into a CICS Liberty JVM server
There are two ways to deploy the WAR. 
1. You can add an <application> element to your server.xml which points to your uploaded WAR file location.
2. You can use a CICS bundle. In this article, we will introduce how to deploy the Spring Boot WAR as a WAR bundlepart with a CICS bundle.
  
Both methods are described in [Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle/


### Add Datasource defintion to server.xml

The server.xml requires a datasource defintion to enable the connection to DB2. There are two possibilities for this defintion: type 2 and type 4. Either can be used 

### Type 4
An example of a type 4 datasource is as follows:

```xml
  <dataSource id="t4a"  jndiName="jdbc/jdbcDataSource" type="javax.sql.DataSource">
         <jdbcDriver libraryRef="db2Lib"/>
        <properties.db2.jcc currentSchema="DSN81110" databaseName="DSNV11P2" driverType="4" password="+++++++++++++"
                      portNumber="<port num>" serverName="<your server name>" user="<user id"/>
  </dataSource>
 
  <library id="db2Lib">
        <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
        <fileset dir="/usr/lpp/db2v11/jdbc/lib"/>
   </library>
```

A type 4 datasource does not require CICS to have an installed DB2COONN defintion.

### Type 2
An example of a type 2 datasource is as follows:

```xml
   <dataSource id="t2a" jndiName="jdbc/jdbcDataSource" transactional="false" commitOrRollbackOnCleanup="commit">
        <jdbcDriver libraryRef="db2Lib"/>
        <properties.db2.jcc currentSchema="DSN81110" driverType="2"/>
        <connectionManager agedTimeout="0"/>
    </dataSource>
 <library id="db2Lib">
        <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
        <fileset dir="/usr/lpp/db2v11/jdbc/lib"/>
   </library>
```

A type 2 connection to DB2 requires that CICS has an active DB2CONN resource installed in the CICS region. 

In order to demonstrate the used of Springboot JDBC as simply as possible we have included the option commitOrRollbackOnCleanup="commit" on the datasource defintion. This will ensure that updates to the DB2 table are committed. Without this the default behaviour is to roll back any updates to the table. Without this option the application will, for example on the addEmployee request will not commit the update to the table and the record will not be stored in the table. This would not be realistic for a real world application. In a real world implementation the transactional features provided by Springboot should be added to the application. This is all described [Spring Boot Java applications for CICS - Part 3 - Transactions](https://github.com/cicsdev/cics-java-liberty-springboot-transactions/blob/master/blog/Blog.md)


## Step 4: Trying out the sample


#Trying out the sample
Find the base URL for the application in the Liberty messages.log 
    e.g. `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc-0.1.0.`


Paste the base URL along with the REST service suffix 'allRows' into the browser 
    e.g. `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc-0.1.0/allRows`


The browser will prompt for basic authentication. Enter a valid userid and password - according to the configured registry for your target Liberty JVM server.


All the rows in table EMP should be returned.


The allRows request calls a method in the application which uses the application.properties file to determine which datasource definition to use. If you make the same request to REST service allRows2 then the application uses the @Bean annotated dataSource method to determine the correct dataSource. The @Bean method will use the jndiName used in dataSource t4b whereas the application.properties file will used the jndiName specified in t4a.
    
## Summary of all available interfaces     


`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc/allRows`
    
 > All rows in table EMP will be returned - the datasource is obtained from the application.properties file
    
`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc/allRows2`
  
  >All rows in table EMP will be returned - the datasource is obtained from an @Bean method
    
`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc/addEmployee/{firstName}/{lastName}`
  
  >A new employee record will be created using the first name and last name supplied. All other fields in
  the table will be set by the application to the same values by this demo application.
  If successful the employee number created will be returned.
    
`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc/oneEmployee/{empno}`
  
  >A single employee record will be displayed if it exists.
    
`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc/updateEmployee/{empNo}/{newSalary}`
  >The employee record will be updated with the salary amount specified.
    
`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jdbc/deleteEmployee/{empNo}`
  
  >The employee record with the empNo specified will be deleted if it exists


## Notes:
{firstName} and {lastName} should be replaced by names of your choosing.
>>the definition of FIRSTNME in table EMP is VARCHAR(12)
>>the definition of LASTNAME in table EMP is VARCHAR(15)


{empno} would be replaced by a 6 character employee number. 
>>the definition of EMPNO in the EMP table is char(6)


{newSalary} should be replaced by a numeric amount 
>>the definition of SALARY in the EMP table is DECIMAL(9, 2)





