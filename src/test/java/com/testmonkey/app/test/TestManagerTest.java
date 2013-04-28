/**
 * 
 */
package com.testmonkey.app.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.testmonkey.app.TestManager;
import com.testmonkey.model.TestCaseResult;
import com.testmonkey.model.TestModule;
import com.testmonkey.model.TestSuite;
import com.testmonkey.util.IRunCommandMethod;
import com.testmonkey.util.InputSourceFactory;
import com.testmonkey.util.RunCommandMethodFactory;

import static org.hamcrest.Matchers.*;

/**
 * @author phil
 *
 */
public class TestManagerTest {
	
	Mockery context = new Mockery();
	
	// define some mock objects
	final IRunCommandMethod mockCommandRunner = context.mock(IRunCommandMethod.class);
	
	{
		// register mock object provider with factory
		RunCommandMethodFactory.registerRunCommandMethodProvider(new RunCommandMethodFactory() {
			@Override
			protected IRunCommandMethod createRunCommandMethod() {
				return mockCommandRunner;
			}
    	});
	}

	/**
	 * Test method for {@link com.testmonkey.app.TestManager#getTests(com.testmonkey.model.TestModule)}.
	 * @throws IOException 
	 */
	@Test
	public void testGetTests() throws IOException {
		// specify the command string we expect to be used
		final String expectedCommandString = "/test/file1 --gtest_list_tests";
		
		// specify the command runner mock output from getCmdOutput
		final String outputString = 
				"Running main() from doesntexist.cpp\n" +
				"TestSuiteOne.\n" +
				"  SomeTestCase\n" +
				"  AnotherTestCase\n" +
				"TestSuiteTwo.\n" +
				"  OneFinalCase\n";
		
		// create a TestModule test class
		TestModule testModule = new TestModule();
		testModule.setModuleFilePath("/test/file1");
		
		// create the TestManager object to be exercised
		TestManager testManager = new TestManager();
		
		//expectations
		context.checking(new Expectations() {{
			oneOf (mockCommandRunner).runCommand(expectedCommandString); 
		    oneOf (mockCommandRunner).getCmdOutput(); will(returnValue(new ByteArrayInputStream(outputString.getBytes())));
		}});
		
		// execute
		List<TestSuite> testSuites = testManager.getTests(testModule);
		
		// verify
        context.assertIsSatisfied(); // were the mocks called with the right params in the right order?
        
        // check data returned from testManager.getTests()
        assertEquals(2, testSuites.size());
        assertEquals(2, testSuites.get(0).getTestCases().size());
        assertEquals(1, testSuites.get(1).getTestCases().size());
        assertEquals("TestSuiteOne", testSuites.get(0).getSuiteName());
        assertEquals("SomeTestCase", testSuites.get(0).getTestCases().get(0));
        assertEquals("AnotherTestCase", testSuites.get(0).getTestCases().get(1));
        assertEquals("TestSuiteTwo", testSuites.get(1).getSuiteName());
        assertEquals("OneFinalCase", testSuites.get(1).getTestCases().get(0));
	}

	/**
	 * Test method for {@link com.testmonkey.app.TestManager#runTests(com.testmonkey.model.TestModule, java.lang.String)}.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Test
	public void testRunTests() throws IOException, InterruptedException {
		final String outputString = "dummy output";
		
		// create a TestModule test class
		TestModule testModule = new TestModule();
		testModule.setModuleFilePath("/test/file1");
		
		// create the TestManager object to be exercised
		TestManager testManager = new TestManager();
		
		//expectations
		context.checking(new Expectations() {{
	        oneOf (mockCommandRunner).runCommand(with(containsString("/test/file1 --gtest_filter=* --gtest_output=xml:")));
	        oneOf (mockCommandRunner).getCmdOutput(); will(returnValue(new ByteArrayInputStream(outputString.getBytes())));
	        oneOf (mockCommandRunner).getCmdError(); will(returnValue(new ByteArrayInputStream(outputString.getBytes())));
	        oneOf (mockCommandRunner).waitForCompletion();
		}});
			
		// execute
		String guid = testManager.runTests(testModule, "*");
		
		// verify
        context.assertIsSatisfied(); // were the mocks called with the right params in the right order?
        
        // check guid is valid (ie doesn't throw exception)
        @SuppressWarnings("unused")
		UUID valid = UUID.fromString(guid);
	}

	/**
	 * Test method for {@link com.testmonkey.app.TestManager#getTestResults(java.lang.String)}.
	 */
	@Test
	public void testGetTestResults() {
		// define an InputSourceFactory that returns a stubbed InputSource (can't use a mock in this instance
		// as InputSource isn't an interface, but a stub will do)
		InputSourceFactory.registerInputSourceProvider(new InputSourceFactory() {
			// define some gtest format output xml that has a test failure
			final String testXmlOutput = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<testsuites tests=\"3\" failures=\"1\" disabled=\"0\" errors=\"0\" time=\"0\" name=\"AllTests\">" +
				"   <testsuite name=\"TestSuiteOne\" tests=\"2\" failures=\"1\" disabled=\"0\" errors=\"0\" time=\"0\">" +
				"      <testcase name=\"SomeTestCase\" status=\"run\" time=\"0.384\" classname=\"TestSuiteOne\" />" +
				"      <testcase name=\"AnotherTestCase\" status=\"run\" time=\"0\" classname=\"TestSuiteOne\">" +
				"         <failure message=\"Value of: true&#x0A;  Actual: true&#x0A;Expected: false\" type=\"\">" +
				"            <![CDATA[/path/test/somefile.cpp:123" +
				"             Value of: true" +
				"             Actual: true" +
				"             Expected: false]]>" +
				"         </failure>" +
				"      </testcase>" +
				"   </testsuite>" +
				"   <testsuite name=\"TestSuiteTwo\" tests=\"1\" failures=\"0\" disabled=\"0\" errors=\"0\" time=\"0\">" +
				"      <testcase name=\"OneFinalCase\" status=\"run\" time=\"0.384\" classname=\"TestSuiteTwo\" />" +
				"   </testsuite>" +
				"</testsuites>";
			
			@Override
			protected InputSource createInputSource(String sourceIdentifier) {
				return new InputSource(new StringReader(testXmlOutput));
			}
    	});
		
		// create a TestModule test class
		TestModule testModule = new TestModule();
		testModule.setModuleFilePath("/test/file1");
		
		// create the TestManager object to be exercised
		TestManager testManager = new TestManager();
			
		// execute
		List<TestCaseResult> testResults = testManager.getTestResults("dummyId");
		
		// verify
        context.assertIsSatisfied(); // were the mocks called with the right params in the right order?
        
        // check data returned from testManager.getTestResults()
        assertEquals(3, testResults.size());
        assertEquals("TestSuiteOne", testResults.get(0).getSuiteName());
        assertEquals("TestSuiteOne", testResults.get(1).getSuiteName());
        assertEquals("TestSuiteTwo", testResults.get(2).getSuiteName());
        assertEquals("SomeTestCase", testResults.get(0).getCaseName());
        assertEquals("AnotherTestCase", testResults.get(1).getCaseName());
        assertEquals("OneFinalCase", testResults.get(2).getCaseName());
        assertTrue(testResults.get(0).isPassed());
        assertFalse(testResults.get(1).isPassed());
        assertTrue(testResults.get(2).isPassed());
	}
}
