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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.blue.star.base.blue;
import org.blue.star.base.utils;
import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class statusmap extends blue_servlet {
    
public static String UNKNOWN_GD2_ICON      = "unknown.gd2";
public static String UNKNOWN_ICON_IMAGE    = "unknown.gif";
public static String NAGIOS_GD2_ICON       = "blue.gd2";
public static String NAGIOS_GD2_IMAGE      = "blue.gif";

public static final int  DEFAULT_NODE_WIDTH		= 40 ;
public static final int  DEFAULT_NODE_HEIGHT		= 65 ;

public static final int  DEFAULT_NODE_VSPACING           = 15 ;
public static final int  DEFAULT_NODE_HSPACING           = 45 ;

public static final int  DEFAULT_PROXIMITY_WIDTH		= 1000 ;
public static final int  DEFAULT_PROXIMITY_HEIGHT	= 800 ;

public static final int  MINIMUM_PROXIMITY_WIDTH         = 250 ;
public static final int  MINIMUM_PROXIMITY_HEIGHT        = 200 ;

public static final int  COORDS_WARNING_WIDTH            = 650 ;
public static final int  COORDS_WARNING_HEIGHT           = 60 ;

public static final int  CIRCULAR_DRAWING_RADIUS         = 100 ;

public static final int  CREATE_HTML	= 0 ;
public static final int  CREATE_IMAGE	= 1 ;

public static final int  LAYOUT_USER_SUPPLIED            = 0 ;
public static final int  LAYOUT_SUBLAYERS                = 1 ;
public static final int  LAYOUT_COLLAPSED_TREE           = 2 ;
public static final int  LAYOUT_BALANCED_TREE            = 3 ;
public static final int  LAYOUT_CIRCULAR                 = 4 ;
public static final int  LAYOUT_CIRCULAR_MARKUP          = 5 ;
public static final int  LAYOUT_CIRCULAR_BALLOON         = 6 ;

public static String physical_logo_images_path ; // MAX_FILENAME_LENGTH

public static cgiauth_h.authdata current_authdata;

public static int create_type=CREATE_HTML;

public static BufferedImage unknown_logo_image=null;
public static BufferedImage logo_image=null;
public static BufferedImage map_image=null;
public static Graphics2D gd_map_image;
public static BufferedImage background_image=null;

public static Color color_white;
public static Color color_black;
public static Color color_red;
public static Color color_lightred;
public static Color color_green;
public static Color color_lightgreen;
public static Color color_blue;
public static Color color_yellow;
public static Color color_orange;
public static Color color_grey;
public static Color color_lightgrey;

public static int show_all_hosts=common_h.TRUE;
public static String host_name="all";

public static int embedded=common_h.FALSE;
public static int display_header=common_h.TRUE;
public static int display_popups=common_h.TRUE;
public static int use_links=common_h.TRUE;
public static int use_text=common_h.TRUE;
public static int use_highlights=common_h.TRUE;
public static int user_supplied_canvas=common_h.FALSE;
public static int user_supplied_scaling=common_h.FALSE;

public static int layout_method=LAYOUT_USER_SUPPLIED;

public static int proximity_width=DEFAULT_PROXIMITY_WIDTH;
public static int proximity_height=DEFAULT_PROXIMITY_HEIGHT;

public static int coordinates_were_specified=common_h.FALSE;   /* were any coordinates specified in extended host information entries? */

public static int scaled_image_width=0;        /* size of the image actually displayed on the screen (after scaling) */
public static int scaled_image_height=0;
public static int canvas_width=0;              /* actual size of the image (or portion thereof) that we are drawing */
public static int canvas_height=0;
public static int total_image_width=0;         /* actual size of the image that would be created if we drew all hosts */
public static int total_image_height=0;
public static int max_image_width=0;           /* max image size the user wants (scaled) */
public static int max_image_height=0;
public static double scaling_factor=1.0;       /* scaling factor to use */
public static double user_scaling_factor=1.0;  /* user-supplied scaling factor */
public static int background_image_width=0;
public static int background_image_height=0;

public static int canvas_x=0;                     /* upper left coords of drawing canvas */
public static int canvas_y=0;

public static int bottom_margin=0;

public static int draw_child_links=common_h.FALSE;
public static int draw_parent_links=common_h.FALSE;

public static int draw_nagios_icon=common_h.FALSE;    /* should we drawn the Nagios process icon? */
public static int nagios_icon_x=0;           /* coords of Nagios icon */
public static int nagios_icon_y=0;

public static ArrayList<String>  layer_list= new ArrayList<String>();
public static int exclude_layers=common_h.TRUE;
public static int all_layers=common_h.FALSE;

public void reset_context() {
   physical_logo_images_path = null;

   current_authdata = new cgiauth_h.authdata ();

   create_type=CREATE_HTML;

   unknown_logo_image=null;
   logo_image=null;
   map_image=null;
   gd_map_image = null;
   background_image=null;

   color_white = null;
   color_black = null;
   color_red = null;
   color_lightred = null;
   color_green = null;
   color_lightgreen = null;
   color_blue = null;
   color_yellow = null;
   color_orange = null;
   color_grey = null;
   color_lightgrey = null;

   show_all_hosts=common_h.TRUE;
   host_name="all";

   embedded=common_h.FALSE;
   display_header=common_h.TRUE;
   display_popups=common_h.TRUE;
   use_links=common_h.TRUE;
   use_text=common_h.TRUE;
   use_highlights=common_h.TRUE;
   user_supplied_canvas=common_h.FALSE;
   user_supplied_scaling=common_h.FALSE;

   layout_method=LAYOUT_USER_SUPPLIED;

   proximity_width=DEFAULT_PROXIMITY_WIDTH;
   proximity_height=DEFAULT_PROXIMITY_HEIGHT;

   coordinates_were_specified=common_h.FALSE;   /* were any coordinates specified in extended host information entries? */

   scaled_image_width=0;        /* size of the image actually displayed on the screen (after scaling) */
   scaled_image_height=0;
   canvas_width=0;              /* actual size of the image (or portion thereof) that we are drawing */
   canvas_height=0;
   total_image_width=0;         /* actual size of the image that would be created if we drew all hosts */
   total_image_height=0;
   max_image_width=0;           /* max image size the user wants (scaled) */
   max_image_height=0;
   scaling_factor=1.0;       /* scaling factor to use */
   user_scaling_factor=1.0;  /* user-supplied scaling factor */
   background_image_width=0;
   background_image_height=0;

   canvas_x=0;                     /* upper left coords of drawing canvas */
   canvas_y=0;

   bottom_margin=0;

   draw_child_links=common_h.FALSE;
   draw_parent_links=common_h.FALSE;

   draw_nagios_icon=common_h.FALSE;    /* should we drawn the Nagios process icon? */
   nagios_icon_x=0;           /* coords of Nagios icon */
   nagios_icon_y=0;

   layer_list.clear();
   exclude_layers=common_h.TRUE;
   all_layers=common_h.FALSE;
}

public void call_main() {
   main( null );
}

public static void main(String[] args){
	int result;

    /* read the CGI configuration file */
	result=cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		if(create_type==CREATE_HTML)
            cgiutils.cgi_config_file_error(cgiutils.get_cgi_config_location());
		document_footer();
		cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* defaults from CGI config file */
    layout_method=cgiutils.default_statusmap_layout_method;

    /* get the arguments passed in the URL */
    process_cgivars();
    
    /* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		if(create_type==CREATE_HTML)
            cgiutils.main_config_file_error(cgiutils.main_config_file);
		document_footer();
        cgiutils.exit(  common_h.ERROR );
        return;
	        }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		if(create_type==CREATE_HTML)
			cgiutils.object_data_error();
		document_footer();
        cgiutils.exit(  common_h.ERROR );
        return;
                }

	/* read all status data */
	result=cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(),statusdata_h.READ_ALL_STATUS_DATA);
	if(result==common_h.ERROR){
		document_header(common_h.FALSE);
		if(create_type==CREATE_HTML)
			cgiutils.status_data_error();
		document_footer();
        cgiutils.exit(  common_h.ERROR );
        return;
                }


	document_header(common_h.TRUE);

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	/* display the network map... */
	display_map();

	document_footer();

    cgiutils.exit(  common_h.OK );
        }



public static void document_header(int use_stylesheet){
	String date_time ; // MAX_DATETIME_LENGTH

	if(create_type==CREATE_HTML){
        if ( response != null ) {
           response.setHeader( "Cache-Control",  "no-store" );
           response.setHeader( "Pragma",  "no-cache" );
           response.setIntHeader( "Refresh", cgiutils.refresh_rate );
           response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
           response.setDateHeader( "Expires", System.currentTimeMillis() );
           response.setContentType("text/html");
        } else {
      		System.out.printf("Cache-Control: no-store\r\n");
      		System.out.printf("Pragma: no-cache\r\n");
      		System.out.printf("Refresh: %d\r\n",cgiutils.refresh_rate);
      
      		date_time = cgiutils.get_time_string(0,common_h.HTTP_DATE_TIME);
      		System.out.printf("Last-Modified: %s\r\n",date_time);
      		
      		date_time = cgiutils.get_time_string( 0,common_h.HTTP_DATE_TIME);
      		System.out.printf("Expires: %s\r\n",date_time);
      		
      		System.out.printf("Content-Type: text/html\r\n\r\n");
        }
        
		if(embedded==common_h.TRUE)
			return;

		System.out.printf("<html>\n");
		System.out.printf("<head>\n");
		System.out.printf("<title>\n");
		System.out.printf("Network Map\n");
		System.out.printf("</title>\n");

		if(use_stylesheet==common_h.TRUE){
			System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.COMMON_CSS);
			System.out.printf("<LINK REL='stylesheet' TYPE='text/css' HREF='%s%s'>\n",cgiutils.url_stylesheets_path,cgiutils_h.STATUSMAP_CSS);
		        }

		/* write JavaScript code for popup window */
		write_popup_code();

		System.out.printf("</head>\n");
		
		System.out.printf("<body CLASS='statusmap' name='mappage' id='mappage'>\n");

		/* include user SSI header */
        cgiutils.include_ssi_files(cgiutils_h.STATUSMAP_CGI,cgiutils_h.SSI_HEADER);

		System.out.printf("<div id=\"popup\" style=\"position:absolute; z-index:1; visibility: hidden\"></div>\n");
	        }

	else{
        if ( response != null ) {
           response.setHeader( "Cache-Control",  "no-store" );
           response.setHeader( "Pragma",  "no-cache" );
           response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
           response.setDateHeader( "Expires", System.currentTimeMillis() );
           response.setContentType("image/png");
        } else {
      		System.out.printf("Cache-Control: no-store\r\n");
      		System.out.printf("Pragma: no-cache\r\n");
      		
      		date_time = cgiutils.get_time_string(0,common_h.HTTP_DATE_TIME);
      		System.out.printf("Last-Modified: %s\r\n",date_time);
      		
      		date_time = cgiutils.get_time_string(0,common_h.HTTP_DATE_TIME);
      		System.out.printf("Expires: %s\r\n",date_time);
      
      		System.out.printf("Content-Type: image/png\r\n\r\n");
        }
        }

	return;
        }


public static void document_footer(){

	if(embedded==common_h.TRUE)
		return;

	if(create_type==CREATE_HTML){

		/* include user SSI footer */
		cgiutils.include_ssi_files(cgiutils_h.STATUSMAP_CGI,cgiutils_h.SSI_FOOTER);

		System.out.printf("</body>\n");
		System.out.printf("</html>\n");
	        }

	return;
        }



public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables= getcgi.getcgivars( request_string );

	for(x=0; x < variables.length ;x++){

		/* do some basic length checking on the variable identifier to prevent buffer overflows */
		if( variables[x].length() >= common_h.MAX_INPUT_BUFFER-1){
			x++;
			continue;
		        }

		/* we found the host argument */
		else if(variables[x].equals("host")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

				host_name = variables[x];

			if( host_name.equals( "all"))
				show_all_hosts=common_h.TRUE;
			else
				show_all_hosts=common_h.FALSE;
		        }

		/* we found the image creation option */
		else if(variables[x].equals("createimage")){
			create_type=CREATE_IMAGE;
		        }

		/* we found the embed option */
		else if(variables[x].equals("embedded"))
			embedded=common_h.TRUE;

		/* we found the noheader option */
		else if(variables[x].equals("noheader"))
			display_header=common_h.FALSE;

		/* we found the canvas origin */
		else if(variables[x].equals("canvas_x")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			canvas_x=atoi(variables[x]);
			user_supplied_canvas=common_h.TRUE;
		        }
		else if(variables[x].equals("canvas_y")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			canvas_y=atoi(variables[x]);
			user_supplied_canvas=common_h.TRUE;
		        }

		/* we found the canvas size */
		else if(variables[x].equals("canvas_width")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			canvas_width=atoi(variables[x]);
			user_supplied_canvas=common_h.TRUE;
		        }
		else if(variables[x].equals("canvas_height")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			canvas_height=atoi(variables[x]);
			user_supplied_canvas=common_h.TRUE;
		        }
		else if(variables[x].equals("proximity_width")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			proximity_width=atoi(variables[x]);
			if(proximity_width<0)
				proximity_width=DEFAULT_PROXIMITY_WIDTH;
		        }
		else if(variables[x].equals("proximity_height")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			proximity_height=atoi(variables[x]);
			if(proximity_height<0)
				proximity_height=DEFAULT_PROXIMITY_HEIGHT;
		        }

		/* we found the scaling factor */
		else if(variables[x].equals("scaling_factor")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			user_scaling_factor=strtod(variables[x],null);
			if(user_scaling_factor>0.0)
				user_supplied_scaling=common_h.TRUE;
		        }

		/* we found the max image size */
		else if(variables[x].equals("max_width")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			max_image_width=atoi(variables[x]);
		        }
		else if(variables[x].equals("max_height")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			max_image_height=atoi(variables[x]);
		        }

		/* we found the layout method option */
		else if(variables[x].equals("layout")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			layout_method=atoi(variables[x]);
		}

		/* we found the no links argument*/
		else if(variables[x].equals("nolinks"))
			use_links=common_h.FALSE;

		/* we found the no text argument*/
		else if(variables[x].equals("notext"))
			use_text=common_h.FALSE;

		/* we found the no highlights argument*/
		else if(variables[x].equals("nohighlights"))
			use_highlights=common_h.FALSE;

		/* we found the no popups argument*/
		else if(variables[x].equals("nopopups"))
			display_popups=common_h.FALSE;

		/* we found the layer inclusion/exclusion argument */
		else if(variables[x].equals("layermode")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			if(variables[x].equals("include"))
				exclude_layers=common_h.FALSE;
			else
				exclude_layers=common_h.TRUE;
		        }

		/* we found the layer argument */
		else if(variables[x].equals("layer")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }

			add_layer(variables[x]);
		        }
	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



