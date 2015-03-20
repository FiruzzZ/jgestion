package jgestion.controller;

import jgestion.entity.UsuarioAcciones;
import jgestion.entity.Producto;
import jgestion.jpa.controller.UsuarioAccionesJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesController {

    private static final Logger LOG = Logger.getLogger(UsuarioAccionesController.class);

    static UsuarioAcciones createUA(Object entity, Object id, String description, char accion) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(id);
        Objects.requireNonNull(description);
        String detalle = null;
        String descripcion = description;
        if (descripcion.length() > 200) {
            descripcion = description.substring(0, 200);
            detalle = description.substring(200);
            if (detalle.length() > 2000) {
                LOG.info(entity.getClass().getSimpleName() + ", detalle demasiado largo, se perdió:" + detalle.substring(2000));
                detalle = detalle.substring(0, 2000);
            }
        }
        UsuarioAcciones ua = new UsuarioAcciones(accion, descripcion, detalle, entity.getClass().getSimpleName(), (Integer) id, UsuarioController.getCurrentUser());
        return ua;
    }
    private final UsuarioAccionesJpaController jpaController;

    public UsuarioAccionesController() {
        jpaController = new UsuarioAccionesJpaController();
    }

    public void create(UsuarioAcciones o) {
        if (o.getUsuario() == null) {
            o.setUsuario(UsuarioController.getCurrentUser());
        }
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (o.getIp() == null) {
                o.setIp(local.getHostAddress());
            }
            if (o.getHostname() == null) {
                o.setHostname(local.getHostName());
            }
        } catch (UnknownHostException ex) {
            LOG.error(ex, ex);
        }
        String descripcion = o.getDescripcion();
        String detalle = o.getDetalle();
        if (o.getDescripcion().length() > 200) {
            descripcion = o.getDescripcion().substring(0, 200);
            detalle = o.getDescripcion().substring(200) + (detalle == null ? "" : detalle);
            if (detalle.length() > 2000) {
                detalle = detalle.substring(0, 2000);
            }
        }
        o.setDescripcion(descripcion);
        o.setDetalle(detalle);
        jpaController.persist(o);
    }

    public void createLog(Producto edited) {
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
        UsuarioAcciones ua = new UsuarioAcciones('u', sb.toString(), null, old.getClass().getSimpleName(), old.getId(), UsuarioController.getCurrentUser());
        create(ua);
    }
}
