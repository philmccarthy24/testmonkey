/**
 * Ajax coms module
 */
var ajax = (function () {
	//Private members

	//	Start of privileged methods
	return {
		/**
		 * Get data from REST backend
		 * 
		 * @param url the address to get data from
		 * @param onDataReceived callback function, which takes an object
		 *  or an array	of objects as a parameter (depending on the url)
		 */
		doRestGetAsync : function (url, onDataReceived) {
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open("GET", url, true);
			xmlhttp.onreadystatechange = function ()
			{
				if (xmlhttp.readyState === 4 && 
						xmlhttp.status === 200)
				{
					// parse the JSON to JS object(s)
					var data = JSON.parse(xmlhttp.responseText, null);
					// call the callback function with the array
					onDataReceived(data);
				}
			};
			xmlhttp.send();
		}
	
	/**
	 * Creates a new entity on the restful backend (data domain)
	 * @param name - name of the entity
	 * @param onEntityCreated - entity creation callback with new entity as param
	 *
	, doRestPostAsync : function (name, onEntityCreated, x, y) {
		var entity = {};
		entity.id = "0";	// id doesn't matter, service will generate one
		entity.name = name;
		var entityAsJson = JSON.stringify(entity, null);

		var xmlhttp = new XMLHttpRequest();
		xmlhttp.open("POST","model/entities",true);
		xmlhttp.setRequestHeader("Content-type","application/json");
		xmlhttp.setRequestHeader("Content-length", entityAsJson.length);
		xmlhttp.setRequestHeader("Connection", "close");
		xmlhttp.onreadystatechange = function()	{
			if (xmlhttp.readyState === 4 && 
					xmlhttp.status === 201)	//201=created
			{
				// get newly created object id from status string
				var newEntityURI = xmlhttp.getResponseHeader("Location");
				entity.id = newEntityURI.match(new RegExp(".*\/(.*)"))[1];
				onEntityCreated(entity, x, y);
			}
		};
		xmlhttp.send(entityAsJson);
	}*/
	};
})();


