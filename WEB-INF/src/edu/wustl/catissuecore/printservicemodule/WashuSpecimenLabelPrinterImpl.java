
package edu.wustl.catissuecore.printservicemodule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.wustl.catissuecore.domain.ExternalIdentifier;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.util.global.CommonUtilities;
import edu.wustl.common.util.global.Validator;

/**
 * This Class is used to define method for Specimen label printing.
 * @author Renuka_Bajpai
 */
public class WashuSpecimenLabelPrinterImpl extends SpecimenLabelPrinterImpl
{

	/**
	 * This method adds Data To Print.
	 * @param specimen Specimen object.
	 * @param listMap list Map
	 * @param printerType printer Type
	 * @param printerLocation printer Location
	 * @param ipAddress IP Address
	 */
	@Override
	protected void addDataToPrint(Specimen specimen, List listMap, String printerType,
			String printerLocation, String ipAddress,String loginName)
	{
		System.out.println("############### printing labels for Specimen: "+ specimen.getLabel());
		final Map<String, String> dataMap = new LinkedHashMap<String, String>();
		dataMap.put("class", specimen.getClassName());
		dataMap.put("id", specimen.getId().toString());
		String label = specimen.getLabel();
		dataMap.put(PrintWebServiceConstants.USER_IPADDRESS, ipAddress);
		if(specimen.getExternalIdentifierCollection() != null && !specimen.getExternalIdentifierCollection().isEmpty()){
			
			 StringBuilder exIds = new StringBuilder(100);
			 for (ExternalIdentifier exId : specimen.getExternalIdentifierCollection()) {
			 exIds.append(exId.getName());
			 exIds.append(":");
			 exIds.append(exId.getValue());
			 exIds.append(",");
			 }
			 dataMap.put(PrintWebServiceConstants.EX_ID, exIds.toString());
			 }
			 if (!Validator.isEmpty(loginName))
			 {
			 dataMap.put(PrintWebServiceConstants.LOGIN_NAME, loginName);
			 }
		if (specimen.getClassName() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_CLASS, CommonUtilities.toString(specimen
					.getClassName()));
		}
		if (specimen.getId() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_IDENTIFIER, CommonUtilities
					.toString(specimen.getId()));
		}
		if (specimen.getSpecimenType() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_TYPE, CommonUtilities.toString(specimen
					.getSpecimenType()));
		}
		if (label == null || label.equals(""))
		{
			label = specimen.getSpecimenType();
		}
		dataMap.put(PrintWebServiceConstants.SPECIMEN_LABEL, CommonUtilities.toString(label));
		if (specimen.getBarcode() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_BARCODE, CommonUtilities
					.toString(specimen.getBarcode()));
		}
		if (specimen.getTissueSite() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_TISSUE_SITE, CommonUtilities
					.toString(specimen.getTissueSite()));
		}
		if (specimen.getCollectionStatus() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_COLLECTION_STATUS, CommonUtilities
					.toString(specimen.getCollectionStatus()));
		}
		if (specimen.getComment() != null && !specimen.getComment().equals(""))
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_COMMENT, CommonUtilities
					.toString(specimen.getComment()));
		}
		if (specimen.getCreatedOn() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_CREATED_ON, CommonUtilities
					.toString(specimen.getCreatedOn()));
		}
		if (specimen.getLineage() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_LINEAGE, CommonUtilities
					.toString(specimen.getLineage()));
		}
		if (specimen.getMessageLabel() != null && !"".equals(specimen.getMessageLabel()))
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_MESSAGE_LABEL, CommonUtilities
					.toString(specimen.getMessageLabel()));
		}
		if (specimen.getSpecimenPosition() != null
				&& specimen.getSpecimenPosition().getStorageContainer() != null)
		{
			dataMap.put(PrintWebServiceConstants.SPECIMEN_STORAGE_CONTAINER_NAME, CommonUtilities
					.toString(specimen.getSpecimenPosition().getStorageContainer().getName()));
			dataMap.put(PrintWebServiceConstants.SPECIMEN_POSITION_DIMENSION_ONE, CommonUtilities
					.toString(specimen.getSpecimenPosition().getPositionDimensionOne()));
			dataMap.put(PrintWebServiceConstants.SPECIMEN_POSITION_DIMENSION_TWO, CommonUtilities
					.toString(specimen.getSpecimenPosition().getPositionDimensionTwo()));
		}

		if (Constants.MOLECULAR.equals(specimen.getClassName()))
		{
			final String concentration = CommonUtilities.toString(String
					.valueOf((specimen)
							.getConcentrationInMicrogramPerMicroliter()));
			dataMap.put(PrintWebServiceConstants.CONCENTRATION, concentration);
		}
		if (specimen.getAvailableQuantity() != null)
		{
			dataMap.put(PrintWebServiceConstants.QUANTITY, CommonUtilities.toString(String
					.valueOf(specimen.getAvailableQuantity())));
		}
		if (specimen.getPathologicalStatus() != null)
		{
			dataMap.put(PrintWebServiceConstants.PATHOLOGICAL_STATUS, CommonUtilities
					.toString(specimen.getPathologicalStatus()));
		}
		
		 try
		  {
		  String participant = specimen.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getParticipant().getId().toString();
		  dataMap.put("participant", participant);
		  String participantF = specimen.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getParticipant().getFirstName().toString();
		  String participantL = specimen.getSpecimenCollectionGroup().getCollectionProtocolRegistration().getParticipant().getLastName().toString();
		  if(!Validator.isEmpty(participantF)){
		  dataMap.put("participantF", participantF);
		  }
		  if(!Validator.isEmpty(participantL)){
		  dataMap.put("participantL", participantL);
		  }
		  System.out.println((new StringBuilder("ParticipantT: ")).append(participant).append(" ").append(participantF).append(" ").append(participantL).toString());
		  }
		  catch(Exception e)
		  {
		  e.printStackTrace();
		 // dataMap.put("participant", "null");
		 // dataMap.put("participantF", "null");
		 // dataMap.put("participantL", "null");
		  }

		final String cpTitle = CommonUtilities.toString(specimen.getSpecimenCollectionGroup()
				.getCollectionProtocolRegistration().getCollectionProtocol().getShortTitle());
		final String ppi = specimen.getSpecimenCollectionGroup()
				.getCollectionProtocolRegistration().getProtocolParticipantIdentifier();
		dataMap.put(PrintWebServiceConstants.CP_TITLE, cpTitle);
		if (ppi != null && !ppi.equals(""))
		{
			dataMap.put(PrintWebServiceConstants.PARTICIPANT_PROTOCOL_IDENTIFIER, CommonUtilities
					.toString(ppi));
		}

		listMap.add(dataMap);

	}
}
