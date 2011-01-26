package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HostExtInfo extends BlueObject
{
	private String hostname = "";
	private String notes = "";
	private String notesURL = "";
	private String actionURL = "";
	private String iconImage= "";
	private String iconImageAlt = "";
	private String vrmlImage = "";
	private String statusMapImage = "";
	private double twodX = 0.0;
	private double twodY = 0.0;
	private double threedX = 0.0;
	private double threedY = 0.0;
	private double threedZ = 0.0;
		
	public HostExtInfo()
	{
		
	}
	
	public HostExtInfo(HostExtInfo hei)
	{
		super(hei);
		this.hostname = hei.hostname;
		this.notes = hei.notes;
		this.notesURL = hei.notesURL;
		this.actionURL = hei.actionURL;
		this.iconImage = hei.iconImage;
		this.iconImageAlt = hei.iconImageAlt;
		this.vrmlImage = hei.vrmlImage;
		this.statusMapImage = hei.statusMapImage;
		this.twodX = hei.twodX;
		this.twodY = hei.twodY;
		this.threedX = hei.threedX;
		this.threedY = hei.threedY;
		this.threedZ = hei.threedZ;
	}
	
	public String getName()
	{
		return this.hostname;
	}
	
	public String getActionURL() 
	{
		return this.actionURL;
	}

	public void setActionURL(String actionURL) 
	{
		this.actionURL = actionURL;
	}

	public String getHostname() 
	{
		return this.hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getIconImage()
	{
		return this.iconImage;
	}

	public void setIconImage(String iconImage)
	{
		this.iconImage = iconImage;
	}

	public String getIconImageAlt() 
	{
		return this.iconImageAlt;
	}

	public void setIconImageAlt(String iconImageAlt) 
	{
		this.iconImageAlt = iconImageAlt;
	}

	public String getNotes() 
	{
		return this.notes;
	}

	public void setNotes(String notes) 
	{
		this.notes = notes;
	}

	public String getNotesURL() 
	{
		return this.notesURL;
	}

	public void setNotesURL(String notesURL)
	{
		this.notesURL = notesURL;
	}

	public String getStatusMapImage()
	{
		return this.statusMapImage;
	}

	public void setStatusMapImage(String statusMapImage)
	{
		this.statusMapImage = statusMapImage;
	}

	public double getThreedX() 
	{
		return this.threedX;
	}

	public void setThreedX(double threedX) 
	{
		this.threedX = threedX;
	}

	public double getThreedY() 
	{
		return this.threedY;
	}

	public void setThreedY(double threedY) 
	{
		this.threedY = threedY;
	}

	public double getThreedZ()
	{
		return this.threedZ;
	}

	public void setThreedZ(double threedZ)
	{
		this.threedZ = threedZ;
	}

	public double getTwodX() 
	{
		return this.twodX;
	}

	public void setTwodX(double twodX) 
	{
		this.twodX = twodX;
	}

	public double getTwodY()
	{
		return this.twodY;
	}

	public void setTwodY(double twodY)
	{
		this.twodY = twodY;
	}

	public String getVrmlImage()
	{
		return this.vrmlImage;
	}

	public void setVrmlImage(String vrmlImage)
	{
		this.vrmlImage = vrmlImage;
	}
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("host_name");details.add(getHostname());
		details.add("notes");details.add(getNotes());
		details.add("notes_url");details.add(getNotesURL());
		details.add("action_url");details.add(getActionURL());
		details.add("icon_image");details.add(getIconImage());
		details.add("icon_image_alt");details.add(getIconImageAlt());
		details.add("vrml_image");details.add(getVrmlImage());
		details.add("statusmap_image");details.add(getStatusMapImage());
		details.add("twod_coords");details.add(getTwodX() + "," + getTwodY());
		details.add("threed_coords");details.add(getThreedX() + "," + getThreedY() + "," + getThreedZ());
		
		return details;
	}
	
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("host_name",getHostname());
		details.put("notes",getNotes());
		details.put("notes_url",getNotesURL());
		details.put("action_url",getActionURL());
		details.put("icon_image",getIconImage());
		details.put("icon_image_alt",getIconImageAlt());
		details.put("vrml_image",getVrmlImage());
		details.put("statusmap_image",getStatusMapImage());
		details.put("twod_coords",getTwodX() + "," + getTwodY());
		details.put("threed_coords",getThreedX() + "," + getThreedY() + "," + getThreedZ());
		
		return details;
	}
	
}
