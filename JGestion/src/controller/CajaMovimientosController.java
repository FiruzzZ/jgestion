package controller;

import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import entity.*;
import generics.GenericBeanCollection;
import gui.*;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.ActionListenerManager;
import jgestion.JGestionUtils;
import jgestion.Wrapper;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.UnidadDeNegocioJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 * Encargada de registrar todos los asientos (ingresos/egresos) de las cajas..
 *
 * @author FiruzzZ
 */
public class CajaMovimientosController implements ActionListener {

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
     * Arma la descripción del detalleCajaMovimiento. Si se imprime factura ej:
     * F + [letra de factura] + [número de factura] Si es interno ej: I +
     * [número de movimento interno]
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
     * Crea (abre) una
     * <code>CajaMovimientos</code> implicitamente con la creación de una
     * <code>Caja</code>, así como el 1er
     * <code>DetalleCajaMovimientos</code> con monto inicial $0.
     *
     * @param caja a la cual se van a vincular la <code>CajaMovimientos</code> y
     * <code>DetalleCajaMovimientos</code>.
     */
    void nueva(Caja caja) {
        CajaMovimientos cm = new CajaMovimientos();
        cm.setCaja(caja);
        cm.setFechaApertura(new Date());
        cm.setMontoApertura(0.0);
        cm.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>(1));
        //creando el 1er movimiento de la caja (apertura en $0)
        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        dcm.setDescripcion("Apertura de caja (creación)");
        dcm.setIngreso(true);
        dcm.setMonto(0);
        dcm.setNumero(-1); //meaningless yet...
        dcm.setTipo(DetalleCajaMovimientosJpaController.APERTURA_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        dcm.setCuenta(CuentaController.EFECTIVO);
        dcm.setCajaMovimientos(cm);
        cm.getDetalleCajaMovimientosList().add(dcm);
        jpaController.create(cm);
    }

