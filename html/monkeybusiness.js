// Business logic for TestMonkey

// Create global helper objects
rest = new ajax();
testRowIterator = new TestRowIterator();

// Code to run on page load
window.onload = function onPageLoad() {
	// Call rest get on tests
	rest.doAjaxGet("/rest/tests/json", function (testSuites) {
		// asynchronous ajax call has returned
		if (!Array.isArray(testSuites))
		{
			// not an array, so return unavailable text
			document.getElementById('tests').innerHTML = "Tests unavailable";
			return;
		}
		
		/*
		// we have an array of test suites back, so construct html tables
		var testHtml = "";
		for (var i = 0; i < testSuites.length; i++)
		{
			var suiteName = testSuites[i].suiteName;
			testHtml += "<h2>" + suiteName + "</h2>";
			testHtml += "<a id=\"Select." + suiteName + "\" href=\"\">Select all tests in suite</a>";
			testHtml += "<a id=\"Unselect." + suiteName + "\" href=\"\">Deselect all tests in suite</a>";
			testHtml += "<a id=\"Run." + suiteName + "\" href=\"\">Run selected tests in suite</a>";
			testHtml += "<table id=\"" + suiteName + "\"><thead><tr><th>Enabled</th><th>Test name</th><th>Last Ran</th><th>Running time</th><th>Run</th><th>Result</th><th>Messages</th></tr></thead>";
			var testCases = testSuites[i].testCases;
			if (testCases.length > 0)
				testHtml += "<tbody>";
			for (var j = 0; j < testCases.length; j++)
			{
				var testName = suiteName + "." + testCases[j];
				testHtml += "<tr class=\"enabled\" id=\"" + testName + "\"><td><input type=\"checkbox\" checked=\"checked\" /></td><td>" + testName + "</td><td>Never</td><td>0.0</td><td><a id=\"Run." + testName + "\" href=\"\"></a></td><td><span></span></td><td></td></tr>";
			}
			if (testCases.length > 0)
				testHtml += "</tbody>";
			testHtml += "</table>";
		}
		
		// put the html into the dom
		document.getElementById('tests').innerHTML = testHtml;
		*/
		
		// add click handler to all hyperlinks
		var links = document.querySelectorAll("a");
		for (var i = 0; i < links.length; i++)
		{
			addClickHandler(links[i], linkClickHandler);
		}
		// add click handler to all checkboxes
		var chkboxes = document.querySelectorAll("input[type=checkbox]");
		for (var i = 0; i < chkboxes.length; i++)
		{
			addClickHandler(chkboxes[i], checkboxClickHandler);
		}
		
	});
};

/**
 * Link click handler function that determines the
 * type of link clicked on (by the id string) and
 * performs the appropriate action
 * @param link - the link element clicked on
 * @param evt - the click event
 */
function linkClickHandler(link, evt)
{
	// prevent default action for hyperlinks - we don't
	// want page refreshing
	evt.preventDefault();
	// pull out command and target node id from clicked element id
	var linkId = link.getAttribute("id");
	var cmd = linkId.replace(/^(.+?)\..*$/, "$1");
	var targetNodeId = linkId.replace(/^.*?\.(.*)$/, "$1");
	if (cmd === "Run")
	{
		runSelectedTests(targetNodeId);
	} else {
		if (cmd !== "Select" && cmd !== "Unselect")
		{
			// unknown command: error out
			alert ("Error - link id not recognised!");
			return;
		}
		// iterate over test rows under specified target, set checkbox
		// state and row css enabled or disabled state depending on
		// type of link
		testRowIterator.forEachTestUnderElement(
			targetNodeId, 
			testRowIterator.noFilter, 	// operate on all rows
			function(row) {
				// update checkbox state
				var chkbox = row.querySelector("input[type=checkbox]");
				chkbox.checked = (cmd === "Select");
				// update row style to reflect selection state
				row.setAttribute("class", cmd === "Select" ? "enabled" : "disabled"); 
			});
	}
}

/**
 * Simple checkbox handler function to enable
 * or disable test row by updating css style
 * @param chkbox
 */
function checkboxClickHandler(chkbox)
{
	// get the parent row
	var tdNode = chkbox.parentNode;
	var trNode = tdNode.parentNode;
	trNode.setAttribute("class", chkbox.checked ? "enabled" : "disabled");
}

/**
 * Adds the specified handler function to the click event of
 * the specified element
 * @param element
 * @param clickFunc - function given (targetNode, event)
 */
function addClickHandler(element, clickFunction)
{
	function crossBrowserClickFunc(evt)
	{
		if (!evt) {
			evt = window.event;
		}
		var node = evt.target ? evt.target : evt.srcElement;
		clickFunction(node, evt);
	};
    if (element.addEventListener) {
    	element.addEventListener("click", crossBrowserClickFunc, false);
    } else if (element.attachEvent) {
    	element.attachEvent("onclick", crossBrowserClickFunc);
    } else {
    	element["onclick"] = crossBrowserClickFunc;
    }
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
		rest.doAjaxGet("/rest/results/" + encodeURIComponent(runFilter), handleTestResults);
		
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
		alert("No results available.\nPlease check that tests are enabled in the application under test.");
	} else {
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
	// cancel running state for all running rows so users don't think app has hung
	testRowIterator.forEachTestUnderElement(
		"tests",
		testRowIterator.onlySelectedFilter, 
		function (row) { row.setAttribute("class", "enabled"); },
		"tbody > tr[class=running]");
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


