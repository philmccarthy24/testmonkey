package com.testmonkey.app;

import java.io.IOException;
import java.util.List;

import com.testmonkey.model.TestCaseResult;
import com.testmonkey.model.TestModule;
import com.testmonkey.model.TestSuite;

public interface IGTestRunner {
	public List<TestSuite> getTests(TestModule gtestAppInfo);
	
	public String runTests(TestModule gtestAppInfo, String gtestFilter) throws IOException, InterruptedException;
	
	public List<TestCaseResult> getTestResults(String testRunId);
}
