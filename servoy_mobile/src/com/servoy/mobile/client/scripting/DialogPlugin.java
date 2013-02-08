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

package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.user.client.Window;
import com.servoy.base.plugins.IMobileDialogProvider;

/**
 * @author lvostinar
 *
 */
@Export
public class DialogPlugin implements IMobileDialogProvider, Exportable
{
	public DialogPlugin()
	{
	}

	public String showWarningDialog(String dialogTitle, String dialogMessage)
	{
		Window.alert(dialogMessage == null ? "<null>" : dialogMessage.toString()); //$NON-NLS-1$
		return "OK"; //$NON-NLS-1$
	}

	public String showQuestionDialog(String dialogTitle, String dialogMessage)
	{
		boolean result = Window.confirm(dialogMessage == null ? "<null>" : dialogMessage.toString()); //$NON-NLS-1$
		return (result ? "OK" : "Cancel"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
