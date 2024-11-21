package com.servoy.mobile.client.scripting;

import java.util.HashMap;
import java.util.Map;

import com.servoy.mobile.client.angular.Proxy;

public class ElementScope extends Scope
{
	private final Map<String, Proxy> elements = new HashMap<>();

	public void addComponent(String name, Proxy component)
	{
		exportProperty(name);
		elements.put(name, component);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.servoy.mobile.client.scripting.Scope#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String variable)
	{
		return elements.get(variable);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.servoy.mobile.client.scripting.Scope#setValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(String variable, Object vaue)
	{
		// ignore, readonly.
	}

	@Override
	public void setVariableType(String variable, int type)
	{
	}

	@Override
	public int getVariableType(String variable)
	{
		return -4; // media
	}

	public void destroy()
	{
		elements.clear();
		ScriptEngine.clearScope(this);
	}
}
