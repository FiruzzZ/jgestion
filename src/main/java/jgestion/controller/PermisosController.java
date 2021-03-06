package jgestion.controller;

import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.entity.Permisos;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import jgestion.entity.Usuario;
import jgestion.gui.PanelABMUsuarios;

/**
 *
 * @author Administrador
 */
public class PermisosController {

    /**
     * cuando se agrega un PermisoDe hay que modificar.
     * 1- {@link PanelABMUsuarios} > GUI > add JCheckBox, methods getter
     * 2- {@link UsuarioController#getPermisos(entity.Permisos) }
     * 3- {@link UsuarioController#setPanelABM(entity.Usuario) }
     * 4- {@link UsuarioController#checkPermiso(controller.PermisosJpaController.PermisoDe) }
     */
    public static enum PermisoDe {

        ABM_PRODUCTOS,
        ABM_PROVEEDORES,
        ABM_CLIENTES,
        ABM_CAJAS,
        ABM_USUARIOS,
        ABM_LISTA_PRECIOS,
        TESORERIA,
        DATOS_GENERAL,
        VENTA,
        COMPRA,
        CERRAR_CAJAS,
        ABM_CATALOGOWEB,
        ABM_OFERTASWEB,
        ORDENES_IO,
        VENTA_NUMERACION_MANUAL,
        ANULAR_COMPROBANTES,
        CHEQUES_ADMINISTRADOR, 
        CONFIGURACION;
    }

    public PermisosController() {
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD..">
    public void create(Permisos permisos) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario usuario = permisos.getUsuario();
            if (usuario != null) {
                usuario = em.getReference(usuario.getClass(), usuario.getId());
                permisos.setUsuario(usuario);
            }
            em.persist(permisos);
            if (usuario != null) {
                Permisos oldPermisosOfUsuario = usuario.getPermisos();
                if (oldPermisosOfUsuario != null) {
                    oldPermisosOfUsuario.setUsuario(null);
                    oldPermisosOfUsuario = em.merge(oldPermisosOfUsuario);
                }
                usuario.setPermisos(permisos);
                usuario = em.merge(usuario);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Permisos permisos) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Permisos persistentPermisos = em.find(Permisos.class, permisos.getId());
            Usuario usuarioOld = persistentPermisos.getUsuario();
            Usuario usuarioNew = permisos.getUsuario();
            if (usuarioNew != null) {
                usuarioNew = em.getReference(usuarioNew.getClass(), usuarioNew.getId());
                permisos.setUsuario(usuarioNew);
            }
            permisos = em.merge(permisos);
            if (usuarioOld != null && !usuarioOld.equals(usuarioNew)) {
                usuarioOld.setPermisos(null);
                usuarioOld = em.merge(usuarioOld);
            }
            if (usuarioNew != null && !usuarioNew.equals(usuarioOld)) {
                Permisos oldPermisosOfUsuario = usuarioNew.getPermisos();
                if (oldPermisosOfUsuario != null) {
                    oldPermisosOfUsuario.setUsuario(null);
                    oldPermisosOfUsuario = em.merge(oldPermisosOfUsuario);
                }
                usuarioNew.setPermisos(permisos);
                usuarioNew = em.merge(usuarioNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = permisos.getId();
                if (findPermisos(id) == null) {
                    throw new NonexistentEntityException("The permisos with id " + id + " no longer exists.");
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
            Permisos permisos;
            try {
                permisos = em.getReference(Permisos.class, id);
                permisos.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The permisos with id " + id + " no longer exists.", enfe);
            }
            Usuario usuario = permisos.getUsuario();
            if (usuario != null) {
                usuario.setPermisos(null);
                usuario = em.merge(usuario);
            }
            em.remove(permisos);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Permisos> findPermisosEntities() {
        return findPermisosEntities(true, -1, -1);
    }

    public List<Permisos> findPermisosEntities(int maxResults, int firstResult) {
        return findPermisosEntities(false, maxResults, firstResult);
    }

    private List<Permisos> findPermisosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Permisos as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Permisos findPermisos(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Permisos.class, id);
        } finally {
            em.close();
        }
    }

    public int getPermisosCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Permisos as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    Permisos getDefaultPermisos() {
        return new Permisos();
    }
}
