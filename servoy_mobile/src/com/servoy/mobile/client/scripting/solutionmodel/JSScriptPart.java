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
import org.timepedia.exporter.client.NoExport;

import com.servoy.j2db.scripting.api.solutionmodel.IBaseSMMethod;
import com.servoy.mobile.client.scripting.Scope;

/**
 * @author acostescu
 */
@Export
public abstract class JSScriptPart implements IBaseSMMethod, Exportable
{

	protected static final String SCOPES = "scopes"; //$NON-NLS-1$
	protected static final String FORMS = "forms"; //$NON-NLS-1$
	protected final String[] path;
	protected final JSSolutionModel model;

	public JSScriptPart(String parentScopeName, String scopeName, String name, JSSolutionModel model)
	{
		this.model = model;
		path = new String[] { parentScopeName, scopeName, name };
	}

	@Override
	public String getName()
	{
		return path[2];
	}

	@Override
	public String getScopeName()
	{
		if (SCOPES.equals(path[0])) return path[1];
		return null;
	}

	@NoExport
	public String getReferenceString()
	{
		if (SCOPES.equals(path[0]))
		{
			return path[0] + "." + path[1] + "." + path[2]; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if (FORMS.equals(path[0]))
		{
			return path[2];
		}
		return null;
	}

	@NoExport
	public abstract boolean exists();

	@NoExport
	protected void reloadScope()
	{
		// TODO ac do this a bit more fine grained / like only reload this method - not everything, including default values for variables
		if (SCOPES.equals(path[0]))
		{
			model.getApplication().getScriptEngine().reloadScopeIfInitialized(path[1]);
		}
		else if (FORMS.equals(path[0]))
		{
			model.getApplication().getFormManager().reloadScopeIfInitialized(path[1]);
		}
	}

	@NoExport
	public final native void reloadScopeInternal(Scope scope) /*-{
		if (scope != null) {
		}
		return $wnd._ServoyInit_[parentScope][scope].fncs[fName] = eval(code);
	}-*/;

}
