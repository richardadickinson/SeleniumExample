//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import common.*;
import org.openqa.selenium.*;


public class Search extends WgnTestStep
{
    private int mSearchTimeout = 60000;
    private String errMsg = "";
    private long totalRunTime = 0;
    private static String mPageSize;
    private String mMaxResults;
    private String mHashId;
    private String mPanelId;
    private String drvHash;
    private String mUserSearch;
    private String mDrvSrchName;
    private String mDrvSrchDesc;
    private String mEeTo;
    private String mNoResults;
    private Boolean mDWonly = false;
    private Boolean mQuarantine = true;
    private Boolean mCleanup = false;
    private Boolean bGoBack = true;
    private static String parentHandle;
    private int totalIterations;

    public Search() { }
    
    public static int GetPageSize()
    {
        if (!mPageSize.isEmpty())
        {
            return Integer.parseInt(mPageSize);
        } else {
            return 25;
        }
    }

    @Override
    public void Run() throws Exception
    {
        // retrieve and prepare test case parameters        
        SetParameters();
        
        // run search
        if (!mCleanup)
        {
            if ( !(mPageSize.isEmpty() && mMaxResults.isEmpty()) ) setupTest(true);
            VALIDATION:
            try
            {
                for (int numIterations=0; numIterations < totalIterations; numIterations++)
                {
                    if (numIterations == 0) {
                        if (!RunSearch()) { break VALIDATION; }
                        Utils.SetSearchAttributes(mTestCase);
                        if (bGoBack && totalIterations > 1) 
                        {
                            Utils.CloseWindow(mTestCase, parentHandle);
                        }
                    }

                    if (bGoBack)
                    {
                        if (numIterations > 0) {
                            if (!RunSearch()) { break VALIDATION; }
                            if (numIterations != totalIterations-1)
                            {
                                Utils.CloseWindow(mTestCase, parentHandle);  
                            }
                        }
                    } else {
                        StopWatch itimer = new StopWatch().start();
                        Utils.DoClick(mTestCase, By.id("btn_Results.Refresh"), null, 0);
                        int count = 0;
                        while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && count < mSearchTimeout/1000) { count++; Thread.sleep(1000); }
                        itimer.stop();
                        mTestRunner.WriteLog("PASS: Standard search refreshed successfully (" + String.valueOf(itimer.getElapsedTime()) + " secs)", LogFile.iTEST);
                        if (numIterations == 0)
                        {
                            totalRunTime = itimer.getElapsedTime();
                        } else {
                            totalRunTime = totalRunTime + itimer.getElapsedTime();
                        }
                    }
                }
                long avgRunTime = totalRunTime / totalIterations;
                mTestRunner.WriteLog("Average search runtime = " + String.valueOf(avgRunTime) + " secs (iterations=" + String.valueOf(totalIterations) + ")", LogFile.iINFO);
            }
            catch(Exception e)
            {               
                //e.printStackTrace();  // restore this line for debugging only
                ReportError(String.format("Search '%s' failed due to exception: %s", mPanelId, e.getMessage()));
                throw e;
            }

            // abort the whole test case if there were problems retrieving search results
            if (errMsg.length() != 0) {
                Utils.CloseWindow(mTestCase, parentHandle);
                Utils.Logoff(mTestCase);
            }
        } else {
            cleanupTest();
        }

    }

    public boolean RunSearch() throws Exception
    {
        StopWatch timer = new StopWatch();
        
        if (!mQuarantine || mDWonly || !mUserSearch.isEmpty())
        {
            int k=0; while (!Utils.isElementPresent(mTestCase, By.id("btn_Favorite.published." + mHashId), false) && k <10) { Thread.sleep(1000); k++; } 
            Utils.DoClick(mTestCase, By.id("btn_Favorite.published." + mHashId), null, 10);
            Utils.DoClick(mTestCase, By.id("cxt_ActionEdit"), By.id("txtSubject"), 60000);

            if (!mUserSearch.isEmpty())
            {
                // select e-mails only
                Utils.DoClick(mTestCase, By.id("choTypes1"), null, 10);
                if (Utils.isSelected(mTestCase, By.id("lstDiM.All")))
                {
                    Utils.DoClick(mTestCase, By.id("lstDiM.All"), null, 10);
                    Utils.DoClick(mTestCase, By.id("lstDiM.0"), null, 10);
                } else if (!Utils.isSelected(mTestCase, By.id("lstDiM.0"))) {
                    Utils.DoClick(mTestCase, By.id("lstDiM.0"), null, 10);
                }
                if (Utils.isSelected(mTestCase, By.id("lstDaR.All"))) { Utils.DoClick(mTestCase, By.id("lstDaR.All"), null, 10); }
                if (Utils.isSelected(mTestCase, By.id("lstDiU.All"))) { Utils.DoClick(mTestCase, By.id("lstDiU.All"), null, 10); }
                // set sender address to usersearch parameter value
                Utils.DoClick(mTestCase, By.id("EmailEvents_desc_toggle"), null, 10);
                Utils.DoType(mTestCase, By.id("txtEEAddress1"), mUserSearch);
                Utils.SelectOptionByText(mTestCase, By.id("lstEEDir"), mEeTo);
            }
            Utils.DoClick(mTestCase, By.id("tab_1"), By.id("lstQU.0"), 5000);
            if (!mQuarantine && !mDWonly)
            {
                Utils.DoClick(mTestCase, By.id("chkHW"), null, 10);
                Utils.DoClick(mTestCase, By.id("chkBL"), null, 10);
            }
            if (!mQuarantine && mDWonly)
            {
                Utils.DoClick(mTestCase, By.id("chkWD"), null, 10);
            }

            timer.start();
            parentHandle = Utils.GetNewWindow(mTestCase, By.id("btn_Customize.Run"));
        } else {
            timer.start();
            parentHandle = Utils.GetNewWindow(mTestCase, By.id("treeTitle_Run.published." + mHashId));
        }
        
        int count = 0;
        while (!Utils.isElementPresent(mTestCase, By.id("Row0"), false) && count < 30) { count++; Thread.sleep(1000); }
        count = 0;
        while (!Utils.isDisplayed(mTestCase, By.id("Row0"))  && count < 30) { count++; Thread.sleep(1000); }
        timer.stop();

        if (Utils.isElementPresent(mTestCase, By.id("divResultsTable"), false))
        {
            if (Utils.isTextPresent(mTestCase, mNoResults, false))
            {
                ReportError("ERROR: No Search Results were returned");
                errMsg = "No Results!";
                return false;
            } else {
                mTestRunner.WriteLog("PASS: Standard search ran successfully (" + String.valueOf(timer.getElapsedTime()) + " secs)", LogFile.iTEST);
                totalRunTime = totalRunTime + timer.getElapsedTime();
            }
        } else {
            mTestRunner.WriteLog("FAIL: Standard search failed to run", LogFile.iERROR);
        }
        return true;
    }


    private void setupTest(Boolean bSetup) throws Exception
    {
        Boolean setPageSize = false;
        Boolean createDerivedSearch = false;        
        if (!bSetup) // restore current defaults
        {
            if (mPageSize.isEmpty()) mPageSize = "25";  // reset to default
        }
        // now see if there is any setup or cleanup to do
        if ( !bSetup || !mPageSize.isEmpty() || !mMaxResults.isEmpty() )
        {
            // if the search results window is open from a previous test step we need to close it
            if (Utils.isElementPresent(mTestCase, By.id("divResultsTable"), false))  Utils.CloseWindow(mTestCase, parentHandle);
            // make sure we're an Admin user
            if (!Utils.isElementPresent(mTestCase, By.id("tab_manage"), false))
            {
                Utils.Logoff(mTestCase);
                Utils.Login(mTestCase, "admin");
            }
            Utils.DoClick(mTestCase, By.id("tab_manage"), By.id("treeTitle_Search.published."+mHashId), 10000);
            if (!mPageSize.isEmpty()) setPageSize = setGlobalPageSize();
            // now handle the derived search
            if (!Utils.isElementPresent(mTestCase, By.id("treeTitle_Search.published."+mHashId), false))
            {
                Utils.DoClick(mTestCase, By.id("tab_manage.searches"), By.id("treeTitle_Search.published."+mHashId), 30000);
            }
            if (bSetup)
            {
                if (!mMaxResults.isEmpty()) createDerivedSearch = createDerivedSearch();
            } else {
                deleteDerivedSearch(drvHash);
            }
            // logout and back in as the primary login for the test case  (to load new search prefs and search)        
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "primary");

            // now reset the login's local prefs
            if (setPageSize) resetLocalPrefs();

            if (createDerivedSearch) // check derived search is available
            {
                int count = 0;
                while (!Utils.isElementPresent(mTestCase, By.id("treeTitle_Run.published."+drvHash), false) && count < 10) { count++; Thread.sleep(1000); }
                if (Utils.isElementPresent(mTestCase, By.id("treeTitle_Run.published."+drvHash), false) )
                {
                    mHashId = drvHash;
                    mTestRunner.WriteLog("PASS: successfully set new row limit in derived Std Search", LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: couldn't set new row limit in derived Std Search", LogFile.iERROR);
                }
            }
        }
    }
    
    private Boolean setGlobalPageSize()
    {
        try 
        {
            if (!Utils.isElementPresent(mTestCase, By.id("btn_EditSearches"), false))
            {
                Utils.DoClick(mTestCase, By.id("tab_manage.resources"), By.id("btn_EditSettings"), 30000);
            }
            Utils.DoClick(mTestCase, By.id("btn_EditSettings"), By.id("tab_3"), 10000);
            // change the SearchResultsPageSize
            Utils.DoClick(mTestCase, By.id("tab_3"), By.id("SearchResultsPageSize"), 3000);
            Utils.DoType(mTestCase, By.id("SearchResultsPageSize"), mPageSize);
            Utils.DoClick(mTestCase, By.id("btn_EditResources.OK"), null, 1000);
            //int count = 0; while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && count < 10) { count++; Thread.sleep(1000); }            
            return true;
        }
        catch (Exception e)
        {
            mTestRunner.WriteLog("ERROR occurred setting Global Search Results Page size: " + e.getMessage(), LogFile.iERROR);
            return false;
        }
    }
    
    private void resetLocalPrefs() throws Exception
    {
        Utils.DoClick(mTestCase, By.id("lnk_profile"), By.id("tab_audit"), 10000);
        Utils.DoClick(mTestCase, By.id("btn_Prefs.Reset"), null, 100);
        Utils.DoClick(mTestCase, By.id("btn_Message.Yes"), null, 500);
        int count = 0; while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && count < 10) { count++; Thread.sleep(1000); }
        //check the value was set correctly
        Utils.DoClick(mTestCase, By.id("tab_search"), By.id("SearchResultsPageSize"), 5000);
        String value = Utils.GetAttribute(mTestCase, By.id("SearchResultsPageSize"), "value", false);
        if (value.contentEquals(mPageSize))
        {
            mTestRunner.WriteLog("PASS: page size successfully adjusted to " + mPageSize, LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: couldn't set page size to " + mPageSize + " (still " + value + ")", LogFile.iERROR);
        }
        Utils.DoClick(mTestCase, By.id("btn_Prefs.OK"), null, 100);
    }
    
    private Boolean createDerivedSearch()
    {
        try 
        {
            // go to the Std Search Edit Properties page
            if (Utils.isElementPresent(mTestCase, By.id("treeTitle_Folder.published."+mHashId)))
            {
                Utils.DoClick(mTestCase, By.id("treeTitle_Folder.published."+mHashId), null, 500, false);
            }
            Utils.DoClick(mTestCase, By.id("treeTitle_Search.published."+mHashId), By.id("Customize"), 30000);
            Utils.DoClick(mTestCase, By.id("tab_4"), By.id("$ROWLIMIT"), 10000);
            Utils.DoType(mTestCase, By.id("$ROWLIMIT"), mMaxResults);
            Utils.DoClick(mTestCase, By.id("btn_Customize.Save"), By.id("SaveSearch_desc"), 10000);
            Utils.DoType(mTestCase, By.id("txtName"), mDrvSrchName);
            Utils.PlainType(mTestCase, By.id("txtDesc"), mDrvSrchDesc);
            Utils.DoClick(mTestCase, By.id("btn_SaveSearch.OK"), By.id("RestrictResources"), 5000);

            // now set the new search as allowed to be run by all user roles                
            if (Utils.isElementPresent(mTestCase, By.id("RestrictResources"), false))
            {
                Utils.DoClick(mTestCase, By.id("treeChk_ResourceFolderRoles"), null, 500, false);
                Utils.DoClick(mTestCase, By.id("btn_RestrictResources.OK"), null, 500, false);
            }

            return true;
        }
        catch (Exception e)
        {
            mTestRunner.WriteLog("ERROR occurred creating the Derived Search: " + e.getMessage(), LogFile.iERROR);
            return false;
        }
    }
    
    private void deleteDerivedSearch(String drvHash) throws Exception
    {
        if (Utils.isElementPresent(mTestCase, By.id("treeToggle_Folder.published."+mHashId)))
        {
            Utils.DoClick(mTestCase, By.id("treeToggle_Folder.published."+mHashId), null, 500);
            int count = 0;
            while (!Utils.isEditable(mTestCase, By.id("btn_TreeAction.published."+drvHash)) && count < 20)
            {
                count++;
                Thread.sleep(1000);
            }                

            if (Utils.isElementPresent(mTestCase, By.id("btn_TreeAction.published."+drvHash)) )
            {
               // uninstall the derived Std Search from context menu
               Utils.DoClick(mTestCase, By.id("btn_TreeAction.published."+drvHash), null, 500);
               Utils.DoClick(mTestCase, By.id("cxt_ActionUninstall"), By.id("Message"), 5000);
               Utils.DoClick(mTestCase, By.id("btn_Message.OK"), null, 10);
               while (Utils.isElementPresent(mTestCase, By.id("btn_TreeAction.published."+drvHash), false) && count < 20)
               {
                   count++;
                   Thread.sleep(1000);
               }                                     
               if (!Utils.isElementPresent(mTestCase, By.id("btn_TreeAction.published."+drvHash), false) )
               {
                   mTestRunner.WriteLog("PASS: Derived search deleted", LogFile.iTEST);
               } else {
                   mTestRunner.WriteLog("FAIL: Derived search NOT deleted!", LogFile.iERROR);
               }
            } else {
                mTestRunner.WriteLog("Derived search Clean-up not required - the test search does not exist", LogFile.iINFO);
            }
        } else {
            mTestRunner.WriteLog("Derived search Clean-up not required - the test search does not exist", LogFile.iINFO);
        }
    }

    private void cleanupTest() throws Exception
    {
        setupTest(false);
    }
    
    private void SetParameters()
    {
        mHashId = Utils.SetParam(mTestCase, "StdSrchHash");
        mPanelId = "Results.published." + mHashId;
        drvHash = Utils.SetParam(mTestCase, "myDerivedSrchHash");
        mDrvSrchName = Utils.SetParam(mTestCase, "drvSrchName");
        mDrvSrchDesc = Utils.SetParam(mTestCase, "drvSrchDesc");
        mEeTo = Utils.SetParam(mTestCase, "eeto");
        mNoResults = Utils.SetParam(mTestCase, "noResults");
        
        if (mParameters.containsKey("searchtimeout")) { mSearchTimeout = Integer.parseInt(mParameters.get("searchtimeout")) * 1000; }
        if (mParameters.containsKey("iterations")) 
        { 
            totalIterations = Integer.parseInt(mParameters.get("iterations")); 
        } else {
            totalIterations = 1;
        }
        if (mParameters.containsKey("quarantine")) { mQuarantine = Boolean.parseBoolean(mParameters.get("quarantine")); }
        if (mParameters.containsKey("DWonly")) { mDWonly = Boolean.parseBoolean(mParameters.get("DWonly")); }
        if (mParameters.containsKey("searchuser"))
        {
            try 
            {
                TestUser match = (TestUser)mTestCase.GetTestUsers().get(mParameters.get("searchuser"));
                mUserSearch = match.GetUserName();
                mTestRunner.WriteLog("Username:" + mUserSearch, LogFile.iINFO);
            } 
            catch (Exception e) 
            {
                ReportError("ERROR: Username '" + mUserSearch + "' could not be found in the system users list (" + e.getMessage() + ")");
            }
        } else {
            mUserSearch = "";
        }
        
        if (mParameters.containsKey("back")) { bGoBack = Boolean.parseBoolean(mParameters.get("back")); }
        if (mParameters.containsKey("cleanup")) { mCleanup = Boolean.parseBoolean(mParameters.get("cleanup")); }

        // setup pageSize and MaxResults if necessary
        // *** this is valid for an Admin logon only - don't use these params for RLS logons ***
        mPageSize = "";
        mMaxResults = "";
        if (mParameters.containsKey("pagesize")) { mPageSize = mParameters.get("pagesize"); }
        if (mParameters.containsKey("maxresults")) { mMaxResults = mParameters.get("maxresults"); }        
        mTestRunner.WriteLog("Page size length = " + mPageSize.length(), LogFile.iDEBUG);
        mTestRunner.WriteLog("Max Results length = " + mMaxResults.length(), LogFile.iDEBUG);
    }

}