/* top of page */
public static void display_page_header(){
	String temp_buffer ; // MAX_INPUT_BUFFER
	int zoom;
	int zoom_width, zoom_height;
	int zoom_width_granularity=0;
	int zoom_height_granularity=0;
	int current_zoom_granularity=0;
	objects_h.hostgroup temp_hostgroup;
//	layer *temp_layer;
	int found=0;


	if(create_type!=CREATE_HTML)
		return;

	if(display_header==common_h.TRUE){

		/* begin top table */
		System.out.printf("<table border=0 width=100%% cellspacing=0 cellpadding=0>\n");
		System.out.printf("<tr>\n");

		/* left column of the first row */
		System.out.printf("<td align=left valign=top>\n");

		if(show_all_hosts==common_h.TRUE)
			temp_buffer = "Network Map For All Hosts";
		else
			temp_buffer= String.format( "Network Map For Host <I>%s</I>",host_name);
		cgiutils.display_info_table(temp_buffer,common_h.TRUE,current_authdata);

		System.out.printf("<TABLE BORDER=1 CELLPADDING=0 CELLSPACING=0 CLASS='linkBox'>\n");
		System.out.printf("<TR><TD CLASS='linkBox'>\n");

		if(show_all_hosts==common_h.FALSE){
			System.out.printf("<a href='%s?host=all&max_width=%d&max_height=%d'>View Status Map For All Hosts</a><BR>",cgiutils_h.STATUSMAP_CGI,max_image_width,max_image_height);
			System.out.printf("<a href='%s?host=%s'>View Status Detail For This Host</a><BR>\n",cgiutils_h.STATUS_CGI,cgiutils.url_encode(host_name));
		        }
		System.out.printf("<a href='%s?host=all'>View Status Detail For All Hosts</a><BR>\n",cgiutils_h.STATUS_CGI);
		System.out.printf("<a href='%s?hostgroup=all'>View Status Overview For All Hosts</a>\n",cgiutils_h.STATUS_CGI);

		System.out.printf("</TD></TR>\n");
		System.out.printf("</TABLE>\n");

		System.out.printf("</td>\n");



		/* center column of top row */
		System.out.printf("<td align=center valign=center>\n");

		/* print image size and scaling info */
//#ifdef DEBUG
//		System.out.printf("<p><div align=center><font size=-1>\n");
//		System.out.printf("[ Raw Image Size: %d x %d pixels | Scaling Factor: %1.2lf | Scaled Image Size: %d x %d pixels ]",canvas_width,canvas_height,scaling_factor,(int)(canvas_width*scaling_factor),(int)(canvas_height*scaling_factor));
//		System.out.printf("</font></div></p>\n");
//
//		System.out.printf("<p><div align=center><font size=-1>\n");
//		System.out.printf("[ Canvas_x: %d | Canvas_y: %d | Canvas_width: %d | Canvas_height: %d ]",canvas_x,canvas_y,canvas_width,canvas_height);
//		System.out.printf("</font></div></p>\n");
//#endif

		/* zoom links */
		if(user_supplied_canvas==common_h.FALSE && !host_name.equals("all") && display_header==common_h.TRUE){
			
			System.out.printf("<p><div align=center>\n");

			zoom_width_granularity=((total_image_width-MINIMUM_PROXIMITY_WIDTH)/11);
			if(zoom_width_granularity==0)
				zoom_width_granularity=1;
			zoom_height_granularity=((total_image_height-MINIMUM_PROXIMITY_HEIGHT)/11);

			if(proximity_width<=0)
				current_zoom_granularity=0;
			else
				current_zoom_granularity=(total_image_width-proximity_width)/zoom_width_granularity;
			if(current_zoom_granularity>10)
				current_zoom_granularity=10;

			System.out.printf("<table border=0 cellpadding=0 cellspacing=2>\n");
			System.out.printf("<tr>\n");
			System.out.printf("<td valign=center class='zoomTitle'>Zoom Out&nbsp;&nbsp;</td>\n");

			for(zoom=0;zoom<=10;zoom++){

				zoom_width=total_image_width-(zoom*zoom_width_granularity);
				zoom_height=total_image_height-(zoom*zoom_height_granularity);

				System.out.printf("<td valign=center><a href='%s?host=%s&layout=%d&max_width=%d&max_height=%d&proximity_width=%d&proximity_height=%d%s%s",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(host_name),layout_method,max_image_width,max_image_height,zoom_width,zoom_height,(display_header==common_h.TRUE)?"":"&noheader",(display_popups==common_h.FALSE)?"&nopopups":"");
				if(user_supplied_scaling==common_h.TRUE)
					System.out.printf("&scaling_factor=%2.1f",user_scaling_factor);
				print_layer_url(common_h.TRUE);
				System.out.printf("'>");
				System.out.printf("<img src='%s%s' border=0 alt='%d' title='%d'></a></td>\n",cgiutils.url_images_path,(current_zoom_granularity==zoom)?cgiutils_h.ZOOM2_ICON:cgiutils_h.ZOOM1_ICON,zoom,zoom);
		                }

			System.out.printf("<td valign=center class='zoomTitle'>&nbsp;&nbsp;Zoom In</td>\n");
			System.out.printf("</tr>\n");
			System.out.printf("</table>\n");

			System.out.printf("</div></p>\n");
	                }

		System.out.printf("</td>\n");



		/* right hand column of top row */
		System.out.printf("<td align=right valign=top>\n");

		System.out.printf("<table border=0 CLASS='optBox'>\n");
		System.out.printf("<tr><td valign=top>\n");
		System.out.printf("<form method=\"POST\" action=\"%s\">\n",cgiutils_h.STATUSMAP_CGI);
		System.out.printf("<input type='hidden' name='host' value='%s'>\n",host_name);
		System.out.printf("<input type='hidden' name='layout' value='%d'>\n",layout_method);

		System.out.printf("</td><td valign=top>\n");

		System.out.printf("<table border=0>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'>\n");
		System.out.printf("Layout Method:<br>\n");
		System.out.printf("<select name='layout'>\n");
//#ifndef DUMMY_INSTALL
//		System.out.printf("<option value=%d %s>User-supplied coords\n",LAYOUT_USER_SUPPLIED,(layout_method==LAYOUT_USER_SUPPLIED)?"selected":"");
//#endif
		System.out.printf("<option value=%d %s>Depth layers\n",LAYOUT_SUBLAYERS,(layout_method==LAYOUT_SUBLAYERS)?"selected":"");
		System.out.printf("<option value=%d %s>Collapsed tree\n",LAYOUT_COLLAPSED_TREE,(layout_method==LAYOUT_COLLAPSED_TREE)?"selected":"");
		System.out.printf("<option value=%d %s>Balanced tree\n",LAYOUT_BALANCED_TREE,(layout_method==LAYOUT_BALANCED_TREE)?"selected":"");
		System.out.printf("<option value=%d %s>Circular\n",LAYOUT_CIRCULAR,(layout_method==LAYOUT_CIRCULAR)?"selected":"");
		System.out.printf("<option value=%d %s>Circular (Marked Up)\n",LAYOUT_CIRCULAR_MARKUP,(layout_method==LAYOUT_CIRCULAR_MARKUP)?"selected":"");
		System.out.printf("<option value=%d %s>Circular (Balloon)\n",LAYOUT_CIRCULAR_BALLOON,(layout_method==LAYOUT_CIRCULAR_BALLOON)?"selected":"");
		System.out.printf("</select>\n");
		System.out.printf("</td>\n");
		System.out.printf("<td CLASS='optBoxItem'>\n");
		System.out.printf("Scaling factor:<br>\n");
		System.out.printf("<input type='text' name='scaling_factor' maxlength='5' size='4' value='%2.1f'>\n",(user_supplied_scaling==common_h.TRUE)?user_scaling_factor:0.0);
		System.out.printf("</td></tr>\n");

		/*
		System.out.printf("<tr><td CLASS='optBoxItem'>\n");
		System.out.printf("Max image width:<br>\n");
		System.out.printf("<input type='text' name='max_width' maxlength='5' size='4' value='%d'>\n",max_image_width);
		System.out.printf("</td>\n");
		System.out.printf("<td CLASS='optBoxItem'>\n");
		System.out.printf("Max image height:<br>\n");
		System.out.printf("<input type='text' name='max_height' maxlength='5' size='4' value='%d'>\n",max_image_height);
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'>\n");
		System.out.printf("Proximity width:<br>\n");
		System.out.printf("<input type='text' name='proximity_width' maxlength='5' size='4' value='%d'>\n",proximity_width);
		System.out.printf("</td>\n");
		System.out.printf("<td CLASS='optBoxItem'>\n");
		System.out.printf("Proximity height:<br>\n");
		System.out.printf("<input type='text' name='proximity_height' maxlength='5' size='4' value='%d'>\n",proximity_height);
		System.out.printf("</td></tr>\n");
		*/

		System.out.printf("<input type='hidden' name='max_width' value='%d'>\n",max_image_width);
		System.out.printf("<input type='hidden' name='max_height' value='%d'>\n",max_image_height);
		System.out.printf("<input type='hidden' name='proximity_width' value='%d'>\n",proximity_width);
		System.out.printf("<input type='hidden' name='proximity_height' value='%d'>\n",proximity_height);

		System.out.printf("<tr><td CLASS='optBoxItem'>Drawing Layers:<br>\n");
		System.out.printf("<select multiple name='layer' size='4'>\n");
		for(Iterator iter = objects.hostgroup_list.iterator(); iter.hasNext(); ){
            temp_hostgroup= (objects_h.hostgroup) iter.next();
            
			if( cgiauth.is_authorized_for_hostgroup(temp_hostgroup,current_authdata)==common_h.FALSE)
				continue;
			found=0;
			for( String layer_name : layer_list ){
				if( layer_name.equals( temp_hostgroup.group_name)){
					found=1;
					break;
				        }
			        }
			System.out.printf("<option value='%s' %s>%s\n",temp_hostgroup.group_name,(found==1)?"SELECTED":"",temp_hostgroup.alias);
		        }
		System.out.printf("</select>\n");
		System.out.printf("</td><td CLASS='optBoxItem' valign=top>Layer mode:<br>");
		System.out.printf("<input type='radio' name='layermode' value='include' %s>Include<br>\n",(exclude_layers==common_h.FALSE)?"CHECKED":"");
		System.out.printf("<input type='radio' name='layermode' value='exclude' %s>Exclude\n",(exclude_layers==common_h.TRUE)?"CHECKED":"");
		System.out.printf("</td></tr>\n");

		System.out.printf("<tr><td CLASS='optBoxItem'>\n");
		System.out.printf("Suppress popups:<br>\n");
		System.out.printf("<input type='checkbox' name='nopopups' %s>\n",(display_popups==common_h.FALSE)?"CHECKED":"");
		System.out.printf("</td><td CLASS='optBoxItem'>\n");
		System.out.printf("<input type='submit' value='Update'>\n");
		System.out.printf("</td></tr>\n");

		/* display context-sensitive help */
		System.out.printf("<tr><td></td><td align=right valign=bottom>\n");
		cgiutils.display_context_help(cgiutils_h.CONTEXTHELP_MAP);
		System.out.printf("</td></tr>\n");

		System.out.printf("</table>\n");

		System.out.printf("</form>\n");
		System.out.printf("</td></tr>\n");
		System.out.printf("</table>\n");

		System.out.printf("</td>\n");
	
		/* end of top table */
		System.out.printf("</tr>\n");
		System.out.printf("</table>\n");
	        }


	return;
        }



