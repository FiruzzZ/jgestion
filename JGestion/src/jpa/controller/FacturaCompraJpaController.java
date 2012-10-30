package jpa.controller;

import controller.DAO;
import controller.DetalleCompraJpaController;
import entity.DetalleCompra;
import entity.FacturaCompra;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;

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
        } finally {
            if (em != null) {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }
}
