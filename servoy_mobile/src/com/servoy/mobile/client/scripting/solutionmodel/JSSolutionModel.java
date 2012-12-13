/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */

package com.servoy.mobile.client.scripting.solutionmodel;

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JsArrayString;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel;
import com.servoy.j2db.util.DataSourceUtilsBase;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Solution;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.ScriptEngine;
import com.servoy.mobile.client.util.Utils;

/**
 * @author jcompagner
 *
 */
@Export
public class JSSolutionModel implements IBaseSolutionModel, Exportable
{
	private final MobileClient application;
	private final Solution solution;

	public JSSolutionModel(MobileClient application)
	{
		this.application = application;
		this.solution = application.getSolution();
		export(ExporterUtil.wrap(this));
	}

	public MobileClient getApplication()
	{
		return application;
	}

	private native void export(Object object)
	/*-{
		$wnd.solutionModel = object;
	}-*/;

	public JSForm getForm(String name)
	{
		Form form = solution.getForm(name);
		if (form != null)
		{
			return new JSForm(form, this);
		}
		return null;
	}

	@Override
	public JSForm newForm(String name, String serverName, String tableName, String styleName, boolean show_in_menu, int width, int height)
	{
		return newForm(name, DataSourceUtilsBase.createDBTableDataSource(serverName, tableName), styleName, show_in_menu, width, height);
	}

	@Override
	public JSForm newForm(String name, String dataSource, String styleName, boolean show_in_menu, int width, int height)
	{
		Form form = solution.getForm(name);
		if (form == null)
		{
			return new JSForm(solution.newForm(name, dataSource, width, height), this);
		}
		return null;
	}

