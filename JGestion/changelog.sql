--20150315
alter table usuario rename column estado to activo;
alter table usuario alter column activo drop default;
alter table usuario alter column activo set data type boolean using activo=1;
--20150206
INSERT INTO cheque_estado VALUES(9,'RECHAZADO');
ALTER TABLE cheque_terceros ADD CONSTRAINT fk_cheque_terceros_cheque_estado FOREIGN KEY (estado) REFERENCES cheque_estado (id);
ALTER TABLE cheque_propio ADD CONSTRAINT fk_cheque_propio_cheque_estado FOREIGN KEY (estado) REFERENCES cheque_estado (id);
--20150129
CREATE TABLE ventasimpleconfig (
   id serial NOT NULL, 
   cliente_id integer NOT NULL, 
   caja_id integer NOT NULL, 
   sucursal_id integer NOT NULL, 
   lista_precios_id integer NOT NULL, 
   unidad_de_negocio_id integer, 
   cuenta_id integer, 
   subcuenta_id integer, 
    PRIMARY KEY (id), 
    FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    FOREIGN KEY (caja_id) REFERENCES caja (id),
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id),
    FOREIGN KEY (cuenta_id) REFERENCES movimiento_concepto (id),
    FOREIGN KEY (subcuenta_id) REFERENCES subcuenta (id),
    FOREIGN KEY (lista_precios_id) REFERENCES lista_precios (id)
);
CREATE TABLE ventasimpleconfig_rubro (
  ventasimpleconfig_id integer NOT NULL,
  rubro_id integer NOT NULL,
  CONSTRAINT ventasimpleconfig_rubro_pkey PRIMARY KEY (ventasimpleconfig_id, rubro_id),
  CONSTRAINT ventasimpleconfig_rubro_rubro_id_fkey FOREIGN KEY (rubro_id) REFERENCES rubro (idrubro),
  CONSTRAINT ventasimpleconfig_rubro_ventasimpleconfig_id_fkey FOREIGN KEY (ventasimpleconfig_id) REFERENCES ventasimpleconfig (id)
);
--20141029
ALTER TABLE ctacte_cliente ADD CONSTRAINT ctacte_cliente_entregas_check CHECK (entregado >=0 and entregado <= importe);
--20140825
ALTER TABLE presupuesto ADD COLUMN observacion varchar(250);
--20131127
ALTER TABLE cheque_propio ADD COLUMN estadoprevio INTEGER;
--20131121
ALTER TABLE cheque_terceros ADD COLUMN estadoprevio INTEGER;
--20131111
ALTER TABLE cliente DROP CONSTRAINT fk_cliente_sucursal;
ALTER TABLE cliente DROP COLUMN sucursal;
CREATE TABLE documento_comercial (
   id serial NOT NULL, 
   nombre character varying(100) NOT NULL, 
   minlength integer NOT NULL DEFAULT 1, 
   maxlength integer NOT NULL DEFAULT 1, 
   alphanumeric boolean NOT NULL DEFAULT false, 
   unicoporempresa boolean NOT NULL DEFAULT false, 
    PRIMARY KEY (id), 
    UNIQUE (nombre)
);
CREATE TABLE documento_comercial_cliente (
   id serial NOT NULL, 
   cliente_id integer NOT NULL, 
   documento_comercial_id integer NOT NULL, 
   numero character varying(40) NOT NULL, 
   importe numeric(12,2) NOT NULL, 
   fecha date NOT NULL DEFAULT now(), 
   origen character varying(200) NOT NULL, 
   anulado boolean NOT NULL DEFAULT false, 
   observacion character varying(200), 
    PRIMARY KEY (id), 
    FOREIGN KEY (cliente_id) REFERENCES cliente (id), 
    FOREIGN KEY (documento_comercial_id) REFERENCES documento_comercial (id)
);
CREATE TABLE documento_comercial_proveedor (
   id serial NOT NULL, 
   proveedor_id integer NOT NULL, 
   documento_comercial_id integer NOT NULL, 
   numero character varying(40) NOT NULL, 
   importe numeric(12,2) NOT NULL, 
   fecha date NOT NULL DEFAULT now(), 
   origen character varying(200) NOT NULL, 
   anulado boolean NOT NULL DEFAULT false, 
   observacion character varying(200), 
    PRIMARY KEY (id), 
    FOREIGN KEY (proveedor_id) REFERENCES proveedor (id), 
    FOREIGN KEY (documento_comercial_id) REFERENCES documento_comercial (id)
);
--20130924
ALTER TABLE ctacte_cliente ALTER COLUMN factura DROP NOT NULL;
ALTER TABLE ctacte_cliente ADD COLUMN notadebito_id integer;
ALTER TABLE ctacte_cliente DROP CONSTRAINT fk_ctacte_cliente_factura;
ALTER TABLE ctacte_cliente ADD CONSTRAINT fk_ctacte_cliente_factura FOREIGN KEY (factura) REFERENCES factura_venta (id);
ALTER TABLE ctacte_cliente ADD FOREIGN KEY (notadebito_id) REFERENCES nota_debito (id);
ALTER TABLE ctacte_cliente ADD CHECK ((factura is not null and notadebito_id is null) or (factura is null and notadebito_id is not null));
--después de cargar todo a la cta cte cliente!!!
ALTER TABLE nota_debito DROP COLUMN recibo_id;
ALTER TABLE nota_debito DROP CONSTRAINT fk_nota_debito_recibo;

