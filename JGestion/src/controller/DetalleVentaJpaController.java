package controller;

import controller.exceptions.NonexistentEntityException;
import entity.DetalleVenta;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.FacturaVenta;

/**
 *
 * @author Administrador
 */
public class DetalleVentaJpaController {

   public static final String CLASS_NAME = DetalleVenta.class.getSimpleName();

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   public void create(DetalleVenta detallesVenta) {
      EntityManager em = null;
      try {
         em = DAO.getEntityManager();
         em.getTransaction().begin();
         FacturaVenta factura = detallesVenta.getFactura();
         if (factura != null) {
            factura = em.merge(factura);
            detallesVenta.setFactura(factura);
         }
         em.persist(detallesVenta);
         if (factura != null) {
            factura.getDetallesVentaList().add(detallesVenta);
            factura = em.merge(factura);
         }
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

//   public void edit(DetalleVenta detallesVenta) throws NonexistentEntityException, Exception {
//      EntityManager em = null;
//      try {
//         em = DAO.getEntityManager();
//         em.getTransaction().begin();
//         DetalleVenta persistentDetallesVenta = em.find(DetalleVenta.class, detallesVenta.getId());
//         FacturaVenta facturaOld = persistentDetallesVenta.getFactura();
//         FacturaVenta facturaNew = detallesVenta.getFactura();
//         if (facturaNew != null) {
//            facturaNew = em.merge(facturaNew);
//            detallesVenta.setFactura(facturaNew);
//         }
//         detallesVenta = em.merge(detallesVenta);
//         if (facturaOld != null && !facturaOld.equals(facturaNew)) {
//            facturaOld.getDetallesVentaList().remove(detallesVenta);
//            facturaOld = em.merge(facturaOld);
//         }
//         if (facturaNew != null && !facturaNew.equals(facturaOld)) {
//            facturaNew.getDetallesVentaList().add(detallesVenta);
//            facturaNew = em.merge(facturaNew);
//         }
//         em.getTransaction().commit();
//      } catch (Exception ex) {
//         String msg = ex.getLocalizedMessage();
//         if (msg == null || msg.length() == 0) {
//            Integer id = detallesVenta.getId();
//            if (findDetallesVenta(id) == null) {
//               throw new NonexistentEntityException("The detalleVenta with id " + id + " no longer exists.");
//            }
//         }
//         throw ex;
//      } finally {
//         if (em != null) {
//            em.close();
//         }
//      }
//   }

//   public void destroy(Integer id) throws NonexistentEntityException {
//      EntityManager em = null;
//      try {
//         em = DAO.getEntityManager();
//         em.getTransaction().begin();
//         DetalleVenta detallesVenta;
//         try {
//            detallesVenta = em.getReference(DetalleVenta.class, id);
//            detallesVenta.getId();
//         } catch (EntityNotFoundException enfe) {
//            throw new NonexistentEntityException("The detalleVenta with id " + id + " no longer exists.", enfe);
//         }
//         FacturaVenta factura = detallesVenta.getFactura();
//         if (factura != null) {
//            factura.getDetallesVentaList().remove(detallesVenta);
//            factura = em.merge(factura);
//         }
//         em.remove(detallesVenta);
//         em.getTransaction().commit();
//      } finally {
//         if (em != null) {
//            em.close();
//         }
//      }
//   }

   public List<DetalleVenta> findDetallesVentaEntities() {
      return findDetallesVentaEntities(true, -1, -1);
   }

   public List<DetalleVenta> findDetallesVentaEntities(int maxResults, int firstResult) {
      return findDetallesVentaEntities(false, maxResults, firstResult);
   }

   private List<DetalleVenta> findDetallesVentaEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = DAO.getEntityManager();
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

   public DetalleVenta findDetallesVenta(Integer id) {
      EntityManager em = DAO.getEntityManager();
      try {
         return em.find(DetalleVenta.class, id);
      } finally {
         em.close();
      }
   }

   public int getDetallesVentaCount() {
      EntityManager em = DAO.getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from " + CLASS_NAME + " as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   List<DetalleVenta> findByFactura(int facturaVentaID) {
      return DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.factura.id = " + facturaVentaID).getResultList();
   }
}