	@Override
	public boolean removeForm(String name)
	{
		if (name != null && application.getFormManager().removeForm(name))
		{
			for (int i = 0; i < application.getSolution().formCount(); i++)
			{
				Form form = application.getSolution().getForm(i);
				if (form != null && name.equals(form.getName()))
				{
					application.getSolution().removeForm(i);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean removeGlobalMethod(String scopeName, String name)
	{
		JSMethod method = getGlobalMethod(scopeName, name);
		return method.remove();
	}

	@Override
	public boolean removeGlobalVariable(String scopeName, String name)
	{
		JSVariable variable = getGlobalVariable(scopeName, name);
		return variable.remove();
	}

	@Override
	public JSForm[] getForms(String datasource)
	{
		List<JSForm> forms = new ArrayList<JSForm>();
		for (int i = 0; i < application.getSolution().formCount(); i++)
		{
			if (datasource == null || datasource.equals(application.getSolution().getForm(i).getDataSource()))
			{
				forms.add(new JSForm(application.getSolution().getForm(i), this));
			}
		}
		return forms.toArray(new JSForm[0]);
	}

	@Override
	public JSForm[] getForms(String server, String tablename)
	{
		return getForms(DataSourceUtilsBase.createDBTableDataSource(server, tablename));
	}

	@Override
	public JSForm[] getForms()
	{
		return getForms(null);
	}

	@Override
	public JSValueList getValueList(String name)
	{
		ValueList vl = application.getSolution().getValueList(name);
		return vl != null ? new JSValueList(vl) : null;
	}

	@Override
	public JSValueList[] getValueLists()
	{
		List<JSValueList> valuelists = new ArrayList<JSValueList>();
		for (int i = 0; i < application.getSolution().valuelistCount(); i++)
		{
			valuelists.add(new JSValueList(application.getSolution().getValueList(i)));
		}
		return valuelists.toArray(new JSValueList[0]);
	}

	@Override
	public JSVariable newGlobalVariable(String scopeName, String name, int type)
	{
		String scope = (scopeName == null ? "globals" : scopeName); //$NON-NLS-1$

		JSVariable gv = new JSVariable(ScriptEngine.SCOPES, scope, name, this);

		if (!gv.exists())
		{
			createScopeIfNecessary(scope);
			gv.create(type);
			return gv;
		}
		return null;
	}

	@Override
	public JSVariable getGlobalVariable(String scopeName, String name)
	{
		JSVariable gv = new JSVariable(ScriptEngine.SCOPES, scopeName, name, this);
		return gv.exists() ? gv : null;
	}

	@Override
	public String[] getScopeNames()
	{
		return Utils.unwrapArray(ScriptEngine.getScopeNamesInternal(ScriptEngine.SCOPES));
	}

	@Override
	public JSVariable[] getGlobalVariables()
	{
		return getGlobalVariables(null);
	}

	@Override
	public JSVariable[] getGlobalVariables(String scopeName)
	{
		List<JSVariable> methods = new ArrayList<JSVariable>();
		if (scopeName == null)
		{
			for (String scope : getScopeNames())
			{
				methods.addAll(getGlobalVariablesForAScope(scope));
			}
		}
		else
		{
			methods = getGlobalVariablesForAScope(scopeName);
		}
		return methods.toArray(new JSVariable[0]);
	}

	private List<JSVariable> getGlobalVariablesForAScope(String scopeName)
	{
		List<JSVariable> variables = new ArrayList<JSVariable>();
		JsArrayString names = ScriptEngine.getVariableNamesInternal(ScriptEngine.SCOPES, scopeName);
		if (names != null)
		{
			for (int i = names.length() - 1; i >= 0; i--)
			{
				variables.add(getGlobalVariable(scopeName, names.get(i)));
			}
		}
		return variables;
	}

	@Override
	public JSMethod newGlobalMethod(String scopeName, String code)
	{
		if (code == null) return null;
		String scope = (scopeName == null ? "globals" : scopeName); //$NON-NLS-1$

		String[] codeAndName = JSMethod.splitCodeFromName(code);
		if (codeAndName != null && codeAndName.length == 2)
		{
			JSMethod gm = new JSMethod(ScriptEngine.SCOPES, scope, codeAndName[1], this);

			if (!gm.exists())
			{
				createScopeIfNecessary(scope);
				gm.create(codeAndName[0]);
				return gm;
			}
		}
		return null;
	}

	private final native void createScopeIfNecessary(String scope) /*-{
		if (!$wnd._ServoyInit_.scopes[scope]) {
			Object.defineProperty($wnd.scopes, scope, {
				get : function() {
					return $wnd._ServoyUtils_.getGlobalScope(scope);
				},
				configurable : false
			});
			$wnd._ServoyInit_.scopes[scope] = {
				fncs : {},
				vrbs : {}
			};
		}
	}-*/;

	@Override
	public JSMethod getGlobalMethod(String scopeName, String name)
	{
		JSMethod gm = new JSMethod(ScriptEngine.SCOPES, scopeName, name, this);
		return gm.exists() ? gm : null;
	}

	@Override
	public JSMethod[] getGlobalMethods()
	{
		return getGlobalMethods(null);
	}

	@Override
	public JSMethod[] getGlobalMethods(String scopeName)
	{
		List<JSMethod> methods = new ArrayList<JSMethod>();
		if (scopeName == null)
		{
			for (String scope : getScopeNames())
			{
				methods.addAll(getGlobalMethodsForAScope(scope));
			}
		}
		else
		{
			methods = getGlobalMethodsForAScope(scopeName);
		}
		return methods.toArray(new JSMethod[0]);
	}

	private List<JSMethod> getGlobalMethodsForAScope(String scopeName)
	{
		List<JSMethod> methods = new ArrayList<JSMethod>();
		JsArrayString names = ScriptEngine.getFunctionNamesInternal(ScriptEngine.SCOPES, scopeName);
		if (names != null)
		{
			for (int i = names.length() - 1; i >= 0; i--)
			{
				methods.add(getGlobalMethod(scopeName, names.get(i)));
			}
		}
		return methods;
	}

}
