package com.stonepeak.monkey.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.stonepeak.monkey.data.GlobalConfig;
import com.stonepeak.monkey.data.TestCaseResult;
import com.stonepeak.monkey.data.TestManager;
import com.stonepeak.monkey.data.TestSuite;
import com.sun.jersey.api.provider.jaxb.XmlHeader;

@Path("/")
public class TestResource {
	
	private TestManager testManager = new TestManager();
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET @Path("/tests/{appId}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TestSuite> handleTests(@PathParam("appId") int nAppId) {
    	List<TestSuite> testSuites = null;
    	try {
    		String gtestApp = GlobalConfig.getConfig().getGtestAppPath(nAppId);
    		testSuites = testManager.getTests(gtestApp);
		} catch (IndexOutOfBoundsException iobe) {
			System.out.println("App id specified does not exist.");
		}
    	return testSuites;
    }
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET @Path("/tests/{appId}/xml")
    @Produces(MediaType.APPLICATION_XML)
    @XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"/tests.xsl\"?>")
    public List<TestSuite> handleTestsAsXml(@PathParam("appId") int nAppId) {
    	List<TestSuite> testSuites = null;
    	try {
    		String gtestApp = GlobalConfig.getConfig().getGtestAppPath(nAppId);
    		testSuites = testManager.getTests(gtestApp);
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
    		String gtestApp = GlobalConfig.getConfig().getGtestAppPath(nAppId);
    		String testRunId = testManager.runTests(gtestApp, filter);
    		
    		// convert the results to POJOs
        	results = testManager.getTestResults(testRunId);
    	} catch (IndexOutOfBoundsException iobe) {
    		System.out.println("App id specified does not exist.");
    	} catch (Exception e) {
    		System.out.println("An error occured while attempting to run tests from the google test executable.");
    	}
    	
    	return results;
    }
}