/* top-level map generation... */
public static void display_map(){

	load_background_image();
	calculate_host_coords(); 
	calculate_total_image_bounds();
	calculate_canvas_bounds();
	calculate_scaling_factor();
	find_eligible_hosts();

	/* display page header */
	display_page_header();

	initialize_graphics();
	draw_background_image();
	draw_background_extras();
	draw_host_links();

	if(create_type==CREATE_HTML)
		System.out.printf("<map name='statusmap'>\n");

	draw_hosts();

	if(create_type==CREATE_HTML)
		System.out.printf("</map>\n");

	write_graphics();
	cleanup_graphics();


	/* write the URL location for the image we just generated - the web browser will come and get it... */
	if(create_type==CREATE_HTML){
		System.out.printf("<P><DIV ALIGN=center>\n");
		System.out.printf("<img src='%s?host=%s&createimage",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(host_name));
		System.out.printf("&canvas_x=%d&canvas_y=%d&canvas_width=%d&canvas_height=%d&max_width=%d&max_height=%d&layout=%d%s%s%s",canvas_x,canvas_y,canvas_width,canvas_height,max_image_width,max_image_height,layout_method,(use_links==common_h.FALSE)?"&nolinks":"",(use_text==common_h.FALSE)?"&notext":"",(use_highlights==common_h.FALSE)?"&nohighlights":"");
		print_layer_url(common_h.TRUE);
		System.out.printf("' width=%d height=%d border=0 name='statusimage' useMap='#statusmap'>\n",(int)(canvas_width*scaling_factor),(int)(canvas_height*scaling_factor));
		System.out.printf("</DIV></P>\n");
	        }
	
	return;
        }



/******************************************************************/
/********************* CALCULATION FUNCTIONS **********************/
/******************************************************************/

