package org.blue.star.plugins;

import java.text.DecimalFormat;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class check_memory extends check_base {

	private Sigar sigar;
	private double warning = -1;
	private double critical = -1;
	private String report = "K";
	private double totalMem;
	private double actualFree;
	private double actualUsed;
		
	public static void main(String[] args)
	{
		new check_memory().process_request(args);
	}
	
	/* Get Author Method */
	protected String getAuthor() {
		return "Rob Blake, Rob.Blake@arjuna.com";
	}

	/* Get Copyright Method */
	protected String getCopyright() {
		return "Arjuna Technologies 2007";
	}

	/* Get Description Method */
	protected String getDescription() {
		return "\nThis Plugin is used to Report on Memory Usage Statistics for a Host\n";
	}

	/* Get Notes Method */
	protected String getNotes() {
		return "\nThis Plugin is used to gather memory usage of a particular Host. It will report the "+
		"memory usage statistics in either kilobytes,megabytes or gigabytes dependent on your requirements." +
		"\nThe warning and critical thresholds are used to set the amount of memory that should remain free. Should "+
		"you not specify a reporting type, this defaults to kilobytes. If you specify a reporting type, it will "+
		"compare the memory free in the units of the report format";
	}
	
	/* Init Command Method */
	public void init_command(){
	}

	
	/* Add Command Arguments Method */
	public void add_command_arguments(Options options) {
		
		Option w = new Option("w","Warning",true,"The Warning threshold");
		w.setArgName("Warning Threshold");
		options.addOption(w);
		
		Option c = new Option("c","Critical",true,"The Critical Threshold");
		c.setArgName("Critical Threshold");
		options.addOption(c);
		
		Option R = new Option("R","Report",true,"The Memory Report Format (K|M|G)");
		R.setArgName("Memory Report Format");
		options.addOption(R);
	}

	/* Process command option method */
	public void process_command_option(Option o) throws IllegalArgumentException 
	{
		String argValue = o.getValue().trim();
		
		switch(o.getId())
		{
			case 'w':
					if(!utils.is_nonnegative(argValue))
						throw new IllegalArgumentException("Warning Threshold must be a Positive Number");
			
					warning = Double.valueOf(argValue);
				break;
			
			case 'c':
					if(!utils.is_nonnegative(argValue))
						throw new IllegalArgumentException("Critical Threshold must be a Positive Number");
					
					critical = Double.valueOf(argValue);
					break;
				
			case 'R':
				if(!checkValidReportFormat(argValue))
					throw new IllegalArgumentException("Valid Report Formats are: K -> kilobytes, M -> megabytes, G -> Gigabytes.");
				
				report = argValue;
		}
		
	}
	
	/* Process command arguments method */
	public void process_command_arguments(String[] argv) throws IllegalArgumentException {
	}
	
	/* Validate command arguments method */
	public void validate_command_arguments() throws IllegalArgumentException {
		
		if(warning == -1 || critical == -1)
		{
			throw new IllegalArgumentException("You must set both a Warning and Critical Threshold.");
		}
		
		if(warning <= critical)
		{
			throw new IllegalArgumentException("Warning Threshold must be greater than Critical Threshold");
		}
	}

	/* Execute check method */
	public boolean execute_check() {
		
		sigar = new Sigar();
		
		try
		{
			Mem memory = sigar.getMem();
			totalMem = memory.getTotal();
			actualFree = memory.getActualFree();
			actualUsed = memory.getActualUsed();
		}
		catch(SigarException e)
		{
			totalMem = -1;
			actualFree = -1;
			actualUsed = -1;
			return false;
		}
		return true;
	}

	/* Check Message method */
	public String check_message() {
		
		if(this.getMemoryInByteFormat(actualFree) > warning && actualFree != -1)
			return "OK - " + this.getGenericMessage();
		else if(this.getMemoryInByteFormat(actualFree) <= warning && this.getMemoryInByteFormat(actualFree) > critical && actualFree != -1)
			return "Warning - " + this.getGenericMessage();
		else if(this.getMemoryInByteFormat(actualFree) <= critical && actualFree != -1)
			return "Critical - " + this.getGenericMessage();
		
		return "Error Retrieving Memory Measurements";
			
	}

	/* Check state method */
	public int check_state() {
		
		if(this.getMemoryInByteFormat(actualFree) > warning && actualFree != -1)
			return common_h.STATE_OK;
		else if(this.getMemoryInByteFormat(actualFree) <= warning && this.getMemoryInByteFormat(actualFree) > critical && actualFree != -1)
			return common_h.STATE_WARNING;
		else if(this.getMemoryInByteFormat(actualFree) <= critical && actualFree != -1)
			return common_h.STATE_CRITICAL;
		
		return common_h.STATE_UNKNOWN;
	}

	private String getGenericMessage()
	{
		return "Memory Free = " + getMemoryInReportFormat(actualFree) + "|" + getMemoryInReportFormat(actualFree) + "," + getMemoryInReportFormat(actualUsed) + "," + getMemoryInReportFormat(totalMem);
	}
	
	/**
	 * Method used to verify that the user has selected a valid reporting format.
	 * @param reportFormat - The report format requested by the user.
	 * @return - boolean, true if the report format is a valid one. 
	 */
	private boolean checkValidReportFormat(String reportFormat)
	{
		return reportFormat.equalsIgnoreCase("k") || reportFormat.equalsIgnoreCase("m") || reportFormat.equalsIgnoreCase("g");
	}
	
	/**
	 * This method is used to return our memory measurements in the requested report format.
	 * 
	 * @param memory - our memory measurement.
	 * @return - String, the memory in report format.
	 */
	private String getMemoryInReportFormat(double memory)
	{
		DecimalFormat form = new DecimalFormat();
		form.setMaximumFractionDigits(2);
		form.setGroupingUsed(false);
		
		if(report.equalsIgnoreCase("k"))
			return form.format(this.getMemoryInByteFormat(memory)) + "kb";
		else if(report.equalsIgnoreCase("m"))
			return form.format(this.getMemoryInByteFormat(memory)) + "mb";
		else if(report.equalsIgnoreCase("g"))
			return form.format(this.getMemoryInByteFormat(memory)) + "gb";
				
		return "Error calculating memory usage";
	}
	
	/**
	 * This method returns our memory calculation in the given format dependent on our
	 * reporting method.
	 * 
	 * @return - Double, the memory in our given reporting method.
	 */
	private double getMemoryInByteFormat(double memory)
	{
		if(report.equalsIgnoreCase("k"))
			return memory/1024;
		else if(report.equalsIgnoreCase("m"))
			return memory/1024/1024;
		else if(report.equalsIgnoreCase("g"))
			return memory/1024/1024/1024;
		
		return -1;
	}
}