//Copyright © 2012 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * 23/12/2011
 */

import common.*;
import org.openqa.selenium.By;
import org.w3c.dom.*;


public class ReviewTab extends WgnTestStep
{
    protected DataReader mDR;
    private String parentHandle;
    private String BTN_FAV_STD_SEARCH;
    private String TTL_RUN_PUBL;
    private String DLG_CUSTOMIZE;
    private String BTN_CUST_CLOSE;
    private String CXT_ACTION_EDIT;
    private String DLG_PROGRESS;
    private String DLG_ABOUT;
    
    public ReviewTab() 
    { 
        mDR = new DataReader();
    }

    @Override
    public void Run() throws Exception
    {
        mTestRunner.WriteLog("DEBUG: Loading XML Data structure...", LogFile.iDEBUG);
        Boolean bCfg = mDR.Load(mParameters.get("dataStructure"));
        mTestRunner.WriteLog("DEBUG: XML Data structure loaded.", LogFile.iDEBUG);
        mDR.DebugHashMap(mTestCase, mDR.GetParamTable());
        mDR.SetParameters(mParameters);
        
        BTN_FAV_STD_SEARCH = Utils.SetParam(mTestCase, "BTN_FAV");
        TTL_RUN_PUBL = Utils.SetParam(mTestCase, "TTL_RUN_PUBL");
        DLG_CUSTOMIZE = Utils.SetParam(mTestCase, "DLG_CUSTOMIZE");
        BTN_CUST_CLOSE = Utils.SetParam(mTestCase, "BTN_CUST_CLOSE");
        CXT_ACTION_EDIT = Utils.SetParam(mTestCase, "CXT_ACTION_EDIT");
        DLG_PROGRESS = Utils.SetParam(mTestCase, "DLG_PROGRESS");
        DLG_ABOUT = Utils.SetParam(mTestCase, "DLG_ABOUT");
    
        if (bCfg)
        {
            // verify OOTB Dashboard list
            traverseTabs("DASHBOARD");
            // verify OOTB Reports list
            traverseTabs("REPORT");
            // verify OOTB Searches list
            traverseTabs("SEARCH");
        } 
        else 
        {
            mTestRunner.WriteLog(mDR.GetFailureMessage(), LogFile.iERROR);
        }
    }
    
    private void traverseTabs(String nodename) throws Exception
    {
        mTestRunner.NewLogFileGroup("Testing the " + nodename.toLowerCase() + " tab: ", LogFile.iINFO);     
        NodeList items = mDR.GetNodesByName(nodename);            
 
        if (items != null && items.getLength() > 0)
        {
            String sTabId = mDR.GetAttributeValue(items.item(0).getParentNode(), "clickid");           
                    
            Utils.DoClick(mTestCase, By.id(sTabId), By.id(TTL_RUN_PUBL + mDR.GetAttributeValue(items.item(0), "hash")), 10000);
            // cycle through the test tabs performing the tests
            for (int i=0; i < items.getLength(); i++)
            {
                Node item = items.item(i);
                String hash = mDR.GetAttributeValue(item, "hash");
                String desc = mDR.GetAttributeValue(item, "label");
                String runid = mDR.GetAttributeValue(item, "runid");
                if (Utils.isElementPresent(mTestCase, By.id(TTL_RUN_PUBL + hash), false) )
                {
                    mTestRunner.WriteLog("PASS: OOTB " + desc + " is available", LogFile.iTEST);
                    Utils.DoClick(mTestCase, By.id(BTN_FAV_STD_SEARCH + hash), null, 100);
                    Utils.DoClick(mTestCase, By.id(CXT_ACTION_EDIT), null, 100);
                    int j=0; while (!Utils.isElementPresent(mTestCase, By.id(DLG_CUSTOMIZE), false) && j<20) { Thread.sleep(1000); j++; }
                    
                    mTestRunner.NewLogFileGroup("Checking defaults for " + desc, LogFile.iINFO);
                    NodeList tabs = item.getChildNodes();
                    mDR.processNodeTypes(mTestCase, tabs, sTabId, mDR.NOPOPULATION);
                    Utils.DoClick(mTestCase, By.id(BTN_CUST_CLOSE), null, 0);
                    mTestRunner.CloseLogFileGroup("Completed default checks for " + desc, LogFile.iINFO);
                    // now run the item
                    RunItem(runid, desc, hash);

                    mTestRunner.WriteLog("", LogFile.iINFO);
                } else {
                    mTestRunner.WriteLog("FAIL: OOTB " + desc + " is NOT available", LogFile.iERROR);
                }
            }
        }
        mTestRunner.CloseLogFileGroup("Completed " + nodename.toLowerCase() + " tab", LogFile.iINFO);
        mTestRunner.WriteLog("", LogFile.iINFO);
    }


    private void RunItem(String runid, String desc, String hash) throws Exception
    {
        if (!runid.contains("DONTDOIT"))
        {
            By by = By.id(runid);
            if (desc.contains("Dashboard"))
            {
                by = By.cssSelector("*[id*='"+runid+"']");
            }
            StopWatch timer = new StopWatch().start();
            parentHandle = Utils.GetNewWindow(mTestCase, By.id(TTL_RUN_PUBL + hash));
            while ( Utils.isElementPresent(mTestCase, By.id(DLG_PROGRESS), false) ) { Thread.sleep(1000); }
            while ( !Utils.isElementPresent(mTestCase, By.linkText(DLG_ABOUT), false) ) { Thread.sleep(1000); }
            Thread.sleep(500);
            Boolean bFrame = false;
            bFrame = enteriFrame(bFrame);
            int iRun = 0; while ( !Utils.isElementPresent(mTestCase, by, false) && iRun < 120 ) 
            { 
                Thread.sleep(1000); 
                iRun++;
                bFrame = enteriFrame(bFrame);
            }            
            timer.stop();
            if (Utils.isElementPresent(mTestCase, by, false))
            {
                if (timer.getElapsedTime() < 60)
                {
                    mTestRunner.WriteLog("PASS: OOTB " + desc + " ran as expected in " + timer.getElapsedTime() + " secs", LogFile.iTEST);
                } else {
                    mTestRunner.WriteLog("WARNING: OOTB " + desc + " ran as expected but took " + timer.getElapsedTime() + " secs", LogFile.iERROR);
                }
            } else {
                mTestRunner.WriteLog("FAIL: OOTB " + desc + " didn't run correctly (" + timer.getElapsedTime() + " secs)", LogFile.iERROR);
            }
            if (bFrame) Utils.switchBackFromiFrame(mTestCase);
            Utils.CloseWindow(mTestCase, parentHandle);
        } else {
            mTestRunner.WriteLog("INFO: OOTB " + desc + " requires further configuration to run", LogFile.iINFO);
        }        
    }
    
    private Boolean enteriFrame(Boolean bFrame)
    {
        if (Utils.isElementPresent(mTestCase, By.id("DLP.ReportIframe"), mFailed)) 
        {
            bFrame = true;
            mTestRunner.WriteLog("DEBUG: XSL Reports iFrame entered", LogFile.iINFO);
            Utils.switchToiFrame(mTestCase, "DLP.ReportIframe");
        }
        return bFrame;
    }
}
