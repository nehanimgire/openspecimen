<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/nlevelcombo.tld" prefix="ncombo" %>
<%@ page import="edu.wustl.catissuecore.util.global.AppUtility"%>
<%@ page import="edu.wustl.catissuecore.util.global.Variables"%>
<%@ page import="edu.wustl.catissuecore.actionForm.FrozenEventParametersForm"%>
<%@ page import="edu.wustl.catissuecore.util.global.Constants"%>
<%@ page import="edu.wustl.common.util.global.CommonServiceLocator"%>
<%@ include file="/pages/content/common/AutocompleterCommon.jsp" %>
<%@ page language="java" isELIgnored="false" %>
<head>
<!-- Mandar : 434 : for tooltip -->
<script language="JavaScript" type="text/javascript" src="jss/javaScript.js"></script>
<!-- Mandar 21-Aug-06 : For calendar changes -->
<script src="jss/calendarComponent.js"></script>
<SCRIPT>var imgsrc="images/";</SCRIPT>
<LINK href="css/calanderComponent.css" type=text/css rel=stylesheet>
<link href="css/catissue_suite.css" rel="stylesheet" type="text/css" />
<!-- Mandar 21-Aug-06 : calendar changes end -->

<link rel="stylesheet" type="text/css" href="dhtmlxSuite_v35/dhtmlxCombo/codebase/dhtmlxcombo.css">
<link rel="stylesheet" type="text/css" href="dhtmlxSuite_v35/dhtmlxWindows/codebase/skins/dhtmlxwindows_dhx_skyblue.css">

<script src="dhtmlxSuite_v35/dhtmlxGrid/codebase/dhtmlxcommon.js"></script>
<script src="dhtmlxSuite_v35/dhtmlxCombo/codebase/dhtmlxcombo.js"></script>

</head>

<%@ include file="/pages/content/common/ActionErrors.jsp" %>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
<html:form action='${requestScope.formName}'>
	<html:hidden property="operation" />
	<html:hidden property="id" />
	<html:hidden property="onSubmit" />
	<html:hidden property="specimenId" value='${requestScope.specimenId}'/>
    <tr>
    <td align="left" class="tr_bg_blue1">
		  <span class="blue_ar_b">&nbsp;<bean:message  key="eventparameters"/> &quot;<em>
				<bean:message key="frozeneventparameters"/></em>&quot;</span>
		  </td>
        </tr>
        <tr>
          <td colspan="4" class="showhide1"></td>
        </tr>
        <tr>
			<td colspan="4" class="showhide">
			<table width="100%" border="0" cellpadding="1" cellspacing="0">
				<tr>
					<td width="1%" align="center" class="black_ar"><img src="images/uIEnhancementImages/star.gif" alt="Mandatory Field" width="6" height="6" hspace="0" vspace="0" /></td>
		            <td width="15%" align="left" nowrap class="black_ar"><bean:message key="eventparameters.user"/></td>
				    <td align="left" valign="middle" width="30%">
					<html:select property="userId" styleClass="formFieldSized18" styleId="userId" size="1"					onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
						<html:options collection='${requestScope.userListforJSP}' labelProperty="name" property="value"/>
					</html:select></td>
					<td width="1%"></td>
				<td colspan="2"></td>
				</tr>

                <tr>
                  <td align="center" class="black_ar"><img src="images/uIEnhancementImages/star.gif" alt="Mandatory Field" width="6" height="6" hspace="0" vspace="0" /></td>
                  <td align="left" class="black_ar"><bean:message key="eventparameters.dateofevent"/></td>
                  <td align="left"><logic:notEmpty name="currentEventParametersDate">
<ncombo:DateTimeComponent name="dateOfEvent"
			  id="dateOfEvent"
			  formName="frozenEventParametersForm"
			                  month='${requestScope.eventParametersMonth}'
							  year='${requestScope.eventParametersYear}'
							  day='${requestScope.eventParametersDay}'
							  pattern="<%=CommonServiceLocator.getInstance().getDatePattern()%>"
							  value='${requestScope.currentEventParametersDate}'
							  styleClass="black_ar"	/>
</logic:notEmpty>
<logic:empty name="currentEventParametersDate">
<ncombo:DateTimeComponent name="dateOfEvent"
			  id="dateOfEvent"
			  formName="frozenEventParametersForm"
			  pattern="<%=CommonServiceLocator.getInstance().getDatePattern()%>"
			  styleClass="black_ar" />
