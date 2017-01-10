//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 *
 * @author bowju01 & dicri02
 * 09/12/2011
 * 
 */

import common.*;
import org.openqa.selenium.By;
import org.w3c.dom.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyEditor extends WgnTestStep
{
    
    protected DataReader mDataReader;
    //constants
    private String polLink;
    private String optsLink;
//    private String treeId;
    private String treeLabel;
    private String classIndex;
    private static String parentHandle;
    
    public PolicyEditor() 
    { 
        mDataReader = new DataReader();
    }
 
    @Override
    public void Run() throws Exception
    {
        String classFile = mParameters.get("polClass");
        String commonObj = mParameters.get("commonObj");
        Boolean bCfg = mDataReader.Load(classFile, commonObj);
        mDataReader.DebugHashMap(mTestCase, mDataReader.GetParamTable());
       
        if (bCfg)
        {       
            // Set the vars for opening the Policy Editor window 
            polLink = mDataReader.GetAttributeValue(mDataReader.GetNodesByName("LINK").item(0), "clickid");
            mTestRunner.WriteLog("polLink = " + polLink, LogFile.iDEBUG);
            
            NodeList trees = mDataReader.GetNodesByName("TREE");
            if (trees != null && trees.getLength() > 0)
            {
                for (int i=0; i < trees.getLength(); i++)
                {
                    Node tree = trees.item(i);
                    if (tree.getNodeType() == Node.ELEMENT_NODE) 
                    {
                        // retrieve the class index from the tree node for the class under test
                        openPolicyPack(polLink);
                        String classLink = mDataReader.GetAttributeValue(tree, "label");
                        if (classLink.contains("/"))
                        {
                            // finding elements by link text doesn't like '/' characters so just use the text after it
                            classLink = classLink.substring(classLink.indexOf("/")+2);
                        }
                        String classId = Utils.GetAttribute(mTestCase, By.partialLinkText(classLink), "id", true);
                        mTestRunner.WriteLog("classId = " + classId, LogFile.iDEBUG);
                        
                        Matcher m = Pattern.compile("\\D*(\\d+)").matcher(classId);                        
                        if (m.find())
                        {
                            classIndex = m.group(1);
                            mTestRunner.WriteLog("classIndex = " + classIndex, LogFile.iDEBUG);
                        } else {
                            mTestRunner.WriteLog("ERROR: Failed to set classIndex", LogFile.iERROR);
                        }
                        if (classIndex != null)
                        {
                            Utils.CloseWindow(mTestCase, parentHandle);
                            // Set the vars for opening the Rules dialog 
                            optsLink = mDataReader.GetAttributeValue(tree, "clickid") + classIndex;
                            mTestRunner.WriteLog("optsLink = " + optsLink, LogFile.iDEBUG);
//                            treeId = mDataReader.GetAttributeValue(tree, "checkid") + classIndex + "_0_";
                            treeLabel = mDataReader.GetAttributeValue(tree, "label");

                            NodeList subtrees = tree.getChildNodes();

                            //-----Testing that all inputs can be changed and saved for each of the policy editor dialogs-----//
                            mTestRunner.NewLogFileGroup(treeLabel + ": Verify default values and set new values:", LogFile.iINFO);

                            traverseXML(subtrees, mDataReader.POPULATION);

                            Utils.Logoff(mTestCase);

                            mTestRunner.CloseLogFileGroup(treeLabel + ": Default checking complete and new values set", LogFile.iINFO);
                            mTestRunner.WriteLog("", LogFile.iINFO);

                            //------- validate the details entered and then clear down to restore blank defaults--------//
                            mTestRunner.NewLogFileGroup(treeLabel + ": Confirm new values have been saved: ", LogFile.iINFO);
                            Utils.Login(mTestCase, "primary");

                            traverseXML(subtrees, mDataReader.RESTORATION);

                            Utils.Logoff(mTestCase);
                            mTestRunner.CloseLogFileGroup(treeLabel + ": Save value confirmation test complete", LogFile.iINFO);
                            mTestRunner.WriteLog("", LogFile.iINFO);

                            //--------Log back in and run the default loop to check defaults have been returned correctly-----//
                            mTestRunner.NewLogFileGroup(treeLabel + ": Confirm defaults have been restored: ", LogFile.iINFO);
                            Utils.Login(mTestCase, "primary");

                            traverseXML(subtrees, mDataReader.NOPOPULATION);

                            mTestRunner.CloseLogFileGroup(treeLabel + ": Default restoration confirmation complete", LogFile.iINFO);
                        }
                    }
                }
            }
        } else {
            mTestRunner.WriteLog(mDataReader.GetFailureMessage(), LogFile.iERROR);
        }
        mTestRunner.WriteLog(mDataReader.GetDebugMessage(), LogFile.iDEBUG);
    }

    private void traverseXML(NodeList nodelist, String valType) throws Exception
    {
        int policyIndex=0;
        for (int index=0; index < nodelist.getLength(); index++)
        {
            Node node = nodelist.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (node.getNodeName().contentEquals("EXTRA_NODE"))
                {
                    NodeList nodes = node.getChildNodes();
                    handleSubPolicy(nodes, valType, policyIndex);
                } else {
                    String sDesc = mDataReader.GetAttributeValue(node, "label");
                    mTestRunner.NewLogFileGroup("Testing '" + sDesc + "' dialog", LogFile.iINFO);

                    // prepare the dialog ids e.g. btn_Edit_8_0_0_
                    String sEDITBUTTON;
                    sEDITBUTTON = mDataReader.GetAttributeValue(node, "clickid") + classIndex + "_" + Integer.toString(policyIndex) + "_";

                    String sRULES = mDataReader.GetAttributeValue(node, "checkid");
                    mTestRunner.WriteLog("sEDITBUTTON = " + sEDITBUTTON, LogFile.iDEBUG);
                    
                    // Open the Policy Editor and dialog under test
                    if (!Utils.isElementPresent(mTestCase, By.id(sEDITBUTTON), false) ) 
                    {
                        openPolicyPack(polLink);
                        viewPolicyTree(optsLink, sEDITBUTTON);
                        Thread.sleep(500);
                    }

                    //Click the 'Edit' button and check the title of the opened dialog
                    viewPolicyTree(sEDITBUTTON, sRULES);

                    // pass the index of the current class and policy to the DataReader
                    mDataReader.SetIndices(classIndex, Integer.toString(policyIndex));

                    // now test the elements in the dialog
                    NodeList tabs = node.getChildNodes();                
                    mDataReader.processNodeTypes(mTestCase, tabs, "", valType);

                    //Close down policy editor and save any changes
                    Save_and_close();
                    mTestRunner.CloseLogFileGroup("Completed testing '" + sDesc + "' dialog", LogFile.iINFO);
                    mTestRunner.WriteLog("", LogFile.iINFO);
                    mDataReader.clearIndices(); // reset tab and element counts ready for the next policy
                }
                policyIndex++;
            }
        }
    }
    
    private void handleSubPolicy(NodeList nodelist, String valType, int policyIndex) throws Exception
    {
        int subPolicyIndex=0;
        for (int index=0; index < nodelist.getLength(); index++)
        {
            Node node = nodelist.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {           
                String sDesc = mDataReader.GetAttributeValue(node, "label");
                mTestRunner.NewLogFileGroup("Testing '" + sDesc + "' dialog", LogFile.iINFO);

                // prepare the dialog ids e.g. btn_Edit_8_0_0_
                String sEDITBUTTON;
                mTestRunner.WriteLog("policyIndex = " + policyIndex + ":" + subPolicyIndex, LogFile.iDEBUG);
                sEDITBUTTON = mDataReader.GetAttributeValue(node, "clickid") + classIndex + "_" + policyIndex + "_" + subPolicyIndex + "_";

                String sRULES = mDataReader.GetAttributeValue(node, "checkid");
                mTestRunner.WriteLog("sEDITBUTTON = " + sEDITBUTTON, LogFile.iDEBUG);

                // Open the Policy Editor and dialog under test
                if (!Utils.isElementPresent(mTestCase, By.id(sEDITBUTTON), false) ) 
                {
                    openPolicyPack(polLink);
                    Node parent = node.getParentNode(); 
                    String EXTRANODE = mDataReader.GetAttributeValue(parent, "clickid") + classIndex + "." + policyIndex;
                    mTestRunner.WriteLog("EXTRANODE = " + EXTRANODE, LogFile.iDEBUG);
                    viewPolicyTree(optsLink, EXTRANODE);
                    Thread.sleep(1000);
                    viewPolicyTree(EXTRANODE, sEDITBUTTON);
                }

                //Click the 'Edit' button and check the title of the opened dialog
                viewPolicyTree(sEDITBUTTON, sRULES);

                // pass the index of the current class, policy and subpolicy to the DataReader
                String pol = Integer.toString(policyIndex) + ":" + Integer.toString(subPolicyIndex);
                mDataReader.SetIndices(classIndex, pol);

                // now test the elements in the dialog
                NodeList tabs = node.getChildNodes();                
                mDataReader.processNodeTypes(mTestCase, tabs, "", valType);

                //Close down policy editor and save any changes
                Save_and_close();
                mTestRunner.CloseLogFileGroup("Completed testing '" + sDesc + "' dialog", LogFile.iINFO);
                mTestRunner.WriteLog("", LogFile.iINFO);
                mDataReader.clearIndices(); // reset tab and element counts ready for the next policy
                subPolicyIndex++;
            }
        }
    }
     
    private void viewPolicyTree (String clickcol, String checkcol) throws Exception
    {
        int i=0; while (!Utils.isDisplayed(mTestCase, By.id(clickcol)) && i<60) { Thread.sleep(500); i++; }
        Utils.DoClick(mTestCase, By.id(clickcol), By.id(checkcol), 30000, true);       
    }     
 
    private void openPolicyPack(String polLink) throws Exception
    {
        Utils.DoClick(mTestCase, By.id("tab_editPolicy"), By.id(polLink), 60000, false);
        // Click the desired Policy Pack Editor Link and move selenium control to the child window
        Thread.sleep(500);
        parentHandle = Utils.GetNewWindow(mTestCase, By.id(polLink));
        // Check the iconsole has opened the Policy Editor in a child window and that it contains an expected element
        int i=0; while ( !Utils.isElementPresent(mTestCase, By.id("treeToggle_0"), false)
                && Utils.isElementPresent(mTestCase, By.id("progressContent"), false) && i<180) { Thread.sleep(500); i++; }
        Thread.sleep(500);
    }

    private void Save_and_close() throws Exception
    {
        // Click the Close button on the Rules dialog
        Utils.DoClick(mTestCase, By.id("btn_Rules.Close"), null, 10);
        while ( Utils.isElementPresent(mTestCase, By.id("Rules"), false)) { Thread.sleep(500); }
        
        // check if the Save button has been enabled (i.e. if there are any changes to save)
        if (Utils.isElementPresent(mTestCase, By.cssSelector("button#btn_Policies.Apply[class~='disBtn']"), false))
        {
            // Click the Cancel button on the Policy Editor - closes window so use Click()
            Utils.Click(mTestCase, By.id("btn_Policies.Close"), 1000);
        } else {
            // Click the Save button on the Policy Editor - closes window so use Click()
            Utils.Click(mTestCase, By.id("btn_Policies.Save"), 1000);
        }
        // Tell selenium to return to the parent window
        Utils.selectWindow(mTestCase, parentHandle);

        // check that we are on the parent window
        int i=0; while ( !Utils.isElementPresent(mTestCase, By.id("tab_searches"), false) && i<20) { Thread.sleep(500); i++; } 
        // Click on the homepage tab and check for an element
        Utils.DoClick(mTestCase, By.id("tab_searches"), By.id("treeTitle_Run.published."+mParameters.get("StdSrchHash")), 10000, false);
    }

}
