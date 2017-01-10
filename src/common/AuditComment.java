
package common;

/**
 *
 * @author smaho01
 */
public class AuditComment
{
    private String mText;
    private int mWKIndex;

    public AuditComment(String text, int wkIndex)
    {
        mText = text;
        mWKIndex = wkIndex;
    }

    public String GetText()
    {
        return mText;
    }

    public int GetWKIndex()
    {
        return mWKIndex;
    }
}
