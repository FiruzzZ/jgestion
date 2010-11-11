package controller;

import controller.exceptions.NonexistentEntityException;
import entity.DetalleCompra;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.FacturaCompra;

/**
 *
 * @author FiruzzZ
 */
public class DetalleCompraJpaController {

   public static final String CLASS_NAME = DetalleCompra.class.getSimpleName();

   // <editor-fold defaultstate="collapsed" desc="CRUD, List's">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(DetalleCompra detallesCompra) throws Exception {
      DAO.create(detallesCompra);
   }

   public void edit(DetalleCompra detallesCompra) throws NonexistentEntityException, Exception {
      throw new UnsupportedOperationException();
   }

   public void destroy(Integer id) throws NonexistentEntityException {
      throw new UnsupportedOperationException();
   }

   public List<DetalleCompra> findDetallesCompraEntities() {
      return findDetallesCompraEntities(true, -1, -1);
   }

   public List<DetalleCompra> findDetallesCompraEntities(int maxResults, int firstResult) {
      return findDetallesCompraEntities(false, maxResults, firstResult);
   }

   private List<DetalleCompra> findDetallesCompraEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from " + CLASS_NAME + " as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public DetalleCompra findDetallesCompra(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(DetalleCompra.class, id);
      } finally {
         em.close();
      }
   }

   public int getDetallesCompraCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from "+CLASS_NAME+" as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   List<DetalleCompra> findByFactura(FacturaCompra factura) {
      return DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.factura.id=" + factura.getId()).getResultList();
   }
}
