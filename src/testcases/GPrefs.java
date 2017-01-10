//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import common.*;
import org.openqa.selenium.*;
import org.w3c.dom.*;
import java.util.*;

public class GPrefs extends WgnTestStep
{
    protected DataReader mGlobalPrefs;
    protected DataReader mLocalPrefs;
    private String parentHandle;
    private Map<String, String> params;
    private Map<String, String> localparams;
    private String sTAB_MANAGE, sCXT_MANAGE, sTAB_SEARCHES;
    private String sDLG_EDIT, sBTN_EDIT, sBTN_EDITSEARCHES, sBTN_EDITPORTTYPE;
    private String sTXT_PAGESIZE, sCHK_ALLOWDEF, sTREE_TYPES, sTREE_SEARCHES, sTREE_DASHB, sTREE_REPORTS;
    private String sBTN_EDIT_OK, sBTN_EDIT_RESET; //, sBTN_EDIT_XCLOSE;
    private String sBTN_EDITPORTLETS, sTREE_PORTLETS;
    private String sBTN_LOCAL_OK, sLNK_LOCAL_PREFS, sDLG_PREFS;    
    private String sBTN_LOCAL_RESET, sBTN_LOCAL_YES, sBTN_LOCAL_XCLOSE;
    private String sTBL_RESULTS, sStdSrchHash, testPageSize;

    public GPrefs() 
    { 
        mGlobalPrefs = new DataReader();
        mLocalPrefs = new DataReader();
    }

    @Override
    public void Run() throws Exception
    {
        // load the prefs from the DataStructure XML
        mTestRunner.WriteLog("Data Structure file: " + mParameters.get("dataStructure"), LogFile.iDEBUG);
        Boolean bCfg = mGlobalPrefs.Load(mParameters.get("dataStructure"));
        if (bCfg) mLocalPrefs.Load(mParameters.get("localPrefsData"));

        if (bCfg)
        {
            setParameters();        
            setLocalParameters();
            // ensure the login's local prefs aren't overriding the global prefs
            forceLocalPrefsToDefault(true);
            // Open Global Prefs & verify the defaults
            NodeList dialogs = mGlobalPrefs.GetNodesByName("DIALOG");
            if (dialogs != null && dialogs.getLength() > 0)
            {
                mTestRunner.NewLogFileGroup("Default verification for Resources.", LogFile.iINFO);
                traverseXML(dialogs, mGlobalPrefs.POPULATION);                
                mTestRunner.CloseLogFileGroup("Default verification tests completed.", LogFile.iINFO);
                // check the loaded session defaults match the Global Prefs
                // *** admin login must have default local prefs for this to work ***
                verifySessionPrefs("default", dialogs);
                
                // Test the Save button
                testSaveChanges(dialogs);

                // restore factory defaults with Reset button
                testResetButton(dialogs);                

                // check the application of the search results page size field
                testSearchResultsPageSize();
            }
        } else {
            mTestRunner.WriteLog("ERROR: Failed to load data structures from XML", LogFile.iERROR);
        }
        Utils.Logoff(mTestCase);
    }
    
