
Setup: Notes
-------------

1. Two AutoIT exes are included with the project to complete the domain login popups that can arise on starting the browsers.
   Deploy them local to the main test suite jar.

2. Two search definition files, test_Searches.xml and test_Reports.xml, are included in the package at <dist>\xml. 
   Set their location in the 'searchesPath' and 'reportsPath' parameters in cfg_admintab.xml.

3. If tests are to be run on Google Chrome the ChromeDriver.exe file must also be deployed and its location set in SYS.xml
   e.g. chromepath="C:\\Program Files\\Java\\chromedriver.exe"

4. a. Copy the config\sys.xml file to the same location as iConsole_automation.jar.
      Configure all the parameters in sys.xml for your system.
	
   b. Use the cfg_xxx.xml files to define which tests to run (a default cfg file containing example scripts for all test cases 
      is packaged in the zip and installed to the same location as the jar).

   c. Do NOT edit gbl.xml




To run test suite:
------------------

1. In the cmd prompt go to the location of the test suite jar file.

2. Set your java classpath e.g.
   path=C:\Program Files\Java\jdk1.6.0_21\bin;./lib;

3. run the Test Suite e.g.
   java -jar iConsole_automation.jar -sys sys.xml -gbl gbl.xml -cfg cfg.xml -log mylog.log




