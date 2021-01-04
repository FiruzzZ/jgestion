package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.entity.Rubro;
import utilities.general.UTIL;
import jgestion.gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import jgestion.jpa.controller.RubroJpaController;

/**
 * Clase encargada del DAO y CRUD de los Rubro's de los Productos, Clientes y Proveedores.
 *
 * @author FiruzzZ
 */
public class RubroController implements ActionListener, MouseListener {

    public static final int DE_PRODUCTO = 1;
//    public static final int DE_CLIENTE=2;
//    public static final int DE_PROVEEDOR=3;
    public final String CLASS_NAME = "Rubro";
    private JDMiniABM abm;
    /**
     * Con este, separamos los rubros que usan las distintas entidades. 1= productos, 2= clientes, 3
     * = proveedores
     */
    private final short TIPO;
    private Rubro rubro;
    private RubroJpaController dao = new RubroJpaController();

    /**
     * @param tipo 1=Productos, 2=clientes, 3=Proveedores
     */
    public RubroController() {
        TIPO = 1;
    }

    private void checkConstraints(Rubro rubro) throws MessageException, Exception {
        String idQuery = "";
        if (rubro.getId() != null) {
            idQuery = "o.id!=" + rubro.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + rubro.getNombre() + "' AND o.tipo=" + rubro.getTipo(), Rubro.class).getSingleResult();
            throw new MessageException("Ya existe otro " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }
        if (rubro.getCodigo() != null && rubro.getCodigo().length() > 0) {
            try {
                DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                        + " WHERE " + idQuery + " o.codigo='" + rubro.getCodigo() + "' AND o.tipo=" + rubro.getTipo(), Rubro.class).getSingleResult();
                throw new MessageException("Ya existe otro " + CLASS_NAME + " con este código.");
            } catch (NoResultException ex) {
            }
        }

        if (rubro.getId() == null) {
            dao.persist(rubro);
        } else {
            dao.merge(rubro);
        }
    }

    private void setEntity() throws MessageException {
        if (rubro == null) {
            rubro = new Rubro();
        }
        if (abm.getTfNombre() == null || abm.getTfNombre().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre de " + CLASS_NAME.toLowerCase());
        }
        rubro.setNombre(abm.getTfNombre().trim().toUpperCase());
        if (abm.getTfCodigo().trim().length() > 0) {
            rubro.setCodigo(abm.getTfCodigo().trim().toUpperCase());
        }
        rubro.setTipo(TIPO); // <--- la magia
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();

            if (boton.equals(abm.getbNuevo())) {
                rubro = null;
                abm.clearPanelFields();
            } else if (boton.equals(abm.getbEliminar())) {
                try {
                    eliminarRubro();
                    rubro = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                rubro = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(rubro);
                    rubro = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                }
            }
        }// </editor-fold>
    }

    public JDialog getABM(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, true);
        initABM();
        return abm;
    }

    private void initABM() throws MessageException {
        abm.hideFieldExtra();
        abm.hideBtnLock();
        abm.setTitle("ABM - Rubros");
        abm.getTaInformacion().setText("Los Rubros (y SubRubros) son utilizados para clasificar y segmentar los "
                + "distintos productos");
        UTIL.getDefaultTableModel(abm.getjTable1(),
                new String[]{"Nº", "Nombre", "Código"},
                new int[]{20, 120, 80});
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        cargarDTM(abm.getDTM(), null);
        abm.setListeners(this);
    }

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        DefaultTableModel dtm = (DefaultTableModel) ((javax.swing.JTable) e.getSource()).getModel();
        if (selectedRow > -1) {
            rubro = (Rubro) DAO.getEntityManager().find(Rubro.class,
                    Integer.valueOf((dtm.getValueAt(selectedRow, 0)).toString()));
        }
        if (rubro != null) {
            setPanelFields(rubro);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        dtm.setRowCount(0);
        List<Rubro> l;
        if (query == null || query.length() < 10) {
            l = dao.findAll();
        } else {
            // para cuando se usa el Buscador del ABM
            l = dao.findByNativeQuery(query);
        }
        l.forEach(o -> dtm.addRow(new Object[]{o.getId(), o.getNombre(), o.getCodigo()}));
    }

    private void setPanelFields(Rubro o) {
        abm.setTfNombre(o.getNombre());
        abm.setTfCodigo(o.getCodigo());
    }

    private void eliminarRubro() throws MessageException, NonexistentEntityException, IllegalOrphanException {
        if (rubro == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
        }
        dao.remove(rubro);
    }

}
