// Business logic for TestMonkey

// Create global helper objects
rest = new ajax();
testRowIterator = new TestRowIterator();

// Code to run on page load
window.onload = function onPageLoad() {
	// Call rest get on tests
	rest.doAjaxGet("/rest/tests", function (testSuites) {
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
			testHtml += "<table id=\"" + suiteName + "\"><thead><tr><th>Enabled</th><th>Test name</th><th>Last Ran</th><th>Running time</th><th>   </th><th>Result</th><th>Messages</th></tr></thead>";
			var testCases = testSuites[i].testCases;
			if (testCases.length > 0)
				testHtml += "<tbody>";
			for (var j = 0; j < testCases.length; j++)
			{
				var testName = suiteName + "." + testCases[j];
				testHtml += "<tr class=\"enabled\" id=\"" + testName + "\"><td><input type=\"checkbox\" name=\"" + testName + "\" checked=\"checked\" /></td><td>" + testName + "</td><td>Never</td><td>0.0</td><td><a id=\"Run" + testName + "\" href=\"\">Run</a></td><td>Not run</td><td></td></tr>";
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
		addClickHandlerById("RunAllTestsLink", function() { runSelectedTests("tests"); });
		
		// set click handlers for per-test suite controls
		for (var i = 0; i < testSuites.length; i++)
		{
			var suiteName = testSuites[i].suiteName;
			var suiteEnableLinkId = "Select" + suiteName + "Suite";
			var suiteDisableLinkId = "Unselect" + suiteName + "Suite";
			var suiteRunLinkId = "Run" + suiteName + "Suite";
			// now for some hairy syntax to preserve scope of suiteName in closures
			// this might be able to be circumvented by using arguments var arg array and function.apply
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
			addClickHandlerById(suiteRunLinkId, ( function (preserveVarScope) {
													return function() {
														runSelectedTests(preserveVarScope);
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
						//onClickCheckbox(cbvar, pridvar);
						updateTestsSelectionState(pridvar, cbvar.checked);
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
 * Updates the specified tests to enabled/selected or disabled/unselected
 * @param parentElementId Id of the parent element whose children test rows
 * 							will be updated (checkbox state and classes for row styling) 
 * @param selectionState true to select, false to unselect 
 */
function updateTestsSelectionState(parentElementId, selectionState)
{	
	// promote this to top level and get rid of updateTestsSelectionState fn
	testRowIterator.forEachTestUnderElement(
			parentElementId, 
			testRowIterator.noFilter, 	// operate on all rows
			selectionState ? 
					testRowIterator.tickCheckboxAndSetRowStyleEnabled : 
					testRowIterator.untickCheckboxAndSetRowStyleDisabled);
}

/**
 * Gets a list of the selected tests, runs the tests and updates the tests to "running" status (with
 * .running css class)
 * Then updates tests with pass/fail status (again with updated styling)
 */
function runSelectedTests(parentElementId)
{
	var testsToRun = testRowIterator.forEachTestUnderElement(
						parentElementId, 
						testRowIterator.onlySelectedFilter, 	// only operate on selected rows
						function (row) { return row.getAttribute("id"); }); // return test id
	
	if (testsToRun.length > 0)
	{
		// optional step to reformat test array in a shortened form
		testsToRun = shortenTestArray(testsToRun);
		var runFilter = testsToRun.join(":");
		
		// Call rest get on tests
		rest.doAjaxGet("/rest/results/" + runFilter, handleTestResults);
		
		// update test row styles to running, to give user feedback
		testRowIterator.forEachTestUnderElement(
				parentElementId,
				testRowIterator.onlySelectedFilter,
				function (row) { row.setAttribute("class", "running"); });
	} // otherwise silently ignore attempted run of 0 tests
}

/**
 * Handler function called when test results are received back from
 * the rest server
 * @param testResults
 */
function handleTestResults(testResults)
{
	if (!Array.isArray(testResults))
	{
		// not an array, so error out
		alert("Error - Server returned unexpected test result objects.");
		// cancel running state
		testRowIterator.forEachTestUnderElement(
				"test", 
				testRowIterator.onlySelectedFilter, 
				function (row) { row.setAttribute("class", "enabled"); });
		return;
	}
	for (var i = 0; i < testResults.length; i++)
	{
		// get id of test we have the result for
		var testResult = testResults[i];
		var testId = testResult.suiteName + "." + testResult.caseName;
		// update test row style
		testRowIterator.forEachTestUnderElement(
				testId, 
				testRowIterator.noFilter, 
				function (row) { row.setAttribute("class", testResult.passed ? "passed" : "failed"); });
		var testRow = document.getElementById(testId);
		// update last ran time
		var timeStamp = (new Date()).toString();
		timeStamp = timeStamp.replace(/^(.*:\d{2}).*$/, "$1"); // get rid of time zone, daylight saving etc suffixes
		testRow.childNodes.item(2).innerHTML = timeStamp;
		// update running time
		testRow.childNodes.item(3).innerHTML = testResult.elapsedTime;
		// update messages
		testRow.childNodes.item(6).innerHTML = testResult.passed ?  "" : testResult.errorMessage;
	}
	
}

/**
 * Reformat array that will be turned into the gtest test filter
 * string by substituting complete lists of
 * test cases in a suite with just the suite name, and then all suite
 * names by "*" if everything is selected.
 * 
 * @param testsToRun array of test cases to run
 * @returns {Array} same array in shortened notation
 */
function shortenTestArray(testsToRun)
{
	// get all test suites and cases from dom
	var tests = testRowIterator.getAllTests();
	// substitute lists of individual complete test cases
	// with the single name of the test suite
	for (var suite in tests)
	{
		var cases = tests[suite];
		var completeSuite = true;
		for (var i = 0; i < cases.length; i++)
		{
			if (testsToRun.indexOf(cases[i]) == -1)
			{
				completeSuite = false;
				break;
			}
		}
		if (completeSuite)
		{
			// perform the substitution
			var startIndex = testsToRun.indexOf(cases[0]);
			testsToRun.splice(startIndex, cases.length, suite + ".*");
		}
	}
	
	// compress filter array to * in the case that all test
	// suites have been selected
	var suiteLeftOut = false;
	for (var suite in tests)
	{
		if (testsToRun.indexOf(suite + ".*") == -1)
		{
			suiteLeftOut = true;
			break;
		}
	}
	if (!suiteLeftOut)
		testsToRun = ["*"];
	
	return testsToRun;
}


