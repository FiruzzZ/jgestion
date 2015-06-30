package jgestion.controller;

import afip.ws.exception.WSAFIPErrorResponseException;
import jgestion.jpa.controller.ProvinciaJpaController;
import generics.WaitingDialog;
import java.awt.GridLayout;
import java.awt.Window;
import jgestion.jpa.controller.NotaCreditoJpaController;
import jgestion.jpa.controller.SucursalJpaController;
import jgestion.jpa.controller.NotaDebitoJpaController;
import jgestion.jpa.controller.ReciboJpaController;
import jgestion.jpa.controller.FacturaVentaJpaController;
import jgestion.jpa.controller.RemitoJpaController;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Departamento;
import jgestion.entity.Municipio;
import jgestion.entity.Provincia;
import jgestion.entity.Sucursal;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMSucursal;
import jgestion.gui.PanelNumeracionActual;
import java.awt.event.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.NoResultException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestion;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Producto;
import jgestion.entity.Stock;
import jgestion.entity.UsuarioAcciones;
import jgestion.gui.PanelDistribucionStock;
import jgestion.jpa.controller.ProductoJpaController;
import jgestion.jpa.controller.StockJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.xml.sax.SAXException;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class SucursalController implements ActionListener {

    private static final Logger LOG = Logger.getLogger(SucursalController.class.getName());
    public final String CLASS_NAME = "Sucursal";
    private JDContenedor contenedor = null;
    private JDABM abm;
    private PanelABMSucursal panel;
    private SucursalJpaController jpaController;
    /**
     * Variable global interna, se una para el alta y modificación de una entidad Sucursal. Uso
     * exclusivo dentro de la class.
     */
    private Sucursal entity;
    private PanelDistribucionStock panelDis;

    public SucursalController() {
        jpaController = new SucursalJpaController();
    }

    private void initABM(Window owner, boolean isEditting, ActionEvent e) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        if (isEditting && entity == null) {
            throw new MessageException("Debe elegir una fila");
        }
        panel = new PanelABMSucursal();
        UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findAll(), true);
        AutoCompleteDecorator.decorate(panel.getCbProvincias());
        UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
        panel.setListener(this);
        if (isEditting) {
            setPanel(entity);
        }
        abm = new JDABM(owner, "ABM " + CLASS_NAME + "es", true, panel);
        abm.setLocationRelativeTo(owner);
        abm.setListener(this);
        abm.setVisible(true);
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     *
     * @param filtro
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre LIKE '" + filtro.toUpperCase() + "%'";
        }
        cargarDTM(contenedor.getDTM(), query);
    }

    public void initContenedor(Window owner, boolean modal) {
        //init contenedor
        contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME);
        contenedor.setSize(contenedor.getWidth() + 200, contenedor.getHeight());
