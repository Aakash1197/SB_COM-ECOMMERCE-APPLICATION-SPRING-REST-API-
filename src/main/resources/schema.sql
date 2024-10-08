SET SQL_REQUIRE_PRIMARY_KEY=OFF;
SET SQL_MODE ="NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT =0;
Start TRANSACTION ;
SET TIME_ZONE ="+00:00";

create table user_address (
                              address_id bigint not null,
                              user_id bigint not null
) ;

alter table user_address
    add constraint FKrmincuqpi8m660j1c57xj7twr
        foreign key (user_id)
            references users (user_id);

alter table user_address
    add constraint FKpv7y2l6mvly37lngi3doarqhd
        foreign key (address_id)
            references addresses (address_id);



