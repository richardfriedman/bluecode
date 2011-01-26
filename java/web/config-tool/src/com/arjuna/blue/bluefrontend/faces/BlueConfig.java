package com.arjuna.blue.bluefrontend.faces;

import java.util.Properties;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;

/*
 * Class used to represent the main Blue Configuration File!
 */

public class BlueConfig 
{
	private BlueConfigXMLFileStore fileStore;
	
	private String logFile = "";
	private String[] objectConfigurationFiles;
	private String objectCacheFile = "";
	private String resourceFile = "";
	private String tempFile = "";
	private String statusFile = "";
	private boolean aggregateStatusUpdates = true;
	private int statusUpdateInterval = 15;
	private String blueUser = "nagios";
	private String blueGroup = "nagios";
	private boolean enableNotifications = true;
	private boolean executeServiceChecks = true;
	private boolean acceptPassiveServiceChecks = false;
	private boolean executeHostChecks = true;
	private boolean acceptPassiveHostChecks = false;
	private boolean enableEventHandlers = true;
	private String logRotationMethod = "n";
	private String logArchivePath = "";
	private boolean checkExternalCommands = false;
	private String commandCheckInterval = "30s";
	private String commandFile = "";
	private String downtimeFile = "";
	private String commentFile = "";
	private String lockFile = "";
	private boolean retainStateInformation = true;
	private String stateRetentionFile = "";
	private int retentionUpdateInterval = 60;
	private boolean useRetainedProgramState = false;
	private boolean useRetainedSchedulingInfo = false;
	private boolean useSyslog = false;
	private boolean logNotifications = true;
	private boolean logServiceRetries = true;
	private boolean logHostRetries = true;
	private boolean logEventHandlers = true;
	private boolean logInitialStates = true;
	private boolean logExternalCommands = true;
	private boolean logPassiveChecks = true;
	private String	globalHostEventHandler = "";
	private String	globalServiceEventHandler = "";
	private double sleepTime = 1.0;
	private String serviceInterCheckDelayMethod = "s";
	private int maxServiceCheckSpread = 30;
	private String serviceInterleaveFactor = "s";
	private int maxConcurrentChecks = 20;
	private int serviceReaperFrequency = 10;
	private String hostInterCheckDelayMethod = "s";
	private int maxHostCheckSpread = 30;
	private int intervalLength = 60;
	private boolean autoRescheduleChecks = false;
	private int autoReschedulingInterval = 30;
	private int autoReschedulingWindow = 180;
	private boolean useAggressiveHostChecking = false;
	private boolean enableFlapDetection = true;
	private double lowServiceFlapThreshold = 25.0;
	private double highServiceFlapThreshold = 50.0;
	private double lowHostFlapThreshold = 25.0;
	private double highHostFlapThreshold = 75.0;
	private boolean softStateDependencies = false;
	private int serviceCheckTimeout = 60;
	private int hostCheckTimeout = 60;
	private int eventHandlerTimeout =60;
	private int notificationTimeout =60;
	private int ocspTimeout =60;
	private int ochpTimeout =60;
	private int perfdataTimeout =60;
	private boolean obsessOverServices = false;
	private String ocspCommand = "";
	private boolean obsessOverHosts = false;
	private String ochpCommand = "";
	private boolean processPerformanceData = false;
	private String hostPerfdataCommand = "";
	private String servicePerfdataCommand = "";
	private String hostPerfdataFile = "";
	private String servicePerfdataFile ="";
	private String hostPerfdataFileTemplate = "[HOSTPERFDATA]\t$TIMET$\t$HOSTNAME$\t$HOSTEXECUTIONTIME$\t$HOSTOUTPUT$\t$HOSTPERFDATA$";
	private String servicePerfdataFileTemplate = "[SERVICEPERFDATA]\t$TIMET$\t$HOSTNAME$\t$SERVICEDESC$\t$SERVICEEXECUTIONTIME$\t$SERVICELATENCY$\t$SERVICEOUTPUT$\t$SERVICEPERFDATA$";
	private String hostPerfdataFileMode = "a";
	private String servicePerfdataFileMode = "a";
	private int hostPerfdataFileProcessingInterval = 0;
	private int servicePerfdataFileProcessingInterval = 0;
	private String hostPerfdataFileProcessingCommand = "";
	private String servicePerfdataFileProcessingCommand = "";
	private boolean checkForOrphanedServices = false;
	private boolean checkServiceFreshness = true;
	private int serviceFreshnessCheckInterval = 60;
	private boolean checkHostFreshness = true;
	private int hostFreshnessCheckInterval = 60;
	private String dateFormat = "";
	private String illegalObjectNameChars = "'~!$%^&*\"|'<>?,()=";
	private String illegalMacroOutputChars = "~$^&\"|'<>";
	private boolean useRegexpMatching = false;
	private boolean useTrueRegexpMatching = false;
	private String adminEmail = "";
	private String adminPager = "";
	private Properties p;

