package jgestion.jpa.controller;

import jgestion.entity.Banco;
import jgestion.entity.ChequeTerceros;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class ChequeTercerosJpaController extends JGestionJpaImpl<ChequeTerceros, Integer> {

    /**
     *
     * @param banco
     * @param numero
     * @return an {@link ChequeTerceros} instance or {@code null} if there is no result.
     */
    public ChequeTerceros findBy(Banco banco, Long numero) {
        try {
            return (ChequeTerceros) getEntityManager().createQuery(getSelectFrom() + " WHERE o.banco.id=" + banco.getId() + " AND o.numero=" + numero).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
