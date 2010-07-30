/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.DetallePresupuesto;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Presupuesto;

/**
 *
 * @author Administrador
 */
public class DetallePresupuestoJpaController {

   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(DetallePresupuesto detallePresupuesto) throws PreexistingEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Presupuesto presupuesto = detallePresupuesto.getPresupuesto();
         if (presupuesto != null) {
            presupuesto = em.getReference(presupuesto.getClass(), presupuesto.getId());
            detallePresupuesto.setPresupuesto(presupuesto);
         }
         em.persist(detallePresupuesto);
         if (presupuesto != null) {
            presupuesto.getDetallePresupuestoList().add(detallePresupuesto);
            presupuesto = em.merge(presupuesto);
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         if(em.getTransaction().isActive())
            em.getTransaction().rollback();
         
         if (findDetallePresupuesto(detallePresupuesto.getId()) != null) {
            throw new PreexistingEntityException("DetallePresupuesto " + detallePresupuesto + " already exists.", ex);
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(DetallePresupuesto detallePresupuesto) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         DetallePresupuesto persistentDetallePresupuesto = em.find(DetallePresupuesto.class, detallePresupuesto.getId());
         Presupuesto presupuestoOld = persistentDetallePresupuesto.getPresupuesto();
         Presupuesto presupuestoNew = detallePresupuesto.getPresupuesto();
         if (presupuestoNew != null) {
            presupuestoNew = em.getReference(presupuestoNew.getClass(), presupuestoNew.getId());
            detallePresupuesto.setPresupuesto(presupuestoNew);
         }
         detallePresupuesto = em.merge(detallePresupuesto);
         if (presupuestoOld != null && !presupuestoOld.equals(presupuestoNew)) {
            presupuestoOld.getDetallePresupuestoList().remove(detallePresupuesto);
            presupuestoOld = em.merge(presupuestoOld);
         }
         if (presupuestoNew != null && !presupuestoNew.equals(presupuestoOld)) {
            presupuestoNew.getDetallePresupuestoList().add(detallePresupuesto);
            presupuestoNew = em.merge(presupuestoNew);
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = detallePresupuesto.getId();
            if (findDetallePresupuesto(id) == null) {
               throw new NonexistentEntityException("The detallePresupuesto with id " + id + " no longer exists.");
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
         DetallePresupuesto detallePresupuesto;
         try {
            detallePresupuesto = em.getReference(DetallePresupuesto.class, id);
            detallePresupuesto.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The detallePresupuesto with id " + id + " no longer exists.", enfe);
         }
         Presupuesto presupuesto = detallePresupuesto.getPresupuesto();
         if (presupuesto != null) {
            presupuesto.getDetallePresupuestoList().remove(detallePresupuesto);
            presupuesto = em.merge(presupuesto);
         }
         em.remove(detallePresupuesto);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<DetallePresupuesto> findDetallePresupuestoEntities() {
      return findDetallePresupuestoEntities(true, -1, -1);
   }

   public List<DetallePresupuesto> findDetallePresupuestoEntities(int maxResults, int firstResult) {
      return findDetallePresupuestoEntities(false, maxResults, firstResult);
   }

   private List<DetallePresupuesto> findDetallePresupuestoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from DetallePresupuesto as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public DetallePresupuesto findDetallePresupuesto(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(DetallePresupuesto.class, id);
      } finally {
         em.close();
      }
   }

   public int getDetallePresupuestoCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from DetallePresupuesto as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }

}
