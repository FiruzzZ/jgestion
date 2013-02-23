
package controller;

import controller.exceptions.NonexistentEntityException;
import entity.DetalleListaPrecios;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.ListaPrecios;

/**
 *
 * @author FiruzzZ
 */
public class DetalleListaPreciosJpaController {


    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(DetalleListaPrecios detalleListaPrecios) throws Exception {
        DAO.create(detalleListaPrecios);
    }

    public void edit(DetalleListaPrecios detalleListaPrecios) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            DetalleListaPrecios persistentDetalleListaPrecios = em.find(DetalleListaPrecios.class, detalleListaPrecios.getId());
            ListaPrecios listaPrecioOld = persistentDetalleListaPrecios.getListaPrecio();
            ListaPrecios listaPrecioNew = detalleListaPrecios.getListaPrecio();
            if (listaPrecioNew != null) {
                listaPrecioNew = em.getReference(listaPrecioNew.getClass(), listaPrecioNew.getId());
                detalleListaPrecios.setListaPrecio(listaPrecioNew);
            }
            detalleListaPrecios = em.merge(detalleListaPrecios);
            if (listaPrecioOld != null && !listaPrecioOld.equals(listaPrecioNew)) {
                listaPrecioOld.getDetalleListaPreciosList().remove(detalleListaPrecios);
                listaPrecioOld = em.merge(listaPrecioOld);
            }
            if (listaPrecioNew != null && !listaPrecioNew.equals(listaPrecioOld)) {
                listaPrecioNew.getDetalleListaPreciosList().add(detalleListaPrecios);
                listaPrecioNew = em.merge(listaPrecioNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = detalleListaPrecios.getId();
                if (findDetalleListaPrecios(id) == null) {
                    throw new NonexistentEntityException("The detalleListaPrecios with id " + id + " no longer exists.");
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
            DetalleListaPrecios detalleListaPrecios;
            try {
                detalleListaPrecios = em.getReference(DetalleListaPrecios.class, id);
                detalleListaPrecios.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detalleListaPrecios with id " + id + " no longer exists.", enfe);
            }
            ListaPrecios listaPrecio = detalleListaPrecios.getListaPrecio();
            if (listaPrecio != null) {
                listaPrecio.getDetalleListaPreciosList().remove(detalleListaPrecios);
                listaPrecio = em.merge(listaPrecio);
            }
            em.remove(detalleListaPrecios);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DetalleListaPrecios> findDetalleListaPreciosEntities() {
        return findDetalleListaPreciosEntities(true, -1, -1);
    }

    public List<DetalleListaPrecios> findDetalleListaPreciosEntities(int maxResults, int firstResult) {
        return findDetalleListaPreciosEntities(false, maxResults, firstResult);
    }

    private List<DetalleListaPrecios> findDetalleListaPreciosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from DetalleListaPrecios as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public DetalleListaPrecios findDetalleListaPrecios(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DetalleListaPrecios.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetalleListaPreciosCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from DetalleListaPrecios as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
