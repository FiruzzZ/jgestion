package jgestion.controller;

import jgestion.jpa.controller.RemitoJpaController;
import jgestion.jpa.controller.NotaCreditoJpaController;
import jgestion.jpa.controller.FacturaVentaJpaController;
import jgestion.jpa.controller.ReciboJpaController;
import jgestion.jpa.controller.SucursalJpaController;
import jgestion.jpa.controller.NotaDebitoJpaController;
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
import java.math.BigInteger;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestion;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class SucursalController implements ActionListener {

    private static final Logger LOG = Logger.getLogger(SucursalController.class.getName());
    public final String CLASS_NAME = "Sucursal";
    private JDContenedor contenedor = null;
    private JDABM abm;
    private final String[] colsName = {"Nº", "Nombre", "Punto Venta", "Encargado", "Dirección", "Teléfonos", "Prov.", "Depto.", "Municipio", "Email"};
    private final int[] colsWidth = {15, 100, 20, 80, 100, 100, 40, 40, 40, 50};
    private PanelABMSucursal panel;
    private SucursalJpaController jpaController;
    /**
     * Variable global interna, se una para el alta y modificación de una
     * entidad Sucursal. Uso exclusivo dentro de la class.
     */
    private Sucursal entity;

    public SucursalController() {
        jpaController = new SucursalJpaController();
    }

    private void initABM(boolean isEditting, ActionEvent e) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        if (isEditting && entity == null) {
            throw new MessageException("Debe elegir una fila");
        }
        panel = new PanelABMSucursal();
        UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
        UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
        panel.setListener(this);
        if (isEditting) {
            setPanel(entity);
        }
        abm = new JDABM(contenedor, "ABM " + CLASS_NAME + "es", true, panel);
        abm.setLocationRelativeTo(contenedor);
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

    public void initContenedor(java.awt.Frame frame, boolean modal) {
        //init contenedor
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME);
        contenedor.setSize(contenedor.getWidth() + 200, contenedor.getHeight());
//        contenedor.hideBtmEliminar();
        contenedor.hideBtmImprimir();
        try {
            UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
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
                        o.getProvincia().getNombre(),
                        o.getDepartamento().getNombre(),
                        o.getMunicipio().getNombre(),
                        o.getEmail() != null ? o.getEmail() : ""});
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
        long puntoVenta;
        Integer factura_a, factura_b, notaCredito, notaDebitoA, notaDebitoB, recibo, remito;
        // <editor-fold defaultstate="collapsed" desc="CTRLS">
        if (panel.getTfNombre() == null || panel.getTfNombre().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
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
            notaCredito = Integer.valueOf(panel.getTfInicialNotaCredito().getText().trim());
            if (notaCredito < 1 || notaCredito > 99999999) {
                throw new MessageException("Número de Nota de Credito no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Crédito no válido (debe estar compuesto solo por números)");
        }
        try {
            notaDebitoA = Integer.valueOf(panel.getTfInicialNotaDebitoA().getText().trim());
            if (notaDebitoA < 1 || notaDebitoA > 99999999) {
                throw new MessageException("Número de Nota de Débito \"A\" no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Nota de Débito \"A\" no válido (debe estar compuesto solo por números)");
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
            recibo = Integer.valueOf(panel.getTfInicialRecibo().getText().trim());
            if (recibo < 1 || recibo > 99999999) {
                throw new MessageException("Número de Recibo no válido (debe estar compuesto solo por números, mayor a cero y menor o igual a 99999999)");
            }
        } catch (Exception e) {
            throw new MessageException("Número de Recibo no válido (debe estar compuesto solo por números)");
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
        // NOT NULLABLE's
        entity.setNombre(panel.getTfNombre().toUpperCase());
        entity.setDireccion(panel.getTfDireccion());
        entity.setProvincia((Provincia) panel.getSelectedProvincia());
        entity.setDepartamento((Departamento) panel.getSelectedDepartamento());
        entity.setMunicipio((Municipio) panel.getSelectedMunicipio());
        entity.setPuntoVenta(puntoVenta);
        entity.setFactura_a(factura_a);
        entity.setFactura_b(factura_b);
        entity.setNotaCredito(notaCredito);
        entity.setNotaDebitoA(notaDebitoA);
        entity.setNotaDebitoB(notaDebitoA);
        entity.setRecibo(recibo);
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
            if (!sucursal.getRecibo().equals(oldInstance.getRecibo())) {
                next = new ReciboJpaController().getNextNumero(sucursal);
                if (next > sucursal.getRecibo()) {
                    throw new MessageException("Existen registros de Recibo superior a " + UTIL.AGREGAR_CEROS(sucursal.getRecibo(), 8) + "."
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
            if (!sucursal.getNotaCredito().equals(oldInstance.getNotaCredito())) {
                next = new NotaCreditoJpaController().getNextNumero(sucursal);
                if (next > sucursal.getNotaCredito()) {
                    throw new MessageException("Existen registros de Nota de Crédito superior a " + UTIL.AGREGAR_CEROS(sucursal.getNotaCredito(), 8) + "."
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
        }
    }

    private void setPanel(Sucursal s) {
        panel.setTfNombre(s.getNombre());
        panel.getTfPuntoVenta().setText(UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4));
        panel.getTfInicialFacturaA().setText(UTIL.AGREGAR_CEROS(s.getFactura_a(), 8));
        panel.getTfInicialFacturaB().setText(UTIL.AGREGAR_CEROS(s.getFactura_b(), 8));
        panel.getTfInicialNotaCredito().setText(UTIL.AGREGAR_CEROS(s.getNotaCredito(), 8));
        panel.getTfInicialNotaDebitoA().setText(UTIL.AGREGAR_CEROS(s.getNotaDebitoA(), 8));
        panel.getTfInicialRecibo().setText(UTIL.AGREGAR_CEROS(s.getRecibo(), 8));
        panel.getTfInicialRemito().setText(UTIL.AGREGAR_CEROS(s.getRemito(), 8));
        panel.setTfDireccion(s.getDireccion());
        if (s.getEncargado() != null) {
            panel.setTfEncargado(s.getEncargado());
        }

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

        if (s.getEmail() != null) {
            panel.setTfEmail(s.getEmail());
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
                    initABM(false, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            } else if (boton.getName().equalsIgnoreCase("edit")) {
                try {
                    entity = getSelectedFromContendor();
                    initABM(true, e);
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
        PanelNumeracionActual paneln = new PanelNumeracionActual();
        Integer actual = (new FacturaVentaJpaController().getNextNumero(sucursal, 'a'));
        actual = actual.equals(sucursal.getFactura_a()) ? actual : actual - 1;
        paneln.setTfInicialFacturaA(UTIL.AGREGAR_CEROS(actual, 8));

        actual = (new FacturaVentaJpaController().getNextNumero(sucursal, 'b'));
        actual = actual.equals(sucursal.getFactura_b()) ? actual : actual - 1;
        paneln.setTfInicialFacturaB(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new ReciboJpaController().getNextNumero(sucursal);
        actual = actual.equals(sucursal.getRecibo()) ? actual : actual - 1;
        paneln.setTfInicialRecibo(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new RemitoJpaController().getNextNumero(sucursal);
        actual = actual.equals(sucursal.getRemito()) ? actual : actual - 1;
        paneln.setTfInicialRemito(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new NotaCreditoJpaController().getNextNumero(sucursal);
        actual = actual.equals(sucursal.getNotaCredito()) ? actual : actual - 1;
        paneln.setTfInicialNotaCredito(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new NotaDebitoJpaController().getNextNumero(sucursal, "A");
        actual = actual.equals(sucursal.getNotaDebitoA()) ? actual : actual - 1;
        paneln.setTfInicialNotaDebitoA(UTIL.AGREGAR_CEROS(actual, 8));

        actual = new NotaDebitoJpaController().getNextNumero(sucursal, "B");
        actual = actual.equals(sucursal.getNotaDebitoB()) ? actual : actual - 1;
        paneln.setTfInicialNotaDebitoB(UTIL.AGREGAR_CEROS(actual, 8));
        
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
}
