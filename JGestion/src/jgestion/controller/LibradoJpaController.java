package jgestion.controller;

import jgestion.entity.Librado;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author Administrador
 */
public class LibradoJpaController implements Serializable {

    private static List<Librado> LIBRADO_LIST;

    public LibradoJpaController() {
        if (LIBRADO_LIST == null) {
            LIBRADO_LIST = getEntityManager().createNamedQuery("Librado.findAll").getResultList();
            if (LIBRADO_LIST.isEmpty()) {
                create(new Librado(1, "A LA ORDEN"));
                create(new Librado(2, "AL PORTADOR"));
                create(new Librado(3, "NO A LA ORDEN"));
            }
            LIBRADO_LIST = DAO.getEntityManager().createNamedQuery("Librado.findAll").getResultList();
        }
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public final void create(Librado librado) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(librado);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Librado findLibrado(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Librado.class, id);
        } finally {
            em.close();
        }
    }

    public List<Librado> findEntities() {
        return LIBRADO_LIST;
    }
}
