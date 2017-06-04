package jgestion.controller;

import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.entity.Cuenta;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMCuenta;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.jpa.controller.CuentaJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class CuentaController {

    private static final Logger LOG = LogManager.getLogger();
    private final CuentaJpaController jpaController = new CuentaJpaController();
    private Cuenta EL_OBJECT;
    public static final Cuenta SIN_CLASIFICAR; //antes llamado EFECTIVO

    static {
        SIN_CLASIFICAR = DAO.getEntityManager().find(Cuenta.class, 1);
    }

    public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
        ArrayList<String> lis;
        Cuenta cuenta;
        try {
            cuenta = jpaController.find(id);
            cuenta.getId();
        } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("No existe el registro.", enfe);
        }
        if (!jpaController.findAll("SELECT o.id FROM " + DetalleCajaMovimientos.class.getSimpleName() + " o WHERE o.cuenta.id=" + cuenta.getId()).isEmpty()) {
            lis = new ArrayList<String>(1);
            lis.add("No se puede eliminar el registro " + jpaController.getEntityClass().getSimpleName() + " porque está relacionado a otro/s registro/s.");
            throw new IllegalOrphanException(lis);
        }
        jpaController.remove(cuenta);
    }

    public List<Cuenta> findAll() {
        return jpaController.findAll();
    }

    public Cuenta findMovimientoConcepto(Integer id) {
        try {
            return jpaController.find(id);
        } finally {
            jpaController.closeEntityManager();
        }
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
        UTIL.getDefaultTableModel(contenedor.getjTable1(), new String[]{"id", "Cuenta", "Ingreso"}, new int[]{1, 100, 100}, new Class<?>[]{null, null, Boolean.class});
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedor(contenedor.getDTM(), null);
        contenedor.setLocationRelativeTo(owner);
        contenedor.setVisible(true);
    }

    private void cargarContenedor(DefaultTableModel dtm, String query) {
        dtm.setRowCount(0);
        if (query == null || query.isEmpty()) {
            query = "";
        }
        List<Cuenta> l = jpaController.findAll("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.id > 1 AND o.nombre LIKE '%" + query + "%' ORDER BY o.nombre");
        for (Cuenta o : l) {
            dtm.addRow(new Object[]{o.getId(), o.getNombre(), o.isIngreso()});
        }
    }

    /**
     * Init UI ABM de MovimientoConcepto
     *
     * @param owner owner, patern bla bla
     * @throws MessageException End-User messages
     */
    private void initABM(Window owner, boolean editing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        if (editing) {
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
            EL_OBJECT = jpaController.find(EL_OBJECT.getId());
        }
        final PanelABMCuenta panelABM = new PanelABMCuenta();
        if (editing) {
            panelABM.getTfNombre().setText(EL_OBJECT.getNombre());
            panelABM.getCheckIngresos().setSelected(EL_OBJECT.isIngreso());
        }
        final JDABM abm = new JDABM(owner, "ABM Cuentas", true, panelABM);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (EL_OBJECT == null) {
                    EL_OBJECT = new Cuenta();
                }
                EL_OBJECT.setNombre(panelABM.getTfNombre().getText().trim().toUpperCase());
                EL_OBJECT.setIngreso(panelABM.getCheckIngresos().isSelected());
                try {
                    abm.getbAceptar().setEnabled(false);
                    checkConstraints(EL_OBJECT);
                    //persistiendo......
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        jpaController.persist(EL_OBJECT);
                        msg = " creado";
                    } else {
                        jpaController.merge(EL_OBJECT);
                        msg = " editado";
                    }
                    JOptionPane.showMessageDialog(abm, jpaController.getEntityClass().getSimpleName() + msg);
                    EL_OBJECT = null;
                    abm.dispose();
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
                    LOG.error(ex, ex);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
                    LOG.error(ex, ex);
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
        abm.setVisible(true);
    }

    private void checkConstraints(Cuenta o) throws MessageException, Exception {
        if (o.getNombre().isEmpty()) {
            throw new MessageException("Nombre no válido.");
        }
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id <> " + o.getId() + " AND ";
        }

        try {
            DAO.getEntityManager().createQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                    + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "'").getSingleResult().equals(o.getClass());
            throw new MessageException("Ya existe un registro con el nombre \"" + o.getNombre() + "\"");
        } catch (NoResultException ex) {
        }
    }

    public List<Cuenta> findByTipo(boolean ingreso) {
        return jpaController.findByTipo(ingreso);
    }
}
