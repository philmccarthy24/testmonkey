/**
 * 
 */
package com.testmonkey.util;

import org.xml.sax.InputSource;


/**
 * @author phil
 *
 */
public abstract class InputSourceFactory {
	private static InputSourceFactory registeredFactory = null;
	
	/**
	 * Abstract factory method for implementation
	 * @return
	 */
	protected abstract InputSource createInputSource(String sourceIdentifier);
	
	public static void registerInputSourceProvider(InputSourceFactory provider)
	{
		registeredFactory = provider;
	}
	
	/**
	 * Static factory method
	 * @return
	 */
	public static InputSource buildInputSource(String sourceIdentifier)
	{
		if (registeredFactory == null)
			throw new IllegalStateException("No InputSource factory registered. Ensure registerInputSourceProvider is called.");
		
		return registeredFactory.createInputSource(sourceIdentifier);
	}
}
