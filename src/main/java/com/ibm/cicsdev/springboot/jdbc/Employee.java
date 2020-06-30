/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Copyright IBM Corp. 2020 All Rights Reserved   
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.cicsdev.springboot.jdbc;

import java.sql.Date;

/**
 * @author Tony Fitzgerald
 *
 * class representing the EMP table
 */
public class Employee {
	
	/*
	 * Db2 supplied EMP table 
	 * 
	 * CREATE TABLE DSN81110.EMP                                           
   (EMPNO                CHAR(6) FOR SBCS DATA NOT NULL,            
    FIRSTNME             VARCHAR(12) FOR SBCS DATA NOT NULL,        
    MIDINIT              CHAR(1) FOR SBCS DATA NOT NULL,            
    LASTNAME             VARCHAR(15) FOR SBCS DATA NOT NULL,        
    WORKDEPT             CHAR(3) FOR SBCS DATA WITH DEFAULT NULL,   
    PHONENO              CHAR(4) FOR SBCS DATA WITH DEFAULT NULL,   
    HIREDATE             DATE WITH DEFAULT NULL,                    
    JOB                  CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,   
    EDLEVEL              SMALLINT WITH DEFAULT NULL,                
    SEX                  CHAR(1) FOR SBCS DATA WITH DEFAULT NULL,   
    BIRTHDATE            DATE WITH DEFAULT NULL,                    
    SALARY               DECIMAL(9, 2) WITH DEFAULT NULL,           
    BONUS                DECIMAL(9, 2) WITH DEFAULT NULL,           
    COMM                 DECIMAL(9, 2) WITH DEFAULT NULL,           
    CONSTRAINT EMPNO                                                
    PRIMARY KEY (EMPNO),                                            
    CONSTRAINT NUMBER                                               
      CHECK (PHONENO >= '0000' AND PHONENO <= '9999'),              
    CONSTRAINT PERSON CHECK (SEX = 'M' OR SEX = 'F'))               
  IN DSN8D11A.DSN8S11E                                              
  PARTITION BY (EMPNO ASC)                                          
   (PARTITION 1 ENDING AT ('099999'),                               
    PARTITION 2 ENDING AT ('199999'),                               
    PARTITION 3 ENDING AT ('299999'),                               
    PARTITION 4 ENDING AT ('499999'),                               
    PARTITION 5 ENDING AT ('999999'))                               
  EDITPROC  DSN8EAE1 WITH ROW ATTRIBUTES                            
  AUDIT NONE                                                        
  DATA CAPTURE NONE                                                 
  CCSID      EBCDIC                                                 
  NOT VOLATILE                                                      
  APPEND NO  ;                                                      
	 */
	
	
	
	private String empNo;
	private String firstNme;
	private String midinit;
	private String lastName;
	private String workdept;
	private String phoneNo;
	private Date hireDate;
	private String job;
	private int edLevel;
	private String sex;
	private String birthDate;
	private long salary;
	private long bonus;
	private long comm;

	
	/**
	 * @param empNo		- employee Number 			- 6 characters
	 * @param firstNme 	- employee first name 		- 12 characters
	 * @param midinit	- employee middle initial	- 1 character
	 * @param lastName	- employee last name		- 15 characters
	 * @param workdept	- employee work department 	- 3 characters
	 * @param phoneNo	- employee phone number 	- 4 characters
	 * @param hireDate	- employee hire date
	 * @param job		- job title					- 8 characters
	 * @param edLevel	- employee education level	- integer (1,2, or 3)
	 * @param sex		- employee gender 			- 1 character
	 * @param birthDate	- employee birth date
	 * @param salary	- employee salary amount	- decimal (9,2)
	 * @param bonus		- employee bonus amount		- decimal (9,2)
	 * @param comm		- employee commission amount-decimal (9,2)
	 */
	public Employee(String empNo, String firstNme, String midinit, String lastName, String workdept, String phoneNo,
			Date hireDate, String job, int edLevel, String sex, String birthDate, long salary, long bonus, long comm) {
		super();
		this.empNo = empNo;
		this.firstNme = firstNme;
		this.midinit = midinit;
		this.lastName = lastName;
		this.workdept = workdept;
		this.phoneNo = phoneNo;
		this.hireDate = hireDate;
		this.job = job;
		this.edLevel = edLevel;
		this.sex = sex;
		this.birthDate = birthDate;
		this.salary = salary;
		this.bonus = bonus;
		this.comm = comm;
	}

