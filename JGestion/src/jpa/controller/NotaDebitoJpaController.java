package jpa.controller;

import controller.DAO;
import entity.Cliente;
import entity.NotaDebito;
import entity.NotaDebito_;
import entity.Sucursal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class NotaDebitoJpaController extends AbstractDAO<NotaDebito, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Integer getNextNumero(Sucursal s, String tipo) {
        return getNextNumero(s, tipo.charAt(0));
    }

    public Integer getNextNumero(Sucursal s, char tipo) {
        EntityManager em = getEntityManager();
        Integer next = null;
        if (Character.toUpperCase(tipo) == 'A') {
            next = s.getNotaDebitoA();
        } else if (Character.toUpperCase(tipo) == 'B') {
            next = s.getNotaDebitoB();
        } else {
            throw new IllegalArgumentException("Parameter tipo no corresponde a ningún ti<po de Nota de Débito (A, B).");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<NotaDebito> from = cq.from(getEntityClass());
        cq.select(cb.max(from.get(NotaDebito_.numero)))
                .where(cb.and(
                cb.equal(from.get(NotaDebito_.sucursal), s),
                cb.equal(from.get(NotaDebito_.tipo), Character.toUpperCase(tipo))));

        Integer o = em.createQuery(cq).getSingleResult();
        if (o != null) {
            Integer nextNumeroSegunDB = 1 + Integer.valueOf(o.toString());
            if (nextNumeroSegunDB > next) {
                //quiere decir que hay registrado un comprobante con mayor numeracion que supera la configuración de la sucursal
                next = nextNumeroSegunDB;
            }
        }
        return next;
    }

    public NotaDebito findBy(Sucursal sucursal, char tipo, Integer numero) {
        try {
            return (NotaDebito) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o"
                    + " WHERE o.sucursal.id=" + sucursal.getId()
                    + " AND o.numero=" + numero
                    + " AND o.tipo='" + Character.toUpperCase(tipo) + "'").getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
