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

import java.util.ArrayList;

import org.blue.star.common.objects;
import org.blue.star.common.statusdata;
import org.blue.star.include.cgiauth_h;
import org.blue.star.include.cgiutils_h;
import org.blue.star.include.common_h;
import org.blue.star.include.objects_h;
import org.blue.star.include.statusdata_h;

public class statuswrl extends blue_servlet {

    public static String NAGIOS_VRML_IMAGE               = "nagiosvrml.png";
    
    public static final float DEFAULT_NODE_WIDTH		= 0.5f;
    public static final float DEFAULT_HORIZONTAL_SPACING	=1.0f;
    public static final float DEFAULT_VERTICAL_SPACING	= 1.0f;
    
    /* needed for auto-layout modes */
    public static final float DEFAULT_NODE_HEIGHT             = 0.5f;
    public static final float DEFAULT_NODE_HSPACING           = 1.0f;
    public static final float DEFAULT_NODE_VSPACING           = 1.0f;
    public static final float CIRCULAR_DRAWING_RADIUS         = 5.0f;
    
    public static final int LAYOUT_USER_SUPPLIED            = 0;
    public static final int LAYOUT_COLLAPSED_TREE           = 2;
    public static final int LAYOUT_BALANCED_TREE            = 3;
    public static final int LAYOUT_CIRCULAR                 = 4;
    
    public static cgiauth_h.authdata current_authdata;
    
    public static float link_radius=0.016f;
    
    public static float floor_width=0.0f;
    public static float floor_depth=0.0f;
    
    public static double min_z_coord=0.0;
    public static double min_x_coord=0.0;
    public static double min_y_coord=0.0;
    public static double max_z_coord=0.0;
    public static double max_x_coord=0.0;
    public static double max_y_coord=0.0;
    
    public static double max_world_size=0.0;
    
    public static double nagios_icon_x=0.0;
    public static double nagios_icon_y=0.0;
    public static int draw_nagios_icon=common_h.FALSE;
    
    public static double custom_viewpoint_x=0.0;
    public static double custom_viewpoint_y=0.0;
    public static double custom_viewpoint_z=0.0;
    public static int custom_viewpoint=common_h.FALSE;
    
    public static float vertical_spacing=DEFAULT_VERTICAL_SPACING;
    public static float horizontal_spacing=DEFAULT_HORIZONTAL_SPACING;
    public static float node_width=DEFAULT_NODE_WIDTH;
    public static float node_height=DEFAULT_NODE_WIDTH;	/* should be the same as the node width */
    
    public static String host_name="all";
    public static int show_all_hosts=common_h.TRUE;
    
    public static int use_textures=common_h.TRUE;
    public static int use_text=common_h.TRUE;
    public static int use_links=common_h.TRUE;
    
    public static int layout_method=LAYOUT_USER_SUPPLIED;
    
    public static int coordinates_were_specified=common_h.FALSE;   /* were drawing coordinates specified with extended host info entries? */

    public void reset_context() {
       current_authdata = new cgiauth_h.authdata ();
       
       link_radius=0.016f;
       
       floor_width=0.0f;
       floor_depth=0.0f;
       
       min_z_coord=0.0;
       min_x_coord=0.0;
       min_y_coord=0.0;
       max_z_coord=0.0;
       max_x_coord=0.0;
       max_y_coord=0.0;
       
       max_world_size=0.0;
       
       nagios_icon_x=0.0;
       nagios_icon_y=0.0;
       draw_nagios_icon=common_h.FALSE;
       
       custom_viewpoint_x=0.0;
       custom_viewpoint_y=0.0;
       custom_viewpoint_z=0.0;
       custom_viewpoint=common_h.FALSE;
       
       vertical_spacing=DEFAULT_VERTICAL_SPACING;
       horizontal_spacing=DEFAULT_HORIZONTAL_SPACING;
       node_width=DEFAULT_NODE_WIDTH;
       node_height=DEFAULT_NODE_WIDTH;  /* should be the same as the node width */
       
       host_name="all";
       show_all_hosts=common_h.TRUE;
       
       use_textures=common_h.TRUE;
       use_text=common_h.TRUE;
       use_links=common_h.TRUE;
       
       layout_method=LAYOUT_USER_SUPPLIED;
       
       coordinates_were_specified=common_h.FALSE;   /* were drawing coordinates specified with extended host info entries? */
    }
    
    public void call_main() {
       main( null );
    }
    
