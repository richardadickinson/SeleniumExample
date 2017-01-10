//Copyright © 2012-13 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * test script for the Home Page wizard 
 * 05/10/2012
 **/

//import java.util.*;
import common.*;
import org.openqa.selenium.*;


public class HomePageWizard extends WgnTestStep
{
    
    @Override
    public void Run() throws Exception
    {
        Utils.DoClick(mTestCase, By.id("tab_homepage"), By.id("myPortal"), 5000);
        findDefPortlets();
        Utils.DoClick(mTestCase, By.id("lnk_customize"), By.id("Homepage.CustomizeDialog"), 5000);
        checkDefAllowedPortlets();
    }
    
    private void findDefPortlets()
    {
        Boolean allPresent = true;
        String notHere = "";
        if (!Utils.isElementPresent(mTestCase, By.id("#ReviewerFavorites")))
        {
            allPresent = false;
            notHere += "ReviewerFavorites ";
        }
        if (!Utils.isElementPresent(mTestCase, By.id("#ReviewerIncidents")))
        {
            allPresent = false;
            notHere += "#ReviewerIncidents ";
        }
        if (!Utils.isElementPresent(mTestCase, By.id("#ReviewerMessage")))
        {
            allPresent = false;
            notHere += "#ReviewerMessage ";
        }
        if (!Utils.isElementPresent(mTestCase, By.id("#ReviewerTrend")))
        {
            allPresent = false;
            notHere += "#ReviewerTrend ";
        }
        if (!Utils.isElementPresent(mTestCase, By.id("#ReviewerAdmin")))
        {
            allPresent = false;
            notHere += "#ReviewerAdmin";
        }
        if (allPresent)
        {
            mTestRunner.WriteLog("PASS: The expected 5 default portlets are present", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The expected 5 default portlets are NOT present", LogFile.iERROR);
            mTestRunner.WriteLog("    : The missing ids are: " + notHere, LogFile.iINFO);
        }
    }
    
    private void checkDefAllowedPortlets()
    {
        
    }
}
