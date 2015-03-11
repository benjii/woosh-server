create table Card_Acceptance (Card_id varchar(255) not null, acceptances_id varchar(255) not null, unique (acceptances_id));

alter table Card_Acceptance add constraint FKBFC796E6B631114F foreign key (acceptances_id) references Acceptance;
alter table Card_Acceptance add constraint FKBFC796E68A60A2F4 foreign key (Card_id) references Card;
