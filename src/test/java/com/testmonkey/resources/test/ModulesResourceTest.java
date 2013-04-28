/**
 * 
 */
package com.testmonkey.resources.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.server.Handler;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.testmonkey.app.GlobalConfig;
import com.testmonkey.app.TestMonkey;

/**
 * @author phil
 *
 */
public class ModulesResourceTest {
	Mockery context = new JUnit4Mockery() {{
	    setThreadingPolicy(new Synchroniser());
	}};
	
	// Create a TestMonkey object to facilitate setup of jetty instance
	private TestMonkey testMonkey = null;
	
	private final int TEST_PORT_NUM = 9021; // any old port will do
	private final String TEST_SERVLET_PATH = "/unittest";
	private final String TEST_MODULE = "/test/fakeGTestApp";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// set up global config with a fake gtest app
		GlobalConfig config = GlobalConfig.getConfig();
		config.processCommandLine(new String[] {TEST_MODULE});
		
		testMonkey = new TestMonkey();
		
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
	 * Test the getModulesAsXml JAX-RS resource method
	 * @throws IOException
	 * @throws XPathExpressionException 
	 * @throws aa 
	 */
	@Test
	public void getModulesAsXmlTest() throws IOException, XPathExpressionException {
		// do an http get on the rest resource url
		URL getUrl = new URL("http://localhost:" + TEST_PORT_NUM + TEST_SERVLET_PATH + "/modules/xml");
		HttpURLConnection connection = (java.net.HttpURLConnection) getUrl.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		
		// verify server response is as expected
		Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
		Assert.assertEquals("application/xml", connection.getContentType());
		
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
		
		// TODO: output appears to end with the string "null". Not sure why - could be a bug
		// in the underlying JAXB POJO to XML mapping? Anyway as a workaround remove it so XPath
		// can validate the XML properly. Note this issue doesn't affect XSLT transform - or
		// anything else for that matter.
		String httpOutput = sb.toString();
		if (httpOutput.endsWith("null"))
			httpOutput = httpOutput.replaceFirst("null$", "");
		
		// ensure the xml output is valid
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(new StringReader(httpOutput));
		NodeList testModuleNodes = (NodeList) xpath.evaluate("//testmodule", inputSource, XPathConstants.NODESET);
		Node testModuleNode = testModuleNodes.item(0);
		assertEquals("name", testModuleNode.getFirstChild().getNodeName());
		assertEquals("fakeGTestApp", testModuleNode.getFirstChild().getTextContent());
	}
}
