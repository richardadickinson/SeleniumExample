//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import common.*;
import org.openqa.selenium.*;

public class VerifyQuickViewPanes extends WgnTestStep
{
    private boolean mbUseNextEventButton;
    private boolean mbCloseQuickViewPanes;
    private boolean mbExpandCollapse;
    private int mNumEvents;

    public VerifyQuickViewPanes()
    {
        mbUseNextEventButton = false;
        mbCloseQuickViewPanes = false;
        mNumEvents = -1;
    }

    @Override
    public void Run() throws Exception
    {
        mTestRunner.WriteLog("Test step " + this.mName +" started", LogFile.iINFO);

        mbUseNextEventButton = Boolean.parseBoolean(mParameters.get("usenexteventbutton"));
        mbCloseQuickViewPanes = Boolean.parseBoolean(mParameters.get("closequickviewpanes"));
        mbExpandCollapse = Boolean.parseBoolean(mParameters.get("expandcollapse"));

        if (mParameters.containsKey("numevents"))
        {
            mNumEvents = Integer.parseInt(mParameters.get("numevents"));
        }

        if (mNumEvents == -1)
        {
            int numSearchResults = Integer.parseInt(mTestCase.GetAttribute("numSearchResults")) + 1;
            int numEventsPerPage = Integer.parseInt(mTestCase.GetAttribute("pageSize"));

            int numPages = numSearchResults/numEventsPerPage + 1;
            int leftOvers = numSearchResults % numEventsPerPage;
            int j = 0, k = 1;
            if (mbUseNextEventButton)
            {
                // cycling through the results using the Move to Next Button on QV pane
                boolean bFirst = true;
                for (int i=0; i < numSearchResults; i++)
                {
                    String clickElement = "";
                    if (bFirst)
                    {
                        mTestRunner.NewLogFileGroup("Cycling through Results Page " + k, LogFile.iINFO);
                        clickElement = "Row"+Integer.toString(0);
                        bFirst = false;
                    } else {
                        clickElement = "btn_Pane.NextEvent";
                    }                    
                    Utils.DoClick(mTestCase, By.id(clickElement), By.cssSelector("ul.quickPanes"), 30000);
                    
                    // when the page size is reached the results row ids reset to 0
                    if (j == numEventsPerPage) 
                    {
                        mTestRunner.CloseLogFileGroup("Completed Results Page " + k, LogFile.iINFO);
                        j = 0; k++;
                        mTestRunner.NewLogFileGroup("Cycling through Results Page " + k, LogFile.iINFO);
                    }
                    // get the subject line for the next event
                    String evSubj = Utils.GetText(mTestCase, By.xpath("//tr[@id='Row" + Integer.toString(j) + "']/td[3]/span"), true); j++;
                    mTestRunner.NewLogFileGroup("Page " + k + ", Event id:" + i, LogFile.iINFO);
                    CheckPanes(evSubj);

                    if (mbExpandCollapse)
                    {
                        ExpandCollapseQuickView();
                    }
                    mTestRunner.CloseLogFileGroup("Completed Page " + k + ", Event id:" + i, LogFile.iINFO);
                    if (i == numSearchResults-1)
                    {
                        Utils.DoClick(mTestCase, By.id("btn_Results.Nav.FirstPage"), null, 100); 
                    }
                }
                mTestRunner.CloseLogFileGroup("Completed Results Page " + k, LogFile.iINFO);
            } else {
                // cycling through the results by page
                for (int pageCount=0; pageCount < numPages; pageCount++)
                {
                    int currentPage = pageCount+1;
                    mTestRunner.NewLogFileGroup("Cycling through Results Page " + currentPage, LogFile.iINFO);
                    if (pageCount == (numPages-1)) { numEventsPerPage = leftOvers; } // last page
                    ViewQuickViewPanes(numEventsPerPage);
                    mTestRunner.CloseLogFileGroup("Completed Results Page " + currentPage, LogFile.iINFO);

                    if (pageCount != numPages-1) {
                        Utils.DoClick(mTestCase, By.id("btn_Results.Nav.NextPage"), null, 100);
                    } else {
                        Utils.DoClick(mTestCase, By.id("btn_Results.Nav.FirstPage"), null, 100);
                    }
                }
            }
        } else {
            // cycling through a specified number of events
            ViewQuickViewPanes(mNumEvents);
        }

        mTestRunner.WriteLog(String.format("Test step " + this.mName + " finished"), LogFile.iINFO);

    }

