package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.controller.exceptions.PreexistingEntityException;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.entity.Iva;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import jgestion.entity.Producto;
import utilities.general.UTIL;
import jgestion.gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import jgestion.controller.exceptions.ConstraintViolationJpaException;
import jgestion.jpa.controller.IvaJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class IvaController implements ActionListener {

    private static final Logger LOG = LogManager.getLogger();
    private JDMiniABM abm;
    private final String[] colsName = {"Nº", "IVA (%)"};
    private final int[] colsWidth = {20, 20};
    private Iva EL_OBJECT;
    private final IvaJpaController jpaController = new IvaJpaController();

    public IvaController() {
    }

    public void initABM(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        abm.getTaInformacion().setText("ABM de las Alicuotas (IVA) de los Productos.");
        // solo queda visible tfCodigo....
        abm.getjLabelCodigo().setText("IVA %");
        abm.hideFieldNombre();
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.setTitle("ABM - IVA's");
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth, new Class<?>[]{null, String.class});
        UTIL.setHorizonalAlignment(abm.getjTable1(), String.class, SwingConstants.RIGHT);
        cargarTablaIvas(abm.getjTable1(), null);
        abm.setListeners(this);
        abm.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
                if (selectedRow > -1) {
                    EL_OBJECT = jpaController.find(Integer.parseInt(UTIL.getSelectedValueFromModel(abm.getjTable1(), 0).toString()));
                }
                if (EL_OBJECT != null) {
                    setPanelFields(EL_OBJECT);
                }
            }
        });
        abm.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void cargarTablaIvas(JTable jtable, String nativeQuery) {
        DefaultTableModel dtm = (DefaultTableModel) jtable.getModel();
        dtm.setRowCount(0);
        List<Iva> l = null;
        if (nativeQuery == null || nativeQuery.length() < 10) {
            l = jpaController.findAll();
        } else {
            try {
                // para cuando se usa el Buscador del ABM
                l = (List<Iva>) DAO.getNativeQueryResultList(nativeQuery, Iva.class);
            } catch (DatabaseErrorException ex) {
                //ignored...
            }
        }
        for (Iva o : l) {
            dtm.addRow(new Object[]{o.getId(), UTIL.PRECIO_CON_PUNTO.format(o.getIva())});
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (EL_OBJECT == null) {
                        throw new MessageException("Debe seleccionar la fila que desea borrar");
                    }
                    jpaController.remove(EL_OBJECT);
                    clearPanelFields();
                    cargarTablaIvas(abm.getjTable1(), null);
                    abm.showMessage("Eliminado..", jpaController.getEntityClass().getSimpleName(), 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                } catch (ConstraintViolationJpaException ex) {
                    abm.showMessage("El registro no puede ser eliminado por estar relacionado a Productos\n"
                            + ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(EL_OBJECT);
                    clearPanelFields();
                    cargarTablaIvas(abm.getjTable1(), null);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                }
            }
        }// </editor-fold>
    }

    private void clearPanelFields() {
        EL_OBJECT = null;
        abm.setTfCodigo("");
    }

    private void setPanelFields(Iva iva) {
    }

    private void setEntity() throws MessageException {
        EL_OBJECT = new Iva();
        try {
            EL_OBJECT.setIva(Float.parseFloat(abm.getTfCodigo()));
        } catch (NumberFormatException ex) {
            throw new MessageException("Porcentaje no válido");
        }
    }

    private void checkConstraints(Iva iva) throws MessageException {
        DefaultTableModel dtm = abm.getDTM();
        for (int i = dtm.getRowCount() - 1; i > -1; i--) {
            if (Double.valueOf(iva.getIva()) == Double.valueOf(dtm.getValueAt(i, 1).toString())) {
                if (iva.getId() != null && (iva.getId() == Integer.valueOf(dtm.getValueAt(i, 0).toString()))) {
                    throw new MessageException("Ya existe un IVA con este porcentaje");
                }
            }
        }

        //persistiendo......
        if (iva.getId() == null) {
            jpaController.persist(iva);
        } else {
            jpaController.merge(iva);
        }
    }

    Iva findByProducto(Integer productoID) {
        return jpaController.findByProducto(productoID);
    }
}