//        contenedor.hideBtmEliminar();
        contenedor.hideBtmImprimir();
        try {
            UTIL.getDefaultTableModel(contenedor.getjTable1(),
                    new String[]{"Nº", "Nombre", "Punto Venta", "Encargado", "Dirección", "Teléfonos", "AFIP FE"},
                    new int[]{15, 100, 30, 80, 50, 60, 40},
                    new Class<?>[]{Integer.class, null, null, null, null, null, Boolean.class});
        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        cargarDTM(contenedor.getDTM(), null);
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }
        });
        contenedor.setListener(this);
        contenedor.setVisible(true);

    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        dtm.setRowCount(0);
        List<Sucursal> l;
        if (query == null) {
            l = jpaController.findAll();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, Sucursal.class).getResultList();
        }

        for (Sucursal o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getNombre(),
                UTIL.AGREGAR_CEROS(o.getPuntoVenta(), 4),
                o.getEncargado() != null ? o.getEncargado() : "",
                o.getDireccion(),
                getTele1ToString(o) + " / " + getTele2ToString(o),
                o.isWebServices()
            });
        }
    }

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        if (selectedRow > -1) {
            entity = (Sucursal) DAO.getEntityManager().find(Sucursal.class,
                    Integer.valueOf((((javax.swing.JTable) e.getSource()).getValueAt(selectedRow, 0)).toString()));
        }
    }

    private void setEntity() throws MessageException, Exception {
        if (entity == null) {
            entity = new Sucursal();
        }
        Integer puntoVenta;
        Integer factura_a, factura_b, factura_c, notaCredito_a, notaCredito_b, notaCredito_c,
                notaDebitoA, notaDebitoB, notaDebitoC, recibo_a, recibo_b, recibo_c, remito;
        // <editor-fold defaultstate="collapsed" desc="CTRLS">
        String nombre = panel.getTfNombre().toUpperCase().trim();
        if (nombre.isEmpty()) {
            throw new MessageException("Nombre no válido");
        } else {
            if (!UTIL.VALIDAR_REGEX(UTIL.REGEX_ALFANUMERIC_WITH_WHITE, nombre)) {
                throw new MessageException("Nombre solo puede contener caracteres alfanuméricos");
            }
        }
        try {
            puntoVenta = Integer.valueOf(panel.getTfPuntoVenta().getText().trim());
            if (puntoVenta < 1 || puntoVenta > 9999) {
                throw new MessageException("Punto de Venta no válido (debe estar compuesto solo por números, entre 0001 y 9999)");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Punto de Venta no válido (debe estar compuesto solo por números, entre 0001 y 9999)");
        }
        try {
            factura_a = Integer.valueOf(panel.getTfInicialFacturaA().getText().trim());
            if (factura_a < 1 || factura_a > 99999999) {
                throw new MessageException("Número de Factura 'A' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Factura 'A' no válido (debe estar compuesto solo por números)");
        }
        try {
            factura_b = Integer.valueOf(panel.getTfInicialFacturaB().getText().trim());
            if (factura_b < 1 || factura_b > 99999999) {
                throw new MessageException("Número de Factura 'B' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Factura 'B' no válido (debe estar compuesto solo por números)");
        }
        try {
            factura_c = Integer.valueOf(panel.getTfInicialFacturaC().getText().trim());
            if (factura_c < 1 || factura_c > 99999999) {
                throw new MessageException("Número de Factura 'C' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Factura 'C' no válido (debe estar compuesto solo por números)");
        }
        try {
            notaCredito_a = Integer.valueOf(panel.getTfInicialNotaCreditoA().getText().trim());
            if (notaCredito_a < 1 || notaCredito_a > 99999999) {
                throw new MessageException("Número de Nota de Credito 'A' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Crédito 'A' no válido (debe estar compuesto solo por números)");
        }
        try {
            notaCredito_b = Integer.valueOf(panel.getTfInicialNotaCreditoB().getText().trim());
            if (notaCredito_b < 1 || notaCredito_b > 99999999) {
                throw new MessageException("Número de Nota de Credito 'B' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Crédito 'B' no válido (debe estar compuesto solo por números)");
        }
        try {
            notaCredito_c = Integer.valueOf(panel.getTfInicialNotaCreditoC().getText().trim());
            if (notaCredito_c < 1 || notaCredito_c > 99999999) {
                throw new MessageException("Número de Nota de Credito 'C' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Crédito 'C' no válido (debe estar compuesto solo por números)");
        }
        try {
            notaDebitoA = Integer.valueOf(panel.getTfInicialNotaDebitoA().getText().trim());
            if (notaDebitoA < 1 || notaDebitoA > 99999999) {
                throw new MessageException("Número de Nota de Débito 'A' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Débito 'A' no válido (debe estar compuesto solo por números)");
        }
        try {
            notaDebitoB = Integer.valueOf(panel.getTfInicialNotaDebitoB().getText().trim());
            if (notaDebitoB < 1 || notaDebitoB > 99999999) {
                throw new MessageException("Número de Nota de Débito \"B\" no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Débito \"B\" no válido (debe estar compuesto solo por números)");
        }
        try {
            notaDebitoC = Integer.valueOf(panel.getTfInicialNotaDebitoC().getText().trim());
            if (notaDebitoC < 1 || notaDebitoC > 99999999) {
                throw new MessageException("Número de Nota de Débito 'C' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Débito 'C' no válido (debe estar compuesto solo por números)");
        }
        try {
            recibo_a = Integer.valueOf(panel.getTfInicialReciboA().getText().trim());
            if (recibo_a < 1 || recibo_a > 99999999) {
                throw new MessageException("Número de Recibo 'A' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Recibo 'A' no válido (debe estar compuesto solo por números)");
        }
        try {
            recibo_b = Integer.valueOf(panel.getTfInicialReciboB().getText().trim());
            if (recibo_b < 1 || recibo_b > 99999999) {
                throw new MessageException("Número de Recibo 'B' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Recibo 'B' no válido (debe estar compuesto solo por números)");
        }
        try {
            recibo_c = Integer.valueOf(panel.getTfInicialReciboC().getText().trim());
            if (recibo_c < 1 || recibo_c > 99999999) {
                throw new MessageException("Número de Recibo 'C' no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Recibo 'C' no válido (debe estar compuesto solo por números)");
        }
        try {
            remito = Integer.valueOf(panel.getTfInicialRemito().getText().trim());
            if (remito < 1 || remito > 99999999) {
                throw new MessageException("Número de Remito no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Remito no válido (debe estar compuesto solo por números)");
        }
        if (panel.getTfDireccion() == null) {
            throw new MessageException("Debe indicar la dirección de la sucursal");
        }
        if (panel.getCbProvincias().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar una Provincia, Departamento, Municipio");
        }
        if (panel.getCbDepartamentos().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Departamento");
        }
        if (panel.getCbMunicipios().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Municipio");
        }
        if ((panel.getTfInterno1() != null && panel.getTfInterno1().length() > 0) && panel.getTfTele1() == null) {
            throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
        }
        if ((panel.getTfInterno2() != null && panel.getTfInterno2().length() > 0)
                && (panel.getTfTele2() == null || panel.getTfTele2().length() < 1)) {
            throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
        }// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="NULLable's">
        if (panel.getTfEncargado() != null) {
            entity.setEncargado(panel.getTfEncargado().toUpperCase());
        }

        if (panel.getTfEmail() != null) {
            entity.setEmail(panel.getTfEmail().toUpperCase());
        }

        if (panel.getTfTele1() != null && panel.getTfTele1().length() > 0) {
            entity.setTele1(new BigInteger(panel.getTfTele1()));
            if (panel.getTfInterno1().length() > 0) {
                entity.setInterno1(new Integer(panel.getTfInterno1()));
            }
        }
        if (panel.getTfTele2() != null && panel.getTfTele2().length() > 0) {
            entity.setTele2(new BigInteger(panel.getTfTele2()));
            if (panel.getTfInterno2().length() > 0) {
                entity.setInterno2(new Integer(panel.getTfInterno2()));
            }
        }// </editor-fold>
        entity.setNombre(nombre);
        entity.setWebServices(panel.getCheckWebServices().isSelected());
        entity.setDireccion(panel.getTfDireccion());
        entity.setProvincia((Provincia) panel.getSelectedProvincia());
        entity.setDepartamento((Departamento) panel.getSelectedDepartamento());
        entity.setMunicipio((Municipio) panel.getSelectedMunicipio());
        entity.setPuntoVenta(puntoVenta);
        entity.setFactura_a(factura_a);
        entity.setFactura_b(factura_b);
        entity.setFactura_b(factura_c);
        entity.setNotaCredito_a(notaCredito_a);
        entity.setNotaCredito_b(notaCredito_b);
        entity.setNotaCredito_c(notaCredito_c);
        entity.setNotaDebitoA(notaDebitoA);
        entity.setNotaDebitoB(notaDebitoB);
        entity.setNotaDebitoC(notaDebitoC);
        entity.setRecibo_a(recibo_a);
        entity.setRecibo_b(recibo_b);
        entity.setRecibo_c(recibo_c);
        entity.setRemito(remito);
    }

    private void checkConstraints(Sucursal sucursal) throws MessageException {
        String idQuery = "";
        if (sucursal.getId() != null) {
            idQuery = "o.id !=" + sucursal.getId() + " AND ";
        }
        if (!jpaController.findByNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                + " WHERE " + idQuery + " o.nombre='" + sucursal.getNombre() + "' ").isEmpty()) {
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        }
        if (!jpaController.findByNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                + " WHERE " + idQuery + " o.puntoventa=" + sucursal.getPuntoVenta()).isEmpty()) {
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este punto de venta.");
        }
        if (sucursal.getId() != null) {
            Sucursal oldInstance = jpaController.find(sucursal.getId());
            String buttonMessage = "Para ver la actual numeración de los comprobantes, utilice el botón de \"Ver Numeración Actual\".";
            String anterior = "Nota: Si lo que desea es atrasar la numeración para cargar comprobantes anteriores (antiguos), puede hacerlo en:"
                    + "\nMenú -> Tesorería -> Venta (Numeración manual) -> "
                    + "\nEste le permitirá especificar el número del comprobante.";
            Integer next;
            if (!sucursal.getFactura_a().equals(oldInstance.getFactura_a())) {
                next = new FacturaVentaJpaController().getNextNumero(sucursal, 'a');
                if (next > sucursal.getFactura_a()) {
                    throw new MessageException("Existen registros de Factura 'A' superior a " + UTIL.AGREGAR_CEROS(sucursal.getFactura_a(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Facturación");
                }
            }
            if (!sucursal.getFactura_b().equals(oldInstance.getFactura_b())) {
                next = new FacturaVentaJpaController().getNextNumero(sucursal, 'b');
                if (next > sucursal.getFactura_b()) {
                    throw new MessageException("Existen registros de Factura 'B' superior a " + UTIL.AGREGAR_CEROS(sucursal.getFactura_b(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Facturación");
                }
            }
            if (!sucursal.getRecibo_a().equals(oldInstance.getRecibo_a())) {
                next = new ReciboJpaController().getNextNumero(sucursal, 'A');
                if (next > sucursal.getRecibo_a()) {
                    throw new MessageException("Existen registros de Recibo 'A' superior a " + UTIL.AGREGAR_CEROS(sucursal.getRecibo_a(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Recibo");
                }
            }
            if (!sucursal.getRecibo_b().equals(oldInstance.getRecibo_b())) {
                next = new ReciboJpaController().getNextNumero(sucursal, 'B');
                if (next > sucursal.getRecibo_b()) {
                    throw new MessageException("Existen registros de Recibo 'B' superior a " + UTIL.AGREGAR_CEROS(sucursal.getRecibo_a(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Recibo");
                }
            }
            if (!sucursal.getRecibo_c().equals(oldInstance.getRecibo_c())) {
                next = new ReciboJpaController().getNextNumero(sucursal, 'C');
                if (next > sucursal.getRecibo_c()) {
                    throw new MessageException("Existen registros de Recibo 'C' superior a " + UTIL.AGREGAR_CEROS(sucursal.getRecibo_a(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Recibo");
                }
            }
            if (!sucursal.getRemito().equals(oldInstance.getRemito())) {
                next = new RemitoJpaController().getNextNumero(sucursal);
                if (next > sucursal.getRemito()) {
                    throw new MessageException("Existen registros de Remito superior a " + UTIL.AGREGAR_CEROS(sucursal.getRemito(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Remito");
                }
            }
            if (!sucursal.getNotaCredito_a().equals(oldInstance.getNotaCredito_a())) {
                next = new NotaCreditoJpaController().getNextNumero(sucursal, 'A');
                if (next > sucursal.getNotaCredito_a()) {
                    throw new MessageException("Existen registros de Nota de Crédito superior a " + UTIL.AGREGAR_CEROS(sucursal.getNotaCredito_a(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Nota de Crédito");
                }
            }
            if (!sucursal.getNotaDebitoA().equals(oldInstance.getNotaDebitoA())) {
                next = new NotaDebitoJpaController().getNextNumero(sucursal, "A");
                if (next > sucursal.getNotaDebitoA()) {
                    throw new MessageException("Existen registros de Nota de Débito \"A\" superior a " + UTIL.AGREGAR_CEROS(sucursal.getNotaDebitoA(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Nota de Débito");
                }
            }
            if (!sucursal.getNotaDebitoB().equals(oldInstance.getNotaDebitoB())) {
                next = new NotaDebitoJpaController().getNextNumero(sucursal, "B");
                if (next > sucursal.getNotaDebitoB()) {
                    throw new MessageException("Existen registros de Nota de Débito \"B\" superior a " + UTIL.AGREGAR_CEROS(sucursal.getNotaDebitoA(), 8) + "."
                            + "\n" + buttonMessage
                            + "\n" + anterior + "Nota de Débito");
                }
            }
            if (panel.getCheckWebServices().isSelected()) {
                try {
                    AFIPWSController afipwsController = new AFIPWSController(null);
                    List<Integer> puntosVenta = afipwsController.getPuntoVentas();
                    if (!puntosVenta.contains(sucursal.getPuntoVenta())) {
                        throw new MessageException("El punto de venta: " + sucursal.getPuntoVenta()
                                + " no se encuentra entre los habilitado para facturación electrónica."
                                + "Habilitados: " + Arrays.toString(puntosVenta.toArray()));
                    }
                } catch (MessageException ex) {
                    throw ex;
                } catch (WSAFIPErrorResponseException ex) {
                    throw new MessageException(ex.getMessage());
                } catch (Exception ex) {
                    LOG.error("Error AFIPWSController: " + sucursal.toString(), ex);
                    throw new MessageException("Error verificando punto de venta con web services de AFIP."
                            + ex.getMessage());
                }
            }
        }
    }

    private void setPanel(Sucursal s) {
        panel.setTfNombre(s.getNombre());
        panel.getTfPuntoVenta().setText(UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4));
        panel.getCheckWebServices().setSelected(s.isWebServices());
        panel.getTfInicialFacturaA().setText(UTIL.AGREGAR_CEROS(s.getFactura_a(), 8));
        panel.getTfInicialFacturaB().setText(UTIL.AGREGAR_CEROS(s.getFactura_b(), 8));
        panel.getTfInicialFacturaC().setText(UTIL.AGREGAR_CEROS(s.getFactura_c(), 8));
        panel.getTfInicialNotaCreditoA().setText(UTIL.AGREGAR_CEROS(s.getNotaCredito_a(), 8));
        panel.getTfInicialNotaCreditoB().setText(UTIL.AGREGAR_CEROS(s.getNotaCredito_b(), 8));
        panel.getTfInicialNotaCreditoC().setText(UTIL.AGREGAR_CEROS(s.getNotaCredito_c(), 8));
        panel.getTfInicialNotaDebitoA().setText(UTIL.AGREGAR_CEROS(s.getNotaDebitoA(), 8));
        panel.getTfInicialNotaDebitoB().setText(UTIL.AGREGAR_CEROS(s.getNotaDebitoB(), 8));
        panel.getTfInicialNotaDebitoC().setText(UTIL.AGREGAR_CEROS(s.getNotaDebitoC(), 8));
        panel.getTfInicialReciboA().setText(UTIL.AGREGAR_CEROS(s.getRecibo_a(), 8));
        panel.getTfInicialReciboB().setText(UTIL.AGREGAR_CEROS(s.getRecibo_b(), 8));
        panel.getTfInicialReciboC().setText(UTIL.AGREGAR_CEROS(s.getRecibo_c(), 8));
        panel.getTfInicialRemito().setText(UTIL.AGREGAR_CEROS(s.getRemito(), 8));
        panel.setTfDireccion(s.getDireccion());
        panel.setTfEncargado(s.getEncargado());
        panel.setTfEmail(s.getEmail());
        if (s.getTele1() != null) {
            panel.setTfTele1(s.getTele1().toString());
        }
        if (s.getInterno1() != null) {
            panel.setTfInterno1(s.getInterno1().toString());
        }

        if (s.getTele2() != null) {
            panel.setTfTele2(s.getTele2().toString());
        }
        if (s.getInterno2() != null) {
            panel.setTfInterno2(s.getInterno2().toString());
        }

        for (int i = 0; i < panel.getCbProvincias().getItemCount(); i++) {
            if (panel.getCbProvincias().getItemAt(i).toString().equals(s.getProvincia().getNombre())) {
                panel.getCbProvincias().setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < panel.getCbDepartamentos().getItemCount(); i++) {
            if (panel.getCbDepartamentos().getItemAt(i).toString().equals(s.getDepartamento().getNombre())) {
                panel.getCbDepartamentos().setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < panel.getCbMunicipios().getItemCount(); i++) {
            if (panel.getCbMunicipios().getItemAt(i).toString().equals(s.getMunicipio().getNombre())) {
                panel.getCbMunicipios().setSelectedIndex(i);
                break;
            }
        }
    }

    private String getTele1ToString(Sucursal o) {
        String tele = null;
        if (o.getTele1() != null) {
            tele = o.getTele1().toString();
            if (o.getInterno1() != null) {
                tele += "-" + o.getInterno1();
            }
        }
        return tele != null ? tele : "-";
    }

    private String getTele2ToString(Sucursal o) {
        String tele = null;
        if (o.getTele2() != null) {
            tele = o.getTele2().toString();
            if (o.getInterno2() != null) {
                tele += "-" + o.getInterno1();
            }
        }
        return tele != null ? tele : "-";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource() instanceof JButton) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                try {
                    initABM(contenedor, false, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            } else if (boton.getName().equalsIgnoreCase("edit")) {
                try {
                    entity = getSelectedFromContendor();
                    initABM(contenedor, true, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }

            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (entity == null) {
                        throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
                    }
                    jpaController.remove(entity);
                    cargarDTM(contenedor.getDTM(), null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {
            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor = null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setEntity();
                    checkConstraints(entity);
                    String msg = entity.getId() == null ? "Registrado" : "Modificado";
                    if (entity.getId() == null) {
                        jpaController.persist(entity);
                        abm.showMessage(JGestion.resourceBundle.getString("info.newsucursal"), CLASS_NAME, 2);
                    } else {
                        jpaController.merge(entity);
                    }
                    abm.showMessage(msg, CLASS_NAME, 1);
                    entity = null;
                    cargarDTM(contenedor.getDTM(), null);
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panel = null;
                abm = null;
                entity = null;
            } else if (boton.getName().equalsIgnoreCase("verNumeracionActual")) {
                if (entity != null && entity.getId() != null) {
                    showNumeracionActualDialog(entity);
                } else {
                }
            }
        } // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="JTextField">
        else if (e.getSource() instanceof JTextField) {
            JTextField tf = (JTextField) e.getSource();
            if (tf.getName().equalsIgnoreCase("tfFiltro")) {
            }
        } // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="ComboBox">
        else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
            if (combo.getName().equalsIgnoreCase("cbProvincias")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panel.getCbDepartamentos(), new DepartamentoController().findDeptosFromProvincia(((Provincia) combo.getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panel.getCbDepartamentos(), new DepartamentoController().findDeptosFromProvincia(0), true);
                }
            } else if (combo.getName().equalsIgnoreCase("cbDepartamentos")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioController().findMunicipiosFromDepto(((Departamento) combo.getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioController().findMunicipiosFromDepto(0), true);
                }
            }
        }
        // </editor-fold>
    }

    private void showNumeracionActualDialog(Sucursal sucursal) {
        JDialog jd = new JDialog(abm, "Numeración actual", true);
        final PanelNumeracionActual paneln = new PanelNumeracionActual();
        Integer actual = new FacturaVentaJpaController().getNextNumero(sucursal, 'a');
        actual = actual.equals(sucursal.getFactura_a()) ? actual : actual - 1;
        paneln.setTfInicialFacturaA(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new FacturaVentaJpaController().getNextNumero(sucursal, 'b');
        actual = actual.equals(sucursal.getFactura_b()) ? actual : actual - 1;
        paneln.setTfInicialFacturaB(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new FacturaVentaJpaController().getNextNumero(sucursal, 'C');
        actual = actual.equals(sucursal.getFactura_c()) ? actual : actual - 1;
        paneln.setTfInicialFacturaC(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new ReciboJpaController().getNextNumero(sucursal, 'A');
        actual = actual.equals(sucursal.getRecibo_a()) ? actual : actual - 1;
        paneln.setTfInicialReciboA(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new ReciboJpaController().getNextNumero(sucursal, 'B');
        actual = actual.equals(sucursal.getRecibo_b()) ? actual : actual - 1;
        paneln.setTfInicialReciboB(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new ReciboJpaController().getNextNumero(sucursal, 'C');
        actual = actual.equals(sucursal.getRecibo_c()) ? actual : actual - 1;
        paneln.setTfInicialReciboC(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new RemitoJpaController().getNextNumero(sucursal);
        actual = actual.equals(sucursal.getRemito()) ? actual : actual - 1;
        paneln.setTfInicialRemito(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new NotaCreditoJpaController().getNextNumero(sucursal, 'A');
        actual = actual.equals(sucursal.getNotaCredito_a()) ? actual : actual - 1;
        paneln.setTfInicialNotaCreditoA(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new NotaCreditoJpaController().getNextNumero(sucursal, 'B');
        actual = actual.equals(sucursal.getNotaCredito_b()) ? actual : actual - 1;
        paneln.getTfInicialNotaCreditoB().setText(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new NotaCreditoJpaController().getNextNumero(sucursal, 'C');
        actual = actual.equals(sucursal.getNotaCredito_c()) ? actual : actual - 1;
        paneln.getTfInicialNotaCreditoC().setText(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new NotaDebitoJpaController().getNextNumero(sucursal, "A");
        actual = actual.equals(sucursal.getNotaDebitoA()) ? actual : actual - 1;
        paneln.setTfInicialNotaDebitoA(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new NotaDebitoJpaController().getNextNumero(sucursal, "B");
        actual = actual.equals(sucursal.getNotaDebitoB()) ? actual : actual - 1;
        paneln.setTfInicialNotaDebitoB(UTIL.AGREGAR_CEROS(actual, 8));
        actual = new NotaDebitoJpaController().getNextNumero(sucursal, "C");
        actual = actual.equals(sucursal.getNotaDebitoC()) ? actual : actual - 1;
        paneln.getTfInicialNotaDebitoC().setText(UTIL.AGREGAR_CEROS(actual, 8));
        if (sucursal.isWebServices()) {
            WaitingDialog.initWaitingDialog(jd, "AFIP WebServices", "Recuperando últimos números de comprobantes", () -> {
                try {
                    AFIPWSController afipwsController = new AFIPWSController(null);
                    paneln.getTfFactuA_AFIP().setText(afipwsController.getUltimoCompActualizado(sucursal.getPuntoVenta(), 1) + "");
                    paneln.getTfFactuB_AFIP().setText(afipwsController.getUltimoCompActualizado(sucursal.getPuntoVenta(), 6) + "");
                    paneln.getTfFactuC_AFIP().setText(afipwsController.getUltimoCompActualizado(sucursal.getPuntoVenta(), 11) + "");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Error recuperando números de últimos comprobantes\n" + ex.getMessage(),
                            "AFIP Web Services", JOptionPane.ERROR_MESSAGE);
                    LOG.error("Recuperando últimos comprobantes de Sucursal.id=" + sucursal.getId(), ex);
                }
            });
        } else {
            paneln.hideAFIPFields();
        }
        jd.add(paneln);
        jd.pack();
        jd.setLocationRelativeTo(abm);
        jd.setVisible(true);

    }

    private Sucursal getSelectedFromContendor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
        } else {
            return null;
        }
    }

    public void initDistribucionStock(Window owner) throws MessageException {
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        panelDis = new PanelDistribucionStock();
        UTIL.loadComboBox(panelDis.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
        AutoCompleteDecorator.decorate(panelDis.getCbProductos());
        List<EntityWrapper<Sucursal>> l = JGestionUtils.getWrappedSucursales(uh.getSucursales());
        UTIL.loadComboBox(panelDis.getCbSucursalOrigen(), l, false);
        UTIL.loadComboBox(panelDis.getCbSucursalDestino(), l, false);
        ActionListener aa = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setStockDistribucion();
                } catch (MessageException ex) {
                    ex.displayMessage(null);
                }
            }
        };
        panelDis.getCbProductos().addActionListener(aa);
        panelDis.getCbSucursalOrigen().addActionListener(aa);
        panelDis.getCbSucursalDestino().addActionListener(aa);
        panelDis.getBtnStockGral().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EntityWrapper<Producto> cbw = (EntityWrapper<Producto>) panelDis.getCbProductos().getSelectedItem();
                    if (cbw == null) {
                        throw new MessageException("Producto no válido");
                    }
                    Producto producto = cbw.getEntity();
                    new ProductoController().initStockGral(SwingUtilities.getWindowAncestor(panelDis), producto);
                } catch (MessageException ex) {
                    ex.displayMessage(null);
                }
            }
        });

        JDABM abm1 = new JDABM(owner, "Distribución de Stock entre Sucursales", true, panelDis);
        abm1.getbAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EntityWrapper<Producto> cbw = (EntityWrapper<Producto>) panelDis.getCbProductos().getSelectedItem();
                    Producto producto = cbw.getEntity();
                    producto = new ProductoJpaController().find(producto.getId());
                    int toDist;
                    try {
                        toDist = Integer.valueOf(panelDis.getTfCantidad().getText());
                        if (toDist < 1) {
                            throw new MessageException("Cantidad a distribuir no válida");
                        }
                    } catch (NumberFormatException ex) {
                        throw new MessageException("Cantidad a distribuir no válida");
                    }
                    Sucursal origen = ((EntityWrapper<Sucursal>) panelDis.getCbSucursalOrigen().getSelectedItem()).getEntity();
                    Sucursal destino = ((EntityWrapper<Sucursal>) panelDis.getCbSucursalDestino().getSelectedItem()).getEntity();
                    if (origen.equals(destino)) {
                        throw new MessageException("Sucursal Origen y Destino deben ser diferentes");
                    }
                    StockJpaController stockJpa = new StockJpaController();
                    Stock stockO = stockJpa.findBy(producto, origen);
                    if (stockO.getStockSucu() < toDist) {
                        throw new MessageException("La cantidad a distribuir no puede ser superior al stock actual");
                    }
                    Stock stockD = stockJpa.findBy(producto, destino);
                    stockO.setStockSucu(stockO.getStockSucu() - toDist);
                    stockD.setStockSucu(stockD.getStockSucu() + toDist);
                    stockJpa.merge(stockO);
                    stockJpa.merge(stockD);
                    UsuarioAcciones ua = UsuarioAccionesController.build(producto, producto.getId(), "DistribuciónStock: Origen=" + origen.getNombre() + ", Destino=" + destino.getNombre() + ", Producto=" + producto.getNombre() + ", cantidad=" + toDist, null, 'u');
                    new UsuarioAccionesController().create(ua);
                    panelDis.getTfCantidad().setText(null);
                } catch (MessageException ex1) {
                    ex1.displayMessage(null);
                }
            }
        });
        abm1.getbCancelar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        abm1.setLocationRelativeTo(owner);
        abm1.setVisible(true);
    }

    private void setStockDistribucion() throws MessageException {
        EntityWrapper<Producto> cbw = (EntityWrapper<Producto>) panelDis.getCbProductos().getSelectedItem();
        if (cbw == null) {
            throw new MessageException("Producto no válido");
        }
        Producto producto = cbw.getEntity();
        producto = new ProductoJpaController().find(producto.getId());
        Sucursal origen = ((EntityWrapper<Sucursal>) panelDis.getCbSucursalOrigen().getSelectedItem()).getEntity();
        Stock stockO;
        try {
            stockO = new StockJpaController().findBy(producto, origen);
        } catch (NoResultException e) {
            Stock ss = new Stock();
            ss.setProducto(producto);
            ss.setSucursal(origen);
            ss.setUsuario(UsuarioController.getCurrentUser());
            ss.setStockSucu(0);
            ss.setFechaCarga(jpaController.getServerDate());
            new StockJpaController().persist(ss);
            stockO = ss;
        }
        Sucursal destino = ((EntityWrapper<Sucursal>) panelDis.getCbSucursalDestino().getSelectedItem()).getEntity();
        panelDis.getTfStockOrigen().setText(stockO.getStockSucu() + "");
        Stock stockD;
        try {
            stockD = new StockJpaController().findBy(producto, destino);
        } catch (NoResultException e) {
            Stock ss = new Stock();
            ss.setProducto(producto);
            ss.setSucursal(destino);
            ss.setUsuario(UsuarioController.getCurrentUser());
            ss.setStockSucu(0);
            ss.setFechaCarga(jpaController.getServerDate());
            new StockJpaController().persist(ss);
            stockD = ss;
        }
        panelDis.getTfStockDestino().setText(stockD.getStockSucu() + "");
        panelDis.getTfStockActual().setText(new StockController().getStockGlobal(producto.getId()) + "");
        panelDis.setProductoFields(producto);
    }

    public void displayInformeStock(Window owner) throws MessageException {
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        JPanel pp = new JPanel();
        pp.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        pp.setLayout(new GridLayout(2, 0));
        final JComboBox<Object> cbSucursal = new JComboBox<>();
        UTIL.loadComboBox(cbSucursal, JGestionUtils.getWrappedSucursales(uh.getSucursales()), false);
        pp.add(new JLabel("Sucursal"));
        pp.add(cbSucursal);
        final JCheckBox checkStockCero = new JCheckBox("Incluir productos sin stock");
        pp.add(checkStockCero);
        final JDABM jd = new JDABM(owner, "Sucursales - Informe de stock", false, pp);
        jd.getbCancelar().setEnabled(false);
        jd.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer id = (Integer) ((EntityWrapper<Sucursal>) cbSucursal.getSelectedItem()).getId();
                    doReportSucursalStock(id, checkStockCero.isSelected());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(jd, ex.getMessage(), "Error generando reporte", JOptionPane.ERROR_MESSAGE);
                    LOG.error("reporteando", ex);
                }
            }

        });
        jd.setVisible(true);
    }

    private void doReportSucursalStock(Integer sucursalID, boolean incluirStockCero) throws MissingReportException, JRException {
        Reportes r = new Reportes("SucursalStock.jasper", "Sucursal - Stock");
        r.addParameter("SUCURSAL_ID", sucursalID);
        r.addParameter("INCLUIR_STOCK_CERO", incluirStockCero);
        r.viewReport();
    }
}
