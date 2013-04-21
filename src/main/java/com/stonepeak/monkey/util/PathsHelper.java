package com.stonepeak.monkey.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.stonepeak.monkey.GlobalConfig;
import com.stonepeak.monkey.exceptions.VarNotFoundException;

public class PathsHelper {

    /**
     * Gets the name of the module, minus the windows file extension if on Windows
     * @param modulePath
     * @return the test module name
     */
    public static String getFileNameNoExtensionFromPath(String modulePath)
    {
    	Path path = Paths.get(modulePath);
		String fileNameNoPath = path.getFileName().toString();
		String fileNameNoExtension = fileNameNoPath;
		int extIdx = 0;
		if ((extIdx = fileNameNoPath.lastIndexOf(".")) != -1)
		{
			fileNameNoExtension = fileNameNoPath.substring(0, extIdx + 1);
		}
		return fileNameNoExtension;
    }
    
    public static String addRunLocalPathToFilenameWithoutPath(String pathName)
    {
		String pathSeparator = System.getProperty("file.separator");
		String runLocalNotation = "." + pathSeparator;
		if (!pathName.contains(pathSeparator))
		{
			// pathName is just a filename - prefix run local ./
			//( TODO test this also works on windows)
			pathName = runLocalNotation + pathName;
		}
		return pathName;
    }
    
    /**
     * Joins two parts of a path (path, filename) together. If the path
     * is missing a trailing path separator, one is added
     * @param firstPart
     * @param secondPart
     * @return
     */
    public static String joinPaths(String firstPart, String secondPart)
    {
    	String pathSeparator = System.getProperty("file.separator");
    	String joined = firstPart;
		if (!joined.endsWith(pathSeparator))
		{
			joined += pathSeparator;
		}
		joined += secondPart;
		return joined;
    }
    
    /**
     * If variables are present in the passed-in string, attempt to substitute
     * them for command vars specified on the command line
     * @throws VarNotFoundException 
     */
    public static String expandVars(String nonExpanded) throws VarNotFoundException
    {
    	String expandedString = nonExpanded;
    	Pattern varPattern = Pattern.compile("\\$\\((.+?)\\)");
    	GlobalConfig config = GlobalConfig.getConfig();
		Matcher m = varPattern.matcher(nonExpanded);
		Map<String, String> possibleReplacementMap = new HashMap<String, String>();
		while (m.find()) // for each $(...) variable in the string
		{
			String varMatched = m.group(1);
			if (config.varExists(varMatched))
			{
				//variable can be replaced
				possibleReplacementMap.put("\\$\\(" + varMatched + "\\)", config.getVar(varMatched));
			}
		}
		// now carry out the specific replacements
		for (String key : possibleReplacementMap.keySet())
		{
			expandedString = expandedString.replaceAll(key, possibleReplacementMap.get(key));
		}
		return expandedString;
    }
}
