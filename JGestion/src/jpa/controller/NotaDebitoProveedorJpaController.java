package jpa.controller;

import controller.DAO;
import entity.NotaDebitoProveedor;
import entity.NotaDebitoProveedor_;
import entity.Proveedor;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class NotaDebitoProveedorJpaController extends AbstractDAO<NotaDebitoProveedor, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public List<NotaDebitoProveedor> findBy(Proveedor proveedor, Boolean recibadas) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<NotaDebitoProveedor> cq = cb.createQuery(getEntityClass());
        Root<NotaDebitoProveedor> from = cq.from(getEntityClass());
        if (recibadas != null) {
            if (recibadas) {
                cq.where(cb.equal(from.get(NotaDebitoProveedor_.proveedor), proveedor),
                        cb.isNotNull(from.get(NotaDebitoProveedor_.remesa)));
            } else {
                cq.where(cb.equal(from.get(NotaDebitoProveedor_.proveedor), proveedor),
                        cb.isNull(from.get(NotaDebitoProveedor_.remesa)));
            }
        } else {
            cq.where(cb.equal(from.get(NotaDebitoProveedor_.proveedor), proveedor));
        }
        cq.orderBy(cb.asc(from.get(NotaDebitoProveedor_.fechaNotaDebito)));
        return getEntityManager().createQuery(cq).getResultList();
    }
}
