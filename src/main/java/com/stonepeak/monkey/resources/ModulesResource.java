package com.stonepeak.monkey.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.stonepeak.monkey.GlobalConfig;
import com.stonepeak.monkey.data.TestModule;
import com.sun.jersey.api.provider.jaxb.XmlHeader;

@Path("/modules")
public class ModulesResource {
    
	/**
	 * Main tests handler - gets list of tests from executable
	 * @return
	 */
    @GET @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    @XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"/modules.xsl\"?>")
    public List<TestModule> getModulesAsXml() {
    	GlobalConfig config = GlobalConfig.getConfig();
		return config.getGtestAppsList();
    }
}
