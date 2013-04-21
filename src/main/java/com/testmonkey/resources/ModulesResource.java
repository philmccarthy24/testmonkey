package com.testmonkey.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.provider.jaxb.XmlHeader;
import com.testmonkey.GlobalConfig;
import com.testmonkey.data.TestModule;

@Path("/modules")
public class ModulesResource {
    
	/**
	 * REST handler for list of modules under test
	 * @return
	 */
    @GET @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    @XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"/xmlstyle.xsl\"?>")
    public List<TestModule> getModulesAsXml() {
    	GlobalConfig config = GlobalConfig.getConfig();
		return config.getGtestAppsList();
    }
}
