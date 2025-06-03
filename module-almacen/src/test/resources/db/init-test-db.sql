-- Crear esquemas necesarios
CREATE SCHEMA IF NOT EXISTS comercial;
CREATE SCHEMA IF NOT EXISTS almacenes;
CREATE SCHEMA IF NOT EXISTS logistica;

CREATE TABLE logistica.tbunidad (
	id_unidad serial4 NOT NULL,
	abrev_unidad varchar(20) NULL,
	desc_unidad varchar(100) NULL,
	status int4 DEFAULT 1 NULL,
	id_medida_si int4 NULL,
	is_concentracion int2 DEFAULT 0 NULL,
	cod_unidad varchar(5) DEFAULT NULL::character varying NULL,
	CONSTRAINT tbunidad_pkey PRIMARY KEY (id_unidad)
);

INSERT INTO logistica.tbunidad (id_unidad, abrev_unidad, desc_unidad, id_medida_si, is_concentracion, cod_unidad) VALUES (
    1, 'KG', 'KILOGRAMO', 2, 0, 'KGM'
);

CREATE TABLE almacenes.kardex (
	id_kardex bigserial NOT NULL,
	tipo_kardex int2 DEFAULT 0 NULL,
	detalle text NULL,
	cantidad numeric(18, 6) NULL,
	valor_unidad numeric(18, 6) NULL,
	valor_total numeric(18, 6) NULL,
	fecha_movimiento date NULL,
	id_articulo int4 NULL,
	status int2 DEFAULT 1 NULL,
	id_unidad int4 NULL,
	id_unidad_salida int4 NULL,
	id_almacen int4 NULL,
	saldo_stock numeric(18, 6) NULL,
	id_documento int4 NULL,
	id_detalle_documento int4 NULL,
	saldo_lote numeric(18, 6) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT kardex_pkey PRIMARY KEY (id_kardex)
);

CREATE TABLE logistica.tbfamilia (
	id_familia serial4 NOT NULL,
	abrev_familia varchar(20) NULL,
	desc_familia varchar(100) NULL,
	ispolyester int4 NULL,
	isalgodon int4 NULL,
	correlativo_familia int4 DEFAULT 0 NULL,
	id_tipo_producto int4 NULL,
	status int4 DEFAULT 1 NULL,
	CONSTRAINT tbfamilia_pkey PRIMARY KEY (id_familia)
);

INSERT INTO logistica.tbfamilia (id_familia, abrev_familia, desc_familia, ispolyester, isalgodon, correlativo_familia, id_tipo_producto) VALUES (
    5, 'PRO', 'PRODUCTOS QUIMICOS', 0, 0, 184, 1
);

CREATE TABLE comercial.tctipo_persona (
	id_tipo_per serial4 NOT NULL,
	no_tipo_per varchar(100) NULL,
	CONSTRAINT tctipo_persona_pkey PRIMARY KEY (id_tipo_per)
);

CREATE TABLE comercial.tbclientes (
	id_cliente serial4 NOT NULL,
	id_tipo_per int4 NULL,
	no_apepat varchar(100) NULL,
	no_apemat varchar(100) NULL,
	no_nombres varchar(100) NULL,
	no_razon varchar(200) NULL,
	no_alias varchar(100) NULL,
	nu_ruc varchar NULL,
	no_dir varchar(200) NULL,
	co_depart varchar(6) NULL,
	co_provin varchar(6) NULL,
	co_distri varchar(6) NULL,
	nu_telefono varchar(15) NULL,
	nu_celular varchar(15) NULL,
	no_correo varchar(100) NULL,
	no_web varchar(100) NULL,
	fe_const date NULL,
	fe_inicio date NULL,
	no_cont varchar(20) NULL,
	no_aget varchar(20) NULL,
	id_tipodoc int4 NULL,
	is_proveedor int2 NULL,
	id_login int4 NULL,
	is_cliente_facturado int2 DEFAULT 0 NOT NULL,
	porc_facturado numeric NULL,
	add_igv int2 DEFAULT 0 NOT NULL,
	CONSTRAINT tbclientes_pkey PRIMARY KEY (id_cliente)
);
ALTER TABLE comercial.tbclientes ADD CONSTRAINT tctipo_persona_fk FOREIGN KEY (id_tipo_per) REFERENCES comercial.tctipo_persona(id_tipo_per) MATCH FULL;

