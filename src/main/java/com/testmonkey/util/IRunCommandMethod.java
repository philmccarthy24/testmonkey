package com.testmonkey.util;

import java.io.IOException;
import java.io.InputStream;

public interface IRunCommandMethod {
	
	public void runCommand(String command) throws IOException;
	
	public InputStream getCmdOutput();
	
	public InputStream getCmdError();
	
	public void waitForCompletion() throws InterruptedException;
}
