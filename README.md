## TestMonkey v1.0 web-based test runner for google test

This software allows C++ test harness applications compiled with the [google test library]
(http://code.google.com/p/googletest) to be run easily and for unit test results to be clearly displayed in a web browser.

To build Test Monkey you will need to [download maven](http://maven.apache.org/download.cgi),
and also get the latest [jdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) for your platform.
I'd also recommend [getting eclipse](http://www.eclipse.org/downloads/packages/eclipse-classic-422/junosr2) - an eclipse
project is included in the repository. To get going you will need to:

- ensure the maven and java bin directories are on your path
- run "mvn package" to download all the dependencies to your local maven repo and build	the single large testmonkey jar file in the target subdir. 
- from the terminal / command prompt, start the embedded web server with "java -jar testmonkey-1.0.jar [name of google test application]". Note you might need to prepend the name of the test app with "./" if running from the same directory
- point your web browser at http://localhost:8080
- use the interface to run individual tests / test suites.
- press ctrl-c from the command prompt to stop the web server.
	
Note the google test harness executable under test can be updated while the web server is running.

The following browsers have been tested:

- Firefox 19.0.2
- Safari 6.0.3
- Internet Explorer 10 (works with minor display issues)
- Google Chrome (recent versions)
	
Internet Explorer versions prior to 10 are not supported due to Microsoft's historic lack of web standards compliance.

Enjoy!

Phil McCarthy, April 2013
