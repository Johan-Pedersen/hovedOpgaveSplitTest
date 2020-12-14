-- schema's

-- !Ups
create table owners(
OWNERNAME varchar(25) UNIQUE,
PASS varchar(170),
SALT varchar(55),

primary key (OWNERNAME, PASS)
);


create table experiment(
OWNERNAME varchar(25),
EXPERIMENT_ID varchar(25),
NUM_OF_VARIANTS numeric NOT NULL,
NUM_OF_WIN_TYPES numeric NOT NULL,

foreign key (OWNERNAME) references owners(OWNERNAME) on delete cascade,
primary key (OWNERNAME , EXPERIMENT_ID)
);

create table variant(
OWNERNAME varchar(25),
EXPERIMENT_ID varchar(25),
USER_ID varchar(25),
VARIANT_TYPE numeric NOT NULL,
NUM_OF_DISPLAYS numeric,
TOTAL_NUM_OF_WINS numeric,

foreign key(OWNERNAME, EXPERIMENT_ID) references experiment(OWNERNAME,EXPERIMENT_ID) on delete cascade,
primary key(OWNERNAME, EXPERIMENT_ID, USER_ID)
);

create table timeStamps(
OWNERNAME varchar(25),
EXPERIMENT_ID varchar(25),
USER_ID varchar(25),
VARIANT_TYPE numeric NOT NULL,
REGISTERED_DATE varchar(10) NOT NULL,
DISPLAY boolean,
WIN boolean,
ID INT NOT NULL AUTO_INCREMENT,

foreign key(OWNERNAME, EXPERIMENT_ID, USER_ID) references variant(OWNERNAME,EXPERIMENT_ID, USER_ID) on delete cascade,
primary key(OWNERNAME, EXPERIMENT_ID, USER_ID, ID),
INDEX(id)
);

create table win(
OWNERNAME varchar(25),
EXPERIMENT_ID varchar(25),
USER_ID varchar(25),
VARIANT_TYPE numeric NOT NULL,
WIN_TYPE numeric,
NUM_OF_WINS numeric,

foreign key(OWNERNAME, EXPERIMENT_ID, USER_ID) references variant(OWNERNAME,EXPERIMENT_ID, USER_ID) on delete cascade,
primary key(OWNERNAME, EXPERIMENT_ID, USER_ID, WIN_TYPE)
);

-- !Downs
drop table win
drop table timeStamps
drop table variant
drop table experiment
drop table owners