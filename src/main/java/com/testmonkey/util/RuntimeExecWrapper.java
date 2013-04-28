package com.testmonkey.util;

import java.io.IOException;
import java.io.InputStream;


public class RuntimeExecWrapper implements IRunCommandMethod {
	
	private Process wrappedProcess = null;

	@Override
	public void runCommand(String command) throws IOException {
		wrappedProcess = Runtime.getRuntime().exec(command);
	}

	@Override
	public InputStream getCmdOutput() {
		if (wrappedProcess == null)
		{
			throw new IllegalStateException("Command must be run before output can be retrieved.");
		}
		return wrappedProcess.getInputStream();
	}

	@Override
	public InputStream getCmdError() {
		if (wrappedProcess == null)
		{
			throw new IllegalStateException("Command must be run before error stream can be retrieved.");
		}
		return wrappedProcess.getErrorStream();
	}

	@Override
	public void waitForCompletion() throws InterruptedException {
		if (wrappedProcess == null)
		{
			throw new IllegalStateException("Command must be run before it can be waited on for completion.");
		}
		wrappedProcess.waitFor();
	}

}
