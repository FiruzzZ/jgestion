package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.ListaPrecios;
import jgestion.entity.DetalleListaPrecios;
import jgestion.entity.Rubro;
import utilities.general.UTIL;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMListaPrecio;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.persistence.NoResultException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.controller.exceptions.ConstraintViolationJpaException;
import jgestion.jpa.controller.ListaPreciosJpaController;
import jgestion.jpa.controller.RubroJpaController;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class ListaPreciosController {

    public static final String CLASS_NAME = "ListaPrecios";
    private final String[] colsName = {"id", "Nombre", "M. General", "Margen (%)", "Cat. Web"};
    private final int[] colsWidth = {10, 150, 20, 20, 20};
    private final Class[] colsClass = {Object.class, Object.class, Object.class, Object.class, Boolean.class};
    private JDContenedor contenedor = null;
    private JDABM abm;
    private PanelABMListaPrecio panel;
    private ListaPrecios EL_OBJECT;
    private List<Rubro> listaDeRubros;
    private final ListaPreciosJpaController dao = new ListaPreciosJpaController();

    public ListaPreciosController() {
    }

    public void initContenedor(Window frame, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_LISTA_PRECIOS);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>
        contenedor = new JDContenedor(frame, modal, "ABM Lista de Precios");
        contenedor.hideBtmImprimir();
        contenedor.setSize(570 + 80, 300 + 50);
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth, colsClass);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedor();
        contenedor.getbNuevo().addActionListener(evt -> {
            try {
                EL_OBJECT = new ListaPrecios();
                initABM();
                abm.setVisible(true);
            } catch (MessageException ex) {
                ex.displayMessage(null);
            } catch (Exception ex) {
                LogManager.getLogger();
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
            }
        });
        contenedor.getbModificar().addActionListener(evt -> {
            try {
                EL_OBJECT = (ListaPrecios) UTIL.getSelectedValueFromModel(contenedor.getjTable1(), 0);
                if (EL_OBJECT == null) {
                    JOptionPane.showConfirmDialog(contenedor, "Debe seleccionar un registro", null, JOptionPane.WARNING_MESSAGE);
                    return;
                }
                initABM();
                setUI(EL_OBJECT);
                abm.setVisible(true);
            } catch (MessageException ex) {
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
                LogManager.getLogger();
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
            }
        });
        contenedor.getbBorrar().addActionListener(evt -> {
            try {
                EL_OBJECT = (ListaPrecios) UTIL.getSelectedValueFromModel(contenedor.getjTable1(), 0);
                if (EL_OBJECT == null) {
                    JOptionPane.showConfirmDialog(contenedor, "Debe seleccionar un registro", null, JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dao.remove(EL_OBJECT);
                contenedor.showMessage("Lista de precios eliminada", CLASS_NAME, 1);
                cargarContenedor();
            } catch (ConstraintViolationJpaException ex) {
                JOptionPane.showMessageDialog(contenedor, ex.getMessage());
            } catch (Exception ex) {
                LogManager.getLogger();
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
            }
        });
        contenedor.getbImprimir().setVisible(false);
        contenedor.setVisible(true);
    }

    private void cargarContenedor() {
        DefaultTableModel dtm = contenedor.getDTM();
        dtm.setRowCount(0);
        List<ListaPrecios> l = dao.findAll(dao.getSelectFrom() + " WHERE upper(o.nombre) like '%" + contenedor.getTfFiltro().getText().toUpperCase() + "%'");
        l.forEach(o -> {
            dtm.addRow(new Object[]{
                o,
                o.getNombre(),
                o.getMargenGeneral() ? "Si" : "No",
                o.getMargen().toString(),
                o.getParaCatalogoWeb()
            });
        });
    }

    private void initABM() throws MessageException {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_LISTA_PRECIOS);
        } catch (MessageException ex) {
            ex.displayMessage(null);
            return;
        }// </editor-fold>

        //lista usada para comparación entre tablas (Rubros y RubrosAfectados)
        listaDeRubros = new RubroJpaController().findAll();
        panel = new PanelABMListaPrecio();
        panel.getBtnADD().addActionListener(evt -> {
            try {
                addRubro();
            } catch (MessageException ex) {
                ex.displayMessage(null);
            } catch (Exception ex) {
                abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            }
        });
        panel.getBtnDEL().addActionListener(evt -> {
            delRubro();
        });
        UTIL.getDefaultTableModel(
                panel.getjTable1(),
                new String[]{"Object Rubro", "Rubro"},
                new int[]{5, 200});
        UTIL.hideColumnTable(panel.getjTable1(), 0);
        UTIL.getDefaultTableModel(
                panel.getjTable2(),
                new String[]{"Object Rubro", "Rubro", "Margen %"},
                new int[]{5, 80, 40});
        UTIL.hideColumnTable(panel.getjTable2(), 0);

        cargarRubros();
        abm = new JDABM(contenedor, "ABM " + CLASS_NAME, true, panel);
        abm.setLocationRelativeTo(null);
        abm.getbAceptar().addActionListener(evt -> {
            try {
                String msg = EL_OBJECT.getId() == null ? "creada" : "modificada";
                setEntity();
                String msj = "Lista de precios " + msg;
                checkConstraints(EL_OBJECT);
                abm.showMessage(msj, CLASS_NAME, 1);
                cargarContenedor();
                if (EL_OBJECT.getId() != null) {
                    abm.dispose();
                }
            } catch (MessageException ex) {
                abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
                LogManager.getLogger();
                abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
            }
        });
        abm.getbCancelar().addActionListener(evt -> {
            abm.dispose();
            panel = null;
            abm = null;
            EL_OBJECT = null;
        });
    }

    private void setUI(ListaPrecios listaPrecios) {
        if (listaPrecios.getMargenGeneral()) {
            panel.setTfMargenGeneral(String.valueOf(listaPrecios.getMargen()));
        } else {
            panel.setTfMargenGeneral("");
        }
        panel.getCheckMargenGeneral().setSelected(listaPrecios.getMargenGeneral());
        panel.setTfNombre(listaPrecios.getNombre());
        panel.setTfMargenGeneral(listaPrecios.getMargen().toString());
        panel.setCheckCatalagoWEB(listaPrecios.getParaCatalogoWeb());
        cargarRubrosAfectados(listaPrecios.getDetalleListaPreciosList());
    }

    private void setEntity() throws MessageException {
        String nombre = StringUtils.trimToNull(panel.getTfNombre().toUpperCase());
        if (nombre.length() < 1) {
            throw new MessageException("Nombre no válido");
        }

        if (panel.getCheckMargenGeneral().isSelected()) {
            try {
                if (Double.valueOf(panel.getTfMargenGeneral()) < 0) {
                    throw new MessageException("Margen general no puede ser menor a 0");
                }
            } catch (NumberFormatException e) {
                throw new MessageException("Margen general no válido");
            }
        } else if (panel.getDTMAfectados().getRowCount() < 1) {
            throw new MessageException("No hay rubros afectados en la lista de precios,"
                    + "\ndebe elegir al menos uno o un marge general.");
        }

        if (panel.getCheckCatalagoWEB().isSelected()) {
            ListaPrecios uniqueCatalogo = dao.findParaCatalogoWeb();
            if (uniqueCatalogo != null && !uniqueCatalogo.equals(EL_OBJECT)) {
                throw new MessageException("Ya existe una Lista de Precios de referencia para el Catlálogo Web.\n"
                        + uniqueCatalogo.getNombre());
            }
        }
        EL_OBJECT.setNombre(nombre);
        EL_OBJECT.setParaCatalogoWeb(panel.getCheckCatalagoWEB().isSelected());
        EL_OBJECT.setMargenGeneral(panel.getCheckMargenGeneral().isSelected());
        EL_OBJECT.setMargen(0.0);
        if (EL_OBJECT.getMargenGeneral()) {
            //si elegió margen general
            EL_OBJECT.setMargen(Double.valueOf(panel.getTfMargenGeneral()));
            EL_OBJECT.getDetalleListaPreciosList().clear();
        }
    }

    private void checkConstraints(ListaPrecios object) throws MessageException, Exception {
        if (object.getId() == null) {
            dao.persist(object);
        } else {
            dao.merge(object);
        }
    }

    private void cargarRubrosAfectados(List<DetalleListaPrecios> detalle) {
        DefaultTableModel dtm = (DefaultTableModel) panel.getjTable2().getModel();
        dtm.setRowCount(0);
        detalle.sort((o1, o2) -> o1.getRubro().getNombre().compareToIgnoreCase(o2.getRubro().getNombre()));
        detalle.forEach(detalleListaPrecios -> {
            dtm.addRow(new Object[]{
                detalleListaPrecios.getRubro(),
                detalleListaPrecios.getRubro().getNombre(),
                detalleListaPrecios.getMargen()});
        });
        cargarRubros();
    }

    private void cargarRubros() {
        DefaultTableModel dtmRubros = panel.getDTMRubros();
        dtmRubros.setRowCount(0);
        DefaultTableModel dtmRubrosAfectados = panel.getDTMAfectados();
        for (Rubro rubro : listaDeRubros) {
            boolean cargarRubroATabla = true;
            for (int i = 0; i < dtmRubrosAfectados.getRowCount(); i++) {
                if (dtmRubrosAfectados.getValueAt(i, 0).equals(rubro)) {
                    cargarRubroATabla = false;
                    break;
                }
            }
            if (cargarRubroATabla) {
                dtmRubros.addRow(new Object[]{rubro, rubro.getNombre()});
            }
        }
    }

    private void addRubro() throws MessageException {
        int selectedRow = panel.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            try {
                if (Double.valueOf(panel.getTfMargenPorRubro()) <= 0) {
                    throw new MessageException("Margen del Rubro debe ser mayor a cero");
                }
            } catch (NumberFormatException e) {
                throw new MessageException("Margen no válido");
            }
            DefaultTableModel dtmRubrosAfectados = panel.getDTMAfectados();
            Rubro rubro = (Rubro) UTIL.getSelectedValueFromModel(panel.getjTable1(), 0);
            dtmRubrosAfectados.addRow(new Object[]{
                rubro,
                rubro.getNombre(),
                panel.getTfMargenPorRubro()
            });
            EL_OBJECT.getDetalleListaPreciosList().add(new DetalleListaPrecios(Double.valueOf(panel.getTfMargenPorRubro()), EL_OBJECT, rubro));
            //quitar el rubro seleccionado de la tabla izquierda
            panel.getDTMRubros().removeRow(selectedRow);
        } else {
            throw new MessageException("Debe seleccionar un Rubro");
        }
    }

    private void delRubro() {
        int selectedRow = panel.getjTable2().getSelectedRow();
        if (selectedRow > -1) {
            Rubro rub = (Rubro) UTIL.getSelectedValueFromModel(panel.getjTable2(), 0);
            for (ListIterator<DetalleListaPrecios> iterator = EL_OBJECT.getDetalleListaPreciosList().listIterator(); iterator.hasNext();) {
                DetalleListaPrecios det = iterator.next();
                if (det.getRubro().equals(rub)) {
                    iterator.remove();
                    break;
                }
            }
            DefaultTableModel dtmRubrosAfectados = panel.getDTMAfectados();
            dtmRubrosAfectados.removeRow(selectedRow);
            cargarRubros();
        }
    }

    public ListaPrecios findListaPreciosParaCatalogo() {
        return dao.findParaCatalogoWeb();
    }
}
