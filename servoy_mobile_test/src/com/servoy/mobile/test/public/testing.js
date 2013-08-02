if (typeof(_ServoyTesting_) == "undefined") {
	
	
	_ServoyTesting_ = {
		SCOPE_NAME_SEPARATOR : "_sNS_"	// this constant is also defined in MobileExporter.java and TestSuiteController.java; please update those as well if you change the value
	};
	
	// used for locating the origin of a function inside and AssertionFailedError
	_ServoyInit_.getFunctionStart = function (s1Name, s2Name, fName) {
		return "function " + s1Name + _ServoyTesting_.SCOPE_NAME_SEPARATOR + s2Name + _ServoyTesting_.SCOPE_NAME_SEPARATOR + fName;
	};
	
}