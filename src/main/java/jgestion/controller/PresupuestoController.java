package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Cliente;
import jgestion.entity.DetallePresupuesto;
import jgestion.entity.Iva;
import jgestion.entity.ListaPrecios;
import jgestion.entity.Presupuesto;
import jgestion.entity.Producto;
import jgestion.entity.Sucursal;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDFacturaVenta;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.PresupuestoJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class PresupuestoController implements ActionListener {

    private static final Logger LOG = LogManager.getLogger();
    public static final String CLASS_NAME = "Presupuesto";
    private JDFacturaVenta jdFacturaVenta;
    private final FacturaVentaController facturaVentaController;
    private JDBuscadorReRe buscador;
    private boolean MODO_VISTA = false;
    private Presupuesto selectedPresupuesto;
    private final PresupuestoJpaController jpaController = new PresupuestoJpaController();

    public PresupuestoController() {
        facturaVentaController = new FacturaVentaController();
    }

    /**
     * Inicializa una {@link jgestion.gui.JDFacturaVenta} con un Object listener <code>this</code>.
     * Implementa la actionListener del botón Aceptar
     *
     * @param owner
     * @param modal to be or not to be...
     * @param setVisible cuando se va levantar la gui desde el buscador es <code>false</code>
     * @param loadDefaultData
     * @throws MessageException
     */
    public void initPresupuesto(Window owner, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController.displayABM(owner, modal, this, 4, setVisible, loadDefaultData);
        facturaVentaController.getContenedor().setUIToPresupuesto();
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    doPresupuesto();
                    facturaVentaController.borrarDetalles();
                } catch (MessageException ex) {
                    ex.displayMessage(null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error(ex, ex);
                } finally {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //solo se hace cargo de persistir la entity Presupuesto
        //todo las demás acciones son manejadas (delegadas) -> FacturaVentaJpaController
    }

    /**
     * Control que la {@link jgestion.entity.Presupuesto} esté necesariamente setteada, persiste y
     * realiza el reporte.
     *
     * @return <code>true</code> si la creación del reporte finalizó correctamente.
     * @throws MessageException
     * @throws Exception
     */
    private boolean doPresupuesto() throws MessageException, Exception {
        Presupuesto newPresupuesto = selectedPresupuesto;
        if (!MODO_VISTA) {
            jdFacturaVenta = facturaVentaController.getContenedor();
            String observacion;

            if (jdFacturaVenta.getDcFechaFactura() == null) {
                throw new MessageException("Fecha de factura no válida");
            }
            observacion = jdFacturaVenta.getTfObservacion().getText().trim();
            if (observacion.length() > 100) {
                throw new MessageException("Observación no puede tener mas de 100 caracteres, NO ES UNA NOVELA!");
            } else if (observacion.isEmpty()) {
                observacion = null;
            }

            DefaultTableModel dtm = jdFacturaVenta.getDtm();
            if (dtm.getRowCount() < 1) {
                throw new MessageException(CLASS_NAME + " debe tener al menos un item.");
            }
            if (jdFacturaVenta.getCbFormaPago().getSelectedIndex() == 0) {
                throw new MessageException("Seleccionar forma de pago");
            }
            Valores.FormaPago fp = (Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem();
            if (fp.equals(Valores.FormaPago.CTA_CTE)) {
                try {
                    if (Short.valueOf(jdFacturaVenta.getTfDias()) < 1) {
                        throw new MessageException("Cantidad de días de Cta. Cte. no válida. Debe ser mayor a 0");
                    }
                } catch (NumberFormatException ex) {
                    throw new MessageException("Cantidad de días de Cta. Cte. no válida");
                }
            }

            newPresupuesto = new Presupuesto();
            newPresupuesto.setCliente((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
            newPresupuesto.setListaPrecios((ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem());
            newPresupuesto.setSucursal(((EntityWrapper<Sucursal>) jdFacturaVenta.getCbSucursal().getSelectedItem()).getEntity());
            newPresupuesto.setUsuario(UsuarioController.getCurrentUser());
            newPresupuesto.setFormaPago((short) fp.getId());
            if (fp.equals(Valores.FormaPago.CTA_CTE)) {
                newPresupuesto.setDias(Short.parseShort(jdFacturaVenta.getTfDias()));
            }
            newPresupuesto.setObservacion(observacion);
            newPresupuesto.setDescuento(UTIL.parseToDouble(jdFacturaVenta.getTfTotalDesc()));
            newPresupuesto.setImporte(UTIL.parseToDouble(jdFacturaVenta.getTfTotal()));
            newPresupuesto.setIva10(UTIL.parseToDouble(jdFacturaVenta.getTfTotalIVA105()));
            newPresupuesto.setIva21(UTIL.parseToDouble(jdFacturaVenta.getTfTotalIVA21()));
            newPresupuesto.setDetallePresupuestoList(new ArrayList<>(dtm.getRowCount()));
            // carga de detalleVenta
            DetallePresupuesto detalle;
            ProductoJpaController productoController = new ProductoJpaController();
            for (int i = 0; i < dtm.getRowCount(); i++) {
                detalle = new DetallePresupuesto();
                detalle.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
                detalle.setPrecioUnitario((BigDecimal) dtm.getValueAt(i, 4));
                detalle.setDescuento((BigDecimal) dtm.getValueAt(i, 6));
                detalle.setTipoDesc(Integer.valueOf(dtm.getValueAt(i, 8).toString()));
                Producto p = productoController.find((Integer) dtm.getValueAt(i, 9));
                detalle.setProducto(p);
                newPresupuesto.getDetallePresupuestoList().add(detalle);
            }
            jpaController.persist(newPresupuesto);
        }
        return doReport(newPresupuesto);
    }

    private boolean doReport(Presupuesto presupuesto) {
        try {
            Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Presupuesto.jasper", "Presupuesto");
            r.addParameter("PRESUPUESTO_ID", presupuesto.getId());
            r.viewReport();
            return r.isReporteFinalizado();
        } catch (MissingReportException | JRException ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error generando reporte", JOptionPane.ERROR_MESSAGE);
            LOG.error(ex, ex);
        }
        return false;
    }

    public void initBuscador(Window owner) throws MessageException {
        initBuscador(owner, false);
    }

    public Presupuesto initBuscador(Window owner, boolean selectorMode) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(owner, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
        buscador.getjTable1().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    selectedPresupuesto = jpaController.find((Integer) UTIL.getSelectedValueFromModel(buscador.getjTable1(), 0));
                    if (selectorMode) {
                        buscador.dispose();
                    } else {
                        setDatos(selectedPresupuesto);
                    }
                }
            }
        });
        //personalizando vista de Buscador
        buscador.hideFactura();
        buscador.hideCaja();
        buscador.hideUDNCuentaSubCuenta();
        buscador.hideVendedor();
        buscador.getbImprimir().setVisible(false);
        buscador.getBtnToExcel().setVisible(false);
        buscador.getjTfOcteto().setVisible(false);
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findAll(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Arrays.asList(Valores.FormaPago.values()), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"ObjectID", "Nº " + CLASS_NAME, "Cliente", "Importe", "Fecha", "Sucursal", "Usuario"},
                new int[]{1, 15, 50, 50, 50, 80, 50});
        buscador.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getbBuscar().addActionListener((ActionEvent e) -> {
            try {
                armarQuery();
            } catch (MessageException ex) {
                buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
            } catch (Exception ex) {
                LOG.error(ex, ex);
                buscador.showMessage("Ocurrió un error", "Buscador - " + CLASS_NAME, 0);
            }
        });
        MODO_VISTA = true;
        buscador.setListeners(this);
        buscador.setVisible(true);
        return selectedPresupuesto;
    }

    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.id > -1");
        //filtro por nº de ReRe
        if (buscador.getTfCuarto().length() > 0) {
            try {
                Integer presupuestoID = Integer.valueOf(buscador.getTfOcteto());
                query.append("AND o.id = ").append(presupuestoID);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido.\n\n" + ex.getMessage());
            }
        }

        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
        if (buscador.getDcDesdeSistema() != null) {
            query.append(" AND o.").append("fechaalta").append(" >= '").append(yyyyMMdd.format(buscador.getDcDesdeSistema())).append("'");
        }
        if (buscador.getDcHastaSistema() != null) {
            query.append(" AND o.").append("fechaalta").append(" <= '").append(yyyyMMdd.format(buscador.getDcHastaSistema())).append("'");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal.id = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                Sucursal sucursal = (Sucursal) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal.id=" + sucursal.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente.id = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            query.append(" AND o.formaPago = ").append(((Valores.FormaPago) buscador.getCbFormasDePago().getSelectedItem()).getId());
        }
        query.append(" ORDER BY o.id");
        List<Presupuesto> ll = jpaController.findAll(query.toString());
        cargarDtmBuscador(ll);
    }

    private void cargarDtmBuscador(List<Presupuesto> l) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        for (Presupuesto presupuesto : l) {
            dtm.addRow(new Object[]{
                presupuesto.getId(), // <--- no es visible
                JGestionUtils.getNumeracion(presupuesto, true),
                presupuesto.getCliente().getNombre(),
                presupuesto.getImporte(),
                UTIL.TIMESTAMP_FORMAT.format(presupuesto.getFechaalta()),
                presupuesto.getSucursal().getNombre(),
                presupuesto.getUsuario().getNick()
            });
        }
    }

    /**
     * Cuando se selecciona una Presupuesto desde el Buscador {@link jgestion.gui.JDBuscadorReRe}
     *
     * @param presupuesto
     */
    private void setDatos(Presupuesto presupuesto) {
        try {
            initPresupuesto(null, true, false, false);
        } catch (MessageException ex) {
            ex.displayMessage(null);
        }
        jdFacturaVenta = facturaVentaController.getContenedor();
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.getCbCliente().addItem(presupuesto.getCliente().getNombre());
        Sucursal s = presupuesto.getSucursal();
        jdFacturaVenta.getCbSucursal().addItem(new EntityWrapper<>(s, s.getId(), s.getNombre()));
        jdFacturaVenta.getCbListaPrecio().addItem(presupuesto.getListaPrecios().getNombre());
        jdFacturaVenta.getTfObservacion().setText(presupuesto.getObservacion());
        jdFacturaVenta.setDcFechaFactura(presupuesto.getFechaalta());
        for (Valores.FormaPago formaPago : Valores.FormaPago.getFormasDePago()) {
            if (formaPago.getId() == (presupuesto.getFormaPago())) {
                jdFacturaVenta.getCbFormaPago().addItem(formaPago);
                if (presupuesto.getDias() != null && presupuesto.getDias() > 0) {
                    jdFacturaVenta.setTfDias(presupuesto.getDias().toString());
                } else {
                    jdFacturaVenta.setTfDias("");
                }
            }
        }
        jdFacturaVenta.setTfNumMovimiento(String.valueOf(presupuesto.getId()));
        List<DetallePresupuesto> lista = presupuesto.getDetallePresupuestoList();
        DefaultTableModel dtm = jdFacturaVenta.getDtm();
        for (DetallePresupuesto detalle : lista) {
            Iva iva = detalle.getProducto().getIva();
            if (iva == null) {
                Producto findProducto = new ProductoJpaController().find(detalle.getProducto().getId());
                iva = findProducto.getIva();
                LOG.error("Producto con Iva NULL!" + detalle.getProducto());
                while (iva == null || iva.getIva() == null) {
                    LOG.error("Producto con Iva NULL!!" + detalle.getProducto());
                    iva = new IvaController().findByProducto(detalle.getProducto().getId());
                }
            }
            BigDecimal pu = JGestionUtils.setScale(detalle.getPrecioUnitario());
            BigDecimal desc = JGestionUtils.setScale(detalle.getDescuento());
            BigDecimal productoConIVA = pu.add(UTIL.getPorcentaje(pu.subtract(desc),
                    JGestionUtils.setScale(iva.getIva())));
            dtm.addRow(new Object[]{
                iva.getIva(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                UTIL.PRECIO_CON_PUNTO.format(pu),
                productoConIVA, desc,
                UTIL.PRECIO_CON_PUNTO.format((detalle.getCantidad() * productoConIVA.doubleValue())),
                detalle.getTipoDesc(),
                detalle.getProducto().getId(),
                null
            });
        }
        //totales
        jdFacturaVenta.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getImporte() - (presupuesto.getIva10() + presupuesto.getIva21())));
        jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getIva10()));
        jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getIva21()));
        jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getImporte()));
        jdFacturaVenta.modoVista(); //   <----------------------
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setVisible(true);
    }

}
