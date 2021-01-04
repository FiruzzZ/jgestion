package jgestion.controller;

import utilities.general.UTIL;
import jgestion.entity.Unidadmedida;
import jgestion.gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import jgestion.controller.exceptions.MessageException;
import jgestion.jpa.controller.UnidadmedidaJpaController;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class UnidadmedidaController implements ActionListener, MouseListener {

    private String CLASS_NAME = "Unidadmedida";
    private final String[] colsName = {"Nº", "Nombre"};
    private final int[] colsWidth = {20, 20};
    private JDMiniABM abm;
    private Unidadmedida unidadMedida;
    private UnidadmedidaJpaController dao = new UnidadmedidaJpaController();

    public UnidadmedidaController() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (unidadMedida == null) {
                        throw new MessageException("Debe seleccionar la fila que desea borrar");
                    }
                    dao.remove(unidadMedida);
                    clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    LogManager.getLogger();
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(unidadMedida);
                    clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    LogManager.getLogger();//(this.getClass()).error(null, ex);
                }
            }

            return;
        }// </editor-fold>
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void clearPanelFields() {
        unidadMedida = null;
        abm.setTfNombre("");
    }

    private void cargarDTM(DefaultTableModel dtm, String naviteQuery) {
        for (int i = dtm.getRowCount(); i > 0; i--) {
            dtm.removeRow(i - 1);
        }
        java.util.List<Unidadmedida> l;
        if (naviteQuery == null || naviteQuery.length() < 10) {
            l = dao.findAll();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(naviteQuery, Unidadmedida.class).getResultList();
        }
        for (Unidadmedida o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getNombre(),});
        }
    }

    private void setEntity() throws MessageException {
        if (unidadMedida == null) {
            unidadMedida = new Unidadmedida();
        }
        if (abm.getTfNombre().trim().length() < 1) {
            throw new MessageException("Nombre de Unidad de medida no válido");
        }
        unidadMedida.setNombre(abm.getTfNombre().trim().toUpperCase());
    }

    private void checkConstraints(Unidadmedida unidadMedida) throws MessageException {
        Unidadmedida old = dao.findByNombre(unidadMedida.getNombre());
        if (old != null && !Objects.equals(unidadMedida, old)) {
            throw new MessageException("Ya existe una unidad de medida con este nombre");
        }
        if (unidadMedida.getId() == null) {
            dao.persist(unidadMedida);
        } else {
            dao.merge(unidadMedida);
        }
    }

    public void initABM(JFrame frame, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(frame, modal);
        abm.hideBtnLock();
        abm.hideFieldCodigo();
        abm.hideFieldExtra();
        abm.setTitle("ABM - Unidades de medida");
        abm.getTaInformacion().setText("Las Unidad de medida, expresan la forma de cuantificación del Producto, "
                + "sirven como información adicional en informes/reportes del sistema.");
        try {
            UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
            UTIL.hideColumnTable(abm.getjTable1(), 0);
        } catch (Exception ex) {
            LogManager.getLogger();//(this.getClass()).error(null, ex);
        }
        cargarDTM(abm.getDTM(), null);
        abm.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    unidadMedida = DAO.getEntityManager().find(Unidadmedida.class,
                            UTIL.getSelectedValue(abm.getjTable1(), 0));
                }
                if (unidadMedida != null) {
                    setPanelFields(unidadMedida);
                }
            }
        });
        abm.setListeners(this);
        abm.setVisible(true);
    }

    private void setPanelFields(Unidadmedida unidadMedida) {
        abm.setTfNombre(unidadMedida.getNombre());
    }

}
