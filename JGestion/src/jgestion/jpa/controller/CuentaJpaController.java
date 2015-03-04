package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.Cuenta;
import jgestion.entity.Cuenta_;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class CuentaJpaController extends AbstractDAO<Cuenta, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public List<Cuenta> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Cuenta> cq = cb.createQuery(getEntityClass());
        Root<Cuenta> root = cq.from(getEntityClass());
        cq.select(root);
        cq.orderBy(cb.asc(root.get(Cuenta_.nombre)));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
    }

    public List<Cuenta> findByTipo(boolean ingreso) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Cuenta> cq = cb.createQuery(getEntityClass());
        Root<Cuenta> root = cq.from(getEntityClass());
        cq.select(root);
        cq.where(cb.equal(root.get(Cuenta_.ingreso), ingreso));
        cq.orderBy(cb.asc(root.get(Cuenta_.nombre)));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
    }
}
