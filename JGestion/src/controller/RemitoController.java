package controller;

import controller.exceptions.*;
import entity.Cliente;
import entity.Remito;
import entity.DetalleRemito;
import entity.FacturaVenta;
import entity.Sucursal;
import gui.JDFacturaVenta;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JFP;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.RemitoJpaController;
import org.apache.log4j.Logger;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class RemitoController implements ActionListener, KeyListener {

    public static final String CLASS_NAME = Remito.class.getSimpleName();
    private final FacturaVentaController facturaVentaController;
    private JDBuscadorReRe buscador;
    private boolean MODO_VISTA;
    private Remito selectedRemito;
    private boolean toFacturar;
    private boolean unlockedNumeracion = false;
    private RemitoJpaController jpaController;
    private static final Logger LOG = Logger.getLogger(RemitoController.class.getName());
    //global mutable
    private boolean editing = false;
    private boolean anulando;

    public RemitoController() {
        jpaController = new RemitoJpaController();
        facturaVentaController = new FacturaVentaController();
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD..">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public Remito create(Remito remito) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<DetalleRemito> detalleRemitoList = remito.getDetalleRemitoList();
            remito.setDetalleRemitoList(new ArrayList<DetalleRemito>());
            em.persist(remito);
            em.getTransaction().commit();
            //por que no respeta el orden en q fueron agregados los items a la List
            for (DetalleRemito detalleRemito : detalleRemitoList) {
                detalleRemito.setRemito(remito);
                DAO.create(detalleRemito);
            }
            remito.setDetalleRemitoList(detalleRemitoList);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return remito;
    }

    public void edit(Remito remito) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.find(Remito.class, remito.getId());
            remito = em.merge(remito);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = remito.getId();
                if (findRemito(id) == null) {
                    throw new NonexistentEntityException("The remito " + remito.getNumero() + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Remito> findRemitoEntities() {
        return findRemitoEntities(true, -1, -1);
    }

    public List<Remito> findRemitoEntities(int maxResults, int firstResult) {
        return findRemitoEntities(false, maxResults, firstResult);
    }

    private List<Remito> findRemitoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Remito as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Remito findRemito(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Remito.class, id);
        } finally {
            em.close();
        }
    }

    public int getRemitoCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Remito as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public JDBuscadorReRe getBuscador() {
        return buscador;
    }

    public void initRemito(JFrame owner, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController.initFacturaVenta(owner, modal, this, 3, setVisible, loadDefaultData);
        //implementación del boton Aceptar
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    if (!editing) {
                        createRemito();
                    } else {
                        editRemito(selectedRemito);
                        buscador.setVisible(true);
                    }
                } catch (MessageException ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(RemitoController.class).error(ex.getLocalizedMessage(), ex);
                } finally {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
                }
            }
        });
    }

    private void editRemito(Remito remitoToEdit) throws MessageException, NonexistentEntityException, Exception {
        JDFacturaVenta facturaVentaUI = facturaVentaController.getContenedor();
        Cliente selectedCliente;
        Sucursal selectedSucursal;
        try {
            selectedCliente = (Cliente) facturaVentaUI.getCbCliente().getSelectedItem();
        } catch (ClassCastException ex) {
            throw new MessageException("Cliente no válido");
        }
        try {
            selectedSucursal = facturaVentaController.getSelectedSucursalFromJDFacturaVenta();
        } catch (ClassCastException ex) {
            throw new MessageException("Sucursal no válido");
        }

        if (facturaVentaUI.getDcFechaFactura() == null) {
            throw new MessageException("Fecha no válida");
        }
        if (!remitoToEdit.getSucursal().equals(selectedSucursal)) {
            remitoToEdit.setNumero(getNextNumero(selectedSucursal));
        }
        remitoToEdit.setCliente(selectedCliente);
        remitoToEdit.setSucursal(selectedSucursal);
        remitoToEdit.setFechaRemito(facturaVentaUI.getDcFechaFactura());
        try {
            edit(remitoToEdit);
        } catch (IllegalOrphanException ex) {
            throw new MessageException(ex.getMessage());
        }
        doImprimir(remitoToEdit);
        facturaVentaUI.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //todo las demás acciones son manejadas (delegadas) -> FacturaVentaJpaController
        if (e.getSource() instanceof JButton) {
            JButton boton = (JButton) e.getSource();
            if (buscador != null && buscador.isActive() && boton.equals(buscador.getbBuscar())) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                } catch (Exception ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                    ex.printStackTrace();
                }
            } else {
                facturaVentaController.actionPerformed(e);
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        facturaVentaController.keyTyped(e);
    }

    public void keyReleased(KeyEvent e) {
        facturaVentaController.keyReleased(e);
    }

    @Deprecated
    public void keyPressed(KeyEvent e) {
    }

    private void createRemito() throws MessageException, Exception {
        if (MODO_VISTA) {
            doImprimir(selectedRemito);
        } else {
            JDFacturaVenta facturaVentaUI = facturaVentaController.getContenedor();
            Cliente selectedCliente;
            Sucursal selectedSucursal;

            // <editor-fold defaultstate="collapsed" desc="CONTROLES">
            try {
                selectedCliente = (Cliente) facturaVentaUI.getCbCliente().getSelectedItem();
            } catch (ClassCastException ex) {
                throw new MessageException("Cliente no válido");
            }
            try {
                selectedSucursal = facturaVentaController.getSelectedSucursalFromJDFacturaVenta();
            } catch (ClassCastException ex) {
                throw new MessageException("Sucursal no válido");
            }

            if (facturaVentaUI.getDcFechaFactura() == null) {
                throw new MessageException("Fecha no válida");
            }
            if (unlockedNumeracion) {
                try {
                    Integer octeto = Integer.valueOf(facturaVentaUI.getTfFacturaOcteto());
                    if (octeto < 1 && octeto > 99999999) {
                        throw new MessageException("Número de Remito no válido, debe ser mayor a 0 y menor o igual a 99999999");
                    }
                    Remito oldFactura = find(facturaVentaController.getSelectedSucursalFromJDFacturaVenta(), octeto);
                    if (oldFactura != null) {
                        throw new MessageException("Ya existe un registro de Remito N° " + JGestionUtils.getNumeracion(oldFactura, true));
                    }
                } catch (Exception ex) {
                    throw new MessageException("Número de Remito no válido, ingrese solo dígitos");
                }
            }
            DefaultTableModel dtm = facturaVentaUI.getDtm();
            if (dtm.getRowCount() < 1) {
                throw new MessageException("El Detalle no contiene ningún item.");
            }
            // </editor-fold>

            Remito newRemito = new Remito();
            if (unlockedNumeracion) {
                newRemito.setNumero(Integer.valueOf(facturaVentaUI.getTfFacturaOcteto()));
            } else {
                newRemito.setNumero(getNextNumero(selectedSucursal));
            }

            newRemito.setCliente(selectedCliente);
            newRemito.setSucursal(selectedSucursal);
            newRemito.setFechaRemito(facturaVentaUI.getDcFechaFactura());
            newRemito.setUsuario(UsuarioController.getCurrentUser());
            newRemito.setDetalleRemitoList(new ArrayList<DetalleRemito>(dtm.getRowCount()));
            // carga de detalleVenta
            DetalleRemito detalleRemito;
            for (int i = 0; i < dtm.getRowCount(); i++) {
                detalleRemito = new DetalleRemito();
                detalleRemito.setProducto(new ProductoController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
                detalleRemito.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
                detalleRemito.setRemito(newRemito);
                newRemito.getDetalleRemitoList().add(detalleRemito);
            }
            try {
                newRemito = create(newRemito);
            } catch (PreexistingEntityException ex) {
                Logger.getLogger(PresupuestoController.class.getName()).error(null, ex);
            }
            doImprimir(newRemito);
            limpiarPanel();
        }
    }

    public Integer getNextNumero(Sucursal s) {
        return jpaController.getNextNumero(s);
    }

    private void doImprimir(Remito p) {
        try {
            Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Remito.jasper", "Remito");
            r.addParameter("REMITO_ID", p.getId());
            r.printReport(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            Logger.getLogger(this.getClass()).error("Error - Impresión de Remito.id=" + p.getId(), ex);
        }
    }

    private void limpiarPanel() {
        facturaVentaController.borrarDetalles();
        facturaVentaController.getContenedor().setTfNumMovimiento(String.valueOf(getRemitoCount() + 1));
        Sucursal s = facturaVentaController.getSelectedSucursalFromJDFacturaVenta();
        facturaVentaController.setNumeroFactura(s, getNextNumero(s));
    }

    public void initBuscador(JDialog dialog, boolean modal, boolean setVisible) {
        buscador = new JDBuscadorReRe((Window) dialog, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        buscador.hideFormaPago();
        initBuscador(setVisible);
    }

    /**
     * Inicia el buscador con el JFrame como padre
     *
     * @param frame
     * @param modal
     * @param setVisible
     * @throws MessageException
     */
    public void initBuscador(JFrame frame, boolean modal, boolean setVisible) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        initBuscador(setVisible);
    }

    private void initBuscador(boolean setVisible) {
        buscador.getjTable1().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    selectedRemito = (Remito) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
                    if (toFacturar) {
                        //cuando se va relacionar un Remito a una FacturaVenta
                        buscador.dispose();
                    } else {
                        try {
                            if ((editing || anulando) && selectedRemito.getFacturaVenta() != null) {
                                throw new MessageException("No se puede " + (editing ? "modificar" : "anular") + " el Remito " + JGestionUtils.getNumeracion(selectedRemito, true)
                                        + "\nporque ya fue relacionado a la Factura " + JGestionUtils.getNumeracion(selectedRemito.getFacturaVenta()));
                            }
                            setDatos(selectedRemito);
                        } catch (MessageException ex) {
                            buscador.showMessage(ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        //personalizando vista de Buscador
        buscador.setPanelInfoParaRemitos();
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Instance", "Nº " + CLASS_NAME, "Nº Factura", "Cliente", "Fecha", "Sucursal", "Usuario"},
                new int[]{1, 15, 20, 50, 50, 80, 50});
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        MODO_VISTA = true;
        buscador.setListeners(this);
        buscador.setVisible(setVisible);
    }

    private void setDatos(Remito remito) {
        try {
            initRemito(null, true, false, editing);
        } catch (MessageException ex) {
            // no va saltar nunca este
        }
        JDFacturaVenta jdFacturaVenta = facturaVentaController.getContenedor();
        jdFacturaVenta.setTfNumMovimiento(remito.getId().toString());
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(remito.getSucursal().getPuntoVenta(), 4));
        jdFacturaVenta.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(remito.getNumero(), 8));
        if (editing) {
            UTIL.setSelectedItem(jdFacturaVenta.getCbCliente(), remito.getCliente());
            UTIL.setSelectedItem(jdFacturaVenta.getCbSucursal(), new ComboBoxWrapper<Sucursal>(remito.getSucursal(), remito.getSucursal().getId(), remito.getSucursal().getNombre()));
            jdFacturaVenta.getCbCliente().setEnabled(true);
            jdFacturaVenta.getCbSucursal().setEnabled(true);
            jdFacturaVenta.setEnableDcFechaFactura(true);
            JButton btnAceptar = jdFacturaVenta.getBtnAceptar();
            btnAceptar.setEnabled(true);
            btnAceptar.setText("Modificar");
            btnAceptar.setSize(btnAceptar.getWidth() + 10, btnAceptar.getHeight());
            btnAceptar.setMnemonic('f');
            btnAceptar.setEnabled(true);
        } else {
            jdFacturaVenta.getCbCliente().removeAllItems();
            jdFacturaVenta.getCbCliente().addItem(remito.getCliente());
            jdFacturaVenta.getCbSucursal().removeAllItems();
            jdFacturaVenta.getCbSucursal().addItem(new ComboBoxWrapper<Sucursal>(remito.getSucursal(), remito.getSucursal().getId(), remito.getSucursal().getNombre()));
            if (anulando) {
                jdFacturaVenta.getBtnAceptar().setVisible(false);
                jdFacturaVenta.getBtnAnular().setVisible(true);
                jdFacturaVenta.getBtnAnular().setEnabled(true);
                jdFacturaVenta.getBtnAnular().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        selectedRemito.setAnulada(DAO.getDateFromDB());
                        jpaController.merge(selectedRemito);
                    }
                });
            }
        }
