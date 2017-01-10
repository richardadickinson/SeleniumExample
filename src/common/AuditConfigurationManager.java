
package common;

/**
 *
 * @author smaho01
 */

import java.util.*;
import java.sql.*;
import com.ddtek.jdbc.extensions.ExtEmbeddedConnection;

public class AuditConfigurationManager
{
    private List<AuditStatusValue> mAvailableAuditStatuses;
    private List<AuditActionValue> mAvailableAuditActions;
    private List<AuditButton> mAvailableAuditButtons;
    private List<AuditResolutionValue> mAvailableAuditResolutions;
    private List<AuditComment> mAvailableComments;
    private List<String> mStringList;
    private Connection mDatabaseConnection;
    private TestRunner mTestRunner;

    public String[] bulkAuditButtonIds =
    {
        "btn_Results.ToolAudit01",
        "btn_Results.ToolAudit02",
        "btn_Results.ToolAudit03",
        "btn_Results.ToolAudit04",
        "btn_Results.ToolAudit05",
        "btn_Results.ToolAudit06",
        "btn_Results.ToolAudit07",
        "btn_Results.ToolAudit08",
        "btn_Results.ToolAudit09",
        "btn_Results.ToolAudit10"
    };

    public String[] singleAuditButtonIds =
    {
        "btn_Pane.AuditButton01",
        "btn_Pane.AuditButton02",
        "btn_Pane.AuditButton03",
        "btn_Pane.AuditButton04",
        "btn_Pane.AuditButton05",
        "btn_Pane.AuditButton06",
        "btn_Pane.AuditButton07",
        "btn_Pane.AuditButton08",
        "btn_Pane.AuditButton09",
        "btn_Pane.AuditButton10"
    };

    public AuditConfigurationManager(TestRunner r)
    {
        mAvailableAuditStatuses = new ArrayList<AuditStatusValue>();
        mAvailableAuditResolutions = new ArrayList<AuditResolutionValue>();       
        mAvailableAuditActions = new ArrayList<AuditActionValue>();
        mAvailableAuditButtons = new ArrayList<AuditButton>();
        mAvailableComments = new ArrayList<AuditComment>();

        mTestRunner = r;
    }

    public AuditStatusValue GetAuditStatusValue(int wkindex)
    {
        AuditStatusValue v = mAvailableAuditStatuses.get(wkindex);
        return v;
    }

    public AuditActionValue GetAuditActionValue(int wkindex)
    {
        AuditActionValue a = new AuditActionValue("Failed to find Action for this wkindex", wkindex);
        for (int i=0; i < mAvailableAuditActions.size(); i++)
        {
            if (mAvailableAuditActions.get(i).GetWKIndex() == wkindex)
            {
                a = mAvailableAuditActions.get(i);
            }
        }
        return a;
    }

    public AuditResolutionValue GetAuditResolution(int wkindex)
    {
        AuditResolutionValue r = new AuditResolutionValue("Failed to find Resolution for this wkindex", wkindex);
        for (int i=0; i < mAvailableAuditResolutions.size(); i++)
        {
            if (mAvailableAuditResolutions.get(i).GetWKIndex() == wkindex)
            {
                r = mAvailableAuditResolutions.get(i);
            }
        }
        return r;
    }

    public List<AuditStatusValue> GetAuditStatusValues()
    {
        return mAvailableAuditStatuses;
    }

    public List<String> GetAuditStatusStrList()
    {
        mStringList = new ArrayList<String>();
        for (int i=0; i < mAvailableAuditStatuses.size(); i++)
        {
            mStringList.add(mAvailableAuditStatuses.get(i).GetAuditStatusText());
        }

        return mStringList;
    }

    public List<String> GetAuditResnStrList()
    {
        mStringList = new ArrayList<String>();
        for (int i=0; i < mAvailableAuditResolutions.size(); i++)
        {
            mStringList.add(mAvailableAuditResolutions.get(i).GetText());
        }

        return mStringList;
    }

    public List<String> GetAuditActionStrList()
    {
        mStringList = new ArrayList<String>();

        for (int i=0; i < mAvailableAuditActions.size(); i++)
        {
            mStringList.add(mAvailableAuditActions.get(i).GetText());
        }

        return mStringList;
    }

    public List<AuditButton> GetAvailableAuditButtons()
    {
        return mAvailableAuditButtons;
    }

    public AuditComment GetComment(int wkindex)
    {
        AuditComment c = mAvailableComments.get(wkindex);
        return c;
    }

