package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.Cheque;
import jgestion.entity.ChequePropio;
import jgestion.entity.CuentaBancaria;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.CuentabancariaMovimientos_;
import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaMovimientosJpaController extends AbstractDAO<CuentabancariaMovimientos, Integer> {

    private EntityManager entityManager;

    public CuentabancariaMovimientosJpaController() {
        getEntityManager();
    }

    @Override
    protected final EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public CuentabancariaMovimientos findBy(Cheque cheque) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CuentabancariaMovimientos> cq = cb.createQuery(getEntityClass());
        Root<CuentabancariaMovimientos> from = cq.from(getEntityClass());
        if (cheque instanceof ChequePropio) {
            cq.where(cb.equal(from.get(CuentabancariaMovimientos_.chequePropio), cheque));
        } else {
            cq.where(cb.equal(from.get(CuentabancariaMovimientos_.chequeTerceros), cheque));
        }
        CuentabancariaMovimientos o;
        try {
            o = getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            //cuando el cheque no se marcó como ChequeEstado.DEBITADO
            //no se generó el débito en la cuenta aún
            o = null;
        }
        return o;
    }

    public BigDecimal getSaldo(CuentaBancaria cb) {
        BigDecimal saldo = (BigDecimal) findAttribute(
                "SELECT "
                + "SUM(o." + CuentabancariaMovimientos_.credito.getName() + ") -"
                + "SUM(o." + CuentabancariaMovimientos_.debito.getName() + ") "
                + "FROM " + getEntityClass().getSimpleName() + " o "
                + "WHERE o.cuentaBancaria.id=" + cb.getId());

        return saldo;
    }
}
