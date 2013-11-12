/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.mobile.client.ui;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.persistence.Bean;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponentProvider;
import com.servoy.mobile.client.scripting.RuntimeBean;
import com.servoy.mobile.client.util.IDestroyable;
import com.sksamuel.jqm4gwt.JQMWidget;

/**
 * @author gboros
 *
 */
public class BeanComponent extends JQMWidget implements IComponent, IRuntimeComponentProvider, IDestroyable
{
	private DivWidget divWidget;
	private RuntimeBean scriptable;

	public BeanComponent(Bean bean, Executor executor, MobileClient application)
	{
		initWidget(divWidget = new DivWidget());
		String innerHTML = bean.getInnerHTML();
		if (innerHTML != null && innerHTML.contains("\\n")) //$NON-NLS-1$
		{
			innerHTML = innerHTML.replaceAll("\\\\n", "\n"); //$NON-NLS-1$//$NON-NLS-2$
		}
		if (innerHTML != null) setInnerHTML(innerHTML);
		setId();
		scriptable = new RuntimeBean(application, executor, this, bean);
	}

	class DivWidget extends Widget
	{
		DivElement divElement;

		DivWidget()
		{
			setElement(divElement = Document.get().createDivElement());
		}
	}

	public String getInnerHTML()
	{
		return divWidget.divElement.getInnerHTML();
	}

	public void setInnerHTML(String innerHTML)
	{
		divWidget.divElement.setInnerHTML(innerHTML);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IRuntimeComponentProvider#getRuntimeComponent()
	 */
	@Override
	public IRuntimeComponent getRuntimeComponent()
	{
		return scriptable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.util.IDestroyable#destroy()
	 */
	@Override
	public void destroy()
	{
		removeFromParent();
		scriptable.destroy();
		scriptable = null;
	}
}
