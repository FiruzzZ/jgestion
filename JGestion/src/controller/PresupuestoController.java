package controller;

import controller.exceptions.*;
import entity.Cliente;
import entity.Presupuesto;
import entity.Sucursal;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import entity.DetallePresupuesto;
import entity.Iva;
import entity.ListaPrecios;
import entity.Producto;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jpa.controller.PresupuestoJpaController;
import jpa.controller.ProductoJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author Administrador
 */
public class PresupuestoController implements ActionListener, KeyListener {

    private static final Logger LOG = Logger.getLogger(PresupuestoController.class.getName());
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
     * Inicializa una {@link gui.JDFacturaVenta} con un Object listener
     * <code>this</code>. Implementa la actionListener del botón Aceptar
     *
     * @param frame
     * @param modal to be or not to be...
     * @param setVisible cuando se va levantar la gui desde el buscador es
     * <code>false</code>
     * @param loadDefaultData
     * @throws MessageException
     */
    public void initPresupuesto(JFrame frame, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController.initFacturaVenta(frame, modal, this, 2, setVisible, loadDefaultData);
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    doPresupuesto();
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
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                } catch (Exception ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                }
            } else {
                if (!MODO_VISTA) {
                }
            }
        } else {
            if (!MODO_VISTA) {
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

    /**
     * Control que la {@link entity.Presupuesto} esté necesariamente setteada,
     * persiste y realiza el reporte.
     *
     * @return <code>true</code> si la creación del reporte finalizó
     * correctamente.
     * @throws MessageException
     * @throws Exception
     */
    private boolean doPresupuesto() throws MessageException, Exception {
        Presupuesto newPresupuesto = selectedPresupuesto;
        if (!MODO_VISTA) {
            jdFacturaVenta = facturaVentaController.getContenedor();

            // <editor-fold defaultstate="collapsed" desc="CONTROLES">

            if (jdFacturaVenta.getDcFechaFactura() == null) {
                throw new MessageException("Fecha de factura no válida");
            }

            DefaultTableModel dtm = jdFacturaVenta.getDtm();
            if (dtm.getRowCount() < 1) {
                throw new MessageException(CLASS_NAME + " debe tener al menos un item.");
            }

            if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
                try {
                    if (Short.valueOf(jdFacturaVenta.getTfDias()) < 1) {
                        throw new MessageException("Cantidad de días de Cta. Cte. no válida. Debe ser mayor a 0");
                    }
                } catch (NumberFormatException ex) {
                    throw new MessageException("Cantidad de días de Cta. Cte. no válida");
                }
            }
            // </editor-fold>

            newPresupuesto = new Presupuesto();
            newPresupuesto.setCliente((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
            newPresupuesto.setListaPrecios((ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem());
            newPresupuesto.setSucursal(((ComboBoxWrapper<Sucursal>) jdFacturaVenta.getCbSucursal().getSelectedItem()).getEntity());
            newPresupuesto.setUsuario(UsuarioController.getCurrentUser());
            if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CONTADO)) {
                newPresupuesto.setFormaPago((short) Valores.FormaPago.CONTADO.getId());
            } else if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
                newPresupuesto.setFormaPago((short) Valores.FormaPago.CTA_CTE.getId());
                newPresupuesto.setDias(Short.parseShort(jdFacturaVenta.getTfDias()));
            }
            newPresupuesto.setDescuento(UTIL.parseToDouble(jdFacturaVenta.getTfTotalDesc()));
            newPresupuesto.setImporte(UTIL.parseToDouble(jdFacturaVenta.getTfTotal()));
            newPresupuesto.setIva10(UTIL.parseToDouble(jdFacturaVenta.getTfTotalIVA105()));
            newPresupuesto.setIva21(UTIL.parseToDouble(jdFacturaVenta.getTfTotalIVA21()));
            newPresupuesto.setDetallePresupuestoList(new ArrayList<DetallePresupuesto>(dtm.getRowCount()));
            // carga de detalleVenta
            DetallePresupuesto detalle;
            ProductoJpaController productoController = new ProductoJpaController();
            for (int i = 0; i < dtm.getRowCount(); i++) {
                detalle = new DetallePresupuesto();
                detalle.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
                detalle.setPrecioUnitario((BigDecimal) dtm.getValueAt(i, 4));
                detalle.setDescuento(Double.valueOf(dtm.getValueAt(i, 6).toString()));
                detalle.setTipoDesc(Integer.valueOf(dtm.getValueAt(i, 8).toString()));
                Producto p = productoController.find((Integer) dtm.getValueAt(i, 9));
                detalle.setProducto(p);
//                detalle.setPresupuesto(newPresupuesto); //innecesario
                newPresupuesto.getDetallePresupuestoList().add(detalle);
            }
            jpaController.create(newPresupuesto);
        }
        return doReport(newPresupuesto);
    }

    private boolean doReport(Presupuesto presupuesto) {
        try {
            Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Presupuesto.jasper", "Presupuesto");
            r.addParameter("PRESUPUESTO_ID", presupuesto.getId());
            r.printReport(true);
            return r.isReporteFinalizado();
        } catch (MissingReportException | JRException ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error generando reporte", JOptionPane.ERROR_MESSAGE);
            LOG.error(ex, ex);
        }
        return false;
    }

    private void buscadorPresupuestoMouseClicked(MouseEvent evt) {
        if (evt.getClickCount() >= 2) {
            selectedPresupuesto = (Presupuesto) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
            setDatos(selectedPresupuesto);
        }
    }

    public void initBuscador(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(owner, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
        buscador.getjTable1().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buscadorPresupuestoMouseClicked(evt);
            }
        });
        //personalizando vista de Buscador
        buscador.hideFactura();
        buscador.hideCaja();
        buscador.hideFormaPago();
        buscador.getjTfOcteto().setVisible(false);
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findAll(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Nº " + CLASS_NAME, "Cliente", "Importe", "Fecha", "Sucursal", "Usuario"},
                new int[]{15, 50, 50, 50, 80, 50});
        MODO_VISTA = true;
        buscador.setListeners(this);
        buscador.setVisible(true);
    }

    private void armarQuery() throws MessageException {
        String query = "SELECT o.* FROM " + CLASS_NAME + " o WHERE o.id > -1";

        //filtro por nº de ReRe
        if (buscador.getTfCuarto().length() > 0) {
            try {
                Integer presupuestoID = Integer.valueOf(buscador.getTfOcteto());
                query += " AND o.id = " + presupuestoID;
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido.\n\n" + ex.getMessage());
            }
        }

        if (buscador.getDcDesde() != null) {
            query += " AND o.fecha_creacion >= '" + buscador.getDcDesde() + "'";
        }
        if (buscador.getDcHasta() != null) {
            query += " AND o.fecha_creacion <= '" + buscador.getDcHasta() + "'";
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
        } else {
            query += " AND (";
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                Sucursal sucursal = (Sucursal) buscador.getCbSucursal().getItemAt(i);
                query += " o.sucursal=" + sucursal.getId();
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query += " OR ";
                }
            }
            query += ")";
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query += " AND o.cliente = " + ((Cliente) buscador.getCbClieProv().getSelectedItem()).getId();
        }
        query += " ORDER BY o.id";
        cargarDtmBuscador(query);
    }

    private void cargarDtmBuscador(String query) {
        buscador.dtmRemoveAll();
        DefaultTableModel dtm = buscador.getDtm();
        List<Presupuesto> l = DAO.getEntityManager().createNativeQuery(query, Presupuesto.class).getResultList();
        for (Presupuesto presupuesto : l) {
            dtm.addRow(new Object[]{
                presupuesto, // <--- no es visible
                presupuesto.getCliente(),
                presupuesto.getImporte(),
                UTIL.TIMESTAMP_FORMAT.format(presupuesto.getFechaalta()),
                presupuesto.getSucursal(),
                presupuesto.getUsuario()});
        }
    }

    /**
     * Cuando se selecciona una Presupuesto desde el Buscador
     * {@link gui.JDBuscadorReRe}
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
        jdFacturaVenta.getCbCliente().addItem(presupuesto.getCliente());
        jdFacturaVenta.getCbSucursal().addItem(presupuesto.getSucursal());
        jdFacturaVenta.getCbListaPrecio().addItem(presupuesto.getListaPrecios());  //<---
//      jdFacturaVenta.getCbUsuario().addItem(presupuesto.getUsuario());           //<---
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
                Producto findProducto = (Producto) DAO.findEntity(Producto.class, detalle.getProducto().getId());
                iva = findProducto.getIva();
                LOG.debug("Producto con Iva NULL!!" + detalle.getProducto());
                while (iva == null || iva.getIva() == null) {
                    System.out.print(".");
                    iva = new IvaController().findByProducto(detalle.getProducto().getId());
                }
            }
            BigDecimal productoConIVA = detalle.getPrecioUnitario().add(UTIL.getPorcentaje(detalle.getPrecioUnitario(), BigDecimal.valueOf(iva.getIva()))).setScale(4, RoundingMode.HALF_EVEN);
//         "IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
            dtm.addRow(new Object[]{
                iva.getIva(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto(),
                detalle.getCantidad(),
                UTIL.PRECIO_CON_PUNTO.format(detalle.getPrecioUnitario()),
                productoConIVA,
                detalle.getDescuento(),
                UTIL.PRECIO_CON_PUNTO.format((detalle.getCantidad() * productoConIVA.doubleValue()) - detalle.getDescuento()),
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
