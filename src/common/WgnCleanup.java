//Copyright © 2011 CA. All rights reserved.

package common;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import org.openqa.selenium.*;
import java.util.*;
import testcases.*;


public class WgnCleanup {

    Boolean bCleaned = true;
    private TestRunner mTestRunner;
    protected WebDriver mDriver;
    protected Map<String, String> mParameters;
    private String mParentHandle;
    private String StdSHash, DrvSHash, ISRHash, DIHash, POSHash, RActHash; // searches and reports that may have been unpublished
    private String BBGExpHash, BBGStdSHash, AuditStatusHash, DAlertHash; // BBG searches
    private String BTN_ADMIN_INSTALL, TAB_ADMIN_SEARCHES; // common tab controls
    private String CHK_SEARCH_PUBL, TTL_SEARCH_PUBL, CHK_SEARCH_UNPUBL, TTL_SEARCH_UNPUBL, BTN_ACTION_PUBL, TOG_FOLDER_PUBL; // common admin controls
    private String DLG_PROGRESS, DLG_MSG, BTN_MSG_OK, BTN_MSG_NO, BTN_MSG_YES, BTN_MSG_CANCEL, LNK_LOGOFF, DLG_PICKER, BTN_PICKER_CANCEL,
            DLG_CUSTOMIZE, BTN_CUST_CLOSE, DLG_EDITRES, BTN_EDITRES_CANCEL, BTN_EDITRES_RESET, BTN_EDITRES_OK, BTN_EDIT_SETTINGS; // common dialog controls
    private String LNK_PREFS, DLG_PREFS, BTN_PREFS_CANCEL, BTN_PREFS_OK, BTN_PREFS_RESET; // common prefs controls
    private String BTN_ADMIN_BULK, CXT_BULK_UNINST, CXT_BULK_PUBL, CXT_ACTION_UNINST; // common context menu controls

    public WgnCleanup() { }

    public Boolean Cleanup(WgnTestCase wtc) throws Exception
    {
        mTestRunner = wtc.GetTestRunner();
        mDriver = wtc.GetWebDriver();
        mParameters = new HashMap<>();
        mParentHandle = wtc.GetParentHandle();
        Run(wtc);
        return bCleaned;
    }

    private void Run(WgnTestCase wtc) throws Exception
    {
        mParameters = wtc.GetParameters();
        SetParameters(wtc);
        try
        {
            mTestRunner.NewLogFileGroup("Starting cleanup...", LogFile.iINFO);

            closeAnyDialogs(wtc);            
            closeOrphanedWindows();
            
            // Need to logoff then login (to clear any js errors etc) and then do cleanup
            if (Utils.isElementPresent(wtc, By.id(LNK_LOGOFF), false) )
            {
                Utils.Logoff(wtc);
            }            
            Utils.Login(wtc, "admin");
            cleanUserPasswords();
            resetPrefs(wtc);  // can't do Global policy reset until the Group policy mgmt changes are complete
            cleanSearches(wtc);

            mTestRunner.CloseLogFileGroup("Cleanup finished", LogFile.iINFO);

        } catch (Exception ex) {
            bCleaned = false;
            throw ex;
        }
        
    }
    
    private void closeAnyDialogs(WgnTestCase wtc) throws Exception
    {
        // check for erroneously open dialogs
        if (Utils.isElementPresent(wtc,By.id(DLG_MSG), false))
        {
            if (Utils.isElementPresent(wtc,By.id(BTN_MSG_NO), false))
            {
                mDriver.findElement(By.id(BTN_MSG_NO)).click();
            } 
            else if (Utils.isElementPresent(wtc,By.id(BTN_MSG_CANCEL), false))
            {
                mDriver.findElement(By.id(BTN_MSG_CANCEL)).click();
            }
            mTestRunner.WriteLog("A Message dialog was open", LogFile.iINFO);
        }
        if (Utils.isElementPresent(wtc,By.id(DLG_PREFS), false))
        {
            mDriver.findElement(By.id(BTN_PREFS_CANCEL)).click();
            mTestRunner.WriteLog("Prefs dialog was open", LogFile.iINFO);
        }
        if (Utils.isElementPresent(wtc,By.id(DLG_PICKER), false))
        {
            mDriver.findElement(By.id(BTN_PICKER_CANCEL)).click();
            mTestRunner.WriteLog("A Picker dialog was open", LogFile.iINFO);
        }
        if (Utils.isElementPresent(wtc,By.id(DLG_CUSTOMIZE), false))
        {
            mDriver.findElement(By.id(BTN_CUST_CLOSE)).click();
            mTestRunner.WriteLog("A Picker dialog was open", LogFile.iINFO);
        }
        if (Utils.isElementPresent(wtc,By.id(DLG_EDITRES), false))
        {
            mDriver.findElement(By.id(BTN_EDITRES_CANCEL)).click();
            mTestRunner.WriteLog("A Picker dialog was open", LogFile.iINFO);
        }
    }

