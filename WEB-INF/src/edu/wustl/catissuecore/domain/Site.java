/**
 * <p>Title: Site Class>
 * <p>Description:  A physical location associated with biospecimen collection,
 * storage, processing, or utilization. </p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Aniruddha Phadnis
 * @version 1.00
 */

package edu.wustl.catissuecore.domain;

import java.util.Collection;
import java.util.HashSet;

import edu.wustl.catissuecore.actionForm.SiteForm;
import edu.wustl.catissuecore.util.SearchUtil;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.actionForm.IValueObject;
import edu.wustl.common.bizlogic.IActivityStatus;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.exception.AssignDataException;
import edu.wustl.common.exception.ErrorKey;
import edu.wustl.common.participant.domain.ISite;
import edu.wustl.common.util.logger.Logger;

/**
 * A physical location associated with biospecimen collection, storage, processing, or utilization.
 * @hibernate.class table="CATISSUE_SITE"
 */
public class Site extends AbstractDomainObject
		implements
			java.io.Serializable,
			IActivityStatus,
			ISite
{

	/**
	 * logger Logger - Generic logger.
	 */
	private static Logger logger = Logger.getCommonLogger(Site.class);

	/**
	 * Serial Version ID.
	 */
	private static final long serialVersionUID = 1234567890L;

	/**
	 * System generated unique identifier.
	 */
	protected Long id;

	/**
	 * Name of the physical location.
	 */
	protected String name;

	/**
	 * Function of the site (e.g. Collection site, repository, or laboratory).
	 */
	protected String type;

	/**
	 * EmailAddress Address of the site.
	 */
	protected String emailAddress;

	/**
	 * Defines whether this Site record can be queried (Active) or not queried (Inactive) by any actor.
	 */
	protected String activityStatus;

	//Change for API Search   --- Ashwin 04/10/2006
	/**
	 * The User who currently coordinates operations at the Site.
	 */
	protected User coordinator;

	//Change for API Search   --- Ashwin 04/10/2006
	/**
	 * The address of the site.
	 */
	private Address address;

	/**
	 * SpecimenCollectionGroupCollection.
	 */
	private Collection<SpecimenCollectionGroup> specimenCollectionGroupCollection;
	
	/**
	 * CollectionProtocolEventcollection.
	 */
	private Collection<CollectionProtocolEvent> collectionProtocolEventcollection;
	

	/**
	 * HashSet containing CollectionProtocol.
	 */
	private Collection<CollectionProtocol> collectionProtocolCollection = new HashSet<CollectionProtocol>();

	/**
	 * HashSet containing User.
	 */
	private Collection<User> assignedSiteUserCollection = new HashSet<User>();

	protected String facilityId;

	/**
	 * Default Constructor Required by hibernate.
	 */
	public Site()
	{
		super();
	}

	/**
	 * Copy Constructor.
	 * @param site Site object
	 */
	public Site(Site site)
	{
		super();
		id = Long.valueOf(site.getId().longValue());
		name = site.getName();
		assignedSiteUserCollection = null;
		collectionProtocolCollection = null;
		specimenCollectionGroupCollection = null;
		collectionProtocolEventcollection = null;
		coordinator = null;
		address = null;
	}

	/**
	 * Parameterized constructor.
	 * @param abstractForm AbstractActionForm.
	 * @throws AssignDataException : AssignDataException
	 */
	public Site(AbstractActionForm abstractForm) throws AssignDataException
	{
		super();
		this.setAllValues(abstractForm);
	}

	/**
	 * Returns the system generated unique identifier.
	 * @hibernate.id name="id" column="IDENTIFIER" type="long" length="30"
	 * unsaved-value="null" generator-class="native"
	 * @hibernate.generator-param name="sequence" value="CATISSUE_SITE_SEQ"
	 * @return the system generated unique identifier.
	 * @see #setId(Long)
	 */
	@Override
	public Long getId()
	{
		return id;
	}

	/**
	 * Sets a unique system identifier.
	 * @param identifier identifier to be set.
	 * @see #getId()
	 */
	@Override
	public void setId(Long identifier)
	{
		id = identifier;
	}

	/**
	 * Returns the name of the physical location.
	 * @hibernate.property name="name" type="string"
	 * column="NAME" length="255" not-null="true" unique="true"
	 * @return the name of the physical location.
	 * @see #setName(String)
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the physical location.
	 * @param name the physical location to be set.
	 * @see #getName()
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the function of the site.
	 * @hibernate.property name="type" type="string"
	 * column="TYPE" length="50"
	 * @return the function of the site.
	 * @see #setType(String)
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the function of the site..
	 * @param type Function of the site to be set.
	 * @see #getType()
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * Returns the emailAddress Address of the site.
	 * @hibernate.property name="emailAddress" type="string"
	 * column="EMAIL_ADDRESS" length="255"
	 * @return String representing the emailAddress address of the site.
	 */
	public String getEmailAddress()
	{
		return emailAddress;
	}

	/**
	 * Sets the emailAddress address of the site.
	 * @param emailAddress String representing emailAddress address of the site.
	 * @see #getEmailAddress()
	 */
	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}

	/**
	 * Returns the coordinator associated with this site.
	 * @hibernate.many-to-one column="USER_ID"  class="edu.wustl.catissuecore.domain.User" constrained="true"
	 * @return coordinator associated with this site.
	 * @see #setCoordinator(User)
	 */
	public User getCoordinator()
	{
		return coordinator;
	}

	/**
	 * Sets the coordinator to this site.
	 * @param coordinator coordinator to be set.
	 * @see #getCoordinator()
	 */
	public void setCoordinator(edu.wustl.catissuecore.domain.User coordinator)
	{
		this.coordinator = coordinator;
	}

	/**
	 * Returns the activity status.
	 * @hibernate.property name="activityStatus" type="string"
	 * column="ACTIVITY_STATUS" length="50"
	 * @return String the activity status.
	 * @see #getActivityStatus(User)
	 */
	public String getActivityStatus()
	{
		return activityStatus;
	}

	/**
	 * Sets the the activity status.
	 * @param activityStatus activity status of the site to be set.
	 * @see #getActivityStatus()
	 */
	public void setActivityStatus(String activityStatus)
	{
		this.activityStatus = activityStatus;
	}

	/**
	 * Returns the address of the site.
	 * @return Address of the site.
	 * @hibernate.many-to-one column="ADDRESS_ID"
	 * class="edu.wustl.catissuecore.domain.Address" constrained="true"
	 * @see #setAddress(Address)
	 */
	public Address getAddress()
	{
		return address;
	}

	/**
	 * Sets the address of the site.
	 * @param address address of the site to be set.
	 * @see #getAddress()
	 */
	public void setAddress(Address address)
	{
		this.address = address;
	}

	/**
	 * This function Copies the data from an SiteForm object to a Site object.
	 * @param abstractForm - siteForm An SiteForm object containing the information about the site.
	 * @throws AssignDataException : AssignDataException
	 * */
	@Override
	public void setAllValues(IValueObject abstractForm) throws AssignDataException
	{
		try
		{

			//Change for API Search   --- Ashwin 04/10/2006
			if (SearchUtil.isNullobject(coordinator))
			{
				coordinator = new User();
			}
			//Change for API Search   --- Ashwin 04/10/2006
			if (SearchUtil.isNullobject(address))
			{
				address = new Address();
			}

			final SiteForm form = (SiteForm) abstractForm;
			id = Long.valueOf(form.getId());
			name = form.getName().trim();
			type = form.getType();

			emailAddress = form.getEmailAddress();

			activityStatus = form.getActivityStatus();
			logger.debug("form.getCoordinatorId() " + form.getCoordinatorId());
			coordinator.setId(Long.valueOf(form.getCoordinatorId()));

			address.setStreet(form.getStreet());
			address.setCity(form.getCity());
			address.setState(form.getState());
			address.setCountry(form.getCountry());
			address.setZipCode(form.getZipCode());
			address.setPhoneNumber(form.getPhoneNumber());
			address.setFaxNumber(form.getFaxNumber());
			this.setFacilityId(form.getFacilityId());
		}
		catch (final Exception excp)
		{
			Site.logger.error(excp.getMessage(), excp);
			excp.printStackTrace();
			final ErrorKey errorKey = ErrorKey.getErrorKey("assign.data.error");
			throw new AssignDataException(errorKey, null, "Site.java :");
		}
	}

	/**
	 * Returns message label to display on success add or edit.
	 * @return String
	 */
	@Override
	public String getMessageLabel()
	{
		return name;
	}

	
	
	public Collection<SpecimenCollectionGroup> getSpecimenCollectionGroupCollection()
	{
		return specimenCollectionGroupCollection;
	}

	
	public void setSpecimenCollectionGroupCollection(Collection<SpecimenCollectionGroup> specimenCollectionGroupCollection)
	{
		this.specimenCollectionGroupCollection = specimenCollectionGroupCollection;
	}

	
	public Collection<CollectionProtocolEvent> getCollectionProtocolEventcollection()
	{
		return collectionProtocolEventcollection;
	}

	
	public void setCollectionProtocolEventcollection(Collection<CollectionProtocolEvent> collectionProtocolEventcollection)
	{
		this.collectionProtocolEventcollection = collectionProtocolEventcollection;
	}

	/**
	* @return Returns the collectionProtocolCollection.
	* @hibernate.set name="collectionProtocolCollection" table="CATISSUE_SITE_COLLECTION_PROTOCOLS"
	* cascade="none" inverse="true" lazy="true"
	* @hibernate.collection-key column="SITE_ID"
	* @hibernate.collection-many-to-many class="edu.wustl.catissuecore.domain.
	* CollectionProtocol" column="COLLECTION_PROTOCOL_ID"
	*/
	public Collection<CollectionProtocol> getCollectionProtocolCollection()
	{
		return collectionProtocolCollection;
	}

	/**
	 * Set CollectionProtocolCollection.
	 * @param collectionProtocolCollection Collection of CollectionProtocol.
	 */
	public void setCollectionProtocolCollection(
			Collection<CollectionProtocol> collectionProtocolCollection)
	{
		this.collectionProtocolCollection = collectionProtocolCollection;
	}

	/**
	 * Returns the collection of Users(Authorized users) registered for this Site.
	 * @hibernate.set name="userCollection" table="CATISSUE_SITE_USERS"
	 * cascade="none" inverse="false" lazy="false"
	 * @hibernate.collection-key column="SITE_ID"
	 * @hibernate.collection-many-to-many class="edu.wustl.catissuecore.domain.User" column="USER_ID"
	 * @return The collection of Users(Authorized users) registered for this Site.
	 */
	public Collection<User> getAssignedSiteUserCollection()
	{
		return assignedSiteUserCollection;
	}

	/**
	 * Set AssignedSiteUserCollection.
	 * @param userCollection Collection of User objects.
	 */
	public void setAssignedSiteUserCollection(Collection<User> userCollection)
	{
		assignedSiteUserCollection = userCollection;
	}

	/**
	 * @return the facilityId
	 */
	public String getFacilityId()
	{
		return facilityId;
	}

	/**
	 * @param facilityId the facilityId to set
	 */
	public void setFacilityId(String facilityId)
	{
		this.facilityId = facilityId;
	}

}