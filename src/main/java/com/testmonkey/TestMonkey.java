package com.testmonkey;

import java.util.logging.Level;
import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.testmonkey.util.HushLogger;

public class TestMonkey {
	
	private static final String WEB_SERVICE_ROOT_PATH = "/rest";
	private static final String JAR_WEB_CONTENT_DIRECTORY = "html";
	private static final String JAR_WEB_CONTENT_STARTPAGE = "/index.html";
	private static final int DEFAULT_PORT_NUM = 8080;
	private static final String PORT_COMMAND_ARG = "Port";
	
	/**
	 * Main method
	 * @param args
	 */
    public static void main(String[] args)
    {
    	TestMonkey app = new TestMonkey();
    	app.runApplication(args);
    }
    
    /**
     * method that main method delegates too. Allows us to add
     * non-blocking web server behaviour if we want to in future
     * @param args
     */
    public void runApplication(String[] args)
    {
    	// process command line args and set global app config
    	GlobalConfig config = GlobalConfig.getConfig();
    	try {
    		config.processCommandLine(args);
    	} catch (Exception e) {
    		System.out.println("\nError: " + e.getMessage());
    		System.out.println("TestMonkey command options:\ntestharness1name ... testharnessNname [Port=portnum]\nOR:\nschedule=testschedule.xml [VarName1=value1] ... [VarNameN=valueN] [Port=portnum]\n\n");
    		System.exit(-1);
    	}
    	
    	suppressFrameworkLogging();
    		
    	// Get port number - if not specified on command line use default
    	int portNum = DEFAULT_PORT_NUM;
    	if (config.varExists(PORT_COMMAND_ARG))
    	{
    		try {
    			portNum = config.getIntVar(PORT_COMMAND_ARG);
    		} catch (Exception e) {} // if can't get port arg, use default
    	}
    	
    	// Create the embedded web server
        Server server = new Server(portNum);
        server.setHandler(getServerHandlers());
        
        System.out.println("Starting app...\n");
        
        try {
            server.start();
            System.out.println("Started OK! Point your browser to http://localhost:" + portNum + ". Press Ctrl-c to exit.\n\n");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up the handlers for the server
     * @return
     */
    private HandlerList getServerHandlers()
    {
    	// set up servlet handler for rest calls
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        
        context.setContextPath(WEB_SERVICE_ROOT_PATH);
        
        ServletHolder h = new ServletHolder(new ServletContainer());
        h.setInitParameter("com.sun.jersey.config.property.packages", "com.testmonkey.resources");
		h.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        context.addServlet(h, "/*");
        
        // set up a resource handler to serve up static content from jar html dir
        String webDir = context.getClass().getClassLoader().getResource(JAR_WEB_CONTENT_DIRECTORY).toExternalForm();
        
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{ JAR_WEB_CONTENT_STARTPAGE });
        resource_handler.setResourceBase(webDir);
        
        // create a rewrite handler to redirect / or /anything.html to the rest tests xml get path.
        // the xslt style sheet will then be used to generate the html page
        RewriteHandler rewrite = new RewriteHandler();
        rewrite.setRewriteRequestURI(true);
        rewrite.setRewritePathInfo(false);
        rewrite.setOriginalPathAttribute("requestedPath");
       
        RedirectRegexRule redirect = new RedirectRegexRule();
        redirect.setRegex("/(?:.*html)?");
        if (GlobalConfig.getConfig().getGtestAppCount() > 1)
        {
        	// set the root redirect to go to the modules page
        	redirect.setReplacement("/rest/modules/xml");
        } else {
        	// set the root redirect to go to the only module's tests - no
        	// point in the user just selecting a single module
        	redirect.setReplacement("/rest/tests/xml/0");
        }
        rewrite.addRule(redirect);
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { rewrite, resource_handler, context });
        
        return handlers;
    }
    
    /**
     * Prevent jersey and jetty logging
     */
    private void suppressFrameworkLogging()
    {
    	// Turn off jetty logging to stderr
    	System.setProperty( "org.eclipse.jetty.util.log.class", "com.testmonkey.HushLogger");
    	org.eclipse.jetty.util.log.Log.setLog(new HushLogger());
    	// Turn off jersey logging
    	java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger("com.sun.jersey");
    	jerseyLogger.setLevel(Level.OFF);
    }
}