CREATE TABLE almacenes.almacen (
	id_almacen serial4 NOT NULL,
	id_tipoalmacen int4 NULL,
	id_encargado int4 NULL,
	cod_almacen varchar(100) NULL,
	no_almacen varchar(100) NULL,
	alias_almacen varchar(100) NULL,
	desc_almacen varchar(100) NULL,
	anexo_almacen varchar(100) NULL,
	prefijo_entrada varchar(100) NULL,
	prefijo_salida varchar(100) NULL,
	prefijo_ajuste varchar(100) NULL,
	corre_entrada int4 NULL,
	corre_salida int4 NULL,
	corre_ajuste int4 NULL,
	fec_registro date NULL,
	status int4 DEFAULT 1 NULL,
	isoc int4 DEFAULT 0 NULL,
	isdespacho int2 DEFAULT 0 NULL,
	isvale int4 DEFAULT 0 NULL,
	id_ubicacion int4 DEFAULT 0 NULL,
	isdevolucion int4 DEFAULT 0 NULL,
	id_proceso int4 NULL,
	CONSTRAINT id_almacen_pkey PRIMARY KEY (id_almacen)
);

INSERT INTO almacenes.almacen (
    id_almacen, id_tipoalmacen, id_encargado, cod_almacen, no_almacen,
    alias_almacen, desc_almacen, anexo_almacen, prefijo_entrada, prefijo_salida,
    prefijo_ajuste, corre_entrada, corre_salida, corre_ajuste, isoc,
    isdespacho, isvale, id_ubicacion, isdevolucion, id_proceso) VALUES(
    1, 1, 165, 'ALGI', 'ALMACEN DE INSUMOS',
    'ALM INSUMOS', 'ESTE ALMACEN CONTIENE PRODUCTOS QUIMICOS Y COLORANTES', '(113)', 'ALGI-I', 'ALGI-S',
    'ALGI-A', 5873, 89730, 2032, 0,
    0, 4, NULL, 0, NULL);

CREATE TABLE almacenes.tbmotivosingresos (
	id_motivos_ingreso serial4 NOT NULL,
	id_motivo int4 NULL,
	id_tipo_almacen int4 NULL,
	id_tipo_movimiento int4 NULL,
	isoc int4 DEFAULT 0 NULL,
	status int4 DEFAULT 1 NULL,
	CONSTRAINT tbmotivosalmacen_pkey PRIMARY KEY (id_motivos_ingreso)
);

INSERT INTO almacenes.tbmotivosingresos (id_motivos_ingreso,id_motivo,id_tipo_almacen,id_tipo_movimiento,isoc,status) VALUES
	 (2,4,1,1,1,1),
	 (3,2,5,2,0,1),
	 (4,5,1,1,0,1),
	 (5,6,1,1,1,1),
	 (6,2,1,2,1,1),
	 (7,7,5,2,0,1),
	 (8,8,1,2,0,1),
	 (9,9,1,2,0,1),
	 (13,11,1,1,0,1),
	 (14,12,1,2,0,1),
	 (15,13,1,2,0,1),
	 (16,15,1,3,0,1),
	 (17,15,3,3,0,1),
	 (18,15,4,3,0,1),
	 (19,3,1,1,0,1),
	 (20,14,1,2,0,1),
	 (21,16,1,2,0,1),
	 (12,10,1,2,0,0),
	 (22,22,5,2,0,1),
	 (23,23,1,2,0,1),
	 (24,24,5,2,0,1),
	 (25,21,1,2,0,1),
	 (26,20,1,2,0,1),
	 (27,17,1,2,0,1),
	 (28,26,1,2,0,1),
	 (29,27,5,1,0,1),
	 (1,1,5,1,0,1),
	 (30,28,1,2,0,1),
	 (31,29,5,1,1,1),
	 (32,29,5,2,0,1),
	 (33,30,1,2,0,1),
	 (34,31,1,2,0,1),
	 (35,32,1,1,0,1),
	 (36,32,1,2,0,1);


CREATE TABLE almacenes.tbmotivos (
	id_motivo serial4 NOT NULL,
	no_motivo varchar(100) NULL,
	status int4 DEFAULT 1 NULL,
	descripcion text NULL,
	CONSTRAINT tbmotivos_pkey PRIMARY KEY (id_motivo)
);