    // get parameters from data structures
    private void setParameters() 
    {
        mGlobalPrefs.SetParameters(mParameters);
        sStdSrchHash = Utils.SetParam(mTestCase, "TTL_RUN_PUBL") + Utils.SetParam(mTestCase, "StdSrchHash");
        params = mGlobalPrefs.GetParamTable();
        mGlobalPrefs.DebugHashMap(mTestCase, params);        
        sTAB_MANAGE = params.get("TAB_MANAGE_DD");
        sCXT_MANAGE = params.get("CXT_ROLES");
        sBTN_EDIT = params.get("BTN_EDITSETTINGS");
        sDLG_EDIT = Utils.SetParam(mTestCase, "DLG_EDITRES");
        sTAB_SEARCHES = Utils.SetParam(mTestCase, "TAB_REVIEW");
        sBTN_EDIT_OK = Utils.SetParam(mTestCase, "BTN_EDITRES_OK");
        sBTN_EDIT_RESET = Utils.SetParam(mTestCase, "BTN_EDITRES_RESET");
//        sBTN_EDIT_XCLOSE = Utils.SetParam(mTestCase, "BTN_EDITRES_XCLOSE");
        sTBL_RESULTS = Utils.SetParam(mTestCase, "TBL_RESULTS");
        testPageSize = "15";
        sTXT_PAGESIZE = params.get("TXT_PAGESIZE");
	sCHK_ALLOWDEF = params.get("CHK_ALLOWDEF");
	sBTN_EDITPORTTYPE = params.get("BTN_EDITPORTTYPE");
	sTREE_TYPES = params.get("TREE_TYPES");
	sBTN_EDITSEARCHES = params.get("BTN_EDITSEARCHES");
	sTREE_SEARCHES = params.get("TREE_SEARCHES");
	sTREE_DASHB = params.get("TREE_DASHB");
	sTREE_REPORTS = params.get("TREE_REPORTS");
        sBTN_EDITPORTLETS = params.get("BTN_EDITPORTLETS"); 
        sTREE_PORTLETS = params.get("TREE_PORTLETS");
    }
    private void setLocalParameters() 
    {   
        localparams = mLocalPrefs.GetParamTable();        
        mLocalPrefs.DebugHashMap(mTestCase, localparams);                
        sLNK_LOCAL_PREFS = mLocalPrefs.GetAttributeValue(mLocalPrefs.GetNodesByName("PREFS").item(0), "clickid");
        sDLG_PREFS = mLocalPrefs.GetAttributeValue(mLocalPrefs.GetNodesByName("PREFS").item(0), "checkid");
        sBTN_LOCAL_OK = localparams.get("BTN_PREFS_OK");
        sBTN_LOCAL_RESET = localparams.get("BTN_PREFS_RESET");
        sBTN_LOCAL_YES = localparams.get("BTN_MESSAGE_YES");
        sBTN_LOCAL_XCLOSE = localparams.get("BTN_PREFS_XCLOSE");
    }
    
