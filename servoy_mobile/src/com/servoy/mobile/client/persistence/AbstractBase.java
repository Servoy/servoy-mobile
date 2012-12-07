package com.servoy.mobile.client.persistence;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.servoy.mobile.client.util.Utils;

/**
 *
 * @author gboros
 */
public abstract class AbstractBase extends JavaScriptObject
{
	protected AbstractBase()
	{
	}

	public final native String getName() /*-{
		return this.name;
	}-*/;

	protected final native void setAttributeValueInt(String attrName, int value) /*-{
		this[attrName] = value;
	}-*/;

	protected final native void setAttributeValueString(String attrName, String value) /*-{
		this[attrName] = value;
	}-*/;

	public final native void setAttributeValueBoolean(String attrName, boolean value) /*-{
		this[attrName] = value;
	}-*/;

	protected final native String getAttributeValueString(String attrName, String defaultValue) /*-{
		return this[attrName] ? this[attrName] : defaultValue;
	}-*/;

	protected final native int getAttributeValueInt(String attrName, int defaultValue) /*-{
		return this[attrName] ? this[attrName] : defaultValue;
	}-*/;

	public final native boolean getAttributeValueBoolean(String attrName, boolean defaultValue) /*-{
		return this[attrName] != undefined ? this[attrName] : defaultValue;
	}-*/;

	protected final native String getCustomPropertiesInt() /*-{
		return this.customProperties;
	}-*/;

	protected final native void setCustomPropertiesInt(String customProperties) /*-{
		if (customProperties != null)
			this.customProperties = customProperties;
		else
			delete (this.customProperties);
	}-*/;

	protected final MobilePropertiesWrapper getMobilePropertiesWrapper()
	{
		String customProperties = getCustomPropertiesInt();
		if (customProperties != null)
		{
			return getMobileProperties(customProperties);
		}

		return null;
	}

	public final MobileProperties getMobilePropertiesCopy()
	{
		MobilePropertiesWrapper x = getMobilePropertiesWrapper();
		return x == null ? null : x.get();
	}

	public final void setCustomProperties(CustomProperties cp)
	{
		if (cp != null) setCustomPropertiesInt(Utils.getJSONString(cp));
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

	public final MobilePropertiesWrapper getOrCreateMobilePropertiesCopy()
	{
		String customProperties = getCustomPropertiesInt();
		if (customProperties == null)
		{
			return getMobileProperties("{\"mobile\":{}}"); //$NON-NLS-1$
		}
		else
		{
			CustomProperties cp = getCustomProperties(customProperties);
			return cp.getOrCreateMobile();
		}
	}

	public final void setMobileProperties(MobilePropertiesWrapper mp)
	{
		if (mp != null) setCustomPropertiesInt(Utils.getJSONString(mp.parent));
		else
		{
			CustomProperties cp = getCustomProperties();
			cp.deleteMobileInternal();
			setCustomPropertiesInt(Utils.getJSONString(cp));
		}
	}

	private final CustomProperties getCustomProperties(String customProperties)
	{
		JSONObject customPropertiesJSON = JSONParser.parseStrict(customProperties).isObject();
		if (customPropertiesJSON != null)
		{
			return (CustomProperties)customPropertiesJSON.getJavaScriptObject().cast();
		}
		return null;
	}

	private final MobilePropertiesWrapper getMobileProperties(String customProperties)
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

		public final MobilePropertiesWrapper getOrCreateMobile()
		{
			MobilePropertiesWrapper mpw = getMobile();
			return mpw == null ? new MobilePropertiesWrapper(this, createMobile()) : mpw;
		}

		public final MobilePropertiesWrapper getMobile()
		{
			MobileProperties mp = getMobileInternal();
			return mp == null ? null : new MobilePropertiesWrapper(this, mp);
		}

		public final native MobileProperties createMobile() /*-{
			this.mobile = {};
		}-*/;

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

		public final native void setHeaderLeftButton() /*-{
			this.headerLeftButton = true;
		}-*/;

		public final native boolean isHeaderLeftButton() /*-{
			return this.headerLeftButton ? this.headerLeftButton : false;
		}-*/;

		public final native void setHeaderRightButton() /*-{
			this.headerRightButton = true;
		}-*/;

		public final native boolean isHeaderRightButton() /*-{
			return this.headerRightButton ? this.headerRightButton : false;
		}-*/;

		public final native void setHeaderText() /*-{
			this.headerText = true;
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

		public final native void setDataIcon(String di) /*-{
			this.dataIcon = di;
		}-*/;

		public final native String getDataIcon() /*-{
			return this.dataIcon;
		}-*/;
	}
}
