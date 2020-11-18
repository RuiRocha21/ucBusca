drop schema ucbusca;
create database IF NOT EXISTS ucbusca ;
use ucbusca;


CREATE TABLE utilizador (
	nickname varchar(30),
	password varchar(20),
	admin	 boolean DEFAULT false,
	emailFB	varchar(30) DEFAULT NULL,
	tokenFB varchar(30) DEFAULT NULL,
	PRIMARY KEY(nickname)
)ENGINE=MyISAM CHARACTER SET latin1;


CREATE TABLE notificacao (
	id			 int AUTO_INCREMENT,
	notificacao	 varchar(50),
	utilizador_nickname varchar(30) NOT NULL,
	PRIMARY KEY(id)
)ENGINE=MyISAM CHARACTER SET latin1;


CREATE TABLE pagina (
	id	 bigint AUTO_INCREMENT,
	url	 LONGTEXT NOT NULL,
	titulo	 LONGTEXT NOT NULL,
	sintese	 LONGTEXT NOT NULL,
	urlorigem LONGTEXT ,
	PRIMARY KEY(id)
)ENGINE=MyISAM CHARACTER SET latin1;


CREATE TABLE pesquisa (
	id	 int AUTO_INCREMENT,
	palavras	 varchar(100),
	utilizador varchar(30),
	PRIMARY KEY(id)
)ENGINE=MyISAM CHARACTER SET latin1;


CREATE TABLE servidor (
	id	 int AUTO_INCREMENT,
	ip	 varchar(150),
	port varchar(10),
	PRIMARY KEY(id)
)ENGINE=MyISAM CHARACTER SET latin1;

ALTER TABLE notificacao ADD CONSTRAINT notificacao_fk1 FOREIGN KEY (utilizador_nickname) REFERENCES utilizador(nickname);