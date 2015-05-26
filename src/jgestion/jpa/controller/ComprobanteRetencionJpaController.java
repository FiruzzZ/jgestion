package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.ComprobanteRetencion;
import jgestion.entity.ComprobanteRetencion_;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public final class ComprobanteRetencionJpaController extends AbstractDAO<ComprobanteRetencion, Integer> {

    private EntityManager entityManager;

    public ComprobanteRetencionJpaController() {
        getEntityManager();
    }

    
    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public ComprobanteRetencion findByNumero(Long numero) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ComprobanteRetencion> query = cb.createQuery(getEntityClass());
        Root<ComprobanteRetencion> root = query.from(getEntityClass());
        query.where(cb.equal(root.get(ComprobanteRetencion_.numero), numero));
        try {
            return getEntityManager().createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
