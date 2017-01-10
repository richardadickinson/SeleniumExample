//Copyright © 2011 CA. All rights reserved.

package common;

/**
 * @owner dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.util.*;
import testcases.*;
import webdriver.*;

public class ConfigurationFile
{
    private String mFileName;
    private String mGFileName;
    private String mSysFileName;
    private String mFailureMessage;
    protected Map<String, TestUser> mUsers;
    private String mCMSServer;
    private String mDatabaseName;
    private String mDatabaseUserName;
    private String mDatabasePassword;
    private String mSeleniumServerHost;
    private String mTargetBrowser;
    private String mFFDomaintitle;
    private String mIEDomaintitle;
    private String mDomainUser;
    private String mDomainUserPW;
    private String mExtrasPath;
    private String mProtocol;
    private String mWebServer;
    private String mVirtualDirectory;
    private String mBrowserStr;
    private String mDBType;
    private String mDatabaseServer;
    private String mPop3Host;
    private String mFirebug;
    private int mLogLevel;
    private final Object lock;   // only 1 thread may log at a time
    private List<WgnTestCase> mTests;

    // retrieves string values passed in from the cmd line args
    public final String GetCmdString(String name, HashMap<String, Object> cmdTable) {
            String sub;
            synchronized (lock) {
                sub = (String)cmdTable.get(name);
            }
            String retval = "";
            if (sub != null) {
                retval = sub;
            }
            return retval;
    }

    public ConfigurationFile()
    {
        mUsers = new HashMap<>();
        lock = new Object();
        HashMap<String, Object> cmdTable = WebDriver.GetCmdTable();
        if (!GetCmdString("sys", cmdTable).isEmpty())
        {
            mSysFileName = GetCmdString("sys", cmdTable);
        } else {
            mSysFileName = "sys.xml";
        }
        if (!GetCmdString("cfg", cmdTable).isEmpty())
        {
            mFileName = GetCmdString("cfg", cmdTable);
        } else {
            mFileName = "cfg.xml";
        }
        if (!GetCmdString("gbl", cmdTable).isEmpty())
        {
            mGFileName = GetCmdString("gbl", cmdTable);
        } else {
            mGFileName = "gbl.xml";
        }
        mTests = new ArrayList<>();
    }

    public String GetSeleniumServerHost()
    {
        return mSeleniumServerHost;
    }

    public String GetTargetBrowser()
    {
        return mTargetBrowser;
    }

    public String GetDomainDialogTitle()
    {
        if (GetTargetBrowser().contains("firefox"))
        {
            return mFFDomaintitle;
        } else {
            return mIEDomaintitle;
        }
    }
    
    public String GetExtrasPath()
    {
        return mExtrasPath;
    }

    public String GetDomainUser()
    {
        return mDomainUser;
    }

    public String GetDomainUserPassword()
    {
        return mDomainUserPW;
    }

    public String GetTestBrowser()
    {
        return mBrowserStr;
    }

    public String GetProtocol()
    {
        return mProtocol;
    }

    public String GetVirtualDirectory()
    {
        return mVirtualDirectory;
    }

    public String GetWebServer()
    {
        return mWebServer;
    }

    public String GetCMSServer()
    {
        return mCMSServer;
    }

    public String GetDatabaseName()
    {
        return mDatabaseName;
    }

    public String GetDatabaseUserName()
    {
        return mDatabaseUserName;
    }

    public String GetDatabasePassword()
    {
        return mDatabasePassword;
    }

    public String GetFailureMessage()
    {
        return mFailureMessage;
    }

    public String GetDbType()
    {
        return mDBType;
    }

    public String GetDatabaseServer()
    {
        return mDatabaseServer;
    }

    public String GetPop3Host()
    {
        return mPop3Host;
    }
    
    public String GetFirebug()
    {
        return mFirebug;
    }

    public int GetLogLevel()
    {
        return mLogLevel;
    }

    public List<WgnTestCase> GetTestList()
    {
        return mTests;
    }

    //@SuppressWarnings("UseSpecificCatch")
    public boolean Load()
    {
        Boolean bLoadTests = false;
        
        try
        {
            File file = new File(mFileName);
            File gfile = new File(mGFileName);
            File sysfile = new File(mSysFileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilderFactory gdbf = DocumentBuilderFactory.newInstance();
            DocumentBuilderFactory sysdbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            DocumentBuilder gdb = gdbf.newDocumentBuilder();
            DocumentBuilder sysdb = sysdbf.newDocumentBuilder();
            Document doc = db.parse(file);
            Document gdoc = gdb.parse(gfile);
            Document sysdoc = sysdb.parse(sysfile);
            doc.getDocumentElement().normalize();
            gdoc.getDocumentElement().normalize();
            sysdoc.getDocumentElement().normalize();
            
            NodeList nodeList = sysdoc.getElementsByTagName("config");
            if (nodeList != null && nodeList.getLength() > 0)
            {
                Element configNode = (Element)nodeList.item(0);

                mCMSServer = configNode.getAttribute("cms");
                if (mCMSServer.isEmpty())
                {
                    mFailureMessage = "CMS server attribute is empty";
                    return false;
                }

                mDatabaseName = configNode.getAttribute("dbname");
                if (mDatabaseName.isEmpty())
                {
                    mFailureMessage = "database attribute is empty";
                    return false;
                }

                mDatabaseUserName = configNode.getAttribute("dbusername");
                if (mDatabaseUserName.isEmpty())
                {
                    mFailureMessage = "database username attribute is empty";
                    return false;
                }

                mDatabasePassword = configNode.getAttribute("dbpassword");
                if (mDatabasePassword.isEmpty())
                {
                    mFailureMessage = "database password attribute is empty";
                    return false;
                }

                mProtocol = configNode.getAttribute("protocol");
                if (mProtocol.isEmpty())
                {
                    mProtocol = "http://";
                }

                mWebServer = configNode.getAttribute("webserver");
                if (mWebServer.isEmpty())
                {
                    mFailureMessage = "Webserver attribute is empty";
                    return false;
                }

                mVirtualDirectory = configNode.getAttribute("virtualdir");
                if (mWebServer.isEmpty())
                {
                    mFailureMessage = "Virtual Directory attribute is empty";
                    return false;
                }

                mSeleniumServerHost = configNode.getAttribute("seleniumhost");
                if (mSeleniumServerHost.isEmpty())
                {
                    mFailureMessage = "selenium server host attribute is empty";
                    return false;
                }
                
                mTargetBrowser = configNode.getAttribute("targetbrowser");
                if (mTargetBrowser.isEmpty())
                {
                    mFailureMessage = "Target browser attribute is empty";
                    return false;                    
                }
                mFFDomaintitle = configNode.getAttribute("ffdomainlogin");
                if (mFFDomaintitle.isEmpty())
                {
                    mFFDomaintitle = "";
                }
                mIEDomaintitle = configNode.getAttribute("iedomainlogin");
                if (mIEDomaintitle.isEmpty())
                {
                    mIEDomaintitle = "";
                }

                mDomainUser = configNode.getAttribute("domainuser");
                if (mDomainUser.isEmpty())
                {
                    mDomainUser = "";
                }
                mDomainUserPW = configNode.getAttribute("domainpassword");
                if (mDomainUserPW.isEmpty())
                {
                    mDomainUserPW = "";
                }

                mExtrasPath = configNode.getAttribute("extraspath");
                if (mExtrasPath.isEmpty())
                {
                    mExtrasPath = "";
                }
                
                mDBType = configNode.getAttribute("dbtype");
                if (mDBType.isEmpty())
                {
                    mDBType = "sqlserver";
                }

                mDatabaseServer = configNode.getAttribute("dbserver");
                if (mDatabaseServer.isEmpty())
                {
                    mFailureMessage = "database server name is empty";
                    return false;
                }
                mPop3Host = configNode.getAttribute("pop3host");
                if (mPop3Host.isEmpty())
                {
                    mFailureMessage = "pop3host server name is empty";
                    return false;
                }
                try
                {
                    mFirebug = configNode.getAttribute("firebug");
                }
                catch (Exception e)
                {
                    mFirebug = "";
                }

                String s = configNode.getAttribute("loglevel");
                if (s.isEmpty())
                {
                    mLogLevel = LogFile.iDEBUG; // set level to debug
                } else {
                    mLogLevel = Integer.parseInt(s.trim());
                }
                // collect the test users
                NodeList userList = sysdoc.getElementsByTagName("users");
                if (userList != null && userList.getLength() > 0)
                {
                    Element userlist = (Element)userList.item(0);
                    NodeList users = userlist.getElementsByTagName("user");
                    for (int Index=0; Index < users.getLength(); Index++)
                    {
                        Element user = (Element)users.item(Index);
                        String nodeName = user.getNodeName();
                        if (nodeName.equals("user"))
                        {
                            TestUser u = new TestUser(user.getAttribute("ref"),user.getAttribute("name"),user.getAttribute("value"));
                            mUsers.put(user.getAttribute("ref"),u);
                        }
                    }
                }

                // now get the global parameters and test cases
                NodeList globalList = gdoc.getElementsByTagName("global");
                NodeList testList = doc.getElementsByTagName("test");
                if (testList.getLength() > 0)
                {
                    bLoadTests = LoadTests(testList, globalList);
                }
                else
                {
                    mFailureMessage = "no <test> elements have been defined";
                    return false;
                }
            }
            else
            {
                mFailureMessage = "<config> element is missing";
                return false;
            }
        }
        catch(Exception e)
        {            
            mFailureMessage = "Failed to load configuration file due to exception: " + e.getMessage();
            return false;
        }
        
        if (bLoadTests)
        {
            return true;
        } else {
            return false;
        }
        
    }

    private boolean LoadTests(NodeList testList, NodeList globalList)
    {
        for (int index=0; index < testList.getLength(); index++)
        {
            Element test = (Element)testList.item(index);
            WgnTestCase wtc = new WgnTestCase();
            wtc.SetName(test.getAttribute("name"));
            // add the test users to the test case
            wtc.SetTestUsers(mUsers);
            // is the test running with SSO or under a named user?
            if (test.getAttribute("sso") != null && test.getAttribute("sso").equals("false"))
            {
                try
                {
                    wtc.isSSO(test.getAttribute("sso"));
                    TestUser primary = (TestUser)mUsers.get(test.getAttribute("username"));
                    wtc.SetAttribute("username", primary.GetUserName());
                    wtc.SetAttribute("password", primary.GetUserValue());
                } 
                catch (Exception e) 
                {
                    System.err.println("ERROR setting primary user: " + e.toString());
                    mFailureMessage = "No matching user found in test configuration for '" + test.getAttribute("username") + "'";
                    return false;
                }
                if (test.getAttribute("rlsusername") != null && !test.getAttribute("rlsusername").isEmpty())
                {
                    try
                    {
                        TestUser secondary = (TestUser)mUsers.get(test.getAttribute("rlsusername"));
                        wtc.SetAttribute("rlsusername", secondary.GetUserName());
                        wtc.SetAttribute("rlspassword", secondary.GetUserValue());
                    } 
                    catch (Exception e) 
                    {
                        System.err.println("ERROR setting secondary user: " + e.toString());
                        mFailureMessage = "No matching user found in test configuration for '" + test.getAttribute("rlsusername") + "'";
                        return false;
                    }
                }
            }
            // always set the admin user for wgncleanup()
            try
            {
                TestUser admin = (TestUser)mUsers.get("admin");
                wtc.SetAttribute("adminuser", admin.GetUserName());
                wtc.SetAttribute("adminpw", admin.GetUserValue());
            } catch (Exception e) {
                System.err.println("ERROR setting Admin user: " + e.getMessage());
                mFailureMessage = "No Admin user found in test configuration";
                return false;
            }

            // load the parameter list for the new test step
            NodeList testSteps = test.getElementsByTagName("step");
            for (int cIndex=0; cIndex < testSteps.getLength(); cIndex++)
            {
                Element step = (Element)testSteps.item(cIndex);
                String nodeName = step.getNodeName();

                if (nodeName.equals("step"))
                {
                    ConfigurationTestStep t = new ConfigurationTestStep(step.getAttribute("classname"));
                    t.SetTestStepName(step.getAttribute("name"));
                    NodeList parameterList = step.getElementsByTagName("parameter");
                    // load the test step specific params to the wgnteststep
                    if (parameterList.getLength() > 0)
                    {
                        for (int paramIndex=0; paramIndex < parameterList.getLength(); paramIndex++)
                        {
                            Element param = (Element)parameterList.item(paramIndex);
                            t.AddParameter(param.getAttribute("name"), param.getAttribute("value"));
                            wtc.AddParameter(param.getAttribute("name"), param.getAttribute("value"));
                        }
                    }
                    // load the global list of parameters to the wgntestcase & wgnteststep
                    for(int idx=0;idx<globalList.getLength();idx++)
                    {
                        Element globals = (Element)globalList.item(idx);

                        NodeList gParams = globals.getElementsByTagName("parameter");
                        for(int gIdx=0;gIdx<gParams.getLength();gIdx++)
                        {
                            Element gParamList = (Element)gParams.item(gIdx);
                            String nodeName2 = gParamList.getNodeName();

                            if (nodeName2.equals("parameter"))
                            {
                                Element param = (Element)gParams.item(gIdx);
                                t.AddParameter(param.getAttribute("name"), param.getAttribute("value"));
                                if (!param.getAttribute("desc").isEmpty())
                                {
                                    t.AddParameter(param.getAttribute("name")+"_desc", param.getAttribute("desc"));
                                    wtc.AddParameter(param.getAttribute("name")+"_desc", param.getAttribute("desc"));
                                }
                                wtc.AddParameter(param.getAttribute("name"), param.getAttribute("value"));
                            }
                            else
                            {
                                mFailureMessage = String.format("incorrect tag '%s' found in test configuration",nodeName2);
                                return false;
                            }
                        }
                    }
                    // create the new test step in the testcase
                    wtc.AddTestStep(t);
                }
                else
                {
                    mFailureMessage = String.format("incorrect tag '%s' found in test configuration",nodeName);
                    return false;
                }
            }
            wtc.SetParameters(wtc.GetParameters());
            mTests.add(wtc);
        }

        return true;
    }
}
