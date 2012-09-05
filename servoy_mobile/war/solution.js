if (typeof(_ServoyInit_) == "undefined") {
  _ServoyInit_ = {
	init: function() {
			// create forms scope
			forms = { };
			
			// define all the forms
			Object.defineProperty(forms, "offline_data", {get: function() {
				var form = _ServoyUtils_.getFormScope("offline_data");
				if (!form._initialized_) {
					var onAction = _ServoyUtils_.wrapFunction(function (event) {
	globals.myglobaltest(event);
}
,form);
			        form.onAction = onAction;
					

					// define standard things (controller,foundset,elements)
					_ServoyUtils_.defineStandardFormVariables(form);
										
			        form._initialized_ = true;
			     }
			     return form;
			  }, configurable: false});
			
		    // create top level global scope.
			scopes = { };
			
			var createScope = function() {
				scopes.globals = _ServoyUtils_.getGlobalScope("globals");
				
				var myglobaltest = _ServoyUtils_.wrapFunction( function (x) {
	application.output("test: " + x);
}
 ,scopes.globals);
				scopes.globals.myglobaltest = myglobaltest;
				
				
				return 
			};
			createScope();

			var createScope = function() {
				scopes.ascope = _ServoyUtils_.getGlobalScope("ascope");
				
				var test = _ServoyUtils_.wrapFunction( function () {
	globals.myglobaltest(x);
}
 ,scopes.ascope);
				scopes.ascope.test = test;
				
				_ServoyUtils_.defineVariable(scopes.ascope,"x",10,35);
				
				return 
			};
			createScope();
			
			// support the 'old' notation "globals.xxx" without scopes.
			globals = scopes.globals;

			// define all variables on the toplevel, scope and form
			// also include all the servoy scripting scope variables: "foundset","controller","elements","currentcontroller"
			var windowVariablesArray = ["x","foundset","controller","elements","currentcontroller"];
			for(var i = 0;i<windowVariablesArray.length;i++) {
				_ServoyUtils_.defineWindowVariable(windowVariablesArray[i]);
			}
		}	
	}
}