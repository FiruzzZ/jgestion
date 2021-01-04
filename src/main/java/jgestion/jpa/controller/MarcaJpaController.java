package jgestion.jpa.controller;

import java.util.HashMap;
import jgestion.entity.Marca;

/**
 *
 * @author FiruzzZ
 */
public class MarcaJpaController extends JGestionJpaImpl<Marca, Integer> {

    /**
     *
     * @param nombre CI
     * @return
     */
    public Marca findByNombre(String nombre) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("nombre", nombre);
        return findByQuery(getSelectFrom() + " WHERE upper(o.nombre) = upper(:nombre)", p);
    }
}
