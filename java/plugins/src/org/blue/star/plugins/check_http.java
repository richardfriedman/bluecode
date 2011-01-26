package org.blue.star.plugins;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class check_http extends check_base 
{
	
   protected String getAuthor() { return "Rob Blake rob.blake@arjuna.com"; }
   protected String getCopyright() { return "Arjuna 2007"; }
   protected String getDescription() { 
      return "\nUse this plugin to check HTTP connections to a remote host.\n";
   }
   protected String getNotes() {
      return "\nThis plugin uses HTTP to connect to the HTTP port of a specified Host. " +
             "It has support for basic user authentication, allows the user to specify their" +
             "agent, allows the user to specify an action on redirects and also the size of "+
             "the page returned and any expected messages. It returns critical if no connection " + 
             "could be made, warning if the connection was made but the returned data did not meet" +
             "user specifications, and OK at any other time. For more information please read the attached" + 
             "help";
   }
	
	private String httpExpect = "HTTP/1.";
	private String httpString = "http://";
	private String hostname;
	private String ipAddress;
	private int port = 80;
	private boolean useSSL;
	private long responseTime;
	private int certificateDays;
	private String contentString;
	private String urlString ="/";
	private String postString;
	private boolean usePost;
	private boolean noBody;
	private int maxAge;
	private String ageType;
	private boolean checkAge;
	private String contentType;
	private boolean lineSpan;
	private String regexString;
	private String eRegExString;
	private boolean invertRegEx;
	private String username;
	private String password;
	private String userAgent;
	private List<String> headerStringList = new ArrayList<String>();
	private long minPageSize;
	private long maxPageSize;
	private boolean comparePageSize;
	private int redirectDepth;
	private boolean verbose;
	private String redirectOption;
	private boolean followRedirect;
	private boolean useIp6;
	private long warningSeconds = -1;
	private long criticalSeconds = -1;
	private String check_message = "";
	private int check_state = common_h.STATE_UNKNOWN;
	private DecimalFormat format;
	
	private HttpClient client; 
	private HttpMethod method;
	
	
	public static void main(String[] args)
	{
		new check_http().process_request(args);
	}
	/* Give out some details about ourself */
	public void init_command()
	{
	}
	
	/* Add all our plugin options */
	public void add_command_arguments(Options options)
	{
		Option I = new Option("I","Ip Address",true,"Ip Address to try for HTTP connection");
		I.setArgName("IP Address");
		options.addOption(I);
		
		Option p = new Option("p","Port",true,"Port to try for HTTP connection");
		p.setArgName("Port");
		options.addOption(p);
		
		Option S = new Option("S","SSL",false,"Use SSL Connection");
		S.setArgName("SSL");
		options.addOption(S);
		
		Option C = new Option("C","Certificate",true,"Maximum Number of Days that Certificate has to be valid");
		C.setArgName("Number of Valid Days");
		options.addOption(C);
		
		Option e = new Option("e","Expect String",true,"String to Expect in first line of Server Response (default:" + httpExpect + ")");
		e.setArgName("Expect String");
		options.addOption(e);
		
		Option s = new Option("s","String",true,"String to Expect in the content");
		s.setArgName("String");
		options.addOption(s);
		
		Option u = new Option("u","URL Path",true,"URL to GET or POST (default: /");
		u.setArgName("URL Path");
		options.addOption(u);
		
		Option P = new Option("P","Post String",true,"URL Encoded POST data");
		P.setArgName("POST Data");
		options.addOption(P);
		
		Option N = new Option("N","No Body",false,"Don't wait for Document Body: stop after reading Headers");
		N.setArgName("No Body");
		options.addOption(N);
		
		Option M = new Option("M","Maximum Age",true,"Maximum age of the Document in Seconds");
		M.setArgName("Maximum Age");
		options.addOption(M);
		
		Option T = new Option("T","Content Type",true,"Content-Type header when POSTing");
		T.setArgName("Content-Type");
		options.addOption(T);
		
		Option l = new Option("l","Line Span",false,"Allow regex to span newlines");
		l.setArgName("Line Span");
		options.addOption(l);
		
		Option r = new Option("r","Regex",true,"Search page for Regex String");
		r.setArgName("String");
		options.addOption(r);
		
		Option R = new Option("R","ERegex",true,"Search page for case-insensitive regex String");
		R.setArgName("ERegex String");
		options.addOption(R);
		
		Option a = new Option("a","Auth-Pair",true,"Username:Password for sites with basic authentication");
		a.setArgName("Auth-Pair user:pass");
		options.addOption(a);
		
		Option A = new Option("A","User-Agent",true,"String for User-Agent");
		A.setArgName("User-Agent");
		options.addOption(A);
		
		Option k = new Option("k","Header String",true,"Any other String to be sent in Header (Use multiple times for additional headers");
		k.setArgName("Header String");
		options.addOption(k);
		
		Option f = new Option("f","On Redirect",true,"How to Handler redirected pages <ok|warning|critical|follow>"); 
		f.setArgName("On Redirect");
		options.addOption(f);
		
		Option m = new Option("m","Pagesize",true,"Minimum:Maximum Page size required in bytes");
		m.setArgName("Pagesize min:max");
		options.addOption(m);
		
		Option w = new Option("w","Warning",true,"The response time in Seconds that will generate a Warning return");
		w.setArgName("Warning Time");
		options.addOption(w);
		
		Option c = new Option("c","Critical",true,"The repsonse time in Seconds that will generate a Critical return");
		c.setArgName("Critical Time");
		options.addOption(c);
		
		OptionGroup group = new OptionGroup();
	    group.addOption(new Option("4", "use-ipv4", false, "Use IPv4 connection"));
	    group.addOption(new Option("6", "use-ipv6", false, "Use IPv6 connection"));
	    options.addOptionGroup(group);
	    
	}

	/* Process the user supplied options */
	public void process_command_option(Option o)
       throws IllegalArgumentException
	{
		String argValue = o.getValue();
		
		switch(o.getId())
		{
			case '4':
				useIp6 = false;
				break;
			case '6':
				useIp6 = true;
				break;
 			case 'H':
				if(netutils.is_host(argValue.trim()))
					hostname = argValue.trim();
				else 
                   throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Hostname> (%H) must be a valid Host\n", argValue ) );
                break;
			case 'I':
				 if(netutils.is_addr(argValue.trim()))
					 ipAddress = argValue.trim();
				 else 
				    throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Ip Address> (%I) must be a valid IP Address\n", argValue ) );
				 break;
			case 'p':
				if(utils.is_intnonneg(argValue))
					port = Integer.valueOf(argValue);
				else
                    throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Port> (%p) must be a non-negative number\n", argValue ) );
				break;
			case 'S':
				 useSSL = true;
				 break;
			case 'C':
				if(utils.is_intnonneg(argValue))
					certificateDays = Integer.valueOf(argValue.trim());
				else
                   throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Certificate Days> (%C) must be a non-negative number\n", argValue ) );
				break;
			case 'e':
				httpExpect = argValue.trim();
				break;
			case 's':
				contentString = argValue.trim();
				break;
			case 'u':
				urlString = argValue.trim();
				break;
			case 'P':
				postString = argValue.trim();
				usePost = true;
				break;
			case 'N':
				noBody = true;
				break;
			case 'M':
				if(utils.is_intnonneg(argValue.trim()))
				{
					maxAge = Integer.valueOf(argValue.trim());
					checkAge = true;
				}				
				else
					/* Perhaps we have an identifier on the end */
					if(utils.is_intnonneg(argValue.substring(0,argValue.length()-1)))
					{
						maxAge = Integer.valueOf(argValue.substring(0,argValue.length()-1));
						ageType = argValue.substring(argValue.length()-1);
						
						if(!checkValidTimeExtension(ageType))
							throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Max Age> (%M) can only have valid followers s|m|h|d\n", argValue ) );
						
						checkAge = true;
					}
					else
							throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Max Age> (%M) must be a non-negative Integer\n", argValue ) );
				break;
			case 'T':
					contentType = argValue.trim();
					break;
			case 'l':
					lineSpan = true;
					break;
			case 'r':
					regexString = argValue.trim();
					break;
			case 'R':
					eRegExString = argValue.trim();
					break;
			case 'n':
					invertRegEx = true;
					break;
			case 'a':
					if(isValidPair(argValue.trim()))
					{
						username = argValue.split(":")[0];
						password = argValue.split(":")[1];
					}
					else
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Auth String> (%a) must be a user:pass pairing\n", argValue ) );
					break;
			case 'A':
					userAgent = argValue.trim();
					break;
			case 'k':
					if(argValue.split(":").length == 2)
					headerStringList.add(argValue.trim());
					else
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Headers> (%k) must be in the format <header name>:<header value>\n", argValue ) );
					break;
			case 'f':
					if(isValidRedirectOption(argValue))
						redirectOption = argValue.trim();
					else
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<On Redirect> (%f) must be one of the following <ok|warning|critical|follow>\n", argValue ) );
					break;
			case 'm':
					if(argValue.split(":").length == 2)
						if(utils.is_intnonneg(argValue.split(":")[0].trim()) && utils.is_intnonneg(argValue.split(":")[1].trim()))
						{
							minPageSize = Long.parseLong(argValue.split(":")[0].trim());
							maxPageSize = Long.parseLong(argValue.split(":")[1].trim());
							comparePageSize = true;
						}
						else
                           throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Page Size> (%m) must both be a non-negative Integer", argValue ) );
					else
						if(utils.is_intnonneg(argValue.trim()))
						{
							minPageSize = Long.valueOf(argValue.trim());
							comparePageSize = true;
						}
						else
                           throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Page Size> (%m) must be a non-negative Integer", argValue ) );
					break;
			case 'w':
					if(utils.is_intnonneg(argValue.trim()))
						warningSeconds = Long.valueOf(argValue);
					else
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Warning> (%w) must be a non-negative Integer", argValue ) );
					break;
			case 'c':
					if(utils.is_intnonneg(argValue.trim()))
						criticalSeconds = Long.parseLong(argValue);
					else
                       throw new IllegalArgumentException( utils.formatArgumentError( this.getClass().getName(), "<Critical> (%c) must be a non-negative Integer", argValue ) );
					break;
		}
	}
	
	public void process_command_arguments(String[] argv)
	   throws IllegalArgumentException {
	}
	
	public void validate_command_arguments()
	{
		if(hostname == null && ipAddress == null)
		{
			throw new IllegalArgumentException( "You must specify a hostname and/or a IPAddress to connect to." );
		}
		
		if(criticalSeconds == -1 || warningSeconds == -1)
		{
			throw new IllegalArgumentException( "You must specify both Warning and Critical Thresholds in seconds for this plugin" );
		}
	}
	
	
	public boolean execute_check()
	{
		client = new HttpClient();
		
		long startTime;
		long endTime;
		byte[] responseBody;
		int statusCode;
		
		client.getParams().setSoTimeout(utils_h.timeout_interval);
		
		if(useSSL)
			httpString = "https://";
				
		if(usePost)
			if(hostname != null)
				method = new PostMethod(httpString + hostname + urlString);
			else
				method = new PostMethod(httpString + ipAddress + urlString);
		else
			if(hostname != null)
				method = new GetMethod(httpString + hostname + urlString);
			else
				method = new GetMethod(httpString + ipAddress + urlString);
		
		/* Add in user Agent Header if present */
		if(userAgent !=null)
			method.addRequestHeader("User-Agent:",userAgent);
		
		if(usePost && contentType !=null)
			method.addRequestHeader("Content-Type:",contentType);
		
		/* We'll handle redirects ourself */
		method.setFollowRedirects(false);
		
		/* Setup any Authentication requirements */
		if(username != null && password !=null)
		{
			method.setDoAuthentication(true);
			/* Uses urlString as the default realm for any credentials */
			client.getState().setCredentials(new AuthScope(hostname != null ? hostname : ipAddress,port,urlString),new UsernamePasswordCredentials(username,password));
		}
					
		/* Add the header strings provided by the user, user is responsible for these
		 * so I'm not going to sanity check them */
		for(String s: headerStringList)
			method.addRequestHeader(s.split(":")[0],s.split(":")[1]);
		
		/* Setup default retry handling - will retry the connection 3 times */
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(3,false));
		
		/* call the http request! */
		try
		{
			/* Rudimentary timing of call */
			startTime = System.currentTimeMillis();
			statusCode = client.executeMethod(method);
			endTime = System.currentTimeMillis();
			responseTime = endTime - startTime;
			
			/* See if we need to compare returned Status Line */
			if(!httpExpect.equals("HTTP/1."))
			{
				if(!method.getStatusLine().equals(httpExpect))
				{
					check_message = "Warning - Status Line not expected " + method.getStatusLine();
					check_state = common_h.STATE_WARNING;
					return true;
				}
			}
			
			/* Process anything with a status code of 300 or higher */
			if(statusCode >= 300)
				if(!processStatusCode(statusCode))
					return true;
			
			/* Are we following redirects ? */
			if(followRedirect)
				if(!processFollowRedirect())
					return true;
				
			responseBody = method.getResponseBody();
			
			/* Regular expression matching if needs be */
			if(!noBody && regexString != null)
				if(!check_regex(regexString,false))
					return true;
			
			/* Case insensitive Regular Expression matching if needs be */
			if(!noBody && eRegExString != null)
				if(!check_regex(eRegExString,true))
					return true;
			
			/* Compare response body size */
			if(!noBody && comparePageSize)
				if(!compareResponseBodySize(responseBody.length))
					return true;
			
			/* Run document Age checks if needs be */ 
			if(!noBody && checkAge)
				if(!checkDocumentAge())
					return true;

		}
		catch(Exception e)
		{
			check_message = "CRITICAL - " + e.getMessage();
			check_state = common_h.STATE_CRITICAL;
			return true;
		}
		finally
		{
			/* Close out our connection */
			method.releaseConnection();
		}
				
		return true;
	}

	public String check_message()
	{
		format = new DecimalFormat();
		format.setMaximumFractionDigits(3);
		double executeTime = responseTime;
		executeTime = executeTime/1000;		
	
		if(executeTime < warningSeconds)
			check_message = "OK - HTTP Request completed in " + format.format(executeTime) + " secs";
		else if(executeTime >= warningSeconds && executeTime < criticalSeconds)
			check_message = "Warning - HTTP Request completed in " + format.format(executeTime) + " secs";
		else if(executeTime >= criticalSeconds)
			check_message = "Critical - HTTP Request completed in " + format.format(executeTime) + " secs";

		return check_message;
	}

	public int check_state()
	{
		format = new DecimalFormat();
		format.setMaximumFractionDigits(3);
		double executeTime = responseTime;
		executeTime = executeTime/1000;
		
		if(executeTime < warningSeconds)
			check_state = common_h.STATE_OK;
		else if(executeTime >= warningSeconds && executeTime < criticalSeconds)
			check_state = common_h.STATE_WARNING;
		else if(executeTime >= criticalSeconds)
			check_state = common_h.STATE_CRITICAL;
		
		return check_state;
	}

	/* Check to see if a value pairing has been created properly */
	private boolean isValidPair(String paring)
	{
		if(paring.split(":").length !=2)
			return false;
		
		return true;
	}
	
	/* Check to see if the user specified redirect option is valid */
	private boolean isValidRedirectOption(String option)
	{
		if(option.trim().equalsIgnoreCase("ok") || option.trim().equalsIgnoreCase("warning") || option.trim().equalsIgnoreCase("critical") || option.trim().equalsIgnoreCase("follow"))
			return true;
		
		return false;
	}
	
	/* Method that deals with a range of response codes from any HTTP request */
	private boolean processStatusCode(int statusCode)
	{
		/*Deal with the status code in an acceptable manner */
		
		if(statusCode >=300 && statusCode < 400)
		{
			if(redirectOption.equalsIgnoreCase("follow"))
				followRedirect = true;
			else if(redirectOption.equalsIgnoreCase("OK"));
			else if(redirectOption.equalsIgnoreCase("warning"))
			{
				check_message = "Warning - " + method.getStatusText();
				check_state = common_h.STATE_WARNING;
				return false;
			}
			else if(redirectOption.equalsIgnoreCase("critical"))
			{
				check_message = "Critical - " + method.getStatusText();
				check_state = common_h.STATE_WARNING;
			}
				
				
		}
		else if(statusCode >= 400 && statusCode < 500)
		{
			check_message = "Warning - " + method.getStatusText();
			check_state = common_h.STATE_WARNING;
			followRedirect = false;
			return false;
		}
		else if(statusCode >= 500)
		{
			check_message = "Critical - " + method.getStatusText();
			check_state = common_h.STATE_CRITICAL;
			followRedirect = false;
			return false;
		}
		
		return true;
	}
	
	/* Method that deals with any redirects we have to follow */
	private boolean processFollowRedirect() throws HttpException,IOException
	{
		int redirectCount =0;
		Header locationHeader;
		int statusCode = -1;
		long startTime;
		long endTime;
		
		while(followRedirect && redirectCount <= redirectDepth)
		{
			/* Find out our redirect */
			locationHeader = method.getResponseHeader("location");
			method.setURI(new URI(locationHeader.getValue(),false));
			
			startTime = System.currentTimeMillis();
			statusCode = client.executeMethod(method);
			endTime = System.currentTimeMillis();
			responseTime = endTime - startTime;
			
			if(statusCode < 300)
				break;
			if(!processStatusCode(statusCode))
				return false;
		
			redirectCount++;
		}
		
		if(statusCode >= 300 && statusCode < 400)
		{
			check_message = "Warning - Redirect Depth Exceeded " + method.getStatusText();
			check_state = common_h.STATE_WARNING;
			return false;
		}
		
		return true;
	}

	/* Method that compares the received page size with any values set */
	private boolean compareResponseBodySize(long bodyLength)
	{
		/* Is maxPageSize set? */
		if(maxPageSize > 0)
		{
			if(bodyLength < minPageSize || bodyLength > maxPageSize)
			{
				check_message = "Warning - Request Size out of range - " + bodyLength + " bytes";
				check_state = common_h.STATE_WARNING;
				return false;
}
		}
		else
		{
			if(bodyLength < minPageSize)
			{
				check_message = "Warning - Request Size too small - " + bodyLength + " bytes";
				check_state = common_h.STATE_WARNING;
				return false;
			}
		}
		return true;
	}

    /* Check the Document Age - Only works if remote host adds correct Http Headers */
    private boolean checkDocumentAge()
    {
        String date = null;
        String lastMod = null;
        try
        {
            date = method.getRequestHeader("Date").getValue();
            lastMod = method.getRequestHeader("Last-Modified").getValue();
        }
        catch(Exception e)
        {}
        
        if(date == null || lastMod == null)
        {
            check_message = "Warning - Http Headers for Document Age not Set";
            check_state = common_h.STATE_WARNING;
            return false;
        }
        
        long today = processDateString(date);
        long docTime = processDateString(lastMod);
        long docAge = -1;
        String ageEnd =" secs";
        
        if(ageType == null || ageType.equalsIgnoreCase("s"))
        {
            docAge = (today - docTime)/1000;
        }
        else if(ageType.equalsIgnoreCase("m"))
        {
            docAge = (today - docTime)/1000/60;
            ageEnd = " mins";
        }
        else if(ageType.equalsIgnoreCase("h"))
        {
            docAge = (today - docTime)/1000/60/60;
            ageEnd = " hrs";
        }
        else if(ageType.equalsIgnoreCase("d"))
        {
            docAge = (today - docTime)/1000/60/60/24;
            ageEnd = " days";
        }
        
        if(docAge > maxAge)
        {
            check_message = "Warning - Document age limit exceeded - Age = " + docAge + ageEnd;
            check_state = common_h.STATE_WARNING;
            return false;
        }
        return true;
    }
    
    /* Method to parse the response strings from the server into a format from which
     * I can create a Calendar object */
    private long processDateString(String dateString)
    {
        String[] elements = dateString.split(":",2);
        DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", new Locale("en"));
        Calendar cal = Calendar.getInstance();
        Date date;
        
        try
        {
            date = format.parse(elements[1].trim());
            cal.setTimeInMillis(date.getTime());
        }
        catch(Exception e)
        {}
        return cal.getTimeInMillis();
    }
    
    private boolean check_regex(String regex,boolean caseInsensitive)
    {
        Pattern p;
        
        if(caseInsensitive)
             p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        else
            p = Pattern.compile(regex);
        //TODO - finish regex stuffs.
        
        return true;
    }
    
    /* Checks to see if the time extension the user has supplied is valid */
    private boolean checkValidTimeExtension(String timeExtension)
    {
        if(timeExtension.equalsIgnoreCase("s") || timeExtension.equalsIgnoreCase("m") || timeExtension.equalsIgnoreCase("h") || timeExtension.equalsIgnoreCase("d"))
            return true;
        
        return false;
    }
    
}
