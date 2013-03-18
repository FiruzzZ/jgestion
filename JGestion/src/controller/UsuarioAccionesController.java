package controller;

import entity.*;
import jpa.controller.*;
//import org.apache.log4.Logger;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesController {

//    private static final Logger LOG = Logger.getLogger(UsuarioAccionesController.class.getName());
    private final UsuarioAccionesJpaController jpaController;

    public UsuarioAccionesController() {
        jpaController = new UsuarioAccionesJpaController();
    }

    public void create(UsuarioAcciones o) {
        String descripcion = o.getDescripcion();
        String detalle = o.getDetalle();
        if (o.getDescripcion().length() > 200) {
            descripcion = o.getDescripcion().substring(0, 200);
            detalle = o.getDescripcion().substring(200) + (detalle == null ? "" : detalle);
            if (detalle.length() > 2000) {
//                LOG.warn(o.getClass().getSimpleName() + ", detalle demasiado largo, se perdió:" + detalle.substring(2000));
                detalle = detalle.substring(0, 2000);
            }
        }
        o.setDescripcion(descripcion);
        o.setDetalle(detalle);
        jpaController.create(o);
    }

    public void log(Producto edited) {
        Producto old = new ProductoJpaController().find(edited.getId());
        StringBuilder sb = new StringBuilder(100);
        if (!old.getCodigo().equalsIgnoreCase(edited.getCodigo())) {
            sb.append("Código: ").append(old.getCodigo()).append(" -> ").append(edited.getCodigo());
        }
        if (!old.getNombre().equalsIgnoreCase(edited.getNombre())) {
            sb.append("Nombre: ").append(old.getNombre()).append(" -> ").append(edited.getNombre());
        }
        if (!old.getMarca().equals(edited.getMarca())) {
            sb.append("Marca: ").append(old.getMarca().getNombre()).append(" -> ").append(edited.getMarca().getNombre());
        }
        if (!old.getIva().equals(edited.getIva())) {
            sb.append("Iva: ").append(old.getIva()).append(" -> ").append(edited.getIva());
        }
        if (!old.getPrecioVenta().equals(edited.getPrecioVenta())) {
            sb.append("PrecioVenta: ").append(old.getPrecioVenta()).append(" -> ").append(edited.getPrecioVenta());
        }
        if (sb.toString().isEmpty()) {
            return;
        }
        UsuarioAcciones ua = new UsuarioAcciones('u',sb.toString(), null, old.getClass().getSimpleName(), old.getId(), UsuarioController.getCurrentUser());
        create(ua);
    }
}
