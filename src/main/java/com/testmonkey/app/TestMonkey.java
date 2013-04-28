package com.testmonkey.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.xml.sax.InputSource;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.testmonkey.model.TestModule;
import com.testmonkey.util.HashMethodFactory;
import com.testmonkey.util.HushLogger;
import com.testmonkey.util.IHashMethod;
import com.testmonkey.util.IRunCommandMethod;
import com.testmonkey.util.RunCommandMethodFactory;
import com.testmonkey.util.RuntimeExecWrapper;
import com.testmonkey.util.InputSourceFactory;
import com.testmonkey.util.Sha1Hash;

public class TestMonkey {
	private Server server = null;
	
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
    	// register any object factories we require
    	performRegistration();
    	
    	// process command line args and set global app config
    	GlobalConfig config = GlobalConfig.getConfig();
    	try {
    		config.processCommandLine(args);
    		
    		// validate test modules specified (either on command line or in schedule file)
        	List<TestModule> testModules = config.getGtestAppsList();
    		
    		// check all gtest files exist (better to do this now than have user discover it
        	// when web interface is used)
    		for (TestModule testModule : testModules)
    		{
    			File f = new File(testModule.getModuleFilePath());
    			if (!f.exists())
    			{
    				throw new FileNotFoundException("Gtest app \"" + testModule.getModuleFilePath() + "\" does not exist.");
    			}
    		}
    	} catch (Exception e) {
    		System.out.println("\nError: " + e.getMessage());
    		System.out.println("TestMonkey command options:\ntestharness1name ... testharnessNname [Port=portnum]\nOR:\nschedule=testschedule.xml [VarName1=value1] ... [VarNameN=valueN] [Port=portnum]\n\n");
    		System.exit(-1);
    	}
    	
    	// Get port number - if not specified on command line use default
    	int portNum = DEFAULT_PORT_NUM;
    	if (config.varExists(PORT_COMMAND_ARG))
    	{
    		try {
    			portNum = config.getIntVar(PORT_COMMAND_ARG);
    		} catch (Exception e) {} // if can't get port arg, use default
    	}
    	
    	// create a new instance of the app
    	TestMonkey app = new TestMonkey();
    	
    	// turn off jetty and jersey default sdtout logging
    	app.suppressFrameworkLogging();
    	
        // use helper functions to easily create handler objects...
    	// create rest webapp handler
    	Handler restHandler = app.createRestHandler(WEB_SERVICE_ROOT_PATH, "com.testmonkey.resources");
    	
    	// get location of static content from jar html dir
        String webDir = app.getClass().getClassLoader().getResource(JAR_WEB_CONTENT_DIRECTORY).toExternalForm();
    	// create handler to serve out static content such as images and style sheets
        Handler staticContentHandler = app.createStaticContentHandler(webDir, JAR_WEB_CONTENT_STARTPAGE);
    	
        // create handler to redirect requests to rest url, where rest xml output will then be transformed to
        // html output by xmlstyle.xsl xslt stylesheet
    	int nTestHarnessCount = GlobalConfig.getConfig().getGtestAppCount();
    	Handler redirectHandler = app.createRedirectHandler("/(?:.*html)?", 
    			nTestHarnessCount > 1 ? "/rest/modules/xml" : "/rest/tests/xml/0");  // if only 1 app under test, go direct to tests page
    	
    	// spark up the web server
    	System.out.println("Starting app...\n");
    	try {
			app.runServer(portNum, redirectHandler, restHandler, staticContentHandler);
			System.out.println("Started OK! Point your browser to http://localhost:" + portNum + ". Press Ctrl-c to exit.\n\n");
		} catch (Exception e) {
			System.out.println("Error starting the embedded web server: " + e.getMessage());
		}
    	
