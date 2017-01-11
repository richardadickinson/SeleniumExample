# SeleniumExample
Example Selenium project in Java

iConsole: Web-based console which provides search, reporting and audit review capabilities for captured Data Loss Prevention data.

The iConsole Automated Test Suite is a Java project that uses the Selenium client libraries to execute tests against the iConsole 
on different browsers. The Test Suite sends commands to the Selenium Server (which must already be running) and acts on the return 
values. 

It provides comprehensive logging to the user to review test success.

As Selenium can only interact with the web browser running the suite Java must also perform some other tasks to facilitate 
effective testing:

1.	Database interaction: to review test outcomes it is necessary to know certain things about the CA DLP CMS configuration. 
This requires direct querying of the CMS DB and so Java libraries for SQL Server and Oracle have been included in the project to provide 
this possibility. It is also possible to change database values in this way to force certain configurations without needing to automate 
other CA DLP components.

2.	IIS: some iConsole config changes don’t take effect until after a timeout or an IIS reset. Waiting for the timeout would makes 
tests too lengthy and the browser session could timeout first. Therefore in some cases it can be necessary to force an IIS Reset 
on the iConsole web server. This can be achieved with the psexec utility, the use of which can be automated in the Java suite.

3.	Registry keys: Much of the iConsole configuration is set in the registry. To test the effect of configuration changes it is 
therefore necessary to change the registry values. Java has existing libraries for this and if remote registry interaction is 
required psexec is also an option.

4.	System pop-ups: Selenium can only interact with the browser. Where a system dialog is encountered e.g. a Browse dialog for 
file upload, we need a way of passing the necessary information to the dialog and dismissing it. A tool called AutoIt exists 
for this purpose; it can be used to create executables specific for the dialog you need to automate and these exes can then be 
called via Java code in the suite.

5.	Mail verification: the iConsole can send Audit mails to senders/recipients of captured events on the CMS. To verify these 
mails have been sent correctly with correct flags and content etc. there is a Java library available called Javamail which can 
log into a Mail server and open a user’s mailbox to retrieve specific mail.
