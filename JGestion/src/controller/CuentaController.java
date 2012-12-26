package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Cuenta;
import entity.DetalleCajaMovimientos;
import gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jpa.controller.CuentaJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class CuentaController {

    private static final Logger LOG = Logger.getLogger(CuentaController.class.getName());
    private final CuentaJpaController jpaController;
    private Cuenta EL_OBJECT;
    public static final Cuenta SIN_CLASIFICAR; //antes llamado EFECTIVO

    static {
        SIN_CLASIFICAR = DAO.getEntityManager().find(Cuenta.class, 1);
    }

    public CuentaController() {
        jpaController = new CuentaJpaController();
    }

    public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
        ArrayList<String> lis;
        try {
            Cuenta cuenta;
            try {
                cuenta = jpaController.find(id);
                cuenta.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("No existe el registro.", enfe);
            }
            if (!jpaController.findByQuery("SELECT o.id FROM " + DetalleCajaMovimientos.class.getSimpleName() + " o WHERE o.cuenta.id=" + cuenta.getId()).isEmpty()) {
                lis = new ArrayList<String>(1);
                lis.add("No se puede eliminar el registro " + jpaController.getEntityClass().getSimpleName() + " porque está relacionado a otro/s registro/s.");
                throw new IllegalOrphanException(lis);
            }
            jpaController.remove(cuenta);
        } finally {
        }
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

    /**
     * Init UI ABM de MovimientoConcepto
     *
     * @param jFrame owner, patern bla bla
     * @throws MessageException End-User messages
     */
    public void initABM(JFrame jFrame) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        final JDMiniABM abm = new JDMiniABM(jFrame, true);
        abm.hideBtnLock();
        abm.hideFieldCodigo();
        abm.hideFieldExtra();
        abm.setVisibleTaInformacion(false);
        abm.pack();
        abm.setTitle("ABM - " + jpaController.getEntityClass().getSimpleName() + "'s");
        UTIL.getDefaultTableModel(abm.getjTable1(),
                new String[]{"Nº", "Nombre"},
                new int[]{10, 150});
        cargarDTM(abm.getDTM(), null);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (EL_OBJECT == null) {
                    EL_OBJECT = new Cuenta();
                }
                EL_OBJECT.setNombre(abm.getTfNombre());
                try {
                    abm.getbAceptar().setEnabled(false);
                    checkConstraints(EL_OBJECT);
                    //persistiendo......
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        jpaController.create(EL_OBJECT);
                        msg = " creado";
                    } else {
                        jpaController.merge(EL_OBJECT);
                        msg = " editado";
                    }
                    JOptionPane.showMessageDialog(abm, jpaController.getEntityClass().getSimpleName() + msg);
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), null);
                    EL_OBJECT = null;
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
        abm.getbEliminar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(abm, "¿Confirma eliminación de \"" + EL_OBJECT + "\"?", null, JOptionPane.YES_NO_OPTION)) {
                    try {
                        abm.getbEliminar().setEnabled(false);
                        destroy(EL_OBJECT.getId());
                        EL_OBJECT = null;
                        abm.clearPanelFields();
                        cargarDTM(abm.getDTM(), null);
                        JOptionPane.showMessageDialog(abm, "Eliminado..");
                    } catch (IllegalOrphanException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage());
                    } catch (NonexistentEntityException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage());
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
                    EL_OBJECT = (Cuenta) DAO.getEntityManager().find(Cuenta.class,
                            Integer.valueOf((abm.getjTable1().getModel().getValueAt(selectedRow, 0)).toString()));
                    if (EL_OBJECT != null) {
                        abm.setFields(EL_OBJECT.getNombre(), null, null);
                    }
                }
            }
        });
        abm.setVisible(true);
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        List<Cuenta> list;
        if (query == null || query.length() < 10) {
            list = DAO.getEntityManager().createQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.id > 1 ORDER BY o.nombre").getResultList();
        } else {
            // para cuando se usa el Buscador del ABM
            list = DAO.getEntityManager().createNativeQuery(query, Cuenta.class).getResultList();
        }

        for (Cuenta o : list) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),});
        }
    }

    private void checkConstraints(Cuenta o) throws MessageException, Exception {
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
}
