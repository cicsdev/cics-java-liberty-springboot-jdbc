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

E.g. as follows for JDBC type 2 connectivity (substitute values as necessary!):

``` XML
<dataSource id="t2" jndiName="jdbc/jdbcDataSource" transactional="false">
        <jdbcDriver>   
            <library name="DB2LIB">
                <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
                <fileset dir="/usr/lpp/db2v11/jdbc/lib" includes="libdb2jcct2zos4_64.so"/>
            </library>
        </jdbcDriver>
        <properties.db2.jcc currentSchema="YOUR_SCHEMA" driverType="2"/>
        <connectionManager agedTimeout="0"/>
</dataSource>
```        

...or for JDBC type 4 connectivity (substitute values as necessary!):

``` XML
<dataSource id="t4" jndiName="jdbc/jdbcDataSource" type="javax.sql.XADataSource">
    <jdbcDriver>   
        <library name="DB2LIB">
            <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
            <fileset dir="/usr/lpp/db2v11/jdbc/lib" includes="libdb2jcct2zos4_64.so"/>
        </library>
    </jdbcDriver>
    <properties.db2.jcc driverType="4" 
        serverName="yourserver.corporation.com"   
        portNumber="41100" 
        currentSchema="YOUR_SCHEMA"       
        databaseName="YOUR_DATABASE" 
        user="USER"
        password="PASSWORD"               
    />     
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

2. Copy the context root from message CWWKT0016I along with the REST service suffix into you web browser. For example display all the Employees from the EMP table:
    - `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/allEmployees` 

   The browser will prompt for basic authentication. Enter a valid userid and password - according to the configured registry for your target Liberty JVM server.
   
3. For more information on how to use this sample, request the context root:
   - `http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jdbc-0.1.0/`
     
    
## Additional notes on Transactional behaviour
There are three types of Db2 DataSource definition that can be used in CICS Liberty, all use the Db2 JDBC driver (JCC). They are:
- the original `cicsts_dataSource` using type 2 connectivity (DB2CONN) and supporting driver manager
- a Liberty `dataSource` with type 2 connectivity (using CICS DB2CONN for connection management)
- a Liberty `dataSource` with type 4 connectivity (using TCP/IP and Liberty for connection management)

DataSources are defined in server.xml, and JNDI is used by this application to autowire to the specified DataSource given by the URL in `application.properties`. 
It is important to note that when the Db2 JDBC driver is operating in a CICS environment with type 2 connectivity, the autocommit property is <i>forced</i> to 'false' and  
by default the `commitOrRollbackOnCleanup` property is set to 'rollback'. Traditionally this has been because the driver defers to CICS UOW processing to demark transactions in a CICS application.
Conversely, JDBC type 4 connectivity defaults to 'autocommit=true' as this is more standard in a distributed environment. 

Additionally the `commitOrRollbackOnCleanup` property does <b>not</b> apply if autocommit is on, AND autocommit does not apply if using a global txn.

The differing values of these properties for different DataSource types, give rise to different transactional behaviour when used in CICS Liberty. 
For example, calling the `/addEmployee` endpoint in this sample with a Liberty type 4 DataSource will result in an automatic commit, 
the same call using a Liberty type 2 DataSource will result in rollback, because autocommit=false (forced by JCC driver) and the clean-up behaviour (if there is no explicit transaction) is to rollback.
For the `cicsts_dataSource` which uses type 2 connectivity, the behaviour is similar to Liberty type 4 but this DataSource implementation does not involve the Liberty transaction manager by default and so the clean-up behaviour does not apply. 
Thus when the transaction finishes, CICS will implicitly commit the UOW, and the database updates are committed. 

You can emulate the autocommit behaviour for a Liberty DataSource with type 2 connectivity by setting the `commitOrRollbackOnCleanUp` property to 'commit'. 
However, should the application then cause an exception or abend, the CICS UOW containing the Db2 update, has already been committed and only a second new (empty) UOW is rolled back.

Thus, for each update operation in this sample we provide a second end-point version (postfix 'Tx') which wraps the call in an XA (global) transaction and in all environments the behaviour remains fully transactional and consistent.
You can observe the differences in behaviour by defining different DataSource types in your server.xml, and driving the different local vs global transaction endpoints.


### License:
This project is licensed under [Eclipse Public License - v 2.0](LICENSE). 