    public List<String> GetAuditCommentsStrList()
    {
        mStringList = new ArrayList<String>();

        for (int i=0; i < mAvailableComments.size(); i++)
        {
            mStringList.add(mAvailableComments.get(i).GetText());
        }

        return mStringList;
    }

    public boolean ConnectToDB() throws ClassNotFoundException, SQLException
    {
        boolean bRet = true;
        ConfigurationFile config = mTestRunner.GetConfigurationFile();
        StringBuilder sbDatabaseURL = new StringBuilder();

        if (config.GetDbType().toLowerCase().equals("oracle"))
        {
            //String url = "jdbc:wgn:oracle://ta-dev1-net:1521;SID=apmdevmb"; // use your hostname and port number here
            //String url = "jdbc:wgn:oracle://593-DEV-ORCL1.ca.com:1521;SID=apmdev11";

            sbDatabaseURL.append("jdbc:wgn:oracle://");
            sbDatabaseURL.append(config.GetDatabaseServer());
            sbDatabaseURL.append(":");
            sbDatabaseURL.append("1521");
            sbDatabaseURL.append(";SID=");
            sbDatabaseURL.append(config.GetDatabaseName());

            //String login = "apmuser"; // use your login here
            //String password = "test"; // use your password here

            Class.forName("com.wgn.jdbc.oracle.OracleDriver");
            mDatabaseConnection = DriverManager.getConnection(sbDatabaseURL.toString(),config.GetDatabaseUserName(),config.GetDatabasePassword());

            if (mDatabaseConnection instanceof ExtEmbeddedConnection)
            {
                ExtEmbeddedConnection embeddedCon = (ExtEmbeddedConnection)mDatabaseConnection;
                boolean unlocked = embeddedCon.unlock("WiGaN");
                if (!unlocked)
                {
                    bRet = false;
                }
            }
            else
            {
                bRet = false;
            }
        }
        else
        {
            sbDatabaseURL.append("jdbc:sqlserver://");
            sbDatabaseURL.append(config.GetDatabaseServer());
            sbDatabaseURL.append(":1433;databaseName=");
            sbDatabaseURL.append(config.GetDatabaseName());
            sbDatabaseURL.append(";username=");
            sbDatabaseURL.append(config.GetDatabaseUserName());
            sbDatabaseURL.append(";password=");
            sbDatabaseURL.append(config.GetDatabasePassword());
            sbDatabaseURL.append(";");

            //String url = "jdbc:sqlserver://ta-hms-w2k8:1433;databaseName=WGN_TA-HMS-W2K8;username=sa;password=H0ward7;";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            mDatabaseConnection = DriverManager.getConnection(sbDatabaseURL.toString());
        }

        return bRet;
    }

    public Boolean LoadAuditConfig()
    {
        Boolean rtn;
        rtn = LoadAuditResolutions();
        if (rtn) rtn = LoadAuditActions();
        if (rtn) rtn = LoadAuditStatuses();
        if (rtn) rtn = LoadAuditButtonStatuses();
        if (rtn) rtn = LoadComments();
        return rtn;
    }

    private Boolean LoadComments()
    {
        try
        {
            String sql = "SELECT WKString FROM WgnWellKnownString WHERE WKType=3 ORDER BY WKIndex";
            PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            int count=0;
            while(rs.next())
            {
                String s = rs.getString("WKString");
                mAvailableComments.add(new AuditComment(s,count));

                count++;
            }
            rs.close();
            statement.close();
        } 
        catch (Exception e)
        {
            System.err.println("Exception getting audit comments from DB:" + e.getMessage());
            mTestRunner.WriteLog("Exception getting audit comments from DB:" + e.getMessage(), LogFile.iERROR);
            return false;
        }
        return true;
    }

    private Boolean LoadAuditResolutions()
    {
        try 
        {
            String sql = "SELECT WKString, WKIndex FROM WgnWellKnownString WHERE WKType=6 ORDER BY WKIndex";
            PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while(rs.next())
            {
                String s = rs.getString("WKString");
                int i = rs.getInt("WKIndex");
                mAvailableAuditResolutions.add(new AuditResolutionValue(s,i));
            }
            rs.close();
            statement.close();
        } 
        catch (Exception e)
        {
            System.err.println("Exception getting audit resolution from DB:" + e.getMessage());
            mTestRunner.WriteLog("Exception getting audit resolution from DB:" + e.getMessage(), LogFile.iERROR);
            return false;
        }
        return true;
    }

