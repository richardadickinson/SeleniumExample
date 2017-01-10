
package testcases;

/**
 *
 * @author dicri02
 */
import common.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class AdminDerivedSearches extends WgnTestStep
{
    // variables for the test
    private String StdSrchName, StdSHash, drvSrchName, drvSrchDesc, drvSHash;  // search names and hashes
    private String pol1, pol2, pol3;  // policy names
    private String as2Esc, colEvAsc, colSubAsc, colFromDesc;   // search parameters
    private String TAB_ADMIN, DLG_CUSTOMIZE, TTL_SEARCH_PUBL, CHK_SEARCH_PUBL, TOG_FOLDER_PUBL, BTN_ACTION_PUBL, TTL_RUN_PUBL,
            LNK_LOGOFF, BTN_CUST_SAVE, DLG_PICKER, CXT_POLICY, BTN_POLICY_CXT, CXT_POLICY_SHOWALL, BTN_POLICY_SHOWALL, CHK_RESULTS_ROW;
    private boolean bContinue;
    
    public AdminDerivedSearches() 
    { 
        bContinue = false;
    }

    @Override
    public void Run() throws Exception
    {
        SetParameters();
        mTestRunner.NewLogFileGroup("Derived Search tests started", LogFile.iINFO);
        // make sure we are using the primary login if this test step followed another
        if (!Utils.isElementPresent(mTestCase, By.id(LNK_LOGOFF), false))
        {
            Utils.Login(mTestCase, "primary");
        }
        // go to the Search Manage page and go to Edit Properties for the Std Search
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(CHK_SEARCH_PUBL+StdSHash), 10000);
        
        bContinue = configureSearch();

        if (bContinue) bContinue = updateSearch(); // revisit the Derived search and save it again - check for overwrite popup

        if (bContinue)
        {
            // load the search on the Review tab and check the parameters were correctly saved
            checkParamsSaved();
            // run the derived search and then check it has applied the correct parameters
            runSearch();
            // finally delete the derived search (wgncleanup() will catch it if there have been problems)
            deleteSearch();
        }
        mTestRunner.CloseLogFileGroup("Derived Search tests completed", LogFile.iINFO);
    }
    
    private void SetParameters()
    {
        // get all localisable parameters for the test
        StdSrchName = Utils.SetParam(mTestCase, "StdSrchName");
        StdSHash = Utils.SetParam(mTestCase, "StdSrchHash");
        drvSrchName = Utils.SetParam(mTestCase, "drvSrchName");
        drvSrchDesc = Utils.SetParam(mTestCase, "drvSrchDesc");
        drvSHash = Utils.SetParam(mTestCase, "myDerivedSrchHash");
        pol1 = Utils.SetParam(mTestCase, "polInappLang");
        pol2 = Utils.SetParam(mTestCase, "polOffRelRom");
        pol3 = Utils.SetParam(mTestCase, "polSocNetwk");
        as2Esc = Utils.SetParam(mTestCase, "as2Escalate");
        colEvAsc = Utils.SetParam(mTestCase, "colEvType") + Utils.SetParam(mTestCase, "colAsc");
        colSubAsc = Utils.SetParam(mTestCase, "colSubject") + Utils.SetParam(mTestCase, "colAsc");
        colFromDesc = Utils.SetParam(mTestCase, "colFrom") + Utils.SetParam(mTestCase, "colDesc");
        // common GUI controls
        TAB_ADMIN = Utils.SetParam(mTestCase, "TAB_ADMIN"); //"tab_manage";
        DLG_CUSTOMIZE = Utils.SetParam(mTestCase, "DLG_CUSTOMIZE");
        BTN_CUST_SAVE = Utils.SetParam(mTestCase, "BTN_CUST_SAVE");
        TTL_SEARCH_PUBL = Utils.SetParam(mTestCase, "TTL_SEARCH_PUBL");
	TOG_FOLDER_PUBL = Utils.SetParam(mTestCase, "TOG_FOLDER_PUBL");
	CHK_SEARCH_PUBL = Utils.SetParam(mTestCase, "CHK_SEARCH_PUBL");
        BTN_ACTION_PUBL = Utils.SetParam(mTestCase, "BTN_ACTION_PUBL");
        TTL_RUN_PUBL = Utils.SetParam(mTestCase, "TTL_RUN_PUBL"); //"treeTitle_Run.published.";
        LNK_LOGOFF = Utils.SetParam(mTestCase, "LNK_LOGOFF");
        DLG_PICKER = Utils.SetParam(mTestCase, "DLG_PICKER");
        
        // to move to XML
        BTN_POLICY_CXT = "toolbar_rightDD";
        CXT_POLICY = "MIR_ContextMenu_ToolBarMore__attr1lstPolicyClass";
        CXT_POLICY_SHOWALL = "cxt_ctxt_Nav.ShowAll";
        BTN_POLICY_SHOWALL = "btn___attr1lstPolicyClass.Nav.ShowAll";
        CHK_RESULTS_ROW = "chk_Results.Dlg100";
    }
    
    private Boolean configureSearch() throws Exception
    {
        Boolean success = false;
        mTestRunner.NewLogFileGroup("Creating the Derived Search", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id(TTL_SEARCH_PUBL+StdSHash), By.id(DLG_CUSTOMIZE), 15000);
        if ( Utils.isElementPresent(mTestCase, By.id(DLG_CUSTOMIZE), false) && Utils.isTextPresent(mTestCase, StdSrchName) )
        {
            // change some parameters
            Utils.DoClick(mTestCase, By.id("__attr1dateRange1"), null, 100);
            Utils.SelectOptionByValue(mTestCase, By.id("__attr4dateRange"), "9"); // predefined date of 'This Year'
            Utils.DoClick(mTestCase, By.id("PrintEvents_desc_toggle"), By.id("txtPrintServer"), 1000);
            Utils.DoType(mTestCase, By.id("txtPrintServer"), "myTestServer");                // Print event server name
            Utils.DoClick(mTestCase, By.id("tab_1"), By.id("txtSmartTag"), 2000); // go to Incidents tab
            Utils.DoClick(mTestCase, By.id("btnlstPolicyClass"), By.id(DLG_PICKER), 10000);   // open the Policy Picker
            // Show All option can be hidden on a '>>' dropdown menu 
            if (Utils.isElementPresent(mTestCase, By.id(CXT_POLICY),false))
            {
                Utils.DoClick(mTestCase, By.id(BTN_POLICY_CXT), null, 250);
                Utils.DoClick(mTestCase, By.id(CXT_POLICY_SHOWALL), By.id(CHK_RESULTS_ROW), 20000);                
            } else {
                Utils.DoClick(mTestCase, By.id(BTN_POLICY_SHOWALL), By.id(CHK_RESULTS_ROW), 20000);
            }
            // select some policies by name from the picker and store their text
            Utils.DoClick(mTestCase, By.xpath("//span[contains(text(), '"+pol1+"')]"), null, 500);
            Utils.DoClick(mTestCase, By.xpath("//span[contains(text(), '"+pol2+"')]"), null, 500);
            Utils.DoClick(mTestCase, By.xpath("//span[contains(text(), '"+pol3+"')]"), null, 500);
            Utils.DoClick(mTestCase, By.id("btn___attr1lstPolicyClass.Action.0"), null, 100);
            Utils.DoClick(mTestCase, By.id("btn_Picker.OK"), null, 100);

            Utils.DoClick(mTestCase, By.id("chkHW"), null, 100);  // Heeded Warning checkbox
            Utils.DoClick(mTestCase, By.id("tab_2"), By.id("chkME"), 2000); // go to Identity tab
            Utils.SelectOptionByValue(mTestCase, By.id("lstUG"), "2");      // select Groups
            Utils.DoType(mTestCase, By.id("txtNameMatch"), "patsy");  // Set Group Name match to Patsy
            Utils.DoClick(mTestCase, By.id("tab_3"), By.id("ReviewAttrs"), 2000); // go to Audit tab
            Utils.SelectOptionByValue(mTestCase, By.id("lstAS2"), "5");  // Select Audit field 2 - Escalate to Management

            // save it as "Derived Search Test"
            Utils.DoClick(mTestCase, By.id(BTN_CUST_SAVE),  By.id("SaveSearch"), 5000);
            Utils.DoType(mTestCase, By.id("txtName"), drvSrchName);
            Utils.DoType(mTestCase, By.id("txtDesc"), "test");
            Utils.DoClick(mTestCase, By.id("btn_SaveSearch.OK"), By.id("RestrictResources"), 15000);
            // now set the new search as allowed to be run by all user roles if necessary                
            if (Utils.isElementPresent(mTestCase, By.id("RestrictResources"), false))
            {
                Utils.DoClick(mTestCase, By.id("btn_RestrictResources.OK"), null, 500, false);
            }
            if (Utils.isElementPresent(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), false))
            {
                Utils.DoClick(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), null, 500);
            }
            int i = 0; while (!Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+drvSHash), false) && i<5) { Thread.sleep(1000); i++; }
            if ( Utils.isElementPresent(mTestCase, By.id(CHK_SEARCH_PUBL+drvSHash), false) )
            {
                mTestRunner.WriteLog("PASS: Derived version of Std Search successfully saved.", LogFile.iTEST);
                success = true;
            } else {
                mTestRunner.WriteLog("FAIL: Expected Derived version of Std Search not present.", LogFile.iERROR);
            }
        }
        mTestRunner.CloseLogFileGroup("Search Creation stage complete", LogFile.iINFO);
        return success;
    }
    
    private Boolean updateSearch() throws Exception
    {
        Boolean success = true;
        mTestRunner.NewLogFileGroup("Updating the new Derived Search", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id(TTL_SEARCH_PUBL+drvSHash), By.id(DLG_CUSTOMIZE), 15000);

        if (Utils.isElementPresent(mTestCase, By.id(DLG_CUSTOMIZE), false) && Utils.isTextPresent(mTestCase, drvSrchName) )
        {
            Utils.DoType(mTestCase, By.id("txtSubject"), "Bozo");
            // Set Display Sort order on Settings tab
            Utils.DoClick(mTestCase, By.id("tab_4"), By.id("__chkOptColumnevent_type"), 2000); // go to Settings tab
            Utils.SelectOptionByValue(mTestCase, By.id("__lstSortColumn_available"), "2");     // Select Event Type
            executeJS4Chrome();
            Utils.DoClick(mTestCase, By.id("btn___lstSortColumn.Copy.0"), null, 100);          // click asc button
            Utils.SelectOptionByValue(mTestCase, By.id("__lstSortColumn_available"), "4");     // Select Subject/Filename
            executeJS4Chrome();
            Utils.DoClick(mTestCase, By.id("btn___lstSortColumn.Copy.0"), null, 100);          // click asc button
            Utils.SelectOptionByValue(mTestCase, By.id("__lstSortColumn_available"), "6");     // Select From/User/Host
            executeJS4Chrome();
            Utils.DoClick(mTestCase, By.id("btn___lstSortColumn.Copy.1"), null, 100);          // click desc button

            // Save the derived search again
            Utils.DoClick(mTestCase, By.id(BTN_CUST_SAVE), By.id("SaveSearch"), 5000);
            Utils.DoType(mTestCase, By.id("txtDesc"), drvSrchDesc);
            // Make the desc multi-line
            Utils.sendKeys(mTestCase, By.id("txtDesc"), Keys.ENTER.toString());
            Utils.sendKeys(mTestCase, By.id("txtDesc"), "1");
            Utils.sendKeys(mTestCase, By.id("txtDesc"), Keys.ENTER.toString());
            Utils.sendKeys(mTestCase, By.id("txtDesc"), "2");
            Utils.sendKeys(mTestCase, By.id("txtDesc"), Keys.ENTER.toString());

            Utils.DoClick(mTestCase, By.id("btn_SaveSearch.OK"), By.id("Message"), 5000);
            Utils.DoClick(mTestCase, By.id("btn_Message.OK"), null, 100);
            int i = 0; while (Utils.isElementPresent(mTestCase, By.id(DLG_CUSTOMIZE), false) && i < 10) { Thread.sleep(500); i++; }
            Thread.sleep(1000);
            Utils.DoClick(mTestCase, By.id("tab_searches"), By.id("tab_searches.Reports"), 10000);
            String descTest = Utils.GetText(mTestCase, By.xpath("//a[@id='"+TTL_RUN_PUBL+drvSHash+"']/span"),false);
            if ( Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL+drvSHash), false) && descTest.contains(drvSrchDesc) )
            {
                mTestRunner.WriteLog("PASS: Derived version of Std Search successfully overwritten.", LogFile.iTEST);
                int lineCount = Utils.lineCount(descTest);
                if ( lineCount > 1)
                {
                    mTestRunner.WriteLog("PASS: Derived search description was saved multiline (No. of lines = " + lineCount + ")", LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("FAIL: Derived search description was not saved multiline (No. of lines = " + lineCount + ")", LogFile.iERROR);
                }
            } else {
                mTestRunner.ReportError("FAIL: Expected updated Derived version of Std Search not present.");
                //mTestRunner.WriteLog("FAIL: Expected updated Derived version of Std Search not present.", LogFile.iERROR);
                success = false;
            }
        } else {
            success = false;
            if (Utils.isElementPresent(mTestCase, By.id("btn_Customize.smlClose"), false))
            {
                Utils.DoClick(mTestCase, By.id("btn_Customize.smlClose"), null, 500);
            }
        }
        mTestRunner.CloseLogFileGroup("Derived search update stage complete", LogFile.iINFO);
        return success;
    }
    
    private void executeJS4Chrome() throws Exception
    {
        if (mTestRunner.GetTargetBrowser().contains("chrome"))
        {
            // the chromedriver fails to trigger the js called by the onchange event when a multiselect option is selected
            // so we must do it manually |:-\            
            String myjs = "var el = document.getElementById('__lstSortColumn_available'); el.control.UpdateButtons.call(el.control);"; 
            Utils.ExecuteJavaScript(mTestCase, myjs);
        }
    }
    
    private void checkParamsSaved() throws Exception
    {
        mTestRunner.NewLogFileGroup("Confirm that the parameters have all been saved into the new search", LogFile.iINFO);
        // check the parameters were stored correctly in the derived search
        Utils.DoClick(mTestCase, By.id("btn_Favorite.published."+drvSHash), null, 250);
        Utils.DoClick(mTestCase, By.id("cxt_ActionEdit"), By.id(DLG_CUSTOMIZE), 10000);
        
        if (Utils.isElementPresent(mTestCase, By.id(DLG_CUSTOMIZE), false))
        {
            boolean bParams = true;
            if (!Utils.GetAttribute(mTestCase, By.id("txtSubject"), "value", false).matches("Bozo") 
                    || !Utils.isSelected(mTestCase, By.id("__attr1dateRange1"))
                    || Utils.GetSelectedValueAsInt(mTestCase, By.id("__attr4dateRange")) != 9 ) bParams = false;                        
            mTestRunner.WriteLog("Date Range & Subject: " + bParams, LogFile.iDEBUG);

            Utils.DoClick(mTestCase, By.id("PrintEvents_desc_toggle"), null, 100);
            if (!Utils.GetAttribute(mTestCase, By.id("txtPrintServer"), "value", false).matches("myTestServer")) bParams = false;
            mTestRunner.WriteLog("Print Server: " + bParams, LogFile.iDEBUG);

            // check that the Incidents tab params were saved correctly in the search
            Utils.DoClick(mTestCase, By.id("tab_1"), By.id("txtSmartTag"), 2000);
            if (!Utils.isSelected(mTestCase, By.id("chkHW"))) bParams = false;  // Heeded warning checkbox
            mTestRunner.WriteLog("Heeded Warning: " + bParams, LogFile.iDEBUG);

            // get the text string of the selected triggers and checks that they match the expected list entries
            String aItems = Utils.GetText(mTestCase, By.id("__attr1lstPolicyClass"), false);
            if (!aItems.contains(pol1) || !aItems.contains(pol2) || !aItems.contains(pol3) ) bParams = false;
            mTestRunner.WriteLog("Policy text: " + bParams, LogFile.iDEBUG);

            // Identity tab
            Utils.DoClick(mTestCase, By.id("tab_2"), By.id("lstUG"), 2000); // go to Identity tab
            String slabel = Utils.GetSelectedLabel(mTestCase, By.id("lstUG"));
            String svalue = Utils.GetAttribute(mTestCase, By.id("txtNameMatch"), "value", false); 
            if (!slabel.matches("Group") || !svalue.matches("patsy")) bParams = false;
            mTestRunner.WriteLog("Group name match: " + bParams + " <" + svalue + "> <" + slabel + ">", LogFile.iDEBUG);

            // Audit tab
            Utils.DoClick(mTestCase, By.id("tab_3"), By.id("ReviewAttrs"), 2000); // go to Audit tab
            if (!Utils.GetSelectedLabel(mTestCase, By.id("lstAS2")).matches(as2Esc)) bParams = false;
            mTestRunner.WriteLog("Audit Field 2: " + bParams, LogFile.iDEBUG);
            //mTestRunner.WriteLog(Utils.GetSelectedLabel(mTestCase, By.id("lstAS2")), LogFile.iDEBUG); //debug

            // get the array of items from the display sort order and check that they match the expected list entries
            Utils.DoClick(mTestCase, By.id("tab_4"), By.id("__chkOptColumnevent_type"), 2000); // go to Settings tab
            String colItems[] = Utils.GetSelectOptions(mTestCase, By.id("__lstSortColumn"));
            if (!colItems[0].equals(colEvAsc) || !colItems[1].equals(colSubAsc) || !colItems[2].equals(colFromDesc)) bParams = false;
            mTestRunner.WriteLog("Display Sort Order: " + bParams, LogFile.iDEBUG);
            mTestRunner.WriteLog("0: " + colItems[0] + ", 1: " + colItems[1] + ", 2: " + colItems[2], LogFile.iDEBUG);

            if (bParams)
            {
                mTestRunner.WriteLog("PASS: Derived search parameter values saved correctly.", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: Derived search parameter values not saved correctly.", LogFile.iERROR);
            }
        }
        mTestRunner.CloseLogFileGroup("Parameter checking complete", LogFile.iINFO);
    }
    
    private void runSearch() throws Exception
    {
        String parentHandle = Utils.GetNewWindow(mTestCase, By.id("btn_Customize.Run"));
        int i = 0; while (!Utils.isElementPresent(mTestCase, By.id("divResultsTable"), false) && i < 60 ) { Thread.sleep(1000); i++; }
        if (Utils.isElementPresent(mTestCase, By.id("divResultsTable"), false) ) 
        {
           // validate the parameters used in the search
           mTestRunner.NewLogFileGroup("Validating the parameters used when running the derived search:", LogFile.iINFO);
           String params = Utils.GetSearchParams(mTestCase);
           Utils.VerifySearchParameter(mTestCase, params, "dateRange", "attrs", "2;1;2;9"); // This Year
           Utils.VerifySearchParameter(mTestCase, params, "txtSubject", "value", "Bozo");
           Utils.VerifySearchParameter(mTestCase, params, "txtPrintServer", "value", "myTestServer");
           Utils.VerifySearchParameter(mTestCase, params, "chkHW", "value", "true");
           Utils.VerifySearchParameter(mTestCase, params, "lstPolicyClass", "report_value", pol1+";"+pol2+";"+pol3);
           Utils.VerifySearchParameter(mTestCase, params, "lstUG", "report_value", "Group");
           Utils.VerifySearchParameter(mTestCase, params, "txtNameMatch", "value", "patsy");
           Utils.VerifySearchParameter(mTestCase, params, "lstAS2", "report_value", as2Esc);
           mTestRunner.CloseLogFileGroup("Search Parameter validation complete", LogFile.iINFO);
        }
        Utils.CloseWindow(mTestCase, parentHandle);
        Thread.sleep(500);
    }
    
    private void deleteSearch() throws Exception
    {
        mTestRunner.NewLogFileGroup("Deleting derived search", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id(TAB_ADMIN), By.id(BTN_ACTION_PUBL+drvSHash), 5000);
        Utils.DoClick(mTestCase, By.id(TOG_FOLDER_PUBL+StdSHash), null, 100);
        if (Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+drvSHash), false) )
        {
           Utils.DoClick(mTestCase, By.id(BTN_ACTION_PUBL+drvSHash), null, 100);
           Utils.DoClick(mTestCase, By.id("cxt_ActionUninstall"), By.id("Message"), 5000);
           Utils.DoClick(mTestCase, By.id("btn_Message.OK"), null, 10);
           int i = 0; while (Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+drvSHash), false) && i < 20) { Thread.sleep(500); i++; }
           if (!Utils.isElementPresent(mTestCase, By.id(BTN_ACTION_PUBL+drvSHash), false) )
           {
               mTestRunner.WriteLog("PASS: Derived search deleted", LogFile.iTEST);
           } else {
               mTestRunner.WriteLog("FAIL: Derived search NOT deleted!", LogFile.iERROR);
           }
        } else {
           mTestRunner.WriteLog("WARNING: Where has the derived search gone? I didn't delete it!", LogFile.iINFO);
        }
        mTestRunner.CloseLogFileGroup("Derived search delete stage complete", LogFile.iINFO);
    }

}
