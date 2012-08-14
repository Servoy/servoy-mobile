package com.servoy.mobile.client;

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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.sksamuel.jqm4gwt.JQMContext;
import com.sksamuel.jqm4gwt.JQMPage;

/**
 * The main form manager, should be subclassed
 * @author jblok
 */
public abstract class FormManager 
{
	protected MobileClient application;
	protected LinkedHashMap<String, JQMPage> pageMap = new LinkedHashMap<String, JQMPage>();
	protected History history = new History();
	
	protected FormManager(MobileClient mc)
	{
		this.application = mc;
	}
	
	protected abstract JQMPage getFirstForm();

	protected abstract JQMPage getLoginForm();

	public abstract void showForm(String formName, Object data); 

	protected JQMPage getForm(String id) 
	{
		return pageMap.get(id);
	}
	protected void showForm(String id, JQMPage page) 
	{
		if (!pageMap.containsKey(id))
		{
			pageMap.put(id,page);
			if (pageMap.size() > 16)
			{
				Iterator<Entry<String,JQMPage>> it = pageMap.entrySet().iterator();
				Entry<String,JQMPage> entry = it.next();
				it.remove(); //remove first/oldest entry
				JQMPage oldPage = entry.getValue();
				removePage(oldPage);
			}
		}
		JQMContext.changePage(page);
	}

	private void removePage(JQMPage oldPage) 
	{
		oldPage.removeFromParent();//keep dom small
	}
	
	public History getHistory()
	{
		return history;
	}

	public class History
	{
		public final native void back()/*-{ 
			return $wnd.history.back();
		}-*/;
		
		public void clear()
		{
			removeAllForms();
		}
	}

	void removeAllForms() 
	{
		Iterator<JQMPage> it = pageMap.values().iterator();
		while (it.hasNext()) 
		{
			JQMPage oldPage = it.next();
			removePage(oldPage);
		}
		pageMap = new LinkedHashMap<String, JQMPage>();
	}

	public void showFirstForm() 
	{
		JQMContext.changePage(getFirstForm());
	}

	public void showLoginForm() 
	{
		JQMContext.changePage(getFirstForm());
	}
}
