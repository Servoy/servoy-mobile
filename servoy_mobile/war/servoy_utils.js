if (typeof(_ServoyUtils_) == "undefined") 
{
	_ServoyUtils_ = { };
	
   _ServoyUtils_.stack = new Array();
   
   _ServoyUtils_.getValue = function(name) {
     var scope = _ServoyUtils_.stack[_ServoyUtils_.stack.length-1];
     return _ServoyUtils_.getScopeVariable(scope,name);
   }
   
    _ServoyUtils_.setValue = function(name, value) {
    	var scope = _ServoyUtils_.stack[_ServoyUtils_.stack.length-1];
		return _ServoyUtils_.setScopeVariable(scope,name,value);
   }
   
   _ServoyUtils_.wrapFunction = function(func, scope) {
      return function() {
	   _ServoyUtils_.stack.push(scope);
       try {
          func.apply(scope,arguments);
       } finally {
        _ServoyUtils_.stack.pop();   
       }
     }
   }
   
   _ServoyUtils_.defineStandardFormVariables = function(form) {
   		_ServoyUtils_.defineVariable(form,"foundset");
   		_ServoyUtils_.defineVariable(form,"controller");
   		_ServoyUtils_.defineVariable(form,"elements");
   }
   
   _ServoyUtils_.defineVariable = function(object, name, defaultValue, type) {
		Object.defineProperty(object, name, {get: function() { return _ServoyUtils_.getScopeVariable(object,name);},
			                                 set: function(val) { _ServoyUtils_.setScopeVariable(object,name,val);} });
		if (type != undefined) _ServoyUtils_.setScopeVariableType(object,name,type);
		if (defaultValue != undefined) _ServoyUtils_.setScopeVariable(object,name,defaultValue); // set default value
   }
   
    _ServoyUtils_.defineWindowVariable = function(name) {
   			Object.defineProperty(window, name, {get: function() { return _ServoyUtils_.getValue(name);},
		   		                                 set: function(val) { _ServoyUtils_.setValue(name,val);} });
	}
 }