/* calculates host drawing coordinates */
public static void calculate_host_coords(){
	objects_h.hostextinfo temp_hostextinfo;
    objects_h.host this_host;
    objects_h.host temp_host;
	int child_hosts=0;
	int parent_hosts=0;
	int max_layer_width=1;
	int current_child_host=0;
	int current_parent_host=0;
	int center_x=0;
	int offset_x=DEFAULT_NODE_WIDTH/2;
	int offset_y=DEFAULT_NODE_WIDTH/2;
	int current_layer=0;
	int layer_members=0;
	int current_layer_member=0;
	int max_drawing_width=0;
  
	/******************************/
	/***** MANUAL LAYOUT MODE *****/
	/******************************/

	/* user-supplied coords */
	if(layout_method==LAYOUT_USER_SUPPLIED){

		/* see which hosts we should draw and calculate drawing coords */
		for(Iterator iter = objects.hostextinfo_list.iterator(); iter.hasNext(); ){
            temp_hostextinfo= (objects_h.hostextinfo) iter.next();
			
			if(temp_hostextinfo.have_2d_coords==common_h.TRUE)
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
			else
				temp_hostextinfo.should_be_drawn=common_h.FALSE;
		        }

		return;
	        }


	/*****************************/
	/***** AUTO-LAYOUT MODES *****/
	/*****************************/

	/* add empty extended host info entries for all hosts that don't have any */
	for(Iterator iter = objects.host_list.iterator(); iter.hasNext(); ){
	    temp_host = (objects_h.host) iter.next();

		/* find the corresponding hostextinfo definition */
		temp_hostextinfo = objects.find_hostextinfo(temp_host.name);

		/* none was found, so add a blank one */
		if(temp_hostextinfo==null)
			objects.add_hostextinfo(temp_host.name,null,null,null,null,null,null,null,0,0,0.0,0.0,0.0,0,0);
	        }


	/***** DEPTH LAYER MODE *****/
	if(layout_method==LAYOUT_SUBLAYERS){

		/* find the "main" host we're displaying */
		if(show_all_hosts==common_h.TRUE)
			this_host=null;
		else
			this_host=objects.find_host(host_name);

		/* find total number of immediate parents/children for this host */
		child_hosts=objects.number_of_immediate_child_hosts(this_host);
		parent_hosts=objects.number_of_immediate_parent_hosts(this_host);

		if(child_hosts==0 && parent_hosts==0)
			max_layer_width=1;
		else
			max_layer_width=(child_hosts>parent_hosts)?child_hosts:parent_hosts;

		/* calculate center x coord */
		center_x=(((DEFAULT_NODE_WIDTH*max_layer_width)+(DEFAULT_NODE_HSPACING*(max_layer_width-1)))/2)+offset_x;

		/* coords for Nagios icon if necessary */
		if(this_host==null || this_host.parent_hosts==null || this_host.parent_hosts.size() == 0 ){
			nagios_icon_x=center_x;
			nagios_icon_y=offset_y;
			draw_nagios_icon=common_h.TRUE;
		        }

		/* do we need to draw a link to parent(s)? */
		if(this_host!=null && objects.is_host_immediate_child_of_host(null,this_host)==common_h.FALSE){
			draw_parent_links=common_h.TRUE;
			offset_y+=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;
		        }

		/* see which hosts we should draw and calculate drawing coords */
        for(Iterator iter = objects.hostextinfo_list.iterator(); iter.hasNext(); ){
            temp_hostextinfo= (objects_h.hostextinfo) iter.next();
            
			/* find the host that matches this entry */
			temp_host=objects.find_host(temp_hostextinfo.host_name);

			if(temp_host==null)
				continue;
			
			/* this is an immediate parent of the "main" host we're drawing */
			else if(objects.is_host_immediate_parent_of_host(this_host,temp_host)==common_h.TRUE){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x-(((parent_hosts*DEFAULT_NODE_WIDTH)+((parent_hosts-1)*DEFAULT_NODE_HSPACING))/2)+(current_parent_host*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
				temp_hostextinfo.y_2d=offset_y;
				current_parent_host++;
			        }
			
			/* this is the "main" host we're drawing */
			else if(this_host==temp_host){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x;
				temp_hostextinfo.y_2d=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y;
			        }

			/* this is an immediate child of the "main" host we're drawing */
			else if(objects.is_host_immediate_child_of_host(this_host,temp_host)==common_h.TRUE){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x-(((child_hosts*DEFAULT_NODE_WIDTH)+((child_hosts-1)*DEFAULT_NODE_HSPACING))/2)+(current_child_host*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
				if(this_host==null)
					temp_hostextinfo.y_2d=(DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING)+offset_y;
				else
					temp_hostextinfo.y_2d=((DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING)*2)+offset_y;
				current_child_host++;
				if( objects.number_of_immediate_child_hosts(temp_host)>0){
					bottom_margin=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;
					draw_child_links=common_h.TRUE;
				        }
			        }

			/* else do not draw this host */
			else{
				temp_hostextinfo.should_be_drawn=common_h.FALSE;
				temp_hostextinfo.have_2d_coords=common_h.FALSE;
			        }
		        }
	        }



	/***** COLLAPSED TREE MODE *****/
	else if(layout_method==LAYOUT_COLLAPSED_TREE){

		/* find the "main" host we're displaying  - DO NOT USE THIS (THIS IS THE OLD METHOD) */
		/*
		if(show_all_hosts==common_h.TRUE)
			this_host=null;
		else
			this_host=find_host(host_name);
		*/

		/* always use null as the "main" host, screen coords/dimensions are adjusted automatically */
		this_host=null;

		/* find total number of immediate parents for this host */
		parent_hosts=objects.number_of_immediate_parent_hosts(this_host);

		/* find the max layer width we have... */
		max_layer_width=max_child_host_layer_members(this_host);
		if(parent_hosts>max_layer_width)
			max_layer_width=parent_hosts;

		/* calculate center x coord */
		center_x=(((DEFAULT_NODE_WIDTH*max_layer_width)+(DEFAULT_NODE_HSPACING*(max_layer_width-1)))/2)+offset_x;

		/* coords for Nagios icon if necessary */
		if(this_host==null || this_host.parent_hosts==null || this_host.parent_hosts.size() == 0 ){
			nagios_icon_x=center_x;
			nagios_icon_y=offset_y;
			draw_nagios_icon=common_h.TRUE;
		        }

		/* do we need to draw a link to parent(s)? */
		if(this_host!=null && objects.is_host_immediate_child_of_host(null,this_host)==common_h.FALSE){
			draw_parent_links=common_h.TRUE;
			offset_y+=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;
		        }

		/* see which hosts we should draw and calculate drawing coords */
        for(Iterator iter = objects.hostextinfo_list.iterator(); iter.hasNext(); ){
            temp_hostextinfo= (objects_h.hostextinfo) iter.next();

			/* find the host that matches this entry */
			temp_host=objects.find_host(temp_hostextinfo.host_name);

			if(temp_host==null)
				continue;
			
			/* this is an immediate parent of the "main" host we're drawing */
			else if(objects.is_host_immediate_parent_of_host(this_host,temp_host)==common_h.TRUE){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x-(((parent_hosts*DEFAULT_NODE_WIDTH)+((parent_hosts-1)*DEFAULT_NODE_HSPACING))/2)+(current_parent_host*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
				temp_hostextinfo.y_2d=offset_y;
				current_parent_host++;
			        }
			
			/* this is the "main" host we're drawing */
			else if(this_host==temp_host){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x;
				temp_hostextinfo.y_2d=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y;
			        }

			/* else do not draw this host (we might if its a child - see below, but assume no for now) */
			else{
				temp_hostextinfo.should_be_drawn=common_h.FALSE;
				temp_hostextinfo.have_2d_coords=common_h.FALSE;
			        }
		        }


		/* TODO: REORDER CHILD LAYER MEMBERS SO THAT WE MINIMIZE LINK CROSSOVERS FROM PARENT HOSTS */

		/* draw hosts in child "layers" */
		for(current_layer=1;;current_layer++){
			
			/* how many members in this layer? */
			layer_members=number_of_host_layer_members(this_host,current_layer);

			if(layer_members==0)
				break;

			current_layer_member=0;

			/* see which hosts are members of this layer and calculate drawing coords */
            for(Iterator iter = objects.hostextinfo_list.iterator(); iter.hasNext(); ){
                temp_hostextinfo= (objects_h.hostextinfo) iter.next();

				/* find the host that matches this entry */
				temp_host=objects.find_host(temp_hostextinfo.host_name);

				if(temp_host==null)
					continue;

				/* is this host a member of the current child layer? */
				if(host_child_depth_separation(this_host,temp_host)==current_layer){
					temp_hostextinfo.should_be_drawn=common_h.TRUE;
					temp_hostextinfo.have_2d_coords=common_h.TRUE;
					temp_hostextinfo.x_2d=center_x-(((layer_members*DEFAULT_NODE_WIDTH)+((layer_members-1)*DEFAULT_NODE_HSPACING))/2)+(current_layer_member*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
					if(this_host==null)
						temp_hostextinfo.y_2d=((DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING)*current_layer)+offset_y;
					else
						temp_hostextinfo.y_2d=((DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING)*(current_layer+1))+offset_y;
					current_layer_member++;
				        }
			        }
		        }

	        }


	/***** "BALANCED" TREE MODE *****/
	else if(layout_method==LAYOUT_BALANCED_TREE){

		/* find the "main" host we're displaying  - DO NOT USE THIS (THIS IS THE OLD METHOD) */
		/*
		if(show_all_hosts==common_h.TRUE)
			this_host=null;
		else
			this_host=find_host(host_name);
		*/
		
		/* always use null as the "main" host, screen coords/dimensions are adjusted automatically */
		this_host=null;

		/* find total number of immediate parents for this host */
		parent_hosts=objects.number_of_immediate_parent_hosts(this_host);

		/* find the max drawing width we have... */
		max_drawing_width=max_child_host_drawing_width(this_host);
		if(parent_hosts>max_drawing_width)
			max_drawing_width=parent_hosts;

		/* calculate center x coord */
		center_x=(((DEFAULT_NODE_WIDTH*max_drawing_width)+(DEFAULT_NODE_HSPACING*(max_drawing_width-1)))/2)+offset_x;

		/* coords for Nagios icon if necessary */
		if(this_host==null || this_host.parent_hosts==null || this_host.parent_hosts.size() == 0 ){
			nagios_icon_x=center_x;
			nagios_icon_y=offset_y;
			draw_nagios_icon=common_h.TRUE;
		        }

		/* do we need to draw a link to parent(s)? */
		if(this_host!=null && objects.is_host_immediate_child_of_host(null,this_host)==common_h.FALSE){
			draw_parent_links=common_h.TRUE;
			offset_y+=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;
		        }

		/* see which hosts we should draw and calculate drawing coords */
        for(Iterator iter = objects.hostextinfo_list.iterator(); iter.hasNext(); ){
            temp_hostextinfo= (objects_h.hostextinfo) iter.next();

			/* find the host that matches this entry */
			temp_host=objects.find_host(temp_hostextinfo.host_name);

			if(temp_host==null)
				continue;
			
			/* this is an immediate parent of the "main" host we're drawing */
			else if(objects.is_host_immediate_parent_of_host(this_host,temp_host)==common_h.TRUE){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x-(((parent_hosts*DEFAULT_NODE_WIDTH)+((parent_hosts-1)*DEFAULT_NODE_HSPACING))/2)+(current_parent_host*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
				temp_hostextinfo.y_2d=offset_y;
				current_parent_host++;
			        }
			
			/* this is the "main" host we're drawing */
			else if(this_host==temp_host){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_2d_coords=common_h.TRUE;
				temp_hostextinfo.x_2d=center_x;
				temp_hostextinfo.y_2d=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y;
			        }

			/* else do not draw this host (we might if its a child - see below, but assume no for now) */
			else{
				temp_hostextinfo.should_be_drawn=common_h.FALSE;
				temp_hostextinfo.have_2d_coords=common_h.FALSE;
			        }
		        }

		/* draw all children hosts */
		calculate_balanced_tree_coords(this_host,center_x,DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y);

	        }


	/***** CIRCULAR LAYOUT MODE *****/
	else if(layout_method==LAYOUT_CIRCULAR || layout_method==LAYOUT_CIRCULAR_MARKUP || layout_method==LAYOUT_CIRCULAR_BALLOON){

		/* draw process icon */
		nagios_icon_x=0;
		nagios_icon_y=0;
		draw_nagios_icon=common_h.TRUE;

		/* calculate coordinates for all hosts */
		calculate_circular_coords();
	        }

	return;
        }



/* calculates max possible image dimensions */
public static void calculate_total_image_bounds(){

	total_image_width=0;
	total_image_height=0;

	/* check all extended host information entries... */
	for(objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

		/* only check entries that have 2-D coords specified */
		if(temp_hostextinfo.have_2d_coords==common_h.FALSE)
			continue;

		/* skip hosts we shouldn't be drawing */
		if(temp_hostextinfo.should_be_drawn==common_h.FALSE)
			continue;
		
		if(temp_hostextinfo.x_2d>total_image_width)
			total_image_width=temp_hostextinfo.x_2d;
		if(temp_hostextinfo.y_2d>total_image_height)
			total_image_height=temp_hostextinfo.y_2d;

		coordinates_were_specified=common_h.TRUE;
	        }

	/* add some space for icon size and overlapping text... */
	if(coordinates_were_specified==common_h.TRUE){

		total_image_width+=(DEFAULT_NODE_WIDTH*2);
		total_image_height+=DEFAULT_NODE_HEIGHT;

		/* add space for bottom margin if necessary */
		total_image_height+=bottom_margin;
	        }

	/* image size should be at least as large as dimensions of background image */
	if(total_image_width<background_image_width)
		total_image_width=background_image_width;
	if(total_image_height<background_image_height)
		total_image_height=background_image_height;

	/* we didn't find any hosts that had user-supplied coordinates, so we're going to display a warning */
	if(coordinates_were_specified==common_h.FALSE){
		coordinates_were_specified=common_h.FALSE;
		total_image_width=COORDS_WARNING_WIDTH;
		total_image_height=COORDS_WARNING_HEIGHT;
	        }

	return;
        }


/* calculates canvas coordinates/dimensions */
public static void calculate_canvas_bounds(){

	if(user_supplied_canvas==common_h.FALSE && !host_name.equals("all"))
		calculate_canvas_bounds_from_host(host_name);

	/* calculate canvas origin (based on total image bounds) */
	if(canvas_x<=0 || canvas_width>total_image_width)
		canvas_x=0;
	if(canvas_y<=0 || canvas_height>total_image_height)
		canvas_y=0;

	/* calculate canvas dimensions */
	if(canvas_height<=0)
		canvas_height=(total_image_height-canvas_y);
	if(canvas_width<=0)
		canvas_width=(total_image_width-canvas_x);

	if(canvas_x+canvas_width>total_image_width)
		canvas_width=total_image_width-canvas_x;
	if(canvas_y+canvas_height>total_image_height)
		canvas_height=total_image_height-canvas_y;

	return;
        }


/* calculates canvas coordinates/dimensions around a particular host */
public static void calculate_canvas_bounds_from_host(String host_name){
	objects_h.hostextinfo temp_hostextinfo;
	int zoom_width;
	int zoom_height;

	/* find the extended host info */
	temp_hostextinfo=objects.find_hostextinfo(host_name);
	if(temp_hostextinfo==null)
		return;

	/* make sure we have 2-D coords */
	if(temp_hostextinfo.have_2d_coords==common_h.FALSE)
		return;
	
	if(max_image_width>0 && proximity_width>max_image_width)
		zoom_width=max_image_width;
	else
		zoom_width=proximity_width;
	if(max_image_height>0 && proximity_height>max_image_height)
		zoom_height=max_image_height;
	else
		zoom_height=proximity_height;

	canvas_width=zoom_width;
	if(canvas_width>=total_image_width)
		canvas_x=0;
	else
		canvas_x=(temp_hostextinfo.x_2d-(zoom_width/2));

	canvas_height=zoom_height;
	if(canvas_height>=total_image_height)
		canvas_y=0;
	else
		canvas_y=(temp_hostextinfo.y_2d-(zoom_height/2));


	return;
        }


/* calculates scaling factor used in image generation */
public static void calculate_scaling_factor(){
	double x_scaling=1.0;
	double y_scaling=1.0;

	/* calculate horizontal scaling factor */
	if(max_image_width<=0 || canvas_width<=max_image_width)
		x_scaling=1.0;
	else
		x_scaling=(max_image_width/canvas_width);

	/* calculate vertical scaling factor */
	if(max_image_height<=0 || canvas_height<=max_image_height)
		y_scaling=1.0;
	else
		y_scaling=(max_image_height/canvas_height);

	/* calculate general scaling factor to use */
	if(x_scaling<y_scaling)
		scaling_factor=x_scaling;
	else
		scaling_factor=y_scaling;

	/*** USER-SUPPLIED SCALING FACTOR ***/
	if(user_supplied_scaling==common_h.TRUE)
		scaling_factor=user_scaling_factor;

	return;
        }


/* finds hosts that can be drawn in the canvas area */
public static void find_eligible_hosts(){
	int total_eligible_hosts=0;
	objects_h.host temp_host;

	/* check all extended host information entries... */
	for(objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

		/* find the host */
		temp_host=objects.find_host(temp_hostextinfo.host_name);

		if(temp_host==null)
			temp_hostextinfo.should_be_drawn=common_h.FALSE;

		/* only include hosts that have 2-D coords supplied */
		else if(temp_hostextinfo.have_2d_coords==common_h.FALSE)
			temp_hostextinfo.should_be_drawn=common_h.FALSE;

		/* make sure coords are all positive */
		else if(temp_hostextinfo.x_2d<0 || temp_hostextinfo.y_2d<0)
			temp_hostextinfo.should_be_drawn=common_h.FALSE;

		/* make sure x coordinates fall within canvas bounds */
		else if(temp_hostextinfo.x_2d<(canvas_x-DEFAULT_NODE_WIDTH) || temp_hostextinfo.x_2d>(canvas_x+canvas_width))
			temp_hostextinfo.should_be_drawn=common_h.FALSE;

		/* make sure y coordinates fall within canvas bounds */
		else if(temp_hostextinfo.y_2d<(canvas_y-DEFAULT_NODE_HEIGHT) || temp_hostextinfo.y_2d>(canvas_y+canvas_height))
			temp_hostextinfo.should_be_drawn=common_h.FALSE;

		/* see if the user is authorized to view the host */
		else if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
			temp_hostextinfo.should_be_drawn=common_h.FALSE;

		/* all checks passed, so we can draw the host! */
		else{
			temp_hostextinfo.should_be_drawn=common_h.TRUE;
			total_eligible_hosts++;
		        }
	        }

	return;
        }



/******************************************************************/
/*********************** DRAWING FUNCTIONS ************************/
/******************************************************************/


/* loads background image from file */
public static void load_background_image(){
	String temp_buffer ; // MAX_INPUT_BUFFER

	/* bail out if we shouldn't be drawing a background image */
	if(layout_method!=LAYOUT_USER_SUPPLIED || cgiutils.statusmap_background_image==null)
		return;

	temp_buffer = String.format( "%s%s", cgiutils.physical_images_path, cgiutils.statusmap_background_image);

	/* read the background image into memory */
	background_image=load_image_from_file(temp_buffer);

	/* grab background image dimensions for calculating total image width later */
	if(background_image!=null){
		background_image_width= background_image.getWidth();
		background_image_height = background_image.getHeight();
	        }

	/* if we are just creating the html, we don't need the image anymore */
//	if(create_type==CREATE_HTML && background_image!=null)
//		gdImageDestroy(background_image);

	return;
	}


/* draws background image on drawing canvas */
public static void draw_background_image(){

	/* bail out if we shouldn't be drawing a background image */
	if(create_type==CREATE_HTML || layout_method!=LAYOUT_USER_SUPPLIED || cgiutils.statusmap_background_image==null)
		return;

	/* bail out if we don't have an image */
	if(background_image==null)
		return;

	/* copy the background image to the canvas */  
    gd_map_image.drawImage( background_image, null, 0, 0 ); 
//	gdImageCopy(map_image,background_image,0,0,canvas_x,canvas_y,canvas_width,canvas_height);

	/* free memory for background image, as we don't need it anymore */
//	gdImageDestroy(background_image);

	return;
        }



/* draws background "extras" */
public static void draw_background_extras(){

	/* bail out if we shouldn't be here */
	if(create_type==CREATE_HTML)
		return;

	/* circular layout stuff... */
	if(layout_method==LAYOUT_CIRCULAR_MARKUP){

		/* draw colored sections... */
		draw_circular_markup();
	        }

	return;
        }


/* draws host links */
public static void draw_host_links(){
	objects_h.hostextinfo temp_hostextinfo;
    objects_h.hostextinfo temp_parent_hostextinfo;
    objects_h.host this_host;
    objects_h.host main_host;
    objects_h.host parent_host;
    objects_h.hostsmember temp_hostsmember;
	Color status_color=color_black;
    statusdata_h.hoststatus this_hoststatus;
    statusdata_h.hoststatus parent_hoststatus;
	int child_in_layer_list=common_h.FALSE;
	int parent_in_layer_list=common_h.FALSE;
	int dotted_line=common_h.FALSE;
	int x=0;
	int y=0;

	if(create_type==CREATE_HTML)
		return;

	if(use_links==common_h.FALSE)
		return;

	/* find the "main" host we're drawing */
	main_host=objects.find_host(host_name);
	if(show_all_hosts==common_h.TRUE)
		main_host=null;

	/* check all extended host information entries... */
	for(Iterator iter = objects.hostextinfo_list.iterator(); iter.hasNext(); ){
        temp_hostextinfo= (objects_h.hostextinfo) iter.next();

		/* find the config entry for this host */
		this_host=objects.find_host(temp_hostextinfo.host_name);
		if(this_host==null)
			continue;

		/* only draw link if user is authorized to view this host */
		if(cgiauth.is_authorized_for_host(this_host,current_authdata)==common_h.FALSE)
			continue;

		/* this is a "root" host, so draw link to Nagios process icon if using auto-layout mode */
		if( (this_host.parent_hosts==null || this_host.parent_hosts.size() == 0) && layout_method!=LAYOUT_USER_SUPPLIED && draw_nagios_icon==common_h.TRUE){

			x=temp_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2)-canvas_x;
			y=temp_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH/2)-canvas_y;

			draw_line(x,y,nagios_icon_x+(DEFAULT_NODE_WIDTH/2)-canvas_x,nagios_icon_y+(DEFAULT_NODE_WIDTH/2)-canvas_y,color_black);
		        }

		/* this is a child of the main host we're drawing in auto-layout mode... */
		if(layout_method!=LAYOUT_USER_SUPPLIED && draw_child_links==common_h.TRUE && objects.number_of_immediate_child_hosts(this_host)>0 && objects.is_host_immediate_child_of_host(main_host,this_host)==common_h.TRUE){
			/* determine color to use when drawing links to children  */
			this_hoststatus=statusdata.find_hoststatus(temp_hostextinfo.host_name);
			if(this_hoststatus!=null){
				if(this_hoststatus.status==statusdata_h.HOST_DOWN || this_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
					status_color=color_red;
				else
					status_color=color_black;
		                }
			else
				status_color=color_black;

			x=temp_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2)-canvas_x;
			y=(temp_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH)/2)-canvas_y;

			draw_dashed_line(x,y,x,y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING,status_color);

			/* draw arrow tips */
			draw_line(x,y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING,x-5,y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING-5,color_black);
			draw_line(x,y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING,x+5,y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING-5,color_black);
		        }

		/* this is a parent of the main host we're drawing in auto-layout mode... */
		if(layout_method!=LAYOUT_USER_SUPPLIED && draw_parent_links==common_h.TRUE && objects.is_host_immediate_child_of_host(this_host,main_host)==common_h.TRUE){

			x=temp_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2)-canvas_x;
			y=temp_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH/2)-canvas_y;

			draw_dashed_line(x,y,x,y-DEFAULT_NODE_HEIGHT-DEFAULT_NODE_VSPACING,color_black);

			/* draw arrow tips */
			draw_line(x,y-DEFAULT_NODE_HEIGHT-DEFAULT_NODE_VSPACING,x-5,y-DEFAULT_NODE_HEIGHT-DEFAULT_NODE_VSPACING+5,color_black);
			draw_line(x,y-DEFAULT_NODE_HEIGHT-DEFAULT_NODE_VSPACING,x+5,y-DEFAULT_NODE_HEIGHT-DEFAULT_NODE_VSPACING+5,color_black);
		        }

		/* draw links to all parent hosts */
		for(Iterator iter2 = this_host.parent_hosts.iterator(); iter2.hasNext(); ){
            temp_hostsmember= (objects_h.hostsmember) iter2.next();

			/* find extended info entry for this parent host */
			temp_parent_hostextinfo=objects.find_hostextinfo(temp_hostsmember.host_name);
			if(temp_parent_hostextinfo==null)
				continue;

			/* don't draw the link if we don't have the coords */
			if(temp_parent_hostextinfo.have_2d_coords==common_h.FALSE || temp_hostextinfo.have_2d_coords==common_h.FALSE)
				continue;

			/* find the parent host config entry */
			parent_host=objects.find_host(temp_parent_hostextinfo.host_name);
			if(parent_host==null)
				continue;

			/* only draw link if user is authorized for this parent host */
			if( cgiauth.is_authorized_for_host(parent_host,current_authdata)==common_h.FALSE)
				continue;

			/* are the hosts in the layer list? */
			child_in_layer_list=is_host_in_layer_list(this_host);
			parent_in_layer_list=is_host_in_layer_list(parent_host);

			/* use dotted or solid line? */
			/* either the child or parent should not be drawn, so use a dotted line */
			if((child_in_layer_list==common_h.TRUE && parent_in_layer_list==common_h.FALSE) || (child_in_layer_list==common_h.FALSE && parent_in_layer_list==common_h.TRUE))
				dotted_line=common_h.TRUE;
			/* both hosts should not be drawn, so use a dotted line */
			else if((child_in_layer_list==common_h.FALSE && parent_in_layer_list==common_h.FALSE && exclude_layers==common_h.FALSE) || (child_in_layer_list==common_h.TRUE && parent_in_layer_list==common_h.TRUE && exclude_layers==common_h.TRUE))
				dotted_line=common_h.TRUE;
			/* both hosts should be drawn, so use a solid line */
			else
				dotted_line=common_h.FALSE;

			/* determine color to use when drawing links to parent host */
			parent_hoststatus=statusdata.find_hoststatus(temp_parent_hostextinfo.host_name);
			if(parent_hoststatus!=null){
				if(parent_hoststatus.status==statusdata_h.HOST_DOWN || parent_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
					status_color=color_red;
				else
					status_color=color_black;
		                }
			else
				status_color=color_black;

			/* draw the link */
			if(dotted_line==common_h.TRUE)
				draw_dotted_line((temp_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2))-canvas_x,(temp_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH)/2)-canvas_y,(temp_parent_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2))-canvas_x,(temp_parent_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH/2))-canvas_y,status_color);
			else
				draw_line((temp_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2))-canvas_x,(temp_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH)/2)-canvas_y,(temp_parent_hostextinfo.x_2d+(DEFAULT_NODE_WIDTH/2))-canvas_x,(temp_parent_hostextinfo.y_2d+(DEFAULT_NODE_WIDTH/2))-canvas_y,status_color);
		        }

	        }

	return;
        }



