package com.stonepeak.monkey.util;

import java.nio.file.Path;
import java.nio.file.Paths;

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
}
