/* Copyright © 2011 CA. All rights reserved. */

package common;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import testcases.*;
import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang.StringUtils; // pre selenium-standalone-server-2.28.0.jar
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import org.openqa.selenium.interactions.Actions;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import org.joda.time.*;   // for GetTimeSinceEpoch()

public class Utils {

    public static boolean GetUserPreferences(WgnTestCase wtc) throws Exception
    {
        // get the current user preferences for the actions on auditing a search result
        WebDriver dr = wtc.GetWebDriver();
        String lnk_prof = SetParam(wtc, "LNK_PREFS");
        String dlg_prefs = SetParam(wtc, "DLG_PREFS");
        String btn_cancel = SetParam(wtc, "BTN_PREFS_CANCEL");
        String pref_move = SetParam(wtc, "CHK_PREF_MOVE");
        String pref_remove = SetParam(wtc, "CHK_PREF_REMOVE");
        
        // Open the preferences dialog
        DoClick(wtc, By.id(lnk_prof), By.id(dlg_prefs), 10000, false);
        // Get the user preferences
        boolean removeAuditedEvents = dr.findElement(By.id(pref_remove)).isSelected();
        boolean moveNextEvent = dr.findElement(By.id(pref_move)).isSelected();

        try 
        {
             wtc.GetTestRunner().CreateUserPreferences(removeAuditedEvents, moveNextEvent);
        }
        catch(Exception e) 
        {
            System.err.println("Exception creating user preferences:" + e.getMessage());
        }
        // Now close the preferences dialog
        DoClick(wtc, By.id(btn_cancel), null, 100);
        int i=0; while (isElementPresent(wtc, By.id(dlg_prefs), false) && i < 10) { Thread.sleep(500); i++; }

        return true;
    }

    public static boolean SetSearchAttributes(WgnTestCase wtc)
    {
        // set the search attribute values for page size and number of results in the test case
        WebDriver dr = wtc.GetWebDriver();
        String numSearchResults = (String) ((JavascriptExecutor) dr).executeScript("return oResultsManager.oResults.last_record;");

        if (numSearchResults == null || numSearchResults.isEmpty())
        {
            wtc.ReportError("Utils.SetSearchAttributes() - expected attribute 'recordSize' is null or empty");
            return false;
        }

        wtc.SetAttribute("numSearchResults", numSearchResults);

        String pageSize = (String) ((JavascriptExecutor) dr).executeScript("return oResultsManager.oResults.page_size;");                

        if (pageSize == null || pageSize.isEmpty())
        {
            wtc.ReportError("Utils.SetSearchAttributes() - expected attribute 'pageSize' is null or empty");
            return false;
        }

        wtc.SetAttribute("pageSize", pageSize);
        return true;
    }
    
    public static String SetParam(WgnTestCase wtc, String paramName)
    {
        // this function should be called whenever retrieving params from the XML
        String param = wtc.GetParameters().get(paramName);
        if (param == null || param.isEmpty())
        {
            wtc.ReportError(paramName + " parameter is empty - check your config");
            return "Bugger it!";
        }
        return param;
    }
    
    public static void Click(WgnTestCase wtc, By clickElement, int timeout) throws Exception
    {
        // alt to DoClick() for when a click will close a browser window.
        // in this scenario the checks for js errors in DoClick() provoke an error as they try to act on the destroyed window
        WebDriver dr = wtc.GetWebDriver();
        TestRunner tr = wtc.GetTestRunner();
        try
        {
            dr.findElement(clickElement).click(); 
            if (timeout > 0)
            {
                Thread.sleep(timeout);                
            }
        } 
        catch (Exception e) 
        {
            tr.WriteLog("Utils.Click() caught exception: " + e.toString(), LogFile.iDEBUG);
            throw e;
        }
        
    }
    
    public static void DoubleClick(WgnTestCase wtc, By clickElement, int timeout) throws Exception
    {
        // DoubleClick() on a webelement
        WebDriver dr = wtc.GetWebDriver();
        TestRunner tr = wtc.GetTestRunner();
        try
        {
            WebElement element = dr.findElement(clickElement);
            Actions builder = new Actions(dr);
            builder.moveToElement(element).perform();
            builder.doubleClick().perform();
            
            if (timeout > 0)
            {
                Thread.sleep(timeout);                
            }
        } 
        catch (Exception e) 
        {
            tr.WriteLog("Utils.DoubleClick() caught exception: " + e.toString(), LogFile.iDEBUG);
            throw e;
        }        
    }
    
