package jgestion.jpa.controller;

import jgestion.entity.CtacteCliente;
import jgestion.entity.CtacteCliente_;
import jgestion.entity.FacturaVenta;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.NotaDebito;

/**
 *
 * @author FiruzzZ
 */
public class CtacteClienteJpaController extends JGestionJpaImpl<CtacteCliente, Integer> {

    public CtacteClienteJpaController() {
    }

    public CtacteCliente findByNotaDebito(NotaDebito nd) {
        try {
            return (CtacteCliente) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
                    + " where o.notaDebito.id = " + nd.getId()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public CtacteCliente findByFactura(Integer id) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CtacteCliente> query = cb.createQuery(CtacteCliente.class);
        Root<CtacteCliente> from = query.from(CtacteCliente.class);
        FacturaVenta facturaVenta = new FacturaVenta();
        facturaVenta.setId(id);
        query.select(from).where(cb.equal(from.get(CtacteCliente_.factura), facturaVenta));
        try {
            return getEntityManager().createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
// the easy way....        
//        try {
//            return (CtacteCliente) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
//                    + " where o.factura.id = " + id).getSingleResult();
//        } catch (NoResultException e) {
//            return null;
//        }
    }

}
