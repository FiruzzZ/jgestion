package jgestion.controller;

import jgestion.entity.SubCuenta;
import jgestion.entity.Caja;
import jgestion.entity.Cuenta;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.FacturaVenta;
import jgestion.entity.UnidadDeNegocio;
import jgestion.entity.CajaMovimientos;
import jgestion.gui.PanelBuscadorCajaToCaja;
import jgestion.gui.PanelMovimientosVarios;
import jgestion.gui.JDCajaToCaja;
import jgestion.gui.JDABM;
import jgestion.gui.JDCierreCaja;
import jgestion.gui.PanelBuscadorCajasCerradas;
import jgestion.gui.PanelBuscadorMovimientosVarios;
import jgestion.gui.JDBuscador;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import generics.GenericBeanCollection;
import generics.ProjectUtils;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jgestion.ActionListenerManager;
import jgestion.JGestionUtils;
import jgestion.JGestion;
import jgestion.Wrapper;
import jgestion.jpa.controller.CajaMovimientosJpaController;
import jgestion.jpa.controller.SubCuentaJpaController;
import jgestion.jpa.controller.UnidadDeNegocioJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 * Encargada de registrar todos los asientos (ingresos/egresos) de las cajas..
 *
 * @author FiruzzZ
 */
public class CajaMovimientosController implements ActionListener {

    private static final Logger LOG = LogManager.getLogger();
    public static final String CLASS_NAME = CajaMovimientos.class.getSimpleName();
    private JDCierreCaja jdCierreCaja;
    private JDCajaToCaja jdCajaToCaja;
    private JDABM abm;
    private PanelMovimientosVarios panelMovVarios;
    private JDBuscador buscador;
    private PanelBuscadorMovimientosVarios panelBuscadorMovimientosVarios;
    private PanelBuscadorCajasCerradas panelBuscadorCajasCerradas;
    private CajaMovimientos selectedCajaMovimientos;
    private PanelBuscadorCajaToCaja panelBuscadorCajaToCaja;
    private CajaMovimientosJpaController jpaController;

    public CajaMovimientosController() {
        jpaController = new CajaMovimientosJpaController();
    }

    /**
     * Arma la descripción del detalleCajaMovimiento. Si se imprime factura ej: F + [letra de
     * factura] + [número de factura] Si es interno ej: I + [número de movimento interno]
     *
     * @param factura
     * @return Un String con la descripción.
     */
    private String getDescripcion(FacturaVenta facturaVenta) {
        String codigo_descrip;
        if (facturaVenta.getNumero() == 0) {
            codigo_descrip = "I" + facturaVenta.getMovimientoInterno();
        } else {
            codigo_descrip = "F" + facturaVenta.getTipo()
                    + UTIL.AGREGAR_CEROS(String.valueOf(facturaVenta.getNumero()), 12);
        }
        return codigo_descrip;
    }

    /**
     * Crea (abre) una <code>CajaMovimientos</code> implicitamente con la creación de una
     * <code>Caja</code>, así como el 1er <code>DetalleCajaMovimientos</code> con monto inicial $0.
     *
     * @param caja a la cual se van a vincular la <code>CajaMovimientos</code> y
     * <code>DetalleCajaMovimientos</code>.
     */
    void nueva(Caja caja) {
        CajaMovimientos cm = new CajaMovimientos();
        cm.setCaja(caja);
        cm.setFechaApertura(new Date());
        cm.setMontoApertura(BigDecimal.ZERO);
        cm.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>(1));
        //creando el 1er movimiento de la caja (apertura en $0)
        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        dcm.setDescripcion("Apertura de caja (creación)");
        dcm.setIngreso(true);
        dcm.setMonto(BigDecimal.ZERO);
        dcm.setNumero(-1); //meaningless yet...
        dcm.setTipo(DetalleCajaMovimientosController.APERTURA_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        dcm.setCuenta(CuentaController.SIN_CLASIFICAR);
        dcm.setCajaMovimientos(cm);
        cm.getDetalleCajaMovimientosList().add(dcm);
        jpaController.persist(cm);
    }

    /**
     * Crea (abre) LA SIGUIENTE <code>CajaMovimientos</code> implicita POST cierre de la actual. El
     * montoApertura de la nueva <code>CajaMovimientos</code> es == al montoCierre de la anterior.
     * La fechaApertura de la nueva es == a un día después de la anterior.
     *
     * @param cajaMovimiento la que precede a la que se va abrir.
     */
    private void abrirNextCajaMovimiento(CajaMovimientos cajaMovimiento) throws Exception {
        CajaMovimientos nextCaja = new CajaMovimientos();
        nextCaja.setCaja(cajaMovimiento.getCaja());
        //fecha de apertura, un día después del cierre de ESTA
        nextCaja.setFechaApertura(UTIL.customDateByDays(cajaMovimiento.getFechaCierre(), +1));
        nextCaja.setMontoApertura(cajaMovimiento.getMontoCierre());
        nextCaja.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>());
        //creando el 1er detalleCajaMovimiento..
        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        dcm.setCajaMovimientos(nextCaja);
        dcm.setDescripcion("Apertura de caja");
        dcm.setIngreso(true);
        dcm.setMonto(nextCaja.getMontoApertura());
        dcm.setNumero(-1); //meaningless yet...
        dcm.setTipo(DetalleCajaMovimientosController.APERTURA_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        if (dcm.getCuenta() == null) {
            //default value
            dcm.setCuenta(CuentaController.SIN_CLASIFICAR);
        }
        nextCaja.getDetalleCajaMovimientosList().add(dcm);
        jpaController.persist(nextCaja);
    }

    public void displayCierreCaja(JFrame frame, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.CERRAR_CAJAS);
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>

