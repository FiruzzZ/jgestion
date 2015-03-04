package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Vendedor;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMVendedor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.jpa.controller.VendedorJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class VendedorController implements ActionListener {

    private static final Logger LOG = Logger.getLogger(VendedorController.class.getName());
    private final String[] colsName = {"ID", "Apellido", "Nombre", "Dirección", "Teléfonos"};
    private final int[] colsWidth = {1, 50, 50, 100, 100};
    private JDContenedor contenedor;
    private JDABM abm;
    private Vendedor EL_OBJECT;
    private PanelABMVendedor panelABM;
    private final VendedorJpaController jpaController;

    public VendedorController() {
        jpaController = new VendedorJpaController();
    }

    public JDialog initContenedor(Window frame, boolean modal) {
        contenedor = new JDContenedor(frame, modal, "Administrador - " + jpaController.getEntityClass().getSimpleName());
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }

            private void armarQuery(String filtro) {
                String query = null;
                if (filtro != null && filtro.length() > 0) {
                    query = "SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o"
                            + " WHERE UPPER(o.apellido) LIKE '%" + filtro.toUpperCase() + "%'"
                            + " OR UPPER(o.nombre) LIKE '%" + filtro.toUpperCase() + "%'";
                }
                cargarContenedorTabla(query);
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedorTabla(null);
        contenedor.setListener(this);
        return contenedor;
    }

    private void cargarContenedorTabla(String query) {
        DefaultTableModel dtm = contenedor.getDTM();
        dtm.setRowCount(0);
        List<Vendedor> l;
        if (query == null) {
            l = jpaController.findAll();
        } else {
            l = jpaController.findAll(query);
        }

        for (Vendedor o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getApellido(),
                o.getNombre(),
                o.getDireccion(),
                ((o.getTele1() != null) ? o.getTele1().toString() : "-")
                + ((o.getTele2() != null) ? o.getTele2().toString() : "-")
            });
        }
    }

    private void initABM(boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }
        panelABM = new PanelABMVendedor();
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(contenedor, "ABM " + jpaController.getEntityClass().getSimpleName() + "es", true, panelABM);
        abm.setListener(this);
        abm.setLocationRelativeTo(contenedor);
        abm.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            try { //global catch block
                if (boton.equals(contenedor.getbNuevo())) {
                    EL_OBJECT = null;
                    initABM(false);
                } else if (boton.equals(contenedor.getbModificar())) {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        EL_OBJECT = jpaController.find((Integer) contenedor.getDTM().getValueAt(selectedRow, 0));
                    } else {
                        EL_OBJECT = null;
                    }
                    initABM(true);

                } else if (boton.equals(contenedor.getbBorrar())) {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        destroy(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                        cargarContenedorTabla(null);
                    } else {
                        throw new MessageException("No hay " + jpaController.getEntityClass().getSimpleName() + " seleccionado");
                    }
                    JOptionPane.showMessageDialog(contenedor, "Registro eliminado");
                } else if (boton.getName().equalsIgnoreCase("Print")) {
                    //no implementado aun...
                } else if (boton.getName().equalsIgnoreCase("exit")) {
                    contenedor.dispose();
                    contenedor = null;
                } else if (boton.equals(abm.getbAceptar())) {
                    setEntity();
                    checkConstraints(EL_OBJECT);
                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                    //persistiendo......
                    if (EL_OBJECT.getId() == null) {
                        jpaController.persist(EL_OBJECT);
                    } else {
                        jpaController.merge(EL_OBJECT);
                    }
                    abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                    cargarContenedorTabla(null);
                    abm.dispose();
                } else if (boton.equals(abm.getbCancelar())) {
                    abm.dispose();
                    panelABM = null;
                    abm = null;
                    EL_OBJECT = null;
                }
            } catch (MessageException ex) {
                contenedor.showMessage(ex.getMessage(), null, 2);
            } catch (Exception ex) {
                contenedor.showMessage(ex.getMessage(), null, 0);
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }// </editor-fold>
    }

    private void setPanel(Vendedor o) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("apellido", o.getApellido());
        data.put("nombre", o.getNombre());
        data.put("direccion", o.getDireccion());
        data.put("tele1", o.getTele1() == null ? null : o.getTele1().toString());
        data.put("tele2", o.getTele2() == null ? null : o.getTele2().toString());
        data.put("email", o.getEmail());
        data.put("observacion", o.getObservacion());
        data.put("activo", o.getActivo());
        panelABM.setData(data);
    }

    private void setEntity() throws MessageException {
        Long tele1 = null, tele2 = null;
        Map<String, Object> data = panelABM.getData();
        if (data.get("apellido").toString().isEmpty()) {
            throw new MessageException("Apellido no válido");
        }
        if (data.get("nombre").toString().isEmpty()) {
            throw new MessageException("Nombre no válido");
        }
        try {
            if (!data.get("tele1").toString().isEmpty()) {
                tele1 = Long.valueOf(data.get("tele1").toString());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 1 no válido");
        }
        try {
            if (!data.get("tele2").toString().isEmpty()) {
                tele2 = Long.valueOf(data.get("tele2").toString());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 2 no válido");
        }

        if (EL_OBJECT == null) {
            EL_OBJECT = new Vendedor();
        }
        EL_OBJECT.setApellido(data.get("apellido").toString());
        EL_OBJECT.setNombre(data.get("nombre").toString());
        EL_OBJECT.setDireccion(data.get("direccion").toString().isEmpty() ? null : data.get("direccion").toString());
        EL_OBJECT.setEmail(data.get("email").toString().isEmpty() ? null : data.get("email").toString());
        EL_OBJECT.setObservacion(data.get("observacion").toString().isEmpty() ? null : data.get("observacion").toString());
        EL_OBJECT.setTele1(tele1);
        EL_OBJECT.setTele1(tele2);
        EL_OBJECT.setActivo((Boolean) data.get("activo"));
    }

    private void checkConstraints(Vendedor EL_OBJECT) {
    }

    private void destroy(Integer id) throws MessageException {
        Vendedor toDelete = jpaController.find(id);
        if (toDelete == null) {
            throw new MessageException("El vendedor que intenta borrar no existe mas");
        }
        if (!toDelete.getFacturaVentas().isEmpty() || !toDelete.getRemitos().isEmpty()) {
            throw new MessageException("No se puede este Vendedor porque registros relacionados");
        }
        jpaController.remove(toDelete);
        jpaController.closeEntityManager();
    }
}
