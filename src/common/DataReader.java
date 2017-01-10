//Copyright © 2011 CA. All rights reserved.

package common;

/**
 *
 * @author dicri02
 * 09/12/2011
 * 
 */
import testcases.*;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;

import org.openqa.selenium.*;

import java.util.concurrent.TimeUnit;


public class DataReader 
{
    private String mFailureMessage;
    private String mDebugMessage;
    private Document mDoc;
    private Document mCommon;
    private Boolean isCommon;
    public String NOPOPULATION = "0";  // check defaults, don't change them
    public String POPULATION = "1";    // check defaults and change them for test values
    public String RESTORATION = "2";   // check test values were stored and restore defaults
    public String NORESTORATION = "3"; // check test values were stored, DON'T restore defaults
    protected Map<String,String> paramTable; // holds the test script parameters defined in the data structure XML
    protected Map<String,String> dummyAttrs; // supports scripts that don't need to supply attributes on nodes
    protected Map<String,String> pickerAttrs; // attribute map for picker objects
    protected Map<String,String> commonAttrs; // attribute map for common objects
    protected Map<String,String> columns; // attribute map for available columns
    protected Map<String,String> selcols; // attribute map for selected columns
    protected Map<String,String> mParameters; // to hold the test case parameters
    private String classIndex;
    private String policyIndex;
    private int tabIndex;
    private int elementIndex;
    private String asany;
    private String asunset;
    private String asanynon;

    public void SetParameters(Map<String,String> params)
    {
        mParameters.putAll(params);
    }
        
    public Map<String,String> SetAttrs(WgnTestCase wtc, Map<String,String> table, Node node)
    {
        // clear any existing attrs from the table first
        table.clear();
        NamedNodeMap attrs = node.getAttributes();
        for (int i=0; i < attrs.getLength(); i++)
        {
            table.put(attrs.item(i).getNodeName(), replaceRef(attrs.item(i).getNodeValue(), table));
        }
        DebugHashMap(wtc, table);
        
        return table;
    }
    
    public void SetIndices (String sClass, String sPolicy)
    {
        classIndex = sClass;
        policyIndex = sPolicy;
    }
    
    public void clearIndices() // reset tab and element counts
    {
        elementIndex = 0;
        tabIndex = 0;
    }
    
    public boolean Load(String xmlfile) throws Exception
    {
        return Load(xmlfile, "none");
    }    
    
