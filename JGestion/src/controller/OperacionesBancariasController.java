package controller;

import controller.exceptions.MessageException;
import entity.OperacionesBancarias;
import gui.JDMiniABM;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jpa.controller.OperacionesBancariasJpaController;
import org.apache.log4j.Logger;
import org.omg.CORBA.TRANSACTION_MODE;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class OperacionesBancariasController {

    private static final Logger LOG = Logger.getLogger(OperacionesBancariasController.class.getName());
    private OperacionesBancariasJpaController jpaController;
    private OperacionesBancarias selectedEntity;
    private JDMiniABM abm;
    public static final int DEPOSITO = 1;
    public static final int EXTRACCION = 2;
    public static final int TRANSFERENCIA = 3;

    public OperacionesBancariasController() {
        jpaController = new OperacionesBancariasJpaController();
    }

    /**
     *
     * @param defaultOB
     * {@link #DEPOSITO}, {@link #TRASNFERENCIA}, {@link #EXTRACIION}
     * @return
     */
    public OperacionesBancarias getOperacion(int defaultOB) {
        if (defaultOB != DEPOSITO && defaultOB != TRANSFERENCIA && defaultOB != EXTRACCION) {
            throw new IllegalArgumentException("default " + jpaController.getEntityClass().getSimpleName() + " no válida");
        }
        return jpaController.find(defaultOB);
    }

    /**
     * Init UI ABM de MovimientoConcepto
     *
     * @param owner owner, patern bla bla
     * @throws MessageException End-User messages
     */
    public void initUIABM(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        abm = new JDMiniABM(owner, true);
        abm.hideBtnLock();
        abm.hideFieldCodigo();
        abm.hideFieldExtra();
        abm.setVisibleTaInformacion(false);
        abm.pack();
        abm.setTitle("ABM - Operaciones Bancarias");
        UTIL.getDefaultTableModel(abm.getjTable1(),
                new String[]{"Nº", "Nombre"},
                new int[]{10, 150});
        cargarDTM(abm.getDTM(), null);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedEntity == null) {
                    selectedEntity = new OperacionesBancarias();
                }
                selectedEntity.setNombre(abm.getTfNombre());
                try {
                    abm.getbAceptar().setEnabled(false);
                    checkConstraints(selectedEntity);
                    //persistiendo......
                    String msg;
                    if (selectedEntity.getId() == null) {
                        jpaController.create(selectedEntity);
                        msg = " creado";
                    } else {
                        jpaController.merge(selectedEntity);
                        msg = " editado";
                    }
                    JOptionPane.showMessageDialog(abm, msg);
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), null);
                    selectedEntity = null;
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.WARNING_MESSAGE);
                    LOG.error(ex, ex);
                } finally {
                    abm.getbAceptar().setEnabled(true);
                }
            }
        });
        abm.getbEliminar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(abm, "¿Confirma la eliminación del registro?", null, JOptionPane.YES_NO_OPTION)) {
                    try {
                        abm.getbEliminar().setEnabled(false);
                        jpaController.remove(selectedEntity);
                        selectedEntity = null;
                        abm.clearPanelFields();
                        cargarDTM(abm.getDTM(), null);
                        JOptionPane.showMessageDialog(abm, "Eliminado..");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), ex.getClass().toString(), JOptionPane.ERROR_MESSAGE);
                        LOG.error(ex, ex);
                    } finally {
                        abm.getbEliminar().setEnabled(true);
                    }
                }
            }
        });
        abm.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    selectedEntity = jpaController.find(Integer.valueOf((abm.getjTable1().getModel().getValueAt(selectedRow, 0)).toString()));
                    if (selectedEntity != null) {
                        abm.setFields(selectedEntity.getNombre(), null, null);
                    }
                }
            }
        });
        abm.setVisible(true);
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        List<OperacionesBancarias> list;
        if (query == null || query.isEmpty()) {
            list = jpaController.findAll();
        } else {
            // para cuando se usa el Buscador del ABM
            list = jpaController.findByNativeQuery(query);
        }

        for (OperacionesBancarias o : list) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre()});
        }
    }

    private void checkConstraints(OperacionesBancarias o) throws MessageException, Exception {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id <> " + o.getId() + " AND ";
        }

        if (!jpaController.findByQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "'").isEmpty()) {
            throw new MessageException("Ya existe un registro con el nombre \"" + o.getNombre() + "\"");
        }
    }
}
