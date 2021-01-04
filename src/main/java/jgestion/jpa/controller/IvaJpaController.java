package jgestion.jpa.controller;

import java.util.HashMap;
import jgestion.entity.Iva;
import jgestion.entity.Producto;
import jgestion.entity.Producto_;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class IvaJpaController extends JGestionJpaImpl<Iva, Integer> {

    public IvaJpaController() {
    }

    public Iva findByProducto(Integer productoID) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Iva> query = cb.createQuery(Iva.class);
        Root<Producto> from = query.from(Producto.class);
        query.select(from.get(Producto_.iva)).where(cb.equal(from.get(Producto_.id), productoID));
        return getEntityManager().createQuery(query).getSingleResult();
    }

    public Iva findByValue(float value) {
        HashMap<String, Object> p = new HashMap<>(1);
        p.put("val", value);
        return findByQuery(getSelectFrom() + " where o.iva =:val", p);
    }
}
