package controller;

import gui.JDCuentabancariaManager;
import java.awt.Window;
import javax.swing.JDialog;
import jpa.controller.CuentabancariaJpaController;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaMovimientosController {

    private static final Logger LOG = Logger.getLogger(CuentabancariaMovimientosController.class.getName());
    private CuentabancariaJpaController jpaController;
    private JDCuentabancariaManager manager;

    public CuentabancariaMovimientosController() {
        jpaController = new CuentabancariaJpaController();
    }

    public JDialog getContenedor(Window owner) {
        manager = new JDCuentabancariaManager(owner);
        
        return manager;
    }
}
