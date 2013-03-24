package com.stonepeak.monkey.data;

public class TestCaseResult {
	private String suiteName;
	private String caseName;
	private boolean passed;
	private String errorMessage;
	private double elapsedTime;
	
	/**
	 * @return the suiteName
	 */
	public String getSuiteName() {
		return suiteName;
	}
	/**
	 * @param suiteName the suiteName to set
	 */
	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}
	/**
	 * @return the caseName
	 */
	public String getCaseName() {
		return caseName;
	}
	/**
	 * @param caseName the caseName to set
	 */
	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}
	/**
	 * @return the passed
	 */
	public boolean isPassed() {
		return passed;
	}
	/**
	 * @param passed the passed to set
	 */
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	/**
	 * @return the elapsedTime
	 */
	public double getElapsedTime() {
		return elapsedTime;
	}
	/**
	 * @param elapsedTime the elapsedTime to set
	 */
	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
}
