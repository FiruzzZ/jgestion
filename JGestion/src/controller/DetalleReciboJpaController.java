
package controller;

import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.DetalleRecibo;
import entity.FacturaVenta;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Recibo;

/**
 *
 * @author Administrador
 */
public class DetalleReciboJpaController {

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(DetalleRecibo detalleRecibo) throws PreexistingEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Recibo recibo = detalleRecibo.getRecibo();
         if (recibo != null) {
            recibo = em.getReference(recibo.getClass(), recibo.getId());
            detalleRecibo.setRecibo(recibo);
         }
         em.persist(detalleRecibo);
         if (recibo != null) {
            recibo.getDetalleReciboList().add(detalleRecibo);
            recibo = em.merge(recibo);
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (findDetalleRecibo(detalleRecibo.getId()) != null) {
            throw new PreexistingEntityException("DetalleRecibo " + detalleRecibo + " already exists.", ex);
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(DetalleRecibo detalleRecibo) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         DetalleRecibo persistentDetalleRecibo = em.find(DetalleRecibo.class, detalleRecibo.getId());
         Recibo reciboOld = persistentDetalleRecibo.getRecibo();
         Recibo reciboNew = detalleRecibo.getRecibo();
         if (reciboNew != null) {
            reciboNew = em.getReference(reciboNew.getClass(), reciboNew.getId());
            detalleRecibo.setRecibo(reciboNew);
         }
         detalleRecibo = em.merge(detalleRecibo);
         if (reciboOld != null && !reciboOld.equals(reciboNew)) {
            reciboOld.getDetalleReciboList().remove(detalleRecibo);
            reciboOld = em.merge(reciboOld);
         }
         if (reciboNew != null && !reciboNew.equals(reciboOld)) {
            reciboNew.getDetalleReciboList().add(detalleRecibo);
            reciboNew = em.merge(reciboNew);
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = detalleRecibo.getId();
            if (findDetalleRecibo(id) == null) {
               throw new NonexistentEntityException("The detalleRecibo with id " + id + " no longer exists.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void destroy(Integer id) throws NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         DetalleRecibo detalleRecibo;
         try {
            detalleRecibo = em.getReference(DetalleRecibo.class, id);
            detalleRecibo.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The detalleRecibo with id " + id + " no longer exists.", enfe);
         }
         Recibo recibo = detalleRecibo.getRecibo();
         if (recibo != null) {
            recibo.getDetalleReciboList().remove(detalleRecibo);
            recibo = em.merge(recibo);
         }
         em.remove(detalleRecibo);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<DetalleRecibo> findDetalleReciboEntities() {
      return findDetalleReciboEntities(true, -1, -1);
   }

   public List<DetalleRecibo> findDetalleReciboEntities(int maxResults, int firstResult) {
      return findDetalleReciboEntities(false, maxResults, firstResult);
   }

   private List<DetalleRecibo> findDetalleReciboEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from DetalleRecibo as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public DetalleRecibo findDetalleRecibo(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(DetalleRecibo.class, id);
      } finally {
         em.close();
      }
   }

   public int getDetalleReciboCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from DetalleRecibo as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   List<DetalleRecibo> findDetalleReciboEntitiesByFactura(FacturaVenta factura) {
      return getEntityManager().createNamedQuery("DetalleRecibo.findByFacturaVenta")
              .setParameter("facturaVenta", factura)
              .getResultList();
   }

}
