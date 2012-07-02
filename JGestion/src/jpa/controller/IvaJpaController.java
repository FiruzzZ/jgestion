package jpa.controller;

import controller.DAO;
import entity.Iva;
import entity.Producto;
import entity.Producto_;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class IvaJpaController extends AbstractDAO<Iva, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Iva findByProducto(Integer productoID) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Iva> query = cb.createQuery(Iva.class);
        Root<Producto> from = query.from(Producto.class);
        query.select(from.get(Producto_.iva)).where(cb.equal(from.get(Producto_.id), productoID));
        return getEntityManager().createQuery(query).getSingleResult();
    }
}
