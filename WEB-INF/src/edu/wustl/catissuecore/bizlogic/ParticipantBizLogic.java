/**
 * <p>Title: ParticipantHDAO Class>
 * <p>Description:	ParticipantHDAO is
 * used to add Participant's information
 * into the database using Hibernate.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Aniruddha Phadnis
 * @version 1.00
 * Created on Jul 23, 2005
 */

package edu.wustl.catissuecore.bizlogic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.krishagni.catissueplus.core.audit.domain.Audit;
import com.krishagni.catissueplus.core.common.util.ObjectType;
import com.krishagni.catissueplus.core.common.util.Operation;

import edu.wustl.catissuecore.domain.CollectionProtocol;
import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
import edu.wustl.catissuecore.domain.ConsentTier;
import edu.wustl.catissuecore.domain.ConsentTierResponse;
import edu.wustl.catissuecore.domain.CpSyncAudit;
import edu.wustl.catissuecore.domain.Participant;
import edu.wustl.catissuecore.domain.ParticipantMedicalIdentifier;
import edu.wustl.catissuecore.domain.Race;
import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.domain.pathology.IdentifiedSurgicalPathologyReport;
import edu.wustl.catissuecore.dto.ConsentResponseDto;
import edu.wustl.catissuecore.dto.ConsentTierDTO;
import edu.wustl.catissuecore.dto.MedicalRecordNumberDTO;
import edu.wustl.catissuecore.dto.ParticipantDTO;
import edu.wustl.catissuecore.util.ApiSearchUtil;
import edu.wustl.catissuecore.util.global.AppUtility;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.NameValueBean;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.bizlogic.IBizLogic;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.exception.ApplicationException;
import edu.wustl.common.exception.BizLogicException;
import edu.wustl.common.exception.ErrorKey;
import edu.wustl.common.factory.AbstractFactoryConfig;
import edu.wustl.common.factory.IFactory;
import edu.wustl.common.lookup.DefaultLookupParameters;
import edu.wustl.common.lookup.DefaultLookupResult;
import edu.wustl.common.lookup.LookupLogic;
import edu.wustl.common.participant.bizlogic.CommonParticipantBizlogic;
import edu.wustl.common.util.Utility;
import edu.wustl.common.util.XMLPropertyHandler;
import edu.wustl.common.util.global.CommonServiceLocator;
import edu.wustl.common.util.global.Status;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.global.Variables;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.dao.DAO;
import edu.wustl.dao.HibernateDAO;
import edu.wustl.dao.JDBCDAO;
import edu.wustl.dao.QueryWhereClause;
import edu.wustl.dao.condition.EqualClause;
import edu.wustl.dao.daofactory.DAOConfigFactory;
import edu.wustl.dao.exception.AuditException;
import edu.wustl.dao.exception.DAOException;
import edu.wustl.dao.query.generator.ColumnValueBean;
import edu.wustl.dao.query.generator.DBTypes;
import edu.wustl.dao.util.NamedQueryParam;
import edu.wustl.security.exception.SMException;
import edu.wustl.security.exception.UserNotAuthorizedException;
import edu.wustl.security.global.Permissions;
import edu.wustl.security.privilege.PrivilegeCache;
import edu.wustl.security.privilege.PrivilegeManager;

/**
 * ParticipantHDAO is used to to add Participant's information into the database
 * using Hibernate.
 * 
 * @author aniruddha_phadnis
 */
public class ParticipantBizLogic extends CatissueDefaultBizLogic
{

	/**
	 * Logger object.
	 */
	private static final Logger logger = Logger.getCommonLogger(ParticipantBizLogic.class);
	/**
	 * List of cprIds.
	 */
	private final List<Long> cprIdList = new ArrayList<Long>();

	/**
	 * Saves the Participant object in the database.
	 * 
	 * @param obj
	 *            The storageType object to be saved.
	 * @param dao
	 *            - DAO object
	 * @param sessionDataBean
	 *            - The session in which the object is saved.
	 * @throws BizLogicException
	 *             throws BizLogicException
	 */
	protected void insert(final Object obj, final DAO dao, final SessionDataBean sessionDataBean)
			throws BizLogicException
	{
		try
		{
			Participant participant = insertParticipant(obj, dao);
			resisterCPR(dao, sessionDataBean, participant);
			auditParticipant(participant, dao, sessionDataBean,Operation.INSERT.toString());
		}
		catch (final DAOException daoExp)
		{
			logger.error(daoExp.getMessage(), daoExp);
			throw this
					.getBizLogicException(daoExp, daoExp.getErrorKeyName(), daoExp.getMsgValues());
		}
	}

	/**
	 * @param dao
	 *            DAO Object
	 * @param sessionDataBean
	 *            SessionDataBean
	 * @param participant
	 *            Participant Object
	 * @throws DAOException
	 *             DAO Exception
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void resisterCPR(final DAO dao, SessionDataBean sessionDataBean,
			final Participant participant) throws DAOException, BizLogicException
	{
		final Collection<CollectionProtocolRegistration> cprCollection = participant
				.getCollectionProtocolRegistrationCollection();

		if (cprCollection == null || cprCollection.isEmpty())
		{
			this.insertAuthData(participant);
		}
		else
		{
			this.registerToCPR(dao, sessionDataBean, participant);
		}
	}

	/**
	 * Insert participant.
	 * 
	 * @param obj
	 *            Participant Object
	 * @param dao
	 *            DAO Object
	 * @return the participant
	 * @throws BizLogicException
	 *             the BizLogicException
	 * @throws DAOException
	 *             the DAO exception
	 * @throws AuditException
	 *             the audit exception
	 */
	private Participant insertParticipant(Object obj, DAO dao) throws BizLogicException,
			DAOException
	{

		return (Participant) CommonParticipantBizlogic.insert(obj, dao,
				new ParticipantMedicalIdentifier());

	}

	/**
	 * @param participant
	 *            - Participant object.
	 * @throws DAOException
	 *             throws DAOException
	 */
	private void insertAuthData(final Participant participant) throws DAOException
	{
		final Set<Participant> protectionObjects = new HashSet<Participant>();
		protectionObjects.add(participant);

	}

