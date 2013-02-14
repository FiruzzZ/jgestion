package jpa.controller;

import controller.DAO;
import entity.Producto;
import javax.persistence.EntityManager;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Administrador
 */
public class ProductoJpaController extends AbstractDAO<Producto, Integer> {

    private EntityManager entityManager;

    public ProductoJpaController() {
        getEntityManager();
    }

    @Override
    protected final EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public Producto find(Integer id) {
        Producto find = getEntityManager().find(getEntityClass(), id);
        entityManager.setProperty(QueryHints.REFRESH, Boolean.TRUE);
        entityManager.refresh(find);
        return find;
    }

    public Producto findByCodigo(String codigo) {
//        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
//        CriteriaQuery<Producto> cq = cb.createQuery(getEntityClass());
//        Root<Producto> from = cq.from(getEntityClass());
//        cb.equal(from.get(Producto_.codigo), codigoProducto);
//        return getEntityManager().createQuery(cq).getSingleResult();
        return getEntityManager().
                createQuery("SELECT o FROM Producto o WHERE o.codigo='" + codigo + "'", getEntityClass()).
                setHint(QueryHints.REFRESH, Boolean.TRUE).
                getSingleResult();
    }
    
}
