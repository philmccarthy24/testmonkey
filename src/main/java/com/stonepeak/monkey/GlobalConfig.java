package com.stonepeak.monkey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;

import com.stonepeak.monkey.data.TestModule;
import com.stonepeak.monkey.exceptions.VarNotFoundException;
import com.stonepeak.monkey.util.PathsHelper;

public class GlobalConfig {
	
	// the singleton instance
	private static GlobalConfig config = new GlobalConfig();
	
	private List<TestModule> gtestAppsList = new ArrayList<TestModule>();	// only used if test schedule not specified
	private TestSchedule testSchedule = null;
	private Map<String, String> commandVars = new HashMap<String, String>();
	// var key to specify test schedule file on command line (case insensitive)
	private static String SCHEDULE_FILE = "Schedule";
	
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
		TestModule testModule = null;
		if (testSchedule != null)
		{
			// using test schedule
			try {
				testModule = testSchedule.getTestModuleListFromSchedule().get(nId);
			} catch (Exception e) {
				System.out.println("Error getting gtest app info from test schedule - please check it still exists.");
			}
		} else {
			testModule = gtestAppsList.get(nId);
		}
		return testModule;
	}
	
	/**
	 * Get number of gtest apps served
	 * @return number of gtest apps
	 */
	public int getGtestAppCount()
	{	
		int nModuleCount = 0;
		if (testSchedule != null)
		{
			// using test schedule
			try {
				nModuleCount = testSchedule.getTestModuleListFromSchedule().size();
			} catch (Exception e) {
				System.out.println("Error getting gtest app info from test schedule - please check it still exists.");
			}
		} else {
			nModuleCount = gtestAppsList.size();
		}
		return nModuleCount;
	}
	
	/**
	 * Gets the list of TestModules containing info about the gtest apps under test.
	 * This info is either generated from the command line test module paths or specified
	 * in a test schedule file
	 * @return
	 */
	public List<TestModule> getGtestAppsList()
	{		
		List<TestModule> testModules = null;
		if (testSchedule != null)
		{
			// using test schedule
			try {
				testModules = testSchedule.getTestModuleListFromSchedule();
			} catch (Exception e) {
				System.out.println("Error getting gtest app info from test schedule - please check it still exists.");
			}
		} else {
			testModules = gtestAppsList;
		}
		return testModules;
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
				TestModule testModule = new TestModule();
				testModule.setModuleFilePath(gtestAppPath);
				testModule.setModuleDefaultName(gtestAppPath);
				testModule.setModuleDefaultDescription(testModule.getModuleName());
				gtestAppsList.add(testModule);
			}
		}
		
		if (varExists(SCHEDULE_FILE))
		{
			// schedule file specified
			testSchedule = new TestSchedule(getVar(SCHEDULE_FILE));
		}
		
		// check conflicting conditions
		if (gtestAppsList.size() > 0 && testSchedule != null)
			throw new IllegalArgumentException("Command arguments invalid.");
		
		List<TestModule> testModules = gtestAppsList;
		if (testSchedule != null)
		{
			// this triggers first read/processing of schedule xml
			testModules = testSchedule.getTestModuleListFromSchedule();
		}
		
		// check that we have an app to serve
		if (testModules.isEmpty())
			throw new IllegalArgumentException("No gtest app specified.");
		
		// check all gtest files exist
		for (TestModule testModule : testModules)
		{
			File f = new File(testModule.getModuleFilePath());
			if (!f.exists())
			{
				throw new FileNotFoundException("Gtest app \"" + testModule.getModuleFilePath() + "\" does not exist.");
			}
		}
	}
}