    /*
     * Test Methods
     */
    // got to the Administration tab, Resources subtab
    private void goToResources() throws Exception
    {
        Utils.DoClick(mTestCase, By.id(sTAB_MANAGE), null, 250);
        Utils.DoClick(mTestCase, By.id(sCXT_MANAGE), By.id(sBTN_EDIT), 5000);
    }
    // Open the Edit Resources dialog with the passed in Edit button value
    private void openEditResources(String button) throws Exception
    {
        Utils.DoClick(mTestCase, By.id(button), By.id(sDLG_EDIT), 5000);
    }
    // traverse the ResourcesForRoles.xml which defines the Resources GUI under test
    // and pass the nodes in the XML for processing by the DataReader
    private void traverseXML(NodeList nodelist, String valType) throws Exception
    {
        int testDlgs = nodelist.getLength();
        // only do the save and restoration tests for the Edit_Settings dialog
        if (valType.contentEquals(mGlobalPrefs.NOPOPULATION))
        {
            testDlgs = 1;
        }
        for (int index=0; index < testDlgs; index++)
        {
            Node node = nodelist.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                String sButton = mGlobalPrefs.GetAttributeValue(node, "clickid");
                String sLabel = mGlobalPrefs.GetAttributeValue(node, "label");
                mTestRunner.NewLogFileGroup("Testing the " + sLabel + " dialog", LogFile.iINFO);
                
                // the Edit Portlet Types section only appears when AllowPortletDefinition is 'true'
                if (sButton.contentEquals(sBTN_EDITPORTTYPE))
                {
                    openEditResources(sBTN_EDIT);
                    Utils.DoClick(mTestCase, By.id("tab_1"), By.id(sCHK_ALLOWDEF), 5000);                    
                    if (!Utils.isSelected(mTestCase, By.id(sCHK_ALLOWDEF)))
                    {                
                        Utils.DoClick(mTestCase, By.id(sCHK_ALLOWDEF), null, 250);
                    }
                    saveAndClose();
                    int i=0; while (!Utils.isElementPresent(mTestCase, By.id(sButton), false) && i < 10) { Thread.sleep(500); i++; }
                }
                
                openEditResources(sButton);
                NodeList items = node.getChildNodes();
                mGlobalPrefs.processNodeTypes(mTestCase, items, sDLG_EDIT, valType);
                saveAndClose();
                
                mTestRunner.CloseLogFileGroup("Completed testing the " + sLabel + " dialog", LogFile.iINFO);
            }
        }
    }
    // close and save changes in the Edit Resources dialog
    private void saveAndClose() throws Exception
    {
        Utils.DoClick(mTestCase, By.id(sBTN_EDIT_OK), null, 100);
        int i= 0; while ( Utils.isElementPresent(mTestCase, By.id(sDLG_EDIT), false) && i < 10 ) { Thread.sleep(500); i++; }
    }
    
    // the preference values were changed as part of processNodeTypes() POPULATION routine in the 
    // default checking step - Verify the changes were saved ok
    private void testSaveChanges(NodeList dialogs) throws Exception
    {
        mTestRunner.NewLogFileGroup("Save changes tests for Global Prefs:", LogFile.iINFO);
        Utils.Logoff(mTestCase);
        Utils.Login(mTestCase, "primary");
        goToResources();
        traverseXML(dialogs, mGlobalPrefs.NORESTORATION);        
        verifySessionPrefs("saved", dialogs);        
        verifyIsEnforced();

        mTestRunner.CloseLogFileGroup("Save change tests completed for Global Prefs", LogFile.iINFO);
    }

    // verify that Reset button restores factory defaults to the Edit Settings dialog
    // also restore factory defaults for other dialogs here (so subsequent test cases will work)
    private void testResetButton(NodeList dialogs) throws Exception
    {
        // restore the available portlet and search lists to defaults
        // there are no reset buttons for this so just do it manually
        openEditResources(sBTN_EDITPORTTYPE); // do this BEFORE using Reset or this Edit button will disappear
        if (!Utils.isSelected(mTestCase, By.id(sTREE_TYPES)))
        {
            Utils.DoClick(mTestCase, By.id(sTREE_TYPES), null, 250);
        }
        saveAndClose();
   
        // restore searches and reports to Review tab
        openEditResources(sBTN_EDITSEARCHES);
        Thread.sleep(500);
        if (!Utils.isSelected(mTestCase, By.id(sTREE_SEARCHES)))
        {
            Utils.DoClick(mTestCase, By.id(sTREE_SEARCHES), null, 250);
        }
        Utils.DoClick(mTestCase, By.id("tab_1"), By.id(sTREE_DASHB), 5000);
        if (!Utils.isSelected(mTestCase, By.id(sTREE_DASHB)))
        {
            Utils.DoClick(mTestCase, By.id(sTREE_DASHB), null, 250);
        }
        Utils.DoClick(mTestCase, By.id("tab_2"), By.id(sTREE_REPORTS), 5000);
        if (!Utils.isSelected(mTestCase, By.id(sTREE_REPORTS)))
        {
            Utils.DoClick(mTestCase, By.id(sTREE_REPORTS), null, 250);
        }        
        saveAndClose();
        
        // restore portlets to Home Page
        openEditResources(sBTN_EDITPORTLETS);
        Thread.sleep(500);
        if (!Utils.isSelected(mTestCase, By.id(sTREE_PORTLETS)))
        {
            Utils.DoClick(mTestCase, By.id(sTREE_PORTLETS), null, 250);
        }
        saveAndClose();
        
        mTestRunner.NewLogFileGroup("Test default values are restored by the Reset button:", LogFile.iINFO);
        // open the Edit Settings dialog and reset it
        openEditResources(sBTN_EDIT);
        Utils.DoClick(mTestCase, By.id(sBTN_EDIT_RESET), null, 1000);
        Utils.DoClick(mTestCase, By.id(sBTN_EDIT_OK), null, 500);
        Utils.Logoff(mTestCase);
        // Login and verify Global Prefs were restored to defaults
        Utils.Login(mTestCase, "primary");
        goToResources();
        // first check the Edit button for Portlet types is hidden again
        if (!Utils.isElementPresent(mTestCase, By.id(sBTN_EDITPORTTYPE), false))
        {
            mTestRunner.WriteLog("PASS: The Edit button for Portlet types is hidden with default values", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The Edit button for Portlet types is NOT hidden with default values", LogFile.iERROR);
        }
        traverseXML(dialogs, mGlobalPrefs.NOPOPULATION);
        verifySessionPrefs("default", dialogs);
        mTestRunner.CloseLogFileGroup("Reset test complete", LogFile.iINFO);
        
    }

    // check that the search results page size value is applied by running a search
    private void testSearchResultsPageSize() throws Exception
    { 
        mTestRunner.NewLogFileGroup("Test the Search Results Page Size field:", LogFile.iINFO);
        openEditResources(sBTN_EDIT);
        Utils.DoClick(mTestCase, By.id("tab_3"), By.id(sTXT_PAGESIZE), 5000);                    
        Utils.PlainType(mTestCase, By.id(sTXT_PAGESIZE), "17");
        saveAndClose();
        // update the local prefs to pickup the new value
        forceLocalPrefsToDefault(false);
        // run the std search and get hold of the child window
        Utils.DoClick(mTestCase, By.id(sTAB_SEARCHES), By.id(sStdSrchHash), 500);
        parentHandle = Utils.GetNewWindow(mTestCase, By.id(sStdSrchHash));
        int i=0; while (!Utils.isElementPresent(mTestCase, By.id(sTBL_RESULTS), false) && i < 60) { Thread.sleep(500); i++; }

        // check the values of the page size and results returned
        String numSearchResults = (String) ((JavascriptExecutor) mDriver).executeScript("return oResultsManager.oResults.max_records;");
        String pageSize = (String) ((JavascriptExecutor) mDriver).executeScript("return oResultsManager.oResults.page_size;");
        if ( (pageSize == null || pageSize.isEmpty()) || (numSearchResults == null || numSearchResults.isEmpty() || numSearchResults.contentEquals("0")) )
        {
            ReportError("GPrefs.testSearchResultsPageSize() - one or both of expected attributes 'page_size' or 'max_records' is null or empty");
            mTestRunner.WriteLog("ERROR: Standard search returned 0 results or failed to run", LogFile.iINFO);
        } else {
            if (pageSize.contentEquals(testPageSize))
            {
                mTestRunner.WriteLog("PASS: The correct page size (" + pageSize + ") is set on the search results page", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: The page size is set to '" + pageSize + "' on the search results page -> Expected '" + testPageSize + "'", LogFile.iERROR);
            }
        }
        // return to the parent iConsole window
        Utils.CloseWindow(mTestCase, parentHandle);
        // restore default
        goToResources();
        openEditResources(sBTN_EDIT);
        Utils.DoClick(mTestCase, By.id(sBTN_EDIT_RESET), null, 1000);
        Utils.DoClick(mTestCase, By.id(sBTN_EDIT_OK), null, 500);
        mTestRunner.CloseLogFileGroup("Search Results Page Size field tests complete", LogFile.iINFO);
    }
    
    private void verifySessionPrefs(String mode, NodeList dialogs) throws Exception
    {
        for (int i=0; i < dialogs.getLength(); i++)
        {
            Node dialog = dialogs.item(i);
            if (dialog.getNodeType() == Node.ELEMENT_NODE)
            {
                String clickid = mGlobalPrefs.GetAttributeValue(dialog, "clickid");
                String checkid = mGlobalPrefs.GetAttributeValue(dialog, "checkid");                
                if (clickid.contains(sBTN_EDIT))
                {
                    Utils.DoClick(mTestCase, By.id(clickid), By.id(checkid), 500);
                    NodeList tabs = dialog.getChildNodes();
                    verifySessionSettingsPrefs(mode, tabs);
                    Utils.DoClick(mTestCase, By.id(sBTN_EDIT_OK), null, 500);
                }
                /* b2908 - this test needs updating. Old session value no longer used.
                 * need to use portlet wizard to see allowed list instead
                if (clickid.contains(sBTN_EDITPORTLETS))
                {
                    NodeList nodes = dialog.getChildNodes();
                    verifyPortletPrefs(mode, nodes);                           
                }
                 * 
                 */
            }
        }
    }

    // preferences to be applied for the logged in user are stored in the oInit (and oPrefs) DOM containers
    // retrieve their values with js and check they are set as expected
    private void verifySessionSettingsPrefs(String mode, NodeList nodes) throws Exception
    {
        mTestRunner.NewLogFileGroup("Check the loaded session prefs match the " + mode + " values:", LogFile.iINFO);
        
        for (int index=0; index < nodes.getLength(); index++)
        {
            Node node = nodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                String tabId = mGlobalPrefs.GetAttributeValue(node, "clickid");
                NodeList progeny = node.getChildNodes();
                for (int i=0; i < progeny.getLength(); i++)
                {
                    Node child = progeny.item(i);
                    String actual = "";
                    if (child.getNodeType() == Node.ELEMENT_NODE)
                    {                        
                        actual = "false";
                        String nodename = child.getNodeName();
                        if (nodename.contentEquals("SELECT")) actual = "0";
                        String clickid = mGlobalPrefs.GetAttributeValue(child, "clickid");
                        // session pref names match GUI Ids except for the case of 'e' on 'enforced'
                        String prefid = clickid.replace("_e", "_E");
                        String label = mGlobalPrefs.GetAttributeValue(child, "label");
                        String sTestval = mGlobalPrefs.GetAttributeValue(child, "default");
                        // get the test value for testSearchResultsPageSize() from the XML rather than rely on hard-coded default to be correct
                        if (prefid.contentEquals("SearchResultsPageSize")) testPageSize = mGlobalPrefs.GetAttributeValue(child, "testval");
                        // check we're on the right tab for the element under test
                        if (!Utils.isElementPresent(mTestCase, By.id(clickid), false) && !tabId.isEmpty())
                        {
                            Utils.DoClick(mTestCase, By.id(tabId), By.id(clickid), 5000);
                        }
                         // the pref is not created in the DOM if it is 'false' onpageload so do 'typeof' test first to avoid error
                        String sEval = "oInit.prefs." + prefid;
                        if (prefid.contentEquals("ContentProxy")) sEval = "oInit.prefs.ContentProxies[0].text";
                        String jsVal = (String) ((JavascriptExecutor) mDriver).executeScript("return typeof " + sEval + ";");
                        if (!jsVal.contentEquals("undefined") && jsVal != null)
                        {
                            if ( (prefid.startsWith("Display") && !prefid.endsWith("Enforced")) || prefid.contentEquals("ContentProxy"))
                            {
                                actual = (String) ((JavascriptExecutor) mDriver).executeScript("return " + sEval + ";");
                            } 
                            else if ( (prefid.startsWith("Search") && !prefid.endsWith("Enforced")) || prefid.contentEquals("lstLayout")) 
                            {
                                actual = ((Long) ((JavascriptExecutor) mDriver).executeScript("return " + sEval + ";")).toString();
                            } else {
                                actual = ((Boolean) ((JavascriptExecutor) mDriver).executeScript("return " + sEval + ";")).toString();
                            } 
                        }
                        if (mode.contains("saved"))
                        {
                            if (nodename.contentEquals("CHECKBOX"))
                            {
                                if (sTestval.contentEquals("on")) 
                                {
                                    sTestval = "off";
                                }
                                else if (sTestval.contentEquals("off"))
                                {
                                    sTestval = "on";
                                }
                            } 
                            else if (nodename.contentEquals("SELECT") && !prefid.contentEquals("ContentProxy")) 
                            {
                                sTestval = "0"; // hard-coded value == bad!
                            } else {
                                sTestval = mGlobalPrefs.GetAttributeValue(child, "testval");
                            }
                        } else {
                            if (nodename.contentEquals("SELECT")) sTestval = mGlobalPrefs.GetAttributeValue(child, "value");
                        }
                        if (sTestval.contentEquals("on")) sTestval = "true";
                        if (sTestval.contentEquals("off")) sTestval = "false";
                        Utils.CompareValues(mTestCase, actual, label, sTestval);
                    }
                }
            }
        }
        mTestRunner.CloseLogFileGroup("Session prefs " + mode + " checks complete.", LogFile.iINFO);
    }
    
    /* b2908 - the ExcludedPortletList session value is no longer used (other than for legacy on upgrade)
    // so need a new way to validate these settings - which will be use the portlet wizard
    private void verifyPortletPrefs(String mode, NodeList nodes) throws Exception
    {
        // check the excluded portlet list - they all use the same session pref so get it once then cycle through them
        mTestRunner.NewLogFileGroup("Check the " + mode + " portlet preferences are applied:", LogFile.iINFO);
        String actual = "";
        String jsVal = (String) ((JavascriptExecutor) mDriver).executeScript("return typeof oInit.prefs.ExcludedPortletList;");
        if (!jsVal.contentEquals("undefined") && jsVal != null)
        {
            // the pref is not created in the DOM if it is 'false' onpageload
            actual = (String) ((JavascriptExecutor) mDriver).executeScript("return oInit.prefs.ExcludedPortletList;");
        }
        for (int index=0; index < nodes.getLength(); index++)
        {
            Node node = nodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                String clickid = mGlobalPrefs.GetAttributeValue(node, "clickid");
                String label = mGlobalPrefs.GetAttributeValue(node, "label");
                String sTestval = mGlobalPrefs.GetAttributeValue(node, "default");
                String prefNames[] = clickid.split("_");
                String prefName = prefNames[1];
                if (mode.contains("default"))
                {
                    if ( (actual.contains(prefName) && sTestval.contentEquals("on"))
                            || ( !actual.contains(prefName) && sTestval.contentEquals("off")) )
                    {
                        mTestRunner.WriteLog("PASS: correct default applied for " + label, LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: incorrect default applied for " + label + " (Excluded portlist pref='"+actual+"')", LogFile.iERROR);
                    }
                } else {
                    if (sTestval.contentEquals("on"))
                    {
                        sTestval = "off";
                    } else {
                        sTestval = "on";
                    }
                    if ( (actual.contains(prefName) && sTestval.contentEquals("on"))
                            || ( !actual.contains(prefName) && sTestval.contentEquals("off")) )
                    {
                        mTestRunner.WriteLog("PASS: correct session preference applied for " + label + " ('" + prefName + "')", LogFile.iTEST);
                    } else {
                        mTestRunner.WriteLog("FAIL: incorrect session preference applied for " + label + " (Excluded portlist pref='" + actual + "')", LogFile.iERROR);
                    }
                }
            }
        }
        Utils.DoClick(mTestCase, By.id(sBTN_EDIT_XCLOSE), null, 500);
        mTestRunner.CloseLogFileGroup("Portlet preference use tests complete", LogFile.iINFO);
    }
     * 
     */

    // preferences applied at run time are a composite of the global settings and the logged in user's local settings
    // force the local prefs to defaults to ensure they match the global values that we're testing
    private void forceLocalPrefsToDefault(Boolean reload) throws Exception
    {
        Utils.DoClick(mTestCase, By.id(sLNK_LOCAL_PREFS), By.id(sDLG_PREFS), 10000);
        Utils.DoClick(mTestCase, By.id(sBTN_LOCAL_RESET), null, 10);
        Utils.DoClick(mTestCase, By.id(sBTN_LOCAL_YES), null, 10);
        Utils.DoClick(mTestCase, By.id(sBTN_LOCAL_XCLOSE), null, 100);
        if (reload)
        {
            // Logout and back in to reload session values in the DOM
            Utils.Logoff(mTestCase);
            Utils.Login(mTestCase, "primary");
            goToResources();
        }
    }

    // check that when the Global value is enforced that its local value is disabled in the Settings dialog 
    private void verifyIsEnforced() throws Exception
    { 
        // first load the localprefs.xml with DataReader to cycle through the user Settings dialog GUI
        NodeList localprefs = mLocalPrefs.GetNodesByName("PREFS");
        mTestRunner.NewLogFileGroup("Local Preference Enforcement tests:", LogFile.iINFO);
        for (int i=0; i < localprefs.getLength(); i++)
        {
            Node pref = localprefs.item(i);
            if (pref.getNodeType() == Node.ELEMENT_NODE)
            {
                String clickid = mLocalPrefs.GetAttributeValue(pref, "clickid");
                String checkid = mLocalPrefs.GetAttributeValue(pref, "checkid");
                Utils.DoClick(mTestCase, By.id(clickid), By.id(checkid), 5000);

                NodeList tabs = pref.getChildNodes();
                for (int k=0; k < tabs.getLength(); k++)
                {
                    Node tab = tabs.item(k);
                    if (tab.getNodeType() == Node.ELEMENT_NODE)
                    {
                        String tabid = mLocalPrefs.GetAttributeValue(tab, "clickid");
                        String tablabel = mLocalPrefs.GetAttributeValue(tab, "label");                        
                        mTestRunner.NewLogFileGroup("Checking Local Prefs " + tablabel + " tab:", LogFile.iINFO);
                        NodeList elements = tab.getChildNodes();
                        for (int m=0; m < elements.getLength(); m++)
                        {
                            Node element = elements.item(m);
                            if (element.getNodeType() == Node.ELEMENT_NODE)
                            {                          
                                String elementid = mLocalPrefs.GetAttributeValue(element, "clickid");
                                String elementlabel = mLocalPrefs.GetAttributeValue(element, "label");
                                if (!Utils.isElementPresent(mTestCase, By.id(elementid), false))
                                {
                                    Utils.DoClick(mTestCase, By.id(tabid), By.id(elementid), 5000);
                                }
                                if (!elementid.contentEquals("HighVis"))
                                {
                                    Boolean sPass = true;
                                    if (element.getNodeName().contentEquals("INTEGERBOX") )
                                    {
                                        String readonly = Utils.GetAttribute(mTestCase, By.id(elementid), "readonly", false);
                                        if (readonly == null || readonly.contentEquals("false")) sPass = false;                                    
                                    } else {
                                        if ( Utils.isEditable(mTestCase, By.id(elementid)) ) sPass = false;
                                    }
                                    if ( sPass )
                                    {
                                        mTestRunner.WriteLog("PASS: local '" + elementlabel + "' preference is disabled when global parent is enforced", LogFile.iTEST);
                                    } else {
                                        mTestRunner.WriteLog("FAIL: local '" + elementlabel + "' preference is enabled when global parent is enforced", LogFile.iERROR);
                                    }
                                }
                            }
                        }
                        mTestRunner.CloseLogFileGroup("Checking Local Prefs " + tablabel + " tab complete.", LogFile.iINFO);
                    }
                }
            }
        }
	Utils.DoClick(mTestCase, By.id(sBTN_LOCAL_OK), null, 500);
        mTestRunner.CloseLogFileGroup("Local Preference Enforcement tests complete", LogFile.iINFO);
    }

}
