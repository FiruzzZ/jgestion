package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.FacturaVenta;
import jgestion.entity.ListaPrecios;
import jgestion.entity.Producto;
import jgestion.entity.VentaSimpleConfig;
import generics.CustomABMJDialog;
import jgestion.gui.JDVentaSimple;
import jgestion.gui.PanelVentaSimpleConfig;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import jgestion.JGestion;
import jgestion.jpa.controller.JGestionJpaImpl;
import jgestion.jpa.controller.UnidadDeNegocioJpaController;

/**
 *
 * @author FiruzzZ
 */
public class VentaSimpleController {

    private JDVentaSimple ventaSimpleUI;
    private FacturaVenta EL_OBJECT;
    private Producto selectedProducto;
    private ListaPrecios selectedListaPrecios;
    private final JGestionJpaImpl<VentaSimpleConfig, Integer> jpaConfig = new JGestionJpaImpl<VentaSimpleConfig, Integer>() {
    };
    private JDVentaSimple jdVentaSimple;

    public VentaSimpleController() {
    }

    public void displayConfiguracion(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        if (new UnidadDeNegocioJpaController().findAll().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("info.unidaddenegociosempty"));
        }
        if (uh.getCajas(true).isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.caja"));
        }
        final PanelVentaSimpleConfig ui = new PanelVentaSimpleConfig();
        if (findConfig() != null) {
            ui.setConfig(findConfig());
        }
        final CustomABMJDialog abm = new CustomABMJDialog(owner, ui, "Configuración de Venta Simple", true, null);
        abm.getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                VentaSimpleConfig config = ui.getConfig();
                jpaConfig.merge(config);
                JOptionPane.showMessageDialog(abm, "Configuración guardada");
            }
        });
        abm.getBtnCancelar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(owner);
        abm.setVisible(true);
    }

    public void displayVentaSimple(Window owner) throws MessageException {
        VentaSimpleConfig config = findConfig();
        if (config == null) {
            throw new MessageException("No se ha especificado aún la configuración para las Ventas simples."
                    + "\nDatos Generales > Configuración de Ventas Simples");
        }
        /**
         * Ventana de selección de vendedor
         */
        jdVentaSimple = new JDVentaSimple(owner, true);
        jdVentaSimple.setVisible(true);
    }

    public VentaSimpleConfig findConfig() {
        return jpaConfig.find(1);
    }
}
