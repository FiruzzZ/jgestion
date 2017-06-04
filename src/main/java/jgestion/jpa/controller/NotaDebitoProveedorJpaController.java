package jgestion.jpa.controller;

import jgestion.entity.NotaDebitoProveedor;
import jgestion.entity.Proveedor;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class NotaDebitoProveedorJpaController extends JGestionJpaImpl<NotaDebitoProveedor, Integer> {

    public NotaDebitoProveedorJpaController() {
    }

    public NotaDebitoProveedor findBy(Proveedor proveedor, char tipo, Long numero) {
        try {
            return findByQuery(getSelectFrom()
                    + " WHERE o.proveedor.id=" + proveedor.getId()
                    + " AND o.numero=" + numero
                    + " AND o.tipo='" + Character.toUpperCase(tipo) + "'");
        } catch (NoResultException e) {
            return null;
        }
    }
}