	/**
	 * @param dao
	 *            - DAO object.
	 * @param sessionDataBean
	 *            - SessionDataBean object
	 * @param participant
	 *            - Participant object
	 * @throws DAOException
	 *             throws DAOException
	 */
	private void registerToCPR(DAO dao, SessionDataBean sessionDataBean, Participant participant)
			throws BizLogicException
	{
		final CollectionProtocolRegistrationBizLogic cprBizLogic = new CollectionProtocolRegistrationBizLogic();
		final Collection<CollectionProtocolRegistration> cprCollection = participant
				.getCollectionProtocolRegistrationCollection();
		final Iterator<CollectionProtocolRegistration> itcprCollection = cprCollection.iterator();
		while (itcprCollection.hasNext())
		{
			final CollectionProtocolRegistration cpr = itcprCollection.next();
			/**
			 * If label generation is off for CPR then cprCollection will return
			 * a CPR with null CP. so while inserting CPR it will give
			 * NullPointerException.
			 */
			if (cpr.getCollectionProtocol() != null)
			{
				cpr.setParticipant(participant);
				cpr.setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.toString());
				cprBizLogic.insert(cpr, dao, sessionDataBean);
				cprIdList.add(cpr.getId());
			}
		}
	}

	/**
	 * This method gets called after insert method Any logic after inserting
	 * object in database can be included here.
	 * 
	 * @param obj
	 *            The inserted object.
	 * @param dao
	 *            the DAO object
	 * @param sessionDataBean
	 *            session specific data
	 * @throws BizLogicException
	 *             throws BizLogicException
	 * */
	protected void postInsert(Object obj, DAO dao, SessionDataBean sessionDataBean)
			throws BizLogicException
	{
		// super.postInsert(obj, dao, sessionDataBean);
		// IParticipantManagerLookupLogic lookUpLogic =
		// (IParticipantManagerLookupLogic) Utility.getObject(XMLPropertyHandler
		// .getValue(Constants.PARTICIPANT_LOOKUP_ALGO));
		// IParticipant participant = (Participant)obj;
		// lookUpLogic.updatePartiicpantCache(participant);
		try
		{
			final LinkedHashSet<Long> userIdSet = ParticipantUtil.getUserIdSet(obj,
					sessionDataBean.getUserId(), dao);
			CommonParticipantBizlogic.postInsert(obj, userIdSet);

			dao.closeSession();
		}
		catch (DAOException e)
		{
			throw new BizLogicException(e);
		}

	}

	/**
	 * This method gets called after update method. Any logic after updating
	 * into database can be included here.
	 * 
	 * @param dao
	 *            the object
	 * @param currentObj
	 *            The object to be updated.
	 * @param oldObj
	 *            The old object.
	 * @param sessionDataBean
	 *            session specific data
	 * @throws BizLogicException
	 *             throws BizLogicException
	 * */
	protected void postUpdate(DAO dao, Object currentObj, Object oldObj,
			SessionDataBean sessionDataBean) throws BizLogicException
	{
		try
		{
			// added a call to fetch userId set
			final LinkedHashSet<Long> userIdSet = ParticipantUtil.getUserIdSet(currentObj,
					sessionDataBean.getUserId(), dao);

			// changed signature of the postUpdate method because userId set is
			// required for eMPI processing
			CommonParticipantBizlogic.postUpdate(oldObj, currentObj, sessionDataBean, userIdSet);
			dao.closeSession();

		}
		catch (DAOException e)
		{
			throw new BizLogicException(e);
		}
	}

	/**
	 * Returns CollectionProtocolRegistration object if it exist in collection.
	 * 
	 * @param collprotId
	 *            - Long
	 * @param cprColl
	 *            - Collection of protocol registration.
	 * @return CollectionProtocolRegistration object.
	 */
	private CollectionProtocolRegistration getCollectionProtocolRegistrationOld(long collprotId,
			Collection<CollectionProtocolRegistration> cprColl)
	{
		CollectionProtocolRegistration collProtReg = null;
		final Iterator<CollectionProtocolRegistration> itCPRColl = cprColl.iterator();
		while (itCPRColl.hasNext())
		{
			final CollectionProtocolRegistration cprOnj = itCPRColl.next();
			final long cpId = cprOnj.getCollectionProtocol().getId();
			if (cpId == collprotId)
			{
				collProtReg = cprOnj;
			}
		}
		return collProtReg;
	}

	/**
	 * Updates the persistent object in the database.
	 * 
	 * @param dao
	 *            - DAO object
	 * @param obj
	 *            The object to be updated.
	 * @param oldObj
	 *            - Object
	 * @param sessionDataBean
	 *            The session in which the object is saved.
	 * @throws BizLogicException
	 *             throws BizLogicException
	 */
	protected void update(DAO dao, Object obj, Object oldObj, SessionDataBean sessionDataBean)
			throws BizLogicException
	{
		try
		{
			final Participant participant = (Participant) obj;
			final Participant oldParticipant = (Participant) oldObj;
			setPMI(participant);
			final CommonParticipantBizlogic comBizLogic = new CommonParticipantBizlogic();
			comBizLogic.modifyParticipantObject(dao, sessionDataBean, participant, oldParticipant);
			updateCPR(dao, sessionDataBean, participant, oldParticipant);
			logger.debug("participant.getActivityStatus() " + participant.getActivityStatus());
			if (participant.getActivityStatus().equals(Status.ACTIVITY_STATUS_DISABLED.toString()))
			{
				disableObject(dao, participant);
			}

            Iterator<ParticipantMedicalIdentifier> oldMrnitr = oldParticipant.getParticipantMedicalIdentifierCollection().iterator();
               while (oldMrnitr.hasNext())
               {
                   ParticipantMedicalIdentifier oldMRNObj = oldMrnitr.next();
                   boolean mrnFoundFlag = false;
                   Iterator<ParticipantMedicalIdentifier> newMrnitr = participant.getParticipantMedicalIdentifierCollection().iterator();
                  while(newMrnitr.hasNext())
                   {
                      ParticipantMedicalIdentifier newMRNObj = newMrnitr.next();
                       if (oldMRNObj!=null && newMRNObj!=null && oldMRNObj.getId().compareTo(newMRNObj.getId())==0)
                       {
                           mrnFoundFlag = true;
                           break;
                       }
                   }
                   if (!mrnFoundFlag)
                   {  
                           dao.delete(oldMRNObj);
                      
                   }

               }
     		   auditParticipant(participant, dao, sessionDataBean,Operation.UPDATE.toString());
			
			
		}catch (DAOException daoExp) {
		    logger.error(daoExp.getMessage(), daoExp);
            throw this
                    .getBizLogicException(daoExp, daoExp.getErrorKeyName(), daoExp.getMsgValues());
		}
		catch (final BizLogicException daoExp)
		{
			logger.error(daoExp.getMessage(), daoExp);
			throw this
					.getBizLogicException(daoExp, daoExp.getErrorKeyName(), daoExp.getMsgValues());
		}
	}

	private void auditParticipant(Participant participant, DAO dao, SessionDataBean sessionDataBean,String operation) throws DAOException {
		Collection<CollectionProtocolRegistration> cprColl = participant.getCollectionProtocolRegistrationCollection();
		if(cprColl != null){
			for (CollectionProtocolRegistration cpr : cprColl) {
				Audit audit = getAuditObject(cpr, sessionDataBean,operation);
				dao.insert(audit);
			}
		}
	}

	private Audit getAuditObject(CollectionProtocolRegistration cpr, SessionDataBean sessionDataBean, String operation) {
		Audit audit = new Audit();
		audit.setCpId(cpr.getCollectionProtocol().getId());
		audit.setObjectId(cpr.getParticipant().getId());
		audit.setObjectType(ObjectType.PARTICIPANT.toString());
		audit.setOperation(operation);
		audit.setUpdatedDate(new Date());
		audit.setIpAddress(sessionDataBean.getIpAddress());
		audit.setUserId(sessionDataBean.getUserId());
		audit.setReasonForChange(Operation.INSERT.toString());
		return audit;
	}
	/**
	 * This method is for Setting PMI.
	 * 
	 * @param participant
	 *            Participant Object
	 */
	private void setPMI(final Participant participant)
	{
		final Collection<ParticipantMedicalIdentifier> pmiColl = participant
				.getParticipantMedicalIdentifierCollection();
		final Iterator<ParticipantMedicalIdentifier> iterator = pmiColl.iterator();
		while (iterator.hasNext())
		{
			final ParticipantMedicalIdentifier pmIdentifier = iterator.next();
			ApiSearchUtil.setParticipantMedicalIdentifierDefault(pmIdentifier);
			pmIdentifier.setParticipant(participant);
		}
	}

	/**
	 * Disable Object, If Activity Status = Disabled
	 * 
	 * @param dao
	 *            DAO Object
	 * @param participant
	 *            participant Object
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void disableObject(DAO dao, final Participant participant) throws BizLogicException
	{
		logger.debug("participant.getActivityStatus() " + participant.getActivityStatus());
		final Long participantIDArr[] = {participant.getId()};

		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final CollectionProtocolRegistrationBizLogic bizLogic = (CollectionProtocolRegistrationBizLogic) factory
				.getBizLogic(Constants.COLLECTION_PROTOCOL_REGISTRATION_FORM_ID);
		bizLogic.disableRelatedObjectsForParticipant(dao, participantIDArr);
	}

	/**
	 * Update CollectionProtocolRegistration
	 * 
	 * @param dao
	 *            DAO Object
	 * @param sessionDataBean
	 *            SessionDataBean Object
	 * @param participant
	 *            Participant Object
	 * @param oldParticipant
	 *            Old Participant Object
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void updateCPR(DAO dao, SessionDataBean sessionDataBean, final Participant participant,
			final Participant oldParticipant) throws BizLogicException
	{
		final CollectionProtocolRegistrationBizLogic cprBizLogic = new CollectionProtocolRegistrationBizLogic();
		final Collection<CollectionProtocolRegistration> oldCPRColl = oldParticipant
				.getCollectionProtocolRegistrationCollection();
		final Collection<CollectionProtocolRegistration> cprCollection = participant
				.getCollectionProtocolRegistrationCollection();
		final Iterator<CollectionProtocolRegistration> itCPRColl = cprCollection.iterator();
		while (itCPRColl.hasNext())
		{
			final CollectionProtocolRegistration collectionProtReg = itCPRColl.next();
			ApiSearchUtil.setCollectionProtocolRegistrationDefault(collectionProtReg);
			if (collectionProtReg.getCollectionProtocol().getId() != null
					&& !collectionProtReg.equals(""))
			{
				collectionProtReg.setParticipant(participant);
				final CollectionProtocolRegistration oldCPR = (CollectionProtocolRegistration) this
						.getCorrespondingOldObject(oldCPRColl, collectionProtReg.getId());
				if (collectionProtReg.getId() == null) // If Collection Protocol
				// Registration is not
				// happened for given
				// participant
				{
					collectionProtReg.setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.toString());
					cprBizLogic.insert(collectionProtReg, dao, sessionDataBean);
					cprIdList.add(collectionProtReg.getId());
					continue;
				}
				cprBizLogic.update(dao, collectionProtReg, oldCPR, sessionDataBean);
			}
		}
	}

	/**
	 * This method check for duplicate collection protocol registration for
	 * given participant.
	 * 
	 * @param cprColl
	 *            - Collection of cpr objects
	 * @return boolean value based on duplicate collection protocol
	 */
	private boolean isDuplicateCollectionProtocol(Collection<CollectionProtocolRegistration> cprColl)
	{
		boolean isCPDuplicate = false;
		final Collection<CollectionProtocolRegistration> newCPRColl = new LinkedHashSet<CollectionProtocolRegistration>();
		if (cprColl != null)
		{
			final Iterator<CollectionProtocolRegistration> crpItr = cprColl.iterator();
			while (crpItr.hasNext())
			{
				final CollectionProtocolRegistration collProtReg = crpItr.next();
				if (collProtReg.getCollectionProtocol() != null && !collProtReg.equals(""))
				{
					final long collProtId = collProtReg.getCollectionProtocol().getId().longValue();
					if (this.isCollectionProtocolExist(newCPRColl, collProtId))
					{
						isCPDuplicate = true;
					}
					else
					{
						newCPRColl.add(collProtReg);
					}
				}
			}
		}
		return isCPDuplicate;
	}

	/**
	 * @param cprColl
	 *            : collectionProtocolRegistrationCollection.
	 * @param collProtId
	 *            : collectinProtocolId.
	 * @return boolean value based on CP.
	 */
	private boolean isCollectionProtocolExist(Collection<CollectionProtocolRegistration> cprColl,
			long collProtId)
	{
		boolean isCollProtExist = false;
		final Iterator<CollectionProtocolRegistration> itrCPR = cprColl.iterator();
		while (itrCPR.hasNext())
		{
			final CollectionProtocolRegistration collProtReg = itrCPR.next();
			final long cpId = collProtReg.getCollectionProtocol().getId().longValue();
			if (cpId == collProtId)
			{
				isCollProtExist = true;
			}
		}
		return isCollProtExist;
	}

	/**
	 * @param dao
	 *            : DAO object.
	 * @param cprId
	 *            - Long
	 * @return boolean value
	 * @throws BizLogicException
	 *             - BizLogicException
	 */
	protected boolean isSpecimenExistsForRegistration(DAO dao, Long cprId) throws BizLogicException
	{
		boolean flag = false;
		final String hql = " select " + " elements(scg.specimenCollection) " + "from "
				+ " edu.wustl.catissuecore.domain.CollectionProtocolRegistration as cpr"
				+ ", edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg"
				+ ", edu.wustl.catissuecore.domain.Specimen as s" + " where cpr.id = " + cprId
				+ " and " + " cpr.id = scg.collectionProtocolRegistration.id and"
				+ " scg.id = s.specimenCollectionGroup.id and " + " s.activityStatus = '"
				+ Status.ACTIVITY_STATUS_ACTIVE.toString() + "' " + "and s.collectionStatus = '"
				+ Constants.COLLECTION_STATUS_COLLECTED + "'";

		final List scgList = this.executeHqlQuery(dao, hql);
		if ((scgList != null) && (scgList).size() > 0)
		{
			flag = true;
		}
		else
		{
			flag = false;
		}
		return flag;
	}

	/**
	 * Changed for bug 14350
	 * 
	 * @param dao
	 *            - DAO object
	 * @param participantId
	 *            - participant Id
	 * @return if collected specimens present or not
	 * @throws BizLogicException
	 *             - BizLogicException
	 */
	protected boolean isCollectedSpecimenExists(DAO dao, Long participantId)
			throws BizLogicException
	{
		boolean isCollSpecExists = false;
		final String hql = "select count(s.id) " + "from"
				+ " edu.wustl.catissuecore.domain.Participant as p"
				+ ",edu.wustl.catissuecore.domain.CollectionProtocolRegistration as cpr"
				+ ", edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg"
				+ ", edu.wustl.catissuecore.domain.Specimen as s" + " where p.id = "
				+ participantId + " and" + " p.id = cpr.participant.id and "
				+ " cpr.id = scg.collectionProtocolRegistration.id and"
				+ " scg.id = s.specimenCollectionGroup.id and " + " s.activityStatus = '"
				+ Status.ACTIVITY_STATUS_ACTIVE.toString() + "' and s.collectionStatus = '"
				+ Constants.COLLECTION_STATUS_COLLECTED + "'";

		final List specimenList = this.executeHqlQuery(dao, hql);
		if ((specimenList != null) && !specimenList.isEmpty()
				&& !specimenList.get(0).toString().equals("0"))
		{
			isCollSpecExists = true;
		}
		else
		{
			isCollSpecExists = false;
		}
		return isCollSpecExists;

	}

	/**
	 * @param dao
	 *            : DAO object. Overriding the parent class's method to validate
	 *            the enumerated attribute values.
	 */
	protected boolean validate(Object obj, DAO dao, String operation) throws BizLogicException
	{
		final Participant participant = (Participant) obj;
		final Validator validator = new Validator();
		try
		{
			CommonParticipantBizlogic.validate(participant, dao, operation, validator);
		}
		catch (BizLogicException exp)
		{
			throw getBizLogicException(null, exp.getMsgValues(), exp.getCustomizedMsg());
		}
		final Collection<CollectionProtocolRegistration> cprCollection = validateCPR(dao,
				participant, validator);
		final boolean isCPDuplicate = this.isDuplicateCollectionProtocol(cprCollection);
		if (isCPDuplicate)
		{
			throw this.getBizLogicException(null,
					"errors.participant.duplicate.collectionProtocol", "");
		}
		if (participant.getActivityStatus().equalsIgnoreCase(Constants.DISABLED))
		{
			final boolean isSpecimenExist = this
					.isCollectedSpecimenExists(dao, participant.getId());
			if (isSpecimenExist)
			{
				throw this.getBizLogicException(null, "participant.specimen.exists", "");
			}

		}
		final Collection paticipantMedCol = participant.getParticipantMedicalIdentifierCollection();
        
        if (paticipantMedCol != null && !paticipantMedCol.isEmpty())
        {
	            final Iterator itr = paticipantMedCol.iterator();
	            java.util.HashSet<Long> siteNameset = new java.util.HashSet<Long>();
	            while (itr.hasNext())
	            {
	                ParticipantMedicalIdentifier partiMedobj = (ParticipantMedicalIdentifier) itr
	                        .next();
	                Site site = partiMedobj.getSite();
	                if (site != null)
	                {
	                   
	                    boolean checkDuplicate = siteNameset.add(site.getId());
	                    if (!checkDuplicate)
	                    {
	                        //duplicate site present in collection , so find old one delete that one as well.
	                        throw new BizLogicException(null, null,
	                            "errors.participant.mediden.duplicate", "");
	                    }
	                }
	            }
        }

		return true;
	}

	/**
	 * Validate Collection ProtocolRegistration
	 * 
	 * @param dao
	 *            DAO Object
	 * @param participant
	 *            participant object
	 * @param validator
	 *            Validator Object
	 * @return CollectionProtocolRegistration
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private Collection<CollectionProtocolRegistration> validateCPR(DAO dao,
			final Participant participant, final Validator validator) throws BizLogicException
	{
		final Collection<CollectionProtocolRegistration> cprColl = participant
				.getCollectionProtocolRegistrationCollection();
		if (cprColl != null && !cprColl.isEmpty())
		{
			final Iterator<CollectionProtocolRegistration> cprItr = cprColl.iterator();
			while (cprItr.hasNext())
			{
				final CollectionProtocolRegistration cpr = cprItr.next();
				checkCPAndCPR(dao, validator, cpr);
				getActivityStatus(dao, cpr);
			}
		}
		return cprColl;
	}

	/**
	 * Check Activity Status.
	 * 
	 * @param dao
	 *            DAO Object
	 * @param cpr
	 *            CollectionProtocolRegistration Object
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void getActivityStatus(DAO dao, final CollectionProtocolRegistration cpr)
			throws BizLogicException
	{
		if (cpr.getActivityStatus() != null
				&& cpr.getActivityStatus().equalsIgnoreCase(Constants.DISABLED))
		{

			final boolean isSpecimenExist = this.isSpecimenExistsForRegistration(dao, cpr.getId());
			if (isSpecimenExist)
			{
				throw this.getBizLogicException(null, "collectionprotocolregistration.scg.exists",
						"");
			}
		}
	}

	/**
	 * @param dao
	 *            DAO Object
	 * @param validator
	 *            Validator Object
	 * @param cpr
	 *            CollectionProtocolRegistration Object
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void checkCPAndCPR(DAO dao, final Validator validator,
			final CollectionProtocolRegistration cpr) throws BizLogicException
	{
		if (cpr.getCollectionProtocol() != null && !cpr.equals(""))
		{
			final String cprDate = Utility.parseDateToString(cpr.getRegistrationDate(),
					CommonServiceLocator.getInstance().getDatePattern());
			checkCpTitle(validator, cpr, cprDate);
			checkForCollectionProtocolIdentifier(dao, cpr);
		}
	}

	/**
	 * @param cprDate
	 *            CPR Date
	 * @param validator
	 *            Validator Object
	 * @param cpr
	 *            CollectionProtocolRegistration Object
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void checkCpTitle(final Validator validator, final CollectionProtocolRegistration cpr,
			final String cprDate) throws BizLogicException
	{
		final String errorKey = validator.validateDate(cprDate, true);
		if ((cpr.getCollectionProtocol().getId() == null && cpr.getCollectionProtocol().getTitle() == null)
				|| errorKey.trim().length() > 0)
		{
			throw this.getBizLogicException(null,
					"errors.participant.collectionProtocolRegistration.missing", "");
		}
	}

	/**
	 * Check For Collection Protocol Identifier.
	 * 
	 * @param dao
	 *            DAO
	 * @param cprID
	 *            CollectionProtocolRegistration
	 * @throws BizLogicException
	 *             BizLogicException
	 */
	private void checkForCollectionProtocolIdentifier(DAO dao,
			final CollectionProtocolRegistration cprID) throws BizLogicException
	{
		try
		{
			final String cpTitle = cprID.getCollectionProtocol().getTitle();
			final String sourceObjectName = CollectionProtocol.class.getName();
			final String[] selectColumnName = {"id", "activityStatus"};
			final QueryWhereClause queryWhereClause = new QueryWhereClause(sourceObjectName);
			if (cprID.getCollectionProtocol().getId() != null)
			{
				queryWhereClause.addCondition(new EqualClause("id", cprID.getCollectionProtocol()
						.getId()));
			}
			else if (cpTitle != null)
			{
				queryWhereClause.addCondition(new EqualClause("title", cpTitle));
			}
			final List list = dao.retrieve(sourceObjectName, selectColumnName, queryWhereClause);
			if (!list.isEmpty())
			{
				Object objCP[] = (Object[]) list.get(0);
				cprID.getCollectionProtocol().setId((Long) objCP[0]);
				cprID.getCollectionProtocol().setActivityStatus((String) objCP[1]);
				if (Constants.DISABLE.equals(cprID.getCollectionProtocol().getActivityStatus()))
				{
					throw this.getBizLogicException(null, "cp.disabled", "");
				}
				SynchronizeCollectionProtocolBizLogic synchronizeCollectionProtocolBizLogic = new SynchronizeCollectionProtocolBizLogic();
				CpSyncAudit cpSyncAudit = synchronizeCollectionProtocolBizLogic
						.getSyncStatus((Long) objCP[0]);
				if (cpSyncAudit != null)
				{
					String syncStatus = cpSyncAudit.getStatus();
					if ("In Process".equalsIgnoreCase(syncStatus))
					{
						throw this.getBizLogicException(null,
								"errors.collectionprotocolregistration.syncinprocess", "");
					}
				}
			}
			else
			{
				throw this.getBizLogicException(null, "cp.nt.found", "");
			}
		}
		catch (DAOException e)
		{
			throw this.getBizLogicException(null, "cp.nt.found", "");
		}
	}

	/**
	 * @param participant
	 *            - participant object.
	 * @param lookupLogic
	 *            - LookupLogic object
	 * @return - List of matched participant.
	 * @throws Exception
	 *             - throws exception.
	 */
	public List<DefaultLookupResult> getListOfMatchingParticipants(Participant participant,
			LookupLogic lookupLogic) throws Exception
	{
		final DefaultLookupParameters params = new DefaultLookupParameters();
		params.setObject(participant);
		return lookupLogic.lookup(params);
	}

	/**
	 * @return Map of all participant.
	 * @throws BizLogicException
	 *             throws BizLogicException
	 */
	public Map<Long, Participant> getAllParticipants() throws BizLogicException
	{
		DAO dao = null;
		final Map<Long, Participant> mapOfParticipants = new HashMap<Long, Participant>();
		try
		{
			dao = this.openDAOSession(null);
			final String partQueryStr = "from " + Participant.class.getName()
					+ " where activityStatus !='" + Status.ACTIVITY_STATUS_DISABLED.toString()
					+ "'";
			final List<Participant> listOfParticipants = dao.executeQuery(partQueryStr);
			if (listOfParticipants != null)
			{
				final Iterator<Participant> participantIterator = listOfParticipants.iterator();
				while (participantIterator.hasNext())
				{
					final Participant participant = participantIterator.next();
					final Participant cloneParticipant = new Participant(participant);
					final Long participantId = cloneParticipant.getId();
					mapOfParticipants.put(participantId, cloneParticipant);
				}
			}
		}
		catch (final DAOException e)
		{
			logger.error(e.getMessage(), e);
			throw this.getBizLogicException(e, e.getErrorKeyName(), e.getMsgValues());
		}
		finally
		{
			this.closeDAOSession(dao);
		}
		return mapOfParticipants;
	}

	/**
	 * This function takes identifier as parameter and returns corresponding
	 * Participant.
	 * 
	 * @param identifier
	 *            Participantidentifier
	 * @return - Participant object
	 */
	public Participant getParticipantById(Long identifier) throws Exception
	{
		final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
		final IBizLogic bizLogic = factory.getBizLogic(Constants.DEFAULT_BIZ_LOGIC);
		final String sourceObjectName = Participant.class.getName();
		return (Participant) bizLogic.retrieve(sourceObjectName, identifier);

	}

	/**
	 * @param columnList
	 *            - List
	 * @param partMRNColName
	 *            - StringBuffer
	 * @return List column list
	 * @throws BizLogicException
	 *             throws BizLogicException
	 */
	public List getColumnList(List<String> columnList, StringBuffer partMRNColName)
			throws BizLogicException
	{
		final List<String> displayList = new ArrayList<String>();
		JDBCDAO jdbcDao = null;
		try
		{
			jdbcDao = this.openJDBCSession();
			jdbcDao.openSession(null);
			final String sql = "SELECT  columnData.COLUMN_NAME,displayData.DISPLAY_NAME FROM "
					+ "CATISSUE_INTERFACE_COLUMN_DATA columnData,"
					+ "CATISSUE_TABLE_RELATION relationData,"
					+ "CATISSUE_QUERY_TABLE_DATA tableData,"
					+ "CATISSUE_SEARCH_DISPLAY_DATA displayData "
					+ "where relationData.CHILD_TABLE_ID = columnData.TABLE_ID and "
					+ "relationData.PARENT_TABLE_ID = tableData.TABLE_ID and "
					+ "relationData.RELATIONSHIP_ID = displayData.RELATIONSHIP_ID and "
					+ "columnData.IDENTIFIER = displayData.COL_ID and"
					+ " tableData.ALIAS_NAME = 'Participant'";

			logger.debug("DATA ELEMENT SQL : " + sql);
			final List list = jdbcDao.executeQuery(sql);
			final Iterator<String> iterator1 = columnList.iterator();

			while (iterator1.hasNext())
			{
				final String colName1 = iterator1.next();
				logger.debug("colName1------------------------" + colName1);
				final Iterator iterator2 = list.iterator();
				while (iterator2.hasNext())
				{
					final List<String> rowList = (List) iterator2.next();
					final String colName2 = rowList.get(0);
					logger.debug("colName2------------------------" + colName2);
					if (colName1.equals(colName2))
					{
						if (colName1.equals(Constants.PARTICIPANT_MEDICAL_RECORD_NO))
						{
							partMRNColName.append(rowList.get(1));
						}
						displayList.add(rowList.get(1));
					}
				}
			}
			jdbcDao.closeSession();
		}
		catch (final DAOException exp)
		{
			logger.error(exp.getMessage(), exp);
			throw this.getBizLogicException(exp, exp.getErrorKeyName(), exp.getMsgValues());
		}
		finally
		{
			this.closeJDBCSession(jdbcDao);
		}

		return displayList;
	}

	/**
	 * @return page row to show.
	 */
	public String getPageToShow()
	{
		return "";
	}

	/**
	 * @return List of matching objects.
	 */
	public List<Object> getMatchingObjects()
	{
		return new ArrayList<Object>();
	}

	/**
	 * Executes hql Query and returns the list of associated scg id.
	 * 
	 * @param participant
	 *            Participant
	 * @return List of SCG
	 * @throws BizLogicException
	 *             DAOException.
	 */
	public List getSCGList(Long participantId) throws BizLogicException
	{
		DAO dao = null;
		try
		{
			final String scgHql = "select scg.id, scg.surgicalPathologyNumber,"
					+ " scg.identifiedSurgicalPathologyReport.id,scg.name "
					+ " from edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg, "
					+ " edu.wustl.catissuecore.domain.CollectionProtocolRegistration as cpr,"
					+ " edu.wustl.catissuecore.domain.Participant as p " + " where p.id = "
					+ participantId + " and p.id = cpr.participant.id "
					+ " and scg.id in elements(cpr.specimenCollectionGroupCollection)";

			dao = this.openDAOSession(null);
			List list = null;
			list = dao.executeQuery(scgHql);
			return list;
		}
		catch (final DAOException exp)
		{
			logger.error(exp.getMessage(), exp);
			throw this.getBizLogicException(exp, exp.getErrorKeyName(), exp.getMsgValues());
		}
		finally
		{
			this.closeDAOSession(dao);
		}
	}

	/**
	 * Executes hql Query and returns the results.
	 * 
	 * @param hql
	 *            String hql
	 * @param dao
	 *            - DAO object
	 * @return list of objects
	 * @throws BizLogicException
	 *             DAOException
	 */
	private List executeHqlQuery(DAO dao, String hql) throws BizLogicException
	{
		try
		{
			return dao.executeQuery(hql);
		}
		catch (final DAOException daoExp)
		{
			logger.error(daoExp.getMessage(), daoExp);
			daoExp.printStackTrace();
			throw this
					.getBizLogicException(daoExp, daoExp.getErrorKeyName(), daoExp.getMsgValues());
		}

	}

	/**
	 * @param userId
	 *            - Long.
	 * @return list of CP for users with registration access
	 * @throws BizLogicException
	 *             throws BizLogicException
	 */
	public List getCPForUserWithRegistrationAcess(long userId) throws BizLogicException
	{
		final List<NameValueBean> cpList = new ArrayList<NameValueBean>();
		final Set<Long> cpIds = new HashSet<Long>();
		cpList.add(new NameValueBean(Constants.SELECT_OPTION, "-1"));
		DAO dao = null;
		try
		{
			dao = this.openDAOSession(null);
			final User user = (User) dao.retrieveById(User.class.getName(), userId);
			final PrivilegeManager privilegeManager = PrivilegeManager.getInstance();
			final PrivilegeCache privilegeCache = privilegeManager.getPrivilegeCache(user
					.getLoginName());
			final Collection<CollectionProtocol> cpCollection = user
					.getAssignedProtocolCollection();
			if (cpCollection != null && !cpCollection.isEmpty())
			{
				for (final CollectionProtocol cp : cpCollection)
				{
					final StringBuffer stringBuff = new StringBuffer();
					stringBuff.append(CollectionProtocol.class.getName()).append("_")
							.append(cp.getId());
					final boolean hasPrivilege = privilegeCache.hasPrivilege(stringBuff.toString(),
							Variables.privilegeDetailsMap.get(Constants.CP_BASED_VIEW_FILTRATION));

					if (hasPrivilege)
					{
						cpList.add(new NameValueBean(cp.getShortTitle(), cp.getId()));
						cpIds.add(cp.getId());
					}
				}
			}
			final IFactory factory = AbstractFactoryConfig.getInstance().getBizLogicFactory();
			final UserBizLogic userBizLogic = (UserBizLogic) factory
					.getBizLogic(Constants.USER_FORM_ID);
			final Set<Long> siteIds = userBizLogic.getRelatedSiteIds(userId);

			if (siteIds != null && !siteIds.isEmpty())
			{
				final SiteBizLogic siteBizLogic = (SiteBizLogic) factory
						.getBizLogic(Constants.SITE_FORM_ID);
				for (final Long siteId : siteIds)
				{
					final String peName = Constants.getCurrentAndFuturePGAndPEName(siteId);
					if (privilegeCache.hasPrivilege(peName,
							Variables.privilegeDetailsMap.get(Constants.CP_BASED_VIEW_FILTRATION)))
					{
						final Collection<CollectionProtocol> cp1Collection = siteBizLogic
								.getRelatedCPs(siteId, dao);

						if (cp1Collection != null && !cp1Collection.isEmpty())
						{
							final List<NameValueBean> list = new ArrayList<NameValueBean>();
							for (final CollectionProtocol cp1 : cp1Collection)
							{
								if (!cpIds.contains(cp1.getId()))
								{
									list.add(new NameValueBean(cp1.getShortTitle(), cp1.getId()));
								}
							}
							cpList.addAll(list);
						}

					}
				}
			}

		}
		catch (final DAOException e)
		{
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			throw this.getBizLogicException(e, e.getErrorKeyName(), e.getMsgValues());
		}
		catch (final SMException e)
		{
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			throw AppUtility.handleSMException(e);
		}
		finally
		{
			this.closeDAOSession(dao);
		}

		return cpList;
	}

	/**
	 * Called from DefaultBizLogic to get ObjectId for authorization check.
	 * (non-Javadoc)
	 * 
	 * @see edu.wustl.common.bizlogic.DefaultBizLogic#getObjectId(edu.wustl.common.dao.DAO,
	 *      java.lang.Object)
	 */
	public String getObjectId(DAO dao, Object domainObject) throws BizLogicException
	{
		final String objectId = Constants.ADD_GLOBAL_PARTICIPANT;

		if (domainObject instanceof Participant)
		{
			final Participant participant = (Participant) domainObject;
			final Collection<CollectionProtocolRegistration> cprCollection = participant
					.getCollectionProtocolRegistrationCollection();
			if (cprCollection != null && cprCollection.isEmpty())
			{
				return objectId;
			}

			else
			{
				final StringBuffer stringBuffer = new StringBuffer();
				boolean isNewCPRPresent = false;

				if (cprCollection != null && !cprCollection.isEmpty())
				{
					stringBuffer.append(Constants.COLLECTION_PROTOCOL_CLASS_NAME);
					for (final CollectionProtocolRegistration cpr : cprCollection)
					{
//						if (cpr.getId() == null)
							if (cpr.getCollectionProtocol() == null)
							{
								return objectId;
							}
							checkForCollectionProtocolIdentifier(dao, cpr);
							stringBuffer.append("_").append(cpr.getCollectionProtocol().getId());
							isNewCPRPresent = true;
					}
				}
				if (isNewCPRPresent)
				{
					return stringBuffer.toString();
				}
			}
		}
		return objectId;
	}

	/**
	 * To get PrivilegeName for authorization check from
	 * 'PermissionMapDetails.xml'. (non-Javadoc)
	 * 
	 * @see edu.wustl.common.bizlogic.DefaultBizLogic#getPrivilegeName(java.lang.Object)
	 */
	protected String getPrivilegeKey(Object domainObject)
	{
		return Constants.ADD_EDIT_PARTICIPANT;
	}

	/**
	 * Over-ridden for the case of Non - Admin user should be able to Add Global
	 * Participant
	 * 
	 * @throws UserNotAuthorizedException
	 * @see edu.wustl.common.bizlogic.DefaultBizLogic
	 *      #isAuthorized(edu.wustl.common.dao.DAO, java.lang.Object,
	 *      edu.wustl.common.beans.SessionDataBean)
	 */
	public boolean isAuthorized(DAO dao, Object domainObject, SessionDataBean sessionDataBean)
			throws BizLogicException
	{
		boolean isAuthorized = false;
		try
		{
			if (sessionDataBean != null && sessionDataBean.isAdmin())
			{
				return true;
			}

			final String privilegeName = this.getPrivilegeName(domainObject);
			final String protEltName = this.getObjectId(dao, domainObject);
			final PrivilegeCache privilegeCache = PrivilegeManager.getInstance().getPrivilegeCache(
					sessionDataBean.getUserName());
			if (protEltName.equals(Constants.ADD_GLOBAL_PARTICIPANT))
			{
				User user = null;
				try
				{
					user = (User) dao.retrieveById(User.class.getName(),
							sessionDataBean.getUserId());
				}
				catch (final DAOException e)
				{
					logger.error(e.getMessage(), e);
					e.printStackTrace();
				}
				final Collection<CollectionProtocol> cpCollection = user
						.getAssignedProtocolCollection();
				if (cpCollection != null && !cpCollection.isEmpty())
				{
					for (final CollectionProtocol cp : cpCollection)
					{
						if (privilegeCache.hasPrivilege(CollectionProtocol.class.getName() + "_"
								+ cp.getId(), privilegeName))
						{
							isAuthorized = true;
							break;
						}
					}
					if (!isAuthorized)
					{
						isAuthorized = AppUtility.checkForAllCurrentAndFutureCPs(privilegeName,
								sessionDataBean, null);
					}
				}
				else
				{
					isAuthorized = AppUtility.checkForAllCurrentAndFutureCPs(privilegeName,
							sessionDataBean, null);
				}
			}
			else
			{
				final String[] prArray = protEltName.split("_");
				final String baseObjectId = prArray[0];
				String objId = "";
				for (int i = 1; i < prArray.length; i++)
				{
					objId = baseObjectId + "_" + prArray[i];
					isAuthorized = privilegeCache.hasPrivilege(objId, privilegeName);
					if (!isAuthorized)
					{
						break;
					}
				}

			}

			if (isAuthorized)
			{
				return isAuthorized;
			}
			else
			// Check for ALL CURRENT & FUTURE CASE
			{
				if (!protEltName.equals(Constants.ADD_GLOBAL_PARTICIPANT))
				{
					final String protEltNames[] = protEltName.split("_");

					final Long cpId = Long.valueOf(protEltNames[1]);
					final Set<Long> cpIdSet = new UserBizLogic().getRelatedCPIds(
							sessionDataBean.getUserId(), false);

					if (cpIdSet.contains(cpId))
					{
						// bug 11611 and 11659
						throw AppUtility.getUserNotAuthorizedException(privilegeName, protEltName,
								domainObject.getClass().getSimpleName());
					}
					isAuthorized = AppUtility.checkForAllCurrentAndFutureCPs(privilegeName,
							sessionDataBean, protEltNames[1]);
				}
			}
			if (!isAuthorized)
			{
				// bug 11611 and 11659
				throw AppUtility.getUserNotAuthorizedException(privilegeName, protEltName,
						domainObject.getClass().getSimpleName());
			}
		}
		catch (final SMException e1)
		{
			logger.error(e1.getMessage(), e1);
			e1.printStackTrace();
		}
		return isAuthorized;
	}

	@Override
	public boolean isReadDeniedTobeChecked()
	{
		return true;
	}

	@Override
	public String getReadDeniedPrivilegeName()
	{
		return Permissions.REGISTRATION + "," + Permissions.READ_DENIED;
	}

	public boolean hasPrivilegeToView(String objName, Long identifier,
			SessionDataBean sessionDataBean)
	{
		return AppUtility.hasPrivilegeToView(objName, identifier, sessionDataBean,
				this.getReadDeniedPrivilegeName())
				|| AppUtility.hasPrivilegeToView(objName, identifier, sessionDataBean,
						Permissions.SPECIMEN_PROCESSING);
	}

	/**
	 * Returns a list of Specimen objects with their IDs set as TiTLi needs only
	 * instance IDs in order to refresh indexes.
	 * 
	 * @param scg
	 *            the SpecimenCollectionGroup instance
	 * @return list of Specimen objects
	 * @throws BizLogicException
	 *             throws BizLogicException
	 */
	private List<Specimen> getSpecimenCollection(SpecimenCollectionGroup scg)
			throws BizLogicException
	{
		final String hql = " select s.id from edu.wustl.catissuecore.domain.Specimen s"
				+ " where s.specimenCollectionGroup.id=" + scg.getId();

		DAO dao = null;

		final List<Specimen> specimens = new ArrayList<Specimen>();
		try
		{
			dao = this.openDAOSession(null);
			final List specimenIds = dao.executeQuery(hql);
			if (specimenIds != null && (!specimenIds.isEmpty()))
			{
				for (final Iterator<Long> it = specimenIds.iterator(); it.hasNext();)
				{
					final Specimen specimen = new Specimen();
					specimen.setId(it.next());
					specimens.add(specimen);
				}
			}
		}
		catch (final Exception e)
		{
			logger.error("Error occured while retrieving Specimen List" + e.getMessage(), e);
			e.printStackTrace();
		}
		finally
		{
			this.closeDAOSession(dao);
		}
		return specimens;
	}

	/**
	 * Refresh Titli index.
	 * 
	 * @param operation
	 *            Add/Edit
	 * @param obj
	 *            Participant object
	 */
	public void refreshTitliSearchIndexSingle(String operation, Object obj)
	{
		try
		{
			super.refreshTitliSearchIndexSingle(operation, obj);
			final Participant participant = (Participant) obj;
			final Collection<CollectionProtocolRegistration> cprCollection = participant
					.getCollectionProtocolRegistrationCollection();
			if (cprCollection != null)
			{
				final Iterator<CollectionProtocolRegistration> itcprCollection = cprCollection
						.iterator();

				while (itcprCollection.hasNext())
				{
					final CollectionProtocolRegistration cpr = itcprCollection.next();
					if (cprIdList.contains(cpr.getId()))
					{
						final Collection<SpecimenCollectionGroup> specimenCollectionGroupCollection = cpr
								.getSpecimenCollectionGroupCollection();

						if (specimenCollectionGroupCollection != null)
						{
							final Iterator<SpecimenCollectionGroup> itscgCollection = specimenCollectionGroupCollection
									.iterator();
							while (itscgCollection.hasNext())
							{
								final SpecimenCollectionGroup scg = itscgCollection.next();
								super.refreshTitliSearchIndexSingle(operation, scg);
								final Collection<Specimen> specimenCollection = this
										.getSpecimenCollection(scg);
								if (specimenCollection != null)
								{
									final Iterator<Specimen> itspecimenCollection = specimenCollection
											.iterator();
									while (itspecimenCollection.hasNext())
									{
										final Specimen specimen = itspecimenCollection.next();
										super.refreshTitliSearchIndexSingle(operation, specimen);
									}
								}

							}
						}
					}
				}
			}
		}
		catch (final BizLogicException exp)
		{
			logger.error(exp.getMessage());
			exp.printStackTrace();
		}
	}

	public Long registerParticipant(String userName,Participant participant)
      throws ApplicationException
	{
		this.insert(participant, AppUtility.getSessionDataBean(userName));
		return participant.getId();
	
	}
	/**
	 * logic: check whether this participant object is having
	 * 
	 * @param participant
	 *            :participant.
	 * @param cpid
	 *            : cpid
	 * @param sessionDataBean
	 *            :sessionDataBean
	 * @throws ApplicationException
	 *             : ApplicationException
	 */
	public Long registerParticipant(Object object, Long cpid, String userName)
			throws ApplicationException
	{
		final Participant participant = (Participant) object;
		try
		{
			String operation = Constants.ADD;
			List resultList = new ArrayList<Long>();
			if (participant.getId() != null)
			{

				QueryWhereClause queryWhereClause = new QueryWhereClause(
						Participant.class.getName());
				final String[] selectColumnName = {"protocolParticipantIdentifier"};
				queryWhereClause = new QueryWhereClause(
						CollectionProtocolRegistration.class.getName());
				queryWhereClause.addCondition(
						new EqualClause("participant.id", participant.getId())).andOpr();
				queryWhereClause.addCondition(new EqualClause("collectionProtocol.id", cpid));
				resultList = new ArrayList<Long>();
				resultList = this.retrieve(CollectionProtocolRegistration.class.getName(),
						selectColumnName, queryWhereClause);
				operation = Constants.EDIT;
			}

			if (resultList == null || resultList.isEmpty())
			{
				this.addEditParticipant(participant, cpid, userName, operation);
			}
		}
		catch (final DAOException daoEx)
		{
			logger.error(daoEx.getMessage(), daoEx);
			daoEx.printStackTrace();
			throw new BizLogicException(daoEx);
		}
		return participant.getId();

	}

	/**
	 * @param participant
	 *            : participant.
	 * @param cpid
	 *            : cpid
	 * @param userName
	 *            : username.
	 * @param operation
	 *            :operation
	 * @throws BizLogicException
	 *             :BizLogicException
	 * @throws ApplicationException
	 *             : ApplicationException
	 */
	private void addEditParticipant(Participant participant, Long cpid, String userName,
			String operation) throws BizLogicException, ApplicationException
	{
		final Collection<CollectionProtocolRegistration> cprColl = participant
				.getCollectionProtocolRegistrationCollection();
		String protocolParticipantIdentifier = "";
		for (CollectionProtocolRegistration collectionProtocolRegistration : cprColl)
		{
			protocolParticipantIdentifier = collectionProtocolRegistration
					.getProtocolParticipantIdentifier();
		}
		final CollectionProtocolRegistration cpr = new CollectionProtocolRegistration();
		cpr.setActivityStatus(Constants.ACTIVITY_STATUS_VALUES[1]);
		final CollectionProtocol collectionProtocol = new CollectionProtocol();
		collectionProtocol.setId(cpid);
		cpr.setCollectionProtocol(collectionProtocol);
		cpr.setProtocolParticipantIdentifier(protocolParticipantIdentifier);
		cpr.setParticipant(participant);
		final Collection<CollectionProtocolRegistration> colProtoRegColn = new HashSet<CollectionProtocolRegistration>();
		colProtoRegColn.add(cpr);
		cpr.setRegistrationDate(new Date());
		participant.setCollectionProtocolRegistrationCollection(colProtoRegColn);
		if (operation.equals(Constants.ADD))
		{
			this.insert(participant, AppUtility.getSessionDataBean(userName));
		}
		else
		{
			this.updateParticipant(userName, participant);
		}
	}

	/**
	 * @param userName
	 *            : user name.
	 * @param participant
	 *            : participant
	 * @throws BizLogicException
	 *             : BizLogicException
	 */
	public void updateParticipant(String userName, Participant participant)
			throws BizLogicException
	{
		HibernateDAO hibernateDao = null;
		try
		{
			final String appName = this.getAppName();
			hibernateDao = (HibernateDAO) DAOConfigFactory.getInstance().getDAOFactory(appName)
					.getDAO();
			hibernateDao.openSession(null);
			AbstractDomainObject abstractDomainOld;
			abstractDomainOld = (AbstractDomainObject) hibernateDao.retrieveById(
					Participant.class.getName(), participant.getId());
			this.update(participant, abstractDomainOld, AppUtility.getSessionDataBean(userName));
		}
		catch (final DAOException e)
		{
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			throw new BizLogicException(ErrorKey.getErrorKey("common.errors.item"), e,
					"Error while opening the session");
		}
		finally
		{
			try
			{
				hibernateDao.closeSession();
			}
			catch (final Exception e)
			{
				logger.error(e.getMessage(), e);
				e.printStackTrace();
				throw new BizLogicException(ErrorKey.getErrorKey("common.errors.item"), e,
						"Failed while updating ");
			}
		}
	}

	/**
	 * This method will return the id of the participant whose ppi is as given
	 * in PPI in the given CP.
	 * 
	 * @param cpLabel
	 *            collection protocol Label.
	 * @param ppi
	 *            ppi of the participant.
	 * @return id of the paticiapnt.
	 * @throws BizLogicException
	 *             exception.
	 */
	public Long getParticipantIdByPPI(String cpLabel, String ppi) throws ApplicationException
	{
		Long participantId = null;
		if (ppi != null && cpLabel != null)
		{
			final String hql = "select cpr.participant.id from "
					+ CollectionProtocolRegistration.class.getName()
					+ " as cpr where cpr.protocolParticipantIdentifier = ? and cpr.collectionProtocol.shortTitle= ? and cpr.activityStatus <> '"
					+ Status.ACTIVITY_STATUS_DISABLED.toString() + "' ";

			List<ColumnValueBean> columnValueBean = new ArrayList<ColumnValueBean>();
			columnValueBean.add(new ColumnValueBean("ppi", ppi));
			columnValueBean.add(new ColumnValueBean("shortTitle", cpLabel));
			final List<Long> list = this.executeQuery(hql, columnValueBean);
			if (list == null || list.isEmpty())
			{
				throw new BizLogicException(ErrorKey.getErrorKey("invalid.ppi.participant"), null,
						ppi + ":" + cpLabel);
			}
			participantId = list.get(0);
		}

		return participantId;
	}

	/**
	 * @param consentDto
	 * @param disposeSpecimen
	 * @param dao
	 * @param sessionDataBean
	 * @throws BizLogicException
	 * Update participant response cpr consent and SCG and all specimen under particiapnt
	 */
	public void updateConsentResponse(ConsentResponseDto consentDto, boolean disposeSpecimen,
			DAO dao, SessionDataBean sessionDataBean) throws BizLogicException
	{
		try
		{

			Iterator<ConsentTierDTO> consentTierDtoIter = consentDto.getConsentTierList().iterator();

			//Below while loop is to update ConsentResponse Objects 
			//with participant response  depending on  consentTierDto list
			CollectionProtocolRegistration cprObj = null;
			while (consentTierDtoIter.hasNext())
			{
				ConsentTierDTO consentTierDto = consentTierDtoIter.next();
				Map<String, NamedQueryParam> params = new HashMap<String, NamedQueryParam>();
				params.put("0", new NamedQueryParam(DBTypes.LONG, consentDto.getConsentLevelId()));
				params.put("1", new NamedQueryParam(DBTypes.LONG, consentTierDto.getId()));
				Collection consentResponse = ((HibernateDAO) dao).executeNamedQuery(
						"selectCprConsentResponse", params);
				Iterator ite = consentResponse.iterator();
				if (ite.hasNext())
				{
					ConsentTierResponse responseObj = (ConsentTierResponse) ite.next();
					responseObj.setResponse(consentTierDto.getParticipantResponses());
					dao.update(responseObj);
				}else{
				    ConsentTier consentTier = new ConsentTier();
				    consentTier.setId(consentTierDto.getId());
				    if(cprObj == null){
				        cprObj = (CollectionProtocolRegistration)dao.retrieveById(CollectionProtocolRegistration.class.getName(), consentDto.getConsentLevelId());
				    }
				    ConsentTierResponse responseObj = new ConsentTierResponse(); 
				    responseObj.setResponse(consentTierDto.getParticipantResponses());
				    responseObj.setConsentTier(consentTier);
				    cprObj.getConsentTierResponseCollection().add(responseObj);
			
				}
			}

			//Below changes are for updateing cprObj with ConsentSignatureDate, witness,SignedConsentDocumentURL.			
			if(cprObj!=null){
			     dao.update(cprObj);
			}else{
    			cprObj = (CollectionProtocolRegistration) dao
    					.retrieveById(CollectionProtocolRegistration.class.getName(),
    							consentDto.getConsentLevelId());
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(CommonServiceLocator.getInstance()
					.getDatePattern(), CommonServiceLocator.getInstance().getDefaultLocale());

			cprObj.setConsentSignatureDate(consentDto.getConsentDate());

			cprObj.setSignedConsentDocumentURL(consentDto.getConsentUrl());
			if (consentDto.getWitnessId() != null && consentDto.getWitnessId() != -1)
			{
				User witness = new User();
				witness.setId(consentDto.getWitnessId());
				cprObj.setConsentWitness(witness);
			}
			dao.update(cprObj);

			//Below code is written to update all SCG under specimen.
            SpecimenCollectionGroupBizLogic scgBizLogic = new SpecimenCollectionGroupBizLogic();
            Iterator<SpecimenCollectionGroup> scgIterator = cprObj.getSpecimenCollectionGroupCollection().iterator();
            while (scgIterator.hasNext())
            {
                SpecimenCollectionGroup scgObj = scgIterator.next();
                scgBizLogic.updateScgConsentStatus(scgObj.getId(), consentDto.getConsentTierList(), disposeSpecimen,
                        dao, sessionDataBean);
            }

		}
		catch (DAOException e)
		{
			logger.error(e.getMsgValues());
			throw new BizLogicException(e.getErrorKey(), e, e.getMsgValues());
		}

	}

	/**
	 * This method will check whether the participant with given id exists or
	 * not.
	 * 
	 * @param participantId
	 *            participant Id.
	 * @return true if participant with given id exists.
	 * @throws BizLogicException
	 *             exception.
	 */
	public boolean isParticipantExists(String participantId) throws BizLogicException
	{
		final String hql = "select participant.id from " + Participant.class.getName()
				+ " as participant where participant.id = " + participantId
				+ " and participant.activityStatus <> '"
				+ Status.ACTIVITY_STATUS_DISABLED.toString() + "' ";

		final List<Long> list = this.executeQuery(hql);
		if (list == null || list.isEmpty())
		{
			throw new BizLogicException(ErrorKey.getErrorKey("invalid.id.participant"), null,
					participantId);
		}
		return true;
	}

	public ParticipantDTO getParticipantDTO(DAO dao, Long pId, Long cpId) throws BizLogicException
	{
		ParticipantDTO participantDTO = new ParticipantDTO();
		try
		{
			ColumnValueBean columnValueBean = new ColumnValueBean(pId);
			List<ColumnValueBean> columnValueBeans = new ArrayList();
			columnValueBeans.add(columnValueBean);

			String hql = "select participant.lastName,participant.firstName,participant.middleName,"
					+ "participant.birthDate,participant.gender,participant.socialSecurityNumber,"
					+ "participant.ethnicity from "
					+ Participant.class.getName()
					+ " as participant where participant.id = ?";
			List participants = dao.executeQuery(hql, columnValueBeans);
			Object[] participantInfo = (Object[]) participants.get(0);
			participantDTO.setLastName((String) participantInfo[0]);
			participantDTO.setFirstName((String) participantInfo[1]);
			Date dobdate = (Date) participantInfo[3];
			if (dobdate != null && !"".equals(dobdate))
			{
				participantDTO.setDob(dobdate);
			}
			participantDTO.setGender((String) participantInfo[4]);
			participantDTO.setSsn((String) participantInfo[5]);
			participantDTO.setEthnicity((String) participantInfo[6]);
			participantDTO.setParticipantId(pId);
			participantDTO.setCpId(cpId);

			String mrnhql = "select pmi.medicalRecordNumber ,site.name from "
					+ ParticipantMedicalIdentifier.class.getName() + " as pmi, "
					+ Site.class.getName()
					+ " as site where pmi.participant.id=? and pmi.site.id=site.id";
			List mrns = dao.executeQuery(mrnhql, columnValueBeans);
			List<MedicalRecordNumberDTO> mrnDtos = new ArrayList<MedicalRecordNumberDTO>();
			if (!mrns.isEmpty())
			{
				for (Object mrn : mrns)
				{
					Object[] mrnArray = (Object[]) mrn;
					MedicalRecordNumberDTO mrnDto = new MedicalRecordNumberDTO();
					mrnDto.setMrn(mrnArray[0].toString());
					mrnDto.setSiteName(mrnArray[1].toString());
					mrnDtos.add(mrnDto);
					//mrnBuffer.append(((Object[])mrn)[1]).append("(").append(((Object[])mrn)[0]).append(")").append(",");
				}
			}
			participantDTO.setMrns(mrnDtos);

			String raceSql = "select race.raceName from " + Race.class.getName()
					+ " as race where race.participant.id=?";
			List races = dao.executeQuery(raceSql, columnValueBeans);
			List<String> participantRaces = new ArrayList<String>();
			if (!races.isEmpty())
			{
				for (Object race : races)
				{
					participantRaces.add(race.toString());
				}
			}
			participantDTO.setRace(participantRaces);

			String cprSql = "select cpr.protocolParticipantIdentifier,cpr.registrationDate,cpr.consentDocumentName from "
					+ CollectionProtocolRegistration.class.getName()
					+ " as cpr where cpr.participant.id=? and cpr.collectionProtocol.id =?";
			ColumnValueBean columnValueBeanCp = new ColumnValueBean(new Long(cpId));
			columnValueBeans.add(columnValueBeanCp);

			List cprs = dao.executeQuery(cprSql, columnValueBeans);
			if (!cprs.isEmpty())
			{
				Object[] protocolInfo = (Object[]) cprs.get(0);
				participantDTO.setPpid((String) protocolInfo[0]);

				participantDTO.setRegistrationDate((Date) protocolInfo[1]);
				if(protocolInfo[2]!=null){
				    participantDTO.setIsConsented(Constants.BOOLEAN_YES);
				}
			}

			List consentResponses = getConsentResponse(dao, columnValueBeans);
			if (!consentResponses.isEmpty())
			{
				for (Object consentResponse : consentResponses)
				{
					//(ArrayList<String>)consentResponse.get
					if (Constants.YES.equalsIgnoreCase(((ConsentTierResponse) consentResponse).getResponse())
							|| Constants.BOOLEAN_NO.equalsIgnoreCase(((ConsentTierResponse) consentResponse).getResponse()))
					{
						participantDTO.setIsConsented(Constants.BOOLEAN_YES);
						break;
					}
				}
			}
		}
		catch (DAOException e)
		{
			throw new BizLogicException(ErrorKey.getErrorKey("biz.exequery.error"), null, null);
		}
		return participantDTO;
	}

	public List getConsentResponse(DAO dao, List<ColumnValueBean> columnValueBeans)
			throws DAOException
	{
		String consentResponsesHql = "select ccpr.consentTierResponseCollection from "
				+ CollectionProtocolRegistration.class.getName()
				+ " as ccpr  where ccpr.participant.id=? and ccpr.collectionProtocol.id=?";
		List consentResponses = dao.executeQuery(consentResponsesHql, columnValueBeans);
		return consentResponses;
	}

	public void disableParticipant(Long participantId) throws BizLogicException
	{
		DAO dao = null;
		Participant participant = new Participant();
		participant.setId(participantId);
		try
		{
			dao = this.openDAOSession(null);
			disableObject(dao, participant);
		}
		catch (BizLogicException daoException)
		{
			throw new BizLogicException(ErrorKey.getErrorKey("biz.disableobj.error"), null,
					participantId.toString());
		}
		finally
		{
			try
			{
				if (dao != null)
				{
					dao.closeSession();
				}
			}
			catch (DAOException exception)
			{
				throw new BizLogicException(ErrorKey.getErrorKey("biz.closesession.error"), null,
						participantId.toString());
			}
		}
	}
	

   
}
