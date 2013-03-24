package com.stonepeak.monkey.data;

import java.util.ArrayList;
import java.util.List;

public class TestSuite {
	
	private String suiteName;
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
}