    private Boolean LoadAuditActions()
    {
        try
        {
            // Get available Actions
            String sql = "SELECT WKString, WKIndex FROM WgnWellKnownString WHERE WKType=5 ORDER BY WKIndex";
            PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            sql = "SELECT WKString FROM WgnWellKnownString WHERE WKType=19 ORDER BY WKIndex";
            statement = mDatabaseConnection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rsFilter = statement.executeQuery();

            if (rsFilter.first())
            {
                int count = 0;
                while(rs.next())
                {
                    String s = rs.getString("WKString");
                    int i = rs.getInt("WKIndex");
                    AuditActionValue v = new AuditActionValue(s,i);
                    rsFilter.absolute(count+1);
                    String sFilter = rsFilter.getString(1);

                    // sFilter will be null if this audit option has no resolution values
                    if (sFilter != null)
                    {
                        v.SetFilter(sFilter,mAvailableAuditResolutions);
                    //    mAvailableAuditActions.add(v);
                    }
                    mAvailableAuditActions.add(v);
                    count++;
                }
            }

            rsFilter.close();
            rs.close();
            statement.close();
        } 
        catch (Exception e)
        {
            System.err.println("Exception getting audit action from DB:" + e.getMessage());
            mTestRunner.WriteLog("Exception getting audit action from DB:" + e.getMessage(), LogFile.iERROR);
            return false;
        }
        return true;
    }

    private Boolean LoadAuditStatuses()
    {
        try
        {
            // Get available audit statuses
            String sql = "SELECT WKString FROM WgnWellKnownString WHERE WKType=2";
            PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
            ResultSet rsStatuses = statement.executeQuery();

            sql = "SELECT WKString FROM WgnWellKnownString WHERE WKType=18 ORDER BY WKIndex";
            statement = mDatabaseConnection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rsFilter = statement.executeQuery();

            if (rsFilter.first())
            {
                int rawCount = 0;
                while (rsStatuses.next())
                {
                    String s = rsStatuses.getString("WKString");

                    AuditStatusValue v = new AuditStatusValue(s,rawCount);
                    rsFilter.absolute(rawCount+1);
                    String sFilter = rsFilter.getString(1);
                    v.SetFilter(sFilter,mAvailableAuditActions);
                    mAvailableAuditStatuses.add(v);
                    rawCount++;
                }
            }
            rsStatuses.close();
            statement.close();
        } 
        catch (Exception e)
        {
            System.err.println("Exception getting audit status from DB:" + e.getMessage());
            mTestRunner.WriteLog("Exception getting audit status from DB:" + e.getMessage(), LogFile.iERROR);
            return false;
        }
        return true;
    }

    private Boolean LoadAuditButtonStatuses()
    {
        try
        {
            // Get audit button configurations
            String sql = "SELECT WKString FROM WgnWellKnownString WHERE WKType=8 ORDER BY WKIndex";
            PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            int count = 0;
            while(rs.next())
            {
                String s = rs.getString("WKString");
                AuditButton b = new AuditButton(s,bulkAuditButtonIds[count],singleAuditButtonIds[count],mAvailableAuditStatuses);
                mAvailableAuditButtons.add(b);
                count++;
            }

            rs.close();
            statement.close();
        } 
        catch (Exception e)
        {
            System.err.println("Exception getting audit buttons from DB:" + e.getMessage());
            mTestRunner.WriteLog("Exception getting audit buttons from DB:" + e.getMessage(), LogFile.iERROR);
            return false;
        }
        return true;
    }

