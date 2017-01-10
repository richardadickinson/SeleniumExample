//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 * @author dicri02
 * ported from iConsole test project 
 * 25/11/2011
 */

import common.*;
import org.openqa.selenium.*;
import java.util.*;

public class WgnTestStep
{
    protected TestRunner mTestRunner;
    protected WebDriver mDriver;
    protected String mURL;
    protected Map<String, String> mParameters;
    protected String mFailedReason;
    protected Boolean mFailed;
    protected WgnTestCase mTestCase;
    protected String mName;

    public WgnTestStep()
    {
        mParameters = new HashMap<String, String>();
        mFailed = false;
    }

    public void SetTestCase(WgnTestCase tc)
    {
        mTestCase = tc;
    }

    public void SetTestStepName(String sName)
    {
        mName = sName;
    }

    public void SetTestRunner(TestRunner r)
    {
        mTestRunner = r;
        mDriver = r.GetDriver();                
    }

    public void SetURL(String url)
    {
        mURL = url;
    }

    public void SetParameters(Map<String, String> parameters)
    {
        mParameters.putAll(parameters);
    }

    public String GetFailureReason()
    {
        return mFailedReason;
    }

    public boolean HasFailed()
    {
        return mFailed;
    }

    public String GetName()
    {
        return mName;
    }

    public void Run() throws Exception
    {
        
    }

    public void ReportError(String sErrorMessage)
    {
        System.err.println(sErrorMessage);
        mFailed = true;
        mFailedReason = sErrorMessage;
    }
}
