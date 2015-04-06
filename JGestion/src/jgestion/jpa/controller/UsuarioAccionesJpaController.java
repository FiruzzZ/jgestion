package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.UsuarioAcciones;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.UsuarioAcciones_;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesJpaController extends AbstractDAO<UsuarioAcciones, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public UsuarioAcciones findCreate(Object entidad, Integer entidadId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UsuarioAcciones> cq = cb.createQuery(getEntityClass());
        Root<UsuarioAcciones> from = cq.from(getEntityClass());
        cq.where(
                cb.equal(from.get(UsuarioAcciones_.entidad), entidad.getClass().getSimpleName()),
                cb.equal(from.get(UsuarioAcciones_.entidadId), entidadId),
                cb.equal(from.get(UsuarioAcciones_.accion), 'c')
        );
        try {
            return getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
