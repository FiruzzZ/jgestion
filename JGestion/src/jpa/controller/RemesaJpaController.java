package jpa.controller;

import controller.DAO;
import entity.*;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class RemesaJpaController extends AbstractDAO<Remesa, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Remesa findByNumero(long numero) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Remesa> cq = cb.createQuery(getEntityClass());
        Root<Remesa> from = cq.from(getEntityClass());
        cq.select(from).
                where(cb.equal(from.get(Remesa_.numero), numero));
        return getEntityManager().createQuery(cq).getSingleResult();
    }

    public List<Remesa> findByFactura(FacturaCompra facturaCompra) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Remesa> cq = cb.createQuery(getEntityClass());
        Root<DetalleRemesa> from = cq.from(DetalleRemesa.class);
        cq.select(from.get(DetalleRemesa_.remesa)).
                where(cb.equal(from.get(DetalleRemesa_.facturaCompra), facturaCompra));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<DetalleRemesa> findDetalleRemesaByFactura(FacturaCompra factura) {
        return getEntityManager().
                createQuery("SELECT o FROM DetalleRemesa o"
                + " WHERE o.facturaCompra = :facturaCompra", DetalleRemesa.class).
                setParameter("facturaCompra", factura).getResultList();
    }
}
