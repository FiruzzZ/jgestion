package jgestion.jpa.controller;

import jgestion.controller.DAO;
import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import jgestion.controller.exceptions.ConstraintViolationJpaException;

public abstract class JGestionJpaImpl<T, ID extends Serializable> extends AbstractDAO<T, ID> {

    protected EntityManager entityManager;

    public JGestionJpaImpl() {
    }

    public JGestionJpaImpl(boolean forceRefresh) {
        super(forceRefresh);
    }

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public void remove(T entity) {
        try {
            super.remove(entity);
        } catch (ValidationException ex) {
            if (ex instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) ex;
                throw new ConstraintViolationJpaException(
                        "No se puede eliminar porque existen otros registros que están relacionados a este",
                        //                        cve.getSQLException(), cve.getSQL(), cve.getConstraintName()
                        null, null, null
                );
            } else {
                throw ex;
            }
        } catch (PersistenceException e) {
            if (e.getCause().getCause() != null) {
                if (e.getCause().getCause() instanceof org.postgresql.util.PSQLException) {
                    throw new ConstraintViolationJpaException(
                            "No se puede eliminar porque existen otros registros que están relacionados a este",
                            null, null, null
                    );
                }
            }
        }
    }

    @Override
    public void loadLazies(T o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
