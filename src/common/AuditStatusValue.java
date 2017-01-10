
package common;

/**
 *
 * @author smaho01
 */

import java.util.*;

public class AuditStatusValue
{
    private boolean mbOtherFieldsCompleted;
    private String mText;
    private String[] mFilter;
    private List<String> mAvailableAuditActions;
    private int mWKIndex;

    public AuditStatusValue(String text,int wkindex)
    {
        mWKIndex = wkindex;

        if (text == null)
        {
            text = "";
        }

        if(text.endsWith("*"))
        {
            mbOtherFieldsCompleted = true;
            mText = text.substring(0, text.length()-1);
        }
        else
        {
            mText = text;
        }

        mAvailableAuditActions = new ArrayList<String>();
    }

    public String GetAuditStatusText()
    {
        return mText;
    }

    public int GetWKIndex()
    {
        return mWKIndex;
    }

    boolean OtherFieldsMustBeCompleted()
    {
        return mbOtherFieldsCompleted;
    }

    public void SetFilter(String sFilter, List<AuditActionValue> actionValues)
    {
        // Filter is a ':' delimited string, eg 1:2:3:4:5:6
        mFilter = sFilter.split(":");

        for (int i=0; i < mFilter.length; i++)
        {
            // You get all action values by default, so just ignore those that haven't been set
            if (i < actionValues.size())
            {
                String actionValue = actionValues.get(Integer.parseInt(mFilter[i])).GetText();
                mAvailableAuditActions.add(actionValue);
            }
        }
    }

    public List<String> GetAvailableAuditOptions()
    {
        return mAvailableAuditActions;
    }

    public boolean CheckConfiguration()
    {
        return true;
    }
}
