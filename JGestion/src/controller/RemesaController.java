package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.CtacteProveedor;
import entity.Remesa;
import entity.Sucursal;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleRemesa;
import entity.FacturaCompra;
import entity.Proveedor;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.RemesaJpaController;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class RemesaController implements ActionListener, FocusListener {

    private final String CLASS_NAME = Remesa.class.getSimpleName();
    private static final String[] COLUMN_NAMES = {"facturaID", "Factura", "Observación", "Entrega", "Acredidato"};
    private static final int[] COLUMN_WIDTH = {1, 50, 150, 30, 10};
    private static final Class[] COLUMN_CLASS = {Object.class, Object.class, String.class, Double.class, Boolean.class};
    private JDReRe jdReRe;
    private CtacteProveedor selectedCtaCte;
    private Date selectedFechaReRe = null;
    private JDBuscadorReRe buscador;
    private Remesa rereSelected;
    private static Logger LOGGER = Logger.getLogger(RemesaController.class);
    private RemesaJpaController jpaController;

    public RemesaController() {
        jpaController = new RemesaJpaController();
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    private Long getNextNumeroRemesa() {
        EntityManager em = getEntityManager();
        Long next_factu = 100000001L;
        try {
            next_factu = 1 + (Long) em.createQuery("SELECT MAX(o.id)"
                    + " FROM " + CLASS_NAME + " o").getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("pintó la 1ra " + CLASS_NAME + "....NoResultEx");
        } catch (NullPointerException ex) {
            System.out.println("pintó la 1ra " + CLASS_NAME + "....NullPointerEx");
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return next_factu;
    }

    public void initRemesa(JFrame frame, boolean modal) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.COMPRA);
        jdReRe = new JDReRe(frame, modal);
        jdReRe.setUIForRemesas();
        UTIL.getDefaultTableModel(jdReRe.getjTable1(), COLUMN_NAMES, COLUMN_WIDTH, COLUMN_CLASS);
        UTIL.loadComboBox(jdReRe.getCbSucursal(), new UsuarioHelper().getSucursales(), false);
        UTIL.loadComboBox(jdReRe.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);
        UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
        jdReRe.getbAnular().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    anular(rereSelected);
                    jdReRe.showMessage(CLASS_NAME + " anulada..", CLASS_NAME, 1);
                    resetPanel();
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        jdReRe.getbAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkConstraints();
                    Remesa re = setEntity();
                    jpaController.create(re);
                    for (DetalleRemesa detalleRemesa : re.getDetalleRemesaList()) {
                        //actuliza saldo pagado de cada ctacte
                        actualizarMontoEntrega(detalleRemesa.getFacturaCompra(), detalleRemesa.getMontoEntrega());
                    }
                    Long numeroRemesa = re.getNumero();
                    jdReRe.showMessage(CLASS_NAME + "Nº" + numeroRemesa + " registrada..", CLASS_NAME, 1);
                    limpiarDetalle();
                    resetPanel();

                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
        jdReRe.getBtnADD().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addEntregaToDetalle();
                } catch (MessageException ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
        jdReRe.getBtnDEL().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                delEntragaFromDetalle();
            }
        });
        jdReRe.getCbClienteProveedor().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdReRe.getCbClienteProveedor().getSelectedIndex() > 0) {
                    cargarCtaCtes((Proveedor) jdReRe.getCbClienteProveedor().getSelectedItem());
                } else {
                    //si no eligió nada.. vacia el combo de cta cte's
                    UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
                    limpiarDetalle();
                }
            }
        });
        jdReRe.getCbCtaCtes().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    try {
                        selectedCtaCte = (CtacteProveedor) jdReRe.getCbCtaCtes().getSelectedItem();
                        jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                        jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                        jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
                    } catch (ClassCastException ex) {
                        selectedCtaCte = null;
                        LOGGER.trace("No se pudo caster a CtaCteProveedor -> " + jdReRe.getCbCtaCtes().getSelectedItem());
                    }
                } catch (NullPointerException ex) {
                    //cuando no eligio una ctacte aún o el cliente/proveedor no tiene ninguna
                }
            }
        });
        jdReRe.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {
        if (buscador != null) {
            if (e.getClickCount() > 1) {
                setSelectedRemesa();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private void checkConstraints() throws MessageException {
        if (jdReRe.getDtm().getRowCount() < 1) {
            throw new MessageException("No ha hecho ninguna entrega");
        }

        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Fecha de " + CLASS_NAME + " no válida");
        }

    }

    private Remesa setEntity() throws Exception {
        Remesa re = new Remesa();
        re.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
        re.setSucursal((Sucursal) jdReRe.getCbSucursal().getSelectedItem());
        re.setUsuario(UsuarioJpaController.getCurrentUser());
        re.setEstado(true);
        re.setFechaRemesa(jdReRe.getDcFechaReRe());
        re.setMontoEntrega(Double.parseDouble(jdReRe.getTfTotalPagado()));
        // 30% faster on ArrayList with initialCapacity
        re.setDetalleRemesaList(new ArrayList<DetalleRemesa>(jdReRe.getDtm().getRowCount()));
        DefaultTableModel dtm = jdReRe.getDtm();
        FacturaCompraController fcc = new FacturaCompraController();
        DetalleRemesa detalle;
//        double montoParaDesacreditar =0.0;
        for (int i = 0; i < dtm.getRowCount(); i++) {
            detalle = new DetalleRemesa();
            detalle.setFacturaCompra(fcc.findFacturaCompra(Integer.valueOf(dtm.getValueAt(i, 0).toString())));
            detalle.setObservacion(dtm.getValueAt(i, 2).toString());
            detalle.setMontoEntrega(Double.parseDouble(dtm.getValueAt(i, 3).toString()));
            detalle.setAcreditado((Boolean) dtm.getValueAt(i, 4));
            detalle.setRemesa(re);
            //detalle.setRemesa(re); <--- no hace falta...
            re.getDetalleRemesaList().add(detalle);
//            if (detalle.isAcreditado()) {
//                montoParaDesacreditar += detalle.getMontoEntrega();
//            }
        }
        //registrando pago en CAJA
        new CajaMovimientosJpaController().asentarMovimiento(re);
//        if (montoParaDesacreditar > 0) {
//            new NotaCreditoJpaController().desacreditar(re, (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem(), montoParaDesacreditar);
//        }
        return re;
    }

    private void actualizarMontoEntrega(FacturaCompra factu, double monto) {
        CtacteProveedor ctacte = new CtacteProveedorJpaController().findCtacteProveedorByFactura(factu.getId());
        Logger.getLogger(this.getClass()).trace("updatingMontoEntrega: CtaCte=" + ctacte.getId()
                + " -> Importe= $" + ctacte.getImporte() + " Entregado= $" + ctacte.getEntregado() + " + " + monto);

        ctacte.setEntregado(ctacte.getEntregado() + monto);
        if (ctacte.getImporte() == ctacte.getEntregado()) {
            ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getId());
            System.out.println("ctaCte PAGADA");
        }
        DAO.doMerge(ctacte);
    }

    private void limpiarDetalle() {
        UTIL.limpiarDtm(jdReRe.getjTable1());
        jdReRe.setTfImporte("0");
        jdReRe.setTfEntrega("");
        jdReRe.setTfObservacion("");
        jdReRe.setTfSaldo("0");
        jdReRe.setTfTotalPagado("0");
        selectedFechaReRe = null;
    }

    private void addEntregaToDetalle() throws MessageException {
        if (jdReRe.getDcFechaReRe() == null) {
            throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes");
        }

        if (selectedCtaCte == null) {
            throw new MessageException("No hay Factura seleccionada");
        }

        if (jdReRe.getDcFechaReRe().before(selectedCtaCte.getFactura().getFechaCompra())) {
            throw new MessageException("La fecha de la " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la deuda de la Cta. Cte. del Proveedor ("
                    + UTIL.DATE_FORMAT.format(selectedCtaCte.getFechaCarga()) + ")");
        }

        // si ya se cargó un detalle de entrega
        // y sigue habiendo al menos UN detalle agregado (dtm no vacia)
        // ctrla que la fecha de ReRe siga siendo la misma
        if ((selectedFechaReRe != null) && (jdReRe.getDtm().getRowCount() > 0)
                && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                    + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                    + "\nActual: " + UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe()));
        } else {
            selectedFechaReRe = jdReRe.getDcFechaReRe();
        }
        FacturaCompra facturaToAddToDetail = selectedCtaCte.getFactura();
        double entrega;
        String observacion = jdReRe.getTfObservacion();
        try {
            entrega = Double.parseDouble(jdReRe.getTfEntrega());
            if (entrega <= 0) {
                throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
            }

//         if(entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) )
            if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
                throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");
            }

        } catch (NumberFormatException e) {
            throw new MessageException("Monto de entrega no válido");
        }
        if (observacion.length() > 200) {
            throw new MessageException("La Observación no puede superar los 200 caracteres");
        }

        boolean acreditado = jdReRe.getCheckAcreditarEntrega().isSelected();

        double entregaParcial = 0;
        for (int i = 0; i < jdReRe.getDtm().getRowCount(); i++) {
            if (facturaToAddToDetail.getId() == (Integer) jdReRe.getDtm().getValueAt(i, 0)) {
                entregaParcial += (Double) jdReRe.getDtm().getValueAt(i, 3);
                if (acreditado == (Boolean) jdReRe.getDtm().getValueAt(i, 4)) {
                    throw new MessageException("El detalle ya contiene una entrega "
                            + (acreditado ? " (ACREDITADA)" : "") + " de esta factura.");
                }
            }
        }
        if ((entrega + entregaParcial) > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("El monto de esta entrega $" + entrega + " + $" + entregaParcial
                    + "\nsuperan la deuda de la Factura $" + (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
        }
        double restante;
        if (acreditado) {
            restante = Double.parseDouble(jdReRe.getTfRestanteCreditoDebito().getText());
            //si se intenta hacer un pago superior a la deuda
            if (restante < entrega) {
                throw new MessageException("La entrega no puede superar el deuda");
            }
        }
        DefaultTableModel dtm = jdReRe.getDtm();
        dtm.addRow(new Object[]{
                    facturaToAddToDetail.getId(),
                    UTIL.AGREGAR_CEROS(String.valueOf(facturaToAddToDetail.getNumero()), 12),
                    observacion,
                    entrega,
                    acreditado
                });
        double totalEntregado = Double.valueOf(jdReRe.getTfTotalPagado());
        jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(totalEntregado + entrega));

    }

    private void delEntragaFromDetalle() {
        int selectedRow = jdReRe.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            double entrega = Double.valueOf(jdReRe.getDtm().getValueAt(selectedRow, 2).toString());
            double totalEntregado = Double.valueOf(jdReRe.getTfTotalPagado());
            jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(totalEntregado - entrega));
            jdReRe.getDtm().removeRow(selectedRow);
        }
    }

    public JDialog initBuscador(JDialog dialog, boolean modal) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.COMPRA);
        buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Proveedor", "Nº " + CLASS_NAME);
        buscador.setLocationRelativeTo(dialog);
        buscador.setListeners(this);
        UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new UsuarioHelper().getCajas(true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Nº", "Monto", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha/Hora (Sist)"},
                new int[]{50, 30, 40, 50, 50, 50, 70});
        buscador.getbBuscar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        buscador.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                }
            }
        });
        return buscador;
    }

    private void cargarCtaCtes(Proveedor proveedor) {
        limpiarDetalle();
        List<CtacteProveedor> ctacteProveedorPendientesList = new CtacteProveedorJpaController().findCtacteProveedorByProveedor(proveedor.getId(), Valores.CtaCteEstado.PENDIENTE.getId());
        UTIL.loadComboBox(jdReRe.getCbCtaCtes(), ctacteProveedorPendientesList, false);

    }

    private void resetPanel() {
        jdReRe.setDcFechaReRe(new Date());
        jdReRe.getCbClienteProveedor().setSelectedIndex(0);
        bloquearVentana(false);
    }

    private void armarQuery() throws MessageException {
        String query = "SELECT o.* FROM remesa o, proveedor p , caja c, detalle_remesa dr, factura_compra f, usuario u, sucursal s  "
                + " WHERE o.id = dr.remesa "
                + " AND o.caja = c.id "
                + " AND o.sucursal = s.id "
                + " AND f.id = dr.factura_compra "
                + " AND p.id = f.proveedor ";

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
                query += " AND o.id = " + numero;
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
                query += " AND f.numero = " + numero;
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query += " AND o.fecha_remesa >= '" + buscador.getDcDesde() + "'";
        }
        if (buscador.getDcHasta() != null) {
            query += " AND o.fecha_remesa <= '" + buscador.getDcHasta() + "'";
        }
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            query += " AND o.caja = " + ((Caja) buscador.getCbCaja().getSelectedItem()).getId();
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
        }
        if (buscador.isCheckAnuladaSelected()) {
            query += " AND o.estado = false";
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query += " AND p.id = " + ((Proveedor) buscador.getCbClieProv().getSelectedItem()).getId();
        }

        query += " GROUP BY o.id, o.fecha_carga, o.monto_entrega, o.usuario, o.caja, o.sucursal, o.fecha_remesa, o.estado"
                + " ORDER BY o.id";
        System.out.println("QUERY: " + query);
        cargarDtmBuscador(query);
    }

    private void cargarDtmBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        List<Remesa> l = DAO.getEntityManager().createNativeQuery(query, Remesa.class).getResultList();
        for (Remesa remesa : l) {
            dtm.addRow(new Object[]{
                        remesa.getNumero(),
                        remesa.getMonto(),
                        UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()),
                        remesa.getSucursal(),
                        remesa.getCaja(),
                        remesa.getUsuario(),
                        UTIL.DATE_FORMAT.format(remesa.getFechaCarga()) + " - " + UTIL.TIME_FORMAT.format(remesa.getFechaCarga())
                    });
        }
    }

    private void setSelectedRemesa() {
        int rowIndex = buscador.getjTable1().getSelectedRow();
        long numero = Long.valueOf(buscador.getjTable1().getValueAt(rowIndex, 0).toString());
        rereSelected = jpaController.findByNumero(numero);
        if (rereSelected != null) {
            buscador.dispose();
        }


    }

    /**
     * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
     * detalles de la Remesa, imprimir y ANULAR, pero NO MODIFICAR
     *
     * @param remesa
     */
    private void setDatosCtaCte(Remesa remesa) {
        bloquearVentana(true);
        String numero = UTIL.AGREGAR_CEROS(String.valueOf(remesa.getNumero()), 12);
        jdReRe.setTfCuarto(numero.substring(0, 4));
        jdReRe.setTfOcteto(numero.substring(4));

        //por no redundar en DATOOOOOOOOOSS...!!!
        Proveedor p = new FacturaCompraController().findFacturaCompra(remesa.getDetalleRemesaList().get(0).getFacturaCompra().getId()).getProveedor();

        jdReRe.setDcFechaReRe(remesa.getFechaRemesa());
        jdReRe.setDcFechaCarga(remesa.getFechaCarga());

        //Uso los .toString por el 1er Item de los combos <Vacio> o <Elegir>
        // van a tirar error de ClassCastException
        UTIL.setSelectedItem(jdReRe.getCbSucursal(), remesa.getSucursal().getNombre());
        UTIL.setSelectedItem(jdReRe.getCbCaja(), remesa.getCaja().toString());
        UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), p.toString());

        cargarDetalleReRe(remesa.getDetalleRemesaList());

        jdReRe.setTfImporte("");
        jdReRe.setTfPagado("");
        jdReRe.setTfSaldo("");
        jdReRe.setTfTotalPagado(String.valueOf(remesa.getMonto()));
    }

    private void cargarDetalleReRe(List<DetalleRemesa> detalleRemesaList) {
        UTIL.limpiarDtm(jdReRe.getjTable1());
        DefaultTableModel dtm = jdReRe.getDtm();
        for (DetalleRemesa r : detalleRemesaList) {
            dtm.addRow(new Object[]{
                        UTIL.AGREGAR_CEROS(String.valueOf(r.getFacturaCompra().getNumero()), 12),
                        r.getObservacion(),
                        r.getMontoEntrega()
                    });
        }
    }

    public void focusGained(FocusEvent e) {
        //..........
    }

    public void focusLost(FocusEvent e) {
        if (buscador != null) {
            if (e.getSource().getClass().equals(javax.swing.JTextField.class)) {
                javax.swing.JTextField tf = (JTextField) e.getSource();
                if (tf.getName().equalsIgnoreCase("tfocteto")) {
                    if (buscador.getTfOcteto().length() > 0) {
                        buscador.setTfOcteto(UTIL.AGREGAR_CEROS(buscador.getTfOcteto(), 8));
                    }
                } else if (tf.getName().equalsIgnoreCase("tfFactu8")) {
                }
            }
        }

    }

    private void bloquearVentana(boolean b) {
        jdReRe.getbAnular().setEnabled(b);
        jdReRe.getbImprimir().setEnabled(b);
        jdReRe.getBtnADD().setEnabled(!b);
        jdReRe.getBtnDEL().setEnabled(!b);
        jdReRe.getbAceptar().setEnabled(!b);
        jdReRe.getCbCtaCtes().setEnabled(!b);
        jdReRe.getCbCaja().setEnabled(!b);
        jdReRe.getCbSucursal().setEnabled(!b);
        jdReRe.getCbClienteProveedor().setEnabled(!b);
        jdReRe.getDcFechaReRe(!b);
    }

    /**
     * La anulación de una Remesa, resta a
     * <code>CtaCteProveedor.entregado</code> los pagos/entregas
     * (parciales/totales) realizados de cada DetalleRemesa y cambia
     * <code>Remesa.estado = false<code>
     *
     * @throws MessageException
     * @throws IllegalOrphanException
     * @throws NonexistentEntityException
     */
    public void anular(Remesa remesa) throws MessageException, Exception {
        EntityManager em = getEntityManager();
        if (remesa == null) {
            throw new MessageException("Remesa is NULL");
        }
        if (!remesa.getEstado()) {
            throw new MessageException("Esta " + CLASS_NAME + " ya está anulada");
        }

        List<DetalleRemesa> detalleRemesaList = remesa.getDetalleRemesaList();
        CtacteProveedor ctaCteProveedor;
        try {
            em.getTransaction().begin();
            for (DetalleRemesa dr : detalleRemesaList) {
                //se resta la entrega ($) que implicaba este detalle con respecto a la factura
                ctaCteProveedor = new CtacteProveedorJpaController().findCtacteProveedorByFactura(dr.getFacturaCompra().getId());
                ctaCteProveedor.setEntregado(ctaCteProveedor.getEntregado() - dr.getMontoEntrega());
                // y si había sido pagada en su totalidad..
                if (ctaCteProveedor.getEstado() == Valores.CtaCteEstado.PAGADA.getId()) {
                    ctaCteProveedor.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
                }
                em.merge(ctaCteProveedor);
            }
            em.getTransaction().commit();
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
        remesa.setEstado(false);
        jpaController.merge(remesa);
    }

    List<Remesa> findByFactura(FacturaCompra factura) {
        List<DetalleRemesa> detalleRemesaList = jpaController.findDetalleRemesaByFactura(factura);
        List<Remesa> remesas = new ArrayList<Remesa>(detalleRemesaList.size());
        for (DetalleRemesa detalleRecibo : detalleRemesaList) {
            remesas.add(detalleRecibo.getRemesa());
        }
        return remesas;
    }
}