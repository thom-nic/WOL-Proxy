-- Bootstrap script to create an temporal in-memory DB for HSQL. 
create sequence host_seq as int start with 1;

create table host (
  id integer primary key,
  owner_id varchar(20) not null,
  mac_address char(17) not null,
  alias varchar(80),
  host_name varchar(80),
  ip varchar(20),
  broadcast_addr varchar(20),
  port integer,
  created_dttm date not null,
  shared_group varchar(40),
  
 	constraint idx_mac_addr unique (mac_address),
 	constraint idx_host_name unique (host_name) --,
-- 	constraint idx_owner_id (owner_id),
-- 	constraint idx_shared_group (shared_group)
);

create table persistent_logins (
	username varchar(64) not null, 
	series varchar(64) primary key, 
	token varchar(64) not null, 
	last_used timestamp not null
);