package testcases;

/*
 *
 * @author dicri02
 *
 */

import common.*;
import org.openqa.selenium.*;
import org.w3c.dom.*;
import java.util.*;

public class MyPrefs extends WgnTestStep
{
    protected DataReader mDR;    
    private Map<String, String> params;
    private String sPrefsLnk, sPrefs;
    private String sTAB_SEARCHES;
    private String sBTN_PASSWORD, sTXT_CURRENTPW;
    private String sTXT_NEWPW, sTXT_CONFIRMPW, sBTN_PW_OK;
    private String sDLG_MESSAGE, sBTN_MESSAGE_CLOSE, sBTN_MESSAGE_YES;
    private String sBTN_PREFS_OK, sBTN_PREFS_CANCEL;
    private String sBTN_PREFS_RESET, sBTN_PREFS_XCLOSE, sBTN_PREFS_APPLY;
    private String sHIVIS, sSTDVIS;
    
    public MyPrefs() 
    {
        mDR = new DataReader();
    }

    @Override
    public void Run() throws Exception
    {
        // load the prefs from the DataStructure XML
        mTestRunner.WriteLog("Data Structure file: " + mParameters.get("dataStructure"), LogFile.iDEBUG);
        Boolean bCfg = mDR.Load(mParameters.get("dataStructure"));

        if (bCfg)
        {
            setParameters();
            // open the Settings dialog
            Utils.DoClick(mTestCase, By.id(sPrefsLnk), By.id(sPrefs), 10000);
            
            NodeList tabs = mDR.GetNodesByName("TAB");
            if (tabs != null && tabs.getLength() > 0)
            {
                // verify defaults
                mTestRunner.NewLogFileGroup("Default verification for Local Preferences.", LogFile.iINFO);
                mDR.processNodeTypes(mTestCase, tabs, sPrefs, mDR.POPULATION);
                mTestRunner.CloseLogFileGroup("Default verification tests completed for Local Preferences.", LogFile.iINFO);
                // Save button tests
                saveTests(tabs);
                // reset button test
                resetButtonTests(tabs);
                // run the change password test
                passwordTests();
            }
            mTestRunner.WriteLog("End of My Profile tests as " + mTestCase.GetAttribute("username"), LogFile.iINFO);
        } else {
            mTestRunner.WriteLog(mDR.GetFailureMessage(), LogFile.iERROR);
        }
    }
    
    private void setParameters() 
    {
        params = mDR.GetParamTable();
        mDR.DebugHashMap(mTestCase, params);
        sPrefsLnk = mDR.GetAttributeValue(mDR.GetNodesByName("PREFS").item(0), "clickid");
        sPrefs = mDR.GetAttributeValue(mDR.GetNodesByName("PREFS").item(0), "checkid");
        sTAB_SEARCHES = params.get("TAB_SEARCHES");
        sBTN_PASSWORD = params.get("BTN_PASSWORD");
        sTXT_CURRENTPW = params.get("TXT_CURRENTPW");
        sTXT_NEWPW = params.get("TXT_NEWPW");
        sTXT_CONFIRMPW = params.get("TXT_CONFIRMPW");
        sBTN_PW_OK = params.get("BTN_PW_OK");
        sDLG_MESSAGE = params.get("DLG_MESSAGE");
        sBTN_MESSAGE_CLOSE = params.get("BTN_MESSAGE_CLOSE");
        sBTN_MESSAGE_YES = params.get("BTN_MESSAGE_YES");
        sBTN_PREFS_OK = params.get("BTN_PREFS_OK");
        sBTN_PREFS_RESET = params.get("BTN_PREFS_RESET");
        sBTN_PREFS_XCLOSE = params.get("BTN_PREFS_XCLOSE");
        sBTN_PREFS_APPLY = params.get("BTN_PREFS_APPLY");
        sBTN_PREFS_CANCEL = params.get("BTN_PREFS_CANCEL");
        sHIVIS = params.get("HIVIS");
        sSTDVIS = params.get("STDVIS");
    }
    
