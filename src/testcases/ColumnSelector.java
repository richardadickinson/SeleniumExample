package testcases;

/**
 *
 * @author dicri02 17 Jan 2011
 */

import common.*;
import java.util.*;
import org.openqa.selenium.By;

public class ColumnSelector extends WgnTestStep
{
    public ColumnSelector() {  }

    private String colEvType,colSubject,colTimestamp;
    private String colFrom,colRecip,colAudit,colPolicy;
    private String colSeverity,colAction;
    private String noColumns;

    @Override
    public void Run() throws Exception
    {
        SetParameters();
        mTestRunner.NewLogFileGroup(String.format("Test step %s started", mName), LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id("btn_Results.Reorder"), By.id("ColumnSelector"), 5000);

        verifyDefaults();
        selectorTests();

        Utils.CloseWindow(mTestCase, mTestCase.GetParentHandle());
        mTestRunner.CloseLogFileGroup(String.format("Test step %s finished", mName), LogFile.iINFO);
    }
    
    private void SetParameters()
    {
        colEvType = Utils.SetParam(mTestCase, "colEvType");
        colSubject = Utils.SetParam(mTestCase, "colSubject");
        colTimestamp = Utils.SetParam(mTestCase, "colTimestamp");
        colFrom = Utils.SetParam(mTestCase, "colFrom");
        colRecip = Utils.SetParam(mTestCase, "colRecip");
        colAudit = Utils.SetParam(mTestCase, "colAuditStatus");
        colPolicy = Utils.SetParam(mTestCase, "colPolicy");
        colSeverity = Utils.SetParam(mTestCase, "colSeverity");
        colAction = Utils.SetParam(mTestCase, "colAction");
        noColumns = Utils.SetParam(mTestCase, "noColumns");
    }

