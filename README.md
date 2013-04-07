## TestMonkey v1.0 web-based test runner for google test

This software allows C++ test harness applications compiled with the google test library
to be run easily and for unit test results to be clearly displayed in a web browser.

From the terminal / command prompt, start the embedded web server with:

java -jar testmonkey-1.0.jar [name of google test application]

Then point your web browser at:

http://localhost:8080

and use the interface to run individual tests / test suites. Note the gtest binary can be
updated while the web server is running.

ctrl-c from the command prompt to quit.

Enjoy!

Phil McCarthy, April 2013
