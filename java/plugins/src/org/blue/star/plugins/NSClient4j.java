package org.blue.star.plugins;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;


/**
 * <p>Title: NSClient4j</p>
 * <p>Description: NSClient4j Client Adapter</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>@author Nicholas Whitehead (nwhitehe@yahoo.com)</p>
 * <p>@author Richard Friedman (akuns@users.sourceforeg.net</p>
 * @version $Revision: 1.1 $
 * 
 * This is from LGPL code base of NSClient4J.  Need to make serious changes to better behave like
 * check_nt needs it.  Original code base is located at <a href="https://nsclient4j.dev.java.net/" >nsclient4j</a>
 * 
 */

public class NSClient4j {
  /** The string representation of the host name to connect to */
  protected String hostName = null;
  /** The port number NSClient is running on. Default is 1248 */
  protected int portNumber = 1248;
  /** The password for connecting to NSClient. Default is "None" */
  protected String password = "None";
  /** The socket for communicating with Remote NSClient */
  protected Socket socket = null;
  protected OutputStream os = null;
  protected InputStream is  = null;
  protected BufferedInputStream bis = null;
  protected ByteArrayOutputStream baos = null;
  protected int socketTimeout = 5000;

  protected boolean inited = false;
  public void setPassword(String password) {
    this.password = password;
  }

  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }



  /**
   * NSClient4j Constructor.
   * @param hostName String The name or IP address of the host that is running NSClient
   * @throws NSClient4JException
   */
  public NSClient4j(String hostName) throws NSClient4JException {
    this.hostName = hostName;
    initSocket();
  }

  /**
   * NSClient4j Constructor.
   * @param hostName String The name or IP address of the host that is running NSClient
   * @param portNumber int The port number NSClient is listening on if not the default
   * @throws NSClient4JException
   */
  public NSClient4j(String hostName, int portNumber) throws NSClient4JException {
    this.hostName = hostName;
    this.portNumber = portNumber;
    initSocket();
  }

  /**
   * NSClient4j Constructor.
   * @param hostName String The name or IP address of the host that is running NSClient
   * @param portNumber int The port number NSClient is listening on if not the default
   * @param password String
   * @throws NSClient4JException
   */
  public NSClient4j(String hostName, int portNumber, String password)  throws NSClient4JException {
    this.hostName = hostName;
    this.portNumber = portNumber;
    this.password = password;
    initSocket();
  }

  /**
   * NSClient4j Constructor.
   * @param hostName String The name or IP address of the host that is running NSClient
   * @param password String
   * @throws NSClient4JException
   */
  public NSClient4j(String hostName, String password)  throws NSClient4JException {
    this.hostName = hostName;
    this.password = password;
    initSocket();
  }

  /**
   * Parameterless constructor.
   * Unlike the other constructors, this one does not init the socket layer.
   * Once the configuration parameters have been set, the init() method must be called before the client can be used.
   */
  public NSClient4j() {
    inited = false;
  }

  /**
   * Sets the time out on the NSRequests. If the request times out, it will throw an exception.
   * The default is 5000 ms
   * @param timeout int the request timeout in milliseconds
   */
  public void setSocketTimeOut(int timeout) {
    socketTimeout = timeout;
  }

  /**
   * Initializes the socket layer.
   * Intended for use in concert with the parameterless constructor
   * @throws NSClient4JException
   */
  public void init() throws NSClient4JException {
    initSocket();
  }

  /**
   * Opens the socket as configured to the remote NSClient.
   * Initializes all the io streams.
   * @throws NSClient4JException
   */

  protected void initSocket() throws NSClient4JException {
    if(!inited) {
      try {
        socket = new Socket(hostName, portNumber);
        socket.setSoTimeout(socketTimeout);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        bis = new BufferedInputStream(is);
        baos = new ByteArrayOutputStream();
        inited = true;
      }
      catch (UnknownHostException ex) {
        inited = false;
        throw new NSClient4JException("Unknown Host:" + hostName, ex);
      }
      catch (IOException ex) {
        inited = false;
        throw new NSClient4JException("Exception Connecting to " + hostName +
                                      ":" + portNumber + " -> " + ex, ex);
      }
    }
  }

  /**
   * Closes the socket
   */
  public void close() {
    try { socket.close(); } catch (Exception ex) { }
  }

  /**
   * Last ditch effort to close the socket
   */
  public void finalize() {
    try { socket.close(); } catch (Exception ex) { }
    try { super.finalize(); } catch (Throwable t) { }
  }

  /**
   * Returns the version of the NSClient that is connected to.
   * @throws NSClient4JException
   * @return String
   */
  public String getNSClientVersion() throws NSClient4JException {
    String result =  submittRequest(password + "&1");
    return result;
  }

  /**
   * Returns the used disk space for the volume name passed
   * @param diskVol String  (e.g. C:)
   * @return String
   * @throws NSClient4JException
   */
  public String getUsedDiskSpace(String diskVol) throws NSClient4JException {
    // Returns <free>&<total>
    String result[] =  split(submittRequest(password + "&4&" + diskVol), "&");
    double freeSpace = Double.parseDouble(result[0]);
    double totalSpace = Double.parseDouble(result[1]);
    return "" + (totalSpace-freeSpace);
  }

  /**
   * Returns the free disk space for the volume name passed
   * @param diskVol String (e.g. C:)
   * @return String
   * @throws NSClient4JException
   */
  public String getFreeDiskSpace(String diskVol) throws NSClient4JException {
    // Returns <free>&<total>
    String result[] =  split(submittRequest(password + "&4&" + diskVol), "&");
    double freeSpace = Double.parseDouble(result[0]);
    return "" + freeSpace;
  }

  /**
   * Returns the total disk space for the volume name passed
   * @param diskVol String (e.g. C:)
   * @return String
   * @throws NSClient4JException
   */
  public String getTotalDiskSpace(String diskVol) throws NSClient4JException {
    // Returns <free>&<total>
    String result[] =  split(submittRequest(password + "&4&" + diskVol), "&");
    double totalSpace = Double.parseDouble(result[1]);
    return "" + totalSpace;
  }

  /**
   * Returns the disk space information for the volume passed in.
   * @param diskVol String (e.g. C:)
   * @return String
   * @throws NSClient4JException
   */
  public double[] getFreeAndTotalDiskSpace(String diskVol) throws NSClient4JException {
    // Returns <free>&<total>
     String result[] =  split(submittRequest(password + "&4&" + diskVol), "&");
     double[] free_and_total = new double[2];
     free_and_total[0] = Double.parseDouble(result[0]);  //free space
     free_and_total[1] = Double.parseDouble(result[1]); // total space
     return free_and_total;
  }

  /**
   * Returns the percentage of disk space used for the volume name passed
   * @param diskVol String (e.g. C:)
   * @return String
   * @throws NSClient4JException
   */
  public String getUsedPercentDiskSpace(String diskVol) throws NSClient4JException {
    // Returns <free>&<total>
    String result[] =  split(submittRequest(password + "&4&" + diskVol), "&");
    double freeSpace = Double.parseDouble(result[0]);
    double totalSpace = Double.parseDouble(result[1]);
    double usedSpace = totalSpace - freeSpace;
    int percent =(int)((usedSpace / totalSpace) * 100);
    return "" + (percent);
  }

  /**
   * Returns the percentage of disk space free for the volume name passed
   * @param diskVol String (e.g. C:)
   * @return String
   * @throws NSClient4JException
   */
  public String getFreePercentDiskSpace(String diskVol) throws NSClient4JException {
    // Returns <free>&<total>
    String result[] =  split(submittRequest(password + "&4&" + diskVol), "&");
    double freeSpace = Double.parseDouble(result[0]);
    double totalSpace = Double.parseDouble(result[1]);
    int percent = (int)((freeSpace / totalSpace) * 100);
    return "" + (percent);
  }



  /**
   * <p>Retrieves the value of a NT Performance Monitor counter value.
   * <p>Example counter string are: <ul>
   * <li>\\ProcessorPerformance(ACPI\\GenuineIntel_-_x86_Family_6_Model_9\\_0_0)\\Processor Frequency
   * <li>\\Processor(_Total)\\% Processor Time
   * </ul>
   * @param counterName String
   * @throws NSClient4JException
   * @return String
   */
  public String getPerfMonCounter(String counterName)throws NSClient4JException {
    String result = submittRequest(password + "&8&" + counterName);
    return result;
  }
  
  /**
   * This method is needed since calling the perfMon counter always show close to 100 % util.
   * @throws NSClient4JException
   * @return String
   */
  public String getCPUUsage() throws NSClient4JException {
    //CPU Load 1% (1 min average)
    String result = submittRequest(password + "&2&1&1&1");
    return result;
  }

  /**
   * This method is needed since calling the perfMon counter always show close to 100 % util.
   * @param range minutes range
   * @throws NSClient4JException
   * @return String
   */
  public String getCPUUsage(long range) throws NSClient4JException {
    //CPU Load 1% (1 min average)
    String result = submittRequest(password + "&2&" + range );
    return result;
  }
  /**
   * Retrieves the up time of the server in seconds
   * @throws NSClient4JException
   * @return int The up time in seconds
   */
  public int getUpTimeSeconds()throws NSClient4JException {
    String result = submittRequest(password + "&3" );
    return Integer.parseInt(result);
  }

  /**
   * Retrieves the up time of the server in minutes
   * @throws NSClient4JException
   * @return int The up time in minutes (rounded)
   */
  public int getUpTimeMinutes()throws NSClient4JException {
    String result = submittRequest(password + "&3" );
    int secs = Integer.parseInt(result);
    int minutes = secs / 60;
    return minutes;
  }

  /**
   * Retrieves the up time of the server in hours
   * @throws NSClient4JException
   * @return int The up time in hours (rounded)
   */
  public int getUpTimeHours()throws NSClient4JException {
    String result = submittRequest(password + "&3" );
    int secs = Integer.parseInt(result);
    int hours = secs / 60 / 60;
    return hours;
  }

  /**
   * Retrieves the up time of the server in Days
   * @throws NSClient4JException
   * @return int The up time in days
   */
  public float getUpTimeDays()throws NSClient4JException {
    String result = submittRequest(password + "&3" );
    float secs = Float.parseFloat(result);
    float days = secs / 60 / 60 / 24;
    return days;
  }

  /**
   * Retrieves the up time of the server as the date the server started
   * @throws NSClient4JException
   * @return Date The date the server started.
   */
  public Date getUpTimeDate()throws NSClient4JException {
    String result = submittRequest(password + "&3" );
    long startTime = System.currentTimeMillis() - Long.parseLong(result) * 1000;
    return new Date(startTime);
  }

  /**
   * Tests a service by service name. If the service is up, returns true.
   * If it is stopped, or unknown, returns a false.
   * @param serviceName String
   * @throws NSClient4JException
   * @return boolean
   */
  public boolean isServiceUp(String serviceName) throws NSClient4JException {
    String result = submittRequest(password + "&5&ShowAll&" + serviceName);
    String[] results = split(result, ":");
    if(results[1].trim().equalsIgnoreCase("Started")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Checks a list of services, returning result code and response.
   * If it is stopped, or unknown, returns a false.
   * @param serviceList  array of services
   * @param showall show all services or not.
   * @throws NSClient4JException
   * @return String[] array of result(int), list(string)
   */
  public String[] checkServices(String[] serviceList, boolean showAll ) throws NSClient4JException {
     String show = showAll?"ShowAll":"ShowFail";
     StringBuffer serviceString= new StringBuffer();
     for ( int x = 0; x < serviceList.length ; x++  ) { 
        if ( x != 0 )
           serviceString.append( '&' );
        serviceString.append( x );
     }

     String result = submittRequest(password + "&5&"+show+"&" + serviceString );
     return split(result, "&");
  }
  
  /**
   * Tests a process by process name. If process is running, returns true.
   * If process is not running, or unknown, returns false.
   * @param processName String
   * @throws NSClient4JException
   * @return boolean
   */
  public boolean isProcessUp(String processName) throws NSClient4JException {
    String result = submittRequest(password + "&6&ShowAll&" + processName);
    String[] results = split(result, ":");
    if(results[1].trim().equalsIgnoreCase("Running")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Checks a list of processes, returning result code and response.
   * If it is stopped, or unknown, returns a false.
   * @param serviceList  array of services
   * @param showall show all services or not.
   * @throws NSClient4JException
   * @return String[] array of result(int), list(string)
   */
  public String[] checkProcesses(String[] processList, boolean showAll ) throws NSClient4JException {
     String show = showAll?"ShowAll":"ShowFail";
     StringBuffer serviceString= new StringBuffer();
     for ( int x = 0; x < processList.length ; x++  ) { 
        if ( x != 0 )
           serviceString.append( '&' );
        serviceString.append( x );
     }
     String result = submittRequest(password + "&6&"+show+"&" + serviceString );
     return split(result, "&");
  }
  
  /**
   * Gets the file date of the passed file name
   * @param fileName String
   * @throws NSClient4JException
   * @return Date
   */
  public Date getFileDate(String fileName)  throws NSClient4JException {
    String result = submittRequest(password + "&9&" + fileName);
    // 8500&Date: 08/28/2004 1:05:42 PM
    String[] results = split(result, ":");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    try {
      return dateFormat.parse(results[1].trim());
    }
    catch (ParseException ex) {
      throw new NSClient4JException("Bad Date Format:" + results[1], ex);
    }
  }
  
  /**
   * Get the file information Date and 
   */
  public String[] getFileInformation(String filename) throws NSClient4JException {
     String result = submittRequest(password + "&9&" + filename);
     String[] results = split( result , "&" );
     return results;
  }
  

  /**
   * Get memory usage.
   * @throws NSClient4JException
   * @return double[] { Commit Limit, Commit Bytes } 
   */
  public double[] getMemoryage() throws NSClient4JException {
    String result = submittRequest(password + "&7" );
    String[] results = split(result, "&");
    double mem_commitLimit=Double.parseDouble(results[0]);
    double mem_commitByte=Double.parseDouble(results[1]);
    return new double[] { mem_commitLimit, mem_commitByte } ;
  }
  
  /**
   * Returns the port number connected to
   * @return int
   */
  public int getPortNumber() {
    return portNumber;
  }

  /**
   * Returns the host name connected to
   * @return String
   */
  public String getHostName() {
    return hostName;
  }





  /**
   * Generic request submission method used by all NSClient4j calls.
   * @param request String
   * @throws NSClient4JException
   * @return String
   */
  protected synchronized String submittRequest(String request) throws NSClient4JException {
    byte[] buffer = new byte[1024];
     baos.reset();
    String result = null;
    if(!inited) {
      initSocket();
    }
    try {
      socket.setSoTimeout(socketTimeout);
      os.write(request.getBytes());
      os.flush();
      while (true) {
        int read = bis.read(buffer);

        if (read > 0) {
          baos.write(buffer, 0, read);
          break;
        }
        else {
          break;
        }
      }
      result = baos.toString();
      testResult(result);
      return result;
    }
    catch (Exception ex) {
      inited = false;
      throw new NSClient4JException(ex.getMessage(), ex);
    }

  }


  /**
   * Static convenience method.
   * Reads the value of the counter on the specified host.
   * @param hostName String
   * @param port int
   * @param password String
   * @param counterName String
   * @return String
   */
  public static String getCounter(String hostName, int port, String password, String counterName) {
    try {
      NSClient4j client = new NSClient4j(hostName, port, password);
      return client.getPerfMonCounter(counterName);
    }
    catch (NSClient4JException ex) {
      return "Exception:" + ex;
    }
  }


  /**
   * Provides a simple version of Java 1.4's java.lang.String.split functionality in pre-Java 1.4 VMs.
   * @param s String The string to parse
   * @param delim String The delimeter to parse on
   * @return String[] Returns an array of strings of all the parsed values.
   */
  public static String[] split(String s, String delim) {
    StringTokenizer tokenizer = new StringTokenizer(s, delim);
    String[] result = new String[tokenizer.countTokens()];
    int i = 0;
    while(tokenizer.hasMoreTokens()) {
      result[i] = tokenizer.nextToken();
      i++;
    }
    return result;
  }

  /**
   * Tests the result coming back from the server and creates an appropriate exception if applicable.
   * @param result String
   * @throws NSClient4JServerException
   */
  public void testResult(String result) throws NSClient4JServerException {
      if(result==null || result.length() < 1 || result.indexOf(NSClient4JServerException.ERROR_MARKER)!=-1) {
      // Java 1.5 Specific Call
      // if(result==null || result.length() == 1 || result.contains(NSClient4JServerException.ERROR_MARKER)) {
          throw new NSClient4JServerException(result);
      }
  }
}


