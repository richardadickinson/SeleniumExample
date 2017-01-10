/*
 * The SmtpServer must be set in the iConsole registry for this test case.
 * The Pop3 server must be running on the mail exchange
 */

package testcases;

/**
 * @author dicri02
 */

import common.*;
import javax.mail.*;
import org.openqa.selenium.By;

public class SendAuditMail extends WgnTestStep
{
    private String mUserSearch;
    private String mUserPw;
    private int mNumberEvents;
    private int sentSuccess;
    private int nextEvent;
    private String mFailMailSend;
    private String mSentMailText; 
    private String mSentMailError;

    public SendAuditMail() { }

    @Override
    public void Run() throws Exception
    {
        SetParameters();
        
        for (int i=0; i < mNumberEvents; i++)
        {            
            mTestRunner.NewLogFileGroup("Testing Audit Mails for Test Event:" + i, LogFile.iINFO);
            String sId = "Row" + Integer.toString(nextEvent);
            Utils.DoClick(mTestCase, By.id(sId), null, 500);
            while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false)) { Thread.sleep(500); }
            while (!Utils.isElementPresent(mTestCase, By.id("btn_Pane.Reply"), false)) { Thread.sleep(500); }
 
            // not all events capture a sender so we need to find one that did before trying to send the audit mail
            Boolean bFailed = LocateValidEvent();

            if (!bFailed)
            {
                // send the first test mail
                String recip = Utils.GetText(mTestCase, By.cssSelector("td.inputFill>ul>li>div>span"), false);
                mTestRunner.WriteLog("User '" + recip + "' added", LogFile.iINFO);
                Utils.DoType(mTestCase, By.id("sendMail.input.subject"), "findme");
                Utils.DoClick(mTestCase, By.id("btn_SendMail.Send"), By.id("btn_Message.Close"), 20000); // send mail

                if (!Utils.isTextPresent(mTestCase, mFailMailSend, false))
                {
                    Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 250);
                    int k = 0; while (Utils.isElementPresent(mTestCase, By.id("SendMail"), false) && k < 50) { Thread.sleep(500); k++; }
                    // Open Event History dialog and retrieve Action text
                    Utils.DoClick(mTestCase, By.id("btn_Pane.History"), null, 1000);
                    k = 0; while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && k < 50) { Thread.sleep(500); k++; }
                    
