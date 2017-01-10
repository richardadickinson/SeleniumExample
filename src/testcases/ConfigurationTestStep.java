/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testcases;

/**
 *
 * @author smaho01
 */

import java.util.*;

import common.*;

public class ConfigurationTestStep
{
    private String mJavaClassName;
    private Map<String, String> mParameters;
    private String mName;

    public ConfigurationTestStep(String javaClassName)
    {
        mParameters = new HashMap<String, String>();
        mJavaClassName = javaClassName;
    }

    public void SetTestStepName(String sName)
    {
        mName = sName;
    }

    public String GetName()
    {
        return mName;
    }

    public WgnTestStep TestStepFactory(TestRunner r, String sURL) throws Exception
    {
        WgnTestStep wts = null;

        try
        {
            Class<?> c = Class.forName(mJavaClassName);
            wts = (WgnTestStep)c.newInstance();
            wts.SetTestRunner(r);
            wts.SetURL(sURL);
            wts.SetParameters(mParameters);
        }
        catch(NoClassDefFoundError ex)
        {
            System.err.println(String.format("Class Not Found Exception %s attempting to create test step %s", ex.getMessage(), mJavaClassName));
            //r.WriteLogFileEntry("Class Not Found Exception attempting to create test step " + mJavaClassName + " :" + ex.getMessage(), LogFile.iERROR);
            throw new ClassNotFoundException("Class Not Found Exception attempting to create test step " + mJavaClassName + " :" + ex.getMessage());
        }
        catch(Exception e)
        {
            System.err.println(String.format("Exception %s attempting to create test step %s", e.getMessage(), mJavaClassName));
            r.WriteLog("Exception attempting to create test step " + e.getMessage(), LogFile.iERROR);
            throw e;
        }

        return wts;
    }

    public void AddParameter(String key, String value)
    {
        mParameters.put(key,value);
    }
}
