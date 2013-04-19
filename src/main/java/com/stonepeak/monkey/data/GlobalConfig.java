package com.stonepeak.monkey.data;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.stonepeak.monkey.exceptions.VarNotFoundException;
import com.stonepeak.monkey.util.Hash;

public class GlobalConfig {

	// Some config keys used internally. note case irrelevant
	private static final String SCHEDULE_FILE = "Schedule";
	private static final String SCHEDULE_HASH = "ScheduleSHA1";
	
	private static GlobalConfig config = new GlobalConfig();
	
	private List<String> gtestAppPaths = new ArrayList<String>();
	private Map<String, String> commandVars = new HashMap<String, String>();
	
	/**
	 * Singleton accessor
	 * @return the singleton instance
	 */
	public static GlobalConfig getConfig()
	{
		return config;
	}
	
	/**
	 * private constructor for singleton
	 */
	private GlobalConfig()
	{
	}
	
	/**
	 * Test if the relevant command variable is set
	 * @param keyName
	 * @return true if set, false otherwise
	 */
	public boolean varExists(String keyName)
	{
		// upper case specified to make keys (but not values) case insensitive
		return commandVars.containsKey(keyName.toUpperCase());
	}
	
	/**
	 * Get global command variable
	 * @param keyName
	 * @return the associated value
	 * @throws VarNotFoundException 
	 */
	public String getVar(String keyName) throws VarNotFoundException
	{
		// upper case specified to make keys (but not values) case insensitive
		String strValue = commandVars.get(keyName.toUpperCase());
		if (strValue == null)
			throw new VarNotFoundException();
		return strValue;
	}
	
	/**
	 * Gets global command variable as an int
	 * @param keyName
	 * @return value as int
	 * @throws VarNotFoundException
	 * @throws NumberFormatException
	 */
	public int getIntVar(String keyName) throws VarNotFoundException, NumberFormatException
	{
		String strValue = getVar(keyName);
		return Integer.parseInt(strValue);
	}
	
	/**
	 * Gets the "nId"th gtest app path specified
	 * @param nId
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public String getGtestAppPath(int nId) throws IndexOutOfBoundsException
	{
		// if test schedule file specified, xml may have been updated by user on-the-fly.
		if (varExists(SCHEDULE_FILE))
		{
			try {
				// get hash of previously read file
				String scheduleHash = getVar(SCHEDULE_HASH);
				// get current hash of file
				String currentScheduleHash = Hash.sha1(getVar(SCHEDULE_FILE));
				if (!scheduleHash.equals(currentScheduleHash))
				{
					// schedule file has been updated - parse contents again to get gtest app paths
					gtestAppPaths.clear();
					processTestSchedule();
					// store the new hash
					commandVars.put(SCHEDULE_HASH, currentScheduleHash);
				}
			} catch (Exception e) {
				System.out.println("Error processing test schedule file - please check it still exists.");
			}
		}
		return gtestAppPaths.get(nId);
	}
	
	/**
	 * Get number of gtest apps served
	 * @return number of gtest apps
	 */
	public int getGtestAppCount()
	{
		return gtestAppPaths.size();
	}
	
	/**
	 * Process the test schedule file
	 * @throws XPathExpressionException 
	 * @throws VarNotFoundException 
	 */
    private void processTestSchedule() throws XPathExpressionException, VarNotFoundException
    {
    	XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(getVar(SCHEDULE_FILE));
		
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
		substitutePathVars();
    }
    
    /**
     * If variables are present in the gtest app paths, attempt to substitute
     * them for command vars specified on the command line
     * @throws VarNotFoundException 
     */
    private void substitutePathVars() throws VarNotFoundException
    {
    	Pattern varPattern = Pattern.compile("\\$\\((.+)\\)");
    	for (int i = 0; i < gtestAppPaths.size(); i++)
    	{
    		String gtestAppPath = gtestAppPaths.get(i);
    		Matcher m = varPattern.matcher(gtestAppPath);
    		while (m.find())
    		{
    			String varMatched = m.group(1);
    			if (varExists(varMatched))
    			{
    				//variable can be replaced
    				String replacement = getVar(varMatched);
    				gtestAppPaths.set(i, m.replaceAll(replacement));
    			}
    		}
    	}
    }
    
    /**
     * Process the command line args
     * @param args
     * @throws FileNotFoundException, IllegalArgumentException
     * @throws XPathExpressionException 
     * @throws VarNotFoundException 
     */
    public void processCommandLine(String[] args) throws FileNotFoundException, IllegalArgumentException, XPathExpressionException, VarNotFoundException
	{
		// setup regex for extracting command line vars
		Pattern varPattern = Pattern.compile("(.*?)=(.*)");
	    
		// iterate over command line args
		for (String arg : args)
		{
			// try to match command line var pattern
			Matcher m = varPattern.matcher(arg);
			if (m.matches())
			{
				// arg is a command variable. store key as uppercase for case insensitivity
				commandVars.put(m.group(1).toUpperCase(), m.group(2));
			} else {
				// otherwise variable is taken to be a gtest executable.
				// add ./ (or similar) if no path specified
				String gtestAppPath = "";
				String pathSeparator = System.getProperty("file.separator");
				if (!arg.contains(pathSeparator))
				{
					gtestAppPath = "." + pathSeparator;
				}
				gtestAppPath += arg;
				gtestAppPaths.add(gtestAppPath);
			}
		}
		
		// check conflicting conditions
		if (gtestAppPaths.size() > 0 && varExists(SCHEDULE_FILE))
			throw new IllegalArgumentException("Command argumants invalid.");
		
		if (varExists(SCHEDULE_FILE))
		{
			// do an initial parse of the test schedule so we can test validity of 
			// gtest exe paths straight away
			processTestSchedule();
		}
		
		// check that we have an app to serve
		if (gtestAppPaths.isEmpty())
			throw new IllegalArgumentException("No gtest app specified.");
		
		// check all gtest files exist
		for (String gtestApp : gtestAppPaths)
		{
			File f = new File(gtestApp);
			if (!f.exists())
			{
				throw new FileNotFoundException("Gtest app \"" + gtestApp + "\" does not exist.");
			}
		}
	}
}