--20130716
ALTER TABLE detalle_presupuesto ALTER COLUMN descuento TYPE numeric(12,4);
ALTER TABLE detalle_presupuesto ALTER COLUMN precio_unitario TYPE numeric(12,4);
--20130619
CREATE TABLE dominio (
   id serial NOT NULL, 
   nombre character varying(100) NOT NULL, 
    PRIMARY KEY (id), 
    UNIQUE (nombre)
); 
ALTER TABLE factura_compra ADD COLUMN dominio_id integer;
ALTER TABLE factura_compra ADD FOREIGN KEY (dominio_id) REFERENCES dominio (id);
ALTER TABLE factura_compra ADD COLUMN dominio_id integer;
--20130504
alter table remesa add column por_conciliar boolean not null default false;
alter table remesa add column proveedor_id integer;
alter table remesa add constraint fk_remesa_proveedor_id foreign key (proveedor_id) references proveedor (id);
--20130425
alter table permisos add column cheques_administrador boolean not null default false;
--20130415
alter table cuentabancaria_movimientos add column conciliado boolean not null default false;
--20130325
alter table recibo add column por_conciliar boolean not null default false;
alter table recibo add column cliente_id integer;
alter table recibo add constraint fk_recibo_cliente_id foreign key (cliente_id) references cliente (id);
--20130316
alter table usuario_acciones add column accion character(1) not null;
alter table usuario_acciones alter column accion drop default;
--20130311
CREATE TABLE nota_debito_proveedor (
  id serial NOT NULL,
  numero numeric(12,0) NOT NULL,
  fecha_nota_debito date NOT NULL,
  fecha_carga timestamp with time zone NOT NULL DEFAULT now(),
  importe numeric(12,2) NOT NULL,
  usuario_id integer NOT NULL,
  proveedor_id integer NOT NULL,
  observacion character varying(200),
  gravado numeric(12,2) NOT NULL DEFAULT 0,
  no_gravado numeric(12,2) NOT NULL DEFAULT 0,
  iva10 numeric(12,2) NOT NULL DEFAULT 0,
  iva21 numeric(12,2) NOT NULL DEFAULT 0,
  otros_ivas numeric(12,2) NOT NULL DEFAULT 0,
  anulada boolean NOT NULL DEFAULT false,
  impuestos_recuperables numeric(12,2) NOT NULL DEFAULT 0,
  remesa_id integer,
  tipo character(1) NOT NULL,
  CONSTRAINT pk_nota_debito_proveedor PRIMARY KEY (id),
  CONSTRAINT fk_nota_debito_proveedor_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor (id),
  CONSTRAINT fk_nota_debito_proveedor_remesa FOREIGN KEY (remesa_id) REFERENCES remesa (id),
  CONSTRAINT fk_nota_debito_proveedor_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
  CONSTRAINT unq_nota_debito_proveedor_proveedor_numero UNIQUE (proveedor_id, numero)
);
CREATE TABLE detalle_nota_debito_proveedor (
  id serial NOT NULL,
  nota_debito_proveedor_id integer NOT NULL,
  concepto character varying(200) NOT NULL,
  importe numeric(12,2) NOT NULL,
  iva_id integer, -- Si la nota de débito es tipo "B", no se discriminan, por lo tanto esta columna va ser <null>
  CONSTRAINT pk_detalle_nota_debito_proveedor PRIMARY KEY (id),
  CONSTRAINT fk_detalle_nota_debito_proveedor_iva_id FOREIGN KEY (iva_id) REFERENCES iva (id),
  CONSTRAINT fk_detalle_nota_debito_proveedor_nota_debito_proveedor_id FOREIGN KEY (nota_debito_proveedor_id) REFERENCES nota_debito_proveedor (id)
);
ALTER TABLE detalle_nota_debito_proveedor OWNER TO postgres;
COMMENT ON COLUMN detalle_nota_debito_proveedor.iva_id IS 'Si la nota de débito es tipo "B", no se discriminan, por lo tanto esta columna va ser <null>';
ALTER TABLE detalle_remesa ALTER COLUMN factura_compra DROP NOT NULL;
ALTER TABLE detalle_remesa ADD COLUMN nota_debito_proveedor_id integer;
ALTER TABLE detalle_remesa ADD CONSTRAINT fk_detalle_remesa_nota_debito_proveedor_id FOREIGN KEY (nota_debito_proveedor_id) REFERENCES nota_debito_proveedor (id);
--20130310
CREATE TABLE cheque_terceros_entrega (
  id serial NOT NULL,
  usuario_emisor_id integer NOT NULL,
  usuario_receptor_id integer NOT NULL,
  fecha_creacion timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT cheque_terceros_entrega_pkey PRIMARY KEY (id),
  CONSTRAINT fk_cheque_terceros_entrega_usuario_emisor_id FOREIGN KEY (usuario_emisor_id) REFERENCES usuario (id),
  CONSTRAINT fk_cheque_terceros_entrega_usuario_receptor_id FOREIGN KEY (usuario_receptor_id) REFERENCES usuario (id)
);
CREATE TABLE cheque_terceros_entrega_detalle (
  id serial NOT NULL,
  cheque_terceros_id integer NOT NULL,
  cheque_terceros_entrega_id integer NOT NULL,
  CONSTRAINT cheque_terceros_entrega_detalle_pkey PRIMARY KEY (id ),
  CONSTRAINT cheque_terceros_entrega_detalle_cheque_terceros_entrega_id_fkey FOREIGN KEY (cheque_terceros_entrega_id)
      REFERENCES cheque_terceros_entrega (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_entrega_detalle_cheque_terceros_id_fkey FOREIGN KEY (cheque_terceros_id)
      REFERENCES cheque_terceros (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_entrega_detalle_cheque_terceros_entrega_id_key UNIQUE (cheque_terceros_entrega_id , cheque_terceros_id )
);
--20130306
alter table detalle_venta add column costo_compra numeric(12,4) not null default 0;
--20130305
ALTER TABLE cheque_terceros ADD CONSTRAINT unq_cliente_banco_numero UNIQUE (cliente, banco, numero);
ALTER TABLE cheque_propio ADD CONSTRAINT unq_proveedor_banco_numero UNIQUE (proveedor, banco, numero);
--20130304
alter table producto add column bien_de_cambio boolean not null default true;
ALTER TABLE detalle_recibo ALTER COLUMN factura_venta DROP NOT NULL;
ALTER TABLE detalle_recibo ADD COLUMN nota_debito_id integer;
ALTER TABLE detalle_recibo ADD FOREIGN KEY (nota_debito_id) REFERENCES nota_debito (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
--20130301
ALTER TABLE sucursal ADD COLUMN notadebito_a NUMERIC(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ADD COLUMN notadebito_b NUMERIC(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ALTER COLUMN notadebito_a DROP DEFAULT;
ALTER TABLE sucursal ALTER COLUMN notadebito_b DROP DEFAULT;
CREATE TABLE nota_debito (
  id serial NOT NULL,
  numero numeric(8,0) NOT NULL,
  tipo character(1) NOT NULL,
  fecha_nota_debito date NOT NULL,
  fecha_carga timestamp with time zone NOT NULL DEFAULT now(),
  importe numeric(12,2) NOT NULL,
  usuario_id integer NOT NULL,
  cliente_id integer NOT NULL,
  sucursal_id integer NOT NULL,
  observacion character varying(200),
  gravado numeric(12,2) NOT NULL DEFAULT 0,
  no_gravado numeric(12,2) NOT NULL DEFAULT 0,
  iva10 numeric(12,2) NOT NULL DEFAULT 0,
  iva21 numeric(12,2) NOT NULL DEFAULT 0,
  otros_ivas numeric(12,2) NOT NULL DEFAULT 0,
  anulada boolean NOT NULL DEFAULT false,
  impuestos_recuperables numeric(12,2) NOT NULL DEFAULT 0,
  recibo_id integer,
  CONSTRAINT nota_debito_pkey PRIMARY KEY (id ),
  CONSTRAINT fk_nota_debito_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
  CONSTRAINT fk_nota_debito_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursal (id),
  CONSTRAINT fk_nota_debito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
  CONSTRAINT fk_nota_debito_recibo FOREIGN KEY (recibo_id) REFERENCES recibo (id),
  CONSTRAINT unq_nota_debito_sucursal_id_tipo_numero UNIQUE (sucursal_id, tipo, numero)
) WITH (  OIDS=FALSE);
CREATE TABLE detalle_nota_debito (
  id serial NOT NULL,
  nota_debito_id integer NOT NULL,
  concepto character varying (200) NOT NULL,
  importe numeric(12,2) NOT NULL,
  iva_id integer,
  primary key (id),
  foreign key (nota_debito_id) references nota_debito (id),
  foreign key (iva_id) references iva (id)
 ) WITH (  OIDS=FALSE);
COMMENT ON COLUMN detalle_nota_debito.iva_id IS 'Si la nota de débito es tipo "B", no se discriminan, por lo tanto esta columna va ser <null>';
--20130228
CREATE TABLE vendedor (
  id serial NOT NULL, 
  apellido character varying(30) NOT NULL, 
  nombre character varying(50) NOT NULL, 
  direccion character varying(100), 
  tele1 bigint, 
  tele2 bigint, 
  email character varying(50), 
  observacion character varying(200),
activo boolean NOT NULL,
  PRIMARY KEY (id)
) 
WITH (  OIDS = FALSE);
ALTER TABLE factura_venta ADD COLUMN vendedor_id integer;
ALTER TABLE factura_venta ADD FOREIGN KEY (vendedor_id) REFERENCES vendedor (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
alter table remito add column vendedor_id integer;
alter table remito add foreign key (vendedor_id) references vendedor (id);
--20130224
update factura_compra set perc_dgr = perc_iva;
update factura_compra set perc_iva = 0;
--20130221 arreglando la descripción
update operaciones_bancarias set nombre ='x' where id = 2;
update operaciones_bancarias set nombre ='TRANSFERENCIA' where id = 3;
update operaciones_bancarias set nombre ='EXTRACCIÓN' where id = 2;
--20130208
ALTER TABLE producto ALTER COLUMN costo_compra TYPE numeric(12,4);
ALTER TABLE producto ALTER COLUMN precio_venta TYPE numeric(12,4);
alter table cliente add column limite_ctacte numeric(12,0) not null default 0;
alter table proveedor add column limite_ctacte numeric(12,0) not null default 0;
alter table movimiento_concepto add column ingreso boolean not null default true;
--20130204
ALTER TABLE detalle_venta ALTER COLUMN descuento TYPE numeric(12,4);
ALTER TABLE detalle_venta ALTER COLUMN precio_unitario TYPE numeric(12,4);
--20130123
CREATE TABLE credito_proveedor (
  id serial NOT NULL,
  proveedor_id integer NOT NULL,
  debe boolean NOT NULL,
  importe numeric(12,2) NOT NULL,
  concepto character varying(200) NOT NULL,
  fecha_carga timestamp with time zone NOT NULL DEFAULT now(),
  PRIMARY KEY (id ),
  FOREIGN KEY (proveedor_id) REFERENCES proveedor (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH ( OIDS=FALSE );
--20121219
ALTER TABLE subcuenta DROP CONSTRAINT subcuenta_nombre_key;
ALTER TABLE subcuenta ADD UNIQUE (cuenta_id, nombre);
--20121217
ALTER TABLE factura_compra ALTER COLUMN factura_octeto DROP NOT NULL;
ALTER TABLE factura_compra ALTER COLUMN factura_cuarto DROP NOT NULL;
ALTER TABLE factura_venta ADD COLUMN observacion character(100);
ALTER TABLE factura_compra ADD COLUMN observacion character(100);
CREATE TABLE usuario_acciones (
  id serial NOT NULL,
  usuario_id integer NOT NULL,
  descripcion character(200) NOT NULL,
  detalle character(2000),
  fechasistema timestamp with time zone NOT NULL DEFAULT now(),
  ip character(24),
  hostname character(50),
  entidad character(100) NOT NULL,
  entidad_id integer NOT NULL,
  CONSTRAINT usuario_acciones_pkey PRIMARY KEY (id),
  CONSTRAINT usuario_acciones_usuarios_id_fkey FOREIGN KEY (usuario_id)
      REFERENCES usuario (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (  OIDS=FALSE);

ALTER TABLE factura_compra ADD COLUMN unidad_de_negocio_id integer;
ALTER TABLE factura_compra ADD COLUMN cuenta_id integer;
ALTER TABLE factura_compra ADD COLUMN subcuenta_id integer;
ALTER TABLE factura_compra ADD FOREIGN KEY (unidad_de_negocio_id) REFERENCES unidad_de_negocio (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE factura_compra ADD FOREIGN KEY (subcuenta_id) REFERENCES subcuenta (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE factura_compra ADD FOREIGN KEY (cuenta_id) REFERENCES movimiento_concepto (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE factura_venta ADD COLUMN unidad_de_negocio_id integer;
ALTER TABLE factura_venta ADD COLUMN cuenta_id integer;
ALTER TABLE factura_venta ADD COLUMN subcuenta_id integer;
ALTER TABLE factura_venta ADD FOREIGN KEY (unidad_de_negocio_id) REFERENCES unidad_de_negocio (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE factura_venta ADD FOREIGN KEY (subcuenta_id) REFERENCES subcuenta (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE factura_venta ADD FOREIGN KEY (cuenta_id) REFERENCES movimiento_concepto (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE detalle_caja_movimientos DROP CONSTRAINT detalle_caja_movimientos_movimiento_concepto_fkey;
ALTER TABLE detalle_caja_movimientos ADD COLUMN unidad_de_negocio_id integer;
ALTER TABLE detalle_caja_movimientos ADD COLUMN subcuenta_id integer;
ALTER TABLE detalle_caja_movimientos ADD CONSTRAINT fk_detalle_caja_movimientos_unidad_de_negocio FOREIGN KEY (unidad_de_negocio_id) REFERENCES unidad_de_negocio (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE detalle_caja_movimientos ADD CONSTRAINT fk_detalle_caja_movimientos_subcuenta FOREIGN KEY (subcuenta_id) REFERENCES subcuenta (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE TABLE subcuenta (
   id serial NOT NULL, 
   nombre character(100) NOT NULL, 
   cuenta_id integer NOT NULL, 
    PRIMARY KEY (id), 
    FOREIGN KEY (cuenta_id) REFERENCES movimiento_concepto (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
    UNIQUE (nombre)
) WITH (  OIDS = FALSE);
CREATE TABLE unidad_de_negocio (
    id serial NOT NULL,
    nombre character(100),
    PRIMARY KEY (id),
    UNIQUE (nombre)
) with ( OIDS = FALSE );
CREATE TABLE unidad_de_negocio_sucursal (
   unidad_de_negocio_id integer NOT NULL, 
   sucursal_id integer NOT NULL, 
    PRIMARY KEY (unidad_de_negocio_id, sucursal_id), 
    FOREIGN KEY (unidad_de_negocio_id) REFERENCES unidad_de_negocio (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
    FOREIGN KEY (sucursal_id) REFERENCES sucursal (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (  OIDS = FALSE);
ALTER TABLE cheque_terceros ALTER COLUMN cliente DROP NOT NULL;
alter table cuentabancaria alter column numero type character(22) using numero::character(22);
ALTER TABLE remesa ADD COLUMN anulada timestamp with time zone;
ALTER TABLE remito ADD COLUMN anulada timestamp with time zone;
ALTER TABLE factura_compra ADD COLUMN otros_ivas numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE nota_credito ADD COLUMN otros_ivas numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE cheque_terceros DROP COLUMN librado;
ALTER TABLE cheque_propio DROP COLUMN librado;
ALTER TABLE detalle_compra ALTER precio_unitario TYPE numeric(12,4);
ALTER TABLE nota_credito ADD COLUMN recibo integer;
ALTER TABLE nota_credito ADD FOREIGN KEY (recibo) REFERENCES recibo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE detalle_nota_credito ALTER precio_unitario TYPE numeric(12,4);
ALTER TABLE ctacte_proveedor ALTER entregado TYPE numeric(12,2);
ALTER TABLE ctacte_proveedor ALTER importe TYPE numeric(12,2);
ALTER TABLE ctacte_cliente ALTER entregado TYPE numeric(12,2);
ALTER TABLE ctacte_cliente ALTER importe TYPE numeric(12,2);
ALTER TABLE detalle_remesa ADD UNIQUE (remesa, factura_compra);
ALTER TABLE detalle_recibo ADD UNIQUE (recibo, factura_venta);
2012/10/14
ALTER TABLE cheque_propio ADD COLUMN cuentabancaria_id integer NOT NULL;
ALTER TABLE cheque_propio ADD FOREIGN KEY (cuentabancaria_id) REFERENCES cuentabancaria (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE comprobante_retencion DROP CONSTRAINT unq_comprobante_retencion_0;
ALTER TABLE comprobante_retencion ADD COLUMN propio boolean NOT NULL DEFAULT false;
ALTER TABLE comprobante_retencion ADD UNIQUE (numero, propio);
ALTER TABLE comprobante_retencion ALTER COLUMN propio DROP DEFAULT;
ALTER TABLE remesa DROP CONSTRAINT remesa_unique_añofiajwñoefijawo;
ALTER TABLE remesa ADD UNIQUE (sucursal, numero);
2012/10/10
ALTER TABLE iva ALTER iva TYPE numeric(5,2);
ALTER TABLE nota_credito DROP CONSTRAINT unq_nota_credito_0;
ALTER TABLE nota_credito ADD UNIQUE (sucursal, numero);
CREATE TABLE nota_credito_proveedor (
  id serial NOT NULL,
  anulada boolean NOT NULL,
  desacreditado numeric(12,2) NOT NULL,
  fecha_carga timestamp with time zone NOT NULL DEFAULT now(),
  fecha_nota_credito_proveedor date NOT NULL,
  gravado numeric(12,2) NOT NULL,
  importe numeric(12,2) NOT NULL,
  impuestos_recuperables numeric(12,2) NOT NULL,
  iva10 numeric(12,2) NOT NULL,
  iva21 numeric(12,2) NOT NULL,
  no_gravado numeric(12,2) NOT NULL,
  numero bigint NOT NULL,
  observacion character varying(250),
  proveedor integer NOT NULL,
  usuario integer NOT NULL,
  remesa integer,
  PRIMARY KEY (id),
  FOREIGN KEY (proveedor) REFERENCES proveedor (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  FOREIGN KEY (usuario) REFERENCES usuario (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  FOREIGN KEY (remesa) REFERENCES remesa (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  UNIQUE (proveedor, numero)
)
WITH ( OIDS=FALSE );

CREATE TABLE detalle_nota_credito_proveedor (
  id serial NOT NULL,
  cantidad integer NOT NULL,
  precio_unitario numeric(12,4) NOT NULL,
  nota_credito_proveedor integer NOT NULL,
  producto integer NOT NULL,
  CONSTRAINT detalle_nota_credito_proveedor_pkey PRIMARY KEY (id),
  CONSTRAINT fk_detalle_nota_credito_proveedor_nota_credito_proveedor FOREIGN KEY (nota_credito_proveedor)
      REFERENCES nota_credito_proveedor (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_detalle_nota_credito_proveedor_producto FOREIGN KEY (producto)
      REFERENCES producto (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH ( OIDS=FALSE );
2012/10/08
CREATE TABLE remesa_pagos (
   id serial NOT NULL, 
   comprobante_id integer NOT NULL, 
   forma_pago integer NOT NULL, 
   remesa_id integer NOT NULL, 
    PRIMARY KEY (id), 
    FOREIGN KEY (remesa_id) REFERENCES remesa (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (  OIDS = FALSE);
COMMENT ON COLUMN remesa_pagos.forma_pago IS '0=Efectivo, 1=Cheque Propio, 2=Cheque Tercero, 3=Nota de Crédito, 4=Retención';
CREATE TABLE cheque_estado (
  id integer NOT NULL,
  nombre character varying(25) NOT NULL,
  CONSTRAINT cheque_estado_pkey PRIMARY KEY (id),
  CONSTRAINT cheque_estado_nombre_key UNIQUE (nombre)
) WITH (  OIDS=FALSE);
2012/09/23
ALTER TABLE recibo ALTER COLUMN retencion SET DEFAULT 0;
ALTER TABLE banco ADD COLUMN webpage character varying(100);
ALTER TABLE banco ADD UNIQUE (webpage);
CREATE TABLE recibo_pagos (
  id serial NOT NULL,
  comprobante_id integer NOT NULL,
  forma_pago integer NOT NULL, -- 0=Efectivo, 1=Cheque Propio, 2=Cheque Tercero, 3=Nota de Crédito, 4=Retención
  recibo_id integer NOT NULL,
  CONSTRAINT recibo_pagos_pkey PRIMARY KEY (id),
  CONSTRAINT fk_recibo_pagos_recibo_id FOREIGN KEY (recibo_id)
      REFERENCES recibo (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (  OIDS=FALSE);
COMMENT ON COLUMN recibo_pagos.forma_pago IS '0=Efectivo, 1=Cheque Propio, 2=Cheque Tercero, 3=Nota de Crédito, 4=Retención';

CREATE TABLE comprobante_retencion (
  id serial NOT NULL,
  fecha date NOT NULL,
  importe numeric(12,2) NOT NULL,
  numero bigint NOT NULL,
  CONSTRAINT comprobante_retencion_pkey PRIMARY KEY (id),
  CONSTRAINT unq_comprobante_retencion_0 UNIQUE (numero)
) WITH (  OIDS=FALSE);
CREATE TABLE cuentabancaria(
  id serial NOT NULL,
  activa boolean NOT NULL,
  numero bigint NOT NULL,
  saldo numeric(12,2) NOT NULL,
  banco_id integer NOT NULL,
  CONSTRAINT cuentabancaria_pkey PRIMARY KEY (id),
  CONSTRAINT fk_cuentabancaria_banco_id FOREIGN KEY (banco_id)
      REFERENCES banco (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT unq_cuentabancaria_0 UNIQUE (banco_id, numero)
)
WITH (  OIDS=FALSE);

CREATE TABLE operaciones_bancarias (
  id serial NOT NULL,
  nombre character varying(60) NOT NULL,
  CONSTRAINT operaciones_bancarias_pkey PRIMARY KEY (id),
  CONSTRAINT operaciones_bancarias_nombre_key UNIQUE (nombre)
)
WITH ( OIDS=FALSE);
CREATE TABLE cuentabancaria_movimientos (
  id serial NOT NULL,
  cuentabancaria_id integer NOT NULL,
  fecha_operacion date NOT NULL,
  descripcion character varying(200),
  fecha_credito_debito date,
  credito numeric(10,2) NOT NULL,
  debito numeric(10,2) NOT NULL,
  operaciones_bancarias_id integer NOT NULL,
  usuario_id integer NOT NULL,
  fecha_sistema timestamp with time zone NOT NULL DEFAULT now(),
  cheque_propio_id integer,
  cheque_terceros_id integer,
  anulada boolean NOT NULL,
  CONSTRAINT cuentabancaria_movimientos_pkey PRIMARY KEY (id),
  CONSTRAINT cuentabancaria_movimientos_cheque_propio_id_fkey FOREIGN KEY (cheque_propio_id)
      REFERENCES cheque_propio (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cuentabancaria_movimientos_cheque_terceros_id_fkey FOREIGN KEY (cheque_terceros_id)
      REFERENCES cheque_terceros (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cuentabancaria_movimientos_cuentabancaria_id_fkey FOREIGN KEY (cuentabancaria_id)
      REFERENCES cuentabancaria (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cuentabancaria_movimientos_operaciones_bancarias_id_fkey FOREIGN KEY (operaciones_bancarias_id)
      REFERENCES operaciones_bancarias (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cuentabancaria_movimientos_usuario_id_fkey FOREIGN KEY (usuario_id)
      REFERENCES usuario (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH ( OIDS=FALSE);
ALTER TABLE permisos ADD COLUMN abm_cuentabancaria boolean NOT NULL DEFAULT false;
ALTER TABLE permisos ALTER COLUMN abm_cuentabancaria DROP NOT NULL;
2012/08/26
ALTER TABLE factura_venta ADD COLUMN no_gravado numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE nota_credito ADD COLUMN no_gravado numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE nota_credito ADD COLUMN impuestos_recuperables numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE factura_compra ADD COLUMN no_gravado numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE factura_compra ADD COLUMN gravado numeric(12,2) NOT NULL DEFAULT 0;
ALTER TABLE factura_compra ALTER impuestos_recuperables TYPE numeric(12,2);
ALTER TABLE factura_compra ALTER impuestos_norecuperables TYPE numeric(12,2);
ALTER TABLE factura_compra ALTER descuento TYPE numeric(12,2);
2012/08/15
ALTER TABLE nota_credito ALTER numero TYPE numeric(8,0);
ALTER TABLE factura_venta ADD COLUMN diferencia_redondeo numeric(12,2) NOT NULL DEFAULT 0;
2012/07/03
alter table producto add column updateprecioventa boolean default false not null;
ALTER TABLE producto DROP COLUMN margen;
ALTER TABLE producto DROP COLUMN tipomargen;
2012/04/13
--alter table sucursal add column puntoventa integer not null default 1;
ALTER TABLE sucursal ADD COLUMN factura_a numeric(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ADD COLUMN factura_b numeric(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ADD COLUMN remito numeric(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ADD COLUMN recibo numeric(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ADD COLUMN notacredito numeric(8,0) NOT NULL DEFAULT 1;
ALTER TABLE sucursal ALTER COLUMN puntoventa DROP DEFAULT;
ALTER TABLE sucursal ALTER COLUMN factura_a DROP DEFAULT;
ALTER TABLE sucursal ALTER COLUMN factura_b DROP DEFAULT;
ALTER TABLE sucursal ALTER COLUMN remito DROP DEFAULT;
ALTER TABLE sucursal ALTER COLUMN recibo DROP DEFAULT;
ALTER TABLE sucursal ALTER COLUMN notacredito DROP DEFAULT;
2012/04/11
ALTER TABLE permisos ADD COLUMN venta_numeracion_manual boolean NOT NULL DEFAULT false;
2012/04/03
CREATE TABLE usuario_permisossucursal (
  usuario_id integer NOT NULL,
  sucursales_id integer NOT NULL,
  CONSTRAINT usuario_permisossucursal_pkey PRIMARY KEY (usuario_id, sucursales_id),
  CONSTRAINT fk_usuario_permisossucursal_sucursales_id FOREIGN KEY (sucursales_id)
      REFERENCES permisossucursal (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_usuario_permisossucursal_usuario_id FOREIGN KEY (usuario_id)
      REFERENCES usuario (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (  OIDS=FALSE);
ALTER TABLE presupuesto ADD COLUMN numero numeric(8,0);
update presupuesto set numero = id;
ALTER TABLE presupuesto add unique (numero, sucursal);
--checkear que la columna numero tenga mas de un caracter (cuando son movimientos internos happends)
UPDATE factura_venta SET numero = to_number(substring(numero|| '' from 2) , '99999999');
ALTER TABLE factura_venta ALTER numero TYPE numeric(8,0);
UPDATE remito SET numero = to_number(substring(numero|| '' from 2) , '99999999');
ALTER TABLE remito ALTER numero TYPE numeric(8,0);
update recibo set numero = cast(substring(id || '' from 2) as integer);
ALTER TABLE recibo ADD COLUMN numero numeric(8,0);
CREATE SEQUENCE depto_iddepto_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 99999999
   CACHE 1;
ALTER TABLE depto ALTER COLUMN iddepto SET DEFAULT nextval('depto_iddepto_seq');
CREATE SEQUENCE recibo_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 99999999
   CACHE 1;
ALTER TABLE recibo add unique (numero, sucursal);
ALTER TABLE recibo ALTER id TYPE integer;
ALTER TABLE recibo ALTER COLUMN id SET DEFAULT nextval('recibo_id_seq');
ALTER TABLE sucursal ADD COLUMN puntoventa integer NOT NULL DEFAULT 1;
ALTER TABLE sucursal ADD UNIQUE (puntoventa);
2011/11/21
//-- nueva estructura de mensajes informativos del sistema
DROP TABLE msj_informativos;
CREATE TABLE msj_informativos
(
  id integer NOT NULL,
  message text NOT NULL,
  code character varying(500) NOT NULL,
  CONSTRAINT msj_informativos_pkey PRIMARY KEY (id),
  CONSTRAINT msj_informativos_code_key UNIQUE (code)
)
WITH (
  OIDS=FALSE
);

2011/09/03
    ALTER TABLE ADD COLUMN acreditado boolean NOT NULL DEFAULT false
2011/08/30
//DB CREATE TABLE's cheque_estado, librado, banco, banco_sucursal, cheque_propio, cheque_tercero
CREATE TABLE cheque_estado
(
  id integer NOT NULL,
  nombre character varying(20) NOT NULL,
  CONSTRAINT cheque_estado_pkey PRIMARY KEY (id),
  CONSTRAINT cheque_estado_nombre_key UNIQUE (nombre)
)
WITH (
  OIDS=FALSE
);
CREATE TABLE librado
(
  id integer NOT NULL,
  nombre character varying(30) NOT NULL,
  CONSTRAINT librado_pkey PRIMARY KEY (id),
  CONSTRAINT librado_nombre_key UNIQUE (nombre)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE librado OWNER TO postgres;
COMMENT ON TABLE librado IS 'Se refiera a la forma/situación/estado que se encuentra el cheque';

CREATE TABLE banco
(
  id serial NOT NULL,
  nombre character varying(100) NOT NULL,
  version_banco bigint,
  CONSTRAINT banco_pkey PRIMARY KEY (id),
  CONSTRAINT banco_nombre_key UNIQUE (nombre)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE banco OWNER TO postgres;
COMMENT ON TABLE banco IS 'Bancos, los cuales poseén sucursales (tabla banco_sucursal)';

CREATE TABLE banco_sucursal
(
  id serial NOT NULL,
  nombre character varying(200) NOT NULL,
  codigo character varying(20),
  direccion character varying(200) NOT NULL,
  telefono numeric(12,0),
  banco integer NOT NULL,
  CONSTRAINT banco_sucursal_pkey PRIMARY KEY (id),
  CONSTRAINT banco_sucursal_banco_fkey FOREIGN KEY (banco)
      REFERENCES banco (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT banco_sucursal_nombre_key UNIQUE (nombre, banco)
)
WITH (
  OIDS=FALSE
);

CREATE TABLE cheque_propio
(
  id serial NOT NULL,
  numero numeric(20,0) NOT NULL,
  fecha_cheque date NOT NULL,
  banco integer NOT NULL,
  banco_sucursal integer NOT NULL,
  cruzado boolean NOT NULL DEFAULT false,
  observacion character varying(300),
  estado integer NOT NULL,
  fecha_creacion timestamp with time zone NOT NULL DEFAULT now(),
  fecha_cobro date,
  usuario integer NOT NULL,
  endosatario character varying(200),
  fecha_endoso date,
  librado integer NOT NULL,
  importe numeric(9,2) NOT NULL,
  proveedor integer NOT NULL,
  version_cheque bigint,
  bound integer NOT NULL, -- 1=factura_compra, 3 =remesa
  bound_id integer,
  CONSTRAINT cheque_propio_pkey PRIMARY KEY (id),
  CONSTRAINT cheque_propio_banco_fkey FOREIGN KEY (banco)
      REFERENCES banco (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_propio_banco_sucursal_fkey FOREIGN KEY (banco_sucursal)
      REFERENCES banco_sucursal (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_propio_librado_fkey FOREIGN KEY (librado)
      REFERENCES librado (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_propio_proveedor_fkey FOREIGN KEY (proveedor)
      REFERENCES proveedor (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_propio_usuario_fkey FOREIGN KEY (usuario)
      REFERENCES usuario (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_propio_numero_key UNIQUE (numero)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE cheque_propio OWNER TO postgres;
COMMENT ON COLUMN cheque_propio.bound IS '1=factura_compra, 3 =remesa';

CREATE TABLE cheque_terceros
(
  id serial NOT NULL,
  numero numeric(20,0) NOT NULL,
  fecha_cheque date NOT NULL,
  banco integer NOT NULL,
  banco_sucursal integer NOT NULL,
  cruzado boolean NOT NULL DEFAULT false,
  observacion character varying(300),
  estado integer NOT NULL,
  fecha_creacion timestamp with time zone NOT NULL DEFAULT now(),
  fecha_cobro date,
  usuario integer NOT NULL,
  endosatario character varying(200),
  fecha_endoso date,
  librado integer NOT NULL,
  importe numeric(9,2) NOT NULL,
  cliente integer NOT NULL,
  version_cheque bigint,
  bound integer NOT NULL, -- 2=factura_venta, 4=recibo
  bound_id integer,
  CONSTRAINT cheque_terceros_pkey PRIMARY KEY (id),
  CONSTRAINT cheque_terceros_banco_fkey FOREIGN KEY (banco)
      REFERENCES banco (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_banco_sucursal_fkey FOREIGN KEY (banco_sucursal)
      REFERENCES banco_sucursal (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_cliente_fkey FOREIGN KEY (cliente)
      REFERENCES cliente (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_librado_fkey FOREIGN KEY (librado)
      REFERENCES librado (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_usuario_fkey FOREIGN KEY (usuario)
      REFERENCES usuario (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cheque_terceros_cliente_key UNIQUE (cliente, numero)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE cheque_terceros OWNER TO postgres;
COMMENT ON COLUMN cheque_terceros.bound IS '2=factura_venta, 4=recibo';

2011/08/23
//DB
    ALTER TABLE detalle_acreditacion ADD COLUMN anulado boolean NOT NULL DEFAULT false;
2011/08/08
Se agregó facturación electrónica
//DB
    CREATE TABLE documento_tipo
    (
      id serial NOT NULL,
      nombre character varying(50),
      CONSTRAINT documento_tipo_pkey PRIMARY KEY (id),
      CONSTRAINT unq_documento_tipo_0 UNIQUE (nombre)
    )
    WITH (
      OIDS=FALSE
    );
    ALTER TABLE documento_tipo OWNER TO postgres;

    CREATE TABLE factura_electronica
    (
      id serial NOT NULL,
      fecha_proceso character varying(14) NOT NULL,
      resultado character varying(1) NOT NULL,
      concepto integer NOT NULL,
      cbte_tipo integer NOT NULL, -- según AFIP: Facturas: 1= "A", 6= "B", 11= "C"
      cbte_numero integer NOT NULL,
      cae character varying(14),
      cae_fecha_vto date,
      observaciones text, -- eventos relacionados a la obtención del CAE
      fecha_serv_desde date,
      fecha_serv_hasta date,
      CONSTRAINT factura_electronica_pkey PRIMARY KEY (id),
      CONSTRAINT factura_electronica_cbte_numero_key UNIQUE (cbte_numero),
      CONSTRAINT unq_factura_electronica_0 UNIQUE (cbte_numero)
    )
    WITH (
      OIDS=FALSE
    );
    ALTER TABLE factura_electronica OWNER TO postgres;
    COMMENT ON TABLE factura_electronica IS 'Datos relacionados a la facturación electrónica de la AFIP.';
    COMMENT ON COLUMN factura_electronica.cbte_tipo IS 'según AFIP: Facturas: 1= "A", 6= "B", 11= "C"';
    COMMENT ON COLUMN factura_electronica.observaciones IS 'eventos relacionados a la obtención del CAE';

2011/04/27
//DB
    CREATE TABLE movimiento_concepto (
      id serial NOT NULL, 
      nombre character varying(30) NOT NULL, 
      PRIMARY KEY (id), 
      UNIQUE (nombre)
     ) WITH ( OIDS = FALSE );
      INSERT INTO movimiento_concepto VALUES (1,'efectivo');
      ALTER TABLE detalle_caja_movimientos ADD COLUMN movimiento_concepto INTEGER NOT NULL DEFAULT 1;
      ALTER TABLE detalle_caja_movimientos ADD FOREIGN KEY (movimiento_concepto) REFERENCES movimiento_concepto (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
2011/04/18
//DB
    CREATE TABLE detalle_acreditacion
    ALTER COLUMNS TYPE double precision to NUMERIC(12,3);
      ALTER TABLE caja_movimientos ALTER monto_apertura TYPE numeric(12,3);
      ALTER TABLE caja_movimientos ALTER monto_cierre TYPE numeric(12,3);
      ALTER TABLE ctacte_cliente ALTER entregado TYPE numeric(12,3);
      ALTER TABLE ctacte_cliente ALTER importe TYPE numeric(12,3);
      ALTER TABLE ctacte_proveedor ALTER entregado TYPE numeric(12,3);
      ALTER TABLE ctacte_proveedor ALTER importe TYPE numeric(12,3);
      ALTER TABLE detalle_acreditacion ALTER monto TYPE numeric(12,3);
      ALTER TABLE detalle_caja_movimientos ALTER monto TYPE numeric(12,3);
      ALTER TABLE detalle_compra ALTER precio_unitario TYPE numeric(12,3);
      ALTER TABLE detalle_lista_precios ALTER margen TYPE numeric(12,3);
      ALTER TABLE detalle_nota_credito ALTER precio_unitario TYPE numeric(12,3);
      ALTER TABLE detalle_presupuesto ALTER descuento TYPE numeric(12,3);
      ALTER TABLE detalle_presupuesto ALTER precio_unitario TYPE numeric(12,3);
      ALTER TABLE detalle_recibo ALTER monto_entrega TYPE numeric(12,3);
      ALTER TABLE detalle_remesa ALTER monto_entrega TYPE numeric(12,3);
      ALTER TABLE detalle_venta ALTER descuento TYPE numeric(12,3);
      ALTER TABLE detalle_venta ALTER precio_unitario TYPE numeric(12,3);
      ALTER TABLE factura_compra ALTER perc_iva TYPE numeric(12,3);
      ALTER TABLE factura_compra ALTER perc_dgr TYPE numeric(12,3);
      ALTER TABLE factura_compra ALTER importe TYPE numeric(12,3);
      ALTER TABLE factura_compra ALTER iva10 TYPE numeric(12,3);
      ALTER TABLE factura_compra ALTER iva21 TYPE numeric(12,3);
      ALTER TABLE factura_venta ALTER importe TYPE numeric(12,3);
      ALTER TABLE factura_venta ALTER descuento TYPE numeric(12,3);
      ALTER TABLE factura_venta ALTER iva10 TYPE numeric(12,3);
      ALTER TABLE factura_venta ALTER iva21 TYPE numeric(12,3);
      ALTER TABLE factura_venta ALTER gravado TYPE numeric(12,3);
      ALTER TABLE nota_credito ALTER importe TYPE numeric(12,3);
      ALTER TABLE nota_credito ALTER gravado TYPE numeric(12,3);
      ALTER TABLE nota_credito ALTER iva10 TYPE numeric(12,3);
      ALTER TABLE nota_credito ALTER iva21 TYPE numeric(12,3);
      ALTER TABLE nota_credito ALTER desacreditado TYPE numeric(12,3);
      ALTER TABLE presupuesto ALTER importe TYPE numeric(12,3);
      ALTER TABLE presupuesto ALTER descuento TYPE numeric(12,3);
      ALTER TABLE presupuesto ALTER iva10 TYPE numeric(12,3);
      ALTER TABLE presupuesto ALTER iva21 TYPE numeric(12,3);
      ALTER TABLE producto ALTER costo_compra TYPE numeric(12,3);
      ALTER TABLE producto ALTER precio_venta TYPE numeric(12,3);
      ALTER TABLE productos_web ALTER precio TYPE numeric(12,3);
      ALTER TABLE recibo ALTER monto TYPE numeric(12,3);
      ALTER TABLE remesa ALTER monto_entrega TYPE numeric(12,3);
2011/04/12
//DB
    ALTER TABLE remito ADD COLUMN fecha_remito date NOT NULL DEFAULT now();
    ALTER TABLE remito ALTER COLUMN fecha_remito DROP DEFAULT;
2011/04/05
//Added new feat!! "Nota de crédito"
//new Entity NotaCredito
//GUI ABM NotaCredito
//DB
    CREATE TABLE nota_credito
    CREATE TABLE detalle_nota_credito
//Report JGestion_NotaCredito


2011/02/17
//Edit GUI:
// Agregaron permisos correspondientes a ProdudosWeb y Oferas.
//DB
    ALTER TABLE permisos ADD COLUMN abm_catalogoweb boolean NOT NULL DEFAULT false;
    ALTER TABLE permisos ADD COLUMN abm_ofertasweb boolean NOT NULL DEFAULT false;
2011/02/14
//Edit GUI:
   - JDFacturaVenta // Detección de Productos en oferta
//DB
    ALTER TABLE detalle_venta ADD COLUMN oferta integer;
    ALTER TABLE detalle_venta ADD FOREIGN KEY (oferta) REFERENCES historial_ofertas (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
2011/02/11
//DB Modif:
    CREATE TABLE historial_ofertas
2011/02/10
//Integrando con la Web
// Added GUI:
   - Ventana de asignación de productos al catálogo.
   - Ventana de creación de ofertas/destacados.
//Modif:
//   Class ListaPrecios, se le agregó el atributo paraCatalogoWeb.
    ALTER TABLE lista_precios ADD COLUMN para_catalogo_web BOOLEAN NOT NULL DEFAULT FALSE
2010/10/??
//DB
//verdura para carrito web
    CREATE ROLE jgestion_web LOGIN
    NOSUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE CONNECTION LIMIT 10;
    CREATE TABLE productos_web (
      producto integer NOT NULL,
      precio numeric(12,2) NOT NULL,
      destacado boolean NOT NULL DEFAULT false,
      oferta boolean NOT NULL DEFAULT false,
      inicio_oferta date,
      fin_oferta date,
      estado smallint NOT NULL DEFAULT 1, -- 1 =nuevo, 2= modificado, 3= eliminado
      chequeado smallint NOT NULL DEFAULT 0, -- por la web page..
      id integer NOT NULL,
      CONSTRAINT productos_web_pkey PRIMARY KEY (id),
      CONSTRAINT fk_productos_web_producto FOREIGN KEY (producto)
          REFERENCES producto (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT productos_web_producto_fkey FOREIGN KEY (producto)
          REFERENCES producto (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT productos_web_producto_key UNIQUE (producto),
      CONSTRAINT unq_productos_web_0 UNIQUE (producto)
    )
    WITH (
      OIDS=FALSE
    );
    ALTER TABLE productos_web OWNER TO postgres;
    GRANT ALL ON TABLE productos_web TO postgres;
    GRANT SELECT, UPDATE ON TABLE productos_web TO jgestion_web;
    COMMENT ON COLUMN productos_web.estado IS '1 =nuevo, 2= modificado, 3= eliminado';
    COMMENT ON COLUMN productos_web.chequeado IS 'por la web page..';
2010/09/27
//Added GUI:
//   - Impl de los AutoCompleteComboBox (Facturas venta, compra y no se donde mas)
//   - Impl el Thread de checkeo de conexión con la DB (gui.JFP)
2010/09/13
//Added GUI:
       Menu > Productos > Movimientos
//Added Reporte: MovimientosProductos
2010/09/07
//implementación de anulación de FacturaCompra
ALTER TABLE detalle_remesa ADD COLUMN anulado boolean NOT NULL DEFAULT false;
2010/09/09
//para que esté == a detalles_compra
ALTER TABLE detalles_compra RENAME factura_compra  TO factura;
2010/09/07
//implementación de anulación de FacturaVenta
ALTER TABLE detalle_recibo ADD COLUMN anulado boolean NOT NULL DEFAULT false;
2010/07/13
//para relacionar una FacturaVenta con un Remito (OneToOne)
ALTER TABLE factura_venta ADD COLUMN remito integer;
ALTER TABLE factura_venta ADD FOREIGN KEY (remito) REFERENCES remito (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE remito ADD COLUMN factura_venta integer;
ALTER TABLE remito ADD FOREIGN KEY (factura_venta) REFERENCES factura_venta (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
2010/07/08
 reporte Movimientos varios
 reporte Movimientos entre Cajas
2010/07/07
 modulo Presupuestos
 reporte Presupuestos
26-05
 ALTER TABLE datos_empresa ADD COLUMN fecha_inicio_actividad date;
-! impresión de listados VARIOS
-> resumenes de Cta cte (cliente y proveedor)
13-05
-> reporte Recibo A B C
11-05
-> Buscador y Reporte de Cierres de Caja (parcial y definitivo)

-> MovimientosVarios habilitado
add a new caja_movimiento.tipo = 8 (MovimientosVarios)
07-05