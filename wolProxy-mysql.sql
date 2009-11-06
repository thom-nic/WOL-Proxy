create schema wolproxy character set 'UTF8';
use wolproxy;

create table host (
  id int unsigned primary key,
  owner_id varchar(20) not null,
  mac_address char(17) not null,
  alias varchar(80),
  host_name varchar(80),
  ip varchar(20),
  broadcast_addr varchar(20),
  port int,
  created_dttm date not null,
  
 	constraint idx_mac_addr unique (mac_address),
 	constraint idx_host_name unique (host_name)
);

/* Pseudo-sequence. See: 
 * http://dev.mysql.com/doc/refman/5.0/en/information-functions.html
 * and:
 * http://www.mysqlperformanceblog.com/2008/04/02/stored-function-to-generate-sequences/
 */
create table seq ( 
	name char(10) not null primary key,
	val INT NOT NULL
);
INSERT INTO seq VALUES ( 'host', 0 );

delimiter //
create function next_value_for( seq_name char(10) ) returns int
begin
	UPDATE seq SET val=LAST_INSERT_ID( val + 1 ) where name=seq_name;
	return LAST_INSERT_ID();
end //
delimiter ;

grant select, insert, update, delete on wolproxy.* to 'wol'@'localhost';
grant execute function wolproxy.next_value_for to 'wol'@'localhost' identified by '!wolProxy1';

flush privileges;