    private void verifyDefaults() throws Exception
    {
        mTestRunner.NewLogFileGroup("Confirming the Standard search default columns are selected:", LogFile.iINFO);
        // Available columns - Severity, Action, Policy
        Boolean sevTest = false, polTest = false, actTest = false, badTest = false;
        String[] avlCols = Utils.GetSelectOptions(mTestCase, By.id("lstColumns_available"));
        String avlColsStr = "";
        for (int i = 0; i < avlCols.length; i++) {
            if (avlCols[i].contentEquals(colSeverity)) sevTest = true;
            else if(avlCols[i].contentEquals(colPolicy)) polTest = true;
            else if(avlCols[i].contentEquals(colAction)) actTest = true;
            else badTest = true;
            if (i==0) {
                avlColsStr = avlCols[i].toString();
            } else {
                avlColsStr = avlColsStr.concat(", ");
                avlColsStr = avlColsStr.concat(avlCols[i].toString());
            }
        }
        if ( sevTest && polTest && actTest && !badTest )
        {
            mTestRunner.WriteLog("PASS: The expected available columns are present (" + colSeverity + ", " + colPolicy + ", " + colAction + ")", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The available column list does not match the expected defaults", LogFile.iERROR);
            mTestRunner.WriteLog("      Expected: " + colSeverity + ", " + colPolicy + ", " + colAction, LogFile.iINFO);
            mTestRunner.WriteLog("      Actual: " + avlColsStr, LogFile.iINFO);
        }
        // Selected columns - Event Type, Subject/Filename, Timestamp, From/User/Host, Recipients/Participants/URL/Path, Audit Status
        Boolean evtTest = false, sbjTest = false, tspTest = false, frmTest = false, recTest = false, ausTest = false;
        badTest = false;
        String[] selCols = Utils.GetSelectOptions(mTestCase, By.id("lstColumns"));                
        String selColsStr = "";
        for (int i = 0; i < selCols.length; i++) {
            if (selCols[i].contentEquals(colEvType)) evtTest = true;
            else if(selCols[i].contentEquals(colSubject)) sbjTest = true;
            else if(selCols[i].contentEquals(colTimestamp)) tspTest = true;
            else if(selCols[i].contentEquals(colFrom)) frmTest = true;
            else if(selCols[i].contentEquals(colRecip)) recTest = true;
            else if(selCols[i].contentEquals(colAudit)) ausTest = true;
            else badTest = true;
            if (i==0) {
                selColsStr = selCols[i].toString();
            } else {
                selColsStr = selColsStr.concat(", ");
                selColsStr = selColsStr.concat(selCols[i].toString());
            }
        }
        if ( evtTest && sbjTest && tspTest && frmTest && recTest && ausTest && !badTest )
        {
            mTestRunner.WriteLog("PASS: The expected selected columns are present (" + colEvType
                    + ", " + colSubject + ", " + colTimestamp + ", " + colFrom + ", " + colRecip + ", " + colAudit + ")", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The selected column list does not match the expected defaults", LogFile.iERROR);
            mTestRunner.WriteLog("      Expected: " + colEvType + ", " + colSubject + ", "
                    + colTimestamp + ", " + colFrom + ", " + colRecip + ", " + colAudit, LogFile.iINFO);
            mTestRunner.WriteLog("      Actual: " + selColsStr, LogFile.iINFO);
        }
        mTestRunner.CloseLogFileGroup("Standard Search default columns test completed", LogFile.iINFO);
    }

    private void selectorTests() throws Exception
    {
        // Remove All button
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Action.3"), null, 100);
        Utils.DoClick(mTestCase, By.id("btn_ColumnSelector.OK"), By.id("Message"), 5000);
        if (Utils.isTextPresent(mTestCase, noColumns, false))
        {
            mTestRunner.WriteLog("PASS: 'Remove All' button cleared the Selected Columns list", LogFile.iTEST);
            Utils.DoClick(mTestCase, By.id("btn_Message.Close"), null, 100);
        } else {
            mTestRunner.WriteLog("FAIL: 'Remove All' button failed to clear the Selected Columns list", LogFile.iERROR);
        }

        // Add Selected item button        
        Utils.SelectOptionByText(mTestCase, By.id("lstColumns_available"), colPolicy);
        Thread.sleep(2000);
        if (mTestRunner.GetTargetBrowser().contains("chrome"))
        {
            // the chromedriver fails to trigger the js called by the onchange event when a multiselect option is selected
            // so we must do it manually |:-\            
            String myjs = "var el = document.getElementById('lstColumns_available'); el.control.UpdateButtons.call(el.control);"; 
            Utils.ExecuteJavaScript(mTestCase, myjs);
        }
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Action.1"), null, 500);

        if (Utils.GetMultiFirstSelectedLabel(mTestCase, By.id("lstColumns")).contentEquals(colPolicy))
        {
            mTestRunner.WriteLog("PASS: Add Selected button added '" + colPolicy + "' to the Selected Columns list", LogFile.iTEST);
            Boolean polTest = false;
            String[] selCols = Utils.GetSelectOptions(mTestCase, By.id("lstColumns_available"));
            String dbgSelCols = "";
            for (int i = 0; i < selCols.length; i++) 
            {
                dbgSelCols.concat(selCols[i]+",");
                if (selCols[i].contentEquals(colPolicy)) polTest = true;
            }
            if (!polTest)
            {
                mTestRunner.WriteLog("PASS: 'Add Selected' button removed '" + colPolicy + "' from the Available Columns list", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: 'Add Selected' button failed to remove '" + colPolicy + "' from the Available Columns list", LogFile.iERROR);
                mTestRunner.WriteLog("DEBUG: selCols: " + dbgSelCols, LogFile.iDEBUG);
            }
        } else {
            mTestRunner.WriteLog("FAIL: 'Add Selected' button failed to move '" + colPolicy + "' to the Selected Columns list {" + Utils.GetMultiFirstSelectedLabel(mTestCase, By.id("lstColumns")) + "}", LogFile.iERROR);
        }

        // verify the results table updates to show only the selected column
        Utils.DoClick(mTestCase, By.id("btn_ColumnSelector.OK"), null, 500);
        String[] expdArray = {colPolicy};
        if (getVisibleColumns(expdArray))
        {
            mTestRunner.WriteLog("PASS: the columns have been reconfigured on the Results table", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: the columns have not been reconfigured on the Results table", LogFile.iERROR);
        }
        Utils.DoClick(mTestCase, By.id("btn_Results.Reorder"), By.id("btn_ColumnSelector.OK"), 5000);

        // workaround to bug 59695 (need to select an entry in the available columns list to enable the Add All button)
        Utils.SelectOptionByText(mTestCase, By.id("lstColumns_available"), colAudit);
        // Add All button
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Action.0"), null, 100);
        String[] newSelCols = Utils.GetSelectOptions(mTestCase, By.id("lstColumns_available"));
        String[] allCols = Utils.GetSelectOptions(mTestCase, By.id("lstColumns"));
        if (newSelCols.length == 0 && allCols.length == 9 )
        {
            mTestRunner.WriteLog("PASS: 'Add All' button moved all columns to the Selected Columns list", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: 'Add All' button failed to clear the Available Columns list (options=" + newSelCols.length + ")", LogFile.iERROR);
        }

        // Move Up button
        Utils.SelectOptionByText(mTestCase, By.id("lstColumns"), colFrom);                
        if (mTestRunner.GetTargetBrowser().contains("chrome"))
        {
            // the chromedriver fails to trigger the js called by the onchange event when a multiselect option is selected
            // so we must do it manually |:-\
            String myjs = "var el = document.getElementById('lstColumns'); el.control.OnChange.call(el.control, MIR.BI.GetEvent(), el);"; 
            Utils.ExecuteJavaScript(mTestCase, myjs);
        }
        int idx = Utils.GetSelectedIndex(mTestCase, By.id("lstColumns"));
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Order.0"), null, 100);
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Order.0"), null, 100);
        int idx2 = Utils.GetSelectedIndex(mTestCase, By.id("lstColumns"));

        if (idx2 == idx-2)
        {
            mTestRunner.WriteLog("PASS: 'Move Up' button moved the " + colFrom + " column up two places", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: 'Move Up' button failed to move the " + colFrom + " column up two places (idx=" + idx + ", idx2=" + idx2 + ")", LogFile.iERROR);
        }

        // Move Down button
        Utils.SelectOptionByText(mTestCase, By.id("lstColumns"), colTimestamp);
        if (mTestRunner.GetTargetBrowser().contains("chrome"))
        {
            // the chromedriver fails to trigger the js called by the onchange event when a multiselect option is selected
            // so we must do it manually |:-\
            String myjs = "var el = document.getElementById('lstColumns'); el.control.OnChange.call(el.control, MIR.BI.GetEvent(), el);"; 
            Utils.ExecuteJavaScript(mTestCase, myjs);
        }        
        int idx3 = Utils.GetSelectedIndex(mTestCase, By.id("lstColumns"));
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Order.1"), null, 100);
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Order.1"), null, 100);
        int idx4 = Utils.GetSelectedIndex(mTestCase, By.id("lstColumns"));

        if (idx4 == idx3+2)
        {
            mTestRunner.WriteLog("PASS: 'Move Down' button moved the " + colTimestamp + " column down two places", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: 'Move Down' button failed to move the " + colTimestamp + " column down two places (idx3=" + idx3 + ", idx4=" + idx4 + ")", LogFile.iERROR);
        }

        // Remove Selected Item button
        Utils.SelectOptionByText(mTestCase, By.id("lstColumns"), colSeverity);
        if (mTestRunner.GetTargetBrowser().contains("chrome"))
        {
            // the chromedriver fails to trigger the js called by the onchange event when a multiselect option is selected
            // so we must do it manually |:-\
            String myjs = "var el = document.getElementById('lstColumns'); el.control.OnChange.call(el.control, MIR.BI.GetEvent(), el);"; 
            Utils.ExecuteJavaScript(mTestCase, myjs);
        }        
        Utils.DoClick(mTestCase, By.id("btn_lstColumns.Action.2"), null, 100);

        if (Utils.GetSelectedLabel(mTestCase, By.id("lstColumns_available")).contentEquals(colSeverity))
        {
            mTestRunner.WriteLog("PASS: 'Remove Selected' button moved '" + colSeverity + "' to the Available Columns list", LogFile.iTEST);
            Boolean sevTest = false;
            String[] selCols = Utils.GetSelectOptions(mTestCase, By.id("lstColumns"));
            for (int i = 0; i < selCols.length; i++) {
                if (selCols[i].contentEquals(colSeverity)) sevTest = true;
            }
            if (!sevTest)
            {
                mTestRunner.WriteLog("PASS: 'Remove Selected' button removed '"+ colSeverity +"' from the Selected Columns list", LogFile.iTEST);
            } else {
                mTestRunner.WriteLog("FAIL: 'Remove Selected' button failed to remove '"+ colSeverity +"' from the Selected Columns list", LogFile.iERROR);
            }
        } else {
            mTestRunner.WriteLog("FAIL: 'Remove Selected' button failed to move '"+ colSeverity +"' to the Available Columns list", LogFile.iERROR);
        }

        // verify the results table updates to show only the selected columns
        Utils.DoClick(mTestCase, By.id("btn_ColumnSelector.OK"), null, 100);
        String[] exptdArray = {colEvType,colSubject,colTimestamp,colFrom,colRecip,colAudit,colPolicy,colAction};
        if (getVisibleColumns(exptdArray))
        {
            mTestRunner.WriteLog("PASS: The columns have been reconfigured on the Results table ('Severity' removed)", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The columns have not been reconfigured correctly on the Results table", LogFile.iERROR);
        }
        Utils.DoClick(mTestCase, By.id("btn_Results.Reorder"), By.id("btn_ColumnSelector.Reset"), 5000);
        
        mTestRunner.NewLogFileGroup("Checking the Reset button works:", LogFile.iINFO);
        Utils.DoClick(mTestCase, By.id("btn_ColumnSelector.Reset"), null, 100);
        // verify the default columns have been restored
        String[] expArray = {colEvType,colSubject,colTimestamp,colFrom,colRecip,colAudit};
        if (getVisibleColumns(expArray))
        {
            mTestRunner.WriteLog("PASS: The default columns have been restored on the Results table", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: The default columns have not been restored on the Results table", LogFile.iERROR);
        }

        Utils.DoClick(mTestCase, By.id("btn_Results.Reorder"), By.id("ColumnSelector"), 5000);
        verifyDefaults();
        mTestRunner.CloseLogFileGroup("Reset tests complete", LogFile.iINFO);

        // Cancel button
        Utils.DoClick(mTestCase, By.id("btn_ColumnSelector.Cancel"), null, 100);
        if ( !Utils.isElementPresent(mTestCase, By.id("ColumnSelector"), false) )
        {
            mTestRunner.WriteLog("PASS: Cancel button closed the Column Selector dialog", LogFile.iTEST);
        } else {
            mTestRunner.WriteLog("FAIL: Cancel button failed to close the Column Selector dialog", LogFile.iERROR);
        }

    }

    private Boolean getVisibleColumns(String[] expArray) throws Exception
    {
        // check which columns are currently visible
        Boolean b = true;
        String str = "", expStr = "";

        String[] colsArray = {"th_severity","th_policy","th_action","th_event_type","th_subject","th_capturets","th_sender","th_recipients","th_audit"};
        String[] colNamesArray = {colSeverity, colPolicy, colAction, colEvType, colSubject, colTimestamp, colFrom, colRecip, colAudit};
        List <String> visList = new ArrayList<String>();

        for (int i = 0; i < colsArray.length; i++) {
            if ( Utils.isElementPresent(mTestCase, By.id(colsArray[i]),false) )
            {
                if (str.contentEquals("")) { str = colNamesArray[i]; } else { str = str.concat(", " + colNamesArray[i]); }
                visList.add(colNamesArray[i]);
            }
        }
        if (expArray.length != visList.size()) { b = false; }
        for (int j = 0; j < expArray.length; j++) {
            if (j==0) { expStr = expArray[j]; } else { expStr = expStr.concat(", " + expArray[j]); }
            if (!str.contains(expArray[j])) { b = false; }
        }

        if (!b)
        {
            mTestRunner.WriteLog("DEBUG: Expected array length = " + expArray.length + ", Visible array length = " + visList.size(), LogFile.iDEBUG);           
            mTestRunner.WriteLog("DEBUG: Expected visible columns: " + expStr, LogFile.iDEBUG);
        }
        mTestRunner.WriteLog("INFO: Actual visible columns: " + str, LogFile.iINFO);

        return b;
    }

} // end ColumnSelector