    private void closeOrphanedWindows() throws Exception
    {        
        // close any open search results or other windows that have been left hanging
        // if this is necessary it's a Wigan bug - the iConsole should tidy up child windows itself on logout.
        if (mParentHandle != null && !mParentHandle.isEmpty())
        {
            Set<String> handles = mDriver.getWindowHandles();
            handles.remove(mParentHandle);
            if (handles.isEmpty())
            {
                mTestRunner.WriteLog("There are no orphaned windows to close.", LogFile.iINFO);
            } else {
                while (handles.iterator().hasNext())
                {
                    try 
                    {
                        mDriver.switchTo().window(handles.iterator().next()); 
                        mDriver.close(); 
                    } catch (Exception ex) {
                        mTestRunner.WriteLog("Failed to close window: " + ex.getMessage(), LogFile.iERROR);
                        break;
                    }
                }
            }
            mDriver.switchTo().window(mParentHandle);
        }
    }

    private void cleanUserPasswords()
    {        
    }

    //@SuppressWarnings("SleepWhileInLoop")
    private void resetPrefs(WgnTestCase mTestCase)
    {        
        try 
        {
            // local prefs
            Utils.DoClick(mTestCase, By.id(LNK_PREFS), By.id(DLG_PREFS), 10000, false);
            Utils.DoClick(mTestCase, By.id(BTN_PREFS_RESET), By.id(BTN_MSG_YES), 5000, false);
            Utils.DoClick(mTestCase, By.id(BTN_MSG_YES), null, 100, false);
            int i = 0; while (Utils.isElementPresent(mTestCase, By.id(DLG_MSG), false) && i < 20) { Thread.sleep(500); i++; }
            i = 0; while (Utils.isElementPresent(mTestCase, By.id(DLG_PROGRESS), false) && i < 20) { Thread.sleep(500); i++; }
            Utils.DoClick(mTestCase, By.id(BTN_PREFS_OK), null, 100);
            i = 0; while (Utils.isElementPresent(mTestCase, By.id(DLG_PREFS), false) && i < 20) { Thread.sleep(500); i++; }
            mTestRunner.WriteLog("Local Preferences successfully reset", LogFile.iINFO);
        } 
        catch (Exception ex) 
        {
            mTestRunner.WriteLog("Local Preferences reset failed: " + ex.getMessage(), LogFile.iERROR);
            bCleaned = false;
        }
        // Global Prefs reset
        try
        {
            Utils.DoClick(mTestCase, By.id("tab_manageDD"), null, 500);
            Utils.DoClick(mTestCase, By.id("cxt_Role Assignments"), By.id(BTN_EDIT_SETTINGS), 10000);
            Utils.DoClick(mTestCase, By.id(BTN_EDIT_SETTINGS), By.id(DLG_EDITRES), 5000);
            Utils.DoClick(mTestCase, By.id(BTN_EDITRES_RESET), null, 500);
            Utils.DoClick(mTestCase, By.id(BTN_EDITRES_OK), null, 1000);  
            mTestRunner.WriteLog("Global Preferences successfully reset", LogFile.iINFO);
        }
        catch (Exception ex) 
        {
            mTestRunner.WriteLog("Global Preferences reset failed: " + ex.getMessage(), LogFile.iERROR);
            bCleaned = false;            
        }
    }

