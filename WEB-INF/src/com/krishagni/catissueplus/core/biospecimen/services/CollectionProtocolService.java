
package com.krishagni.catissueplus.core.biospecimen.services;

import com.krishagni.catissueplus.core.biospecimen.events.AllCollectionProtocolsEvent;
import com.krishagni.catissueplus.core.biospecimen.events.AllConsentsSummaryEvent;
import com.krishagni.catissueplus.core.biospecimen.events.CreateRegistrationEvent;
import com.krishagni.catissueplus.core.biospecimen.events.DeleteEvent;
import com.krishagni.catissueplus.core.biospecimen.events.DeleteParticipantEvent;
import com.krishagni.catissueplus.core.biospecimen.events.DeleteRegistrationEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ParticipantsSummaryEvent;
import com.krishagni.catissueplus.core.biospecimen.events.RegistrationCreatedEvent;
import com.krishagni.catissueplus.core.biospecimen.events.RegistrationUpdatedEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ReqAllCollectionProtocolsEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ReqConsentsSummaryEvent;
import com.krishagni.catissueplus.core.biospecimen.events.ReqParticipantsSummaryEvent;
import com.krishagni.catissueplus.core.biospecimen.events.UpdateRegistrationEvent;

public interface CollectionProtocolService {

	public AllCollectionProtocolsEvent getAllProtocols(ReqAllCollectionProtocolsEvent req);

	public ParticipantsSummaryEvent getRegisteredParticipantList(ReqParticipantsSummaryEvent reqParticipantsSummaryEvent);

	public AllConsentsSummaryEvent getConsents(ReqConsentsSummaryEvent event);

	public RegistrationCreatedEvent createRegistration(CreateRegistrationEvent event);

	public RegistrationUpdatedEvent updateResgistration(UpdateRegistrationEvent event);

	public void delete(DeleteParticipantEvent event);
	
	public void delete(DeleteRegistrationEvent event);

}
