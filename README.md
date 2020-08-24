# cics-java-liberty-springboot-jdbc

This project demonstrates a Spring Boot JDBC application integrated with IBM CICS that can be deployed to a CICS Liberty JVM server. The application makes use of the employee sample table supplied with Db2 for z/OS. The application allows you to add, update, delete or display employee information from the table EMP. The sample also provides a set of Gradle and Maven build files for use either in Eclipse or standalone build environments.

## Requirements

* CICS TS V5.3 or later
* A configured Liberty JVM server
* Java SE 1.8 or later on the z/OS system
* Java SE 1.8 or later on the workstation
* IBM Db2 V11 or later on z/OS
* An Eclipse development environment on the workstation (optional)
* Either Gradle or Apache Maven on the workstation (optional if using Wrappers)

## Downloading
* Clone the repository using your IDE's support, such as the Eclipse Git plugin
* **or**, download the sample as a ZIP and unzip onto the workstation

>*Tip: Eclipse Git provides an 'Import existing Projects' check-box when cloning a repository.*
### Check Dependencies

Before building this sample, you should verify that the correct CICS TS bill of materials (BOM) is specified for your target release of CICS. The BOM specifies a consistent set of artifacts, and adds information about their scope. In the example below the version specified is compatible with CICS TS V5.5 with JCICS APAR PH25409, or newer. That is, the Java byte codes built by compiling against this version of JCICS will be compatible with later CICS TS versions and subsequent JCICS APARs. You can browse the published versions of the CICS BOM at Maven Central.

Gradle (build.gradle):

`compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:5.5-20200519131930-PH25409")`

Maven (POM.xml):

``` xml
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
```

## Building

You can build the sample using an IDE of your choice, or you can build it from the command line. For both approaches, using the supplied Gradle or Maven wrapper is the recommended way to get a consistent version of build tooling.

On the command line, you simply swap the Gradle or Maven command for the wrapper equivalent, `gradlew` or `mvnw` respectively.

For an IDE, taking Eclipse as an example, the plug-ins for Gradle *buildship* and Maven *m2e* will integrate with the "Run As..." capability, allowing you to specify whether you want to build the project with a Wrapper, or a specific version of your chosen build tool.

The required build-tasks are typically `clean bootWar` for Gradle and `clean package` for Maven. Once run, Gradle will generate a WAR file in the `build/libs` directory, while Maven will generate it in the `target` directory.

**Note:** When building a WAR file for deployment to Liberty it is good practice to exclude Tomcat from the final runtime artifact. We demonstrate this in the pom.xml with the *provided* scope, and in build.gradle with the *providedRuntime()* dependency.

**Note:** If you import the project to your IDE, you might experience local project compile errors. To resolve these errors you should run a tooling refresh on that project.
For example, in Eclipse: 
* for Gradle, right-click on "Project", select "Gradle -> Refresh Gradle Project", 
* for Maven, right-click on "Project", select "Maven -> Update Project...".

> Tip: *In Eclipse, Gradle (buildship) is able to fully refresh and resolve the local classpath even if the project was previously updated by Maven. However, Maven (m2e) does not currently reciprocate that capability. If you previously refreshed the project with Gradle, you'll need to manually remove the 'Project Dependencies' entry on the Java build-path of your Project Properties to avoid duplication errors when performing a Maven Project Update.*

#### Gradle Wrapper (command line)

Run the following in a local command prompt:

On Linux or Mac:

```shell
./gradlew clean bootWar
```
On Windows:

```shell
gradlew.bat clean bootWar
```

This creates a WAR file in the `build/libs` directory.

#### Maven Wrapper (command line)


Run the following in a local command prompt:

On Linux or Mac:

```shell
./mvnw clean package
```

On Windows:

```shell
mvnw.cmd clean package
```

This creates a WAR file in the `target` directory.

## Deploying to a CICS Liberty JVM Server

- Ensure you have the following features defined in your Liberty `server.xml`:           
    - `<servlet-3.1>` or `<servlet-4.0>` depending on the version of Java EE in use.  
    - `<cicsts:security-1.0>` if CICS security is enabled.
    - `<jsp-2.3>`
    - `<jdbc-4.0>`

>**Note:** `servlet-4.0` will only work for CICS TS V5.5 or later

- add a datasource definition to 'server.xml'.

E.g. as follows:

