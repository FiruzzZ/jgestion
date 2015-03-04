package jgestion.controller;

import jgestion.entity.MsjInformativos;
import jgestion.gui.JDInfo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Administrador
 */
public class MsjInformativosJpaController {

   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public List<MsjInformativos> findMsjInformativosEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from MsjInformativos as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   
   public MsjInformativos findMsjInformativos(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(MsjInformativos.class, id);
      } finally {
         em.close();
      }
   }
   
   public MsjInformativos findMsjInformativos(String code) {
      EntityManager em = getEntityManager();
      try {
         return em.find(MsjInformativos.class, code.toUpperCase());
      } finally {
         em.close();
      }
   }

   void mostrarMensaje(javax.swing.JDialog papi, String string) {
      MsjInformativos o = findMsjInformativos(1);
      JDInfo jd;
   }

   void mostrarMensaje(java.awt.Frame papi, String string) {
      MsjInformativos o = findMsjInformativos(1);
      JDInfo jd;
   }



}