INSERT INTO almacenes.tbmotivos (id_motivo,no_motivo,status,descripcion) VALUES
	 (1,'INGRESO',1,NULL),
	 (2,'SALIDA',1,NULL),
	 (4,'COMPRAS',1,NULL),
	 (5,'MUESTRA GRATUITA',1,NULL),
	 (6,'SERVICIOS',1,NULL),
	 (7,'RECHAZADO',1,NULL),
	 (8,'NUEVO',1,NULL),
	 (9,'CAMBIO',1,NULL),
	 (11,'INVENTARIO INICIAL',1,NULL),
	 (12,'VALE DE ACABADO',1,NULL),
	 (13,'VALE TINTORERIA',1,NULL),
	 (14,'TRANSFORMACION - PRODUCTO',1,NULL),
	 (15,'AJUSTE DE INVENTARIO',1,NULL),
	 (16,'DOTACION',1,NULL),
	 (17,'RECETA DE LAVADO',1,NULL),
	 (20,'RECETA DE TEÑIDO',1,NULL),
	 (21,'RECETA DE ACABADO',1,NULL),
	 (10,'VALE PARTIDA',0,NULL),
	 (22,'DEVOLUCION',1,NULL),
	 (23,'VALE DE ECONOMATO',1,NULL),
	 (24,'DESPACHO AL CLIENTE',1,NULL),
	 (25,'MOVIMIENTO PARTIDAS EN PRODUCCION',1,NULL),
	 (26,'RECETA TERMOFIJADO',1,NULL),
	 (27,'INGRESO - DEVOLUCIÓN',1,NULL),
	 (3,'REINGRESO',1,NULL),
	 (28,'CREACION/VALIDACION RECETAS',1,NULL),
	 (29,'SERVICIO PERCHADO',1,NULL),
	 (30,'VENTAS',1,NULL),
	 (31,'RECETA DE DESMONTADO',1,NULL),
	 (32,'MOVIMIENTO ALM. INSUMO',1,NULL);


CREATE TABLE logistica.conversion_si (
	id_conversion_si serial4 NOT NULL,
	is_multiplo bpchar(1) NOT NULL,
	id_uni_medida int4 NOT NULL,
	id_uni_medida_conv int4 NULL,
	valor_conv int4 NULL,
	CONSTRAINT conversion_si_pkey PRIMARY KEY (id_conversion_si)
);
INSERT INTO logistica.conversion_si (id_conversion_si, is_multiplo, id_uni_medida, id_uni_medida_conv, valor_conv) VALUES
    (1, 1, 1, 6, 3);

CREATE TABLE almacenes.inventario (
	id_articulo int4 NOT NULL,
	stock numeric(13, 4) DEFAULT 0 NULL,
	varios_lotes int2 DEFAULT 0 NULL,
	id_almacen int4 NULL,
	CONSTRAINT articulo_almacen_uk UNIQUE (id_articulo, id_almacen),
	CONSTRAINT inventario_id_articulo_uk UNIQUE (id_articulo, id_almacen)
);
CREATE INDEX inventario_id_articulo_idx ON almacenes.inventario USING btree (id_articulo, id_almacen);

INSERT INTO almacenes.inventario (id_articulo, stock, varios_lotes, id_almacen) VALUES (289, 544982.9000, 1, 1);

CREATE TABLE almacenes.detalle_inventario (
	id_lote serial4 NOT NULL,
	id_articulo int4 NULL,
	lote bpchar(10) NULL,
	formato_date bpchar(10) NULL,
	fecha_vencimiento date NULL,
	id_almacen int4 NULL,
	id_ubicacion int4 NULL,
	cantidad float8 NULL,
	cantidad_disponible float8 NULL,
	costo_compra numeric NULL,
	precio_venta numeric NULL,
	status int2 DEFAULT 1 NULL,
	costo_consumo numeric(14, 7) DEFAULT 0.00 NULL,
	fecha_ingreso timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
	id_detordeningreso int4 NULL,
	fec_ing_inv date NULL,
	id_moneda int4 NULL,
	excento_impuesto int4 NULL,
	CONSTRAINT detalle_inventario_pkey PRIMARY KEY (id_lote)
);

INSERT INTO almacenes.detalle_inventario (id_lote, id_articulo, id_almacen, cantidad, cantidad_disponible, costo_compra, status, costo_consumo, id_detordeningreso) VALUES
    (38699, 289, 1, 120000.0, 120000.0, 2.15000, 1, 0.0021500, 303834),
    (38641, 289, 1, 240000.0, 240000.0, 2.15000, 1, 0.0021500, 303180),
    (38615, 289, 1, 120000.0, 120000.0, 2.15000, 1, 0.0021500, 302522),
    (38583, 289, 1, 120000.0, 64982.9, 2.15000, 1, 0.0021500, 302091),
    (38522, 289, 1, 240000.0, 0.0, 2.15000, 1, 0.0021500, 301796);

CREATE OR REPLACE FUNCTION almacenes.incrementar_stock()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$DECLARE
BEGIN
    INSERT INTO almacenes.inventario (id_articulo, stock, id_almacen) VALUES (NEW.id_articulo, NEW.cantidad, NEW.id_almacen) ON CONFLICT ON CONSTRAINT articulo_almacen_uk DO UPDATE SET stock=inventario.stock+NEW.cantidad;
	--DO NOTHING;
    RETURN NEW;
END;
$function$
;