//      jdFacturaVenta.getCbUsuario().addItem(remito.getUsuario());
        jdFacturaVenta.setDcFechaFactura(remito.getFechaRemito());

        List<DetalleRemito> lista = remito.getDetalleRemitoList();
        DefaultTableModel dtm = jdFacturaVenta.getDtm();
        for (DetalleRemito detallesPresupuesto : lista) {
            dtm.addRow(new Object[]{
                        null,
                        detallesPresupuesto.getProducto().getCodigo(),
                        detallesPresupuesto.getProducto().getNombre() + " " + detallesPresupuesto.getProducto().getMarca().getNombre(),
                        detallesPresupuesto.getCantidad(),
                        0,
                        0,
                        0,
                        0
                    });
        }
        jdFacturaVenta.modoVista();
        jdFacturaVenta.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM remito o"
                + " WHERE o.id > -1 ");

        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                Integer numero = Integer.valueOf(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }

        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_remito >='").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_remito <='").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal= ").append(((ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getEntity().getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                Sucursal s = ((ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i)).getEntity();
                query.append(" o.sucursal=").append(s.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente =").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            if (buscador.getCbFormasDePago().getSelectedIndex() == 1) {
                query.append(" AND o.factura_venta IS NULL");
            } else {
                query.append(" AND o.factura_venta IS NOT NULL");
            }
        }
        query.append(" ORDER BY o.id");
        cargarTablaBuscador(query.toString());
    }

    private void cargarTablaBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        List<Remito> list = DAO.getEntityManager().createNativeQuery(query, Remito.class).getResultList();
        for (Remito remito : list) {
            dtm.addRow(new Object[]{
                        remito,
                        JGestionUtils.getNumeracion(remito, true),
                        remito.getFacturaVenta() != null ? JGestionUtils.getNumeracion(remito.getFacturaVenta()) : "",
                        remito.getCliente().getNombre(),
                        UTIL.DATE_FORMAT.format(remito.getFechaRemito()),
                        remito.getSucursal().getNombre(),
                        remito.getUsuario().getNick()});
        }
    }

    /**
     * Para distingir cuando una selección es para visualizar un Remito y una de
     * para "relaciónar un Remito a una Factura Venta".
     *
     * @param toFacturar
     */
    public void setToFacturar() {
        this.toFacturar = true;
    }

    public Remito getSelectedRemito() {
        return selectedRemito;
    }

    /**
     * Inicializa y hace visible la UI (modal) de busqueda y selección de Remito
     * para relacionarlo con una {@link FacturaVenta}
     *
     * @param owner
     * @param cliente entity {@link Cliente} del cual se van a buscar el Remito
     */
    void initBuscadorToFacturar(JDialog owner, Cliente cliente) {
        buscador = new JDBuscadorReRe((Window) owner, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
        buscador.hideFormaPago();
        initBuscador(false);
        setToFacturar();
        UTIL.setSelectedItem(buscador.getCbClieProv(), cliente);
        buscador.getCbFormasDePago().setSelectedIndex(1); // los NO facturados
        buscador.getCbFormasDePago().setEnabled(false);
        buscador.getCbClieProv().setEnabled(false);
        buscador.setVisible(true);
    }

    public void unlockedABM(JFrame owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA_NUMERACION_MANUAL);
        initRemito(owner, true, false, true);
        unlockedNumeracion = true;
        facturaVentaController.getContenedor().setNumeroFacturaEditable(true);
        facturaVentaController.getContenedor().setVisible(true);

    }

    private Remito find(Sucursal sucursal, Integer numero) {
        return getEntityManager().createQuery("SELECT o FROM Remito o"
                + " WHERE o.sucursal.id=" + sucursal.getId() + " AND o.numero=" + numero, Remito.class).getSingleResult();
    }

    public void initRemitoEditor(JFrame owner) throws MessageException {
        this.editing = true;
//        initRemito(owner, true, false, true);
        initBuscador(owner, true, false);
        MODO_VISTA = false;
        buscador.setVisible(true);
    }

    public void initBuscadorToAnular(Window owner) throws MessageException {
        this.editing = false;
        this.anulando = true;
        MODO_VISTA = false;
        initBuscador((JFrame) owner, false, true);
        buscador.setVisible(true);
    }
}