    /**
     * Crea (abre) LA SIGUIENTE
     * <code>CajaMovimientos</code> implicita POST cierre de la actual. El
     * montoApertura de la nueva
     * <code>CajaMovimientos</code> es == al montoCierre de la anterior. La
     * fechaApertura de la nueva es == a un día después de la anterior.
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
        dcm.setTipo(DetalleCajaMovimientosJpaController.APERTURA_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        if (dcm.getCuenta() == null) {
            //default value
            dcm.setCuenta(CuentaController.EFECTIVO);
        }
        nextCaja.getDetalleCajaMovimientosList().add(dcm);
        jpaController.create(nextCaja);
    }

    public void initCierreCaja(JFrame frame, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.CERRAR_CAJAS);
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>

        if (jdCierreCaja == null) {
            jdCierreCaja = new JDCierreCaja(frame, modal);
            UTIL.getDefaultTableModel(
                    jdCierreCaja.getjTable1(),
                    new String[]{"Descripción", "Monto", "Fecha (Hora)", "Usuario"},
                    new int[]{180, 20, 60, 40},
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
                        Logger.getLogger(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                    } catch (JRException ex) {
                        JOptionPane.showMessageDialog(jdCierreCaja, ex.getMessage());
                        Logger.getLogger(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
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
                            Logger.getLogger(CajaMovimientosController.class.getSimpleName()).fatal(ex.getLocalizedMessage(), ex);
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
        Double totalIngresos = 0.0 - (cajaMovimientos.getMontoApertura());
        Double totalEgresos = 0.0;
        List<DetalleCajaMovimientos> detalleCajaMovimientoList = new DetalleCajaMovimientosJpaController().getDetalleCajaMovimientosByCajaMovimiento(cajaMovimientos.getId());
        for (DetalleCajaMovimientos detalleCajaMovimientos : detalleCajaMovimientoList) {
            //sumando totales..
            if (detalleCajaMovimientos.getIngreso()) {
                totalIngresos += detalleCajaMovimientos.getMonto();
            } else {
                //los montos "egreso" son negativos
                totalEgresos += detalleCajaMovimientos.getMonto();
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
        dtm.addRow(new Object[]{"Total", UTIL.PRECIO_CON_PUNTO.format(cajaMovimientos.getMontoApertura() + totalIngresos + totalEgresos)});
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
        cajaMovimientos.setMontoCierre(getTotal());
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

    private Double getTotal() {
        return Double.valueOf(jdCierreCaja.getjTable1().getValueAt(jdCierreCaja.getjTable1().getRowCount() - 1, 1).toString());
    }

    /**
     * Busca las Cajas ACTIVAS a las cuales tiene permisos el "Usuario
     * actualmente loggeado", después busca las CajaMovimientos correspondientes
     * a cada Caja
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
                Logger.getLogger(this.getClass()).error("No debería entrar acá!! fixCaja=" + caja);
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
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);

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
        jdCajaToCaja.dispose();
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
        double monto;
        try {
            monto = Double.valueOf(jdCajaToCaja.getTfMontoMovimiento().getText().trim());
            if (monto <= 0) {
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
        dcm.setMonto(-monto); // <--- NEGATIVIZAR!
        dcm.setNumero(Integer.parseInt(jdCajaToCaja.getTfMovimiento().getText()));
        dcm.setTipo(DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        new DetalleCajaMovimientosJpaController().create(dcm);// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Destino to Origen - INGRESO">
        dcm = new DetalleCajaMovimientos();
        dcm.setCajaMovimientos(cajaDestino);
        dcm.setDescripcion(descripcion);
        dcm.setIngreso(true);
        dcm.setMonto(monto);
        dcm.setNumero(Integer.parseInt(jdCajaToCaja.getTfMovimiento().getText()));
        dcm.setTipo(DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        new DetalleCajaMovimientosJpaController().create(dcm);// </editor-fold>

        jdCajaToCaja.showMessage("Realizado", "Movimiento entre cajas", 1);
        jdCajaToCaja.resetPanel();
        jdCajaToCaja.getTfMovimiento().setText(String.valueOf(getNextMovimientoCajaToCaja()));
        DAO.getEntityManager().clear();
    }

    private int getNextMovimientoCajaToCaja() {
        return jpaController.getNextNumeroMovimientoCajaToCaja();
    }

    private void setDatosCajaToCajaCombo(String name) {
        CajaMovimientos cajaMovimientos = null;
        String balanceCajaActual = null;
        Date fechaApertura = null;
        try {
            if (name.equalsIgnoreCase("cajaOrigen")) {
                cajaMovimientos = (CajaMovimientos) jdCajaToCaja.getCbCajaOrigen().getSelectedItem();
            } else {
                cajaMovimientos = (CajaMovimientos) jdCajaToCaja.getCbCajaDestino().getSelectedItem();
            }
            //si no saltó la ClassCastException...
            cajaMovimientos = jpaController.find(cajaMovimientos.getId());
            fechaApertura = cajaMovimientos.getFechaApertura();

            Double totalIngresos = 0.0; // VA INCLUIR monto de apertura
            Double totalEgresos = 0.0;
            List<DetalleCajaMovimientos> detalleCajaMovimientoList = cajaMovimientos.getDetalleCajaMovimientosList();
            for (DetalleCajaMovimientos detalleCajaMovimientos : detalleCajaMovimientoList) {
                if (detalleCajaMovimientos.getIngreso()) {
                    totalIngresos += detalleCajaMovimientos.getMonto();
                } else {
                    //siempre son montos negativos
                    totalEgresos += detalleCajaMovimientos.getMonto();
                }
            }
            balanceCajaActual = UTIL.PRECIO_CON_PUNTO.format(totalIngresos + totalEgresos);
        } catch (ClassCastException e) {
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
     */
    public void initMovimientosVarios(Window owner, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        } catch (MessageException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
            return;
        }// </editor-fold>
        initMovimientosVarios(owner, modal, true, null);
    }

    private void initMovimientosVarios(Window owner, boolean modal, final boolean visible, final DetalleCajaMovimientos toEdit) {
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
        }
        UTIL.loadComboBox(panelMovVarios.getCbCaja(), new UsuarioHelper().getWrappedCajas(true), false);
        List<ComboBoxWrapper<UnidadDeNegocio>> l = new Wrapper<UnidadDeNegocio>().getWrapped(new UnidadDeNegocioJpaController().findAll());
        UTIL.loadComboBox(panelMovVarios.getCbUnidadDeNegocio(), l, false);
        ActionListenerManager.setCuentaSubcuentaActionListener(panelMovVarios.getCbCuenta(), false, panelMovVarios.getCbSubCuenta(), true, true);
        abm = new JDABM(owner, "Movimientos Varios", modal, panelMovVarios);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (toEdit == null) {
                        abm.getbAceptar().setEnabled(false);
                        DetalleCajaMovimientos dcm = setMovimientoVarios();
                        new DetalleCajaMovimientosJpaController().create(dcm);
                        abm.showMessage("Realizado..", "Movimientos Varios Nº" + dcm.getId(), 1);
                        cleanPanelMovimientosVarios();
                    } else {
                        DetalleCajaMovimientos dcm = setMovimientoVarios();
                        dcm.setId(toEdit.getId());
                        new DetalleCajaMovimientosJpaController().edit(dcm);
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
        abm.setVisible(visible);
    }

    /**
     * Asienta movimientos varios (tipo == 8)
     *
     * @throws MessageException
     */
    @SuppressWarnings("unchecked")
    private DetalleCajaMovimientos setMovimientoVarios() throws MessageException {
        //ctrl's................
        CajaMovimientos cajaMovimiento;
        UnidadDeNegocio unidadDeNegocio;
        SubCuenta subCuenta;
        try {
            cajaMovimiento = (CajaMovimientos) panelMovVarios.getCbCaja().getSelectedItem();
        } catch (ClassCastException ex) {
            throw new MessageException("No tiene acceso a ninguna Caja");
        }
        try {
            subCuenta = ((ComboBoxWrapper<SubCuenta>) panelMovVarios.getCbSubCuenta().getSelectedItem()).getEntity();
        } catch (ClassCastException ex) {
            subCuenta = null;
        }
        try {
            unidadDeNegocio = ((ComboBoxWrapper<UnidadDeNegocio>) panelMovVarios.getCbUnidadDeNegocio().getSelectedItem()).getEntity();
        } catch (ClassCastException ex) {
            unidadDeNegocio = null;
        }
        double monto;
        try {
            monto = Double.valueOf(panelMovVarios.getTfMontoMovimiento());
            if (monto <= 0) {
                throw new MessageException("El monto debe ser mayor a 0");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Monto no válido");
        }

        if (panelMovVarios.getTfDescripcion().length() < 1) {
            throw new MessageException("Debe ingresar una Descripción");
        }

        //setting entity.....
        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        dcm.setCajaMovimientos(cajaMovimiento);
        dcm.setIngreso(panelMovVarios.isIngreso());
        dcm.setDescripcion("MV" + (dcm.getIngreso() ? "I" : "E") + "-" + panelMovVarios.getTfDescripcion());
        dcm.setMonto(dcm.getIngreso() ? monto : -monto);
        dcm.setCuenta(((ComboBoxWrapper<Cuenta>) panelMovVarios.getCbCuenta().getSelectedItem()).getEntity());
        dcm.setTipo(DetalleCajaMovimientosJpaController.MOVIMIENTO_VARIOS);
        dcm.setUsuario(UsuarioController.getCurrentUser());
        dcm.setFechaMovimiento(panelMovVarios.getDcMovimientoFecha());
        dcm.setUnidadDeNegocio(unidadDeNegocio);
        dcm.setSubCuenta(subCuenta);
        return dcm;
    }

    private void initBuscadorMovimientosVarios(Window owner) {
        panelBuscadorMovimientosVarios = new PanelBuscadorMovimientosVarios();
        UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbCaja(), new UsuarioHelper().getWrappedCajas(true), true);
        UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbUnidadDeNegocio(), JGestionUtils.getWrappedUnidadDeNegocios(new UnidadDeNegocioJpaController().findAll()), false);
        ActionListenerManager.setCuentaSubcuentaActionListener(panelBuscadorMovimientosVarios.getCbCuenta(), true, panelBuscadorMovimientosVarios.getCbSubCuenta(), true, true);
        buscador = new JDBuscador(owner, "Buscardor - Movimientos varios", false, panelBuscadorMovimientosVarios);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Caja", "Descripción", "U. de Negocio", "Cuenta", "Sub Cuenta", "Mov. Fecha", "Monto", "Fecha (Sistema)", "Usuario"},
                new int[]{70, 160, 60, 60, 60, 40, 20, 60, 50});
        buscador.getjTable1().getColumnModel().getColumn(5).setCellRenderer(FormatRenderer.getDateRenderer());
        buscador.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getjTable1().getColumnModel().getColumn(7).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryMovimientosVarios(false);
                } catch (Exception ex) {
                    Logger.getLogger(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                }
            }
        });
        buscador.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryMovimientosVarios(true);
                } catch (Exception ex) {
                    Logger.getLogger(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
                }
            }
        });
        buscador.hideLimpiar();
        buscador.setLocationRelativeTo((Component) owner);
        buscador.setVisible(true);
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
        buscador.hideLimpiar();
        buscador.setListener(this);
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryMovimientosCajaToCaja(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }
            }
        });
        buscador.getbImprimir().addActionListener(new ActionListener() {
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

    @SuppressWarnings("unchecked")
    private void armarQueryMovimientosVarios(boolean doReport) throws Exception {
        String query = "SELECT CONCAT(o.cajaMovimientos.caja.nombre, CONCAT(\" (\", CONCAT(o.cajaMovimientos.id, \")\"))),"
                + " o.descripcion, o.unidadDeNegocio, o.cuenta, o.subCuenta, o.fechaMovimiento, o.monto, o.fecha, o.usuario.nick FROM " + DetalleCajaMovimientos.class.getSimpleName() + " o "
                + " WHERE o.tipo=" + DetalleCajaMovimientosJpaController.MOVIMIENTO_VARIOS;
        if (panelBuscadorMovimientosVarios.getCbCaja().getSelectedIndex() > 0) {
            query += " AND o.cajaMovimientos.caja.id=" + ((ComboBoxWrapper<Caja>) panelBuscadorMovimientosVarios.getCbCaja().getSelectedItem()).getId();
        } else {
            // carga todas las Cajas que tiene permitidas..
            query += " AND (";
            for (int i = 1; i < panelBuscadorMovimientosVarios.getCbCaja().getItemCount(); i++) {
                if (i > 1) {
                    query += " OR ";
                }
                query += " o.cajaMovimientos.caja.id =" + ((ComboBoxWrapper<Caja>) panelBuscadorMovimientosVarios.getCbCaja().getItemAt(i)).getId();
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
            query += " AND o.fechaMovimiento >='" + UTIL.DATE_FORMAT.format(panelBuscadorMovimientosVarios.getDcDesde()) + "'";
        }

        if (panelBuscadorMovimientosVarios.getDcHasta() != null) {
            query += " AND o.fechaMovimiento <='" + panelBuscadorMovimientosVarios.getDcHasta() + "'";
        }
        if (panelBuscadorMovimientosVarios.getCbUnidadDeNegocio().getSelectedIndex() > 0) {
            query += " AND o.unidadDeNegocio.id = " + ((ComboBoxWrapper<UnidadDeNegocio>) panelBuscadorMovimientosVarios.getCbUnidadDeNegocio().getSelectedItem()).getId();
        }
        if (panelBuscadorMovimientosVarios.getCbCuenta().getSelectedIndex() > 0) {
            query += " AND o.cuenta.id = " + ((ComboBoxWrapper<Cuenta>) panelBuscadorMovimientosVarios.getCbCuenta().getSelectedItem()).getId();
        }
        if (panelBuscadorMovimientosVarios.getCbSubCuenta().getSelectedIndex() > 0) {
            query += " AND o.subCuenta.id = " + ((ComboBoxWrapper<SubCuenta>) panelBuscadorMovimientosVarios.getCbSubCuenta().getSelectedItem()).getId();
        }
        Logger.getLogger(this.getClass()).debug(query);
        cargarTablaBuscadorMovimientosVarios(query);
        if (doReport) {
            doReportMovimientosVarios();
        }
    }

    private void cargarTablaBuscadorMovimientosVarios(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        @SuppressWarnings("unchecked")
        List<Object[]> movVariosList = DAO.createQuery(query, true).getResultList();
        for (Object[] objects : movVariosList) {
            dtm.addRow(objects);
        }
    }

    private void doReportMovimientosVarios() throws MissingReportException, JRException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_MovimientosVarios.jasper", "Resumen: Movimientos varios");
        List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscador.getjTable1().getRowCount());
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            data.add(new GenericBeanCollection(dtm.getValueAt(row, 0), dtm.getValueAt(row, 1), dtm.getValueAt(row, 2), dtm.getValueAt(row, 3), dtm.getValueAt(row, 4), dtm.getValueAt(row, 5),
                    BigDecimal.valueOf((Double) dtm.getValueAt(row, 6)), null, null, null, null, null));
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
        panelMovVarios.setTfMonto("");
        panelMovVarios.setTfDescripcion("");
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
        buscador.getbImprimir().setEnabled(false);
        buscador.hideLimpiar();
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
        buscador.getbBuscar().addActionListener(new ActionListener() {
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
        DetalleCajaMovimientosJpaController c = new DetalleCajaMovimientosJpaController();
        DetalleCajaMovimientos dcm = c.findDetalleCajaMovimientosByNumero(facturaVenta.getId(), DetalleCajaMovimientosJpaController.FACTU_VENTA);
        dcm.setDescripcion(getDescripcion(facturaVenta));
        c.edit(dcm);
    }

    private CajaMovimientos getSelectedCajaMovimientos(javax.swing.JTable table) {
        return (CajaMovimientos) table.getModel().getValueAt(table.getSelectedRow(), 0);
    }

    private void armarQueryMovimientosCajaToCaja(boolean doReport) throws Exception {
        String query =
                "SELECT b.*, u.nick"
                + " FROM (SELECT oo.*"
                + " FROM detalle_caja_movimientos oo, caja_movimientos cm, caja"
                + " WHERE oo.ingreso = true AND oo.caja_movimientos = cm.id  AND cm.caja = caja.id AND oo.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA;

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
                + " WHERE oo.ingreso = false AND oo.caja_movimientos = cm.id  AND cm.caja = caja.id AND oo.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA;

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
     * Abre una {@link CajaMovimientos} que ya debería haberse abierto.. pero
     * por alguna razón fantástica, esto a veces no pasa Note: solo a Ruben le
     * pasa esto
     *
     * @param caja la cual corresponde a la {@link CajaMovimientos} que debería
     * estar abierta.
     */
    private void fixCajaMalAbierta(Caja caja) {
        Integer lastIDCajaMovimientoCerrada = jpaController.findLastCajaMovimientoIDCerrada(caja);
        CajaMovimientos lastCajaMovCerrada = jpaController.find(lastIDCajaMovimientoCerrada);
        Logger.getLogger(this.getClass()).warn(
                "Corrigiendo error en creación de caja. fixCaja()"
                + "\n\túltima Caja cerrada: id=" + caja.getId() + ", nombre=" + caja.getNombre() + "-> lastID:" + lastIDCajaMovimientoCerrada);
        try {
            abrirNextCajaMovimiento(lastCajaMovCerrada);
        } catch (Exception ex) {
            Logger.getLogger(CajaMovimientosController.class.getName()).log(Level.ERROR, null, ex);
        }
    }
}