-- Table Triggers
CREATE TRIGGER tg_incrementar_stock AFTER
INSERT ON almacenes.detalle_inventario FOR EACH ROW EXECUTE FUNCTION almacenes.incrementar_stock();

CREATE TABLE logistica.ordencompra (
	id_orden serial4 NOT NULL,
	cod_orden varchar(30) NULL,
	id_cliente int4 NULL,
	id_moneda int4 NULL,
	id_tipago int4 NULL,
	fec_ingreso date NULL,
	fec_entrega date NULL,
	exc_igv int4 DEFAULT 0 NULL,
	subtotal numeric(10, 2) NULL,
	igv numeric(10, 2) NULL,
	total numeric(10, 2) NULL,
	id_usuario int4 NULL,
	observacion varchar(5000) NULL,
	id_comprobante int4 DEFAULT 1 NULL,
	status int4 DEFAULT 0 NULL,
	"event" varchar(1) NULL,
	id_forma_pago int4 NULL,
	aprobar_precio bool DEFAULT false NULL,
	recibido int2 DEFAULT 0 NULL,
	CONSTRAINT ordencompra_pkey PRIMARY KEY (id_orden)
);

INSERT INTO logistica.ordencompra
    (id_orden, cod_orden, id_cliente, id_moneda, id_tipago,
    fec_ingreso, fec_entrega, exc_igv, subtotal, igv,
    total, id_comprobante, id_forma_pago, id_usuario) VALUES
    (13505, 'OCG25-13458', 86, 2, 2,
    '2025-05-02', '2025-05-02', 0, 516.00, 92.88,
    608.88, 1, 50, 4),
    (13523,	'OCG25-13476', 86, 2, 2, '2025-05-05', '2025-05-05', 0, 258.00, 46.44, 304.44, 1, 50, 4),
    (13538,	'OCG25-13491', 86, 2, 2, '2025-05-07', '2025-05-07', 0, 258.00, 46.44, 304.44, 1, 50, 4),
    (13557,	'OCG25-13510', 86, 2, 2, '2025-05-09', '2025-05-09', 0, 516.00, 92.88, 608.88, 1, 50, 4),
    (13569,	'OCG25-13522', 86, 2, 2, '2025-05-12', '2025-05-12', 0, 258.00, 46.44, 304.44, 1, 50, 4);

CREATE TABLE logistica.detordencompra (
	id_detordencompra serial4 NOT NULL,
	id_orden int4 NULL,
	id_articulo int4 NULL,
	cantidad numeric(10, 2) NULL,
	costo numeric(10, 4) NULL,
	total numeric(10, 2) NULL,
	exento_imp int4 DEFAULT 0 NULL,
	id_moneda int4 DEFAULT 1 NULL,
	change_precio bool DEFAULT false NULL,
	mto_ult_compra numeric(14, 3) NULL,
	precio_aprobado bool DEFAULT false NULL,
	saldo numeric(13, 2) DEFAULT NULL::numeric NULL,
	id_categoria_oc int4 NULL,
	id_subcategoria_oc int4 NULL,
	id_tipo_subcategoria_oc int4 NULL,
	id_maquina int4 NULL,
	id_detalle_categ_oc int4 NULL,
	CONSTRAINT logistica_detordencompra_pkey PRIMARY KEY (id_detordencompra)
);

INSERT INTO logistica.detordencompra (id_detordencompra, id_orden, id_articulo, cantidad, costo, total, exento_imp, id_moneda, mto_ult_compra, id_categoria_oc, id_subcategoria_oc) VALUES
    (30574, 13505, 289, 240.00, 2.1500, 608.88, 0, 2, 2.150, 4, 17),
    (30619, 13523, 289, 120.00, 2.1500, 304.44, 0, 2, 2.150, 4, 17),
    (30646, 13538, 289, 120.00, 2.1500, 304.44, 0, 2, 2.150, 4, 17),
    (30702, 13557, 289, 240.00, 2.1500, 608.88, 0, 2, 2.150, 4, 17),
    (30732, 13569, 289, 120.00, 2.1500, 304.44, 0, 2, 2.150, 4, 17);

CREATE OR REPLACE FUNCTION logistica.update_autorizar_firmas()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    lc_aprobar_precio integer := NULL;
BEGIN
    SELECT COUNT(id_detordencompra) INTO lc_aprobar_precio
    FROM logistica.detordencompra AS det_ord_comp
    WHERE det_ord_comp.id_orden=OLD.id_orden AND det_ord_comp.change_precio=TRUE AND det_ord_comp.precio_aprobado=FALSE
    GROUP BY det_ord_comp.id_orden;
    UPDATE logistica.ordencompra SET aprobar_precio = CASE WHEN lc_aprobar_precio IS NULL THEN FALSE ELSE TRUE END WHERE id_orden = OLD.id_orden;
    RETURN NEW;
