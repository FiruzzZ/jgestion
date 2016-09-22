package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.CreditoProveedor;
import jgestion.entity.CtacteProveedor;
import jgestion.entity.DetalleRemesa;
import jgestion.entity.FacturaCompra;
import jgestion.entity.Proveedor;
import jgestion.entity.Remesa;
import jgestion.gui.JDResumenCtaCtes;
import generics.gui.JDialogTable;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.entity.NotaDebitoProveedor;
import jgestion.jpa.controller.CreditoProveedorJpaController;
import jgestion.jpa.controller.CtacteProveedorJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import jgestion.jpa.controller.RemesaJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class CtacteProveedorController implements ActionListener {

    public static final Logger LOG = LogManager.getLogger();
    public static final String CLASS_NAME = CtacteProveedor.class.getSimpleName();
    private JDResumenCtaCtes resumenCtaCtes;
    private Double totalDebe;
    private Double totalHaber;

    public CtacteProveedorController() {
    }

    void addToCtaCte(FacturaCompra o) {
        CtacteProveedor ccp = new CtacteProveedor();
        ccp.setDias(o.getDiasCtaCte());
        ccp.setEntregado(BigDecimal.ZERO);
        ccp.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
        ccp.setFactura(o);
        ccp.setFechaCarga(o.getFechaCompra());
        ccp.setImporte(o.getImporte());
        new CtacteProveedorJpaController().persist(ccp);
    }

    void addToCtaCte(NotaDebitoProveedor o) {
        CtacteProveedor ccp = new CtacteProveedor();
        ccp.setDias(0);
        ccp.setEntregado(BigDecimal.ZERO);
        ccp.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
        ccp.setNotaDebito(o);
        ccp.setFechaCarga(o.getFechaNotaDebito());
        ccp.setImporte(o.getImporte());
        new CtacteProveedorJpaController().persist(ccp);
    }

    List<CtacteProveedor> findCtacteProveedorByProveedor(Proveedor proveedor, Valores.CtaCteEstado ctaCteEstado) {
        List<CtacteProveedor> listaCtaCteProveedor = new CtacteProveedorJpaController().findAllBy(proveedor, ctaCteEstado);
        return listaCtaCteProveedor;
    }

    /**
     *
     * @param owner
     * @param modal
     * @param proveedor if != null, set this Proveedor as selected and do the search for it Cta. Cte
     * @return
     * @throws MessageException
     * @throws JRException
     * @throws MissingReportException
     * @see #getResumenCtaCte(java.awt.Window, boolean)
     */
    public JDialog getResumenCtaCte(Window owner, boolean modal, Proveedor proveedor) throws MessageException, JRException, MissingReportException {
        getResumenCtaCte(owner, modal);
        resumenCtaCtes.getCheckExcluirAnuladas().setSelected(true);
        resumenCtaCtes.getCheckExcluirPagadas().setSelected(true);
        if (proveedor != null) {
            UTIL.setSelectedItem(resumenCtaCtes.getCbClieProv(), proveedor);
            armarQuery(false);
        }
        return resumenCtaCtes;

    }

    public JDialog getResumenCtaCte(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        resumenCtaCtes = new JDResumenCtaCtes(owner, modal, false);
        resumenCtaCtes.getjTableResumen().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = resumenCtaCtes.getjTableResumen().getSelectedRow();
                if (selectedRow > 0) {
                    CtacteProveedor cc = new CtacteProveedorJpaController().find(
                            (Integer) UTIL.getSelectedValueFromModel(resumenCtaCtes.getjTableResumen(), 0));
                    //selecciona una factura CtaCteCliente
                    cargarComboBoxRemesasDeCtaCte(cc);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    CtacteProveedor cc = new CtacteProveedorJpaController().find(
                            (Integer) UTIL.getSelectedValueFromModel(resumenCtaCtes.getjTableResumen(), 0));
                    if (cc.getFactura() != null) {
                        FacturaCompra factura = cc.getFactura();
                        try {
                            new FacturaCompraController().show(factura, false);
                        } catch (MessageException ex) {
                            ex.displayMessage(null);
                        }
                    } else {
                        try {
                            NotaDebitoProveedor notaDebito = cc.getNotaDebito();
                            new NotaDebitoProveedorController().displaABM(notaDebito, false, false);
                        } catch (MessageException ex) {
                            ex.displayMessage(null);
                        }
                    }
                }
            }

        });
        UTIL.loadComboBox(resumenCtaCtes.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAll()), false);
        UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), null, true);
        UTIL.getDefaultTableModel(
                resumenCtaCtes.getjTableResumen(),
                new String[]{"ctacteProveedorID", "Detalle", "Fecha", "Vencimiento", "Debe", "Haber", "Saldo", "Acumulativo"},
                new int[]{1, 60, 50, 50, 30, 30, 30, 50});
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(2).setCellRenderer(FormatRenderer.getDateRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(3).setCellRenderer(FormatRenderer.getDateRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(resumenCtaCtes.getjTableResumen(), 0);
        UTIL.getDefaultTableModel(
                resumenCtaCtes.getjTableDetalle(),
                new String[]{"Nº Factura", "Observación", "Monton"},
                new int[]{60, 100, 50});
        resumenCtaCtes.getjTableDetalle().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getBtnCuenta().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayDetalleCredito(resumenCtaCtes);
            }
        });
        resumenCtaCtes.getCbReRes().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (resumenCtaCtes.getCbReRes().isFocusOwner()) {
                    setDatosRemesaSelected();
                }

            }
        });
        resumenCtaCtes.setListener(this);
        return resumenCtaCtes;
    }

    private void displayDetalleCredito(Window owner) {
        JTable tabla = UTIL.getDefaultTableModel(null,
                new String[]{"Concepto", "Fecha", "Debe", "Haber"},
                new int[]{200, 50, 50, 50});
        tabla.getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tabla.getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        Proveedor p = ((EntityWrapper<Proveedor>) resumenCtaCtes.getCbClieProv().getSelectedItem()).getEntity();
        List<CreditoProveedor> lista = new CreditoProveedorJpaController().findBy(p);
        DefaultTableModel dtm = (DefaultTableModel) tabla.getModel();
        BigDecimal debe = BigDecimal.ZERO;
        BigDecimal haber = BigDecimal.ZERO;
        for (CreditoProveedor cp : lista) {
            if (cp.getDebe()) {
                debe = debe.add(cp.getImporte());
            } else {
                haber = haber.add(cp.getImporte());
            }
            dtm.addRow(new Object[]{
                cp.getConcepto(),
                UTIL.TIMESTAMP_FORMAT.format(cp.getFechaCarga()),
                cp.getDebe() ? cp.getImporte() : null,
                cp.getDebe() ? null : cp.getImporte()});
        }
        dtm.addRow(new Object[]{"----------------------", null, debe, haber});
        dtm.addRow(new Object[]{"---------TOTAL--------", null, null, haber.subtract(debe)});
        JDialogTable jd = new JDialogTable(owner, "Detalle de crédito: " + p.getNombre(), true, tabla);
        jd.setSize(600, 400);
        jd.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            JButton btn = (JButton) e.getSource();

            // <editor-fold defaultstate="collapsed" desc="verResumenCCC">
            if (btn.equals(resumenCtaCtes.getbBuscar())) {
                try {
                    armarQuery(false);
                } catch (MessageException ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                    LogManager.getLogger();//(CtacteClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }// </editor-fold>
            else if (btn.equals(resumenCtaCtes.getbImprimir())) {
                try {
                    armarQuery(true);
                } catch (MessageException ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                    LogManager.getLogger();//(CtacteClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }// </editor-fold>
    }

    private void armarQuery(boolean imprimirResumen) throws MessageException, JRException, MissingReportException {
        totalDebe = 0.0;
        totalHaber = 0.0;

        String query
                = " SELECT ctacte.*"
                + " FROM ( "
                + " SELECT ccc.*, proveedor.id proveedor_id, fv.fecha_compra fecha, "
                + " (ccc.importe - ccc.entregado) as saldo, ccc.estado,"
                + " fv.anulada"
                + " FROM ctacte_proveedor ccc"
                + " JOIN factura_compra fv ON ccc.factura = fv.id"
                + " JOIN proveedor ON fv.proveedor = proveedor.id"
                + " UNION"
                + " SELECT ccc.*, proveedor.id proveedor_id, fv.fecha_nota_debito fecha,"
                + " (ccc.importe - ccc.entregado) as saldo, ccc.estado,"
                + " fv.anulada"
                + " FROM ctacte_proveedor ccc"
                + " JOIN nota_debito_proveedor fv ON ccc.notadebito_id = fv.id"
                + " JOIN proveedor ON fv.proveedor_id = proveedor.id"
                + " ) ctacte "
                + " WHERE id is not null ";
        String filters = "";
        try {
            filters += " AND proveedor_id=" + ((EntityWrapper<?>) resumenCtaCtes.getCbClieProv().getSelectedItem()).getId();
        } catch (ClassCastException ex) {
            throw new MessageException("Proveedor no válido");
        }

        if (resumenCtaCtes.getDcDesde() != null) {
            //calcula los totales del DEBE / HABER / SALDO ACUMULATIVO de la CtaCte
            // anterior a la fecha desde la cual se eligió en el buscador
            setResumenHistorial(query + filters + " AND fecha < '" + UTIL.yyyy_MM_dd.format(resumenCtaCtes.getDcDesde()) + "'");
            filters += " AND fecha >= '" + UTIL.yyyy_MM_dd.format(resumenCtaCtes.getDcDesde()) + "'";
        }
        if (resumenCtaCtes.getCheckExcluirPagadas().isSelected()) {
            filters += " AND saldo > 0";
        }
        if (resumenCtaCtes.getCheckExcluirAnuladas().isSelected()) {
            filters += " AND anulada=FALSE";
        }
        query += filters + " ORDER BY fecha";
        cargarDtmResumen(query);
        if (imprimirResumen) {
            doReportResumenCCP(resumenCtaCtes.getDcDesde(), filters);
        }
    }

    private void setResumenHistorial(String query) {
        List<CtacteProveedor> lista = DAO.getEntityManager().createNativeQuery(query, CtacteProveedor.class).getResultList();
        for (CtacteProveedor ccc : lista) {
            if (ccc.getEstado() != Valores.CtaCteEstado.ANULADA.getId()) {
                totalDebe += ccc.getImporte().doubleValue();
                totalHaber += ccc.getEntregado().doubleValue();
            }
        }
    }

    private void cargarDtmResumen(String query) {
        DefaultTableModel dtm = resumenCtaCtes.getDtmResumen();
        UTIL.limpiarDtm(dtm);
        List<CtacteProveedor> lista = DAO.getEntityManager().createNativeQuery(query, CtacteProveedor.class).getResultList();

        //agregar la 1er fila a la tabla
        BigDecimal saldoAcumulativo = BigDecimal.valueOf(totalDebe - totalHaber);
        dtm.addRow(new Object[]{null, "RESUMEN PREVIOS", null, null, totalDebe, totalHaber, null, saldoAcumulativo});
        for (CtacteProveedor ctaCte : lista) {
            //checkea que no esté anulada la ccc
            boolean isAnulada = (ctaCte.getEstado() == Valores.CtaCteEstado.ANULADA.getId());
            if (!isAnulada) {
                saldoAcumulativo = saldoAcumulativo.add(ctaCte.getImporte().subtract(ctaCte.getEntregado()));
            }
            Date date = ctaCte.getFactura() != null ? ctaCte.getFactura().getFechaCompra() : ctaCte.getNotaDebito().getFechaNotaDebito();
            dtm.addRow(new Object[]{
                ctaCte.getId(), // <--------- No es visible desde la GUI
                ctaCte.getFactura() != null ? JGestionUtils.getNumeracion(ctaCte.getFactura()) : JGestionUtils.getNumeracion(ctaCte.getNotaDebito()),
                date,
                UTIL.customDateByDays(date, ctaCte.getDias()),
                ctaCte.getImporte(),
                isAnulada ? "ANULADA" : ctaCte.getEntregado(),
                isAnulada ? "ANULADA" : ctaCte.getImporte().subtract(ctaCte.getEntregado()),
                isAnulada ? "ANULADA" : saldoAcumulativo,
                ctaCte.getEstado()
            });
        }
    }

    private void doReportResumenCCP(Date filterDate, String filters) throws JRException, MissingReportException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_ResumenCCP.jasper", "Resumen Cta. Cte. Proveedor");
        r.addCurrent_User();
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        r.addParameter("FILTER_DATE", filterDate);
        r.addParameter("FILTERS", filters);
        r.viewReport();
    }

    private void cargarComboBoxRemesasDeCtaCte(CtacteProveedor ctacteProveedor) {
        List<Remesa> list = new RemesaJpaController().findByFactura(ctacteProveedor.getFactura());
        UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), JGestionUtils.getWrappedRemesas(list), false);
        setDatosRemesaSelected();
    }

    private void setDatosRemesaSelected() {
        try {
            Remesa remesa = ((EntityWrapper<Remesa>) resumenCtaCtes.getCbReRes().getSelectedItem()).getEntity();
            resumenCtaCtes.setTfReciboFecha(UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()));
            resumenCtaCtes.setTfReciboMonto(UTIL.DECIMAL_FORMAT.format(remesa.getMonto()));
            cargarDtmDetallesDeCtaCte(remesa);
        } catch (ClassCastException ex) {
            // si el comboBox está vacio
            resumenCtaCtes.setTfReciboFecha("");
            resumenCtaCtes.setTfReciboMonto("");
            UTIL.limpiarDtm(resumenCtaCtes.getDtmDetalle());
        }
    }

    private void cargarDtmDetallesDeCtaCte(Remesa remesa) {
        DefaultTableModel dtm = (DefaultTableModel) resumenCtaCtes.getjTableDetalle().getModel();
        dtm.setRowCount(0);
        List<DetalleRemesa> detalleReList = remesa.getDetalle();
        for (DetalleRemesa detalleRe : detalleReList) {
            dtm.addRow(new Object[]{
                detalleRe.getFacturaCompra() != null
                ? JGestionUtils.getNumeracion(detalleRe.getFacturaCompra())
                : JGestionUtils.getNumeracion(detalleRe.getNotaDebitoProveedor()),
                detalleRe.getObservacion(),
                UTIL.PRECIO_CON_PUNTO.format(detalleRe.getMontoEntrega())
            });
        }
    }

    List<Object[]> findSaldos(Date desde, Date hasta) {
        List<CtacteProveedor> l;
        CtacteProveedorJpaController jpa = new CtacteProveedorJpaController();
        l = jpa.findAll(
                jpa.getSelectFrom() + " LEFT JOIN o.factura f LEFT JOIN o.notaDebito nd LEFT JOIN f.proveedor cf LEFT JOIN nd.proveedor cnd"
                + " WHERE o.estado = 1"
                + " AND ((nd IS NULL AND f.anulada = FALSE) OR"
                + " (f IS NULL AND nd.anulada = FALSE)) "
                + (desde != null ? " AND ("
                        + "     (f IS NOT NULL AND f.fechaCompra >='" + UTIL.DATE_FORMAT.format(desde) + "')"
                        + "     OR (nd IS NOT NULL AND nd.fechaNotaDebito >='" + UTIL.DATE_FORMAT.format(desde) + "')"
                        + ")" : "")
                + (hasta != null ? " AND ("
                        + "     (f IS NOT NULL AND f.fechaCompra <='" + UTIL.DATE_FORMAT.format(desde) + "')"
                        + "     OR (nd IS NOT NULL AND nd.fechaNotaDebito <='" + UTIL.DATE_FORMAT.format(desde) + "')"
                        + ")" : "")
                + " ORDER BY cf.nombre, cnd.nombre");
        if (l.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<Object[]> data = new ArrayList<>();
        BigDecimal importeCCC = BigDecimal.ZERO;
        CtacteProveedor cc = l.get(0);
        Proveedor c = cc.getFactura() != null ? cc.getFactura().getProveedor() : cc.getNotaDebito().getProveedor();
        for (CtacteProveedor ccc : l) {
            Proveedor c1 = ccc.getFactura() != null ? ccc.getFactura().getProveedor() : ccc.getNotaDebito().getProveedor();
            if (!c.equals(c1)) {
                data.add(new Object[]{c.getId(), c.getNombre(), importeCCC});
                importeCCC = BigDecimal.ZERO;
                c = c1;
            }
            importeCCC = importeCCC.add(ccc.getImporte().subtract(ccc.getEntregado())).setScale(2, RoundingMode.HALF_UP);
        }
        data.add(new Object[]{c.getId(), c.getNombre(), importeCCC});
        return data;
    }

}
