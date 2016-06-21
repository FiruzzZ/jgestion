package jgestion.test;

import jgestion.controller.*;
import jgestion.entity.*;
import jgestion.jpa.controller.*;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import generics.PropsUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class JPATesting {

    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            Properties properties = PropsUtils.load(new File("cfg.ini"));
            DAO.setProperties(properties);
            Usuario u = new UsuarioJpaController().find(1);
            new UsuarioController().checkLoginUser(u.getNick(), u.getPass());
            new JPATesting();
        } catch (Exception ex) {
            LOG.error("Error:" + ex.getLocalizedMessage(), ex);
        }
    }

    public JPATesting() throws Exception {
        Cliente cl = new ClienteJpaController().find(1);
        Integer numero = 99999;
        List<CtacteCliente> cc = new CtacteClienteController().findBy(cl, Valores.CtaCteEstado.PENDIENTE);
        Recibo r = new Recibo();
        r.setCliente(cl);
        r.setCaja(new CajaController().findCajaEntities().get(0));
        r.setFechaRecibo(new Date());
        r.setNumero(numero);
        r.setSucursal(new SucursalJpaController().findAll().get(0));
        r.setTipo('B');
        r.setUsuario(UsuarioController.getCurrentUser());
        new ReciboJpaController().persist(r);
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
            LOG.error("algo salió mal", ex);
            System.exit(0);
        }
    }
}
