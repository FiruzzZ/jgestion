package jgestion.jpa.controller;

import java.math.BigDecimal;
import java.util.List;
import jgestion.controller.Valores;
import jgestion.entity.CtacteProveedor;
import jgestion.entity.FacturaCompra;
import jgestion.entity.NotaDebitoProveedor;
import jgestion.entity.Proveedor;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class CtacteProveedorJpaController extends JGestionJpaImpl<CtacteProveedor, Integer> {

    public CtacteProveedorJpaController() {
    }

    public CtacteProveedor findBy(FacturaCompra nd) {
        return findByQuery(getSelectFrom() + " WHERE o.factura.id=" + nd.getId());
    }

    public CtacteProveedor findBy(NotaDebitoProveedor nd) {
        return findByQuery(getSelectFrom() + " WHERE o.notaDebito.id=" + nd.getId());
    }

    public List<CtacteProveedor> findAllBy(Proveedor proveedor, Valores.CtaCteEstado ctaCteEstado) {
        List<CtacteProveedor> ff = getEntityManager().createQuery(getSelectFrom()
                + " WHERE o.estado=" + ctaCteEstado.getId()
                + " AND o.factura.proveedor.id =" + proveedor.getId()
                + " ORDER BY o.factura.numero", getEntityClass())
                .setHint(QueryHints.REFRESH, Boolean.TRUE)
                .getResultList();
        List<CtacteProveedor> nndd = getEntityManager().createQuery(getSelectFrom()
                + " WHERE o.estado=" + ctaCteEstado.getId()
                + " AND o.notaDebito.proveedor.id =" + proveedor.getId()
                + " ORDER BY o.notaDebito.numero", getEntityClass())
                .setHint(QueryHints.REFRESH, Boolean.TRUE)
                .getResultList();
        ff.addAll(nndd);
        return ff;
    }

    public BigDecimal getSaldo(NotaDebitoProveedor nd) {
        return (BigDecimal) findAttribute("SELECT o.importe - o.entregado FROM " + getAlias() + " WHERE o.notaDebito.id=" + nd.getId());
    }
}
