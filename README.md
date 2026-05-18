# cics-java-liberty-springboot-jdbc

[![Build](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/actions/workflows/java.yaml/badge.svg)](https://github.com/cicsdev/cics-java-liberty-springboot-jdbc/actions/workflows/java.yaml)
[![License](https://img.shields.io/badge/License-EPL%202.0-green.svg)](https://opensource.org/licenses/EPL-2.0)

## Overview

This sample demonstrates how to integrate **Spring Boot** with **IBM CICS** using **JDBC** on **CICS Liberty JVM server**. The application makes use of the employee sample table supplied with Db2 for z/OS. The application allows you to add, update, delete or display employee information from the table EMP. 

The sample is intended both as a runnable example and as an educational reference for developers building enterprise-grade Spring Boot applications with JDBC on CICS Liberty.

**What This Sample Does:**
- Provides REST endpoints for employee data management (add, update, delete, display)
- Demonstrates Spring Boot JDBC integration with Db2 for z/OS
- Shows both transactional and non-transactional database operations
- Supports both JDBC Type 2 (local) and Type 4 (remote) connectivity
- Integrates with CICS security for authentication and authorization

---

## Table of Contents

1. [Design and Architecture](#design-and-architecture)
2. [How It Works](#how-it-works)
3. [Before You Start: Files to Modify](#before-you-start-files-to-modify)
4. [Requirements](#requirements)
5. [Project Structure](#project-structure)
6. [Configuration Guide](#configuration-guide)
7. [Building the Sample](#building-the-sample)
8. [Deploying to CICS](#deploying-to-cics)
    - [Method 1: CICS Bundle Deployment (Gradle/Maven)](#method-1-cics-bundle-deployment-gradlemaven)
    - [Method 2: CICS Explorer Deployment](#method-2-cics-explorer-deployment)
    - [Method 3: Direct Liberty Application Deployment](#method-3-direct-liberty-application-deployment)
    - [Common Bundle Installation Steps](#common-bundle-installation-steps)
9. [Running the Sample](#running-the-sample)
10. [Troubleshooting](#troubleshooting)
11. [License](#license)
12. [Additional Resources](#additional-resources)
13. [Contributing](#contributing)

---

## Design and Architecture

### High-Level Design Intent

This sample addresses a fundamental challenge: **how to build Spring Boot applications that integrate with CICS and Db2 for z/OS while maintaining enterprise-grade transaction management and security**.

Key components:

1. **Spring Boot Framework** - Provides dependency injection, REST support, and JDBC abstraction
2. **Spring Data JDBC** - Simplifies database access with JdbcTemplate
3. **Liberty JNDI Datasource** - Connects to Db2 via JNDI lookup
4. **CICS Transaction Context** - Ensures operations run within CICS transactions

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         HTTP Client                             │
│                    (Browser, curl, etc.)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ REST Requests
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CICS Liberty JVM Server                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │           Spring Boot Application (WAR)                   │  │
│  │                                                           │  │
│  │  ┌──────────────────────────────────────────────────┐     │  │
│  │  │      EmployeeRestController                      │     │  │
│  │  │  • @GetMapping endpoints                         │     │  │
│  │  │  • /allEmployees                                 │     │  │
│  │  │  • /listEmployee/{empno}                         │     │  │
│  │  │  • /addEmployee/{firstName}/{lastName}           │     │  │
│  │  │  • /updateEmployee/{empNo}/{newSalary}           │     │  │
│  │  │  • /deleteEmployee/{empNo}                       │     │  │
│  │  │  • Transactional variants (*Tx endpoints)        │     │  │
│  │  └────────────┬─────────────────────────────────────┘     │  │
│  │               │                                           │  │
│  │               ▼                                           │  │
│  │  ┌──────────────────────────────────────────────────┐     │  │
│  │  │      EmployeeService                             │     │  │
│  │  │  • Business logic layer                          │     │  │
│  │  │  • Uses Spring JdbcTemplate                      │     │  │
│  │  │  • selectAll(), addEmployee()                    │     │  │
│  │  │  • updateEmployee(), deleteEmployee()            │     │  │
│  │  └────────────┬─────────────────────────────────────┘     │  │
│  │               │                                           │  │
│  │               ▼                                           │  │
│  │  ┌──────────────────────────────────────────────────┐     │  │
│  │  │      Spring JdbcTemplate                         │     │  │
│  │  │  • Autowired from Spring context                 │     │  │
│  │  │  • Configured via application.properties         │     │  │
│  │  └────────────┬─────────────────────────────────────┘     │  │
│  │               │                                           │  │
│  │               ▼                                           │  │
│  │  ┌──────────────────────────────────────────────────┐     │  │
│  │  │   Liberty JNDI Datasource                        │     │  │
│  │  │  • jndiName: jdbc/jdbcDataSource                 │     │  │
│  │  │  • Configured in server.xml                      │     │  │
│  │  └────────────┬─────────────────────────────────────┘     │  │
│  │               │                                           │  │
│  └───────────────┼───────────────────────────────────────────┘  │
│                  │                                              │
│                  ▼                                              │
│  ┌──────────────────────────────────────────────────┐           │
│  │   IBM Db2 JDBC Driver (Type 2 or Type 4)        │           │
│  └────────────┬─────────────────────────────────────┘           │
└───────────────┼──────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    IBM Db2 for z/OS                             │
│                    (EMP Sample Table)                           │
└─────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Purpose | Key Features |
|-----------|---------|--------------|
| **Application** | Spring Boot entry point | `@SpringBootApplication`, main method |
| **EmployeeRestController** | REST API endpoints | `@RestController`, `@GetMapping`, routes requests |
| **EmployeeService** | Business logic layer | `@Service`, `@Autowired JdbcTemplate`, CRUD operations |
| **Employee** | Data model | POJO representing EMP table structure |
| **ServletInitializer** | WAR deployment support | Extends `SpringBootServletInitializer` |
| **application.properties** | Configuration | JNDI datasource name |
| **server.xml** | Liberty configuration | Datasource, features, security |

---

## How It Works

### Request Flow (Step-by-Step)

1. **User Sends HTTP Request**
   - HTTP GET to `/allEmployees` or other endpoint
   - Request authenticated by Liberty security

2. **Spring Boot Routes Request**
   - `EmployeeRestController` receives the request
   - Appropriate method is invoked based on URL mapping
   - For transactional endpoints (`*Tx`), `@Transactional` annotation is active

3. **Service Layer Processes Request**
   - `EmployeeService` method is called
   - Business logic is executed (validation, data transformation)
   - SQL query is constructed

4. **Database Access via JdbcTemplate**
   - Spring's `JdbcTemplate` executes the SQL
   - JNDI lookup retrieves the datasource (`jdbc/jdbcDataSource`)
   - Connection is obtained from the Liberty datasource pool

5. **Db2 Executes Query**
   - JDBC driver (Type 2 or Type 4) communicates with Db2
   - Query is executed against the EMP table
   - Results are returned to the application

6. **Response Returned to Client**
   - Data is serialized to JSON (for list operations)
   - Or a success/failure message is returned (for updates)
   - HTTP response is sent back to the client

### Transaction Management

**Non-Transactional Operations (Default):**
- Each database operation auto-commits immediately
- Suitable for simple read operations
- Example: `/allEmployees`, `/listEmployee/{empno}`

**Transactional Operations (XA):**
- Annotated with `@Transactional`
- Participates in global (XA) transactions
- Rollback on exception, commit on success
- Example: `/addEmployeeTx/{firstName}/{lastName}`
- Requires `transactional="true"` or XA datasource in server.xml

---

## Before You Start: Files to Modify

Before building and deploying this sample, you **must** customize the following files with your environment-specific values:

### 1. Db2 Connection Configuration

**File:** Liberty `server.xml`

**What to change:**

For **JDBC Type 2** (local connectivity):
```xml
<!-- Configure the IBM Data Server Driver for JDBC and SQLJ for Db2 driver library -->
<library id="db2Type2Driver">
    <fileset dir="/usr/lpp/db2v12/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
    <fileset dir="/usr/lpp/db2v12/jdbc/lib" includes="libdb2jcct2zos4_64.so"/>
</library>

<!-- Configure the DataSource -->
<dataSource id="db2Type2" jndiName="jdbc/jdbcDataSource" transactional="false" commitOrRollbackOnCleanup="commit" type="javax.sql.DataSource">
    <jdbcDriver libraryRef="db2Type2Driver"/>    
    <properties.db2.jcc currentSchema="YOUR_SCHEMA" driverType="2"/>    
    <connectionManager agedTimeout="0"/>
</dataSource>
```

For **JDBC Type 4** (substitute your values as necessary):
```xml
<dataSource id="t4" jndiName="jdbc/jdbcDataSource" type="javax.sql.XADataSource">
    <jdbcDriver>   
        <library name="DB2LIB">
            <fileset dir="/usr/lpp/db2v11/jdbc/classes" includes="db2jcc4.jar db2jcc_license_cisuz.jar"/>
            <fileset dir="/usr/lpp/db2v11/jdbc/lib" includes="libdb2jcct2zos4_64.so"/>
        </library>
    </jdbcDriver>
    <properties.db2.jcc driverType="4" 
        serverName="YOUR.SERVER.CORPORATION.COM"   
        portNumber="YOUR_PORT_NUMBER" 
        currentSchema="YOUR_SCHEMA"       
        databaseName="YOUR_DATABASE" 
        user="USER"
        password="PASSWORD"               
    />     
</dataSource>
```

---

### 2. CICS Region Configuration

**Prerequisites:**

Before deploying, ensure your CICS region has:

1. **DB2CONN Setup:**
   - DB2CONN resource defined and installed in CICS

2. **CICS SIT Parameters:**
   ```
   DB2CONN=YES
   ```

3. **JCL Configuration:**
   Add these lines to your CICS region JCL:
   ```
   //         DD DSN=SYS2.DB2.V12.SDSNLOAD,DISP=SHR
   //         DD DSN=SYS2.DB2.V12.SDSNLOD2,DISP=SHR
   ```

---

### 3. Liberty Server Configuration

**File:** Liberty `server.xml`

**Required Features:**
```xml
<featureManager>
    <feature>servlet-6.0</feature>        <!-- or servlet-4.0 for CICS TS V5.5+ -->
    <feature>pages-3.1</feature>
    <feature>jdbc-4.3</feature>           <!-- or jdbc-4.1 -->
    <feature>cicsts:security-1.0</feature> <!-- if CICS security is enabled -->
</featureManager>
```

**Notes:**
- `servlet-6.0` requires CICS TS V6.1 or later
- `cicsts:security-1.0` is automatically added if SEC=YES in SIT
- For `jdbc-4.3`, add `type="javax.sql.DataSource"` to datasource definition

---

### 4. Application Configuration

**File:** `cics-java-liberty-springboot-jdbc-app/src/main/resources/application.properties`

**Verify JNDI name matches:**
```properties
spring.datasource.jndi-name=jdbc/jdbcDataSource
```

This must match the `jndiName` attribute in your server.xml datasource definition.

---

### 5. Build Configuration (Optional)

**Files:**
- `cics-java-liberty-springboot-jdbc-cicsbundle/build.gradle`
- `cics-java-liberty-springboot-jdbc-cicsbundle/pom.xml`

**When is this needed?**
Only if using **CICS Bundle Deployment** with Gradle or Maven. This tells the CICS bundle plugins which Liberty JVM server will run your application.

**What to change:**

Gradle (`build.gradle`):
```gradle
cics.jvmserver = 'YOUR_JVMSERVER_NAME'  // e.g., 'DFHWLP'
```

Maven (`pom.xml`):
```xml
<cics.jvmserver>YOUR_JVMSERVER_NAME</cics.jvmserver>  <!-- e.g., DFHWLP -->
```

---

### Summary Checklist

Before building:
- [ ] Verified Db2 connection is available from CICS region
- [ ] Updated `server.xml` with datasource configuration (Type 2 or Type 4)
- [ ] Configured DB2CONN in CICS region
- [ ] Added DB2CONN=YES to CICS SIT parameters
- [ ] Updated CICS region JCL with Db2 load libraries
- [ ] Verified Liberty features in `server.xml`
- [ ] Confirmed JNDI name matches in `application.properties` and `server.xml`
- [ ] Updated JVM server name in build files (if using CICS bundle deployment)

---

## Requirements

### Workstation Requirements
* **Java:** Java SE 17 or later (required for Spring Boot 3.x)
* **Build Tools:**
  - **Gradle:** Version 7.3+ (Java 17 support) - Recommended: 8.0+ - included via wrapper
  - **Maven:** Version 3.8.1+ (Java 17 support) - Recommended: 3.9.0+ - included via wrapper
* **IDE (Optional):**
  - Eclipse with IBM CICS SDK for Java EE, Jakarta EE and Liberty
  - IntelliJ IDEA, VS Code, or any IDE with Gradle/Maven support
  - Command line (no IDE required if using wrappers)

### z/OS Requirements
* **CICS TS:** V5.3 or later
* **WebSphere Liberty:** Included with CICS
* **IBM Db2:** V11 or later on z/OS
* **Java:** IBM Semeru Runtime 17 or later on z/OS

### Database Requirements
* Access to Db2 for z/OS with the EMP sample table
* The EMP table is typically provided in the Db2 sample database
* Appropriate permissions to SELECT, INSERT, UPDATE, DELETE on the EMP table

---

## Project Structure

```
cics-java-liberty-springboot-jdbc/
├── README.md                                    # This file
├── LICENSE                                      # EPL 2.0 license
├── MAINTAINERS.md                               # Project maintainers
├── settings.gradle                              # Gradle multi-project settings
├── pom.xml                                      # Root Maven POM (multi-module)
├── gradle.properties                            # Java version configuration
├── gradlew / gradlew.bat                        # Gradle wrapper scripts
├── mvnw / mvnw.cmd                              # Maven wrapper scripts
│
├── cics-java-liberty-springboot-jdbc-app/       # Main Spring Boot application
│   ├── build.gradle                             # App-level Gradle build
│   ├── pom.xml                                  # App-level Maven POM
│   └── src/main/
│       ├── java/com/ibm/cicsdev/springboot/jdbc/
│       │   ├── Application.java                 # Spring Boot entry point
│       │   ├── EmployeeRestController.java      # REST API endpoints
│       │   ├── EmployeeService.java             # Business logic layer
│       │   ├── Employee.java                    # Data model (EMP table)
│       │   └── ServletInitializer.java          # WAR deployment support
│       ├── resources/
│       │   └── application.properties           # Spring configuration (JNDI name)
│       └── webapp/WEB-INF/
│           └── web.xml                          # Web application descriptor
│
├── cics-java-liberty-springboot-jdbc-cicsbundle/    # CICS bundle (Gradle/Maven)
│   ├── build.gradle                             # Bundle Gradle build
│   └── pom.xml                                  # Bundle Maven POM
│
├── etc/config/cics_bundle_project/              # CICS Explorer bundle project
│   └── cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0/
│       ├── cics-java-liberty-springboot-jdbc.warbundle
│       └── META-INF/cics.xml
│
└── gradle/ & .mvn/                              # Wrapper support files
```

---

## Configuration Guide

### Verify Spring Boot Version

This sample uses **Spring Boot 3.X**, which requires **Java 17** or later.

**Gradle** (`build.gradle`):
```gradle
plugins {
    id 'org.springframework.boot' version '3.5.8'
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    sourceCompatibility = JavaVersion.toVersion(17)
    targetCompatibility = JavaVersion.toVersion(17)
}
```

**Maven** (`pom.xml`):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.8</version>
</parent>

<properties>
    <java.version>17</java.version>
</properties>
```

## Building the Sample

You can build using Gradle, Maven, or Eclipse. The wrappers are pre-configured with compatible versions.

### Option 1: Building with Gradle

**From the root directory:**

Linux/Mac:
```bash
./gradlew clean build
```

Windows:
```cmd
gradlew.bat clean build
```

**Output:**
- WAR file: `cics-java-liberty-springboot-jdbc-app/build/libs/cics-java-liberty-springboot-jdbc.war`
- CICS bundle ZIP: `cics-java-liberty-springboot-jdbc-cicsbundle/build/distributions/cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0.zip`

**Note:** 
- In Eclipse, the `build` directory may be hidden. To view it: Package Explorer → ⋮ menu → Filters → Uncheck "Gradle build folder"

---

### Option 2: Building with Maven

**From the root directory:**

Linux/Mac:
```bash
./mvnw clean package
```

Windows:
```cmd
mvnw.cmd clean package
```

**Output:**
- WAR file: `cics-java-liberty-springboot-jdbc-app/target/cics-java-liberty-springboot-jdbc.war`
- CICS bundle ZIP: `cics-java-liberty-springboot-jdbc-cicsbundle/target/cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0.zip`

---

### Option 3: Building with Eclipse

1. **Clone and Import Repository:**
   - File → Import → Git → Projects from Git → Clone URI
   - Enter the repository URL
   - Ensure "Import existing Eclipse projects" box is checked
   - Complete the wizard to clone and import the projects

2. **Resolve Build Path (if needed):**
   - Right-click project → Properties → Java Build Path → Libraries
   - Add Library → CICS with Enterprise Java and Liberty
   - Select appropriate CICS and Java EE versions

3. **Build:**
   - Right-click `cics-java-liberty-springboot-jdbc` → Run As → Gradle Build (or Maven Build)
   - Goals: `clean build` (Gradle) or `clean package` (Maven)

**Tip:** If switching between Gradle and Maven in Eclipse, you may need to manually remove duplicate "Project Dependencies" entries from the build path.

---

## Deploying to CICS

### Method 1: CICS Bundle Deployment (Gradle/Maven)

1. **Build the bundle** (see [Building the Sample](#building-the-sample))

2. **Upload the bundle ZIP to z/OS:**
   ```bash
   # Upload the ZIP file to zFS
   scp cics-java-liberty-springboot-jdbc-cicsbundle/build/distributions/cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0.zip user@zos:/path/to/bundles/
   ```

   **Note:** `scp` is a standard Unix/Linux command for secure file transfer. Replace `user@zos` with your z/OS credentials and `/path/to/bundles/` with your target directory.

3. **Extract on z/OS:**
   ```bash
   cd /path/to/bundles
   jar xf cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0.zip
   ```

4. **Define and install the bundle in CICS:**

   See [Common Bundle Installation Steps](#common-bundle-installation-steps) below.

---

### Method 2: CICS Explorer Deployment

This method uses IBM CICS Explorer (an Eclipse-based IDE) to create a CICS bundle and deploy it directly to z/OS. This approach is ideal for developers who prefer a GUI-based deployment workflow and want integrated tooling for CICS development.

**Prerequisites:**
- IBM CICS Explorer installed on your workstation
- CICS Explorer configured with connection to your z/OS system
- SSH/SFTP access to z/OS UNIX System Services (USS)
- CICS region configured and running

#### Step 1: Review CICS Bundle Project in Eclipse

A CICS bundle is a deployment package that can contain multiple resources (WARs, JARs, OSGi bundles, etc.) and their metadata.
The bundle project is already provided in the repository (imported when you cloned the repo).

1. **Locate the Bundle Project:**
    In Project Explorer, locate the imported bundle project
    (e.g., cics-java-liberty-springboot-jdbc-cicsbundle).

    Look into the project to verify its structure and contents.

2. **Verify WAR Bundle Part Configuration:**
    - Locate the .warbundle file
    - Confirm the following:
        JVM server is correctly specified (e.g., DFHWLP)
        WAR file path points to the correct application artifact

3. **Review Generated Files:**
   
   The bundle project now contains:
   ```
   cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0/
   ├── META-INF/
   │   └── cics.xml          # Bundle manifest (defines bundle contents)
   ├── .project              # Eclipse project file
   └── cics-java-liberty-springboot-jdbc.warbundle  # WAR bundle part descriptor
   ```

#### Step 2: Export Bundle to z/OS UNIX File System (zFS)

This step deploys your bundle to z/OS and makes it available to CICS.

1. **Initiate Export:**
   - In **Project Explorer**, right-click on your bundle project
   - Select **Export Bundle Project to z/OS UNIX File System**
   - Click **Next**

2. **Specify Bundle Deployment Location:**
   
   Choose where on z/OS to deploy the bundle:
   - **Target directory**: `/u/cicsts/bundles/cics-java-liberty-springboot-jdbc-cicsbundle_1.0.0`
   
   **Important Path Considerations:**
   - The directory will be created if it doesn't exist

   Click **Finish** to start the export.

   Ensure that the CICS region user has read and write access to the bundle directory structure.

#### Step 3: Define and Install the Bundle in CICS

    After export, define and install the bundle in your CICS region. See [Common Bundle Installation Steps](#common-bundle-installation-steps) below.

#### Step 4: Verify Deployment

1. **Check Liberty Messages:**
   - View Liberty server logs
   - Look for:
   ```
   [AUDIT   ] CWWKT0016I: Web application available (default_host):
              http://hostname:port/cics-java-liberty-springboot-jdbc/
   ```

2. **Verify in CICS Explorer:**
   - Status should show: **Enabled** and **Installed**

---

### Method 3: Direct Liberty Application Deployment

1. **Upload the WAR file to zFS:**

   ```bash
   # From your workstation (using secure copy)
   scp cics-java-liberty-springboot-jdbc-app/build/libs/cics-java-liberty-springboot-jdbc.war user@zos:/path/to/apps/
   ```

   **Note:** Replace `user@zos` with your z/OS credentials and `/path/to/liberty/apps/` with your Liberty apps directory.

2. **Add to Liberty server.xml:**
   ```xml
   <application id="cics-java-liberty-springboot-jdbc"  
                location="${server.config.dir}/apps/cics-java-liberty-springboot-jdbc.war"
                type="war">
       <application-bnd>
           <security-role name="cicsAllAuthenticated">
               <special-subject type="ALL_AUTHENTICATED_USERS"/>
           </security-role>
       </application-bnd>  
   </application>
   ```

3. **Restart or refresh the Liberty server**

---

### Common Bundle Installation Steps

These steps apply to both Method 1 and Method 2 after the bundle is on z/OS.

**Option A: Using CEDA Commands**

1. **Define the Bundle:**
   ```
   CEDA DEFINE BUNDLE(JDBCBNDL)
    GROUP(MYGROUP)
    BUNDLEDIR(/u/cicsts/bundles/cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0)
    STATUS(ENABLED)
   ```

2. **Install the Bundle:**
   ```
   CEDA INSTALL BUNDLE(JDBCBNDL) GROUP(MYGROUP)
   ```

3. **Enable (if needed):**
   ```
   CEDA SET BUNDLE(JDBCBNDL) ENABLED
   ```

**Option B: Using CICS Explorer UI**

1. Navigate to **CICS SM** (Systems Management) view
2. Expand your CICS region → **Bundle Definitions**
3. Right-click → **New** → **Bundle Definition**
4. Fill in:
   - Name: `JDBCBNDL`
   - Group: `MYGROUP`
   - Bundle Directory: `/u/cicsts/bundles/cics-java-liberty-springboot-jdbc-cicsbundle-1.0.0`
   - Status: `Enabled`
5. Save and right-click → **Install**

**Verify Installation:**
- Check Liberty messages.log for application startup
- In CICS Explorer: Bundle Definitions → `JDBCBNDL` should show **Enabled** and **Installed**

---

## Running the Sample

### Step 1: Verify Deployment

Check that the application started successfully in Liberty:

```bash
# Check Liberty messages.log
tail -f /path/to/liberty/logs/messages.log
```

Look for:
```
CWWKT0016I: Web application available (default_host): http://host:port/cics-java-liberty-springboot-jdbc
```

---

### Step 2: Access the Application

Open a web browser or use curl to access the application:

**Root endpoint (usage information):**
```
http://host:port/cics-java-liberty-springboot-jdbc/
```

The browser will prompt for basic authentication. Enter a valid userid and password according to your Liberty security configuration.

---

### Step 3: Try the REST Endpoints

**Display all employees:**
```bash
curl -u userid:password "http://host:port/cics-java-liberty-springboot-jdbc/allEmployees"
```

**Display specific employee:**
```bash
curl -u userid:password "http://host:port/cics-java-liberty-springboot-jdbc/listEmployee/000100"
```

---

**Note:** For XA transactions to work properly, ensure your datasource is configured with `type="javax.sql.XADataSource"` in server.xml.

---

### Available Endpoints Summary

| Endpoint | Method | Description | Transactional |
|----------|--------|-------------|---------------|
| `/` | GET | Usage information and endpoint list | No |
| `/allEmployees` | GET | List all employees | No |
| `/listEmployee/{empno}` | GET | Get employee by number | No |
| `/addEmployee/{firstName}/{lastName}` | GET | Add new employee | No |
| `/updateEmployee/{empNo}/{newSalary}` | GET | Update employee salary | No |
| `/deleteEmployee/{empNo}` | GET | Delete employee | No |
| `/addEmployeeTx/{firstName}/{lastName}` | GET | Add employee (XA transaction) | Yes |
| `/updateEmployeeTx/{empNo}/{newSalary}` | GET | Update salary (XA transaction) | Yes |
| `/deleteEmployeeTx/{empNo}` | GET | Delete employee (XA transaction) | Yes |

**Note:** Update operations should use POST/PUT/DELETE methods. This sample uses GET for simplicity.

---

## Troubleshooting

### Issue: Application fails to start

**Symptom:**
```
CWWKZ0002E: An exception occurred while starting the application cics-java-liberty-springboot-jdbc
```

**Possible Causes & Solutions:**

1. **Missing Liberty features:**
   - Verify `servlet-6.0` (or `servlet-4.0`), `pages-3.1`, and `jdbc-4.3` are in server.xml
   - Check Liberty messages.log for feature-related errors

2. **Java version mismatch:**
   - Spring Boot 3.x requires Java 17 or later
   - Update JAVA_HOME if necessary

3. **Datasource not found:**
   - Verify JNDI name in application.properties matches server.xml
   - Check that datasource is defined in server.xml

---

### Issue: Database connection fails

**Symptom:**
```
SQLException: Connection refused
```
or
```
DB2 SQL Error: SQLCODE=-204, SQLSTATE=42704
```

**Solutions:**

1. **For Type 2 connectivity:**
   - Verify DB2CONN is installed in CICS: `CEMT INQ DB2CONN`
   - Check DB2CONN=YES in CICS SIT
   - Verify Db2 load libraries in CICS region JCL
   - Ensure CICS region has RACF authority to access Db2

2. **For Type 4 connectivity:**
   - Verify network connectivity to Db2 server
   - Verify user credentials

3. **Schema/table not found (SQLCODE=-204):**
   - Verify `currentSchema` in server.xml matches your Db2 schema
   - Check that EMP table exists: `SELECT * FROM YOUR_SCHEMA.EMP`
   - Verify user has SELECT/INSERT/UPDATE/DELETE privileges

---

### Issue: Authentication fails

**Symptom:**
```
HTTP 401 Unauthorized
```

**Solutions:**

1. **CICS security not configured:**
   - Verify SEC=YES in CICS SIT
   - Check that `cicsts:security-1.0` feature is in server.xml
   - Ensure user is defined in RACF/security manager

2. **Liberty security configuration:**
   - Verify `<application-bnd>` section in server.xml
   - Check that user has appropriate CICS transaction authority
   - Review Liberty security logs

---

### Issue: Transactional operations fail

**Symptom:**
```
Transaction rolled back
```
or
```
XA transaction error
```

**Solutions:**

1. **Datasource not configured for XA:**
   - Change datasource type to `javax.sql.XADataSource` in server.xml
   - For Type 2: Use `transactional="true"`

---

### Logging and Diagnostics

**Enable detailed logging in server.xml:**
```xml
<logging traceSpecification="*=info:com.ibm.cicsdev.springboot.*=all:org.springframework.*=debug"/>
```

**Check logs:**
```bash
# Liberty messages.log
tail -f /path/to/liberty/logs/messages.log

# CICS MSGUSR
# Check for CICS-related messages
```
---

## License

This project is licensed under the [Eclipse Public License - v 2.0](LICENSE).

### Usage Terms

By downloading, installing, and/or using this sample, you acknowledge that separate license terms may apply to any dependencies that might be required as part of the installation and/or execution and/or automated build of the sample, including the following IBM license terms for relevant IBM components:

• IBM CICS development components terms: https://www.ibm.com/support/customer/csol/terms/?id=L-ACRR-BBZLGX

---

## Additional Resources

- [CICS TS Documentation](https://www.ibm.com/docs/en/cics-ts)
- [WebSphere Liberty Documentation](https://www.ibm.com/docs/en/was-liberty)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JDBC Documentation](https://spring.io/projects/spring-data-jdbc)
- [IBM Db2 for z/OS Documentation](https://www.ibm.com/docs/en/db2-for-zos)
- [IBM Tutorial: Spring Boot Java applications for CICS, Part 4: JDBC](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-4-jdbc/)

---

## Contributing

This is a sample project maintained by IBM CICS development. For issues or questions:
- Open an issue on GitHub
- Contact IBM Support for CICS-related questions

---
