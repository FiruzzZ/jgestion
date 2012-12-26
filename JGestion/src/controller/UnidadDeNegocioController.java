package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Sucursal;
import entity.UnidadDeNegocio;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMUnidadDeNegocio;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jpa.controller.SucursalJpaController;
import jpa.controller.UnidadDeNegocioJpaController;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class UnidadDeNegocioController {

    private static final Logger LOG = Logger.getLogger(UnidadDeNegocioController.class.getName());
    private UnidadDeNegocioJpaController jpaController;
    private UnidadDeNegocio EL_OBJECT;
    private JDABM abm;
    private JDContenedor contenedor;
    private PanelABMUnidadDeNegocio panelABMUnidadDeNegocio;

    public UnidadDeNegocioController() {
        jpaController = new UnidadDeNegocioJpaController();
    }

    public void initContenedor(JFrame owner, boolean modal, boolean modoBuscador) {
        contenedor = new JDContenedor(owner, modal, "ABM - Unidad de Negocio");
        contenedor.getTfFiltro().setToolTipText("Filtra por nombre");
        contenedor.setModoBuscador(modoBuscador);
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = null;
                    initABM(false);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    LOG.error(ex, ex);
                }
            }
        });
        contenedor.getbModificar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = getSelectedFromContenedor();
                    initABM(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), null, 0);
                    LOG.error(ex, ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.DATOS_GENERAL);
                    jpaController.remove(EL_OBJECT);
                    contenedor.showMessage("Producto eliminado..", null, 1);
                } catch (IllegalArgumentException ex) {
                    contenedor.showMessage(ex.getMessage(), null, 2);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error(ex, ex);
                    contenedor.showMessage(ex.getMessage(), null, 0);
                }
            }
        });
//        contenedor.getbImprimir().addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    doReportProductList();
//                } catch (MessageException ex) {
//                    contenedor.showMessage(ex.getMessage(), "Advertencia", 2);
//                } catch (Exception ex) {
//                    contenedor.showMessage(ex.getMessage(), "Error REPORT!", 0);
//                }
//            }
//        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), new String[]{"ID", "Nombre", "Sucursales"}, new int[]{1, 80, 300});
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        cargarContenedorTabla();
//        contenedor.setListener(this);
        contenedor.setVisible(true);
    }

    private void initABM(boolean isEditing) throws MessageException, IOException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.ABM_PRODUCTOS);
        panelABMUnidadDeNegocio = new PanelABMUnidadDeNegocio();
        List<Sucursal> sucursales = new SucursalJpaController().findAll();
        DefaultTableModel dtm = (DefaultTableModel) panelABMUnidadDeNegocio.getjTable1().getModel();
        dtm.setRowCount(0);
        for (Sucursal sucursal : sucursales) {
            dtm.addRow(new Object[]{sucursal, sucursal.getNombre(), false});
        }
        if (isEditing) {
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(contenedor, "ABM Unidad de Negocio", true, panelABMUnidadDeNegocio);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UnidadDeNegocio o = setEntity();
                    checkConstraints(EL_OBJECT);
                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                    //persistiendo......
                    if (EL_OBJECT.getId() == null) {
                        jpaController.create(EL_OBJECT);
                    } else {
                        o.setId(EL_OBJECT.getId());
                        jpaController.merge(o);
                    }
                    abm.showMessage(msg, "Unidad de Negocio", 1);
                    cargarContenedorTabla();
                    EL_OBJECT = null;
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EL_OBJECT = null;
                abm.dispose();
            }
        });
        abm.setLocationRelativeTo(contenedor);
        abm.setVisible(true);
    }

    private UnidadDeNegocio setEntity() {
        if (EL_OBJECT == null) {
            EL_OBJECT = new UnidadDeNegocio();
//            EL_OBJECT.setSucursales(new HashSet<Sucursal>());
        }
        EL_OBJECT.setNombre(panelABMUnidadDeNegocio.getTfNombre().getText().trim());
        DefaultTableModel dtm = (DefaultTableModel) panelABMUnidadDeNegocio.getjTable1().getModel();
        Set<Sucursal> selected = new HashSet<Sucursal>();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            if ((Boolean) dtm.getValueAt(row, 2)) {
                Sucursal s = (Sucursal) dtm.getValueAt(row, 0);
                selected.add(s);
            }
        }
        EL_OBJECT.setSucursales(selected);
        return EL_OBJECT;
    }

    private void checkConstraints(UnidadDeNegocio o) throws MessageException {
        List<UnidadDeNegocio> list = jpaController.findByQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE UPPER(o.nombre)='" + o.getNombre().toUpperCase() + "'");
        if (!list.isEmpty()) {
            if (list.get(0).getId() != o.getId()) {
                throw new MessageException("Ya existe una Unidad de Negocio con este nombre");
            }
        }
        if (o.getSucursales().isEmpty()) {
            throw new MessageException("Debe seleccionar al menos una Sucursal para la Unidad de Negocios");
        }
    }

    private void cargarContenedorTabla() {
        DefaultTableModel dtm = (DefaultTableModel) contenedor.getjTable1().getModel();
        dtm.setRowCount(0);
        List<UnidadDeNegocio> l = jpaController.findAll();
        for (UnidadDeNegocio un : l) {
            String sucus = "";
            for (Sucursal sucursal : un.getSucursales()) {
                sucus += sucursal.getNombre() + ", ";
            }
            sucus = sucus.substring(0, sucus.length() - 2);
            dtm.addRow(new Object[]{un.getId(), un.getNombre(), sucus});
        }
    }

    private UnidadDeNegocio getSelectedFromContenedor() {
        try {
            Integer selectedRow = contenedor.getjTable1().getSelectedRow();
            if (selectedRow > -1) {
                return jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
            } else {
                return null;
            }
        } finally {
            jpaController.closeEntityManager();
        }
    }

    private void setPanel(UnidadDeNegocio EL_OBJECT) {
        panelABMUnidadDeNegocio.getTfNombre().setText(EL_OBJECT.getNombre());
        DefaultTableModel dtm = (DefaultTableModel) panelABMUnidadDeNegocio.getjTable1().getModel();
        for (Sucursal sucursal : EL_OBJECT.getSucursales()) {
            for (int row = 0; row < dtm.getRowCount(); row++) {
                Sucursal s = (Sucursal) dtm.getValueAt(row, 0);
                if (sucursal.equals(s)) {
                    dtm.setValueAt(Boolean.TRUE, row, 2);
                    break;
                }
            }
        }
    }
}
