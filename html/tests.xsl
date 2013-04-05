<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
	<html>
		<head>
			<!-- business logic in monkey business script -->
			<script type="text/javascript" src="/ajax.js"></script>
			<script type="text/javascript" src="/testrowiterator.js"></script>
			<script type="text/javascript" src="/monkeybusiness.js"></script>
			<link rel="stylesheet" href="/monkeystyle.css" />
		</head>
		<body>
			<h1>TestMonkey Google Test web runner</h1>
			<div id="global_controls">
				<a id="Select.tests" href="">Select all tests</a>
				<a id="Unselect.tests" href="">Deselect all Tests</a>
				<a id="Run.tests" href="">Run selected tests</a>
			</div>
			<div id="tests">
				<xsl:for-each select="testSuites/test">
					<h2><xsl:value-of select="testsuite"/></h2>
					<a id="Select.{testsuite}" href="">Select all tests in suite</a>
					<a id="Unselect.{testsuite}" href="">Deselect all tests in suite</a>
					<a id="Run.{testsuite}" href="">Run selected tests in suite</a>
					<table id="{testsuite}">
						<thead>
							<tr>
								<th>Enabled</th>
								<th>Test name</th>
								<th>Last Ran</th>
								<th>Running time</th>
								<th>Run</th>
								<th>Result</th>
								<th>Messages</th>
							</tr>
						</thead>
						<tbody>
							<xsl:for-each select=".//testcase">
								<tr class="enabled" id="{../../testsuite}.{text()}">
									<td><input type="checkbox" checked="checked" /></td>
									<td><xsl:value-of select="../../testsuite"/>.<xsl:value-of select="text()"/></td>
									<td>Never</td>
									<td>0.0</td>
									<td><a id="Run.{../../testsuite}.{text()}" href=""></a></td>
									<td><span></span></td>
									<td></td>
								</tr>
							</xsl:for-each>
						</tbody>
					</table>
				</xsl:for-each>
			</div>
		</body>
	</html>
</xsl:template>
</xsl:stylesheet>

