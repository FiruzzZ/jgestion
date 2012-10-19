package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import entity.*;
import generics.AutoCompleteComboBox;
import generics.GenericBeanCollection;
import gui.JDABM;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import gui.PanelReasignacionDeCaja;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.*;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 * Clase (Ventana) usada para crear FacturaVenta, Remitos, Presupuestos
 *
 * @author FiruzzZ
 */
public class FacturaVentaController implements ActionListener, KeyListener {

    /**
     * contiene los nombres de las columnas de la ventana de facturación
     */
    public static final String[] COLUMN_NAMES = {"IVA", "Cód. Producto", "Producto (IVA)", "Cantidad", "Precio U.", "Precio con IVA", "Desc", "Total", "TipoDescuento", "Producto.id", "HistorialOfertas.id"};
    public static final int[] COLUMN_WIDTHS = {1, 70, 180, 10, 30, 30, 30, 30, 1, 1, 1};
    public static final Class[] COLUMN_CLASS_TYPES = {null, null, null, Integer.class, String.class, String.class, String.class, String.class, null, null, null};
    /**
     * Cantidad de item que puede contenedor el detalle. Limitado por el tamaño
     * del reporte (la factura pre-impresa)
     */
    public static final int LIMITE_DE_ITEMS = 14;
    private JDFacturaVenta jdFactura;
    private FacturaVenta EL_OBJECT;
    /**
     * Para cuando se está usando
     * {@link FacturaVentaJpaController#jdFacturaVenta} Lleva el ctrl para saber
     * cuando se seleccionó un Producto.
     */
    private Producto selectedProducto;
    private ListaPrecios selectedListaPrecios;
    private JDBuscadorReRe buscador;
    private boolean viewMode = false;
    private Remito remitoToFacturar;
    /**
     * Solo usada para reasignación de Caja, cuando se anula una Factura.
     */
    private CajaMovimientos cajaMovToAsentarAnulacion;
    private HistorialOfertas productoEnOferta;
    private static Logger LOG = Logger.getLogger(FacturaVentaController.class);
    private boolean unlockedNumeracion = false;
    private FacturaVentaJpaController jpaController;

    public FacturaVentaController() {
        jpaController = new FacturaVentaJpaController();
    }

    public FacturaVenta findFacturaVenta(Integer id) {
        FacturaVenta f = jpaController.find(id);
        return f;
    }

