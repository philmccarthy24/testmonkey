package com.stonepeak.monkey.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GlobalConfig {
	
	private static GlobalConfig instance = new GlobalConfig();
	
	private List<String> gtestAppPaths = new ArrayList<String>();
	private Map<String, String> commandVars = new HashMap<String, String>();
	private String scheduleFile = null;
	
	/**
	 * private constructor for singleton
	 */
	private GlobalConfig()
	{
	}
	
	/**
	 * Singleton accessor
	 * @return
	 */
	public static GlobalConfig getConfig()
	{
		return instance;
	}

	/**
	 * @return the scheduleFile
	 */
	public String getScheduleFile() {
		return scheduleFile;
	}

	/**
	 * @param scheduleFile the scheduleFile to set
	 */
	public void setScheduleFile(String scheduleFile) {
		this.scheduleFile = scheduleFile;
	}
	
	/**
	 * get whether schedule file has been specified
	 * @return boolean - true if schedule file specified
	 */
	public boolean hasScheduleFile() {
		return scheduleFile != null;
	}

	/**
	 * @return the gtestAppNames
	 */
	public List<String> getGtestAppPaths() {
		return gtestAppPaths;
	}

	/**
	 * @return the commandVars
	 */
	public Map<String, String> getCommandVars() {
		return commandVars;
	}
	
	/**
	 * Process the test schedule file. Called each main page refresh to allow
	 * web app to be dynamic and pick up changes to xml config file
	 * @throws XPathExpressionException 
	 */
    public void processTestSchedule() throws XPathExpressionException
    {
    	if (scheduleFile == null)
    	{
    		// test schedule not specified
    		return;
    	}
    	XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(scheduleFile);
		
		// get runallfrom attrib from root node
		Node unitTestScheduleNode = (Node) xpath.evaluate("UnitTestSchedule", inputSource, XPathConstants.NODE);
		NamedNodeMap testScheduleAttribs = unitTestScheduleNode.getAttributes();
		Node runAllAttrib = testScheduleAttribs.getNamedItem("runallfrom");
		
		// iterate over enabled UnitTestModule nodes
		NodeList unitTestModuleNodes = (NodeList) xpath.evaluate("//UnitTestModule[@enabled='true']", inputSource, XPathConstants.NODESET);
		for (int i = 0; i < unitTestModuleNodes.getLength(); i++)
		{
			String gtestAppPath = "";
			Node testModuleNode = unitTestModuleNodes.item(i);
			if (runAllAttrib == null)
			{
				// run all from path not specified on the top level,
				// so get path from the "runfrom" attrib if possible
				NamedNodeMap attribs = testModuleNode.getAttributes();
				Node runFromAttrib = attribs.getNamedItem("runfrom");
				if (runFromAttrib != null)
				{
					gtestAppPath += runFromAttrib.getNodeValue();
				}
			} else {
				// runallfrom path specified so use this instead of paths
				// specified on each node
				gtestAppPath += runAllAttrib.getNodeValue();
			}
			// append app name to path (if specified)
			String pathSeparator = System.getProperty("file.separator");
			if (gtestAppPath.isEmpty())
			{
				// if gtestAppPath has just a name and not any path
				// then prepend ./ to it ( TODO test this also works on windows)
				gtestAppPath = "." + pathSeparator;
			} else {
				if (!gtestAppPath.endsWith(pathSeparator))
					gtestAppPath += pathSeparator;
				gtestAppPath += testModuleNode.getTextContent();
			}
			
			// add full app path to app names list
			gtestAppPaths.add(gtestAppPath);
		}
		
		// substitute command variables on paths if any are specified
		processCommandVars();
    }
    
    /**
     * If variables are present in the gtest app paths, attempt to substitute
     * them for command vars specified on the command line
     */
    private void processCommandVars()
    {
    	Pattern varPattern = Pattern.compile("\\$\\((.+)\\)");
    	for (int i = 0; i < gtestAppPaths.size(); i++)
    	{
    		String gtestAppPath = gtestAppPaths.get(i);
    		Matcher m = varPattern.matcher(gtestAppPath);
    		while (m.find())
    		{
    			String varMatched = m.group(1);
    			if (commandVars.containsKey(varMatched))
    			{
    				//variable can be replaced
    				String replacement = commandVars.get(varMatched);
    				gtestAppPaths.set(i, m.replaceAll(replacement));
    			}
    		}
    	}
    }
}
