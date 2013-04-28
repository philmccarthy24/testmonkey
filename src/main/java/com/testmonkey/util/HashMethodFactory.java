/**
 * 
 */
package com.testmonkey.util;


/**
 * @author phil
 *
 */
public abstract class HashMethodFactory {
	private static HashMethodFactory registeredFactory = null;
	
	/**
	 * Abstract factory method for implementation
	 * @return
	 */
	protected abstract IHashMethod createHashMethod();
	
	public static void registerHashMethodProvider(HashMethodFactory provider)
	{
		registeredFactory = provider;
	}
	
	/**
	 * Static factory method
	 * @return
	 */
	public static IHashMethod buildHashMethod()
	{
		if (registeredFactory == null)
			throw new IllegalStateException("No HashMethod factory registered. Ensure registerHashMethodProvider is called.");
		
		return registeredFactory.createHashMethod();
	}
}