    /**
     * Inicializa la GUI de Ventas {@link gui.JDFacturaVenta}, está misma es
     * usada para realizar FacturasVenta, Presupestos y Remitos.
     *
     * @param owner Papi de {@link gui.JDFacturaVenta}
     * @param modal bla bla...
     * @param listener Object encargado de manejar los Eventos de la GUI
     * (Action, Mouse, Key, Focus)
     * @param factVenta1_Presup2_Remito3 Es para settear algunos Labels,
     * TextFields según la entidad que va usar la GUI.
     * @param setVisible Si la GUI debe hacerse visible cuando se invoca este
     * método. Se pone <code>false</code> cuando se va usar en MODO_VISTA, así
     * 1ro se settean los datos correspondiendtes a la entidad que la va
     * utilizar y luego se puede hacer visible.
     * @param loadDefaultData determina si se cargan los comboBox (listaprecios,
     * cajas, clientes, productos, sucursales) y si se le asignan los
     * {@link ActionListener} a los botones.
     * @throws MessageException Mensajes personalizados de alerta y/o
     * información para el usuario.
     */
    public void initFacturaVenta(JFrame owner, boolean modal, final Object listener,
            final int factVenta1_Presup2_Remito3, boolean setVisible, boolean loadDefaultData)
            throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new IllegalArgumentException(Main.resourceBundle.getString("unassigned.sucursal"));
        }
        if (factVenta1_Presup2_Remito3 == 1) {
            if (uh.getCajas(true).isEmpty()) {
                throw new IllegalArgumentException(Main.resourceBundle.getString("unassigned.caja"));
            }
        }
        jdFactura = new JDFacturaVenta(owner, modal, factVenta1_Presup2_Remito3);
        UTIL.getDefaultTableModel(jdFactura.getjTable1(),
                COLUMN_NAMES,
                COLUMN_WIDTHS,
                COLUMN_CLASS_TYPES);
        UTIL.setHorizonalAlignment(jdFactura.getjTable1(), String.class, SwingConstants.RIGHT);
        //esconde las columnas IVA y Tipo de Descuento, Producto.id, HistorialOferta.id
        UTIL.hideColumnsTable(jdFactura.getjTable1(), new int[]{0, 8, 9, 10});
        //contenedor Productos
        jdFactura.getbBuscarProducto().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    initBuscarProducto();
                } catch (DatabaseErrorException ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                }
            }
        });
        //agregar item del detalle
        jdFactura.getBtnADD().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addProductoToDetails();
                } catch (MessageException ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex, ex);
                }
            }
        });
        //quitar item del detalle
        jdFactura.getBtnDEL().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteProductoFromLista(jdFactura);
            }
        });
        //contenedor Clientes
        jdFactura.getBtnCliente().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClienteController ctrl = new ClienteController();
                ctrl.initContenedor(null, true).setVisible(true);
                UTIL.loadComboBox(jdFactura.getCbCliente(), ctrl.findEntities(), false);
            }
        });
        jdFactura.getBtnCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jdFactura.dispose();
            }
        });
        jdFactura.getTfProductoCodigo().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (jdFactura.getTfProductoCodigo().getText().trim().length() > 0 && e.getKeyCode() == 10) {
                    try {
                        buscarProducto(jdFactura.getTfProductoCodigo().getText().trim());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(jdFactura, "Error en la recuparación de información del Producto:\n"
                                + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre());
                        LOG.error("Error en la recuparación de información del Producto:\n"
                                + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre(), ex);
                    }
                }
            }
        });
        jdFactura.getTfCambio().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (jdFactura.getTfCambio().getText().length() > 0) {
                    jdFactura.setCambio();
                }
            }
        });
        jdFactura.getTfCambio().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (jdFactura.getTfCambio().getText().length() > 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    jdFactura.setCambio();
                }
            }
        });
        jdFactura.getCbSucursal().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdFactura.getCbSucursal().isFocusOwner()) {
                    Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                    if (factVenta1_Presup2_Remito3 == 1) {
                        setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
                    } else if (factVenta1_Presup2_Remito3 == 2) {
                        if (listener != null) {
                            setNumeroFactura(s, ((PresupuestoJpaController) listener).getNextNumero(s));
                        }
                    } else if (factVenta1_Presup2_Remito3 == 3) {
                        setNumeroFactura(s, ((RemitoController) listener).getNextNumero(s));
                    }
                }
            }
        });

        //Cuando es FALSE, se va usar en MODO VISTA (Factura, Remito o Presupuesto, NotaCredito)
        //por lo tanto no es necesario cargar todos los combos
        //tampoco se asignan listeners a los botones
        if (loadDefaultData) {
            jdFactura.setTfRemito("Sin Remito");
            jdFactura.setDcFechaFactura(new Date());
            UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(), false);
            // <editor-fold defaultstate="collapsed" desc="AutoCompleteComboBox">
            // must be editable!!!.................
            jdFactura.getCbProductos().setEditable(true);
            JTextComponent editor = (JTextComponent) jdFactura.getCbProductos().getEditor().getEditorComponent();
            editor.setDocument(new AutoCompleteComboBox(jdFactura.getCbProductos()));
            editor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        @SuppressWarnings("unchecked")
                        ComboBoxWrapper<Producto> cbw = (ComboBoxWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                        selectedProducto = new ProductoController().findProductoByCodigo(cbw.getEntity().getCodigo());
                        setInformacionDeProducto(jdFactura, selectedProducto);
                    } catch (ClassCastException ex) {
                        //cuando no seleccionó ningún Producto del combo
                        jdFactura.setTfPrecioUnitario("");
                        jdFactura.setTfProductoIVA("");
                    }
                }
            });
            editor.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            @SuppressWarnings("unchecked")
                            ComboBoxWrapper<Producto> cbw = (ComboBoxWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                            selectedProducto = new ProductoController().findProductoByCodigo(cbw.getEntity().getCodigo());
                            setInformacionDeProducto(jdFactura, selectedProducto);
                        } catch (ClassCastException ex) {
                            //imposible que pase esto.. cierto?
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(jdFactura, "Error en la recuparación de información del Producto:\n"
                                    + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre());
                            LOG.error("Error en la recuparación de información del Producto:\n"
                                    + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre(), ex);
                        }
                    }
                }
            });// </editor-fold>
            UTIL.loadComboBox(jdFactura.getCbCliente(), new ClienteController().findEntities(), false);
            UTIL.loadComboBox(jdFactura.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), false);
            UTIL.loadComboBox(jdFactura.getCbListaPrecio(), new ListaPreciosJpaController().findListaPreciosEntities(), false);
            UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
            jdFactura.getCbListaPrecio().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!selectedListaPrecios.equals((ListaPrecios) jdFactura.getCbListaPrecio().getSelectedItem())) {
                        if (jdFactura.getjTable1().getModel().getRowCount() > 0) {
                            //consulta al usuario antes de limpiar el detalle de factura
                            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFactura,
                                    "Si cambia la lista de precios, se borrarán todos los items ingresados."
                                    + "\nEsto es necesario para volver a calcular los precios en base al porcentaje de ganancia.",
                                    "Advertencia",
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                borrarDetalles();
                                selectedListaPrecios = (ListaPrecios) jdFactura.getCbListaPrecio().getSelectedItem();
                                setIconoListaPrecios(selectedListaPrecios);
                            }
                        } else {
                            selectedListaPrecios = (ListaPrecios) jdFactura.getCbListaPrecio().getSelectedItem();
                            setIconoListaPrecios(selectedListaPrecios);
                        }
                    }
                }
            });
            if (factVenta1_Presup2_Remito3 == 1) {
                try {
                    cargarComboTiposFacturas((Cliente) jdFactura.getCbCliente().getSelectedItem());
                } catch (ClassCastException ex) {
                    throw new MessageException("Debe crear un Cliente para poder realizar una Facturas venta, Recibos, Presupuestos o Remitos.");
                }
                //carga los tipos de facturas que puede se le pueden dar al Cliente
                jdFactura.getCbCliente().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            cargarComboTiposFacturas((Cliente) jdFactura.getCbCliente().getSelectedItem());
                            remitoToFacturar = null;
                            jdFactura.setTfRemito("Sin Remito");
                        } catch (ClassCastException ex) {
                            jdFactura.setTfRemito("Cliente no válido");
                        }
                    }
                });
                //seeking and binding of a Remito to a FacturaVenta
                jdFactura.getBtnBuscarRemito().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Cliente clienteSeleccionado;
                        try {
                            clienteSeleccionado = (Cliente) jdFactura.getCbCliente().getSelectedItem();
                            if (clienteSeleccionado == null) {
                                JOptionPane.showMessageDialog(jdFactura, "Debe seleccionar un Cliente, del cual se buscará el Remito.", "Error", JOptionPane.WARNING_MESSAGE, null);
                            }
                            initBuscadorRemito(clienteSeleccionado);
                        } catch (ClassCastException ex) {
                            JOptionPane.showMessageDialog(jdFactura, "Debe seleccionar un Cliente, del cual se buscará el Remito.", "Error (No existe cliente)", JOptionPane.WARNING_MESSAGE, null);
                        }
                    }
                });
                jdFactura.getCbFacturaTipo().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (jdFactura.getCbFacturaTipo().getSelectedItem() != null) {
                            Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                            setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
                        }
                    }
                });
                jdFactura.getBtnAceptar().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            jdFactura.getBtnFacturar().setEnabled(false);
                            jdFactura.getBtnAceptar().setEnabled(false);
                            setAndPersist(false);
                        } catch (MessageException ex) {
                            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        } catch (Exception ex) {
                            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                            LOG.error(ex, ex);
                        } finally {
                            jdFactura.getBtnFacturar().setEnabled(true);
                            jdFactura.getBtnAceptar().setEnabled(true);
                        }
                    }
                });
                jdFactura.getBtnFacturar().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            jdFactura.getBtnFacturar().setEnabled(false);
                            jdFactura.getBtnAceptar().setEnabled(false);
                            setAndPersist(true);
                        } catch (MessageException ex) {
                            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        } catch (Exception ex) {
                            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                            LOG.error(ex, ex);
                        } finally {
                            jdFactura.getBtnFacturar().setEnabled(true);
                            jdFactura.getBtnAceptar().setEnabled(true);
                        }
                    }
                });
                //hasta que no elija el TIPO A,B,C no se sabe el nº
                jdFactura.setTfFacturaCuarto("");
                jdFactura.setTfFacturaOcteto("");
                UTIL.loadComboBox(jdFactura.getCbCaja(), new UsuarioHelper().getCajas(true), false);
                jdFactura.setTfNumMovimiento(jpaController.getNextMovimientoInterno().toString());
                Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
            } else if (factVenta1_Presup2_Remito3 == 2) {
                // y aca?...
            } else if (factVenta1_Presup2_Remito3 == 3) {
                jdFactura.getLabelNumMovimiento().setVisible(false);
                jdFactura.getTfNumMovimiento().setVisible(false);
                Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                setNumeroFactura(s, ((RemitoController) listener).getNextNumero(s));
            }
        }
        if (factVenta1_Presup2_Remito3 == 1) {
            jdFactura.getBtnAnular().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (EL_OBJECT.getAnulada()) {
                            throw new MessageException("Ya está ANULADA!");
                        }
                        String factu_compro = EL_OBJECT.getMovimientoInterno() == 0 ? "Factura Venta" : "Comprobante";
                        String msg_extra_para_ctacte = EL_OBJECT.getFormaPago() == Valores.FormaPago.CTA_CTE.getId() ? "\n- Recibos de pago de Cta.Cte." : "";
                        int showOptionDialog = JOptionPane.showOptionDialog(jdFactura, "- " + factu_compro + " Nº:" + UTIL.AGREGAR_CEROS(EL_OBJECT.getNumero() + "\n- Movimiento de Caja\n- Movimiento de Stock" + msg_extra_para_ctacte, 12), "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null);
                        if (showOptionDialog == JOptionPane.OK_OPTION) {
                            anular(EL_OBJECT);
                            msg_extra_para_ctacte = (msg_extra_para_ctacte.length() > 1 ? "\nNota: Si el Recibo contenía como único detalle de pago esta Factura, este será anulado completamente\ny no solamente la referencia a esta Factura" : "");
                            jdFactura.showMessage("Anulada" + msg_extra_para_ctacte, "Factura Venta", 1);
                            jdFactura.dispose();
                        }
                    } catch (MessageException ex) {
                        jdFactura.showMessage(ex.getMessage(), "ERROR", 0);
                    } catch (Exception ex) {
                    }
                }
            });
        }
        try {
            selectedListaPrecios = (ListaPrecios) jdFactura.getCbListaPrecio().getSelectedItem();
            setIconoListaPrecios(selectedListaPrecios);
        } catch (ClassCastException ex) {
            throw new MessageException("Debe crear al menos una Lista de Precios para poder realizar VENTAS."
                    + "\nMenú -> Productos -> Lista de Precios.");
        }
        jdFactura.setLocation(jdFactura.getOwner().getX() + 100, jdFactura.getY() + 50);
        jdFactura.setVisible(setVisible);
    }

    private void buscarProducto(String codigoProducto) {
        selectedProducto = new ProductoController().findProductoByCodigo(codigoProducto);
        setInformacionDeProducto(jdFactura, selectedProducto);
    }

    private void addProductoToDetails() throws MessageException {
        if (selectedProducto == null) {
            throw new MessageException("Seleccione un producto");
        }
        if (jdFactura.getDtm().getRowCount() >= LIMITE_DE_ITEMS) {
            throw new MessageException("La factura no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
        }
        if (selectedListaPrecios == null) {
            throw new MessageException("Debe elegir una lista de precios antes."
                    + "\nSi no existe ninguna debe crearla en el Menú -> Productos -> Lista de precios.");
        }
        for (int row = 0; row < jdFactura.getDtm().getRowCount(); row++) {
            Integer productoID = (Integer) jdFactura.getDtm().getValueAt(row, 9);
            if (selectedProducto.getId() == productoID) {
                throw new MessageException("Este ha agregado este producto al detalle.");
            }
        }
        int cantidad;
        BigDecimal precioUnitarioSinIVA; // (precioUnitario + margen listaPrecios)
        BigDecimal descuentoUnitario;

        // <editor-fold defaultstate="collapsed" desc="ctrl tfCantidad">
        try {
            cantidad = Integer.valueOf(jdFactura.getTfCantidad());
            if (cantidad < 1) {
                throw new MessageException("La cantidad no puede ser menor a 1");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Cantidad no válida (solo números enteros)");
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="ctrl tfPrecioUnitario">
        try {
            precioUnitarioSinIVA = new BigDecimal(jdFactura.getTfPrecioUnitario());
            if (precioUnitarioSinIVA.intValue() < 0) {
                throw new MessageException("El precio unitario no puede ser menor a 0");
            }
            if (!isPrecioVentaMinimoValido(precioUnitarioSinIVA.doubleValue())) {
                throw new MessageException("El precio unitario de venta ($" + Contabilidad.PU_FORMAT.format(precioUnitarioSinIVA) + ") no puede"
                        + " ser menor al mínimo de Venta establecido ($" + Contabilidad.PU_FORMAT.format(selectedProducto.getMinimoPrecioDeVenta()) + ")"
                        + "\nPara poder vender el producto al precio deseado, debe cambiar el \"precio de venta\" del producto."
                        + "\nUtilice el botón a la izquierda del campo CÓDIGO o"
                        + "\n(Menú -> Productos -> ABM Productos -> Modificar)");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Precio Unitario no válido");
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="ctrl tfDescuento">
        try {
            if (jdFactura.getTfProductoDesc().length() == 0) {
                descuentoUnitario = BigDecimal.ZERO;
            } else {
                descuentoUnitario = new BigDecimal(jdFactura.getTfProductoDesc());
                if (descuentoUnitario.intValue() < 0) {
                    throw new MessageException("Descuento no puede ser menor a 0");
                }

                //cuando el descuento es por porcentaje (%)
                if ((jdFactura.getCbDesc().getSelectedIndex() == 0) && (descuentoUnitario.intValue() > 100)) {
                    throw new MessageException("El descuento no puede ser superior al 100%");
                } else if ((jdFactura.getCbDesc().getSelectedIndex() == 1) && (descuentoUnitario.compareTo(precioUnitarioSinIVA) == 1)) {
                    // cuando es por un monto fijo ($)
                    throw new MessageException("El descuento (" + UTIL.PRECIO_CON_PUNTO.format(descuentoUnitario) + ") "
                            + "no puede ser superior al precio venta (" + UTIL.PRECIO_CON_PUNTO.format(precioUnitarioSinIVA) + ")");
                }
            }
            // el descuento se aplica SOBRE LA GANANCIA es decir:
            // (precioUnitario - precioMinimoVenta)
            // un descuento del 100% == precioMinimoVenta (cero ganacia y pérdida)
            descuentoUnitario = Contabilidad.GET_MARGEN(
                    (precioUnitarioSinIVA.subtract(BigDecimal.valueOf(selectedProducto.getMinimoPrecioDeVenta()))),
                    jdFactura.getCbDesc().getSelectedIndex() + 1,
                    descuentoUnitario);

            if (!isPrecioVentaMinimoValido(precioUnitarioSinIVA.subtract(descuentoUnitario).doubleValue())) {
                throw new MessageException("El descuento deseado produce un precio de venta ($" + UTIL.PRECIO_CON_PUNTO.format(precioUnitarioSinIVA.subtract(descuentoUnitario)) + ")"
                        + "\nmenor al mínimo de Venta establecido ($" + selectedProducto.getMinimoPrecioDeVenta() + ")"
                        + "\nPara poder realizar este descuento, debe cambiar este mínimo de venta ajustando el \"precio de venta\" del producto."
                        + "\n(Menú -> Productos -> ABM Productos -> Modificar)");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Descuento no válido");
        }// </editor-fold>
        //adiciona el descuento
        descuentoUnitario = new BigDecimal(UTIL.PRECIO_CON_PUNTO.format(descuentoUnitario));
        precioUnitarioSinIVA = precioUnitarioSinIVA.subtract(descuentoUnitario);

        if (productoEnOferta == null) {
        } else {
            //el precio en oferta ya incluye IVA
            //así que hay que hacerle la inversa 
            if ((selectedProducto.getIva().getIva() > 0)) {
                double d = ((selectedProducto.getIva().getIva() / 100) + 1);
                precioUnitarioSinIVA = precioUnitarioSinIVA.divide(BigDecimal.valueOf(d));
            }
        }
        //quitando decimes indeseados..
        precioUnitarioSinIVA = precioUnitarioSinIVA.setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal alicuota = BigDecimal.valueOf(selectedProducto.getIva().getIva());
        //carga detallesVenta en tabla
        jdFactura.getDtm().addRow(new Object[]{
                    selectedProducto.getIva().getIva(),
                    selectedProducto.getCodigo(),
                    selectedProducto.getNombre() + "(" + selectedProducto.getIva().getIva() + ")",
                    cantidad,
                    precioUnitarioSinIVA, //columnIndex == 4
                    precioUnitarioSinIVA.multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE))).setScale(4, RoundingMode.HALF_EVEN),
                    descuentoUnitario.multiply(BigDecimal.valueOf(cantidad)),
                    precioUnitarioSinIVA.multiply(BigDecimal.valueOf(cantidad)).multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE))).setScale(2, RoundingMode.HALF_EVEN), //subTotal
                    (descuentoUnitario.intValue() == 0) ? -1 : (jdFactura.getCbDesc().getSelectedIndex() + 1),//Tipo de descuento
                    selectedProducto.getId(),
                    productoEnOferta
                });
        refreshResumen(jdFactura);
    }

    void deleteProductoFromLista(JDFacturaVenta contenedor) {
        if (contenedor.getjTable1().getSelectedRow() >= 0) {
            contenedor.getDtm().removeRow(contenedor.getjTable1().getSelectedRow());
            refreshResumen(contenedor);
        }
    }

    /**
     * Setea la info del Producto en la instancia de {@link gui.JDFacturaVenta}
     *
     * @param contenedor instancia de <code>gui.JDFacturaVenta</code>
     * @param selectedProducto entity Producto
     */
    private void setInformacionDeProducto(JDFacturaVenta contenedor, Producto selectedProducto) {
        if (selectedProducto != null) {
            contenedor.setLabelCodigoNoRegistradoVisible(false);
            contenedor.setTfProductoCodigo(selectedProducto.getCodigo());
            UTIL.setSelectedItem(contenedor.getCbProductos(), selectedProducto.getNombre());
            contenedor.getTfMarca().setText(selectedProducto.getMarca().getNombre());
            contenedor.setTfProductoIVA(selectedProducto.getIva().getIva().toString());
            Double precioUnitario = selectedProducto.getMinimoPrecioDeVenta();
            //buscamos si el producto está en oferta
            productoEnOferta = new HistorialOfertasJpaController().findOfertaVigente(selectedProducto);
            LOG.debug("productoEnOferta.id=" + (productoEnOferta != null ? productoEnOferta.getId() : null));
            ListaPrecios listaPreciosParaCatalogo = new ListaPreciosJpaController().findListaPreciosParaCatalogo();
            //Cuando el producto NO está en oferta o cuando NO hay lista designada para CatalogoWeb
            if (productoEnOferta == null || listaPreciosParaCatalogo == null) {
                //agrega el margen de ganancia según la ListaPrecio
                precioUnitario += Contabilidad.GET_MARGEN_SEGUN_LISTAPRECIOS(selectedListaPrecios, selectedProducto, null);
                contenedor.setTfPrecioUnitario(UTIL.PRECIO_CON_PUNTO.format(precioUnitario));
            } else {
                LOG.trace("¡¡¡Producto ES OFERTA!!!");
                if (listaPreciosParaCatalogo.equals(selectedListaPrecios)) {
                    LOG.trace("¡¡¡LISTA PRECIOS [CW]!!!");
                    contenedor.setTfPrecioUnitario(UTIL.PRECIO_CON_PUNTO.format(productoEnOferta.getPrecio()));
                }
            }
            //Si el Producto está en OFERTA, deshabilitamos todos los campos para
            //que no se pueda modificar el precio de la oferta.
            contenedor.enableModificacionPrecios(productoEnOferta == null);
            contenedor.setLabelOfertaVisible(productoEnOferta != null);
            contenedor.setFocusCantidad();
        } else {
            contenedor.setLabelCodigoNoRegistradoVisible(true);
            contenedor.setTfProductoIVA("");
            contenedor.setTfPrecioUnitario("");
        }
    }

    /**
     * Actualiza los campos de montos de
     * <code>gui.JD.FacturaVenta</code> según los items en el detalle. Gravado
     * (SubTotal), Iva10, Iva21, Descuentos, Total y Cambio
     *
     * @param contenedor
     */
    void refreshResumen(JDFacturaVenta contenedor) {
        BigDecimal redondeoTotal = BigDecimal.ZERO;
        BigDecimal gravado = BigDecimal.ZERO;
        BigDecimal noGravado = BigDecimal.ZERO;
        BigDecimal iva10 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal otrosImps = BigDecimal.ZERO;
        BigDecimal desc = BigDecimal.ZERO;
        BigDecimal subTotal = BigDecimal.ZERO;
        DefaultTableModel dtm = contenedor.getDtm();
        for (int index = 0; index < dtm.getRowCount(); index++) {
            BigDecimal cantidad = new BigDecimal(dtm.getValueAt(index, 3).toString());
            BigDecimal precioUnitario = new BigDecimal(dtm.getValueAt(index, 4).toString());
            BigDecimal alicuota = new BigDecimal(dtm.getValueAt(index, 0).toString());

            // Gravado (precioSinIVA + cantidad)
            if (alicuota.intValue() > 0) {
                gravado = gravado.add(precioUnitario.multiply(cantidad));
                BigDecimal precioUnitarioConIva = new BigDecimal(dtm.getValueAt(index, 5).toString());
                redondeoTotal = redondeoTotal.add(precioUnitarioConIva.multiply(cantidad));
            }
            /**
             * Se calcula sin aplicar ningún redondeo (se trabaja posiblemente
             * mas de 2 decimales).
             */
            BigDecimal sinRedondeo = precioUnitario.multiply(cantidad).multiply(alicuota.divide(new BigDecimal("100")));
            if (alicuota.intValue() == 0) {
                noGravado = noGravado.add(cantidad.multiply(precioUnitario));
//                gravado = gravado.subtract(precioUnitario.multiply(cantidad));
            } else if (alicuota.toString().equalsIgnoreCase("10.5")) {
                iva10 = iva10.add(cantidad.multiply(precioUnitario).multiply((alicuota.divide(new BigDecimal("100")))));
            } else if (alicuota.toString().equalsIgnoreCase("21.0")) {
                iva21 = iva21.add(cantidad.multiply(precioUnitario).multiply((alicuota.divide(new BigDecimal("100")))));
            } else if (alicuota.compareTo(BigDecimal.ZERO) > 0) {
                otrosImps = otrosImps.add(cantidad.multiply(alicuota.divide(new BigDecimal("100"))));
            } else {
                throw new IllegalArgumentException("IVA no determinado");
            }
            //Descuento++
            //si el subtotal es > 0... para no dar resultados negativos!!!
            if (new BigDecimal(dtm.getValueAt(index, 7).toString()).intValue() >= 0) {
                desc = desc.add(new BigDecimal(dtm.getValueAt(index, 6).toString()));
            }
            LOG.debug("alicuota=" + alicuota + ", redondeo=" + sinRedondeo);

            subTotal = subTotal.add(new BigDecimal(dtm.getValueAt(index, 7).toString()));
        }
        contenedor.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado));
        contenedor.setTfTotalNoGravado(UTIL.PRECIO_CON_PUNTO.format(noGravado));
        contenedor.setTfTotalDesc(UTIL.PRECIO_CON_PUNTO.format(desc));
        contenedor.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(iva10));
        contenedor.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(iva21));
        contenedor.setTfTotalOtrosImps(UTIL.PRECIO_CON_PUNTO.format(otrosImps));
        redondeoTotal = gravado.add(iva10).add(iva21).add(otrosImps).subtract(redondeoTotal);
        contenedor.getTfDiferenciaRedondeo().setText(UTIL.PRECIO_CON_PUNTO.format(redondeoTotal));
        contenedor.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(subTotal));
        contenedor.setTfCambio(UTIL.PRECIO_CON_PUNTO.format(subTotal));
        LOG.debug("Gravado:" + gravado + ", Desc.:" + desc + ", IVA105:" + iva10 + ", IVA21:" + iva21 + ", OtrosImp.:" + otrosImps + ", Redondeo:" + redondeoTotal);
    }

    /**
     * Calcula el margen monetario ganancia/perdida sobre el monto.
     *
     * @param monto cantidad monetaria sobre la cual se hará el cálculo
     * @param tipoDeMargen Indica como se aplicará el margen al monto. If
     * <code>(tipo > 2 || 1 > tipo)</code> will return <code>null</code>. <lu>
     * <li>1 = % (porcentaje) <li>2 = $ (monto fijo) <lu>
     * @param margen monto fijo o porcentual.
     * @return El margen de ganancia correspondiente al monto.
     */
    public static Double GET_MARGEN(double monto, int tipoDeMargen, double margen) {
        if (margen == 0) {
            return 0.0;
        }
        Double total;
        switch (tipoDeMargen) {
            case 1: { // margen en %
                total = ((monto * margen) / 100);
                break;
            }
            case 2: { // margen en $ (monto fijo).. no hay mucha science...
                total = margen;
                break;
            }
            default:
                total = null;
        }
        return total;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
            if (tf.getName().equalsIgnoreCase("cuarteto")) {
                if (tf.getText().length() < 4) {
                    UTIL.soloNumeros(e);
                } else {
                    e.setKeyChar((char) KeyEvent.VK_CLEAR);
                }
            } else if (tf.getName().equalsIgnoreCase("octeto")) {
                if (tf.getText().length() < 8) {
                    UTIL.soloNumeros(e);
                } else {
                    e.setKeyChar((char) KeyEvent.VK_CLEAR);
                }
            } else if (tf.getName().equalsIgnoreCase("dias")) {
                if (tf.getText().length() >= 3) {
                    e.setKeyChar((char) KeyEvent.VK_CLEAR);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        if (e.getSource().getClass().equals(JButton.class)) {
//            JButton boton = (JButton) e.getSource();
//        }
    }

    private void checkConstraints() throws MessageException {
        if (jdFactura.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de factura no válida");
        }

        try {
            Sucursal s = getSelectedSucursalFromJDFacturaVenta();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Sucursal para poder realizar la Factura Venta."
                    + "\nMenú: Datos Generales -> Sucursales");
        }
        try {
            ListaPrecios c = (ListaPrecios) jdFactura.getCbListaPrecio().getSelectedItem();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Lista de Precios para poder realizar la Factura Venta."
                    + "\nMenú: Productos -> Lista de Precios");
        }
        try {
            Caja c = (Caja) jdFactura.getCbCaja().getSelectedItem();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Caja para poder registrar la Factura Venta."
                    + "\nMenú: Tesorería -> ABM Cajas");
        }
        if (((Valores.FormaPago) jdFactura.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
            try {
                if (Short.valueOf(jdFactura.getTfDias()) < 1) {
                    throw new MessageException("Cantidad de días de Cta. Cte. no válida. Debe ser mayor a 0");
                }
            } catch (NumberFormatException e) {
                throw new MessageException("Cantidad de días de Cta. Cte. no válida");
            }
        }

        DefaultTableModel dtm = jdFactura.getDtm();
        if (dtm.getRowCount() < 1) {
            throw new MessageException("La factura debe tener al menos un item.");
        } else if (dtm.getRowCount() > LIMITE_DE_ITEMS) {
            throw new MessageException("La factura no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
        }

        if (!jdFactura.isEditMode() && remitoToFacturar != null && remitoToFacturar.getFacturaVenta() != null) {
            throw new MessageException("De alguna forma misteriosa ha seleccionado un Remito que ya fue asociado a una Factura."
                    + "\nFactura " + JGestionUtils.getNumeracion(remitoToFacturar.getFacturaVenta()));
        }

        //checkeando vigencia de la oferta de los productos a facturar COMO "oferta"
        HistorialOfertas ofertaToCheck;
        HistorialOfertasJpaController historialOfertasController = new HistorialOfertasJpaController();
        for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
            ofertaToCheck = (HistorialOfertas) dtm.getValueAt(rowIndex, 10);
            if (ofertaToCheck != null && !historialOfertasController.isOfertaVigente(ofertaToCheck.getId())) {
                throw new MessageException("¡La oferta del Producto:"
                        + "\n" + ofertaToCheck.getProducto().getCodigo()
                        + "\n" + ofertaToCheck.getProducto().getNombre()
                        + "\nNO SE ENCUENTRA MAS VIGENTE!.");
            }
        }
        if (unlockedNumeracion) {
            try {
                Integer octeto = Integer.valueOf(jdFactura.getTfFacturaOcteto());
                if (octeto < 1 && octeto > 99999999) {
                    throw new MessageException("Número de factura no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                char letra = jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0);
                FacturaVenta oldFactura = findBySucursal(letra, getSelectedSucursalFromJDFacturaVenta(), octeto);
                if (oldFactura != null) {
                    throw new MessageException("Ya existe un registro de Factura Venta N° " + JGestionUtils.getNumeracion(oldFactura));
                }
            } catch (NumberFormatException numberFormatException) {
                throw new MessageException("Número de factura no válido, ingrese solo dígitos");
            }
        }
    }

    /**
     * Set a instance of {@link FacturaVenta} from
     * {@link FacturaVentaJpaController#jdFacturaVenta} UI.
     *
     * @param facturar si es factura o comprobante interno
     * @return a instance of {@link FacturaVenta} ready to persist
     */
    private FacturaVenta getEntity(boolean facturar) {
        DefaultTableModel dtm = UTIL.getDtm(jdFactura.getjTable1());
        //set entity.fields
        FacturaVenta newFacturaVenta = new FacturaVenta();
        newFacturaVenta.setAnulada(false);
        newFacturaVenta.setFechaVenta(jdFactura.getDcFechaFactura());

        //setting entities
        newFacturaVenta.setCliente((Cliente) jdFactura.getCbCliente().getSelectedItem());
        newFacturaVenta.setSucursal(getSelectedSucursalFromJDFacturaVenta());
        newFacturaVenta.setUsuario(UsuarioController.getCurrentUser());
        newFacturaVenta.setCaja((Caja) jdFactura.getCbCaja().getSelectedItem());
        newFacturaVenta.setListaPrecios(selectedListaPrecios);
        newFacturaVenta.setRemito(remitoToFacturar);

        if (facturar) {
            newFacturaVenta.setMovimientoInterno(0);
            newFacturaVenta.setTipo(jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
            if (unlockedNumeracion) {
                newFacturaVenta.setNumero(Integer.valueOf(jdFactura.getTfFacturaOcteto()));
            } else {
                newFacturaVenta.setNumero(jpaController.getNextNumeroFactura(newFacturaVenta.getSucursal(), newFacturaVenta.getTipo()));
            }
        } else {
            newFacturaVenta.setNumero(0);
            newFacturaVenta.setMovimientoInterno(jpaController.getNextMovimientoInterno());
            newFacturaVenta.setTipo('I');
        }

        //setting fields
        newFacturaVenta.setFormaPago(((Valores.FormaPago) jdFactura.getCbFormaPago().getSelectedItem()).getId());
        newFacturaVenta.setImporte(Double.valueOf(jdFactura.getTfTotal()));
        newFacturaVenta.setGravado(Double.valueOf(jdFactura.getTfGravado()));
        newFacturaVenta.setNoGravado(new BigDecimal(jdFactura.getTfTotalNoGravado()));
        newFacturaVenta.setIva10(Double.valueOf(jdFactura.getTfTotalIVA105()));
        newFacturaVenta.setIva21(Double.valueOf(jdFactura.getTfTotalIVA21()));
        newFacturaVenta.setDescuento(Double.valueOf(jdFactura.getTfTotalDesc()));
        newFacturaVenta.setDiferenciaRedondeo(new BigDecimal(jdFactura.getTfDiferenciaRedondeo().getText()));
        if (newFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
            newFacturaVenta.setDiasCtaCte(Short.parseShort(jdFactura.getTfDias()));
        } else {
            newFacturaVenta.setDiasCtaCte((short) 0);
        }
        if (!jdFactura.isEditMode()) {
            newFacturaVenta.setDetallesVentaList(new ArrayList<DetalleVenta>());
            /// carga de detalleVenta
            DetalleVenta detalleVenta;
            ProductoJpaController productoController = new ProductoJpaController();
            for (int i = 0; i < dtm.getRowCount(); i++) {
                detalleVenta = new DetalleVenta();
                detalleVenta.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
                detalleVenta.setPrecioUnitario(((BigDecimal) dtm.getValueAt(i, 4)).doubleValue());
                detalleVenta.setDescuento(Double.valueOf(dtm.getValueAt(i, 6).toString()));
                detalleVenta.setTipoDesc(Integer.valueOf(dtm.getValueAt(i, 8).toString()));
                detalleVenta.setProducto(productoController.find((Integer) dtm.getValueAt(i, 9)));
                detalleVenta.setFactura(newFacturaVenta);
                if (dtm.getValueAt(i, 10) != null) {
                    detalleVenta.setOferta((HistorialOfertas) dtm.getValueAt(i, 10));
                }
                newFacturaVenta.getDetallesVentaList().add(detalleVenta);
            }
        }
        return newFacturaVenta;
    }

    private void registrarVentaSegunFormaDePago(FacturaVenta facturaVenta) throws Exception {
        switch (facturaVenta.getFormaPago()) {
            case 1: { // CONTADO
                new CajaMovimientosJpaController().asentarMovimiento(facturaVenta);
                break;
            }
            case 2: { // CTA CTE CLIENTE (NO HAY NINGÚN MOVIMIENTO DE CAJA)
                new CtacteClienteJpaController().nuevaCtaCte(facturaVenta);
                break;
            }
            case 3: { // CHEQUE Terceros (tampoco HAY NINGÚN MOVIMIENTO DE CAJA)
                break;
            }
            case 4: { // CONTADO-CHEQUE Terceros (tampoco HAY NINGÚN MOVIMIENTO DE CAJA)
                new CajaMovimientosJpaController().asentarMovimiento(facturaVenta);
                break;
            }

            default: {
                throw new IllegalArgumentException("FormaPago de la FacturaVenta.id="
                        + facturaVenta.getId() + " no existe");
            }
        }
    }

    /**
     * Agrega a
     * <code>numero</code> tantos 0 (ceros) a la izq hasta completar los 12
     * dígitos (#### ########) y setea en la GUI {@link JDFacturaVenta}
     *
     * @param numero
     */
    void setNumeroFactura(Sucursal s, Integer numero) {
        jdFactura.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4));
        jdFactura.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(numero, 8));
    }

    // <editor-fold defaultstate="collapsed" desc="Tipos de Factura A B C M X">
    private void cargarComboTiposFacturas(Cliente cliente) {
        if (cliente != null) {
            jdFactura.getCbFacturaTipo().removeAllItems();
            if (cliente.getContribuyente().getFactuA()) {
                jdFactura.getCbFacturaTipo().addItem("A");
            }
            if (cliente.getContribuyente().getFactuB()) {
                jdFactura.getCbFacturaTipo().addItem("B");
            }
            if (cliente.getContribuyente().getFactuC()) {
                jdFactura.getCbFacturaTipo().addItem("C");
            }
            if (cliente.getContribuyente().getFactuM()) {
                jdFactura.getCbFacturaTipo().addItem("M");
            }
            if (cliente.getContribuyente().getFactuX()) {
                jdFactura.getCbFacturaTipo().addItem("X");
            }
        }
    }// </editor-fold>

    private void initBuscarProducto() throws DatabaseErrorException {
        ProductoController p = new ProductoController();
        p.initContenedor(null, true, false);
        UTIL.loadComboBox(jdFactura.getCbProductos(), p.findWrappedProductoToCombo(), false);
    }

    public void initBuscador(JFrame frame, final boolean modal, final boolean paraAnular) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(frame, "Buscador - Facturas venta", modal, "Cliente", "Nº Factura");
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"facturaID", "Nº factura", "Mov.", "Cliente", "Importe", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 90, 10, 50, 40, 50, 50, 80, 50, 70},
                new Class<?>[]{Integer.class, null, Integer.class, null, null, String.class, null, null, null, null});
        buscador.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        UTIL.setHorizonalAlignment(buscador.getjTable1(), String.class, SwingConstants.RIGHT);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        try {
                            EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                            setDatosToAnular(EL_OBJECT, paraAnular);
                        } catch (MessageException ex) {
                            buscador.showMessage(ex.getMessage(), "Error de datos", 0);
                        }
                    }
                }
            }
        });
        if (paraAnular) {
            buscador.getCheckAnulada().setEnabled(false);
        }
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQuery();
                    cargarTablaBuscador(query);
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + jpaController.getEntityClass().getSimpleName(), 0);
                }
            }
        });
        buscador.getbImprimir().setVisible(true);
        buscador.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQuery();
                    cargarTablaBuscador(query);
                    doReportFacturas();
                } catch (JRException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                }
            }
        });
        buscador.getbExtra().setVisible(true);
        buscador.getbExtra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscador.getjTable1().getSelectedRow() > -1) {
                    try {
                        EL_OBJECT = jpaController.find((Integer) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0));
                        Caja caja = EL_OBJECT.getCaja();
                        if (caja.isBaja()) {
                            throw new MessageException("La Caja a la cual afecta esta factura fue dada de BAJA");
                        }
                        if (!caja.getEstado()) {
                            throw new MessageException("La Caja a la cual afecta esta factura está en BAJA");

                        }
                        if (EL_OBJECT.getAnulada()) {
                            throw new MessageException("No se puede editar un comprobante ANULADO");
                        }
                        if (EL_OBJECT.getFormaPagoEnum().equals(Valores.FormaPago.CONTADO)) {
                            CajaMovimientos cm = new CajaMovimientosJpaController().findBy(EL_OBJECT.getId(), DetalleCajaMovimientosJpaController.FACTU_VENTA);
                            if (cm.getFechaCierre() != null) {
                                throw new MessageException("No se puede editar el comprobante porque la Caja ( N°" + cm.getId() + ") ya fue cerrada."
                                        + "\nFecha de cierre:" + UTIL.DATE_FORMAT.format(cm.getFechaCierre())
                                        + "\nFecha de sistema:" + UTIL.TIMESTAMP_FORMAT.format(cm.getSistemaFechaCierre()));
                            }
                        } else if (EL_OBJECT.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
                            CtacteCliente ccc = new CtacteClienteJpaController().findBy(EL_OBJECT);
                            if (ccc.getEntregado() > 0) {
                                throw new MessageException("No se puede modificar una Factura cuya forma de pago es a Cta. Cte. y ya tiene asignado pago(s)."
                                        + "\nEstado de la Cta. Cte. para esta Factura: " + ccc.getEstadoEnum()
                                        + "\nImporte:   $" + UTIL.PRECIO_CON_PUNTO.format(ccc.getImporte())
                                        + "\nEntregado: $" + UTIL.PRECIO_CON_PUNTO.format(ccc.getEntregado()));
                            }
                        }
                        setDatosToEdit();
                        String query = armarQuery();
                        cargarTablaBuscador(query);
                    } catch (MessageException ex) {
                        buscador.showMessage(ex.getMessage(), "Error de datos", 0);
                    }
                } else {
                    buscador.showMessage("Seleccione la fila que corresponde a la Factura que desea editar", null, JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        viewMode = true;
        buscador.setListeners(this);
        buscador.setLocationRelativeTo(frame);
        buscador.setVisible(true);
    }

    private void doReportFacturas() throws MissingReportException, JRException {
        List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscador.getjTable1().getRowCount());
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {

            data.add(new GenericBeanCollection(
                    dtm.getValueAt(row, 1),
                    dtm.getValueAt(row, 2),
                    dtm.getValueAt(row, 3),
                    dtm.getValueAt(row, 4),
                    dtm.getValueAt(row, 5),
                    dtm.getValueAt(row, 6),
                    dtm.getValueAt(row, 7),
                    dtm.getValueAt(row, 8),
                    dtm.getValueAt(row, 9),
                    null, null, null));
        }
        Reportes r = new Reportes("JGestion_ListadoFacturasCompra.jasper", "Listado Facturas Venta");
        r.setDataSource(data);
        r.addParameter("IS_COMPRA", false);
        r.addMembreteParameter();
        r.addConnection();
        r.viewReport();
    }

    private void btnAceptarActionWhenEditing(boolean facturar) {
        try {
            jdFactura.getBtnFacturar().setEnabled(false);
            jdFactura.getBtnAceptar().setEnabled(false);
            setAndEdit(facturar);
            jdFactura.dispose();
        } catch (MessageException ex) {
            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
        } catch (Exception ex) {
            jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
            LOG.error(ex, ex);
        } finally {
            jdFactura.getBtnFacturar().setEnabled(true);
            jdFactura.getBtnAceptar().setEnabled(true);
        }
    }

    private void setDatosToEdit() throws MessageException {
        initFacturaVenta(null, true, this, 1, false, true);
        jdFactura.setModoEdicion();
        jdFactura.hidePanelCambio();
        jdFactura.addActionAndKeyListener(this);
        for (ActionListener actionListener : jdFactura.getBtnAceptar().getActionListeners()) {
            jdFactura.getBtnAceptar().removeActionListener(actionListener);
        }
        for (ActionListener actionListener : jdFactura.getBtnFacturar().getActionListeners()) {
            jdFactura.getBtnFacturar().removeActionListener(actionListener);
        }
        jdFactura.getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnAceptarActionWhenEditing(false);
            }
        });
        jdFactura.getBtnFacturar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnAceptarActionWhenEditing(true);
            }
        });
        // seteando datos de Factura
        jdFactura.setDcFechaFactura(EL_OBJECT.getFechaVenta());
        UTIL.setSelectedItem(jdFactura.getCbCliente(), EL_OBJECT.getCliente());
        Sucursal s = EL_OBJECT.getSucursal();
        UTIL.setSelectedItem(jdFactura.getCbSucursal(), new ComboBoxWrapper<Sucursal>(s, s.getId(), s.getNombre()));
        cargarComboTiposFacturas(EL_OBJECT.getCliente());
        if (EL_OBJECT.getRemito() != null) {
            jdFactura.setTfRemito(JGestionUtils.getNumeracion(EL_OBJECT.getRemito(), false));
        }

        //Los tipos de factura se tienen q cargar antes, sinó modifica el Nº de factura y muestra el siguiente
        //y no el de Factura seleccionada
        UTIL.setSelectedItem(jdFactura.getCbListaPrecio(), EL_OBJECT.getListaPrecios());
        UTIL.setSelectedItem(jdFactura.getCbCaja(), EL_OBJECT.getCaja());
        UTIL.setSelectedItem(jdFactura.getCbFormaPago(), EL_OBJECT.getFormaPagoEnum().toString());
        jdFactura.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(EL_OBJECT.getSucursal().getPuntoVenta(), 4));
        jdFactura.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(EL_OBJECT.getNumero(), 8));
        jdFactura.setTfNumMovimiento(String.valueOf(EL_OBJECT.getMovimientoInterno()));
        if (EL_OBJECT.getFormaPagoEnum() == Valores.FormaPago.CTA_CTE) {
            if (EL_OBJECT.getDiasCtaCte() != null) {
                jdFactura.setTfDias(EL_OBJECT.getDiasCtaCte().toString());
            }
        }
        // <editor-fold defaultstate="collapsed" desc="Carga de DetallesVenta">
        List<DetalleVenta> lista = EL_OBJECT.getDetallesVentaList();
        DefaultTableModel dtm = (DefaultTableModel) jdFactura.getjTable1().getModel();
        dtm.setRowCount(0);
        for (DetalleVenta detalle : lista) {
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
            try {
                double productoConIVA = detalle.getPrecioUnitario()
                        + UTIL.getPorcentaje(detalle.getPrecioUnitario(), Double.valueOf(iva.getIva()));
                //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
                dtm.addRow(new Object[]{
                            iva.getIva(),
                            detalle.getProducto().getCodigo(),
                            detalle.getProducto().getNombre() + "(" + detalle.getProducto().getIva().getIva() + ")",
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            productoConIVA,
                            detalle.getDescuento(),
                            UTIL.PRECIO_CON_PUNTO.format((detalle.getCantidad() * productoConIVA) - detalle.getDescuento()),
                            detalle.getTipoDesc(),
                            detalle.getProducto().getId(),
                            null
                        });
            } catch (NullPointerException e) {
                throw new MessageException("Ocurrió un error recuperando el detalle y los datos del Producto:"
                        + "\nCódigo:" + detalle.getProducto().getNombre()
                        + "\nNombre:" + detalle.getProducto().getCodigo()
                        + "\nIVA:" + detalle.getProducto().getIva()
                        + "\n\n   Intente nuevamente.");
            }
        }// </editor-fold>
        //totales
        jdFactura.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getImporte() - (EL_OBJECT.getIva10() + EL_OBJECT.getIva21())));
        jdFactura.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getIva10()));
        jdFactura.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getIva21()));
        jdFactura.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getImporte()));
        jdFactura.setLocationRelativeTo(buscador);
        jdFactura.setVisible(true);
    }

    private void cargarTablaBuscador(String query) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        List<FacturaVenta> l = jpaController.findByNativeQuery(query);
        for (FacturaVenta facturaVenta : l) {
            dtm.addRow(new Object[]{
                        facturaVenta.getId(), // <--- no es visible
                        JGestionUtils.getNumeracion(facturaVenta),
                        facturaVenta.getMovimientoInterno(),
                        facturaVenta.getCliente().getNombre(),
                        BigDecimal.valueOf(facturaVenta.getImporte()),
                        UTIL.DATE_FORMAT.format(facturaVenta.getFechaVenta()),
                        facturaVenta.getSucursal().getNombre(),
                        facturaVenta.getCaja().getNombre(),
                        facturaVenta.getUsuario().getNick(),
                        UTIL.TIMESTAMP_FORMAT.format(facturaVenta.getFechaalta())
                    });
        }
    }

    private String armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM factura_venta o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu8());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + jpaController.getEntityClass().getSimpleName() + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_venta >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_venta <= '").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            query.append(" AND o.caja = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbCaja().getItemCount(); i++) {
                Caja caja = (Caja) buscador.getCbCaja().getItemAt(i);
                query.append(" o.caja=").append(caja.getId());
                if ((i + 1) < buscador.getCbCaja().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal = ").append(((ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            query.append(" AND o.forma_pago = ").append(((Valores.FormaPago) buscador.getCbFormasDePago().getSelectedItem()).getId());
        }

        if (buscador.getTfFactu4().trim().length() > 0) {
            try {
                query.append(" AND o.movimiento_interno = ").append(Integer.valueOf(buscador.getTfFactu4()));
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de movimiento no válido");
            }
        }
        query.append(" ORDER BY o.id");
        LOG.trace("queryBuscador=" + query);
        return query.toString();
    }

    /**
     * Setea los datos de
     * <code>FacturaVenta</code> en la instancia de {@link JDFacturaVenta} del
     * controlador.
     *
     * @param selectedFacturaVenta
     * @param paraAnular
     */
    void setDatosToAnular(FacturaVenta selectedFacturaVenta, final boolean paraAnular) throws MessageException {
        initFacturaVenta(null, true, this, 1, false, false);
        jdFactura.getBtnAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jdFactura.getBtnFacturar().setEnabled(false);
                    jdFactura.getBtnAceptar().setEnabled(false);
                    setAndPersist(false);
                } catch (MessageException ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    Logger.getLogger(SucursalController.class.getName()).log(Level.ERROR, null, ex);
                } finally {
                    jdFactura.getBtnFacturar().setEnabled(true);
                    jdFactura.getBtnAceptar().setEnabled(true);
                }
            }
        });
        jdFactura.getBtnFacturar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jdFactura.getBtnFacturar().setEnabled(false);
                    jdFactura.getBtnAceptar().setEnabled(false);
                    setAndPersist(true);
                } catch (MessageException ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    jdFactura.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    Logger.getLogger(SucursalController.class.getName()).log(Level.ERROR, null, ex);
                } finally {
                    jdFactura.getBtnFacturar().setEnabled(true);
                    jdFactura.getBtnAceptar().setEnabled(true);
                }
            }
        });
        // seteando datos de FacturaCompra
        jdFactura.getCbCliente().addItem(selectedFacturaVenta.getCliente());
        Sucursal s = selectedFacturaVenta.getSucursal();
        ComboBoxWrapper<Sucursal> cbw = new ComboBoxWrapper<Sucursal>(s, s.getId(), s.getNombre());
        jdFactura.getCbSucursal().addItem(cbw);
        jdFactura.getCbListaPrecio().addItem(selectedFacturaVenta.getListaPrecios());
        jdFactura.setDcFechaFactura(selectedFacturaVenta.getFechaVenta());
        if (selectedFacturaVenta.getRemito() != null) {
            jdFactura.setTfRemito(JGestionUtils.getNumeracion(selectedFacturaVenta.getRemito(), false));
        }

        //Los tipos de factura se tienen q cargar antes, sinó modifica el Nº de factura y muestra el siguiente
        //y no el de Factura seleccionada
        cargarComboTiposFacturas(selectedFacturaVenta.getCliente());
        jdFactura.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(selectedFacturaVenta.getSucursal().getPuntoVenta(), 4));
        jdFactura.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(selectedFacturaVenta.getNumero(), 8));
        jdFactura.setTfNumMovimiento(String.valueOf(selectedFacturaVenta.getMovimientoInterno()));
        jdFactura.getCbCaja().addItem(selectedFacturaVenta.getCaja());
        UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
        UTIL.setSelectedItem(jdFactura.getCbFormaPago(), selectedFacturaVenta.getFormaPagoEnum().toString());
        if (selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CTA_CTE) {
            if (selectedFacturaVenta.getDiasCtaCte() != null) {
                jdFactura.setTfDias(selectedFacturaVenta.getDiasCtaCte().toString());
            }
        }
        // <editor-fold defaultstate="collapsed" desc="Carga de DetallesVenta">
        selectedFacturaVenta = jpaController.find(selectedFacturaVenta.getId());
        DefaultTableModel dtm = (DefaultTableModel) jdFactura.getjTable1().getModel();
        for (DetalleVenta detallesVenta : selectedFacturaVenta.getDetallesVentaList()) {
            Iva iva = detallesVenta.getProducto().getIva();
            try {
                double productoConIVA = detallesVenta.getPrecioUnitario()
                        + UTIL.getPorcentaje(detallesVenta.getPrecioUnitario(), iva.getIva());
                //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
                dtm.addRow(new Object[]{
                            iva.getIva(),
                            detallesVenta.getProducto().getCodigo(),
                            detallesVenta.getProducto().getNombre() + "(" + iva.getIva() + ")",
                            detallesVenta.getCantidad(),
                            detallesVenta.getPrecioUnitario(),
                            UTIL.PRECIO_CON_PUNTO.format(productoConIVA),
                            detallesVenta.getDescuento(),
                            UTIL.PRECIO_CON_PUNTO.format((detallesVenta.getCantidad() * productoConIVA) - detallesVenta.getDescuento()),
                            detallesVenta.getTipoDesc(),
                            detallesVenta.getProducto().getId(),
                            null
                        });
            } catch (NullPointerException e) {
                throw new MessageException("Ocurrió un error recuperando el detalle y los datos del Producto:"
                        + "\nCódigo:" + detallesVenta.getProducto().getNombre()
                        + "\nNombre:" + detallesVenta.getProducto().getCodigo()
                        + "\nIVA:" + detallesVenta.getProducto().getIva()
                        + "\n\n   Intente nuevamente.");
            }
        }// </editor-fold>
        refreshResumen(jdFactura);
        jdFactura.modoVista();

        //viendo si se habilita el botón FACTURAR
        if ((!selectedFacturaVenta.getAnulada())
                && selectedFacturaVenta.getNumero() == 0
                && selectedFacturaVenta.getTipo() == 'I') {
            //si NO está anulada
            //es decir que es un movimiento interno, y puede ser cambiado a FACTURA VENTA
            jdFactura.getBtnFacturar().setEnabled(true);
        }

        if (paraAnular) {
            jdFactura.hidePanelCambio();
            jdFactura.getBtnCancelar().setVisible(false);
            jdFactura.getBtnAceptar().setVisible(false);
            jdFactura.getBtnFacturar().setVisible(false);
            jdFactura.getBtnAnular().setVisible(paraAnular);
            jdFactura.getCheckFacturacionElectronica().setVisible(false);
        }
        jdFactura.addActionAndKeyListener(this);
        jdFactura.setLocationRelativeTo(buscador);
        jdFactura.setVisible(true);
    }

    private void anular(FacturaVenta factura) throws MessageException {
        Caja oldCaja = factura.getCaja();
        List<Caja> cajasPermitidasList = new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true);
        if (cajasPermitidasList.isEmpty() || !cajasPermitidasList.contains(oldCaja)) {
            throw new MessageException("No tiene permiso de acceso a la Caja " + oldCaja + ", con la que fue realizada la venta.");
        }
        CajaMovimientosJpaController cmController = new CajaMovimientosJpaController();
        CajaMovimientos cmAbierta;
        try {
            cmAbierta = cmController.findCajaMovimientoAbierta(oldCaja);
        } catch (NoResultException e) {
            throw new MessageException("ERROR DE SISTEMA\nHay un problema con la Caja " + oldCaja.getNombre());
        }

        if (cmAbierta.getCaja().isBaja() || (!cmAbierta.getCaja().getEstado())) {
            //cuando la Caja sobre la cual se facturó, está dada de BAJA o no está mas
            // disponible, se tiene que elegir otra caja (se muestra una GUI para esto)
            cmAbierta = initReAsignacionCajaMovimiento(cajasPermitidasList);
            if (cmAbierta == null) {
                throw new MessageException("Proceso de anulación CANCELADO");
            }
        }
        try {
            cmController.anular(factura, cmAbierta);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(jdFactura, ex.getMessage());
            Logger.getLogger(FacturaVentaController.class.getName()).log(Level.ERROR, null, ex);
        }
    }

    CajaMovimientos initReAsignacionCajaMovimiento(List<Caja> cajasPermitidasList) {
        List<CajaMovimientos> l = new ArrayList<CajaMovimientos>();
        for (Caja caja : cajasPermitidasList) {
            l.add(new CajaMovimientosJpaController().findCajaMovimientoAbierta(caja));
        }
        final PanelReasignacionDeCaja panel = new PanelReasignacionDeCaja();
        UTIL.loadComboBox(panel.getCbCaja(), l, false);
        JDABM abm = new JDABM(null, null, true, panel);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cajaMovToAsentarAnulacion = (CajaMovimientos) panel.getCbCaja().getSelectedItem();
            }
        });
        abm.setVisible(true);
        return cajaMovToAsentarAnulacion;
    }

    private void imprimirMovimientoInterno(FacturaVenta facturaVenta) throws Exception {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_FacturaVenta_I.jasper", "Comprobante venta N°" + facturaVenta.getMovimientoInterno());
        r.addMembreteParameter();
        r.addParameter("FACTURA_ID", facturaVenta.getId());
        r.printReport(false);
    }

    private void imprimirFactura(FacturaVenta facturaVenta) throws Exception {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_FacturaVenta_" + facturaVenta.getTipo() + ".jasper", "Factura Venta");
        r.addParameter("FACTURA_ID", facturaVenta.getId());
        if (facturaVenta.getRemito() != null) {
            r.addParameter("REMITO", JGestionUtils.getNumeracion(facturaVenta.getRemito(), false));
        }
        if (facturaVenta.getCheque() != null) {
            r.addParameter("CHEQUE", UTIL.AGREGAR_CEROS(facturaVenta.getCheque().getNumero(), 12));
        }
        r.printReport(true);
    }

    private String getNextNumeroFacturaConGuion(char letra) {
        Sucursal s = getSelectedSucursalFromJDFacturaVenta();
        Integer nextNumero = jpaController.getNextNumeroFactura(s, letra);
        String factuString = UTIL.AGREGAR_CEROS(nextNumero.toString(), 8);
        factuString = UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4) + "-" + factuString;
        return factuString;
    }

    private void cambiarMovimientoInternoToFactura(FacturaVenta facturaVenta) throws Exception {
        Sucursal s = getSelectedSucursalFromJDFacturaVenta();
        facturaVenta.setTipo(jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
        facturaVenta.setNumero(jpaController.getNextNumeroFactura(s, facturaVenta.getTipo()));
        facturaVenta.setMovimientoInterno(0);
        DAO.doMerge(facturaVenta);
        if (facturaVenta.getFormaPago() == Valores.FormaPago.CONTADO.getId()) {
            //si NO es al CONTADO no tiene un registro de DetalleCajaMovimiento
            new CajaMovimientosController().actualizarDescripcion(facturaVenta);
        }
        imprimirFactura(facturaVenta);
        jdFactura.getBtnFacturar().setEnabled(false);
//      limpiarPanel();
    }

    private void setAndEdit(boolean facturar) throws MessageException, Exception {
        checkConstraints();
        FacturaVenta editedFacturaVenta = getEntity(facturar);
        editedFacturaVenta.setId(EL_OBJECT.getId());
        editedFacturaVenta.setFechaalta(EL_OBJECT.getFechaalta());
        List<String> modificaciones = new ArrayList<String>(9);
        boolean cambiaCliente = false;
        boolean cambiaSucursal = false;
        boolean cambiaFacturacion = false;
        boolean cambiaCaja = false;
        boolean cambiaFormaPago = false;
        if (0 != UTIL.compararIgnorandoTimeFields(EL_OBJECT.getFechaVenta(), editedFacturaVenta.getFechaVenta())) {
            modificaciones.add("Se modificará la Fecha del comprobante de: " + UTIL.DATE_FORMAT.format(EL_OBJECT.getFechaVenta())
                    + ", por: " + UTIL.DATE_FORMAT.format(editedFacturaVenta.getFechaVenta()));
        }
        if (!EL_OBJECT.getCliente().equals(editedFacturaVenta.getCliente())) {
            modificaciones.add("Se modificará el Cliente: " + EL_OBJECT.getCliente().getNombre() + " (" + EL_OBJECT.getCliente().getNumDoc() + ")"
                    + ", por: " + editedFacturaVenta.getCliente().getNombre() + " (" + editedFacturaVenta.getCliente().getNumDoc() + ")");
            cambiaCliente = true;
        }
        if (!EL_OBJECT.getSucursal().equals(editedFacturaVenta.getSucursal())) {
            modificaciones.add("Se modificará la Sucursal: " + EL_OBJECT.getSucursal().getNombre() + ", por: " + editedFacturaVenta.getSucursal().getNombre());
            cambiaSucursal = true;
        }
        if (!EL_OBJECT.getCaja().equals(editedFacturaVenta.getCaja())) {
            modificaciones.add("Se modificará la Caja: " + EL_OBJECT.getCaja().getNombre() + ", por: " + editedFacturaVenta.getCaja().getNombre());
            cambiaCaja = true;
        }
        if (!EL_OBJECT.getFormaPagoEnum().equals(editedFacturaVenta.getFormaPagoEnum())) {
            modificaciones.add("Se modificará la Forma de Pago: " + EL_OBJECT.getFormaPagoEnum() + ", por: " + editedFacturaVenta.getFormaPagoEnum());
            cambiaFormaPago = true;
        }
        if (editedFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
            if (!cambiaFormaPago && EL_OBJECT.getDiasCtaCte() != editedFacturaVenta.getDiasCtaCte()) {
                modificaciones.add("Se modificará la cantidad de días de la Cta. Cte: " + EL_OBJECT.getDiasCtaCte() + ", por: " + editedFacturaVenta.getDiasCtaCte());
            }
        }
        //siempre va cambiar (ser mayor) el N° de factura cuando le de FACTURAR
        if ((facturar && !cambiaSucursal && 'I' != Character.toUpperCase(EL_OBJECT.getTipo()))
                || (!facturar && !cambiaSucursal && 'I' == Character.toUpperCase(EL_OBJECT.getTipo()))) {
            LOG.debug("Se mantiene la numeración, porque facturar=" + facturar + ", cambiaSucursal=" + cambiaSucursal + ":"
                    + "\nOld==" + JGestionUtils.getNumeracion(EL_OBJECT)
                    + "\nEdi==" + JGestionUtils.getNumeracion(editedFacturaVenta));
            editedFacturaVenta.setMovimientoInterno(EL_OBJECT.getMovimientoInterno());
            editedFacturaVenta.setNumero(EL_OBJECT.getNumero());
        } else if ((EL_OBJECT.getMovimientoInterno() != editedFacturaVenta.getMovimientoInterno())
                || EL_OBJECT.getNumero() != editedFacturaVenta.getNumero()) {
            modificaciones.add("Se modificará la Numeración: " + JGestionUtils.getNumeracion(EL_OBJECT) + ", por: " + JGestionUtils.getNumeracion(editedFacturaVenta));
            cambiaFacturacion = true;
        }
        if (EL_OBJECT.getRemito() != null && !editedFacturaVenta.getRemito().equals(EL_OBJECT.getRemito())) {
            Remito remitoToUnbind = EL_OBJECT.getRemito();
            modificaciones.add("Se desvinculará el Remito N°" + JGestionUtils.getNumeracion(remitoToUnbind, true) + " del comprobante.");
        }
        if (editedFacturaVenta.getRemito() != null && !editedFacturaVenta.getRemito().equals(EL_OBJECT.getRemito())) {
            modificaciones.add("Se vinculará el Remito N°" + JGestionUtils.getNumeracion(remitoToFacturar, true) + " al comprobante.");
        }

        if (!modificaciones.isEmpty()) {
            StringBuilder sb = new StringBuilder(modificaciones.size() * 50);
            for (String string : modificaciones) {
                sb.append("\n").append(string);
            }
            sb.append("\n¿Confirma las modificaciones?");
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdFactura, sb.toString(), "Confirmación de modificaciónes", JOptionPane.YES_NO_OPTION)) {
                String mensaje = edit(cambiaCaja, cambiaFormaPago, editedFacturaVenta);
                if (facturar) {
                    imprimirFactura(editedFacturaVenta);
                } else if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFactura,
                        "¿Imprimir comprobante?", jpaController.getEntityClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION)) {
                    imprimirMovimientoInterno(editedFacturaVenta);
                }
                jdFactura.showMessage("Modificaciones realizadas.\n" + mensaje, null, JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            throw new MessageException("No se han realizado modificaciones en el comprobante");
        }
    }

    private String edit(boolean cambiaCaja, boolean cambiaFormaPago, FacturaVenta editedFacturaVenta) throws MessageException, Exception {
        String mensajeDeQueMierdaPaso = null;
        CajaMovimientos cm = new CajaMovimientosJpaController().findBy(EL_OBJECT.getId(), DetalleCajaMovimientosJpaController.FACTU_VENTA);
        if (cm.getFechaCierre() != null) {
            throw new MessageException("La Caja " + cm.getCaja().getNombre() + " N°" + cm.getId() + " fue cerrada mientras pensabas");
        }
        System.out.println("ANTES" + editedFacturaVenta);
        editedFacturaVenta = jpaController.merge(editedFacturaVenta);
        System.out.println("DESPU" + editedFacturaVenta);
        if (EL_OBJECT.getFormaPagoEnum().equals(Valores.FormaPago.CONTADO)) {
            DetalleCajaMovimientos dcm = new DetalleCajaMovimientosJpaController().findBy(EL_OBJECT.getId(), DetalleCajaMovimientosJpaController.FACTU_VENTA);
            if (cambiaCaja || cambiaFormaPago) {
                new DetalleCajaMovimientosJpaController().remove(dcm);
                mensajeDeQueMierdaPaso = "Se eliminó de la Caja " + cm.getCaja().getNombre() + " N°" + cm.getId()
                        + "\nEl detalle: " + dcm.getDescripcion() + ", monto $" + UTIL.PRECIO_CON_PUNTO.format(dcm.getMonto());
                registrarVentaSegunFormaDePago(editedFacturaVenta);
            } else {
                //cambió Sucursal, Cliente, Fecha
                dcm.setDescripcion(JGestionUtils.getNumeracion(editedFacturaVenta) + " " + editedFacturaVenta.getCliente().getNombre());
                new DetalleCajaMovimientosJpaController().edit(dcm);
            }
        } else if (EL_OBJECT.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
            CtacteClienteJpaController cccController = new CtacteClienteJpaController();
            CtacteCliente oldCCC = cccController.findCtacteClienteByFactura(EL_OBJECT.getId());
            if (cambiaFormaPago) {
                cccController.destroy(oldCCC.getId());
                mensajeDeQueMierdaPaso = "Se eliminó el DEBE correspondiente a la Factura " + JGestionUtils.getNumeracion(EL_OBJECT)
                        + "\nde la Cta. Cte. del Cliente " + EL_OBJECT.getCliente().getNombre() + ".";
                registrarVentaSegunFormaDePago(editedFacturaVenta);
            } else {
                //cambió Sucursal, Cliente, Fecha, Dias Ctacte
                oldCCC.setDias(editedFacturaVenta.getDiasCtaCte());
                oldCCC.setFactura(editedFacturaVenta);
                cccController.edit(oldCCC);
            }
//        } else if (EL_OBJECT.getFormaPagoEnum().equals(Valores.FormaPago.CHEQUE)) {
//            throw new MessageException("Modificación de Forma de pago con CHEQUE no soportadas aún");
//            ChequeTerceros cheque = null;
//            if (editedFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CHEQUE)
//                    || editedFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CONTADO_CHEQUE)) {
//                //entity Cheque persisted..
//                cheque = getChequeToBind(editedFacturaVenta);
//            }
//            if (cheque != null) {
//                cheque.setBoundId(editedFacturaVenta.getId().longValue());
//                new ChequeTercerosJpaController().edit(cheque);
//                editedFacturaVenta.setCheque(cheque);
//            }
        } else {
            mensajeDeQueMierdaPaso = "Algo salió mal";
        }

        if (EL_OBJECT.getRemito() != null && !editedFacturaVenta.getRemito().equals(EL_OBJECT.getRemito())) {
            Remito remitoToUnbind = EL_OBJECT.getRemito();
            remitoToUnbind.setFacturaVenta(null);
            new RemitoController().edit(remitoToUnbind);
        }
        if (editedFacturaVenta.getRemito() != null && !editedFacturaVenta.getRemito().equals(EL_OBJECT.getRemito())) {
            Remito remitoToBind = editedFacturaVenta.getRemito();
            remitoToBind.setFacturaVenta(editedFacturaVenta);
            new RemitoController().edit(remitoToBind);
        }

        return mensajeDeQueMierdaPaso;
    }

    private void setAndPersist(boolean conFactura) throws MessageException, Exception {
        if (!viewMode) {
            //cuando se está en modo vista.. no va entrar ACA
            //sinó que va ir directo a la opción de imprimir (Re-imprimir)
            checkConstraints();
            if (!conFactura && unlockedNumeracion) {
                throw new MessageException("Para registrar la carga de una Factura Venta Antigua debe utilizar el botón \"Facturar\"."
                        + "\nNota: No se pueden generar Movimientos Internos antiguos.");
            }
            FacturaVenta newFacturaVenta = getEntity(conFactura);
            jpaController.create(newFacturaVenta);
            //refreshing the entity from DB
            newFacturaVenta = (FacturaVenta) DAO.findEntity(FacturaVenta.class, newFacturaVenta.getId());
            if (newFacturaVenta.getRemito() != null) {
                remitoToFacturar.setFacturaVenta(newFacturaVenta);
                new RemitoController().edit(remitoToFacturar);
            }

            //actualiza Stock
            new StockJpaController().updateStock(newFacturaVenta);
            //asiento en caja..
            registrarVentaSegunFormaDePago(newFacturaVenta);
            if (conFactura) {
                if (jdFactura.getCheckFacturacionElectronica().isSelected()) {
                    AFIPWSController afipWS = new AFIPWSController();
                    afipWS.showSetting(newFacturaVenta);
                }
                imprimirFactura(newFacturaVenta);
            } else {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFactura,
                        "¿Imprimir comprobante?", jpaController.getEntityClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION)) {
                    imprimirMovimientoInterno(newFacturaVenta);
                }
            }
            limpiarPanel();
        } else {
            if (conFactura) {
                if (EL_OBJECT.getTipo() == 'I') {
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFactura,
                            "¿Cambiar factura Movimiento interno Nº" + EL_OBJECT.getMovimientoInterno()
                            + " por Factura Venta \"" + jdFactura.getCbFacturaTipo().getSelectedItem() + "\" Nº"
                            + getNextNumeroFacturaConGuion(jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0)) + "?", "Facturación - Venta", JOptionPane.OK_CANCEL_OPTION)) {
                        cambiarMovimientoInternoToFactura(EL_OBJECT);
                    }
                } else {
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFactura,
                            "La Factura Nº" + EL_OBJECT.getNumero()
                            + " ya fue impresa.\n¿Volver a imprimir?", jpaController.getEntityClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION)) {
                        imprimirFactura(EL_OBJECT);
                    }
                }
            } else {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFactura,
                        "¿Re-Imprimir comprobante?", jpaController.getEntityClass().getSimpleName(), JOptionPane.OK_CANCEL_OPTION)) {
                    imprimirMovimientoInterno(EL_OBJECT);
                }
            }
        }
    }

    /**
     * Borra el detalle de la factura Actualiza Movimiento interno y/o Número de
     * factura (según corresponda)
     */
    private void limpiarPanel() {
        borrarDetalles();
        jdFactura.setTfRemito("Sin Remito");
        remitoToFacturar = null;
        if (jdFactura.getModoUso() == 1) {
            jdFactura.setTfNumMovimiento(jpaController.getNextMovimientoInterno().toString());
            Sucursal s = getSelectedSucursalFromJDFacturaVenta();
            setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
        }
    }

    /**
     * Limpiar la dtm de detalles de venta. refresca los totales (gravado,
     * IVA's, ...) selectedProducto = null y la info sobre este
     */
    void borrarDetalles() {
        UTIL.limpiarDtm(jdFactura.getDtm());
        refreshResumen(jdFactura);
        selectedProducto = null;
        setInformacionDeProducto(jdFactura, selectedProducto);
        jdFactura.setLabelCodigoNoRegistradoVisible(false);
        jdFactura.getCbCliente().requestFocus();
    }

    JDFacturaVenta getContenedor() {
        return jdFactura;
    }

    private void initBuscadorRemito(Cliente clienteSeleccionado) {
        RemitoController remitoController = new RemitoController();
        remitoController.initBuscadorToFacturar(jdFactura, clienteSeleccionado);
        remitoToFacturar = remitoController.getSelectedRemito();
        if (remitoToFacturar != null) {
            jdFactura.setTfRemito(JGestionUtils.getNumeracion(remitoToFacturar, false));
            if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(jdFactura, "Desea cargar el detalle del remito a la Factura?",
                    "Volcar detalle", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                cargarRemitoToDetallesFactura(remitoToFacturar);
            }
        } else {
            jdFactura.setTfRemito("Sin Remito");
        }
    }

    private void cargarRemitoToDetallesFactura(Remito selectedRemito) {
        List<DetalleRemito> detalleRemitoList = selectedRemito.getDetalleRemitoList();
        Producto producto;
        Integer cantidad;
        ProductoJpaController p = new ProductoJpaController();
        for (DetalleRemito detalleRemito : detalleRemitoList) {
            producto = detalleRemito.getProducto();
            cantidad = detalleRemito.getCantidad();
            selectedProducto = p.find(producto.getId());
            jdFactura.setTfCantidad(cantidad.toString());
            setInformacionDeProducto(jdFactura, selectedProducto);
            try {
                addProductoToDetails();
            } catch (MessageException ex) {
                jdFactura.showMessage(ex.getMessage(), "DUPLICADO", 2);
            }
        }
    }

    /**
     * Verifica el precio mínimo de venta de un producto. <p>Si el producto está
     * en <b>OFERTA</b> NO SE APLICA la regla. <p>Si el producto tiene precio de
     * venta mínimo seteado se compara este con precioUnitario.
     *
     * @param precioUnitario
     * @return true si
     */
    private boolean isPrecioVentaMinimoValido(double precioUnitario) {
        if (productoEnOferta != null) {
            return true;
        } else {
            return (precioUnitario >= selectedProducto.getMinimoPrecioDeVenta());
        }
    }

    private void setIconoListaPrecios(ListaPrecios listaPrecios) {
        if (listaPrecios == null) {
            jdFactura.setVisibleEstrellita(Boolean.FALSE);
        } else {
            ListaPrecios toCatalogoWeb = new ListaPreciosJpaController().findListaPreciosParaCatalogo();
            if (toCatalogoWeb == null) {
                jdFactura.setVisibleEstrellita(Boolean.FALSE);
            } else {
                jdFactura.setVisibleEstrellita(toCatalogoWeb.equals(listaPrecios));
            }
        }
    }

    Sucursal getSelectedSucursalFromJDFacturaVenta() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdFactura.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    /**
     * Inicializa la GUI de Ventas {@link gui.JDFacturaVenta}, que permite la
     * insersión manual del N° del comprobante.
     *
     * @param owner
     * @throws MessageException
     * @see #initFacturaVenta(javax.swing.JFrame, boolean, java.lang.Object,
     * int, boolean, boolean)
     */
    public void unlockedABM(JFrame owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA_NUMERACION_MANUAL);
        unlockedNumeracion = true;
        initFacturaVenta(owner, true, this, 1, false, true);
        jdFactura.setTfFacturaOcteto(null);
        jdFactura.getBtnAceptar().setEnabled(false);
        jdFactura.setNumeroFacturaEditable(true);
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.setVisible(true);
    }

    private FacturaVenta findBySucursal(char tipo, Sucursal sucursal, Integer numero) {
        return jpaController.find(tipo, sucursal, numero);

    }
}
