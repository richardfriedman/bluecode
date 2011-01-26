<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>BLUE:: Open Source System and Network Monitoring</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<link href="styles/blue_layout.css" rel="stylesheet" type="text/css" />
<script src="scripts/prototype.js" type="text/javascript"></script>
<script src="scripts/scriptaculous.js" type="text/javascript"></script>

<script language="javascript">
function popUp(URL)
 {
	day = new Date();
	id = day.getTime();
	eval("page" + id + " = window.open(URL, '" + id + "', 'toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=0,width=450,height=600');");
}
</script>

</head>

<body><div id="content"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<div id="nav">
	<%@ include file="menu.html" %>
</div>
<div id="main">

<c:if test="${accountHandler.inWizard}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>A Time Period allows you to define a period in time in which notifications and monitoring results will be valid.</p>
<p>Time Periods also introduce the concept of templating. To save time in building your configuration, we have supplied a number of pre-determined Time Periods. To use these
simply select them from the Templates drop-down list and click on the 'Load' button. Once a template is loaded, click on the 'Save Time Period' button and you have added your first Time
Period. You can of course define your own Time Periods and save them as templates for future use by filling in the form and clicking on 'Save as Template'.</p>
<p>For the sake of this wizard, please select the 24x7 Time Period from the template list and save it as a Time Period. Should you want to know more about Time Periods, please
click the Help link.</p>
</div>
</c:if>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>

<h3>Add a new Time Period:<br/>
<a href="#" onclick="new Effect.toggle('search','slide',{duration:0.5})">(Search for existing Time Periods/</a>
<c:choose>
	<c:when test="${timePeriodHandler.templateCount == 0}">
		<span class="disabled">No Templates Available)</span></a>
	</c:when>
	<c:otherwise>
<a href="#"  onclick="new Effect.toggle('template','slide',{duration:0.5})">Use Template)</a>
	</c:otherwise>
</c:choose>
 <a href="javascript:popUp('timeperiodhelp.html')">Need Help? Click Here!</a>
</h3>
	<div id="search" style="display: none" class="dropbox">
		<h:form>
			<label for="tp_search">Time Period Name:</label>	<h:inputText size="20" id="tp_search" maxlength="30" value="#{searchHandler.searchString}" required="true"/><h:message for="host_search"/>
						<h:commandButton value="Search" action="#{searchHandler.searchObjects}" actionListener="#{searchHandler.setSearchType}">
							<f:attribute name="searchType" value="6"/>
	     				</h:commandButton>
						<input type="reset" value="Clear"/>
		</h:form>
	</div>
	<div id="template" style="display: none" class="dropbox">
		<h:form id="template_form">
		<label for="template_selector">Time Period Name:</label>	<h:selectOneListbox id="template_selector" value="#{timePeriodHandler.templateToLoad}" size="1">
			<f:selectItems value="#{timePeriodHandler.templateNames}"/>
    	  </h:selectOneListbox>
	    		 <h:commandButton action="#{timePeriodHandler.useTemplate}" value="Load"/>
    	  </h:form>
		<h:message id="template_message" for="template_selector"/></td></div>

	<p>* Denotes required field.</p>
	<table name="timeperiod_table">
		<h:form>
		<tr><td>Timeperiod Name: (*)</td><td colspan="4" align="left"><h:inputText id="timeperiod_name" size="20" value="#{timePeriodHandler.timePeriod.name}" required="true"/></td><td><h:message for="timeperiod_name"/></td></tr>
		<tr><td>Alias: (*)</td><td colspan="4" align="left"><h:inputText id="alias" size="20" value="#{timePeriodHandler.timePeriod.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
		<tr><td>Sunday:</td><td>Begins:</td><td><h:inputText id="sunday_start" value="#{timePeriodHandler.timePeriod.sundayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="sunday_end" value="#{timePeriodHandler.timePeriod.sundayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Monday:</td><td>Begins:</td><td><h:inputText id="monday_start" value="#{timePeriodHandler.timePeriod.mondayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="monday_end" value="#{timePeriodHandler.timePeriod.mondayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Tuesday:</td><td>Begins:</td><td><h:inputText id="tuesday_start" value="#{timePeriodHandler.timePeriod.tuesdayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="tuesday_end" value="#{timePeriodHandler.timePeriod.tuesdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Wednesday:</td><td>Begins:</td><td><h:inputText id="wednesday_start" value="#{timePeriodHandler.timePeriod.wednesdayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="wednesday_end" value="#{timePeriodHandler.timePeriod.wednesdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Thursday:</td><td>Begins:</td><td><h:inputText id="thursday_start" value="#{timePeriodHandler.timePeriod.thursdayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="thursday_end" value="#{timePeriodHandler.timePeriod.thursdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Friday:</td><td>Begins:</td><td><h:inputText id="friday_start" value="#{timePeriodHandler.timePeriod.fridayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="friday_end" value="#{timePeriodHandler.timePeriod.fridayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Saturday:</td><td>Begins:</td><td><h:inputText id="saturday_start" value="#{timePeriodHandler.timePeriod.saturdayStart}" size="10"/></td><td>Ends:</td><td><h:inputText id="saturday_end" value="#{timePeriodHandler.timePeriod.saturdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td colspan="6"><h:commandButton styleClass="button" value="Add TimePeriod" action="#{timePeriodHandler.addTimePeriod}" rendered="#{timePeriodHandler.timePeriod.isModifiable == false}"/> <h:commandButton value="Modify TimePeriod" styleClass="button" action="#{timePeriodHandler.modifyTimePeriod}" rendered="#{timePeriodHandler.timePeriod.isModifiable}"/> <h:commandButton styleClass="button" value="Save As Template" action="#{timePeriodHandler.addTemplate}" rendered="#{timePeriodHandler.timePeriod.isTemplate == false}"/> <input type="reset" class="button" value="Clear"></td></tr>		
		</h:form>
	</table>	
