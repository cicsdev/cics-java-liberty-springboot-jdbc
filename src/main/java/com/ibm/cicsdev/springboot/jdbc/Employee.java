package com.ibm.cicsdev.springboot.jdbc;

import java.sql.Date;

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

	public String getEmpNo() {
		return empNo;
	}

	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}

	public String getFirstName() {
		return firstNme;
	}

	public void setFirstName(String firstName) {
		this.firstNme = firstName;
	}

	public String getMidinit() {
		return midinit;
	}

	public void setMidinit(String midinit) {
		this.midinit = midinit;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getWorkdept() {
		return workdept;
	}

	public void setWorkdept(String workdept) {
		this.workdept = workdept;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public int getEdLevel() {
		return edLevel;
	}

	public void setEdLevel(int edLevel) {
		this.edLevel = edLevel;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public long getSalary() {
		return salary;
	}

	public void setSalary(long salary) {
		this.salary = salary;
	}

	public long getBonus() {
		return bonus;
	}

	public void setBonus(long bonus) {
		this.bonus = bonus;
	}

	public long getComm() {
		return comm;
	}

	public void setComm(long comm) {
		this.comm = comm;
	}

	
}
