package jgestion.controller;

import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.controller.exceptions.PreexistingEntityException;
import jgestion.entity.Cliente;
import jgestion.entity.DetalleAcreditacion;
import jgestion.entity.DetalleRecibo;
import jgestion.entity.NotaCredito;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import jgestion.entity.DetalleNotaCredito;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Iva;
import jgestion.entity.Producto;
import jgestion.entity.Recibo;
import jgestion.entity.Sucursal;
import utilities.general.UTIL;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDFacturaVenta;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.NotaCreditoJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import utilities.general.EntityWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class NotaCreditoController {

    private static final Logger LOG = Logger.getLogger(NotaCreditoController.class.getName());
    private FacturaVentaController facturaVentaController;
    private JDFacturaVenta jdFacturaVenta;
    private JDBuscadorReRe buscador;
    private NotaCredito EL_OBJECT;
    private static int OBSERVACION_PROPERTY_LIMIT_LENGHT = 200;
    private NotaCreditoJpaController jpaController;

    public NotaCreditoController() {
        jpaController = new NotaCreditoJpaController();
    }

    public void initComprobanteUI(JFrame owner, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController = new FacturaVentaController();
        facturaVentaController.displayABM(owner, modal, this, 2, false, loadDefaultData);
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //deshabilita botón para no darle fruta
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    if (!facturaVentaController.getContenedor().isViewMode()) {
                        NotaCredito notaCreditoToPersist = setEntity();
                        jpaController.persist(notaCreditoToPersist);
                        reporte(notaCreditoToPersist);
                        facturaVentaController.setNumeroFactura(notaCreditoToPersist.getSucursal(), jpaController.getNextNumero(notaCreditoToPersist.getSucursal()));
                        facturaVentaController.borrarDetalles();
                    } else {
                        if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(jdFacturaVenta, "¿Desea reimprimir la Nota de crédito?", "Re imprimir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                            reporte(EL_OBJECT);
                        }
                    }
                } catch (MessageException ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    LOG.error(ex, ex);
                } finally {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
                }
            }
        });
        if (loadDefaultData) {
            try {
                @SuppressWarnings("unchecked")
                Sucursal s = ((EntityWrapper<Sucursal>) facturaVentaController.getContenedor().getCbSucursal().getSelectedItem()).getEntity();
                facturaVentaController.setNumeroFactura(s, jpaController.getNextNumero(s));
            } catch (Exception e) {
                throw new MessageException("Para realizar una Nota de Crédito debe tener habilitada al menos una Sucursal");
            }
        }
        facturaVentaController.getContenedor().setUIToNotaCredito();
        jdFacturaVenta = facturaVentaController.getContenedor();
        jdFacturaVenta.setVisible(setVisible);
    }

    private NotaCredito setEntity() throws MessageException, Exception {
        if (jdFacturaVenta.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de comprobante no válida");
        }
        Date fechaNotaCredito = jdFacturaVenta.getDcFechaFactura();
        Sucursal sucursal;
        Cliente cliente = (Cliente) jdFacturaVenta.getCbCliente().getSelectedItem();
        String observacion = null;
        try {
            sucursal = facturaVentaController.getSelectedSucursalFromJDFacturaVenta();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Sucursal."
                    + "\nMenú: Datos Generales -> Sucursales");
        }
        if (jdFacturaVenta.getTfObservacion().getText().trim().length() > 0) {
            observacion = jdFacturaVenta.getTfObservacion().getText().trim();
            if (observacion.length() >= OBSERVACION_PROPERTY_LIMIT_LENGHT) {
                throw new MessageException("La obsevación no puede tener mas de 250 caracteres (tamaño actual=" + observacion.length() + ").");
            }
        }
        DefaultTableModel dtm = jdFacturaVenta.getDtm();
        if (dtm.getRowCount() < 1) {
            throw new MessageException("No ha agregado ningún item al detalle.");
        } else if (dtm.getRowCount() >= FacturaVentaController.LIMITE_DE_ITEMS) {
            throw new MessageException("El límite del detalle son " + FacturaVentaController.LIMITE_DE_ITEMS + " items.");
        }

        NotaCredito newNotaCredito = new NotaCredito();
        newNotaCredito.setNumero(jpaController.getNextNumero(sucursal));
        newNotaCredito.setFechaNotaCredito(fechaNotaCredito);
        newNotaCredito.setImporte(new BigDecimal(UTIL.parseToDouble(jdFacturaVenta.getTfTotal())));
        newNotaCredito.setDesacreditado(BigDecimal.ZERO);
        newNotaCredito.setGravado(UTIL.parseToDouble(jdFacturaVenta.getTfGravado()));
        newNotaCredito.setNoGravado(new BigDecimal(UTIL.parseToDouble(jdFacturaVenta.getTfTotalNoGravado())));
        newNotaCredito.setIva10(UTIL.parseToDouble(jdFacturaVenta.getTfTotalIVA105()));
        newNotaCredito.setIva21(UTIL.parseToDouble(jdFacturaVenta.getTfTotalIVA21()));
        newNotaCredito.setImpuestosRecuperables(new BigDecimal(UTIL.parseToDouble(jdFacturaVenta.getTfTotalOtrosImps())));
        newNotaCredito.setCliente(cliente);
        newNotaCredito.setSucursal(sucursal);
        newNotaCredito.setUsuario(UsuarioController.getCurrentUser());
        newNotaCredito.setObservacion(observacion);
        newNotaCredito.setDetalleNotaCreditoCollection(new ArrayList<DetalleNotaCredito>(dtm.getRowCount()));
        ProductoJpaController productoJpaController = new ProductoJpaController();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            DetalleNotaCredito detalleVenta = new DetalleNotaCredito();
            detalleVenta.setCantidad((Integer) dtm.getValueAt(i, 3));
            detalleVenta.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
            detalleVenta.setProducto(productoJpaController.find((Integer) dtm.getValueAt(i, 9)));
            newNotaCredito.getDetalleNotaCreditoCollection().add(detalleVenta);
        }
        return newNotaCredito;
    }

    /**
     *
     * @param owner
     * @param toAnular
     * @param cliente
     * @param soloAcreditables no están ligadas a un Recibo aún.
     * @return
     * @throws MessageException
     */
    NotaCredito initBuscador(Window owner, final boolean toAnular, Cliente cliente, final boolean soloAcreditables) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        if (toAnular) {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.ANULAR_COMPROBANTES);
        }
        buscador = new JDBuscadorReRe(owner, "Buscador - Notas de crédito", true, "Cliente", "Nº");
        buscador.setParaNotaCreditoCliente();
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findAll(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"NotaCreditoID", "Nº Nota de Crédito", "Cliente", "Importe", "Recibo", "Fecha", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 60, 150, 50, 50, 50, 50, 70},
                new Class<?>[]{null, null, null, Double.class, null, null, null, null});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarDtmBuscador(armarQuery(soloAcreditables));
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                }
            }
        });
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && buscador.getjTable1().getSelectedRow() > -1) {
                    EL_OBJECT = jpaController.find(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                    if (soloAcreditables) {
                        buscador.dispose();
                    } else {
                        try {
                            setComprobanteUI(EL_OBJECT, toAnular);
                            //refresh post anulación...
                            if (EL_OBJECT == null) {
                                cargarDtmBuscador(armarQuery(soloAcreditables));
                            }
                        } catch (MessageException ex) {
                            ex.displayMessage(buscador);
                        }
                    }
                }
            }
        });
        if (toAnular) {
            buscador.getCheckAnulada().setEnabled(false);
            buscador.getCheckAnulada().setToolTipText("Buscador para ANULAR");
        }
        if (cliente != null) {
            UTIL.setSelectedItem(buscador.getCbClieProv(), cliente);
            buscador.getCbClieProv().setEnabled(false);
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
        return EL_OBJECT;
    }

    public void initBuscador(JFrame owner, final boolean paraAnular) throws MessageException {
        initBuscador(owner, paraAnular, null, false);
    }

    /**
     * Arma la consulta según los filtros de la UI {@link JDBuscadorReRe}
     *
     * @return String SQL native query
     * @throws MessageException
     */
    @SuppressWarnings("unchecked")
    private String armarQuery(boolean soloAcreditables) throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM nota_credito o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());
        if (soloAcreditables) {
            query.append(" AND o.recibo IS NULL");
        }
        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_nota_credito >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_nota_credito <= '").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal = ").append(((EntityWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }
        query.append(" ORDER BY o.id");
        LOG.trace("queryBuscador=" + query.toString());
        return query.toString();
    }

    private void cargarDtmBuscador(String query) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        List<NotaCredito> l = jpaController.findByNativeQuery(query);
        for (NotaCredito o : l) {
            dtm.addRow(new Object[]{
                o.getId(), // <--- no es visible
                JGestionUtils.getNumeracion(o, true),
                o.getCliente().getNombre(),
                o.getImporte(),
                o.getRecibo() != null ? JGestionUtils.getNumeracion(o.getRecibo(), true) : null,
                UTIL.DATE_FORMAT.format(o.getFechaNotaCredito()),
                o.getUsuario().getNick(),
                UTIL.DATE_FORMAT.format(o.getFechaCarga()) + " (" + UTIL.TIME_FORMAT.format(o.getFechaCarga()) + ")"
            });
        }
    }

    private void setComprobanteUI(final NotaCredito notaCredito, boolean paraAnular) throws MessageException {
        initComprobanteUI(null, true, false, false);
        jdFacturaVenta.setViewMode(true);
        jdFacturaVenta.modoVista();
        // setting info on UI
        String numFactura = UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12);
        jdFacturaVenta.getCbCliente().addItem(new EntityWrapper<>(notaCredito.getCliente(), notaCredito.getCliente().getId(), notaCredito.getCliente().getNombre()));
        jdFacturaVenta.getCbSucursal().addItem(new EntityWrapper<>(notaCredito.getSucursal(), notaCredito.getId(), notaCredito.getSucursal().getNombre()));
        jdFacturaVenta.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFacturaVenta.setTfFacturaOcteto(numFactura.substring(4));
        jdFacturaVenta.setDcFechaFactura(notaCredito.getFechaNotaCredito());
        jdFacturaVenta.getTfObservacion().setText(notaCredito.getObservacion());
        Collection<DetalleNotaCredito> lista = notaCredito.getDetalleNotaCreditoCollection();
        DefaultTableModel dtm = jdFacturaVenta.getDtm();
        for (DetalleNotaCredito detalle : lista) {
            Iva iva = detalle.getProducto().getIva();
            if (iva == null) {
                Producto findProducto = (Producto) DAO.findEntity(Producto.class, detalle.getProducto().getId());
                iva = findProducto.getIva();
                LOG.debug("Producto con Iva NULL!!" + detalle.getProducto());
                while (iva == null || iva.getIva() == null) {
                    System.out.print(".");
                    iva = new IvaController().findByProducto(detalle.getProducto().getId());
                }
            }
            try {
                BigDecimal productoConIVA = BigDecimal.valueOf(detalle.getPrecioUnitario()
                        + UTIL.getPorcentaje(detalle.getPrecioUnitario(), Double.valueOf(iva.getIva()))).setScale(4, RoundingMode.HALF_EVEN);
                //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
                dtm.addRow(new Object[]{
                    iva.getIva(),
                    detalle.getProducto().getCodigo(),
                    detalle.getProducto().getNombre() + "(" + iva.getIva() + ")",
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    productoConIVA,
                    0.0,
                    productoConIVA.multiply(BigDecimal.valueOf(detalle.getCantidad())).setScale(2, RoundingMode.HALF_EVEN)
                });
            } catch (NullPointerException e) {
                throw new MessageException("Ocurrió un error recuperando el detalle y los datos del Producto:"
                        + "\nCódigo:" + detalle.getProducto().getNombre()
                        + "\nNombre:" + detalle.getProducto().getCodigo()
                        + "\nIVA:" + detalle.getProducto().getIva()
                        + "\n\n   Intente nuevamente.");
            }
        }
        //totales
        jdFacturaVenta.setTfGravado(UTIL.DECIMAL_FORMAT.format(notaCredito.getGravado()));
        jdFacturaVenta.setTfTotalNoGravado(UTIL.DECIMAL_FORMAT.format(notaCredito.getNoGravado()));
        jdFacturaVenta.setTfTotalIVA105(UTIL.DECIMAL_FORMAT.format(notaCredito.getIva10()));
        jdFacturaVenta.setTfTotalIVA21(UTIL.DECIMAL_FORMAT.format(notaCredito.getIva21()));
        jdFacturaVenta.setTfTotal(UTIL.DECIMAL_FORMAT.format(notaCredito.getImporte()));
        jdFacturaVenta.setVisibleListaPrecio(false);// EXCLU NotaCredito
        if (paraAnular) {
            jdFacturaVenta.getBtnCancelar().setVisible(false);
            jdFacturaVenta.getBtnAceptar().setVisible(false);
            jdFacturaVenta.getBtnFacturar().setVisible(false);
            jdFacturaVenta.getBtnAnular().setVisible(paraAnular);
            jdFacturaVenta.getBtnAnular().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        anular(notaCredito);
                        JOptionPane.showMessageDialog(jdFacturaVenta, "Nota de credito Nº" + UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12) + " anulada.");
                        jdFacturaVenta.dispose();
                        EL_OBJECT = null;
                        buscador.getbBuscar();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(jdFacturaVenta, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setVisible(true);
    }

    private void anular(NotaCredito notaCredito) throws MessageException {
        if (notaCredito.getDesacreditado().compareTo(BigDecimal.ZERO) != 0) {
            throw new MessageException("La Nota de crédito Nº" + JGestionUtils.getNumeracion(notaCredito, true) + " ha sido usada para acreditar pagos y no puede ser anulada.");
        }
        if (notaCredito.getAnulada()) {
            throw new MessageException("La Nota de crédito Nº" + JGestionUtils.getNumeracion(notaCredito, true) + " ya está anulada.");
        }
        notaCredito.setAnulada(true);
        DAO.merge(notaCredito);
    }

    private void reporte(NotaCredito notaCredito) throws MissingReportException, JRException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_NotaCredito.jasper", "Nota de Crédito");
        r.addCurrent_User();
        r.addParameter("NOTA_CREDITO_ID", notaCredito.getId());
        r.printReport(true);
    }

    double getCreditoDisponible(Cliente cliente) {
//        Object[] o = (Object[]) getEntityManager().createQuery(
//                " SELECT sum(o.importe), sum (o.desacreditado) "
        BigDecimal o = (BigDecimal) DAO.getEntityManager().createQuery(
                " SELECT sum(o.importe)"
                + "FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE o.recibo IS NULL AND o.anulada = FALSE AND o.cliente.id =" + cliente.getId()).getSingleResult();
        double creditoDisponible;
        try {
//            creditoDisponible = ((Double) o[0]) - ((Double) o[1]);
            creditoDisponible = o.doubleValue();
        } catch (NullPointerException e) {
            //Cliente hasn't got registers on table NotaCredito.. so o[0] and o[1] are null
            creditoDisponible = 0;
        }
        return creditoDisponible;
    }

    /**
     * Find entities of {@link NotaCredito#cliente} = cliente
     *
     * @param cliente
     * @return
     * @throws DatabaseErrorException
     */
    List<NotaCredito> findNotaCreditoFrom(Cliente cliente) {
        return findNotaCreditoFrom(cliente, null);
    }

    List<NotaCredito> findNotaCreditoFrom(Cliente cliente, boolean anulada) {
        return findNotaCreditoFrom(cliente, Boolean.valueOf(anulada));
    }

    private List<NotaCredito> findNotaCreditoFrom(Cliente cliente, Boolean anulada) {
        String filtro = (anulada == null) ? "" : " AND o.anulada= " + anulada;
        return DAO.getEntityManager().createQuery(
                "SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE o.cliente.id= " + cliente.getId()
                + filtro).getResultList();
    }

    /**
     * Acredita el monto de {@link DetalleAcreditacion} a la {@link NotaCredito} Este método se
     * utiliza cuando se anula un {@link Recibo} o
     * {@link FacturaVenta#formaPago} == {@link Valores.FormaPago#CTA_CTE}
     *
     * @param anular {@link DetalleAcreditacion} que fue anulado.
     * @return instancia de {@link NotaCredito} que fue modificada (acreditada nuevamente).
     */
    public NotaCredito acreditar(DetalleAcreditacion anular) {
        if (!anular.isAnulado()) {
            throw new IllegalArgumentException(DetalleAcreditacion.class + " must be anulado == TRUE");
        }
        NotaCredito notaCredito = anular.getNotaCredito();
        notaCredito.setDesacreditado(notaCredito.getDesacreditado().subtract(BigDecimal.valueOf(anular.getMonto())));
        DAO.merge(notaCredito);
        return notaCredito;
    }

    /**
     * NotaCredito cuyo... {@link NotaCredito#anulada} == false && {@link NotaCredito#importe} &lt>
     * {@link NotaCredito#desacreditado}
     *
     * @param cliente Del cual se quieren las NotaCredito
     * @return chocolate..
     */
    List<NotaCredito> findNotaCreditoAcreditables(Cliente cliente) {
        return jpaController.findAll("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.importe <> o.desacreditado AND o.anulada = FALSE AND o.cliente.id=" + cliente.getId());
    }

    Integer getNextNumero(Sucursal s) {
        return jpaController.getNextNumero(s);
    }
}