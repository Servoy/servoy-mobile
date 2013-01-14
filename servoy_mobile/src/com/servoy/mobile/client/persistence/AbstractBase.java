package com.servoy.mobile.client.persistence;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.servoy.j2db.persistence.constants.IContentSpecConstantsBase;
import com.servoy.j2db.scripting.solutionhelper.IMobileProperties;
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

	public final void setName(String name)
	{
		setAttributeValueString(IContentSpecConstantsBase.PROPERTY_NAME, name);
	}

	public final native int getTypeID() /*-{
		return this.typeid;
	}-*/;

	public final native JsArray<AbstractBase> getChildren() /*-{
		return this.items;
	}-*/;

	public final AbstractBase getChild(String uuid)
	{
		JsArray<AbstractBase> children = getChildren();
		for (int i = children.length() - 1; i >= 0; i--)
		{
			if (children.get(i).getUUID().equals(uuid)) return children.get(i);
		}
		return null;
	}

	public final native void removeChild(int index) /*-{
		this.items.splice(index, 1);
	}-*/;

//	will be useful when you realy want to clone/copy stuff using solution model
//	public void resetUUID()
//	{
//		setUUID(Utils.createStringUUID());
//	}
//
//	private final native void setUUID(String newUUID) /*-{
//		this.uuid = newUUID;
//	}-*/;

	public final native String getUUID() /*-{
		return this.uuid;
	}-*/;

	public final native void markAsCopy() /*-{
		this.clone = true;
	}-*/;

	public final native boolean isClone() /*-{
		return this.clone ? true : false;
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

	public final MobileProperties getMobileProperties()
	{
		String customProperties = getCustomPropertiesInt();
		if (customProperties != null)
		{
			return getMobileProperties(customProperties);
		}

		return null;
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

	public final MobileProperties getOrCreateMobileProperties()
	{
		String customProperties = getCustomPropertiesInt();
		if (customProperties == null)
		{
			return getMobileProperties("{\"mobile\":{}}"); //$NON-NLS-1$
		}
		else
		{
			CustomProperties cp = getCustomProperties(customProperties);
			return getOrCreateMobile(cp);
		}
	}

	public final void removeMobileProperties()
	{
		CustomProperties cp = getCustomProperties();
		cp.deleteMobileInternal();
		setCustomPropertiesInt(Utils.getJSONString(cp));
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

	private final MobileProperties getMobileProperties(String customProperties)
	{
		CustomProperties cp = getCustomProperties(customProperties);
		if (cp != null) return getMobile(cp);
		return null;
	}


	private final MobileProperties getOrCreateMobile(CustomProperties cp)
	{
		MobileProperties mpw = getMobile(cp);
		return mpw == null ? new MobileProperties(this, cp, cp.createMobile()) : mpw;
	}

	private final MobileProperties getMobile(CustomProperties cp)
	{
		MobilePropertiesInternal mp = cp.getMobileInternal();
		return mp == null ? null : new MobileProperties(this, cp, mp);
	}

	private static class CustomProperties extends JavaScriptObject
	{
		protected CustomProperties()
		{
		}

		private final native MobilePropertiesInternal createMobile() /*-{
			this.mobile = {};
		}-*/;

		private final native MobilePropertiesInternal getMobileInternal() /*-{
			return this.mobile;
		}-*/;

		public final native void deleteMobileInternal() /*-{
			delete this.mobile;
		}-*/;

	}

	public static class MobileProperties implements IMobileProperties
	{
		private final MobilePropertiesInternal wrapped;
		private final CustomProperties parent;
		private final AbstractBase base;

		protected MobileProperties(AbstractBase base, CustomProperties parent, MobilePropertiesInternal toWrap)
		{
			this.base = base;
			wrapped = toWrap;
			this.parent = parent;
		}

		private void save()
		{
			base.setCustomPropertiesInt(Utils.getJSONString(parent));
		}

		@Override
		public <T> void setPropertyValue(MobileProperty<T> property, T value)
		{
			if (property.defaultValue == value || (property.defaultValue != null && property.defaultValue.equals(value))) wrapped.deletePropertyInternal(property.propertyName);

			// add more here if more primitive types are used
			if (value instanceof Integer) wrapped.setPropertyValueInternal(property.propertyName, ((Integer)value).intValue());
			else if (value instanceof Boolean) wrapped.setPropertyValueInternal(property.propertyName, ((Boolean)value).booleanValue());
			else wrapped.setPropertyValueInternal(property.propertyName, value);

			save();
		}

		@Override
		public <T> T getPropertyValue(MobileProperty<T> property)
		{
			return wrapped.getPropertyValueInternal(property.propertyName, property.defaultValue);
		}

	}

	private static class MobilePropertiesInternal extends JavaScriptObject
	{
		protected MobilePropertiesInternal()
		{
		}

		private final native void setPropertyValueInternal(String propertyName, int value) /*-{
			this[propertyName] = value;
		}-*/;

		private final native void setPropertyValueInternal(String propertyName, boolean value) /*-{
			this[propertyName] = value;
		}-*/;

		private final native void setPropertyValueInternal(String propertyName, Object value) /*-{
			this[propertyName] = value;
		}-*/;

		private final native void deletePropertyInternal(String propertyName) /*-{
			delete this[propertyName];
		}-*/;


		private final native <T> T getPropertyValueInternal(String propertyName, T defaultValue) /*-{
			return $wnd.internal.Utils
					.wrapIfPrimitive(this[propertyName] ? this[propertyName]
							: defaultValue);
		}-*/;

//		public final native void setMobileForm() /*-{
//			this.mobileform = true;
//		}-*/;
//
//		public final native void setHeaderLeftButton() /*-{
//			this.headerLeftButton = true;
//		}-*/;
//
//		public final native boolean isHeaderLeftButton() /*-{
//			return this.headerLeftButton ? this.headerLeftButton : false;
//		}-*/;
//
//		public final native void setHeaderRightButton() /*-{
//			this.headerRightButton = true;
//		}-*/;
//
//		public final native boolean isHeaderRightButton() /*-{
//			return this.headerRightButton ? this.headerRightButton : false;
//		}-*/;
//
//		public final native void setHeaderText() /*-{
//			this.headerText = true;
//		}-*/;
//
//		public final native boolean isHeaderText() /*-{
//			return this.headerText ? this.headerText : false;
//		}-*/;
//
//		public final native boolean isFooterItem() /*-{
//			return this.footeritem ? this.footeritem : false;
//		}-*/;
//
//		public final native void setFooterItem() /*-{
//			this.footeritem = true;
//		}-*/;
//
//		public final native boolean isFormTabPanel() /*-{
//			return this.formtabpanel ? this.formtabpanel : false;
//		}-*/;
//
//		public final native boolean isListTabPanel() /*-{
//			return this.list ? this.list : false;
//		}-*/;
//
//		public final native boolean isListItemButton() /*-{
//			return this.listitemButton ? this.listitemButton : false;
//		}-*/;
//
//		public final native boolean isListItemSubtext() /*-{
//			return this.listitemSubtext ? this.listitemSubtext : false;
//		}-*/;
//
//		public final native boolean isListItemCount() /*-{
//			return this.listitemCount ? this.listitemCount : false;
//		}-*/;
//
//		public final native boolean isListItemImage() /*-{
//			return this.listitemImage ? this.listitemImage : false;
//		}-*/;
//
//		public final native boolean isListItemHeader() /*-{
//			return this.listitemHeader ? this.listitemHeader : false;
//		}-*/;
//
//		public final native int getHeaderSize() /*-{
//			return this.headerSize ? this.headerSize : 4;
//		}-*/;
//
//		public final native int getRadioStyle() /*-{
//			return this.radioStyle ? this.radioStyle : 0;
//		}-*/;
//
//		public final native void setDataIcon(String di) /*-{
//			this.dataIcon = di;
//		}-*/;
//
//		public final native String getDataIcon() /*-{
//			return this.dataIcon;
//		}-*/;
	}
}
