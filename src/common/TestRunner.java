//Copyright © 2011 CA. All rights reserved.

package common;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import java.util.*;
import java.sql.*;
import testcases.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.ie.*;
import org.openqa.selenium.remote.DesiredCapabilities;  // required if need to start IE in ignore-domain mode
//import org.openqa.selenium.ie.InternetExplorerDriver.*;
//import java.util.concurrent.TimeUnit;    // required if implicit waits are re-enabled
import java.io.*;
import org.openqa.selenium.remote.CapabilityType;

public class TestRunner
{
    private boolean isInitialized;
    public boolean isStarted;
    private UserPreferences mUserPrefs;
    private String mFailureReason;
    private boolean mHasFailed;
    private LogFile mLogFile;
    private String mCurrentTestName;
    private ConfigurationFile mConfigFile;
    private StringBuilder msbURL;
    private AuditConfigurationManager mAuditConfigMgr;
    private WgnCleanup mWgnCleanup;
    private WgnTestCase c;
    private int cleanup;
    private Boolean mExceptionOccurred;
    private String[] mCommands;
    private WebDriver driver;

    public TestRunner()
    {
        isInitialized = false;
        isStarted = false;
        mLogFile = new LogFile();
        mConfigFile = new ConfigurationFile();
        mAuditConfigMgr = new AuditConfigurationManager(this);
        mWgnCleanup = new WgnCleanup();
        cleanup = 0;
        mExceptionOccurred = false;
    }

    public void CreateUserPreferences(boolean removeAuditedEvents, boolean moveNextEvent) throws Exception
    {
        if (mUserPrefs != null)
        {
            throw new Exception("User Preferences have already been created");
        }

        mUserPrefs = new UserPreferences(removeAuditedEvents, moveNextEvent);
    }

    public UserPreferences GetUserPreferences()
    {
        return mUserPrefs;
    }

    public AuditConfigurationManager GetAuditConfigurationManager()
    {
        return mAuditConfigMgr;
    }

    public LogFile GetLogFile()
    {
        return mLogFile;
    }
    
    public int GetLogLevel()
    {
        return mConfigFile.GetLogLevel();
    }

    public WebDriver GetDriver()
    {        
        return driver;
    }       

    public boolean HasFailed()
    {
        return mHasFailed;
    }

    public String GetFailureReason()
    {
        return mFailureReason;
    }

    public String GetFailedTestName()
    {
        return mCurrentTestName;
    }
    
    public String GetTargetBrowser()
    {
        return mConfigFile.GetTargetBrowser();
    }

    public String[] GetDomainLoginCommand()
    {
        return mCommands;
    }

    public ConfigurationFile GetConfigurationFile()
    {
        return mConfigFile;
    }

    public String getPackageVersion() throws IOException
    {
        String version = "";
        Package pkg = Package.getPackage("iconsoletestproject");
        if (pkg != null)
        {
            version = pkg.getImplementationVersion();
        }

        return version;
    }

