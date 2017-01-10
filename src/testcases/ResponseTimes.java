//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 **/

import common.*;
import org.openqa.selenium.By;

public class ResponseTimes extends WgnTestStep
{

    private String mUserMatch;
    private String mUserAddress;
    private String parentHandle;
    private int stressLoop;

    public ResponseTimes() { }

    @Override
    public void Run() throws Exception
    {
        // get the user details for the search by username
        if (mParameters.containsKey("usermatch"))
        {
            TestUser match = (TestUser)mTestCase.GetTestUsers().get(mParameters.get("usermatch"));
            mUserMatch = match.GetUserName();
            mUserAddress = match.GetUserValue();
        } else {
            mUserMatch = ""; mUserAddress = "";
        }
        mTestRunner.WriteLog("Search User:" + mUserMatch + ", address: " + mUserAddress, LogFile.iINFO);
        
        // how many times do we want to loop through this test to stress the iConsole?
        stressLoop = 1;
        if (mParameters.containsKey("stressLoop"))
        {
            stressLoop = Integer.parseInt(mParameters.get("stressLoop"));
        }
        
        for (int count=0; count < stressLoop; count++)
        {
            String logNo = String.valueOf(count+1);
            mTestRunner.NewLogFileGroup("Starting Stress Loop: " + logNo, LogFile.iINFO);
            Utils.DoClick(mTestCase, By.id("btn_Favorite.published."+mParameters.get("StdSrchHash")), null, 250);
            Utils.DoClick(mTestCase, By.id("cxt_ActionEdit"), By.id("Customize"), 20000);
            int iTests = 7;
            for (int i=0; i < iTests; i++)
            {
                // customize the search params
                CustomizeSearch(i);
                // run and time the search
                StopWatch itimer = new StopWatch().start();

                if (i==0)
                {
                    parentHandle = Utils.GetNewWindow(mTestCase, By.id("btn_Customize.Run"));
                    while (!Utils.isElementPresent(mTestCase, By.cssSelector("table#divResultsTable"), false)) { }
                } else {
                    Utils.DoClick(mTestCase, By.id("btn_Customize.Run"), By.id("progressContent"), 5000);
                    while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false)) { }
                }
                itimer.stop();
                mTestRunner.WriteLog("PASS: Test case " + i + " search ran successfully in " + String.valueOf(itimer.getElapsedTime()) + " secs", LogFile.iTEST);

                // do some validation of parameters applied
                CheckParameters(i);

                // return to Customize page
                if (i != iTests-1)  Utils.DoClick(mTestCase, By.id("btn_Results.Edit"), By.id("Customize"), 20000);               
            }
            Utils.CloseWindow(mTestCase, parentHandle);

