package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Caja;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import utilities.general.UTIL;
import entity.Usuario;
import gui.JDMiniABM;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jpa.controller.CajaMovimientosJpaController;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class CajaController implements ActionListener {

    public final String CLASS_NAME = Caja.class.getSimpleName();
    private JDMiniABM abm;
    private Caja ELOBJECT;
    private static final String messageFirstCaja = "Está por crear la primer Caja."
            + "\nLas Cajas registran los movimientos monetarios del sistema (Facturas Compra/Venta, Recibos,"
            + "\nMovimientos entre Cajas, Movimientos varios, cobro Cheques) en efectivo."
            + "\nLa creación de una Caja implica la apertura de la misma (con su primer detalle indicadolo), ningún usuario"
            + "\nincluyendo quien la creó tendrá permiso de acceso a esta. Para tener acceso debe ir a:"
            + "\nMenú -> Usuarios -> ABM Usuarios -> Seleccionar el usuario -> Modificar -> y seleccionar en la tabla inferior la/s Caja/s";

    // <editor-fold defaultstate="collapsed" desc="CRUD..">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Caja caja) throws Exception {
        DAO.create(caja);
    }

    public void edit(Caja caja) throws IllegalOrphanException, NonexistentEntityException, Exception {
        DAO.doMerge(caja);
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        // LAS CAJAS NO SE BORRAN................
    }

    public List<Caja> findCajaEntities() {
        return findCajaEntities(true, -1, -1);
    }

    public List<Caja> findCajaEntities(int maxResults, int firstResult) {
        return findCajaEntities(false, maxResults, firstResult);
    }

    private List<Caja> findCajaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Caja as o ORDER BY o.nombre");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Caja findCaja(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Caja.class, id);
        } finally {
            em.close();
        }
    }

    public int getCajaCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Caja as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    private void checkConstraints(Caja caja) throws MessageException, Exception {
        String idQuery = "";
        if (caja.getId() != null) {
            idQuery = "o.id <> " + caja.getId() + " AND ";
        }

        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + caja.getNombre() + "'", Caja.class).getSingleResult();
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }

        //persistiendo......
        if (caja.getId() == null) {
            create(caja);
            //se crea y hace una apertura implicitamente de cajaMovimiento
            new CajaMovimientosController().nueva(caja);
        } else {
            edit(caja);
        }
    }

    private void setEntity() throws MessageException {
        if (ELOBJECT == null) {
            ELOBJECT = new Caja();
        }
        if (abm.getTfNombre() == null || abm.getTfNombre().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre de " + CLASS_NAME.toLowerCase());
        }

        ELOBJECT.setNombre(abm.getTfNombre().trim().toUpperCase());
        ELOBJECT.setEstado(true);
        ELOBJECT.setBaja(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                ELOBJECT = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
//                try {
//                    eliminarCaja();
//                    EL_OBJECT = null;
//                    abm.clearPanelFields();
//                    cargarDTM(abm.getDTM(), null);
//                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
//                } catch (MessageException ex) {
//                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
//                } catch (IllegalOrphanException ex) {
//                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                ELOBJECT = null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(ELOBJECT);
                    ELOBJECT = null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), null);
                    abm.showMessage("Las Cajas nuevas por defecto no están asignadas a ningún usuario."
                            + "\nPara asignar permisos a esta, debe ir a Menú -> Usuarios -> ABM Usuarios -> Seleccionar el usuario > Modificar", CLASS_NAME, 2);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(this.getClass()).error("Error en al creación de Caja", ex);
                }
            } else if (boton.getName().equalsIgnoreCase("lock")) {
                try {
                    cambiarEstado();
                    cargarDTM(abm.getDTM(), null);
                    abm.showMessage(CLASS_NAME + " modificada", CLASS_NAME, 1);

                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(this.getClass()).error("Error cambiando de estado la Caja", ex);
                } finally {
                    ELOBJECT = null;
                }
            }
            return;
        }// </editor-fold>
    }

    public JDialog initABM(JFrame frame, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_CAJAS);
        abm = new JDMiniABM(frame, modal);
        abm.getTaInformacion().setText("Las Cajas sirve para agrupar registros de los diferentes comprobantes de Venta y Compras que se ingresen al sistema.");
        abm.setListeners(this);
        abm.hideBtnElimiar();
        abm.hideFieldCodigo();
        abm.hideFieldExtra();
        abm.pack();
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        UTIL.getDefaultTableModel(abm.getjTable1(),
                new String[]{"Nº", "Nombre", "Habilitada", "Últ. apertura", "Eliminada"},
                new int[]{20, 120, 30, 50, 20});
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        cargarDTM(abm.getDTM(), null);
        abm.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                int selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    ELOBJECT = (Caja) DAO.getEntityManager().find(Caja.class,
                            Integer.valueOf((abm.getjTable1().getModel().getValueAt(selectedRow, 0)).toString()));
                }

                if (ELOBJECT != null) {
                    setPanelFields(ELOBJECT);
                    setLockIcon(ELOBJECT.getEstado());
                }
            }
        });
        if (getCajaCount() < 1) {
            showInfoMessage();
        }
        return abm;
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        List<Caja> cajasList;
        if (query == null || query.length() < 10) {
            cajasList = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findByBaja").setParameter("baja", false).getResultList();
        } else {
            // para cuando se usa el Buscador del ABM
            cajasList = DAO.getEntityManager().createNativeQuery(query, Caja.class).getResultList();
        }

        CajaMovimientosJpaController cmController = new CajaMovimientosJpaController();
        for (Caja o : cajasList) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),
                        o.getEstado() ? "Si" : "No",
                        UTIL.DATE_FORMAT.format(cmController.findCajaMovimientoAbierta(o).getFechaApertura()), o.isBaja() ? "Si" : "No" // <--- eliminadas
                    });
        }
    }

    private void setPanelFields(Caja o) {
        abm.setTfNombre(o.getNombre());
        //bloqueo del botón Eliminar si..
        abm.getbEliminar().setEnabled(!o.isBaja());
    }

    private void eliminarCaja() throws IllegalOrphanException, NonexistentEntityException, MessageException {
        if (ELOBJECT == null) {
            throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
        }
        destroy(ELOBJECT.getId());
        cargarDTM(abm.getDTM(), null);
    }

    private void cambiarEstado() throws MessageException, Exception {
        if (ELOBJECT == null) {
            throw new MessageException("Debe seleccionar una " + CLASS_NAME);
        }
        ELOBJECT.setEstado(!ELOBJECT.getEstado());
        edit(ELOBJECT);
    }

    private void setLockIcon(boolean estado) {
        if (estado) {
            abm.getBtnLock().setIcon(new ImageIcon(getClass().getResource("/iconos/lock.png"))); //
            abm.getBtnLock().setText("Baja");
        } else {
            abm.getBtnLock().setIcon(new ImageIcon(getClass().getResource("/iconos/unlock.png"))); //
            abm.getBtnLock().setText("Activar");
        }
    }

    /**
     * Devuelve una lista de Caja permitidas a este usuario
     * @param usuario que solicita la Lista de Cajas
     * @param estado estado de la Caja (si está activa o no). Si es null, bring both
     * @return <code>List<Caja>, o null si no tiene permisos de Caja
     */
    @SuppressWarnings("unchecked")
    public List<Caja> findCajasPermitidasByUsuario(Usuario usuario, Boolean estado) {
        Query q;
        if (estado != null) {
            q = DAO.createQuery("SELECT c FROM " + CLASS_NAME + " c, PermisosCaja pc "
                    + " WHERE pc.caja.id = c.id AND pc.usuario.id = " + usuario.getId() + " AND c.estado = " + estado
                    + " ORDER BY c.nombre", true);
        } else {
            q = DAO.createQuery("SELECT c FROM " + CLASS_NAME + " c, PermisosCaja pc "
                    + " WHERE pc.caja.id = c.id AND pc.usuario.id = " + usuario.getId()
                    + " ORDER BY c.nombre", true);
        }
        return q.getResultList();
    }

    private void showInfoMessage() {
        JOptionPane.showMessageDialog(abm, messageFirstCaja, "Creación de Cajas", JOptionPane.INFORMATION_MESSAGE);
    }
}