    public static void DoClick(WgnTestCase wtc, By clickElement, By waitElement, int timeout) throws Exception
    {
        DoClick(wtc, clickElement, waitElement, timeout, true);
        
    }
    public static void DoClick(WgnTestCase wtc, By clickElement, final By waitElement, int timeout, Boolean dolog) throws Exception
    {
        // wraps the WebDriver click() method to handle waits and capture any js errors provoked by a click
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        String DLG_PROGRESS = SetParam(wtc, "DLG_PROGRESS");

        try
        {
            WebElement clickme = dr.findElement(clickElement);
            // check the element is in view on screen before clicking it
            String myId = clickme.getAttribute("id");
            if (myId == null || myId.isEmpty()) myId = "null";
            tr.WriteLog("myId for click = " + myId, LogFile.iDEBUG);
            if (!myId.startsWith("cxt") && !myId.contains("sdfile") && !myId.startsWith("lnk_") && !myId.startsWith("tab"))
            {
                try 
                {
                    ((JavascriptExecutor) dr).executeScript("arguments[0].scrollIntoView(true);", clickme);
                    tr.WriteLog("DBG: click element scrolled into view", LogFile.iDEBUG);
                } 
                catch (Exception e) 
                {
                    tr.WriteLog("INFO: Couldn't scroll element " + clickElement + ", into view: " + e.getMessage(), LogFile.iINFO);
                }
            }
            // click() fails for some webelements on the chromeDriver so use the ENTER key instead, or SPACE for buttons
            if (tr.GetTargetBrowser().contains("chrome"))
            {
                String myType = clickme.getAttribute("type");
                if (myType == null || myType.isEmpty())  myType = "null";
                String myTagName = clickme.getTagName();
                tr.WriteLog("DoClick(): myId=" + myId + " myType=" + myType + " myTagName=" + myTagName, LogFile.iDEBUG);
                // move the focus to the click element
                Actions moveme = new Actions(dr);
                moveme.moveToElement(clickme).perform();
                
                /*if (myId.startsWith("btn") && !myId.contains("btn_About.Close")) 
                {
                    tr.WriteLog("DBG: sending SPACE key instead of click - Chrome", LogFile.iDEBUG);
                    clickme.sendKeys(Keys.SPACE);
                } 
                else
                 */
                if (!myId.startsWith("cxt") && !myId.contains("oggle") && !myId.startsWith("lnk_") && !myType.contentEquals("radio")
                        && !myId.contains("sdfile") && !myType.contentEquals("checkbox") && !myId.startsWith("treeChk") && !myId.startsWith("Dlg")
                        && !myTagName.contentEquals("span"))
                {
                    tr.WriteLog("DBG: sending ENTER key instead of click - Chrome", LogFile.iDEBUG);
                    clickme.sendKeys(Keys.ENTER);
                } else {
                    clickme.click();
                }
            } else {
                clickme.click();
            }
            // now look for the waitElement if one was supplied
            if (waitElement != null)
            {
                try
                {
                    WebElement element = (new WebDriverWait(dr, timeout/1000))
                            .until(new ExpectedCondition<WebElement>(){
                                @Override
                                public WebElement apply(WebDriver d) {
                                    return d.findElement( waitElement );
                                   }});                    
                    String waitEl = (String) ((JavascriptExecutor) dr).executeScript("return arguments[0].tagName" , element);
                    if (dolog) tr.WriteLog("INFO: Wait Element found: " + waitEl + " id=" + element.getAttribute("id"), LogFile.iNONE);
                }
                catch (Exception e)
                {                    
                    tr.WriteLog("FAIL: expected result not achieved by click, wait element not found: " + waitElement.toString(), LogFile.iERROR);
                    tr.WriteLog("INFO: " + e.getMessage(), LogFile.iINFO);
                }
            }
            else if (waitElement == null && timeout > 0)
            {
                Thread.sleep(timeout);                
            }
            int i = 0; while (Utils.isElementPresent(wtc, By.id(DLG_PROGRESS), false) && i < 20) { Thread.sleep(500); i++; }
            // check here for js errors where the click() succeeded but a subsequent action or method called failed
            GetLastJavaScriptError(wtc);            
        } 
        catch (org.openqa.selenium.NoSuchElementException nse)
        {
            // this line is handy when a bad click value is passed from XML->Java->to here which can be hard to spot
            tr.WriteLog("Utils.DoClick() caught NoSuchElementException for " + clickElement.toString(), LogFile.iNONE);            
            throw nse;
        }
        catch (Exception e) 
        {
            tr.WriteLog("Utils.DoClick() caught exception: " + e.toString(), LogFile.iDEBUG);
            // catches js errors provoked by the click() action
            GetLastJavaScriptError(wtc);
            throw e;
        }
    }
    
    public static void GetLastJavaScriptError(WgnTestCase wtc)
    {
        // retrieves the last javascript error that occurred from the browser
        WebDriver dr = wtc.GetWebDriver();
        String sJSError = (String) ((JavascriptExecutor) dr).executeScript("return getLastJavascriptError();");
        if (!sJSError.equals(""))
        {
            wtc.ReportError("Detected javascript " + sJSError);
        }
    }

    public static void CompareTextBoxValue(WgnTestCase wtc, By by, String strDesc, String strValue) throws Exception
    {
        // check that the current text box value matches the expected value
        TestRunner tr = wtc.GetTestRunner();
        String test = GetAttribute(wtc, by, "value", false);
        if (test.isEmpty()) test = "<blank>";
        if ( strValue == null || strValue.isEmpty() ) strValue = "<blank>";
        
        if (test.equals(strValue))
        {
            tr.WriteLog("PASS: " + strDesc + " -> value = " + test, LogFile.iTEST);
        } else {
            tr.WriteLog("FAIL: " + strDesc + " -> value = " + test + ", Expected: " + strValue, LogFile.iERROR);
        }
    }
    
    public static boolean isSomethingSelected(WgnTestCase wtc, By by) throws Exception
    {
        // Determines whether any option in a drop-down menu is selected
        WebDriver dr = wtc.GetWebDriver();
        WebElement select = dr.findElement(by);
        String tagName = select.getTagName().toLowerCase();
        if (!"select".equals(tagName)) {
            throw new Exception("Specified element is not a Select");
        }

        for (WebElement option : select.findElements(By.tagName("option"))) {
              if (option.isSelected()) {
                return true;
              }
        }
        return false;
    }
    
    public static void CompareSelectLabel(WgnTestCase wtc, By by, String strDesc, String strValue) throws Exception
    {
        // check that the current value of a select element matches the expected value
        TestRunner tr = wtc.GetTestRunner();
        
        if (Utils.isSomethingSelected(wtc, by))
        {
            String selLabel = GetSelectedLabel(wtc, by);
            // 'navPattern' is a special case for the search results navigator dropdown
            if (strValue.contentEquals("navPattern"))
            {
                // look for "1 to x of y" with varying number of spaces inbetween
                // string can contain &nbsp; as well as ordinary space - so look for spaces like this: [\\p{Z}\\s]
                if (selLabel.matches("^[\\p{Z}\\s]*1[\\p{Z}\\s]+to[\\p{Z}\\s]+\\d+[\\p{Z}\\s]+of[\\p{Z}\\s]+\\d+"))  
                {
                    tr.WriteLog("PASS: default navigation selection for " + strDesc + " matches expected pattern (" + selLabel + ")", LogFile.iTEST);
                } else {
                    tr.WriteLog("FAIL: default navigation selection for " + strDesc + " doesn't match expected pattern (" + selLabel + ")", LogFile.iERROR);
                }
            }
            else 
            {
                if (selLabel.contains(strValue)) 
                {
                    tr.WriteLog("PASS: selected value for " + strDesc + " is correct (" + strValue + ")", LogFile.iTEST);
                // Review tab Std Search File Scope field fails to give up it's selected text. No idea why!! Write a Known Fail for now.
                } else if (strDesc.contentEquals("Scope:") && strValue.contentEquals("Workstation and Server")) {
                    tr.WriteLog("KNOWN FAIL: selected text for " + strDesc + " cannot be retrieved", LogFile.iKNOWN);
                } else {
                    tr.WriteLog("FAIL: selected value for " + strDesc + " is incorrect (" + selLabel + ")", LogFile.iERROR);
                }
            }
        } else {
            if (strValue.contentEquals("")) 
            {
                tr.WriteLog("PASS: nothing is selected in the " + strDesc + " select box", LogFile.iTEST);
            } else {
                tr.WriteLog("FAIL: nothing is selected in the " + strDesc + " select box (expected: " + strValue + ")", LogFile.iERROR);
            }
        }        
    }

