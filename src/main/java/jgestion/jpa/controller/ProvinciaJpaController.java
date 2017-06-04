package jgestion.jpa.controller;

import java.util.List;
import jgestion.entity.Provincia;
import jgestion.jpa.controller.JGestionJpaImpl;

/**
 *
 * @author FiruzzZ
 */
public class ProvinciaJpaController extends JGestionJpaImpl<Provincia, Integer> {

    public ProvinciaJpaController() {
    }

    @Override
    public List<Provincia> findAll() {
        return super.findAll(getSelectFrom() + " ORDER BY o.nombre");
    }
    
    

}
