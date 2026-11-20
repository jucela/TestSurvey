BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "MES" (
	"CODIGO"	CHAR(2) NOT NULL,
	"NOMBRE"	CHAR(15),
	CONSTRAINT "PK_CODIGO" PRIMARY KEY("CODIGO")
);
INSERT INTO "MES" ("CODIGO","NOMBRE") VALUES ('01','Enero'),
 ('02','Febrero'),
 ('03','Marzo'),
 ('04','Abril'),
 ('05','Mayo'),
 ('06','Junio'),
 ('07','Julio'),
 ('08','Agosto'),
 ('09','Setiembre'),
 ('10','Octubre'),
 ('11','Noviembre'),
 ('12','Diciembre');
COMMIT;
