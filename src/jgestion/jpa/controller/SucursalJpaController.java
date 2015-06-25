package jgestion.jpa.controller;

import jgestion.entity.Sucursal;
import jgestion.entity.Sucursal_;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Administrador
 */
public class SucursalJpaController extends JGestionJpaImpl<Sucursal, Integer> {

    public SucursalJpaController() {
    }

    /**
     *
     * @return ordered by {@link Sucursal#nombre}.
     */
    @Override
    public List<Sucursal> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Sucursal> cq = cb.createQuery(getEntityClass());
        Root<Sucursal> from = cq.from(getEntityClass());
        cq.select(from);
        cq.orderBy(cb.asc(from.get(Sucursal_.nombre)));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
    }

    public Sucursal findByPuntoVenta(int puntoVenta) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Sucursal> cq = cb.createQuery(getEntityClass());
        Root<Sucursal> from = cq.from(getEntityClass());
        cq.select(from);
        cq.where(cb.equal(from.get(Sucursal_.puntoVenta), puntoVenta));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getSingleResult();
    }
}
