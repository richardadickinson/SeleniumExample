
package common;

/**
 *
 * @author smaho01
 */

import java.util.*;

public class AuditActionValue
{
    private String[] mFilter;
    private String mText;
    private int mWKIndex;
    private List<String> mAvailableAuditResolutions;

    public AuditActionValue(String text, int wkIndex)
    {
        mText = text;
        mWKIndex = wkIndex;
        mAvailableAuditResolutions = new ArrayList<String>();
    }

    public void SetFilter(String sFilter, List<AuditResolutionValue> auditResolutions)
    {
        mFilter = sFilter.split(":");

        for (int i=0; i < mFilter.length; i++)
        {
            // You get all audit action values by default, so just ignore those that are not set
            if (i < auditResolutions.size())
            {
                AuditResolutionValue resolutionValue = auditResolutions.get(Integer.parseInt(mFilter[i]));
                mAvailableAuditResolutions.add(resolutionValue.GetText());
            }
        }
    }

    public String GetText()
    {
        return mText;
    }

    public int GetWKIndex()
    {
        return mWKIndex;
    }

    public List<String> GetAvailableAuditResolutions()
    {
        return mAvailableAuditResolutions;
    }

    public boolean CheckConfiguration()
    {
        return true;
    }
}
