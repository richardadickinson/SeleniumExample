
package common;

/**
 *
 * @author dicri02
 */

import testcases.*;
import java.util.*;
import javax.mail.*;
import java.io.*;

public class ReadMail {

    private static Folder defFolder;
    private static Store store;
    private static Message msg;
    private static String mSender;
    private static String mRecips;

    public static Boolean getMessage(WgnTestCase wtc, String host, String user, String password, String subj)
    {        
        TestRunner tr = wtc.GetTestRunner();
        msg = null;

        // Get a session. Use a blank Properties object.
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);

        try
        {
            // Get a Store object
            store = session.getStore("pop3");
            store.connect(host, user, password);
            tr.WriteLog("Connected to "+ host+ " Exchange session and " + user + " store", LogFile.iDEBUG);
            // Open the inbox folder
            defFolder = store.getFolder("INBOX");
            defFolder.open(1);
            // Get count of messages in mailbox
            int count = defFolder.getMessageCount();

            if (count > 0)
            {
                // get the last message in the mailbox if it has the expected subject line
                if (defFolder.getMessage(count).getSubject().contentEquals(subj))
                {
                    msg = defFolder.getMessage(count);
                } else  {
                    return false;
                }
            } else {
                tr.WriteLog("ERROR: The INBOX is empty.", LogFile.iERROR);
                return false;
            }
        } 
        catch (Exception e)
        {
            //e.printStackTrace();  //uncomment to output stack to cmd line
            tr.WriteLog("POP3 Connection FAILED: " + e.toString(), LogFile.iERROR);
            return false;
        }

        return true;
    }

    public static void closeMailbox() throws MessagingException
    {
        // finished so tidy up
        defFolder.close(true);
        store.close();
    }

    public static String getSender() throws MessagingException
    {
        Address[] addresses = msg.getFrom();
        if (addresses != null)
        {
            for (int i=0; i < addresses.length; i++)
            {
                Address f = (Address) addresses[i];
                if (i == 0)
                {
                    mSender = f.toString();
                } else {
                    mSender.concat("," + f.toString());
                }
            }
        } else {
           mSender = "There is no From address";
        }
        return mSender;
    }

    public static String getRecips() throws MessagingException
    {
        // Get all recips for message
        Address[] recips = msg.getAllRecipients();
        if (recips != null)
        {
            for (int i=0; i < recips.length; i++)
            {
                Address r = (Address) recips[i];
                if (i == 0)
                {
                    mRecips = r.toString();
                } else {
                    mRecips.concat("," + r.toString());
                }
            }
        } else {
            mRecips = "There are no Recipients";
        }
        return mRecips;
    }

    public static String getHeader(String expHeader) throws MessagingException
    {
        String value = "";
        // retrieve header values from header enumerations
        Enumeration<?> mHeaders = msg.getAllHeaders();
        while (mHeaders.hasMoreElements())
        {
            Header h = (Header) mHeaders.nextElement();            
            if (h.getName().equalsIgnoreCase(expHeader))
            {
                value = h.getValue();
            }
        }
        return value;
    }

    public static String getFullMessageContent(String subject) throws MessagingException
    {
        // Retrieves message headers and content
        String message = "";
        if (msg.getSubject().equalsIgnoreCase(subject) || subject == null || subject.isEmpty()) 
        {
            // Subject and sent date
            message = "Subject: " + msg.getSubject();
            message = message + "Sent: " + msg.getSentDate().toString() + "\r\n";

            // Get message contents
            try {
                String messageText = getBodyText(msg);
                if (messageText != null)
                {
                    message = message + "\r\n   ------------ Contents of message Part 1 ------------\r\n";
                    message = message + messageText;
                } else {
                    message = message + "\r\n   No message content found";
                }
            } 
            catch (UnsupportedEncodingException e)
            {
                //e.printStackTrace(); //uncomment to output stack to cmd line
                message = "Unsupported Encoding Exception: " + e.toString();
                System.err.println(e.toString());
            } 
            catch (Exception e2)
            {
                message = "Exception: " + e2.toString();
                System.err.println(e2.toString());
            }
        }
        return message;
    }
    
    public static String getBodyText(Part p) throws MessagingException, IOException
    {
        // Returns the primary text content of the message.
        if (p.isMimeType("text/*"))
        {
            String s = (String)p.getContent();
            return s;
        }
        else if (p.isMimeType("multipart/alternative") || p.isMimeType("message/rfc822"))
        {
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++)
            {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain"))
                {
                    text = getBodyText(bp);
                    return text;
                }
            }
            return text;
        }
        else if (p.isMimeType("multipart/*"))
        {
            Multipart mp = (Multipart)p.getContent();
            String msgbody = "";
            for (int i = 0; i < mp.getCount(); i++)
            {
                if (i > 0)
                {
                    msgbody = msgbody + "\r\n   ------------ Contents of message Part " + (i+1) + " ------------\r\n";
                }
                msgbody = msgbody + "     *** Type: " + mp.getBodyPart(i).getContentType().toString() + "\r\n";
                if (mp.getBodyPart(i).isMimeType("message/rfc822"))
                {
                    // it's a mime message attachment
                    msgbody = msgbody + "     *** Message headers:\r\n\r\n";
                    // get any mail headers from the mime attachment
                    Enumeration<?> headers = mp.getBodyPart(i).getAllHeaders();
                    while (headers.hasMoreElements())
                    {
                        Header h = (Header) headers.nextElement();
                        msgbody = msgbody + h.getName() + ": " + h.getValue() + "\r\n";
                    }
                    // retrieve the mime message content as text
                    msgbody = msgbody + "\r\n     *** Message content:\r\n";
                    msgbody = msgbody + getBodyText((Part)mp.getBodyPart(i).getContent());
                } else {
                    msgbody = msgbody + getBodyText(mp.getBodyPart(i));
                }
                msgbody = msgbody + "\r\n";
            }
            if (msgbody != null)
            {
              return msgbody;
            }
        }

        if (p.isMimeType("application/x-pkcs7-mime"))
        {
            String enc = "\r\n   *** Message content is encrypted ***";
            return enc;
        }

        return null;
    }

}
