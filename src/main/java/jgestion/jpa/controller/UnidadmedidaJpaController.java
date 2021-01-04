package jgestion.jpa.controller;

import java.util.HashMap;
import java.util.List;
import jgestion.entity.Unidadmedida;
import jgestion.entity.Unidadmedida_;

/**
 *
 * @author FiruzzZ
 */
public class UnidadmedidaJpaController extends JGestionJpaImpl<Unidadmedida, Integer> {

    public Unidadmedida findUnitario() {
        return find(1);
    }

    @Override
    public List<Unidadmedida> findAll() {
        return super.findAll(getOrder(Unidadmedida_.nombre, true));
    }

    /**
     *
     * @param nombre CI
     * @return
     */
    public Unidadmedida findByNombre(String nombre) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("nombre", nombre);
        return findByQuery(getSelectFrom() + " WHERE upper(o.nombre) = upper(:nombre)", p);
    }
}
