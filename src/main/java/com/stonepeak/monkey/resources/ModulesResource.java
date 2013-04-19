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
public class ModulesResource {
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET @Path("/modules/xml")
    @Produces(MediaType.APPLICATION_XML)
    @XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"/modules.xsl\"?>")
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
}