    private void ViewQuickViewPanes(int numEventsPerPage) throws Exception
    {
        for (int id = 0; id < numEventsPerPage; id++)
        {
            mTestRunner.NewLogFileGroup("Testing event result id:" + id, LogFile.iINFO);
            String evSubj = Utils.GetText(mTestCase, By.xpath("//tr[@id='Row" + Integer.toString(id) + "']/td[3]/span"), true);
            Utils.DoClick(mTestCase, By.id("Row"+Integer.toString(id)), By.cssSelector("ul.quickPanes"), 30000);

            CheckPanes(evSubj);

            if (mbExpandCollapse)
            {
                ExpandCollapseQuickView();
            }

            if (mbCloseQuickViewPanes)
            {
                Utils.DoClick(mTestCase, By.id("btn_Pane.Close"), null, 100);
                int i = 0; while (Utils.isElementPresent(mTestCase, By.cssSelector("ul.quickPanes"), false) && i < 10) { Thread.sleep(500); i++; }
            }
            mTestRunner.CloseLogFileGroup("Tests complete for event result id:" + id, LogFile.iINFO);
        }               
    }

    private void CheckPanes(String evSubj) throws Exception
    {
        String BTN_QUICKVIEWPANES = "btn_Pane.Quick";
        int i = 0; while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && i < 10) { Thread.sleep(500); i++; }
        // cycle through each of the quick view panes in wide mode and check that the other panes
        // are correctly hidden        
        mTestRunner.NewLogFileGroup("Test the Quick View button:", LogFile.iINFO);
        // Check default view first
        mTestRunner.NewLogFileGroup("Confirm default view:", LogFile.iINFO);
        checkElementPresent(By.id("event"), "Event pane", true);
        checkElementPresent(By.id("audit"), "Audit pane", true);
        mTestRunner.CloseLogFileGroup("Finished default view checks", LogFile.iINFO);

        mTestRunner.NewLogFileGroup("First Quick View button click:", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id(BTN_QUICKVIEWPANES), null, 500);
        checkElementPresent(By.id("event"), "Event pane", true);
        checkElementPresent(By.id("audit"), "Audit pane", false);
        mTestRunner.CloseLogFileGroup("Finished first click checks", LogFile.iINFO);

        mTestRunner.NewLogFileGroup("Second Quick View button click:", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id(BTN_QUICKVIEWPANES), null, 500);
        checkElementPresent(By.id("event"), "Event pane", false);
        checkElementPresent(By.id("audit"), "Audit pane", true);
        mTestRunner.CloseLogFileGroup("Finished second click checks", LogFile.iINFO);
        
        mTestRunner.NewLogFileGroup("Third Quick View button click:", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id(BTN_QUICKVIEWPANES), null, 500);
        checkElementPresent(By.id("event"), "Event pane", true);
        checkElementPresent(By.id("audit"), "Audit pane", true);
        mTestRunner.CloseLogFileGroup("Finished third click checks", LogFile.iINFO);

        mTestRunner.CloseLogFileGroup("Finished Quick View button tests", LogFile.iINFO);

        // Print Preview button
        mTestRunner.NewLogFileGroup("Click Print button:", LogFile.iINFO);
        // open the new window (resultswin preserves the current window handle)
        String resultsWin = Utils.GetNewWindow(mTestCase, By.id("btn_Pane.Print"));                
        // many characters are replaced in a subject line before it is used in a window title
        // we need to escape the same characters to produce an expected window title
        // see Utilities.charsReplace(String) for the full list of escapees
        mTestRunner.WriteLog("evSubj = " + evSubj, LogFile.iDEBUG);  // subject line pre-escaping
        String rep_evSubj = Utils.ReplaceChars(evSubj);
        mTestRunner.WriteLog("rep_evSubj = " + rep_evSubj, LogFile.iDEBUG);  // subject line post-escaping