        if (jdCierreCaja == null) {
            jdCierreCaja = new JDCierreCaja(frame, modal);
            UTIL.getDefaultTableModel(
                    jdCierreCaja.getjTable1(),
                    new String[]{"Descripción", "Monto", "Fecha (Hora)", "Usuario"},
                    new int[]{300, 20, 60, 40},
                    new Class<?>[]{null, String.class, String.class, null});
            UTIL.setHorizonalAlignment(jdCierreCaja.getjTable1(), String.class, JLabel.RIGHT);
            UTIL.loadComboBox(jdCierreCaja.getCbCaja(), getCajaMovimientosActivasFromCurrentUser(), true);
            jdCierreCaja.getbBuscar().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initBuscadorCierreCaja();
                }
            });
            jdCierreCaja.getbImprimir().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (selectedCajaMovimientos != null) {
                            imprimirCierreCaja(selectedCajaMovimientos);
                        } else {
                            imprimirCierreCaja((CajaMovimientos) jdCierreCaja.getCbCaja().getSelectedItem());
                        }
                    } catch (MissingReportException ex) {
                        JOptionPane.showMessageDialog(jdCierreCaja, ex.getMessage());
                        LogManager.getLogger();//(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                    } catch (JRException ex) {
                        JOptionPane.showMessageDialog(jdCierreCaja, ex.getMessage());
                        LogManager.getLogger();//(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                    } catch (ClassCastException ex) {
                        jdCierreCaja.showMessage("No hay ninguna Caja seleccionada", null, 2);
                    }
                }
            });
            jdCierreCaja.getbCerrar().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jdCierreCaja.getCbCaja().getSelectedIndex() > 0) {
                        try {
                            cerrarCajaMovimiento((CajaMovimientos) jdCierreCaja.getCbCaja().getSelectedItem());
                        } catch (MessageException ex) {
                            jdCierreCaja.showMessage(ex.getMessage(), "Error", 2);
                        } catch (Exception ex) {
                            jdCierreCaja.showMessage(ex.getMessage(), "Error.Exception", 0);
                            LogManager.getLogger();//(CajaMovimientosController.class.getSimpleName()).fatal(ex.getLocalizedMessage(), ex);
                        }
                    } else {
                        jdCierreCaja.showMessage("No hay Caja seleccionada", "Error", 2);
                    }
                }
            });
            jdCierreCaja.getCbCaja().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jdCierreCaja.getCbCaja().getSelectedIndex() > 0) {
                        setInfoCajaMovimientos((CajaMovimientos) jdCierreCaja.getCbCaja().getSelectedItem());
                    } else {
                        UTIL.limpiarDtm(jdCierreCaja.getDtm());
                        jdCierreCaja.setDcApertura(null);
                    }
                }
            });
            jdCierreCaja.setListener(this);
            jdCierreCaja.setLocation(jdCierreCaja.getOwner().getX() + 150, jdCierreCaja.getOwner().getY() + 100);
            jdCierreCaja.setVisible(true);
        } else {
            UTIL.loadComboBox(jdCierreCaja.getCbCaja(), getCajaMovimientosActivasFromCurrentUser(), true);
            jdCierreCaja.setVisible(true);
            jdCierreCaja.requestFocus();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();

            if (jdCierreCaja != null) {
            } // <editor-fold defaultstate="collapsed" desc="Panel y Buscador CajaToCaja">
            else if (jdCajaToCaja != null) {
            }// </editor-fold>

        }// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="JComboBox">
        else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
            // <editor-fold defaultstate="collapsed" desc="Cierre de caja">
            if (combo.getName().equalsIgnoreCase("caja")) {
            } // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc="Panel CajaToCaja">
            else if (combo.getName().equalsIgnoreCase("cajaOrigen")) {
            } else if (combo.getName().equalsIgnoreCase("cajaDestino")) {
            }// </editor-fold>
        }// </editor-fold>
    }

    private void setInfoCajaMovimientos(CajaMovimientos cajaMovimientos) {
        DefaultTableModel dtm = jdCierreCaja.getDtm();
        dtm.setRowCount(0);
        jdCierreCaja.setDcApertura(cajaMovimientos.getFechaApertura());

        // el detalleCajaMov de "apertura de caja" es ingreso = true
        // PERO NO DEBE ser parte del Total de Ingresos de la Caja
        // por eso se le resta una vez ... y luego se suma..
        BigDecimal totalIngresos = cajaMovimientos.getMontoApertura().negate();
        BigDecimal totalEgresos = BigDecimal.ZERO;
        List<DetalleCajaMovimientos> detalleCajaMovimientoList = new DetalleCajaMovimientosController().getDetalleCajaMovimientosByCajaMovimiento(cajaMovimientos.getId());
        for (DetalleCajaMovimientos detalleCajaMovimientos : detalleCajaMovimientoList) {
            //sumando totales..
            if (detalleCajaMovimientos.getIngreso()) {
                totalIngresos = totalIngresos.add(detalleCajaMovimientos.getMonto());
            } else {
                //los montos "egreso" son negativos
                totalEgresos = totalEgresos.add(detalleCajaMovimientos.getMonto());
            }

            //carga de tabla..............
            dtm.addRow(new Object[]{
                detalleCajaMovimientos.getDescripcion(),
                UTIL.PRECIO_CON_PUNTO.format(detalleCajaMovimientos.getMonto()),
                UTIL.DATE_FORMAT.format(detalleCajaMovimientos.getFecha()) + " (" + UTIL.TIME_FORMAT.format(detalleCajaMovimientos.getFecha()) + ")",
                detalleCajaMovimientos.getUsuario()
            });
        }
        dtm.addRow(new Object[]{"Total Ingresos", UTIL.PRECIO_CON_PUNTO.format(totalIngresos)});
        dtm.addRow(new Object[]{"Total Egresos", UTIL.PRECIO_CON_PUNTO.format(totalEgresos)});
        dtm.addRow(new Object[]{"Total", UTIL.PRECIO_CON_PUNTO.format(cajaMovimientos.getMontoApertura().add(totalIngresos).add(totalEgresos))});
        if (cajaMovimientos.getFechaCierre() != null) {
            selectedCajaMovimientos = cajaMovimientos;
            jdCierreCaja.setDcCierre(cajaMovimientos.getFechaCierre());
            jdCierreCaja.getDcCierre().setEnabled(false);
            jdCierreCaja.getbCerrar().setEnabled(false);
            dtm.addRow(new Object[]{"----CAJA CERRADA----CAJA CERRADA----CAJA CERRADA----CAJA CERRADA----"});

            jdCierreCaja.getLabelCAJACERRADA().setText("CAJA Nº" + selectedCajaMovimientos.getId() + " CERRADA");
            jdCierreCaja.getLabelCAJACERRADA().setVisible(true);
        } else {
            selectedCajaMovimientos = null;
            jdCierreCaja.setDcCierre(null);
            jdCierreCaja.getDcCierre().setEnabled(true);
            jdCierreCaja.getbCerrar().setEnabled(true);
            jdCierreCaja.getLabelCAJACERRADA().setVisible(false);
        }
    }

    private void cerrarCajaMovimiento(CajaMovimientos cajaMovimientos) throws MessageException, MissingReportException, JRException, Exception {
        if (cajaMovimientos == null) {
            throw new MessageException("Debe elegir una Caja para cerrar");
        }
        if (jdCierreCaja.getFechaCierre() == null) {
            throw new MessageException("Fecha de cierre no válida");
        }
        if (jdCierreCaja.getDcApertura().after(jdCierreCaja.getFechaCierre())) {
            throw new MessageException("La fecha de cierre de caja no puede ser anterior a la de apertura");
        }

        int imprimir_caja_OK = JOptionPane.showConfirmDialog(jdCierreCaja,
                "¿Imprimir cierre de caja?",
                "Cierre de Caja",
                JOptionPane.OK_CANCEL_OPTION);
        cajaMovimientos.setFechaCierre(jdCierreCaja.getFechaCierre());
        cajaMovimientos.setMontoCierre(new BigDecimal(jdCierreCaja.getjTable1().getValueAt(jdCierreCaja.getjTable1().getRowCount() - 1, 1).toString()));
        //datos implicitos
        cajaMovimientos.setSistemaFechaCierre(new Date());
        cajaMovimientos.setUsuarioCierre(UsuarioController.getCurrentUser());
        jpaController.merge(cajaMovimientos);

        abrirNextCajaMovimiento(cajaMovimientos);
        if (imprimir_caja_OK == 0) {
            imprimirCierreCaja(cajaMovimientos);
        }
        //refresh jdCierreCaja...................
        reloadJDCierreCaja();
    }

    /**
     * Busca las Cajas ACTIVAS a las cuales tiene permisos el "Usuario actualmente loggeado",
     * después busca las CajaMovimientos correspondientes a cada Caja
     *
     * @return eso que tanto buscó!..
     */
    private List<CajaMovimientos> getCajaMovimientosActivasFromCurrentUser() {
        //get cajas permitidas para ESTE usuario
        List<Caja> cajasPermitidasList = new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true);
        List<CajaMovimientos> cajaMovimientosAbiertasList = new ArrayList<CajaMovimientos>(cajasPermitidasList.size());
        CajaMovimientos cajaMovimiento;
        for (Caja caja : cajasPermitidasList) {
            try {
                //get cajaMovim abierta correspondiente a cada Caja
                cajaMovimiento = jpaController.findCajaMovimientoAbierta(caja);
            } catch (NoResultException ex) {
                LogManager.getLogger();//(this.getClass()).error("No debería entrar acá!! fixCaja=" + caja);
                fixCajaMalAbierta(caja);
                cajaMovimiento = jpaController.findCajaMovimientoAbierta(caja);
            }
//            if (cajaMovimiento != null) {
            cajaMovimientosAbiertasList.add(cajaMovimiento);
//            }
        }
        return cajaMovimientosAbiertasList;
    }

    private void reloadJDCierreCaja() {
        UTIL.loadComboBox(jdCierreCaja.getCbCaja(), getCajaMovimientosActivasFromCurrentUser(), true);
        jdCierreCaja.setDcApertura(null);
        jdCierreCaja.setDcCierre(null);
    }

    public void initCajaToCaja(JFrame frame, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);

        jdCajaToCaja = new JDCajaToCaja(frame, modal);
        jdCajaToCaja.setLocationRelativeTo(frame);
        UTIL.loadComboBox(jdCajaToCaja.getCbCajaOrigen(), getCajaMovimientosActivasFromCurrentUser(), true);
        UTIL.loadComboBox(jdCajaToCaja.getCbCajaDestino(), getCajaMovimientosActivasFromCurrentUser(), true);
        jdCajaToCaja.getTfMovimiento().setText(String.valueOf(getNextMovimientoCajaToCaja()));
        jdCajaToCaja.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    asentarMovimientoCajaToCaja();
                } catch (MessageException ex) {
                    jdCajaToCaja.showMessage(ex.getMessage(), "Error", 2);
                } catch (Exception ex) {
                    jdCajaToCaja.showMessage(ex.getMessage(), "Error", 0);
                    ex.printStackTrace();
                }
            }
        });
        jdCajaToCaja.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initBuscadorCajaToCaja(jdCajaToCaja);
            }
        });
        jdCajaToCaja.getCbCajaOrigen().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDatosCajaToCajaCombo("cajaOrigen");
            }
        });
        jdCajaToCaja.getCbCajaDestino().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDatosCajaToCajaCombo("y si no es origen tiene q ser destino.. no ?");
            }
        });
        jdCajaToCaja.setListener(this);
        jdCajaToCaja.setVisible(true);
    }

    private void asentarMovimientoCajaToCaja() throws MessageException, Exception {
        if (jdCajaToCaja.getCbCajaOrigen().getSelectedIndex() < 1) {
            throw new MessageException("Elegir Caja de origen");
        }
        if (jdCajaToCaja.getCbCajaDestino().getSelectedIndex() < 1) {
            throw new MessageException("Elegir Caja de destino");
        }
        if (jdCajaToCaja.getCbCajaOrigen().getSelectedIndex() == jdCajaToCaja.getCbCajaDestino().getSelectedIndex()) {
            throw new MessageException("La Caja de destino no puede ser la misma que la de origen");
        }
        if (jdCajaToCaja.getTfMontoMovimiento().getText().length() < 1) {
            throw new MessageException("Ingresar monto del movimiento");
        }
        BigDecimal monto;
        try {
            monto = BigDecimal.valueOf(Double.valueOf(jdCajaToCaja.getTfMontoMovimiento().getText().trim()));
            if (monto.compareTo(BigDecimal.ZERO) != 1) {
                throw new MessageException("El monto de movimiento debe ser mayor a 0");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto de movimiento no válido");
        }

        CajaMovimientos cajaOrigen = (CajaMovimientos) jdCajaToCaja.getCbCajaOrigen().getSelectedItem();
        CajaMovimientos cajaDestino = (CajaMovimientos) jdCajaToCaja.getCbCajaDestino().getSelectedItem();

        String observ = "";
        if (jdCajaToCaja.getTfObservacion().getText().trim().length() > 0) {
            observ = jdCajaToCaja.getTfObservacion().getText().trim();
        }
        // formato: N#-{cajaOrigen.nombre} -> {cajaDestino.nombre} [observacio]
        String descripcion = "N" + jdCajaToCaja.getTfMovimiento().getText() + "- " + cajaOrigen.toString() + " -> " + cajaDestino.toString();
        if (observ.length() > 0) {
            descripcion += " " + observ;
        }

        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        // <editor-fold defaultstate="collapsed" desc="Origen to Destino - EGRESO">
        dcm.setCajaMovimientos(cajaOrigen);
        dcm.setDescripcion(descripcion);
        dcm.setIngreso(false);
        dcm.setMonto(monto.negate()); // <--- NEGATIVIZAR!
        dcm.setNumero(Integer.parseInt(jdCajaToCaja.getTfMovimiento().getText()));
        dcm.setTipo(DetalleCajaMovimientosController.MOVIMIENTO_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        new DetalleCajaMovimientosController().create(dcm);// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Destino to Origen - INGRESO">
        dcm = new DetalleCajaMovimientos();
        dcm.setCajaMovimientos(cajaDestino);
        dcm.setDescripcion(descripcion);
        dcm.setIngreso(true);
        dcm.setMonto(monto);
        dcm.setNumero(Integer.parseInt(jdCajaToCaja.getTfMovimiento().getText()));
        dcm.setTipo(DetalleCajaMovimientosController.MOVIMIENTO_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        new DetalleCajaMovimientosController().create(dcm);// </editor-fold>

        jdCajaToCaja.showMessage("Realizado", "Movimiento entre cajas", 1);
        jdCajaToCaja.resetPanel();
        jdCajaToCaja.getTfMovimiento().setText(String.valueOf(getNextMovimientoCajaToCaja()));
        DAO.getEntityManager().clear();
    }

    private int getNextMovimientoCajaToCaja() {
        return jpaController.getNextNumeroMovimientoCajaToCaja();
    }

    private void setDatosCajaToCajaCombo(String name) {
        String balanceCajaActual = null;
        Date fechaApertura = null;
        try {
            CajaMovimientos cajaMovimientos;
            if (name.equalsIgnoreCase("cajaOrigen")) {
                cajaMovimientos = (CajaMovimientos) jdCajaToCaja.getCbCajaOrigen().getSelectedItem();
            } else {
                cajaMovimientos = (CajaMovimientos) jdCajaToCaja.getCbCajaDestino().getSelectedItem();
            }
            //si no saltó la ClassCastException...
            cajaMovimientos = jpaController.find(cajaMovimientos.getId());
            fechaApertura = cajaMovimientos.getFechaApertura();

            BigDecimal totalIngresos = BigDecimal.ZERO; // VA INCLUIR monto de apertura
            BigDecimal totalEgresos = BigDecimal.ZERO;
            List<DetalleCajaMovimientos> detalleCajaMovimientoList = cajaMovimientos.getDetalleCajaMovimientosList();
            for (DetalleCajaMovimientos detalleCajaMovimientos : detalleCajaMovimientoList) {
                if (detalleCajaMovimientos.getIngreso()) {
                    totalIngresos = totalIngresos.add(detalleCajaMovimientos.getMonto());
                } else {
                    //siempre son montos negativos
                    totalEgresos = totalEgresos.add(detalleCajaMovimientos.getMonto());
                }
            }
            balanceCajaActual = UTIL.PRECIO_CON_PUNTO.format(totalIngresos.add(totalEgresos));
        } catch (ClassCastException e) {
            //ignored..
        }

        if (name.equalsIgnoreCase("CajaOrigen")) {
            jdCajaToCaja.getTfTotalOrigen().setText(balanceCajaActual);
            jdCajaToCaja.getDcOrigen().setDate(fechaApertura);
        } else {
            jdCajaToCaja.getTfTotalDestino().setText(balanceCajaActual);
            jdCajaToCaja.getDcDestino().setDate(fechaApertura);
        }
    }

    /**
     * Desplega la GUI para realizar Movimientos varios
     *
     * @param owner papi Component
     * @param modal
     * @return
     * @throws MessageException
     */
    public JDialog getABMMovimientosVarios(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        initMovimientosVarios(owner, modal, null);
        return abm;
    }

    private void initMovimientosVarios(Window owner, boolean modal, final DetalleCajaMovimientos toEdit) throws MessageException {
        if (panelMovVarios == null) {
            panelMovVarios = new PanelMovimientosVarios();
            panelMovVarios.setVisibleResponsables(false);
            panelMovVarios.getbBuscar().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initBuscadorMovimientosVarios(abm);
                    buscador.setVisible(true);
                }
            });
            panelMovVarios.getRadioIngreso().addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    System.out.println("getRadioIngreso()");
                    if (panelMovVarios.getRadioIngreso().isSelected()) {
                        ActionListenerManager.setCuentasIngresosSubcuentaActionListener(panelMovVarios.getCbCuenta(), false, panelMovVarios.getCbSubCuenta(), true, true);
                    } else {
                        ActionListenerManager.setCuentasEgresosSubcuentaActionListener(panelMovVarios.getCbCuenta(), false, panelMovVarios.getCbSubCuenta(), true, true);
                    }
                }
            });
        }
        if (new UnidadDeNegocioJpaController().findAll().isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("info.unidaddenegociosempty"));
        }
        List<EntityWrapper<Caja>> ll = JGestionUtils.getWrappedCajas(new UsuarioHelper().getCajas(true));
        if (ll.isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.caja"));
        }
        UTIL.loadComboBox(panelMovVarios.getCbCaja(), ll, false);
        List<EntityWrapper<UnidadDeNegocio>> l = new Wrapper<UnidadDeNegocio>().getWrapped(new UnidadDeNegocioJpaController().findAll());
        UTIL.loadComboBox(panelMovVarios.getCbUnidadDeNegocio(), l, false);
        ActionListenerManager.setCuentasIESubcuentaActionListener(panelMovVarios.getCbCuenta(), false, panelMovVarios.getCbSubCuenta(), true, true);
        abm = new JDABM(owner, "Movimientos Varios", modal, panelMovVarios);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (toEdit == null) {
                        abm.getbAceptar().setEnabled(false);
                        DetalleCajaMovimientos dcm = setMovimientoVarios();
                        new DetalleCajaMovimientosController().create(dcm);
                        abm.showMessage("Realizado..", "Movimientos Varios Nº" + dcm.getId(), 1);
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(abm, "¿Imprimir comprobante?")) {
                            doReport(dcm);
                        }
                        cleanPanelMovimientosVarios();
                    } else {
                        DetalleCajaMovimientos dcm = setMovimientoVarios();
                        dcm.setId(toEdit.getId());
                        new DetalleCajaMovimientosController().merge(dcm);
                        abm.showMessage("Editado..", "Movimientos Varios Nº" + dcm.getId(), 1);
                        abm.dispose();
                        UTIL.limpiarDtm(buscador.getjTable1());
                    }
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), "Error", 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), "Error", 0);
                } finally {
                    abm.getbAceptar().setEnabled(true);
                }
            }
        });
        abm.setLocationRelativeTo(owner);
        abm.toFront();
    }

    private void doReport(DetalleCajaMovimientos dcm) throws MissingReportException, JRException {
        Reportes r = new Reportes("JGestion_ComprobanteMovimientosVarios.jasper", "Comprobante N° " + dcm.getId());
        r.addMembreteParameter();
        r.addParameter("ENTITY_ID", dcm.getId());
        r.viewReport();
    }

    /**
     * Asienta movimientos varios (tipo == 8)
     *
     * @throws MessageException
     */
    @SuppressWarnings("unchecked")
    DetalleCajaMovimientos setMovimientoVarios() throws MessageException {
        //ctrl's................
        Caja caja;
        UnidadDeNegocio unidadDeNegocio;
        Cuenta cuenta;
        SubCuenta subCuenta;
        try {
            caja = ((EntityWrapper<Caja>) panelMovVarios.getCbCaja().getSelectedItem()).getEntity();
        } catch (ClassCastException ex) {
            throw new MessageException("No tiene acceso a ninguna Caja");
        }
        try {
            unidadDeNegocio = ((EntityWrapper<UnidadDeNegocio>) panelMovVarios.getCbUnidadDeNegocio().getSelectedItem()).getEntity();
        } catch (ClassCastException ex) {
            throw new MessageException("Unidad de Negocios no válida");
        }
        try {
            cuenta = ((EntityWrapper<Cuenta>) panelMovVarios.getCbCuenta().getSelectedItem()).getEntity();
        } catch (ClassCastException e) {
            throw new MessageException("Cuenta no válida");
        }
        try {
            subCuenta = ((EntityWrapper<SubCuenta>) panelMovVarios.getCbSubCuenta().getSelectedItem()).getEntity();
        } catch (ClassCastException ex) {
            subCuenta = null;
        }
        BigDecimal monto;
        try {
            monto = BigDecimal.valueOf(Double.valueOf(panelMovVarios.getTfMontoMovimiento().getText().trim()));
            if (monto.compareTo(BigDecimal.ZERO) != 1) {
                throw new MessageException("El monto debe ser mayor a 0");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto no válido");
        }
        String descripcion = panelMovVarios.getTfDescripcion().getText().trim();
        if (descripcion.isEmpty()) {
            throw new MessageException("Debe ingresar una Descripción");
        }

        //setting entity.....
        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        dcm.setCajaMovimientos(new CajaMovimientosJpaController().findCajaMovimientoAbierta(caja));
        dcm.setIngreso(panelMovVarios.isIngreso());
        dcm.setDescripcion("MV" + (dcm.getIngreso() ? "I" : "E") + "-" + descripcion);
        dcm.setMonto(dcm.getIngreso() ? monto : monto.negate());
        dcm.setTipo(DetalleCajaMovimientosController.MOVIMIENTO_VARIOS);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        dcm.setFechaMovimiento(panelMovVarios.getDcMovimientoFecha());
        dcm.setUnidadDeNegocio(unidadDeNegocio);
        dcm.setCuenta(cuenta);
        dcm.setSubCuenta(subCuenta);
        return dcm;
    }

    private void initBuscadorMovimientosVarios(Window owner) {
        panelBuscadorMovimientosVarios = new PanelBuscadorMovimientosVarios();
        UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbCaja(), JGestionUtils.getWrappedCajas(new UsuarioHelper().getCajas(true)), true);
        UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbUnidadDeNegocio(), JGestionUtils.getWrappedUnidadDeNegocios(new UnidadDeNegocioJpaController().findAll()), true);
        ActionListenerManager.setCuentasIngresosSubcuentaActionListener(panelBuscadorMovimientosVarios.getCbCuenta(), true, panelBuscadorMovimientosVarios.getCbSubCuenta(), true, true);
        buscador = new JDBuscador(owner, "Buscardor - Movimientos varios", false, panelBuscadorMovimientosVarios);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"detalleCajaMovimiento.id", "Caja", "Descripción", "U. de Negocio", "Cuenta", "Sub Cuenta", "Mov. Fecha", "Monto", "Fecha (Sistema)", "Usuario"},
                new int[]{1, 70, 160, 60, 60, 60, 40, 20, 60, 50});
        buscador.getjTable1().getColumnModel().getColumn(6).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(8).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQueryMovimientosVarios();
                    cargarTablaBuscadorMovimientosVarios(query);
                } catch (Exception ex) {
                    LogManager.getLogger();//(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                }
            }
        });
        buscador.getBtnImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQueryMovimientosVarios();
                    cargarTablaBuscadorMovimientosVarios(query);
                    doReportMovimientosVarios();
                } catch (Exception ex) {
                    LogManager.getLogger();//(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                }
            }
        });
        buscador.getBtnToExcel().setIcon(ProjectUtils.getIcon("impresora.png"));
        buscador.getBtnToExcel().setText("Imp. Comprobante");
        buscador.getBtnToExcel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer dcmID = (Integer) UTIL.getSelectedValueFromModel(buscador.getjTable1(), 0);
                try {
                    if (dcmID == null) {
                        throw new MessageException("Debe seleccionar la fila del comprobante que desea imprimir");
                    }
                    doReport(new DetalleCajaMovimientos(dcmID));
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                } catch (JRException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                }

            }
        });
        buscador.setLocationRelativeTo((Component) owner);
        buscador.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private String armarQueryMovimientosVarios() throws Exception {
        String query = "SELECT o.id, CONCAT(o.cajaMovimientos.caja.nombre, CONCAT(\" (\", CONCAT(o.cajaMovimientos.id, \")\"))),"
                + " o.descripcion, o.unidadDeNegocio, o.cuenta, o.subCuenta, o.fechaMovimiento, o.monto, o.fecha, o.usuario.nick FROM " + DetalleCajaMovimientos.class.getSimpleName() + " o "
                + " WHERE o.tipo=" + DetalleCajaMovimientosController.MOVIMIENTO_VARIOS;
        if (panelBuscadorMovimientosVarios.getCbCaja().getSelectedIndex() > 0) {
            query += " AND o.cajaMovimientos.caja.id=" + ((EntityWrapper<Caja>) panelBuscadorMovimientosVarios.getCbCaja().getSelectedItem()).getId();
        } else {
            // carga todas las Cajas que tiene permitidas..
            query += " AND (";
            for (int i = 1; i < panelBuscadorMovimientosVarios.getCbCaja().getItemCount(); i++) {
                if (i > 1) {
                    query += " OR ";
                }
                query += " o.cajaMovimientos.caja.id =" + ((EntityWrapper<Caja>) panelBuscadorMovimientosVarios.getCbCaja().getItemAt(i)).getId();
            }
            query += ")";
        }

        if (panelBuscadorMovimientosVarios.getCbEstadoCaja().getSelectedIndex() == 0) {
            query += " AND o.cajaMovimientos.fechaCierre IS NULL ";
        } else if (panelBuscadorMovimientosVarios.getCbEstadoCaja().getSelectedIndex() == 1) {
            query += " AND o.cajaMovimientos.fechaCierre IS NOT NULL ";
        }

        if (panelBuscadorMovimientosVarios.getCbIngresoEgreso().getSelectedIndex() == 1) {
            query += " AND o.ingreso = TRUE";
        } else if (panelBuscadorMovimientosVarios.getCbIngresoEgreso().getSelectedIndex() == 2) {
            query += " AND o.ingreso = FALSE";
        }

        if (panelBuscadorMovimientosVarios.getDcDesde() != null) {
            query += " AND o.fechaMovimiento >='" + UTIL.yyyy_MM_dd.format(panelBuscadorMovimientosVarios.getDcDesde()) + "'";
        }

        if (panelBuscadorMovimientosVarios.getDcHasta() != null) {
            query += " AND o.fechaMovimiento <='" + UTIL.yyyy_MM_dd.format(panelBuscadorMovimientosVarios.getDcHasta()) + "'";
        }
        if (panelBuscadorMovimientosVarios.getCbUnidadDeNegocio().getSelectedIndex() > 0) {
            query += " AND o.unidadDeNegocio.id = " + ((EntityWrapper<UnidadDeNegocio>) panelBuscadorMovimientosVarios.getCbUnidadDeNegocio().getSelectedItem()).getId();
        }
        if (panelBuscadorMovimientosVarios.getCbCuenta().getSelectedIndex() > 0) {
            query += " AND o.cuenta.id = " + ((EntityWrapper<Cuenta>) panelBuscadorMovimientosVarios.getCbCuenta().getSelectedItem()).getId();
        }
        if (panelBuscadorMovimientosVarios.getCbSubCuenta().getSelectedIndex() > 0) {
            query += " AND o.subCuenta.id = " + ((EntityWrapper<SubCuenta>) panelBuscadorMovimientosVarios.getCbSubCuenta().getSelectedItem()).getId();
        }
        LogManager.getLogger();//(this.getClass()).debug(query);
        return query;
    }

    private void cargarTablaBuscadorMovimientosVarios(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        @SuppressWarnings("unchecked")
        List<Object[]> movVariosList = DAO.createQuery(query, true).getResultList();
        for (Object[] object : movVariosList) {
            UnidadDeNegocio u = (UnidadDeNegocio) object[3];
            Cuenta c = (Cuenta) object[4];
            SubCuenta s = (SubCuenta) object[5];
            EntityWrapper<UnidadDeNegocio> cu = (u != null ? new EntityWrapper<UnidadDeNegocio>(u, u.getId(), u.getNombre()) : null);
            EntityWrapper<Cuenta> cc = (c != null ? new EntityWrapper<Cuenta>(c, c.getId(), c.getNombre()) : null);
            EntityWrapper<SubCuenta> cs = (s != null ? new EntityWrapper<SubCuenta>(s, s.getId(), s.getNombre()) : null);
            object[3] = cu;
            object[4] = cc;
            object[5] = cs;
            dtm.addRow(object);
        }
    }

    private void initBuscadorCajaToCaja(JDialog owner) {
        panelBuscadorCajaToCaja = new PanelBuscadorCajaToCaja();
        UTIL.loadComboBox(panelBuscadorCajaToCaja.getCbCajaOrigen(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        UTIL.loadComboBox(panelBuscadorCajaToCaja.getCbCajaDestino(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        buscador = new JDBuscador(owner, "Buscardor - Movimientos entre Cajas", false, panelBuscadorCajaToCaja);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Descripción", "Monto", "Fecha (Hora)", "Usuario"},
                new int[]{150, 20, 60, 50});
        buscador.hideBtnToExcel();
        buscador.setListener(this);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryMovimientosCajaToCaja(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }
            }
        });
        buscador.getBtnImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryMovimientosCajaToCaja(true);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), "CajaMovimientos -> Buscador Movimientos entre Cajas", 0);
                }
            }
        });
        buscador.setLocationRelativeTo((Component) owner);
        buscador.setVisible(true);
    }

    private void doReportMovimientosVarios() throws MissingReportException, JRException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_MovimientosVarios.jasper", "Resumen: Movimientos varios");
        List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscador.getjTable1().getRowCount());
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            data.add(new GenericBeanCollection(dtm.getValueAt(row, 1), dtm.getValueAt(row, 2), dtm.getValueAt(row, 3), dtm.getValueAt(row, 4), dtm.getValueAt(row, 5),
                    dtm.getValueAt(row, 6), dtm.getValueAt(row, 7), null, null, null, null, null));
        }
        r.setDataSource(data);
        r.addConnection();
        r.addParameter("FECHA_DESDE", panelBuscadorMovimientosVarios.getDcDesde());
        r.addParameter("FECHA_HASTA", panelBuscadorMovimientosVarios.getDcHasta());
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        r.addCurrent_User();
        r.viewReport();
    }

    private void doReport(String query) throws Exception {
        Reportes r = null;
        r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_CajaToCaja.jasper", "Resumen: Movimientos entre Cajas");
        r.addParameter("QUERY", query);
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        r.addCurrent_User();
        r.viewReport();
    }

    private void cargarTablaBuscadorCajaToCaja(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<DetalleCajaMovimientos> lista = DAO.createNativeQuery(query, DetalleCajaMovimientos.class, true).getResultList();
        for (DetalleCajaMovimientos dcm : lista) {
            dtm.addRow(new Object[]{
                dcm.getDescripcion(),
                UTIL.PRECIO_CON_PUNTO.format(dcm.getMonto()),
                UTIL.DATE_FORMAT.format(dcm.getFecha()) + "(" + UTIL.TIME_FORMAT.format(dcm.getFecha()) + ")",
                dcm.getUsuario()
            });
        }
    }

    private void cleanPanelMovimientosVarios() {
        panelMovVarios.getTfMontoMovimiento().setText("");
        panelMovVarios.getTfDescripcion().setText("");
    }

    private void resetBuscadorMovimientosVarios() {
        panelBuscadorMovimientosVarios.getCbCaja().setSelectedIndex(0);
        panelBuscadorMovimientosVarios.getCbEstadoCaja().setSelectedIndex(0);
        panelBuscadorMovimientosVarios.getCbIngresoEgreso().setSelectedIndex(0);
        panelBuscadorMovimientosVarios.setDatesToNull();
        UTIL.limpiarDtm(buscador.getDtm());
    }

    private void initBuscadorCierreCaja() {
        panelBuscadorCajasCerradas = new PanelBuscadorCajasCerradas();
        UTIL.loadComboBox(panelBuscadorCajasCerradas.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        buscador = new JDBuscador(jdCierreCaja, "Buscador - Cajas cerradas", true, panelBuscadorCajasCerradas);
        buscador.getBtnImprimir().setEnabled(false);
        buscador.hideBtnToExcel();
        // <editor-fold defaultstate="collapsed" desc="InitJTable">
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Caja", "F. apertura", "F. Cierre", "Total cierre", " Usuario"},
                new int[]{70, 50, 50, 30, 60});// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Action mouseClicked">
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador != null && panelBuscadorCajasCerradas != null) {
                        setInfoCajaMovimientos(getSelectedCajaMovimientos(buscador.getjTable1()));
                        buscador.dispose();
                    }
                }
            }
        });// </editor-fold>
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryCierreCajas();
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        buscador.setListener(this);
        buscador.setLocationRelativeTo(jdCierreCaja);
        buscador.setVisible(true);
    }

    private void armarQueryCierreCajas() throws MessageException {
        if (panelBuscadorCajasCerradas.getCbCaja().getItemCount() == 1) {
            throw new MessageException("No se a creado ninguna caja aún.");
        }
        String query = "SELECT o.*"
                + " FROM caja_movimientos o, caja"
                + " WHERE o.caja = caja.id AND o.fecha_cierre IS NOT NULL";

        if (panelBuscadorCajasCerradas.getCbCaja().getSelectedIndex() > 0) {
            query += " AND o.caja =" + ((Caja) panelBuscadorCajasCerradas.getCbCaja().getSelectedItem()).getId();
        } else {
            // carga todas las Cajas que tiene permitidas..
            query += " AND (";
            for (int i = 1; i < panelBuscadorCajasCerradas.getCbCaja().getItemCount(); i++) {
                if (i > 1) {
                    query += " OR ";
                }
                query += " o.caja =" + ((Caja) panelBuscadorCajasCerradas.getCbCaja().getItemAt(i)).getId();
            }
            query += ")";
        }
        if (panelBuscadorCajasCerradas.getDcDesde() != null) {
            query += " AND o.fecha_cierre >='" + panelBuscadorCajasCerradas.getDcDesde() + "'";
        }

        if (panelBuscadorCajasCerradas.getDcHasta() != null) {
            query += " AND o.fecha_cierre <='" + panelBuscadorCajasCerradas.getDcHasta() + "'";
        }
        query += " ORDER BY o.id";
        System.out.println(query);
        cargarDtmBuscadorCierreCaja(query);
    }

    private void cargarDtmBuscadorCierreCaja(String query) {
        DefaultTableModel dtm = buscador.getDtm();
        UTIL.limpiarDtm(dtm);
        List<CajaMovimientos> cajaMovimientosList = DAO.getEntityManager().createNativeQuery(query, CajaMovimientos.class).getResultList();
        for (CajaMovimientos cajaMovimientos : cajaMovimientosList) {
            dtm.addRow(new Object[]{
                cajaMovimientos,
                UTIL.DATE_FORMAT.format(cajaMovimientos.getFechaApertura()),
                UTIL.DATE_FORMAT.format(cajaMovimientos.getFechaCierre()),
                UTIL.PRECIO_CON_PUNTO.format(cajaMovimientos.getMontoCierre()),
                cajaMovimientos.getUsuarioCierre()
            });
        }
    }

    private void imprimirCierreCaja(CajaMovimientos cajaMovimientos) throws MissingReportException, JRException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_CierreCaja.jasper", "Cierre de caja");
        r.addCurrent_User();
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        try {
            r.addParameter("USUARIO_CIERRE", cajaMovimientos.getUsuarioCierre().toString());
        } catch (NullPointerException ex) {
            r.addParameter("USUARIO_CIERRE", "");
        }
        r.addParameter("CAJA_MOVIMIENTO_ID", cajaMovimientos.getId());
        r.viewReport();
    }

    /**
     * Pone como Descripción del DetalleCajaMovimiento la FacturaVenta
     *
     * @param facturaVenta
     */
    void actualizarDescripcion(FacturaVenta facturaVenta) {
        DetalleCajaMovimientosController c = new DetalleCajaMovimientosController();
        DetalleCajaMovimientos dcm = c.findDetalleCajaMovimientosByNumero(facturaVenta.getId(), DetalleCajaMovimientosController.FACTU_VENTA);
        dcm.setDescripcion(getDescripcion(facturaVenta));
        c.merge(dcm);
    }

    private CajaMovimientos getSelectedCajaMovimientos(javax.swing.JTable table) {
        return (CajaMovimientos) table.getModel().getValueAt(table.getSelectedRow(), 0);
    }

    private void armarQueryMovimientosCajaToCaja(boolean doReport) throws Exception {
        String query
                = "SELECT b.*, u.nick"
                + " FROM (SELECT oo.*"
                + " FROM detalle_caja_movimientos oo, caja_movimientos cm, caja"
                + " WHERE oo.ingreso = true AND oo.caja_movimientos = cm.id  AND cm.caja = caja.id AND oo.tipo = " + DetalleCajaMovimientosController.MOVIMIENTO_CAJA;

        String cajasQuery = "";
        if (panelBuscadorCajaToCaja.getCbCajaDestino().getSelectedIndex() > 0) {
            cajasQuery += " AND caja.id=" + ((Caja) panelBuscadorCajaToCaja.getCbCajaDestino().getSelectedItem()).getId();
        } else {
            // carga todas las Cajas ORIGEN que tiene permitidas..
            cajasQuery += " AND (";
            for (int i = 1; i < panelBuscadorCajaToCaja.getCbCajaOrigen().getItemCount(); i++) {
                if (i > 1) {
                    cajasQuery += " OR ";
                }
                cajasQuery += " caja.id =" + ((Caja) panelBuscadorCajaToCaja.getCbCajaOrigen().getItemAt(i)).getId();
            }
            cajasQuery += ")";
        }
        query += cajasQuery;

        query += ") b,"
                + " (SELECT oo.numero"
                + " FROM detalle_caja_movimientos oo, caja_movimientos cm, caja "
                + " WHERE oo.ingreso = false AND oo.caja_movimientos = cm.id  AND cm.caja = caja.id AND oo.tipo = " + DetalleCajaMovimientosController.MOVIMIENTO_CAJA;

        cajasQuery = "";
        if (panelBuscadorCajaToCaja.getCbCajaOrigen().getSelectedIndex() > 0) {
            cajasQuery += " AND caja.id=" + ((Caja) panelBuscadorCajaToCaja.getCbCajaOrigen().getSelectedItem()).getId();
        } else {
            // carga todas las Cajas ORIGEN que tiene permitidas..
            cajasQuery += " AND (";
            for (int i = 1; i < panelBuscadorCajaToCaja.getCbCajaOrigen().getItemCount(); i++) {
                if (i > 1) {
                    cajasQuery += " OR ";
                }
                cajasQuery += " caja.id =" + ((Caja) panelBuscadorCajaToCaja.getCbCajaOrigen().getItemAt(i)).getId();
            }
            cajasQuery += ")";
        }
        query += cajasQuery;
        query += ") a, usuario u";
        query += " WHERE a.numero = b.numero AND b.usuario = u.id";

        if (panelBuscadorCajaToCaja.getDcDesde() != null) {
            query += " AND b.fecha >='" + panelBuscadorCajaToCaja.getDcDesde() + "'";
        }
        if (panelBuscadorCajaToCaja.getDcHasta() != null) {
            query += " AND b.fecha <='" + panelBuscadorCajaToCaja.getDcHasta() + "'";
        }

        query += " ORDER BY b.id";

        System.out.println(query);
        cargarTablaBuscadorCajaToCaja(query);
        if (doReport) {
            doReport(query);
        }
    }

    /**
     * Abre una {@link CajaMovimientos} que ya debería haberse abierto.. pero por alguna razón
     * fantástica, esto a veces no pasa Note: solo a Ruben le pasa esto
     *
     * @param caja la cual corresponde a la {@link CajaMovimientos} que debería estar abierta.
     */
    private void fixCajaMalAbierta(Caja caja) {
        Integer lastIDCajaMovimientoCerrada = jpaController.findLastCajaMovimientoIDCerrada(caja);
        CajaMovimientos lastCajaMovCerrada = jpaController.find(lastIDCajaMovimientoCerrada);
        LOG.warn("Corrigiendo error en creación de caja. fixCaja()"
                + "\n\túltima Caja cerrada: id=" + caja.getId() + ", nombre=" + caja.getNombre() + "-> lastID:" + lastIDCajaMovimientoCerrada);
        try {
            abrirNextCajaMovimiento(lastCajaMovCerrada);
        } catch (Exception ex) {
            LogManager.getLogger();//(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
        }
    }

    public void asignador() {
        panelBuscadorMovimientosVarios = new PanelBuscadorMovimientosVarios();
        UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbCaja(), JGestionUtils.getWrappedCajas(new UsuarioHelper().getCajas(true)), true);
        UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbUnidadDeNegocio(), JGestionUtils.getWrappedUnidadDeNegocios(new UnidadDeNegocioJpaController().findAll()), false);
        ActionListenerManager.setCuentasIngresosSubcuentaActionListener(panelBuscadorMovimientosVarios.getCbCuenta(), true, panelBuscadorMovimientosVarios.getCbSubCuenta(), true, true);
        buscador = new JDBuscador(null, "Buscardor - Movimientos varios", false, panelBuscadorMovimientosVarios);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"id", "Caja", "Descripción", "U. de Negocio", "Cuenta", "Sub Cuenta", "Mov. Fecha", "Monto", "Fecha (Sistema)", "Usuario"},
                new int[]{1, 70, 160, 60, 60, 60, 40, 20, 60, 50},
                new Class<?>[]{},
                new int[]{3, 4, 5});
        JComboBox unidades = new JComboBox();
        JComboBox cuentas = new JComboBox();
        JComboBox subCuentas = new JComboBox();
        UTIL.loadComboBox(unidades, new Wrapper<UnidadDeNegocio>().getWrapped(new UnidadDeNegocioJpaController().findAll()), false);
        UTIL.loadComboBox(cuentas, new Wrapper<Cuenta>().getWrapped(new CuentaController().findAll()), false);
        UTIL.loadComboBox(subCuentas, JGestionUtils.getWrappedSubCuentas(new SubCuentaJpaController().findAll()), false);
        buscador.getjTable1().getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(unidades));
        buscador.getjTable1().getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(cuentas));
        buscador.getjTable1().getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(subCuentas));
        buscador.getjTable1().getColumnModel().getColumn(6).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(8).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQueryMovimientosVarios();
                    cargarTablaBuscadorAsignacion(query);
                } catch (Exception ex) {
                    LogManager.getLogger();//(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                }
            }
        });
        buscador.getjTable1().getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    DetalleCajaMovimientosController jpaDcm = new DetalleCajaMovimientosController();
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    TableModel model = (TableModel) e.getSource();
                    Object data = model.getValueAt(row, column);
                    System.out.println(row + "/" + column + " =" + data);
                    if (data != null) {
                        DetalleCajaMovimientos selected = jpaDcm.find((Integer) model.getValueAt(row, 0));
                        if (column == 8) {
                            UnidadDeNegocio unidad = ((EntityWrapper<UnidadDeNegocio>) data).getEntity();
                            selected.setUnidadDeNegocio(unidad);
                        } else if (column == 9) {
                            Cuenta unidad = ((EntityWrapper<Cuenta>) data).getEntity();
                            selected.setCuenta(unidad);
                        } else if (column == 10) {
                            SubCuenta unidad = ((EntityWrapper<SubCuenta>) data).getEntity();
                            selected.setSubCuenta(unidad);
                        }
                        jpaDcm.merge(selected);
                    }
                }
            }
        });
        buscador.getBtnImprimir().setEnabled(false);
        buscador.hideBtnToExcel();
        buscador.setLocationRelativeTo(null);
        buscador.setVisible(true);
    }

    private void cargarTablaBuscadorAsignacion(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        @SuppressWarnings("unchecked")
        List<Object[]> movVariosList = DAO.createQuery(query, true).getResultList();
        for (Object[] object : movVariosList) {
            UnidadDeNegocio u = (UnidadDeNegocio) object[3];
            Cuenta c = (Cuenta) object[4];
            SubCuenta s = (SubCuenta) object[5];
            EntityWrapper<UnidadDeNegocio> cu = (u != null ? new EntityWrapper<UnidadDeNegocio>(u, u.getId(), u.getNombre()) : null);
            EntityWrapper<Cuenta> cc = (c != null ? new EntityWrapper<Cuenta>(c, c.getId(), c.getNombre()) : null);
            EntityWrapper<SubCuenta> cs = (s != null ? new EntityWrapper<SubCuenta>(s, s.getId(), s.getNombre()) : null);
            object[3] = cu;
            object[4] = cc;
            object[5] = cs;
            dtm.addRow(object);
        }
    }
}