    public static Boolean isChecked(WgnTestCase wtc, By by, String strDesc, String strChecked, Boolean log) throws Exception
    {
        // verifies the current checkbox state against the expected value
        // this function is a TEST and writes pass/fail entries based on the expected value of the checkbox
        // if you simply want to get the current value of a checkbox use isSelected() instead.
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        Boolean bool = false;
        try 
        {
            bool = dr.findElement(by).isSelected();
        } 
        catch (Exception e) 
        {
            if (strChecked.contentEquals("false")) bool = true;
            tr.WriteLog("ERROR: Element not found (" + e.getMessage() + ")", LogFile.iERROR);
        }
                 
        if ( (bool && (strChecked.contentEquals("true") || strChecked.contentEquals("on")) ) 
               || (!bool && (strChecked.contentEquals("false") || strChecked.contentEquals("off")) ) )
        {
            if (log) tr.WriteLog("PASS: " + strDesc + " -> CHECKED = " + strChecked, LogFile.iTEST);
            return true;
        } else {
            if (log) tr.WriteLog("FAIL: " + strDesc + " -> CHECKED = " + bool + ", Expected: " + strChecked, LogFile.iERROR);
            return false;
        }
    }
    
    public static void CompareValues(WgnTestCase wtc, String strPref, String strDesc, String strExpValue) throws Exception
    {
        // support the original use of compareValues for Session Prefs without updating all the calls in MyPrefs and GPrefs 
        CompareValues(wtc, strPref, strDesc, strExpValue, "Session Pref");
    }

    public static void CompareValues(WgnTestCase wtc, String strPref, String strDesc, String strExpValue, String strType) throws Exception
    {
        // check that actual values match expected values
        TestRunner tr = wtc.GetTestRunner();
        
        if (strPref.contentEquals(strExpValue))
        {
            tr.WriteLog("PASS: " + strType + " for '" + strDesc + "' -> value = " + strExpValue, LogFile.iTEST);
        } else {
            tr.WriteLog("FAIL: " + strType + " for '" + strDesc + "' -> value = " + strPref + ", Expected: " + strExpValue, LogFile.iERROR);
        }
    }

    public static String ReplaceChars(String oldStr)
    {
    /* the following characters are escaped to '_' in event subjects before being used in iConsole window titles, filenames etc:
     *      '?' '[' ']' '/' '\\' '=' '+' '<' '>' ':' ';' '\'' ',' '*' '#' '&' '\"'
     * so we need to do the same before comparing the subject line to any retrieved window titles, filenames etc
     */
        int numChar = oldStr.length();
        char[] charArray = new char[numChar];
        oldStr.getChars(0, numChar, charArray,0);
        int i=0;
        while(i < charArray.length)
        {
            if (charArray[i] == '?' ||
                charArray[i] == '[' ||
                charArray[i] == ']' ||
                charArray[i] == '/' ||
                charArray[i] == '\\' ||
                charArray[i] == '=' ||
                charArray[i] == '+' ||
                charArray[i] == '<' ||
                charArray[i] == '>' ||
                charArray[i] == ':' ||
                charArray[i] == ';' ||
                charArray[i] == '\'' ||
                charArray[i] == ',' ||
                charArray[i] == '*' ||
                charArray[i] == '#' ||
                charArray[i] == '&' ||
                charArray[i] == '\"'
                    )
            {
              charArray[i] = '_';
            }
            i++;
        }
        String newStr = new String(charArray);
        return newStr;
    }

    public static void Logoff(WgnTestCase wtc) throws Exception
    {
        // logout of the iConsole
        TestRunner tr = wtc.GetTestRunner();
        String DLG_MESSAGE = SetParam(wtc, "DLG_MESSAGE");
        String BTN_MESSAGE_YES = SetParam(wtc, "BTN_MESSAGE_YES");
        String LNK_LOGOFF = SetParam(wtc, "LNK_LOGOFF");
        String LOGIN_BRAND = SetParam(wtc, "LOGIN_BRAND");
        
        try
        {
            Click(wtc ,By.id(LNK_LOGOFF), 500);
            int i = 0; 
            while (!isElementPresent(wtc, By.id(BTN_MESSAGE_YES), false) && i < 10) { Thread.sleep(1000); i++; }
            if (isElementPresent(wtc, By.id(DLG_MESSAGE), false)) 
            {
               Click(wtc, By.id(BTN_MESSAGE_YES), 100);
            } else {
               Click(wtc ,By.id(LNK_LOGOFF), 3000);
               if (isElementPresent(wtc, By.id(DLG_MESSAGE), false)) { Click(wtc, By.id(BTN_MESSAGE_YES), 100); }
            }
            
            for (int count = 0; count < 30; count++)
            {               
                Thread.sleep(1000);
                if (isElementPresent(wtc, By.id(LOGIN_BRAND), false)) 
                {
                    tr.WriteLog("Logout successful", LogFile.iINFO);
                    break;
                } 
                else if (count == 29) 
                {
                    tr.ReportError("ERROR: Logout has failed");
                }
            }
        }
        catch (Exception e)
        {
            // catches js errors provoked by the Logoff process
            GetLastJavaScriptError(wtc);
            throw e;
        }
    }

