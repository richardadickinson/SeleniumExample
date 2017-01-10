//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 **/

import java.util.*;

import common.*;

import org.openqa.selenium.*;

public class Audit extends WgnTestStep
{
    private boolean mVerify;
    private boolean mIsBulk;
    private boolean mAuditAll;
    private int mNumberEvents;
    private boolean mAllPages;
    private int mPages;
    private boolean mUseNewIssue;
    private int mAuditButton;
    private String mDlgProg;

    public Audit() { }

    @Override
    public void Run() throws Exception
    {
        if (!ParseParameters())
        {
            return;
        }

        Utils.CheckAuditPrefs(mTestCase, true);
        if (mVerify)
        {
            if(!VerifyAuditOptions())
            {
                mTestRunner.WriteLog("*** The Verify Audit Options function has reported errors. "
                        + "Check your Audit Button config before attempting to run the audit tests.", LogFile.iNONE);
                mTestRunner.WriteLog("*** The user running the test requires these CA DataMinder privileges: "
                        + "'audit: allow audit without viewing the event' and 'events: allow bulk session management'", LogFile.iNONE);
                return;
            }
        }

        if (mIsBulk)
        {
            if (!DoBulkAudit())
            {
                return;
            }
        } else {
            if (!DoSingleEventAudit())
            {
                return;
            }
        }
        Utils.CheckAuditPrefs(mTestCase, false);
        
        // finally close the results window and return to the parent window for shutdown
        Utils.CloseWindow(mTestCase, mTestCase.GetParentHandle());
    }

