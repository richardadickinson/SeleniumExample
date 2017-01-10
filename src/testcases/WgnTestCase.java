//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

//import java.io.IOException;
import org.openqa.selenium.*;
import common.*;
import java.util.*;
/* // these imports are required if we ever need to use Robot to interact with
 *    a system popup rather than AutoIT
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;
import java.lang.reflect.Field;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
 * 
 */

public class WgnTestCase
{
    protected TestRunner mTestRunner;
    protected WebDriver mDriver;
    protected boolean mFailed;
    protected String mFailedReason;
    protected String mTestName;
    protected String mClassName;
    protected boolean mIsStarted;
    protected String mURL;
    protected String mParentHandle;
    protected Map<String, String> mParameters;
    protected Map<String, String> mAttributes;
    protected Map<String, TestUser> mTestUsers;
    protected int mNumIterations;
    protected boolean mIsSSO = true;

    protected List<ConfigurationTestStep> mTestSteps;

    public WgnTestCase()
    {
        mParameters = new HashMap<>();
        mAttributes = new HashMap<>();
        mTestUsers = new HashMap<>();
        mNumIterations = 1;
        mTestSteps = new ArrayList<>();
    }

    public boolean HasFailed()
    {
        return mFailed;
    }

    public String GetFailureReason()
    {
        return mFailedReason;
    }

    public void SetName(String sName)
    {
        mTestName = sName;
    }
    
    public void SetParentHandle(String handle)
    {
        mParentHandle = handle;
    }
    public String GetParentHandle()
    {
        return mParentHandle;
    }
  
    public void isSSO (String sSSO)
    {
        if (sSSO.equals("false")) {
            mIsSSO = false;
        }
    }

    public void SetTestRunner(TestRunner t)
    {
        mTestRunner = t;
        mDriver = t.GetDriver();
    }

    public TestRunner GetTestRunner()
    {
        return mTestRunner;
    }

    public void SetURL(String url) 
    {
        mURL = url;
    }

    public WebDriver GetWebDriver()
    {
        return mDriver;
    }

    public void AddTestStep(ConfigurationTestStep ts)
    {
        mTestSteps.add(ts);
    }

    public void Run() throws Exception
    {
        Iterator<ConfigurationTestStep> it = mTestSteps.iterator();
       
        while (it.hasNext())
        {
            WgnTestStep wts;
            try
            {
                ConfigurationTestStep c = (ConfigurationTestStep)it.next();
                wts = c.TestStepFactory(mTestRunner, mURL);
                wts.SetTestCase(this);
                wts.SetTestStepName(c.GetName());
            } catch (Exception ex) {
                mFailed = true;
                mFailedReason = ex.getMessage();
                throw ex;
            }

            mTestRunner.NewLogFileGroup(String.format("Test step %s started", wts.GetName()), LogFile.iINFO);
			wts.Run();                
			mTestRunner.CloseLogFileGroup(String.format("Test step %s finished", wts.GetName()), LogFile.iINFO);
			
			if (wts.HasFailed())
			{
			    ReportError(wts.GetFailureReason());
			    return;
			}
        }

    }


