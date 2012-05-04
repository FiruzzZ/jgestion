
package jpa.controller;

import entity.FacturaCompra;
import java.io.Serializable;
import javax.persistence.EntityManager;

/**
 *
 * @author Administrador
 */
public class FacturaCompraJpaController extends AbstractDAO<FacturaCompra, Integer> {

    @Override
    protected EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
