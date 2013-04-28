/**
 * 
 */
package com.testmonkey.app;


/**
 * @author phil
 *
 */
public abstract class GTestRunnerFactory {
	private static GTestRunnerFactory registeredFactory = null;
	
	/**
	 * Abstract factory method for implementation
	 * @return
	 */
	protected abstract IGTestRunner createGTestRunner();
	
	public static void registerGTestRunnerProvider(GTestRunnerFactory provider)
	{
		registeredFactory = provider;
	}
	
	/**
	 * Static factory method
	 * @return
	 */
	public static IGTestRunner buildGTestRunner()
	{
		if (registeredFactory == null)
			throw new IllegalStateException("No GTestRunner factory registered. Ensure registerGTestRunnerProvider is called.");
		
		return registeredFactory.createGTestRunner();
	}
}
