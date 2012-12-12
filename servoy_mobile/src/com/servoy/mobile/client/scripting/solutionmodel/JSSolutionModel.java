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

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.GWT;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm;
import com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel;
import com.servoy.j2db.util.DataSourceUtilsBase;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.Solution;
import com.servoy.mobile.client.persistence.ValueList;
import com.servoy.mobile.client.scripting.ScriptEngine;

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
		GWT.create(SM_ALIGNMENT.class);
		GWT.create(SM_DEFAULTS.class);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#cloneForm(java.lang.String, com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm)
	 */
	@Override
	public JSForm cloneForm(String newName, IBaseSMForm form)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#cloneComponent(java.lang.String,
	 * com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent)
	 */
	@Override
	public JSComponent cloneComponent(String newName, IBaseSMComponent component)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#cloneComponent(java.lang.String,
	 * com.servoy.j2db.scripting.api.solutionmodel.IBaseSMComponent, com.servoy.j2db.scripting.api.solutionmodel.IBaseSMForm)
	 */
	@Override
	public JSComponent cloneComponent(String newName, IBaseSMComponent component, IBaseSMForm newParentForm)
	{
		// TODO ac Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#removeForm(java.lang.String)
	 */
	@Override
	public boolean removeForm(String name)
	{
		// TODO ac Auto-generated method stub
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

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getForms(java.lang.String)
//	 */
//	@Override
//	public JSForm[] getForms(String datasource)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getForms(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public JSForm[] getForms(String server, String tablename)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getForms()
//	 */
//	@Override
//	public JSForm[] getForms()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

	@Override
	public JSValueList getValueList(String name)
	{
		ValueList vl = application.getSolution().getValueList(name);
		return vl != null ? new JSValueList(vl) : null;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getValueLists()
//	 */
//	@Override
//	public JSValueList[] getValueLists()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getScopeNames()
	 */
	@Override
	public String[] getScopeNames()
	{
		// TODO ac Auto-generated method stub
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalVariables()
//	 */
//	@Override
//	public JSVariable[] getGlobalVariables()
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalVariables(java.lang.String)
//	 */
//	@Override
//	public JSVariable[] getGlobalVariables(String scopeName)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

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

	// TODO ac there are several methods like this one commented out because of a compilation error in GWT. Please uncomment and implement all of them
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalMethods()
//	 */
//	@Override
//	public JSMethod[] getGlobalMethods()
//	{
//		// TODO ac Auto-generated method stub
//		// see the implementation in JSForm 
//		return null;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.servoy.j2db.scripting.api.solutionmodel.IBaseSolutionModel#getGlobalMethods(java.lang.String)
//	 */
//	@Override
//	public JSMethod[] getGlobalMethods(String scopeName)
//	{
//		// TODO ac Auto-generated method stub
//		return null;
//	}

}