    	// and wait for user to press ctrl-c
    	app.waitForTermination();
    }
    
    /**
     * Wrapper function for server.start(). Note order of handlers is important and determines which
     * handler has a go at processing the url first
     * @param args
     * @throws Exception 
     */
    public void runServer(int portNum, Handler... handlers) throws Exception
    {
    	// put all these handlers into a HandlerList
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(handlers);
        
    	// Create the embedded web server
        server = new Server(portNum);
        server.setHandler(handlerList);
        
        server.start();
    }
    
    /**
     * Wrapper function for server.join()
     * @throws InterruptedException 
     */
    public void waitForTermination()
    {
    	if (server == null)
    		throw new IllegalStateException("runServer must be called first.");
    	try {
			server.join();
		} catch (InterruptedException e) {
			// we don't care if the wait gets interrupted
		}
    }
    
    /**
     * Stops the embedded web server
     */
    public void shutdownServer()
    {
    	try {
			server.stop();
		} catch (Exception e) {
			// we don't really care about exceptions raised here
		}
    }
    
    /**
     * All the boilerplate to create a jersey rest servlet handler
     * @param webServiceRootPath the root webapp url
     * @param resourcePackages comma separated list of packages jersey should look in
     * 							for rest resource classes
     * @return Handler configured jersey web app handler
     */
    public Handler createRestHandler(String webServiceRootPath, String resourcePackages) {
		// set up servlet handler for rest calls
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(webServiceRootPath);
        ServletHolder h = new ServletHolder(new ServletContainer());
        h.setInitParameter("com.sun.jersey.config.property.packages", resourcePackages);
		h.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        context.addServlet(h, "/*");
        return context;
	}
    
    /**
     * Boilerplate required to create a resource handler for static content
     * @param webDir directory from which to server
     * @param startPage page to direct to from "/" requests
     * @return
     */
    public Handler createStaticContentHandler(String webDir, String startPage) {
		
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{ startPage });
        resource_handler.setResourceBase(webDir);
        return resource_handler;
	}
    
    /**
     * Boilerplate required to create a redirection handler. Wraps RewriteHandler
     * and RedirectRegexRule
     * @param fromPattern regex pattern to detect
     * @param redirectTo url matches should be redirected to
     * @return
     */
    public Handler createRedirectHandler(String fromPattern, String redirectTo) {
		// create a rewrite handler to redirect "/" or "/anything.html" to the rest tests xml get path.
        // the xslt style sheet will then be used to generate the html page
        RewriteHandler rewrite = new RewriteHandler();
        rewrite.setRewriteRequestURI(true);
        rewrite.setRewritePathInfo(false);
        rewrite.setOriginalPathAttribute("requestedPath");
        RedirectRegexRule redirect = new RedirectRegexRule();
        redirect.setRegex(fromPattern);
        redirect.setReplacement(redirectTo);
        rewrite.addRule(redirect);
        return rewrite;
	}
    
    /**
     * Prevents default jersey and jetty logging to stdout
     */
    public void suppressFrameworkLogging()
    {
    	// Turn off jetty logging to stderr
    	System.setProperty( "org.eclipse.jetty.util.log.class", "com.testmonkey.HushLogger");
    	org.eclipse.jetty.util.log.Log.setLog(new HushLogger());
    	// Turn off jersey logging
    	java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger("com.sun.jersey");
    	jerseyLogger.setLevel(Level.OFF);
    }
    
    /**
     * Perform object factory registration
     */
    private static void performRegistration()
    {
    	GTestRunnerFactory.registerGTestRunnerProvider(new GTestRunnerFactory() {
			@Override
			protected IGTestRunner createGTestRunner() {
				return new TestManager();
			}
    	});
    	RunCommandMethodFactory.registerRunCommandMethodProvider(new RunCommandMethodFactory() {
			@Override
			protected IRunCommandMethod createRunCommandMethod() {
				return new RuntimeExecWrapper();
			}
    	});
    	InputSourceFactory.registerInputSourceProvider(new InputSourceFactory() {
			@Override
			protected InputSource createInputSource(String sourceIdentifier) {
				return new InputSource(sourceIdentifier);
			}
    	});
    	HashMethodFactory.registerHashMethodProvider(new HashMethodFactory() {
			@Override
			protected IHashMethod createHashMethod() {
				return new Sha1Hash();
			}
    	});
    }
}
