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
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.JsArrayString;
import com.servoy.base.solutionmodel.IBaseSMForm;
import com.servoy.base.solutionmodel.mobile.IMobileSolutionModel;
import com.servoy.base.util.DataSourceUtilsBase;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.FlattenedSolution;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.ScriptEngine;
import com.servoy.mobile.client.scripting.solutionhelper.JSList;
import com.servoy.mobile.client.util.Utils;

/**
 * @author jcompagner
 *
 */
@Export
@ExportPackage("")
public class JSSolutionModel implements IMobileSolutionModel, Exportable
{
	private final MobileClient application;
	private final FlattenedSolution solution;

	public JSSolutionModel(MobileClient application)
	{
		this.application = application;
		this.solution = application.getFlattenedSolution();
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
	public JSForm newForm(String name, String dataSource)
	{
		return newForm(name, dataSource, null, false, 0, 0);
	}

	@Override
	public IBaseSMForm revertForm(String formName)
	{
		application.getFlattenedSolution().revertForm(formName);
		application.getFormManager().removeForm(formName); // so that it will reload the correct form into the map when accessed
		return getForm(formName);
	}

	@Override
	public boolean removeForm(String name)
	{
		if (name != null)
		{
			Form f = application.getFlattenedSolution().getForm(name);
			if (f != null)
			{
				if (application.getFormManager().removeForm(name))
				{
					application.getFlattenedSolution().removeFormOrCopy(f);
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
		for (Form f : application.getFlattenedSolution().getForms())
		{
			if (datasource == null || datasource.equals(f.getDataSource()))
			{
				forms.add(new JSForm(f, this));
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
		ValueList vl = application.getFlattenedSolution().getValueList(name);
		return vl != null ? new JSValueList(vl) : null;
	}

	@Override
	public JSValueList[] getValueLists()
	{
		List<JSValueList> valuelists = new ArrayList<JSValueList>();
		for (int i = 0; i < application.getFlattenedSolution().valuelistCount(); i++)
		{
			valuelists.add(new JSValueList(application.getFlattenedSolution().getValueList(i)));
		}
		return valuelists.toArray(new JSValueList[0]);
	}

	@Override
	public JSVariable newGlobalVariable(String scopeName, String name, int type)
	{
		String scope = (scopeName == null ? "globals" : scopeName); //$NON-NLS-1$

		JSVariable gv = new JSVariable(ScriptEngine.SCOPES, scope, name, this, null);

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
		JSVariable gv = new JSVariable(ScriptEngine.SCOPES, scopeName, name, this, null);
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

		String[] codeAndName = JSMethod.splitFullCode(code);
		if (codeAndName != null && codeAndName.length == 3)
		{
			JSMethod gm = new JSMethod(ScriptEngine.SCOPES, scope, codeAndName[1], this, null);

			if (!gm.exists())
			{
				createScopeIfNecessary(scope);
				gm.create(codeAndName[0], codeAndName[2]);
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
				_sv_fncs : {},
				_sv_vrbs : {}
			};
		}
	}-*/;

	@Override
	public JSMethod getGlobalMethod(String scopeName, String name)
	{
		JSMethod gm = new JSMethod(ScriptEngine.SCOPES, scopeName, name, this, null);
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

	@Override
	public JSList newListForm(String formName, String dataSource, String textDataProviderID)
	{
		if (getForm(formName) != null) return null; // a form with that name already exists

		// create form
		JSForm listForm = newForm(formName, dataSource, null, false, 100, 380);
		listForm.setView(IBaseSMForm.LOCKED_TABLE_VIEW);

		// create list abstraction
		JSList listComponent = new JSList(listForm);

		// create other persists for remaining contents of list
		if (textDataProviderID != null) listComponent.setTextDataProviderID(textDataProviderID);

		return listComponent;
	}

	@Override
	public JSList getListForm(String name)
	{
		JSForm f = getForm(name);
		if (f != null && f.getView() == IBaseSMForm.LOCKED_TABLE_VIEW)
		{
			return new JSList(f);
		}
		return null;
	}

	@Override
	public JSList[] getListForms()
	{
		List<JSList> listFormsList = new ArrayList<JSList>();
		JSForm[] forms = getForms();
		if (forms != null)
		{
			for (JSForm form : forms)
			{
				if (form.getView() == IBaseSMForm.LOCKED_TABLE_VIEW)
				{
					listFormsList.add(new JSList(form));
				}
			}
		}
		return listFormsList.toArray(new JSList[listFormsList.size()]);
	}
}
