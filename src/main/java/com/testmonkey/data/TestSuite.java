package com.testmonkey.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="test")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuite {
	
	public TestSuite() {}
	
	@XmlElement(name="testmodule")
	private String testModule;
	
	@XmlElement(name="testsuite")
	private String suiteName;
	
	@XmlElementWrapper(name="testcases")
	@XmlElement(name="testcase")
	private List<String> testCases = new ArrayList<String>();
	
	/**
	 * @return the suiteName
	 */
	public String getSuiteName() {
		return suiteName;
	}
	/**
	 * @param suiteName the suiteName to set
	 */
	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}
	/**
	 * @return the testCases
	 */
	public List<String> getTestCases() {
		return testCases;
	}
	/**
	 * @param testCases the testCases to set
	 */
	public void setTestCases(List<String> testCases) {
		this.testCases = testCases;
	}
	
	public void addTestCase(String testCase)
	{
		testCases.add(testCase);
	}
	/**
	 * @return the testModule
	 */
	public String getTestModule() {
		return testModule;
	}
	/**
	 * @param testModule the testModule to set
	 */
	public void setTestModule(String testModule) {
		this.testModule = testModule;
	}
	
	
}
