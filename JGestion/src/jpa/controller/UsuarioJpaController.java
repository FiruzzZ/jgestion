package jpa.controller;

import controller.DAO;
import entity.ChequeTerceros;
import entity.ChequeTerceros_;
import entity.Usuario;
import entity.Usuario_;
import entity.enums.ChequeEstado;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioJpaController extends AbstractDAO<Usuario, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public List<Usuario> findByEstado(int estado) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(getEntityClass());
        cq.where(cb.equal(cq.from(getEntityClass()).get(Usuario_.estado), estado));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<Usuario> findWithCheques() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(getEntityClass());
        Root<ChequeTerceros> from = cq.from(ChequeTerceros.class);
        cq.select(from.get(ChequeTerceros_.usuario));
        cq.where(cb.equal(from.get(ChequeTerceros_.estado), ChequeEstado.CARTERA.getId()));
        cq.groupBy(from.get(ChequeTerceros_.usuario));
        return getEntityManager().createQuery(cq).getResultList();
    }
}
