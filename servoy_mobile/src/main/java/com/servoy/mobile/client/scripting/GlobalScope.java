package com.servoy.mobile.client.scripting;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.util.Utils;


public class GlobalScope extends Scope
{
	public static final String GLOBAL_SCOPE = "globals"; //$NON-NLS-1$
	public static final String GLOBALS_DOT_PREFIX = GLOBAL_SCOPE + '.';
	public static final String SCOPES = "scopes"; //$NON-NLS-1$
	public static final String SCOPES_DOT_PREFIX = SCOPES + '.';

	private final String name;
	private final Map<String, Object> scopeVariables = new HashMap<String, Object>();
	protected final Map<String, Object> servoyProperties = new HashMap<String, Object>();
	protected final Map<String, Integer> variableTypes = new HashMap<String, Integer>();
	protected final MobileClient client;

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
		Object object = scopeVariables.get(variable);
		if (object != null)
		{
			if (object instanceof Number)
			{
				return IColumnTypeConstants.NUMBER;
			}
			else if (object instanceof Date)
			{
				return IColumnTypeConstants.DATETIME;
			}
			else if (object instanceof Boolean)
			{
				return Types.BOOLEAN;
			}
		}
		Integer type = variableTypes.get(variable);
		if (type != null) return type.intValue();
		return IColumnTypeConstants.MEDIA;
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

		Object oldValue = scopeVariables.get(variable);
		int type = getVariableType(variable);
		Object obj = getValueAsRightType(value, type, null);
		if (obj != null && type == IColumnTypeConstants.DATETIME && !(obj instanceof Date || obj instanceof Number))
		{
			Log.error("Can't set value: " + obj + " on dataprovider: " + variable + " of globalscope: " + getName() + ", not a date or number");
			return;
		}
		scopeVariables.put(variable, obj);

		if (!Utils.equalObjects(oldValue, obj)) fireModificationEvent(variable, obj);
	}

	public boolean hasVariable(String variable)
	{
		return scopeVariables.containsKey(variable) || variableTypes.containsKey(variable);
	}

	/**
	 * Get the scope of a variable.
	 * <br> globals.x -> [globals, x]
	 * <br> scopes.s.x -> [s, x]
	 * <br> x -> [null, x]
	 */
	public static String[] getVariableScope(String idParam)
	{
		if (idParam == null) return null;
		String id = idParam;

		int firstDotIdx = id.indexOf('.');
		if (firstDotIdx != -1 && firstDotIdx < id.length() - 1)
		{
			String idWithoutPrefix = id.substring(firstDotIdx + 1);
			if (idWithoutPrefix.startsWith(GLOBALS_DOT_PREFIX) || idWithoutPrefix.startsWith(SCOPES_DOT_PREFIX))
			{
				// this is a variable from a module, remove the module name from the id
				id = idWithoutPrefix;
			}
		}

		String scopeName = null;
		String dpName = id;

		if (id.startsWith(GLOBALS_DOT_PREFIX))
		{
			scopeName = GLOBAL_SCOPE;
			dpName = id.substring(GLOBALS_DOT_PREFIX.length());
		}
		else if (id.startsWith(SCOPES_DOT_PREFIX))
		{
			int dot = id.indexOf('.', SCOPES_DOT_PREFIX.length() + 1);
			if (dot >= 0)
			{
				scopeName = id.substring(SCOPES_DOT_PREFIX.length(), dot);
				dpName = id.substring(SCOPES_DOT_PREFIX.length() + scopeName.length() + 1);
			}
		}

		return new String[] { scopeName, dpName };
	}
}
