CREATE TABLE DYEXTN_LABEL (
    IDENTIFIER bigint(19) NOT NULL auto_increment, 
    PRIMARY KEY (IDENTIFIER)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE DYEXTN_CONTROL ADD SHOW_LABEL bit(1);
ALTER TABLE DYEXTN_CONTROL ADD yPosition bigint(10);
UPDATE DYEXTN_CONTROL set SHOW_LABEL=1;
UPDATE DYEXTN_CONTROL set YPOSITION=0;

CREATE TABLE DYEXTN_FORMULA (
   IDENTIFIER bigint(19) NOT NULL auto_increment,
   EXPRESSION varchar(255),
   CATEGORY_ATTRIBUTE_ID bigint(19),
   primary key (IDENTIFIER)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE  DYEXTN_CONTROL ADD(IS_CALCULATED bit(1) default 0);
ALTER TABLE  DYEXTN_CATEGORY_ATTRIBUTE ADD(IS_CAL_ATTRIBUTE bit(1) default 0);
ALTER TABLE  DYEXTN_CATEGORY_ATTRIBUTE ADD(CAL_CATEGORY_ATTR_ID bigint(19));
ALTER TABLE  DYEXTN_CATEGORY_ATTRIBUTE ADD(CAL_DEPENDENT_CATEGORY_ATTR_ID bigint(19));

ALTER TABLE DYEXTN_FORMULA add constraint FK7A0DA06B41D885B234 foreign key (CATEGORY_ATTRIBUTE_ID) references DYEXTN_CATEGORY_ATTRIBUTE (IDENTIFIER);
ALTER TABLE DYEXTN_CATEGORY_ATTRIBUTE add constraint FKEF3B77585C7C8694E foreign key (CAL_CATEGORY_ATTR_ID) references DYEXTN_CATEGORY_ATTRIBUTE (IDENTIFIER);
ALTER TABLE DYEXTN_CATEGORY_ATTRIBUTE add constraint FK7A0DA606B41D885B234 foreign key (CAL_DEPENDENT_CATEGORY_ATTR_ID) references DYEXTN_CATEGORY_ATTRIBUTE (IDENTIFIER);

ALTER TABLE DYEXTN_CONTAINER ADD (PARENT_CONTAINER_ID bigint(19));