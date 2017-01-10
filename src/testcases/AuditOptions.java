//Copyright © 2011-2013 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 25/11/2011
 * overhauled for Truro 10/01/2013
 */

import java.util.*;
import common.*;
import org.openqa.selenium.By;
import java.sql.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AuditOptions extends WgnTestStep
{
    private int commentCount = 1; // one default comment exists from policies.msi, so count from 1 not 0 when adding more
    private String mDefAssoc;     // to retain the default action associations for the default status
    private String mDefAuditCfg;  // to retain the default Audit configuration as defined in WKType=11[9]
    private int mDefStatus;       // to retain the index of default audit status (currently WKType=2[1] "Pending" from policies.msi)
    private String[] mAuditFields = new String[7]; // for the default fields on the New Issue dialog
    private String mAuditLabel1 = "Ghostbusters";
    private String mAuditLabel2 = "Groundhog Day";
    private String mAuditLabel3 = "Lostin Translation";  // switch back to doublespace when bug 70129 is implemented
    private String parentHandle;  // parent iConsole window handle
    private String mStdSrchHash;
    private AuditConfigurationManager auditConfig;
    // params for GUI IDs
    private String mDlgProgress,mDlgCustomize,mBtnCustRun,
            mPaneAuditBtn3,mLnkViewIssue0,mBtnNewIssue,mDlgIssue,mTxtName,mStatus,mLblStatus,mAction,mLblAction,mResn,mLblResn,mPpts,
            mUserSearch,mAssocInc,mLstComment,mSelComment,mBtnComment,mBtnAdv,mBtnIssueOK,mBtnIssueCancel,
            mRow0,mChkRow,mBtnResEdit,mBtnResRefresh,mBtnBulk2,mBtnBulk3,mBtnBulk5,mDlgOp,mBtnOpOK,mTxtOpUp,mTxtOpSkip,mDlgBulkOp,mBtnBulkOpClose,
            mMsgBulkDesc,mMsgBulkClose,mRow0Update,mDlgShuttle,mLstShuttleAvl,mBtnShuttleAct0,mBtnShuttleAct3,mBtnShuttleOK,mBtnShuttleCancel,
            mClassDisBtn,mBtnReply,mDlgSendMail,mBtnTempls,mTxtTempl,mBtnTemplCancel,mBtnSendmailClose,mBtnFavPub,mCxtEdit,mOptEvType,
            mChkDIUAll,mChkDARAll,mChkNBA,mChkWeb,mTabAudit,mChkUnrev,mSelStatus;
    // params to hold default values
    private String as1Label, AF1_3_orig, mustComplete, button5restore, button3restore;   
    // params for text values (for localisation)
    private String txtMustBeComplete,txtReviewed,txtGenEsc,txtNonIssue,mAS1Close,mAS1Escalate,mIsComplete,
            mAuditSkippedText,mEventInfo,mTxtEmail,mAuditAction,mAuditResolution,mRequiredField;

    public AuditOptions() { }

    @Override
    public void Run() throws Exception
    {
        parentHandle = mTestCase.GetParentHandle();
        auditConfig = mTestRunner.GetAuditConfigurationManager();
        SetParameters();
       
        // need to select an email event to validate the default setting for the Participant field ("Sender")        
        GetEmailEvent();
        VerifyDefaultValues();        
        // set the first set of test values
        setTestValues1();
        // restart the iConsole (inc. iisreset)
        Reload();
        // validate changes were applied
        ValidateTest1();

        // load second set of test values
        setTestValues2();
        // reload iConsole (inc. iisreset)
        Reload();
        // validate changes were applied
        ValidateTest2();

        // load third set of test values
        setTestValues3();
        // reload iConsole (inc. iisreset)
        Reload();
        // validate changes were applied
        ValidateTest3();
        
        mTestRunner.CloseLogFileGroup("Specific value Auditing tests complete", LogFile.iINFO);
        // COMPLETE: Clean-up: restore the default audit options and config
        RestoreDefaults(button3restore, button5restore, AF1_3_orig);
        
        // close the results window and switch back to the parent window for shutdown
        Utils.CloseWindow(mTestCase, parentHandle);
    }
    
    private void GetEmailEvent() throws Exception
    {
        // need to select an email event to validate the default setting for the Participant field ("Sender")        
        for (int i = 0; i < Search.GetPageSize(); i++)
        {
            if (Utils.GetAttribute(mTestCase, By.cssSelector("tr#Row"+i+">td.iconField>span"), "title", true).startsWith(mTxtEmail))
            {
                String myEventRow = "Row"+Integer.toString(i);
                Utils.DoClick(mTestCase, By.id(myEventRow), null, 500);                
                break;
            }
        }
        // now open the New Issue dialog for the event    
        if (Utils.isElementPresent(mTestCase, By.id(mBtnNewIssue), true))
        {
            Utils.DoClick(mTestCase, By.id(mBtnNewIssue), By.id(mDlgIssue), 10000);
        } else {
            ReportError("ERROR: Couldn't locate an email event");
        }
    }
    
    private void VerifyDefaultValues() throws Exception
    {
        // see which fields are visible and whether this matches the expected state
        Boolean[] expected = auditConfig.GetAuditCheckboxStates();
        ConfirmVisibleFields(expected);

        // check policies.msi default options
        CheckIssueNameValue(Utils.GetAttribute(mTestCase, By.id(mTxtName), "value" ,false)); // by default should be using Custom name of "Security Issue"
        VerifyAuditSelectOptions(mStatus, auditConfig.GetAuditStatusStrList());
        VerifyAuditSelectOptions(mAction, auditConfig.GetAuditActionStrList());
        VerifyAuditSelectOptions(mResn, auditConfig.GetAuditResnStrList());
        CheckAssocParticipant(Utils.GetSelectedLabel(mTestCase, By.id(mPpts)));
        Utils.DoClick(mTestCase, By.id(mBtnComment), By.id(mDlgShuttle), 5000);
        VerifyAuditSelectOptions(mLstComment, auditConfig.GetAuditCommentsStrList());
        Utils.DoClick(mTestCase, By.id(mBtnShuttleCancel), null, 0);
        Utils.DoClick(mTestCase, By.id(mBtnIssueCancel), null, 0);

        // get default template value
        CheckMailReplyTemplate();
    }
    
    private void ValidateTest1() throws Exception
    {
        // first check visible fields
        Boolean[] newexpected = auditConfig.GetAuditCheckboxStates();
        ConfirmVisibleFields(newexpected);
        CheckIssueNameValue(Utils.GetAttribute(mTestCase, By.id(mTxtName), "value", false)); // should be using AS2 value of "Discuss with user"
        // verify the Audit field options
        VerifyAuditSelectOptions(mStatus, auditConfig.GetAuditStatusStrList());
        VerifyAuditSelectOptions(mAction, auditConfig.GetAuditActionStrList());
        // Check the Multiselect for AS3 is enabled correctly
        ValidateMultiAS3();
        // check All Managed users are associated with the new issue
        ValidateAllManaged();
        // check comments
        CommentCheck();
        // select one of the newly added statuses to check the new actions
        Utils.SelectOptionByText(mTestCase, By.id(mStatus), "Scrooged");
        VerifyAuditSelectOptions(mAction, auditConfig.GetAuditActionStrList());
        Utils.DoClick(mTestCase, By.id(mBtnIssueCancel), null, 250);
        int k = 0; while (Utils.isElementPresent(mTestCase, By.id(mBtnIssueCancel), false) && k < 10) { Thread.sleep(500); k++; }
        // check Mail templates 
        CheckMailReplyTemplate();
    }
    
    private void ValidateTest2() throws Exception
    {
        // confirm visible fields
        Boolean[] alloff_excAssUser = auditConfig.GetAuditCheckboxStates();
        ConfirmVisibleFields(alloff_excAssUser);
        // Issue name
        CheckIssueNameValue(Utils.GetAttribute(mTestCase, By.id(mTxtName), "value", false)); // should be using "empty"
        // Audit options
        VerifyAuditSelectOptions(mResn, auditConfig.GetAuditResnStrList());
        // Associated participant
        CheckAssocParticipant(Utils.GetSelectedLabel(mTestCase, By.id(mPpts)));
        // close Issue dialog
        Utils.DoClick(mTestCase, By.id(mBtnIssueCancel), null, 100);
    }
    
    private void ValidateTest3() throws Exception
    {    
        // confirm changes
        CheckIssueNameValue(Utils.GetAttribute(mTestCase, By.id(mTxtName), "value", false)); // should be using AS1 value of "Scrooged"        
        // check "Must be Complete" is applied correctly for Manual audit operation
        SingleAuditMustBeCompleteTests();
        // check "Must be Completed" is applied correctly for Bulk audit operation
        BulkAuditTests();
        // validate audit button 3 changes - can only audit events with existing issues of status=Escalate (2) 
        AuditButton3Tests();
    }
    
    private boolean VerifyAuditSelectOptions(String selectId, List<String> compareList)
    {
        // get the options from the dependent select option.
        Boolean ok = false;
        String sOptionsXml = Utils.GetSelectOptionsHTML(mTestCase, selectId);
        SelectElement selectxml = new SelectElement(sOptionsXml);
        selectxml.Parse();
 
        if (!selectxml.Compare(selectId, compareList))
        {
            ReportError(selectxml.GetErrorDescription());
        } else {
            mTestRunner.WriteLog("PASS: the correct values are listed in the " + selectId + " select {" + selectxml.GetDebugText() + "}", LogFile.iTEST);
            ok = true;
            //mTestRunner.WriteLogFileEntry(selectxml.GetDebugText(), LogFile.iDEBUG);
        }
        return ok;
    }

    private void ConfirmVisibleFields(Boolean[] expected)
    {
        Boolean pass = true;
        String badFields = "";
        String goodFields = "";
        for (int i=0; i < mAuditFields.length; i++)
        {
            if (Utils.isDisplayed(mTestCase, By.id(mAuditFields[i])) && expected[i])
            {
                if (goodFields.isEmpty())
                {
                    goodFields = mAuditFields[i];
                } else {
                    goodFields = goodFields + ", " + mAuditFields[i];
                }
            }
            else if (Utils.isDisplayed(mTestCase, By.id(mAuditFields[i])) && !expected[i])
            {
                pass = false;
                mTestRunner.WriteLog("FAIL: " + mAuditFields[i] + " is visible", LogFile.iERROR);
                if (badFields.isEmpty())
                {
                    badFields = mAuditFields[i];
                } else {
                    badFields = badFields + ", " + mAuditFields[i];
                }
            } 
            else if (!Utils.isDisplayed(mTestCase, By.id(mAuditFields[i])) && expected[i])
            {
                pass = false;
                mTestRunner.WriteLog("FAIL: " + mAuditFields[i] + " is not visible", LogFile.iERROR);
                if (badFields.isEmpty())
                {
                    badFields = mAuditFields[i];
                } else {
                    badFields = badFields + ", " + mAuditFields[i];
                }
            }            
        }
        if (pass)
        {
            mTestRunner.WriteLog("PASS: the correct audit fields are displayed (" + goodFields + ")", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: incorrect audit field display (bad fields: " + badFields + ")", LogFile.iERROR);
        }
    }

    private void CheckMailReplyTemplate() throws Exception
    {
        // mail templates are cached in the frontend for up to 60 mins so an iisreset is required prior to calling this function
        // check default mail reply template
        Utils.DoClick(mTestCase, By.id(mBtnReply), By.id(mDlgSendMail), 30000);
        Utils.DoClick(mTestCase, By.id(mBtnTempls), By.id(mTxtTempl), 5000);

        // get default template value
        String expTemp = auditConfig.GetWKType11Value(8);
        if (expTemp.contentEquals("") || expTemp.isEmpty() || expTemp == null) // Default reply template is set to "<Use Default Template>"
        {
            expTemp = auditConfig.GetAuditValue(10, 0); // get the Default mail template
        }
        String tempText = Utils.GetText(mTestCase, By.cssSelector("div#"+mTxtTempl+">ol>li.selected>a"), false);
        if (tempText.contains(expTemp))
        {
            mTestRunner.WriteLog("PASS: Default reply template is: " + expTemp, LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Default reply template is: " + tempText + ", expected: " + expTemp, LogFile.iERROR);
        }
        Utils.DoClick(mTestCase, By.id(mBtnTemplCancel), null, 0);
        int count = 0;
        while (Utils.isElementPresent(mTestCase, By.id(mTxtTempl), false) && count < 5) { count++; Thread.sleep(1000); }
        Utils.DoClick(mTestCase, By.id(mBtnSendmailClose), null, 0);
        count = 0;
        while (Utils.isElementPresent(mTestCase, By.id(mDlgSendMail), false) && count < 5) { count++; Thread.sleep(1000); }
    }

    private void CheckIssueNameValue(String text) throws Exception
    {
        if ( Utils.isElementPresent(mTestCase, By.id(mBtnAdv), false) )
        {
            Utils.DoClick(mTestCase, By.id(mBtnAdv), null, 100);
        }
        String s = auditConfig.GetWKType11Value(10);
        String report = "<blank>";
        switch (s) {
            case "0":
                s = auditConfig.GetWKType11Value(11);
                break;
            case "1":
                s = auditConfig.GetWKType11Value(2);
                s = auditConfig.GetAuditValue(2, Integer.parseInt(s.trim()));
                break;
            case "2":
                s = auditConfig.GetWKType11Value(3);
                s = auditConfig.GetAuditValue(5, Integer.parseInt(s.trim()));
                break;
            case "3":
                s = "";
                break;
        }
        if (!s.contentEquals("")) { report = s; }

        if (s.contentEquals(text)) 
        {
            mTestRunner.WriteLog("PASS: the Issue name is set correctly (" + report + ")", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: the Issue name is not set correctly (expected: " + report + ", actual: " + text + ")", LogFile.iERROR);
        }
    }
    
    private void CheckAssocParticipant(String text) throws Exception 
    {
        String s = auditConfig.GetWKType11Value(5);
        switch (s) {
            case "-3":
                s = "Sender";
                break;
            case "-4":
                s = "All";
                break;
            case "-5":
                s = "None (event-related)";
                break;
        }
        if (text.contains(s)) 
        {
            mTestRunner.WriteLog("PASS: the default associated user is set to " + text , LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: the default associated user is not set correctly (expected: " + s + ", actual: " + text + ")", LogFile.iERROR);
        }
    }

    private void Reload() throws Exception
    {
        // close the results window & logout
        Utils.CloseWindow(mTestCase, parentHandle);        
        Utils.Logoff(mTestCase);
        ConfigurationFile mConfigFile = mTestRunner.GetConfigurationFile();
        // issue an iisreset to the iConsole Frontend server to clear its cache (mail templates are cached)
        try
        {
            String exePath = mConfigFile.GetExtrasPath();
            if (!exePath.isEmpty()) exePath = exePath + "\\";
            String psexecCmd = "cmd /c start " + exePath + "psexec -u administrator -p tone \\\\" + mConfigFile.GetWebServer() + " iisreset";
            Process exec = Runtime.getRuntime().exec(psexecCmd);
            // look for any errors from the exec process
            BufferedReader errInput = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
            String line = null;            
            while ( (line = errInput.readLine()) != null)
            {
                mTestRunner.WriteLog("ERROR: " + line, LogFile.iINFO);
            }
            // look for any other output from the exec process
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            while ( (line = stdInput.readLine()) != null)
            {
                mTestRunner.WriteLog("PSEXEC: " + line, LogFile.iINFO);
            }
            // wait until psexec completes and check the exit value
            int exitVal = exec.waitFor();
            if (exitVal == 0)
            {
                mTestRunner.WriteLog("IISReset succeeded on " + mConfigFile.GetWebServer(), LogFile.iINFO);
            } else {
                mTestRunner.WriteLog("IISReset Failed (" + exitVal + ") on " + mConfigFile.GetWebServer(), LogFile.iERROR);
            }
        }
        catch (IOException e)
        {
            mTestRunner.WriteLog("IISReset failed: " + e.toString(), LogFile.iERROR);
        }

        Utils.Login(mTestCase, "primary");
        // get email and IM events only
        Utils.DoClick(mTestCase, By.id(mBtnFavPub + mStdSrchHash), null, 100);
        Utils.DoClick(mTestCase, By.id(mCxtEdit), By.id(mDlgCustomize), 20000);
        Utils.DoClick(mTestCase, By.id(mOptEvType), null, 10);
        Utils.DoClick(mTestCase, By.id(mChkDIUAll), null, 10);
        Utils.DoClick(mTestCase, By.id(mChkDARAll), null, 10);
        Utils.DoClick(mTestCase, By.id(mChkNBA), null, 10);
        Utils.DoClick(mTestCase, By.id(mChkWeb), null, 10);
        
        parentHandle = Utils.GetNewWindow(mTestCase, By.id(mBtnCustRun));
        
        int i = 0; while (!Utils.isElementPresent(mTestCase, By.id(mRow0), false) && i < 20) { i++; Thread.sleep(1000); }
        Utils.DoClick(mTestCase, By.id(mRow0), null, 100);
        Utils.DoClick(mTestCase, By.id(mBtnNewIssue), By.id(mDlgIssue), 10000);
    }
    
    private void VerifyBulkAuditOK(String numEvents) throws Exception
    {
        String isComplete = "";
        while (!isComplete.contains(mIsComplete)) 
        {
            isComplete = Utils.GetText(mTestCase, By.id(mDlgProgress), false);
            //mTestRunner.WriteLogFileEntry("Progress content = " + isComplete, LogFile.iDEBUG);
        }

        String updatedText = Utils.GetText(mTestCase,By.id(mTxtOpUp),false);
        if (!updatedText.contains(numEvents))
        {
            mTestRunner.WriteLog(String.format("FAIL: Error checking bulk audit operation report, updated Text '%s' does not contain number of events on page '%s'",updatedText,numEvents), LogFile.iERROR);
        } else {
            mTestRunner.WriteLog("PASS: Bulk Audit operation completed successfully for " + numEvents + " events", LogFile.iTEST);
        }
        Utils.DoClick(mTestCase, By.id(mBtnBulkOpClose), null, 0);
        
        int count = 0;
        while (Utils.isElementPresent(mTestCase, By.id(mDlgBulkOp), false) && count < 5) { count++; Thread.sleep(1000); }
    }
    
    private void ValidateMultiAS3() throws Exception
    {
        // replace spaces with underscores in Audit field label
        String newLbl = replaceSpace(mAuditLabel3);
        // now test the translated value is used ok
        if (!Utils.isElementPresent(mTestCase, By.id(mResn), false) && Utils.isElementPresent(mTestCase, By.id("btn_"+newLbl+".Search"), false) )
        {
            mTestRunner.WriteLog("PASS: Allow Multi-select for Audit Field 3 (Resolution) successfully enabled", LogFile.iTEST);
            Utils.DoClick(mTestCase, By.id("btn_"+newLbl+".Search"), By.id(mDlgShuttle), 5000, true);
            VerifyAuditSelectOptions(mLstShuttleAvl, auditConfig.GetAuditResnStrList());
            Utils.DoClick(mTestCase, By.id(mBtnShuttleCancel), null, 100);
        } else {
            mTestRunner.WriteLog("FAIL: Allow Multi-select for Audit Field 3 (Resolution) not enabled", LogFile.iERROR);
            VerifyAuditSelectOptions(mResn, auditConfig.GetAuditResnStrList());
        }
        // Check multiselect has been enabled for Associated User field
        if (!Utils.isElementPresent(mTestCase, By.id(mPpts), false) && Utils.isElementPresent(mTestCase, By.id(mUserSearch), false) )
        {
            mTestRunner.WriteLog("PASS: Allow Multi-select for Associated User field successfully enabled", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Allow Multi-select for Associated User field not enabled", LogFile.iERROR);
        }
    }
    
    private void ValidateAllManaged() throws Exception
    {
        if (Utils.isElementPresent(mTestCase, By.cssSelector("label#"+mPpts+"Label>span")) )
        {
            Utils.DoClick(mTestCase, By.id(mUserSearch), By.id(mDlgShuttle), 5000);
            String btn_action0_class = Utils.GetAttribute(mTestCase, By.id(mBtnShuttleAct0), "class", true);
            String btn_action3_class = Utils.GetAttribute(mTestCase, By.id(mBtnShuttleAct3), "class", true);
            if (btn_action0_class.contains(mClassDisBtn) && !btn_action3_class.contains(mClassDisBtn))
            {
                mTestRunner.WriteLog("PASS: All Managed Users are associated with the New Issue", LogFile.iTEST);
                // check "Prevent no associated participants" is ON
                Utils.DoClick(mTestCase, By.id(mBtnShuttleAct3), null, 100);
                Utils.DoClick(mTestCase, By.id(mBtnShuttleOK), null, 100);
                if (Utils.isTextPresent(mTestCase, mRequiredField) && Utils.isElementPresent(mTestCase, By.cssSelector("span.validationMessage"), false) )
                {
                    mTestRunner.WriteLog("PASS: Prevent No Associated Participants was enforced", LogFile.iTEST);
                    Utils.DoClick(mTestCase, By.id(mUserSearch), By.id(mDlgShuttle), 5000);
                    Utils.SelectOptionByValue(mTestCase, By.id(mLstShuttleAvl), "0");
                    Utils.DoClick(mTestCase, By.id(mBtnShuttleAct0), null, 100);
                    Utils.DoClick(mTestCase, By.id(mBtnShuttleOK), null, 100);
                } else {
                    mTestRunner.WriteLog("FAIL: Prevent No Associated Participants was not enforced", LogFile.iERROR);
                }      
            } else {
                mTestRunner.WriteLog("FAIL: All Managed Users are not associated with the New Issue", LogFile.iERROR);
                Utils.DoClick(mTestCase, By.id(mBtnShuttleCancel), null, 10);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Participants field is not marked as a required field", LogFile.iERROR);
        } 
    }
    
    private void CommentCheck() throws Exception
    {
        Utils.DoClick(mTestCase, By.id(mBtnComment), By.id(mDlgShuttle), 5000);
        VerifyAuditSelectOptions(mLstComment, auditConfig.GetAuditCommentsStrList());
        Utils.DoClick(mTestCase, By.id(mBtnShuttleCancel), null, 100);

        // check the Audit field labels
        as1Label = Utils.GetText(mTestCase, By.id(mLblStatus), false);
        if (as1Label.contentEquals(mAuditLabel1+":")) {
            mTestRunner.WriteLog("PASS: Correct label shown for Audit Field 1: " + as1Label, LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Audit Field 1 label is: " + as1Label, LogFile.iERROR);
        }
        String as2Label = Utils.GetText(mTestCase, By.id(mLblAction), false);
        if (as2Label.contentEquals(mAuditLabel2+":")) {
            mTestRunner.WriteLog("PASS: Correct label shown for Audit Field 2: " + as2Label, LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Audit Field 2 label is: " + as2Label, LogFile.iERROR);
        }
        String as3Label = Utils.GetText(mTestCase, By.id(mLblResn), false);
        if (as3Label.contentEquals(mAuditLabel3+":")) {
            mTestRunner.WriteLog("PASS: Correct label shown for Audit Field 3: " + as3Label, LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Audit Field 3 label is: " + as3Label, LogFile.iERROR);
        }
    }
    
    private void SingleAuditMustBeCompleteTests() throws Exception
    {
        // check "Must be Complete" is applied correctly for Manual audit operation
        Utils.SelectOptionByText(mTestCase, By.id(mStatus), mustComplete);        
        if (Utils.isElementPresent(mTestCase, By.cssSelector("label#"+mLblAction+">span"), false)
                && Utils.isElementPresent(mTestCase, By.cssSelector("label#"+mLblAction+">span"), false) ) 
        {
            mTestRunner.WriteLog("PASS: Audit Fields 2 & 3 set to 'Must Be Completed' when '"+mustComplete+"' selected for Field 1", LogFile.iTEST);
            Utils.DoClick(mTestCase, By.id(mBtnIssueOK), null, 0);
            if (Utils.isTextPresent(mTestCase,mRequiredField)
                    && Utils.isElementPresent(mTestCase, By.cssSelector("span.validationMessage"), false) )
            {
                mTestRunner.WriteLog("PASS: Validation error provoked by failing to fill in both 'Must Be Completed' fields", LogFile.iTEST);
                Utils.SelectOptionByText(mTestCase, By.id(mAction), mAuditAction);                
                if (Utils.isTextPresent(mTestCase,mRequiredField)
                        && Utils.isElementPresent(mTestCase, By.cssSelector("span.validationMessage"), false) )
                {
                    mTestRunner.WriteLog("PASS: Validation error provoked by failing to fill in 'Must Be Completed' field 3", LogFile.iTEST);
                    Utils.SelectOptionByValue(mTestCase, By.id(mAction), "-1");
                    Utils.SelectOptionByText(mTestCase, By.id(mResn), mAuditResolution);
                    if (Utils.isTextPresent(mTestCase, mRequiredField) 
                            && Utils.isElementPresent(mTestCase, By.cssSelector("span.validationMessage")) )
                    {
                        mTestRunner.WriteLog("PASS: Validation error provoked by failing to fill in 'Must Be Completed' field 2", LogFile.iTEST);
                        Utils.DoClick(mTestCase, By.id(mBtnIssueCancel), null, 100);
                    } else {
                        mTestRunner.WriteLog("FAIL: Validation error not provoked when creating Issue without setting Field 2 value", LogFile.iERROR);
                    }
                } else {
                     mTestRunner.WriteLog("FAIL: Validation error not provoked when creating Issue without setting Field 3 value", LogFile.iERROR);
                }                
            } else {
                mTestRunner.WriteLog("FAIL: Validation error not provoked when creating Issue without setting Field 2 & 3 values", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Audit Fields 2 & 3 not set to Must Complete when 'Close' selected for Field 1", LogFile.iERROR);
        }
        
        if ( Utils.isElementPresent(mTestCase, By.id(mDlgIssue), false) )
        {
            Utils.DoClick(mTestCase, By.id(mBtnIssueCancel), null, 100);
        }
    }
    
    private void BulkAuditTests() throws Exception
    {
        // Chrome browser can run into problems clicking the results row chkboxes here, a refresh of the results avoids this
        if (mTestRunner.GetTargetBrowser().contentEquals("chrome"))
        {            
            int m = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgIssue), false) && m < 10) { Thread.sleep(500); m++; }
            Utils.DoClick(mTestCase, By.id(mBtnResRefresh), null, 100);            
        }
        // select events to bulk audit and click audit button 5 - should fail
        mTestRunner.NewLogFileGroup("Bulk Audit using Field 1 value with 'Must Be Completed' setting:", LogFile.iINFO);
        Boolean checked = true;
        for (int i = 0; i < 3; i++)
        {
            Utils.DoClick(mTestCase, By.id(mChkRow + i), null, 500);
            if ( !Utils.isSelected(mTestCase, By.id(mChkRow + i)) ) { checked = false; }
        }
        if (!checked)
        {
            mTestRunner.WriteLog("ERROR: failed to select all the results rows checkboxes for bulk auditing", LogFile.iERROR);
        }
        if (Utils.isElementPresent(mTestCase, By.id(mBtnBulk5)) )
        {
            Utils.DoClick(mTestCase, By.id(mBtnBulk5), By.id(mDlgOp), 10000);
            if ( Utils.isElementPresent(mTestCase, By.id(mDlgOp)) )
            {
                mTestRunner.WriteLog("PASS: Audit Operation dialog prompted for 'Must Be Completed' fields", LogFile.iTEST);
                Utils.SelectOptionByText(mTestCase, By.id(mAction), mAuditAction);
                Utils.SelectOptionByText(mTestCase, By.id(mResn), mAuditResolution);                
                Utils.DoClick(mTestCase, By.id(mBtnOpOK), By.id(mDlgBulkOp), 10000);
                VerifyBulkAuditOK("3");
            } else {
                mTestRunner.WriteLog("FAIL: 'Must Be Completed' check failed on Bulk Auditing - no audit operation dialog", LogFile.iERROR);
            }
        }
        mTestRunner.CloseLogFileGroup("Bulk Audit using Field 1 value with 'Must Be Completed' complete", LogFile.iINFO);
        
        // select events to bulk audit and click audit button 2 - will work
        mTestRunner.NewLogFileGroup("Bulk Audit using Field 1 value without 'Must Be Completed' setting:", LogFile.iINFO);
        checked = true;
        for (int i = 3; i < 6; i++)
        {
            String sId = mChkRow + i; //"//tr[@id='" + i + "']/td[position()=1]/input";
            Utils.DoClick(mTestCase, By.id(sId), null, 500);
            if ( !Utils.isChecked(mTestCase, By.id(sId), "Event row id:"+i, "true", true) ) { checked = false; }
        }
        if (!checked)
        {
            mTestRunner.WriteLog("ERROR: failed to select all the results rows checkboxes for bulk auditing", LogFile.iERROR);
        }
        if ( Utils.isElementPresent(mTestCase, By.id(mBtnBulk2)) )
        {
            Utils.DoClick(mTestCase, By.id(mBtnBulk2), By.id(mDlgBulkOp), 10000);
            VerifyBulkAuditOK("3");
        }
        mTestRunner.CloseLogFileGroup("Bulk Audit using Field 1 value without 'Must Be Completed' complete:", LogFile.iINFO);
    }
    
    private void AuditButton3Tests() throws Exception
    {
        // validate audit button 3 changes - can only audit events with existing issues of status=Escalate (2)
        // first retrieve only audited events with status = Escalate & do a single event audit from Quick View pane
        Boolean checked = true;
        mTestRunner.NewLogFileGroup("Testing Auditing Issues with specific values:", LogFile.iINFO);
        Utils.CheckAuditPrefs(mTestCase, true);
        Utils.DoClick(mTestCase, By.id(mBtnResEdit), By.id(mDlgCustomize), 30000);
        Utils.DoClick(mTestCase, By.id(mTabAudit), null, 1000);        
        Utils.ToggleCheckbox(mTestCase, By.id(mChkUnrev), "off");       
        
        Utils.SelectOptionByText(mTestCase, By.id(mSelStatus), mAS1Escalate);
        Utils.DoClick(mTestCase, By.id(mBtnCustRun), null, 500);        
        int count = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgProgress), false) && count < 20) { count++; Thread.sleep(1000); }
        if (Utils.GetText(mTestCase, By.cssSelector("td#"+mRow0Update+">span"), false).contentEquals(mAS1Escalate))
        {
            mTestRunner.WriteLog("PASS: Search returned only audited events with status="+mAS1Escalate, LogFile.iTEST);
            Utils.DoClick(mTestCase, By.id(mRow0), null, 1000);
            count = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgProgress), false) && count < 20) { count++; Thread.sleep(1000); }
            // single audit the selected event with Quick View Audit button 3
            if (Utils.isElementPresent(mTestCase, By.id(mPaneAuditBtn3), false)) 
            {
                Utils.DoClick(mTestCase, By.id(mPaneAuditBtn3), null, 500);
                count = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgProgress), false) && count < 20) { count++; Thread.sleep(1000); }
                Boolean isComplete = false;
                count = 0;
                while (!isComplete && count < 10) 
                {
                    if ( Utils.GetText(mTestCase, By.cssSelector("td#"+mRow0Update+">span"), false).contentEquals(mAS1Close) ) { isComplete = true; }
                    count++; Thread.sleep(1000);
                }
                String eventRowAudit = Utils.GetText(mTestCase, By.cssSelector("td#"+mRow0Update+">span"), true);
                if (eventRowAudit.contentEquals(mAS1Close))
                {
                    mTestRunner.WriteLog("PASS: Audit Issue updated to " + eventRowAudit, LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: Audit Issue not updated; Status: " + eventRowAudit, LogFile.iERROR);
                }

                Utils.DoClick(mTestCase, By.id(mLnkViewIssue0), By.id(mDlgIssue), 5000);
                as1Label = Utils.GetSelectedLabel(mTestCase, By.id(mStatus));
                if (as1Label.contentEquals(mAS1Close)) 
                {
                    mTestRunner.WriteLog("PASS: Audit issue updated to " + as1Label, LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: Audit issue failed to be updated; Status: " + as1Label, LogFile.iERROR);
                }
                Utils.DoClick(mTestCase, By.id(mBtnIssueCancel), null, 100);
            } else {
                mTestRunner.WriteLog("ERROR: Audit Button 3 is not available on the Quick View Toolbar", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Search failed to return only audited events with status="+mAS1Escalate, LogFile.iERROR);
        }
        
        // now retrieve audited events with Status = Close and confirm they can't be bulk updated with Bulk AuditButton3
        Utils.DoClick(mTestCase, By.id(mBtnResEdit), By.id(mDlgCustomize), 20000);
        Utils.DoClick(mTestCase, By.id(mTabAudit), null, 100);  // goto Audit tab
        count = 0;
        while (!Utils.isDisplayed(mTestCase, By.id(mChkUnrev)) && count < 5) { count++; Thread.sleep(1000); }
        Utils.SelectOptionByText(mTestCase, By.id(mSelStatus), mAS1Close);
        Utils.DoClick(mTestCase, By.id(mBtnCustRun), null, 500);
        count = 0; while (Utils.isElementPresent(mTestCase, By.id(mDlgProgress), false) && count < 20) { count++;  Thread.sleep(1000); }
        if (Utils.GetText(mTestCase, By.cssSelector("td#"+mRow0Update+">span"), true).contentEquals(mAS1Close))
        {
            mTestRunner.WriteLog("PASS: Search returned only audited events with status="+mAS1Close, LogFile.iTEST);
            // select events to bulk audit
            checked = true;
            for (int i = 0; i < 2; i++)
            {
                String sId = mChkRow + i; //String x_path = "//tr[@id='" + i + "']/td[position()=1]/input";
                Utils.DoClick(mTestCase, By.id(sId), null, 500);
                if ( !Utils.isChecked(mTestCase, By.id(sId), "Event row id:"+i, "true", true) ) {  checked = false; }
            }
            if (!checked)
            {
                mTestRunner.WriteLog("ERROR: failed to select all the results rows checkboxes for bulk auditing", LogFile.iERROR);
            }
            Utils.DoClick(mTestCase, By.id(mBtnBulk3), By.id(mDlgBulkOp), 10000);
            // Bulk Audit Operation dialog should show the events have been skipped
            String updatedText = Utils.GetText(mTestCase, By.id(mTxtOpSkip), true);
            if (!updatedText.contains("2"))
            {
                mTestRunner.WriteLog("FAIL: Bulk audit operation report does not show 2 events skipped", LogFile.iERROR);
            } else {
                mTestRunner.WriteLog("PASS: Bulk Audit operation skipped 2 events", LogFile.iTEST);
                Utils.DoClick(mTestCase, By.id(mBtnAdv), By.id(mMsgBulkDesc), 5000);
                count = 0;
                while (!Utils.isTextPresent(mTestCase, mEventInfo) && count < 5) { count++; Thread.sleep(1000); }       
                if (Utils.isTextPresent(mTestCase, mAuditSkippedText))
                {
                    mTestRunner.WriteLog("PASS: Event Information dialog shows '" + mAuditSkippedText + "'", LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: Event Information dialog does not show skipped audit reason", LogFile.iERROR);
                }
                Utils.DoClick(mTestCase, By.id(mMsgBulkClose), null, 100);
                count = 0;
                while (Utils.isTextPresent(mTestCase, mEventInfo, false) && count < 5) { count++; Thread.sleep(1000); }
            } 
            Utils.DoClick(mTestCase, By.id(mBtnBulkOpClose), null, 100);
            count = 0;
            while (Utils.isElementPresent(mTestCase, By.id(mDlgBulkOp), false) && count < 5) { count++; Thread.sleep(1000); }           
        } else {
            mTestRunner.WriteLog("FAIL: Search failed to return only audited events with status="+mAS1Close, LogFile.iERROR);
        }
    }

    private void setTestValues1() throws SQLException
    {
        try
        {
            // first store some defaults so they can be restored later
            mDefAuditCfg = auditConfig.GetWKType11Value(-1);
            mDefStatus = Integer.parseInt(auditConfig.GetWKType11Value(2).trim());
            // change and add some audit options
            auditConfig.SetAuditValue(2, 39, "Scrooged");
            auditConfig.SetAuditValue(2, 5, "Done and Dusted");
            auditConfig.SetAuditValue(5, 39, "Kick");
            auditConfig.SetAuditValue(6, 39, "IFA Informed");
            auditConfig.SetAuditValue(6, 5, "Disease cured");
            // add comments
            auditConfig.AddAuditValue(3, commentCount, "Bananas in pyjamas are coming up the stairs"); commentCount++;
            auditConfig.AddAuditValue(3, commentCount, "How do you like them apples?"); commentCount++;
            // change the Audit field default names
            auditConfig.SetAuditValue(7, 0, mAuditLabel1);
            auditConfig.SetAuditValue(7, 1, mAuditLabel2);
            auditConfig.SetAuditValue(7, 2, mAuditLabel3);

            // setup new Action associations for the default Audit Status            
            if (mDefStatus == -1) // check if default is blank
            {
                mDefAssoc = "";   // default is blank so default associations aren't relevant
            } else {
                mDefAssoc = auditConfig.GetAuditValue(18, mDefStatus);
            }
            String concat = mDefAssoc;
            for (int i=11; i < 17; i++)
            {   // add new actions to the Action list
                auditConfig.SetAuditValue(5, i, "Punch"+i);
                if (!(i % 2 == 0))  // only append the new odd numbered Actions
                {
                    concat = concat+":"+i;  // build the new Action association list
                }
            }
            if (mDefStatus != -1)
            {
                // add the new odd numbered actions to the default Status's Action list
                auditConfig.SetAuditValue(18, mDefStatus, concat);
            }

            // change the audit configuration
            // switch all the fields to visible
            auditConfig.SetWKType11Value(9, "503");
            // change default mail reply template
            auditConfig.SetWKType11Value(8, txtGenEsc);
            // allow multiselect for Audit Field 3
            auditConfig.SetWKType11Value(1, "1");            
            mAuditFields[3] = "btn_" + replaceSpace(mAuditLabel3) + ".Search";   // id=mandfield3 should be hidden and this control appear instead
            // allow multiple user association
            auditConfig.SetWKType11Value(6, "1");
            mAuditFields[4] = mUserSearch;   // id=Participants should be hidden and this control appear instead
            // set default issue name to Audit Field 2 default of "Discuss with user"
            auditConfig.SetWKType11Value(10, "2");
            auditConfig.SetWKType11Value(3, "1");
            // set default associated participant to "All Managed"
            auditConfig.SetWKType11Value(5, "-4");
            // set "Prevent no associated participants" 
            auditConfig.SetWKType11Value(7, "1");
        }
        catch (SQLException se)
        {
            ReportError("Failed to change the Audit DB values due to: " + se.toString());
            throw se;
        }
    }
    
    private void setTestValues2() throws SQLException
    {
        try 
        {
            // make some more changes
            // hide all fields on New Issue except Assoc. User
            auditConfig.SetWKType11Value(9, "258");
            // disallow multiselect for Audit Field 3
            auditConfig.SetWKType11Value(1, "0");
            mAuditFields[3] = mResn;   // id=btn_Resolution.Search should be hidden and this control appear instead
            // disallow multiple user association
            auditConfig.SetWKType11Value(6, "0");
            mAuditFields[4] = mPpts;   // id=btn_Associated Users.Search should be hidden and this control appear instead
            // set default issue name to "empty"
            auditConfig.SetWKType11Value(10, "3");
            // set default associated participant to "None"
            auditConfig.SetWKType11Value(5, "-5");
            // unset "Prevent no associated participants" 
            auditConfig.SetWKType11Value(7, "0");
        }
        catch (SQLException se)
        {
            ReportError("Failed to change the Audit DB values due to: " + se.toString());
            throw se;
        }
    }
    
    private void setTestValues3() throws SQLException
    {
        try 
        {
            // show all fields on New Issue dialog:
            // AF1 must be shown as Issue name is being set to value of it
            // AF2 & 3 must be shown as "Must be complete" is being enabled
            auditConfig.SetWKType11Value(9, "503");
            // set default issue name to Audit Field 1 value of "Scrooged"
            auditConfig.SetWKType11Value(10, "1");
            auditConfig.SetWKType11Value(2, "39");
            // set the Audit Field 1 "Close" entry to other fields "Must be Completed"
            // do this by appending a '*' to its current value
            AF1_3_orig = auditConfig.GetAuditValue(2, 3);
            mustComplete = AF1_3_orig; // + "*";
            auditConfig.SetAuditValue(2, 3, mustComplete+"*");
            // configure a bulk audit button to test "Must be complete"
            button5restore = auditConfig.GetAuditValue(8, 4);
            button3restore = auditConfig.GetAuditValue(8, 2);
            auditConfig.SetAuditValue(8,4,"2,2,0,-1,3,-1,-1,2, ,"+txtMustBeComplete+", ");
            auditConfig.SetAuditValue(8,2,"2,3,0,2,3,2,2,2, ,"+txtNonIssue+","+txtReviewed);
        }
        catch (SQLException se)
        {
            ReportError("Failed to change the Audit DB values due to: " + se.toString());
            throw se;
        }
    }
    
    private void RestoreDefaults(String btn3, String btn5, String AF1_3) throws Exception
    {
        try
        {            
            // association changes first to avoid overflows in the available values arrays
            if (mDefStatus != -1)
            {
                auditConfig.SetAuditValue(18, mDefStatus, mDefAssoc);
            }
            // then the audit config
            auditConfig.SetAuditValue(11, 0, mDefAuditCfg);
            // then the actual option values
            auditConfig.SetAuditValue(2, 39, "");
            auditConfig.SetAuditValue(2, 5, "");
            if (auditConfig.GetAuditValue(5, 0).contentEquals("Dummy"))
            {   // do this to avoid the array offset issue which may get fixed in Wigan (bug 61594)
                auditConfig.DeleteWKSRow(5, 0);
            }
            auditConfig.DeleteWKSRow(5, 39);
            auditConfig.DeleteWKSRow(6, 39);
            auditConfig.DeleteWKSRow(6, 5);
            for (int i=11; i < 21; i++)
            {
                auditConfig.DeleteWKSRow(5, i);
            }
            // finally delete any comments that were added
            for (int i = commentCount; i > 0; i--)
            {
                auditConfig.DeleteWKSRow(3, i);
            }
            // restore Audit Field titles
            auditConfig.SetAuditValue(7, 0, "Audit Status");
            auditConfig.SetAuditValue(7, 1, "Action");
            auditConfig.SetAuditValue(7, 2, "Resolution");
            // turn off "Must be completed"
            auditConfig.SetAuditValue(2, 3, AF1_3);
            // restore Audit Button default values
            auditConfig.SetAuditValue(8, 2, btn3);
            auditConfig.SetAuditValue(8, 4, btn5);
            
            Utils.CheckAuditPrefs(mTestCase, false);
        }
        catch (SQLException se)
        {
            ReportError("Failed to clean up the Audit DB values due to: " + se.toString());
            throw se;
        }
    }
    
    private String replaceSpace(String sTranslate)
    {
        // replace spaces with underscores in Audit field label
        int numChar = sTranslate.length();
        char[] charArray = new char[numChar];
        sTranslate.getChars(0, numChar, charArray,0);
        int i=0;
        while(i < charArray.length)
        {
            if (charArray[i] == ' ') charArray[i] = '_';
            i++;
        }
        return new String(charArray);
    }
    
    private void SetParameters()
    {
        mIsComplete = Utils.SetParam(mTestCase, "isComplete");
        mStdSrchHash = Utils.SetParam(mTestCase,"StdSrchHash");
        mAuditAction = Utils.SetParam(mTestCase, "AuditAction");
        mAuditResolution = Utils.SetParam(mTestCase, "AuditResolution");
        mRequiredField = Utils.SetParam(mTestCase, "RequiredField");
        mAS1Close = Utils.SetParam(mTestCase, "as1Close");
        mAS1Escalate = Utils.SetParam(mTestCase, "as1Escalate");
        mAuditSkippedText = Utils.SetParam(mTestCase, "AuditSkippedText");
        mTxtEmail = Utils.SetParam(mTestCase, "txtEmail");
        mEventInfo = Utils.SetParam(mTestCase, "eventInfo");
        // GUI Ids
        mDlgProgress = Utils.SetParam(mTestCase, "DLG_PROGRESS");
        mDlgCustomize = Utils.SetParam(mTestCase, "DLG_CUSTOMIZE");
        mBtnCustRun = Utils.SetParam(mTestCase, "BTN_CUST_RUN");        
        mPaneAuditBtn3 = Utils.SetParam(mTestCase, "BTN_PANE_AUDIT3");
        mLnkViewIssue0 = Utils.SetParam(mTestCase, "LNK_ISSUE0");
        mBtnNewIssue = Utils.SetParam(mTestCase, "BTN_NEW_ISSUE");
        mDlgIssue = Utils.SetParam(mTestCase, "DLG_ISSUE");
        mTxtName = Utils.SetParam(mTestCase, "TXT_ISSUE_NAME");
        mStatus = Utils.SetParam(mTestCase, "SEL_STATUS");
        mLblStatus = Utils.SetParam(mTestCase, "LBL_STATUS");
        mAction = Utils.SetParam(mTestCase, "SEL_ACTION");
        mLblAction = Utils.SetParam(mTestCase, "LBL_ACTION");
        mResn = Utils.SetParam(mTestCase, "SEL_RES");
        mLblResn = Utils.SetParam(mTestCase, "LBL_RES");
        mPpts = Utils.SetParam(mTestCase, "SEL_PPTS");
        mUserSearch = Utils.SetParam(mTestCase, "BTN_PPTS");
        mAssocInc = Utils.SetParam(mTestCase, "BTN_INCS");
        mLstComment = Utils.SetParam(mTestCase, "LST_COMMENT");
        mSelComment = Utils.SetParam(mTestCase, "SEL_COMMENT");
        mBtnComment = Utils.SetParam(mTestCase, "BTN_COMMENT");
        mBtnAdv = Utils.SetParam(mTestCase, "BTN_ADV");
        mBtnIssueOK = Utils.SetParam(mTestCase, "BTN_ISSUE_OK");
        mBtnIssueCancel = Utils.SetParam(mTestCase, "BTN_ISSUE_CANCEL"); 
        mRow0 = Utils.SetParam(mTestCase, "RES_ROW"); //id=Row0
        mRow0Update = Utils.SetParam(mTestCase, "RES_ROW_UPDATE"); //id=Row0_update
        mChkRow = Utils.SetParam(mTestCase, "CHK_RES_ROW"); //id=chk_Results.Row
        mBtnResEdit = Utils.SetParam(mTestCase, "BTN_RES_EDIT"); //id=btn_Results.Edit
        mBtnResRefresh = Utils.SetParam(mTestCase, "BTN_RES_REFRESH"); //id=btn_Results.Refresh
        mBtnBulk2 = Utils.SetParam(mTestCase, "BTN_BULK02"); //id=btn_Results.ToolAudit02
        mBtnBulk3 = Utils.SetParam(mTestCase, "BTN_BULK03"); //id=btn_Results.ToolAudit03
        mBtnBulk5 = Utils.SetParam(mTestCase, "BTN_BULK05"); //id=btn_Results.ToolAudit05
        mDlgOp = Utils.SetParam(mTestCase, "DLG_OP"); //id=Operation
        mBtnOpOK = Utils.SetParam(mTestCase, "BTN_OP_OK"); //id=btn_Operation.OK
        mTxtOpUp = Utils.SetParam(mTestCase, "TXT_OP_UP"); //id=Operation_resUpdated
        mTxtOpSkip = Utils.SetParam(mTestCase, "TXT_OP_SKIP"); //id=Operation_resSkipped
        mDlgBulkOp = Utils.SetParam(mTestCase, "DLG_BULK_OP"); //id=BulkAuditOperation
        mBtnBulkOpClose = Utils.SetParam(mTestCase, "BTN_BULK_CLOSE"); //id=btn_BulkAuditOperation.Close
        mMsgBulkDesc = Utils.SetParam(mTestCase, "TXT_BULK_DESC"); //id=_desc
        mMsgBulkClose = Utils.SetParam(mTestCase, "MSG_BULK_CLOSE"); //id=btn_.Close
        mDlgShuttle = Utils.SetParam(mTestCase, "DLG_SHUTTLE"); //id=ShuttlePicker
        mLstShuttleAvl = Utils.SetParam(mTestCase, "LST_SHUTTLE_AVL"); //id=shuttlepicker_available
        mBtnShuttleAct0 = Utils.SetParam(mTestCase, "BTN_SHUTTLE_ACT0"); //id=btn_shuttlepicker.Action.0
        mBtnShuttleAct3 = Utils.SetParam(mTestCase, "BTN_SHUTTLE_ACT3"); //id=btn_shuttlepicker.Action.3
        mBtnShuttleOK = Utils.SetParam(mTestCase, "BTN_SHUTTLE_OK"); //id=btn_ShuttlePicker.OK
        mBtnShuttleCancel = Utils.SetParam(mTestCase, "BTN_SHUTTLE_CANCEL"); //id=btn_ShuttlePicker.Cancel
        mClassDisBtn = Utils.SetParam(mTestCase, "CLS_DISBTN"); //class=disBtn
        mBtnReply = Utils.SetParam(mTestCase, "BTN_PANE_REPLY"); //id=btn_Pane.Reply
        mDlgSendMail = Utils.SetParam(mTestCase, "DLG_MAIL"); //id=SendMail 
        mBtnTempls = Utils.SetParam(mTestCase, "BTN_TEMPLS"); //id=btn_SendMail.Templates
        mTxtTempl = Utils.SetParam(mTestCase, "TXT_MAIL_TEMPL"); //id=content2Sample
        mBtnTemplCancel = Utils.SetParam(mTestCase, "BTN_TEMPLS_CANCEL"); //id=btn_Templates.Cancel
        mBtnSendmailClose = Utils.SetParam(mTestCase, "BTN_MAIL_CLOSE"); //id=btn_SendMail.Close
        mBtnFavPub = Utils.SetParam(mTestCase, "BTN_FAV"); //id=btn_Favorite.published.
        mCxtEdit = Utils.SetParam(mTestCase, "CXT_ACTION_EDIT"); //id=cxt_ActionEdit
        mOptEvType = Utils.SetParam(mTestCase, "OPT_EV_TYPES"); //id=choTypes1
        mChkDIUAll = Utils.SetParam(mTestCase, "CHK_DIU_ALL"); //id=lstDiU.All
        mChkDARAll = Utils.SetParam(mTestCase, "CHK_DAR_ALL"); //id=lstDaR.All
        mChkNBA = Utils.SetParam(mTestCase, "CHK_DIM_NBA"); //id=lstDiM.3
        mChkWeb = Utils.SetParam(mTestCase, "CHK_DIM_WEB"); //id=lstDiM.4
        mTabAudit = Utils.SetParam(mTestCase, "TAB_CUST_AUDIT"); //id=tab_3
        mChkUnrev = Utils.SetParam(mTestCase, "CHK_UNREV"); //id=chkUE
        mSelStatus = Utils.SetParam(mTestCase, "SEL_AS1"); //id=lstAS1
        
        // text values for audit value changes
        txtMustBeComplete = Utils.SetParam(mTestCase, "txtMustBeComplete");
        txtReviewed = Utils.SetParam(mTestCase, "txtReviewed");
        txtGenEsc = Utils.SetParam(mTestCase, "txtGenEsc");
        txtNonIssue = Utils.SetParam(mTestCase, "txtNonIssue");
        
        // ids of the fields on the New Issue dialog
        // the order of the elements in this array must match the order of the bools returned from AuditConfigurationManager.GetAuditCheckboxStates()
        mAuditFields[0] = mTxtName;
        mAuditFields[1] = mStatus;
        mAuditFields[2] = mAction;
        mAuditFields[3] = mResn;
        mAuditFields[4] = mPpts;
        mAuditFields[5] = mAssocInc;
        mAuditFields[6] = mSelComment; 
    }
}
