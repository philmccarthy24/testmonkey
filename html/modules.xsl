<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
	<html>
		<head>
			<link rel="stylesheet" href="/monkeystyle.css" />
		</head>
		<body>
			<h1>Test<img src="/images/monkey.svg" />Monkey</h1>
			<h2><img src="/images/gtestlogo.gif" />googletest <span>web runner</span></h2>
			<ul class="topnav">
				<li>
					<a id="About.tests" href="" onClick="
					alert('Test Monkey v1.0 created April 2013 by Phil McCarthy.\n\n
						This software is distributed under the MIT license,\n
						and is free for both commercial and non-commercial use.\n
						Images used are the property of the respective copyright holders.\n\n
						See Readme.md for further details')
					">About</a>
				</li>
			</ul>
			<div id="tests">	<!-- id used in CSS - not actually tests but a table of test modules -->
				<h2>Google test modules available:</h2>
				<table>
					<thead>
						<tr>
							<th></th>			<!-- maybe a small google gtest icon that lights up on rollover? -->
							<th>Test Module</th>
							<th>Description</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each select="testModules/testmodule">
							<tr class="enabled">
								<td><span></span></td>
								<!-- below is position - 1 because the rest resource id is 0 indexed -->
								<td><a href="/rest/tests/xml/{position() - 1}"><xsl:value-of select="name" /></a></td>
								<td><xsl:value-of select="description" /></td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
			</div>
		</body>
	</html>
</xsl:template>
</xsl:stylesheet>

