package jgestion.jpa.controller;

import jgestion.entity.Producto;
import jgestion.entity.Stock;
import jgestion.entity.Sucursal;

/**
 *
 * @author FiruzzZ
 */
public class StockJpaController extends JGestionJpaImpl<Stock, Integer> {

    public StockJpaController() {
    }

    public Stock findBy(Producto producto, Sucursal sucursal) {
        return findByQuery(getSelectFrom()
                + " WHERE o.producto.id=" + producto.getId()
                + " AND o.sucursal.id=" + sucursal.getId());
    }
}
