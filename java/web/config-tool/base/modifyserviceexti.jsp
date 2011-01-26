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

</head>

<body><div id="content"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<div id="nav">
	<%@ include file="menu.html" %>
</div>
<f:view>
<div id="main">

<h3>Modify Service Extended Information.</h3>
	<table name="serviceexi_table">
		<h:form>
		<tr><td>Host & Service Description:</td><td><h:selectOneMenu id="service_name" value="#{extInfoHandler.modifyServiceExtInfo.serviceHost}">
							<f:selectItems value="#{serviceHandler.serviceHostNames}"/>
						     </h:selectOneMenu></td><td colspan="2" h:message for="service_name"/></td></tr>
		<tr><td>Notes:</td><td colspan="3"><h:inputText id="notes" value="#{extInfoHandler.modifyServiceExtInfo.notes}" size="40"/></td></tr>
		<tr><td>Notes URL:</td><td colspan="3"><h:inputText id="notes_url" value="#{extInfoHandler.modifyServiceExtInfo.notesURL}" size="20"/></td></tr>
		<tr><td>Action URL:</td><td colspan="3"><h:inputText id="action_url" value="#{extInfoHandler.modifyServiceExtInfo.actionURL}" size="20"/></td></tr>
		<tr><td>Icon Image:</td><td colspan="3"><h:inputText id="ii" value="#{extInfoHandler.modifyServiceExtInfo.iconImage}" size="20"/></td></tr>
		<tr><td>Icon Image Alt:</td><td colspan="3"><h:inputText id="iia" value="#{extInfoHandler.modifyServiceExtInfo.iconImageAlt}" size="20"/></td></tr>
		<tr><td colspan="4"><h:commandButton value="Modify Extended Info" actionListener="#{extInfoHandler.modifyExtInfo}" action="#{extInfoHandler.modResult}">
					<f:attribute name="extType" value="1"/>
				    </h:commandButton> </td></tr>		
		</h:form>
	</table>	
</f:view>

<p>Use the form above to modify the Service Extended Information.</p>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
