if (typeof(_ServoyInit_) == "undefined") {
  _ServoyInit_ = {
	init: function() {
			// create forms scope
			forms = { };
			
			// define all the forms
			Object.defineProperty(forms, "companies", {get: function() {
				return _ServoyUtils_.getFormScope("companies");
			  }, configurable: false});
			Object.defineProperty(forms, "companies_list", {get: function() {
				return _ServoyUtils_.getFormScope("companies_list");
			  }, configurable: false});
			Object.defineProperty(forms, "contact", {get: function() {
				return _ServoyUtils_.getFormScope("contact");
			  }, configurable: false});
			Object.defineProperty(forms, "contact_edit", {get: function() {
				return _ServoyUtils_.getFormScope("contact_edit");
			  }, configurable: false});
			Object.defineProperty(forms, "contacts", {get: function() {
				return _ServoyUtils_.getFormScope("contacts");
			  }, configurable: false});
			Object.defineProperty(forms, "listy", {get: function() {
				return _ServoyUtils_.getFormScope("listy");
			  }, configurable: false});
			Object.defineProperty(forms, "solution_model_start", {get: function() {
				return _ServoyUtils_.getFormScope("solution_model_start");
			  }, configurable: false});
			
		    // create top level global scope.
			scopes = { };
			
			// define all scopes			
			Object.defineProperty(scopes, "globals", {get: function() {
				return _ServoyUtils_.getGlobalScope("globals");
			  }, configurable: false});
			
			// support the 'old' notation "globals.xxx" without scopes. (and init the standard globals directly)
			globals = scopes.globals;

			// define all variables on the toplevel, scope and form
			// also include all the servoy scripting scope variables: "foundset","controller","elements","currentcontroller"
			var windowVariablesArray = ["var2","var1","var3","gvar1","foundset","controller","elements","currentcontroller"];
			for(var i = 0;i<windowVariablesArray.length;i++) {
				_ServoyUtils_.defineWindowVariable(windowVariablesArray[i]);
			}
		},
		
		forms: {
			companies : {
				fncs: {
					miau : "function (event, argu1, argu2) {\n\talert(\"UN MODIFIED\");\n\talert(typeof argu1);\n\talert(typeof argu2);\n\talert(argu2 + 10);\n}",
					onAction : "function (event) {\n\tvar f = solutionModel.getForm('companies');\n\tvar rf = f.getField(\"gugu\");\n\tplugins.mobile.solutionHelper.setRadioFieldHorizontal(rf,false);\n\t\n}",
					onDetailAction : "function (event) \n{\n\tforms.contacts.showContacts(accountmanager_to_companies.companies_to_contacts);\n}",
					onShow : "function (firstShow, event) {\n\tif (foundset.loadRecords) foundset.loadRecords(39)\n}",
					onSyncAction : "function (event) \n{\n\tplugins.mobile.syncData();\n}",
					showSMForm : "function (event, xArg, yArg) {\n\tforms.solution_model_start.controller.show();\n}"
				},
				vrbs: {
					var2 : ["3", 35],
					var1 : ["'miau'", 35]
				}
			},
			companies_list : {
				fncs: {

				},
				vrbs: {

				}
			},
			contact : {
				fncs: {
					onBackAction : "function (event) \n{\n\tvar history = _ServoyUtils_.history;\n\thistory.back();\n}",
					onCall : "function (event) {\n\tvar rec = foundset.getSelectedRecord();\n\tif(rec.phone_direct) plugins.mobile.call(rec.phone_direct);\n\telse plugins.dialogs.showWarningDialog(\"Warning\", \"Missing phone number\");\n}",
					onEditAction : "function (event) \n{\n\tforms.contact_edit.showContact(foundset);\n}",
					onEmail : "function (event) {\n\tvar rec = foundset.getSelectedRecord();\n\tif(rec.email) plugins.mobile.email(rec.email);\n\telse plugins.dialogs.showWarningDialog(\"Warning\", \"Missing email address\");\n}",
					showContact : "function (fs) \n{\n\tcontroller.showRecords(fs)\n}"
				},
				vrbs: {
					var3 : ["1 + 2 + 3", 35]
				}
			},
			contact_edit : {
				fncs: {
					onBackAction : "function (event) \n{\n\tvar history = _ServoyUtils_.history;\n\thistory.back();\n}",
					onSaveAction : "function (event) \n{\n\tvar history = _ServoyUtils_.history;\n\tdatabaseManager.saveData();\n\thistory.back();\n}",
					showContact : "function (fs) \n{\n\tcontroller.showRecords(fs)\n}",
					test : "function (event) \n{\n\tplugins.dialogs.showWarningDialog(\"Check\",\"Are we online: \"+plugins.mobile.isOnline());\n}"
				},
				vrbs: {

				}
			},
			contacts : {
				fncs: {
					back : "function (event) \n{\n\tvar history = _ServoyUtils_.history;\n\thistory.back()\n}",
					onDetailAction : "function (event) \n{\n\tforms.contact.showContact(foundset);\n}",
					onDetailsAction : "function (event) \n{\n\tforms.contact.showContact(foundset);\n}",
					onNewAction : "function (event) \n{\n\tfoundset.newRecord();\n\tforms.contact_edit.showContact(foundset);\n}",
					showContacts : "function (fs) \n{\n\tcontroller.showRecords(fs);\n}"
				},
				vrbs: {

				}
			},
			listy : {
				fncs: {

				},
				vrbs: {

				}
			},
			solution_model_start : {
				fncs: {
					createAndShowForm : "function (event) {\n\totherMethod();\n}",
					deleteCretedForms : "function (event) {\n\tsolutionModel.removeForm('created_by_sm_1');\n\tsolutionModel.removeForm('created_by_sm_2');\n\tsolutionModel.removeForm('created_by_sm_3');\n}",
					deletePrevious : "function (event) {\n\tsolutionModel.removeForm('companies');\n\tforms.companies.controller.show(); // should error out\n}",
					goBack : "function (event) {\n\tvar history = _ServoyUtils_.history;\n\thistory.back();\n}",
					modifyExisting : "function (event) {\n\tvar history = _ServoyUtils_.history;\n\thistory.removeForm('companies');\n\tvar f = solutionModel.getForm('companies');\n\tvar met = f.getMethod('miau');\n\tmet.code = \"function miau() { outp('changed'); }\";\n\tf.newButton(\"added with SM\",0,5000,0,0,met);\n\tforms.companies.controller.show();\n}",
					otherMethod : "function ()\n{\n\tvar history = _ServoyUtils_.history;\n\tvar f = solutionModel.getForm(\"created_by_sm_1\");\n\tif (f == null) {\n\t\toutp('Will create form');\n\t\tf = solutionModel.newForm(\"created_by_sm_1\",\"udm\",\"contacts\",null,false,100,380);\n\t\t\n\t\t// create 3 normal buttons, each one doing a browser alert from a different kind of method\n\t\t// notice that x location and sized are not used, y location determines the order of these components\n\t\tf.newButton(\"use existing global method\",0,0,10,10,\n\t\t\tsolutionModel.getGlobalMethod(\"globals\",\"appOutput\"));\n\t\tf.newButton(\"use new scope method\",0,1,10,10,\n\t\t\tsolutionModel.newGlobalMethod(\"newScope\",\"function appOutputus() { outp(\\\"This is a SM created SCOPE method.\\\"); }\"));\n\t\tf.newButton(\"use new form method\",0,2,10,10,\n\t\t\t\tf.newMethod(\"function appOutput() { outp(\\\"This is a SM created FORM method.\\\"); }\"));\n\t\t\n\t\t// create a text field linked to a DB dataprovider and a password field linked to a newly created form variable\n\t\t// and two buttons - one that modifies the field's dataprovider and another that checks the values in the two fields for equality\n\t\tvar b = f.newButton(\"change field value\",0,3,10,10,\n\t\t\t\t\tf.newMethod(\"function changeFieldValue() { foundset.getSelectedRecord().name_first = foundset.getSelectedRecord().name_first + '*'; }\"));\n\t\tf.newTextField(\"name_first\",0,4,10,10);\n\t\tvar localvar = f.newVariable(\"pwdVar\",JSVariable.TEXT,\"'dummy'\");\n\t\tf.newPassword(localvar,0,5,10,10);\n\t\tb = f.newButton(\"check password equals text field\",0,6,10,10,\n\t\t\t\tf.newMethod(\"function checkPwd() { outp('Are equal: ' + (foundset.getSelectedRecord().name_first == pwdVar) + '. Values: ' + foundset.getSelectedRecord().name_first + ' | ' + pwdVar); }\"));\n\t\tplugins.mobile.solutionHelper.setIconType(b,plugins.mobile.SolutionHelper.ICON_SEARCH);\n\t\t\n\t\t// create a compound mobile field made of a label and a text field\n\t\toutp(\"Component count before add\" + \" : \" + f.getComponents().length)\n\t\toutp(\"Label count before add\"+  \" : \" + f.getLabels().length)\n\t\tvar fld = f.newTextField(\"name_first\",1,7,10,10);\n//\t\tvar lbl = f.newLabel(\"compound\",0,7,10,10);\n//\t\tplugins.mobile.solutionHelper.groupComponents(lbl, fld);\n\t\tplugins.mobile.solutionHelper.setTitleText(fld,\"First Name:\");\n\t\tfld.name = 'comp';\n\t\toutp(\"Component count after add\" + \" : \" + f.getComponents().length)\n\t\toutp(\"Label count after add\"+  \" : \" + f.getLabels().length)\n\n\t\t// create a compound mobile label made of a label and a label\n\t\toutp(\"Component count before add\" + \" : \" + f.getComponents().length)\n\t\toutp(\"Label count before add\"+  \" : \" + f.getLabels().length)\n\t\tvar label = f.newLabel(\"CIAOU\",0,0,0,0);\n//\t\tvar lbl = f.newLabel(\"compound\",0,7,10,10);\n//\t\tplugins.mobile.solutionHelper.groupComponents(lbl, fld);\n//\t\tplugins.mobile.solutionHelper.setTitleText(label,\"Second Name:\");\n//\t\tplugins.mobile.solutionHelper.setTitleVisible(label, false);\n\t\tlabel.name = 'complabel';\n\t\toutp(\"Component count after add\" + \" : \" + f.getComponents().length)\n\t\toutp(\"Label count after add\"+  \" : \" + f.getLabels().length)\n\n\t\t// create an inset list\n\t\tvar insetList = plugins.mobile.solutionHelper.createInsetList(f,8,\"accountmanager_to_companies\",\"Companies\",\"company_name\");\n\t\tinsetList.subtextDataProviderID = \"company_description\";\n\t\tinsetList.onAction = f.newMethod(\"function alertMeFromList() { outp('inset list clicked'); }\");\n\t\tinsetList.name = 'il1';\n\t\t\n\t\tinsetList = plugins.mobile.solutionHelper.getInsetList(f, 'il1');\n\t\tvar newInsetList = plugins.mobile.solutionHelper.createInsetList(f, 9, insetList.relationName, insetList.headerText, insetList.textDataProviderID);\n\t\tnewInsetList.name = 'il2';\n\t\t\n\t\t// create a list form and a button to go to it\n\t\tb = f.newButton(\"Show created list form\",0,10,10,10,\n\t\t\tf.newMethod(\"function showListForm() { forms.created_by_sm_2.controller.show(); }\"));\n\t\tvar list = plugins.mobile.solutionHelper.createListForm('created_by_sm_2', databaseManager.getDataSource(\"udm\",\"contacts\"),\"name_first\");\n\t\tlist.onAction = list.getForm().newMethod(\"function goBack() { var history = _ServoyUtils_.history; history.back(); }\");\n\t\t\n\t\tb = f.newButton(\"Show duplicated list form\",0,11,10,10,\n\t\t\tf.newMethod(\"function showDuplicatedListForm() { forms.created_by_sm_3.controller.show(); }\"));\n\t\tlist = plugins.mobile.solutionHelper.getListForm('created_by_sm_2');\n\t\tvar newList = plugins.mobile.solutionHelper.createListForm('created_by_sm_3', list.getForm().dataSource, list.textDataProviderID);\n\t\tnewList.onAction = newList.getForm().newMethod(list.onAction.code);\n\t\t\n\t\t// header/footer stuff\n\t\tvar backMethod;\n\t\tb = f.newButton(\"back\",0,0,10,10,backMethod = f.newMethod(\"function goBack() { var history = _ServoyUtils_.history; history.back(); }\"));\n\t\tplugins.mobile.solutionHelper.markLeftHeaderButton(b);\n\t\tplugins.mobile.solutionHelper.setIconType(b,plugins.mobile.SolutionHelper.ICON_BACK);\n\t\tb = f.newButton(\"forward\",0,0,10,10,f.newMethod(\"function goForward() { /* todo */ }\"));\n\t\tplugins.mobile.solutionHelper.markRightHeaderButton(b);\n\t\tplugins.mobile.solutionHelper.setIconType(b,plugins.mobile.SolutionHelper.ICON_FORWARD);\n\t\tvar lbl = f.newLabel(\"Generated form\",0,0,10,10);\n\t\tplugins.mobile.solutionHelper.markHeaderText(lbl);\n\t\tb = f.newButton(\"ok\",0,0,10,10,backMethod);\n\t\tplugins.mobile.solutionHelper.setIconType(b,plugins.mobile.SolutionHelper.ICON_CHECK);\n\t\tplugins.mobile.solutionHelper.markFooterItem(b);\n\t\t\n//\t\toutp(\"SM_ALIGNMENT.DEFAULT: \" + SM_ALIGNMENT.DEFAULT + '\\n' +\n//\t\t\"SM_ALIGNMENT.ACCORDION_PANEL: \" + SM_ALIGNMENT.ACCORDION_PANEL + '\\n' +\n//\t\t\"SM_DEFAULTS.DEFAULT: \" + SM_DEFAULTS.DEFAULT + '\\n' +\n//\t\t\"JSField.PASSWORD: \" + JSField.PASSWORD + '\\n' +\n//\t\t\"JSField.HTML_AREA: \" + JSField.HTML_AREA + '\\n');\n\t}\n\tforms[\"created_by_sm_1\"].controller.show();\n}",
					outp : "function (s)\n{\n//\talert(s);\n\tapplication.output(s);\n}",
					removeGroupedAndRefresh : "function (event) {\n\tvar history = _ServoyUtils_.history;\n\tvar f = solutionModel.getForm(\"created_by_sm_1\");\n\thistory.removeForm(f.name);\n\tvar fld = f.getField('comp');\n\toutp(\"Component count before remove\" + \" : \" + f.getComponents().length)\n\toutp(\"Label count before remove\"+  \" : \" + f.getLabels().length)\n\tf.removeField(fld.name);\n\toutp(\"Component count after remove\" + \" : \" + f.getComponents().length)\n\toutp(\"Label count after remove\" + \" : \" + f.getLabels().length)\n\t\n\toutp(\"Component count before remove\" + \" : \" + f.getComponents().length)\n\toutp(\"Label count before remove\"+  \" : \" + f.getLabels().length)\n\tf.removeLabel('complabel');\n\toutp(\"Component count after remove\" + \" : \" + f.getComponents().length)\n\toutp(\"Label count after remove\" + \" : \" + f.getLabels().length)\n\tforms[f.name].controller.show();\n}",
					revertPreviousForm : "function (event) {\n\tvar history = _ServoyUtils_.history;\n\thistory.removeForm('companies');\n\tsolutionModel.revertForm('companies');\n\tforms.companies.controller.show();\n}"
				},
				vrbs: {

				}
			}			     
		},
		
		scopes: {
			globals : {
				fncs: {
					 appOutput : "function () {\n\talert(\"this is an EXISTING global method\");\n}"
				},
				vrbs: {
					gvar1 : ["new Array()",35]
				}
			}			
		},
		
		initScope : function (containerName, subscope, scopeToInit, oldScope) {
			var subs = this[containerName][subscope];

			var fncs = subs.fncs;
			for (var key in fncs) {
				scopeToInit[key] = _ServoyUtils_.wrapFunction(eval("(" + fncs[key] + ")"), scopeToInit);
				eval("var " + key + " = scopeToInit[key];");
			}

			var vrbs = subs.vrbs;
			for (var key in vrbs) {
				var val = vrbs[key];
			   _ServoyUtils_.defineVariable(scopeToInit, key, oldScope ? oldScope[key] : eval("(" + val[0] + ")"), val[1]);
			}
		}
	}
}