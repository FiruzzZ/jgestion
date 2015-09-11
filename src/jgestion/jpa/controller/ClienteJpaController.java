package jgestion.jpa.controller;

import jgestion.entity.Cliente;
import jgestion.entity.Cliente_;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class ClienteJpaController extends JGestionJpaImpl<Cliente, Integer> {

    /**
     *
     *
     * @return a list ordered by {@link Cliente#nombre}
     */
    @Override
    public List<Cliente> findAll() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Cliente> cq = cb.createQuery(getEntityClass());
            Root<Cliente> root = cq.from(getEntityClass());
            cq.select(root);
            cq.orderBy(cb.asc(root.get(Cliente_.nombre)));
            return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
        } finally {
            closeEntityManager();
        }
    }
}
