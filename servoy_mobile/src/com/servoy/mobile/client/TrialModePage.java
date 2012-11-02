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

package com.servoy.mobile.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.button.JQMButton;

/**
 * @author lvostinar
 *
 */
public class TrialModePage extends JQMPage
{

	public TrialModePage(final MobileClient application)
	{
		HTML htmlText = new HTML();
		htmlText.setHTML("<p><i>Welcome to</i></p><center><h1><span><b>Servoy</b></span> Mobile</h1></center>"
			+ "<p>Thank you for evaluating our product!</p>"
			+ "<p>We hope you enjoy the experience of creating great Mobile Business Applications in no-time</p>"
			+ "<p>Contact your Servoy sales representative for available licensing options or email Servoy at <a href=\"mailto:sales@servoy.com\" target=\"_blank\">sales@servoy.com</a></p>"
			+ "<p>If you have any other question, check out our <a href=\"http://forum.servoy.com\" target=\"_blank\">Servoy Talk forum</a> or contact us at <a href=\"mailto:support@servoy.com\" target=\"_blank\">support@servoy.com</a></p>");
		htmlText.setWordWrap(true);
		add(htmlText);
		JQMButton next = new JQMButton("Launch App");
		next.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				if (!application.getFoundSetManager().hasContent() && application.isOnline())
				{
					application.sync();
				}
				else
				{
					application.showFirstForm();
				}
			}
		});
		add(next);
	}
}
