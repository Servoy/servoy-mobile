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

/**
 * @author acostescu
 */
@Export
public class JSMethod extends JSScriptPart implements IBaseSMMethod, Exportable
{

	public JSMethod(String parentScopeName, String scopeName, String name, JSSolutionModel model)
	{
		super(parentScopeName, scopeName, name, model);
	}

	@Override
	public String getCode()
	{
		String c = getCodeInternal(path[0], path[1], path[2]);
		if (c != null)
		{
			c = joinCodeWithName(c, getName());
		}
		return c;
	}

	@NoExport
	public static String joinCodeWithName(String c, String name)
	{
		int idx = c.indexOf("function "); //$NON-NLS-1$
		return (idx >= 0) ? (c.substring(0, idx + 9) + name + c.substring(idx + 9)) : c;
	}

	/**
	 * @return 2 items array, first being code, second name
	 */
	@NoExport
	public static String[] splitCodeFromName(String c)
	{
		int idx1 = c.indexOf("function "); //$NON-NLS-1$
		int idx2 = c.indexOf("("); //$NON-NLS-1$

		if (idx1 >= 0 && idx2 >= 0) return new String[] { c.substring(0, idx1 + 9) + c.substring(idx2), c.substring(idx1 + 9, idx2).trim() };
		return null;
	}

	@Override
	public void setCode(String content)
	{
		if (content == null) return;
		String[] sr = splitCodeFromName(content);
		if (sr != null && sr.length == 2)
		{
			setCodeInternal(path[0], path[1], path[2], sr[0]);
			reloadScope();
		}
	}

	@Override
	public Object[] getArguments()
	{
		return null; // this is only implemented by JSMethodWithArguments
	}

	@Override
	@NoExport
	public String getReferenceString()
	{
		return super.getReferenceString() + "()"; //$NON-NLS-1$
	}

	@Override
	@NoExport
	public boolean exists()
	{
		return existsInternal(path[0], path[1], path[2]);
	}

	@NoExport
	public final native boolean existsInternal(String parentScope, String scope, String fName) /*-{
		var scp = $wnd._ServoyInit_[parentScope][scope];
		if (scp && scp.fncs[fName]) {
			return true;
		}
		return false;
	}-*/;

	@NoExport
	public final native String getCodeInternal(String parentScope, String scope, String fName) /*-{
		return $wnd._ServoyInit_[parentScope][scope].fncs[fName];
	}-*/;

	@NoExport
	public final native void setCodeInternal(String parentScope, String scope, String fName, String code) /*-{
		$wnd._ServoyInit_[parentScope][scope].fncs[fName] = code;
	}-*/;

	@NoExport
	public void create(String code)
	{
		setCodeInternal(path[0], path[1], path[2], code);
		reloadScope();
	}

}
