//Copyright © 2011 CA. All rights reserved.

package testcases;

/**
 *
 * @author dicri02
 */

public class TestUser {

//    private String mRef;
    private String mName;
    private String mValue;

    public TestUser(String ref, String name, String value)
    {
//       mRef = ref;
        mName = name;
        mValue = value;
    }

    public String GetUserName()
    {
        return mName;
    }

    public String GetUserValue()
    {
        return mValue;
    }

}
