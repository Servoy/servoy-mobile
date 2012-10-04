package com.servoy.mobile.client.scripting;

import java.util.HashMap;
import java.util.Map;

import com.servoy.mobile.client.MobileClient;


public class GlobalScope extends Scope
{
	private final String name;
	private final Map<String, Object> scopeVariables = new HashMap<String, Object>();
	protected final Map<String, Object> servoyProperties = new HashMap<String, Object>();
	protected final Map<String, Integer> variableTypes = new HashMap<String, Integer>();
	private final MobileClient client;

	public GlobalScope(String name, MobileClient client)
	{
		this.name = name;
		this.client = client;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void setVariableType(String variable, int type)
	{
		variableTypes.put(variable, Integer.valueOf(type));
	}

	@Override
	public int getVariableType(String variable)
	{
		Integer type = variableTypes.get(variable);
		if (type != null) return type.intValue();
		return -4; // IColumnTypes.MEDIA;
	}

	@Override
	public Object getValue(String variable)
	{
		Object servoyProperty = servoyProperties.get(variable);
		if (servoyProperty != null) return servoyProperty;
		if ("currentcontroller".equals(variable)) return client.getFormManager().getCurrentForm();

		return scopeVariables.get(variable);
	}

	@Override
	public void setValue(String variable, Object value)
	{
		if (servoyProperties.containsKey(variable)) return;

		scopeVariables.put(variable, value);
		// fire property change
	}

}
