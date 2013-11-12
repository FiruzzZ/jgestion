package jgestion.test;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import controller.DAO;
import controller.ReciboController;
import controller.Reportes;
import entity.*;
import generics.PropsUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jgestion.JGestionUtils;
import jpa.controller.*;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.log4j.PropertyConfigurator;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class JPATesting {

    private static final Logger LOG = Logger.getLogger(JPATesting.class.getName());

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("log4j.properties");
            Properties properties = PropsUtils.load(new File("cfg.ini"));
            DAO.setProperties(properties);
            new JPATesting();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    public JPATesting() throws Exception {
        List<Recibo> conci = new ReciboJpaController().findByQuery("SELECT o FROM Recibo o where o.porConciliar = true ");
        for (Recibo r : conci) {
            System.out.println(JGestionUtils.getNumeracion(r, true));
//            new ReciboController().loadPagos(r);
//            for (Object pagos : r.getPagosEntities()) {
//                ChequeTerceros c = (ChequeTerceros) pagos;
//                System.out.println(c.toString());
//            }
        }
//<editor-fold defaultstate="collapsed" desc="pasar las notas de débito a CtacteCliente">
//        List<NotaDebito> notas = new NotaDebitoJpaController().findAll();
//        CtacteClienteJpaController cccc = new CtacteClienteJpaController();
//        for (NotaDebito notaDebito : notas) {
//            CtacteCliente ccc = cccc.findByNotaDebito(notaDebito.getId());
//            if (ccc == null) {
//                CtacteCliente ccp = new CtacteCliente();
//                ccp.setDias((short) 1);
//                ccp.setEstado((Valores.CtaCteEstado.PENDIENTE.getId()));
//                if (notaDebito.getRecibo() != null) {
//                    ccp.setEntregado(notaDebito.getImporte().doubleValue()); //monto $$
//                    ccp.setEstado((Valores.CtaCteEstado.PAGADA.getId()));
//                }
//                ccp.setNotaDebito(notaDebito);
//                ccp.setFechaCarga(notaDebito.getFechaNotaDebito());
//                ccp.setImporte(notaDebito.getImporte().doubleValue());
//                cccc.create(ccp);
//            } else {
//                System.out.println("Existe ccc (" + ccc.getId() + ") de " + notaDebito);
//            }
//        }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="agrega Cliente a todos los Recibos">
//        List<Recibo> l = new ReciboJpaController().findAll();
//        for (Recibo recibo : l) {
//            Cliente c;
//            if (recibo.getDetalle().isEmpty()) {
//                System.out.println("EMPTY:" + recibo);
//                continue;
//            }
//            if (recibo.getDetalle().get(0).getFacturaVenta() != null) {
//                c = recibo.getDetalle().get(0).getFacturaVenta().getCliente();
//            } else {
//                c = recibo.getDetalle().get(0).getNotaDebito().getCliente();
//            }
//            if (!c.equals(recibo.getCliente())) {
//                System.out.println("cambiando " + c.getId() + " por " + recibo.getCliente());
//                recibo.setCliente(c);
//                new ReciboJpaController().merge(recibo);
//            }
//        }
//</editor-fold>
    }

    private void updateCostoCompraYPrecioVentaSegunDetalleCompra() {
        List<FacturaCompra> fc = new FacturaCompraJpaController().findAll();
        ProductoJpaController pc = new ProductoJpaController();
        for (FacturaCompra facturaCompra : fc) {
            for (DetalleCompra d : facturaCompra.getDetalleCompraList()) {
                Producto p = d.getProducto();
                p.setCostoCompra(d.getPrecioUnitario());
                if (p.getUpdatePrecioVenta()) {
                    p.setPrecioVenta(d.getPrecioUnitario());
                }
                pc.merge(p);
            }
        }
    }

    private void dynamicReport() {
        try {
            Reportes r = new Reportes(null, true);
            r.showWaitingDialog();
            List<Producto> list = new ProductoJpaController().findByBienDeCambio(true);
            DynamicReportBuilder drb = new DynamicReportBuilder();
            Style currencyStyle = new Style();
            currencyStyle.setFont(Font.ARIAL_MEDIUM);
            currencyStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
            Style textStyle = new Style();
            textStyle.setFont(Font.ARIAL_MEDIUM);
            drb
                    .addColumn(ColumnBuilder.getNew().setColumnProperty("codigo", String.class).setTitle("Código").setWidth(60).setStyle(textStyle).setFixedWidth(true).build())
                    .addColumn(ColumnBuilder.getNew().setColumnProperty("nombre", String.class).setTitle("Producto").setWidth(200).setStyle(textStyle).build())
                    .addColumn(ColumnBuilder.getNew().setColumnProperty("marca.nombre", String.class).setTitle("Marca").setWidth(80).setStyle(textStyle).build());
            if (true) {
                drb.addColumn(ColumnBuilder.getNew().setColumnProperty("costoCompra", BigDecimal.class.getName()).setTitle("Costo U.").setWidth(60)
                        .setStyle(currencyStyle)
                        .setPattern("¤ #,##0.0000")
                        .setFixedWidth(true).build());
            }
            ListaPrecios lp = null;
//                    if (p.getCheckPrecioVenta().isSelected()) {
//                        drb.addColumn(ColumnBuilder.getNew()
//                                .setColumnProperty("precioVenta", BigDecimal.class.getName()).setTitle("Precio U.").setWidth(60)
//                                .setStyle(currencyStyle)
//                                .setPattern("¤ #,##0.0000")
//                                .setFixedWidth(true).build());
//                        lp = ((ComboBoxWrapper<ListaPrecios>) p.getCbListaPrecio().getSelectedItem()).getEntity();
//                        Double margen = (lp.getMargen() / 100) + 1;
//                        for (Producto producto : list) {
//                            BigDecimal precioVenta = producto.getPrecioVenta();
//                            producto.setPrecioVenta(precioVenta.multiply(BigDecimal.valueOf(margen)));
//                        }
//                    }

            drb.setTitle("Listado de Productos")
                    .setSubtitle(lp == null ? "" : "Según Lista Precios: " + lp.getNombre() + ", " + UTIL.TIMESTAMP_FORMAT.format(new Date()))
                    .setPrintBackgroundOnOddRows(true)
                    .setUseFullPageWidth(true);
            DynamicReport dr = drb.build();
            JRDataSource ds = new JRBeanCollectionDataSource(list);
            JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
            r.setjPrint(jp);
            r.viewReport();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "algo salió mal", ex);
            System.exit(0);
        }
    }
}
