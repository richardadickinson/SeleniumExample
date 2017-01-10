//Copyright © 2011 CA. All rights reserved.

package common;

/**
 * @author smaho01
 * ported from iConsole test project 
 * 25/11/2011
 **/

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class SelectElement
{
    private Map<String,String> mOptions;
    private String msRawOptionsXml;
    private String mError;
    private String mDebug;

    public SelectElement(String sOptionsXml)
    {
        msRawOptionsXml = sOptionsXml;
        mOptions = new LinkedHashMap<String, String>();
    }

    public boolean Parse()
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(msRawOptionsXml)));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("option");
            if (nodeList != null && nodeList.getLength() > 0)
            {
                for (int i=0;i<nodeList.getLength();i++)
                {
                    Element optionsNode = (Element)nodeList.item(i);
                    String value = optionsNode.getAttribute("value");
                    String text = optionsNode.getAttribute("text");

                    mOptions.put(value, text);
                }
            }
        }
        catch(ParserConfigurationException pce)
        {
            mError = String.format("Failed to parse options xml due to ParserConfigurationException: %s", pce.getMessage());
            return false;
        }
        catch(SAXException se)
        {
            mError = String.format("Failed to parse options xml due to SAXException: %s", se.getMessage());
            return false;
        }
        catch(IOException ioe)
        {
            mError = String.format("Failed to parse options xml due to IOException: %s", ioe.getMessage());
            return false;
        }

        return true;
    }

    public boolean Compare(String sName, List<String> availableAuditOptions)
    {
        boolean bRet = true;

        Iterator<Entry<String, String>> it = mOptions.entrySet().iterator();
        StringBuilder sbAvailableAuditOptions = new StringBuilder();
        StringBuilder sbActualAuditOptions = new StringBuilder();
        
        Iterator<String> itOptions = availableAuditOptions.iterator();
        boolean bFirst = true;
        while (itOptions.hasNext())
        {
            if (!bFirst)
            {
                sbAvailableAuditOptions.append(",");
            }
            bFirst = false;
            sbAvailableAuditOptions.append(itOptions.next());
        }
        
        bFirst = true;
        if (!it.hasNext())
        {
            bRet = false;
        }
        while(it.hasNext())
        {
            Entry<String, String> e = it.next();

            String text = (String)e.getKey();
            String value = (String)e.getValue();

            if (bRet)
            {
                if (!value.equals("-1"))
                {
                    if (!availableAuditOptions.contains(text))
                    {
                        bRet = false;
                    }
                }
            }

            if (!bFirst)
            {
                sbActualAuditOptions.append(",");
            }
            bFirst = false;
            sbActualAuditOptions.append(text);
        }

        if (!bRet)
        {
            mError = String.format("ERROR: " + sName + " dropdown does not contain the correct entries, should be {%s} but has {%s}", sbActualAuditOptions.toString(), sbAvailableAuditOptions.toString());
        }
        //mDebug = String.format("Database: {%s}, iConsole: {%s}", sbAvailableAuditOptions.toString(), sbActualAuditOptions.toString());
        mDebug = String.format("%s", sbActualAuditOptions.toString());
        
        return bRet;
    }

    public boolean Compare(AuditActionValue v)
    {

        return true;
    }

    public String GetErrorDescription()
    {
        return mError;
    }

    public String GetDebugText()
    {
        return mDebug;
    }
}
