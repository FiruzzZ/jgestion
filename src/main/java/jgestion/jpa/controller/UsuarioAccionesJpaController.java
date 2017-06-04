package jgestion.jpa.controller;

import jgestion.entity.UsuarioAcciones;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.UsuarioAcciones_;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesJpaController extends JGestionJpaImpl<UsuarioAcciones, Integer> {

    public UsuarioAccionesJpaController() {
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