    public void Start() throws Exception
    {
        // need to use an AutoIT executable to access the Domain login dialog
        String[] commands = mTestRunner.GetDomainLoginCommand();
        try
        {
             Process proc = Runtime.getRuntime().exec(commands);
             Thread.sleep(2000);

             mDriver.get(mURL);
             Thread.sleep(10000);  // dodgy, unreliable wait working around selenium ie driver bug 3392 (auto-dismissal of auth popup)
             // now clean up the login exe if it wasn't required
             try 
             {
                proc.exitValue();
             }
             catch (IllegalThreadStateException ie)
             {
                proc.destroy();
             }             
        }
        catch (Exception e)
        {
             mTestRunner.WriteLog("ERROR running AutoIT exe for Domain login: " + e.toString(), LogFile.iERROR);
             mTestRunner.WriteLog("Login exe parameters retrieved: " + commands[0] + "; " + commands[1] + "; " + commands[2] + "; " + commands[3], LogFile.iINFO);
        }      

        if (Utils.isTextPresent(this, "There is a problem with this website's security certificate", false)
                || Utils.isTextPresent(this, "This Connection is Untrusted", false)) 
        {        // IE & FF error text for certificate error
            mFailed = true;
            mFailedReason = "There is a problem with this website's security certificate";
        } else {
            //mBrowser.windowMaximize(); // replace with WebDriver.Window.setSize(DIMENSION); where dimension is (int width, int height)
                                         // need to retrieve max windowsize via js though
                                         // will be useful for testing the new iConsole fixed layouts from b2900 onward
            
            if (mTestRunner.GetTargetBrowser().contains("firefox") || mTestRunner.GetTargetBrowser().contains("chrome")) // this bit is giving problems on ie
            {
                Utils.GetLastJavaScriptError(this);
                // log browser name and version & platform
                String appName = (String) ((JavascriptExecutor) mDriver).executeScript("return navigator.appName;"); 
                String userAgent = (String) ((JavascriptExecutor) mDriver).executeScript("return navigator.userAgent;"); 
                String platform = (String) ((JavascriptExecutor) mDriver).executeScript("return navigator.platform;"); 

                mTestRunner.WriteLog("Browser: " + appName + " " + userAgent, LogFile.iINFO);
                mTestRunner.WriteLog("Platform: " + platform, LogFile.iINFO);
            }

            mIsStarted = Login();
            mTestRunner.isStarted = true;
            mTestRunner.NewLogFileGroup("Test Case '" + mTestName + "' started.", LogFile.iINFO);
        }

    }
    private Boolean Login()
    {
        String StdSrchHash = Utils.SetParam(this, "BTN_FAV") + Utils.SetParam(this, "StdSrchHash");
        String progress = Utils.SetParam(this,"DLG_PROGRESS");
        String tab_home = Utils.SetParam(this, "TAB_HOMEPAGE");
        String home_cust_desc = Utils.SetParam(this, "HOME_CUST_DESC");
        String tab_review = Utils.SetParam(this, "TAB_REVIEW");
        String dlg_message = Utils.SetParam(this, "DLG_MESSAGE");
        String btn_message_close = Utils.SetParam(this, "BTN_MESSAGE_CLOSE");
        String lnk_about = Utils.SetParam(this, "LNK_ABOUT");
	String btn_about_close = Utils.SetParam(this, "BTN_ABOUT_CLOSE");
	String dlg_about = Utils.SetParam(this, "DLG_ABOUT");
	String xpath_about = Utils.SetParam(this, "XPATH_ABOUT");
        
        try
        {
            int count = 0;
            if (!mIsSSO)  // not using SSO so enter username and password and click login button
            {
                Utils.Login(this, "primary");
            } else {
                if (Utils.isElementPresent(this, By.id(tab_home), false))
                {
                    while (!Utils.isElementPresent(this, By.id(home_cust_desc), false) && count < 20) { count++; Thread.sleep(1000); }
                    if (Utils.isElementPresent(this, By.id(dlg_message), false))
                    {
                        Utils.DoClick(this, By.id(btn_message_close), null, 100);
                    }
                    Utils.DoClick(this, By.id(tab_review), By.id(progress), 20000, false);
                } else {                    
                    while (Utils.isElementPresent(this, By.id(progress), false) && count < 20) { count++; Thread.sleep(1000); }
                }
            }
            count = 0; while (!Utils.isElementPresent(this, By.id(StdSrchHash), false) && count < 20) { Thread.sleep(500); count++; }
            Utils.DoClick(this, By.id(lnk_about), By.id(dlg_about), 5000, false);
            mTestRunner.WriteLog("iConsole " + Utils.GetText(this, By.xpath(xpath_about), false), LogFile.iNONE);
            Utils.DoClick(this, By.id(btn_about_close), null, 100);
            count = 0; while (Utils.isElementPresent(this, By.id(btn_about_close), false) && count < 10) { Thread.sleep(500); count++; }

            SetParentHandle(mDriver.getWindowHandle());
            return true;
        }
        catch (Exception e)
        {
            mTestRunner.WriteLog("ERROR: Failed to login to the iConsole (" + e.getMessage() + ")", LogFile.iERROR);
            return false;
        }
    }

    public void Stop() throws Exception
    {
        String lnk_logoff = Utils.SetParam(this, "LNK_LOGOFF");
        if (mParentHandle != null && !mParentHandle.isEmpty())
        {
            Utils.selectWindow(this, mParentHandle);
        }
        if (Utils.isElementPresent(this, By.id(lnk_logoff), false))
        {
            Utils.Logoff(this);
        }

        mIsStarted = false;
        if (mFailed)
        {
            mTestRunner.CloseLogFileGroup("Test Case '" + mTestName + "' Failed due to error: " + mFailedReason, LogFile.iINFO);
        }
        else
        {
            mTestRunner.CloseLogFileGroup("Test Case '" + mTestName + "' completed execution (see Summary for individual test details)", LogFile.iINFO);
        }
        mTestRunner.WriteLog(" ", LogFile.iINFO);
    }

    // only call this method to report terminal errors - exceptions, javascript errors etc
    public void ReportError(String sErrorMessage)
    {
        mTestRunner.ReportError(sErrorMessage);
        mFailed = true;
        mFailedReason = sErrorMessage;
    }

    public String GetTestName()
    {
        return mTestName;
    }

    public String GetClassName()
    {
        return mClassName;
    }

    public void AddParameter(String key, String value)
    {
        mParameters.put(key,value);
    }

    public void SetParameters(Map<String,String> params)
    {
        mParameters.putAll(params);
    }

    public Map<String,String> GetParameters()
    {
        return mParameters;
    }

    public void SetTestUsers(Map<String,TestUser> users)
    {
        mTestUsers.putAll(users);
    }

    public Map<String,TestUser> GetTestUsers()
    {
        return mTestUsers;
    }

    public void SetAttribute(String key, String value)
    {
        mAttributes.put(key, value);
    }

    public String GetAttribute(String key) throws Exception
    {
        String value = mAttributes.get(key);
        if (value == null || value.isEmpty())
        {
            throw new Exception("Attribute: " + key + " is null.");
        }

        return value;
    }
    
}


        /* alt way to attack the domain login dialog if AutoIT not working for some reason
        switch(mTestRunner.GetTargetBrowser()) 
        {            
            case "ie9": case "ie10":
                mDriver.get(mURL);
                Thread.sleep(1000);
                mDriver.switchTo().alert();

                try {
                    Robot robot = new Robot();
                    String user = mTestRunner.GetConfigurationFile().GetDomainUser();
                    
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection stringSelection = new StringSelection( user );
                    clipboard.setContents(stringSelection, null);

                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
 
                    robot.keyPress(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    Thread.sleep(500);
                    String pw = mTestRunner.GetConfigurationFile().GetDomainUserPassword();
                    stringSelection = new StringSelection( pw );
                    clipboard.setContents(stringSelection, null);

                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);

                } catch (AWTException e) {
                    mTestRunner.WriteLog("ERROR " + e.getMessage(), LogFile.iERROR);
                }
                break;
            default:
                // other stuff, e.g. use AutoIT exe
                break;
         }
         * 
         */