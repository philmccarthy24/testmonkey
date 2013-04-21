package com.testmonkey.exceptions;

public class VarNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4844144029955659527L;

	public VarNotFoundException()
	{
	}
	
	public VarNotFoundException(String message)
	{
		super(message);
	}
}
