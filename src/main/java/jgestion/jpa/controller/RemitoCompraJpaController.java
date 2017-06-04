package jgestion.jpa.controller;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.Proveedor;
import jgestion.entity.RemitoCompra;
import jgestion.entity.RemitoCompra_;

/**
 *
 * @author FiruzzZ
 */
public class RemitoCompraJpaController extends JGestionJpaImpl<RemitoCompra, Integer> {

    public RemitoCompraJpaController() {
    }

    public RemitoCompra findBy(Proveedor proveedor, Long numero) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RemitoCompra> cq = cb.createQuery(getEntityClass());
        Root<RemitoCompra> from = cq.from(getEntityClass());
        cq.where(
                cb.equal(from.get(RemitoCompra_.proveedor), proveedor),
                cb.equal(from.get(RemitoCompra_.numero), numero)
        );
        try {
            return getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
