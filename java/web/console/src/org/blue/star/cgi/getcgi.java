/*****************************************************************************
 *
 * Blue Star, a Java Port of .
 * Last Modified : 3/20/2006
 *
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 *****************************************************************************/

package org.blue.star.cgi;

import java.io.InputStreamReader;
import java.net.URLDecoder;

//#undef PARANOID_CGI_INPUT


public class getcgi {
/* Remove potentially harmful characters from CGI input that we don't need or want */
public static void sanitize_cgi_input(String[] cgivars){
//	String strptr;
//	int x,y,i;
//	int keep;

	/* don't strip for now... */
	return;

//	for(strptr=cgivars[i=0];strptr!=null;strptr=cgivars[++i]){
//
//		for(x=0,y=0;strptr[x]!='\x0';x++){
//
//			keep=1;
//
//			/* remove potentially nasty characters */
//			if(strptr[x]==';' || strptr[x]=='|' || strptr[x]=='&' || strptr[x]=='<' || strptr[x]=='>')
//				keep=0;
////#ifdef PARANOID_CGI_INPUT
////			else if(strptr[x]=='/' || strptr[x]=='\\')
////				keep=0;
////#endif
//			if(keep==1)
//				strptr[y++]=strptr[x];
//		        }
//
//		strptr[y]='\x0';
//	        }

//	return;
        }


///* convert encoded hex string (2 characters representing an 8-bit number) to its ASCII char equivalent */
//unsigned char hex_to_char(String input){
//	unsigned char outchar='\x0';
//	unsigned int outint;
//	char tempbuf[3];
//
//	/* null or empty string */
//	if(input==null)
//		return '\x0';
//	if(input[0]=='\x0')
//		return '\x0';
//
//	tempbuf[0]=input[0];
//	tempbuf[1]=input[1];
//	tempbuf[2]='\x0';
//
//	sscanf(tempbuf,"%X",&outint);
//
//	/* only convert "normal" ASCII characters - we don't want the rest.  Normally you would 
//	   convert all characters (i.e. for allowing users to post binary files), but since we
//	   aren't doing this, stay on the cautious side of things and reject outsiders... */
////#ifdef PARANOID_CGI_INPUT
////	if(outint<32 || outint>126)
////		outint=0;
////#endif
//
//	outchar=(unsigned char)outint;
//
//	return outchar;
//        }



/* unescape hex characters in CGI input */
public static String unescape_cgi_input(String input){
    
    return URLDecoder.decode( input );

        }

/** Simple testing main */
public static void main (String args[] ) {
    String[] cgis = getcgivars( null );
    for ( int x = 0; x < cgis.length; x+=2 )
        System.out.println( "("+cgis[x]+","+cgis[x+1]+")");
}

/* read the CGI input and place all name/val pairs into list. returns list containing name1, value1, name2, value2, ... , null */
/* this is a hacked version of a routine I found a long time ago somewhere - can't remember where anymore */
public static String[] getcgivars(String request_string ){
	int i;
	String request_method;
	String content_type;
	String content_length_string;
	int content_length;
	String cgiinput;
	String[] cgivars;
	String[] pairlist;


	/* initialize char variable(s) */
	cgiinput="";

	/* depending on the request method, read all CGI input into cgiinput */

	request_method=System.getenv("REQUEST_METHOD");
	if(request_method==null)
		request_method="";

    if ( request_string != null ) {
        cgiinput = request_string;
    }
    else if( request_method.equals("GET") || request_method.equals( "HEAD")){

		/* check for null query string environment variable - 04/28/00 (Ludo Bosmans) */
		cgiinput= System.getenv("QUERY_STRING");
        if ( cgiinput == null ) 
            cgiinput = "";

    }

	else if( request_method.equals( "POST") || request_method.equals("PUT")){

		/* if CONTENT_TYPE variable is not specified, RFC-2068 says we should assume it is "application/octet-string" */
		/* mobile (WAP) stations generate CONTENT_TYPE with charset, we we should only check first 33 chars */

		content_type=System.getenv("CONTENT_TYPE");
		if(content_type==null)
			content_type="";
                                                                         
		if( content_type.length() > 0  && content_type.equalsIgnoreCase("application/x-www-form-urlencoded" /* TODO ,33 */)){
			System.out.printf("getcgivars(): Unsupported Content-Type.\n");
			cgiutils.exit( 1);
		        }

		content_length_string=System.getenv("CONTENT_LENGTH");
		if(content_length_string==null)
			content_length_string="0";

		if(0==(content_length=atoi(content_length_string))){
			System.out.printf("getcgivars(): No Content-Length was sent with the POST request.\n") ;
			cgiutils.exit( 1);
		        }

        /* Updaed 2.7 */
        /* suspicious content length */
        if((content_length<0) || (content_length>=Integer.MAX_VALUE-1)){
            System.out.printf("getcgivars(): Suspicious Content-Length was sent with the POST request.\n");
            cgiutils.exit(1);
            }
        
		try {
		    InputStreamReader reader = new InputStreamReader( System.in );
		    char[] buffer = new char[content_length];
		    if ( reader.read( buffer, 0, content_length) != content_length ) {
		        System.out.printf("getcgivars(): Could not read input from STDIN.\n");
		        cgiutils.exit( 1);
		    }
		    cgiinput = new String(buffer);
		} catch (Exception e ) {
		    System.out.printf("getcgivars(): Could not read input from STDIN.\n");
		    cgiutils.exit( 1);
		    
		}
		
	}
	else{
	    
		System.out.printf("getcgivars(): Unsupported REQUEST_METHOD -> '%s'\n",request_method);
		System.out.printf("\n");
		System.out.printf("I'm guessing you're trying to execute the CGI from a command line.\n");
		System.out.printf("In order to do that, you need to set the REQUEST_METHOD environment\n");
		System.out.printf("variable to either \"GET\", \"HEAD\", or \"POST\".  When using the\n");
		System.out.printf("GET and HEAD methods, arguments can be passed to the CGI\n");
		System.out.printf("by setting the \"QUERY_STRING\" environment variable.  If you're\n");
		System.out.printf("using the POST method, data is read from standard input.  Also of\n");
		System.out.printf("note: if you've enabled authentication in the CGIs, you must set the\n");
		System.out.printf("\"REMOTE_USER\" environment variable to be the name of the user you're\n");
		System.out.printf("\"authenticated\" as.\n");
		System.out.printf("\n");

		cgiutils.exit( 1);
	        }

	/* change all plus signs back to spaces */
    cgiinput = cgiinput.replace( '+', ' ');

	/* first, split on ampersands (&) to extract the name-value pairs into pairlist */
	/* allocate memory for 256 name-value pairs at a time, increasing by same
	   amount as necessary... */

    pairlist = cgiinput.split ("[&]" );
    cgivars = new String[ pairlist.length * 2 ];

	/* extract the names and values from the pairlist */
	for(i=0;i<pairlist.length;i++){

        String[] split = pairlist[i].split ( "[=]",2);
		
        /* get the variable name preceding the equal (=) sign */
        if ( split.length == 2 )
            cgivars[i*2+1] = unescape_cgi_input( split[1] );
        else
            cgivars[i*2+1] = unescape_cgi_input( "" );

		/* get the variable value (or name/value of there was no real "pair" in the first place) */
        cgivars[i*2] = unescape_cgi_input( split[0] );
	        }

	/* sanitize the name-value strings */
	sanitize_cgi_input(cgivars);

	/* return the list of name-value strings */
	return cgivars;
        }



/* free() memory allocated to storing the CGI variables */
public static void free_cgivars(String[] cgivars){
        }

private static int atoi(String value) {
    try {
        return Integer.parseInt(value);
    } catch ( NumberFormatException nfE ) {
//        logger.throwing( cn, "atoi", nfE);
        return 0;
    }
}

}