    public boolean Load(String xmlfile, String commonBlocks) throws Exception
    {
        paramTable = new HashMap<>();
        dummyAttrs = new HashMap<>();
        pickerAttrs = new HashMap<>();
        commonAttrs = new HashMap<>();
        columns = new HashMap<>();
        selcols = new HashMap<>();
        mParameters = new HashMap<>();
        isCommon = false;
        
        boolean bRet = false;
        try
        {
            File file = new File(xmlfile);
            mDebugMessage = "Got file: " + xmlfile;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);            
            doc.getDocumentElement().normalize();
            mDebugMessage = xmlfile + " parsed and normalized";
            mDoc = doc;
            
            if (!commonBlocks.contentEquals("none"))
            {
                isCommon = true;
                File cfile = new File(commonBlocks);
                mDebugMessage = "Got file: " + commonBlocks;
                DocumentBuilderFactory dbfc = DocumentBuilderFactory.newInstance();
                dbfc.setIgnoringComments(true);
                dbfc.setIgnoringElementContentWhitespace(true);
                DocumentBuilder dbc = dbfc.newDocumentBuilder();
                Document common = dbc.parse(cfile);            
                common.getDocumentElement().normalize();
                mDebugMessage = commonBlocks + " parsed and normalized";
                mCommon = common;
                bRet = LoadTestParams(common);
            } else {
                bRet = LoadTestParams(doc);           
            }
            return bRet;
        } 
        catch (Exception e)
        {
            System.err.print(e.getMessage());
            mFailureMessage = "Failed to load test case file due to exception: " + e.getMessage();
            throw e;
        }
    }
    
    public boolean LoadTestParams(Document doc) throws Exception
    {
        // collect the test parameters
        NodeList paramlist = doc.getElementsByTagName("PARAMS");
        if (paramlist != null && paramlist.getLength() > 0)
        {
            mDebugMessage = "First node: " + paramlist.item(0).getNodeName();            
            NodeList params = paramlist.item(0).getChildNodes();
            for (int i=0; i < params.getLength(); i++)
            {
                if (params.item(i).getNodeType() == Node.ELEMENT_NODE) 
                {
                    mDebugMessage = "Second node: " + params.item(i).getNodeName();
                    paramTable.put(params.item(i).getNodeName(), params.item(i).getTextContent());
                }
            }
            return true;
        } else {
            mFailureMessage = "Oops! Check your XML format";
        }
        return false;
    }
    
    public NodeList GetNodesByName(String nodename)
    {
        NodeList tags;
        if ( isCommon && nodename.endsWith("_OBJ") )
        {
            tags = mCommon.getElementsByTagName(nodename);
        } else {
            tags = mDoc.getElementsByTagName(nodename);
        }
        return tags;
    }
    
    public NodeList GetChildNodes(WgnTestCase wtc, String parent, String children)
    {
        TestRunner tr = wtc.GetTestRunner();
        // collect the test parameters
        NodeList allChildren = GetNodesByName(parent);
        if (allChildren != null && allChildren.getLength() > 0)
        {
            //tr.WriteLogFileEntry("First node: " + allChildren.item(0).getNodeName(), LogFile.iDEBUG);            
            NodeList objects = allChildren.item(0).getChildNodes();
            if (children.contentEquals("all"))
            {
                return objects;
            }
            for (int i=0; i < objects.getLength(); i++)
            {
                if (objects.item(i).getNodeName().contentEquals(children))
                {
                    return objects.item(i).getChildNodes();
                }
            }            
        } else {
            tr.WriteLog("Oops! No child nodes found for <"+parent+"> node, check your XML format", LogFile.iERROR);
        }
        return null;
    }

    public String getAttribute(Node node, String attrName)
    {
        // return the text value of an attribute on the current node
        // WITHOUT substituting any $attr$ or %PARAM% values
        Element attr = (Element) node;
        return attr.getAttribute(attrName);
    }
        
    public String GetAttributeValue(Node node, String attrName)
    {
        // supports test scripts that don't use attributes on nodes in their data structure
        // - just passes an empty attribute table through the code instead
        return GetAttributeValue(node, attrName, dummyAttrs);
    }
        
    public String GetAttributeValue(Node node, String attrName, Map<String,String> attrTable)
    {
        // return the text value of an attribute on the current node
        Element attr = (Element) node;
        return replaceRef(attr.getAttribute(attrName), attrTable);
    }
    
    public String replaceRef(String ref, Map<String,String> attrTable)
    {
        String ret = "";
        // first look for attribute substitution values
        // there can be more than one conjugated so make match non-greedy
        Pattern p = Pattern.compile("\\$(.+?)\\$");
        Matcher m = p.matcher(ref);
        while (m.find())
        {
            // extract all attribute substitution values
            String attr = attrTable.get(m.group(1));
            ret = ret + attr;
        }
        if (!ret.isEmpty())
        {
            // now restore any prefix and/or suffix to the string
            Pattern p2 = Pattern.compile("(.*?)\\$(.+)\\$(.*)");
            Matcher m2 = p2.matcher(ref);
            if (m2.find())
            {
                ret = m2.group(1) + ret + m2.group(3);
            }
        } else {
            // no attribute substitution values were found 
            // so just pass the original ref value back in
            ret = ref;
        }
        // it may be replaced with a common value from the global parameters
        // from the test case
        Pattern p4 = Pattern.compile("(.*?)£(.+)£");
        Matcher m4 = p4.matcher(ref);
        if (m4.find())
        {
            ret = m4.group(1) + mParameters.get(m4.group(2));
        }
        // finally if it's a parameter placeholder (e.g. %MYVAL%) 
        // substitute the real value in from the parameter table
        Pattern p3 = Pattern.compile("%(.+)%");
        Matcher m3 = p3.matcher(ref);
        if (m3.find())
        {
            ret = GetParamTable().get(m3.group(1));        
        }
        mDebugMessage = "replaceRef(): ref="+ref+"; ret="+ret;
        return ret;
    }
    
    public String GetCloseButtonId(String dialogId)
    {
        // return the id of the dialog's close button
        Pattern p = Pattern.compile("%(.+)%");
        Matcher m = p.matcher(dialogId);
        if (m.find())
        {
            return paramTable.get(m.group(1)+"_CLOSE");        
        }
        return "FAILED to get Close button!";
    }
    
    public Map<String, String> GetParamTable()
    {
        // return the parameters for the current XML file
        return paramTable;
    }
    
    public String GetFailureMessage()
    {
        return mFailureMessage;
    }
    
    public String GetDebugMessage()
    {
        return mDebugMessage;
    }
    
    public void DebugHashMap(WgnTestCase wtc, Map<String,String> table)
    {
        TestRunner tr = wtc.GetTestRunner();
        // Debug code to see the contents of the loaded paramTable here:
        if (tr.GetConfigurationFile().GetLogLevel() == LogFile.iDEBUG)
        {
            tr.NewLogFileGroup("DEBUG: new attribute list:", LogFile.iDEBUG);
            // outputs the contents of the paramTable  
            Iterator<Entry<String, String>> it = table.entrySet().iterator();
            while (it.hasNext()) 
            {
                Entry<String, String> pairs = it.next();
                tr.WriteLog(pairs.getKey() + " = " + pairs.getValue(), LogFile.iDEBUG);
            }
            tr.CloseLogFileGroup("", LogFile.iDEBUG);
        }
    }

    public void processNodeTypes(WgnTestCase wtc, NodeList nodes, String sTabId, String doSave) throws Exception
    {
        processNodeTypes(wtc, nodes, sTabId, dummyAttrs, doSave);
    }
    
    public void processNodeTypes(WgnTestCase wtc, NodeList nodes, String sTabId, Map<String, String> attrs, String doSave) throws Exception
    {
        TestRunner tr = wtc.GetTestRunner();
        
        for (int i=0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            // first check we have an element node (not text, comment or other)
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {                
                String nodename = node.getNodeName(); 
                if (nodename.contentEquals("PREFS") || nodename.contentEquals("DIALOG") || nodename.contentEquals("LINK"))
                {
                    String clickid = GetAttributeValue(node, "clickid", attrs);
                    String checkid = GetAttributeValue(node, "checkid", attrs);
                    Utils.DoClick(wtc, By.id(clickid), By.id(checkid), 5000);   
                    // cycle through child nodes sending each for validation
                    NodeList subnodes = node.getChildNodes();
                    processNodeTypes(wtc, subnodes, sTabId, attrs, doSave);
                }
                if (nodename.contentEquals("COMMON"))
                {
                    // identify the type of COMMON object we've reached
                    String sType = GetAttributeValue(node, "type");
                    // populate the common attributes table with the attrs from this node
                    commonAttrs = SetAttrs(wtc, commonAttrs, node);
                    // now get the common object of the correct type from the Data Structure
                    NodeList commonObj = GetChildNodes(wtc, "COMMON_OBJ", sType);
                    // cycle through its nodes and send them for validation
                    processNodeTypes(wtc, commonObj, sTabId, commonAttrs, doSave);
                }
                else if (nodename.contentEquals("TAB"))
                {
                    sTabId = GetAttributeValue(node, "clickid");
                    if (wtc.GetTestName().contains("Policy Editor")) sTabId = sTabId + Integer.toString(tabIndex);
                    tr.NewLogFileGroup(GetAttributeValue(node, "label") + " tab", LogFile.iINFO);
                    NodeList tabs = node.getChildNodes();
                    // cycle through its nodes and send them for validation
                    processNodeTypes(wtc, tabs, sTabId, attrs, doSave);
                    tr.CloseLogFileGroup(GetAttributeValue(node, "label") + " tab complete", LogFile.iINFO);
                    tabIndex++;
                    // a new tab means the element indexing resets to 0
                    elementIndex = 0;
                }
                else if (nodename.contentEquals("PICKER"))
                {
                    String sName = GetAttributeValue(node, "name");
                    tr.NewLogFileGroup("INFO: Testing the " + sName + " picker control:", LogFile.iINFO);
                    // set the picker attributes
                    pickerAttrs = SetAttrs(wtc, pickerAttrs, node);
                    // get the common PICKER_OBJ node from the XML
                    NodeList pickerObj = GetChildNodes(wtc, "PICKER_OBJ", "all");
                    // cycle through its nodes and send them for validation
                    processNodeTypes(wtc, pickerObj, sTabId, pickerAttrs, doSave);
                    tr.CloseLogFileGroup("INFO: Testing the " + sName + " picker control complete", LogFile.iINFO);
                }
                else if (nodename.contentEquals("SUBTAB"))
                {
                    // check the subtab is expanded
                    String expand = GetAttributeValue(node, "expanded");
                    if (expand.contentEquals("no"))
                    {
                        String clickid = GetAttributeValue(node, "clickid", attrs);
                        Utils.DoClick(wtc, By.id(clickid), null, 1000);
                    }
                    // cycle through child nodes sending each for validation
                    NodeList subnodes = node.getChildNodes();
                    processNodeTypes(wtc, subnodes, sTabId, attrs, doSave);
                }
                else if (nodename.contentEquals("CHECKGRP")) 
                {
                    // these checkboxes toggle a subset of other checkboxes when selected so we don't want to change them 
                    // as it affects the default and save values of the whole subset
                    // for now we'll just check their default and add more tests later
                    String sCompare = GetAttributeValue(node, "default", attrs);
                    String clickid = GetAttributeValue(node, "clickid", attrs);
                    String label = GetAttributeValue(node, "label", attrs);
                    if ( doSave.equals(NOPOPULATION))
                    {
                        Utils.isChecked(wtc, By.id(clickid), label, sCompare, true);
                    }
                    // we need to add a test that verifies toggling a group checkbox also toggles all the checkbox values in the group
                    // what about group checkboxes that enable/disable subelements?
                    
                    // pass the child nodes in the group on for validation
                    NodeList subnodes = node.getChildNodes();
                    processNodeTypes(wtc, subnodes, sTabId, attrs, doSave);
                }
                else if (nodename.contentEquals("IF")) 
                {
                    // IF blocks always have a single attribute so just get the first attr from the getAttributes() list 
                    String attrName = node.getAttributes().item(0).getNodeName();
                    String attrValue = node.getAttributes().item(0).getNodeValue();
                    tr.WriteLog("DEBUG: There's an IF element here: " + attrName, LogFile.iDEBUG);
                    // get the value for comparison - if it's not present then set to a value that won't match
                    String testValue = "";
                    try 
                    {
                        testValue = attrs.get(attrName);
                    }
                    catch (Exception e) 
                    {
                        testValue = "Not present";
                    }
                    if (testValue == null) testValue = "Not Present";
                    // compare the IF attribute value to its parent's matching attribute value, if they match test the subnodes
                    // 'noresults' test is a special case for picker controls
                    Boolean bProcess = false; 
                    if ( attrName.contentEquals("noresults") )
                    {
                        if (!Utils.isTextPresent(wtc, "No Results", false)) bProcess = true;
                    } else {                     
                        if (testValue.contentEquals(attrValue)) bProcess = true;
                    }
                    NodeList subnodes = node.getChildNodes();
                    if (bProcess)
                    {
                        processNodeTypes(wtc, subnodes, sTabId, attrs, doSave);
                    } 
                    else 
                    {
                        tr.WriteLog("DEBUG: No match so skip IF subelements (attr name: " + attrName 
                                + " IF value: " + attrValue + " parent value: " + testValue + ")", LogFile.iDEBUG);
                    }
                }
                else if (node.getNodeName().contentEquals("HIDDEN"))
                {
                    // this node is specific to the PolicyEditor test script;
                    // some elements on the Policy Rules dialog are paired with a supporting hidden element,
                    // the hidden element can't be seen or tested but does take an index out of the sequence and so we need to account for it here
                    elementIndex++;
                }
                else if (node.getNodeName().contentEquals("COLUMNS"))
                {
                    columns.clear();
                    selcols.clear();
                    NodeList subnodes = node.getChildNodes();
                    // count the number of child nodes being skipped as the element index still needs to increment
                    //int k=0;
                    for (int j=0; j < subnodes.getLength(); j++)
                    {
                        if (subnodes.item(j).getNodeType() == Node.ELEMENT_NODE) 
                        {
                            // add the labels for all columns to the columns hashmap for use later
                            if (GetAttributeValue(subnodes.item(j), "default", attrs).contentEquals("on")) 
                            {
                                selcols.put(GetAttributeValue(subnodes.item(j), "label", attrs), GetAttributeValue(subnodes.item(j), "label", attrs)); //k++;
                            } else {
                                columns.put(GetAttributeValue(subnodes.item(j), "label", attrs), GetAttributeValue(subnodes.item(j), "label", attrs)); //k++;
                            }
                        }
                    }
                    processNodeTypes(wtc, subnodes, sTabId, attrs, doSave);
                }
                else if (node.getNodeName().contentEquals("POPUP"))
                {
                    // the POPUP node contains the elements in a picker dialog
                    String clickid = GetAttributeValue(node, "clickid", pickerAttrs);                    
                    String closeVal = GetCloseButtonId(getAttribute(node, "id"));
                    // open the picker dialog
                    Utils.DoClick(wtc, By.id(clickid), By.id(closeVal), 10000);
                    while (Utils.isElementPresent(wtc, By.id("progressContent"), false)) { Thread.sleep(1000); }

                    // click the Search button if results were not automatically loaded (otherwise many controls do not appear)
                    if (!Utils.isElementPresent(wtc, By.id("chk_Results.All"), false) )
                    {
                        Utils.DoClick(wtc, By.id("btn_Picker.Search"), By.id("chk_Results.All"), 20000);
                    }
                    // cycle through the POPUP node's children, sending each for validation
                    NodeList subelements = node.getChildNodes();
                    processNodeTypes(wtc, subelements, sTabId, attrs, doSave);
                    // finally close the picker dialog
                    Utils.DoClick(wtc, By.id(closeVal), null, 500);
                    while ( Utils.isElementPresent(wtc, By.id(closeVal), false) ) { Thread.sleep(500); }
                } else {
                    // we've arrived at a terminal node - go straight to the element validation
                    String clickid = "";
                    if (wtc.GetTestName().contains("Policy Editor"))
                    {
                        clickid = GetAttributeValue(node, "clickid") + classIndex + ":" + policyIndex + ":_" + Integer.toString(tabIndex) + "_" + Integer.toString(elementIndex);
                        tr.WriteLog("DEBUG: clickid=" + clickid, LogFile.iDEBUG);
                    }
                    validateElement(wtc, node, clickid, sTabId, attrs, doSave);
                    elementIndex++;
                }
            }
        }        
    }
    
    public void validateElement(WgnTestCase wtc, Node element, String clickid, String sTabId, String doSave) throws Exception
    {
        validateElement(wtc, element, clickid, sTabId, dummyAttrs, doSave);
    }

    public void validateElement(WgnTestCase wtc, Node element, String clickid, String sTabId, Map<String,String> attrs, String doSave) throws Exception
    {
        TestRunner tr = wtc.GetTestRunner();
        String element_type = element.getNodeName();
        if (clickid.isEmpty()) clickid = GetAttributeValue(element, "clickid", attrs);
        String label = GetAttributeValue(element, "label", attrs);
        
        if (!Utils.isElementPresent(wtc, By.id(clickid), false) || !Utils.isDisplayed(wtc, By.id(clickid)))
        {
            // if the element is not present click on its tab
            tr.WriteLog("DEBUG: An expected element isn't present or visible! clickid = " + clickid, LogFile.iDEBUG);
            Utils.DoClick(wtc, By.id(sTabId), By.id(clickid), 5000);
            Thread.sleep(500);
        }
        switch (element_type) {
            case "TEXTBOX":
                break;
            case "INTEGERBOX":
                // set the expected and save values based on the loop action (POPULATION etc)
                Map<String, String> ivalues = setTestValues(element, doSave, attrs);
                Utils.CompareTextBoxValue(wtc, By.id(clickid), label, ivalues.get("compare"));
                if (element_type.contentEquals("INTEGERBOX") && (doSave.equals(NOPOPULATION))) invalidNumberTest(wtc, element, clickid, sTabId, attrs);
                // now insert a test value if necessary
                if ( !doSave.equals(NOPOPULATION) && !doSave.equals(NORESTORATION) )  Utils.PlainType(wtc, By.id(clickid), ivalues.get("save"));
                break;
            case "SELECT":
                if (GetAttributeValue(element, "clickid", attrs).startsWith("lstAS"))
                {
                    valAuditOptions(wtc, element, clickid);
                }
                break;
            case "MULTI":                
                Map<String, String> values = setTestValues(element, doSave, attrs);
                Utils.CompareSelectLabel(wtc, By.id(clickid), label, values.get("compare"));
                // special tests for the available items in select lists
                if (GetAttributeValue(element, "options", attrs).contentEquals("COLUMNS"))
                {
                    valColumns(wtc, clickid, GetAttributeValue(element, "type", attrs), GetAttributeValue(element, "label", attrs));
                }
                else if ( GetAttributeValue(element, "options", attrs).startsWith("lstAS") )
                {
                    valAuditOptions(wtc, element, clickid);
                }
                // now insert a test value if necessary
                if ( !doSave.equals(NOPOPULATION) && !doSave.equals(NORESTORATION) ) Utils.SelectOptionByText(wtc, By.id(clickid), values.get("save"));
                break;                
            case "RADIO":
                Map<String, String> rvalues = setTestValues(element, doSave, attrs);
                // it would be better to print the label of the selected option here
                // need to get child nodes ("OPTION") and find label of matching value
                if (Utils.isSelected(wtc, By.id(rvalues.get("compare"))) ) 
                {
                    tr.WriteLog("PASS: selected option for " + label + " radio is correct (" + rvalues.get("compare") + ")", LogFile.iTEST);
                } else {
                    tr.WriteLog("FAIL: selected option for " + label + " radio is incorrect", LogFile.iERROR);
                }
                // now select a test value if necessary
                if ( !doSave.equals(NOPOPULATION) && !doSave.equals(NORESTORATION) ) Utils.DoClick(wtc, By.id(rvalues.get("save")), null, 100);
                break;
            case "CHECKBOX":
                String sCompare = GetAttributeValue(element, "default", attrs);
                if ( doSave.equals(RESTORATION) || doSave.equals(NORESTORATION) )
                {
                    if (sCompare.contentEquals("on"))
                    {
                        sCompare = "off";
                    } else {
                        sCompare = "on";
                    }
                }
                Utils.isChecked(wtc, By.id(clickid), label, sCompare, true);
                // now set the checkbox value if necessary
                if ( doSave.equals(POPULATION) || ( doSave.equals(RESTORATION) && 
                        (sCompare.contentEquals("on") && !Utils.isChecked(wtc, By.id(clickid), "", sCompare, false)
                        || sCompare.contentEquals("off") && Utils.isChecked(wtc, By.id(clickid), "", sCompare, false)) ) )
                {
                    Utils.DoClick(wtc, By.id(clickid), null, 100, false);
                }
                break;
            case "LISTITEM":
                // do nothing for now. This node type just builds the columns lists
                break;
            case "BUTTON":
                String sTestVal = GetAttributeValue(element, "testval", attrs);
                String closeVal = GetCloseButtonId(getAttribute(element, "testval"));
                if (!sTestVal.isEmpty())
                {
                    if (sTestVal.startsWith("val="))
                    {
                        // do some clever validation of the button's function
                        tr.WriteLog("INFO: There's no test for the " + label + " button yet!", LogFile.iINFO);
                    } else {
                        // this button opens a dialog
                        Utils.DoClick(wtc, By.id(clickid), By.id(sTestVal), 15000);
                        if (Utils.isElementPresent(wtc, By.id(sTestVal), false))
                        {
                            tr.WriteLog("PASS: " + label + " button opens the expected dialog (" + sTestVal + ")", LogFile.iTEST);
                        } else {
                            tr.WriteLog("FAIL: " + label + " button fails to open the expected dialog (" + sTestVal + ")", LogFile.iERROR);
                        }
                        // close the dialog that was opened
                        Utils.DoClick(wtc, By.id(closeVal), null, 250);
                    }
                } else {
                    // don't know what to do with this button yet!
                    tr.WriteLog("INFO: There's no test for the " + label + " button yet!", LogFile.iINFO);
                }
                break;
            case "TABLE":
                tr.WriteLog("INFO: There's a Results Table here", LogFile.iINFO);
                break;
            case "CONTEXT":
                tr.WriteLog("INFO: There's a context menu here", LogFile.iINFO);
                break;
            default:
                tr.WriteLog("Oops! Why are we here? Unrecognised NODE value = " + element_type, LogFile.iERROR);
        }
    }
    
    private Map<String, String> setTestValues(Node element, String doSave, Map<String,String> attrs)
    {
        // sets compare and save values for elements with default and testval attributes
        String sDefault = "default" ;
        if (element.getNodeName().contentEquals("RADIO")) sDefault = "selected";        
        String sDEFAULT = GetAttributeValue(element, sDefault, attrs);
        String sPOPULATION = GetAttributeValue(element, "testval", attrs);
        String sCompare = sDEFAULT;
        String sValue = sPOPULATION;
        if ( doSave.equals(RESTORATION) || doSave.equals(NORESTORATION) ) 
        {
            sCompare = sPOPULATION;
            sValue = sDEFAULT;
        }
        Map<String, String> result = new HashMap<>();
        result.put("compare", sCompare);
        result.put("save", sValue);
        return result;
    }
    
    private void valColumns(WgnTestCase wtc, String clickid, String type, String label)
    {
        // test that the available columns list is correct in the Sort Order control
        TestRunner tr = wtc.GetTestRunner();
        Map<String,String> mycolumns;
        if (type.contentEquals("AVL"))
        {
            mycolumns = columns;
        } else {
            mycolumns = selcols;
        }
        Boolean bCols = true;
        String cols = "";
        String debug = "";
        String[] opts = Utils.GetSelectOptions(wtc, By.id(clickid));

        // debug mycolumns HashMap
        if (tr.GetLogLevel() == LogFile.iDEBUG)
        {
            Iterator<String> iterator = mycolumns.keySet().iterator();  int j = 0;
            while (iterator.hasNext()) 
            {
               String key = iterator.next().toString();
               String value = mycolumns.get(key).toString();
               tr.WriteLog("mycolumns [" + j + "]: key: '" + key + "' value: '" + value + "'", LogFile.iDEBUG);
               j++;
            }
        }
        // debug ended

        for (int i=0; i < opts.length; i++)
        {
            tr.WriteLog("Select Options value [" + i + "]: '" + opts[i] + "'", LogFile.iDEBUG);
            if (mycolumns.containsValue(opts[i]))
            {
                mycolumns.remove(opts[i]);
                if (debug.isEmpty())
                {
                    debug = opts[i];
                } else {
                    debug = debug + "; " + opts[i];
                }
            } else {
                cols = cols.concat(opts[i]).concat(" ");
                bCols = false;
            }                       
        }
        if (bCols)
        {
            if (mycolumns.isEmpty())
            {
                if (opts.length == 0)
                {
                    tr.WriteLog("PASS: The " + label + " list was empty by default", LogFile.iTEST);
                } else {
                    tr.WriteLog("PASS: All and only the expected columns were found in the " + label + " list", LogFile.iTEST);
                }
                tr.WriteLog("DEBUG: columns = " + debug, LogFile.iDEBUG);
            } else {
                String cols2 = "";
                Iterator<Entry<String, String>> it = mycolumns.entrySet().iterator();
                while (it.hasNext()) cols2 = cols2.concat(it.next().toString()).concat(" ");                    
                tr.WriteLog("FAIL: The " + label + " Columns list was missing expected entries. Check these values: " + cols2, LogFile.iERROR);
            }
        } else {
            tr.WriteLog("FAIL: The " + label + " list contained unexpected entries. Check these values: " + cols, LogFile.iERROR);
        }       
    }
    
    private void valAuditOptions(WgnTestCase wtc, Node element, String clickid)
    {
        // test that the available items lists are correct in the Audit controls
        TestRunner tr = wtc.GetTestRunner();
        String[] opts = Utils.GetSelectOptions(wtc, By.id(clickid));
        tr.WriteLog("DEBUG: " + clickid + " Select options length: " + opts.length, LogFile.iDEBUG);
        
        // get the correct available audit options list
        List<String> compareList = new ArrayList<>();
        if (clickid.contains("lstAS1"))
        {
            compareList = tr.GetAuditConfigurationManager().GetAuditStatusStrList();
        } 
        else if (clickid.contains("lstAS2"))
        {
            compareList = tr.GetAuditConfigurationManager().GetAuditActionStrList();
        } 
        else if (clickid.contains("lstAS3"))
        {
            compareList = tr.GetAuditConfigurationManager().GetAuditResnStrList();
        }
        // add the generic options to the select lists
        asany = Utils.SetParam(wtc, "asany");
        asunset = Utils.SetParam(wtc, "asunset");
        asanynon = Utils.SetParam(wtc, "asanynon") ;
        if (element.getNodeName().contentEquals("SELECT")) compareList.add(asany);
        compareList.add(asanynon);
        compareList.add(asunset);
        
        Boolean bFindme = false;
        String baddy = "";
        for (int i=0; i < opts.length; i++)
        {
            tr.WriteLog("DEBUG: Expected audit option sought: " + opts[i], LogFile.iDEBUG);
            bFindme = false;
            Iterator<String> it = compareList.iterator();
            while (it.hasNext() && !bFindme)
            {
                if (opts[i].contentEquals(it.next()))
                {
                    bFindme = true;
                    tr.WriteLog("DEBUG: Expected audit option found: " + opts[i], LogFile.iDEBUG);
                }
            }
            if (!bFindme) { baddy = opts[i]; break; }
        }            
        if (bFindme)
        {
            tr.WriteLog("PASS: The correct audit options are included in the select: " + clickid, LogFile.iTEST);
        } else {
            tr.WriteLog("FAIL: An unexpected audit option (" + baddy + ") is included in the select: " + clickid, LogFile.iERROR);
        }
    }
    
    private void invalidNumberTest(WgnTestCase wtc, Node element, String clickid, String sTabId, Map<String,String> attrs) throws Exception
    {
        TestRunner tr = wtc.GetTestRunner();
        WebDriver dr = wtc.GetWebDriver();
        dr.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
        
        // we need something to click on to trigger the onchange event on the INTEGERBOX
        if (sTabId.isEmpty()) sTabId = GetAttributeValue(element.getNextSibling(), "clickid", attrs);
        // get all the necessary values for the test
        String sp = "0";
        String dp = GetAttributeValue(element, "dp", attrs);
        Matcher m = Pattern.compile("([0-9]+):([0-9]+)").matcher(dp);
        if (m.find())
        {
            dp = m.group(1); tr.WriteLog("d.p.="+dp, LogFile.iDEBUG);
            sp = m.group(2); tr.WriteLog("s.p.="+sp, LogFile.iDEBUG);
        }
        String min = GetAttributeValue(element, "min", attrs);
        String max = GetAttributeValue(element, "max", attrs);
        String errortext = GetAttributeValue(element, "errortext", attrs);
        String maxerrortext = GetAttributeValue(element, "maxerrortext", attrs);
        String minerrortext = GetAttributeValue(element, "minerrortext", attrs);
        String noentryerrortext = GetAttributeValue(element, "noentryerrortext", attrs);
        String label = GetAttributeValue(element, "label", attrs);
        double minusVal = 1;
        double plusVal = 1;
        switch (dp) {
            case "1":
                minusVal = 0.1;
                plusVal = 0.1;
                break;
            case "2":
                minusVal = 0.01;
                plusVal = 0.01;
                break;
        }
        // handle integer fields where the upper limit is variable
        int max_val = 0;
        if (!max.isEmpty())
        {
            if (max.contentEquals(Utils.SetParam(wtc, "days")) || max.contentEquals(Utils.SetParam(wtc, "weeks"))
                    || max.contentEquals(Utils.SetParam(wtc, "months")) )
            {
                // the max period value is based on 'time since epoch (01-01-1970)' and so is a different value whenever you run the test
                // and depending whether the control is using days, weeks or months for the specified period.
                // therefore we must calculate this value on the fly.
                double time = Utils.GetTimeSinceEpoch(max, tr);
                max_val = (int)time + (int)plusVal;
                maxerrortext = maxerrortext + (int)time;
                minerrortext = minerrortext + (int)time;
            } else {
                // otherwise use the fixed max value supplied in the XML
                plusVal = Double.parseDouble(max) + plusVal;
                max_val = (int)plusVal;
            }           
        }
        
        String sCSS = "span.validationMessage";
        // test field boundaries - max
        if (!max.isEmpty())
        {
            if (sp.contentEquals("0"))
            {
                Utils.DoTypeAll(wtc, By.id(clickid), sTabId, Integer.toString(max_val));
                if (Utils.isElementPresent(wtc, By.id("Message"), false) ) sCSS = "div#Message";
                if (Utils.isTextPresent(wtc, maxerrortext, sCSS))
                {
                    tr.WriteLog("PASS: Correct error text provoked by entry > MAX boundary (" + Integer.toString(max_val) + ") in " + label + " field", LogFile.iTEST);
                } else {
                    tr.WriteLog("FAIL: Expected error text not provoked by entry > MAX boundary (" + Integer.toString(max_val) + ") in " + label + " field, expected text: " + maxerrortext, LogFile.iERROR);
                }
            } else {
                int spi = Integer.parseInt(sp);
                String typeme = "";
                for (int j=0; j<=spi; j++)
                {
                    typeme = typeme.concat("9");
                }
                tr.WriteLog("typeme="+typeme, LogFile.iDEBUG);
                Utils.sendKeys(wtc, By.id(clickid), typeme);
                Utils.sendTAB(wtc, By.id(clickid), sTabId);
                if (Utils.GetText(wtc, By.id(clickid), true).length() > spi)
                {
                    tr.WriteLog("FAIL: " + label + " field allowed entry of greater than " + Integer.toString(spi) + "significant places", LogFile.iERROR);
                } else {
                    tr.WriteLog("PASS: " + label + " field only allows entry up to " + Integer.toString(spi) + " significant places", LogFile.iTEST);
                }
            }
            // sometimes an error dialog is opened
            if (Utils.isElementPresent(wtc, By.id("Message"), false) && Utils.isElementPresent(wtc, By.id("btn_Message.Close"), false) ) 
            {
                Utils.DoClick(wtc, By.id("btn_Message.Close"), null, 100, false);
            }
            // bug 65492: and sometimes a Yes/No message is also present *** ONLY IF MaximumResultsSetSize regkey value has been raised ***
            if (Utils.isElementPresent(wtc, By.id("Message"), false) && Utils.isElementPresent(wtc, By.id("btn_Message.Yes"), false) ) 
            {
                Utils.DoClick(wtc, By.id("btn_Message.Yes"), null, 100, false);
            }           
        } else {
            tr.WriteLog("INFO: No Max boundary provided for the " + label + " field, not tested", LogFile.iINFO);
        }
        // test field boundaries - min
        if (!min.isEmpty())
        {
            minusVal = Double.parseDouble(min) - minusVal;
            int i = (int)minusVal;
            Utils.DoTypeAll(wtc, By.id(clickid), sTabId, Integer.toString(i));
            if (Utils.isElementPresent(wtc, By.id("Message"), false) ) 
            {
                sCSS = "div#Message";
            } else {
                sCSS = "span.validationMessage";
            }
            if (Utils.isTextPresent(wtc, minerrortext, sCSS))
            {
                tr.WriteLog("PASS: Correct error text provoked by entry < MIN boundary (" + Integer.toString(i) + ") in " + label + " field", LogFile.iTEST);
            } else {
                tr.WriteLog("FAIL: Expected error text not provoked by entry < MIN boundary (" + Integer.toString(i) + ") in " + label + " field: " + minerrortext, LogFile.iERROR);
            }
            // sometimes an error dialog is opened
            if (Utils.isElementPresent(wtc, By.id("Message"), false) )
            {
                Utils.DoClick(wtc, By.id("btn_Message.Close"), null, 100, false);
            }            
        } else {
            tr.WriteLog("INFO: No Min boundary provided for the " + label + " field, not tested", LogFile.iINFO);
        }        
        // alpha text entry
        Utils.DoTypeAll(wtc, By.id(clickid), sTabId, "abc123");
        if (Utils.isElementPresent(wtc, By.id("Message"), false) ) 
        {
            sCSS = "div#Message";
        } else {
            sCSS = "span.validationMessage";
        }
        if (Utils.isTextPresent(wtc, errortext, sCSS))
        {
            tr.WriteLog("PASS: Correct error text provoked by alpha text in " + label + " field", LogFile.iTEST);
        } else {
            tr.WriteLog("FAIL: Expected error text not provoked by alpha text in " + label + " field: " + errortext, LogFile.iERROR);
        }
        // negative value entry
        Utils.DoTypeAll(wtc, By.id(clickid), sTabId, "-10");
        if (Utils.isElementPresent(wtc, By.id("Message"), false) ) 
        {
            sCSS = "div#Message";
        } else {
            sCSS = "span.validationMessage";
        }        
        if (Utils.isTextPresent(wtc, errortext, sCSS))
        {
            tr.WriteLog("PASS: Correct error text provoked by negative value in " + label + " field", LogFile.iTEST);
        } else {
            tr.WriteLog("FAIL: Expected error text not provoked by negative value in " + label + " field: " + errortext, LogFile.iERROR);
        }         
        // blank value entry
        if (!noentryerrortext.isEmpty())
        {
            Utils.DoTypeAll(wtc, By.id(clickid), sTabId, "");
            if (Utils.isElementPresent(wtc, By.id("Message"), false) ) 
            {
                sCSS = "div#Message";
            } else {
                sCSS = "span.validationMessage";
            }            
            if (Utils.isTextPresent(wtc, noentryerrortext, sCSS))
            {
                tr.WriteLog("PASS: Correct error text provoked by blank value in " + label + " field", LogFile.iTEST);
            } else {
                tr.WriteLog("FAIL: Expected error text not provoked by blank value in " + label + " field: " + noentryerrortext, LogFile.iERROR);
            }
            // sometimes an error dialog is opened
            if (Utils.isElementPresent(wtc, By.id("Message"), false) )
            {
                Utils.DoClick(wtc, By.id("btn_Message.Close"), null, 100, false);
            }
        }
        // restore default value
        String sDefault = GetAttributeValue(element, "default", attrs);
        Utils.DoTypeAll(wtc, By.id(clickid), sTabId, sDefault);
        if (Utils.isElementPresent(wtc, By.id(sTabId), false))
        {
            Utils.DoClick(wtc, By.id(sTabId), null, 100);
        } else {
            Utils.sendKeys(wtc, By.id(clickid), "\t");
        }
        
        // check all error text cleared
        if (!Utils.isElementPresent(wtc, By.cssSelector("span.validationMessage"), false) && !Utils.isElementPresent(wtc, By.id("Message"), false) )
        {
            tr.WriteLog("PASS: Error text cleared by valid entry in " + label + " field", LogFile.iTEST);
        } else {
            tr.WriteLog("FAIL: Error text not cleared by valid entry in " + label + " field", LogFile.iERROR);
        }
        dr.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
    }
    
}
