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
        usuario = UsuarioJpaController.getCurrentUser();
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

    List<Caja> getCajas(Boolean estado) {
        return new CajaController().findCajasPermitidasByUsuario(usuario, estado);
    }

    List<ComboBoxWrapper<Sucursal>> getWrappedSucursales() {
        List<Sucursal> sucursales = getSucursales();
        List<ComboBoxWrapper<Sucursal>> l = new ArrayList<ComboBoxWrapper<Sucursal>>(sucursales.size());
        for (Sucursal sucursal : sucursales) {
            l.add(new ComboBoxWrapper<Sucursal>(sucursal, sucursal.getId(), sucursal.getNombre()));
        }
        return l;
    }
}