/* draws hosts */
public static void draw_hosts(){
	objects_h.hostextinfo temp_hostextinfo;
	objects_h.host temp_host;
	int x1, x2;
	int y1, y2;
	int has_image=common_h.FALSE;
	String image_input_file ; // MAX_INPUT_BUFFER
	int current_radius=0;
	Color status_color=color_black;
	statusdata_h.hoststatus temp_hoststatus;
	int in_layer_list=common_h.FALSE;
	int average_host_services;
	int host_services;
	double host_services_ratio;
	int outer_radius;
	int inner_radius;
	Color time_color;
	long current_time;
	int translated_x;
	int translated_y;

	
	/* user didn't supply any coordinates for hosts, so display a warning */
	if(coordinates_were_specified==common_h.FALSE){

		if(create_type==CREATE_IMAGE){
			draw_text("You have not supplied any host drawing coordinates, so you cannot use this layout method.",(COORDS_WARNING_WIDTH/2),30,color_black);
			draw_text("Read the FAQs for more information on specifying drawing coordinates or select a different layout method.",(COORDS_WARNING_WIDTH/2),45,color_black);
		        }

		return;
	        }

	/* draw Nagios process icon if using auto-layout mode */
	if(layout_method!=LAYOUT_USER_SUPPLIED && draw_nagios_icon==common_h.TRUE){

		/* get coords of bounding box */
		x1=nagios_icon_x-canvas_x;
		x2=x1+DEFAULT_NODE_WIDTH;
		y1=nagios_icon_y-canvas_y;
		y2=y1+DEFAULT_NODE_HEIGHT;

		/* get the name of the image file to open for the logo */
//		image_input_file = String.format( "%s%s",physical_logo_images_path,NAGIOS_GD2_ICON);
        image_input_file = String.format( "%s%s",physical_logo_images_path,NAGIOS_GD2_IMAGE);

		/* read in the image from file... */
		logo_image=load_image_from_file(image_input_file);

	        /* copy the logo image to the canvas image... */
		if(logo_image!=null){
            gd_map_image.drawImage( logo_image, null, x1, y1 );
//			gdImageCopy(map_image,logo_image,x1,y1,0,0,logo_image.sx,logo_image.sy);
//			gdImageDestroy(logo_image);
                        }

		/* if we don't have an image, draw a bounding box */
		else{
			draw_line(x1,y1,x1,y1+DEFAULT_NODE_WIDTH,color_black);
			draw_line(x1,y1+DEFAULT_NODE_WIDTH,x2,y1+DEFAULT_NODE_WIDTH,color_black);
			draw_line(x2,y1+DEFAULT_NODE_WIDTH,x2,y1,color_black);
			draw_line(x2,y1,x1,y1,color_black);
	                }

		if(create_type==CREATE_IMAGE)
			draw_text("Nagios Process",x1+(DEFAULT_NODE_WIDTH/2),y1+DEFAULT_NODE_HEIGHT,color_black);
	        }

	/* calculate average services per host */
	average_host_services=4;

	/* draw all hosts... */
	for(Iterator iter = objects.hostextinfo_list.listIterator(); iter.hasNext();  ){
        temp_hostextinfo= (objects_h.hostextinfo) iter.next();
		
		/* skip hosts that should not be drawn */
		if(temp_hostextinfo.should_be_drawn==common_h.FALSE)
			continue;

		/* find the host */
		temp_host=objects.find_host(temp_hostextinfo.host_name);

		/* is this host in the layer inclusion/exclusion list? */
		in_layer_list=is_host_in_layer_list(temp_host);
		if((in_layer_list==common_h.TRUE && exclude_layers==common_h.TRUE) || (in_layer_list==common_h.FALSE && exclude_layers==common_h.FALSE))
			continue;

		/* get coords of host bounding box */
		x1=temp_hostextinfo.x_2d-canvas_x;
		x2=x1+DEFAULT_NODE_WIDTH;
		y1=temp_hostextinfo.y_2d-canvas_y;
		y2=y1+DEFAULT_NODE_HEIGHT;

		if(create_type==CREATE_IMAGE){


			temp_hoststatus=statusdata.find_hoststatus(temp_hostextinfo.host_name);
			if(temp_hoststatus!=null){
				if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
					status_color=color_red;
				else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
					status_color=color_red;
				else if(temp_hoststatus.status==statusdata_h.HOST_UP)
					status_color=color_green;
				else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
					status_color=color_grey;
			        }
			else
				status_color=color_black;


			/* use balloons instead of icons... */
			if(layout_method==LAYOUT_CIRCULAR_BALLOON){

				/* get the number of services associated with the host */
				host_services=number_of_host_services(temp_host);

				if(average_host_services==0)
					host_services_ratio=0.0;
				else
					host_services_ratio=(host_services/average_host_services);

				/* calculate size of node */
				if(host_services_ratio>=2.0)
					outer_radius=DEFAULT_NODE_WIDTH;
				else if(host_services_ratio>=1.5)
					outer_radius=(int) (DEFAULT_NODE_WIDTH*0.8);
				else if(host_services_ratio>=1.0)
					outer_radius=(int) (DEFAULT_NODE_WIDTH*0.6);
				else if(host_services_ratio>=0.5)
					outer_radius=(int) (DEFAULT_NODE_WIDTH*0.4);
				else
					outer_radius=(int) (DEFAULT_NODE_WIDTH*0.2);

				/* calculate width of border */
				if(temp_hoststatus==null)
					inner_radius=outer_radius;
				else if((temp_hoststatus.status==statusdata_h.HOST_DOWN || temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE) && temp_hoststatus.problem_has_been_acknowledged==common_h.FALSE)
					inner_radius=outer_radius-3;
				else
					inner_radius=outer_radius;

                Color temp_color = gd_map_image.getColor();
                
				/* fill node with color based on how long its been in this state... */
                gd_map_image.setColor( color_blue );
              gd_map_image.drawArc( x1+(DEFAULT_NODE_WIDTH/2) - outer_radius/2,y1+(DEFAULT_NODE_WIDTH/2)-outer_radius/2,outer_radius,outer_radius,0,360);

				/* determine fill color */
				current_time = utils.currentTimeInSeconds();
				if(temp_hoststatus==null)
					time_color=color_white;
				else if(current_time-temp_hoststatus.last_state_change<=900)
					time_color=color_orange;
				else if(current_time-temp_hoststatus.last_state_change<=3600)
					time_color=color_yellow;
				else
					time_color=color_white;

				/* fill node with appropriate time color */
				/* the fill function only works with coordinates that are in bounds of the actual image */
				translated_x=x1+(DEFAULT_NODE_WIDTH/2);
				translated_y=y1+(DEFAULT_NODE_WIDTH/2);
                gd_map_image.setColor( time_color );
//				if(translated_x>0 && translated_y>0 && translated_x<canvas_width && translated_y<canvas_height)
//					gdImageFillToBorder(map_image,translated_x,translated_y,color_blue,time_color);

				/* border of node should reflect current state */
                gd_map_image.setColor( status_color );
				for(current_radius=outer_radius;current_radius>=inner_radius;current_radius--)
                  gd_map_image.drawArc( x1+(DEFAULT_NODE_WIDTH/2)-current_radius/2,y1+(DEFAULT_NODE_WIDTH/2)- current_radius/2,current_radius,current_radius,0,360);

				/* draw circles around the selected host (if there is one) */
				if( host_name.equals( temp_hostextinfo.host_name) && use_highlights==common_h.TRUE){
					for(current_radius=DEFAULT_NODE_WIDTH*2;current_radius>0;current_radius-=10)
                      gd_map_image.drawArc( x1+(DEFAULT_NODE_WIDTH/2)-current_radius/2,y1+(DEFAULT_NODE_WIDTH/2) - current_radius/2,current_radius,current_radius,0,360 );
			                }
                
                gd_map_image.setColor( temp_color );
			        }


			/* normal method is to use icons for hosts... */
			else{

                Color temp_color = gd_map_image.getColor();
                gd_map_image.setColor( status_color );

                /* draw a target around root hosts (hosts with no parents) */
				if(temp_host!=null && use_highlights==common_h.TRUE){
					if(temp_host.parent_hosts==null || temp_host.parent_hosts.size() == 0){
                      gd_map_image.drawArc( x1+(DEFAULT_NODE_WIDTH/2) - DEFAULT_NODE_WIDTH,y1+(DEFAULT_NODE_WIDTH/2)- DEFAULT_NODE_WIDTH,(DEFAULT_NODE_WIDTH*2),(DEFAULT_NODE_WIDTH*2),0,360 );
						draw_line(x1-(DEFAULT_NODE_WIDTH/2),y1+(DEFAULT_NODE_WIDTH/2),x1+(DEFAULT_NODE_WIDTH*3/2),y1+(DEFAULT_NODE_WIDTH/2),status_color);
						draw_line(x1+(DEFAULT_NODE_WIDTH/2),y1-(DEFAULT_NODE_WIDTH/2),x1+(DEFAULT_NODE_WIDTH/2),y1+(DEFAULT_NODE_WIDTH*3/2),status_color);
				                }
			                }

				/* draw circles around the selected host (if there is one) */
				if( host_name.equals( temp_hostextinfo.host_name) && use_highlights==common_h.TRUE){
					for(current_radius=DEFAULT_NODE_WIDTH*2;current_radius>0;current_radius-=10)
						gd_map_image.drawArc( x1+(DEFAULT_NODE_WIDTH/2) - current_radius/2,y1+(DEFAULT_NODE_WIDTH/2) - current_radius/2,current_radius,current_radius,0,360 );
			                }


				if(temp_hostextinfo.statusmap_image!=null)
					has_image=common_h.TRUE;
				else
					has_image=common_h.FALSE;
				
				/* load the logo associated with this host */
				if(has_image==common_h.TRUE){

				        /* get the name of the image file to open for the logo */
					image_input_file = String.format( "%s%s",physical_logo_images_path,temp_hostextinfo.statusmap_image);

				        /* read in the logo image from file... */
					logo_image=load_image_from_file(image_input_file);

			                /* copy the logo image to the canvas image... */
					if(logo_image!=null){
                        gd_map_image.drawImage( logo_image, null, x1, y1 );
//                      gdImageCopy(map_image,logo_image,x1,y1,0,0,logo_image.sx,logo_image.sy);
//						gdImageDestroy(logo_image);
		                                }
					else
						has_image=common_h.FALSE;
			                }

				/* if the host doesn't have an image associated with it (or the user doesn't have rights to see this host), use the unknown image */
				if(has_image==common_h.FALSE){

					if(unknown_logo_image!=null) {
                        gd_map_image.drawImage( unknown_logo_image, null, x1, y1 );
//						gdImageCopy(map_image,unknown_logo_image,x1,y1,0,0,unknown_logo_image.sx,unknown_logo_image.sy);

                    } else{

						/* last ditch effort - draw a host bounding box */
						draw_line(x1,y1,x1,y1+DEFAULT_NODE_WIDTH,color_black);
						draw_line(x1,y1+DEFAULT_NODE_WIDTH,x2,y1+DEFAULT_NODE_WIDTH,color_black);
						draw_line(x2,y1+DEFAULT_NODE_WIDTH,x2,y1,color_black);
						draw_line(x2,y1,x1,y1,color_black);
				                }
		                        }
                
                gd_map_image.setColor( temp_color );
			        }


			/* draw host name, status, etc. */
			draw_host_text(temp_hostextinfo.host_name,x1+(DEFAULT_NODE_WIDTH/2),y1+DEFAULT_NODE_HEIGHT);
		        }

		/* we're creating HTML image map... */
		else{
			System.out.printf("<AREA shape='rect' ");

			/* coordinates */
			System.out.printf("coords='%d,%d,%d,%d' ",(int)(x1*scaling_factor),(int)(y1*scaling_factor),(int)((x1+DEFAULT_NODE_WIDTH)*scaling_factor),(int)((y1+DEFAULT_NODE_HEIGHT)*scaling_factor));

			/* URL */
			if( host_name.equals(temp_hostextinfo.host_name))
				System.out.printf("href='%s?host=%s' ",cgiutils_h.STATUS_CGI,cgiutils.url_encode(temp_hostextinfo.host_name));
			else{
				System.out.printf("href='%s?host=%s&layout=%d&max_width=%d&max_height=%d&proximity_width=%d&proximity_height=%d%s%s%s%s%s",cgiutils_h.STATUSMAP_CGI,cgiutils.url_encode(temp_hostextinfo.host_name),layout_method,max_image_width,max_image_height,proximity_width,proximity_height,(display_header==common_h.TRUE)?"":"&noheader",(use_links==common_h.FALSE)?"&nolinks":"",(use_text==common_h.FALSE)?"&notext":"",(use_highlights==common_h.FALSE)?"&nohighlights":"",(display_popups==common_h.FALSE)?"&nopopups":"");
				if(user_supplied_scaling==common_h.TRUE)
					System.out.printf("&scaling_factor=%2.1f",user_scaling_factor);
				print_layer_url(common_h.TRUE);
				System.out.printf("' ");
			        }

			/* popup text */
			if(display_popups==common_h.TRUE){

				System.out.printf("onMouseOver='showPopup(\"");
				write_host_popup_text(objects.find_host(temp_hostextinfo.host_name));
				System.out.printf("\",event)' onMouseOut='hidePopup()'");
			        }

			System.out.printf(">\n");
		        }

	        }

	return;
        }