                    if (Utils.isElementPresent(mTestCase, By.id("AuditHistory"), false))
                    {
                        String auditAction = Utils.GetText(mTestCase, By.xpath("//div[@id='AuditHistory']/div[2]/div/div/div/table[contains(@class,'mirTable')]/tbody/tr/td[3]"),false);
                        Utils.DoClick(mTestCase, By.id("btn_AuditHistory.Close"), null, 250);
                        if ( auditAction.contains(mSentMailText) && !auditAction.contains(mSentMailError) )
                        {
                            sentSuccess++;
                            mTestRunner.WriteLog("PASS: audit action text contains the expected text '" + mSentMailText + "' and no error text", LogFile.iTEST);
                        } else {
                            mTestRunner.WriteLog(String.format("FAIL: audit action text shows a problem: %s", auditAction), LogFile.iERROR);
                        }
                    }
                    if (!mUserSearch.isEmpty())
                    {
                        ValidateAuditMail("findme", recip, false, "notset", false);
                        // change flags and send another mail - import=high, read-request
                        SendNewAuditMail(recip, "high");
                        // change flags and send another mail - import=low, no link
                        SendNewAuditMail(recip, "low");
                        // finally create an audit issue and launch the send mail dialog from there
                        SendMailFromIssue(recip);
                    }
                } else {
                    mTestRunner.WriteLog("ERROR: The Audit Mail failed to send - check your SMTP Server configuration", LogFile.iERROR);
                    mTestRunner.WriteLog("Reason given: " + Utils.GetText(mTestCase, By.xpath("//html/body/div[5]/div[2]/div[2]/div/div/div/table/tbody/tr/td[2]/span"), false), LogFile.iNONE);
                    Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 10);
                    Utils.DoClick(mTestCase, By.id("btn_SendMail.Close"), null, 10);
                    Utils.DoClick(mTestCase, By.id("btn_Message.OK"), null, 10);
                    mTestRunner.GetLogFile().ResetIndent();
                    break;
                }
            } else {
                mTestRunner.WriteLog("FAIL: No event could be found with a valid sender", LogFile.iERROR);
            }
            mTestRunner.CloseLogFileGroup("Completed Audit Mail tests for Test Event:" + i, LogFile.iINFO);
        }
        if (!mUserSearch.isEmpty()) mNumberEvents = 5*mNumberEvents;        
        mTestRunner.WriteLog(sentSuccess + " out of " + mNumberEvents + " mails sent successfully", LogFile.iINFO);
        Utils.CloseWindow(mTestCase, mTestCase.GetParentHandle());
    }
    
    private Boolean LocateValidEvent() throws Exception
    {
        Boolean bFailed = true;
        int numSearchResults = Integer.parseInt(mTestCase.GetAttribute("numSearchResults")) + 1;
        for (int j = 0; j < numSearchResults; j++)
        {
            Utils.DoClick(mTestCase, By.id("btn_Pane.Reply"), null, 500);
            int count = 0; while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && count < 20) { count++; Thread.sleep(500); }
            count = 0; while (!Utils.isElementPresent(mTestCase, By.id("SendMail"), false) && count < 20) { count++; Thread.sleep(500); }
            if (Utils.isElementPresent(mTestCase, By.id("SendMail"), true))
            {
                if (!Utils.isElementPresent(mTestCase, By.xpath("//td[@class='inputFill']/ul/li[2]"), false)) // the To field doesn't have to be automatically populated
                {
                    Utils.DoClick(mTestCase, By.id("btn_SendMail.btnAddSender"), null, 100);
                    Thread.sleep(500);
                    if (Utils.isElementPresent(mTestCase, By.id("Message"), false))
                    {
                        mTestRunner.WriteLog("No valid sender, go to next event: " + Utils.GetText(mTestCase, By.id("Message_desc"), false), LogFile.iINFO);
                        Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 100);
                        Utils.DoClick(mTestCase, By.id("btn_SendMail.Close"), null, 100);
                        while (Utils.isElementPresent(mTestCase, By.id("SendMail"), false)) {}

                        if (j != numSearchResults-1)
                        {
                            Utils.DoClick(mTestCase, By.id("btn_Pane.NextEvent"), null, 100);
                        }
                    } else {
                        // the Add Sender button managed to add an address, break out and carry on
                        nextEvent = nextEvent + j + 1;
                        bFailed = false;
                        break;
                    }
                } else {
                    // the To field was auto-populated so break out and carry on                        
                    nextEvent = nextEvent + j + 1;
                    bFailed = false;
                    break;
                }
            }
        }
        return bFailed;
    }
    
    private void SendNewAuditMail(final String recip, final String sImport) throws Exception
    {
        Utils.DoClick(mTestCase, By.id("btn_Pane.Reply"), By.id("SendMail"), 20000);
        if (!Utils.isElementPresent(mTestCase, By.xpath("//td[@class='inputFill']/ul/li[2]"), false)) // the To field doesn't have to be automatically populated
        {
            Utils.DoClick(mTestCase, By.id("btn_SendMail.btnAddSender"), null, 500);
        }
        Utils.DoType(mTestCase, By.id("sendMail.input.subject"), "import="+sImport);
        Boolean bReceipt = true;
        Boolean bLink = false;
        if (sImport.contentEquals("high"))
        {
            Utils.DoClick(mTestCase, By.id("btn_SendMail.btnReadRequest"), null, 10);     // set Request Read Receipt
            Utils.DoClick(mTestCase, By.id("btn_SendMail.btnHighImportance"), null, 10);  // set Importance High
        } else {
            Utils.DoClick(mTestCase, By.id("btn_SendMail.btnAttachLink"), null, 10);     // add link
            Utils.DoClick(mTestCase, By.id("btn_SendMail.btnLowImportance"), null, 10);   // set Importance Low
            bReceipt = false; bLink= true;
        }
        Utils.DoClick(mTestCase, By.id("btn_SendMail.Send"), By.id("btn_Message.Close"), 20000);  // send mail
        if (!Utils.isTextPresent(mTestCase, mFailMailSend, false))
        {
            ValidateAuditMail("import="+sImport, recip, bReceipt, sImport, bLink);
        }                        
        Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 500); 
        int k = 0; while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && k < 10) { Thread.sleep(500); k++; }        
    }    
    
    private void SendMailFromIssue(String recip) throws Exception
    {
            Utils.CheckAuditPrefs(mTestCase, true);
            if (Utils.isElementPresent(mTestCase, By.id("btn_Pane.AuditButton01"), false))
            {
                Utils.DoClick(mTestCase, By.id("btn_Pane.AuditButton01"), null, 500); 
                int i = 0; while (!Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && i < 30) { Thread.sleep(500); i++; }
                i = 0; while (!Utils.isElementPresent(mTestCase, By.id("lnk_Audit.ViewIssue0"), false) && i < 10) { Thread.sleep(500); i++; }
                Utils.DoClick(mTestCase, By.id("lnk_Audit.ViewIssue0"), By.id("btn_ViewIssue.SendMail"), 20000);
                Utils.DoClick(mTestCase, By.id("btn_ViewIssue.SendMail"), By.id("btn_SendMail.Close"), 20000);
                if (!Utils.isElementPresent(mTestCase, By.xpath("//td[@class='inputFill']/ul/li[2]"), false)) // the To field doesn't have to be automatically populated
                {
                    Utils.DoClick(mTestCase, By.id("btn_SendMail.btnAddSender"), null, 100);
                    Thread.sleep(500);
                }
                mTestRunner.WriteLog("Testing the View Issue Send Mail button:", LogFile.iINFO);
                Utils.DoType(mTestCase, By.id("sendMail.input.subject"), "view issue");
                Utils.DoClick(mTestCase, By.id("btn_SendMail.Send"), By.id("btn_Message.Close"), 20000);  // send mail
                if (!Utils.isTextPresent(mTestCase, mFailMailSend, false))
                {                                
                    ValidateAuditMail("view issue", recip, false, "default", true);
                }                            
                Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 100);
            } else {
                mTestRunner.WriteLog("ERROR: Couldn't find Audit button 01 - check your Audit config", LogFile.iERROR);
            }
            Utils.CheckAuditPrefs(mTestCase, false);
    }

    private void ValidateAuditMail(String subject, String recip, Boolean receipt, String priority, Boolean link) throws Exception
    {
        // the mail was sent, now check it was received in the recipients mail box
        mTestRunner.NewLogFileGroup("Validate Audit Mail with subject: " + subject, LogFile.iINFO);
        try
        {
            Boolean msg = false;
            // give the message chance to arrive at the inbox
            int i = 0;
            while (i < 10) 
            {
                msg = ReadMail.getMessage(mTestCase, mTestRunner.GetConfigurationFile().GetPop3Host(), mUserSearch, mUserPw, subject);
                if (msg) break;
                Thread.sleep(1000);
                i++;
            }
            if (msg)
            {
                //mTestRunner.WriteLogFileEntry(ReadMail.getFullMessageContent(subject), LogFile.iINFO);  //debug
                // check mail was returned from user's inbox
                if (ReadMail.getRecips().toLowerCase().contains(recip.toLowerCase()))
                {
                    mTestRunner.WriteLog("PASS: Audit Mail located in Inbox for: " + ReadMail.getRecips(), LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: Audit Mail recipient ("  + ReadMail.getRecips() + ") doesn't match expected value (" + recip + ")", LogFile.iERROR);
                }
                // look for the read receipt when set
                if (receipt)
                {
                    if (!ReadMail.getHeader("Return-Receipt-To").isEmpty())
                    {
                        mTestRunner.WriteLog("PASS: Request Receipt successfully set (to " + ReadMail.getHeader("Return-Receipt-To") + ")", LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: Request Receipt not set in mail headers", LogFile.iERROR);
                    }
                } else {
                    if (ReadMail.getHeader("Return-Receipt-To").isEmpty())
                    {
                        mTestRunner.WriteLog("PASS: Request Receipt not set", LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: Request Receipt was set in mail headers (" + ReadMail.getHeader("Return-Receipt-To") + ")", LogFile.iERROR);
                    }
                }
                // priority sets 3 mail headers
                switch (priority) {
                    case "high":
                        if ( (ReadMail.getHeader("X-Priority").contentEquals("1")) && (ReadMail.getHeader("Importance").equalsIgnoreCase("high")) )
                        {
                            mTestRunner.WriteLog("PASS: Importance set to 'High' (mail headers: X-Priority=1, Importance=high)", LogFile.iTEST);
                        } else {
                            mTestRunner.WriteLog("FAIL: High Importance not set correctly in mail headers (X-Priority: " + ReadMail.getHeader("X-Priority")
                                     + ", Importance: " + ReadMail.getHeader("Importance") + ")", LogFile.iERROR);
                        }
                        break;
                    case "low":
                        if ( (ReadMail.getHeader("X-Priority").contentEquals("5")) && (ReadMail.getHeader("Importance").equalsIgnoreCase("low")) )
                        {
                            mTestRunner.WriteLog("PASS: Importance set to 'Low' (mail headers: X-Priority=5, Importance=low)", LogFile.iTEST);
                        } else {
                            mTestRunner.WriteLog("FAIL: Low Importance not set correctly in mail headers (X-Priority: " + ReadMail.getHeader("X-Priority")
                                     + ", Importance: " + ReadMail.getHeader("Importance") + ")", LogFile.iERROR);
                        }
                        break;
                    default:
                        if ( (ReadMail.getHeader("X-Priority").contentEquals("")) && (ReadMail.getHeader("Importance").contentEquals("")) )
                        {
                            mTestRunner.WriteLog("PASS: no priority mail headers set", LogFile.iTEST);
                        } else {
                            mTestRunner.WriteLog("FAIL: No priority should be set in mail headers (X-Priority: " + ReadMail.getHeader("X-Priority")
                                     + ", Importance: " + ReadMail.getHeader("Importance") + ")", LogFile.iERROR);
                        }
                        break;
                }
                // was event link attached?
                if (link)
                {
                    if (ReadMail.getFullMessageContent(subject).contains("external=%22evl-link%22"))
                    {
                        mTestRunner.WriteLog("PASS: A link to the event content was attached", LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: A link to the event content was not attached", LogFile.iERROR);
                    }
                } else {
                    if (ReadMail.getFullMessageContent(subject).contains("external=%22evl-link%22"))
                    {
                        mTestRunner.WriteLog("FAIL: A link to the event content was attached", LogFile.iERROR);
                    } else {
                        mTestRunner.WriteLog("PASS: A link to the event content was not attached", LogFile.iTEST);
                    }
                }
                sentSuccess++;
                ReadMail.closeMailbox();
            } else {
                mTestRunner.WriteLog("ERROR: Audit Mail not found in Inbox for " + recip, LogFile.iERROR);
            }
        } catch (MessagingException me) {
            mTestRunner.WriteLog("Failed to connect to user's INBOX: " + me.toString(), LogFile.iERROR);
        }
        mTestRunner.CloseLogFileGroup("Completed validation of Audit Mail, subject: " + subject, LogFile.iINFO);
    }
    
    private void SetParameters()
    {
        nextEvent = 0;
        sentSuccess = 0;
        
        mNumberEvents = Integer.parseInt(Utils.SetParam(mTestCase, "quantity"));
        if (mParameters.containsKey("matchuser"))
        {
            TestUser match = (TestUser)mTestCase.GetTestUsers().get(mParameters.get("matchuser"));
            mUserSearch = match.GetUserName();
            mUserPw = match.GetUserValue();
            mTestRunner.WriteLog("Match User:" + mUserSearch + ", pw: " + mUserPw, LogFile.iINFO);
        } else {
            mUserSearch = "";
        }
        mFailMailSend = Utils.SetParam(mTestCase, "failMailSend");
        mSentMailText = Utils.SetParam(mTestCase, "sentmailtext"); 
        mSentMailError = Utils.SetParam(mTestCase, "sentmailerror");
    }

}
