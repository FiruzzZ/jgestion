
package controller;

import controller.exceptions.NonexistentEntityException;
import entity.DetalleRemesa;
import entity.FacturaCompra;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Remesa;

/**
 *
 * @author Administrador
 */
public class DetalleRemesaJpaController {
//
//    public DetalleRemesaJpaController() {
//        emf = Persistence.createEntityManagerFactory("JGestionPU");
//    }
//    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }
    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public void create(DetalleRemesa detalleRemesa) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Remesa remesa = detalleRemesa.getRemesa();
            if (remesa != null) {
                remesa = em.getReference(remesa.getClass(), remesa.getId());
                detalleRemesa.setRemesa(remesa);
            }
            em.persist(detalleRemesa);
            if (remesa != null) {
                remesa.getDetalleRemesaList().add(detalleRemesa);
                remesa = em.merge(remesa);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(DetalleRemesa detalleRemesa) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            DetalleRemesa persistentDetalleRemesa = em.find(DetalleRemesa.class, detalleRemesa.getId());
            Remesa remesaOld = persistentDetalleRemesa.getRemesa();
            Remesa remesaNew = detalleRemesa.getRemesa();
            if (remesaNew != null) {
                remesaNew = em.getReference(remesaNew.getClass(), remesaNew.getId());
                detalleRemesa.setRemesa(remesaNew);
            }
            detalleRemesa = em.merge(detalleRemesa);
            if (remesaOld != null && !remesaOld.equals(remesaNew)) {
                remesaOld.getDetalleRemesaList().remove(detalleRemesa);
                remesaOld = em.merge(remesaOld);
            }
            if (remesaNew != null && !remesaNew.equals(remesaOld)) {
                remesaNew.getDetalleRemesaList().add(detalleRemesa);
                remesaNew = em.merge(remesaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = detalleRemesa.getId();
                if (findDetalleRemesa(id) == null) {
                    throw new NonexistentEntityException("The detalleRemesa with id " + id + " no longer exists.");
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
            DetalleRemesa detalleRemesa;
            try {
                detalleRemesa = em.getReference(DetalleRemesa.class, id);
                detalleRemesa.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detalleRemesa with id " + id + " no longer exists.", enfe);
            }
            Remesa remesa = detalleRemesa.getRemesa();
            if (remesa != null) {
                remesa.getDetalleRemesaList().remove(detalleRemesa);
                remesa = em.merge(remesa);
            }
            em.remove(detalleRemesa);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DetalleRemesa> findDetalleRemesaEntities() {
        return findDetalleRemesaEntities(true, -1, -1);
    }

    public List<DetalleRemesa> findDetalleRemesaEntities(int maxResults, int firstResult) {
        return findDetalleRemesaEntities(false, maxResults, firstResult);
    }

    private List<DetalleRemesa> findDetalleRemesaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from DetalleRemesa as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public DetalleRemesa findDetalleRemesa(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DetalleRemesa.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetalleRemesaCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from DetalleRemesa as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

   List<DetalleRemesa> findDetalleRemesaByFactura(FacturaCompra factura) {
      return getEntityManager().createQuery("SELECT o FROM DetalleRemesa o WHERE o.facturaCompra = :facturaCompra")
              .setParameter("facturaCompra", factura)
              .getResultList();
   }

}
