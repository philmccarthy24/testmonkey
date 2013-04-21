package com.stonepeak.monkey;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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

public class TestSchedule {
	
	private String testScheduleHash = null;
	private String scheduleFileName;
	// cached list of test modules read from schedule file
	private List<TestModule> testModules = new ArrayList<TestModule>();
	
	/**
	 * Constructor
	 * @param scheduleFileName
	 */
	public TestSchedule(String scheduleFileName)
	{
		this.scheduleFileName = scheduleFileName;
	}
	
	/**
	 * Gets list of test module info from test schedule file
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws VarNotFoundException
	 */
	public List<TestModule> getTestModuleListFromSchedule() throws NoSuchAlgorithmException, XPathExpressionException, IOException, VarNotFoundException
	{
		if (hasChanged())
			process();	// file changed - recache test module info
		return testModules;
	}
	
	/**
	 * Determine whether test schedule has been updated
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private boolean hasChanged() throws NoSuchAlgorithmException, IOException
	{
		boolean changed = true;
		if (testScheduleHash != null)
    	{
	    	// get current hash of schedule file
	    	String currentScheduleHash = Hash.sha1(scheduleFileName);
			if (testScheduleHash.equals(currentScheduleHash))
			{
				// hashes are same - return false
				changed = false;
			}
    	} // else file not read yet - return true
		return changed;
	}
	
	/**
	 * Process the test schedule file
	 * @throws XPathExpressionException 
	 * @throws VarNotFoundException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
    private void process() throws XPathExpressionException, VarNotFoundException, NoSuchAlgorithmException, IOException
    {
    	testModules.clear();
    	
    	XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(scheduleFileName);
		
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
			// expand any variables in path if possible
			gtestAppPath = PathsHelper.expandVars(gtestAppPath);
			
			// create a new TestModule object to hold gtest app info
			TestModule testModule = new TestModule();
			
			// add gtest app path
			testModule.setModuleFilePath(gtestAppPath);
			
			// get module friendly name from attribute if it exists
			Node nameAttrib = attribs.getNamedItem("name");
			if (nameAttrib != null)
			{
				// expand vars in name if possible
				testModule.setModuleName(PathsHelper.expandVars(nameAttrib.getNodeValue()));
			} else {
				testModule.setModuleDefaultName(gtestAppPath);
			}
			
			// get description attribute if it exists
			Node descriptionAttrib = attribs.getNamedItem("description");
			if (descriptionAttrib != null)
			{
				// expand vars in description if possible
				testModule.setModuleDescription(PathsHelper.expandVars(descriptionAttrib.getNodeValue()));
			} else {
				testModule.setModuleDefaultDescription(testModule.getModuleName());
			}
			
			testModules.add(testModule);
		}
		
		// update schedule file hash so we use cached TestModule objects if xml unchanged,
		// rather than read them all in again
		testScheduleHash = Hash.sha1(scheduleFileName);
    }

}