``` XML
<dataSource id="t2" jndiName="jdbc/jdbcDataSource" transactional="false">
        <jdbcDriver>   
            <library name="DB2LIB">
                <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
                <fileset dir="/usr/lpp/db2v11/jdbc/lib" includes="libdb2jcct2zos4_64.so"/>
            </library>
        </jdbcDriver>
        <properties.db2.jcc currentSchema="DSN81110" driverType="2"/>
        <connectionManager agedTimeout="0"/>
</dataSource>
```        

- set spring.datasource.jndi-name in application.properties

The file application.properties in /src/main/resources/ contains the setting 
``` shell
spring.datasource.jndi-name=jdbc/jdbcDataSource
```
which will direct the application to the dataSource defintion in the server.xml which must have parameter jndiName set to the same value specified in the application properties file

- alternatively use a datasource Bean

The jndi-name can alternatively be set using a datasource bean in the application code. In order to do this you would define the bean in the application. To do this the application.java class would be as follows:

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
and then in the EmployeeService class the bean must be Autowired as follows:

```
@Autowired
private DataSource myDatasource;
```

- Deployment option 1:
    - Copy and paste the built WAR from your *target* or *build/libs* directory into a Eclipse CICS bundle project and create a new WAR bundlepart that references the WAR file. Then deploy the CICS bundle project from CICS Explorer using the **Export Bundle Project to z/OS UNIX File System** wizard.
    
   
- Deployment option 2:
    - Manually upload the WAR file to zFS and add an `<application>` element to the Liberty server.xml to define the web application with access to all authenticated users. For example the following application element can be used to install a WAR, and grant access to all authenticated users if security is enabled.
 
``` XML
   <application id="com.ibm.cicsdev.springboot.jdbc-0.1.0"  
     location="${server.config.dir}/springapps/com.ibm.cicsdev.springboot.jdbc-0.1.0.war"  
     name="com.ibm.cicsdev.springboot.jdbc-0.1.0" type="war">
     <application-bnd>
        <security-role name="cicsAllAuthenticated">
            <special-subject type="ALL_AUTHENTICATED_USERS"/>
        </security-role>
     </application-bnd>  
   </application>
```

## Trying out the sample
1. Ensure the web application started successfully in Liberty by checking for msg `CWWKT0016I` in the Liberty messages.log:
    - `A CWWKT0016I: Web application available (default_host): http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jcics-0.1.0`
    - `I SRVE0292I: Servlet Message - [com.ibm.cicsdev.springboot.jcics-0.1.0]:.Initializing Spring embedded WebApplicationContext`

2. Copy the context root from message CWWKT0016I along with the REST service suffix into you web browser. For example display all the rows from the EMP table:
    - `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/allRows` 

   The browser will prompt for basic authentication. Enter a valid userid and password - according to the configured registry for your target Liberty JVM server.

   All the rows in table EMP should be returned.

   The `allRows` request calls a method in the application which uses the `application.properties` file to determine which dataSource definition to use. If you make the same request to REST service `/allRows2` then the application uses the `@Bean` annotated dataSource method to determine the correct dataSource. The `@Bean` method will use the `jndiName` value specified in dataSource `t4b` whereas the `application.properties` file will used the `jndiName` value specified in `t4a`.
    
## Summary of all available interfaces     

- `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/allRows`
    
  >All rows in table EMP will be returned - the dataSource is obtained from the `application.properties` file
    
- `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/allRows2`
  
  >All rows in table EMP will be returned - the dataSource is obtained from an `@Bean` method
    
- `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/addEmployee/{firstName}/{lastName}`
  
  >A new employee record will be created using the first name and last name supplied. All other fields in
  the table will be set by the application to the same values by this demo application.
  If successful the employee number created will be returned.
    
- `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/oneEmployee/{empno}`
  
  >A single employee record will be displayed if it exists.
    
- `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/updateEmployee/{empNo}/{newSalary}`
  >The employee record will be updated with the salary amount specified.
    
- `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/deleteEmployee/{empNo}`
  
  >The employee record with the empNo specified will be deleted if it exists

### Notes:
{firstName} and {lastName} should be replaced by names of your choosing.
>>the definition of FIRSTNME in table EMP is VARCHAR(12)
>>the definition of LASTNAME in table EMP is VARCHAR(15)

{empno} would be replaced by a 6 character employee number. 
>>the definition of EMPNO in the EMP table is char(6)

{newSalary} should be replaced by a numeric amount 
>>the definition of SALARY in the EMP table is DECIMAL(9, 2)

License
This project is licensed under [Eclipse Public License - v 2.0](LICENSE). 
