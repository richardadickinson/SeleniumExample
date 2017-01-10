//Copyright © 2011 CA. All rights reserved.

package common;

/**
 *
 * @author smaho01 & dicri02
 */

import java.io.*;
import java.text.*;
import java.util.*;
import webdriver.*;

public class LogFile {

    public static final int iNONE = 0;
    public static final int iERROR = 1;
    public static final int iKNOWN = 2;
    public static final int iTEST = 3;
    public static final int iINFO = 4;
    public static final int iDEBUG = 5;

    private static int fails = 0;
    private static int knowns = 0;
    private static int tests = 0;
    private int indent;
    private final Object lock;   // only 1 thread may log at a time
    public int verboselevel;     // the max level to log at

    private FileWriter mFile;
    private String mFileName;

    public LogFile()
    {
        lock = new Object();
        verboselevel = 0;        // no logging by default
    }

    private String GetLogFileName()
    {
        String sub;
        String path;
        synchronized (lock) {
            sub = (String)WebDriver.GetCmdTable().get("log");
        }
        synchronized (lock) {
            path = (String)WebDriver.GetCmdTable().get("logpath");
        }

        if (sub != null)
        {
            if (path != null)
            {
                return path + "\\" + sub;
            } else {
                return "logs\\"+sub;
            }
        } else {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HHmmss");
            Date date = new Date();
            String name = dateFormat.format(date);
            if (path != null)
            {
                return path + "\\Selenium_"+name+".log";
            } else {
                return "logs\\Selenium_"+name+".log";
            }
        }
    }

    public void Open() //throws IOException
    {
        try {
            File file = new File("logs");
            boolean exists = file.exists();
            if (!exists) {
                /*boolean mkdir =*/	file.mkdir();
            }
            mFileName = GetLogFileName();
            mFile = new FileWriter(new File(mFileName));
            Write("Tests started", iINFO);
            mFile.flush();
            indent = 0;
        } catch (IOException io) {
            System.err.println("Error: " + io.getMessage());
        }
    }

    public void Close(int i) throws IOException
    {
        Write("Tests finished", iINFO);
        Write("Summary: Test cases: " + i + ", Tests: " + tests + ", Fails: " + fails + ", Known Fails: " + knowns);
        if (fails > 0 && WebDriver.errorLevel == 0)
        {
            WebDriver.errorLevel = 2;
        } else if (knowns > 0 && WebDriver.errorLevel == 0)
        {
            WebDriver.errorLevel = 3;
        }
        mFile.close();
    }

    // calls to this method ALWAYS get written as the loglevel is set to the verboselevel
    // N.B. as no loglevel is set these lines can't be counted (e.g. fails++)
    public void Write(String line) throws IOException
    {
            Log(line, 0, verboselevel);
    }
    // calls here are written with indent=0; used for fatal & JavaScript errors
    public void WriteError(String line) throws IOException
    {
            Log(line, 0, LogFile.iERROR);
    }
    // The standard logging call to be used for general log lines
    public void Write(String line, int loglevel) throws IOException
    {
            Log(line, indent, loglevel);
    }

    public void Log(String str, int indent, int loglevel) throws IOException
    {
        // count tests and fails before writing log file (as log level may mean no entry is written)
        if (loglevel == 1) { fails++; tests++; }
        if (loglevel == 2) { knowns++; tests++; }
        if (loglevel == 3) { tests++; }

        // check loglevel to see whether entry is of sufficient importance to log
        if (loglevel > this.verboselevel) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(GetTime());
  //      sb.append(": ");
        // indent line if necessary
        for (int i = 0; i < indent; ++i) { sb.append("    "); }
        sb.append(str);
        sb.append("\r\n");
        synchronized(lock) {
            mFile.write(sb.toString());
            mFile.flush();
        }

    }
    public void Enter(String method, int loglevel) throws IOException
    {
        Write(method, loglevel);
        Indent(loglevel);
    }

    public void Exit(String method, int loglevel) throws IOException
    {
        Unindent(loglevel);
        Write(method, loglevel);
    }

    public void Indent() {Indent(100);}
    public void Indent(int verboselevel)
    {
        if (verboselevel <= this.verboselevel)
                ++indent;
    }

    public void Unindent() {Unindent(100);}
    public void Unindent(int verboselevel)
    {
        if (verboselevel <= this.verboselevel)
                --indent;
    }

    public void ResetIndent() {
        indent = 0;
    }

    private String GetTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public FileWriter GetFileWriter()
    {
        return mFile;
    }

}
