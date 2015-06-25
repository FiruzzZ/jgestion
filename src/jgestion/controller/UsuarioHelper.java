package jgestion.controller;

import jgestion.entity.Caja;
import jgestion.entity.Sucursal;
import jgestion.entity.Usuario;
import java.util.ArrayList;
import java.util.List;
import utilities.general.EntityWrapper;

/**
 *
 * @author Administrador
 */
public class UsuarioHelper {

    private Usuario usuario;

    public UsuarioHelper() {
        usuario = UsuarioController.getCurrentUser();
        refresh();
    }

    public UsuarioHelper(Usuario usuario) {
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

    /**
     * Recupera las sucursales a las cuales el usuario actual tiene acceso
     *
     * @return
     */
    public List<Sucursal> getSucursales() {
        return new UsuarioController().getSucursalesOrderedByNombre(usuario);
    }

    /**
     * Recupera las cajas a las cuales el usuario actual tiene acceso
     *
     * @param estado
     * @return
     */
    public List<Caja> getCajas(Boolean estado) {
        return new CajaController().findCajasPermitidasByUsuario(usuario, estado);
    }

}
