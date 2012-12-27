package jpa.controller;

import controller.DAO;
import entity.Sucursal;
import entity.UnidadDeNegocio;
import entity.UnidadDeNegocio_;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class UnidadDeNegocioJpaController extends AbstractDAO<UnidadDeNegocio, Integer> {

    private EntityManager entityManager;

    public UnidadDeNegocioJpaController() {
    }

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
        String sql = "";
        for (Sucursal oldSucu : old.getSucursales()) {
            if (!udn.getSucursales().contains(oldSucu)) {
                System.out.println("chau=" + oldSucu.getNombre());
                sql += "DELETE FROM unidad_de_negocio_sucursal WHERE unidad_de_negocio_id =" + udn.getId() + " AND sucursal_id=" + oldSucu.getId() + ";";
            }

        }
        if (!sql.isEmpty()) {
            getEntityManager().createNativeQuery(sql).executeUpdate();
        }
        sql = "";
        for (Sucursal newSucu : udn.getSucursales()) {
            if (!old.getSucursales().contains(newSucu)) {
                sql += "INSERT INTO unidad_de_negocio_sucursal VALUES (" + udn.getId() + ", " + newSucu.getId() + ");";
            }
        }
        if (!sql.isEmpty()) {
            getEntityManager().createNativeQuery(sql).executeUpdate();
        }
        getEntityManager().merge(udn);
        getEntityManager().getTransaction().commit();
        return udn;
    }

    @Override
    public List<UnidadDeNegocio> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UnidadDeNegocio> cq = cb.createQuery(getEntityClass());
        cq.select(cq.from(getEntityClass()));
        cq.orderBy(cb.asc(cq.from(getEntityClass()).get(UnidadDeNegocio_.nombre)));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();

    }
}
