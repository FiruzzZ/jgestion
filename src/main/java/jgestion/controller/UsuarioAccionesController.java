package jgestion.controller;

import jgestion.entity.UsuarioAcciones;
import jgestion.entity.Producto;
import jgestion.jpa.controller.UsuarioAccionesJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesController {

    private static final Logger LOG = LogManager.getLogger();
    private final UsuarioAccionesJpaController jpaController = new UsuarioAccionesJpaController();
    /**
     * Map para establecer las relaciones de propiedad entre las entidades. Una entidad no puede
     * tener mas de un Owner, pero puede no tener ninguno
     */
    public static final HashMap<String, String> ownership = new HashMap<>(50);

    static {
//        LOG.info("inicializando ownership map");
//        addOwnership(DisTrasladoPresupuesto.class, DisTraslado.class);
    }

    private static void addOwnership(Class<?> child, Class<?> owner) {
        String strChild = child.getSimpleName();
        String strOwner = owner.getSimpleName();
        if (ownership.containsKey(strChild)) {
            throw new IllegalArgumentException("Ya existe owner para " + strChild + ": " + ownership.get(strChild)
                    + ", no se puede agregar " + strOwner);
        }
        ownership.put(strChild, strOwner);
    }

    public static UsuarioAcciones build(Object entity, Integer id, String description, String detalle, char accion) {
        return build(entity, id, description, detalle, accion, null, null);
    }

    public static UsuarioAcciones build(Object entity, Integer id, String descripcion, String detalle, char accion, Object owner, Object ownerID) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(id);
        Objects.requireNonNull(descripcion);
        String detalleD;
        String descripcionD = descripcion;
        if (descripcionD.length() > 200) {
            descripcionD = descripcion.substring(0, 200);
            detalleD = descripcion.substring(200) + "]";
            detalleD += detalle;
        } else {
            detalleD = detalle;
        }
        if (detalleD != null && detalleD.length() > 2000) {
            LOG.warn(entity.getClass().getSimpleName() + ", detalle demasiado largo, se perdió:" + detalleD.substring(2000));
            detalleD = detalleD.substring(0, 2000);
        }
        UsuarioAcciones ua = new UsuarioAcciones(accion, descripcionD, detalleD, entity.getClass().getSimpleName(), id, UsuarioController.getCurrentUser(),
                (owner != null ? owner.getClass().getSimpleName() : null),
                (ownerID != null ? ownerID.toString() : null));
        return ua;
    }

    public UsuarioAccionesController() {

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