    public static void main(String[] argv){
	int result;

    /* get the arguments passed in the URL */
    process_cgivars();
    
	/* read the CGI configuration file */
	result= cgiutils.read_cgi_config_file(cgiutils.get_cgi_config_location());
	if(result==common_h.ERROR){
		document_header();
		cgiutils.exit( common_h.ERROR);
        return;
	        }

	/* defaults from CGI config file */
	layout_method=cgiutils.default_statuswrl_layout_method;

	document_header();

	/* read the main configuration file */
	result=cgiutils.read_main_config_file(cgiutils.main_config_file);
	if(result==common_h.ERROR) {
		cgiutils.exit(  common_h.ERROR );
        return;
    }

	/* read all object configuration data */
	result=cgiutils.read_all_object_configuration_data(cgiutils.main_config_file,common_h.READ_ALL_OBJECT_DATA);
	if(result==common_h.ERROR) {
        cgiutils.exit(  common_h.ERROR );
        return;
    }

	/* read all status data */
	result=cgiutils.read_all_status_data(cgiutils.get_cgi_config_location(),statusdata_h.READ_ALL_STATUS_DATA);
	if(result==common_h.ERROR){
        cgiutils.exit(  common_h.ERROR );
        return;
                }

	/* get authentication information */
	cgiauth.get_authentication_information(current_authdata);

	/* display the 3-D VRML world... */
	display_world();

	cgiutils.exit(  common_h.OK );
        }



public static void document_header(){
	String date_time ; // MAX_DATETIME_LENGTH

    if ( response != null ) {
       response.setHeader( "Cache-Control",  "no-store" );
       response.setHeader( "Pragma",  "no-cache" );
       response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
       response.setDateHeader( "Expires", System.currentTimeMillis() );
       response.setContentType("x-world/x-vrml");
    } else {
      
      	System.out.printf("Cache-Control: no-store\r\n");
      	System.out.printf("Pragma: no-cache\r\n");
      
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Last-Modified: %s\r\n",date_time);
      	
      	date_time = cgiutils.get_time_string( 0, common_h.HTTP_DATE_TIME);
      	System.out.printf("Expires: %s\r\n",date_time);
      
      	System.out.printf("Content-Type: x-world/x-vrml\r\n\r\n");
    }
    
	return;
        }



public static int process_cgivars(){
	String[] variables;
	int error=common_h.FALSE;
	int x;

	variables=getcgi.getcgivars( request_string );

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

			if( host_name.equals("all"))
				show_all_hosts=common_h.TRUE;
			else
				show_all_hosts=common_h.FALSE;
		        }

		/* we found the no textures argument*/
		else if(variables[x].equals("notextures"))
			use_textures=common_h.FALSE;

		/* we found the no text argument*/
		else if(variables[x].equals("notext"))
			use_text=common_h.FALSE;

		/* we found the no links argument*/
		else if(variables[x].equals("nolinks"))
			use_links=common_h.FALSE;

		/* we found the layout method option */
		else if(variables[x].equals("layout")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			layout_method=atoi(variables[x]);
		        }

		/* we found custom viewpoint coord */
		else if(variables[x].equals("viewx")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			custom_viewpoint_x=strtod(variables[x],null);
			custom_viewpoint=common_h.TRUE;
		        }
		else if(variables[x].equals("viewy")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			custom_viewpoint_y=strtod(variables[x],null);
			custom_viewpoint=common_h.TRUE;
		        }
		else if(variables[x].equals("viewz")){
			x++;
			if(variables[x]==null){
				error=common_h.TRUE;
				break;
			        }
			custom_viewpoint_z=strtod(variables[x],null);
			custom_viewpoint=common_h.TRUE;
		        }

	        }

	/* free memory allocated to the CGI variables */
	getcgi.free_cgivars(variables);

	return error;
        }