<br/>

<h3>List of current Timeperiods:</h3>

	<c:choose>
		<c:when test="${timePeriodHandler.timePeriodCount == 0}">
		<p>There are currently no Time Periods defined</p>
		</c:when>
		<c:otherwise>
		<h:form>
			<h:dataTable value="#{timePeriodHandler.sortedTimePeriodData}" rowClasses="even, odd" var="e">
				<h:column>
					<f:facet name="header">
						<h:commandLink action="#{timePeriodHandler.sortByName}" immediate="true" title="Sort By Name">
							<h:outputText value="Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink id="select_timePeriod" action="#{timePeriodHandler.select}" immediate="true" title="Select Time Period">
						<h:outputText value="#{e.name}"/>
					</h:commandLink>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:commandLink action="#{timePeriodHandler.sortByAlias}" immediate="true" title="Sort By Service Description">
							<h:outputText value="Alias"/>
						</h:commandLink>
					</f:facet>
					<h:outputText value="#{e.alias}"/>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Delete"/>
					</f:facet>
					<h:commandLink action="#{timePeriodHandler.delResult}" actionListener="#{timePeriodHandler.deleteTimePeriod}" immediate="true" title="Delete this TimePeriod">
						<h:outputText value="Delete"/>
						<f:param name="objectId" value="#{e.id}"/>
					</h:commandLink>
				</h:column>		
			</h:dataTable>
			<p>
			First: <h:commandButton id="first" value="<<" disabled="#{timePeriodHandler.scrollFirstDisabled}" action="#{timePeriodHandler.scrollFirst}" title="Scroll to First Page"/> <h:commandButton id="previous" value="<" disabled="#{timePeriodHandler.scrollFirstDisabled}" action="#{timePeriodHandler.scrollPrevious}" title="Scroll to the Previous Page"/> <h:commandButton id="next" value=">" disabled="#{timePeriodHandler.scrollLastDisabled}" action="#{timePeriodHandler.scrollNext}" title="Scroll to the Next Page"/> <h:commandButton value=">>" action="#{timePeriodHandler.scrollLast}" disabled="#{timePeriodHandler.scrollLastDisabled}" title="Scroll to the Last Page" id="last"/> : Last
			</p>
		</h:form>	
		</c:otherwise>
	</c:choose>


	
</f:view>

</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