END;
$function$
;

CREATE TRIGGER trigger_change_autoriza_precio_compra AFTER
UPDATE ON
    logistica.detordencompra FOR EACH ROW
    WHEN ((new.precio_aprobado = TRUE)) EXECUTE FUNCTION logistica.update_autorizar_firmas();


CREATE TRIGGER trigger_change_autoriza_precio_compra_insert AFTER
INSERT ON
    logistica.detordencompra FOR EACH ROW
    WHEN ((new.change_precio = TRUE)) EXECUTE FUNCTION logistica.update_autorizar_firmas();

CREATE TABLE almacenes.ordeningreso (
	id_ordeningreso SERIAL PRIMARY KEY,
	id_cliente int4 NULL,
	id_motivo int4 NULL,
	id_origen int4 NULL,
	id_comprobante int4 NULL,
	nu_comprobante varchar(30) NULL,
	observacion varchar(500) NULL,
	fec_ingreso date NULL,
	fec_ref date NULL,
	fec_registro timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
	status int4 DEFAULT 1 NULL,
	nu_serie varchar(20) NULL,
	cod_ingreso varchar(30) NULL,
	id_almacen int4 NULL,
	id_orden int4 DEFAULT 0 NOT NULL,
	id_centro int4 NULL,
	id_ubicacion int4 NULL,
	id_maquina int4 NULL,
	id_naturaleza int4 NULL,
	descripcion varchar(100) NULL,
	comprobante_ref varchar(30) NULL,
	condicion int2 DEFAULT 1 NULL,
	status_bk int4 NULL,
	id_proceso int4 NULL,
	id_motivo_rechazo int4 NULL,
	update_at timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
	id_orden_serv int4 NULL
);

INSERT INTO almacenes.ordeningreso (id_ordeningreso, id_cliente, id_motivo, id_comprobante, nu_comprobante, fec_ingreso, fec_ref, nu_serie, cod_ingreso, id_almacen, id_orden, condicion) VALUES
    (280694, 86, 4, 1, 1168, '2025-05-02', '2025-05-02', 'F001', 'ALGI-I05831', 1, 13505, 1),
    (280955, 86, 4, 1, 1173, '2025-05-05', '2025-05-05', 'F001', 'ALGI-I05838', 1, 13523, 1),
    (281358, 86, 4, 1, 1175, '2025-05-07', '2025-05-07', 'F001', 'ALGI-I05848', 1, 13538, 1),
    (281997, 86, 4, 1, 1179, '2025-05-09', '2025-05-09', 'F001', 'ALGI-I05856', 1, 13557, 1),
    (282599, 86, 4, 1, 1181, '2025-05-12', '2025-05-12', 'F001', 'ALGI-I05861', 1, 13569, 1)
 ;
ALTER SEQUENCE almacenes.ordeningreso_id_ordeningreso_seq RESTART WITH 287661;

CREATE OR REPLACE FUNCTION almacenes.update_code_ingreso()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    lc_init_correlativo integer DEFAULT 0;
    lc_abrev varchar(6) DEFAULT '';
    lc_correlativo integer DEFAULT 0;
BEGIN
    IF (NEW.cod_ingreso IS NULL OR TRIM(NEW.cod_ingreso)='') OR NEW.id_almacen!=OLD.id_almacen THEN
        SELECT SUBSTRING(COALESCE(ord_ing.cod_ingreso, '0000000'), 7)::numeric
        , CASE WHEN motivo_ing.id_tipo_movimiento=1 THEN alm.prefijo_entrada WHEN motivo_ing.id_tipo_movimiento=2 THEN alm.prefijo_salida ELSE alm.prefijo_ajuste END AS cod_mov
        , CASE WHEN motivo_ing.id_tipo_movimiento=1 THEN alm.corre_entrada WHEN motivo_ing.id_tipo_movimiento=2 THEN alm.corre_salida ELSE alm.corre_ajuste END AS num_correlativo
        INTO lc_correlativo, lc_abrev, lc_init_correlativo
        FROM almacenes.almacen AS alm
        LEFT OUTER JOIN almacenes.tbmotivosingresos AS motiv_movi ON motiv_movi.id_motivo=1
        INNER JOIN almacenes.tbmotivosingresos AS motivo_ing ON motivo_ing.id_motivos_ingreso=motiv_movi.id_motivo AND motivo_ing.id_tipo_movimiento=motiv_movi.id_tipo_movimiento
        LEFT OUTER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_almacen=alm.id_almacen
        WHERE alm.id_almacen=NEW.id_almacen
        ORDER BY ord_ing.id_ordeningreso DESC
        LIMIT 1 OFFSET 0;
        lc_correlativo:=COALESCE(lc_correlativo, COALESCE(lc_init_correlativo, 0))+1;
        IF lc_abrev IS NULL THEN
            RAISE EXCEPTION '%', 'El motivo no coincide con el almacen';
        END IF;
        NEW.cod_ingreso:= trim(lc_abrev)||LPAD(trim(lc_correlativo::varchar(7)), 5,'0');
        RAISE NOTICE 'SE GENERO EL CODIGO %', NEW.cod_ingreso;
    END IF;
    RETURN NEW ;
