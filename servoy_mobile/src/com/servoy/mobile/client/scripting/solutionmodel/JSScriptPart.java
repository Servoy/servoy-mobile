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
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.NoExport;

import com.servoy.mobile.client.scripting.ScriptEngine;

/**
 * @author acostescu
 */
@Export
@ExportPackage("")
public abstract class JSScriptPart implements Exportable
{

	protected final String[] path;
	protected final JSSolutionModel model;
	private final JSForm parentForm;

	public JSScriptPart(String parentScopeName, String scopeName, String name, JSSolutionModel model, JSForm parentForm)
	{
		this.model = model;
		this.parentForm = parentForm; // we need the exact instance which was used for getScript/createScript, so clone is done on that one so that the persist reference gets updated correctly
		path = new String[] { parentScopeName, scopeName, name };
	}

	@Getter
	public String getName()
	{
		return path[2];
	}

	public String getScopeName()
	{
		if (ScriptEngine.SCOPES.equals(path[0])) return path[1];
		return null;
	}

	@NoExport
	public String getReferenceString()
	{
		if (ScriptEngine.SCOPES.equals(path[0]))
		{
			return path[0] + "." + path[1] + "." + path[2]; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if (ScriptEngine.FORMS.equals(path[0]))
		{
			return path[2];
		}
		return null;
	}

	@NoExport
	public abstract boolean exists();

	@NoExport
	public abstract boolean remove();

	@NoExport
	protected void reloadScope()
	{
		if (ScriptEngine.SCOPES.equals(path[0]))
		{
			model.getApplication().getScriptEngine().reloadScopeIfInitialized(path[1]);
		}
		else if (ScriptEngine.FORMS.equals(path[0]))
		{
			model.getApplication().getFormManager().reloadScopeIfInitialized(path[1]);
		}
	}

	protected void cloneFormIfNeeded()
	{
		if (parentForm != null) parentForm.cloneIfNeeded();
	}

}
