package jgestion.jpa.controller;

import jgestion.entity.Dominio;
import jgestion.entity.Dominio_;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class DominioJpaController extends JGestionJpaImpl<Dominio, Integer> {

    public DominioJpaController() {
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
