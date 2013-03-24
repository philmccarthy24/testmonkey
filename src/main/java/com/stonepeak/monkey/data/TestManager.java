package com.stonepeak.monkey.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestManager {
	
	private String gtestAppPath;
	
	private static final String GET_TEST_ARG = "--gtest_list_tests";
	private static final String RUN_TEST_ARG = "--gtest_filter";
	private static final String OUTPUT_XML_ARG = "--gtest_output=xml";
	
	/**
	 * Constructor
	 * @param gtestAppPath The path of the native application binary under test
	 */
	public TestManager(String gtestAppPath)
	{
		this.gtestAppPath = gtestAppPath;
	}
	
	/**
	 * Gets list of tests from gtest binary under test, and returns a list
	 * of objects representing the available test suites and test cases
	 * @return
	 */
	public List<TestSuite> getTests()
	{
		List<TestSuite> tests = new ArrayList<TestSuite>();
		
		String gtestGetTestsCmd = gtestAppPath + " " + GET_TEST_ARG;
		
		try {
			String line = null;
			Process gtestApp = Runtime.getRuntime().exec(gtestGetTestsCmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(gtestApp.getInputStream()));
			TestSuite currentSuite = null;
			while ((line = input.readLine()) != null) {
				
				// the available tests output can be one of 3 cases
				if (line.startsWith("Running main"))
				{
					// this is the first line returned by the gtest app
					continue;
				} else if (line.matches("\\S+\\.")) {
					// this is a new test suite name
					if (currentSuite != null)
					{
						// we need to add previous suite to list
						tests.add(currentSuite);
					}
					currentSuite = new TestSuite();
					// trim the last dot from the test suite name
					line = line.substring(0,  line.length() - 1);
					currentSuite.setSuiteName(line);
				} else if (line.matches("\\s{2}\\S+")) {
					// this is a test case name
					if (currentSuite == null)
					{
						throw new IllegalArgumentException("Unexpected test name detected before test suite name");
					}
					currentSuite.addTestCase(line.trim());
				} else // invalid output from gtest get tests command
					throw new IllegalArgumentException("gtest_list_tests gave unexpected output line: " + line);
				//System.out.println(line);
			}
			input.close();
			// add last test suite to list
			if (currentSuite != null)
			{
				tests.add(currentSuite);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (tests.isEmpty())
			tests = null; // prefer returning null than an empty list
		
		return tests;
	}
	
	/**
	 * Runs a specific test case from the app under test
	 * @param testCaseName the name of the test case eg TestSuite.TestCase
	 * @return GUID of test run, used to uniquely identify a set of results
	 */
	public String runTestCase(String testCaseName)
	{
		String guid = UUID.randomUUID().toString();
		StringBuilder testCommandBuilder = new StringBuilder();
		testCommandBuilder.append(gtestAppPath).append(" ")
			.append(RUN_TEST_ARG).append("=").append(testCaseName).append(" ")
			.append(OUTPUT_XML_ARG).append(":").append(guid).append(".xml");
		System.out.println("Running:\n" + testCommandBuilder.toString());
		try {
			Process gtestApp = Runtime.getRuntime().exec(testCommandBuilder.toString());
			// wait for the gtest app to terminate
			gtestApp.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return guid;
	}

}
