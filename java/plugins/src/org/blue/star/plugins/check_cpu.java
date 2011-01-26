package org.blue.star.plugins;

import java.text.DecimalFormat;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * <p>This class represents a check_cpu plugin. It makes use of Hyperic Sigar to perform cross platform
 * CPU utilisation checking. It will return the amount of CPU utilisation in terms of System time, User
 * time, Wait Time and Idle Time.</p>
 * 
 * @author Rob.Blake@arjuna.com
 * @version 0.1
 * 
 * @see - <b>org.blue.star.plugins.check_base</b> - The class this plugin extends.
 *
 */
public class check_cpu extends check_base{

	/**
	 * Our sigar instance.
	 */
	private Sigar sigar;
	
	/**
	 * Our warning threshold.
	 */
	private double warning = -1;
	
	/**
	 * Our warning threshold.
	 */
	private double critical = -1;
	
	/**
	 * The number of cpus on this machine.
	 */
	private int cpuCount;
	
	/**
	 * Variables for recording our current CPU Times.
	 */
	private double waitTime;
	private double systemTime;
	private double userTime;
	private double idleTime;
	private double totalTime;
		
	/**
	 * Our main method that calls the execution of the plugin.
	 * @param args - Any command line arguments that should be passed to the plugin.
	 */
	public static void main(String[] args)
	{
		new check_cpu().process_request(args);
	}
	
	/* Get Author Method */
	protected String getAuthor() {
		return "Rob Blake, Rob.Blake@arjuna.com";
	}

	/* Get CopyRight Method */
	protected String getCopyright() {
		return "Arjuna Technologies 2007";
	}

	/* Get Description Method */
	protected String getDescription() {
		return "\nThis plugin is used to show the utilisation of a CPU on your machine. It will display System, User, Wait and Idle time statistics\n";
	}

	/* Get Notes Method */
	protected String getNotes() {
		return "\nThis plugin makes use of Hyperic Sigar to check CPU utilisation in a cross platform manner." +
		"The plugin returns the amount of CPU usage in terms of System time, Wait time, User time and Idle time as a percentage of CPU availability." + 
		"The PerfData for the plugin should be interpreted in that order.\n"+
		"If the machine that is being tested has more than one CPU, this plugin will take an average of the loads over the two CPUs\n\n"+
		"\nThe Warning and Critical thresholds represent the TOTAL cpu utilisation i.e. -w 70% -c 90% will send warning at 70% CPU "+
		"utilisation and critical at 90%.";
	}
	
	/* Init Command Method */
	public void init_command() {
	}
	
	/* Add Command Arguments Method */
	public void add_command_arguments(Options options)
	{
		Option c = new Option("c","Critical",true,"The Critical Threshold for this plugin in % (dd.d%)");
		c.setArgName("Critical Threshold");
		options.addOption(c);
		
		Option w = new Option("w","Warning",true,"The Warning Threshold for this plugin in % (dd.d%)");
		w.setArgName("Warning Threshold");
		options.addOption(w);
	}

	/* Process Command Option Method */
	public void process_command_option(Option o) throws IllegalArgumentException
	{
		String argValue = o.getValue();
		
		switch(o.getId())
		{
			case 'w':
				warning = this.setThreshold(argValue);
				break;
			
			case 'c':
				critical = this.setThreshold(argValue);
				break;
		}
	}
	
	/* Process command arguments method */
	public void process_command_arguments(String[] argv) throws IllegalArgumentException {
	}
	
	/* Validate Command Arguments Method */
	public void validate_command_arguments() throws IllegalArgumentException 
	{
		if(critical == -1 || warning == -1)
		{
			throw new IllegalArgumentException("You must set both a warning and critical threshold in the format DD.D%");
		}
		
		if(warning >= critical)
		{
			throw new IllegalArgumentException("Warning Threshold must be less than Critical Threshold");
		}
	}
	
	/* Execute Check Method */
	public boolean execute_check()
	{
		sigar = new Sigar();
		
		try
		{
			CpuPerc[] cpus = sigar.getCpuPercList();
			cpuCount = cpus.length;
			
			for(CpuPerc c: cpus)
			{
				addCpuPerformanceInfo(c);
			}
		}
		catch(SigarException e)
		{
			totalTime = -1;
			return false;
		}
		return true;
	}
	
	/* Check Message Method */
	public String check_message() {
		return this.setCheckMessage();
	}

	/* Check State Method */
	public int check_state() {
		return this.setCheckState();
	}

	
	/**
	 * This method adds any retrieved CPU utilisation information to the fields
	 * used to record the total CPU Times.
	 * 
	 * @param cpuInfo - The collected cpuInfo object.
	 */
	private void addCpuPerformanceInfo(CpuPerc cpuInfo)
	{
		if(cpuInfo == null)
			return;
		
		waitTime += cpuInfo.getWait()*100;
		systemTime += cpuInfo.getSys()*100;
		userTime += cpuInfo.getUser()*100;
		idleTime += cpuInfo.getIdle()*100;
		totalTime += cpuInfo.getCombined()*100;
	}

	/**
	 * This method is used to calculate the average percentages of CPU usage on
	 * this machine should it have 2 or more processors.
	 */
	private void calculatePercentages()
	{
		if(cpuCount == 1)
			return;
		
		waitTime = waitTime/cpuCount;
		systemTime = systemTime/cpuCount;
		userTime = userTime/cpuCount;
		idleTime = idleTime/cpuCount;
		totalTime = totalTime/cpuCount;
	}

	/**
	 * This method is used to set the check Message based around the given percentage thresholds.
	 * @return - String, our exit check message.
	 */
	private String setCheckMessage()
	{
		this.calculatePercentages();
		
		if(totalTime < warning && totalTime >= 0)
			return "OK - CPU Utilisation at " + this.getEndMessage();  
		else if(totalTime >= warning && totalTime < critical)
			return "Warning - CPU Utilisation at " + this.getEndMessage();
		else if(totalTime >= critical)
			return "Critical - CPU Utilisation at " + this.getEndMessage();
		
		return "Error: Unable to retrieve CPU Information";
	}
	
	/**
	 * This method provides us with our generic end message 
	 * @return - String, our generic end message.
	 */
	private String getEndMessage()
	{
		DecimalFormat f = new DecimalFormat();
		f.setMaximumFractionDigits(2);
		
		return f.format(totalTime) + "%|" + f.format(systemTime) + "%," + f.format(waitTime) + "%," + f.format(userTime) + "%," + f.format(idleTime) + "%";
	}
	
	/**
	 * This method is used to calculate our return code 
	 * @return - int, the return code of this plugin.
	 */
	private int setCheckState()
	{
		if(totalTime < warning && totalTime >= 0)
			return common_h.STATE_OK;
		else if(totalTime >=warning && totalTime < critical)
			return common_h.STATE_WARNING;
		else if(totalTime >= critical)
			return common_h.STATE_CRITICAL;
		
		return common_h.STATE_UNKNOWN;
	}
	
	/**
	 * This method is used to parse a percentage out of a String.
	 * @param argValue - The String to parse.
	 * @return - The percentage return value.
	 */
	private double setThreshold(String argValue)
	{
		if(argValue.endsWith("%"))
		{
			argValue = argValue.substring(0,argValue.length()-1);
		}
		
		try
		{
			return Double.valueOf(argValue);
		}
		catch(NumberFormatException e)
		{
			return -1;
		}
	}
}