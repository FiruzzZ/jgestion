package jgestion.jpa.controller;

import java.util.HashMap;
import jgestion.entity.ListaPrecios;

/**
 *
 * @author FiruzzZ
 */
public class ListaPreciosJpaController extends JGestionJpaImpl<ListaPrecios, Integer> {

    /**
     *
     * @param nombre CI
     * @return
     */
    public ListaPrecios findByNombre(String nombre) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("nombre", nombre);
        return findByQuery(getSelectFrom() + " WHERE upper(o.nombre) = upper(:nombre)", p);
    }

    public ListaPrecios findParaCatalogoWeb() {
        return findByQuery(getSelectFrom() + " WHERE o.paraCatalogoWeb = TRUE");
    }
}