END;
$function$
;

-- Table Triggers
CREATE TRIGGER tg_update_cod_ingreso BEFORE
INSERT OR UPDATE ON
    almacenes.ordeningreso FOR EACH ROW EXECUTE FUNCTION almacenes.update_code_ingreso();

CREATE TABLE almacenes.detordeningreso (
	id_detordeningreso serial4 NOT NULL,
	id_ordeningreso int4 NULL,
	id_articulo int4 NULL,
	id_unidad int4 NULL,
	lote varchar(20) NULL,
	peso_ref numeric(13, 3) NULL,
	peso_alm numeric(7, 2) NULL,
	peso_dif numeric(7, 2) NULL,
	nu_rollos numeric(13, 5) NULL,
	observacion varchar(500) NULL,
	cod_os varchar(20) NULL,
	id_tipo_producto int4 NULL,
	costo_compra numeric(13, 5) NULL,
	id_orden int4 DEFAULT 0 NULL,
	id_tipo_comprobante int4 DEFAULT 0 NULL,
	id_comprobante int4 DEFAULT 0 NULL,
	status int4 DEFAULT 1 NULL,
	id_kardex int4 NULL,
	add_orphan int2 DEFAULT 0 NULL,
	id_moneda int4 NULL,
	excento_imp int4 NULL,
	id_tipo int4 NULL,
	peso_merma numeric NULL,
	peso_percha numeric NULL,
	peso_acabado numeric NULL,
	peso_devolucion numeric NULL,
	CONSTRAINT almacen_detordeningreso_pkey PRIMARY KEY (id_detordeningreso),
	CONSTRAINT id_ordeningreso_id_articulo UNIQUE (id_ordeningreso, id_articulo, lote)
);
INSERT INTO almacenes.detordeningreso (id_detordeningreso, id_ordeningreso, lote, id_articulo, nu_rollos, id_unidad, id_moneda, costo_compra, excento_imp) VALUES
    (301796, 280694, '001168-1', 289, 240.00000, 1, 2, 2.15000, 0),
    (302091, 280955, '001173-1', 289, 120.00000, 1, 2, 2.15000, 0),
    (302522, 281358, '001175-1', 289, 120.00000, 1, 2, 2.15000, 0),
    (303180, 281997, '001179-1', 289, 240.00000, 1, 2, 2.15000, 0),
    (303834, 282599, '001181-1', 289, 120.00000, 1, 2, 2.15000, 0);

ALTER SEQUENCE almacenes.detordeningreso_id_detordeningreso_seq RESTART WITH 309161;

CREATE OR REPLACE FUNCTION almacenes.add_ingreso_compra()
    RETURNS trigger
    LANGUAGE plpgsql
AS $function$
DECLARE
    lc_valor_conv integer := 0;
    lc_id_almacen integer:= null;
    json_ultima_compra json;
    lc_is_multiplo char := null;
    lc_tipo_producto_fam integer := null;
    lc_unidad integer := 0;
    lc_unidad_consumo integer := 0;
    lc_cantidad numeric := 0.000;
    lc_mto_consumo numeric :=0.000;
    lc_fec_ult_compra date := null;
    lc_mto_ult_compra numeric :=0.000;