            mTestRunner.CloseLogFileGroup("Completed Stress Loop: " + logNo, LogFile.iINFO);
        }
    }

    private void CustomizeSearch(int i) throws Exception
    {
        /* Test cases:
            0: Search for All events, All dates
            1: Search for All events, All dates for a user specified by Name Match
            2: Search for All events, All dates for a user specified by Specific Match
            3: All e-mails within 1 day range
            4: All e-mails within 2 day range
            5: All e-mails sent from a specified e-mail address within 1 day range
            6: All e-mails sent from a specified e-mail address within 2 day range
         */

        if (i==0)
        {
            // set the date range to All Dates
            Utils.DoClick(mTestCase, By.id("__attr1dateRange1"), null, 100);
            Utils.SelectOptionByText(mTestCase, By.id("__attr4dateRange"), mParameters.get("alldates"));
        } 
        else if (i == 1) 
        {
            // set the username match
            Utils.DoClick(mTestCase, By.id("tab_2"), By.id("txtNameMatch"), 5000);
            Utils.DoType(mTestCase, By.id("txtNameMatch"), mUserMatch);
        } 
        else if (i == 2) 
        {
            Utils.DoClick(mTestCase, By.id("tab_2"), By.id("txtNameMatch"), 5000);
            // clear the name match field
            Utils.DoType(mTestCase, By.id("txtNameMatch"), "");
            // set the specific username match
            Utils.DoClick(mTestCase, By.id("btntxtSpecificMatch"), By.id("AvailableItems_desc"), 10000);
            Utils.DoType(mTestCase, By.id("txtSearchText"), mUserMatch);
            Utils.DoClick(mTestCase, By.id("btn_Picker.Search"), By.id("progressContent"), 5000);
            while (Utils.isElementPresent(mTestCase, By.id("progressContent"), false)) { }            
            // Select the username in the User picker
            if (Utils.isElementPresent(mTestCase, By.id("Dlg0"), false))
            {
                Utils.DoClick(mTestCase, By.id("chk_Results.Dlg0"), null, 100);
                Utils.DoClick(mTestCase, By.id("btn___attr1txtSpecificMatch.Action.0"), null, 100);
            } else {
                mTestRunner.WriteLog("FAIL: User picker failed to find any results", LogFile.iERROR);
            }

            Utils.DoClick(mTestCase, By.id("btn_Picker.OK"), null, 100);
            // check the specific name match was set ok
            String aItems = Utils.GetAttribute(mTestCase, By.id("__attr1txtSpecificMatch"), "value", false);
            if (aItems.contains(mUserMatch) )
            {
                mTestRunner.WriteLog("PASS: Specific name match set to " + aItems, LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: Specific name match NOT set to " + mUserMatch + "(text=" + aItems + ")", LogFile.iERROR);
            }
        } 
        else if (i == 3) 
        {
            Utils.DoClick(mTestCase, By.id("tab_2"), By.id("btn2txtSpecificMatch"), 5000);
            // clear the specific name match
            Utils.DoClick(mTestCase, By.id("btn2txtSpecificMatch"), null, 100);
            // set 1 day time range
            Utils.DoClick(mTestCase, By.id("tab_0"), By.id("__attr4dateRange"), 5000);
            Utils.SelectOptionByText(mTestCase, By.id("__attr4dateRange"), mParameters.get("today"));
            // select e-mails only
            Utils.DoClick(mTestCase, By.id("choTypes1"), null, 100);
            if ( Utils.isSelected(mTestCase, By.id("lstDiM.All")) )
            {
                Utils.DoClick(mTestCase, By.id("lstDiM.All"), null, 100);
                Utils.DoClick(mTestCase, By.id("lstDiM.0"), null, 100);
            } 
            else if ( !Utils.isSelected(mTestCase, By.id("lstDiM.0")) ) 
            {
                Utils.DoClick(mTestCase, By.id("lstDiM.0"), null, 100);
            }
            if (Utils.isSelected(mTestCase, By.id("lstDaR.All")) ) { Utils.DoClick(mTestCase, By.id("lstDaR.All"), null, 100); }
            if (Utils.isSelected(mTestCase, By.id("lstDiU.All")) ) { Utils.DoClick(mTestCase, By.id("lstDiU.All"), null, 100); }

        } 
        else if (i == 4) 
        {
            // change date range to 2 days
            Utils.SelectOptionByText(mTestCase, By.id("__attr4dateRange"), mParameters.get("alldates"));
            Utils.DoClick(mTestCase, By.id("__attr1dateRange1"), null, 100);
            Utils.PlainType(mTestCase, By.id("__attr2dateRange"), "2");
            Utils.SelectOptionByText(mTestCase, By.id("__attr3dateRange"), mParameters.get("days"));

        } 
        else if (i == 5) 
        {
            // change date range to 1 day
            Utils.DoType(mTestCase, By.id("__attr2dateRange"), "1");
            // set email address
            Utils.DoClick(mTestCase, By.id("EmailEvents_desc_toggle"), null, 100);
            Utils.DoType(mTestCase, By.id("txtEEAddress1"), mUserAddress);
            Utils.SelectOptionByText(mTestCase, By.id("lstEEDir"), mParameters.get("eeto"));

        } 
        else if (i == 6) 
        {
            // change date range to 2 days
            Utils.DoType(mTestCase, By.id("__attr2dateRange"), "2");

        } else {
            mTestRunner.WriteLog("ERROR: CustomizeSearch() - No test is designed for iteration " + i, LogFile.iERROR);
        }
    }

    private void CheckParameters(int i) throws Exception
    {
        mTestRunner.NewLogFileGroup("Checking the parameters used in the search:", LogFile.iINFO);
        String params = Utils.GetSearchParams(mTestCase);
        //mTestRunner.WriteLog(params, LogFile.iDEBUG); // debug
        if (i == 0)
        {
            // all events, all dates
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "2;1;2;1");
            Utils.VerifySearchParameter(mTestCase, params, "All Event Types", "value", "true");
        } 
        else if (i == 1) 
        {
            // All events, All dates for a user specified by Name Match
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "2;1;2;1");
            Utils.VerifySearchParameter(mTestCase, params, "All Event Types", "value", "true");
            Utils.VerifySearchParameter(mTestCase, params, "txtNameMatch", "value", mUserMatch);
        } 
        else if (i == 2) 
        {
            // All events, All dates for a user specified by Specific Match
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "2;1;2;1");
            Utils.VerifySearchParameter(mTestCase, params, "All Event Types", "value", "true");
            Utils.VerifySearchParameter(mTestCase, params, "txtSpecificMatch", "report_value", mUserMatch);
        } 
        else if (i == 3) 
        {
            // All e-mails 'Today'
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "2;1;2;2");
            Utils.VerifySearchParameter(mTestCase, params, "E-mail Events only", "value", "true");
        } 
        else if (i == 4) 
        {
            // All e-mails within 2 day range
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "1;2;1;1");
            Utils.VerifySearchParameter(mTestCase, params, "E-mail Events only", "value", "true");
        } 
        else if (i == 5) 
        {
            // All e-mails sent from a specified e-mail address within 1 day range
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "1;1;1;1");
            Utils.VerifySearchParameter(mTestCase, params, "E-mail Events only", "value", "true");
            Utils.VerifySearchParameter(mTestCase, params, "txtEEAddress1", "value", mUserAddress);
            Utils.VerifySearchParameter(mTestCase, params, "lstEEDir", "report_value", mParameters.get("eeto"));
        } 
        else if (i == 6) 
        {
            // All e-mails sent from a specified e-mail address within 2 day range
            Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "1;2;1;1");
            Utils.VerifySearchParameter(mTestCase, params, "E-mail Events only", "value", "true");
            Utils.VerifySearchParameter(mTestCase, params, "txtEEAddress1", "value", mUserAddress);
            Utils.VerifySearchParameter(mTestCase, params, "lstEEDir", "report_value", mParameters.get("eeto"));
        } 
        else 
        {
            mTestRunner.WriteLog("ERROR: CheckParameters() - No test is designed for iteration " + i, LogFile.iERROR);
        }
        mTestRunner.CloseLogFileGroup("Checking search parameters completed", LogFile.iINFO);
    }

}
