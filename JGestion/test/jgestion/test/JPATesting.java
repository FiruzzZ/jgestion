package jgestion.test;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import controller.Contabilidad;
import controller.DAO;
import controller.Reportes;
import controller.TableExcelExporter;
import controller.UsuarioController;
import entity.*;
import generics.PropsUtils;
import gui.PanelDetalleFacturacion;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import jpa.controller.*;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import utilities.general.UTIL;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

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
            new UsuarioController().checkLoginUser("admin", "asdfasdf");
            new JPATesting();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    public JPATesting() throws Exception {
        List<Object[]> resultList = DAO.getEntityManager().createQuery(""
                + " SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), 'Contado', o.fechaVenta, CAST(o.importe as numeric(12,2)) "
                + "FROM FacturaVenta o WHERE o.formaPago=1 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), CONCAT('EF ', dcm.cajaMovimientos.caja.nombre, ' N°', dcm.cajaMovimientos.id), o.fechaRecibo, dcm.monto "
                + "FROM Recibo o JOIN o.pagos p JOIN DetalleCajaMovimientos dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 0 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), CONCAT('CHP ', dcm.numero),                                                   dcm.fechaCobro, dcm.importe "
                + "FROM Recibo o JOIN o.pagos p JOIN ChequePropio dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 1 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), concat('CH ', dcm.numero),                                                    dcm.fechaCobro, dcm.importe "
                + "FROM Recibo o JOIN o.pagos p JOIN ChequeTerceros dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 2 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), CONCAT('NC ', dcm.sucursal.puntoVenta, '-', dcm.numero),                dcm.fechaNotaCredito, dcm.importe "
                + "FROM Recibo o JOIN o.pagos p JOIN NotaCredito dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 3 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), concat('RE ', dcm.numero),                                                         dcm.fecha, dcm.importe "
                + "FROM Recibo o JOIN o.pagos p JOIN ComprobanteRetencion dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 4 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), concat('TR ', dcm.descripcion),                                       dcm.fechaCreditoDebito, dcm.credito "
                + "FROM Recibo o JOIN o.pagos p JOIN  CuentabancariaMovimientos dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 5 "
                + "UNION SELECT CONCAT(o.sucursal.puntoVenta, '-', o.numero), concat('ES ', dcm.descripcion),                                                o.fechaRecibo, dcm.importe "
                + "FROM Recibo o JOIN o.pagos p JOIN Especie dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 6 "
                ).getResultList();
        for (Object[] o : resultList) {
            System.out.println(Arrays.toString(o));
        }
        System.out.println(resultList.size());
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