	@Override
	public String toString() {
		return "Employee [empNo=" + empNo + 
				", firstName=" + firstNme + 
				", midinit=" + midinit + 
				", lastName=" + lastName + 
				", workdept=" + workdept + 
				", phoneNo=" + phoneNo + 
				", hireDate=" + hireDate + 
				", job=" + job + 
				", edLevel=" + edLevel + 
				", sex=" + sex + 
				", birthDate=" + birthDate + 
				", salary=" + salary + 
				", bonus=" + bonus + 
				", comm=" + comm + "]";
	}

	/**
	 * @return employee number
	 */
	public String getEmpNo() {
		return empNo;
	}

	/**
	 * @param empNo - set employee number
	 */
	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}

	/**
	 * @return employee first name
	 */
	public String getFirstName() {
		return firstNme;
	}

	/**
	 * @param firstName - set employee first name  
	 */
	public void setFirstName(String firstName) {
		this.firstNme = firstName;
	}

	/**
	 * @return employee middle initial
	 */
	public String getMidinit() {
		return midinit;
	}

	/**
	 * @param midinit - set employee middle initial
	 */
	public void setMidinit(String midinit) {
		this.midinit = midinit;
	}

	/**
	 * @return employee last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName - set employee last name
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return employee work department
	 */
	public String getWorkdept() {
		return workdept;
	}

	/**
	 * @param workdept - set employee work department
	 */
	public void setWorkdept(String workdept) {
		this.workdept = workdept;
	}

	/**
	 * @return employee phone number
	 */
	public String getPhoneNo() {
		return phoneNo;
	}

	/**
	 * @param phoneNo - set employee phone number
	 */
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	/**
	 * @return employee hire date
	 */
	public Date getHireDate() {
		return hireDate;
	}

	/**
	 * @param hireDate - set employee hire date
	 */
	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	/**
	 * @return employee job title
	 */
	public String getJob() {
		return job;
	}

	/**
	 * @param job - set employee job title
	 */
	public void setJob(String job) {
		this.job = job;
	}

	/**
	 * @return employee education level 1, 2 or 3
	 */
	public int getEdLevel() {
		return edLevel;
	}

	/**
	 * @param edLevel set employee education level 1, 2 or 3
	 */
	public void setEdLevel(int edLevel) {
		this.edLevel = edLevel;
	}

	/**
	 * @return employee gender (1 character)
	 */
	public String getSex() {
		return sex;
	}

	/**
	 * @param sex - set employee gender (1 character)
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}

	/**
	 * @return employee birthdate
	 */
	public String getBirthDate() {
		return birthDate;
	}

	/**
	 * @param birthDate - set employee birthdate
	 */
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	/**
	 * @return employee salary
	 */
	public long getSalary() {
		return salary;
	}

	/**
	 * @param salary - set employee salary 
	 */
	public void setSalary(long salary) {
		this.salary = salary;
	}

	/**
	 * @return employee bonus
	 */
	public long getBonus() {
		return bonus;
	}

	/**
	 * @param bonus - set employee bonus 
	 */
	public void setBonus(long bonus) {
		this.bonus = bonus;
	}

	/**
	 * @return employee commission
	 */
	public long getComm() {
		return comm;
	}

	/**
	 * @param comm - set employee commission
	 */
	public void setComm(long comm) {
		this.comm = comm;
	}

	
}
