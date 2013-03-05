package jpa.controller;

import controller.DAO;
import entity.Producto;
import entity.Producto_;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

    @Override
    public List<Producto> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Producto> cq = cb.createQuery(getEntityClass());
        Root<Producto> from = cq.from(getEntityClass());
        cq.select(from).orderBy(cb.asc(from.get(Producto_.nombre)));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<Producto> findByBienDeCambio(Boolean bienDeCambio) {
        if (bienDeCambio == null) {
            return findAll();
        }
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Producto> cq = cb.createQuery(getEntityClass());
        Root<Producto> from = cq.from(getEntityClass());
        cq.select(from);
        cq.where(cb.equal(from.get(Producto_.bienDeCambio), bienDeCambio));
        cq.orderBy(cb.asc(from.get(Producto_.nombre)));
        return getEntityManager().createQuery(cq).getResultList();
    }
}
