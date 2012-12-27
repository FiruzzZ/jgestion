package jpa.controller;

import controller.DAO;
import controller.DetalleCompraJpaController;
import entity.DetalleCompra;
import entity.FacturaCompra;
import entity.FacturaCompra_;
import entity.Sucursal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class FacturaCompraJpaController extends AbstractDAO<FacturaCompra, Integer> {

    private static final Logger LOG = Logger.getLogger(FacturaCompraJpaController.class.getName());
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public void create(FacturaCompra o) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<DetalleCompra> detallesCompraListToPersist = o.getDetalleCompraList();
            o.setDetalleCompraList(new ArrayList<DetalleCompra>());
            em.persist(o);
            em.getTransaction().commit();
            DetalleCompraJpaController dcController = new DetalleCompraJpaController();
            for (DetalleCompra detallesCompra : detallesCompraListToPersist) {
                detallesCompra.setFactura(o);
                dcController.create(detallesCompra);
            }
        } catch (Exception ex) {
            LOG.fatal(ex, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalArgumentException(ex.getMessage());
        } finally {
            if (em != null) {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    public long getMaxNumeroComprobante(Sucursal sucursal, Character tipo) {
        getEntityManager();
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<FacturaCompra> from = cq.from(getEntityClass());
            cq.where(cb.equal(from.get(FacturaCompra_.tipo), tipo),
                    cb.equal(from.get(FacturaCompra_.sucursal), sucursal));
            cq.select(cb.max(from.get(FacturaCompra_.numero)));
            Long max = getEntityManager().createQuery(cq).getSingleResult();
            if(max == null) {
                return 0;
            } else {
                String s = UTIL.AGREGAR_CEROS(max, 12);
                return Long.valueOf(s.substring(4));
            }
        } finally {
            getEntityManager().close();
        }
    }
}
