drop procedure if exists get_last_id //

create procedure get_last_id(OUT rec_id BIGINT)
reads sql data
begin atomic
 declare last_id bigint;
 select
   last_id into last_id
 from
   dyextn_id_seq
 where
   table_name = 'RECORD_ID_SEQ';

 set rec_id = last_id + 1;
end
//


drop procedure if exists insert_specimen_event_de_record //
create procedure insert_specimen_event_de_record(IN event_name CHAR(32), IN specimen_id BIGINT, IN user_id BIGINT, IN rec_id BIGINT)
modifies sql data
begin atomic
 declare ctx_id bigint;

 select
   fc.identifier into ctx_id
 from
   dyextn_containers c
   inner join catissue_form_context fc on fc.container_id = c.identifier
 where
   c.name = event_name;

 insert into
   catissue_form_record_entry(identifier, form_ctxt_id, object_id, record_id, updated_by, update_time, activity_status)
   values(default, ctx_id, specimen_id, rec_id, user_id, current_timestamp, 'ACTIVE');

 update dyextn_id_seq set last_id = rec_id where table_name = 'RECORD_ID_SEQ';
end
//

drop trigger if exists specimen_coll_event_trg_pre //
create trigger specimen_coll_event_trg_pre before insert
on catissue_coll_event_param
referencing new row AS newrow
for each row begin atomic
  declare rec_id bigint;
  call get_last_id(rec_id);
  set newrow.identifier = rec_id;
end
//

drop trigger if exists specimen_coll_event_trg_post //
create trigger specimen_coll_event_trg_post after insert
on catissue_coll_event_param
referencing new row AS newrow
for each row begin atomic
  call insert_specimen_event_de_record('SpecimenCollectionEvent', newrow.specimen_id, newrow.user_id, newrow.identifier);
end
//

drop trigger if exists specimen_rcvd_event_trg_pre //
create trigger specimen_rcvd_event_trg_pre before insert
on catissue_received_event_param
referencing new row AS newrow
for each row begin atomic
  declare rec_id bigint;
  call get_last_id(rec_id);
  set newrow.identifier = rec_id;
end
//

drop trigger if exists specimen_rcvd_event_trg_post //
create trigger specimen_rcvd_event_trg_post after insert
on catissue_received_event_param
referencing new row AS newrow
for each row begin atomic
  call insert_specimen_event_de_record('SpecimenReceivedEvent', newrow.specimen_id, newrow.user_id, newrow.identifier);
end
//

drop trigger if exists specimen_transfer_event_trg_pre //
create trigger specimen_transfer_event_trg_pre before insert
on catissue_transfer_event_param
referencing new row AS newrow
for each row begin atomic
  declare rec_id bigint;
  call get_last_id(rec_id);
  set newrow.identifier = rec_id;
end
//

drop trigger if exists specimen_transfer_event_trg_post //
create trigger specimen_transfer_event_trg_post after insert
on catissue_transfer_event_param
referencing new row AS newrow
for each row begin atomic
  call insert_specimen_event_de_record('SpecimenTransferEvent', newrow.specimen_id, newrow.user_id, newrow.identifier);
end
//

drop trigger if exists specimen_distri_event_trg_pre //
create trigger specimen_distri_event_trg_pre before insert
on catissue_distri_event_param
referencing new row AS newrow
for each row begin atomic
  declare rec_id bigint;
  call get_last_id(rec_id);
  set newrow.identifier = rec_id;
end
//

drop trigger if exists specimen_distri_event_trg_post //
create trigger specimen_distri_event_trg_post after insert
on catissue_distri_event_param
referencing new row AS newrow
for each row begin atomic
  call insert_specimen_event_de_record('SpecimenDistributedEvent', newrow.specimen_id, newrow.user_id, newrow.identifier);
end
//

drop trigger if exists specimen_disposal_event_trg_pre //
create trigger specimen_disposal_event_trg_pre before insert
on catissue_disposal_event_param
referencing new row AS newrow
for each row begin atomic
  declare rec_id bigint;
  call get_last_id(rec_id);
  set newrow.identifier = rec_id;
end
//

drop trigger if exists specimen_disposal_event_trg_post //
create trigger specimen_disposal_event_trg_post after insert
on catissue_disposal_event_param
referencing new row AS newrow
for each row begin atomic
  call insert_specimen_event_de_record('SpecimenDisposalEvent', newrow.specimen_id, newrow.user_id, newrow.identifier);
end
//
