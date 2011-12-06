package controller;

import controller.exceptions.NonexistentEntityException;
import entity.DetalleAcreditacion;
import entity.DetalleRecibo;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author Administrador
 */
public class DetalleAcreditacionJpaController implements Serializable {

    public DetalleAcreditacionJpaController() {
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(DetalleAcreditacion detalleAcreditacion) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(detalleAcreditacion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(DetalleAcreditacion detalleAcreditacion) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            detalleAcreditacion = em.merge(detalleAcreditacion);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = detalleAcreditacion.getId();
                if (findDetalleAcreditacion(id) == null) {
                    throw new NonexistentEntityException("The detalleAcreditacion with id " + id + " no longer exists.");
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
            DetalleAcreditacion detalleAcreditacion;
            try {
                detalleAcreditacion = em.getReference(DetalleAcreditacion.class, id);
                detalleAcreditacion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detalleAcreditacion with id " + id + " no longer exists.", enfe);
            }
            em.remove(detalleAcreditacion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DetalleAcreditacion> findDetalleAcreditacionEntities() {
        return findDetalleAcreditacionEntities(true, -1, -1);
    }

    public List<DetalleAcreditacion> findDetalleAcreditacionEntities(int maxResults, int firstResult) {
        return findDetalleAcreditacionEntities(false, maxResults, firstResult);
    }

    private List<DetalleAcreditacion> findDetalleAcreditacionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from DetalleAcreditacion as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public DetalleAcreditacion findDetalleAcreditacion(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DetalleAcreditacion.class, id);
        } finally {
            em.close();
        }
    }

    DetalleAcreditacion anular(DetalleRecibo detalle) {
        DetalleAcreditacion o;
        EntityManager em = getEntityManager();
        try {
            o = (DetalleAcreditacion) em.createQuery("SELECT o FROM " + DetalleAcreditacion.class.getSimpleName() + " o "
                    + "WHERE o.detalleRecibo.id= " + detalle.getId()).getSingleResult();
            o.setAnulado(true);
            em.merge(o);
        } finally {
            em.close();
        }
        return o;
    }
}
