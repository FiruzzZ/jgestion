package jgestion.jpa.controller;

import jgestion.entity.ComprobanteRetencion;
import jgestion.entity.ComprobanteRetencion_;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public final class ComprobanteRetencionJpaController extends JGestionJpaImpl<ComprobanteRetencion, Integer> {

    public ComprobanteRetencionJpaController() {
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
