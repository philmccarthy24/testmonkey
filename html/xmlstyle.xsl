<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
	<html>
		<head>
			<xsl:if test="testSuites" > <!-- js only included if we're presenting tests -->
				<!-- business logic in monkey business script -->
				<script type="text/javascript" src="/ajax.js"></script>
				<script type="text/javascript" src="/testrowiterator.js"></script>
				<script type="text/javascript" src="/monkeybusiness.js"></script>
			</xsl:if>
			<link rel="stylesheet" href="/monkeystyle.css" />
		</head>
		<body>
			<h1>Test<img src="/images/monkey.svg" />Monkey</h1>
			<h2><img src="/images/gtestlogo.gif" />googletest <span>web runner <xsl:if test="testSuites" >[<xsl:value-of select="testSuites/test[1]/testmodule"/>]</xsl:if></span></h2>
			<ul class="topnav">
				<xsl:if test="testSuites" >
					<li>
						<a title="Home" id="Redirect./" href=""></a>
					</li>
					<li>
						<a title="Select all tests" id="Select.page" href=""></a>
					</li>
					<li>
						<a title="Deselect all tests" id="Unselect.page" href=""></a>
					</li>
					<li>
						<a title="Launch all selected tests" id="Run.page" href=""></a>
					</li>
				</xsl:if>
				<li>
					<a title="About" id="Redirect.http://philmccarthy24.github.io/testmonkey" href="http://philmccarthy24.github.io/testmonkey"></a>
				</li>
			</ul>
			<div id="page">
				<xsl:choose>
					<xsl:when test="testSuites">
						<xsl:for-each select="testSuites/test">
							<h2><xsl:value-of select="testsuite"/></h2>
							<ul class="nav">
								<li>
									<a id="Select.{testsuite}" href="">Select all tests in suite</a>
								</li>
								<li>
									<a id="Unselect.{testsuite}" href="">Deselect all tests in suite</a>
								</li>
								<li>
									<a id="Run.{testsuite}" href="">Run selected tests in suite</a>
								</li>
							</ul>
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
											<td><xsl:value-of select="text()"/></td>
											<td>Never</td>
											<td>0.0</td>
											<td><a title="Run test" id="Run.{../../testsuite}.{text()}" href=""></a></td>
											<td><span></span></td>
											<td></td>
										</tr>
									</xsl:for-each>
								</tbody>
							</table>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<h2>Google test modules available:</h2>
						<table>
							<thead>
								<tr>
									<th></th>
									<th>Test Module</th>
									<th>Description</th>
								</tr>
							</thead>
							<tbody>
								<xsl:for-each select="testModules/testmodule">
									<!-- below is position - 1 because the rest resource id is 0 indexed -->
									<!-- nasty inline onclick event hack - css being particularly difficult to get right here -->
									<tr class="modulerow" onClick="window.location.href='/rest/tests/xml/{position() - 1}'" onmouseover="this.style.cursor='pointer'">
										<td><span></span></td>
										<td><xsl:value-of select="name" /></td>
										<td><xsl:value-of select="description" /></td>
									</tr>
								</xsl:for-each>
							</tbody>
						</table>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		</body>
	</html>
</xsl:template>
</xsl:stylesheet>

