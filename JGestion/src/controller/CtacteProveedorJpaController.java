
package controller;

import controller.exceptions.NonexistentEntityException;
import entity.CtacteProveedor;
import entity.FacturaCompra;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;

/**
 *
 * @author Administrador
 */
public class CtacteProveedorJpaController {


    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(CtacteProveedor ctacteProveedor) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(ctacteProveedor);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CtacteProveedor ctacteProveedor) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ctacteProveedor = em.merge(ctacteProveedor);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = ctacteProveedor.getId();
                if (findCtacteProveedor(id) == null) {
                    throw new NonexistentEntityException("The ctacteProveedor with id " + id + " no longer exists.");
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
            CtacteProveedor ctacteProveedor;
            try {
                ctacteProveedor = em.getReference(CtacteProveedor.class, id);
                ctacteProveedor.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ctacteProveedor with id " + id + " no longer exists.", enfe);
            }
            em.remove(ctacteProveedor);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CtacteProveedor> findCtacteProveedorEntities() {
        return findCtacteProveedorEntities(true, -1, -1);
    }

    public List<CtacteProveedor> findCtacteProveedorEntities(int maxResults, int firstResult) {
        return findCtacteProveedorEntities(false, maxResults, firstResult);
    }

    private List<CtacteProveedor> findCtacteProveedorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from CtacteProveedor as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public CtacteProveedor findCtacteProveedor(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CtacteProveedor.class, id);
        } finally {
            em.close();
        }
    }

    public int getCtacteProveedorCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from CtacteProveedor as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    void nuevaCtaCte(FacturaCompra facturaCompra) {
        CtacteProveedor ccp = new CtacteProveedor();
        ccp.setDias(facturaCompra.getDiasCtaCte());
        ccp.setEntregado(0.0); //monto $$
        ccp.setEstado(Valores.CtaCteEstado.PENDIENTE.getEstado());
        ccp.setFactura(facturaCompra);
        ccp.setFechaCarga(facturaCompra.getFechaalta());
        ccp.setHoraCarga(new java.util.Date());
        ccp.setImporte(facturaCompra.getImporte());
        create(ccp);
    }

   List<CtacteProveedor> findCtacteProveedorByProveedor(Integer idProveedor, int estadoCtaCte) {
      System.out.println("findCtaCteProveedorFromProveedor ("+idProveedor+", "+estadoCtaCte+")");
      EntityManager em = getEntityManager();
      em.getTransaction().begin();
      List<CtacteProveedor> listaCtaCteProveedor = null;
      try {
         listaCtaCteProveedor = em.createNativeQuery(
              "SELECT o.* FROM ctacte_proveedor o, factura_compra f, proveedor p"
              +" WHERE p.id = f.proveedor AND f.id = o.factura "
              +" AND o.estado = " +estadoCtaCte+ " AND p.id ="+idProveedor,
              CtacteProveedor.class).getResultList();
      }catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteProveedor;
   }

   List<CtacteProveedor> findCtacteProveedorByProveedor(Integer idProveedor) {
      EntityManager em = getEntityManager();
      em.getTransaction().begin();
      List<CtacteProveedor> listaCtaCteProveedor = null;
      try {
         listaCtaCteProveedor = em.createNativeQuery(
              "SELECT o.* FROM ctacte_proveedor o, factura_compra f, proveedor p"
              +" WHERE p.id = f.proveedor AND f.id = o.factura "
              +" AND p.id ="+idProveedor,
              CtacteProveedor.class).getResultList();
      }catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteProveedor;
   }

   CtacteProveedor findCtacteProveedorByFactura(Integer idFacturaCompra) throws NoResultException{
      return (CtacteProveedor) DAO.getEntityManager()
              .createNativeQuery("select * from ctacte_proveedor o " +
              "where o.factura = "+idFacturaCompra, CtacteProveedor.class)
              .getSingleResult();

   }
   
}
