
package testcases;

/**
 *
 * @author dicri02
 */

import common.*;
import org.openqa.selenium.By;

public class Admin_tab extends WgnTestStep
{
    private Boolean runPublishTest = false;
    private Boolean bSelected;
    private String parentHandle;
    private String ISRHash, DIHash, POSHash, RActHash;
    private String StdSHash, IncDBHash, SmartTagHash, DIUHash, DrvSHash;
    private String BBGExpHash, BBGStdSHash, AuditStatusHash, DAlertHash;
    private String successPublish, successUnpublish, successPublish2, successUnpublish2, successInstall;
    //private String successInstall2;
    private String StdSrchName;
    private String findme;
    private String BTN_MSG_OK, DLG_CUSTOMIZE, CXT_ACTIONS;
    private String BTN_CUST_SAVE, BTN_CUST_HELP, DLG_SAVESEARCH, BTN_SAVESEARCH_OK, BTN_ACTION_PUBL, 
            TTL_SEARCH_PUBL, TOG_FOLDER_PUBL, CHK_SEARCH_PUBL, DLG_RESTRICT, BTN_RESTRICT_OK, CHK_RESOURCE_REPORTS, TAB_2;
    private String BTN_CUST_XCLOSE, TTL_RUN_PUBL, CHK_SEARCH_UNPUBL, BTN_ACTION_UNPUBL, CHK_FOLDER_PUBL, TAB_ADMIN_SEARCHES,
            DLG_MESSAGE, BTN_ADMIN_BULK, CXT_BULK_UNINST, CXT_BULK_PUBL, CXT_BULK_UNPUBL, TAB_ADMIN, TAB_ADMIN_RESOURCES,
            TAB_REVIEW, TAB_REVIEW_REPORTS, SDFILE, CHK_PUBL, BTN_INSTALL_INSTALL, DLG_INSTALL_DESC, BTN_INSTALL_SEARCH_CANCEL,
            BTN_ADMIN_INSTALL, TXT_SEARCH_STATUS, DLG_EDIT_RESOURCES, BTN_EDIT_RESOURCES_OK, BTN_EDIT_RESOURCES_CANCEL,
            CHK_RESOURCE_FOLDER, BTN_RESOURCE_EDIT, CXT_ACTION_RENAME, TXTNAME, TXTDESC, BTN_RENAME_OK,
            DLG_RENAME, BTN_ACTION_SYSTEM, CXT_ACTION_TEST, CXT_ACTION_EDIT, CXT_ACTION_PUBL, CXT_ACTION_UNPUBL, CXT_ACTION_UNINST,
            TH_SMARTTAG, BTN_REFRESH, CHK_SEARCH_SYSTEM, TTL_FOLDER_SYSTEM, BTN_DASH_RESET, TOG_FOLDER_SYSTEM, BTN_RESOURCE_SEARCHES;
    
    public Admin_tab() {}

    @Override
    public void Run() throws Exception
    {
        SetParameters();

        testUnpublishSrch();
        if (runPublishTest) 
        {
            testPublishSrch();
        } else {
            mTestRunner.WriteLog("INFO: Skipping publish test.", LogFile.iINFO);
        }

        testSearchSelector();
        testRename();
        testSearchInstall();

        Utils.Logoff(mTestCase);
    }

    private void testUnpublishSrch() throws Exception
    {
        mTestRunner.NewLogFileGroup("Start " + mName + " - Unpublish", LogFile.iINFO);

        // select and unpublish the ISR & DI Reports using the main Actions button        
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(BTN_ADMIN_INSTALL), 10000);
        findme = CHK_SEARCH_PUBL+ISRHash;
        bSelected = Utils.SelectAdminCategory(mTestCase, "Reports", By.id(findme));