/* top-level VRML world generation... */
public static void display_world(){

	/* get the url we will use to grab the logo images... */
	cgiutils.url_logo_images_path = String.format( "%slogos/",cgiutils.url_images_path);

	/* calculate host drawing coordinates */
	calculate_host_coords();

	/* calculate world bounds */
	calculate_world_bounds();

	/* get the floor dimensions */
	if(max_x_coord>0)
		floor_width=(float)(max_x_coord-min_x_coord)+(node_width*2);
	else
		floor_width=(float)(max_x_coord+min_x_coord)+(node_width*2);
	if(max_z_coord>0)
		floor_depth=(float)(max_z_coord-min_z_coord)+(node_height*2);
	else
		floor_depth=(float)(max_z_coord+min_z_coord)+(node_height*2);

	/* write global VRML data */
	write_global_vrml_data();

	/* no coordinates were specified, so display warning message */
	if(coordinates_were_specified==common_h.FALSE){

		System.out.printf("\n");
		System.out.printf("Transform{\n");
		System.out.printf("translation 0.0 0.0 0.0\n");
		System.out.printf("children[\n");

		System.out.printf("Billboard{\n");
		System.out.printf("children[\n");
		System.out.printf("Shape{\n");
		System.out.printf("appearance Appearance {\n");
		System.out.printf("material Material {\n");
		System.out.printf("diffuseColor 1 0 0\n");
		System.out.printf("}\n");
		System.out.printf("}\n");
		System.out.printf("geometry Text {\n");
		System.out.printf("string [ \"Error: You have not supplied any 3-D drawing coordinates.\", \"Read the documentation for more information on supplying\", \"3-D drawing coordinates by defining\", \"extended host information entries in your config files.\" ]\n");
		System.out.printf("fontStyle FontStyle {\n");
		System.out.printf("family \"TYPEWRITER\"\n");
		System.out.printf("size 0.3\n");
		System.out.printf("justify \"MIDDLE\"\n");
		System.out.printf("}\n");
		System.out.printf("}\n");
		System.out.printf("}\n");
		System.out.printf("]\n");
		System.out.printf("}\n");

		System.out.printf("]\n");
		System.out.printf("}\n");
	        }

	/* coordinates were specified... */
	else{

		/* draw Nagios icon */
		if(layout_method!=LAYOUT_USER_SUPPLIED)
			draw_process_icon();

		/* draw all hosts */
		for(objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list )
			draw_host(temp_hostextinfo);

		/* draw host links */
		draw_host_links();
	        }

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

	for( objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		if(objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

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

	for(objects_h.host  temp_host : (ArrayList<objects_h.host>) objects.host_list ){

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
	



/******************************************************************/
/********************* CALCULATION FUNCTIONS **********************/
/******************************************************************/

/* calculates host drawing coordinates */
public static void calculate_host_coords(){
//	hostextinfo *temp_hostextinfo;
	objects_h.host this_host;
//	host *temp_host;
	int parent_hosts=0;
	int max_layer_width=1;
	int current_parent_host=0;
	int center_x=0;
	int offset_x= (int) DEFAULT_NODE_WIDTH/2;
	int offset_y= (int) DEFAULT_NODE_WIDTH/2;
	int current_layer=0;
	int layer_members=0;
	int current_layer_member=0;
	int max_drawing_width=0;
  

	/******************************/
	/***** MANUAL LAYOUT MODE *****/
	/******************************/

	/* user-supplied coords */
	if(layout_method==LAYOUT_USER_SUPPLIED){

		/* see which hosts we should draw (only those with 3-D coords) */
		for( objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

			if(temp_hostextinfo.have_3d_coords==common_h.TRUE)
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
	for( objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		/* find the corresponding hostextinfo definition */
		objects_h.hostextinfo temp_hostextinfo=objects.find_hostextinfo(temp_host.name);

		/* none was found, so add a blank one */
		if(temp_hostextinfo==null)
			objects.add_hostextinfo(temp_host.name,null,null,null,null,null,null,null,0,0,0.0,0.0,0.0,0,0);

		/* default z coord should 0 for auto-layout modes unless overridden later */
		else
			temp_hostextinfo.z_3d=0.0;
	        }


	/***** COLLAPSED TREE MODE *****/
	if(layout_method==LAYOUT_COLLAPSED_TREE){

		/* always use null as the "main" host, screen coords/dimensions are adjusted automatically */
		this_host=null;

		/* find total number of immediate parents for this host */
		parent_hosts=objects.number_of_immediate_parent_hosts(this_host);

		/* find the max layer width we have... */
		max_layer_width=max_child_host_layer_members(this_host);
		if(parent_hosts>max_layer_width)
			max_layer_width=parent_hosts;

		/* calculate center x coord */
		center_x=(int) (((DEFAULT_NODE_WIDTH*max_layer_width)+(DEFAULT_NODE_HSPACING*(max_layer_width-1)))/2)+offset_x;

		/* coords for Nagios icon if necessary */
		if(this_host==null || this_host.parent_hosts==null || this_host.parent_hosts.size() == 0){
			nagios_icon_x=center_x;
			nagios_icon_y=offset_y;
			draw_nagios_icon=common_h.TRUE;
		        }

		/* do we need to draw a link to parent(s)? */
		if(this_host!=null && objects.is_host_immediate_child_of_host(null,this_host)==common_h.FALSE)
			offset_y+=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;

		/* see which hosts we should draw and calculate drawing coords */
        for( objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

			/* find the host that matches this entry */
			objects_h.host temp_host= objects.find_host(temp_hostextinfo.host_name);

			if(temp_host==null)
				continue;
			
			/* this is an immediate parent of the "main" host we're drawing */
			else if(objects.is_host_immediate_parent_of_host(this_host,temp_host)==common_h.TRUE){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_3d_coords=common_h.TRUE;
				temp_hostextinfo.x_3d=center_x-(((parent_hosts*DEFAULT_NODE_WIDTH)+((parent_hosts-1)*DEFAULT_NODE_HSPACING))/2)+(current_parent_host*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
				temp_hostextinfo.y_3d=offset_y;
				current_parent_host++;
			        }
			
			/* this is the "main" host we're drawing */
			else if(this_host==temp_host){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_3d_coords=common_h.TRUE;
				temp_hostextinfo.x_3d=center_x;
				temp_hostextinfo.y_3d=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y;
			        }

			/* else do not draw this host (we might if its a child - see below, but assume no for now) */
			else{
				temp_hostextinfo.should_be_drawn=common_h.FALSE;
				temp_hostextinfo.have_3d_coords=common_h.FALSE;
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
            for( objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

				/* find the host that matches this entry */
				objects_h.host temp_host= objects.find_host(temp_hostextinfo.host_name);

				if(temp_host==null)
					continue;

				/* is this host a member of the current child layer? */
				if(host_child_depth_separation(this_host,temp_host)==current_layer){
					temp_hostextinfo.should_be_drawn=common_h.TRUE;
					temp_hostextinfo.have_3d_coords=common_h.TRUE;
					temp_hostextinfo.x_3d=center_x-(((layer_members*DEFAULT_NODE_WIDTH)+((layer_members-1)*DEFAULT_NODE_HSPACING))/2)+(current_layer_member*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
					if(this_host==null)
						temp_hostextinfo.y_3d=((DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING)*current_layer)+offset_y;
					else
						temp_hostextinfo.y_3d=((DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING)*(current_layer+1))+offset_y;
					current_layer_member++;
				        }
			        }
		        }

	        }


	/***** "BALANCED" TREE MODE *****/
	else if(layout_method==LAYOUT_BALANCED_TREE){

		/* always use null as the "main" host, screen coords/dimensions are adjusted automatically */
		this_host=null;

		/* find total number of immediate parents for this host */
		parent_hosts=objects.number_of_immediate_parent_hosts(this_host);

		/* find the max drawing width we have... */
		max_drawing_width=max_child_host_drawing_width(this_host);
		if(parent_hosts>max_drawing_width)
			max_drawing_width=parent_hosts;

		/* calculate center x coord */
		center_x=(int) (((DEFAULT_NODE_WIDTH*max_drawing_width)+(DEFAULT_NODE_HSPACING*(max_drawing_width-1)))/2)+offset_x;

		/* coords for Nagios icon if necessary */
		if(this_host==null || this_host.parent_hosts==null || this_host.parent_hosts.size() == 0){
			nagios_icon_x=center_x;
			nagios_icon_y=offset_y;
			draw_nagios_icon=common_h.TRUE;
		        }

		/* do we need to draw a link to parent(s)? */
		if(this_host!=null && objects.is_host_immediate_child_of_host(null,this_host)==common_h.FALSE)
			offset_y+=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;

		/* see which hosts we should draw and calculate drawing coords */
        for( objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

			/* find the host that matches this entry */
			objects_h.host temp_host= objects.find_host(temp_hostextinfo.host_name);

			if(temp_host==null)
				continue;
			
			/* this is an immediate parent of the "main" host we're drawing */
			else if( objects.is_host_immediate_parent_of_host(this_host,temp_host)==common_h.TRUE){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_3d_coords=common_h.TRUE;
				temp_hostextinfo.x_3d=center_x-(((parent_hosts*DEFAULT_NODE_WIDTH)+((parent_hosts-1)*DEFAULT_NODE_HSPACING))/2)+(current_parent_host*(DEFAULT_NODE_WIDTH+DEFAULT_NODE_HSPACING))+(DEFAULT_NODE_WIDTH/2);
				temp_hostextinfo.y_3d=offset_y;
				current_parent_host++;
			        }
			
			/* this is the "main" host we're drawing */
			else if(this_host==temp_host){
				temp_hostextinfo.should_be_drawn=common_h.TRUE;
				temp_hostextinfo.have_3d_coords=common_h.TRUE;
				temp_hostextinfo.x_3d=center_x;
				temp_hostextinfo.y_3d=DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y;
			        }

			/* else do not draw this host (we might if its a child - see below, but assume no for now) */
			else{
				temp_hostextinfo.should_be_drawn=common_h.FALSE;
				temp_hostextinfo.have_3d_coords=common_h.FALSE;
			        }
		        }

		/* draw all children hosts */
		calculate_balanced_tree_coords(this_host,center_x,(int)(DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING+offset_y));

	        }


	/***** CIRCULAR LAYOUT MODE *****/
	else if(layout_method==LAYOUT_CIRCULAR){

		/* draw process icon */
		nagios_icon_x=0;
		nagios_icon_y=0;
		draw_nagios_icon=common_h.TRUE;

		/* calculate coordinates for all hosts */
		calculate_circular_coords();
	        }

	return;
        }



/* calculate world dimensions */
public static void calculate_world_bounds(){

	min_x_coord=0.0;
	min_y_coord=0.0;
	min_z_coord=0.0;
	max_x_coord=0.0;
	max_y_coord=0.0;
	max_z_coord=0.0;

	/* check all extended host entries */
    for( objects_h.hostextinfo temp_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

		if(temp_hostextinfo.have_3d_coords==common_h.FALSE){
			temp_hostextinfo.should_be_drawn=common_h.FALSE;
			continue;
		        }

		if(temp_hostextinfo.should_be_drawn==common_h.FALSE)
			continue;

		if(temp_hostextinfo.x_3d < min_x_coord)
			min_x_coord=temp_hostextinfo.x_3d;
		else if(temp_hostextinfo.x_3d > max_x_coord)
			max_x_coord=temp_hostextinfo.x_3d;
		if(temp_hostextinfo.y_3d < min_y_coord)
			min_y_coord=temp_hostextinfo.y_3d;
		else if(temp_hostextinfo.y_3d > max_y_coord)
			max_y_coord=temp_hostextinfo.y_3d;
		if(temp_hostextinfo.z_3d < min_z_coord)
			min_z_coord=temp_hostextinfo.z_3d;
		else if(temp_hostextinfo.z_3d > max_z_coord)
			max_z_coord=temp_hostextinfo.z_3d;

		coordinates_were_specified=common_h.TRUE;
	        }

	/* no drawing coordinates were specified */
	if(coordinates_were_specified==common_h.FALSE){
		min_x_coord=0.0;
		max_x_coord=0.0;
		min_y_coord=0.0;
		max_y_coord=0.0;
		min_z_coord=0.0;
		max_z_coord=6.0;
	        }

	max_world_size=max_x_coord-min_x_coord;
	if(max_world_size<(max_y_coord-min_y_coord))
		max_world_size=max_y_coord-min_y_coord;
	if(max_world_size<(max_z_coord-min_z_coord))
		max_world_size=max_z_coord-min_z_coord;

	return;
        }


/******************************************************************/
/*********************** DRAWING FUNCTIONS ************************/
/******************************************************************/


/* write global VRML data */
public static void write_global_vrml_data(){
	objects_h.hostextinfo temp_hostextinfo;
	float visibility_range=0.0f;
	float viewpoint_z=0.0f;

	/* write VRML code header */
	System.out.printf("#VRML V2.0 utf8\n");

	/* write world information */
	System.out.printf("\n");
	System.out.printf("WorldInfo{\n");
	System.out.printf("title \"Nagios 3-D Network Status View\"\n");
	System.out.printf("info [\"Copyright (c) 1999-2002 Ethan Galstad\"\n");
        System.out.printf("\"blue@blue.org\"]\n");
	System.out.printf("}\n");

	/* background color */
	System.out.printf("\n");
	System.out.printf("Background{\n");
	System.out.printf("skyColor 0.1 0.1 0.15\n");
	System.out.printf("}\n");

	/* calculate visibility range - don't let it get too low */
	visibility_range=(float) (max_world_size*2.0);
	if(visibility_range<25.0)
		visibility_range=25.0f;

	/* write fog information */
	System.out.printf("\n");
	System.out.printf("Fog{\n");
	System.out.printf("color 0.1 0.1 0.15\n");
	System.out.printf("fogType \"EXPONENTIAL\"\n");
	System.out.printf("visibilityRange %2.2f\n",visibility_range);
	System.out.printf("}\n");

	/* custom viewpoint */
	if(custom_viewpoint==common_h.TRUE){
		System.out.printf("\n");
		System.out.printf("Viewpoint{\n");
		System.out.printf("position %2.2f %2.2f %2.2f\n",custom_viewpoint_x,custom_viewpoint_y,custom_viewpoint_z);
		System.out.printf("fieldOfView 0.78\n");
		System.out.printf("description \"Entry Viewpoint\"\n");
		System.out.printf("}\n");
	        }

	/* host close-up viewpoint */
	if(show_all_hosts==common_h.FALSE){

		temp_hostextinfo=objects.find_hostextinfo(host_name);
		if(temp_hostextinfo!=null && temp_hostextinfo.have_3d_coords==common_h.TRUE){
			System.out.printf("\n");
			System.out.printf("Viewpoint{\n");
			System.out.printf("position %2.3f %2.3f %2.3f\n",temp_hostextinfo.x_3d,temp_hostextinfo.y_3d,temp_hostextinfo.z_3d+5.0);
			System.out.printf("fieldOfView 0.78\n");
			System.out.printf("description \"Host Close-Up Viewpoint\"\n");
			System.out.printf("}\n");
	                }
	        }

	/* calculate z coord for default viewpoint - don't get too close */
	viewpoint_z=(float)max_world_size;
	if(viewpoint_z<10.0)
		viewpoint_z=10.0f;

	/* default viewpoint */
	System.out.printf("\n");
	System.out.printf("Viewpoint{\n");
	System.out.printf("position %2.2f %2.2f %2.2f\n",min_x_coord+((max_x_coord-min_x_coord)/2.0),min_y_coord+((max_y_coord-min_y_coord)/2.0),viewpoint_z);
	System.out.printf("fieldOfView 0.78\n");
	System.out.printf("description \"Default Viewpoint\"\n");
	System.out.printf("}\n");

	/* problem timer */
	System.out.printf("DEF ProblemTimer TimeSensor{\n");
	System.out.printf("loop TRUE\n");
	System.out.printf("cycleInterval 5\n");
	System.out.printf("}\n");

	/* host text prototype */
	System.out.printf("PROTO HostText[\n");
	System.out.printf("field MFString the_text [\"\"]\n");
	System.out.printf("field SFColor font_color 0.6 0.6 0.6");
	System.out.printf("]\n");
	System.out.printf("{\n");
	System.out.printf("Billboard{\n");
	System.out.printf("children[\n");
	System.out.printf("Shape{\n");
	System.out.printf("appearance Appearance {\n");
	System.out.printf("material Material {\n");
	System.out.printf("diffuseColor IS font_color\n");
	System.out.printf("}\n");
	System.out.printf("}\n");
	System.out.printf("geometry Text {\n");
	System.out.printf("string IS the_text\n");
	System.out.printf("fontStyle FontStyle {\n");
	System.out.printf("family \"TYPEWRITER\"\n");
	System.out.printf("size 0.1\n");
	System.out.printf("justify \"MIDDLE\"\n");
	System.out.printf("}\n");
	System.out.printf("}\n");
	System.out.printf("}\n");
	System.out.printf("]\n");
	System.out.printf("}\n");
	System.out.printf("}\n");

	/* include user-defined world */
	if(cgiutils.statuswrl_include!=null && coordinates_were_specified==common_h.TRUE && layout_method==LAYOUT_USER_SUPPLIED){
		System.out.printf("\n");
		System.out.printf("Inline{\n");
		System.out.printf("url \"%s%s\"\n",cgiutils.url_html_path,cgiutils.statuswrl_include);
		System.out.printf("}\n");
	        }

	return;
	}



/* draws a host */
public static void draw_host(objects_h.hostextinfo temp_hostextinfo){
	objects_h.host temp_host;
	statusdata_h.hoststatus temp_hoststatus=null;
	String state_string =""; // 16
	double x, y, z;
	String vrml_safe_hostname=null;

	if(temp_hostextinfo==null)
		return;

	/* make sure we have the coordinates */
	if(temp_hostextinfo.have_3d_coords==common_h.FALSE)
		return;
	else{
		x=temp_hostextinfo.x_3d;
		y=temp_hostextinfo.y_3d;
		z=temp_hostextinfo.z_3d;
	        }

	/* find the config entry for this host */
	temp_host=objects.find_host(temp_hostextinfo.host_name);
	if(temp_host==null)
		return;

	/* make the host name safe for embedding in VRML */
	vrml_safe_hostname= temp_host.name;
	if(vrml_safe_hostname==null)
		return;
    
    StringBuffer buffer = new StringBuffer();
    for(  char ch : vrml_safe_hostname.toCharArray() ){
		
		if((ch<'a' || ch>'z') && (ch<'A' || ch>'Z') && (ch<'0' || ch>'9'))
			buffer.append('_');
        else
            buffer.append( ch );
	        }

	/* see if user is authorized to view this host  */
	if(cgiauth.is_authorized_for_host(temp_host,current_authdata)==common_h.FALSE)
		return;

	/* get the status of the host */
	temp_hoststatus=statusdata.find_hoststatus(temp_host.name);

	System.out.printf("\n");


	/* host object */
	System.out.printf("Anchor{\n");
	System.out.printf("children[\n");

	System.out.printf("Transform {\n");
	System.out.printf("translation %2.2f %2.2f %2.2f\n",x,y,z);
	System.out.printf("children [\n");

	System.out.printf("DEF Host%s Shape{\n",vrml_safe_hostname);
	System.out.printf("appearance Appearance{\n");
	System.out.printf("material DEF HostMat%s Material{\n",vrml_safe_hostname);
	if(temp_hoststatus==null)
		System.out.printf("emissiveColor 0.2 0.2 0.2\ndiffuseColor 0.2 0.2 0.2\n");
	else if(temp_hoststatus.status==statusdata_h.HOST_UP)
		System.out.printf("emissiveColor 0.2 1.0 0.2\ndiffuseColor 0.2 1.0 0.2\n");
	else
		System.out.printf("emissiveColor 1.0 0.2 0.2\ndiffuseColor 1.0 0.2 0.2\n");
	System.out.printf("transparency 0.4\n");
	System.out.printf("}\n");
	if(use_textures==common_h.TRUE && temp_hostextinfo.vrml_image!=null){
		System.out.printf("texture ImageTexture{\n");
		System.out.printf("url \"%s%s\"\n",cgiutils.url_logo_images_path,temp_hostextinfo.vrml_image);
		System.out.printf("}\n");
		}
	System.out.printf("}\n");
	System.out.printf("geometry Box{\n");
	System.out.printf("size %2.2f %2.2f %2.2f\n",node_width,node_width,node_width);
	System.out.printf("}\n");
	System.out.printf("}\n");

	System.out.printf("]\n");
	System.out.printf("}\n");

	System.out.printf("]\n");
	System.out.printf("description \"View status details for host '%s' (%s)\"\n",temp_host.name,temp_host.alias);
	System.out.printf("url \"%s?host=%s\"\n",cgiutils_h.STATUS_CGI,temp_host.name);
	System.out.printf("}\n");


	/* draw status text */
	if(use_text==common_h.TRUE){

		System.out.printf("\n");
		System.out.printf("Transform{\n");
		System.out.printf("translation %2.3f %2.3f %2.3f\n",x,y+DEFAULT_NODE_WIDTH,z);
		System.out.printf("children[\n");
		System.out.printf("HostText{\n");

		if(temp_hoststatus!=null){
			if(temp_hoststatus.status==statusdata_h.HOST_UP)
				System.out.printf("font_color 0 1 0\n");
			else if(temp_hoststatus.status==statusdata_h.HOST_DOWN || temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
				System.out.printf("font_color 1 0 0\n");
	                }
		System.out.printf("the_text [\"%s\", \"%s\", ",temp_host.name,temp_host.alias);
		if(temp_hoststatus==null)
			state_string = "UNKNOWN";
		else{
			if(temp_hoststatus.status==statusdata_h.HOST_DOWN)
				state_string="DOWN";
			else if(temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE)
				state_string="UNREACHABLE";
			else if(temp_hoststatus.status==statusdata_h.HOST_PENDING)
				state_string="PENDING";
			else
				state_string="UP";
	                }
		System.out.printf("\"%s\"]\n",state_string);

		System.out.printf("}\n");
		System.out.printf("]\n");
		System.out.printf("}\n");
	        }

	/* host is down or unreachable, so make it fade in and out */
	if(temp_hoststatus!=null && (temp_hoststatus.status==statusdata_h.HOST_DOWN || temp_hoststatus.status==statusdata_h.HOST_UNREACHABLE))
		System.out.printf("ROUTE ProblemTimer.fraction_changed TO HostMat%s.set_transparency\n",vrml_safe_hostname);

	return;
	}



/* draw links between hosts */
public static void draw_host_links(){
    objects_h.hostextinfo parent_hostextinfo;
    objects_h.host child_host;

	if(use_links==common_h.FALSE)
		return;

	for(objects_h.hostextinfo child_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

		if(child_hostextinfo.have_3d_coords==common_h.FALSE)
			continue;

		child_host=objects.find_host(child_hostextinfo.host_name);
		if(child_host==null)
			continue;

		/* check authorization */
		if(cgiauth.is_authorized_for_host(child_host,current_authdata)==common_h.FALSE)
			continue;

		/* draw a link from this host to all of its parent hosts */
		for(objects_h.host parent_host : (ArrayList<objects_h.host>) objects.host_list ){

			if(objects.is_host_immediate_child_of_host(child_host,parent_host)==common_h.TRUE){

				parent_hostextinfo=objects.find_hostextinfo(parent_host.name);

				if(parent_hostextinfo==null)
					continue;

				if(parent_hostextinfo.have_3d_coords==common_h.FALSE)
					continue;
				
				/* check authorization */
				if(cgiauth.is_authorized_for_host(parent_host,current_authdata)==common_h.FALSE)
					continue;

				/* draw the link between the child and parent hosts */
				draw_host_link(parent_host,parent_hostextinfo.x_3d,parent_hostextinfo.y_3d,parent_hostextinfo.z_3d,child_hostextinfo.x_3d,child_hostextinfo.y_3d,child_hostextinfo.z_3d);
			        }
		        }
	        }

	
	return;
        }




/* draws a link from a parent host to a child host */
public static void draw_host_link(objects_h.host hst,double x0, double y0, double z0, double x1, double y1, double z1){

	System.out.printf("\n");

	if(hst!=null)
		System.out.printf("# Host '%s' LINK\n",hst.name);

	System.out.printf("Shape{\n");

	System.out.printf("appearance DEF MATslategrey_0_ Appearance {\n");
	System.out.printf("material Material {\n");
	System.out.printf("diffuseColor 0.6 0.6 0.6\n");
	System.out.printf("ambientIntensity 0.5\n");
	System.out.printf("emissiveColor 0.6 0.6 0.6\n");
        System.out.printf("}\n");
	System.out.printf("}\n");

	System.out.printf("geometry IndexedLineSet{\n");
	System.out.printf("coord Coordinate{\n");
	System.out.printf("point [ %2.3f %2.3f %2.3f, %2.3f %2.3f %2.3f ]\n",x0,y0,z0,x1,y1,z1);
	System.out.printf("}\n");
	System.out.printf("coordIndex [ 0,1,-1 ]\n");
	System.out.printf("}\n");

	System.out.printf("}\n");

	return;
	}



/* draw process icon */
public static void draw_process_icon(){
	objects_h.host child_host;

	if(draw_nagios_icon==common_h.FALSE)
		return;

	/* draw process icon */
	System.out.printf("\n");


	System.out.printf("Anchor{\n");
	System.out.printf("children[\n");

	System.out.printf("Transform {\n");
	System.out.printf("translation %2.2f %2.2f %2.2f\n",nagios_icon_x,nagios_icon_y,0.0);
	System.out.printf("children [\n");

	System.out.printf("DEF ProcessNode Shape{\n");
	System.out.printf("appearance Appearance{\n");
	System.out.printf("material Material{\n");
	System.out.printf("emissiveColor 0.5 0.5 0.5\n");
	System.out.printf("diffuseColor 0.5 0.5 0.5\n");
	System.out.printf("transparency 0.2\n");
	System.out.printf("}\n");
	if(use_textures==common_h.TRUE){
		System.out.printf("texture ImageTexture{\n");
		System.out.printf("url \"%s%s\"\n",cgiutils.url_logo_images_path,NAGIOS_VRML_IMAGE);
		System.out.printf("}\n");
		}
	System.out.printf("}\n");
	System.out.printf("geometry Box{\n");
	System.out.printf("size %2.2f %2.2f %2.2f\n",node_width*3.0,node_width*3.0,node_width*3.0);
	System.out.printf("}\n");
	System.out.printf("}\n");

	System.out.printf("]\n");
	System.out.printf("}\n");

	System.out.printf("]\n");
	System.out.printf("description \"View Nagios Process Information\"\n");
	System.out.printf("url \"%s?type=%d\"\n",cgiutils_h.EXTINFO_CGI,cgiutils_h.DISPLAY_PROCESS_INFO);
	System.out.printf("}\n");


	if(use_links==common_h.FALSE)
		return;

	/* draw links to immediate child hosts */
	for(objects_h.hostextinfo child_hostextinfo : (ArrayList<objects_h.hostextinfo>) objects.hostextinfo_list ){

		if(child_hostextinfo.have_3d_coords==common_h.FALSE)
			continue;

		child_host=objects.find_host(child_hostextinfo.host_name);
		if(child_host==null)
			continue;

		/* check authorization */
		if(cgiauth.is_authorized_for_host(child_host,current_authdata)==common_h.FALSE)
			continue;

		/* draw a link to the host */
		if(objects.is_host_immediate_child_of_host(null,child_host)==common_h.TRUE)
			draw_host_link(null,nagios_icon_x,nagios_icon_y,0.0,child_hostextinfo.x_3d,child_hostextinfo.y_3d,child_hostextinfo.z_3d);
	        }

	return;
        }




/******************************************************************/
/***************** COORDINATE CALCULATION FUNCTIONS ***************/
/******************************************************************/

/* calculates coords of a host's children - used by balanced tree layout method */
public static void calculate_balanced_tree_coords(objects_h.host parent, int x, int y){
	int parent_drawing_width;
	int start_drawing_x;
	int current_drawing_x;
	int this_drawing_width;
	objects_h.hostextinfo temp_hostextinfo;

	/* calculate total drawing width of parent host */
	parent_drawing_width=max_child_host_drawing_width(parent);

	/* calculate starting x coord */
	start_drawing_x=  x- (int) (((DEFAULT_NODE_WIDTH*parent_drawing_width)+(DEFAULT_NODE_HSPACING*(parent_drawing_width-1)))/2);
	current_drawing_x=start_drawing_x;


	/* calculate coords for children */
	for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo==null)
			continue;

		if( objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

			/* get drawing width of child host */
			this_drawing_width=max_child_host_drawing_width(temp_host);

			temp_hostextinfo.x_3d=current_drawing_x+(((DEFAULT_NODE_WIDTH*this_drawing_width)+(DEFAULT_NODE_HSPACING*(this_drawing_width-1)))/2);
			temp_hostextinfo.y_3d=y+DEFAULT_NODE_HEIGHT+DEFAULT_NODE_VSPACING;
			temp_hostextinfo.have_3d_coords=common_h.TRUE;
			temp_hostextinfo.should_be_drawn=common_h.TRUE;
			
			current_drawing_x+=(this_drawing_width*DEFAULT_NODE_WIDTH)+((this_drawing_width-1)*DEFAULT_NODE_HSPACING)+DEFAULT_NODE_HSPACING;

			/* recurse into child host ... */
			calculate_balanced_tree_coords(temp_host,(int) temp_hostextinfo.x_3d, (int) temp_hostextinfo.y_3d);
		        }

	        }

	return;
        }


/* calculate coords of all hosts in circular layout method */
public static void calculate_circular_coords(){

	/* calculate all host coords, starting with first layer */
	calculate_circular_layer_coords(null,0.0,360.0,1, (int) CIRCULAR_DRAWING_RADIUS);

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
//	objects_h.host temp_host;
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
	for(objects_h.host temp_host : (ArrayList<objects_h.host>) objects.host_list ){

		temp_hostextinfo=objects.find_hostextinfo(temp_host.name);
		if(temp_hostextinfo==null)
			continue;

		if( objects.is_host_immediate_child_of_host(parent,temp_host)==common_h.TRUE){

			/* get drawing width of child host */
			this_drawing_width=max_child_host_drawing_width(temp_host);

			/* calculate angle this host gets for drawing */
			available_angle=useable_angle*((double)this_drawing_width/(double)parent_drawing_width);

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
			x_coord=-(Math.sin(-this_drawing_angle*(Math.PI/180.0))*radius);
			y_coord=-(Math.sin((90+this_drawing_angle)*(Math.PI/180.0))*radius);

			temp_hostextinfo.x_3d=(int)x_coord;
			temp_hostextinfo.y_3d=(int)y_coord;
			temp_hostextinfo.have_3d_coords=common_h.TRUE;
			temp_hostextinfo.should_be_drawn=common_h.TRUE;

			/* recurse into child host ... */
			calculate_circular_layer_coords(temp_host,current_drawing_angle+((available_angle-clipped_available_angle)/2),clipped_available_angle,layer+1,radius+ (int) CIRCULAR_DRAWING_RADIUS);

			/* increment current drawing angle */
			current_drawing_angle+=available_angle;
		        }
	        }

	return;
        }

public static int atoi(String value) {
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