BEGIN
    SELECT COALESCE(conv_si.valor_conv, 0), COALESCE(conv_si.is_multiplo, '0'), fam.id_tipo_producto, art.id_unidad, art.id_unidad_consumo INTO lc_valor_conv, lc_is_multiplo, lc_tipo_producto_fam, lc_unidad, lc_unidad_consumo
    FROM logistica.tbarticulos AS art
    LEFT OUTER JOIN logistica.conversion_si AS conv_si ON conv_si.id_uni_medida=art.id_unidad AND conv_si.id_uni_medida_conv=art.id_unidad_consumo
    LEFT OUTER JOIN logistica.tbfamilia AS fam ON fam.id_familia=art.id_familia
    WHERE art.id_articulo=NEW.id_articulo;
    IF lc_tipo_producto_fam != 3 THEN
        IF NEW.costo_compra IS NULL THEN
            RAISE EXCEPTION 'Vaya no tiene registrado el monto de compra %, %', NEW.id_articulo, NEW.costo_compra;
        END IF;
        SELECT id_almacen, fec_ingreso INTO lc_id_almacen, lc_fec_ult_compra FROM almacenes.ordeningreso WHERE id_ordeningreso=NEW.id_ordeningreso;
        lc_cantidad:=NEW.nu_rollos;
        lc_mto_consumo:= NEW.costo_compra;
        IF lc_unidad!=lc_unidad_consumo AND (NEW.id_unidad!=lc_unidad_consumo) THEN
            lc_cantidad=POW(10, lc_valor_conv)*NEW.nu_rollos;
            lc_mto_consumo:= TRUNC((NEW.costo_compra/POW(10, lc_valor_conv))::numeric, 5);
        END IF;
        INSERT INTO almacenes.detalle_inventario (id_articulo, id_almacen, cantidad, cantidad_disponible, costo_compra, costo_consumo, id_detordeningreso) VALUES
            (NEW.id_articulo, lc_id_almacen, lc_cantidad, lc_cantidad, NEW.costo_compra, lc_mto_consumo, NEW.id_detordeningreso);
        SELECT almacenes.fn_seek_precio_ultima_compra_json(NEW.id_articulo) INTO json_ultima_compra;
        UPDATE logistica.tbarticulos SET mto_ult_compra=CASE WHEN json_ultima_compra->>'costo_compra' IS NOT NULL THEN (json_ultima_compra->>'costo_compra')::numeric ELSE NEW.costo_compra END
            , id_moneda_ult_compra=(json_ultima_compra->>'id_moneda')::integer
            , excento_ult_compra=(json_ultima_compra->>'excento')::integer
            , fec_ult_compra=(json_ultima_compra->>'fecha_compra')::date WHERE id_articulo=NEW.id_articulo;
    END IF;
    RETURN NEW;
END;
$function$
;

-- Table Triggers
CREATE TRIGGER tg_ingreso_compra AFTER
INSERT ON almacenes.detordeningreso FOR EACH ROW
    WHEN ((new.add_orphan = 0)) EXECUTE FUNCTION almacenes.add_ingreso_compra();

CREATE TABLE almacenes.detordeningresopeso (
	id_detordeningresopeso serial4 NOT NULL,
	id_ordeningreso int4 NULL,
	cod_rollo varchar(20) NULL,
	peso_rollo numeric(7, 2) NULL,
	id_detordeningreso int4 NOT NULL,
	id_rollo_ingreso int4 NULL,
	status int4 DEFAULT 1 NULL,
	peso_devolucion numeric NULL,
	id_det_peso_liquidacion int4 NULL,
	devolucion int2 DEFAULT 0 NULL,
	observacion text NULL,
	create_at timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
	update_at timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
	peso_merma numeric NULL,
	peso_acabado numeric NULL,
	peso_percha numeric NULL,
	num_cardinal int4 NULL,
	CONSTRAINT almacen_detordeningresopeso_pkey PRIMARY KEY (id_detordeningresopeso),
	CONSTRAINT id_detordeningreso_id_rollo_ingreso_uk UNIQUE (id_detordeningreso, id_rollo_ingreso),
	CONSTRAINT id_detordeningrespeso_id_rollo_ingreso UNIQUE (id_detordeningresopeso, id_rollo_ingreso)
);

ALTER TABLE almacenes.detordeningresopeso ADD CONSTRAINT fk_id_ordeningreso FOREIGN KEY (id_ordeningreso) REFERENCES almacenes.ordeningreso(id_ordeningreso);

CREATE OR REPLACE FUNCTION almacenes.disabled_change_weight_roll()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
BEGIN
  RAISE EXCEPTION 'EL ROLLO % SE ENCUENTRA BLOQUEADO NO PUEDE CAMBIAR DE PESO', OLD.cod_rollo;
END;
$function$
;

CREATE TRIGGER tg_disable_change_peso_crudo BEFORE
UPDATE
    ON almacenes.detordeningresopeso FOR EACH ROW
    WHEN (((old.status <> 1) AND (new.peso_rollo <> old.peso_rollo))) EXECUTE FUNCTION almacenes.disabled_change_weight_roll();

