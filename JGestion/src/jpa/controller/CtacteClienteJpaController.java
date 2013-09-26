package jpa.controller;

import controller.DAO;
import entity.CtacteCliente;
import entity.CtacteCliente_;
import entity.FacturaVenta;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class CtacteClienteJpaController extends AbstractDAO<CtacteCliente, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public CtacteCliente findByNotaDebito(Integer id) {
        try {
            return (CtacteCliente) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
                    + " where o.notaDebito.id = " + id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public CtacteCliente findByFactura(Integer id) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CtacteCliente> query = cb.createQuery(CtacteCliente.class);
        Root<CtacteCliente> from = query.from(CtacteCliente.class);
        FacturaVenta facturaVenta = new FacturaVenta();
        facturaVenta.setId(id);
        query.select(from).where(cb.equal(from.get(CtacteCliente_.factura), facturaVenta));
        try {
            return getEntityManager().createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
// the easy way....        
//        try {
//            return (CtacteCliente) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
//                    + " where o.factura.id = " + id).getSingleResult();
//        } catch (NoResultException e) {
//            return null;
//        }
    }

}
