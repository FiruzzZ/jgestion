package jpa.controller;

import controller.DAO;
import entity.Proveedor;
import entity.Proveedor_;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class ProveedorJpaController extends AbstractDAO<Proveedor, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    /**
     * Ordered by nombre
     *
     * @return a list ordered by {@link Proveedor#nombre}
     */
    @Override
    public List<Proveedor> findAll() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Proveedor> cq = cb.createQuery(getEntityClass());
            Root<Proveedor> root = cq.from(getEntityClass());
            cq.select(root);
            cq.orderBy(cb.asc(root.get(Proveedor_.nombre)));
            return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
        } finally {
            closeEntityManager();
        }
    }

    /**
     * Solo recupera los atributos id, nombre, cuit
     *
     * @return
     */
    public List<Proveedor> findAllLite() {
        try {
//            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
//            ReadAllQuery readAllQuery = new ReadAllQuery(getEntityClass());
//            readAllQuery.dontMaintainCache();
//            FetchGroup fg = new FetchGroup();
//            fg.addAttribute("id");
//            fg.addAttribute("nombre");
//            fg.addAttribute("cuit");
//            readAllQuery.setFetchGroup(fg);
//            CriteriaQuery<Proveedor> cq = cb.createQuery(getEntityClass());
//            Root<Proveedor> root = cq.from(getEntityClass());
//            cq.multiselect(root.get(Proveedor_.id), root.get(Proveedor_.nombre), root.get(Proveedor_.cuit));
//            cq.orderBy(cb.asc(root.get(Proveedor_.nombre)));
            List<Object[]> l = getEntityManager().createQuery("SELECT o.id, o.nombre, o.cuit FROM " + getEntityClass().getSimpleName() + " o "
                    + " ORDER BY o.nombre").setHint(QueryHints.REFRESH, true).getResultList();
            List<Proveedor> ll = new ArrayList<>(l.size());
            for (Object[] o : l) {
                Proveedor p = new Proveedor();
                p.setId((Integer) o[0]);
                p.setNombre((String) o[1]);
                p.setCuit((long) o[2]);
                ll.add(p);
            }
            return ll;
        } finally {
            closeEntityManager();
        }

    }
}
