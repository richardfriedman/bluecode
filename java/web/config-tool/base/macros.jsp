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
<p>This is the first stop in our wizard. On this page you can define what are known as Macros. Macros can basically be thought of as a shortcut to
certain properties that you may wish to use within your system. For example, it's generally a lot easier to define a Macro that Identifies the location of
your plug-in directory, than have to type out the full absolute path each time you define a new command.</p>
<p>You are currently allowed to define 32 Macros within the Blue monitoring framework. To use a macro, simply refer to it by the id that it receives after
you have added it. For example, a Macro with Id 1 can be referred to by using $USER1$.</p> 

<p>For the sake of this Wizard, add 1 macro that points to the location of your plug-in directory! (Note: this will be location_of_blue_install/plugins);</p>
</div>
</c:if>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>

<h3>Macro Creation. <br/><a href="javascript:popUp('macrohelp.html')">Need Help? Click Here!</a></h3>
	<p>* Denotes required value.</p>
	<table name="contact_table">
		<h:form>
		<tr><td>Macro Definition: (*)</td><td><h:inputText id="macro_text" size="50" value="#{macroHandler.macro.macroValue}" required="true"/></td></tr>
		<tr><td colspan="3"><h:commandButton  styleClass="button" value="Save Macro" action="#{macroHandler.addMacro}" rendered="#{macroHandler.macro.isModifiable == false}"/> <h:commandButton id="modify_macro"  styleClass="button" value="Modify Macro" rendered="#{macroHandler.macro.isModifiable}" action="#{macroHandler.modifyMacro}"/><input type="reset" class="button" value="Clear"></td></tr>		
		</h:form>
	</table>	
<br/>
<h3>List of current Macros:</h3>


<c:choose>
<c:when test="${macroHandler.macroCount==0}">
	<p>There are currently no Macros defined</p>
</c:when>
<c:otherwise>
<h:form>
		<h:dataTable value="#{macroHandler.macroDetails}" rowClasses="even, odd" var="e">
			<h:column>
				<f:facet name="header">
					<h:outputText value="Macro Id"/>
				</f:facet>
				<h:outputText value="#{e.id}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
						<h:outputText value="Macro Definition"/>
				</f:facet>
				<h:commandLink action="#{macroHandler.select}" title="View details for this Macro" immediate="true">
					<h:outputText value="#{e.macroValue}"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink  action="#{macroHandler.delResult}" actionListener="#{macroHandler.deleteMacro}" immediate="true" title="Delete this Macro">
					<f:param name="macroId" value="#{e.id}"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>	
		</h:dataTable>
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