        //Do tests on new window
        String printTitle = Utils.GetWindowTitle(mTestCase);
        if ( printTitle.contentEquals(rep_evSubj) )
        {
            mTestRunner.WriteLog("PASS: Print view window has correct Title (" + printTitle + ")", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Print view window has WRONG Title: " + printTitle, LogFile.iERROR);
        }
        if (Utils.isTextPresent(mTestCase, "Audit Summary", true) 
                && Utils.isTextPresent(mTestCase, "Incidents", true) )
        {
            mTestRunner.WriteLog("PASS: Correct section headings are present on print view", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Correct section headings are NOT present on print view", LogFile.iERROR);
        }
        // close the Print window and return to the Results window
        Utils.CloseWindow(mTestCase, resultsWin);

        mTestRunner.CloseLogFileGroup("Finished Print button checks", LogFile.iINFO);

        // Context Search button
        mTestRunner.NewLogFileGroup("Click Context Search button:", LogFile.iINFO);
        // open the Context Search window
        resultsWin = Utils.GetNewWindow(mTestCase, By.id("btn_Pane.Context"));
        int count = 0;
        while (!Utils.isElementPresent(mTestCase, By.cssSelector("tr.contextRoot"), false) && count <10) { count++; Thread.sleep(1000); }        
        
        // context root is now displayed - Do tests on new window        
        String contextTitle = Utils.GetWindowTitle(mTestCase);
        if ( contextTitle.contentEquals(mParameters.get("winContext")) )
        {
            mTestRunner.WriteLog("PASS: Context Search window has correct Title (" + contextTitle + ")", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Context Search window has WRONG Title: " + contextTitle, LogFile.iERROR);
        }

        
        if (Utils.GetText(mTestCase, By.xpath("//tr[contains(@class,'contextRoot')]/td[3]/span"), false).contentEquals(evSubj) )
        {
            mTestRunner.WriteLog("PASS: Correct event ('"+evSubj+"') is highlighted as the context root", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The correct event ('"+evSubj+"') is NOT highlighted as the context root", LogFile.iERROR);
        }
        if (Utils.isDisplayed(mTestCase, By.id("lnk_logoff")))
        {
            mTestRunner.WriteLog("FAIL: The Logout link should be hidden on context windows", LogFile.iERROR);
        } else {
            mTestRunner.WriteLog("PASS: The Logout link is hidden on the Context window", LogFile.iTEST);
        }
        Utils.CloseWindow(mTestCase, resultsWin);
        mTestRunner.CloseLogFileGroup("Finished Context button checks", LogFile.iINFO);

        // Test the Event History and Expiry Date Buttons
        mTestRunner.NewLogFileGroup("Check the Event History button:", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id("btn_Pane.History"), null, 100);
        i = 0; while ( !Utils.isElementPresent(mTestCase, By.id("Message"), false) && !Utils.isElementPresent(mTestCase, By.id("AuditHistory"), false) && i < 20 ) { Thread.sleep(500); i++; }
        if (Utils.isElementPresent(mTestCase, By.id("Message"), false))
        {
            // There's no event history so create some
            mTestRunner.WriteLog("This event has no event history, creating some...", LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 100);                        
            Utils.DoClick(mTestCase, By.id("btn_Pane.ExpiryDate"), By.id("ExpiryDate"), 5000);
            if (!Utils.isSelected(mTestCase, By.id("chkDND")))
            {
                Utils.ToggleCheckbox(mTestCase, By.id("chkDND"), "on");
            }
            if (Utils.isSelected(mTestCase, By.id("chkDND")))
            {
                mTestRunner.WriteLog("PASS: the 'Do Not Delete' checkbox is checked", LogFile.iTEST);
            } else { //try again!
                mTestRunner.WriteLog("FAIL: the 'Do Not Delete' checkbox was not checked", LogFile.iERROR);
                Utils.ToggleCheckbox(mTestCase, By.id("chkDND"), "on");
            }
            Utils.DoType(mTestCase, By.id("selComment"), "Stay awhile, stay forever");  //<-- adding this stops the expiry date comment being written! Wigan bug.
            Thread.sleep(500);
            Utils.DoClick(mTestCase, By.id("btn_ExpiryDate.OK"), null, 1000);
            // Now open the Event History dialog
            Utils.DoClick(mTestCase, By.id("btn_Pane.History"), By.id("AuditHistory"), 10000);
            if (Utils.isElementPresent(mTestCase, By.id("AuditHistory"), false))
            {
                mTestRunner.WriteLog("PASS: the Event History window is displayed", LogFile.iTEST);
                if (Utils.GetText(mTestCase, By.xpath("//div[@id='AuditHistory']/div[2]/div/div/div/table/tbody/tr/td[3]"),false).contentEquals(mParameters.get("expiryDateSet")))
                {
                    mTestRunner.WriteLog("PASS: the Event History window shows the DO NOT DELETE was set", LogFile.iTEST);                    
                } else {
                    mTestRunner.WriteLog("FAIL: the Event History window failed to show the Expiry Date change", LogFile.iERROR);
                }
                Utils.DoClick(mTestCase, By.id("btn_AuditHistory.Close"), null, 2000);
            } else {
                mTestRunner.WriteLog("FAIL: the Event History window failed to be displayed", LogFile.iERROR);
            }
        }
        else if (Utils.isElementPresent(mTestCase, By.id("AuditHistory"), false))
        {
            if (Utils.GetText(mTestCase, By.id("AuditHistory_desc"), false).contentEquals(mParameters.get("eventhistory")))
            {
                mTestRunner.WriteLog("PASS: the Event History window is displayed", LogFile.iTEST);
                Utils.DoClick(mTestCase, By.id("btn_AuditHistory.Close"), null, 100);
                count = 0;
                while (Utils.isElementPresent(mTestCase, By.id("AuditHistory"), false) && count < 5) { count++; Thread.sleep(1000); }
            } else {
                mTestRunner.WriteLog("FAIL: the Event History window failed to open", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Neither the Event History window or 'No Event History exists' Message dialog was displayed", LogFile.iERROR);
        }
        mTestRunner.CloseLogFileGroup("Finished Event History button checks", LogFile.iINFO);
    }

    private void ExpandCollapseQuickView() throws Exception
    {
        int count = 0;
        Utils.Click(mTestCase, By.cssSelector("img[alt='Expand bottom pane']"), 100);  //id("btn_splitter")
        String js = (String) ((JavascriptExecutor) mDriver).executeScript("return document.getElementById('mdSideBar').style.display;");
        while ( !js.contentEquals("none") && count < 5 )
        {
            Thread.sleep(500); count++;
            js = (String) ((JavascriptExecutor) mDriver).executeScript("return document.getElementById('mdSideBar').style.display;");
        }
        if (js.contentEquals("none"))
        {
            mTestRunner.WriteLog("PASS: QuickView display expanded", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: QuickView display not expanded", LogFile.iERROR);
        }
        
        Utils.Click(mTestCase, By.cssSelector("img[alt='Collapse bottom pane']"), 100); //By.id("btn_splitter")
        js = (String) ((JavascriptExecutor) mDriver).executeScript("return document.getElementById('mdSideBar').style.display;");
        while ( !js.contentEquals("table-cell") || !js.contentEquals("block") && count < 10 )
        {
            Thread.sleep(500); count++;
            js = (String) ((JavascriptExecutor) mDriver).executeScript("return document.getElementById('mdSideBar').style.display;");
        }
        if (js.contentEquals("table-cell") || js.contentEquals("block"))
        {
            mTestRunner.WriteLog("PASS: QuickView display collapsed", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: QuickView display not collapsed", LogFile.iERROR);
        }
    }
    
    public void checkElementPresent(By by, String strDesc, Boolean expected) throws Exception
    {
        // checks whether an element is present on the page or not
        String strNot = "";
        if (!expected) { strNot = "NOT "; }
        Boolean bState = Utils.isElementPresent(mTestCase, by, false);

        if ( (bState && expected) || (!bState && !expected) )
        {
            mTestRunner.WriteLog("PASS: " + strDesc + " is " + strNot + "visible", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: " + strDesc + " isElementPresent = " + bState + ", Expected: " + expected, LogFile.iERROR);
        }
    }
}
