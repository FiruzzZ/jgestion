package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.entity.Cuenta;
import jgestion.entity.SubCuenta;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMSubCuenta;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.persistence.RollbackException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.Wrapper;
import jgestion.jpa.controller.SubCuentaJpaController;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class SubCuentaController {

    private static final Logger LOG = Logger.getLogger(SubCuentaController.class.getName());
    private SubCuentaJpaController jpaController;
    private SubCuenta EL_OBJECT;

    public SubCuentaController() {
        jpaController = new SubCuentaJpaController();
    }

    private void initABM(Window owner, boolean editing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        List<Cuenta> l = new CuentaController().findAll();
        if (l.isEmpty()) {
            throw new MessageException("Para poder crear Sub Cuentas, primero debe crear al menos una Cuenta");
        }
        if (editing) {
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
            EL_OBJECT = jpaController.find(EL_OBJECT.getId());
        }
        final PanelABMSubCuenta panelABMSubCuenta = new PanelABMSubCuenta();
        UTIL.loadComboBox(panelABMSubCuenta.getCbCuenta(), new Wrapper<Cuenta>().getWrapped(l), false);
        if (editing) {
            UTIL.setSelectedItem(panelABMSubCuenta.getCbCuenta(), EL_OBJECT.getCuenta().getNombre());
            panelABMSubCuenta.getTfNombre().setText(EL_OBJECT.getNombre());
        }
        final JDABM abm = new JDABM(owner, "ABM Sub Cuentas", true, panelABMSubCuenta);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                if (EL_OBJECT == null) {
                    EL_OBJECT = new SubCuenta();
                }
                EL_OBJECT.setNombre(panelABMSubCuenta.getTfNombre().getText().trim());
                EL_OBJECT.setCuenta(((EntityWrapper<Cuenta>) panelABMSubCuenta.getCbCuenta().getSelectedItem()).getEntity());
                try {
                    abm.getbAceptar().setEnabled(false);
                    checkConstraints(EL_OBJECT);
                    //persistiendo......
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        jpaController.persist(EL_OBJECT);
                        msg = " creada";
                    } else {
                        jpaController.merge(EL_OBJECT);
                        msg = " editada";
                    }
                    JOptionPane.showMessageDialog(abm, jpaController.getEntityClass().getSimpleName() + " " + msg);
                    abm.dispose();
                    EL_OBJECT = null;
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
                    Logger.getLogger(CuentaController.class.getName()).log(Level.ERROR, null, ex);
                } finally {
                    abm.getbAceptar().setEnabled(true);
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
        abm.setLocationRelativeTo(owner);
        abm.setVisible(true);
    }

    public void initContenedor(Window owner) {
        final JDContenedor contenedor = new JDContenedor(owner, true, "ABM Sub Cuentas");
        contenedor.getbImprimir().setVisible(false);
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = null;
                    initABM(contenedor, false);
                    EL_OBJECT = null;
                    cargarContenedor(contenedor.getDTM(), null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex, ex);
                }
            }
        });
        contenedor.getbModificar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        EL_OBJECT = jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                    } else {
                        EL_OBJECT = null;
                    }
                    initABM(contenedor, true);
                    EL_OBJECT = null;
                    cargarContenedor(contenedor.getDTM(), null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex, ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        destroy(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                        cargarContenedor(contenedor.getDTM(), null);
                    } else {
                        throw new MessageException("No hay " + jpaController.getEntityClass().getSimpleName() + " seleccionada");
                    }
                    JOptionPane.showMessageDialog(contenedor, "Registro eliminado");
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (NonexistentEntityException ex) {
                    contenedor.showMessage("El registro que intenta borrar ya no existe mas capo..", jpaController.getEntityClass().getSimpleName(), 0);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), new String[]{"id", "Cuenta", "Sub Cuenta"}, new int[]{1, 100, 100});
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedor(contenedor.getDTM(), null);
        contenedor.setLocationRelativeTo(owner);
        contenedor.setVisible(true);
    }

    public void destroy(Integer id) throws NonexistentEntityException, MessageException {
        try {
            jpaController.remove(jpaController.find(id));
        } catch (RollbackException ex) {
            if (ex.getCause() instanceof DatabaseException) {
                PSQLException ps = (PSQLException) ex.getCause().getCause();
                if (ps.getMessage().contains("viola la llave foránea")) {
                    throw new MessageException("No se puede eliminar porque existen otros registros que están relacionados a este");
                }
            }
            throw ex;
        }
    }

    private void cargarContenedor(DefaultTableModel dtm, String query) {
        dtm.setRowCount(0);
        if (query == null || query.isEmpty()) {
            query = "";
        }
        List<SubCuenta> l = jpaController.findAll("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.nombre LIKE '%" + query + "%' ORDER BY o.nombre, o.cuenta.nombre");
        for (SubCuenta o : l) {
            dtm.addRow(new Object[]{o.getId(), o.getCuenta().getNombre(), o.getNombre()});
        }
    }

    private void checkConstraints(SubCuenta o) throws MessageException {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id <> " + o.getId() + " AND ";
        }
        if (!jpaController.findAll("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + " WHERE " + idQuery + " UPPER(o.nombre)='" + o.getNombre().toUpperCase() + "' AND o.cuenta.id=" + o.getCuenta().getId()).isEmpty()) {
            throw new MessageException("Ya existe una " + jpaController.getEntityClass().getSimpleName() + " con el nombre \"" + o.getNombre() + "\" en la Cuenta: " + o.getCuenta().getNombre());
        }
    }
}