/* draws text */
public static void draw_text(String buffer,int x,int y,Color text_color){
	int string_width=0;
	int string_height=0;

	/* write the string to the generated image... */
    Color temp_color = gd_map_image.getColor();

    string_height=gd_map_image.getFontMetrics().getHeight();
	string_width=gd_map_image.getFontMetrics().stringWidth(buffer);
	if(layout_method!=LAYOUT_CIRCULAR_MARKUP) {
        gd_map_image.setColor( color_white );
		gd_map_image.fillRect( x-(string_width/2)-2,y-(2*string_height),x+(string_width/2)+2,y-string_height);
    }

    gd_map_image.setColor( text_color );
    gd_map_image.drawString( buffer, x-(string_width/2),y-(1*string_height) );

    gd_map_image.setColor( temp_color );
        }


/* draws host text */
public static void draw_host_text(String name,int x,int y){
	statusdata_h.hoststatus temp_hoststatus;
	Color status_color=color_black;
	String temp_buffer ; // MAX_INPUT_BUFFER

	if(use_text==common_h.FALSE)
		return;

	temp_buffer = name;

	/* write the host status string to the generated image... */
	draw_text(temp_buffer,x,y,color_black);

	/* find the status entry for this host */
	temp_hoststatus=statusdata.find_hoststatus(name);

	/* get the status of the host (pending, up, down, or unreachable) */
	if(temp_hoststatus!=null){

		/* draw the status string */
		if(temp_hoststatus.status==statusdata_h.HOST_DOWN){
			temp_buffer = "Down";
			status_color=color_red;
                        }
		else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE){
			temp_buffer = "Unreachable";
			status_color=color_red;
                        }
		else if(temp_hoststatus.status==statusdata_h.HOST_UP){
			temp_buffer = "Up";
			status_color=color_green;
                        }
		else if(temp_hoststatus.status==statusdata_h.HOST_PENDING){
			temp_buffer = "Pending";
			status_color=color_grey;
                        }
		else{
			temp_buffer = "Unknown";
			status_color=color_orange;
	                }

		/* write the host status string to the generated image... */
		draw_text(temp_buffer,x,y+gd_map_image.getFontMetrics().getHeight(),status_color);
                }

	return;
        }


/* writes popup text for a specific host */
public static void write_host_popup_text(objects_h.host hst){
	objects_h.hostextinfo temp_hostextinfo;
	statusdata_h.hoststatus temp_status;
	objects_h.hostsmember temp_hostsmember;
	int service_totals;
	String date_time ; // 48
	long current_time;
	long t;
	String state_duration = "" ; // 48

	if(hst==null){
		System.out.printf("Host data not found");
		return;
	        }

	/* find the status entry for this host */
	temp_status=statusdata.find_hoststatus(hst.name);
	if(temp_status==null){
		System.out.printf("Host status information not found");
		return;
	        }

	/* strip nasty stuff from plugin output */
	cgiutils.sanitize_plugin_output(temp_status.plugin_output);

	System.out.printf("<table border=0 cellpadding=0 cellspacing=5>");

	temp_hostextinfo=objects.find_hostextinfo(hst.name);
	if(temp_hostextinfo!=null){
		System.out.printf("<tr><td><img src=%s%s border=0 width=40 height=40></td>",cgiutils.url_logo_images_path,(temp_hostextinfo.icon_image==null)?UNKNOWN_ICON_IMAGE:temp_hostextinfo.icon_image);
		System.out.printf("<td class=\\\"popupText\\\"><i>%s</i></td></tr>",(temp_hostextinfo.icon_image_alt==null)?"":cgiutils.html_encode(temp_hostextinfo.icon_image_alt));
	        }

	System.out.printf("<tr><td class=\\\"popupText\\\">Name:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>",cgiutils.html_encode(hst.name));
	System.out.printf("<tr><td class=\\\"popupText\\\">Alias:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>",cgiutils.html_encode(hst.alias));
	System.out.printf("<tr><td class=\\\"popupText\\\">Address:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>",cgiutils.html_encode(hst.address));
	System.out.printf("<tr><td class=\\\"popupText\\\">State:</td><td class=\\\"popupText\\\"><b>");

	/* get the status of the host (pending, up, down, or unreachable) */
	if(temp_status.status==statusdata_h.HOST_DOWN){
		System.out.printf("<font color=red>Down");
		if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
			System.out.printf(" (Acknowledged)");
		System.out.printf("</font>");
	        }

	else if(temp_status.status==statusdata_h.HOST_UNREACHABLE){
		System.out.printf("<font color=red>Unreachable");
		if(temp_status.problem_has_been_acknowledged==common_h.TRUE)
			System.out.printf(" (Acknowledged)");
		System.out.printf("</font>");
	        }

	else if(temp_status.status==statusdata_h.HOST_UP)
		System.out.printf("<font color=green>Up</font>");

	else if(temp_status.status==statusdata_h.HOST_PENDING)
		System.out.printf("Pending");

	System.out.printf("</b></td></tr>");
	System.out.printf("<tr><td class=\\\"popupText\\\">Status Information:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>",(temp_status.plugin_output==null)?"":temp_status.plugin_output);

	current_time=utils.currentTimeInSeconds();
	if(temp_status.last_state_change==0)
		t=current_time-blue.program_start;
	else
		t=current_time-temp_status.last_state_change;
	cgiutils.time_breakdown tb =  cgiutils.get_time_breakdown( t );
	state_duration = String.format( "%2dd %2dh %2dm %2ds%s", tb.days,tb.hours,tb.minutes,tb.seconds,(temp_status.last_state_change==0)?"+":"");
	System.out.printf("<tr><td class=\\\"popupText\\\">State Duration:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>", state_duration);

    date_time = cgiutils.get_time_string(temp_status.last_check, common_h.SHORT_DATE_TIME);
	System.out.printf("<tr><td class=\\\"popupText\\\">Last Status Check:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>",(temp_status.last_check==0)?"N/A":date_time);
    date_time = cgiutils.get_time_string(temp_status.last_state_change, common_h.SHORT_DATE_TIME);
	System.out.printf("<tr><td class=\\\"popupText\\\">Last State Change:</td><td class=\\\"popupText\\\"><b>%s</b></td></tr>",(temp_status.last_state_change==0)?"N/A":date_time);

	System.out.printf("<tr><td class=\\\"popupText\\\">Parent Host(s):</td><td class=\\\"popupText\\\"><b>");
	if(hst.parent_hosts==null || hst.parent_hosts.size() == 0)
		System.out.printf("None (This is a root host)");
	else{
		for( objects_h.hostsmember iter_hostsmember : (ArrayList<objects_h.hostsmember>) hst.parent_hosts )
			System.out.printf("%s%s",(iter_hostsmember==hst.parent_hosts.get(0))?"":", ",cgiutils.html_encode(iter_hostsmember.host_name));
	        }
	System.out.printf("</b></td></tr>");

	System.out.printf("<tr><td class=\\\"popupText\\\">Immediate Child Hosts:</td><td class=\\\"popupText\\\"><b>");
	System.out.printf("%d",objects.number_of_immediate_child_hosts(hst));
	System.out.printf("</b></td></tr>");

	System.out.printf("</table>");

	System.out.printf("<br><b><u>Services:</u></b><br>");

	service_totals=statusdata.get_servicestatus_count(hst.name,statusdata_h.SERVICE_OK);
	if(service_totals>0)
		System.out.printf("- <font color=green>%d ok</font><br>",service_totals);
	service_totals=statusdata.get_servicestatus_count(hst.name,statusdata_h.SERVICE_CRITICAL);
	if(service_totals>0)
		System.out.printf("- <font color=red>%d critical</font><br>",service_totals);
	service_totals=statusdata.get_servicestatus_count(hst.name,statusdata_h.SERVICE_WARNING);
	if(service_totals>0)
		System.out.printf("- <font color=orange>%d warning</font><br>",service_totals);
	service_totals=statusdata.get_servicestatus_count(hst.name,statusdata_h.SERVICE_UNKNOWN);
	if(service_totals>0)
		System.out.printf("- <font color=orange>%d unknown</font><br>",service_totals);
	service_totals=statusdata.get_servicestatus_count(hst.name,statusdata_h.SERVICE_PENDING);
	if(service_totals>0)
		System.out.printf("- %d pending<br>",service_totals);

	return;
        }



/* draws a solid line */
public static void draw_line(int x1,int y1,int x2,int y2,Color color){

	if(create_type==CREATE_HTML)
		return;

    Color temp_color = gd_map_image.getColor();
    gd_map_image.setColor(color);

    gd_map_image.drawLine(x1, y1, x2, y2);
    
    gd_map_image.setColor(temp_color);

	}


/* draws a dotted line */
public static void draw_dotted_line(int x1,int y1,int x2,int y2,Color color){

    float[] styleDashed = new float[] {1.0f,5.0f,1.0f,5.0f};

    /* sets current style to a dashed line */
    BasicStroke stroke = (BasicStroke) gd_map_image.getStroke();
    BasicStroke dashed = new BasicStroke( stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), styleDashed, 0.0f );
    gd_map_image.setStroke( dashed );

    /* draws a line (dashed) */
    Color temp_color = gd_map_image.getColor();
    gd_map_image.setColor(color);
    
    gd_map_image.drawLine(x1, y1, x2, y2);
    
    gd_map_image.setColor(temp_color);
    gd_map_image.setStroke( stroke );

}

/* draws a dashed line */
public static void draw_dashed_line(int x1,int y1,int x2,int y2,Color color){
    
    float[] styleDashed = new float[] {4.0f,2.0f};

    /* sets current style to a dashed line */
    BasicStroke stroke = (BasicStroke) gd_map_image.getStroke();
    BasicStroke dashed = new BasicStroke( stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), styleDashed, 0.0f );
    gd_map_image.setStroke( dashed );

    /* draws a line (dashed) */
    Color temp_color = gd_map_image.getColor();
    gd_map_image.setColor(color);
    
    gd_map_image.drawLine(x1, y1, x2, y2);
    
    gd_map_image.setColor(temp_color);
    gd_map_image.setStroke( stroke );

    }


/******************************************************************/
/*********************** GRAPHICS FUNCTIONS ***********************/
/******************************************************************/