    private boolean VerifyAuditOptions() throws Exception
    {
        mTestRunner.NewLogFileGroup("Verifying the correct audit buttons are displayed on the page", LogFile.iINFO);
        boolean isPresent = false;

        Utils.DoClick(mTestCase, By.id("Row0"), By.cssSelector("ul.quickPanes"), 5000);

        Iterator<AuditButton> itm = mTestRunner.GetAuditConfigurationManager().GetAvailableAuditButtons().iterator();
        while (itm.hasNext())
        {
            AuditButton ab = (AuditButton)itm.next();

            if (ab.isDisabled())
            {
                // Check that button is not displayed (either single or bulk)
                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getBulkAuditButtonId()), false);
                if (isPresent)
                {
                    // Test failed - button is present when disabled
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is displayed for bulk audit when it is disabled",ab.getBulkAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getBulkAuditButtonId() + " is NOT displayed for bulk audit when it is disabled", LogFile.iTEST);
                }

                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getSingleAuditButtonId()), false); 

                if (isPresent)
                {
                   // Test failed - button is present when disabled
                   mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is displayed for single event audit when it is disabled",ab.getSingleAuditButtonId()), LogFile.iERROR);
                   return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getSingleAuditButtonId() + " is NOT displayed for single event audit when it is disabled", LogFile.iTEST);
                }
            }

            if (ab.isSingleAndBulk())
            {
                // Check button is present for both single and bulk audit
                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getBulkAuditButtonId()), false);

                if (!isPresent)
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is not displayed for bulk event audit when it is enabled",ab.getBulkAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getBulkAuditButtonId() + " is displayed for bulk event audit when it is enabled", LogFile.iTEST);
                }

                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getSingleAuditButtonId()), false);

                if (!isPresent)
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is not displayed for single event audit when it is enabled",ab.getSingleAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getSingleAuditButtonId() + " is displayed for single event audit when it is enabled", LogFile.iTEST);
                }
            }

            if (ab.isSingleAuditButton())
            {
                // check button is available for single event audit only
                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getBulkAuditButtonId()), false);
                if (isPresent)
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is displayed for bulk event audit when it is only enabled for single event auditing",ab.getBulkAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getBulkAuditButtonId() + " is NOT displayed for bulk event audit when it is only enabled for single event auditing", LogFile.iTEST);
                }

                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getSingleAuditButtonId()), false);
                if (!isPresent)
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is not displayed for single event audit when it is only enabled for single event auditing",ab.getSingleAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getSingleAuditButtonId() + " is displayed for single event audit when it is only enabled for single event auditing", LogFile.iTEST);
                }
            }

            if (ab.isBulk())
            {
                // check button is available for bulk event audit only
                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getBulkAuditButtonId()), false);
                if (!isPresent)
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is not displayed for bulk event audit when it is only enabled for bulk event auditing",ab.getBulkAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getSingleAuditButtonId() + " is displayed for bulk event audit when it is only enabled for bulk event auditing", LogFile.iTEST);
                }

                isPresent = Utils.isElementPresent(mTestCase, By.id(ab.getSingleAuditButtonId()), false);
                if (isPresent)
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit button id=%s is displayed for single event audit when it is only enabled for bulk event auditing",ab.getSingleAuditButtonId()), LogFile.iERROR);
                    return false;
                } else {
                    mTestRunner.WriteLog("PASS: Audit button id=" + ab.getSingleAuditButtonId() + " is not displayed for single event audit when it is only enabled for bulk event auditing", LogFile.iTEST);
                }
            }
        }
        Utils.DoClick(mTestCase, By.id("btn_Pane.Close"), null, 100);
        mTestRunner.CloseLogFileGroup("Completed verification of audit button display", LogFile.iINFO);

        return true;
    }

    private boolean DoBulkAudit() throws Exception
    {
        int numSearchResults = Integer.parseInt(mTestCase.GetAttribute("numSearchResults")) + 1;
        int numEventsPerPage = Integer.parseInt(mTestCase.GetAttribute("pageSize"));
        int numPages = numSearchResults/numEventsPerPage;
        int leftOvers = numSearchResults % numEventsPerPage;  // get remainder of events for last page
        if (leftOvers == 0) { leftOvers = numEventsPerPage; } // if there is no remainder then last page is full

        if (numSearchResults < numEventsPerPage)
        {
            numEventsPerPage = numSearchResults-1;
        }
        if (mNumberEvents > numEventsPerPage) {
            mTestRunner.WriteLog("WARNING: requested quantity of events for audit exceeds the results page size", LogFile.iNONE);
            mTestRunner.WriteLog("         Setting number of events to audit = page size (" + numEventsPerPage + ")", LogFile.iNONE);
            mNumberEvents = numEventsPerPage;
        }
        mTestRunner.WriteLog("mPages = " + mPages + ", numPages = " + numPages + ", leftOvers = " + leftOvers, LogFile.iDEBUG);
        
        if (mAllPages || mPages != 0)
        {
            if (mAllPages) { mPages = numPages; }
            for (int pageCount=0; pageCount < mPages; pageCount++)
            {
                int actualPage = pageCount+1;
                mTestRunner.NewLogFileGroup("Auditing events on page " + actualPage, LogFile.iINFO);

                if (pageCount == (numPages-1))
                {
                    numEventsPerPage = leftOvers;
                    if (mNumberEvents > leftOvers)
                    {
                        mNumberEvents = leftOvers;
                    }
                }
                if (mAuditAll)
                {
                    DoPerPageBulkEventAudit(numEventsPerPage);
                } else {
                    DoEventBulkAudit();
                }
                if (pageCount != (numPages-1))
                {
                    Utils.DoClick(mTestCase, By.id("btn_Results.Nav.NextPage"), null, 100);
                }
                mTestRunner.CloseLogFileGroup("Completed auditing events on page " + actualPage, LogFile.iINFO);
            }
        } else {
            if (mAuditAll)
            {
                if (mPages > 0) 
                {
                    mTestRunner.NewLogFileGroup("Bulk Auditing all events on a single page", LogFile.iINFO);
                    DoPerPageBulkEventAudit(numEventsPerPage);
                    mTestRunner.CloseLogFileGroup("Completed bulk auditing a single page of events", LogFile.iINFO);
                } else {
                    mTestRunner.NewLogFileGroup("Bulk Auditing all events at once", LogFile.iINFO);
                    Utils.CheckAuditPrefs(mTestCase, false);
                    Utils.DoClick(mTestCase, By.id("btn_Results.Nav.ShowAll"), null, 1000);
                    int count = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgProg), false) && count < 20) { count++; Thread.sleep(1000); }
                    DoPerPageBulkEventAudit(numSearchResults);
                    mTestRunner.CloseLogFileGroup("Completed bulk auditing all events at once", LogFile.iINFO);
                }
            } else {
                mTestRunner.NewLogFileGroup("Bulk Auditing a set number (" + mNumberEvents + ") of events on page", LogFile.iINFO);
                DoEventBulkAudit();
                mTestRunner.CloseLogFileGroup("Completed Bulk Auditing a set number of events (" + mNumberEvents + ")", LogFile.iINFO);
            }
        }

        return true;
    }

    private void DoEventBulkAudit() throws Exception
    {
        for (int i=0; i < mNumberEvents; i++)
        {
            Utils.DoClick(mTestCase, By.id("chk_Results.Row" + i), null, 500);
            Utils.isSelected(mTestCase, By.id("chk_Results.Row" + i));
        }

        Utils.DoClick(mTestCase, By.id("btn_Results.ToolAudit0" + mAuditButton), By.id("BulkAuditOperation"), Integer.parseInt(mParameters.get("bulkaudittimeout") + "000"));
        VerifyBulkAuditOK(String.valueOf(mNumberEvents));
        Utils.DoClick(mTestCase, By.id("btn_BulkAuditOperation.Close"), null, 100);
        while ( Utils.isElementPresent(mTestCase, By.id("BulkAuditOperation"), false) ) { Thread.sleep(500); }
    }

    private void DoPerPageBulkEventAudit(int numEventsPerPage) throws Exception
    {        
        Utils.DoClick(mTestCase, By.id("chk_Results.All"), null, 100);
        while (!Utils.isSelected(mTestCase, By.id("chk_Results.All"))) { Thread.sleep(500); }
        if (!CheckAllAuditCheckboxes(numEventsPerPage)) return;        
        Utils.DoClick(mTestCase, By.id("btn_Results.ToolAudit0" + mAuditButton), By.id("BulkAuditOperation"), Integer.parseInt(mParameters.get("bulkaudittimeout") + "000"));
        VerifyBulkAuditOK(String.valueOf(numEventsPerPage));
        Utils.DoClick(mTestCase, By.id("btn_BulkAuditOperation.Close"), null, 100);
        while ( Utils.isElementPresent(mTestCase, By.id("BulkAuditOperation"), false) ) { Thread.sleep(500); }
    }

    private void VerifyBulkAuditOK(String numEvents) throws Exception
    {
        String isComplete = Utils.GetText(mTestCase, By.id(mDlgProg), false);
        int i = 0;
        while (!isComplete.contains(mParameters.get("isComplete")) && i < 60) 
        {
            Thread.sleep(1000);
            isComplete = Utils.GetText(mTestCase, By.id(mDlgProg), false);
            i++;
            mTestRunner.WriteLog("Progress content = " + isComplete, LogFile.iDEBUG);
        }

        String updatedText = Utils.GetText(mTestCase, By.id("Operation_resUpdated"), false);
        if (!updatedText.contains(numEvents))
        {
            mTestRunner.WriteLog(String.format("FAIL: Error checking bulk audit operation report, updated Text '%s' does not contain number of events on page '%s'",updatedText,numEvents), LogFile.iERROR);
        } else {
            mTestRunner.WriteLog("PASS: Bulk Audit operation completed successfully for " + numEvents + " events", LogFile.iTEST);
        }

    }

    private boolean DoSingleEventAudit() throws Exception
    {
        if (mAuditAll)
        {
            int numSearchResults = Integer.parseInt(mTestCase.GetAttribute("numSearchResults")) + 1;
            int numEventsPerPage = Integer.parseInt(mTestCase.GetAttribute("pageSize"));

            int numPages = numSearchResults/numEventsPerPage;
            int leftOvers = numSearchResults % numEventsPerPage;
            // get remainder of events for last page
            if (leftOvers == 0) { leftOvers = numEventsPerPage; } // if there is no remainder then last page is full
            mTestRunner.WriteLog("mPages = " + mPages + ", numPages = " + numPages + ", leftOvers = " + leftOvers, LogFile.iDEBUG);

            if (numSearchResults < numEventsPerPage)
            {
                numEventsPerPage = numSearchResults-1;
            }
            if (mNumberEvents > numEventsPerPage) {
                mTestRunner.WriteLog("WARNING: Your requested quantity of events to audit exceeds the search results page size.", LogFile.iNONE);
                mTestRunner.WriteLog("         Resetting number of events to audit = page size (" + numEventsPerPage + ")", LogFile.iNONE);
                mNumberEvents = numEventsPerPage;
            }

            if (mAllPages || mPages != 0)
            {
                if (mAllPages) { mPages = numPages; }
                for (int pageCount=0; pageCount < mPages; pageCount++)
                {
                    int actualPage = pageCount+1;
                    mTestRunner.NewLogFileGroup("Auditing events on page " + actualPage, LogFile.iINFO);
                    if (pageCount == (numPages-1))
                    {
                        numEventsPerPage = leftOvers;
                    }

                    DoPerPageSingleEventAudit(numEventsPerPage);
                    if (pageCount != (numPages-1))
                    {
                        Utils.DoClick(mTestCase, By.id("btn_Results.Nav.NextPage"), null, 500);
                    }
                    mTestRunner.CloseLogFileGroup("Page " + actualPage + " auditing complete", LogFile.iINFO);
                }
            } else {
                DoPerPageSingleEventAudit(numEventsPerPage);
            }
        } else {
            DoPerPageSingleEventAudit(mNumberEvents);            
        }

        return true;
    }

    private void DoPerPageSingleEventAudit(int numEventsPerPage) throws Exception
    {
        mTestRunner.NewLogFileGroup("Single event auditing (from QuickView pane) started", LogFile.iINFO);
        int count = 0;
        int asv = Integer.parseInt(mParameters.get("auditstatus"));
        for (int id=0; id < numEventsPerPage; id++)
        {
            
            Utils.DoClick(mTestCase, By.id("Row"+id), By.cssSelector("ul.quickPanes"), 30000);
            while (Utils.isElementPresent(mTestCase, By.id(mDlgProg), false)) { Thread.sleep(500); }

            if (mUseNewIssue)
            {
                mTestRunner.NewLogFileGroup("Auditing event id:" + id + " (New Issue dialog)", LogFile.iINFO);
                Utils.DoClick(mTestCase, By.id("btn_Pane.NewIssue"), By.id("Issue"), 10000);

                AuditStatusValue v = mTestRunner.GetAuditConfigurationManager().GetAuditStatusValue(asv);
                String wkIndex = Integer.toString(v.GetWKIndex());
                Utils.SelectOptionByValue(mTestCase, By.id("mandfield1"), wkIndex);

                if(!VerifyMandatoryFieldsDropdown(v.GetAvailableAuditOptions(), "mandfield2"))
                {
                    return;
                }
                
                AuditActionValue aav = mTestRunner.GetAuditConfigurationManager().GetAuditActionValue(Integer.parseInt(mParameters.get("auditaction")));
                wkIndex = Integer.toString(aav.GetWKIndex());
                Utils.SelectOptionByValue(mTestCase, By.id("mandfield2"), wkIndex);

                String selIndex = Integer.toString(Utils.GetSelectedValueAsInt(mTestCase, By.id("mandfield2")));
                String selText = Utils.GetText(mTestCase, By.cssSelector("select#mandfield2>option[value='" + selIndex + "']"), false);

                if (!VerifyMandatoryFieldsDropdown(aav.GetAvailableAuditResolutions(), "mandfield3"))
                {
                    return;
                }

                if (!selText.equals(aav.GetText()))
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit action field '%s' does not match audit action selected '%s' ", selText,aav.GetText()), LogFile.iERROR);
                } else {
                    mTestRunner.WriteLog("PASS: Audit Action value (" + selText + ") matches the value selected in the New Issue dialog", LogFile.iTEST);
                }

                AuditResolutionValue arv = mTestRunner.GetAuditConfigurationManager().GetAuditResolution(Integer.parseInt(mParameters.get("auditresolution")));
                wkIndex = Integer.toString(arv.GetWKIndex());
                Utils.SelectOptionByValue(mTestCase, By.id("mandfield3"), wkIndex);

                selIndex = Integer.toString(Utils.GetSelectedValueAsInt(mTestCase, By.id("mandfield3")));
                selText = Utils.GetText(mTestCase, By.cssSelector("select#mandfield3>option[value='" + selIndex + "']"), false);

                if (!selText.equals(arv.GetText()))
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit resolution field '%s' does not match audit resolution selected '%s' ", selText,arv.GetText()), LogFile.iERROR);
                } else {
                    mTestRunner.WriteLog("PASS: Audit resolution value (" + selText + ") matches the resolution selected in New Issue dialog", LogFile.iTEST);
                }

                Utils.DoClick(mTestCase, By.id("btn_Comment.Search"), By.id("ShuttlePicker"), 5000);

                AuditComment ac = mTestRunner.GetAuditConfigurationManager().GetComment(Integer.parseInt(mParameters.get("auditcomment")));
                Utils.SelectOptionByValue(mTestCase, By.id("selComment_picker"), ac.GetText());

                Utils.DoClick(mTestCase, By.id("btn_ShuttlePicker.OK"), null, 500);
                count = 0; while (Utils.isElementPresent(mTestCase, By.id("ShuttlePicker"), false) && count <10) { Thread.sleep(1000); count++; }

                String selComment = Utils.GetAttribute(mTestCase, By.id("selComment"), "value", false);
                if (!selComment.equals(ac.GetText()))
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit comment field '%s' does not match audit comment selected '%s' ", selComment,ac.GetText()), LogFile.iERROR);
                } else {
                    mTestRunner.WriteLog("PASS: Audit comment value (" + selComment + ") matches value selected in the New Issue dialog", LogFile.iTEST);
                }

                Utils.DoClick(mTestCase, By.id("btn_Issue.OK"), null, 500);
                count = 0; while (Utils.isElementPresent(mTestCase, By.id("Issue"), false) && count <10) { Thread.sleep(1000); count++; }

            } else {
                mTestRunner.NewLogFileGroup("Auditing event id:" + id + " (one-click button)", LogFile.iINFO);
                count = 0; while (!Utils.isEditable(mTestCase, By.id("btn_Pane.AuditButton0" + mAuditButton))&& count <20) { Thread.sleep(500); count++; }
                Utils.DoClick(mTestCase, By.id("btn_Pane.AuditButton0" + mAuditButton), null, 500);
                count = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgProg), false)&& count <20) { Thread.sleep(500); count++; }
                
                // Check that the audit status column has been updated.
                String auditText = Utils.GetText(mTestCase, By.cssSelector("td#Row" + id +"_update>span.textSpan"), false);
                String Field1Text = mTestCase.GetTestRunner().GetAuditConfigurationManager().GetAvailableAuditButtons().get(mAuditButton-1).GetField1Text();
                
                count = 0; 
                while (!auditText.equals(Field1Text) && count < 10) 
                {   
                    Thread.sleep(1000);  
                    auditText = Utils.GetText(mTestCase, By.cssSelector("td#Row" + id +"_update>span.textSpan"), false);
                    count++;
                }
                if (!auditText.equals(Field1Text))
                {
                    mTestRunner.WriteLog(String.format("FAIL: Audit status text does not match for event with (display) id:{%d}, expected audit status: '{%s}' actual audit status: '{%s}'", id, Field1Text,auditText), LogFile.iERROR);
                } else {
                    mTestRunner.WriteLog("PASS: Audit status column text (" + auditText + ") matches status of event id:" + id, LogFile.iTEST);
                }
            }
            mTestRunner.CloseLogFileGroup("Auditing event id: " + id + " complete", LogFile.iINFO);
        }
        mTestRunner.CloseLogFileGroup("Single event auditing complete", LogFile.iINFO);
    }

    private boolean VerifyMandatoryFieldsDropdown(List<String> availableAuditOptions, String selectId)
    {

        WebElement select = mDriver.findElement(By.id( selectId ));
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
        mTestRunner.WriteLog("DEBUG: " +selectId + " options: " + actualOpts.toString(), LogFile.iDEBUG);       
 
        String sOptionsXml = actualOpts.substring(actualOpts.indexOf("<"));

        SelectElement selectxml = new SelectElement(sOptionsXml);
        if (selectxml.Parse())
        {
            if(!selectxml.Compare(selectId, availableAuditOptions))
            {
                mTestRunner.WriteLog(selectxml.GetErrorDescription(), LogFile.iERROR);
                return false;
            } else {
                mTestRunner.WriteLog("PASS: the correct mandatory fields are set on the New Issue dialog", LogFile.iTEST);
            }

            return true;
        }

        mTestRunner.WriteLog(selectxml.GetErrorDescription(), LogFile.iERROR);
        return false;
    }

    public boolean CheckAllAuditCheckboxes(int numEventsPerPage) throws Exception
    {
        try
        {
            boolean isChecked = false;
            for (int rowIndex=0; rowIndex < numEventsPerPage; rowIndex++)
            {
                isChecked = Utils.isSelected(mTestCase, By.id("chk_Results.Row" + rowIndex));
                //isChecked = Utils.isSelected(mTestCase, By.cssSelector("tr#" + rowIndex + " > input[type='checkbox']"));
                if (!isChecked)
                {
                    mTestRunner.WriteLog("Row id:" + rowIndex + " is not selected.", LogFile.iERROR);
                    return false;
                }
            }
        } 
        catch (Exception e)
        {
            ReportError("Selecting all audit checkboxes caught exception: " + e.getMessage());
            return false;
        }
        mTestRunner.WriteLog("PASS: Confirmed 'Check All' selected all rows successfully", LogFile.iTEST);
        return true;
    }

    private boolean ParseParameters() throws Exception
    {
        mDlgProg = Utils.SetParam(mTestCase, "DLG_PROGRESS");
        try
        {
            mVerify = Boolean.parseBoolean(mParameters.get("verify"));
            mIsBulk = Boolean.parseBoolean(mParameters.get("bulk"));

            String sParameter = mParameters.get("quantity");
            if (sParameter.equals("all"))
            {
                mAuditAll = true;
            } else {
                mNumberEvents = Integer.parseInt(sParameter);
            }

            sParameter = mParameters.get("page");
            if (sParameter.equals("all"))
            {
                mAllPages = true;
            } else {
                mPages = Integer.parseInt(sParameter);
            }

            mUseNewIssue = Boolean.parseBoolean(mParameters.get("usenewissue"));
            mAuditButton = Integer.parseInt(mParameters.get("oneclickbutton"));            
        } 
        catch (Exception e)
        {
            ReportError("failed to parse parameters due to exception: " + e.getMessage());
            return false;
        }

        return true;
    }

    
}
