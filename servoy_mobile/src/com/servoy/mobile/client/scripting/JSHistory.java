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

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.servoy.base.scripting.api.IJSHistory;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.FormManager;

@Export
public class JSHistory implements Exportable, IJSHistory
{
	private final FormManager formManager;
	private final List<FormController> historyList = new ArrayList<FormController>();
	private int historyIndex = -1;

	public JSHistory(FormManager formManager)
	{
		this.formManager = formManager;
		export(ExporterUtil.wrap(this));
	}


	public void back()
	{
		go(-1);
	}

	public void forward()
	{
		go(1);
	}

	public void clear()
	{
		this.formManager.removeAllForms();
		historyIndex = -1;
		historyList.clear();
	}

	@Override
	public String getFormName(int i)
	{
		int listIndex = i - 1;
		if (listIndex >= 0 && listIndex < historyList.size())
		{
			return historyList.get(listIndex).getName();
		}
		return null;
	}


	@Override
	public void go(int i)
	{
		int formIndex = historyIndex + i;
		if (formIndex >= 0 && formIndex <= historyList.size() && historyList.get(formIndex) != null)
		{
			int history = formIndex;
			historyIndex = -2;
			formManager.showForm(historyList.get(history), true);
			historyIndex = history;
		}
	}

	@Override
	public int size()
	{
		return historyList.size();
	}


	@Override
	public int getCurrentIndex()
	{
		return historyIndex + 1;
	}


	@Override
	public boolean removeIndex(int index)
	{
		if (index > 0 && index <= historyList.size())
		{
			if (historyList.size() == 1 && index == 1)
			{
				clear();
				return true;
			}
			if (historyIndex == index - 1)
			{
				go(historyIndex == 0 ? 1 : -1);
			}
			historyList.remove(index - 1);
			if (index - 1 < historyIndex)
			{
				historyIndex--;
			}
			return true;
		}
		return false;

	}


	@Override
	public boolean removeForm(String formName)
	{
		int i = historyList.indexOf(formName);
		if (i != -1 && !removeIndex(i))
		{
			return false;
		}
		return formManager.removeForm(formName);
	}

	// cannot use window.history, breaks javascript object
	private final native void export(Object object)
	/*-{
		$wnd._ServoyUtils_.history = object;
	}-*/;


	/**
	 * @param page
	 */
	public void add(FormController page)
	{
		if (historyIndex == -2) return; // skip, generated by itself.
		while (historyList.size() - 1 > historyIndex)
		{
			historyList.remove(historyIndex + 1);
		}
		historyIndex = historyList.size();
		historyList.add(page);

	}

}