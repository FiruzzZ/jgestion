package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.IllegalOrphanException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.controller.exceptions.PreexistingEntityException;
import jgestion.entity.Cliente;
import jgestion.entity.Remito;
import jgestion.entity.DetalleRemito;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Producto;
import jgestion.entity.Sucursal;
import jgestion.entity.Vendedor;
import jgestion.gui.JDFacturaVenta;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import utilities.general.UTIL;
import jgestion.gui.JDBuscadorReRe;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.ProductoJpaController;
import jgestion.jpa.controller.RemitoJpaController;
import jgestion.jpa.controller.VendedorJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.gui.SwingUtil;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class RemitoController implements ActionListener {

    public static final String CLASS_NAME = Remito.class.getSimpleName();
    private final FacturaVentaController facturaVentaController;
    private JDBuscadorReRe buscador;
    private boolean MODO_VISTA;
    private Remito selectedRemito;
    private boolean toFacturar;
    private boolean unlockedNumeracion = false;
    private RemitoJpaController jpaController;
    private static final Logger LOG = LogManager.getLogger();
    //global mutable
    private boolean editing = false;
    private boolean anulando = false;

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

    public void initRemito(Window owner, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController.displayABM(owner, modal, this, 3, setVisible, loadDefaultData);
        //implementación del boton Aceptar
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    if (!editing) {
                        setRemito();
                    } else {
                        merge(selectedRemito);
                        selectedRemito = null;
                    }
                } catch (MessageException ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                    LogManager.getLogger();//(RemitoController.class).error(ex.getLocalizedMessage(), ex);
                } finally {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
                }
            }
        });
    }

    private void merge(Remito remitoToEdit) throws MessageException, NonexistentEntityException, Exception {
        JDFacturaVenta facturaVentaUI = facturaVentaController.getContenedor();
        Cliente cliente;
        Sucursal sucursal;
        try {
            cliente = (Cliente) facturaVentaUI.getCbCliente().getSelectedItem();
        } catch (ClassCastException ex) {
            throw new MessageException("Cliente no válido");
        }
        try {
            sucursal = facturaVentaController.getSelectedSucursalFromJDFacturaVenta();
        } catch (ClassCastException ex) {
            throw new MessageException("Sucursal no válido");
        }

        if (facturaVentaUI.getDcFechaFactura() == null) {
            throw new MessageException("Fecha no válida");
        }
        DefaultTableModel dtm = facturaVentaUI.getDtm();
        if (dtm.getRowCount() < 1) {
            throw new MessageException("El Detalle no contiene ningún item.");
        }
        jpaController.closeEntityManager();
        EntityManager em = DAO.getEntityManager();
        em.getTransaction().begin();
        Remito r = em.find(jpaController.getEntityClass(), remitoToEdit.getId());
        if (!r.getSucursal().equals(sucursal)) {
            r.setNumero(getNextNumero(sucursal));
        }
        r.setCliente(em.find(Cliente.class, cliente.getId()));
        r.setSucursal(em.find(Sucursal.class, sucursal.getId()));
        r.setFechaRemito(facturaVentaUI.getDcFechaFactura());
        if (facturaVentaUI.getCbVendedor().getSelectedIndex() > 0) {
            Vendedor v = (Vendedor) ((EntityWrapper<?>) facturaVentaUI.getCbVendedor().getSelectedItem()).getEntity();
            r.setVendedor(em.find(v.getClass(), v.getId()));
        } else {
            r.setVendedor(null);
        }
        r.getDetalleRemitoList().clear();
        DetalleRemito detalleRemito;
        for (int i = 0; i < dtm.getRowCount(); i++) {
            detalleRemito = new DetalleRemito();
            detalleRemito.setProducto(em.find(Producto.class, (Integer) dtm.getValueAt(i, 9)));
            detalleRemito.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detalleRemito.setRemito(r);
            r.getDetalleRemitoList().add(detalleRemito);
        }
        em.getTransaction().commit();
        doReport(r);
        facturaVentaUI.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //todo las demás acciones son manejadas (delegadas) -> FacturaVentaJpaController
    }

    @SuppressWarnings("unchecked")
    private void setRemito() throws MessageException, Exception {
        if (MODO_VISTA) {
            doReport(selectedRemito);
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
            if (facturaVentaUI.getCbVendedor().getSelectedIndex() > 0) {
                newRemito.setVendedor(((EntityWrapper<Vendedor>) facturaVentaUI.getCbVendedor().getSelectedItem()).getEntity());
            }
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
                LogManager.getLogger();//(PresupuestoController.class.getName()).error(null, ex);
            }
            doReport(newRemito);
            limpiarPanel();
        }
    }

    public Integer getNextNumero(Sucursal s) {
        return jpaController.getNextNumero(s);
    }

    private void doReport(Remito p) {
        try {
            Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Remito.jasper", "Remito");
            r.addParameter("REMITO_ID", p.getId());
            r.printReport(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            LogManager.getLogger();//(this.getClass()).error("Error - Impresión de Remito.id=" + p.getId(), ex);
        }
    }

    private void limpiarPanel() {
        facturaVentaController.borrarDetalles();
        facturaVentaController.getContenedor().setTfNumMovimiento(String.valueOf(getRemitoCount() + 1));
        Sucursal s = facturaVentaController.getSelectedSucursalFromJDFacturaVenta();
        facturaVentaController.setNumeroFactura(s, getNextNumero(s));
    }

//    public void initBuscador(JDialog dialog, boolean modal, boolean setVisible) {
//        buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
//        buscador.hideFormaPago();
//        initBuscador(setVisible);
//    }
    /**
     * Inicia el buscador con el JFrame como padre
     *
     * @param frame
     * @param modal
     * @param setVisible
     * @throws MessageException
     */
    public void showBuscador(Window frame, boolean modal, boolean setVisible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        buscador.setParaRemito();
        initBuscador(setVisible);
    }

    private void initBuscador(boolean setVisible) {
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                } catch (Exception ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                    LOG.error(ex, ex);
                }
            }
        });
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    selectedRemito = (Remito) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
                    try {
                        if (toFacturar) {
                            if (selectedRemito.getFacturaVenta() != null) {
                                throw new MessageException("Este remito ya está relacionado a la Factura " + JGestionUtils.getNumeracion(selectedRemito.getFacturaVenta()));
                            }
                            if (selectedRemito.getAnulada() != null) {
                                throw new MessageException("Un Remito anulado no puede ser relacionado a una Factura");
                            }
                            //chau buscador porque se va relacionar un Remito a una FacturaVenta
                            buscador.dispose();
                        } else {
//                            if ((editing || anulando) && selectedRemito.getFacturaVenta() != null) {
//                                throw new MessageException("No se puede " + (editing ? "modificar" : "anular") + " el Remito " + JGestionUtils.getNumeracion(selectedRemito, true)
//                                        + "\nporque ya fue relacionado a la Factura " + JGestionUtils.getNumeracion(selectedRemito.getFacturaVenta()));
//                            }
                            show(selectedRemito);
                        }
                    } catch (MessageException ex) {
                        buscador.showMessage(ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        //editar button
        buscador.getbExtra().setVisible(true);
        buscador.getbExtra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        editing = true;
                        selectedRemito = (Remito) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
                        if (selectedRemito.getFacturaVenta() != null) {
                            throw new MessageException("No se puede " + (editing ? "modificar" : "anular") + " el Remito " + JGestionUtils.getNumeracion(selectedRemito, true)
                                    + "\nporque ya fue relacionado a la Factura " + JGestionUtils.getNumeracion(selectedRemito.getFacturaVenta()));
                        }
                        show(selectedRemito);
                        armarQuery();
                    } else {
                        buscador.showMessage("Seleccione la fila que corresponde al Remito que desea editar", null, JOptionPane.WARNING_MESSAGE);
                    }
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                } finally {
                    editing = false;
                    selectedRemito = null;
                }
            }
        });
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findAll(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.loadComboBox(buscador.getCbVendedor(), JGestionUtils.getWrappedVendedor(new VendedorJpaController().findAll()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Instance", "Nº " + CLASS_NAME, "Nº Factura", "Cliente", "Fecha", "Sucursal", "Usuario"},
                new int[]{1, 15, 20, 50, 50, 80, 50});
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        MODO_VISTA = true;
        buscador.setListeners(this);
        buscador.setVisible(setVisible);
    }

    private void show(Remito remito) throws MessageException {
        initRemito(buscador, true, false, editing);
        final JDFacturaVenta jdFacturaVenta = facturaVentaController.getContenedor();
        jdFacturaVenta.setEditMode(editing);
        jdFacturaVenta.setTfNumMovimiento(remito.getId().toString());
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(remito.getSucursal().getPuntoVenta(), 4));
        jdFacturaVenta.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(remito.getNumero(), 8));
        jdFacturaVenta.setDcFechaFactura(remito.getFechaRemito());
        SwingUtil.setComponentsEnabled(jdFacturaVenta.getPanelDatosFacturacion().getComponents(), editing, true, (Class<? extends Component>[]) null);
        SwingUtil.setComponentsEnabled(jdFacturaVenta.getPanelProducto().getComponents(), editing, true, (Class<? extends Component>[]) null);
        if (editing) {
            jdFacturaVenta.setTitle("EDITANDO - " + jdFacturaVenta.getTitle());
            UTIL.setSelectedItem(jdFacturaVenta.getCbCliente(), remito.getCliente());
            UTIL.setSelectedItem(jdFacturaVenta.getCbSucursal(), remito.getSucursal());
            if (remito.getVendedor() != null) {
                UTIL.setSelectedItem(jdFacturaVenta.getCbVendedor(), remito.getVendedor());
            }
            jdFacturaVenta.getCbCliente().setEnabled(true);
            jdFacturaVenta.getCbSucursal().setEnabled(true);
            jdFacturaVenta.setEnableDcFechaFactura(true);
            JButton btnAceptar = jdFacturaVenta.getBtnAceptar();
            btnAceptar.setEnabled(true);
            btnAceptar.setText("Modificar");
            btnAceptar.setMnemonic('f');
        } else {
            jdFacturaVenta.getCbCliente().removeAllItems();
            jdFacturaVenta.getCbCliente().addItem(remito.getCliente());
            jdFacturaVenta.getCbSucursal().removeAllItems();
            jdFacturaVenta.getCbSucursal().addItem(new EntityWrapper<Sucursal>(remito.getSucursal(), remito.getSucursal().getId(), remito.getSucursal().getNombre()));
            if (anulando) {
                jdFacturaVenta.setTitle("ANULANDO - " + jdFacturaVenta.getTitle());
                jdFacturaVenta.getBtnAceptar().setVisible(false);
                jdFacturaVenta.getBtnAnular().setVisible(true);
                jdFacturaVenta.getBtnAnular().setEnabled(true);
                jdFacturaVenta.getBtnAnular().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        selectedRemito.setAnulada(DAO.getDateFromDB());
                        jpaController.merge(selectedRemito);
                        jdFacturaVenta.showMessage("Remito " + JGestionUtils.getNumeracion(selectedRemito, true) + " anulado", null, JOptionPane.INFORMATION_MESSAGE);
                        jdFacturaVenta.dispose();
                        try {
                            armarQuery();
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    }
                });
            }
        }

        List<DetalleRemito> lista = remito.getDetalleRemitoList();
        DefaultTableModel dtm = jdFacturaVenta.getDtm();
        ProductoJpaController productoJpaController = new ProductoJpaController();
        for (DetalleRemito detalle : lista) {
            Producto p = productoJpaController.find(detalle.getProducto().getId());
            try {
                dtm.addRow(new Object[]{
                    p.getIva().getIva(),
                    p.getCodigo(),
                    p.getNombre() + " " + p.getMarca().getNombre(),
                    detalle.getCantidad(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    p.getId(),
                    null
                });
            } catch (NullPointerException e) {
                throw new MessageException("Ocurrió un error recuperando el detalle y los datos del Producto:"
                        + "\nCódigo:" + p.getNombre()
                        + "\nNombre:" + p.getCodigo()
                        + "\nIVA:" + p.getIva()
                        + "\n\n   Intente nuevamente.");
            }
        }
        productoJpaController.closeEntityManager();
        jdFacturaVenta.pack();
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
            query.append(" AND o.sucursal= ").append(((EntityWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getEntity().getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                Sucursal s = ((EntityWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i)).getEntity();
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
        if (buscador.getCbVendedor().getSelectedIndex() > 0) {
            query.append(" AND o.vendedor_id = ").append(((EntityWrapper<Vendedor>) buscador.getCbVendedor().getSelectedItem()).getId());
        }
        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            if (buscador.getCbFormasDePago().getSelectedIndex() == 1) {
                query.append(" AND o.factura_venta IS NULL");
            } else {
                query.append(" AND o.factura_venta IS NOT NULL");
            }
        }
        query.append(" AND o.anulada IS ").append(buscador.getCheckAnulada().isSelected() ? " NOT " : "").append(" NULL");
        query.append(" ORDER BY o.id");
        LOG.debug(query.toString());
        cargarTablaBuscador(query.toString());
    }

    private void cargarTablaBuscador(String nativeSQL) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        List<Remito> list = jpaController.findByNativeQuery(nativeSQL);
        for (Remito remito : list) {
            dtm.addRow(new Object[]{
                remito,
                JGestionUtils.getNumeracion(remito, true),
                remito.getFacturaVenta() != null ? JGestionUtils.getNumeracion(remito.getFacturaVenta()) : "",
                remito.getCliente().getNombre(),
                UTIL.DATE_FORMAT.format(remito.getFechaRemito()),
                remito.getSucursal().getNombre(),
                remito.getUsuario().getNick()
            });
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

    public void unlockedABM(JFrame owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA_NUMERACION_MANUAL);
        initRemito(owner, true, false, true);
        unlockedNumeracion = true;
        facturaVentaController.getContenedor().setNumeroFacturaEditable(true);
        facturaVentaController.getContenedor().setVisible(true);

    }

    private Remito find(Sucursal sucursal, Integer numero) {
        return getEntityManager().createQuery("SELECT o FROM Remito o"
                + " WHERE o.sucursal.id=" + sucursal.getId() + " AND o.numero=" + numero, Remito.class).getSingleResult();
    }

    public void initBuscadorToAnular(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        editing = false;
        anulando = true;
        MODO_VISTA = false;
        showBuscador(owner, false, true);
        buscador.getCheckAnulada().setVisible(true);
        buscador.getCheckAnulada().setEnabled(false);
        buscador.getCheckAnulada().setSelected(false);
    }

    /**
     * Inicializa y hace visible la UI (modal) de busqueda y selección de Remito
     * para relacionarlo con una {@link FacturaVenta}
     *
     * @param owner
     * @param cliente entity {@link Cliente} del cual se van a buscar el Remito
     */
    void initBuscadorToFacturar(JDialog owner, Cliente cliente) {
        buscador = new JDBuscadorReRe(owner, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
        buscador.setParaRemito();
        initBuscador(false);
        buscador.hideFormaPago();
        buscador.getCheckAnulada().setSelected(false);
        buscador.getCheckAnulada().setEnabled(false);
        setToFacturar();
        UTIL.setSelectedItem(buscador.getCbClieProv(), cliente);
        buscador.getCbFormasDePago().setSelectedIndex(1); // los NO facturados
        buscador.getCbFormasDePago().setEnabled(false);
        buscador.getCbClieProv().setEnabled(false);
        buscador.setVisible(true);
    }
}