    public static void Login(WgnTestCase wtc, String logintype) throws Exception
    {
        // performs a non-SSO login as part of a test case
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        String DLG_PROGRESS = SetParam(wtc,"DLG_PROGRESS");
        String TAB_HOME = SetParam(wtc, "TAB_HOMEPAGE");
        final String TAB_REVIEW = SetParam(wtc, "TAB_REVIEW");
        String DLG_MESSAGE = SetParam(wtc, "DLG_MESSAGE");
        String BTN_MESSAGE_CLOSE = SetParam(wtc, "BTN_MESSAGE_CLOSE");
        String SUBTAB_REPORTS = SetParam(wtc, "TAB_REVIEW_REPORTS");
        String MYPORTAL = SetParam(wtc, "HOME_PORTAL");
        String BTN_LOGIN = SetParam(wtc, "BTN_LOGIN");
        String LOGIN_ERROR = SetParam(wtc, "LOGIN_ERROR");
        String BTN_FAV_STD_SEARCH = SetParam(wtc, "BTN_FAV") + SetParam(wtc, "StdSrchHash");
        String LOGIN_USER = SetParam(wtc, "LOGIN_USERNAME");
        String LOGIN_PW = SetParam(wtc, "LOGIN_PASSWORD");
        
        int count = 0;
        String username = "";
        String password = "";
        // set the right login name and pw for the login type
        switch (logintype) {
            case "primary":
                username = wtc.GetAttribute("username");
                password = wtc.GetAttribute("password");
                break;
            case "secondary":
                username = wtc.GetAttribute("rlsusername");
                password = wtc.GetAttribute("rlspassword");
                break;
            case "admin":
                username = wtc.GetAttribute("adminuser");
                password = wtc.GetAttribute("adminpw");
                break;
            default:
                username = wtc.GetAttribute("username");
                password = logintype;
                break;
        }
        // enter the username and pw on the login page
        DoType(wtc, By.id(LOGIN_USER), username);
        DoType(wtc, By.id(LOGIN_PW), password);

        try 
        {
            // click Login and wait for the Searches tab to appear
            dr.findElement(By.id(BTN_LOGIN)).click();
            (new WebDriverWait(dr, 120))
                .until(new ExpectedCondition<WebElement>(){
                @Override
                public WebElement apply(WebDriver d) {
                    return d.findElement(By.id(TAB_REVIEW));
                }});
            Thread.sleep(1000);
            // now handle the iConsole's different entry points
            if (!isElementPresent(wtc, By.id(TAB_HOME), false))
            {   // we're going straight to the Review tab
                while ( !isElementPresent(wtc, By.id(SUBTAB_REPORTS), false) && count < 30 ) { Thread.sleep(1000); count++; }
            } else {
                // we're on the HomePage so wait for the portals to load
                while ( !isElementPresent(wtc, By.id(MYPORTAL), false) && count < 30 ) { Thread.sleep(1000); count++; }
                // there may be a message popup - if so dismiss it
                if (isElementPresent(wtc, By.id(DLG_MESSAGE), false))
                {
                    dr.findElement(By.id(BTN_MESSAGE_CLOSE)).click();
                }
                // the HomePage is loaded, now switch to the Review tab
                Utils.DoClick(wtc, By.id(TAB_REVIEW), null, 500, false);                
                count = 0; while ( isElementPresent(wtc, By.id(DLG_PROGRESS), false) && count < 60 ) { Thread.sleep(500); count++; }
                count = 0; while ( !isElementPresent(wtc, By.id(BTN_FAV_STD_SEARCH), false) && !isTextPresent(wtc, "No searches found", false) && count < 40 ) { Thread.sleep(500); count++; }
                if (!isElementPresent(wtc, By.id(BTN_FAV_STD_SEARCH), false) && !isTextPresent(wtc, "No searches found"))
                {
                    // the tab click failed so do it again 
                    // because this is an AJAX site we can't know when the page is sufficiently loaded for the Tab click to be successful
                    // so we need this retry occasionally.
                    Utils.DoClick(wtc, By.id(TAB_REVIEW), null, 500, false);                
                    count = 0; while ( isElementPresent(wtc, By.id(DLG_PROGRESS), false) && count < 60 ) { Thread.sleep(500); count++; }
                }
            }
            if (isElementPresent(wtc, By.id(BTN_FAV_STD_SEARCH), false) )
            {
                tr.WriteLog("Login successful as " + username, LogFile.iINFO);
            } 
            else if (isTextPresent(wtc, "No searches found")) 
            {
                tr.WriteLog("Login successful as " + username + " (no searches are published)", LogFile.iINFO);
            } else {
                tr.WriteLog("ERROR: Login failed as " + username, LogFile.iERROR);
            }
        } 
        catch (Exception e) 
        {
            // catches any js errors provoked during the login process
            GetLastJavaScriptError(wtc);
            if (isElementPresent(wtc, By.id(LOGIN_ERROR), false))
            {
                tr.WriteLog("ERROR: " + Utils.GetText(wtc, By.id(LOGIN_ERROR), false) + "(user=" + username + ")", LogFile.iNONE);
            }
            throw e;
        }

    }
    
    public static String GetSearchParams(WgnTestCase wtc)
    {
        // retrieve the parameters used to run an iConsole search or report
        // only call this when the focus is on a search results window
        return (String) ((JavascriptExecutor) wtc.GetWebDriver()).executeScript("return DLP.Searches.GetParameters();");
    }

