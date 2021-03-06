package jgestion.jpa.controller;

import jgestion.entity.ChequeTerceros;
import jgestion.entity.ChequeTerceros_;
import jgestion.entity.Usuario;
import jgestion.entity.Usuario_;
import jgestion.entity.enums.ChequeEstado;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioJpaController extends JGestionJpaImpl<Usuario, Integer> {

    public UsuarioJpaController() {
    }


    public List<Usuario> findByEstado(boolean activo) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(getEntityClass());
        cq.where(cb.equal(cq.from(getEntityClass()).get(Usuario_.activo), activo));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<Usuario> findWithCheques() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(getEntityClass());
        Root<ChequeTerceros> from = cq.from(ChequeTerceros.class);
        cq.select(from.get(ChequeTerceros_.usuario));
        cq.where(cb.equal(from.get(ChequeTerceros_.estado), ChequeEstado.CARTERA.getId()));
        cq.groupBy(from.get(ChequeTerceros_.usuario));
        return getEntityManager().createQuery(cq).getResultList();
    }
}
