package com.testmonkey.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.provider.jaxb.XmlHeader;
import com.testmonkey.app.GTestRunnerFactory;
import com.testmonkey.app.GlobalConfig;
import com.testmonkey.app.IGTestRunner;
import com.testmonkey.model.TestCaseResult;
import com.testmonkey.model.TestModule;
import com.testmonkey.model.TestSuite;

@Path("/tests")
public class TestResource {
	
	private IGTestRunner testRunner = GTestRunnerFactory.buildGTestRunner();
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET @Path("json/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TestSuite> handleTests(@PathParam("appId") int nAppId) {
    	List<TestSuite> testSuites = null;
    	try {
    		GlobalConfig config = GlobalConfig.getConfig();
    		TestModule gtestApp = config.getGtestApp(nAppId);
    		testSuites = testRunner.getTests(gtestApp);
		} catch (IndexOutOfBoundsException iobe) {
			System.out.println("App id specified does not exist.");
		}
    	return testSuites;
    }
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET @Path("xml/{appId}")
    @Produces(MediaType.APPLICATION_XML)
    @XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"/xmlstyle.xsl\"?>")
    public List<TestSuite> handleTestsAsXml(@PathParam("appId") int nAppId) {
    	List<TestSuite> testSuites = null;
    	try {
    		GlobalConfig config = GlobalConfig.getConfig();
    		TestModule gtestApp = config.getGtestApp(nAppId);
    		testSuites = testRunner.getTests(gtestApp);
    	} catch (IndexOutOfBoundsException iobe) {
    		System.out.println("App id specified does not exist.");
    	}
    	return testSuites;
    }
    
    /**
     * Gets the result of test(s) depending on gtest filter specified
     * @param filter
     * @return
     */
    @GET @Path("/results/{appId}/{gtestfilter}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TestCaseResult> handleTestResults(@PathParam("appId") int nAppId, @PathParam("gtestfilter") String filter) {
    	List<TestCaseResult> results = null;
    	
    	try {
        	// run the test
    		TestModule gtestApp = GlobalConfig.getConfig().getGtestApp(nAppId);
    		String testRunId = testRunner.runTests(gtestApp, filter);
    		
    		// convert the results to POJOs
        	results = testRunner.getTestResults(testRunId);
    	} catch (IndexOutOfBoundsException iobe) {
    		System.out.println("App id specified does not exist.");
    	} catch (Exception e) {
    		System.out.println("An error occured while attempting to run tests from the google test executable.");
    	}
    	
    	return results;
    }
}
