package controller;

import entity.Caja;
import entity.PermisosSucursal;
import entity.Sucursal;
import entity.Usuario;
import java.util.ArrayList;
import java.util.List;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author Administrador
 */
class UsuarioHelper {

    private Usuario usuario;

    UsuarioHelper() {
        usuario = UsuarioController.getCurrentUser();
        refresh();
    }

    UsuarioHelper(Usuario usuario) {
        this.usuario = usuario;
        refresh();
    }

    private void refresh() {
        try {
            DAO.getEntityManager().refresh(usuario);
        } catch (IllegalArgumentException ex) {
            usuario = DAO.getEntityManager().find(usuario.getClass(), usuario.getId());
        }
    }

    List<Sucursal> getSucursales() {
        List<Sucursal> l = new ArrayList<Sucursal>(usuario.getSucursales().size());
        for (PermisosSucursal permisosSucursal : usuario.getSucursales()) {
            l.add(permisosSucursal.getSucursal());
        }
        return l;
    }

    /**
     * Recupera las cajas a las cuales el usuario actual tiene acceso
     *
     * @param estado
     * @return
     */
    List<Caja> getCajas(Boolean estado) {
        return new CajaController().findCajasPermitidasByUsuario(usuario, estado);
    }

    /**
     * Envuelve las cajas recperadas por {@link #getCajas(java.lang.Boolean) }
     *
     * @param estado
     * @return
     */
    List<ComboBoxWrapper<Caja>> getWrappedCajas(Boolean estado) {
        List<Caja> list = getCajas(estado);
        List<ComboBoxWrapper<Caja>> l = new ArrayList<ComboBoxWrapper<Caja>>(list.size());
        for (Caja o : list) {
            l.add(new ComboBoxWrapper<Caja>(o, o.getId(), o.getNombre()));
        }
        return l;
    }

    /**
     * Recupera las sucursales a las cuales el usuario actual tiene acceso
     *
     * @return
     */
    List<ComboBoxWrapper<Sucursal>> getWrappedSucursales() {
        List<Sucursal> list = getSucursales();
        List<ComboBoxWrapper<Sucursal>> l = new ArrayList<ComboBoxWrapper<Sucursal>>(list.size());
        for (Sucursal o : list) {
            l.add(new ComboBoxWrapper<Sucursal>(o, o.getId(), o.getNombre()));
        }
        return l;
    }
}
