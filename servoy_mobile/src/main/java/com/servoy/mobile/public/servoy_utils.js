if (typeof(_ServoyUtils_) == "undefined") 
{
	_ServoyUtils_ = { };

	_ServoyUtils_.stack = new Array();

	_ServoyUtils_.getValue = function(name) {
		var scope = _ServoyUtils_.stack[_ServoyUtils_.stack.length-1];
		return _ServoyUtils_.getScopeVariable(scope, name);
	}

	_ServoyUtils_.setValue = function(name, value) {
		var scope = _ServoyUtils_.stack[_ServoyUtils_.stack.length-1];
		return _ServoyUtils_.setScopeVariable(scope, name, value);
	}

	_ServoyUtils_.wrapFunction = function(func, scope) {
		if(!scope["_destroyCallbackFunctions"]) scope["_destroyCallbackFunctions"] = [];
		scope["_destroyCallbackFunctions"].push(function() {
			func = null;
			scope = null;
		});
		var wrapper = function() {
			_ServoyUtils_.stack.push(scope);
			try {
				return func.apply(scope, arguments);
			} finally {
				_ServoyUtils_.stack.pop();   
			}
		}
		wrapper.realFunction = func;
		return wrapper; 
	}

	_ServoyUtils_.clearScope = function(scope) {
		if(scope._destroyCallbackFunctions)
		{
			for (var i = 0; i < scope._destroyCallbackFunctions.length; i++) {
				scope._destroyCallbackFunctions[i]();
			}
		}
	}
	
	_ServoyUtils_.defineStandardFormVariables = function(form) {
		_ServoyUtils_.defineVariable(form, "foundset");
		_ServoyUtils_.defineVariable(form, "controller");
		_ServoyUtils_.defineVariable(form, "elements");
	}

	_ServoyUtils_.defineVariable = function(scope, name, defaultValue, type) {
		if(!scope["_destroyCallbackFunctions"]) scope["_destroyCallbackFunctions"] = [];
		scope["_destroyCallbackFunctions"].push(function() {
			scope = null;
		});
		Object.defineProperty(scope, name, {get: function() { return _ServoyUtils_.getScopeVariable(scope, name);},
			set: function(val) { _ServoyUtils_.setScopeVariable(scope, name, val);},
			configurable : true });
		if (type != undefined) _ServoyUtils_.setScopeVariableType(scope, name, type);
		if (defaultValue != undefined) _ServoyUtils_.setScopeVariable(scope, name, defaultValue); // set default value
	}

	_ServoyUtils_.defineRedirectVariable = function(scope,javascriptObject, name) {
		if(!scope["_destroyCallbackFunctions"]) scope["_destroyCallbackFunctions"] = []; 
		scope["_destroyCallbackFunctions"].push(function() {
			scope = null;
		});
		Object.defineProperty(javascriptObject, name, {get: function() { return _ServoyUtils_.getScopeVariable(scope, name);},
			set: function(val) { _ServoyUtils_.setScopeVariable(scope, name, val);},
			configurable : true });
	}
	_ServoyUtils_.definedWindowVariables = new Array();
	_ServoyUtils_.persistentDefinedWindowVariables = new Array();
	_ServoyUtils_.defineWindowVariable = function(name,persistent) {
	    if (!(name in window)) {
	    	if (persistent)
	    	{
	    		_ServoyUtils_.persistentDefinedWindowVariables.push(name);
	    	}
	    	else
	    	{
	    		_ServoyUtils_.definedWindowVariables.push(name);
	    	}
			Object.defineProperty(window, name, {get: function() { return _ServoyUtils_.getValue(name);},
				set: function(val) { _ServoyUtils_.setValue(name, val);},
				configurable : true});
		}
		else if (_ServoyUtils_.definedWindowVariables.indexOf(name) == -1 && _ServoyUtils_.persistentDefinedWindowVariables.indexOf(name) == -1){
			_ServoyUtils_.error("window variable: '" + name + "' is already defined, skipping this dataprovider/relationname");
		}
	}
	_ServoyUtils_.clearWindowVariables = function() {
		for (var i=0; i < _ServoyUtils_.definedWindowVariables.length; i++) {
		   delete window[_ServoyUtils_.definedWindowVariables[i]];
		}
		_ServoyUtils_.definedWindowVariables.length = 0;
	}
	_ServoyUtils_.simulateClick = function (target, options) {

		var event = target.ownerDocument.createEvent('MouseEvents'),
		options = options || {};

		//Set your default options to the right of ||
		var opts = {
				type: options.type                  || 'click',
				canBubble:options.canBubble             || true,
				cancelable:options.cancelable           || true,
				view:options.view                       || target.ownerDocument.defaultView, 
				detail:options.detail                   || 1,
				screenX:options.screenX                 || 0, //The coordinates within the entire page
				screenY:options.screenY                 || 0,
				clientX:options.clientX                 || 0, //The coordinates within the viewport
				clientY:options.clientY                 || 0,
				ctrlKey:options.ctrlKey                 || false,
				altKey:options.altKey                   || false,
				shiftKey:options.shiftKey               || false,
				metaKey:options.metaKey                 || false, //I *think* 'meta' is 'Cmd/Apple' on Mac, and 'Windows key' on Win. Not sure, though!
				button:options.button                   || 0, //0 = left, 1 = middle, 2 = right
				relatedTarget:options.relatedTarget     || null
		};

		//Pass in the options
		event.initMouseEvent(
				opts.type,
				opts.canBubble,
				opts.cancelable,
				opts.view, 
				opts.detail,
				opts.screenX,
				opts.screenY,
				opts.clientX,
				opts.clientY,
				opts.ctrlKey,
				opts.altKey,
				opts.shiftKey,
				opts.metaKey,
				opts.button,
				opts.relatedTarget
		);

		//Fire the event
		target.dispatchEvent(event);
	}
	
	_ServoyUtils_.loadMediaResources = function () {
		if(mediaCSS && mediaCSS.length)
		{
			var cssLink;
			for(var i = 0; i < mediaCSS.length; i++)
			{
				cssLink = document.createElement("link");
				cssLink.setAttribute("rel", "stylesheet");
				cssLink.setAttribute("type", "text/css");
				cssLink.setAttribute("href", "media/" + mediaCSS[i]);
				document.getElementsByTagName("head")[0].appendChild(cssLink);	
			}	
		}
		if(mediaJS && mediaJS.length)
		{
			var jsScript;
			for(var i = 0; i < mediaJS.length; i++)
			{
				jsScript = document.createElement("script");
				jsScript.setAttribute("language", "javascript");
				jsScript.setAttribute("type", "text/javascript");
				jsScript.setAttribute("src", "media/" + mediaJS[i]);
				document.getElementsByTagName("head")[0].appendChild(jsScript);	
			}	
		}		
	}
}