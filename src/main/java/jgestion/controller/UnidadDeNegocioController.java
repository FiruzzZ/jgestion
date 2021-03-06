package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Sucursal;
import jgestion.entity.UnidadDeNegocio;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMUnidadDeNegocio;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.jpa.controller.SucursalJpaController;
import jgestion.jpa.controller.UnidadDeNegocioJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class UnidadDeNegocioController {

    private static final Logger LOG = LogManager.getLogger();
    private final UnidadDeNegocioJpaController jpaController;
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
                    UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
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
        UTIL.getDefaultTableModel(contenedor.getjTable1(), new String[]{"ID", "Nombre", "Sucursales"}, new int[]{1, 80, 300});
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        cargarContenedorTabla();
//        contenedor.setListener(this);
        contenedor.setVisible(true);
    }

    private void initABM(boolean isEditing) throws MessageException, IOException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
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

                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";

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

    private UnidadDeNegocio setEntity() throws MessageException {
        String nombre = panelABMUnidadDeNegocio.getTfNombre().getText().trim();
        if (nombre.isEmpty()) {
            throw new MessageException("Nombre no válido");
        } else {
            if (!UTIL.VALIDAR_REGEX(UTIL.REGEX_ALFANUMERIC_WITH_WHITE, nombre)) {
                throw new MessageException("Nombre solo puede contener caracteres alfanuméricos");
            }
        }
        if (EL_OBJECT == null) {
            EL_OBJECT = new UnidadDeNegocio();
        }
        EL_OBJECT.setNombre(nombre);
        DefaultTableModel dtm = (DefaultTableModel) panelABMUnidadDeNegocio.getjTable1().getModel();
        Set<Sucursal> selected = new HashSet<>();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            if ((Boolean) dtm.getValueAt(row, 2)) {
                Sucursal s = (Sucursal) dtm.getValueAt(row, 0);
                selected.add(s);
            }
        }
        EL_OBJECT.setSucursales(selected);
        checkConstraints(EL_OBJECT);
        if (EL_OBJECT.getId() == null) {
            jpaController.persist(EL_OBJECT);
        } else {
            EL_OBJECT.setId(EL_OBJECT.getId());
            jpaController.merge(EL_OBJECT);
        }
        return EL_OBJECT;
    }

    private void checkConstraints(UnidadDeNegocio o) throws MessageException {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id <> " + o.getId() + " AND ";
        }
        if (!jpaController.findAll("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o"
                + " WHERE " + idQuery + " UPPER(o.nombre)='" + o.getNombre().toUpperCase() + "'").isEmpty()) {
            throw new MessageException("Ya existe una Unidad de Negocio con este nombre");
        }
        if (o.getSucursales().isEmpty()) {
            throw new MessageException("Debe seleccionar al menos una Sucursal para la Unidad de Negocios");
        }
        jpaController.closeEntityManager();
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
