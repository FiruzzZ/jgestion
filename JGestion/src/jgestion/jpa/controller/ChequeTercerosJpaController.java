package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.Banco;
import jgestion.entity.ChequeTerceros;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class ChequeTercerosJpaController extends AbstractDAO<ChequeTerceros, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    /**
     * 
     * @param banco
     * @param numero
     * @return an {@link ChequeTerceros} instance or {@code null} if there is no result.
     */
    public ChequeTerceros findBy(Banco banco, Long numero) {
        try {
            return (ChequeTerceros) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o WHERE o.banco.id=" + banco.getId() + " AND o.numero=" + numero).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
