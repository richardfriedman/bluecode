package org.blue.star.plugins;

import java.util.Hashtable;

/**
 * <p>Title: NSClient4j</p>
 * <p>Description: JMX Framework for NSClient4j</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>@author Nicholas Whitehead (nwhitehe@yahoo.com)</p>
 * @version $Revision: 1.1 $
 * Date $Date: 2007/04/17 19:14:23 $
 */
public class NSClient4JServerException extends NSClient4JException {

    public static final String ERROR_MARKER = " - ERROR: ";

    protected String errorCode = null;
    protected String counterName = null;
    protected String errorText = null;

    public static Hashtable errorMessages = null;

    public NSClient4JServerException() {
        super();
    }

    static {
       errorMessages = new Hashtable(76);
       errorMessages.put("800007D0", "Unable to connect to specified machine or machine is off line.");
       errorMessages.put("800007D1", "The specified instance is not present.");
       errorMessages.put("800007D2", "There is more data to return than would fit in the supplied buffer. Allocate a larger buffer and call the function again.");
       errorMessages.put("800007D3", "The data item has been added to the query, but has not been validated nor accessed. No other status information on this data item is available.");
       errorMessages.put("800007D4", "The selected operation should be retried.");
       errorMessages.put("800007D5", "No data to return.");
       errorMessages.put("800007D6", "A counter with a negative denominator value was detected.");
       errorMessages.put("800007D7", "A counter with a negative timebase value was detected.");
       errorMessages.put("800007D8", "A counter with a negative value was detected.");
       errorMessages.put("800007D9", "The user cancelled the dialog box.");
       errorMessages.put("800007DA", "The end of the log file was reached.");
       errorMessages.put("800007DB", "Time out while waiting for asynchronous counter collection thread to end.");
       errorMessages.put("800007DC", "Cannot change set default realtime datasource. There are realtime query sessions collecting counter data.");
       errorMessages.put("C0000BB8", "The specified object is not found on the system.");
       errorMessages.put("C0000BB9", "The specified counter could not be found.");
       errorMessages.put("C0000BBA", "The returned data is not valid.");
       errorMessages.put("C0000BBB", "A PDH function could not allocate enough temporary memory to complete the operation. Close some applications or extend the pagefile and retry the function.");
       errorMessages.put("C0000BBC", "The handle is not a valid PDH object.");
       errorMessages.put("C0000BBD", "A required argument is missing or incorrect.");
       errorMessages.put("C0000BBE", "Unable to find the specified function.");
       errorMessages.put("C0000BBF", "No counter was specified.");
       errorMessages.put("C0000BC0", "Unable to parse the counter path. Check the format and syntax of the specified path.");
       errorMessages.put("C0000BC1", "The buffer passed by the caller is invalid.");
       errorMessages.put("C0000BC2", "The requested data is larger than the buffer supplied. Unable to return the requested data.");
       errorMessages.put("C0000BC3", "Unable to connect to the requested machine.");
       errorMessages.put("C0000BC4", "The specified counter path could not be interpreted.");
       errorMessages.put("C0000BC5", "The instance name could not be read from the specified counter path.");
       errorMessages.put("C0000BC6", "The data is not valid.");
       errorMessages.put("C0000BC7", "The dialog box data block was missing or invalid.");
       errorMessages.put("C0000BC8", "Unable to read the counter and/or explain text from the specified machine.");
       errorMessages.put("C0000BC9", "Unable to create the specified log file.");
       errorMessages.put("C0000BCA", "Unable to open the specified log file.");
       errorMessages.put("C0000BCB", "The specified log file type has not been installed on this system.");
       errorMessages.put("C0000BCC", "No more data is available.");
       errorMessages.put("C0000BCD", "The specified record was not found in the log file.");
       errorMessages.put("C0000BCE", "The specified data source is a log file.");
       errorMessages.put("C0000BCF", "The specified data source is the current activity.");
       errorMessages.put("C0000BD0", "The log file header could not be read.");
       errorMessages.put("C0000BD1", "Unable to find the specified file.");
       errorMessages.put("C0000BD2", "There is already a file with the specified file name.");
       errorMessages.put("C0000BD3", "The function referenced has not been implemented.");
       errorMessages.put("C0000BD4", "Unable to find the specified string in the list of performance name and explain text strings.");
       errorMessages.put("C0000BD5", "Unable to map to the performance counter name data files. The data will be read from the registry and stored locally.");
       errorMessages.put("C0000BD6", "The format of the specified log file is not recognized by the PDH DLL.");
       errorMessages.put("C0000BD7", "The specified Log Service command value is not recognized.");
       errorMessages.put("C0000BD8", "The specified Query from the Log Service could not be found or could not be opened.");
       errorMessages.put("C0000BD9", "The Performance Data Log Service key could not be opened. This may be due to insufficient privilege or because the service has not been installed.");
       errorMessages.put("C0000BDA", "An error occured while accessing the WBEM data store.");
       errorMessages.put("C0000BDB", "Unable to access the desired machine or service. Check the permissions and authentication of the log service or the interactive user session against those on the machine or service being monitored.");
       errorMessages.put("C0000BDC", "The maximum log file size specified is too small to log the selected counters. No data will be recorded in this log file. Specify a smaller set of counters to log or a larger file size and retry this call.");
       errorMessages.put("C0000BDD", "Cannot connect to ODBC DataSource Name.");
       errorMessages.put("C0000BDE", "SQL Database does not contain a valid set of tables for Perfmon, use PdhCreateSQLTables.");
       errorMessages.put("C0000BDF", "No counters were found for this Perfmon SQL Log Set.");
       errorMessages.put("C0000BE0", "Call to SQLAllocStmt failed");
       errorMessages.put("C0000BE1", "Call to SQLAllocConnect failed");
       errorMessages.put("C0000BE2", "Call to SQLExecDirect failed");
       errorMessages.put("C0000BE3", "Call to SQLFetch failed");
       errorMessages.put("C0000BE4", "Call to SQLRowCount failed");
       errorMessages.put("C0000BE5", "Call to SQLMoreResults failed");
       errorMessages.put("C0000BE6", "Call to SQLConnect failed");
       errorMessages.put("C0000BE7", "Call to SQLBindCol failed");
       errorMessages.put("C0000BE8", "Unable to connect to the WMI server on requested machine.");
       errorMessages.put("C0000BE9", "Collection is already running.");
       errorMessages.put("C0000BEA", "The specified start time is after the end time.");
       errorMessages.put("C0000BEB", "Collection does not exist.");
       errorMessages.put("C0000BEC", "The specified end time has already elapsed.");
       errorMessages.put("C0000BED", "Collection did not start, check the application event log for any errors.");
       errorMessages.put("C0000BEE", "Collection already exists.");
       errorMessages.put("C0000BEF", "There is a mismatch in the settings type.");
       errorMessages.put("C0000BF0", "The information specified does not resolve to a valid path name.");
       errorMessages.put("C0000BF1", "The \"Performance Logs & Alerts\" service did not repond.");
       errorMessages.put("C0000BF2", "The information passed is not valid.");
       errorMessages.put("C0000BF3", "The information passed is not valid.");
       errorMessages.put("C0000BF4", "The name supplied is too long.");
       errorMessages.put("C0000BF5", "SQL log format is incorrect. Correct format is \"SQL:<DSN-name>!<LogSet-Name>\".");
       errorMessages.put("C0000BF6", "Performance counter in PdhAddCounter() call has already been added  in the performacne query. This counter is ignored.");
    }


    public NSClient4JServerException(String message) {
        if(message==null || message.length() < 1) {
            errorCode = "No Error Code";
            counterName = "Unknown";
            errorText = "Null Return Value";
        } else {
            counterName = message.substring(0, message.indexOf(ERROR_MARKER));
            errorCode = message.substring(message.indexOf(ERROR_MARKER) + ERROR_MARKER.length()+2);
            errorText = decodeErrorCode(errorCode);
        }
    }

    public String getLocalizedMessage() {
        return getFullMessage();
    }

    public String getMessage() {
        return getFullMessage();
    }


    public String toString() {
        return getFullMessage();
    }

    public NSClient4JServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NSClient4JServerException(Throwable cause) {
        super(cause);
    }

    public String getErrorText() {
        return errorText;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getCounterName() {
        return counterName;
    }

    public String getFullMessage() {
        return errorText + " on " + counterName + "(" + errorCode + ")";
    }

    /**
     * Returns the associated message for the passed error code.
     * @param errorCode String
     * @return String
     */
    public static String decodeErrorCode(String errorCode) {
        String message = (String)errorMessages.get(errorCode);
        if(message!=null) {
            return message;
        } else {
            return "No Message For Code:" + errorCode;
        }
    }
}
