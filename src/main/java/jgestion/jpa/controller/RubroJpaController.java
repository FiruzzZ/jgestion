package jgestion.jpa.controller;

import java.util.HashMap;
import java.util.List;
import jgestion.entity.Rubro;
import jgestion.entity.Rubro_;

/**
 *
 * @author FiruzzZ
 */
public class RubroJpaController extends JGestionJpaImpl<Rubro, Integer> {

    @Override
    public List<Rubro> findAll() {
        return findAll(getOrder(Rubro_.nombre, true));
    }

    public Rubro findByNombre(String nombre) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("nombre", nombre);
        return findByQuery(getSelectFrom() + " where upper(o.nombre)=upper(:nombre)", p);
    }

}