    public void ResetAuditStatus()
    {
        String[] deleteSQL = new String[4];
        deleteSQL[0] = "DELETE FROM Wgn3IssueParticipant";
        deleteSQL[1] = "DELETE FROM Wgn3IssueTrigger";
        deleteSQL[2] = "DELETE FROM Wgn3EventAudit";
        deleteSQL[3] = "DELETE FROM Wgn3EventIssue";

        mAvailableAuditStatuses.clear();
        mAvailableComments.clear();
        mAvailableAuditActions.clear();
        mAvailableAuditResolutions.clear();
        mAvailableAuditButtons.clear();
        
        try
        {
            // truncate the contents of all the Audit tables in the CMS DB
            for (int i=0; i < deleteSQL.length; i++)
            {
                PreparedStatement statement = mDatabaseConnection.prepareStatement(deleteSQL[i]);
                statement.execute();
            }

            // restore the audit config defaults to WGNWellKnownString
            if (!GetAuditValue(11, 0).contentEquals("5,0,1,-1,-1,-3,0,0,User Correspondence,373,0,Security Issue"))
            {
                SetAuditValue(11, 0, "5,0,1,-1,-1,-3,0,0,User Correspondence,373,0,Security Issue");
                mTestRunner.WriteLog("Audit Options WKType 11 was restored to default", LogFile.iINFO);
            }
            // Audit Fields 1 & 3
            if (!GetAuditValue(2, 0).isEmpty())
            {
                SetAuditValue(2, 0, "");
                mTestRunner.WriteLog("Audit Field 1 value idx=0 was restored to default", LogFile.iINFO);
            }
            for (int i = 4; i < 40; i++)
            {
                if (!GetAuditValue(2, i).isEmpty())
                {
                    SetAuditValue(2, i, "");
                    mTestRunner.WriteLog("Audit Field 1 value idx="+i+" was restored to default", LogFile.iINFO);
                }
                if (!GetAuditValue(6, i).isEmpty())
                {
                    SetAuditValue(6, i, "");
                    mTestRunner.WriteLog("Audit Field 3 value idx="+i+" was restored to default", LogFile.iINFO);
                }
            }
            // Audit Field 2
            for (int i = 7; i < 40; i++)
            {
                if (!GetAuditValue(5, i).isEmpty())
                {
                    SetAuditValue(5, i, "");
                    mTestRunner.WriteLog("Audit Field 2 value idx="+i+" was restored to default", LogFile.iINFO);
                }
            }
            // restore Audit Field titles
            if (!GetAuditValue(7, 0).contentEquals("Audit Status")) {
                SetAuditValue(7, 0, "Audit Status");
                mTestRunner.WriteLog("Audit Field 1 Label was restored to default", LogFile.iINFO);
            }
            if (!GetAuditValue(7, 1).contentEquals("Action")) {
                SetAuditValue(7, 1, "Action");
                mTestRunner.WriteLog("Audit Field 2 Label was restored to default", LogFile.iINFO);
            }
            if (!GetAuditValue(7, 2).contentEquals("Resolution")) {
                SetAuditValue(7, 2, "Resolution");
                mTestRunner.WriteLog("Audit Field 3 Label was restored to default", LogFile.iINFO);
            }
            // clear down Audit comments
            for (int i=1; i<6; i++) {
                if (!GetAuditValue(3, i).contentEquals("")) {
                    mTestRunner.WriteLog("Removing Audit comment: " + GetAuditValue(3, i), LogFile.iINFO);
                    DeleteWKSRow(3, i);
                }
            }
            // turn off "Must be completed"
            if (!GetAuditValue(2, 3).contentEquals("Close")) {
                SetAuditValue(2, 3, "Close");
                mTestRunner.WriteLog("Audit Field 1 \"Other Audit Fields\" restored to default", LogFile.iINFO);
            }
            // restore Audit Button default values
            if (!GetAuditValue(8, 2).contentEquals("2,3,0,-1,3,0,0,0, ,Non Issue,Reviewed")) {
                SetAuditValue(8, 2, "2,3,0,-1,3,0,0,0, ,Non Issue,Reviewed");
                mTestRunner.WriteLog("Audit Button 3 settings restored to default", LogFile.iINFO);
            }                            
            if (!GetAuditValue(8, 4).contentEquals("2,0,0,-1,-1,-1,-1,0, ,New Issue, ")) {
                SetAuditValue(8, 4, "2,0,0,-1,-1,-1,-1,0, ,New Issue, ");
                mTestRunner.WriteLog("Audit Button 5 settings restored to default", LogFile.iINFO);
            }
        }
        catch(Exception e)
        {
            System.err.println("Exception resetting audit status:" + e.getMessage());
            mTestRunner.WriteLog("Exception resetting audit status:" + e.getMessage(), LogFile.iERROR);
        }
    }

    public void AddAuditValue(int wktype, int wkindex, String value) throws SQLException
    {
        // create the SQL to add a new comment to the DB
        String sql = "INSERT INTO WGNWellKnownString VALUES (" + wktype + ", " + wkindex + ", '" + value + "')";

        Statement statement = mDatabaseConnection.createStatement();
        statement.executeUpdate(sql);

        // reload the audit comment list
        ReloadAuditList(wktype);
    }

    public void SetAuditValue(int wktype, int wkindex, String value) throws SQLException
    {
        // create the SQL to update an existing audit status entry in the DB
        String sql = "UPDATE WGNWellKnownString SET WKString = '" + value + "' WHERE WKType = " + wktype + " AND WKIndex = " + wkindex;
        Statement statement = mDatabaseConnection.createStatement();
        statement.executeUpdate(sql);

        ReloadAuditList(wktype);
    }

    public String GetAuditValue(int wktype, int wkindex) throws SQLException
    {
        // append text to the WKString value for the selected rows
        String sql = "SELECT WKString FROM WGNWellKnownString WHERE WKType = " + wktype + " AND WKIndex = " + wkindex;
        PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        String s = "";
        while(rs.next())
        {
            s = rs.getString("WKString");
        }
        return s;
    }