    //@SuppressWarnings("SleepWhileInLoop")
    private void cleanSearches(WgnTestCase mTestCase)
    {
        Boolean bSearchesCleaned = false;
        try {
           // check for the derived search & delete if found
           Utils.DoClick(mTestCase, By.id(TAB_ADMIN_SEARCHES), By.id(BTN_ADMIN_INSTALL), 15000, false);
           
           if (Utils.isElementPresent(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), false) )
           {
                Utils.DoClick(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), null, 500);
           }
           if (Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), false) )
           {
               Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), null, 100);
               Utils.DoClick(mTestCase, By.id(CXT_ACTION_UNINST), By.id(DLG_MSG), 5000, false);
               Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100, false);
               int count = 0; while (Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), false) && count < 10) { count++; Thread.sleep(1000); }
               if (!Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), false) )
               {
                   mTestRunner.WriteLog("Derived search found & deleted", LogFile.iINFO);
                   bSearchesCleaned = true;
               } else {
                   mTestRunner.WriteLog("ERROR: Derived search NOT deleted!", LogFile.iERROR);
               }
           } else {
               mTestRunner.WriteLog("The derived search was not present", LogFile.iDEBUG);
           }

           // check for the BBG searches (from Admin_tab test case)
           Boolean uninst = false;
           if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_PUBL+BBGExpHash), false) )
           {
               Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), null, 100);
               uninst = true;
           }
           if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_PUBL+BBGStdSHash), false) )
           {
               Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+BBGStdSHash), null, 100); 
               uninst = true;
           }
           if (uninst)
           {
                // Uninstall the searches
                Utils.DoClick(mTestCase, By.id(BTN_ADMIN_BULK), By.id(CXT_BULK_UNINST), 10000, false);
                Utils.DoClick(mTestCase, By.id(CXT_BULK_UNINST), By.id(DLG_MSG), 5000, false);
                Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100, false);
                int count = 0; while (Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), false) && count < 10) { count++; Thread.sleep(1000); }
                mTestRunner.WriteLog("The BBG Searches were deleted", LogFile.iINFO);
                bSearchesCleaned = true;
           } else {
                mTestRunner.WriteLog("The BBG Searches were not present", LogFile.iDEBUG);
           }

           // check for the BBG reports (from Admin_tab test case)
           uninst = false;
           Boolean bSelected = Utils.SelectAdminCategory(mTestCase, "Reports", By.id(CHK_SEARCH_PUBL+RActHash));

           if (bSelected)
           {               
               if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_UNPUBL+AuditStatusHash), false) )
               {
                   Utils.DoClick(mTestCase, By.id(CHK_SEARCH_UNPUBL+AuditStatusHash), null, 100);
                   uninst = true;
               }
               if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_PUBL+AuditStatusHash), false) )
               {
                   Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+AuditStatusHash), null, 100);
                   uninst = true;
               }
               if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_UNPUBL+DAlertHash), false) )
               {
                    Utils.DoClick(mTestCase, By.id(CHK_SEARCH_UNPUBL+DAlertHash), null, 100);
                    uninst = true;
               }
               if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_PUBL+DAlertHash), false) )
               {
                    Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+DAlertHash), null, 100);
                    uninst = true;
               }
               if (uninst) {
                   // uninstall the reports
                   Utils.DoClick(mTestCase, By.id(BTN_ADMIN_BULK), By.id(CXT_BULK_UNINST), 10000, false);
                   Utils.DoClick(mTestCase, By.id(CXT_BULK_UNINST), By.id(DLG_MSG), 5000, false);
                   Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100, false);
                   mTestRunner.WriteLog("The BBG Reports were deleted", LogFile.iINFO);
                   bSearchesCleaned = true;
               } else {
                   mTestRunner.WriteLog("The BBG Reports were not present", LogFile.iDEBUG);
               }

               // Check for unpublished reports (from Admin_tab test case)
               String[][] unpub = { { "Proof of Supervision", POSHash },
                                    { "Detailed Issue", DIHash},
                                    { "Issues by Status or Resolution", ISRHash},
                                  };
               int unpubCount = unpub.length;
               uninst = false;
               for (int i=0; i < unpubCount; i++) {
                   // select the Reports if unpublished
                   if (Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_UNPUBL+unpub[i][1]), false) ) {
                       Utils.DoClick(mTestCase, By.id(CHK_SEARCH_UNPUBL+unpub[i][1]), null, 50);
                       uninst = true;
                       mTestRunner.WriteLog("The " + unpub[i][0] + " Report was re-published", LogFile.iINFO);
                   }
               }
               if (uninst)
               {
                   // publish the reports again
                   Utils.DoClick(mTestCase, By.id(BTN_ADMIN_BULK), null, 100);
                   Utils.DoClick(mTestCase, By.id(CXT_BULK_PUBL), By.id(DLG_MSG), 5000);
                   Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100, false);
                   bSearchesCleaned = true;
               } else {
                   mTestRunner.WriteLog("No OOTB Reports were unpublished", LogFile.iDEBUG);
               }
           }
            if (bSearchesCleaned)
            {
                mTestRunner.WriteLog("The Searches and Reports were successfully cleaned", LogFile.iINFO);
            } else {
                mTestRunner.WriteLog("The Searches and Reports did not require cleaning", LogFile.iINFO);
            }           
        } 
        catch (Exception ex) 
        {
            mTestRunner.WriteLog(ex.getMessage(), LogFile.iERROR);
            bCleaned = false;
        }
    }
    
    private void SetParameters(WgnTestCase wtc)
    {
        StdSHash = Utils.SetParam(wtc, "StdSrchHash");
        DrvSHash = Utils.SetParam(wtc, "myDerivedSrchHash");
        ISRHash = Utils.SetParam(wtc, "ISRHash");
        DIHash = Utils.SetParam(wtc, "DIHash");
        POSHash = Utils.SetParam(wtc, "POSHash");
        RActHash = Utils.SetParam(wtc, "RevActHash");
        BBGExpHash = Utils.SetParam(wtc, "BBGExpHash");
        BBGStdSHash = Utils.SetParam(wtc, "BBGStdSrchHash");
        AuditStatusHash = Utils.SetParam(wtc, "AuditStatusHash");
        DAlertHash = Utils.SetParam(wtc, "DetailedAlertHash");
        CHK_SEARCH_PUBL = Utils.SetParam(wtc, "CHK_SEARCH_PUBL");
        TTL_SEARCH_PUBL = Utils.SetParam(wtc, "TTL_SEARCH_PUBL");
        BTN_ACTION_PUBL = Utils.SetParam(wtc, "BTN_ACTION_PUBL");
        TOG_FOLDER_PUBL = Utils.SetParam(wtc, "TOG_FOLDER_PUBL");
        DLG_PROGRESS = Utils.SetParam(wtc, "DLG_PROGRESS");
        DLG_MSG = Utils.SetParam(wtc, "DLG_MESSAGE");
        BTN_MSG_NO = Utils.SetParam(wtc, "BTN_MESSAGE_NO"); //"btn_Message.No";
        BTN_MSG_CANCEL = Utils.SetParam(wtc, "BTN_MESSAGE_CANCEL"); //"btn_Message.Cancel";
        BTN_MSG_YES = Utils.SetParam(wtc, "BTN_MESSAGE_YES"); //"btn_Message.Yes";
        BTN_MSG_OK = Utils.SetParam(wtc, "BTN_MESSAGE_OK");
        LNK_LOGOFF = Utils.SetParam(wtc, "LNK_LOGOFF");
        LNK_PREFS = Utils.SetParam(wtc, "LNK_PREFS");
        DLG_PREFS = Utils.SetParam(wtc, "DLG_PREFS");
        BTN_PREFS_CANCEL = Utils.SetParam(wtc, "BTN_PREFS_CANCEL");
        BTN_PREFS_OK = Utils.SetParam(wtc, "BTN_PREFS_OK");
        BTN_PREFS_RESET = Utils.SetParam(wtc, "BTN_PREFS_RESET");
        CHK_SEARCH_UNPUBL = Utils.SetParam(wtc, "CHK_SEARCH_UNPUBL"); //"treeChk_Search.unpublished.";
        TTL_SEARCH_UNPUBL = Utils.SetParam(wtc, "TTL_SEARCH_UNPUBL"); //"treeTitle_Search.unpublished.";
        CXT_ACTION_UNINST = Utils.SetParam(wtc, "CXT_ACTION_UNINST"); //"cxt_ActionUninstall";
        BTN_ADMIN_BULK = Utils.SetParam(wtc, "BTN_ADMIN_BULK"); //"btn_AdminSearches.AdminSearchesBulkActions";
        CXT_BULK_UNINST = Utils.SetParam(wtc, "CXT_BULK_UNINST"); //"cxt_AdminSearches.AdminSearchesBulkActions.ActionUninstall";
        CXT_BULK_PUBL = Utils.SetParam(wtc, "CXT_BULK_PUBL"); //"cxt_AdminSearches.AdminSearchesBulkActions.ActionPublish";
        BTN_ADMIN_INSTALL = Utils.SetParam(wtc, "BTN_ADMIN_INSTALL"); //"btn_AdminSearches.Install";
        DLG_PICKER = Utils.SetParam(wtc, "DLG_PICKER");
        BTN_PICKER_CANCEL = Utils.SetParam(wtc, "BTN_PICKER_CANCEL");
        DLG_CUSTOMIZE = Utils.SetParam(wtc, "DLG_CUSTOMIZE");
        BTN_CUST_CLOSE = Utils.SetParam(wtc, "BTN_CUST_CLOSE");
        DLG_EDITRES = Utils.SetParam(wtc, "DLG_EDITRES");
        BTN_EDITRES_CANCEL = Utils.SetParam(wtc, "BTN_EDITRES_CANCEL");
        TAB_ADMIN_SEARCHES = Utils.SetParam(wtc, "TAB_ADMIN_SEARCHES");
        BTN_EDITRES_RESET = Utils.SetParam(wtc, "BTN_EDITRES_RESET");
        BTN_EDITRES_OK = Utils.SetParam(wtc, "BTN_EDITRES_OK");
        BTN_EDIT_SETTINGS = Utils.SetParam(wtc, "BTN_EDIT_SETTINGS");
    }

}
