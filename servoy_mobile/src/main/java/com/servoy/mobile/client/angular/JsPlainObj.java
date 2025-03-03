package com.servoy.mobile.client.angular;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * This class represents a Plain Javascript Object i.e An object that
 * was created by calling new Object(). Typically in javascript they
 * are created as object literals using initializers e.g. var a = {prop: 10}
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class JsPlainObj
{

	/**
	 * Initialize a plain object with the specified property values. For example,
	 * $jsPlainObj("a",1, "b","somevalue) is eqivalent to the following javascript
	 *  { a: 1, b: "somevalue" }
	 *
	 * @param fieldValues a set of 1 or more (field name, value) pairs
	 * @return The initialzed plain object
	 */
	@JsOverlay
	public static JsPlainObj $jsPlainObj(Object... fieldValues)
	{
		return $(new JsPlainObj(), fieldValues);
	}

	/**
	 * Initialize a specified plain object with the specified property values. For example,
	 * $(new MyJsPlainObj(), "a",1, "b","somevalue) is eqivalent to the following javascript
	 *  { a: 1, b: "somevalue" }
	 *
	 *  @param <O> The type of plain object
	 *  @param jsPlainObj The object to initialize
	 *  @param fieldValues a set of 1 or more (field name, value) pairs
	 *  @return The initialzed plain object
	 */
	@JsOverlay
	public static <O> O $(O jsPlainObj, Object... fieldValues)
	{
		String fieldName = null;

		for (Object f : fieldValues)
		{
			if (fieldName == null)
				fieldName = (String)f;
			else
			{
				Js.asPropertyMap(jsPlainObj).set(fieldName, f);

				fieldName = null;
			}
		}

		return jsPlainObj;
	}

	/**
	 * Return an int parameter from this plain object. NOTE this is method is NOT type safe. If if you access
	 * a property that isn't an int you will get an undefined result. Also if you access a property
	 * that does not exist you will get an undefined error
	 *
	 * @param prop  The property to access
	 * @return      The integer property value
	 */
	@JsOverlay
	final public int getInt(String prop)
	{
		return Js.asPropertyMap(this).getAsAny(prop).asInt();
	}

	/**
	 * Return a double parameter from this plain object. NOTE this method is NOT type safe. If you access
	 * a property that isn't a double you will get an undefined result. Also if you access a property
	 * that does not exist you will get an undefined error
	 *
	 * @param prop  The property to access
	 * @return      The double property value
	 */
	@JsOverlay
	final public double getDbl(String prop)
	{
		return Js.asPropertyMap(this).getAsAny(prop).asDouble();
	}

	/**
	 * Return an boolean parameter from this plain object. NOTE this method is NOT type safe. If you access
	 * a property that isn't a boolean you will get an undefined result. Also if you access a property
	 * that does not exist you will get an undefined error
	 *
	 * @param prop  The property to access
	 * @return      The boolean property value
	 */
	@JsOverlay
	final public boolean getBool(String prop)
	{
		return Js.asPropertyMap(this).getAsAny(prop).asBoolean();
	}

	/**
	 * Return an String parameter from this plain object. NOTE this method is NOT type safe. If you access
	 * a property that isn't an String you will get an undefined result. Also if you access a property
	 * that does not exist you will get an undefined error
	 *
	 * @param prop  The property to access
	 * @return      The String property value
	 */
	@JsOverlay
	final public String getStr(String prop)
	{
		return Js.asPropertyMap(this).getAsAny(prop).asString();
	}

	/**
	 * Return an Object parameter from this plain object. NOTE this method is NOT type safe. If
	 * you access a property that isn't an Object you will get an undefined result. Also if you
	 * access a property that does not exist you will get an undefined error
	 *
	 * @param prop  The property to access
	 * @param <O>   The Object property value
	 * @return      The Object property value
	 */
	@JsOverlay
	final public <O> O getObj(String prop)
	{
		return Js.uncheckedCast(Js.asPropertyMap(this).get(prop));
	}

	/**
	 * Set the specified property on this plain object. The property will be added if it doesn't
	 * currently exist
	 *
	 * @param prop  The property to set
	 * @param v     The value to set the property to
	 */
	@JsOverlay
	final public void set(String prop, int v)
	{
		Js.asPropertyMap(this).set(prop, v);
	}

	/**
	 * Set the specified property on this plain object. The property will be added if it doesn't
	 * currently exist
	 *
	 * @param prop  The property to set
	 * @param v     The value to set the property to
	 */
	@JsOverlay
	final public void set(String prop, double v)
	{
		Js.asPropertyMap(this).set(prop, v);
	}

	/**
	 * Set the specified property on this plain object. The property will be added if it doesn't
	 * currently exist
	 *
	 * @param prop  The property to set
	 * @param v     The value to set the property to
	 */
	@JsOverlay
	final public void set(String prop, boolean v)
	{
		Js.asPropertyMap(this).set(prop, v);
	}

	/**
	 * Set the specified property on this plain object. The property will be added if it doesn't
	 * currently exist
	 *
	 * @param prop  The property to set
	 * @param v     The value to set the property to
	 */
	@JsOverlay
	final public void set(String prop, String v)
	{
		Js.asPropertyMap(this).set(prop, v);
	}

	/**
	 * Set the specified property on this plain object. The property will be added if it doesn't
	 * currently exist
	 *
	 * @param prop  The property to set
	 * @param <V> The type of value to set
	 * @param v The value to set the property to
	 */
	@JsOverlay
	final public <V> void set(String prop, V v)
	{
		Js.asPropertyMap(this).set(prop, v);
	}

	@JsOverlay
	final public String toJSONString()
	{
		return JSON.stringify(this);
	}

}