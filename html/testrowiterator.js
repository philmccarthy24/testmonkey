TestRowIterator = function () {
	
	/**
	 * For each test under the parent id (filtered by the filter function)
	 * carry out the specified action
	 * @param parentElementId
	 * @param filterFunction
	 * @param actionFunction
	 * @returns {Array} list of results from each invocation of actionFunction
	 */
	this.forEachTestUnderElement = function(parentElementId, filterFunction, actionFunction)
	{
		var parent = document.getElementById(parentElementId);
		var rows = [];
		if (parent.tagName === "TR")
		{
			// element is already a specific row, so use this
			rows.push(parent);
		} else {
			// element is a parent of a table or tables, so search for
			// body trs in this
			rows = parent.querySelectorAll("tbody > tr");
		}
		var results = [];
		for (var i = 0; i < rows.length; i++)
		{
			if (filterFunction(rows[i]))
			{
				results.push(actionFunction(rows[i]));
			}
		}
		return results;
	};
	
	/**
	 * Gets list of test suites and test cases in these suites from dom
	 * @returns hash (object) of test suites to test cases
	 */
	this.getAllTests = function()
	{
		var tests = {};
		// iterate through table elements first picking up suite name ids
		var tables = document.querySelectorAll("table");
		for (var t = 0; t < tables.length; t++)
		{
			var testSuiteName = tables[t].getAttribute("id");
			var rows = tables[t].querySelectorAll("tbody > tr");
			var testCases = [];
			for (var r = 0; r < rows.length; r++)
			{
				testCases.push(rows[r].getAttribute("id"));
			}
			tests[testSuiteName] = testCases;
		}
		return tests;
	};

	
	/**
	 * Does no filtering on rows
	 * @param row
	 * @returns {Boolean}
	 */
	this.noFilter = function(row)
	{
		return true;
	};
	
	/**
	 * Only operate on rows where the first td element's
	 * checkbox input control is checked or not
	 * @param row
	 * @returns {Boolean}
	 */
	this.onlySelectedFilter = function(row)
	{
		var chkbox = row.querySelector("input[type=checkbox]");
		return chkbox.checked;
	};
	
};