    public boolean Initialize()
    {
        boolean bRet = true;
        
        try
        {
            bRet = mConfigFile.Load();

            if (bRet)
            {
                mLogFile.verboselevel = mConfigFile.GetLogLevel();
                mLogFile.Open();
                mLogFile.Write("iConsole Automation Package Version: " + getPackageVersion(), LogFile.iINFO);
                mLogFile.Write("TestRunner initialise started ", LogFile.iDEBUG);
            
                String browser = mConfigFile.GetTargetBrowser();
                msbURL = new StringBuilder();
                msbURL.append(mConfigFile.GetProtocol());
                msbURL.append(mConfigFile.GetWebServer());
                msbURL.append("/");
                msbURL.append(mConfigFile.GetVirtualDirectory());
                msbURL.append("/");
                
                //set up the parameters for launching the AutoIT exe to access the domain login dialog
                String title = mConfigFile.GetDomainDialogTitle();                
                String user = mConfigFile.GetDomainUser();
                String pw = mConfigFile.GetDomainUserPassword();
                String exePath = mConfigFile.GetExtrasPath();
                if (!exePath.isEmpty()) exePath = exePath + "\\";
                String exeName = "ff_login.exe"; // name of the AutoIT exe for the browser under test
                if (browser.contains("chrome")) exeName = "chr_login.exe";
                if (browser.startsWith("ie")) exeName = "ie_login.exe";
                exePath = exePath + exeName;
                mLogFile.Write("AutoIT exe path = " + exePath, LogFile.iDEBUG);
                mLogFile.Write("Expected Security dialog title = " + title, LogFile.iDEBUG);
                mCommands = new String[]{exePath, title, user, pw};
                
                // start the desired Selenium browser driver
                String driverPath = mConfigFile.GetExtrasPath(); // the location of the browser Server exes                
                switch (browser) {
                    case "firefox":
                        FirefoxProfile profile = new FirefoxProfile();
                        profile.setPreference("dom.disable_open_during_load", false);
                        profile.setPreference("dom.popup_maximum", 1000);
                        // enable firebug in the selenium window for debugging
                        String firebugPath = mConfigFile.GetFirebug();
                        if (!firebugPath.isEmpty()) profile.addExtension(new File(firebugPath));
                        driver = new FirefoxDriver(profile);
                        break;
                    case "chrome":
                        // Google Chrome requires an extra driver to be started to run tests
                        System.setProperty("webdriver.chrome.driver", driverPath + "//chromedriver.exe");
                        // disable popup blocking or print preview windows (amongst others cannot be opened)
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--disable-popup-blocking");
                        driver = new ChromeDriver(options);
                        break;
                    case "ie8": case "ie9": case "ie10":
                        // IE requires an extra driver to be started to run tests
                        System.setProperty("webdriver.ie.driver", driverPath + "//IEDriverServer.exe");
                        // now we need to set some browser specific configuration for the tests to run smoothly
                        DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
                        // turn off the default behaviour of auto-dismissing alerts - we need to accept some alerts, like the domain login dialog
                        ieCapabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "ignore");
                        // stop the mouse hover hack from working - deprecated
                        //ieCapabilities.setCapability(CapabilityType.ENABLE_PERSISTENT_HOVERING, "false");
                        // IE8 either needs all zones in same Protected mode setting (on|off) or can use the workaround below:
                        if (browser.contentEquals("ie8"))
                        {
                            ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                        }
                        driver = new InternetExplorerDriver(ieCapabilities);                        
                        break;
                    default:
                        ReportError("Specified Target Browser was not recognised.");
                        break;
                }
                
                Boolean bCmsconfig = ConnectToCMSDatabase();
                if (bCmsconfig) 
                {
                    isInitialized = true;
                    mLogFile.Write("TestRunner initialise complete", LogFile.iDEBUG);
                } else {
                    bRet = false;
                    mLogFile.Write("TestRunner failed to connect to CMS Database", LogFile.iDEBUG);
                }
            }
            else
            {
                mFailureReason = "TestRunner failed to load configuration due to error: " + mConfigFile.GetFailureMessage();
            }
           
        }
        catch (Exception e)
        {
            ReportError("TestRunner initialise failed with exception: " + e.getMessage());
            bRet = false;
        }

