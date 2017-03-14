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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.ui.UIObject;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandlerForPageSwitch;

/**
 * @author Ovidiu Buligan
 *
 */
public abstract class TapHandlerForPageSwitchWithBlur extends TapHandlerForPageSwitch
{

	@Override
	public void onSafeTap(TapEvent event)
	{
		EventTarget target = event.getJQueryEvent().getEventTarget();
		UIObject e = (UIObject)event.getSource();
		blurActiveElement(e.getElement(), Element.as(target));
		onTapAfterBlur(event);
	}

	public abstract void onTapAfterBlur(TapEvent event);

	/**
	 * Blurs active element if the active element wasn't the target AND the active element ins't in the ancestral path .
	 * Returns true if the blur was executed or false otherwise.
	 * @param source
	 * @param target
	 * @return
	 */
	protected native boolean blurActiveElement(Element source, Element target)/*-{
		var activeElement = source.ownerDocument.activeElement;
		//check if target fom element contains the active element in it's children
		//(jqm component which as the focus was clicked)
		$wnd.$(target).find('*').each(function(el) {
			if (el == activeElement) {
				return false;
			}
		})

		// check ancestry path 
		$wnd.$(target).parentsUntil('[data-role="page"]').each(function(el) {
			if (el == activeElement) {
				return false;
			}
		})

		activeElement.blur();
		return true;

	}-*/;
}
