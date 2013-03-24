package com.stonepeak.monkey.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

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
     * Gets the result of a specific test case
     * @return
     */
    @GET @Path("/{testcase}")
    @Produces(MediaType.APPLICATION_JSON)
    public TestCaseResult handleTestCase(@PathParam("testcase") String testCase) {
    	// run the specific test
    	String testRunId = testManager.runTestCase(testCase);
    	
    	// convert the results to POJOs
    	//TestCaseResult result = testManager.get
    	return null;
    }
}