    public static void VerifySearchParameter(WgnTestCase wtc, String params, String name, String attrib, String expected) throws Exception
    {
        // parses the full list of parameters used in an iConsole search to check the value for a particular parameter 
        TestRunner tr = wtc.GetTestRunner();
        
        String[] chkEvents = {"chkFE", "chkEE", "chkNE", "chkWE", "chkSE", "chkPE", "chkAME", "chkBB", "chkIME", "chkOE"};
        switch (name) {
            case "All Event Types":
            case "E-mail Events only":
                {
                    // for 'All Dates' check that all event type checkboxes are 'false'
                    // for 'x event type only' allow that type only to be 'true'
                    // n.b. String expected is ignored in this special case
                    Boolean b = true;
                    String content;
                    String types = "";
                    for (int i = 0; i < chkEvents.length; i++)
                    {
                        content = "";
                        String value = StringUtils.substringBetween(params, chkEvents[i], "}");
                        content = StringUtils.substringBetween(value, attrib+":\"", "\"");
                        if (content.contentEquals("true"))
                        {
                            if (name.contentEquals("All Event Types")
                                    || (name.contentEquals("E-mail Events only") && !chkEvents[i].contentEquals("chkEE")) )
                            {
                                b = false;
                                if (types.isEmpty())
                                {
                                    types = chkEvents[i];
                                } else {
                                    types = types + ", " + chkEvents[i];
                                }
                            }
                        }
                    }
                    if (b)
                    {
                         tr.WriteLog("PASS: " + name + " were used in the search", LogFile.iTEST);
                    } else {
                         tr.WriteLog("FAIL: " + name + " were not used in the search -> " + types + " enabled", LogFile.iERROR);
                    }
                    break;
                }
            case "txtTrigger":
            case "lstPolicyClass":
            case "txtSmartTag":
            case "txtClassifier":
            case "txtSpecificMatch":
                {
                    // check the value of elements of type:lookup and lookupbyid
                    // the attrib value should be 'report_value' for elements of type:lookupbyid
                    Boolean bool = true;
                    String[] items = expected.split(";");
                    String value = StringUtils.substringBetween(params, name, "}");
                    String content = StringUtils.substringBetween(value, attrib+":\"", "\"");
                    if (items.length > 0)
                    {
                        for (int i = 0; i < items.length; i++)
                        {
                            if (!content.contains(items[i])) { bool = false; }
                        }
                    } else {
                        bool = false;
                        content = "<blank>";
                    }
                    if (bool)
                    {
                        tr.WriteLog("PASS: the " + name + " element contained the correct entries (" + expected + ")", LogFile.iTEST);
                    } else {
                        tr.WriteLog("FAIL: the " + name + " element didn't contain the correct entries -> " + content, LogFile.iERROR);
                    }
                    break;
                }
            default:
                {
                    // check the value of a single attribute for the supplied parameter name
                    // first retrieve the string for the parameter under test from the output of DLP.Searches.GetParameters()
                    // find the parameter name and do a non-greedy match either side of it up to the closing {}
                    String value = "";
                    Matcher m = Pattern.compile("([^{.]+?"+name+".+?)\\}").matcher(params);
                    if (m.find()) value = m.group(1);
                    // now retrieve the attribute under test from the parameter value
                    String content = StringUtils.substringBetween(value, attrib+":\"", "\"");
                    tr.WriteLog("param "+ name + " = " + value, LogFile.iDEBUG);
                    tr.WriteLog("attrib = " + content, LogFile.iDEBUG);
                    if (content == null || content.isEmpty()) { content = "NULL"; }
                    // convert the date parameter to friendly names (not all combos supported)
                    String sDate = "";
                    switch (content) 
                    {
                        case "2;1;2;1":
                            sDate = "', 'All Dates";
                            break;
                        case "2;1;2;2":
                            sDate = "', 'Today'";
                            break;
                        case "1;1;1;1":
                            sDate = "', 'Specified period: 1 day";
                            break;
                        case "1;2;1;1":
                            sDate = "', 'Specified period: 2 days";
                            break;
                    }
                    if (content.contentEquals(expected))
                    {
                        tr.WriteLog("PASS: The correct '" + name + "' value was used in the search ('" + content + sDate + "')", LogFile.iTEST);
                    } else {
                        tr.WriteLog("FAIL: the '" + name + "' parameter value used in the search was incorrect -> " + content, LogFile.iERROR);
                    }
                    break;
                }
        }
    }
    
    public static void switchToiFrame(WgnTestCase wtc, String frame)
    {
        // switch the webdriver's focus to an iFrame on the page
        // required for the OOTB reports that use XSL formatting e.g. Incidents by Location
        wtc.GetWebDriver().switchTo().frame(frame);
    }
    
    public static void switchBackFromiFrame(WgnTestCase wtc)
    {
        // return the focus from an iFrame to the main page
        wtc.GetWebDriver().switchTo().defaultContent();
    }
    
    public static String GetNewWindow(WgnTestCase wtc, By by) throws Exception
    {
        // open a new window by clicking on a page element e.g. a link
        WebDriver dr = wtc.GetWebDriver();
        String parentHandle = dr.getWindowHandle();        // get the parent window handle
        final Set<String> handles = dr.getWindowHandles(); // handles is the current list of windows BEFORE you open the new one
             
        // click the element that launches the new window
        Utils.DoClick(wtc, by, null, 1000);
        
        (new WebDriverWait(dr, 60)).until
                (new ExpectedCondition<Boolean>() 
                    { 
                        @Override 
                        public Boolean apply(WebDriver d) 
                        {
                            // now the new window is open get the new window list and subtract it from the old one
                            Set<String> newHandles = d.getWindowHandles(); 
                            newHandles.removeAll(handles);
                            // you should be left with just the new window's handle so switch focus to it
                            if (newHandles.size() > 0) 
                            { 
                                d.switchTo().window(newHandles.iterator().next()); 
                                return true; 
                            } else { 
                                return false; 
                            } 
                        } 
                    }
                ); 
        return parentHandle;  // this needs to be stored to return focus to the parent window later
    }
    
    public static void selectWindow(WgnTestCase wtc, String windowHandle) throws Exception
    {
        // To switch to another browser (iConsole) window
        WebDriver dr = wtc.GetWebDriver();
        dr.switchTo().window(windowHandle);
        Thread.sleep(500);
    }
    
    public static void CloseWindow(WgnTestCase wtc, String parentHandle) throws Exception
    {
        // close the active window and restore focus to the parent window
        WebDriver dr = wtc.GetWebDriver();
        if (dr.getWindowHandles().size() > 1)
        {
            dr.close();                             // Close the results window
        }
        dr.switchTo().window(parentHandle);     // To choose the original iConsole window
    }
    public static String GetWindowTitle(WgnTestCase wtc) throws Exception
    {
        // get the title of the active window
        WebDriver dr = wtc.GetWebDriver();
        return dr.getTitle();
    }
    
    public static Boolean isElementPresent(WgnTestCase wtc, By by)
    {
        return isElementPresent(wtc, by, true);
    }    
    
