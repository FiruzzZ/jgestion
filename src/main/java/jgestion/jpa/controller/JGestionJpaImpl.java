package jgestion.jpa.controller;

import jgestion.controller.DAO;
import java.io.Serializable;
import javax.persistence.EntityManager;

public abstract class JGestionJpaImpl<T, ID extends Serializable> extends AbstractDAO<T, ID> {

    public JGestionJpaImpl() {
    }

    protected EntityManager entityManager;
    /**
     * Para cuando se necesita mantener la session abierta.
     * <br>Ej: recuperar objectos en LAZY load; manipular varios JPAControllers simultaneamente;
     * Asegurar atomicidad en procesos largos
     */
    private boolean keepEntityManagerOpen = false;

    public final boolean isKeepEntityManagerOpen() {
        return keepEntityManagerOpen;
    }

    public final void setKeepEntityManagerOpen(boolean keepEntityManagerOpen) {
        this.keepEntityManagerOpen = keepEntityManagerOpen;
    }

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public final void closeEntityManager() {
        if (!isKeepEntityManagerOpen()) {
            super.closeEntityManager();
        }
    }

    @Override
    public void loadLazies(T o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
