package com.servoy.mobile.client.scripting;

import java.util.HashMap;
import java.util.Map;


public class GlobalScope extends Scope 
{
	private final String name;
	private final Map<String, Object> scopeVariables = new HashMap<String, Object>();
	protected final Map<String, Object> servoyProperties = new HashMap<String, Object>();
	protected final Map<String,Integer> variableTypes = new HashMap<String, Integer>();

	public GlobalScope(String name) {
		this.name = name;
		servoyProperties.put("currentcontroller", new Controller());
	}
	
	public String getName() {
		return name;
	}
	
	public void setVariableType(String variable, int type) {
		variableTypes.put(variable, Integer.valueOf(type));
	}
	
	public int getVariableType(String variable) {
		Integer type = variableTypes.get(variable);
		if (type != null) return type.intValue();
		return -4; // IColumnTypes.MEDIA;
	}
	
	public Object getValue(String variable) {
		Object servoyProperty = servoyProperties.get(variable);
		if (servoyProperty != null) return servoyProperty;
		
		return scopeVariables.get(variable);
	}

	public void setValue(String variable, Object value) {
		if (servoyProperties.containsKey(variable)) return;
		
		scopeVariables.put(variable, value);
		// fire property change
	}

}
