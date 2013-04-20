package com.stonepeak.monkey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
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

import com.stonepeak.monkey.data.TestModule;
import com.stonepeak.monkey.exceptions.VarNotFoundException;
import com.stonepeak.monkey.util.Hash;
import com.stonepeak.monkey.util.PathsHelper;

public class GlobalConfig {

	// Some config keys used internally. note case irrelevant
	private static final String SCHEDULE_FILE = "Schedule";
	private static final String SCHEDULE_HASH = "ScheduleSHA1";
	
	private static GlobalConfig config = new GlobalConfig();
	
	private List<TestModule> gtestAppsList = new ArrayList<TestModule>();
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
	public TestModule getGtestApp(int nId) throws IndexOutOfBoundsException
	{
		updateGtestAppsListIfScheduleFileChanged();
		return gtestAppsList.get(nId);
	}
	
	/**
	 * Get number of gtest apps served
	 * @return number of gtest apps
	 */
	public int getGtestAppCount()
	{
		updateGtestAppsListIfScheduleFileChanged();
		return gtestAppsList.size();
	}
	
	/**
	 * Gets the list of TestModules containing info about the gtest apps under test.
	 * This info is either generated from the command line test module paths or specified
	 * in a test schedule file
	 * @return
	 */
	public List<TestModule> getGtestAppsList()
	{
		updateGtestAppsListIfScheduleFileChanged();
		return gtestAppsList;
	}
	
	/**
	 * Update the gtest apps list if the test schedule file has changed.
	 * If not using a test schedule file, does nothing
	 */
	private void updateGtestAppsListIfScheduleFileChanged()
	{
		if (varExists(SCHEDULE_FILE))
		{
			try {
				// if test schedule file specified, xml may have been updated by user on-the-fly.
				// process the schedule again if necessary
				processTestSchedule();
			} catch (Exception e) {
				System.out.println("Error processing test schedule file - please check it still exists.");
			}
		}
	}
	
	/**
	 * Process the test schedule file
	 * @throws XPathExpressionException 
	 * @throws VarNotFoundException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
    private void processTestSchedule() throws XPathExpressionException, VarNotFoundException, NoSuchAlgorithmException, IOException
    {
    	String scheduleFile = getVar(SCHEDULE_FILE);
    	// first determine if we need to process the test schedule.
    	// get current hash of schedule file
    	String currentScheduleHash = Hash.sha1(scheduleFile);
    	if (varExists(SCHEDULE_HASH))
    	{
    		// hash exists so we've processed the xml before. compare stored hash with current file hash	
			String lastScheduleHash = getVar(SCHEDULE_HASH);  // get hash of previously read file
			if (lastScheduleHash.equals(currentScheduleHash))
			{
				// hashes are the same - nothing has changed.
				return;
			} else {
				// schedule file has been updated - need to process again
				gtestAppsList.clear();	// clear out paths list
				// store the new hash
				commandVars.put(SCHEDULE_HASH.toUpperCase(), currentScheduleHash);
			}
    	} else {
    		// store the hash for the first time
			commandVars.put(SCHEDULE_HASH.toUpperCase(), currentScheduleHash);
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
			NamedNodeMap attribs = testModuleNode.getAttributes();
			if (runAllAttrib == null)
			{
				// run all from path not specified on the top level,
				// so get path from the "runfrom" attrib if possible
				Node runFromAttrib = attribs.getNamedItem("runfrom");
				if (runFromAttrib != null)
				{
					gtestAppPath = runFromAttrib.getNodeValue();
				}
			} else {
				// runallfrom path specified so use this instead of paths
				// specified on each node
				gtestAppPath = runAllAttrib.getNodeValue();
			}
			
			// do a bit of fiddling with gtest app path
			gtestAppPath = PathsHelper.joinPaths(gtestAppPath, testModuleNode.getTextContent());
			gtestAppPath = PathsHelper.addRunLocalPathToFilenameWithoutPath(gtestAppPath);	//TODO check this works on windows
			
			// create a new TestModule object to hold gtest app info
			TestModule testModule = new TestModule();
			
			// add gtest app path and module name to module info			
			testModule.setModuleFilePath(gtestAppPath);
			String moduleName = PathsHelper.getFileNameNoExtensionFromPath(gtestAppPath);
			testModule.setModuleName(moduleName);
			
			// get description attribute if it exists
			Node descriptionAttrib = attribs.getNamedItem("description");
			if (descriptionAttrib != null)
			{
				testModule.setModuleDescription(descriptionAttrib.getNodeValue());
			} else {
				// need to generate a description
				testModule.setModuleDescription("Tests in the " + moduleName + " google test harness");
			}
			
			gtestAppsList.add(testModule);
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
    	for (int i = 0; i < gtestAppsList.size(); i++)
    	{
    		TestModule testModule = gtestAppsList.get(i);
    		String gtestAppPath = testModule.getModuleFilePath();
    		Matcher m = varPattern.matcher(gtestAppPath);
    		while (m.find())
    		{
    			String varMatched = m.group(1);
    			if (varExists(varMatched))
    			{
    				//variable can be replaced
    				String replacement = getVar(varMatched);
    				testModule.setModuleFilePath(m.replaceAll(replacement));
    			}
    		}
    	}
    }
    
    /**
     * Process the command line args
     * @param args
     * @throws XPathExpressionException 
     * @throws VarNotFoundException 
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    public void processCommandLine(String[] args) throws IllegalArgumentException, XPathExpressionException, VarNotFoundException, NoSuchAlgorithmException, IOException
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
				String gtestAppPath = PathsHelper.addRunLocalPathToFilenameWithoutPath(arg);
				String gtestModuleName = PathsHelper.getFileNameNoExtensionFromPath(gtestAppPath);
				TestModule testModule = new TestModule();
				testModule.setModuleFilePath(gtestAppPath);
				testModule.setModuleName(gtestModuleName);
				testModule.setModuleDescription("Tests in the " + gtestModuleName + " google test harness");
				gtestAppsList.add(testModule);
			}
		}
		
		// check conflicting conditions
		if (gtestAppsList.size() > 0 && varExists(SCHEDULE_FILE))
			throw new IllegalArgumentException("Command argumants invalid.");
		
		if (varExists(SCHEDULE_FILE))
		{
			// do an initial parse of the test schedule so we can test validity of 
			// gtest exe paths straight away
			processTestSchedule();
		}
		
		// check that we have an app to serve
		if (gtestAppsList.isEmpty())
			throw new IllegalArgumentException("No gtest app specified.");
		
		// check all gtest files exist
		for (TestModule testModule : gtestAppsList)
		{
			File f = new File(testModule.getModuleFilePath());
			if (!f.exists())
			{
				throw new FileNotFoundException("Gtest app \"" + testModule.getModuleName() + "\" does not exist.");
			}
		}
	}
}
