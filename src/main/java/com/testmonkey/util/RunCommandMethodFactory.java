/**
 * 
 */
package com.testmonkey.util;


/**
 * @author phil
 *
 */
public abstract class RunCommandMethodFactory {
	private static RunCommandMethodFactory registeredFactory = null;
	
	/**
	 * Abstract factory method for implementation
	 * @return
	 */
	protected abstract IRunCommandMethod createRunCommandMethod();
	
	public static void registerRunCommandMethodProvider(RunCommandMethodFactory provider)
	{
		registeredFactory = provider;
	}
	
	/**
	 * Static factory method
	 * @return
	 */
	public static IRunCommandMethod buildRunCommandMethod()
	{
		if (registeredFactory == null)
			throw new IllegalStateException("No RunCommandMethod factory registered. Ensure registerRunCommandMethodProvider is called.");
		
		return registeredFactory.createRunCommandMethod();
	}
}
