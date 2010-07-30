-- para cuando se inserta un presupuesto..
CREATE OR REPLACE FUNCTION update_fecha_y_hora_de_creacion_de_presupuesto()
  RETURNS trigger AS
$BODY$
begin
	UPDATE presupuesto
	SET fecha_creacion = now(), hora_creacion = now()
	WHERE id = OLD.id;
    return new;
end; $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
  
create trigger tri_update_fecha_y_hora_de_creacion_de_presupuesto
after insert on presupuesto
for each row execute procedure update_fecha_y_hora_de_creacion_de_presupuesto();