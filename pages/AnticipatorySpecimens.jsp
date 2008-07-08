<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/nlevelcombo.tld" prefix="ncombo" %>
<%@ taglib uri="/WEB-INF/specimenDetails.tld" prefix="md" %>
<%@ page import="edu.wustl.catissuecore.util.global.Constants"%>
<%@ page import="edu.wustl.catissuecore.bean.GenericSpecimen"%>
<%@ page language="java" isELIgnored="false" %>
<script src="jss/script.js"></script>
<link rel="stylesheet" type="text/css" href="css/styleSheet.css" />
<html>
<head>
<script language="JavaScript" type="text/javascript" src="jss/javaScript.js"></script>
<script language="JavaScript" type="text/javascript" src="jss/GenericSpecimenDetailsTag.js"></script>
<script language="JavaScript">
	window.parent.frames['SpecimenEvents'].location="ShowCollectionProtocol.do?pageOf=specimenEventsPage&operation=ViewSummary";
	function ApplyToAll(object,type)
		{
			if(object.checked )
			{
				var fields = document.getElementsByTagName("input");
				var i =0;
				var text="";
				var valueToSet = "";
				var isFirstField = true;
				
				for (i=0; i<fields.length;i++)
				{
					text = fields[i].name;
					if(text.indexOf(type)>=0 && text.indexOf(".selectedContainerName")>=0)
					{
						if(isFirstField)
						{
							valueToSet = fields[i].value;
							isFirstField = false;
						}
						if(fields[i].value=="")
						{
							fields[i].value = valueToSet;
						}
					}
				}
			}
		}

		function saveCollectionProtocol()
		{
				var action ="SubmitSpecimenCollectionProtocol.do?action=collectionprotocol";
				document.forms[0].action = action;
				document.forms[0].submit();
		}

		function showMap(selectedContainerName,positionDimensionOne,positionDimensionTwo,containerId,specimenClassName,cpId)
		{
			frameUrl="ShowFramedPage.do?pageOf=pageOfSpecimen&"+
				"selectedContainerName=" + selectedContainerName +
				"&pos1=" + positionDimensionOne + 
				"&pos2=" + positionDimensionTwo +
				"&containerId=" +containerId +
				"&${requestScope.CAN_HOLD_SPECIMEN_CLASS}="+specimenClassName +
				"&${requestScope.CAN_HOLD_COLLECTION_PROTOCOL}=" + cpId;
			
			var storageContainer = document.getElementById(selectedContainerName).value;
			frameUrl+="&storageContainerName="+storageContainer;

			openPopupWindow(frameUrl,'newSpecimenPage');
			//mapButtonClickedOnSpecimen(frameUrl,'newSpecimenPage');
		}
		
		function ChangeCheckBoxStatus(type,chkInstance)
		{
			var checkedSpecimen ='].checkedSpecimen';
			var elementType = type +'[';
			var ctr=0;
			do
			{
				var elementName = elementType + ctr + checkedSpecimen;

				var chkCount= document.getElementsByName(elementName).length;
				if (chkCount >0)
				{
					var element = document.getElementsByName(elementName)[0];
					if (element.disabled == false)
					{
						element.checked = chkInstance.checked;
					}
					ctr++;
				}
			}while(chkCount>0);
		}
		
		function UpdateCheckBoxStatus()
		{
			var checkedSpecimen ='].checkedSpecimen';
			var aliquotType = "aliquot[";
			var ctr = 0;
			if (document.forms[0].chkAllAliquot != null)
			{
				document.forms[0].chkAllAliquot.disabled = true;
				document.forms[0].chkAllAliquot.checked = true;
				do
				{
					var elementName = aliquotType + ctr + checkedSpecimen;
					var chkCount= document.getElementsByName(elementName).length;
					if (chkCount >0)
					{
						var element = document.getElementsByName(elementName)[0];
						if (element.disabled == false)
						{
							document.forms[0].chkAllAliquot.disabled = false;							
						}
						ctr++;
					}
				}while(chkCount>0);
			}
			
			if (document.forms[0].chkAllAliquot != null)
			{
				ctr = 0;
				var derivedType = "derived[";
				document.forms[0].chkAllDerived.disabled = true;
				document.forms[0].chkAllDerived.checked = true;
				do
				{
					var elementName = derivedType + ctr + checkedSpecimen;
					var chkCount= document.getElementsByName(elementName).length;
					if (chkCount >0)
					{
						var element = document.getElementsByName(elementName)[0];
						if (element.disabled == false)
						{
							document.forms[0].chkAllDerived.disabled = false;							
						}
						ctr++;
					}
				}while(chkCount>0);
			}		
		}
		function pageSubmit()
		{
			
			var printFlag = document.getElementById("printCheckbox");
			if(printFlag.checked)
			{
				document.forms[0].action ='GenericSpecimenSummary.do?save=SCGSpecimens&printflag=1'; 
				document.forms[0].submit();
			}
			else
			{
				document.forms[0].action ='GenericSpecimenSummary.do?save=SCGSpecimens&printflag=0'; 
				document.forms[0].submit();
			}
		}
	</script>
</head>
<body onload="UpdateCheckBoxStatus()">
<script language="javascript">
	${requestScope.refreshTree}