/* initialize graphics */
public static int initialize_graphics( ){
	String image_input_file ; // MAX_INPUT_BUFFER

	if(create_type==CREATE_HTML)
		return common_h.ERROR;

	/* allocate buffer for storing image */
    
	map_image=new BufferedImage( canvas_width,canvas_height, BufferedImage.TYPE_INT_ARGB );
    
	if(map_image==null)
		return common_h.ERROR;
    gd_map_image = map_image.createGraphics();
    gd_map_image.setBackground( color_white );
//    gd_map_image .setComposite(AlphaComposite.getInstance( AlphaComposite.XOR, 1.0f));
    Rectangle2D.Double rect = new Rectangle2D.Double(0,0,canvas_width,canvas_height); 
    gd_map_image.fill(rect);
    
    gd_map_image.setColor( Color.BLACK );

    gd_map_image.setFont( Font.decode("Monospaced" ).deriveFont(10.0f) );
    
	/* allocate colors used for drawing */
	color_white=Color.WHITE;
	color_black=Color.BLACK;
	color_grey=new Color(128,128,128);
	color_lightgrey=new Color(210,210,210);
	color_red=new Color(255,0,0);
	color_lightred=new Color(215,175,175);
	color_green=new Color(0,175,0);
	color_lightgreen=new Color(210,255,215);
	color_blue=new Color(0,0,255);
	color_yellow=new Color(255,255,0);
	color_orange=new Color(255,100,25);

	/* set transparency index */
//	gdImageColorTransparent(map_image,color_white);

	/* make sure the graphic is interlaced */
//	gdImageInterlace(map_image,1);

	/* get the path where we will be reading logo images from (GD2 format)... */
	physical_logo_images_path = String.format( "%slogos/", cgiutils.physical_images_path);

	/* load the unknown icon to use for hosts that don't have pretty images associated with them... */
//	image_input_file = String.format( "%s%s",physical_logo_images_path,UNKNOWN_GD2_ICON);
    image_input_file = String.format( "%s%s",physical_logo_images_path,UNKNOWN_ICON_IMAGE);
	unknown_logo_image=load_image_from_file(image_input_file);

	return common_h.OK;
        }



/* loads a graphic image (GD2, JPG or PNG) from file into memory */
public static BufferedImage load_image_from_file(String filename){
    
    if ( filename == null )
        return null;
    
    try {
        return ImageIO.read( new File( filename ));
    } catch (IOException ioE) {
        // TODO log this
        return null;
    }
    
        }

/* draw graphics */
public static void write_graphics(){
//	FILE *image_output_file=null;

	if(create_type==CREATE_HTML)
		return;

	/* use STDOUT for writing the image data... */
//	image_output_file=stdout;

	/* write the image out in PNG format */
    try {
        ImageIO.write( map_image, "png", System.out );
//        ImageIO.write( map_image, "png", new File( "c://map.png"));
    } catch (IOException ioE) {
        
    }

	/* or we could write the image out in JPG format... */
	/*gdImageJpeg(map_image,image_output_file,99);*/

        }

/* cleanup graphics resources */
public static void cleanup_graphics(){

	if(create_type==CREATE_HTML)
		return;

	/* free memory allocated to image */
    // TODO Clean up image memory.
//	gdImageDestroy(map_image);
        }




/******************************************************************/
/************************* MISC FUNCTIONS *************************/
/******************************************************************/

/* write JavaScript code an layer for popup window */
public static void write_popup_code(){
	String border_color="#000000";
	String background_color="#ffffcc";
	int border=1;
	int padding=3;
	int x_offset=3;
	int y_offset=3;

	System.out.printf("<SCRIPT LANGUAGE='JavaScript'>\n");
	System.out.printf("<!--\n");
	System.out.printf("// JavaScript popup based on code originally found at http://www.helpmaster.com/htmlhelp/javascript/popjbpopup.htm\n");
	System.out.printf("function showPopup(text, eventObj){\n");
	System.out.printf("if(!document.all && document.getElementById)\n");
	System.out.printf("{ document.all=document.getElementsByTagName(\"*\")}\n");
	System.out.printf("ieLayer = 'document.all[\\'popup\\']';\n");
	System.out.printf("nnLayer = 'document.layers[\\'popup\\']';\n");
	System.out.printf("moLayer = 'document.getElementById(\\'popup\\')';\n");

	System.out.printf("if(!(document.all||document.layers||document.documentElement)) return;\n");

	System.out.printf("if(document.all) { document.popup=eval(ieLayer); }\n");
	System.out.printf("else {\n");
	System.out.printf("  if (document.documentElement) document.popup=eval(moLayer);\n");
	System.out.printf("  else document.popup=eval(nnLayer);\n");
	System.out.printf("}\n");

	System.out.printf("var table = \"\";\n");

	System.out.printf("if (document.all||document.documentElement){\n");
	System.out.printf("table += \"<table bgcolor='%s' border=%d cellpadding=%d cellspacing=0>\";\n",background_color,border,padding);
	System.out.printf("table += \"<tr><td>\";\n");
	System.out.printf("table += \"<table cellspacing=0 cellpadding=%d>\";\n",padding);
	System.out.printf("table += \"<tr><td bgcolor='%s' class='popupText'>\" + text + \"</td></tr>\";\n",background_color);
	System.out.printf("table += \"</table></td></tr></table>\"\n");
	System.out.printf("document.popup.innerHTML = table;\n");
	System.out.printf("document.popup.style.left = document.body.scrollLeft + %d;\n",x_offset);
	System.out.printf("document.popup.style.top = document.body.scrollTop + %d;\n",y_offset);
	/*
	System.out.printf("document.popup.style.left = (document.all ? eventObj.x : eventObj.layerX) + %d;\n",x_offset);
	System.out.printf("document.popup.style.top  = (document.all ? eventObj.y : eventObj.layerY) + %d;\n",y_offset);
	*/

	System.out.printf("document.popup.style.visibility = \"visible\";\n");
	System.out.printf("} \n");
 

	System.out.printf("else{\n");
	System.out.printf("table += \"<table cellpadding=%d border=%d cellspacing=0 bordercolor='%s'>\";\n",padding,border,border_color);
	System.out.printf("table += \"<tr><td bgcolor='%s' class='popupText'>\" + text + \"</td></tr></table>\";\n",background_color);
	System.out.printf("document.popup.document.open();\n");
	System.out.printf("document.popup.document.write(table);\n");
	System.out.printf("document.popup.document.close();\n");

	/* set x coordinate */
	System.out.printf("document.popup.left = eventObj.layerX + %d;\n",x_offset);
	
	/* make sure we don't overlap the right side of the screen */
	System.out.printf("if(document.popup.left + document.popup.document.width + %d > window.innerWidth) document.popup.left = window.innerWidth - document.popup.document.width - %d - 16;\n",x_offset,x_offset);
		
	/* set y coordinate */
	System.out.printf("document.popup.top  = eventObj.layerY + %d;\n",y_offset);
	
	/* make sure we don't overlap the bottom edge of the screen */
	System.out.printf("if(document.popup.top + document.popup.document.height + %d > window.innerHeight) document.popup.top = window.innerHeight - document.popup.document.height - %d - 16;\n",y_offset,y_offset);
		
	/* make the popup visible */
	System.out.printf("document.popup.visibility = \"visible\";\n");
	System.out.printf("}\n");
	System.out.printf("}\n");

	System.out.printf("function hidePopup(){ \n");
	System.out.printf("if (!(document.all || document.layers || document.documentElement)) return;\n");
	System.out.printf("if (document.popup == null){ }\n");
	System.out.printf("else if (document.all||document.documentElement) document.popup.style.visibility = \"hidden\";\n");
	System.out.printf("else document.popup.visibility = \"hidden\";\n");
	System.out.printf("document.popup = null;\n");
	System.out.printf("}\n");
	System.out.printf("//-.\n");

	System.out.printf("</SCRIPT>\n");

	return;
        }



/* adds a layer to the list in memory */
public static int add_layer(String group_name){

	if(group_name==null)
		return common_h.ERROR;

    layer_list.add( group_name );
    
	return common_h.OK;
        }


/* checks to see if a host is in the layer list */
public static int is_host_in_layer_list(objects_h.host hst){
	objects_h.hostgroup temp_hostgroup;

	if(hst==null)
		return common_h.FALSE;

	/* check each layer... */
	for(String layer_name : layer_list ){

		/* find the hostgroup */
		temp_hostgroup=objects.find_hostgroup( layer_name);
		if(temp_hostgroup==null)
			continue;
		
		/* is the requested host a member of the hostgroup/layer? */
		if( objects.is_host_member_of_hostgroup(temp_hostgroup,hst)==common_h.TRUE)
			return common_h.TRUE;
	        }

	return common_h.FALSE;
        }


/* print layer url info */
public static void print_layer_url(int get_method){

	for(String layer_name : layer_list ){
		if(get_method==common_h.TRUE)
			System.out.printf("&layer=%s",layer_name);
		else
			System.out.printf("<input type='hidden' name='layer' value='%s'>\n",layer_name);
	        }

	if(get_method==common_h.TRUE)
		System.out.printf("&layermode=%s",(exclude_layers==common_h.TRUE)?"exclude":"include");
	else
		System.out.printf("<input type='hidden' name='layermode' value='%s'>\n",(exclude_layers==common_h.TRUE)?"exclude":"include");

	return;
        }

/******************************************************************/
/************************ UTILITY FUNCTIONS ***********************/
/******************************************************************/

/* calculates how many "layers" separate parent and child - used by collapsed tree layout method */
public static int host_child_depth_separation(objects_h.host parent, objects_h.host child){
	int this_depth=0;
	int min_depth=0;
	int have_min_depth=common_h.FALSE;

	if(child==null)
		return -1;

	if(parent==child)
		return 0;

	if(objects.is_host_immediate_child_of_host(parent,child)==common_h.TRUE)
		return 1;

	for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		if( objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

			this_depth=host_child_depth_separation(temp_host,child);

			if(this_depth>=0 && (have_min_depth==common_h.FALSE || (have_min_depth==common_h.TRUE && (this_depth<min_depth)))){
				have_min_depth=common_h.TRUE;
				min_depth=this_depth;
			        }
		        }
	        }

	if(have_min_depth==common_h.FALSE)
		return -1;
	else
		return min_depth+1;
        }



/* calculates how many hosts reside on a specific "layer" - used by collapsed tree layout method */
public static int number_of_host_layer_members(objects_h.host parent, int layer){
	int current_layer;
	int layer_members=0;

	for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		current_layer=host_child_depth_separation(parent,temp_host);

		if(current_layer==layer)
			layer_members++;
	        }

	return layer_members;
        }



/* calculate max number of members on all "layers" beneath and including parent host - used by collapsed tree layout method */
public static int max_child_host_layer_members(objects_h.host parent){
	int current_layer;
	int max_members=1;
	int current_members=0;

	for(current_layer=1;;current_layer++){

		current_members=number_of_host_layer_members(parent,current_layer);

		if(current_members<=0)
			break;

		if(current_members>max_members)
			max_members=current_members;
	        }

	return max_members;
        }

/* calculate max drawing width for host and children - used by balanced tree layout method */
public static int max_child_host_drawing_width(objects_h.host parent){
	int child_width=0;

	for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){
		
		if(objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE)
			child_width+=max_child_host_drawing_width(temp_host);
	        }

	/* no children, so set width to 1 for this host */
	if(child_width==0)
		return 1;

	else
		return child_width;
        }

/* calculates number of services associated with a particular service */
public static int number_of_host_services(objects_h.host hst){
	int total_services=0;

	if(hst==null)
		return 0;

	/* check all the services */
	for(objects_h.service temp_service : (ArrayList<objects_h.service>) objects.service_list ){
		if( temp_service.host_name.equals(hst.name))
			total_services++;
	        }

	return total_services;
        }
	


/******************************************************************/
/***************** COORDINATE CALCULATION FUNCTIONS ***************/
/******************************************************************/

/* calculates coords of a host's children - used by balanced tree layout method */
public static void calculate_balanced_tree_coords( objects_h.host parent, int x, int y){
	int parent_drawing_width;
	int start_drawing_x;
	int current_drawing_x;
	int this_drawing_width;
	objects_h.host temp_host;
	objects_h.hostextinfo temp_hostextinfo;

	/* calculate total drawing width of parent host */
	parent_drawing_width=max_child_host_drawing_width(parent);

	/* calculate starting x coord */
	start_drawing_x=x-(((DEFAULT_NODE_WIDTH*parent_drawing_width)+(DEFAULT_NODE_HSPACING*(parent_drawing_width-1)))/2);
	current_drawing_x=start_drawing_x;


	/* calculate coords for children */
	for(Iterator iter = objects.host_list.iterator(); iter.hasNext();  ){
        temp_host = (objects_h.host) iter.next();

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo==null)
			continue;

		if( objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

			/* get drawing width of child host */
			this_drawing_width=max_child_host_drawing_width(temp_host);

			temp_hostextinfo.x_2d=current_drawing_x+(((DEFAULT_NODE_WIDTH*this_drawing_width)+(DEFAULT_NODE_HSPACING*(this_drawing_width-1)))/2);
			temp_hostextinfo.y_2d=y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;
			temp_hostextinfo.have_2d_coords=common_h.TRUE;
			temp_hostextinfo.should_be_drawn=common_h.TRUE;
			
			current_drawing_x+=(this_drawing_width*DEFAULT_NODE_WIDTH)+((this_drawing_width-1)*DEFAULT_NODE_HSPACING)+DEFAULT_NODE_HSPACING;

			/* recurse into child host ... */
			calculate_balanced_tree_coords(temp_host,temp_hostextinfo.x_2d,temp_hostextinfo.y_2d);
		        }

	        }

	return;
        }