</logic:empty><span class="grey_ar_s capitalized">[${datePattern}]</span>&nbsp;</td>

                  <td align="center" class="black_ar"><img src="images/uIEnhancementImages/star.gif" alt="Mandatory Field" width="6" height="6" hspace="0" vspace="0" /></td>
                  <td align="left" class="black_ar" width="8%"><bean:message key="eventparameters.time"/></td>
                  <td align="left">						  
						  
				<div style="width:100%"  class="black_ar"><div style="float:left;">
						<select id="timeInHours1" styleClass="black_ar" styleId="timeInHours" size="1"> 
						<logic:iterate id="hourListd" name="hourList">
								
									<option value="<bean:write name='hourListd'/>" selected><bean:write name='hourListd'/></option>
								
							</logic:iterate>
						<select></div><div style="float:left;">&nbsp;<bean:message key="eventparameters.timeinhours"/>&nbsp;&nbsp;
						</div><div style="float:left;">
						<select id="timeInMinutes1" styleClass="black_ar" styleId="timeInMinutes" size="1"> 
						<logic:iterate id="minutesId" name="minutesList">
								
									<option value="<bean:write name='minutesId'/>" selected><bean:write name='minutesId'/></option>
								
							</logic:iterate>
						</select></div><div>&nbsp;&nbsp;<bean:message key="eventparameters.timeinminutes"/>
						</div>
<html:hidden property="timeInHours" value='${frozenEventParametersForm.timeInHours}'/>
<html:hidden property="timeInMinutes"  value='${frozenEventParametersForm.timeInMinutes}'/>

						</div>
								<script>
							 window.dhx_globalImgPath="dhtmlxSuite_v35/dhtmlxWindows/codebase/imgs/";
							  var timeHr = new dhtmlXCombo("timeInHours1","timeInHours1","100px");
							  timeHr.setSize(60);
							  timeHr.enableFilteringMode(true);
							  if('${frozenEventParametersForm.timeInHours}'!=0){
								timeHr.setComboValue('${frozenEventParametersForm.timeInHours}');
							  }
							  timeHr.attachEvent("onChange", function(){
								document.getElementsByName("timeInHours")[0].value= timeHr.getSelectedValue();
							  });  

							   var timeMinute = new dhtmlXCombo("timeInMinutes1","timeInMinutes1","100px");
							  timeMinute.setSize(60);
							  timeMinute.enableFilteringMode(true);
							  if('${frozenEventParametersForm.timeInMinutes}'!=0){
								timeMinute.setComboValue('${frozenEventParametersForm.timeInMinutes}');
							  }
							  timeMinute.attachEvent("onChange", function(){
								document.getElementsByName("timeInMinutes")[0].value= timeMinute.getSelectedValue();
							  });  

						</script>	

				
						  
						  
						  
						  
						  </td>
                </tr>

                <tr>
                 <td align="center" class="black_ar"><img src="images/uIEnhancementImages/star.gif" alt="Mandatory Field" width="6" height="6" hspace="0" vspace="0" /></td>
                  <td align="left" class="black_ar">
						<bean:message key="frozeneventparameters.method"/></td>
                  <td align="left" class="black_ar" colspan="4">
				  
				  <div style="width:100%"  class="black_ar"><div style="float:left;">
						<select id="frozenMethod" styleClass="black_ar" styleId="frozenMethod" size="1"> 
						<logic:iterate id="frozenMethod" name="methodList">
								
									<option value='<bean:write name="frozenMethod" property="value"/>'><bean:write name='frozenMethod' property='name'/></option>
								
							</logic:iterate>
						</select></div>
<html:hidden property="method" value='${frozenEventParametersForm.method}'/>

						</div>
								<script>
							  var frozenMethod = new dhtmlXCombo("frozenMethod","frozenMethod","100px");
							  frozenMethod.setSize(225);
							  frozenMethod.enableFilteringMode(true);
							  if('${frozenEventParametersForm.method}'!=0){
								frozenMethod.setComboValue('${frozenEventParametersForm.method}');
							  }
							  frozenMethod.attachEvent("onChange", function(){
								document.getElementsByName("method")[0].value= frozenMethod.getSelectedValue();
							  });  


						</script>	

		
				  
				  </td>
                </tr>

                <tr>
                  <td align="center" class="black_ar">&nbsp;</td>
                  <td align="left" valign="top" class="black_ar_t"><bean:message key="eventparameters.comments"/></td><td align="left" colspan="5"><html:textarea styleClass="black_ar" cols="73" rows="4" styleId="comments" property="comments" /></td>
                </tr>

          </table></td>
        </tr>
        <tr  >
          <td class="buttonbg">

		  <html:submit styleClass="blue_ar_b" value="Submit" property="Submit" onclick='${requestScope.changeAction}'/>
            </td>
        </tr>


</table>

	 </html:form>
 </table>