	public BlueConfig()
	{
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.BLUECONFIG);
		updateDetails();
	}
	
	private void updateDetails()
	{
		fileStore.loadMainBlueConfig(this);
	}

	public boolean getAcceptPassiveHostChecks() {
		return acceptPassiveHostChecks;
	}

	public void setAcceptPassiveHostChecks(boolean acceptPassiveHostChecks) {
		this.acceptPassiveHostChecks = acceptPassiveHostChecks;
	}

	public boolean getAcceptPassiveServiceChecks() {
		return acceptPassiveServiceChecks;
	}

	public void setAcceptPassiveServiceChecks(boolean acceptPassiveServiceChecks) {
		this.acceptPassiveServiceChecks = acceptPassiveServiceChecks;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getAdminPager() {
		return adminPager;
	}

	public void setAdminPager(String adminPager) {
		
		this.adminPager = adminPager;
	}

	public boolean getAggregateStatusUpdates()
	{
		return aggregateStatusUpdates;
	}

	public void setAggregateStatusUpdates(boolean aggregatedStatusUpdates)
	{
		this.aggregateStatusUpdates = aggregatedStatusUpdates;
	}

	public boolean getAutoRescheduleChecks()
	{
		return autoRescheduleChecks;
	}

	public void setAutoRescheduleChecks(boolean autoRescheduleChecks)
	{
		this.autoRescheduleChecks = autoRescheduleChecks;
	}

	public int getAutoReschedulingInterval() {
		return autoReschedulingInterval;
	}

	public void setAutoReschedulingInterval(int autoReschedulingInterval) {
		this.autoReschedulingInterval = autoReschedulingInterval;
	}

	public int getAutoReschedulingWindow() {
		return autoReschedulingWindow;
	}

	public void setAutoReschedulingWindow(int autoReschedulingWindow) {
		this.autoReschedulingWindow = autoReschedulingWindow;
	}

	public String getBlueGroup() {
		return blueGroup;
	}

	public void setBlueGroup(String blueGroup) {
		this.blueGroup = blueGroup;
	}

	public String getBlueUser() {
		return blueUser;
	}

	public void setBlueUser(String blueUser) {
		this.blueUser = blueUser;
	}

	
	public boolean getCheckExternalCommands() {
		return checkExternalCommands;
	}

	public void setCheckExternalCommands(boolean checkExternalCommands) {
		this.checkExternalCommands = checkExternalCommands;
	}

	public boolean getCheckForOrphanedServices() {
		return checkForOrphanedServices;
	}

	public void setCheckForOrphanedServices(boolean checkForOrphanedServices) {
		this.checkForOrphanedServices = checkForOrphanedServices;
	}

	public boolean getCheckHostFreshness() {
		return checkHostFreshness;
	}

	public void setCheckHostFreshness(boolean checkHostFreshness) {
		this.checkHostFreshness = checkHostFreshness;
	}

	public boolean getCheckServiceFreshness() {
		return checkServiceFreshness;
	}

	public void setCheckServiceFreshness(boolean checkServiceFreshness) {
		this.checkServiceFreshness = checkServiceFreshness;
	}

	public String getCommandCheckInterval() {
		return commandCheckInterval;
	}

	public void setCommandCheckInterval(String commandCheckInterval) {
		this.commandCheckInterval = commandCheckInterval;
	}

	public String getCommandFile() {
		return commandFile;
	}

	public void setCommandFile(String commandFile) {
		this.commandFile = commandFile;
	}

	public String getCommentFile() {
		return commentFile;
	}

	public void setCommentFile(String commentFile) {
		this.commentFile = commentFile;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getDowntimeFile() {
		return downtimeFile;
	}

	public void setDowntimeFile(String downtimeFile) {
		this.downtimeFile = downtimeFile;
	}

	public boolean getEnableEventHandlers() {
		return enableEventHandlers;
	}

	public void setEnableEventHandlers(boolean enableEventHandlers) {
		this.enableEventHandlers = enableEventHandlers;
	}

	public boolean getEnableFlapDetection() {
		return enableFlapDetection;
	}

	public void setEnableFlapDetection(boolean enableFlapDetection) {
		this.enableFlapDetection = enableFlapDetection;
	}

	public boolean getEnableNotifications() {
		return enableNotifications;
	}

	public void setEnableNotifications(boolean enableNotifications) {
		this.enableNotifications = enableNotifications;
	}

	public int getEventHandlerTimeout() {
		return eventHandlerTimeout;
	}

	public void setEventHandlerTimeout(int eventHandlerTimeout) {
		this.eventHandlerTimeout = eventHandlerTimeout;
	}

	public boolean getExecuteHostChecks() {
		return executeHostChecks;
	}

	public void setExecuteHostChecks(boolean executeHostChecks) {
		this.executeHostChecks = executeHostChecks;
	}

	public boolean getExecuteServiceChecks() {
		return executeServiceChecks;
	}

	public void setExecuteServiceChecks(boolean executeServiceChecks) {
		this.executeServiceChecks = executeServiceChecks;
	}

	public String getGlobalHostEventHandler() {
		return globalHostEventHandler;
	}

	public void setGlobalHostEventHandler(String globalHostEventHandler) {
		this.globalHostEventHandler = globalHostEventHandler;
	}

	public String getGlobalServiceEventHandler() {
		return globalServiceEventHandler;
	}

	public void setGlobalServiceEventHandler(String globalServiceEventHandler) {
		this.globalServiceEventHandler = globalServiceEventHandler;
	}

	public double getHighHostFlapThreshold() {
		return highHostFlapThreshold;
	}

	public void setHighHostFlapThreshold(double highHostFlapThreshold) {
		
		if(highHostFlapThreshold > 100)
		{
			highHostFlapThreshold = 100.0;
		}
		
		if(highHostFlapThreshold < 0)
		{
			highHostFlapThreshold = 0;
		}
		
		this.highHostFlapThreshold = highHostFlapThreshold;
	}

	public double getHighServiceFlapThreshold() {
		return highServiceFlapThreshold;
	}

	public void setHighServiceFlapThreshold(double highServiceFlapThreshold) {
		
		if(highServiceFlapThreshold > 100)
		{
			highServiceFlapThreshold = 100.0;
		}
		
		if(highServiceFlapThreshold < 0)
		{
			highServiceFlapThreshold = 0;
		}
		this.highServiceFlapThreshold = highServiceFlapThreshold;
	}

	public int getHostCheckTimeout() {
		return hostCheckTimeout;
	}

	public void setHostCheckTimeout(int hostCheckTimeout) {
		this.hostCheckTimeout = hostCheckTimeout;
	}

	public int getHostFreshnessCheckInterval() {
		return hostFreshnessCheckInterval;
	}

	public void setHostFreshnessCheckInterval(int hostFreshnessCheckInterval) {
		this.hostFreshnessCheckInterval = hostFreshnessCheckInterval;
	}

	public String getHostInterCheckDelayMethod() {
		return hostInterCheckDelayMethod;
	}

	public void setHostInterCheckDelayMethod(String hostInterCheckDelayMethod) {
		this.hostInterCheckDelayMethod = hostInterCheckDelayMethod;
	}

	public String getHostPerfdataCommand() {
		return hostPerfdataCommand;
	}

	public void setHostPerfdataCommand(String hostPerfdataCommand) {
		this.hostPerfdataCommand = hostPerfdataCommand;
	}

	public String getHostPerfdataFile() {
		return hostPerfdataFile;
	}

	public void setHostPerfdataFile(String hostPerfdataFile) {
		this.hostPerfdataFile = hostPerfdataFile;
	}

	public String getHostPerfdataFileMode() {
		return hostPerfdataFileMode;
	}

	public void setHostPerfdataFileMode(String hostPerfdataFileMode) {
		this.hostPerfdataFileMode = hostPerfdataFileMode;
	}

	public String getHostPerfdataFileProcessingCommand() {
		return hostPerfdataFileProcessingCommand;
	}

	public void setHostPerfdataFileProcessingCommand(String hostPerfdataFileProcessingCommand)
	{
		this.hostPerfdataFileProcessingCommand = hostPerfdataFileProcessingCommand;
	}

	public int getHostPerfdataFileProcessingInterval()
	{
		return hostPerfdataFileProcessingInterval;
	}

	public void setHostPerfdataFileProcessingInterval(int hostPerfdataFileProcessingInterval)
	{
		this.hostPerfdataFileProcessingInterval = hostPerfdataFileProcessingInterval;
	}

	public String getHostPerfdataFileTemplate() {
		return hostPerfdataFileTemplate;
	}

	public void setHostPerfdataFileTemplate(String hostPerfdataFileTemplate) {
		this.hostPerfdataFileTemplate = hostPerfdataFileTemplate;
	}

	public String getIllegalMacroOutputChars() {
		return illegalMacroOutputChars;
	}

	public void setIllegalMacroOutputChars(String illegalMacroOutputChars) {
		this.illegalMacroOutputChars = illegalMacroOutputChars;
	}

	public String getIllegalObjectNameChars() {
		return illegalObjectNameChars;
	}

	public void setIllegalObjectNameChars(String illegalObjectNameChars) {
		this.illegalObjectNameChars = illegalObjectNameChars;
	}

	public int getIntervalLength() {
		return intervalLength;
	}

	public void setIntervalLength(int intervalLength) {
		this.intervalLength = intervalLength;
	}

	public String getLockFile() {
		return lockFile;
	}

	public void setLockFile(String lockFile) {
		this.lockFile = lockFile;
	}

	public String getLogArchivePath() {
		return logArchivePath;
	}

	public void setLogArchivePath(String logArchivePath) {
		this.logArchivePath = logArchivePath;
	}

	public boolean getLogEventHandlers() {
		return logEventHandlers;
	}

	public void setLogEventHandlers(boolean logEventHandlers) {
		this.logEventHandlers = logEventHandlers;
	}

	public boolean getLogExternalCommands() {
		return logExternalCommands;
	}

	public void setLogExternalCommands(boolean logExternalCommands) {
		this.logExternalCommands = logExternalCommands;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public boolean getLogHostRetries() {
		return logHostRetries;
	}

	public void setLogHostRetries(boolean logHostRetries) {
		this.logHostRetries = logHostRetries;
	}

	public boolean getLogInitialStates() {
		return logInitialStates;
	}

	public void setLogInitialStates(boolean logInitialStates) {
		this.logInitialStates = logInitialStates;
	}

	public boolean getLogNotifications() {
		return logNotifications;
	}

	public void setLogNotifications(boolean logNotifications) {
		this.logNotifications = logNotifications;
	}

	public boolean getLogPassiveChecks() {
		return logPassiveChecks;
	}

	public void setLogPassiveChecks(boolean logPassiveChecks) {
		this.logPassiveChecks = logPassiveChecks;
	}

	public String getLogRotationMethod() {
		return logRotationMethod;
	}

	public void setLogRotationMethod(String logRotationMethod) {
		this.logRotationMethod = logRotationMethod;
	}

	public boolean getLogServiceRetries() {
		return logServiceRetries;
	}

	public void setLogServiceRetries(boolean logServiceRetries) {
		this.logServiceRetries = logServiceRetries;
	}

	public double getLowHostFlapThreshold() {
		return lowHostFlapThreshold;
	}

	public void setLowHostFlapThreshold(double lowHostFlapThreshold) {
		
		if(lowHostFlapThreshold > 100)
		{
			lowHostFlapThreshold = 100.0;
		}
		
		if(lowHostFlapThreshold < 0)
		{
			lowHostFlapThreshold = 0;
		}
		
		this.lowHostFlapThreshold = lowHostFlapThreshold;
	}

	public double getLowServiceFlapThreshold() {
		return lowServiceFlapThreshold;
	}

	public void setLowServiceFlapThreshold(double lowServiceFlapThreshold) {
		
		if(lowServiceFlapThreshold > 100)
		{
			lowServiceFlapThreshold = 100.0;
		}
		
		if(lowServiceFlapThreshold < 0)
		{
			lowServiceFlapThreshold = 0;
		}
		
		this.lowServiceFlapThreshold = lowServiceFlapThreshold;
	}

	public int getMaxConcurrentChecks() {
		return maxConcurrentChecks;
	}

	public void setMaxConcurrentChecks(int maxConcurrentChecks) {
		this.maxConcurrentChecks = maxConcurrentChecks;
	}

	public int getMaxHostCheckSpread() {
		return maxHostCheckSpread;
	}

	public void setMaxHostCheckSpread(int maxHostCheckSpread) {
		this.maxHostCheckSpread = maxHostCheckSpread;
	}

	public int getMaxServiceCheckSpread() {
		return maxServiceCheckSpread;
	}

	public void setMaxServiceCheckSpread(int maxServiceCheckSpread) {
		this.maxServiceCheckSpread = maxServiceCheckSpread;
	}

	public int getNotificationTimeout() {
		return notificationTimeout;
	}

	public void setNotificationTimeout(int notificationTimeout) {
		this.notificationTimeout = notificationTimeout;
	}

	public String getObjectCacheFile() {
		return objectCacheFile;
	}

	public void setObjectCacheFile(String objectCacheFile) {
		this.objectCacheFile = objectCacheFile;
	}

	public String[] getObjectConfigurationFiles() {
		return objectConfigurationFiles;
	}

	public void setObjectConfigurationFiles(String[] objectConfigurationFiles) {
		this.objectConfigurationFiles = objectConfigurationFiles;
	}

	public boolean getObsessOverHosts() {
		return obsessOverHosts;
	}

	public void setObsessOverHosts(boolean obsessOverHosts) {
		this.obsessOverHosts = obsessOverHosts;
	}

	public boolean getObsessOverServices() {
		return obsessOverServices;
	}

	public void setObsessOverServices(boolean obsessOverServices) {
		this.obsessOverServices = obsessOverServices;
	}

	public String getOchpCommand() {
		return ochpCommand;
	}

	public void setOchpCommand(String ochpCommand) {
		this.ochpCommand = ochpCommand;
	}

	public int getOchpTimeout() {
		return ochpTimeout;
	}

	public void setOchpTimeout(int ochpTimeout) {
		this.ochpTimeout = ochpTimeout;
	}

	public String getOcspCommand() {
		return ocspCommand;
	}

	public void setOcspCommand(String ocspCommand) {
		this.ocspCommand = ocspCommand;
	}

	public int getOcspTimeout() {
		return ocspTimeout;
	}

	public void setOcspTimeout(int ocspTimeout) {
		this.ocspTimeout = ocspTimeout;
	}

	
	public int getPerfdataTimeout() {
		return perfdataTimeout;
	}

	public void setPerfdataTimeout(int perfdataTimeout) {
		this.perfdataTimeout = perfdataTimeout;
	}

	public boolean getProcessPerformanceData() {
		return processPerformanceData;
	}

	public void setProcessPerformanceData(boolean processPerformanceData) {
		this.processPerformanceData = processPerformanceData;
	}

	public int getRetentionUpdateInterval() {
		return retentionUpdateInterval;
	}

	public void setRetentionUpdateInterval(int rententionUpdateInterval) {
		this.retentionUpdateInterval = rententionUpdateInterval;
	}

	public String getResourceFile() {
		return resourceFile;
	}

	public void setResourceFile(String resourceFile) {
		this.resourceFile = resourceFile;
	}

	public boolean getRetainStateInformation() {
		return retainStateInformation;
	}

	public void setRetainStateInformation(boolean retainStateInformation) {
		this.retainStateInformation = retainStateInformation;
	}

	public int getServiceCheckTimeout() {
		return serviceCheckTimeout;
	}

	public void setServiceCheckTimeout(int serviceCheckTimeout) {
		this.serviceCheckTimeout = serviceCheckTimeout;
	}

	public int getServiceFreshnessCheckInterval() {
		return serviceFreshnessCheckInterval;
	}

	public void setServiceFreshnessCheckInterval(int serviceFreshnessCheckInterval) {
		this.serviceFreshnessCheckInterval = serviceFreshnessCheckInterval;
	}

	public String getServiceInterCheckDelayMethod() {
		return serviceInterCheckDelayMethod;
	}

	public void setServiceInterCheckDelayMethod(String serviceInterCheckDelayMethod) {
		this.serviceInterCheckDelayMethod = serviceInterCheckDelayMethod;
	}

	public String getServiceInterleaveFactor() {
		return serviceInterleaveFactor;
	}

	public void setServiceInterleaveFactor(String serviceInterleaveFactor) {
		this.serviceInterleaveFactor = serviceInterleaveFactor;
	}

	public String getServicePerfdataFileMode() {
		return servicePerfdataFileMode;
	}

	public void setServicePerfdataFileMode(String servicePerdataFileMode) {
		this.servicePerfdataFileMode = servicePerdataFileMode;
	}

	public String getServicePerfdataFileProcessingCommand() {
		return servicePerfdataFileProcessingCommand;
	}

	public void setServicePerfdataFileProcessingCommand(
			String servicePerfdataFileProcessingCommand) {
		this.servicePerfdataFileProcessingCommand = servicePerfdataFileProcessingCommand;
	}

	public String getServicePerfdataCommand() {
		return servicePerfdataCommand;
	}

	public void setServicePerfdataCommand(String servicePerfdataCommand) {
		this.servicePerfdataCommand = servicePerfdataCommand;
	}

	public String getServicePerfdataFile() {
		return servicePerfdataFile;
	}

	public void setServicePerfdataFile(String servicePerfdataFile) {
		this.servicePerfdataFile = servicePerfdataFile;
	}

	public int getServicePerfdataFileProcessingInterval() {
		return servicePerfdataFileProcessingInterval;
	}

	public void setServicePerfdataFileProcessingInterval(
			int servicePerfdataFileProcessingInterval) {
		this.servicePerfdataFileProcessingInterval = servicePerfdataFileProcessingInterval;
	}

	public String getServicePerfdataFileTemplate() {
		return servicePerfdataFileTemplate;
	}

	public void setServicePerfdataFileTemplate(String servicePerfdataFileTemplate) {
		this.servicePerfdataFileTemplate = servicePerfdataFileTemplate;
	}

	public int getServiceReaperFrequency() {
		return serviceReaperFrequency;
	}

	public void setServiceReaperFrequency(int serviceReaperFrequency) {
		this.serviceReaperFrequency = serviceReaperFrequency;
	}

	public double getSleepTime()
	{
		return sleepTime;
	}

	public void setSleepTime(double sleepTime)
	{
		this.sleepTime = sleepTime;
	}

	public boolean getSoftStateDependencies() {
		return softStateDependencies;
	}

	public void setSoftStateDependencies(boolean softStateDependencies) {
		this.softStateDependencies = softStateDependencies;
	}

	public String getStateRetentionFile() {
		return stateRetentionFile;
	}

	public void setStateRetentionFile(String stateRetentionFile) {
		this.stateRetentionFile = stateRetentionFile;
	}

	public String getStatusFile() {
		return statusFile;
	}

	public void setStatusFile(String statusFile) {
		this.statusFile = statusFile;
	}

	public int getStatusUpdateInterval() {
		return statusUpdateInterval;
	}

	public void setStatusUpdateInterval(int statusUpdateInterval) {
		this.statusUpdateInterval = statusUpdateInterval;
	}

	public String getTempFile() {
		return tempFile;
	}

	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}

	public boolean getUseAggressiveHostChecking() {
		return useAggressiveHostChecking;
	}

	public void setUseAggressiveHostChecking(boolean useAggressiveHostChecking) {
		this.useAggressiveHostChecking = useAggressiveHostChecking;
	}

	public boolean getUseRegexpMatching() {
		return useRegexpMatching;
	}

	public void setUseRegexpMatching(boolean useRegexpMatching) {
		this.useRegexpMatching = useRegexpMatching;
	}

	public boolean getUseRetainedProgramState() {
		return useRetainedProgramState;
	}

	public void setUseRetainedProgramState(boolean useRetainedProgramState) {
		this.useRetainedProgramState = useRetainedProgramState;
	}

	public boolean getUseRetainedSchedulingInfo() {
		return useRetainedSchedulingInfo;
	}

	public void setUseRetainedSchedulingInfo(boolean useRetainedSchedulingInfo) {
		this.useRetainedSchedulingInfo = useRetainedSchedulingInfo;
	}

	public boolean getUseSyslog() {
		return useSyslog;
	}

	public void setUseSyslog(boolean useSyslog) {
		this.useSyslog = useSyslog;
	}

	public boolean getUseTrueRegexpMatching() {
		return useTrueRegexpMatching;
	}

	public void setUseTrueRegexpMatching(boolean useTrueRegexpMatching) {
		this.useTrueRegexpMatching = useTrueRegexpMatching;
	}
		
	public Properties getBlueOptions()
	{
		p = new Properties();
		
		p.put("log_file",getLogFile());
		p.put("cfg_file","automatically_parsed!");
		p.put("object_cache_file",getObjectCacheFile());
		p.put("resource_file","/cfgs/resource.cfg");
		p.put("temp_file",getTempFile());
		p.put("status_file",getStatusFile());
		p.put("aggregate_status_updates",Utils.booleanToString(getAggregateStatusUpdates()));
		p.put("status_update_interval",getStatusUpdateInterval());
		p.put("nagios_user",getBlueUser());
		p.put("nagios_group",getBlueGroup());
		p.put("enable_notifications",Utils.booleanToString(getEnableNotifications()));
		p.put("execute_service_checks",Utils.booleanToString(getExecuteServiceChecks()));
		p.put("accept_passive_service_checks",Utils.booleanToString(getAcceptPassiveServiceChecks()));
		p.put("execute_host_checks",Utils.booleanToString(getExecuteHostChecks()));
		p.put("accept_passive_host_checks",Utils.booleanToString(getAcceptPassiveHostChecks()));
		p.put("enable_event_handlers",Utils.booleanToString(getEnableEventHandlers()));
		p.put("log_rotation_method",getLogRotationMethod());
		p.put("log_archive_path",getLogArchivePath());
		p.put("check_external_commands",Utils.booleanToString(getCheckExternalCommands()));
		p.put("command_check_interval",getCommandCheckInterval());
		p.put("command_file",getCommandFile());
		p.put("downtime_file",getDowntimeFile());
		p.put("comment_file",getCommentFile());
		p.put("lock_file",getLockFile());
		p.put("retain_state_information",Utils.booleanToString(getRetainStateInformation()));
		p.put("state_retention_file",getStateRetentionFile());
		p.put("retention_update_interval",getRetentionUpdateInterval());
		p.put("use_retained_program_state",Utils.booleanToString(getUseRetainedProgramState()));
		p.put("use_retained_scheduling_info",Utils.booleanToString(getUseRetainedSchedulingInfo()));
		p.put("use_syslog",Utils.booleanToString(getUseSyslog()));
		p.put("log_notifications",Utils.booleanToString(getLogNotifications()));
		p.put("log_service_retries",Utils.booleanToString(getLogServiceRetries()));
		p.put("log_host_retries",Utils.booleanToString(getLogHostRetries()));
		p.put("log_event_handlers",Utils.booleanToString(getLogEventHandlers()));
		p.put("log_initial_states",Utils.booleanToString(getLogInitialStates()));
		p.put("log_external_commands",Utils.booleanToString(getLogExternalCommands()));
		p.put("log_passive_checks",Utils.booleanToString(getLogPassiveChecks()));
		p.put("global_host_event_handler",getGlobalHostEventHandler());
		p.put("global_service_event_handler",getGlobalServiceEventHandler());
		p.put("sleep_time",getSleepTime());
		p.put("service_inter_check_delay_method",getServiceInterCheckDelayMethod());
		p.put("max_service_check_spread",getMaxServiceCheckSpread());
		p.put("service_interleave_factor",getServiceInterleaveFactor());
		p.put("max_concurrent_checks",getMaxConcurrentChecks());
		p.put("service_reaper_frequency",getServiceReaperFrequency());
		p.put("host_inter_check_delay_method",getHostInterCheckDelayMethod());
		p.put("max_host_check_spread",getMaxHostCheckSpread());
		p.put("interval_length",getIntervalLength());
		p.put("auto_reschedule_checks",Utils.booleanToString(getAutoRescheduleChecks()));
		p.put("auto_rescheduling_interval",getAutoReschedulingInterval());
		p.put("auto_rescheduling_window",getAutoReschedulingWindow());
		p.put("use_aggressive_host_checking",Utils.booleanToString(getUseAggressiveHostChecking()));
		p.put("enable_flap_detection",Utils.booleanToString(getEnableFlapDetection()));
		p.put("low_service_flap_threshold",getLowServiceFlapThreshold());
		p.put("high_service_flap_threshold",getHighServiceFlapThreshold());
		p.put("low_host_flap_threshold",getLowHostFlapThreshold());
		p.put("high_host_flap_threshold",getHighHostFlapThreshold());
		p.put("soft_state_dependencies",Utils.booleanToString(getSoftStateDependencies()));
		p.put("service_check_timeout",getServiceCheckTimeout());
		p.put("host_check_timeout",getHostCheckTimeout());
		p.put("event_handler_timeout",getEventHandlerTimeout());
		p.put("notification_timeout",getNotificationTimeout());
		p.put("ocsp_timeout",getOcspTimeout());
		p.put("ochp_timeout",getOchpTimeout());
		p.put("perfdata_timeout",getPerfdataTimeout());
		p.put("obsess_over_services",Utils.booleanToString(getObsessOverServices()));
		p.put("ocsp_command",getOcspCommand());
		p.put("obsess_over_hosts",Utils.booleanToString(getObsessOverHosts()));
		p.put("ochp_command",getOchpCommand());
		p.put("process_performance_data",Utils.booleanToString(getProcessPerformanceData()));
		p.put("host_perfdata_command",getHostPerfdataCommand());
		p.put("service_perfdata_command",getServicePerfdataCommand());
		p.put("host_perfdata_file",getHostPerfdataFile());
		p.put("service_perfdata_file",getServicePerfdataFile());
		p.put("host_perfdata_file_template",getHostPerfdataFileTemplate());
		p.put("service_perfdata_file_template",getServicePerfdataFileTemplate());
		p.put("host_perfdata_file_mode",getHostPerfdataFileMode());
		p.put("service_perfdata_file_mode",getServicePerfdataFileMode());
		p.put("host_perfdata_file_processing_interval",getHostPerfdataFileProcessingInterval());
		p.put("service_perfdata_file_processing_interval",getServicePerfdataFileProcessingInterval());
		p.put("host_perfdata_file_processing_command",getHostPerfdataFileProcessingCommand());
		p.put("service_perfdata_file_processing_command",getServicePerfdataFileProcessingCommand());
		p.put("check_for_orphaned_services",Utils.booleanToString(getCheckForOrphanedServices()));
		p.put("check_service_freshness",Utils.booleanToString(getCheckServiceFreshness()));
		p.put("service_freshness_check_interval",getServiceFreshnessCheckInterval());
		p.put("check_host_freshness",Utils.booleanToString(getCheckHostFreshness()));
		p.put("host_freshness_check_interval",getHostFreshnessCheckInterval());
		p.put("date_format",getDateFormat());
		p.put("illegal_object_name_chars",getIllegalObjectNameChars());
		p.put("illegal_macro_output_chars",getIllegalMacroOutputChars());
		p.put("use_regexp_matching",Utils.booleanToString(getUseRegexpMatching()));
		p.put("use_true_regexp_matching",Utils.booleanToString(getUseTrueRegexpMatching()));
		p.put("admin_email",getAdminEmail());
		p.put("admin_pager",getAdminPager());
		
		return p;
	}
		
}