CREATE TABLE logistica.tbarticulos (
	id_articulo serial4 NOT NULL,
	id_familia int4 NULL,
	id_grupo int4 DEFAULT 0 NULL,
	cod_articulo varchar(20) NULL,
	desc_articulo text NULL,
	id_medida int4 DEFAULT 0 NULL,
	id_unidad int4 DEFAULT 0 NULL,
	id_marca int4 DEFAULT 0 NULL,
	id_componente int4 DEFAULT 0 NULL,
	id_tipo_tela int4 DEFAULT 0 NULL,
	galga varchar(50) NULL,
	descripcion varchar(200) NULL,
	stock_min int4 NULL,
	stock_max int4 NULL,
	msgstockminimo int4 DEFAULT 1 NULL,
	mto_compra numeric(14, 3) NULL,
	imagen varchar(200) NULL,
	fec_ingreso date NULL,
	status int4 DEFAULT 1 NULL,
	id_tipo_producto int2 DEFAULT 1 NULL,
	id_unidad_consumo int4 NULL,
	method_venta int2 DEFAULT 1 NOT NULL,
	mto_minimo numeric(14, 3) DEFAULT 0.00 NOT NULL,
	mto_sugerido numeric(14, 2) DEFAULT 0.00 NOT NULL,
	porc_ganancia numeric(5, 2) DEFAULT 0.00 NOT NULL,
	mto_consumo numeric(14, 5) DEFAULT 0.000 NULL,
	id_mezcla int4 NULL,
	porcentaje varchar(10) NULL,
	id_moneda int4 DEFAULT 1 NULL,
	id_tejido int4 DEFAULT 0 NULL,
	is_transformacion bool DEFAULT false NOT NULL,
	mto_ult_compra numeric(19, 5) DEFAULT 0.00 NOT NULL,
	fec_ult_compra date NULL,
	id_moneda_ult_compra int4 NULL,
	excento_ult_compra int4 NULL,
	excento_impuesto int4 DEFAULT 0 NULL,
	CONSTRAINT tbarticulo_pkey PRIMARY KEY (id_articulo),
	CONSTRAINT tbarticulos_unique UNIQUE (cod_articulo)
);

INSERT INTO logistica.tbarticulos (id_articulo,id_familia,id_grupo,cod_articulo,desc_articulo,id_medida,id_unidad,id_marca,id_componente,id_tipo_tela,galga,descripcion,stock_min,stock_max,msgstockminimo,mto_compra,imagen,fec_ingreso,status,id_tipo_producto,id_unidad_consumo,method_venta,mto_minimo,mto_sugerido,porc_ganancia,mto_consumo,id_mezcla,porcentaje,id_moneda,id_tejido,is_transformacion,mto_ult_compra,fec_ult_compra,id_moneda_ult_compra,excento_ult_compra,excento_impuesto) VALUES
	 (289,5,0,'PQ00080','SILTEX CONC',19,1,0,0,0,'','silicona',0,0,0,2.300,NULL,'2020-01-01',1,1,6,1,0.000,0.00,0.00,0.00200,0,'',2,0,false,2.15000,'2024-12-20',2,0,0);

CREATE OR REPLACE FUNCTION almacenes.fn_seek_precio_ultima_compra_json(p_id_articulo integer)
 RETURNS json
 LANGUAGE plpgsql
AS $function$
DECLARE
    ult_compra numeric := null;
    id_moneda integer := null;
    excento_imp integer := null;
    fecha_compra date := null;
    cur_det_compra REFCURSOR;
    rec_det_compra RECORD;
BEGIN

    OPEN cur_det_compra FOR SELECT det_ing.id_articulo, ord_ing.fec_ingreso, ord_ing.fec_registro, det_ing.nu_rollos
        , det_ord_comp.costo, det_ord_comp.id_moneda, det_ord_comp.exento_imp, det_ing.costo_compra, ord_ing.id_orden, ord_comp.cod_orden, ord_comp.fec_ingreso AS fec_compra
        FROM almacenes.detordeningreso AS det_ing
        INNER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso=det_ing.id_ordeningreso
        INNER JOIN logistica.ordencompra AS ord_comp ON ord_comp.id_orden=ord_ing.id_orden
        INNER JOIN logistica.detordencompra AS det_ord_comp ON det_ord_comp.id_orden=ord_comp.id_orden AND det_ord_comp.id_articulo=det_ing.id_articulo
        WHERE det_ing.id_articulo=p_id_articulo
        ORDER BY ord_ing.fec_ingreso DESC
        LIMIT 1 OFFSET 0;
    LOOP
    	FETCH cur_det_compra INTO rec_det_compra;
    	EXIT WHEN NOT FOUND;
        ult_compra:=rec_det_compra.costo;
        id_moneda:=rec_det_compra.id_moneda;
        excento_imp:=rec_det_compra.exento_imp;
        fecha_compra:=rec_det_compra.fec_compra;
    END LOOP;
    CLOSE cur_det_compra;
    RETURN json_build_object('costo_compra', ult_compra, 'id_moneda', id_moneda, 'excento', excento_imp, 'fecha_compra', fecha_compra);
END;
$function$
;
