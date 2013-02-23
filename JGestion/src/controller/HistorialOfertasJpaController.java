package controller;

import controller.exceptions.NonexistentEntityException;
import entity.HistorialOfertas;
import entity.Producto;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

/**
 *
 * @author Administrador
 */
public class HistorialOfertasJpaController {

   public final static String CLASS_NAME = HistorialOfertas.class.getSimpleName();

   public HistorialOfertasJpaController() {
   }

   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD">
   public void create(HistorialOfertas historialOfertas) {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(historialOfertas);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(HistorialOfertas historialOfertas) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         historialOfertas = em.merge(historialOfertas);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = historialOfertas.getId();
            if (findHistorialOfertas(id) == null) {
               throw new NonexistentEntityException("The historialOfertas with id " + id + " no longer exists.");
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
         HistorialOfertas historialOfertas;
         try {
            historialOfertas = em.getReference(HistorialOfertas.class, id);
            historialOfertas.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The historialOfertas with id " + id + " no longer exists.", enfe);
         }
         em.remove(historialOfertas);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<HistorialOfertas> findHistorialOfertasEntities() {
      return findHistorialOfertasEntities(true, -1, -1);
   }

   public List<HistorialOfertas> findHistorialOfertasEntities(int maxResults, int firstResult) {
      return findHistorialOfertasEntities(false, maxResults, firstResult);
   }

   private List<HistorialOfertas> findHistorialOfertasEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from HistorialOfertas as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public HistorialOfertas findHistorialOfertas(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(HistorialOfertas.class, id);
      } finally {
         em.close();
      }
   }

   public int getHistorialOfertasCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from HistorialOfertas as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   /**
    * Retorna el Producto destacado/oferta vigente del si es que la hay.
    * @param producto del cual se va buscar la oferta.
    * @return the found entity instance or null if the entity does not exist.
    */
   public HistorialOfertas findOfertaVigente(Producto producto) {
      try {
         return (HistorialOfertas) getEntityManager().createQuery(
                 "SELECT o FROM " + CLASS_NAME + " o WHERE o.vigente = TRUE AND o.producto.id=" + producto.getId()).getSingleResult();
      } catch (NoResultException e) {
         return null;
      }
   }

   /**
    * Chequea si historialOfertaID corresponde a una oferta vigente.
    * @param historialOfertaID
    * @return <code>true</code> si existe, <code>false</code> si no o
    * <code>null</code> si hay mas de UNO (culpa del DBA!!).
    */
   public Boolean isOfertaVigente(Integer historialOfertaID) {
      try {
         Object singleResult = getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.vigente = TRUE AND o.id=" + historialOfertaID).getSingleResult();
         return true;
      } catch (NoResultException e) {
         return false;
      } catch (NonUniqueResultException ex) {
         return null;
      }
   }

   /**
    * Busca y deshabilita la oferta vigente del Producto.
    * Si existe una oferta vigente del producto, es puesta en false.
    * @param producto
    * @return true if a entity was found it and updated, otherwise false.
    * @throws NonexistentEntityException
    * @throws Exception 
    */
   public Boolean disableOfertaVigente(Producto producto) throws NonexistentEntityException, Exception {
      HistorialOfertas foundOfertaVigente = findOfertaVigente(producto);
      if (foundOfertaVigente != null) {
         foundOfertaVigente.setVigente(false);
         edit(foundOfertaVigente);
         return true;
      }
      return false;
   }
}
