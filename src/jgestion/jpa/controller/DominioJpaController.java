package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.Dominio;
import jgestion.entity.Dominio_;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class DominioJpaController extends AbstractDAO<Dominio, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public List<Dominio> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Dominio> cq = cb.createQuery(getEntityClass());
        cq.select(cq.from(getEntityClass()));
        cq.orderBy(cb.desc(cq.from(getEntityClass()).get(Dominio_.nombre)));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
    }
}
