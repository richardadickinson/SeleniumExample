//Copyright © 2011 CA. All rights reserved.

package webdriver;

/**
 * @author dicri02
 * 
 */
import common.*;
import java.util.HashMap;


public class WebDriver {

    private static HashMap<String, Object> cmdTable;  // to collate arguments passed from the cmd line
    protected static boolean vflag = false;           // controls whether to report config to the cmd line
    private final static Object lock = new Object();  // only one thread must be allowed to write to cmdTable
    public static int errorLevel = 0;                 // used to feedback success status to cmd line

    /**
     * @param args contains the command line arguments
     */
    public static void main(String[] args) {
 
        TestRunner r = null; 
        Boolean bInit = false;
        
        // handle any cmd line parameters passed to suite
        cmdTable = new HashMap<>();
        parseCmdLine(args, cmdTable);

        try 
        {
            r = new TestRunner();
            bInit = r.Initialize();           
            if (bInit) {
                r.RunTests();

                if (r.HasFailed())
                {
                    System.err.println(String.format("Test '%1$' failed due to error '%2$'", r.GetFailedTestName(), r.GetFailureReason()));
                    errorLevel = 1;
                }
            } else {
                System.err.println(r.GetFailureReason());
                errorLevel = 1;
            }
        }
        catch (Exception e)
        {
            System.err.println("Exception running Test: " + e.getMessage());
            errorLevel = 1;
        }
        finally
        {
            if (bInit) 
            {
                r.Stop();
            }
            System.out.println("Error Level: " + errorLevel);          
        }
        System.exit(errorLevel);
    }

    private static boolean parseCmdLine(String[] args, HashMap<String, Object> cmdTable) {
	// *** DO NOT attempt to write to log files in here !!!
	// *** Output to command line only
	int i = 0, j;
        String arg;
        char flag;
        String str = "";

        synchronized(lock)
        {
            while (i < args.length && args[i].startsWith("-"))
            {
                arg = args[i++];                
                switch (arg) {
                    case "-vb":
                        System.out.println("Verbose mode on");
                        vflag = true;                    
                        break;
                    // check for arguments that require params
                    case "-sys":
                        if (i < args.length)
                        {
                            str = args[i++];
                            cmdTable.put("sys", str);
                            if (vflag) System.out.println("System config file = " + str);
                        } else {
                            System.err.println("ERROR: -sys requires an XML filename");
                            return false;
                        }
                        break;
                    case "-cfg":
                        if (i < args.length)
                        {
                            str = args[i++];
                            cmdTable.put("cfg", str);
                            if (vflag) System.out.println("Testcase config file = " + str);
                        } else {
                            System.err.println("ERROR: -cfg requires an XML filename");
                            return false;
                        }
                        break;
                    case "-gbl":
                        if (i < args.length)
                        {
                            str = args[i++];
                            cmdTable.put("gbl", str);
                            if (vflag) System.out.println("Global params config file = " + str);
                        } else {
                            System.err.println("ERROR: -gbl requires an XML filename");
                            return false;
                        }
                        break;
                    case "-log":
                        if (i < args.length)
                        {
                            str = args[i++];
                            cmdTable.put("log", str);
                            if (vflag) System.out.println("Output log file name = " + str);
                        } else {
                            System.err.println("ERROR: -log requires a valid filename");
                            return false;
                        }
                        break;
                    case "-logpath":
                        if (i < args.length)
                        {
                            str = args[i++];
                            cmdTable.put("logpath", str);
                            if (vflag) System.out.println("Output log path = " + str);
                        } else {
                            System.err.println("ERROR: -logpath requires a valid full file path");
                            return false;
                        }                    
                        break;
                    default: // checks for a series of flag arguments
                        for (j = 1; j < arg.length(); j++)
                        {
                            flag = arg.charAt(j);
                            switch (flag)
                            {
                                case '?':
                                    System.out.println("Usage: iConSeleniumTestSuite [-vb] [-sys <filename>] [-gbl <filename>] [-cfg <filename>] [-log <log filename>] [-logpath <log path>]");
                                    System.out.println("Where: -sys specifies an xml file containing system configuration parameters");
                                    System.out.println("Where: -cfg specifies an xml file containing test case-specific configuration parameters");
                                    System.out.println("Where: -gbl specifies an xml file containing global test case configuration parameters");
                                    System.out.println("Where: -log specifies an output file for test results (created in the logs subdir if no logpath is specified)");
                                    System.out.println("Where: -logpath specifies a full directory path for the output file for test results");
                                    break;
                                default:
                                    System.err.println("ERROR: illegal option on command line");
                                    return false;
                            }
                        }
                }
            }
        }
        if (i != args.length)
        {
            System.err.println("Usage: iConSeleniumTestSuite [-vb] [-sys <filename>] [-gbl <filename>] [-cfg <filename>] [-log <log filename>] [-logpath <log path>]");
            System.err.println("Where: -sys specifies an xml file containing system configuration parameters");
            System.err.println("Where: -cfg specifies an xml file containing test case-specific configuration parameters");
            System.err.println("Where: -gbl specifies an xml file containing global test case configuration parameters");
            System.err.println("Where: -log specifies an output file for test results (created in the logs subdir if no logpath is specified)");
            System.err.println("Where: -logpath specifies a full directory path for the output file for test results");
            return false;
        }

        return true;
    }

    public static HashMap<String, Object> GetCmdTable()
    {
        return cmdTable;
    }
    
}
