ajax = function () {
	
	/**
	 * Get data from REST backend
	 * 
	 * @param url the address to get data from
	 * @param onDataReceived callback function, which takes an object
	 *  or an array	of objects as a parameter (depending on the url)
	 */
	this.doAjaxGet = function (url, onDataReceived) {
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
	};
	
};