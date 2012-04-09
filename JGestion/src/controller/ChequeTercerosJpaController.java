package controller;

import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Banco;
import entity.BancoSucursal;
import entity.Caja;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.DetalleRecibo;
import entity.FacturaVenta;
import entity.Librado;
import entity.Recibo;
import entity.enums.ChequeEstado;
import gui.JDABM;
import gui.JDChequesManager;
import gui.JDContenedor;
import gui.PanelABMCheques;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import jpa.controller.CajaMovimientosJpaController;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class ChequeTercerosJpaController implements ActionListener, Serializable {

    public final String CLASS_NAME = ChequeTerceros.class.getSimpleName();
    public final static int MAX_LENGHT_DIGITS_QUANTITY = 20;
    private ChequeTerceros EL_OBJECT;
    private JDContenedor contenedor;
    private EntityManager entityManager;
    private static Logger LOGGER = Logger.getLogger(ChequePropioJpaController.class);
    private JDABM abm;
    private PanelABMCheques panelABM;
    private Object objectToBound;
    //exclusively related to the GUI
    private JDChequesManager jdChequeManager;
    private final String[] columnNames = {"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Sucursal", "Importe", "Estado", "Librado", "Cruzado", "Usuario"};
    private final int[] columnWidths = {1, 50, 50, 100, 50, 70, 70, 50, 30, 30, 20, 50};
    private final Class[] columnClassTypes = {Integer.class, Number.class, null, null, null, null, null, Number.class, null, null, Boolean.class, null};
    //Este se pone en el comboBox
    private final String[] orderByToComboBoxList = {"N° Cheque", "Fecha de Emisión", "Fecha de Cobro", "Importe", "Banco/Sucursal", "Cliente", "Estado"};
    //Y este es el equivalente (de lo seleccionado en el combo) para el SQL.
    private final String[] orderByToQueryKeyList = {"numero", "fecha_cheque", "fecha_cobro", "importe", "banco.nombre, banco_sucursal.nombre", "cliente.nombre", "estado"};

    public ChequeTercerosJpaController() {
    }

    //<editor-fold defaultstate="collapsed" desc="DAO - CRUD Methods">
    public EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public void create(ChequeTerceros chequeTerceros) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(chequeTerceros);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ChequeTerceros chequeTerceros) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            chequeTerceros = em.merge(chequeTerceros);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = chequeTerceros.getId();
                if (findChequeTerceros(id) == null) {
                    throw new NonexistentEntityException("The chequeTerceros N° " + chequeTerceros.getNumero() + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ChequeTerceros chequeTerceros;
            try {
                chequeTerceros = em.getReference(ChequeTerceros.class, id);
                chequeTerceros.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The chequeTerceros with id " + id + " no longer exists.", enfe);
            }
            em.remove(chequeTerceros);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ChequeTerceros> findChequeTercerosEntities() {
        return findChequeTercerosEntities(true, -1, -1);
    }

    public List<ChequeTerceros> findChequeTercerosEntities(int maxResults, int firstResult) {
        return findChequeTercerosEntities(false, maxResults, firstResult);
    }

    private List<ChequeTerceros> findChequeTercerosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from ChequeTerceros as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public ChequeTerceros findChequeTerceros(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ChequeTerceros.class, id);
        } finally {
            em.close();
        }
    }

    public int getChequeTercerosCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from ChequeTerceros as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    //</editor-fold>

    /**
     *
     * @param isEditing
     * @param e se posicionará a la ventana en relación a este, can be null.
     * @throws MessageException
     */
    private JDialog initABM(boolean isEditing) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }

        panelABM = new PanelABMCheques();
        panelABM.getLabelEmisor().setText("Emisor (Cliente)");
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoJpaController().findEntities(), true);
        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ClienteJpaController().findEntities(), false);
        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);

        panelABM.getCbBancos().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) panelABM.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), new BancoSucursalJpaController().findEntitiesFrom(banco), false);
                } else {
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        panelABM.getCbBancoSucursales().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BancoSucursal bs = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
                    panelABM.getTfSucursalDireccion().setText(bs.getDireccion());
                } catch (Exception ex) {
                    panelABM.getTfSucursalDireccion().setText("");
                }
            }
        });
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(true, contenedor, panelABM);
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        abm.setListener(this);
        return abm;
    }

    private void setPanel(ChequeTerceros EL_OBJECT) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ChequeTerceros getEntity() throws MessageException {
        List<String> erroresList = new ArrayList<String>(0);
        Date fechaCheque, fechaCobro = null, fechaEndoso = null;
        Long numero = null;
        BigDecimal importe = null;
        Banco banco;
        BancoSucursal sucursal = null;
        Librado librado;
        Cliente cliente;
        boolean cruzado, propio, endosado;
        String endosatario = null, observacion = null;

        if (panelABM.getDcCheque() == null) {
            erroresList.add("Debe ingresar la Fecha del Cheque.");
        }
        fechaCheque = panelABM.getDcCheque();
        if (panelABM.getDcCobro() != null) {
            fechaCobro = panelABM.getDcCobro();
            fechaCheque = UTIL.getDateYYYYMMDD(fechaCheque);
            fechaCobro = UTIL.getDateYYYYMMDD(fechaCobro);
            if (fechaCheque.after(fechaCobro)) {
                erroresList.add("Fecha de cobro no puede ser anterior a Fecha de cheque.");
            }
        }
        try {
            numero = Long.valueOf(panelABM.getTfNumero().getText());
        } catch (NumberFormatException numberFormatException) {
            erroresList.add("Número de cheque no válido");
        }
        try {
            importe = BigDecimal.valueOf(Double.valueOf(panelABM.getTfImporte().getText()));
        } catch (NumberFormatException numberFormatException) {
            erroresList.add("Importe no válido");
        }
        if (panelABM.getCbBancos().getSelectedIndex() < 1) {
            erroresList.add("Debe elegir un Banco");
        }
        banco = (Banco) panelABM.getCbBancos().getSelectedItem();
        try {
            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
        } catch (ClassCastException e) {
            erroresList.add("Sucursal de Banco no válida");
        }
        librado = (Librado) panelABM.getCbLibrado().getSelectedItem();
        cliente = (Cliente) panelABM.getCbEmisor().getSelectedItem();
        cruzado = panelABM.getCheckCruzado().isSelected();
//        propio = panelABM.getCheckPropio().isSelected();
        observacion = panelABM.getTaObservacion().getText();
        endosado = panelABM.getCheckEndosado().isSelected();
        if (endosado) {
            endosatario = panelABM.getTfEndosatario().getText().trim();
            if (endosatario == null || endosatario.length() < 1) {
                erroresList.add("Debe especificar un Endosatario si há seleccionado la opción de endosado.");
            }
        }
        if (!erroresList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String string : erroresList) {
                sb.append(string);
                sb.append("\n");
            }
            throw new MessageException(sb.toString());
        }
        ChequeTerceros newCheque = new ChequeTerceros(null, numero, fechaCheque,
                cruzado, observacion, ChequeEstado.CARTERA, fechaCobro, endosatario,
                fechaEndoso, importe, UsuarioJpaController.getCurrentUser(), librado, cliente, sucursal, banco);
        return newCheque;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();

            //<editor-fold defaultstate="collapsed" desc="contenedor EVENTS">
            if (contenedor != null) {
                if (boton.getName().equalsIgnoreCase("new")) {
                    try {
                        EL_OBJECT = null;
                        JDialog jd = initABM(false);
                        jd.setLocationRelativeTo(contenedor);
                        jd.setVisible(true);
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    }
                } else if (boton.getName().equalsIgnoreCase("edit")) {
                    try {
                        int selectedRow = contenedor.getjTable1().getSelectedRow();
                        if (selectedRow > -1) {
                            EL_OBJECT = DAO.getEntityManager().find(ChequeTerceros.class,
                                    Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                            JDialog jd = initABM(true);
                            jd.setLocationRelativeTo(contenedor);
                            jd.setVisible(true);

                        }
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    }

                } else if (boton.getName().equalsIgnoreCase("del")) {
                    try {
                        int selectedRow = contenedor.getjTable1().getSelectedRow();
                        if (selectedRow > -1) {
                            EL_OBJECT = DAO.getEntityManager().find(ChequeTerceros.class,
                                    Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                        }
                        if (EL_OBJECT == null) {
                            throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
                        }
                        destroy(EL_OBJECT.getId());
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (NonexistentEntityException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    } catch (Exception ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        LOGGER.error(ex);
                    }
                } else if (boton.getName().equalsIgnoreCase("Print")) {
                    //no implementado aun...
                } else if (boton.getName().equalsIgnoreCase("exit")) {
                    contenedor.dispose();
                    contenedor = null;
                }
            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="abm & panelABM EVENTS">
            else if (abm != null && panelABM != null) {
                if (boton.getName().equalsIgnoreCase(abm.getbAceptar().getName())) {
                    try {
                        Integer id = null;
                        if (EL_OBJECT != null) {
                            id = EL_OBJECT.getId();
                        }
                        ChequeTerceros cheque = getEntity();
                        if (id != null) {
                            cheque.setId(id);
                        }
                        checkConstraints(cheque);
                        String msg = cheque.getId() == null ? "Registrado" : "Modificado";
                        if (cheque.getId() == null) {
                            create(cheque);
                        } else {
                            edit(cheque);
                        }
                        EL_OBJECT = cheque;
                        abm.showMessage(msg, CLASS_NAME, 1);
                        //                    cargarContenedorTabla(null);
                        abm.dispose();
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                        LOGGER.error(ex);
                    }
                } else if (boton.getName().equalsIgnoreCase(abm.getbCancelar().getName())) {
                    abm.dispose();
                    panelABM = null;
                    abm = null;
                    EL_OBJECT = null;
                } else if (boton.getName().equalsIgnoreCase("bAddBanco")) {
                    try {
                        JDialog initABM = new BancoJpaController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                    UTIL.loadComboBox(panelABM.getCbBancos(), new BancoJpaController().findEntities(), true);
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar Banco>");
                } else if (boton.getName().equalsIgnoreCase("bAddSucursal")) {
                    Banco bancoSelected = (Banco) panelABM.getCbBancos().getSelectedItem();
                    try {
                        JDialog initABM = new BancoSucursalJpaController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSelected);
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                } else if (boton.getName().equalsIgnoreCase("baAddEmisor")) {
                    JDialog initContenedor = new ClienteJpaController().initContenedor(null, true);
                    initContenedor.setVisible(true);
                }
            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="jdChequeManager EVENTS">
            else if (jdChequeManager != null) {
                if (boton.equals(jdChequeManager.getbBuscar())) {
                    armarQuery(false);
                } else if (boton.equals(jdChequeManager.getbLimpiar())) {
                } else if (boton.equals(jdChequeManager.getbACaja())) {
                    int selectedRow = jdChequeManager.getjTable1().getSelectedRow();
                    try {
                        if (selectedRow < 0) {
                            throw new MessageException("No ha seleccionado ningún fila de la tabla");
                        }
                        ChequeTerceros chequeTerceros = findChequeTerceros(Integer.valueOf(UTIL.getSelectedValue(jdChequeManager.getjTable1(), 0).toString()));
                        if (chequeTerceros.getEstado().equals(ChequeEstado.CAJA)) {
                            throw new MessageException("El Cheque seleccionado ya fue movido a una Caja");
                        } else if (chequeTerceros.getEstado().equals(ChequeEstado.RECHAZADO)) {
                            throw new MessageException("Un Cheque " + ChequeEstado.RECHAZADO + " no puede ser movido a una Caja");
                        }
                        Caja cajaToAsentar = initUIAsentarChequeToCaja();
                        try {
                            new CajaMovimientosJpaController().asentarMovimiento(chequeTerceros, cajaToAsentar);
                            chequeTerceros.setEstado(ChequeEstado.CAJA);
                            edit(chequeTerceros);
                        } catch (NonexistentEntityException ex) {
                            JOptionPane.showMessageDialog(jdChequeManager, ex.getMessage());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(jdChequeManager, ex.getMessage());
                            LOGGER.error("Error asentar Cheque a Caja", ex);
                        }
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(jdChequeManager, ex.getMessage());

                    }

                } else if (boton.equals(jdChequeManager.getbAnular())) {
                } else if (boton.equals(jdChequeManager.getbDeposito())) {
                } else if (boton.equals(jdChequeManager.getbImprimir())) {
                }
            }
            //</editor-fold>
        }// </editor-fold>
    }

    private void checkConstraints(ChequeTerceros object) throws MessageException {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id<>" + object.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
                    + "WHERE " + idQuery
                    + " o.numero='" + object.getNumero() + "'"
                    + " AND o.cliente.id=" + object.getCliente().getId(), object.getClass()).getSingleResult();
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este número del Cliente + " + object.getCliente().getNombre());
        } catch (NoResultException noResultException) {
        }

        //checkeo de restricciones según la relación (bound)
        if (objectToBound != null) {
            try {
                Recibo recibo = (Recibo) objectToBound;
                BigDecimal monto = new BigDecimal(0);
                for (DetalleRecibo detalleRecibo : recibo.getDetalleReciboList()) {
                    if (detalleRecibo.isAcreditado()) {
                        monto = monto.add(new BigDecimal(detalleRecibo.getMontoEntrega()));
                    }
                }
                if (object.getImporte().doubleValue() < monto.doubleValue()) {
                    if ((monto.doubleValue() - recibo.getMonto()) == 0) {
                        throw new MessageException("El monto del cheque no puede ser inferior al del recibo ($" + recibo.getMonto() + ")");
                    } else {
                        throw new MessageException("El monto del cheque no puede ser inferior al monto no acreditado ($" + monto.doubleValue() + ")");
                    }
                }
            } catch (ClassCastException e) {
                //ignored...
            }
            try {
                FacturaVenta fv = (FacturaVenta) objectToBound;
                if (fv.getFormaPagoEnum().equals(Valores.FormaPago.CHEQUE)) {
                    if (fv.getImporte() > object.getImporte().doubleValue()) {
                        throw new MessageException("El monto del cheque no puede ser inferior al de la factura ($" + fv.getImporte() + ")");
                    }
                }
            } catch (ClassCastException e) {
                //regardless
            }
        }
    }

    /**
     * Levanta la UI de ABM Cheques, para la creación de uno y relacionar este
     * al
     * {@link Recibo}.
     * {@link ChequeTerceros#bound} will be set as {@link DetalleCajaMovimientosJpaController#RECIBO}
     *
     * @param recibo
     * @return instancia de {@link ChequeTerceros}, a la cual solo le falta
     * settear
     * {@link ChequeTerceros#boundId} == {@link Recibo#id}
     * @throws MessageException Si la creación es cancelada o interrumpida.
     */
    ChequeTerceros getABMCheque(Recibo recibo) throws MessageException {
        objectToBound = recibo;
        initABM(false);
        Cliente cliente = recibo.getDetalleReciboList().get(0).getFacturaVenta().getCliente();
        if (cliente != null) {
            UTIL.setSelectedItem(panelABM.getCbEmisor(), cliente);
        }
        BigDecimal importeNoAcreditado = new BigDecimal(0);
        for (DetalleRecibo detalleRecibo : recibo.getDetalleReciboList()) {
            if (!detalleRecibo.isAcreditado()) {
                importeNoAcreditado = importeNoAcreditado.add(new BigDecimal(detalleRecibo.getMontoEntrega()));
            }
        }
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(importeNoAcreditado.doubleValue()));
        abm.setVisible(true);
        if (EL_OBJECT == null) {
            throw new MessageException("Creación de Cheque cancelada.");
        }
        EL_OBJECT.setBound(DetalleCajaMovimientosJpaController.RECIBO);
        return EL_OBJECT;
    }

    /**
     * Get the GUI ABM de cheques, seteada con algunos valores de la
     * FacturaVenta.
     *
     * @param newFacturaVenta
     * @return
     * @throws MessageException
     */
    JDialog getABMCheque(FacturaVenta newFacturaVenta) throws MessageException {
        initABM(false);
        objectToBound = newFacturaVenta;
        Cliente cliente = newFacturaVenta.getCliente();
        UTIL.setSelectedItem(panelABM.getCbEmisor(), cliente);
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(newFacturaVenta.getImporte()));
        panelABM.getCheckPropio().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCheckPropio().isSelected()) {
                    ChequePropioJpaController chequePropioJpaController = new ChequePropioJpaController();
                    JDialog initManager = chequePropioJpaController.initManager(null, true);
                    initManager.setVisible(true);
                }
            }
        });
        return abm;
    }

    public ChequeTerceros findChequeTerceros(FacturaVenta facturaVenta) {
        ChequeTerceros cheque;
        cheque = (ChequeTerceros) getEntityManager().
                createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.bound=" + DetalleCajaMovimientosJpaController.FACTU_VENTA
                + " AND o.boundId=" + facturaVenta.getId()).setHint(QueryHints.REFRESH, true).getSingleResult();
        return cheque;
    }

    public ChequeTerceros findChequeTerceros(Recibo recibo) {
        ChequeTerceros cheque;
        cheque = (ChequeTerceros) getEntityManager().
                createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.bound=" + DetalleCajaMovimientosJpaController.RECIBO
                + " AND o.boundId=" + recibo.getId()).
                setHint(QueryHints.REFRESH, true).getSingleResult();
        return cheque;
    }

    /**
     * Inicializa la vista encargada de administrar cheques (Propios o Terceros)
     *
     * @param parent JFrame padre.
     * @param listener Para los botones laterales. Si
     * <code>listener == null</code> se asignará
     * <code>this</code> por defecto.
     * @return una instancia de {@link JDChequesManager}.
     */
    public JDialog initManager(JFrame parent, ActionListener listener) {
        initManager(parent);
        if (listener != null) {
            jdChequeManager.addButtonListener(listener);
        } else {
            jdChequeManager.addButtonListener(this);
            jdChequeManager.getLabelEmisor().setText("Emisor por");
            UTIL.loadComboBox(jdChequeManager.getCbEmisor(), new ClienteJpaController().findEntities(), true);
            jdChequeManager.setTitle("Administración de Cheques Terceros");
        }
        return jdChequeManager;
    }

    private JDialog initManager(JFrame parent) {
        jdChequeManager = new JDChequesManager(parent, true);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), new BancoJpaController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(jdChequeManager.getCbLibrado(), new LibradoJpaController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        UTIL.loadComboBox(jdChequeManager.getCbOrderBy(), Arrays.asList(orderByToComboBoxList), false);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(), columnNames, columnWidths, columnClassTypes);
        UTIL.hideColumnTable(jdChequeManager.getjTable1(), 0);
        jdChequeManager.getCbBancos().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) jdChequeManager.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalJpaController().findEntitiesFrom(banco), true);
                } else {
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        return jdChequeManager;
    }

    private void cargarTablaChequeManager(String query) {
        DefaultTableModel dtm = UTIL.getDtm(jdChequeManager.getjTable1());
        UTIL.limpiarDtm(dtm);
        List<Object[]> resultList = getEntityManager().createNativeQuery(query).getResultList();
        for (Object[] objects : resultList) {
            dtm.addRow(objects);
        }
    }

    private void armarQuery(boolean imprimir) {
        StringBuilder query = new StringBuilder("SELECT "
                + " c.id, c.numero, TO_CHAR(c.fecha_cheque,'DD/MM/YYYY'), cliente.nombre as cliente, TO_CHAR(c.fecha_cobro,'DD/MM/YYYY'),"
                + " banco.nombre as banco, banco_sucursal.nombre as sucursal,"
                + " c.importe, cheque_estado.nombre  as estado, librado.nombre as librado, c.cruzado,"
                + " usuario.nick as usuario, "
                + " c.endosatario, c.fecha_endoso, "
                + " c.bound, c.bound_id "
                + " FROM public.cheque_terceros c "
                + " JOIN public.banco ON (c.banco = banco.id) "
                + " JOIN public.banco_sucursal ON (c.banco_sucursal = banco_sucursal.id) "
                + " JOIN public.librado ON (c.librado = librado.id) "
                + " JOIN public.cliente ON (c.cliente = cliente.id) "
                + " JOIN public.usuario ON (c.usuario = usuario.id) "
                + " JOIN cheque_estado  ON (c.estado  = cheque_estado.id)"
                + " WHERE c.id > -1");
        if (jdChequeManager.getTfChequeNumero().getText().trim().length() > 0) {
            try {
                Long numero = Long.valueOf(jdChequeManager.getTfChequeNumero().getText());
                query.append(" AND c.numero='").append(numero).append("'");
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getDcEmisionDesde() != null) {
            query.append(" AND c.fecha_cheque >='").append(jdChequeManager.getDcEmisionDesde()).append("'");
        }
        if (jdChequeManager.getDcEmisionHasta() != null) {
            query.append(" AND c.fecha_cheque <='").append(jdChequeManager.getDcEmisionHasta()).append("'");
        }
        if (jdChequeManager.getDcCobroDesde() != null) {
            query.append(" AND c.fecha_cobro >='").append(jdChequeManager.getDcCobroDesde()).append("'");
        }
        if (jdChequeManager.getDcCobroHasta() != null) {
            query.append(" AND c.fecha_cobro <='").append(jdChequeManager.getDcCobroHasta()).append("'");
        }
        if (jdChequeManager.getTfImporte().getText().trim().length() > 0) {
            try {
                Double importe = Double.valueOf(jdChequeManager.getTfImporte().getText());
                query.append(" AND c.importe").append(jdChequeManager.getCbImporteCondicion().getSelectedItem().toString()).append(importe);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND c.banco=").append(((Banco) jdChequeManager.getCbBancos().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbBancoSucursales().getSelectedIndex() > 0) {
            query.append(" AND c.banco_sucursal=").append(((BancoSucursal) jdChequeManager.getCbBancoSucursales().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEmisor().getSelectedIndex() > 0) {
            query.append(" AND c.cliente=").append(((Cliente) jdChequeManager.getCbEmisor().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbLibrado().getSelectedIndex() > 0) {
            query.append(" AND c.librado=").append(((Librado) jdChequeManager.getCbLibrado().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEstados().getSelectedIndex() > 0) {
            query.append(" AND c.estado=").append(((ChequeEstado) jdChequeManager.getCbEstados().getSelectedItem()).getId());
        }

        query.append(" ORDER BY ").append(orderByToQueryKeyList[jdChequeManager.getCbOrderBy().getSelectedIndex()]);
        System.out.println(query.toString());
        cargarTablaChequeManager(query.toString());
        if (imprimir) {
            doChequeTercerosReport(query.toString());
        }
    }

    private void doChequeTercerosReport(String query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    ChequeTerceros getChequeTerceroInstance() {
        return EL_OBJECT;
    }

    /**
     * Levanta una UI de selección de Caja, en la cual se va asentar el cheque.
     *
     * @return instance of Caja si seleccionó
     */
    private Caja initUIAsentarChequeToCaja() {
        Caja caja = null;
        JPanel p = new JPanel(new GridLayout(3, 1));
        p.add(new JLabel("Seleccionar Caja destino"));
        final JComboBox cbCajas = new JComboBox();
        UTIL.loadComboBox(cbCajas, new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), Boolean.TRUE), false);
        p.add(cbCajas);
        abm = new JDABM(true, jdChequeManager, "Asentar Cheque a Caja", p);
        abm.getbAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Caja selectedCaja = (Caja) cbCajas.getSelectedItem();
                    abm.setAceptarAsLastButtonPressed();
                    abm.dispose();
                } catch (ClassCastException ex) {
                    abm.showMessage("No ha seleccionado ninguna Caja", "Error", 2);
                }
            }
        });
        abm.setVisible(true);
        if (abm.isAceptarLastButtonPressed()) {
            caja = (Caja) cbCajas.getSelectedItem();
        }
        return caja;
    }
}