    public static boolean isElementPresent(WgnTestCase wtc, By by, Boolean log)
    {
        // is the requested element contained on the page?
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try
        {
            dr.findElement(by);
            if (log)  tr.WriteLog("PASS: Element found: " + by.toString(), LogFile.iTEST);
            return true;
        }
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            if (log) tr.WriteLog("FAIL: Element not found: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
            return false;
        }
        catch (org.openqa.selenium.WebDriverException wde) // css selectors return this rather than NoSuchElementException
        {
            if (log) tr.WriteLog("FAIL: Element not found: " + by.toString() + " (" + wde.getMessage() + ")", LogFile.iERROR);
            return false;            
        }
    }
    
    public static String GetText(WgnTestCase wtc, By by, Boolean log)
    {
        // get the text content of the current element
        // use for <span>, <td>, <p>, <option> etc - but not <input type=text> where the text is in the value attribute
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        String text;
        try
        {
            text = dr.findElement(by).getText();
            if (log)  tr.WriteLog("INFO: Text found: " + text, LogFile.iINFO);
            if (text != null) return text;
        } 
        catch (Exception e) 
        {
            tr.WriteLog("FAIL: Text not found for: " + by.toString() + " (" + e.getMessage() + ")", LogFile.iERROR);
            return "EXCEPTION";
        }
        return "";
    }
    