        if (bSelected)
        {
            mTestRunner.WriteLog("Unpublishing the Issues by Status or Resolution & Detailed Issue Reports", LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+ISRHash), null, 100);
            Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+DIHash), null, 100);
            Utils.DoClick(mTestCase, By.id(BTN_ADMIN_BULK), null, 100);
            Utils.DoClick(mTestCase, By.id(CXT_BULK_UNPUBL), By.id(DLG_MESSAGE), 1000);
            Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
            int i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() <= 1 && i < 15) { Thread.sleep(1000); i++; }
            if (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), true).contains(successUnpublish2))
            {
                mTestRunner.WriteLog("PASS: 2 Issues reports were unpublished (via Actions menu)", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: 2 Issues reports were not unpublished", LogFile.iERROR);
            }
            i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() > 1 && i < 10) { Thread.sleep(1000); i++; }
            
            // select and unpublish the Proof of Supervision Report using the report-specific Actions button
            mTestRunner.WriteLog("Unpublishing the Proof of Supervision report", LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+POSHash), null, 10);
            i = 0; while (!Utils.isDisplayed(mTestCase, By.id(CXT_ACTION_UNPUBL)) && i < 20) { Thread.sleep(250); i++; }
            Utils.DoClick(mTestCase, By.id(CXT_ACTION_UNPUBL), By.id(DLG_MESSAGE), 3000);
            Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
            i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() <= 1 && i < 15) { Thread.sleep(1000); i++; }
            if (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), true).contains(successUnpublish))
            {
                mTestRunner.WriteLog("PASS: The Proof of Supervision report was unpublished (report-specific button)", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: The Proof of Supervision report was not unpublished", LogFile.iERROR);
            }            
            i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() > 1 && i < 10) { Thread.sleep(1000); i++; }
            
            // go to Review tab and check the Search list has refreshed ok
            findme = TTL_RUN_PUBL+RActHash;
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW), By.id(TTL_RUN_PUBL+StdSHash), 10000);
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW_REPORTS), By.id(findme), 10000);
            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+ISRHash), false) 
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+DIHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+POSHash), false) )
            {
                mTestRunner.WriteLog("FAIL: The Review list has not refreshed to hide the unpublished reports", LogFile.iERROR);
            } else {
                mTestRunner.WriteLog("PASS: The Review list has refreshed to hide the unpublished reports", LogFile.iTEST);
            }

            // Login as RLS user and look for the ISR, DI & POS Reports on the Report list
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "secondary");
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW_REPORTS), By.id(findme), 10000);
            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+ISRHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+DIHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+POSHash), false) )
            {
                    mTestRunner.WriteLog("FAIL: RLS user can still access the 'unpublished' Reports", LogFile.iERROR);
            } else {
                    mTestRunner.WriteLog("PASS: RLS user cannot access the unpublished Reports", LogFile.iTEST);
                    runPublishTest = true;
            }
        }
        Utils.Logoff(mTestCase);
        mTestRunner.CloseLogFileGroup("Completed " + mName + " - Unpublish", LogFile.iINFO);
    }

    private void testPublishSrch() throws Exception
    {
        mTestRunner.NewLogFileGroup("Start " + mName + " - Publish", LogFile.iINFO);

        // Login as Admin, go to Manage page and re-publish the 3 Reports
        Utils.Login(mTestCase, "primary");
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(BTN_ADMIN_INSTALL), 10000);
        findme = CHK_SEARCH_UNPUBL+ISRHash;
        bSelected = Utils.SelectAdminCategory(mTestCase, "Reports", By.id(findme));

        if (bSelected)
        {
            // select and publish the ISR & DI Report using global Actions button
            mTestRunner.WriteLog("Selecting and publishing the Issues by Status or Resolution & Detailed Issue Report", LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id(CHK_SEARCH_UNPUBL+ISRHash), null, 100);
            Utils.DoClick(mTestCase, By.id(CHK_SEARCH_UNPUBL+DIHash), null, 100);
            Utils.DoClick(mTestCase, By.id(BTN_ADMIN_BULK), null, 100);
            Utils.DoClick(mTestCase, By.id(CXT_BULK_PUBL), By.id(DLG_MESSAGE), 5000);
            Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
            int i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() <= 1 && i < 15) { Thread.sleep(1000); i++; }        
            if (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), true).contains(successPublish2))
            {
                mTestRunner.WriteLog("PASS: The 2 Issues reports were re-published (via Actions menu)", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: The 2 Issues reports were not re-published", LogFile.iERROR);
            }            
            while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() > 1) { Thread.sleep(1000); }
            
            // select and publish the POS Report using report-specific Actions button
            mTestRunner.WriteLog("Publishing the Proof of Supervision report", LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id(BTN_ACTION_UNPUBL+POSHash), null, 100);
            Utils.DoClick(mTestCase, By.id(CXT_ACTION_PUBL), By.id(DLG_MESSAGE), 5000);
            Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
            i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() <= 1 && i < 15) { Thread.sleep(1000); i++; }        
            if (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), true).contains(successPublish))
            {
                mTestRunner.WriteLog("PASS: The Proof of Supervision report was re-published (report-specific button)", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: The Proof of Supervision report was not re-published", LogFile.iERROR);
            }
            while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() > 1) { Thread.sleep(1000); }
            
            // add the reports back into the Unmanaged User Role on the Manage Roles subtab
            Utils.DoClick(mTestCase, By.id(TAB_ADMIN_RESOURCES), By.id(BTN_RESOURCE_SEARCHES), 5000);
            Utils.DoClick(mTestCase, By.id(BTN_RESOURCE_SEARCHES), By.id(DLG_EDIT_RESOURCES), 5000);
            Utils.DoClick(mTestCase, By.id(TAB_2), By.id(CHK_RESOURCE_REPORTS), 5000);
            Utils.DoClick(mTestCase, By.id(CHK_RESOURCE_REPORTS), null, 500);
            Utils.DoClick(mTestCase, By.id(BTN_EDIT_RESOURCES_OK), null, 1000);
            
            // go to the Review tab and check the Search list has refreshed ok
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW), By.id(TTL_RUN_PUBL+StdSHash), 10000);
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW_REPORTS), By.id(TTL_RUN_PUBL+RActHash), 10000);
            if (Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+ISRHash), false) 
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+DIHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+POSHash), false))
            {
                    mTestRunner.WriteLog("PASS: The Review list has refreshed to show the published reports", LogFile.iTEST);
            } else {
                    mTestRunner.WriteLog("FAIL: The Review list has not refreshed to show the published reports", LogFile.iERROR);
            }

            // Login as RLS user and look for the published Reports on the Search list
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "secondary");
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW_REPORTS), By.id(TTL_RUN_PUBL+ISRHash), 10000);
            if (Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+ISRHash), false) 
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+DIHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+POSHash), false) )
            {
                mTestRunner.WriteLog("PASS: RLS user can access all the Reports in the Review list", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: RLS user cannot access all the Reports from the Review list", LogFile.iERROR);
            }
        }
        Utils.Logoff(mTestCase);
        mTestRunner.CloseLogFileGroup("Completed " + mName + " - Publish", LogFile.iINFO);
    }

    // checks the Admin tree and Actions\Test & Edit Properties buttons work ok
    private void testSearchSelector() throws Exception
    {
        mTestRunner.NewLogFileGroup("Start " + mName + " - Search selector", LogFile.iINFO);
        // Login, go to Manage page
        Utils.Login(mTestCase, "primary");
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(BTN_ACTION_PUBL+StdSHash), 10000);
        // Select the "Searches" option
        if (Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+StdSHash), false))
        {
            mTestRunner.WriteLog("PASS: Searches list displayed successfully", LogFile.iTEST);
            // go to the Std Search Edit Properties page
            Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+StdSHash), null, 100);
            Utils.DoClick(mTestCase, By.id(CXT_ACTION_EDIT), By.id(DLG_CUSTOMIZE), 15000);
            if (Utils.isDisplayed(mTestCase, By.id(DLG_CUSTOMIZE)) && Utils.isDisplayed(mTestCase, By.id(BTN_CUST_HELP)) )
            {
                mTestRunner.WriteLog("PASS: Standard Search Properties dialog was displayed successfully", LogFile.iTEST);
                Utils.DoClick(mTestCase, By.id(BTN_CUST_XCLOSE), null, 100);
            } else {
                mTestRunner.WriteLog("FAIL: Standard Search Properties dialog was not displayed", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Searches list was not displayed", LogFile.iERROR);
        }

        // Select the "Reports" option
        findme = CHK_SEARCH_PUBL+ISRHash;
        bSelected = Utils.SelectAdminCategory(mTestCase, "Reports", By.id(findme));
        if (bSelected)
        {
            if (Utils.isElementPresent(mTestCase, By.id(TTL_SEARCH_PUBL+ISRHash), false))
            {
                mTestRunner.WriteLog("PASS: Reports list displayed successfully", LogFile.iTEST);
                // go to the ISR Edit Properties page
                Utils.DoClick(mTestCase, By.id(TTL_SEARCH_PUBL+ISRHash), By.id(DLG_CUSTOMIZE), 15000);
                if (Utils.isDisplayed(mTestCase, By.id(DLG_CUSTOMIZE)) && Utils.isDisplayed(mTestCase, By.id(BTN_CUST_HELP)) )
                {
                    mTestRunner.WriteLog("PASS: Issues by Status or Resolution report Properties page was displayed successfully", LogFile.iTEST);
                    Utils.DoClick(mTestCase, By.id(BTN_CUST_XCLOSE), null, 100);
                } else {
                    mTestRunner.WriteLog("FAIL: Issues by Status or Resolution report Properties page was not displayed", LogFile.iERROR);
                }
            } else {
                mTestRunner.WriteLog("FAIL: Reports list was not displayed", LogFile.iERROR);
            }
        }
        // Select the "Incident Dashboard" option
        findme = CHK_SEARCH_PUBL+IncDBHash;
        bSelected = Utils.SelectAdminCategory(mTestCase, "Dashboard", By.id(findme));
        if (bSelected)
        {
            if (Utils.isElementPresent(mTestCase, By.id(findme), false) )
            {
                mTestRunner.WriteLog("PASS: Dashboard list displayed successfully", LogFile.iTEST);
                // run the dashboard
                StopWatch timer = new StopWatch().start();
                Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+IncDBHash), null, 100);
                parentHandle = Utils.GetNewWindow(mTestCase, By.id(CXT_ACTION_TEST));
                int i = 0; while ( !Utils.isElementPresent(mTestCase, By.cssSelector("*[id*='dashboard']"), false) && i < 60) { Thread.sleep(500); i++; }
                timer.stop();

                if ( Utils.isElementPresent(mTestCase, By.cssSelector("*[id*='dashboard']"), false)
                        && Utils.isElementPresent(mTestCase, By.id(BTN_DASH_RESET), false) )
                {
                    mTestRunner.WriteLog("PASS: Test run - Dashboard was displayed successfully (in " + timer.getElapsedTime() + " secs)", LogFile.iTEST);                
                } else {
                    mTestRunner.WriteLog("FAIL: Test run - Dashboard was not displayed", LogFile.iERROR);
                }
                Utils.CloseWindow(mTestCase, parentHandle);
            } else {
                mTestRunner.WriteLog("FAIL: Dashboard list was not displayed", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Couldn't switch to the Admin Dashboards list", LogFile.iERROR);
        }
        // Select the System\Searches\Smart Tag Picker option
        bSelected = Utils.SelectAdminCategory(mTestCase, "System", By.id(TOG_FOLDER_SYSTEM));
        if (bSelected)
        {        
            findme = CHK_SEARCH_SYSTEM+SmartTagHash;
            Utils.DoClick(mTestCase, By.id(TTL_FOLDER_SYSTEM), null, 3000);
            if ( Utils.isDisplayed(mTestCase, By.id(findme)) )
            {
                mTestRunner.WriteLog("PASS: System search list displayed successfully", LogFile.iTEST);
                // run the Smart tag picker
                int i=0; while ( !Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_SYSTEM+SmartTagHash),false) && i < 10 ) { Thread.sleep(500); i++; }
                Utils.DoClick(mTestCase, By.id(BTN_ACTION_SYSTEM+SmartTagHash), null, 100);
                i=0; while ( !Utils.isDisplayed(mTestCase, By.id(CXT_ACTIONS)) && i < 10 ) { Thread.sleep(500); i++; }
                parentHandle = Utils.GetNewWindow(mTestCase, By.id(CXT_ACTION_TEST));            
                i=0; while ( !Utils.isElementPresent(mTestCase, By.id(TH_SMARTTAG), false) && i < 20 ) { Thread.sleep(500); i++; }

                if (Utils.isElementPresent(mTestCase, By.id(TH_SMARTTAG), false)
                        && Utils.isElementPresent(mTestCase, By.id(BTN_REFRESH), false) )
                {
                    mTestRunner.WriteLog("PASS: Test run - Smart tag picker ran successfully", LogFile.iTEST);
                    Utils.CloseWindow(mTestCase, parentHandle);
                } else {
                    mTestRunner.WriteLog("FAIL: Test run - smart tag picker failed to run", LogFile.iERROR);
                }

            } else {
                mTestRunner.WriteLog("FAIL: System search list was not displayed", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: Couldn't switch to the Admin System list", LogFile.iERROR);
        }
        mTestRunner.CloseLogFileGroup("Completed " + mName + " - Search selector", LogFile.iINFO);
    }

    private void testRename() throws Exception
    {
        mTestRunner.NewLogFileGroup("Start " + mName + " - Rename", LogFile.iINFO);
        // go to the Admin page Searches tab
        findme = BTN_ACTION_PUBL+DIUHash;
        bSelected = Utils.SelectAdminCategory(mTestCase, "Searches", By.id(findme));
        if (bSelected)
        {
            String rename = "My Renamed Search";
            String renameHash = "0451fa528b8f67565665a7b386551bae047e0d1f";
            // check that base searches cannot be renamed
            Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+DIUHash), null, 100);
            Boolean bEnabled = false;
            for (int wait = 0; wait < 10; wait++)
            {
                if (Utils.isElementPresent(mTestCase, By.cssSelector("#cxt_ActionRename.disBtn"), false))
                {
                    bEnabled = true;
                    break;
                } else {
                    Thread.sleep(1000);
                }
            }
            if (bEnabled)
            {
                mTestRunner.WriteLog("PASS: Base searches cannot be renamed", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: Base searches CAN be renamed", LogFile.iERROR);
            }

            // create a derived search and then rename it
            Utils.DoClick(mTestCase, By.id(TTL_SEARCH_PUBL+StdSHash), By.id(DLG_CUSTOMIZE), 15000);
            Boolean bContinue = false;
            if (Utils.isElementPresent(mTestCase, By.id(DLG_CUSTOMIZE), false) && Utils.isTextPresent(mTestCase, StdSrchName) )
            {
                // save it as "Derived Search Test"
                Utils.DoClick(mTestCase, By.id(BTN_CUST_SAVE),  By.id(DLG_SAVESEARCH), 10000);
                Utils.DoType(mTestCase, By.id(TXTNAME), "Derived search test");
                Utils.DoType(mTestCase, By.id(TXTDESC), "Derived search test - All Dates");
                Utils.DoClick(mTestCase, By.id(BTN_SAVESEARCH_OK), null, 100);
                int count = 0;
                while ( (Utils.isElementPresent(mTestCase, By.id(DLG_SAVESEARCH), false) 
                        || !Utils.isElementPresent(mTestCase, By.id(DLG_RESTRICT), false)) && count < 20)
                { 
                    count++; Thread.sleep(1000); 
                }
                // now set the new search as allowed to be run by all user roles                
                if (Utils.isElementPresent(mTestCase, By.id(DLG_RESTRICT), false))
                {
                    Utils.DoClick(mTestCase, By.id(BTN_RESTRICT_OK), null, 500);
                }    
                
                Utils.DoClick(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), null, 100);
                if (Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+DrvSHash), false))
                {
                    mTestRunner.WriteLog("PASS: Derived version of Std Search successfully saved.", LogFile.iTEST);
                    bContinue = true;
                } else {
                    mTestRunner.WriteLog("FAIL: Expected Derived version of Std Search not present.", LogFile.iERROR);
                }
            }
            // rename the new search
            if (bContinue)
            {
                if (!Utils.isDisplayed(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash)))
                {
                    Utils.DoClick(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), null, 500);
                }
                Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), null, 100);
                Utils.DoClick(mTestCase, By.id(CXT_ACTION_RENAME), null, 100);

                if (Utils.isElementPresent(mTestCase, By.id(DLG_RENAME), false))
                {
                    Utils.DoType(mTestCase, By.id(TXTNAME), rename);
                    Utils.DoClick(mTestCase, By.id(BTN_RENAME_OK), null, 100);
                    int i=0; while ( Utils.isElementPresent(mTestCase, By.id(DLG_RENAME), false) && i<10 ) { Thread.sleep(500); i++; }
                    String newName = Utils.GetText(mTestCase, By.id(TTL_SEARCH_PUBL+DrvSHash), false);
                    if (newName.contentEquals(rename))
                    {
                        mTestRunner.WriteLog("PASS: Derived version of Std Search successfully renamed to: " + newName, LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: Derived version of Std Search was not renamed, name is: " + newName, LogFile.iERROR);
                    }
                    // now on the Review tab
                    Utils.DoClick(mTestCase, By.id(TAB_REVIEW), By.id(TTL_RUN_PUBL+StdSHash), 10000);
                    if (Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+renameHash), false))
                    {
                        mTestRunner.WriteLog("PASS: the Review Search list has refreshed to include the new renamed Search", LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: the Review Search list has not refreshed to include the new renamed Search", LogFile.iERROR);
                    }
                    // finally delete the search again
                    Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(BTN_ADMIN_INSTALL), 10000);
                    Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), null, 100);
                    Utils.DoClick(mTestCase, By.id(CXT_ACTION_UNINST), By.id(DLG_MESSAGE), 10000);
                    Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
                    i=0; while ( Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), false) && i < 20 ) { Thread.sleep(500); i++; }
                    if (!Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), false) )
                    {
                        mTestRunner.WriteLog("PASS: Renamed derived search deleted", LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: Renamed derived search NOT deleted!", LogFile.iERROR);
                    }
                } else {
                    mTestRunner.WriteLog("FAIL: Rename Search pop-up not displayed!", LogFile.iERROR);
                    // delete the derived search and finish
                    Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), null, 100);
                    Utils.DoClick(mTestCase, By.id(CXT_ACTION_UNINST), By.id(DLG_MESSAGE), 10000);
                    Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
                    int i=0; while (Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+DrvSHash), false) && i < 20) { Thread.sleep(500); i++; }
                }
            }
        } else {
            mTestRunner.WriteLog("FAIL: Couldn't switch to the Admin Searches list", LogFile.iERROR);
        }
        mTestRunner.CloseLogFileGroup("Completed " + mName + " - Rename", LogFile.iINFO);
    }

    // AutoIT executables are required to pass the xml files for upload as js is disabled in input fields where type=file
    private void testSearchInstall() throws Exception
    {
        boolean tryUninstall = false;
        //debug line below
        //Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(BTN_ACTION_PUBL+DIUHash), 10000);        

        mTestRunner.NewLogFileGroup("Start " + mName + " - Installation", LogFile.iINFO);
        // Got to Manage page and launch install dialog
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(CHK_SEARCH_PUBL+StdSHash), 10000);
        Utils.DoClick(mTestCase, By.id(BTN_ADMIN_INSTALL), By.id(DLG_INSTALL_DESC), 10000);

        // Install 2 test searches PUBLISHED        
        String exePath = Utils.SetParam(mTestCase, "searchExePath");
        String path = Utils.SetParam(mTestCase, "searchesPath");
        
        if (mTestRunner.GetTargetBrowser().contains("firefox"))
        {
            Utils.PlainType(mTestCase, By.id(SDFILE), path);
        } else {
            // for ie use an AutoIT executable to access the File Upload dialog
            try {
                 String[] commands = new String[]{};
                 commands = new String[]{exePath, path}; //location of the AutoIT executable
                 Runtime.getRuntime().exec(commands);
                 Thread.sleep(1000);
            } 
            catch (Exception e) 
            {
                mTestRunner.WriteLog("ERROR installing test searches: " + e.toString(), LogFile.iERROR);
            }
            if (mTestRunner.GetTargetBrowser().startsWith("ie"))
            {
                Utils.DoubleClick(mTestCase, By.id(SDFILE), 3000);
            } else {
                Utils.DoClick(mTestCase, By.id(SDFILE), null, 3000);
            }
        }
        Utils.DoClick(mTestCase, By.id(BTN_INSTALL_INSTALL), null, 100);
        while (Utils.isTextPresent(mTestCase, "This field is required", "span.validationMessage", false) )
        { 
            Thread.sleep(1000); 
            Utils.DoClick(mTestCase, By.id(BTN_INSTALL_INSTALL), null, 100);
        }
        int i = 0; while (!Utils.isElementPresent(mTestCase, By.id(TXT_SEARCH_STATUS), false) && i < 10) { Thread.sleep(1000); i++; }
        
        if (Utils.isTextPresent(mTestCase, successInstall, "td.message"))
        {
            tryUninstall = true;
            mTestRunner.WriteLog("PASS: Test Searches successfully installed", LogFile.iTEST);
            // check they're there and published...
            if ( Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), false)
                    && Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+BBGStdSHash), false) )
            {
                mTestRunner.WriteLog("PASS: the Admin Search list has refreshed to include the new published Searches", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: the Admin Search list has not refreshed to include the new published Searches", LogFile.iERROR);
            }
            i = 0; while (Utils.GetText(mTestCase, By.id(TXT_SEARCH_STATUS), false).length() > 1 && i < 10) { Thread.sleep(1000); i++; }
            // now need to add the new searches to the [Default] Role
            Utils.DoClick(mTestCase, By.id(TAB_ADMIN_RESOURCES), By.id(BTN_RESOURCE_EDIT), 5000);
            Utils.DoClick(mTestCase, By.id(BTN_RESOURCE_SEARCHES), By.id(DLG_EDIT_RESOURCES), 5000);
            if (!Utils.isSelected(mTestCase, By.id(CHK_RESOURCE_FOLDER)))
            {
                Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), null, 100);
                Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+BBGStdSHash), null, 100);
                Utils.DoClick(mTestCase, By.id(BTN_EDIT_RESOURCES_OK), null, 1000);
            } else {
                Utils.DoClick(mTestCase, By.id(BTN_EDIT_RESOURCES_CANCEL), null, 500);
            }
            
            // now check for the searches on the Review tab
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW), By.id(TTL_RUN_PUBL+StdSHash), 10000);
            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+BBGExpHash), false)
                    && Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+BBGStdSHash), false) )
            {
                mTestRunner.WriteLog("PASS: the Review Search list has refreshed to include the new published Searches", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: the Review Search list has not refreshed to include the new published Searches", LogFile.iERROR);
            }
        } else {
            if ( Utils.isElementPresent(mTestCase, By.id(DLG_INSTALL_DESC), false) )
            {
                Utils.DoClick(mTestCase, By.id(BTN_INSTALL_SEARCH_CANCEL), null, 100);
                i=0; while ( Utils.isElementPresent(mTestCase, By.id(DLG_INSTALL_DESC), false) && i < 10) { Thread.sleep(500); i++; }
            }
            tryUninstall = false;
            mTestRunner.WriteLog("FAIL: Search install failed!", LogFile.iERROR);
        }

        // Install 2 test reports UNPUBLISHED
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(TAB_ADMIN_RESOURCES), 10000);
        if (!Utils.isElementPresent(mTestCase, By.id(BTN_ADMIN_INSTALL), false))
        {
            Utils.DoClick(mTestCase, By.id(TAB_ADMIN_SEARCHES), By.id(BTN_ADMIN_INSTALL), 10000);
        }
        Utils.DoClick(mTestCase, By.id(BTN_ADMIN_INSTALL), By.id(DLG_INSTALL_DESC), 10000);
        path = Utils.SetParam(mTestCase, "reportsPath");
        
        if (mTestRunner.GetTargetBrowser().contains("firefox"))
        {
            Utils.PlainType(mTestCase, By.id(SDFILE), path);
        } else {
            // for ie use an AutoIT exe to access the File Upload dialog
            try {
                 String[] commands = new String[]{};
                 commands = new String[]{exePath, path}; //location of the AutoIT executable
                 Runtime.getRuntime().exec(commands);
                 Thread.sleep(1000);
            } 
            catch (Exception e) 
            {
                mTestRunner.WriteLog("ERROR installing test Reports: " + e.toString(), LogFile.iERROR);
            }
            if (mTestRunner.GetTargetBrowser().startsWith("ie"))
            {
                Utils.DoubleClick(mTestCase, By.id(SDFILE), 3000);
            } else {
                Utils.DoClick(mTestCase, By.id(SDFILE), null, 3000);
            }
        }
        Utils.DoClick(mTestCase, By.id(CHK_PUBL), null, 250);
        Utils.DoClick(mTestCase, By.id(BTN_INSTALL_INSTALL), null, 100);
        i=0; while (Utils.isTextPresent(mTestCase, "This field is required", "span.validationMessage", false) && i < 10) 
        { 
            Thread.sleep(1000); i++;
            Utils.DoClick(mTestCase, By.id(BTN_INSTALL_INSTALL), null, 100); 
        }     
        i = 0; while (!Utils.isElementPresent(mTestCase, By.cssSelector("td.message"), false) && i < 10) { Thread.sleep(1000); i++; }

        if ( Utils.isTextPresent(mTestCase, successInstall, "td.message") )
        {
            mTestRunner.WriteLog("PASS: Test Reports successfully installed", LogFile.iTEST);
            // check they're there but unpublished...
            findme = CHK_SEARCH_PUBL+POSHash;
            bSelected = Utils.SelectAdminCategory(mTestCase, "Reports", By.id(findme));
            if (bSelected)
            {
                if ( Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_UNPUBL+AuditStatusHash), false)
                        && Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_UNPUBL+DAlertHash), false) )
                {
                    mTestRunner.WriteLog("PASS: The Admin Report list has refreshed to include the new unpublished reports", LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: The Admin Report list has not refreshed to include the new unpublished reports", LogFile.iERROR);
                }
            }
            // and now on the Review tab...
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW), By.id(TTL_RUN_PUBL+StdSHash), 10000);
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW_REPORTS), By.id(TTL_RUN_PUBL+RActHash), 10000);
            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+AuditStatusHash), false)
                    && Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+DAlertHash), false) )
            {
                mTestRunner.WriteLog("FAIL: The Review Report list has refreshed to include the new UNpublished reports", LogFile.iERROR);
            } else {
                mTestRunner.WriteLog("PASS: The Review Report list has not refreshed to include the new unpublished reports", LogFile.iTEST);
            }
        } else {
            if ( Utils.isElementPresent(mTestCase, By.id(DLG_INSTALL_DESC), false) )
            {
                Utils.DoClick(mTestCase, By.id(BTN_INSTALL_SEARCH_CANCEL), null, 100);
                i=0; while ( Utils.isElementPresent(mTestCase, By.id(DLG_INSTALL_DESC), false) && i < 20) { Thread.sleep(500); i++; }
            }            
            tryUninstall = false;
            mTestRunner.WriteLog("FAIL: Report install failed!", LogFile.iERROR);
        }

        if (tryUninstall)
        {
            // logout and back in to check install success as RLS user
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "secondary");

            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+BBGExpHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+BBGStdSHash), false) )
            {
                    mTestRunner.WriteLog("PASS: the Search list has refreshed to include the new published Searches", LogFile.iTEST);
            } else {
                    mTestRunner.WriteLog("FAIL: the Search list has not refreshed to include the new published Searches", LogFile.iERROR);
            }
            Utils.DoClick(mTestCase, By.id(TAB_REVIEW_REPORTS), By.id(TTL_RUN_PUBL+POSHash), 10000);
            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+AuditStatusHash), false)
                    || Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+DAlertHash), false) )
            {
                    mTestRunner.WriteLog("FAIL: the Report list has refreshed to include the new UNpublished reports", LogFile.iERROR);
            } else {
                    mTestRunner.WriteLog("PASS: the Report list has not refreshed to include the new UNpublished reports", LogFile.iTEST);
            }
            // logout and back in as admin user
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "primary");
        }

        if (tryUninstall)
        {           
            // navigate to the Admin search list
            Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(CHK_SEARCH_PUBL+StdSHash), 10000);
            // Uninstall the searches again            
            Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), null, 100);
            Utils.DoClick(mTestCase, By.id(CHK_SEARCH_PUBL+BBGStdSHash), null, 100);
            Utils.DoClick(mTestCase, By.id(BTN_ADMIN_BULK), By.id(CXT_BULK_UNINST), 10000);
            
            // this next line causes the js error (without the logout/in first)
            Utils.DoClick(mTestCase, By.id(CXT_BULK_UNINST), By.id(DLG_MESSAGE), 10000);
            Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
            i=0; while ( Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), false) && i < 20) { Thread.sleep(500); i++; }

            if ( !Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+BBGExpHash), false)
                    && !Utils.isElementPresent(mTestCase, By.id(CHK_FOLDER_PUBL+BBGStdSHash), false) )
            {
                    mTestRunner.WriteLog("PASS: the Search list has refreshed to remove the new items", LogFile.iTEST);
            } else {
                    mTestRunner.WriteLog("FAIL: the Search list has not refreshed to remove the new items", LogFile.iERROR);
            }

            // uninstall BBG reports via their local Actions button
            findme = CHK_SEARCH_PUBL+POSHash;
            bSelected = Utils.SelectAdminCategory(mTestCase, "Reports", By.id(findme));
            if (bSelected)
            {            
                Utils.DoClick(mTestCase, By.id(BTN_ACTION_UNPUBL+AuditStatusHash), null, 100);
                Utils.DoClick(mTestCase, By.id(CXT_ACTION_UNINST), By.id(DLG_MESSAGE), 10000);
                Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 100);
                Utils.DoClick(mTestCase, By.id(BTN_ACTION_UNPUBL+DAlertHash), null, 100);
                Utils.DoClick(mTestCase, By.id(CXT_ACTION_UNINST), By.id(DLG_MESSAGE), 10000);
                Utils.DoClick(mTestCase, By.id(BTN_MSG_OK), null, 0);
                while ( Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_UNPUBL+DAlertHash), false)) { }

                if (!Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_UNPUBL+AuditStatusHash), false)
                        && !Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_UNPUBL+DAlertHash), false) )
                {
                        mTestRunner.WriteLog("PASS: the Report list has refreshed to remove the new items", LogFile.iTEST);
                } else {
                        mTestRunner.WriteLog("FAIL: the Report list has not refreshed to remove the new items", LogFile.iERROR);
                }
            }
        } else {
            mTestCase.ReportError("ERROR: Test Search and/or Report Uninstall not attempted due to failure of one or other to be installed correctly.");
        }
        mTestRunner.CloseLogFileGroup("Completed " + mName + " - Installation", LogFile.iINFO);
    }
    
    private void SetParameters()
    {
        ISRHash = Utils.SetParam(mTestCase, "ISRHash");
        DIHash = Utils.SetParam(mTestCase, "DIHash");
        POSHash = Utils.SetParam(mTestCase, "POSHash");
        RActHash = Utils.SetParam(mTestCase, "RevActHash");
        StdSHash = Utils.SetParam(mTestCase, "StdSrchHash");
        IncDBHash = Utils.SetParam(mTestCase, "IncDBHash");
        SmartTagHash = Utils.SetParam(mTestCase, "SmartTagHash");
        DIUHash = Utils.SetParam(mTestCase, "diuSrchHash");
        DrvSHash = Utils.SetParam(mTestCase, "myDerivedSrchHash");
        BBGExpHash = Utils.SetParam(mTestCase, "BBGExpHash");
        BBGStdSHash = Utils.SetParam(mTestCase, "BBGStdSrchHash");
        AuditStatusHash = Utils.SetParam(mTestCase, "AuditStatusHash");
        DAlertHash = Utils.SetParam(mTestCase, "DetailedAlertHash");
        successPublish = Utils.SetParam(mTestCase, "successPublish");
        successUnpublish = Utils.SetParam(mTestCase, "successUnpublish");
        successPublish2 = Utils.SetParam(mTestCase, "successPublish2");
        successUnpublish2 = Utils.SetParam(mTestCase, "successUnpublish2");
        StdSrchName = Utils.SetParam(mTestCase, "StdSrchName");
        successInstall = Utils.SetParam(mTestCase, "successInstall");
        //successInstall2 = Utils.SetParam(mTestCase, "successInstall2");
        TAB_ADMIN = Utils.SetParam(mTestCase, "TAB_ADMIN"); //"tab_manage";
        TAB_ADMIN_SEARCHES = Utils.SetParam(mTestCase, "TAB_ADMIN_SEARCHES"); //"tab_manage.searches";
        TAB_ADMIN_RESOURCES = Utils.SetParam(mTestCase, "TAB_ADMIN_RESOURCES");// "tab_manage.resources";
        TAB_REVIEW = Utils.SetParam(mTestCase, "TAB_REVIEW"); //"tab_searches";
        TAB_REVIEW_REPORTS = Utils.SetParam(mTestCase, "TAB_REVIEW_REPORTS"); //"tab_searches.Reports";
        DLG_MESSAGE = Utils.SetParam(mTestCase, "DLG_MESSAGE");
        BTN_MSG_OK = Utils.SetParam(mTestCase, "BTN_MESSAGE_OK");
        DLG_CUSTOMIZE = Utils.SetParam(mTestCase, "DLG_CUSTOMIZE");
        BTN_CUST_SAVE = Utils.SetParam(mTestCase, "BTN_CUST_SAVE");
	BTN_CUST_HELP = Utils.SetParam(mTestCase, "BTN_CUST_HELP");
        DLG_SAVESEARCH = Utils.SetParam(mTestCase, "DLG_SAVESEARCH");
	BTN_SAVESEARCH_OK = Utils.SetParam(mTestCase, "BTN_SAVESEARCH_OK");
	BTN_ACTION_PUBL = Utils.SetParam(mTestCase, "BTN_ACTION_PUBL");
	TTL_SEARCH_PUBL = Utils.SetParam(mTestCase, "TTL_SEARCH_PUBL");
	TOG_FOLDER_PUBL = Utils.SetParam(mTestCase, "TOG_FOLDER_PUBL");
	CHK_SEARCH_PUBL = Utils.SetParam(mTestCase, "CHK_SEARCH_PUBL");
        CHK_FOLDER_PUBL = Utils.SetParam(mTestCase, "CHK_FOLDER_PUBL"); //"treeChk_Folder.published.";
        TTL_RUN_PUBL = Utils.SetParam(mTestCase, "TTL_RUN_PUBL"); //"treeTitle_Run.published.";
	DLG_RESTRICT = Utils.SetParam(mTestCase, "DLG_RESTRICT");
	BTN_RESTRICT_OK = Utils.SetParam(mTestCase, "BTN_RESTRICT_OK");
        BTN_ADMIN_INSTALL = Utils.SetParam(mTestCase, "BTN_ADMIN_INSTALL"); //"btn_AdminSearches.Install";
        CHK_SEARCH_UNPUBL = Utils.SetParam(mTestCase, "CHK_SEARCH_UNPUBL"); //"treeChk_Search.unpublished.";
        BTN_ACTION_UNPUBL = Utils.SetParam(mTestCase, "BTN_ACTION_UNPUBL"); //"btn_TreeAction.unpublished.";
        BTN_ADMIN_BULK = Utils.SetParam(mTestCase, "BTN_ADMIN_BULK"); //"btn_AdminSearches.AdminSearchesBulkActions";
        CXT_BULK_UNINST = Utils.SetParam(mTestCase, "CXT_BULK_UNINST"); //"cxt_AdminSearches.AdminSearchesBulkActions.ActionUninstall";
        CXT_BULK_PUBL = Utils.SetParam(mTestCase, "CXT_BULK_PUBL"); //"cxt_AdminSearches.AdminSearchesBulkActions.ActionPublish";
        CXT_BULK_UNPUBL = Utils.SetParam(mTestCase, "CXT_BULK_UNPUBL"); //"cxt_AdminSearches.AdminSearchesBulkActions.ActionUnpublish";
        CXT_ACTION_UNINST = Utils.SetParam(mTestCase, "CXT_ACTION_UNINST"); //"cxt_ActionUninstall";
        
        // need to move out to XML
        BTN_CUST_XCLOSE = "btn_Customize.smlClose";
        SDFILE = "sdfile";
        CHK_PUBL = "chkPublish";
        BTN_INSTALL_INSTALL = "btn_InstallSearches.Install";
        DLG_INSTALL_DESC = "InstallSearches_desc";
        BTN_INSTALL_SEARCH_CANCEL = "btn_InstallSearches.Cancel";
        TXT_SEARCH_STATUS = "AdminSearchesStatusText";        
        DLG_EDIT_RESOURCES = "EditResources";
        BTN_EDIT_RESOURCES_OK = "btn_EditResources.OK";
        BTN_EDIT_RESOURCES_CANCEL = "btn_EditResources.Cancel";
        CHK_RESOURCE_FOLDER = "treeChk_ResourceFolder.Searches";
        CHK_RESOURCE_REPORTS = "treeChk_ResourceFolder.Reports";
        BTN_RESOURCE_EDIT = "btn_EditSettings";
        BTN_RESOURCE_SEARCHES = "btn_EditSearches";
        TAB_2 = "tab_2";        
        TXTNAME = "txtName";
        TXTDESC = "txtDesc";
        BTN_RENAME_OK = "btn_RenameSearch.OK";
        DLG_RENAME = "RenameSearch";
        BTN_ACTION_SYSTEM = "btn_TreeAction.system.";
        CXT_ACTIONS = "MIR_ContextMenu_AdminSearchesActions";
        CXT_ACTION_RENAME = "cxt_ActionRename";
        CXT_ACTION_TEST = "cxt_ActionTest";
        CXT_ACTION_EDIT = "cxt_ActionEdit";
        CXT_ACTION_PUBL = "cxt_ActionPublish";
        CXT_ACTION_UNPUBL = "cxt_ActionUnpublish";
        TH_SMARTTAG = "th_SmartTagName";
        BTN_REFRESH = "btn_Results.Refresh";
        CHK_SEARCH_SYSTEM = "treeChk_Search.system.";
        TTL_FOLDER_SYSTEM = "treeTitle_Folder.system.Searches";
        TOG_FOLDER_SYSTEM = "treeToggle_Folder.system.Searches";
        BTN_DASH_RESET = "btn_Dashboard.Reset";        
    }

}