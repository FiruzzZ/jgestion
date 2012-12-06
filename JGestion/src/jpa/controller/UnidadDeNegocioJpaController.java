package jpa.controller;

import controller.DAO;
import entity.Sucursal;
import entity.UnidadDeNegocio;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class UnidadDeNegocioJpaController extends AbstractDAO<UnidadDeNegocio, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public UnidadDeNegocio merge(UnidadDeNegocio udn) {
        for (Sucursal sucursal : udn.getSucursales()) {
            System.out.println("edit=" + sucursal.getNombre());
        }
        UnidadDeNegocio old = find(udn.getId());
        for (Sucursal sucursal : old.getSucursales()) {
            System.out.println("old=" + sucursal.getNombre());
        }
        getEntityManager().getTransaction().begin();
        String deleteQuery = "";
        for (Sucursal oldSucu : old.getSucursales()) {
            if (!udn.getSucursales().contains(oldSucu)) {
                System.out.println("chau=" + oldSucu.getNombre());
                deleteQuery += "DELETE FROM unidad_de_negocio_sucursal WHERE unidad_de_negocio_id =" + udn.getId() + " AND sucursal_id=" + oldSucu.getId() + ";";
            }

        }
        if (!deleteQuery.isEmpty()) {
            getEntityManager().createNativeQuery(deleteQuery).executeUpdate();
        }
        deleteQuery = "";
        for (Sucursal newSucu : udn.getSucursales()) {
            if (!old.getSucursales().contains(newSucu)) {
                deleteQuery += "INSERT INTO unidad_de_negocio_sucursal VALUES (" + udn.getId() + ", " + newSucu.getId() + ");";
            }
        }
        if (!deleteQuery.isEmpty()) {
            getEntityManager().createNativeQuery(deleteQuery).executeUpdate();
        }
        getEntityManager().getTransaction().commit();
        return udn;
    }
}