/* calculate coords of all hosts in circular layout method */
public static void calculate_circular_coords(){
	int min_x=0;
	int min_y=0;
	int have_min_x=common_h.FALSE;
	int have_min_y=common_h.FALSE;
	objects_h.hostextinfo temp_hostextinfo;

	/* calculate all host coords, starting with first layer */
	calculate_circular_layer_coords(null,0.0,360.0,1,CIRCULAR_DRAWING_RADIUS);

	/* adjust all calculated coords so none are negative in x or y axis... */

	/* calculate min x, y coords */
	for(Iterator iter = objects.hostextinfo_list.iterator() ; iter.hasNext();  ){
        temp_hostextinfo= (objects_h.hostextinfo) iter.next();
        
		if(have_min_x==common_h.FALSE || temp_hostextinfo.x_2d<min_x){
			have_min_x=common_h.TRUE;
			min_x=temp_hostextinfo.x_2d;
		        }
		if(have_min_y==common_h.FALSE || temp_hostextinfo.y_2d<min_y){
			have_min_y=common_h.TRUE;
			min_y=temp_hostextinfo.y_2d;
		        }
	        }
	
	/* offset all drawing coords by the min x,y coords we found */
    for(Iterator iter = objects.hostextinfo_list.iterator() ; iter.hasNext();  ){
        temp_hostextinfo= (objects_h.hostextinfo) iter.next();
		if(min_x<0)
			temp_hostextinfo.x_2d-=min_x;
		if(min_y<0)
			temp_hostextinfo.y_2d-=min_y;
	        }

	if(min_x<0)
		nagios_icon_x-=min_x;
	if(min_y<0)
		nagios_icon_y-=min_y;

    for(Iterator iter = objects.hostextinfo_list.iterator() ; iter.hasNext();  ){
        temp_hostextinfo= (objects_h.hostextinfo) iter.next();

        temp_hostextinfo.x_2d+=(DEFAULT_NODE_WIDTH/2);
		temp_hostextinfo.y_2d+=(DEFAULT_NODE_HEIGHT/2);
	        }
	nagios_icon_x+=(DEFAULT_NODE_WIDTH/2);
	nagios_icon_y+=(DEFAULT_NODE_HEIGHT/2);

	return;
        }
	

/* calculates coords of all hosts in a particular "layer" in circular layout method */
public static void calculate_circular_layer_coords(objects_h.host parent, double start_angle, double useable_angle, int layer, int radius){
	int parent_drawing_width=0;
	int this_drawing_width=0;
	int immediate_children=0;
	double current_drawing_angle=0.0;
	double this_drawing_angle=0.0;
	double available_angle=0.0;
	double clipped_available_angle=0.0;
	double average_child_angle=0.0;
	double x_coord=0.0;
	double y_coord=0.0;
	objects_h.host temp_host;
	objects_h.hostextinfo temp_hostextinfo;

	/* get the total number of immediate children to this host */
	immediate_children=objects.number_of_immediate_child_hosts(parent);

	/* bail out if we're done */
	if(immediate_children==0)
		return;

    /* calculate total drawing "width" of parent host */
	parent_drawing_width=max_child_host_drawing_width(parent);

	/* calculate average angle given to each child host */
	average_child_angle=(useable_angle/immediate_children);

	/* calculate initial drawing angle */
	current_drawing_angle=start_angle;


	/* calculate coords for children */
	for(Iterator iter = objects.host_list.iterator(); iter.hasNext(); ){
        temp_host = (objects_h.host) iter.next();

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo==null)
			continue;

        if( objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

			/* get drawing width of child host */
			this_drawing_width=max_child_host_drawing_width(temp_host);

			/* calculate angle this host gets for drawing */
			available_angle=useable_angle*(this_drawing_width/parent_drawing_width);

			/* clip available angle if necessary */
			/* this isn't really necessary, but helps keep things looking a bit more sane with less potential connection crossover */
			clipped_available_angle=360.0/layer;
			if(available_angle<clipped_available_angle)
				clipped_available_angle=available_angle;

			/* calculate the exact angle at which we should draw this child */
			this_drawing_angle=current_drawing_angle+(available_angle/2.0);

			/* compensate for angle overflow */
			while(this_drawing_angle>=360.0)
				this_drawing_angle-=360.0;
			while(this_drawing_angle<0.0)
				this_drawing_angle+=360.0;

			/* calculate drawing coords of this host using good ol' geometry... */
			x_coord=-(Math.sin(-this_drawing_angle*(Math.PI /180.0))*radius);
			y_coord=-(Math.sin((90+this_drawing_angle)*( Math.PI /180.0))*radius);

			temp_hostextinfo.x_2d=(int)x_coord;
			temp_hostextinfo.y_2d=(int)y_coord;
			temp_hostextinfo.have_2d_coords=common_h.TRUE;
			temp_hostextinfo.should_be_drawn=common_h.TRUE;

			/* recurse into child host ... */
			calculate_circular_layer_coords(temp_host,current_drawing_angle+((available_angle-clipped_available_angle)/2),clipped_available_angle,layer+1,radius+CIRCULAR_DRAWING_RADIUS);

			/* increment current drawing angle */
			current_drawing_angle+=available_angle;
		        }
	        }

	return;
        }



/* draws background "extras" for all hosts in circular markup layout */
public static void draw_circular_markup(){

	/* calculate all host sections, starting with first layer */
	draw_circular_layer_markup(null,0.0,360.0,1,CIRCULAR_DRAWING_RADIUS);

	return;
        }


/* draws background "extras" for all hosts in a particular "layer" in circular markup layout */
public static void draw_circular_layer_markup(objects_h.host parent, double start_angle, double useable_angle, int layer, int radius){
	int parent_drawing_width=0;
	int this_drawing_width=0;
	int immediate_children=0;
	double current_drawing_angle=0.0;
	double available_angle=0.0;
	double clipped_available_angle=0.0;
	double average_child_angle=0.0;
	double[] x_coord = new double[] {0.0,0.0,0.0,0.0};
	double[] y_coord= new double[] {0.0,0.0,0.0,0.0};
	objects_h.host temp_host;
	statusdata_h.hoststatus temp_hoststatus;
	objects_h.hostextinfo temp_hostextinfo;
	int x_offset=0;
	int y_offset=0;
	int center_x=0;
	int center_y=0;
	Color bgcolor;
	double arc_start_angle=0.0;
	double arc_end_angle=0.0;
	int translated_x=0;
	int translated_y=0;

	/* get the total number of immediate children to this host */
	immediate_children=objects.number_of_immediate_child_hosts(parent);

	/* bail out if we're done */
	if(immediate_children==0)
		return;

	/* calculate total drawing "width" of parent host */
	parent_drawing_width=max_child_host_drawing_width(parent);

	/* calculate average angle given to each child host */
	average_child_angle=(useable_angle/immediate_children);

	/* calculate initial drawing angle */
	current_drawing_angle=start_angle;

	/* calculate coords for children */
	for(Iterator iter = objects.host_list.iterator(); iter.hasNext(); ){
        temp_host = (objects_h.host) iter.next();

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo==null)
			continue;

		if(objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

			/* get drawing width of child host */
			this_drawing_width=max_child_host_drawing_width(temp_host);

			/* calculate angle this host gets for drawing */
			available_angle=useable_angle*(this_drawing_width/parent_drawing_width);

			/* clip available angle if necessary */
			/* this isn't really necessary, but helps keep things looking a bit more sane with less potential connection crossover */
			clipped_available_angle=360.0/layer;
			if(available_angle<clipped_available_angle)
				clipped_available_angle=available_angle;

			/* calculate drawing coords of "leftmost" divider using good ol' geometry... */
			x_coord[0]=-(Math.sin(-current_drawing_angle*(Math.PI/180.0))*(radius-(CIRCULAR_DRAWING_RADIUS/2)));
			y_coord[0]=-(Math.sin((90+current_drawing_angle)*(Math.PI/180.0))*(radius-(CIRCULAR_DRAWING_RADIUS/2)));
			x_coord[1]=-(Math.sin(-current_drawing_angle*(Math.PI/180.0))*(radius+(CIRCULAR_DRAWING_RADIUS/2)));
			y_coord[1]=-(Math.sin((90+current_drawing_angle)*(Math.PI/180.0))*(radius+(CIRCULAR_DRAWING_RADIUS/2)));

			/* calculate drawing coords of "rightmost" divider using good ol' geometry... */
			x_coord[2]=-(Math.sin((-(current_drawing_angle+available_angle))*(Math.PI/180.0))*(radius-(CIRCULAR_DRAWING_RADIUS/2)));
			y_coord[2]=-(Math.sin((90+current_drawing_angle+available_angle)*(Math.PI/180.0))*(radius-(CIRCULAR_DRAWING_RADIUS/2)));
			x_coord[3]=-(Math.sin((-(current_drawing_angle+available_angle))*(Math.PI/180.0))*(radius+(CIRCULAR_DRAWING_RADIUS/2)));
			y_coord[3]=-(Math.sin((90+current_drawing_angle+available_angle)*(Math.PI/180.0))*(radius+(CIRCULAR_DRAWING_RADIUS/2)));


            x_offset=nagios_icon_x+(DEFAULT_NODE_WIDTH/2)-canvas_x;
            y_offset=nagios_icon_y +(DEFAULT_NODE_HEIGHT/2)-canvas_y;

            Color temp_color = gd_map_image.getColor();
            gd_map_image.setColor( color_lightgrey );

			/* draw "slice" dividers */
			if(immediate_children>1 || layer>1){
                
				/* draw "leftmost" divider */
				gd_map_image.drawLine( (int)x_coord[0]+x_offset,(int)y_coord[0]+y_offset,(int)x_coord[1]+x_offset,(int)y_coord[1]+y_offset);

				/* draw "rightmost" divider */
                gd_map_image.drawLine( (int)x_coord[2]+x_offset,(int)y_coord[2]+y_offset,(int)x_coord[3]+x_offset,(int)y_coord[3]+y_offset );
                
			        }

			/* determine arc drawing angles */
			arc_start_angle=current_drawing_angle-90.0;
			while(arc_start_angle<0.0)
				arc_start_angle+=360.0;
			arc_end_angle=arc_start_angle+available_angle;

			/* draw inner arc */
            
			gd_map_image.drawArc(x_offset-(radius-(CIRCULAR_DRAWING_RADIUS/2)),y_offset -(radius-(CIRCULAR_DRAWING_RADIUS/2)),(radius-(CIRCULAR_DRAWING_RADIUS/2))*2,(radius-(CIRCULAR_DRAWING_RADIUS/2))*2,(int) Math.floor(arc_start_angle),(int) Math.ceil(arc_end_angle));

			/* draw outer arc */
            gd_map_image.drawArc(x_offset-(radius+(CIRCULAR_DRAWING_RADIUS/2)),y_offset/*   -(radius+(CIRCULAR_DRAWING_RADIUS/2)) */,(radius+(CIRCULAR_DRAWING_RADIUS/2))*2,(radius+(CIRCULAR_DRAWING_RADIUS/2))*2,(int) Math.floor(arc_start_angle),(int) Math.ceil(arc_end_angle) );

            gd_map_image.setColor(temp_color);

			/* determine center of "slice" and fill with appropriate color */
			center_x=- (int) (Math.sin(-(current_drawing_angle+(available_angle/2.0))*(Math.PI/180.0))*(radius));
			center_y=- (int) (Math.sin((90+current_drawing_angle+(available_angle/2.0))*(Math.PI/180.0))*(radius));
			translated_x=center_x+x_offset;
			translated_y=center_y+y_offset;

			/* determine background color */
			temp_hoststatus=statusdata.find_hoststatus(temp_host.name);
			if(temp_hoststatus==null)
				bgcolor=color_lightgrey;
			else if(temp_hoststatus.status==statusdata_h.HOST_DOWN || temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
				bgcolor=color_lightred;
			else
				bgcolor=color_lightgreen;

			/* fill slice with background color */
			/* the fill function only works with coordinates that are in bounds of the actual image */
            // TODO fill the image
//			if(translated_x>0 && translated_y>0 && translated_x<canvas_width && translated_y<canvas_height)
//				gdImageFillToBorder(map_image,translated_x,translated_y,color_lightgrey,bgcolor);

			/* recurse into child host ... */
			draw_circular_layer_markup(temp_host,current_drawing_angle+((available_angle-clipped_available_angle)/2),clipped_available_angle,layer+1,radius+CIRCULAR_DRAWING_RADIUS);

			/* increment current drawing angle */
			current_drawing_angle+=available_angle;
		        }
	        }

	return;
        }

private static int atoi(String value) {
    try {
        return Integer.parseInt(value);
    } catch ( NumberFormatException nfE ) {
//        logger.throwing( cn, "atoi", nfE);
        return 0;
    }
}

private static double strtod(String value, Object ignore ) {
    try {
        return Double.parseDouble(value);
    } catch ( NumberFormatException nfE ) {
//        logger.throwing( cn, "atoi", nfE);
        return 0.0;
    }
}

}