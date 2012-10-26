package com.servoy.mobile.client.persistence;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * 
 * @author gboros
 */
public class BaseComponent extends JavaScriptObject
{
	protected BaseComponent()
	{
	}

	public final native String getCustomProperties() /*-{
		return this.customProperties;
	}-*/;

	public final MobileProperties getMobileProperties()
	{
		String customProperties = getCustomProperties();
		if (customProperties != null)
		{
			JSONObject customPropertiesJSON = JSONParser.parseStrict(customProperties).isObject();
			if (customPropertiesJSON != null)
			{
				CustomProperties customProperies = (CustomProperties)customPropertiesJSON.getJavaScriptObject().cast();
				return customProperies.getMobile();
			}
		}

		return null;
	}

	static class CustomProperties extends JavaScriptObject
	{
		protected CustomProperties()
		{
		}

		public final native MobileProperties getMobile() /*-{
			return this.mobile;
		}-*/;
	}

	public static class MobileProperties extends JavaScriptObject
	{
		protected MobileProperties()
		{
		}

		public final native boolean isHeaderLeftButton() /*-{
			return this.headerLeftButton ? this.headerLeftButton : false;
		}-*/;

		public final native boolean isHeaderRightButton() /*-{
			return this.headerRightButton ? this.headerRightButton : false;
		}-*/;

		public final native boolean isHeaderText() /*-{
			return this.headerText ? this.headerText : false;
		}-*/;

		public final native boolean isFooterItem() /*-{
			return this.footeritem ? this.footeritem : false;
		}-*/;

		public final native boolean isFormTabPanel() /*-{
			return this.formtabpanel ? this.formtabpanel : false;
		}-*/;

		public final native boolean isListTabPanel() /*-{
			return this.list ? this.list : false;
		}-*/;

		public final native boolean isListItemButton() /*-{
			return this.listitemButton ? this.listitemButton : false;
		}-*/;

		public final native boolean isListItemSubtext() /*-{
			return this.listitemSubtext ? this.listitemSubtext : false;
		}-*/;

		public final native boolean isListItemCount() /*-{
			return this.listitemCount ? this.listitemCount : false;
		}-*/;

		public final native boolean isListItemImage() /*-{
			return this.listitemImage ? this.listitemImage : false;
		}-*/;

		public final native boolean isListItemHeader() /*-{
			return this.listitemHeader ? this.listitemHeader : false;
		}-*/;

		public final native int getHeaderSize() /*-{
			return this.headerSize ? this.headerSize : 4;
		}-*/;

		public final native int getRadioStyle() /*-{
			return this.radioStyle ? this.radioStyle : 0;
		}-*/;
	}
}
