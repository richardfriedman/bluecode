package com.arjuna.blue.bluefrontend.faces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimePeriod extends BlueObject
{

	private String name = "";
	private String alias = "";
	private String sundayStart = "";
	private String sundayEnd = "";
	private String mondayStart = "";
	private String mondayEnd = "";
	private String tuesdayStart = "";
	private String tuesdayEnd = "";
	private String wednesdayStart = "";
	private String wednesdayEnd = "";
	private String thursdayStart = "";
	private String thursdayEnd = "";
	private String fridayStart = "";
	private String fridayEnd = "";
	private String saturdayStart = "";
	private String saturdayEnd = "";
	
	public TimePeriod()
	{
		
	}
	
	public TimePeriod(TimePeriod timePeriod)
	{
		super(timePeriod);
		this.name = timePeriod.name;
		this.alias  = timePeriod.alias;
		this.sundayStart = timePeriod.sundayStart;
		this.sundayEnd = timePeriod.sundayEnd;
		this.mondayStart = timePeriod.mondayStart;
		this.mondayEnd = timePeriod.mondayEnd;
		this.tuesdayStart = timePeriod.tuesdayStart;
		this.tuesdayEnd	= timePeriod.tuesdayEnd;
		this.wednesdayStart = timePeriod.wednesdayStart;
		this.wednesdayEnd = timePeriod.wednesdayEnd;
		this.thursdayStart = timePeriod.thursdayStart;
		this.thursdayEnd = timePeriod.thursdayEnd;
		this.fridayStart = timePeriod.fridayStart;
		this.fridayEnd = timePeriod.fridayEnd;
		this.saturdayStart = timePeriod.saturdayStart;
		this.saturdayEnd = timePeriod.saturdayEnd;
		
	}
	
	public void setName(String timePeriodName)
	{
		this.name = timePeriodName;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	public void setSundayStart(String sundayStart)
	{
		this.sundayStart = sundayStart;
	}
	
	public String getSundayStart()
	{
		return this.sundayStart;
	}
	
	public void setSundayEnd(String sundayEnd)
	{
		this.sundayEnd = sundayEnd;
	}
	
	public String getSundayEnd()
	{
		return this.sundayEnd;
	}
	
	public void setMondayStart(String mondayStart)
	{
		this.mondayStart = mondayStart;
	}
	
	public String getMondayStart()
	{
		return this.mondayStart;
	}
	
	public void setMondayEnd(String mondayEnd)
	{
		this.mondayEnd = mondayEnd;
	}
	
	public String getMondayEnd()
	{
		return this.mondayEnd;
	}
	
	public void setTuesdayStart(String tuesdayStart)
	{
		this.tuesdayStart = tuesdayStart;
	}
	
	public String getTuesdayStart()
	{
		return this.tuesdayStart;
	}
	
	public void setTuesdayEnd(String tuesdayEnd)
	{
		this.tuesdayEnd = tuesdayEnd;
	}
	
	public String getTuesdayEnd()
	{
		return this.tuesdayEnd;
	}
	
	public void setWednesdayStart(String wednesdayStart)
	{
		this.wednesdayStart = wednesdayStart;
	}
	
	public String getWednesdayStart()
	{
		return this.wednesdayStart;
	}
	
	public void setWednesdayEnd(String wednesdayEnd)
	{
		this.wednesdayEnd = wednesdayEnd;
	}
	
	public String getWednesdayEnd()
	{
		return this.wednesdayEnd;
	}

	public void setThursdayStart(String thursdayStart)
	{
		this.thursdayStart = thursdayStart;
	}
	
	public String getThursdayStart()
	{
		return this.thursdayStart;
	}
	
	public void setThursdayEnd(String thursdayEnd)
	{
		this.thursdayEnd = thursdayEnd;
	}
	
	public String getThursdayEnd()
	{
		return this.thursdayEnd;
	}
	
	public void setFridayStart(String fridayStart)
	{
		this.fridayStart = fridayStart;
	}
	
	public String getFridayStart()
	{
		return this.fridayStart;
	}
	
	public String getFridayEnd()
	{
		return this.fridayEnd;
	}
	
	public void setFridayEnd(String fridayEnd)
	{
		this.fridayEnd = fridayEnd;
	}
	
	public void setSaturdayStart(String saturdayStart)
	{
		this.saturdayStart = saturdayStart;
	}
	
	public String getSaturdayStart()
	{
		return this.saturdayStart;
	}
	
	public void setSaturdayEnd(String saturdayEnd)
	{
		this.saturdayEnd = saturdayEnd;
	}
	
	public String getSaturdayEnd()
	{
		return this.saturdayEnd;
	}
	
	public HashMap<String,String> getObjectMapDetails()
	{
		HashMap<String,String> details = new HashMap<String,String>();
		details.put("id",String.valueOf(getId()));
		details.put("timeperiod_name",getName());
		details.put("alias",getAlias());
		if(dayHasValues(getSundayStart(),getSundayEnd()))
		{
			details.put("sunday",getSundayStart() + "-" + getSundayEnd());
		}
		
		if(dayHasValues(getMondayStart(),getMondayEnd()))
		{
			details.put("monday",getMondayStart() + "-" + getMondayEnd());
		}
		
		if(dayHasValues(getTuesdayStart(),getTuesdayEnd()))
		{
			details.put("tuesday",getTuesdayStart() + "-" + getTuesdayEnd());
		}
		
		if(dayHasValues(getWednesdayStart(),getWednesdayEnd()))
		{
			details.put("wednesday",getWednesdayStart() + "-" + getWednesdayEnd());
		}
		
		if(dayHasValues(getThursdayStart(),getThursdayEnd()))
		{
			details.put("thursday",getThursdayStart() + "-" + getThursdayEnd());
		}
		
		if(dayHasValues(getFridayStart(),getFridayEnd()))
		{
			details.put("friday",getFridayStart() + "-" + getFridayEnd());
		}
		
		if(dayHasValues(getSaturdayStart(),getSaturdayEnd()))
		{
			details.put("saturday",getSaturdayStart() + "-" + getSaturdayEnd());
		}
		
		return details;
	}
	
	
	public List<String> getObjectDetails()
	{
		List<String> details = new ArrayList<String>();
		
		details.add(String.valueOf(getId()));
		details.add("timeperiod_name");details.add(getName());
		details.add("alias");details.add(getAlias());
		if(dayHasValues(getSundayStart(),getSundayEnd()))
		{
			details.add("sunday");details.add(getSundayStart() + "-" + getSundayEnd());
		}
		
		if(dayHasValues(getMondayStart(),getMondayEnd()))
		{
			details.add("monday");details.add(getMondayStart() + "-" + getMondayEnd());
		}
		
		if(dayHasValues(getTuesdayStart(),getTuesdayEnd()))
		{
			details.add("tuesday");details.add(getTuesdayStart() + "-" + getTuesdayEnd());
		}
	
		if(dayHasValues(getWednesdayStart(),getWednesdayEnd()))
		{
			details.add("wednesday");details.add(getWednesdayStart() + "-" + getWednesdayEnd());
		}
		
		if(dayHasValues(getThursdayStart(),getThursdayEnd()))
		{
			details.add("thursday");details.add(getThursdayStart() + "-" + getThursdayEnd());
		}
		
		if(dayHasValues(getFridayStart(),getFridayEnd()))
		{
			details.add("friday");details.add(getFridayStart() + "-" + getFridayEnd());
		}
		
		if(dayHasValues(getSaturdayStart(),getSaturdayEnd()))
		{
			details.add("saturday");details.add(getSaturdayStart() + "-" + getSaturdayEnd());
		}
		
		return details;
	}
	
	private boolean dayHasValues(String start,String end)
	{
		if(start == null || start.equals("") || end == null || end.equals(""))
			return false;
				
		return true;
	}
}
