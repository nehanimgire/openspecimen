/*
 * Created on Jul 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.wustl.catissuecore.actionForm;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.domain.AbstractDomainObject;
import edu.wustl.catissuecore.domain.ReceivedEventParameters;
import edu.wustl.catissuecore.util.global.ApplicationProperties;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Validator;
import edu.wustl.common.util.logger.Logger;


/**
 * @author mandar_deshmukh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReceivedEventParametersForm extends EventParametersForm
{

	/**
	 * Grossly evaluated quality of the received specimen.
	 */
	protected String receivedQuality;

	/**
	 * Returns the receivedQuality of the specimen.
	 * @hibernate.property name="receivedQuality" type="string" column="RECEIVED_QUALITY" length=50"
	 * @return receivedQuality of the specimen.
	 */
	public String getReceivedQuality()
	{
		return receivedQuality;
	}

	/**
	 * Sets the receivedQuality of the SPECIMEN.
	 * @param receivedQuality receivedQuality of the SPECIMEN.
	 */
	public void setReceivedQuality(String receivedQuality)
	{
		this.receivedQuality = receivedQuality;
	}
	
//	 ---- super class methods
	// ----- SUPERCLASS METHODS
	/* (non-Javadoc)
	 * @see edu.wustl.catissuecore.actionForm.AbstractActionForm#getFormId()
	 */
	public int getFormId()
	{
		return Constants.RECEIVED_EVENT_PARAMETERS_FORM_ID;
	}

	/* (non-Javadoc)
	 * @see edu.wustl.catissuecore.actionForm.AbstractActionForm#setAllValues(edu.wustl.catissuecore.domain.AbstractDomainObject)
	 */
	public void setAllValues(AbstractDomainObject abstractDomain)
	{
		super.setAllValues(abstractDomain);
		ReceivedEventParameters receivedEventParameterObject = (ReceivedEventParameters)abstractDomain ;
		this.receivedQuality =  receivedEventParameterObject.getReceivedQuality(); 
	}
	
	/**
	 * Overrides the validate method of ActionForm.
     * */
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) 
     {
     	 
         ActionErrors errors = super.validate(mapping, request);
         Validator validator = new Validator();
         
         try
         {
         	// checks the receivedQuality

           	if ( validator.isEmpty( receivedQuality ))
            {
           		errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",ApplicationProperties.getValue("receivedeventparameters.receivedquality")));
            }
         }
         catch(Exception excp)
         {
             Logger.out.error(excp.getMessage());
         }
         return errors;
      }
	

     /**
      * Resets the values of all the fields.
      * This method defined in ActionForm is overridden in this class.
      */
      protected void reset()
     {
//         super.reset();
//         this.receivedQuality = null;
     }
    		
	
}
