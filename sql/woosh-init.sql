
-- enable geospatial extensions for postgres
CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE EXTENSION fuzzystrmatch;
CREATE EXTENSION postgis_tiger_geocoder;


create table Acceptance (id varchar(255) not null, version int4, clientId varchar(255), clientVersion int4, lastUpdated timestamp, deleted bool, accepted bool, acceptedAt timestamp, owner_id varchar(255), offer_id varchar(255), card_id varchar(255), primary key (id));
create table Card (id varchar(255) not null, version int4, clientId varchar(255), clientVersion int4, lastUpdated timestamp, deleted bool, name varchar(255), maximumAccepts int4, maximumRedemptions int4, maximumHops int4, shareMethod varchar(255), lastOffer_id varchar(255), owner_id varchar(255), originalCard_id varchar(255), primary key (id));
create table CardData (id varchar(255) not null, version int4, clientId varchar(255), clientVersion int4, lastUpdated timestamp, deleted bool, name varchar(255), data varchar(255), card_id varchar(255), binaryData_id varchar(255), owner_id varchar(255), primary key (id));
create table Card_CardData (Card_id varchar(255) not null, data_id varchar(255) not null, unique (data_id));
create table Card_Offer (Card_id varchar(255) not null, offers_id varchar(255) not null, unique (offers_id));
create table Offer (id varchar(255) not null, version int4, clientId varchar(255), clientVersion int4, lastUpdated timestamp, deleted bool, maximumAccepts int4, remainingHops int4, offerStart timestamp, offerEnd timestamp, offerRegion geometry not null, autoAccept bool, card_id varchar(255), owner_id varchar(255), primary key (id));
create table RemoteBinaryObject (id varchar(255) not null, version int4, lastUpdated timestamp, deleted bool, remoteId varchar(255), user_id varchar(255), primary key (id));
create table Role (id varchar(255) not null, version int4, authority varchar(255), description varchar(255), primary key (id));
create table Scan (id varchar(255) not null, version int4, clientId varchar(255), clientVersion int4, lastUpdated timestamp, deleted bool, scannedAt timestamp, location geometry not null, owner_id varchar(255), primary key (id));
create table Scan_Card (Scan_id varchar(255) not null, cards_id varchar(255) not null);
create table Scan_Offer (Scan_id varchar(255) not null, offers_id varchar(255) not null);
create table log (id varchar(255) not null, version int4, username varchar(255), action varchar(255), sequence varchar(255), date timestamp, user_id varchar(255), primary key (id));
create table users (id varchar(255) not null, version int4, username varchar(255) not null, password varchar(255) not null, email varchar(255) not null, accountNonExpired bool not null, accountNonLocked bool not null, credentialsNonExpired bool not null, enabled bool not null, lastKnownLocation geometry, lastLogin timestamp, memberSince timestamp, invitationalKey varchar(255), invitedBy_id varchar(255), primary key (id));
create table users_Acceptance (users_id varchar(255) not null, acceptances_id varchar(255) not null, unique (acceptances_id));
create table users_Card (users_id varchar(255) not null, cards_id varchar(255) not null);
create table users_Role (users_id varchar(255) not null, authorities_id varchar(255) not null);
create table users_Scan (users_id varchar(255) not null, scans_id varchar(255) not null, unique (scans_id));
alter table Acceptance add constraint FK2DB58E779508A040 foreign key (offer_id) references Offer;
alter table Acceptance add constraint FK2DB58E778A60A2F4 foreign key (card_id) references Card;
alter table Acceptance add constraint FK2DB58E77FC09DFDB foreign key (owner_id) references users;
alter table Card add constraint FK1FEF30BC79C03 foreign key (originalCard_id) references Card;
alter table Card add constraint FK1FEF302AA23CD6 foreign key (lastOffer_id) references Offer;
alter table Card add constraint FK1FEF30FC09DFDB foreign key (owner_id) references users;
alter table CardData add constraint FK3553AFAF9F63EDE foreign key (binaryData_id) references RemoteBinaryObject;
alter table CardData add constraint FK3553AFA8A60A2F4 foreign key (card_id) references Card;
alter table CardData add constraint FK3553AFAFC09DFDB foreign key (owner_id) references users;
alter table Card_CardData add constraint FK19DC93299001DEE4 foreign key (data_id) references CardData;
alter table Card_CardData add constraint FK19DC93298A60A2F4 foreign key (Card_id) references Card;
alter table Card_Offer add constraint FKB001586D36CCFB25 foreign key (offers_id) references Offer;
alter table Card_Offer add constraint FKB001586D8A60A2F4 foreign key (Card_id) references Card;
alter table Offer add constraint FK4892A3C8A60A2F4 foreign key (card_id) references Card;
alter table Offer add constraint FK4892A3CFC09DFDB foreign key (owner_id) references users;
alter table RemoteBinaryObject add constraint FK3FC2A62690232FC3 foreign key (user_id) references users;
alter table Scan add constraint FK273A9DFC09DFDB foreign key (owner_id) references users;
alter table Scan_Card add constraint FKA90879268EC71C1 foreign key (cards_id) references Card;
alter table Scan_Card add constraint FKA908792DB4A4FD4 foreign key (Scan_id) references Scan;
alter table Scan_Offer add constraint FK482B9E1ADB4A4FD4 foreign key (Scan_id) references Scan;
alter table Scan_Offer add constraint FK482B9E1A36CCFB25 foreign key (offers_id) references Offer;
alter table log add constraint FK1A34490232FC3 foreign key (user_id) references users;
alter table users add constraint FK6A68E08C615561C foreign key (invitedBy_id) references users;
alter table users_Acceptance add constraint FKAB75C10EB631114F foreign key (acceptances_id) references Acceptance;
alter table users_Acceptance add constraint FKAB75C10E89140866 foreign key (users_id) references users;
alter table users_Card add constraint FK94439F0768EC71C1 foreign key (cards_id) references Card;
alter table users_Card add constraint FK94439F0789140866 foreign key (users_id) references users;
alter table users_Role add constraint FK944AA46D89140866 foreign key (users_id) references users;
alter table users_Role add constraint FK944AA46D682B5558 foreign key (authorities_id) references Role;
alter table users_Scan add constraint FK944AEA74345D8A1B foreign key (scans_id) references Scan;
alter table users_Scan add constraint FK944AEA7489140866 foreign key (users_id) references users;

