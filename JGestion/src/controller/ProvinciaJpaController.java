
package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.Provincia;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Departamento;
import java.util.ArrayList;
import java.util.List;
import entity.Proveedor;
import javax.swing.JComboBox;

/**
 *
 * @author FiruzzZ
 */
public class ProvinciaJpaController {

    // <editor-fold defaultstate="collapsed" desc="CRUD..">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(Provincia provincia) throws PreexistingEntityException, Exception {
      DAO.create(provincia);
   }

   public void edit(Provincia provincia) throws IllegalOrphanException, NonexistentEntityException, Exception {
      Provincia mergedEntity = DAO.doMerge(provincia);
   }

   public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Provincia provincia;
         try {
            provincia = em.getReference(Provincia.class, id);
            provincia.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The provincia with id " + id + " no longer exists.", enfe);
         }
         List<String> illegalOrphanMessages = null;
         List<Departamento> deptoListOrphanCheck = provincia.getDeptoList();
         for (Departamento deptoListOrphanCheckDepto : deptoListOrphanCheck) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("This Provincia (" + provincia + ") cannot be destroyed since the Depto " + deptoListOrphanCheckDepto + " in its deptoList field has a non-nullable idprovincia field.");
         }
         List<Proveedor> proveedorListOrphanCheck = provincia.getProveedorList();
         for (Proveedor proveedorListOrphanCheckProveedor : proveedorListOrphanCheck) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("This Provincia (" + provincia + ") cannot be destroyed since the Proveedor " + proveedorListOrphanCheckProveedor + " in its proveedorList field has a non-nullable provincia field.");
         }
         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         em.remove(provincia);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Provincia> findProvinciaEntities() {
      return findProvinciaEntities(true, -1, -1);
   }

   public List<Provincia> findProvinciaEntities(int maxResults, int firstResult) {
      return findProvinciaEntities(false, maxResults, firstResult);
   }

   private List<Provincia> findProvinciaEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Provincia as o order by o.nombre");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Provincia findProvincia(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Provincia.class, id);
      } finally {
         em.close();
      }
   }

   public int getProvinciaCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from Provincia as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

    public JComboBox getComboBoxProvincias() {
        JComboBox cbProvincias = new JComboBox();
         cbProvincias.removeAllItems();
        List<Provincia> l = findProvinciaEntities();

        for (Provincia provincia : l) {
            cbProvincias.addItem(provincia);
        }
        return cbProvincias;
    }

}
