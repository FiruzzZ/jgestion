package jgestion.jpa.controller;

import jgestion.entity.Vendedor;
import jgestion.entity.Vendedor_;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class VendedorJpaController extends JGestionJpaImpl<Vendedor, Integer> {

    public VendedorJpaController() {
    }


    @Override
    public List<Vendedor> findAll() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Vendedor> cq = cb.createQuery(getEntityClass());
            Root<Vendedor> root = cq.from(getEntityClass());
            cq.select(root);
            cq.orderBy(cb.asc(root.get(Vendedor_.apellido)));
            return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
        } finally {
            closeEntityManager();
        }
    }

    public List<Vendedor> findActivos() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Vendedor> cq = cb.createQuery(getEntityClass());
            Root<Vendedor> root = cq.from(getEntityClass());
            cq.select(root)
                    .where(cb.equal(root.get(Vendedor_.activo), Boolean.TRUE))
                    .orderBy(cb.asc(root.get(Vendedor_.apellido)));
            return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
        } finally {
            closeEntityManager();
        }
    }
}