CREATE TABLE persistent_logins
(
  username character varying(64) NOT NULL,
  series character varying(64) NOT NULL,
  token character varying(64) NOT NULL,
  last_used timestamp without time zone NOT NULL,
  CONSTRAINT persistent_logins_pkey PRIMARY KEY (series)
)
WITH (
  OIDS=FALSE
);

-- create the initial users who will control things from here on out

insert into role (id, version, authority, description) 
	values ('ff808081-2d11bee6-012d-11bef683-0003', 1, 'ROLE_USER', 'Standard user role.');

--insert into users (id, version, username, password, email, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled) 
--	values ('ff808081-2d11bfc1-012d-11bfd083-0003', 1, 'test-user', '5f4dcc3b5aa765d61d8327deb882cf99', 'noone@nowhere.com', true, true, true, true);

-- create accounts for ben, tim, and pete
insert into users (id, version, username, password, email, invitationalKey, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled) 
	values ('402881e4-3b14d77f-013b-14d78b42-0007', 1, 'ben.deany', '5f4dcc3b5aa765d61d8327deb882cf99', 'ben.deany@woosh.com', 'hEn6vD3j', true, true, true, true);
--insert into users (id, version, username, password, email, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled) 
--	values ('402881e4-3b14d77f-013b-14d78b43-0008', 1, 'tim.macfarlane', '5f4dcc3b5aa765d61d8327deb882cf99', 'tim@woosh.com', true, true, true, true);
--insert into users (id, version, username, password, email, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled) 
--	values ('402881e4-3b14d77f-013b-14d78b45-0009', 1, 'pete.chong', '5f4dcc3b5aa765d61d8327deb882cf99', 'pete@woosh.com', true, true, true, true);
		
	
--insert into users_role (users_id, authorities_id) values ('ff808081-2d11bfc1-012d-11bfd083-0003', 'ff808081-2d11bee6-012d-11bef683-0003');

-- apply the standard user role to ben, tim, and pete
insert into users_role (users_id, authorities_id) values ('402881e4-3b14d77f-013b-14d78b42-0007', 'ff808081-2d11bee6-012d-11bef683-0003');
--insert into users_role (users_id, authorities_id) values ('402881e4-3b14d77f-013b-14d78b43-0008', 'ff808081-2d11bee6-012d-11bef683-0003');
--insert into users_role (users_id, authorities_id) values ('402881e4-3b14d77f-013b-14d78b45-0009', 'ff808081-2d11bee6-012d-11bef683-0003');


-- insert user record for Thea
--insert into users (id, version, username, password, email, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled) 
--	values ('402881e4-3c591a48-013c-591a6992-0007', 1, 'thea.hd', '5f4dcc3b5aa765d61d8327deb882cf99', 'thea@woosh.com', true, true, true, true);
--insert into users_role (users_id, authorities_id) values ('402881e4-3c591a48-013c-591a6992-0007', 'ff808081-2d11bee6-012d-11bef683-0003');

-- insert user record for Sally
--insert into users (id, version, username, password, email, accountNonExpired, accountNonLocked, credentialsNonExpired, enabled) 
--	values ('402881e4-3c786251-013c-78625bd0-0007', 1, 'sally.mac', '5f4dcc3b5aa765d61d8327deb882cf99', 'sally@woosh.com', true, true, true, true);
--insert into users_role (users_id, authorities_id) values ('402881e4-3c786251-013c-78625bd0-0007', 'ff808081-2d11bee6-012d-11bef683-0003');


