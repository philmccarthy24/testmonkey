// Business logic for TestMonkey

// Code to run on page load
window.onload = function onPageLoad() {
	// Call rest get on tests
	doAjaxGet("/rest/tests", function (testSuites) {
		// asynchronous ajax call has returned
		if (!Array.isArray(testSuites))
		{
			// not an array, so return unavailable text
			document.getElementById('tests').innerHTML = "Tests unavailable";
			return;
		}
		
		// we have an array of test suites back, so construct html tables
		var testHtml = "";
		for (var i = 0; i < testSuites.length; i++)
		{
			var suiteName = testSuites[i].suiteName;
			testHtml += "<h2>" + suiteName + "</h2>";
			testHtml += "<a id=\"Select" + suiteName + "Suite\" href=\"\">Select all tests in suite</a>";
			testHtml += "<a id=\"Unselect" + suiteName + "Suite\" href=\"\">Deselect all tests in suite</a>";
			testHtml += "<a id=\"Run" + suiteName + "Suite\" href=\"\">Run selected tests in suite</a>";
			testHtml += "<table id=\"" + suiteName + "\"><thead><tr><th>Enabled</th><th>Test name</th><th>Last Ran</th><th>Running time</th><th>Result</th><th>Messages</th></tr></thead>";
			var testCases = testSuites[i].testCases;
			if (testCases.length > 0)
				testHtml += "<tbody>";
			for (var j = 0; j < testCases.length; j++)
			{
				var testName = suiteName + "." + testCases[j];
				testHtml += "<tr id=\"" + testName + "\"><td><input type=\"checkbox\" name=\"" + testName + "\" checked=\"checked\" /></td><td>" + testName + "</td><td>Never</td><td>0.0</td><td>Not run</td><td></td></tr>";
			}
			if (testCases.length > 0)
				testHtml += "</tbody>";
			testHtml += "</table>";
		}
		
		// put the html into the dom
		document.getElementById('tests').innerHTML = testHtml;
		
		// set click handlers for top link controls
		addClickHandlerById("SelectAllTestsLink", function() { updateTestsSelectionState("tests", true); });
		addClickHandlerById("UnselectAllTestsLink", function() { updateTestsSelectionState("tests", false); });
		//updateLinkClickHandler("EnableAllTests", updateTestsEnabled, "all");
		
		// set click handlers for per-test suite controls
		for (var i = 0; i < testSuites.length; i++)
		{
			var suiteName = testSuites[i].suiteName;
			var suiteEnableLinkId = "Select" + suiteName + "Suite";
			var suiteDisableLinkId = "Unselect" + suiteName + "Suite";
			// now for some hairy syntax to preserve scope of suiteName in closures
			addClickHandlerById(suiteEnableLinkId, ( function (preserveVarScope) {
													return function() {
														updateTestsSelectionState(preserveVarScope, true);
													};
												})(suiteName));
			addClickHandlerById(suiteDisableLinkId, ( function (preserveVarScope) {
													return function() {
														updateTestsSelectionState(preserveVarScope, false);
													};
												})(suiteName));
			
			// set click handlers for per test controls
			var testCases = testSuites[i].testCases;
			for (var j = 0; j < testCases.length; j++)
			{
				var parentRowId = suiteName + "." + testCases[j];
				// get table row node
				var row = document.getElementById(parentRowId);
				// get row checkbox
				var checkbox = row.querySelector("input[type=checkbox]");
				// add click handler
				addClickHandlerToElement(checkbox, ( function (cbvar, pridvar) {
					return function() {
						onClickCheckbox(cbvar, pridvar);
					};
				})(checkbox, parentRowId));
			}
		}
	});
};

/**
 * Adds the specified handler function to the click event of the element with
 * id elementId. Convenience wrapper for addClickHandlerToElement
 * @param elementId
 * @param handlerFunction
 */
function addClickHandlerById(elementId, handlerFunction)
{
	var element = document.getElementById(elementId);
	addClickHandlerToElement(element, handlerFunction);
}

/**
 * Adds the specified handler function to the click event of
 * the specified element
 * @param element
 * @param handlerFunction
 */
function addClickHandlerToElement(element, handlerFunction)
{
	var clickFunc = function (e) {
        if (element.tagName === "A")
        {
        	// prevent default click action for links so
        	// that link isn't followed
        	e = e || window.event;
        	e.preventDefault();
        }
        handlerFunction(); // call handler function
    };
    if (element.addEventListener) {
    	element.addEventListener("click", clickFunc, false);
    } else if (element.attachEvent) {
    	element.attachEvent("onclick", clickFunc);
    } else {
    	element["onclick"] = clickFunc;
    }
}

/**
 * Click handler for checkbox elements
 * @param checkbox
 * @param parentRowId
 */
function onClickCheckbox(checkbox, parentRowId)
{
	// determine current checkbox state
	if (checkbox.checked)
	{
		// enable test
		updateTestsSelectionState(parentRowId, true);
	} else {
		// disable test
		updateTestsSelectionState(parentRowId, false);
	}
}

/**
 * Updates the specified tests to enabled/selected or disabled/unselected
 * @param parentElementId Id of the parent element whose children test rows
 * 							will be updated (checkbox state and classes for row styling) 
 * @param selectionState true to select, false to unselect 
 */
function updateTestsSelectionState(parentElementId, selectionState)
{
	// get parent
	var parent = document.getElementById(parentElementId);
	// get checkboxes first
	var checkboxes = parent.querySelectorAll("input[type=checkbox]");
	// set them to checked state
	for (var i = 0; i < checkboxes.length; i++)
	{
		checkboxes[i].checked = selectionState;
	}
	
	// now update tr classes for row styling
	var rows = [];
	if (parent.tagName === "TR")
	{
		// element is already at a specific row, so update this
		rows.push(parent);
	} else {
		// element is a parent of a table or tables so search for
		// body trs in this
		rows = parent.querySelectorAll("tbody > tr");
	}
	for (var i = 0; i < rows.length; i++)
	{
		rows[i].setAttribute("class", selectionState ? "enabled" : "disabled");
	}
}

/**
 * Get data from REST backend
 * 
 * @param url the address to get data from
 * @param onDataReceived callback function, which takes an object
 *  or an array	of objects as a parameter (depending on the url)
 */
function doAjaxGet(url, onDataReceived) {
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.open("GET", url, true);
	xmlhttp.onreadystatechange = function ()
	{
		//alert("readystate=" + xmlhttp.readyState + " : status=" + xmlhttp.status);
		if (xmlhttp.readyState === 4)
		{
			// READYSTATE_COMPLETE - all the data has been loaded
			switch (xmlhttp.status)
			{
			case 200 :	// success!
				{
					// parse the JSON to JS object(s)
					var data = JSON.parse(xmlhttp.responseText, null);
					// call the callback function with the array
					onDataReceived(data);
				}
				break;
			case 204 :	// no content (empty return)
				{
					// call the callback with an empty object
					onDataReceived({});
				}
				break;
			};
		}	// ignore everything else
	};
	xmlhttp.send();
}