        return bRet;
    }

    public void Stop()
    {
        //driver.close();
        driver.quit();
    }

    public void RunTests() throws Exception
    {
        int iWTC = 0;

        mLogFile.Write("TestRunner.RunTests() - Started", LogFile.iDEBUG);

        if (!isInitialized)
        {
            throw new Exception("TestRunner has not been initialized");
        }

        Iterator<?> it = mConfigFile.GetTestList().iterator();
        mLogFile.Write("TestRunner.RunTests() - retrieved Test list", LogFile.iDEBUG);

        while (it.hasNext())
        {
            mAuditConfigMgr.ResetAuditStatus();
            mLogFile.Write("TestRunner.RunTests() - reset Audit Status", LogFile.iDEBUG);
            Boolean mAuditCfg = mAuditConfigMgr.LoadAuditConfig();
            if (!mAuditCfg) 
            {  
                WriteLog("Exception getting audit config from DB", LogFile.iERROR);
                break;
            } else {
                mLogFile.Write("TestRunner.RunTests() - retrieved Audit config", LogFile.iDEBUG);
            }
            try
            {
                iWTC++;
                c = (WgnTestCase)it.next();
                c.SetTestRunner(this);
                mCurrentTestName = c.GetTestName();
                mLogFile.Write("TestRunner.RunTests() - Test Case set to: " + mCurrentTestName, LogFile.iDEBUG);

                c.SetURL(msbURL.toString());
                mLogFile.Write("TestRunner.RunTests() - Using URL: " + msbURL.toString(), LogFile.iDEBUG);
                c.Start();
                mLogFile.Write("TestRunner.RunTests() - Started Test Case", LogFile.iDEBUG);

                if (isStarted)
                {
                    c.Run();
                }
                if (c.HasFailed())
                {
                    mFailureReason = c.GetFailureReason();
                    mHasFailed = true;
                    //attempt cleanup (but only once in case an exception occurs inside the cleanup routine)
                    if (cleanup == 0)
                    {
                        cleanup = 1;
                        mLogFile.ResetIndent();
                        Boolean bCleaned = mWgnCleanup.Cleanup(c);
                        if (bCleaned)
                        {
                            mLogFile.Write("Cleanup procedure was successful.", LogFile.iINFO);
                            // reset vars for next
                            cleanup = 0;
                        } else {
                            mLogFile.Write("Cleanup procedure failed.", LogFile.iERROR);
                            c.Stop();
                            break;
                        }
                    }
                }

                c.Stop();
                mHasFailed = false;
            } 
            catch (Exception e)
            {
                ReportError("TestRunner.RunTests() - caught exception: " + e.getMessage());
                e.printStackTrace(new PrintWriter(mLogFile.GetFileWriter()));
                mHasFailed = true;
                mFailureReason = e.getMessage();
                mExceptionOccurred = true;
                break;
            }                 
        }

        if (mExceptionOccurred)
        {
            //attempt cleanup (but only once in case an exception occurs inside the cleanup routine)
            if (cleanup == 0)
            {
                mLogFile.ResetIndent();
                cleanup = 1;
                Boolean bCleaned = mWgnCleanup.Cleanup(c);
                if (bCleaned)
                {
                    mLogFile.Write("Cleanup procedure was successful.", LogFile.iINFO);
                } else {
                    mLogFile.Write("Cleanup procedure failed.", LogFile.iERROR);
                }
            }
            c.Stop();
        }

        mLogFile.Write("TestRunner.RunTests() - Finished", LogFile.iDEBUG);
        mLogFile.Close(iWTC);
    }

    // only call this method to report terminal errors - exceptions, javascript errors etc
    public void ReportError(String s)
    {
        try
        {
            mFailureReason = s;
            mLogFile.WriteError(s);
        }
        catch(IOException ioe)
        {
            System.err.print("TestRunner ReportError() caught exception: " + ioe.getMessage());
        }
    }

    public void WriteLog(String msg, int loglevel)
    {
        try
        {
            mLogFile.Write(msg, loglevel);
        }
        catch(IOException ioe)
        {
            System.err.println(ioe.getMessage());
        }
    }

    public void NewLogFileGroup(String msg, int loglevel)
    {
        try
        {
            mLogFile.Enter(msg, loglevel);
        }
        catch(IOException ioe)
        {
            System.err.println(ioe.getMessage());
        }
    }

    public void CloseLogFileGroup(String msg, int loglevel)
    {
        try
        {
            mLogFile.Exit(msg, loglevel);
        }
        catch(IOException ioe)
        {
            System.err.println(ioe.getMessage());
        }
    }

    private boolean ConnectToCMSDatabase()
    {
        boolean bRet = true;

        try
        {
            mAuditConfigMgr.ConnectToDB();
        }
        catch(ClassNotFoundException cnfe)
        {
            bRet = false;
            ReportError("TestRunner get CMS configuration Class Not Found exception:" + cnfe.getMessage());
        }
        catch(SQLException sqle)
        {
            bRet = false;
            ReportError("TestRunner get CMS configuration SQL exception:" + sqle.getMessage());
        }

        return bRet;
    }
}
