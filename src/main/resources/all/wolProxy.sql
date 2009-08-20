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
  
 	constraint idx_mac_addr unique (mac_address),
 	constraint idx_host_name unique (host_name)
);