    public static String GetAttribute(WgnTestCase wtc, By by, String attrib, Boolean log)
    {
        // get the value of the requested attribute for the web element
        TestRunner tr = wtc.GetTestRunner();
        String text = "ERROR";
        try
        {
            text = wtc.GetWebDriver().findElement(by).getAttribute(attrib);
            if (log)
            {
                if (text != null) 
                {
                    tr.WriteLog("INFO: Attibute value found: " + text, LogFile.iINFO);
                } else {
                    tr.WriteLog("INFO: Attibute value is NULL", LogFile.iINFO);
                }
            }
            return text;
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return "";
    }

    public static Boolean isTextPresent(WgnTestCase wtc, String text)
    {
        return isTextPresent(wtc, text, "", true);
    }    
    public static Boolean isTextPresent(WgnTestCase wtc, String text, String css)
    {
        return isTextPresent(wtc, text, css, true);
    }
    public static Boolean isTextPresent(WgnTestCase wtc, String text, Boolean dolog)
    {
        return isTextPresent(wtc, text, "", dolog);
    }    
    public static Boolean isTextPresent(WgnTestCase wtc, String text, String css, Boolean dolog)
    {
        // is the supplied text contained anywhere on the page?
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        By strBy = By.tagName("body");
        if (!css.isEmpty()) strBy = By.cssSelector(css);
        try
        {
            String searchText = dr.findElement(strBy).getText();
            if (searchText.contains(text)) 
            { 
                return true;
            } else {
                if (dolog) tr.WriteLog("Search text: " + searchText + " (expected text: " + text + ")", LogFile.iINFO);
            }            
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            if (dolog) tr.WriteLog("INFO: Text not found on page: " + text + " (" + nse.getMessage() + ")", LogFile.iINFO);
        }
        catch (Exception e)
        {
            System.err.print(e.getMessage());
            tr.WriteLog(e.getMessage(), LogFile.iERROR);
        }
        return false;
    }
    
    public static Boolean isDisplayed(WgnTestCase wtc, By by)
    {
        // determine whether the element is currently visible on the page
        // method MUST be used on the element with the style="display: none;" attribute set on it and NOT children of that element
        try
        {            
           return wtc.GetWebDriver().findElement(by).isDisplayed();           
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            wtc.GetTestRunner().WriteLog("ERROR: Element not found on page: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return false;
    }
    
    public static Boolean isSelected(WgnTestCase wtc, By by)
    {
        // Determine whether or not this element is selected
        try
        {            
           return wtc.GetWebDriver().findElement(by).isSelected();           
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            wtc.GetTestRunner().WriteLog("ERROR: Element not found on page: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return false;
    }
    
    public static Boolean isEditable(WgnTestCase wtc, By by)
    {
        //  is the element currently enabled or not?
        try
        {            
           return wtc.GetWebDriver().findElement(by).isEnabled();           
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            wtc.GetTestRunner().WriteLog("ERROR: Element not found on page: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return false;
    }
    
    public static void SelectOptionByText (WgnTestCase wtc, By by, String option)            
    {
        // choose option in select list by displayed text
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try
        {
            // First, get the WebElement for the select tag
            WebElement selectElement = dr.findElement(by);                        
            // Then instantiate the Select class with that WebElement
            Select select = new Select(selectElement);
            // if it's a multi select list then clear any selections first
            if (select.isMultiple()) select.deselectAll();
            // Get a list of the options
            List<WebElement> options = select.getOptions();
            // For each option in the list, verify if it's the one you want and then click it
            Boolean ok = false;
            
            for (WebElement we : options) {
                if (we.getText().equals(option)) 
                {
                    we.click();
                    tr.WriteLog("PASS: Option selected: " + option + " in " + Utils.GetAttribute(wtc, by, "id", false), LogFile.iTEST);
                    ok = true;
                    break;
                }
            }
            if (!ok) tr.WriteLog("FAIL: Option ('" + option + "') not found in select: " + by.toString(), LogFile.iERROR);
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }    
    }
    public static void SelectOptionByValue(WgnTestCase wtc, By by, String value) 
    {
        // choose option in select list by value
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try 
        {
            Select selectBox = new Select(dr.findElement(by));
            // if it's a multi select list then clear any selections first
            if (selectBox.isMultiple()) selectBox.deselectAll();
            selectBox.selectByValue(value);
            tr.WriteLog("PASS: Option selected: " + value + " in " + Utils.GetAttribute(wtc, by, "id", false), LogFile.iTEST);
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
    }
    
    public static void SelectOptionsByValue(WgnTestCase wtc, By by, String[] values) 
    {
        // choose multiple options in a multiselect list by value
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try 
        {
            Select selectBox = new Select(dr.findElement(by));
            // check it's a multi select list 
            if (selectBox.isMultiple()) 
            {
                for (String value : values )
                {
                    selectBox.selectByValue(value);
                    tr.WriteLog("PASS: Option selected: " + value + " in " + Utils.GetAttribute(wtc, by, "id", false), LogFile.iTEST);
                }
            } else {
                tr.WriteLog("ERROR: You can't select multiple options in a single select list!", LogFile.iERROR);
            }
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
    }
    
    public static String GetSelectedLabel(WgnTestCase wtc, By by) 
    {
        // get the currently selected label in a single select element
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try 
        {
            Select selectBox = new Select(dr.findElement(by));
            return selectBox.getFirstSelectedOption().getText();
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return "No selected option found";
    }
    
    public static String GetMultiFirstSelectedLabel(WgnTestCase wtc, By by) 
    {
        // get the currently selected label in a multi select element
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        String sLabel = "label";
        if (tr.GetTargetBrowser().startsWith("ie")) sLabel = "text";
        try 
        {
            Select selectBox = new Select(dr.findElement(by));
            return selectBox.getFirstSelectedOption().getAttribute(sLabel);
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return "No selected option found";
    }

    public static int GetSelectedValueAsInt(WgnTestCase wtc, By by) 
    {
        // get the value of the currently selected item in a select element as an int
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try 
        {
            Select selectBox = new Select(dr.findElement(by));
            return Integer.parseInt(selectBox.getFirstSelectedOption().getAttribute("value"));
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return -10;
    }

    public static int GetSelectedIndex(WgnTestCase wtc, By by) 
    {
        // get the index of the currently selected item in a select element
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try 
        {
            Select selectBox = new Select(dr.findElement(by));
            return Integer.parseInt(selectBox.getFirstSelectedOption().getAttribute("index"));
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        return -10;
    }
    
    public static String[] GetSelectOptions(WgnTestCase wtc, By by)
    {
        // get the list of options in a select element as an array
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try
        {
            WebElement select = dr.findElement(by);
            List<WebElement> options = select.findElements(By.tagName("option"));
            String[] labels = new String[options.size()];
            int i = 0;
            for(WebElement option : options)
            {
                labels[i] = option.getAttribute("text");
                i++;
            }
            return labels;
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
        String [] nothing = {""};
        return nothing;
    }
    
    public static String GetSelectOptionsHTML(WgnTestCase wtc, String selectId)
    {
        // get the list of options in a select elements as HTML
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        
        WebElement select = dr.findElement(By.id( selectId ));
        List<WebElement> options = select.findElements(By.tagName("option"));
        StringBuilder actualOpts = new StringBuilder();
        actualOpts.append("<options>");
        for(WebElement option : options){
            actualOpts.append("<option value=\"");
            actualOpts.append(option.getText());
            actualOpts.append("\" text=\"");
            actualOpts.append(option.getAttribute("value"));
            actualOpts.append("\" />");
        }        
        actualOpts.append("</options>");
        tr.WriteLog("DEBUG: " +selectId + " options: " + actualOpts.toString(), LogFile.iDEBUG);
        
        return actualOpts.substring(actualOpts.indexOf("<"));
    }
    
    public static void ToggleCheckbox (WgnTestCase wtc, By by, String set)            
    {
        // where set=on|off for how you want the checkbox to end up
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        
        WebElement checkbox = dr.findElement(by);              
        if ( (!checkbox.isSelected() && set.contentEquals("on"))
                || (checkbox.isSelected() && set.contentEquals("off")) ) 
        {
            checkbox.click();
        }
        tr.WriteLog("INFO: Checkbox "+ by.toString() +" was set to: " + checkbox.isSelected(), LogFile.iINFO);
    }
    
    public static void sendKeys(WgnTestCase wtc, By by, String typeme) throws Exception
    {
        //wrapper for webdriver sendKeys function (types into text fields)
        wtc.GetWebDriver().findElement(by).sendKeys(typeme);
    }
    
    public static void PlainType(WgnTestCase wtc, By by, String typeme) throws Exception
    {
        // enters the typeme string into a text field - overwriting any current entry
        WebDriver dr = wtc.GetWebDriver();
        WebElement element = dr.findElement(by);
        if (!element.getAttribute("value").isEmpty()) element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE); // element.clear();
        element.sendKeys(typeme);
    }    
    
    public static void DoType(WgnTestCase wtc, By by, String typeme) throws Exception
    {
        // enters the typeme string into a text field character-by-character (necessary on some fields on some browsers)
        // overwrites any existing entry
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();       
        char arr[] = typeme.toCharArray(); // convert the String object to array of char
        
        try 
        {
            // get the text input field
            WebElement element = dr.findElement(by);
            element.click();
            if (!element.getAttribute("value").isEmpty()) element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE); // element.clear();
            // Enter some text
            if (typeme.isEmpty())
            {
                element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                //element.clear(); // can't use this cos it triggers field input validation
            } else {
                // type in character-by-character to beat field validation losing focus
                for (char c: arr)
                {
                    Thread.sleep(25);
                    element.sendKeys(String.valueOf(c));
                }
            }
        } 
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
    }
    
    public static void DoTypeAll(WgnTestCase wtc, By by, String sTabId, String typeme) throws Exception
    {
        // enters the typeme string into a text field all at once
        // overwrites any existing entry
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();       
        try 
        {
            // get the text input field
            WebElement element = dr.findElement(by);
            try 
            {
                element.click();
            }
            catch (WebDriverException wde)
            {
                tr.WriteLog("INFO: failed to click text element due to existing error text", LogFile.iINFO);
            }
            // clear the field first if desired
            if (!element.getAttribute("value").isEmpty()) element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE); // element.clear();
            if (typeme.isEmpty())
            {
                element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                // element.clear(); // can't use this cos it triggers field input validation
            } else {
                element.sendKeys(typeme);
            }
            sendTAB(wtc, by, sTabId);
        }
        catch (org.openqa.selenium.NoSuchElementException nse) 
        {
            tr.WriteLog("FAIL: Element not found for: " + by.toString() + " (" + nse.getMessage() + ")", LogFile.iERROR);
        }
    }
    
    public static void sendTAB(WgnTestCase wtc, By by, String sTabId)
    {
        // sends a TAB character to the page to move focus away from a changed element
        wtc.GetWebDriver().findElement(by).sendKeys(Keys.TAB);
        if (!wtc.GetTestRunner().GetTargetBrowser().contains("chrome"))
        {
            // sending a TAB isn't enough to trigger the onchange event on some browsers
            if (isElementPresent(wtc, By.id(sTabId), false)) 
            {
                wtc.GetWebDriver().findElement(By.id(sTabId)).click();
            }
        }        
    }
    
    public static WebElement GetActiveElement(WgnTestCase wtc)
    {
        // gets the active element on the page
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        try
        {
            WebElement focused_element = dr.switchTo().activeElement();
            return focused_element;
        } 
        catch (Exception e) 
        {
            tr.WriteLog("FAIL: No Active Element found (" + e.getMessage() + ")", LogFile.iERROR);
            return null;
        }
    }
    
    public static Boolean SelectAdminCategory(WgnTestCase wtc, String category, By by) throws Exception
    {
        // changes the view on the iConsole Administration, Searches page (Searches, reports, dashboard or System)
        String DLG_PROGRESS = SetParam(wtc, "DLG_PROGRESS");
        String lst_cats = SetParam(wtc, "LST_CATS");
        if (isElementPresent(wtc, By.id(lst_cats), false))
        {
            SelectOptionByValue(wtc, By.id(lst_cats), category);
        }
        int i=0; while ( (!isElementPresent(wtc, by, false) || isElementPresent(wtc, By.id(DLG_PROGRESS), false)) && i < 20) 
        { 
            Thread.sleep(500); i++; 
        }        
        Thread.sleep(500);
        return Utils.isElementPresent(wtc, by, false);
    }
    
    public static void ExecuteJavaScript(WgnTestCase wtc, String script) throws Exception
    {
        // executes the supplied js on the web page
        WebDriver dr = wtc.GetWebDriver();
        ((JavascriptExecutor)dr).executeScript(script);
    }
    
    public static int lineCount(String text) throws IOException
    {
        // counts the lines in the supplied text
        final BufferedReader br = new BufferedReader(new StringReader(text));
        int count = 0;
        while (br.readLine() != null) count++;
        return count;
    }
    
    public static long GetTimeSinceEpoch(String timeReq, TestRunner tr)
    {
        // calculates how many of the specified time periods have passed since the Epoch - 01-01-1970
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); //Set to Epoch time
        DateTime now = new DateTime();
        
        Days days = Days.daysBetween(epoch, now);
        days = days.plus(1);  // daysbetween doesn't include today - the iConsole does!
        long new_weeks = (long)Math.floor(days.getDays()/7);
        //Weeks weeks = Weeks.weeksBetween(epoch, now);
        Months months = Months.monthsBetween(epoch, now);

        long val = 0;
        switch (timeReq) {
            case "Days":
                tr.WriteLog("Days since Epoch: " + days.getDays(), LogFile.iDEBUG);
                val = days.getDays();
                break;
            case "Weeks":
                //tr.WriteLog("Weeks since Epoch: " + weeks.getWeeks(), LogFile.iDEBUG);
                tr.WriteLog("Weeks since Epoch: " + new_weeks + " (days = "+ days.getDays() +")", LogFile.iDEBUG);
                val = new_weeks;
                break;
            case "Months":
                tr.WriteLog("Months since Epoch: " + months.getMonths(), LogFile.iDEBUG);
                val = months.getMonths();
                break;
        }        
        return val;
    }
    
    public static void CheckAuditPrefs(WgnTestCase wtc, Boolean b) throws Exception
    {
        // call with b=true to disable the checkboxes and b=false to enable them
        TestRunner tr = wtc.GetTestRunner();
        // populate params for GUI ids (do this here because this method is called be other test cases)
        String mDlgProgress = Utils.SetParam(wtc,"DLG_PROGRESS");
        String mLnkProfile = Utils.SetParam(wtc, "LNK_PREFS");
        String mDlgPrefs = Utils.SetParam(wtc, "DLG_PREFS");
        String mPrefsOK = Utils.SetParam(wtc, "BTN_PREFS_OK");
        String mPrefsApply = Utils.SetParam(wtc, "BTN_PREFS_APPLY");
        String mAuditTab = Utils.SetParam(wtc,"TAB_PREFS_AUDIT");
        String mPrefMove = Utils.SetParam(wtc,"CHK_PREF_MOVE");
        String mPrefRemove = Utils.SetParam(wtc,"CHK_PREF_REMOVE");
        
        Thread.sleep(500);  // the following click gets lost sometimes on IE
        Utils.DoClick(wtc, By.id(mLnkProfile), By.id(mDlgPrefs), 10000, false);
        Boolean doSave = false;
        // move to correct Prefs tab (id=tab_audit)
        if (!Utils.isElementPresent(wtc, By.id(mPrefRemove), false)) 
        {
            Utils.DoClick(wtc, By.id(mAuditTab), By.id(mPrefRemove), 1000);
        }
        if ( (Utils.isSelected(wtc, By.id(mPrefRemove)) & b)
                || (!Utils.isSelected(wtc, By.id(mPrefRemove)) & !b) ) // this MUST be off for this test to work.
        {
            Utils.DoClick(wtc, By.id(mPrefRemove), null, 100);
            doSave = true;
        }
        if ( (Utils.isSelected(wtc, By.id(mPrefMove)) & b)
                || (!Utils.isSelected(wtc, By.id(mPrefMove)) & !b) ) // test will break on last event on page if this is enabled
        {
            Utils.DoClick(wtc, By.id(mPrefMove), null, 100);
            doSave = true;
        }
        if (doSave)
        {
            Utils.DoClick(wtc, By.id(mPrefsApply), null, 100);
            int count = 0;
            while (Utils.isElementPresent(wtc, By.id(mDlgProgress), false) && count < 20) { count++; Thread.sleep(1000); }
        }
        tr.WriteLog("Remove Events After Audit = " + Utils.isSelected(wtc, By.id(mPrefRemove)), LogFile.iDEBUG);
        tr.WriteLog("Move to Next Event After Audit = " + Utils.isSelected(wtc, By.id(mPrefMove)), LogFile.iDEBUG);
        
        Utils.DoClick(wtc, By.id(mPrefsOK), null, 500);
    }
    
    
}