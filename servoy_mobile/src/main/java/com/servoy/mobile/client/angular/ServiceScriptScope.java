package com.servoy.mobile.client.angular;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceScriptScope
{
	private final String serviceName;
	private final AngularBridge angularBridge;
	private final JavaScriptObject scope;

	public ServiceScriptScope(String serviceName, AngularBridge angularBridge)
	{
		this.serviceName = serviceName;
		this.angularBridge = angularBridge;
		this.scope = createScope(serviceName, angularBridge);
	}

	public JavaScriptObject getScope()
	{
		return scope;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public native void initializeWithScript(JavaScriptObject scriptFn) /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		scriptFn(s);
	}-*/;

	public native boolean hasApiFunction(String methodName) /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		return typeof s.api['_local_' + methodName] === 'function';
	}-*/;

	public native Object invokeApiFunction(String methodName, Object[] args) /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		var fn = s.api['_local_' + methodName];
		if (!fn) return null;
		if (args == null || args.length === 0) {
			return fn.call(s);
		}
		return fn.apply(s, args);
	}-*/;

	public native boolean hasInternalFunction(String methodName) /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		return typeof s[methodName] === 'function';
	}-*/;

	public native Object invokeInternalFunction(String methodName, Object[] args) /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		var fn = s[methodName];
		if (!fn) return null;
		if (args == null || args.length === 0) {
			return fn.call(s);
		}
		return fn.apply(s, args);
	}-*/;

	public void flushChanges()
	{
		flushModelChanges();
	}

	public native boolean hasPendingChanges() /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		return s._dirtyProps_ && Object.keys(s._dirtyProps_).length > 0;
	}-*/;

	private native void flushModelChanges() /*-{
		var s = this.@com.servoy.mobile.client.angular.ServiceScriptScope::scope;
		var dirtyProps = s._dirtyProps_;
		if (!dirtyProps) return;
		var keys = Object.keys(dirtyProps);
		if (keys.length === 0) return;
		var modelData = s._modelData_;
		var bridge = this.@com.servoy.mobile.client.angular.ServiceScriptScope::angularBridge;
		var svcName = this.@com.servoy.mobile.client.angular.ServiceScriptScope::serviceName;
		for (var i = 0; i < keys.length; i++) {
			var prop = keys[i];
			var converted = s._convertForWire_(modelData[prop], prop);
			bridge.@com.servoy.mobile.client.angular.AngularBridge::sendServiceModelChange(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)(svcName, prop, converted);
		}
		var dkeys = Object.keys(dirtyProps);
		for (var i = 0; i < dkeys.length; i++) {
			delete dirtyProps[dkeys[i]];
		}
	}-*/;

	private static native JavaScriptObject createScope(String serviceName, AngularBridge bridge) /*-{
		var scope = {};
		var modelData = {};
		var apiLocal = {};
		var dirtyProps = {};
		var versionCounters = {};

		var specData = $wnd._servicespecdata_ && $wnd._servicespecdata_[serviceName];
		var specModel = specData && specData.model;
		var specTypes = specData && specData.types;

		var mobileClient = bridge.@com.servoy.mobile.client.angular.AngularBridge::getMobileClient()();

		function getPropertySpec(propName, typeContext) {
			if (!typeContext) return null;
			var propSpec = typeContext[propName];
			if (!propSpec) return null;
			return propSpec;
		}

		function getPropertyType(propSpec) {
			if (!propSpec) return null;
			if (typeof propSpec === 'string') return propSpec;
			return propSpec.type || null;
		}

		function getTypeModel(typeName) {
			if (!specTypes || !specTypes[typeName]) return null;
			return specTypes[typeName].model || null;
		}

		function getVersion(propPath) {
			if (!versionCounters[propPath]) {
				versionCounters[propPath] = 1;
			}
			return ++versionCounters[propPath];
		}

		function convertForWire(val, typeName, propSpec) {
			if (val === null || val === undefined) return val;
			if (typeof val === 'function') return null;
			if (typeof val !== 'object') return val;
			if (Array.isArray(val)) {
				var arr = [];
				for (var i = 0; i < val.length; i++) {
					arr.push(convertForWire(val[i], null, null));
				}
				return arr;
			}
			if (typeName) {
				var converted = mobileClient.@com.servoy.mobile.client.MobileClient::convertValueForType(Ljava/lang/String;Ljava/lang/Object;Lcom/servoy/mobile/client/ui/PropertySpec;)(typeName, val, propSpec);
				if (converted !== val) {
					return converted;
				}
			}
			var typeModel = typeName ? getTypeModel(typeName) : null;
			var result = {};
			var keys = Object.keys(val);
			for (var i = 0; i < keys.length; i++) {
				var k = keys[i];
				var subPropSpec = typeModel ? getPropertySpec(k, typeModel) : null;
				var subType = getPropertyType(subPropSpec);
				var converted = convertForWire(val[k], subType, subPropSpec);
				if (converted !== null && converted !== undefined) {
					result[k] = converted;
				}
			}
			if (typeModel) {
				return { "vEr": getVersion(typeName), "v": result };
			}
			return result;
		}

		function createNestedProxy(parentProp, modelStorage, angBridge, svcName) {
			return new Proxy(modelStorage[parentProp], {
				get: function(target, prop) {
					return target[prop];
				},
				set: function(target, prop, value) {
					target[prop] = value;
					dirtyProps[parentProp] = true;
					return true;
				}
			});
		}

		scope.model = new Proxy(modelData, {
			get: function(target, prop) {
				var val = target[prop];
				if (val !== null && val !== undefined && typeof val === 'object') {
					return createNestedProxy(prop, target, bridge, serviceName);
				}
				return val;
			},
			set: function(target, prop, value) {
				target[prop] = value;
				dirtyProps[prop] = true;
				return true;
			}
		});

		scope.api = new Proxy(apiLocal, {
			get: function(target, prop) {
				if (prop.indexOf && prop.indexOf('_local_') === 0) {
					return target[prop];
				}
				if (typeof target['_local_' + prop] === 'function') {
					return target['_local_' + prop];
				}
				return function() {
					var args = Array.prototype.slice.call(arguments);
					bridge.@com.servoy.mobile.client.angular.AngularBridge::executeServiceCall(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)(serviceName, prop, args);
				};
			},
			set: function(target, prop, value) {
				target['_local_' + prop] = value;
				return true;
			}
		});

		scope._dirtyProps_ = dirtyProps;
		scope._modelData_ = modelData;
		scope._convertForWire_ = function(val, propName) {
			var propSpec = specModel ? getPropertySpec(propName, specModel) : null;
			var propType = getPropertyType(propSpec);
			return convertForWire(val, propType, propSpec);
		};

		return scope;
	}-*/;
}
