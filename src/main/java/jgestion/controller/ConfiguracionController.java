package jgestion.controller;

import generics.CustomABMJDialog;
import java.awt.Window;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import jgestion.controller.exceptions.MessageException;
import jgestion.gui.PanelConfiguraciones;
import jgestion.jpa.controller.ConfiguracionDAO;
import utilities.general.UTIL;
import jgestion.entity.Configuracion;

/**
 *
 * @author FiruzzZ
 */
public class ConfiguracionController {

    private final ConfiguracionDAO jpaController = new ConfiguracionDAO();

    public ConfiguracionController() {
    }

    public JDialog getABM(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.CONFIGURACION);
        PanelConfiguraciones panel = new PanelConfiguraciones();
        List<Configuracion> ll = jpaController.findAll();
        DefaultTableModel dtm = (DefaultTableModel) panel.getjTable1().getModel();
        for (Configuracion o : ll) {
            dtm.addRow(new Object[]{o.getKey(), o.getDescription(), o.getValue()});
        }
        dtm.addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                Configuracion o = jpaController.find(UTIL.getSelectedValue(panel.getjTable1(), 0).toString());
                String newValue = UTIL.getSelectedValue(panel.getjTable1(), 2).toString();
                if (!o.getValue().equals(newValue)) {
                    String d = o.getKey() + ": " + o.getValue() + " -> " + newValue;
                    o.setValue(newValue);
                    jpaController.merge(o);
                }
            }
        });
        CustomABMJDialog abm = new CustomABMJDialog(owner, panel, "Sistema - Configuración", true,
                "Configuración de propiedades del sistema\nNo tocar! (preferentemente)");
//        abm.getBtnLog().addActionListener((evt) -> {
//            new UsuariosAccionesController().getAccionViewer(abm, Configuracion.class).setVisible(true);
//        });
        abm.setResizable(true);
        abm.setToolBarVisible(false);
        abm.addBtnDisposeWindowCancelAction();
        return abm;
    }
}
