package controller;

import com.toedter.calendar.JDateChooser;
import controller.exceptions.MessageException;
import entity.Dominio;
import gui.JDABM;
import gui.JDMiniABM;
import gui.generics.GroupLayoutPanelBuilder;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import javax.persistence.RollbackException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.DominioJpaController;
import org.apache.log4j.Logger;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class DominioController implements ActionListener {
    
    private static final Logger LOG = Logger.getLogger(DominioController.class);
    private final DominioJpaController jpaController = new DominioJpaController();
    private JDMiniABM abm;
    private Dominio entity;
    
    public void getABM(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        initABM();
    }
    
    private void initABM() {
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.hideFieldCodigo();
        abm.setTitle("ABM - " + jpaController.getEntityClass().getSimpleName() + "s");
        abm.setVisibleTaInformacion(false);
        UTIL.getDefaultTableModel(abm.getjTable1(), new String[]{"Object", "Nombre"});
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        abm.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    entity = (Dominio) UTIL.getSelectedValue(abm.getjTable1(), 0);
                }
                if (entity != null) {
                    abm.setTfNombre(entity.getNombre());
                    abm.tfNombreRequestFocus();
                }
            }
        });
        cargarDTM();
        abm.setListeners(this);
        abm.setVisible(true);
    }
    
    private void cargarDTM() {
        List<Dominio> findAll = jpaController.findAll();
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        for (Dominio o : findAll) {
            dtm.addRow(new Object[]{
                o,
                o.getNombre()
            });
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            //<editor-fold defaultstate="collapsed" desc="abm Actions">
            if (abm != null) {
                if (boton.equals(abm.getbNuevo())) {
                    entity = null;
                    abm.clearPanelFields();
                } else if (boton.equals(abm.getbEliminar())) {
                    try {
                        if (entity == null) {
                            throw new MessageException("No ha seleccionado ningún registro");
                        }
                        eliminar(entity);
                        entity = null;
                        abm.clearPanelFields();
                        cargarDTM();
                        JOptionPane.showMessageDialog(abm, "Eliminado");
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (boton.equals(abm.getbCancelar())) {
                    entity = null;
                    abm.clearPanelFields();
                } else if (boton.equals(abm.getbAceptar())) {
                    try {
                        setEntity();
                        save(entity);
                        entity = null;
                        abm.clearPanelFields();
                        cargarDTM();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }//</editor-fold>
        }// </editor-fold>
    }
    
    private void setEntity() throws MessageException {
        if (entity == null) {
            entity = new Dominio();
        }
        String nombre = abm.getTfNombre().trim().toUpperCase();
        if (nombre.isEmpty()) {
            throw new MessageException("Nombre no válido");
        }
        if (nombre.length() > 50) {
            throw new MessageException("El nombre no puede superar los 50 caracteres");
        }
        entity.setNombre(nombre);
    }
    
    private void save(Dominio o) throws MessageException {
        if (!jpaController.findByQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE "
                + " o.nombre ='" + o.getNombre() + "'"
                + (o.getId() != null ? " AND o.id <>" + o.getId() : "")).isEmpty()) {
            throw new MessageException("Ya existe un registro con este nombre");
        }
        if (o.getId() == null) {
            jpaController.create(o);
        } else {
            jpaController.merge(o);
        }
    }
    
    private void eliminar(Dominio o) throws MessageException {
        try {
            jpaController.remove(o);
        } catch (RollbackException ex) {
            if (ex.getCause() instanceof DatabaseException) {
                PSQLException ps = (PSQLException) ex.getCause().getCause();
                if (ps.getMessage().contains("viola la llave foránea") || ps.getMessage().contains("violates foreign key constraint")) {
                    throw new MessageException("No se puede eliminar porque existen otros registros que están relacionados a este");
                }
            }
            throw ex;
        } finally {
            jpaController.closeEntityManager();
        }
    }
    
    public void displayInforme(Window owner) {
        JDateChooser dcDesde = new JDateChooser();
        JDateChooser dcHasta = new JDateChooser();
        dcHasta.setVisible(false);
        JLabel l = new JLabel("Fecha Hasta");
        l.setVisible(false);
        JComboBox cbDominios = new JComboBox();
        UTIL.loadComboBox(cbDominios, JGestionUtils.getWrappedDominios(jpaController.findAll()), true);
        final GroupLayoutPanelBuilder glpb = new GroupLayoutPanelBuilder();
        glpb.getInfoLabel().setText("Todos los campos son necesarios");
        glpb.getInfoLabel().setForeground(Color.BLUE);
        glpb.addFormItem(new JLabel("Fecha Desde"), dcDesde);
        glpb.addFormItem(l, dcHasta);
        glpb.addFormItem(new JLabel("Dominios"), cbDominios);
        JPanel panel = glpb.build();
        final JDABM jdabm = new JDABM(owner, "Informe de Facturas Compra por Dominio", true, panel);
        jdabm.setLocationRelativeTo(owner);
        jdabm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        jdabm.setVisible(true);
    }
}
