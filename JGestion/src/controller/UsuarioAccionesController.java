package controller;

import entity.*;
import java.util.logging.Logger;
import jpa.controller.*;
//import org.apache.log4.Logger;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesController {

    private static final Logger LOG = Logger.getLogger(UsuarioAccionesController.class.getName());
    private final UsuarioAccionesJpaController jpaController;

    public UsuarioAccionesController() {
        jpaController = new UsuarioAccionesJpaController();
    }

    public void log(Producto edited) {
        Producto old = new ProductoJpaController().find(edited.getId());

    }
}
