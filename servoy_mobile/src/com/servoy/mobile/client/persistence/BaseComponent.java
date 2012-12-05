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

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native void setAttributeValueInt(String attrName, int value) /*-{
		this[attrName] = value;
	}-*/;

	public final native void setAttributeValueString(String attrName, String value) /*-{
		this[attrName] = value;
	}-*/;

	public final native void setAttributeValueBoolean(String attrName, boolean value) /*-{
		this[attrName] = value;
	}-*/;

	public final native String getAttributeValueString(String attrName, String defaultValue) /*-{
		return this[attrName] ? this[attrName] : defaultValue;
	}-*/;

	public final native int getAttributeValueInt(String attrName, int defaultValue) /*-{
		return this[attrName] ? this[attrName] : defaultValue;
	}-*/;

	public final native boolean getAttributeValueBoolean(String attrName, boolean defaultValue) /*-{
		return this[attrName] != undefined ? this[attrName] : defaultValue;
	}-*/;

	public final native String getCustomPropertiesInt() /*-{
		return this.customProperties;
	}-*/;

	public final native void setCustomPropertiesInt(String customProperties) /*-{
		if (customProperties != null)
			this.customProperties = customProperties;
		else
			delete (this.customProperties);
	}-*/;

	public final MobilePropertiesWrapper getMobilePropertiesWrapper()
	{
		String customProperties = getCustomPropertiesInt();
		if (customProperties != null)
		{
			return getMobileProperties(customProperties);
		}

		return null;
	}

	public final MobileProperties getMobileProperties()
	{
		MobilePropertiesWrapper x = getMobilePropertiesWrapper();
		return x == null ? null : x.get();
	}

	public final void setCustomProperties(CustomProperties cp)
	{
		if (cp != null) setCustomPropertiesInt(cp.toSource());
		else setCustomPropertiesInt(null);
	}

	public final CustomProperties getCustomProperties()
	{
		String customProperties = getCustomPropertiesInt();
		if (customProperties != null)
		{
			return getCustomProperties(customProperties);
		}

		return null;
	}

	public final MobilePropertiesWrapper createMobileProperties()
	{
		return getMobileProperties("{\"mobile\":{}}"); //$NON-NLS-1$
	}

	public final void setMobileProperties(MobilePropertiesWrapper mp)
	{
		if (mp != null) setCustomPropertiesInt(mp.parent.toSource());
		else
		{
			CustomProperties cp = getCustomProperties();
			cp.deleteMobileInternal();
			setCustomPropertiesInt(cp.toSource());
		}
	}

	protected final CustomProperties getCustomProperties(String customProperties)
	{
		JSONObject customPropertiesJSON = JSONParser.parseStrict(customProperties).isObject();
		if (customPropertiesJSON != null)
		{
			return (CustomProperties)customPropertiesJSON.getJavaScriptObject().cast();
		}
		return null;
	}

	protected final MobilePropertiesWrapper getMobileProperties(String customProperties)
	{
		CustomProperties cp = getCustomProperties(customProperties);
		if (cp != null) return cp.getMobile();
		return null;
	}

	static class CustomProperties extends JavaScriptObject
	{
		protected CustomProperties()
		{
		}

		public final MobilePropertiesWrapper getMobile()
		{
			MobileProperties mp = getMobileInternal();
			return mp == null ? null : new MobilePropertiesWrapper(this, mp);
		}

		public final native MobileProperties getMobileInternal() /*-{
			return this.mobile;
		}-*/;

		public final native void deleteMobileInternal() /*-{
			delete this.mobile;
		}-*/;

	}

	public static class MobilePropertiesWrapper
	{
		private final MobileProperties wrapped;
		private final CustomProperties parent;

		protected MobilePropertiesWrapper(CustomProperties parent, MobileProperties toWrap)
		{
			wrapped = toWrap;
			this.parent = parent;
		}

		public MobileProperties get()
		{
			return wrapped;
		}
	}

	public static class MobileProperties extends JavaScriptObject
	{
		protected MobileProperties()
		{
		}

		public final native void setMobileForm() /*-{
			this.mobileform = true;
		}-*/;

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

		public final native String getDataIcon() /*-{
			return this.dataIcon;
		}-*/;
	}
}
