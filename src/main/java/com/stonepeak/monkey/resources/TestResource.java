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

@Path("/tests")
public class TestResource {
	
	private TestManager testManager = new TestManager(GlobalConfig.getGtestAppPath());
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TestSuite> handleTests() {
    	return testManager.getTests();
    }
    
    /**
     * Gets the result of test(s) depending on gtest filter specified
     * @param filter
     * @return
     */
    @GET @Path("/results/{gtestfilter}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TestCaseResult> handleTestResults(@PathParam("gtestfilter") String filter) {
    	// run the test
    	String testRunId = testManager.runTests(filter);
    	
    	// convert the results to POJOs
    	List<TestCaseResult> results = testManager.getTestResults(testRunId);
    	return results;
    }
}
