package controller;

import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.MsjInformativos;
import gui.JDInfo;
import gui.JDMiniABM;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author Administrador
 */
public class MsjInformativosJpaController implements Runnable{

   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(MsjInformativos msjInformativos) throws PreexistingEntityException, Exception {
//      EntityManager em = null;
//      try {
//         em = getEntityManager();
//         em.getTransaction().begin();
//         em.persist(msjInformativos);
//         em.getTransaction().commit();
//      } catch (Exception ex) {
//         if (findMsjInformativos(msjInformativos.getId()) != null) {
//            throw new PreexistingEntityException("MsjInformativos " + msjInformativos + " already exists.", ex);
//         }
//         throw ex;
//      } finally {
//         if (em != null) {
//            em.close();
//         }
//      }
   }

   public void edit(MsjInformativos msjInformativos) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         msjInformativos = em.merge(msjInformativos);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = msjInformativos.getId();
            if (findMsjInformativos(id) == null) {
               throw new NonexistentEntityException("The msjInformativos with id " + id + " no longer exists.");
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
//      EntityManager em = null;
//      try {
//         em = getEntityManager();
//         em.getTransaction().begin();
//         MsjInformativos msjInformativos;
//         try {
//            msjInformativos = em.getReference(MsjInformativos.class, id);
//            msjInformativos.getId();
//         } catch (EntityNotFoundException enfe) {
//            throw new NonexistentEntityException("The msjInformativos with id " + id + " no longer exists.", enfe);
//         }
//         em.remove(msjInformativos);
//         em.getTransaction().commit();
//      } finally {
//         if (em != null) {
//            em.close();
//         }
//      }
   }

//   public List<MsjInformativos> findMsjInformativosEntities() {
//      return findMsjInformativosEntities(true, -1, -1);
//   }
//
//   public List<MsjInformativos> findMsjInformativosEntities(int maxResults, int firstResult) {
//      return findMsjInformativosEntities(false, maxResults, firstResult);
//   }
//
//   private List<MsjInformativos> findMsjInformativosEntities(boolean all, int maxResults, int firstResult) {
//      EntityManager em = getEntityManager();
//      try {
//         Query q = em.createQuery("select object(o) from MsjInformativos as o");
//         if (!all) {
//            q.setMaxResults(maxResults);
//            q.setFirstResult(firstResult);
//         }
//         return q.getResultList();
//      } finally {
//         em.close();
//      }
//   }

//   public int getMsjInformativosCount() {
//      EntityManager em = getEntityManager();
//      try {
//         Query q = em.createQuery("select count(o) from MsjInformativos as o");
//         return ((Long) q.getSingleResult()).intValue();
//      } finally {
//         em.close();
//      }
//   }
   
   public MsjInformativos findMsjInformativos(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(MsjInformativos.class, id);
      } finally {
         em.close();
      }
   }

   public void run() {   }

   void mostrarMensaje(javax.swing.JDialog papi, String string) {
      MsjInformativos o = findMsjInformativos(1);
      JDInfo jd;
      if(string.equalsIgnoreCase("abm_caja") && o.getAbmCaja()) {
         jd = new JDInfo(papi, true, o.getAbmCajaMsj());
         System.out.println("elegió :"+jd.getjCheckMsj());
      }
   }

   void mostrarMensaje(java.awt.Frame papi, String string) {
      MsjInformativos o = findMsjInformativos(1);
      JDInfo jd;
      if(string.equalsIgnoreCase("abm_caja") && o.getAbmCaja()) {
         jd = new JDInfo(papi, true, o.getAbmCajaMsj());
         System.out.println("elegió :"+jd.getjCheckMsj());
      }
   }



}