    public String GetWKType11Value(int i) throws SQLException
    {
        // returns the value of the requested entry stored in WKType=11 in WGNWellKnownString
        // [0] = String version (for decoding values from older CMSs)
        // [1] = Allow Multiple Selection for Audit Field 3 (0/1)
        // [2] = Default wkindex of Audit field 1
        // [3] = Default wkindex of Audit field 2
        // [4] = Default wkindex of Audit field 3
        // [5] = Default Associated user option (0/1/2)
        // [6] = Allow issues to be associated with multiple users (0/1)
        // [7] = Prevent issues having no associated users (0/1)
        // [8] = Default Mail Reply template
        // [9] = bitwise compound of 1st 7 checkboxes on the Audit Options\Issues tab (defines iConsole field layout)
        //         policies.msi default=373; 503 = all on; 272 = all off except AS1; 258 = all off except assoc user
        // [10] = Default Issue name
        // [11] = Custom name

        String sql = "SELECT WKString FROM WGNWellKnownString WHERE WKType = 11 AND WKIndex = 0";
        PreparedStatement statement = mDatabaseConnection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        String s = "";
        while(rs.next())
        {
            s = rs.getString("WKString");
        }
        String[] values = s.split(",");
        if (i == -1)
        {
            return s;
        } else if (values[i] == null || values[i].isEmpty()) {
            return "";
        } else {
            return  values[i];
        }
    }

    public void SetWKType11Value(int i, String value) throws SQLException
    {
        // list of WKType=11 values can be found with GetWKType11Value()
        String s = GetWKType11Value(-1);
        String[] values = s.split(",");
        values[i] = value;
        for (int j = 0; j < values.length; j++)
        {
            if (j == 0)
            {
                s = values[j];
            } else {
                s = s + "," + values[j];
            }
        }
        SetAuditValue(11, 0, s);
    }

    public void DeleteWKSRow(int wktype, int wkindex) throws SQLException
    {
        // create the SQL to delete an audit entry from the DB
        String sql = "DELETE FROM WGNWellKnownString WHERE WKType = " + wktype + " AND WKIndex = " + wkindex;
        Statement statement = mDatabaseConnection.createStatement();
        statement.executeUpdate(sql);

        ReloadAuditList(wktype);
    }

    private void ReloadAuditList(int wktype)
    {
        // reload the altered audit list
        if (wktype == 2)
        {
            mAvailableAuditStatuses.clear();
            LoadAuditStatuses();
        }
        else if (wktype == 3) {
            mAvailableComments.clear();
            LoadComments();
        }
        else if (wktype == 5)
        {
            mAvailableAuditActions.clear();
            LoadAuditActions();
        }
        else if (wktype == 6)
        {
            mAvailableAuditResolutions.clear();
            LoadAuditResolutions();
        }
    }

    public Boolean[] GetAuditCheckboxStates() throws Exception
    {
        // returns an array of bools defining the expected fields shown on the New Issue dialog
        Boolean[] bool =  {false, false, false, false, false, false, false, false};
        // the checkbox states are stored in the db as a bitwise compound
        int AUDIT_ISSUE_SHOW_NAME       = 0x0001;
        int AUDIT_ISSUE_SHOW_USER       = 0x0002;
        int AUDIT_ISSUE_SHOW_COMMENT    = 0x0004;
        int AUDIT_ISSUE_SHOW_FIELD1     = 0x0010;
        int AUDIT_ISSUE_SHOW_FIELD2     = 0x0020;
        int AUDIT_ISSUE_SHOW_FIELD3     = 0x0040;
        int AUDIT_ISSUE_SHOW_INCIDENTS  = 0x0080;
        int AUDIT_ISSUE_CHECK_DUPLICATE = 0x0100;  // not used
         
        int value = Integer.parseInt(GetWKType11Value(9).trim());
        int[] flags = {AUDIT_ISSUE_SHOW_NAME, AUDIT_ISSUE_SHOW_FIELD1, AUDIT_ISSUE_SHOW_FIELD2, AUDIT_ISSUE_SHOW_FIELD3,
                        AUDIT_ISSUE_SHOW_USER, AUDIT_ISSUE_SHOW_INCIDENTS, AUDIT_ISSUE_SHOW_COMMENT, AUDIT_ISSUE_CHECK_DUPLICATE};

        for (int i=0; i < flags.length; i++)
        {
            // use the binary '&' operator to check if the flag is set
            if ((value & flags[i]) == flags[i])
            {
                bool[i] = true;
            }
        }
        return bool;
    }
}