</script>

		<html:errors />
		<html:messages id="messageKey" message="true" header="messages.header" footer="messages.footer">
			${requestScope.messageKey}
		</html:messages>
		
		<html:form action="${requestScope.formAction}">		
		<table>
		<tr>
				<td> &nbsp; </td>
		<td>
		<table summary="" cellpadding="0" cellspacing="0" border="0">
		<logic:equal name="viewSpecimenSummaryForm" property="readOnly" value="false">
		<tr>					
				<td class="formFieldNoBorders" colspan="5"  height="20" >
				<html:checkbox styleId="printCheckbox" property="printCheckbox" value="true" onclick="">
						<bean:message key="print.checkboxLabel"/>
				</html:checkbox>
		    	</td>
		</tr>
		<tr>
		<td>
		<input type="button" value="Submit" onclick="pageSubmit()" />
		</td>
		</tr>
		</logic:equal>
		<tr>
			<td>
				&nbsp;
			</td>
		</tr>
				<tr>
					<td class="dataTablePrimaryLabel" height="20">
						<bean:write name="viewSpecimenSummaryForm" property="title" />
					</td>				
				<logic:equal name="viewSpecimenSummaryForm" property="requestType" value="Multiple Specimen">
					<script language="javascript">
						${requestScope.refreshTree}
					</script>
				</logic:equal>
				</tr>
		<logic:empty name="viewSpecimenSummaryForm" property="specimenList" >
			<tr>
				<td class="dataTableWhiteCenterHeader" colspan="6">  
					No specimens to display for current action!!
				</td>
			</tr>		
		</logic:empty>
		</table>
		<logic:notEmpty name="viewSpecimenSummaryForm" property="specimenList" >	
				<table summary="" cellpadding="3" cellspacing="0" border="0" 
				class="dataTable" >
			<md:genericSpecimenDetails columnHeaderListName="columnHeaderList" formName="viewSpecimenSummaryForm" dataListName="specimenList" dataListType="Parent" columnListName="columnListName" isReadOnly="false" displayColumnListName="dispColumnsList" />
			<%-- custom tag for specimen list by mandar ---  --%>					
				</table>
		</logic:notEmpty>
		<logic:notEmpty name="viewSpecimenSummaryForm" property="eventId">
			<html:hidden property="eventId"  />
			<html:hidden property="lastSelectedSpecimenId"  />
			<html:hidden property="selectedSpecimenId"  />
		</logic:notEmpty>
			<html:hidden property="userAction" />
			<html:hidden property="requestType" />
		<logic:empty name="viewSpecimenSummaryForm" property="aliquotList" >
			<logic:empty name="viewSpecimenSummaryForm" property="derivedList" >
				<table>
				<tr> <td> <br> </td> </tr>
					<tr>
						<td class="dataTablePrimaryLabel" colspan="6" height="20">  
							Child specimens not defined.
						</td>						
					</tr>
					<tr> <td> <br> </td> </tr>
				</table>	
				</logic:empty>				
			</logic:empty>
			<logic:notEmpty name="viewSpecimenSummaryForm" property="aliquotList" >
			&nbsp;
			<table>
				<tr>
					<td class="dataTablePrimaryLabel" height="20">	
					<bean:message key="anticipatorySpecimen.AliquotDetails"/>
					</td>
				</tr>
			</table>
			<table summary="" cellpadding="3" cellspacing="0" border="0" 
			class="dataTable" >
			<md:genericSpecimenDetails columnHeaderListName="subSpecimenColHeaderList" formName="viewSpecimenSummaryForm" dataListName="aliquotList" dataListType="Aliquot" columnListName="columnListName" isReadOnly="false" displayColumnListName="dispColumnsList" />
			<%-- custom tag for specimen list by mandar --- Aliquot  --%>						
				</table>				
			</logic:notEmpty>
		<logic:notEmpty name="viewSpecimenSummaryForm" property="derivedList" >
		&nbsp;
		<table>
		<tr>
		<td class="dataTablePrimaryLabel" height="20">
			<bean:message key="anticipatorySpecimen.DerivativeDetails"/>
		 </td>
		 </tr>
		 </table>
		    <table summary="" cellpadding="3" cellspacing="0" border="0" 
		    class="dataTable" >
		<md:genericSpecimenDetails columnHeaderListName="subSpecimenColHeaderList" formName="viewSpecimenSummaryForm" dataListName="derivedList" dataListType="Derived" columnListName="columnListName" isReadOnly="false" displayColumnListName="dispColumnsList" />
			<%-- custom tag for specimen list by mandar --- Derived --%>					
		    </table>
		</logic:notEmpty>		
&nbsp;&nbsp;
		<table align="bottom">
			<logic:equal name="viewSpecimenSummaryForm" property="requestType" value="Collection Protocol">
			<tr>
			   <td>
				<html:submit  value="Save Collection Protocol" />
				</td>
				</tr>
			</logic:equal>
			<logic:equal name="viewSpecimenSummaryForm" property="requestType" value="Multiple Specimen">		
			<tr>
				<td>
				<html:submit value="Save Specimens" />
				</td>
			 </tr>
			</logic:equal>
		<html:hidden property="targetSuccess" />
		<html:hidden property="submitAction" />
		<html:hidden property="showCheckBoxes" />
		<html:hidden property="showLabel" />
		<html:hidden property="showbarCode" />
		<html:hidden property="readOnly" />
		<html:hidden property="showParentStorage" />
		<logic:equal name="viewSpecimenSummaryForm" property="readOnly" value="false">
		 <tr>
			<td>
			<input type="button" value="Submit" onclick="pageSubmit()" />
			</td>
		 </tr>
		</logic:equal>
		</table>
		</td>
		</tr>
		</table>
		<div id="divForHiddenChild"></div>
		</html:form>
</body>
</html>
<script language="JavaScript" type="text/javascript">
identifyDisabledCheckBox();
</script>