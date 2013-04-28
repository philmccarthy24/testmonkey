/**
 * 
 */
package com.testmonkey.resources.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Handler;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.testmonkey.app.GTestRunnerFactory;
import com.testmonkey.app.GlobalConfig;
import com.testmonkey.app.IGTestRunner;
import com.testmonkey.app.TestMonkey;
import com.testmonkey.model.TestCaseResult;
import com.testmonkey.model.TestModule;
import com.testmonkey.model.TestSuite;

/**
 * @author phil
 *
 */
public class TestResourceTest {
	Mockery context = new JUnit4Mockery() {{
	    setThreadingPolicy(new Synchroniser());
	}};
	
	// Create a TestMonkey object to facilitate setup of jetty instance
	private TestMonkey testMonkey = null;
	
	private final int TEST_PORT_NUM = 9021; // any old port will do
	private final String TEST_SERVLET_PATH = "/unittest";
	private final String TEST_MODULE = "/test/fakeGTestApp";
	
	private final IGTestRunner gtestRunnerMock = context.mock(IGTestRunner.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// set up global config with a fake gtest app
		GlobalConfig config = GlobalConfig.getConfig();
		config.processCommandLine(new String[] {TEST_MODULE});
		
		testMonkey = new TestMonkey();
		
		// register mock object factory
		GTestRunnerFactory.registerGTestRunnerProvider(new GTestRunnerFactory() {
			@Override
			protected IGTestRunner createGTestRunner() {
				return gtestRunnerMock;
			}
		});
		
		// prevent default jetty/jersey logging
		testMonkey.suppressFrameworkLogging();
		
		// create a rest handler which will use our mock objects
		Handler restHandler = testMonkey.createRestHandler(TEST_SERVLET_PATH, "com.testmonkey.resources");
		
		// create instance of web server to serve up our test web app
		testMonkey.runServer(TEST_PORT_NUM, restHandler);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		testMonkey.shutdownServer();
		testMonkey = null;
	}

	/**
	 * Test the handleTests JAX-RS resource method
	 * Don't bother testing handleTestsAsXML method as it's identical apart from output type
	 * @throws IOException
	 */
	@Test
	public void handleTestsTest() throws IOException {
		// create some test objects for the mock to return
		TestSuite testSuite = new TestSuite();
		testSuite.setSuiteName("TestSuite");
		testSuite.setTestModule(TEST_MODULE);
		List<String> testCasesInSuite = new ArrayList<String>();
		testCasesInSuite.add("TestCaseOne");
		testCasesInSuite.add("TestCaseTwo");
		testSuite.setTestModule(TEST_MODULE);
		testSuite.setTestCases(testCasesInSuite);
		final List<TestSuite> testSuites = new ArrayList<TestSuite>();
		testSuites.add(testSuite);
		
		// expectations
		context.checking(new Expectations() {{
			oneOf (gtestRunnerMock).getTests(with(any(TestModule.class))); will(returnValue(testSuites));
		}});
		
		// do an http get on the rest resource url
		URL getUrl = new URL("http://localhost:" + TEST_PORT_NUM + TEST_SERVLET_PATH + "/tests/json/0");
		HttpURLConnection connection = (java.net.HttpURLConnection) getUrl.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");
		
		// verify server response is as expected
		Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
		Assert.assertEquals("application/json", connection.getContentType());
		
		// read web server output
		BufferedReader reader = new BufferedReader(new
		          InputStreamReader(connection.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		sb.append(line);
		while (line != null)
		{
			line = reader.readLine();
			sb.append(line);
		}
		connection.disconnect();
		
		// verify
        context.assertIsSatisfied(); // were the mocks called with the right params in the right order?
		
		// ensure the json output is valid
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readValue(sb.toString(), JsonNode.class);
		assertEquals("TestSuite", rootNode.get(0).get("testsuite").asText());
	}

	/**
	 * Test the TestResource.handleTestResults rest resource handler
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Test
	public void handleTestResultsTest() throws IOException, InterruptedException {
		final List<TestCaseResult> testCaseResults = new ArrayList<TestCaseResult>();
		TestCaseResult testCaseResult = new TestCaseResult();
		testCaseResult.setSuiteName("TestSuite");
		testCaseResult.setCaseName("TestCaseOne");
		testCaseResult.setPassed(true);
		testCaseResults.add(testCaseResult);
		
		// expectations
		context.checking(new Expectations() {{
	        oneOf (gtestRunnerMock).runTests(with(any(TestModule.class)), with(any(String.class))); will(returnValue("dummyId"));
	        oneOf (gtestRunnerMock).getTestResults("dummyId"); will(returnValue(testCaseResults));
		}});
		
		// do an http get on the rest resource url
		URL getUrl = new URL("http://localhost:" + TEST_PORT_NUM + TEST_SERVLET_PATH + "/tests/results/0/*");
		HttpURLConnection connection = (java.net.HttpURLConnection) getUrl.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");
		
		// verify server response is as expected
		Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
		Assert.assertEquals("application/json", connection.getContentType());
		
		// read web server output
		BufferedReader reader = new BufferedReader(new
		          InputStreamReader(connection.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		sb.append(line);
		while (line != null)
		{
			line = reader.readLine();
			sb.append(line);
		}
		connection.disconnect();
		
		// verify
        context.assertIsSatisfied(); // were the mocks called with the right params in the right order?
        
        // ensure the json output is valid
 		ObjectMapper mapper = new ObjectMapper();
 		JsonNode rootNode = mapper.readValue(sb.toString(), JsonNode.class);
 		assertEquals("TestSuite", rootNode.get(0).get("suiteName").asText());
	}
}