    private void saveTests(NodeList tabs) throws Exception
    {
        // step 1: change the settings, logoff and back in - check values were saved
        mTestRunner.NewLogFileGroup("Test that changes to Local Preferences are saved:", LogFile.iINFO);

        Utils.DoClick(mTestCase, By.id(sBTN_PREFS_APPLY), null, 250);
        // wait until the Apply button becomes disabled
        Boolean bDisBtn = false; int i = 0;
        while (!bDisBtn && i < 20) 
        {             
            if (Utils.GetAttribute(mTestCase, By.id(sBTN_PREFS_APPLY), "class", false).contains("disabledBtn")) bDisBtn = true;
            Thread.sleep(500); i++;
        }
        Utils.DoClick(mTestCase, By.id(sBTN_PREFS_OK), null, 100);
        i=0; while (Utils.isElementPresent(mTestCase, By.id(sPrefs), false) && i < 10) { Thread.sleep(500); i++; }
        if (i==10) Utils.DoClick(mTestCase, By.id(sBTN_PREFS_OK), null, 500);
        Utils.Logoff(mTestCase);
        Utils.Login(mTestCase, "primary");
        Utils.DoClick(mTestCase, By.id(sPrefsLnk), By.id(sPrefs), 10000);

        // step 2: check values were saved and confirm the local prefs have overridden the global prefs in the session
        mDR.processNodeTypes(mTestCase, tabs, sPrefs, mDR.NORESTORATION);
        verifySessionPrefs(tabs, "new saved values");

        mTestRunner.CloseLogFileGroup("Save change tests completed for Local Preferences.", LogFile.iINFO);
    }
    
    
    private void resetButtonTests(NodeList tabs) throws Exception
    {
        //  Reset button tests - also restores the default values for other tests
        try
        {
            // Click Reset button on My Profile
            mTestRunner.NewLogFileGroup("Check defaults are restored by Reset button:", LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id(sBTN_PREFS_RESET), By.id(sBTN_MESSAGE_YES), 100);
            Utils.DoClick(mTestCase, By.id(sBTN_MESSAGE_YES), null, 500);
            Utils.DoClick(mTestCase, By.id(sBTN_PREFS_XCLOSE), null, 100);

            // Logout and back in
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "primary");

            // Open Settings dialog and verify that defaults have been restored correctly
            Utils.DoClick(mTestCase, By.id(sPrefsLnk), By.id(sPrefs), 10000);
            mDR.processNodeTypes(mTestCase, tabs, sPrefs, mDR.NOPOPULATION);
            verifySessionPrefs(tabs, "restored default values");

            mTestRunner.CloseLogFileGroup("Reset test complete", LogFile.iINFO);
        } 
        catch (Exception e) 
        {
            mTestRunner.CloseLogFileGroup("Reset test failed: " + e.toString(), LogFile.iERROR);
            if ( Utils.isElementPresent(mTestCase, By.id(sPrefs), false) )
            {
                Utils.DoClick(mTestCase, By.id(sBTN_PREFS_CANCEL), null, 500);
                // sometimes an error dialog is opened
                if (Utils.isElementPresent(mTestCase, By.id("Message"), false) )
                {
                    Utils.DoClick(mTestCase, By.id("btn_Message.No"), null, 100, false);
                }
                int i = 0; while (Utils.isElementPresent(mTestCase, By.id(sPrefs), false) && i < 10) { Thread.sleep(500); i++; }
            }
        }
    }

    private void verifySessionPrefs(NodeList tabs, String desc) throws Exception
    {
        mTestRunner.NewLogFileGroup("Checking the loaded session prefs match the " + desc + ":", LogFile.iINFO);
        
        for (int i=0; i < tabs.getLength(); i++)
        {
            Node tab = tabs.item(i);
            NodeList children = tab.getChildNodes();
            
            for (int j=0; j < children.getLength(); j++)
            {
                if (children.item(j).getNodeType() == Node.ELEMENT_NODE) 
                {
                    Node child = children.item(j); 
                    String sId = mDR.GetAttributeValue(child, "clickid");
                    String sDesc = mDR.GetAttributeValue(child, "label");
                    String sDef = mDR.GetAttributeValue(child, "default");
                    String sType = child.getNodeName();
                    String sSave = "off";
                    if (sType.contentEquals("CHECKBOX"))
                    {   
                        if (sDef.contentEquals("off")) sSave = "on";
                    } else {
                        sSave = mDR.GetAttributeValue(child, "testval");
                    }
                    String sEval = "oPrefs.prefs." + sId;
                    if (sId.contentEquals("HighVis")) sEval = "oInit.visibility";
                    if (sId.contentEquals("ContentProxy")) sEval = "oPrefs.prefs.ContentProxies[0].text";

                    String syspref;
                    if (sId.startsWith("Display") || sId.contentEquals("HighVis") || sId.startsWith("Content"))
                    {
                        syspref = (String) ((JavascriptExecutor) mDriver).executeScript("return " + sEval);
                    } 
                    else if (sId.startsWith("Search")) 
                    {
                        syspref = ((Long) ((JavascriptExecutor) mDriver).executeScript("return " + sEval)).toString();
                    } else {
                        syspref = ((Boolean) ((JavascriptExecutor) mDriver).executeScript("return " + sEval)).toString();
                    }

                    String sCompare = sDef;
                    if (desc.contains("default")) 
                    {
                        if (sId.contentEquals("HighVis"))
                        {
                            sCompare = sSTDVIS;
                        } 
                        else if (sType.contentEquals("SELECT") && !sId.startsWith("Content")) 
                        {
                            sCompare = "1";
                        }
                    } else {
                        if (sId.contentEquals("HighVis"))
                        {                        
                            sCompare = sHIVIS;
                        }
                        else if (sType.contentEquals("SELECT") && !sId.startsWith("Content")) 
                        {
                            sCompare = "0";
                        } 
                        else 
                        {
                            sCompare = sSave;
                        }
                    }
                    if (sCompare.contentEquals("on")) sCompare = "true";
                    if (sCompare.contentEquals("off")) sCompare = "false";
                    Utils.CompareValues(mTestCase, syspref, sDesc, sCompare);
                }
            }
        }
        mTestRunner.CloseLogFileGroup("Session prefs tests complete", LogFile.iINFO);
    }

    private void passwordTests() throws Exception
    {
        boolean Success = false;
        String testPassword = "$_GOGOGO_$";
        mTestRunner.NewLogFileGroup("My Profile - Change Password test started", LogFile.iINFO);
        changePassword(mTestCase.GetAttribute("password"), testPassword);
        Utils.Login(mTestCase, testPassword);
        if ( Utils.isElementPresent(mTestCase, By.id(sTAB_SEARCHES)) )
        {
            mTestRunner.WriteLog("Password changed to '"+ testPassword +"', post-change login successful.", LogFile.iTEST);
            // restore password for other tests
            changePassword(testPassword, mTestCase.GetAttribute("password"));
            Utils.Login(mTestCase, "primary");
            if ( Utils.isElementPresent(mTestCase, By.id(sTAB_SEARCHES)) )
            {
                mTestRunner.WriteLog("Password restored to '"+ mTestCase.GetAttribute("password") +"', post-change login successful.", LogFile.iTEST);
                Success = true;
            }
        }
        if (Success)
        {
            mTestRunner.CloseLogFileGroup("PASS: Change Password test PASSED", LogFile.iINFO);
        } else {
            mTestRunner.CloseLogFileGroup("FAIL: Change password test FAILED", LogFile.iERROR);
        }
    }

    private void changePassword(String oldPassword, String newPassword) throws Exception
    {
        // change the password and logoff
        if (!Utils.isElementPresent(mTestCase, By.id(sPrefs), false))
        {
            Utils.DoClick(mTestCase, By.id(sPrefsLnk), By.id(sPrefs), 10000);
        }
        Utils.DoClick(mTestCase, By.id(sBTN_PASSWORD), By.id(sTXT_CURRENTPW), 10000);
        int i=0; while (!Utils.isDisplayed(mTestCase,  By.id(sTXT_CURRENTPW)) && i < 10 ) { Thread.sleep(500); i++; }
        Utils.DoType(mTestCase, By.id(sTXT_CURRENTPW), oldPassword);
        Utils.DoType(mTestCase, By.id(sTXT_NEWPW), newPassword);
        Utils.DoType(mTestCase, By.id(sTXT_CONFIRMPW), newPassword);
        Utils.DoClick(mTestCase, By.id(sBTN_PW_OK), By.id(sDLG_MESSAGE), 5000);
        Utils.DoClick(mTestCase, By.id(sBTN_MESSAGE_CLOSE), null, 0);
        Utils.DoClick(mTestCase, By.id(sBTN_PREFS_OK), null, 0);
        i=0; while (Utils.isElementPresent(mTestCase, By.id(sPrefs), false) && i < 10) { Thread.sleep(500); i++; }
        Utils.Logoff(mTestCase);
    }

}
