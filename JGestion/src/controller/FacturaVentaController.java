package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import entity.*;
import generics.AutoCompleteComboBox;
import gui.JDABM;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import gui.PanelReasignacionDeCaja;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.FacturaVentaJpaController;
import jpa.controller.PresupuestoJpaController;
import jpa.controller.ProductoJpaController;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 * Clase (Ventana) usada para crear FacturaVenta, Remitos, Presupuestos
 *
 * @author FiruzzZ
 */
public class FacturaVentaController implements ActionListener, KeyListener {

    public static final String CLASS_NAME = FacturaVenta.class.getSimpleName();
    /**
     * contiene los nombres de las columnas de la ventana de facturación
     */
    public static final String[] COLUMN_NAMES = {"IVA", "Cód. Producto", "Producto (IVA)", "Cantidad", "Precio U.", "Precio con IVA", "Desc", "Total", "TipoDescuento", "Producto.id", "HistorialOfertas.id"};
    public static final int[] COLUMN_WIDTHS = {1, 70, 180, 10, 30, 30, 30, 30, 1, 1, 1};
    public static final Class[] COLUMN_CLASS_TYPES = {Object.class, Object.class, Object.class, String.class, String.class, String.class, String.class, String.class, Object.class, Object.class, Object.class};
    /**
     * Cantidad de item que puede contenedor el detalle. Limitado por el tamaño
     * del reporte (la factura pre-impresa)
     */
    public static final int LIMITE_DE_ITEMS = 14;
    private JDFacturaVenta jdFacturaVenta;
    private FacturaVenta EL_OBJECT;
    /**
     * Para cuando se está usando {@link FacturaVentaJpaController#jdFacturaVenta}
     * Lleva el ctrl para saber cuando se seleccionó un Producto.
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
    private Logger LOG = Logger.getLogger(FacturaVentaController.class);
    private boolean unlockedNumeracion = false;
    private FacturaVentaJpaController jpaController;

    public FacturaVentaController() {
        jpaController = new FacturaVentaJpaController();
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public FacturaVenta findFacturaVenta(Integer id) {
//        EntityManager em = getEntityManager();
//        try {
        FacturaVenta f = (FacturaVenta) DAO.findEntity(FacturaVenta.class, id);
        if (f.getFormaPagoEnum() == Valores.FormaPago.CHEQUE) {
            ChequeTerceros chequeTercerosBound = new ChequeTercerosJpaController().findChequeTerceros(f);
            f.setCheque(chequeTercerosBound);
        }
        return f;
//        } finally {
//            em.close();
//        }
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
     * método. Se pone
     * <code>false</code> cuando se va usar en MODO_VISTA, así 1ro se settean
     * los datos correspondiendtes a la entidad que la va utilizar y luego se
     * puede hacer visible.
     * @param loadDefaultData determina si se cargan los comboBox (listaprecios,
     * cajas, clientes, productos, sucursales) y si se le asignan los {@link ActionListener}
     * a los botones.
     * @throws MessageException Mensajes personalizados de alerta y/o
     * información para el usuario.
     */
    public void initFacturaVenta(JFrame owner, boolean modal, final Object listener,
            final int factVenta1_Presup2_Remito3, boolean setVisible, boolean loadDefaultData)
            throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        UsuarioHelper uh = new UsuarioHelper();
        if (uh.getSucursales().isEmpty()) {
            throw new IllegalArgumentException(Main.resourceBundle.getString("unassigned.sucursal"));
        }
        if (factVenta1_Presup2_Remito3 == 1) {
            if (uh.getCajas(true).isEmpty()) {
                throw new IllegalArgumentException(Main.resourceBundle.getString("unassigned.caja"));
            }
        }
        jdFacturaVenta = new JDFacturaVenta(owner, modal, factVenta1_Presup2_Remito3);
        UTIL.getDefaultTableModel(jdFacturaVenta.getjTable1(),
                COLUMN_NAMES,
                COLUMN_WIDTHS,
                COLUMN_CLASS_TYPES);
        UTIL.setHorizonalAlignment(jdFacturaVenta.getjTable1(), String.class, SwingConstants.RIGHT);
        //esconde las columnas IVA y Tipo de Descuento, Producto.id, HistorialOferta.id
        UTIL.hideColumnsTable(jdFacturaVenta.getjTable1(), new int[]{0, 8, 9, 10});
        //contenedor Productos
        jdFacturaVenta.getbBuscarProducto().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    initBuscarProducto();
                } catch (DatabaseErrorException ex) {
                    jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                }
            }
        });
        //agregar item del detalle
        jdFacturaVenta.getBtnADD().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addProductoToDetails();
                } catch (MessageException ex) {
                    jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex, ex);
                }
            }
        });
        //quitar item del detalle
        jdFacturaVenta.getBtnDEL().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteProductoFromLista(jdFacturaVenta);
            }
        });
        //contenedor Clientes
        jdFacturaVenta.getBtnCliente().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ClienteController ctrl = new ClienteController();
                ctrl.initContenedor(null, true).setVisible(true);
                UTIL.loadComboBox(jdFacturaVenta.getCbCliente(), ctrl.findEntities(), false);
            }
        });
        jdFacturaVenta.getBtnCancelar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jdFacturaVenta.dispose();
            }
        });
        jdFacturaVenta.getTfProductoCodigo().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (jdFacturaVenta.getTfProductoCodigo().getText().trim().length() > 0 && e.getKeyCode() == 10) {
                    try {
                        buscarProducto(jdFacturaVenta.getTfProductoCodigo().getText().trim());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(jdFacturaVenta, "Error en la recuparación de información del Producto:\n"
                                + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre());
                        LOG.error("Error en la recuparación de información del Producto:\n"
                                + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre(), ex);
                    }
                }
            }
        });
        jdFacturaVenta.getTfCambio().addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                if (jdFacturaVenta.getTfCambio().getText().length() > 0) {
                    jdFacturaVenta.setCambio();
                }
            }
        });
        jdFacturaVenta.getTfCambio().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (jdFacturaVenta.getTfCambio().getText().length() > 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    jdFacturaVenta.setCambio();
                }
            }
        });
        jdFacturaVenta.getCbSucursal().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdFacturaVenta.getCbSucursal().isFocusOwner()) {
                    Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                    if (factVenta1_Presup2_Remito3 == 1) {
                        setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
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
            jdFacturaVenta.setTfRemito("Sin Remito");
            jdFacturaVenta.setDcFechaFactura(new Date());
            UTIL.loadComboBox(jdFacturaVenta.getCbProductos(), new ProductoController().findWrappedProductoToCombo(), false);
            // <editor-fold defaultstate="collapsed" desc="AutoCompleteComboBox">
            // must be editable!!!.................
            jdFacturaVenta.getCbProductos().setEditable(true);
            JTextComponent editor = (JTextComponent) jdFacturaVenta.getCbProductos().getEditor().getEditorComponent();
            editor.setDocument(new AutoCompleteComboBox(jdFacturaVenta.getCbProductos()));
            editor.addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        @SuppressWarnings("unchecked")
                        ComboBoxWrapper<Producto> cbw = (ComboBoxWrapper<Producto>) jdFacturaVenta.getCbProductos().getSelectedItem();
                        selectedProducto = new ProductoController().findProductoByCodigo(cbw.getEntity().getCodigo());
                        setInformacionDeProducto(jdFacturaVenta, selectedProducto);
                    } catch (ClassCastException ex) {
                        //cuando no seleccionó ningún Producto del combo
                        jdFacturaVenta.setTfPrecioUnitario("");
                        jdFacturaVenta.setTfProductoIVA("");
                    }
                }
            });
            editor.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            @SuppressWarnings("unchecked")
                            ComboBoxWrapper<Producto> cbw = (ComboBoxWrapper<Producto>) jdFacturaVenta.getCbProductos().getSelectedItem();
                            selectedProducto = new ProductoController().findProductoByCodigo(cbw.getEntity().getCodigo());
                            setInformacionDeProducto(jdFacturaVenta, selectedProducto);
                        } catch (ClassCastException ex) {
                            //imposible que pase esto.. cierto?
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(jdFacturaVenta, "Error en la recuparación de información del Producto:\n"
                                    + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre());
                            LOG.error("Error en la recuparación de información del Producto:\n"
                                    + "Id=" + selectedProducto.getId() + ", Nombre=" + selectedProducto.getNombre(), ex);
                        }
                    }
                }
            });// </editor-fold>
            UTIL.loadComboBox(jdFacturaVenta.getCbCliente(), new ClienteController().findEntities(), false);
            UTIL.loadComboBox(jdFacturaVenta.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), false);
            UTIL.loadComboBox(jdFacturaVenta.getCbListaPrecio(), new ListaPreciosJpaController().findListaPreciosEntities(), false);
            UTIL.loadComboBox(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
            jdFacturaVenta.getCbListaPrecio().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!selectedListaPrecios.equals((ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem())) {
                        if (jdFacturaVenta.getjTable1().getModel().getRowCount() > 0) {
                            //consulta al usuario antes de limpiar el detalle de factura
                            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta,
                                    "Si cambia la lista de precios, se borrarán todos los items ingresados."
                                    + "\nEsto es necesario para volver a calcular los precios en base al porcentaje de ganancia.",
                                    "Advertencia",
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                                borrarDetalles();
                                selectedListaPrecios = (ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem();
                                setIconoListaPrecios(selectedListaPrecios);
                            }
                        }
                    }
                }
            });
            if (factVenta1_Presup2_Remito3 == 1) {
                try {
                    cargarComboTiposFacturas((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
                } catch (ClassCastException ex) {
                    throw new MessageException("Debe crear un Cliente para poder realizar una Facturas venta, Recibos, Presupuestos o Remitos.");
                }
                //carga los tipos de facturas que puede se le pueden dar al Cliente
                jdFacturaVenta.getCbCliente().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            cargarComboTiposFacturas((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
                            remitoToFacturar = null;
                            jdFacturaVenta.setTfRemito("Sin Remito");
                        } catch (ClassCastException ex) {
                            jdFacturaVenta.setTfRemito("Cliente no válido");
                        }
                    }
                });
                //seeking and binding of a Remito to a FacturaVenta
                jdFacturaVenta.getBtnBuscarRemito().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Cliente clienteSeleccionado;
                        try {
                            clienteSeleccionado = (Cliente) jdFacturaVenta.getCbCliente().getSelectedItem();
                            if (clienteSeleccionado == null) {
                                JOptionPane.showMessageDialog(jdFacturaVenta, "Debe seleccionar un Cliente, del cual se buscará el Remito.", "Error", JOptionPane.WARNING_MESSAGE, null);
                            }
                            initBuscadorRemito(clienteSeleccionado);
                        } catch (ClassCastException ex) {
                            JOptionPane.showMessageDialog(jdFacturaVenta, "Debe seleccionar un Cliente, del cual se buscará el Remito.", "Error (No existe cliente)", JOptionPane.WARNING_MESSAGE, null);
                        }
                    }
                });
                jdFacturaVenta.getCbFacturaTipo().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (jdFacturaVenta.getCbFacturaTipo().getSelectedItem() != null) {
                            Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                            setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
                        }
                    }
                });
                jdFacturaVenta.getBtnAceptar().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            jdFacturaVenta.getBtnFacturar().setEnabled(false);
                            jdFacturaVenta.getBtnAceptar().setEnabled(false);
                            setAndPersist(false);
                        } catch (MessageException ex) {
                            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                        } catch (Exception ex) {
                            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                            LOG.error(ex, ex);
                        } finally {
                            jdFacturaVenta.getBtnFacturar().setEnabled(true);
                            jdFacturaVenta.getBtnAceptar().setEnabled(true);
                        }
                    }
                });
                jdFacturaVenta.getBtnFacturar().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            jdFacturaVenta.getBtnFacturar().setEnabled(false);
                            jdFacturaVenta.getBtnAceptar().setEnabled(false);
                            setAndPersist(true);
                        } catch (MessageException ex) {
                            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                        } catch (Exception ex) {
                            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                            LOG.error(ex, ex);
                        } finally {
                            jdFacturaVenta.getBtnFacturar().setEnabled(true);
                            jdFacturaVenta.getBtnAceptar().setEnabled(true);
                        }
                    }
                });
                //hasta que no elija el TIPO A,B,C no se sabe el nº
                jdFacturaVenta.setTfFacturaCuarto("");
                jdFacturaVenta.setTfFacturaOcteto("");
                UTIL.loadComboBox(jdFacturaVenta.getCbCaja(), new UsuarioHelper().getCajas(true), false);
                jdFacturaVenta.setTfNumMovimiento(jpaController.getNextMovimientoInterno().toString());
                Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
            } else if (factVenta1_Presup2_Remito3 == 2) {
                // y aca?...
            } else if (factVenta1_Presup2_Remito3 == 3) {
                jdFacturaVenta.getLabelNumMovimiento().setVisible(false);
                jdFacturaVenta.getTfNumMovimiento().setVisible(false);
                Sucursal s = getSelectedSucursalFromJDFacturaVenta();
                setNumeroFactura(s, ((RemitoController) listener).getNextNumero(s));
            }
        }
        if (factVenta1_Presup2_Remito3 == 1) {
            jdFacturaVenta.getBtnAnular().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (EL_OBJECT.getAnulada()) {
                            throw new MessageException("Ya está ANULADA!");
                        }
                        String factu_compro = EL_OBJECT.getMovimientoInterno() == 0 ? "Factura Venta" : "Comprobante";
                        String msg_extra_para_ctacte = EL_OBJECT.getFormaPago() == Valores.FormaPago.CTA_CTE.getId() ? "\n- Recibos de pago de Cta.Cte." : "";
                        int showOptionDialog = JOptionPane.showOptionDialog(jdFacturaVenta, "- " + factu_compro + " Nº:" + UTIL.AGREGAR_CEROS(EL_OBJECT.getNumero() + "\n- Movimiento de Caja\n- Movimiento de Stock" + msg_extra_para_ctacte, 12), "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null);
                        if (showOptionDialog == JOptionPane.OK_OPTION) {
                            anular(EL_OBJECT);
                            msg_extra_para_ctacte = (msg_extra_para_ctacte.length() > 1 ? "\nNota: Si el Recibo contenía como único detalle de pago esta Factura, este será anulado completamente\ny no solamente la referencia a esta Factura" : "");
                            jdFacturaVenta.showMessage("Anulada" + msg_extra_para_ctacte, "Factura Venta", 1);
                            jdFacturaVenta.dispose();
                        }
                    } catch (MessageException ex) {
                        jdFacturaVenta.showMessage(ex.getMessage(), "ERROR", 0);
                    } catch (Exception ex) {
                    }
                }
            });
        }
        try {
            selectedListaPrecios = (ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem();
            setIconoListaPrecios(selectedListaPrecios);
        } catch (ClassCastException ex) {
            throw new MessageException("Debe crear al menos una Lista de Precios para poder realizar VENTAS."
                    + "\nMenú -> Productos -> Lista de Precios.");
        }
        jdFacturaVenta.setLocation(jdFacturaVenta.getOwner().getX() + 100, jdFacturaVenta.getY() + 50);
        jdFacturaVenta.setVisible(setVisible);
    }

    private void buscarProducto(String codigoProducto) {
        selectedProducto = new ProductoController().findProductoByCodigo(codigoProducto);
        setInformacionDeProducto(jdFacturaVenta, selectedProducto);
    }

    private void addProductoToDetails() throws MessageException {
        if (selectedProducto == null) {
            throw new MessageException("Seleccione un producto");
        }
        if (jdFacturaVenta.getDTM().getRowCount() >= LIMITE_DE_ITEMS) {
            throw new MessageException("La factura no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
        }
        if (selectedListaPrecios == null) {
            throw new MessageException("Debe elegir una lista de precios antes."
                    + "\nSi no existe ninguna debe crearla en el Menú -> Productos -> Lista de precios.");
        }

        int cantidad;
        double precioUnitario; // (precioUnitario + margen listaPrecios)
        double descuentoUnitario;

        // <editor-fold defaultstate="collapsed" desc="ctrl tfCantidad">
        try {
            cantidad = Integer.valueOf(jdFacturaVenta.getTfCantidad());
            if (cantidad < 1) {
                throw new MessageException("La cantidad no puede ser menor a 1");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Cantidad no válida (solo números enteros)");
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="ctrl tfPrecioUnitario">
        try {
            precioUnitario = Double.valueOf(jdFacturaVenta.getTfPrecioUnitario());
            if (precioUnitario < 0) {
                throw new MessageException("El precio unitario no puede ser menor a 0");
            }
            if (!isPrecioVentaMinimoValido(precioUnitario)) {
                throw new MessageException("El precio unitario de venta ($" + precioUnitario + ") no puede"
                        + " ser menor al mínimo de Venta establecido ($" + selectedProducto.getMinimoPrecioDeVenta() + ")"
                        + "\nPara poder vender el producto al precio deseado, debe cambiar el \"precio de venta\" del producto."
                        + "\nUtilice el botón a la izquierda del campo CÓDIGO o"
                        + "\n(Menú -> Productos -> ABM Productos -> Modificar)");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Precio Unitario no válido");
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="ctrl tfDescuento">
        try {
            if (jdFacturaVenta.getTfProductoDesc().length() == 0) {
                descuentoUnitario = 0.0;
            } else {
                descuentoUnitario = Double.valueOf(jdFacturaVenta.getTfProductoDesc());
                if (descuentoUnitario < 0) {
                    throw new MessageException("Descuento no puede ser menor a 0");
                }

                //cuando el descuento es por porcentaje (%)
                if ((jdFacturaVenta.getCbDesc().getSelectedIndex() == 0) && (descuentoUnitario > 100)) {
                    throw new MessageException("El descuento no puede ser superior al 100%");
                } else if ((jdFacturaVenta.getCbDesc().getSelectedIndex() == 1) && (descuentoUnitario > precioUnitario)) {
                    // cuando es por un monto fijo ($)
                    throw new MessageException("El descuento (" + descuentoUnitario + ")no puede ser superior al precio venta (" + precioUnitario + ")");
                }
            }
            // el descuento se aplica SOBRE LA GANANCIA es decir:
            // (precioUnitario - precioMinimoVenta)
            // un descuento del 100% == precioMinimoVenta (cero ganacia y pérdida)
            descuentoUnitario = Contabilidad.GET_MARGEN(
                    (precioUnitario - selectedProducto.getMinimoPrecioDeVenta()),
                    jdFacturaVenta.getCbDesc().getSelectedIndex() + 1,
                    descuentoUnitario);

            if (!isPrecioVentaMinimoValido((precioUnitario - descuentoUnitario))) {
                throw new MessageException("El descuento deseado produce un precio de venta ($" + UTIL.PRECIO_CON_PUNTO.format(precioUnitario - descuentoUnitario) + ")"
                        + "\nmenor al mínimo de Venta establecido ($" + selectedProducto.getMinimoPrecioDeVenta() + ")"
                        + "\nPara poder realizar este descuento, debe cambiar este mínimo de venta ajustando el \"precio de venta\" del producto."
                        + "\n(Menú -> Productos -> ABM Productos -> Modificar)");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Descuento no válido");
        }// </editor-fold>

        //adiciona el descuento
        descuentoUnitario = Double.valueOf(UTIL.PRECIO_CON_PUNTO.format(descuentoUnitario));
        precioUnitario -= descuentoUnitario;
        double unitarioConIva;

        if (productoEnOferta == null) {
            //el IVA se calcula sobre (precioUnitario - descuentos)
            unitarioConIva = precioUnitario + UTIL.getPorcentaje(precioUnitario, selectedProducto.getIva().getIva());
        } else {
            //el precio en oferta ya incluye IVA
            //así que hay que hacerle la inversa 
            unitarioConIva = precioUnitario;
            if ((selectedProducto.getIva().getIva() > 0)) {
                precioUnitario = (precioUnitario / ((selectedProducto.getIva().getIva() / 100) + 1));
            }
        }
        for (int i = 0; i < jdFacturaVenta.getjTable1().getRowCount(); i++) {
            if (((Integer) UTIL.getDtm(jdFacturaVenta.getjTable1()).getValueAt(i, 9)).equals(selectedProducto.getId())) {
                if (!UTIL.getDtm(jdFacturaVenta.getjTable1()).getValueAt(i, 1).toString().equals(selectedProducto.getCodigo())) {
                    throw new MessageException("No te pases de vivo pinkiwinki!!");
                }
                throw new MessageException("Ya se ha agregado el Producto: ("
                        + selectedProducto.getCodigo() + ") "
                        + selectedListaPrecios.getNombre() + " al detalle.");
            }
        }

        //quitando decimes indeseados..
        precioUnitario = Double.valueOf(UTIL.PRECIO_CON_PUNTO.format(precioUnitario));
        unitarioConIva = Double.valueOf(UTIL.PRECIO_CON_PUNTO.format(unitarioConIva));

        Logger.getLogger(this.getClass()).debug("precioU=" + precioUnitario + ", descuento=" + descuentoUnitario + ", conIVA=" + unitarioConIva + ", subTotal=" + (cantidad * unitarioConIva));
        //carga detallesVenta en tabla
        jdFacturaVenta.getDTM().addRow(new Object[]{
                    selectedProducto.getIva().toString(),
                    selectedProducto.getCodigo(),
                    selectedProducto.getNombre() + "(" + selectedProducto.getIva().toString() + ")",
                    cantidad,
                    UTIL.PRECIO_CON_PUNTO.format(precioUnitario), //columnIndex == 4
                    UTIL.PRECIO_CON_PUNTO.format(unitarioConIva),
                    UTIL.PRECIO_CON_PUNTO.format(descuentoUnitario * cantidad),
                    UTIL.PRECIO_CON_PUNTO.format(cantidad * unitarioConIva), //subTotal
                    (descuentoUnitario == 0) ? -1 : (jdFacturaVenta.getCbDesc().getSelectedIndex() + 1),//Tipo de descuento
                    selectedProducto.getId(),
                    productoEnOferta
                });
        refreshResumen(jdFacturaVenta);
    }

    void deleteProductoFromLista(JDFacturaVenta contenedor) {
        if (contenedor.getjTable1().getSelectedRow() >= 0) {
            contenedor.getDTM().removeRow(contenedor.getjTable1().getSelectedRow());
            refreshResumen(contenedor);
        }
    }

    /**
     * Setea la info del Producto en la instancia de {@link gui.JDFacturaVenta}
     *
     * @param contenedor instancia de
     * <code>gui.JDFacturaVenta</code>
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
        Double gravado = 0.0;
        Double iva10 = 0.0;
        Double iva21 = 0.0;
        BigDecimal otrosImps = BigDecimal.ZERO;
        Double desc = 0.0;
        Double subTotal = 0.0;
        double precioUnitario;
        DefaultTableModel dtm = contenedor.getDTM();
        for (int index = 0; index < dtm.getRowCount(); index++) {
            double cantidad = Double.valueOf(dtm.getValueAt(index, 3).toString());

            // precioSinIVA + cantidad
            gravado += (cantidad * Double.valueOf(dtm.getValueAt(index, 4).toString()));

            // IVA's ++
            precioUnitario = Double.valueOf(dtm.getValueAt(index, 4).toString());
            Double alicuotaDelProducto;
            if (dtm.getValueAt(index, 0).toString().equalsIgnoreCase("10.5")) {
                alicuotaDelProducto = UTIL.getPorcentaje(precioUnitario, 10.5);
                //quitando decimes indeseados..
                alicuotaDelProducto = Double.valueOf(UTIL.PRECIO_CON_PUNTO.format(alicuotaDelProducto));
                iva10 += cantidad * alicuotaDelProducto;
            } else if (dtm.getValueAt(index, 0).toString().equalsIgnoreCase("21.0")) {
                alicuotaDelProducto = UTIL.getPorcentaje(precioUnitario, 21.0);
                //quitando decimes indeseados..
                alicuotaDelProducto = Double.valueOf(UTIL.PRECIO_CON_PUNTO.format(alicuotaDelProducto));
                iva21 += cantidad * alicuotaDelProducto;
            } else {
                Double alic = Double.valueOf(dtm.getValueAt(index, 0).toString());
                alicuotaDelProducto = UTIL.getPorcentaje(precioUnitario, alic);
                //quitando decimes indeseados..
                alicuotaDelProducto = Double.valueOf(UTIL.PRECIO_CON_PUNTO.format(alicuotaDelProducto));
                otrosImps = otrosImps.add(BigDecimal.valueOf(cantidad).multiply(BigDecimal.valueOf(alicuotaDelProducto)));
                Logger.getLogger(this.getClass()).trace("Alicuota no standart=" + alic + ", alicDel Producto=" + alicuotaDelProducto + ", total Otros Imps=" + otrosImps.toString());
            }
            //Descuento++
            //si el subtotal es > 0... para no dar resultados negativos!!!
            if (Double.valueOf(dtm.getValueAt(index, 7).toString()) >= 0) {
                desc += Double.valueOf(dtm.getValueAt(index, 6).toString());
            }

            subTotal += Double.valueOf(dtm.getValueAt(index, 7).toString());
        }
        contenedor.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado));
        contenedor.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(iva10));
        contenedor.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(iva21));
        contenedor.setTfTotalOtrosImps(UTIL.PRECIO_CON_PUNTO.format(otrosImps));
        contenedor.setTfTotalDesc(UTIL.PRECIO_CON_PUNTO.format(desc));
        if (subTotal < 0) {
            subTotal = 0.0;
        }
        contenedor.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(subTotal));
        contenedor.setTfCambio(UTIL.PRECIO_CON_PUNTO.format(subTotal));
    }

    /**
     * Calcula el margen monetario ganancia/perdida sobre el monto.
     *
     * @param monto cantidad monetaria sobre la cual se hará el cálculo
     * @param tipoDeMargen Indica como se aplicará el margen al monto. If
     * <code>(tipo > 2 || 1 > tipo)</code> will return
     * <code>null</code>. <lu> <li>1 = % (porcentaje) <li>2 = $ (monto fijo)
     * <lu>
     * @param margen monto fijo o porcentual.
     * @return El margen de ganancia correspondiente al monto.
     */
    public static Double GET_MARGEN(double monto, int tipoDeMargen, double margen) {
        if (margen == 0) {
            return 0.0;
        }
        double total = 0.0;
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
                return null;
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
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                }
            }
        }
    }

    private void checkConstraints() throws MessageException {
        if (jdFacturaVenta.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de factura no válida");
        }

        try {
            Sucursal s = getSelectedSucursalFromJDFacturaVenta();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Sucursal para poder realizar la Factura Venta."
                    + "\nMenú: Datos Generales -> Sucursales");
        }
        try {
            ListaPrecios c = (ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Lista de Precios para poder realizar la Factura Venta."
                    + "\nMenú: Productos -> Lista de Precios");
        }
        try {
            Caja c = (Caja) jdFacturaVenta.getCbCaja().getSelectedItem();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Caja para poder registrar la Factura Venta."
                    + "\nMenú: Tesorería -> ABM Cajas");
        }
        if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
            try {
                if (Short.valueOf(jdFacturaVenta.getTfDias()) < 1) {
                    throw new MessageException("Cantidad de días de Cta. Cte. no válida. Debe ser mayor a 0");
                }
            } catch (NumberFormatException e) {
                throw new MessageException("Cantidad de días de Cta. Cte. no válida");
            }
        }

        DefaultTableModel dtm = jdFacturaVenta.getDTM();
        if (dtm.getRowCount() < 1) {
            throw new MessageException("La factura debe tener al menos un item.");
        } else if (dtm.getRowCount() > LIMITE_DE_ITEMS) {
            throw new MessageException("La factura no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
        }

        if (!jdFacturaVenta.isEditMode() && remitoToFacturar != null && remitoToFacturar.getFacturaVenta() != null) {
            throw new MessageException("De alguna forma misteriosa ha seleccionado un Remito que ya fue asociado a una Factura."
                    + "\nFactura F" + JGestionUtils.getNumeracion(remitoToFacturar.getFacturaVenta()));
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
                Integer octeto = Integer.valueOf(jdFacturaVenta.getTfFacturaOcteto());
                if (octeto < 1 && octeto > 99999999) {
                    throw new MessageException("Número de factura no válido, debe ser mayor a 0 y menor o igual a 99999999");
                }
                char letra = jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0);
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
     * Set a instance of {@link FacturaVenta} from {@link FacturaVentaJpaController#jdFacturaVenta}
     * UI.
     *
     * @param facturar si es factura o comprobante interno
     * @return a instance of {@link FacturaVenta} ready to persist
     */
    private FacturaVenta getEntity(boolean facturar) {
        DefaultTableModel dtm = UTIL.getDtm(jdFacturaVenta.getjTable1());
        //set entity.fields
        FacturaVenta newFacturaVenta = new FacturaVenta();
        newFacturaVenta.setAnulada(false);
        newFacturaVenta.setFechaVenta(jdFacturaVenta.getDcFechaFactura());

        //setting entities
        newFacturaVenta.setCliente((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
        newFacturaVenta.setSucursal(getSelectedSucursalFromJDFacturaVenta());
        newFacturaVenta.setUsuario(UsuarioJpaController.getCurrentUser());
        newFacturaVenta.setCaja((Caja) jdFacturaVenta.getCbCaja().getSelectedItem());
        newFacturaVenta.setListaPrecios(selectedListaPrecios);
        newFacturaVenta.setRemito(remitoToFacturar);

        if (facturar) {
            newFacturaVenta.setMovimientoInterno(0);
            newFacturaVenta.setTipo(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
            if (unlockedNumeracion) {
                newFacturaVenta.setNumero(Integer.valueOf(jdFacturaVenta.getTfFacturaOcteto()));
            } else {
                newFacturaVenta.setNumero(jpaController.getNextNumeroFactura(newFacturaVenta.getSucursal(), newFacturaVenta.getTipo()));
            }
        } else {
            newFacturaVenta.setNumero(0);
            newFacturaVenta.setMovimientoInterno(jpaController.getNextMovimientoInterno());
            newFacturaVenta.setTipo('I');
        }

        //setting fields
        newFacturaVenta.setFormaPago(((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).getId());
        newFacturaVenta.setImporte(Double.valueOf(jdFacturaVenta.getTfTotal()));
        newFacturaVenta.setGravado(Double.valueOf(jdFacturaVenta.getTfGravado()));
        newFacturaVenta.setIva10(Double.valueOf(jdFacturaVenta.getTfTotalIVA105()));
        newFacturaVenta.setIva21(Double.valueOf(jdFacturaVenta.getTfTotalIVA21()));
        newFacturaVenta.setDescuento(Double.valueOf(jdFacturaVenta.getTfTotalDesc()));
        if (newFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
            newFacturaVenta.setDiasCtaCte(Short.parseShort(jdFacturaVenta.getTfDias()));
        } else {
            newFacturaVenta.setDiasCtaCte((short) 0);
        }
        if (!jdFacturaVenta.isEditMode()) {
            newFacturaVenta.setDetallesVentaList(new ArrayList<DetalleVenta>());
            /// carga de detalleVenta
            DetalleVenta detalleVenta;
            ProductoJpaController productoController = new ProductoJpaController();
            for (int i = 0; i < dtm.getRowCount(); i++) {
                detalleVenta = new DetalleVenta();
                detalleVenta.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
                detalleVenta.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
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
        jdFacturaVenta.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4));
        jdFacturaVenta.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(numero, 8));
    }

    // <editor-fold defaultstate="collapsed" desc="Tipos de Factura A B C M X">
    private void cargarComboTiposFacturas(Cliente cliente) {
        if (cliente != null) {
            jdFacturaVenta.getCbFacturaTipo().removeAllItems();
            if (cliente.getContribuyente().getFactuA()) {
                jdFacturaVenta.getCbFacturaTipo().addItem("A");
            }
            if (cliente.getContribuyente().getFactuB()) {
                jdFacturaVenta.getCbFacturaTipo().addItem("B");
            }
            if (cliente.getContribuyente().getFactuC()) {
                jdFacturaVenta.getCbFacturaTipo().addItem("C");
            }
            if (cliente.getContribuyente().getFactuM()) {
                jdFacturaVenta.getCbFacturaTipo().addItem("M");
            }
            if (cliente.getContribuyente().getFactuX()) {
                jdFacturaVenta.getCbFacturaTipo().addItem("X");
            }
        }
    }// </editor-fold>

    private void initBuscarProducto() throws DatabaseErrorException {
        ProductoController p = new ProductoController();
        p.initContenedor(null, true, false);
        UTIL.loadComboBox(jdFacturaVenta.getCbProductos(), p.findWrappedProductoToCombo(), false);
    }

    public void initBuscador(JFrame frame, final boolean modal, final boolean paraAnular) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(frame, "Buscador - Facturas venta", modal, "Cliente", "Nº Factura");
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"facturaID", "Nº factura", "Mov.", "Cliente", "Forma Pago", "Importe", "Fecha", "Caja", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 90, 10, 50, 40, 50, 50, 80, 50, 70},
                new Class<?>[]{null, null, Integer.class, null, null, String.class, null, null, null, null});
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        UTIL.setHorizonalAlignment(buscador.getjTable1(), String.class, SwingConstants.RIGHT);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        try {
                            EL_OBJECT = jpaController.find(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                            if (EL_OBJECT.getFormaPagoEnum() == Valores.FormaPago.CHEQUE) {
                                ChequeTerceros chequeTercerosBound = new ChequeTercerosJpaController().findChequeTerceros(EL_OBJECT);
                                EL_OBJECT.setCheque(chequeTercerosBound);
                            }
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
        JButton bImprimir = buscador.getbImprimir();
        bImprimir.setText("Editar");
        bImprimir.setIcon(new ImageIcon(getClass().getResource("/iconos/32px_configure.png")));
        bImprimir.setVisible(true);
        bImprimir.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscador.getjTable1().getSelectedRow() > -1) {
                    try {
                        EL_OBJECT = jpaController.find(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                        if (EL_OBJECT.getFormaPagoEnum() == Valores.FormaPago.CHEQUE) {
                            ChequeTerceros chequeTercerosBound = new ChequeTercerosJpaController().findChequeTerceros(EL_OBJECT);
                            EL_OBJECT.setCheque(chequeTercerosBound);
                        }
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
                        setDatosToEdit(EL_OBJECT);
                        armarQuery();
                    } catch (MessageException ex) {
                        buscador.showMessage(ex.getMessage(), "Error de datos", 0);
                        DAO.getEntityManager().clear();
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

    private void btnAceptarActionWhenEditing(boolean facturar) {
        try {
            jdFacturaVenta.getBtnFacturar().setEnabled(false);
            jdFacturaVenta.getBtnAceptar().setEnabled(false);
            setAndEdit(facturar);
            jdFacturaVenta.dispose();
        } catch (MessageException ex) {
            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
        } catch (Exception ex) {
            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
            LOG.error(ex, ex);
        } finally {
            jdFacturaVenta.getBtnFacturar().setEnabled(true);
            jdFacturaVenta.getBtnAceptar().setEnabled(true);
        }
    }

    private void setDatosToEdit(FacturaVenta selectedFacturaVenta) throws MessageException {
        try {
            if (jdFacturaVenta == null || !jdFacturaVenta.isEditMode()) {
                initFacturaVenta(null, true, this, 1, false, true);
                jdFacturaVenta.setModoEdicion();
                jdFacturaVenta.hidePanelCambio();
                jdFacturaVenta.addActionAndKeyListener(this);
                for (ActionListener actionListener : jdFacturaVenta.getBtnAceptar().getActionListeners()) {
                    jdFacturaVenta.getBtnAceptar().removeActionListener(actionListener);
                }
                for (ActionListener actionListener : jdFacturaVenta.getBtnFacturar().getActionListeners()) {
                    jdFacturaVenta.getBtnAceptar().removeActionListener(actionListener);
                }
                jdFacturaVenta.getBtnAceptar().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btnAceptarActionWhenEditing(false);
                    }
                });
                jdFacturaVenta.getBtnFacturar().addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btnAceptarActionWhenEditing(true);
                    }
                });
            } else {
                LOG.info("jdFacturaVenta already initialized and setted ON editing MODE");
            }
        } catch (MessageException ex) {
            jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
        }
        // seteando datos de Factura
        jdFacturaVenta.setDcFechaFactura(selectedFacturaVenta.getFechaVenta());
        UTIL.setSelectedItem(jdFacturaVenta.getCbCliente(), selectedFacturaVenta.getCliente());
        cargarComboTiposFacturas(selectedFacturaVenta.getCliente());
        if (selectedFacturaVenta.getRemito() != null) {
            jdFacturaVenta.setTfRemito(JGestionUtils.getNumeracion(selectedFacturaVenta.getRemito(), false));
        }

        //Los tipos de factura se tienen q cargar antes, sinó modifica el Nº de factura y muestra el siguiente
        //y no el de Factura seleccionada
        Sucursal s = selectedFacturaVenta.getSucursal();
        UTIL.setSelectedItem(jdFacturaVenta.getCbSucursal(), new ComboBoxWrapper<Sucursal>(s, s.getId(), s.getNombre()));
        UTIL.setSelectedItem(jdFacturaVenta.getCbListaPrecio(), selectedFacturaVenta.getListaPrecios());
        UTIL.setSelectedItem(jdFacturaVenta.getCbCaja(), selectedFacturaVenta.getCaja());
        UTIL.setSelectedItem(jdFacturaVenta.getCbFormaPago(), selectedFacturaVenta.getFormaPagoEnum().toString());
        jdFacturaVenta.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(selectedFacturaVenta.getSucursal().getPuntoVenta(), 4));
        jdFacturaVenta.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(selectedFacturaVenta.getNumero(), 8));
        jdFacturaVenta.setTfNumMovimiento(String.valueOf(selectedFacturaVenta.getMovimientoInterno()));
        if (selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CHEQUE
                || selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CONTADO_CHEQUE) {
            jdFacturaVenta.getLabelDias().setText("N°");
            jdFacturaVenta.setTfDias(selectedFacturaVenta.getCheque().getNumero().toString());
        } else if (selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CTA_CTE) {
            if (selectedFacturaVenta.getDiasCtaCte() != null) {
                jdFacturaVenta.setTfDias(selectedFacturaVenta.getDiasCtaCte().toString());
            }
        }
        // <editor-fold defaultstate="collapsed" desc="Carga de DetallesVenta">
        
        List<DetalleVenta> lista = selectedFacturaVenta.getDetallesVentaList();
        DefaultTableModel dtm = (DefaultTableModel) jdFacturaVenta.getjTable1().getModel();
        dtm.setRowCount(0);
        for (DetalleVenta detallesVenta : lista) {
            Iva iva = detallesVenta.getProducto().getIva();
            if (iva == null) {
                iva = new IvaJpaController().findByProducto(detallesVenta.getProducto().getId());
//                Producto findProducto = (Producto) DAO.findEntity(Producto.class, detallesVenta.getProducto().getId());
//                iva = findProducto.getIva();
                System.out.println("IVAAAAAA=" + iva);
                if (iva == null || iva.getIva() == null) {
                    throw new MessageException("No se pudo recuperar toda la información relacionada al produto:"
//                            + "\nCódigo:" + findProducto.getCodigo()
//                            + "\nNombre:" + findProducto.getNombre()
                            + "\nIva: " + iva);
                }
            }
            double productoConIVA = detallesVenta.getPrecioUnitario()
                    + UTIL.getPorcentaje(detallesVenta.getPrecioUnitario(), iva.getIva());
            //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
            LOG.debug(detallesVenta.toString());
            try {
                dtm.addRow(new Object[]{
                            null,
                            detallesVenta.getProducto().getCodigo(),
                            detallesVenta.getProducto().getNombre() + "(" + detallesVenta.getProducto().getIva().getIva() + ")",
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
        //totales
        jdFacturaVenta.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getImporte() - (selectedFacturaVenta.getIva10() + selectedFacturaVenta.getIva21())));
        jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getIva10()));
        jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getIva21()));
        jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getImporte()));
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setVisible(true);
    }

    private void cargarTablaBuscador(String query) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        List<FacturaVenta> l = jpaController.findByNativeQuery(query);
        for (FacturaVenta facturaVenta : l) {
            dtm.addRow(new Object[]{
                        facturaVenta.getId(), // <--- no es visible
                        JGestionUtils.getNumeracion(facturaVenta),
                        facturaVenta.getMovimientoInterno(),
                        facturaVenta.getCliente().getNombre(),
                        facturaVenta.getFormaPagoEnum(),
                        UTIL.PRECIO_CON_PUNTO.format(facturaVenta.getImporte()),
                        UTIL.DATE_FORMAT.format(facturaVenta.getFechaVenta()),
                        facturaVenta.getCaja().getNombre(),
                        facturaVenta.getUsuario().getNick(),
                        UTIL.TIMESTAMP_FORMAT.format(facturaVenta.getFechaalta())
                    });
        }
    }

    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM factura_venta o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfFactu8().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu8());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
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
        cargarTablaBuscador(query.toString());
    }

    /**
     * Setea los datos de
     * <code>FacturaVenta</code> en la instancia de
     * {@link JDFacturaVenta} del controlador.
     *
     * @param selectedFacturaVenta
     * @param paraAnular
     */
    private void setDatosToAnular(FacturaVenta selectedFacturaVenta, final boolean paraAnular) throws MessageException {
        try {
            initFacturaVenta(null, true, this, 1, false, false);
            jdFacturaVenta.getBtnAceptar().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        jdFacturaVenta.getBtnFacturar().setEnabled(false);
                        jdFacturaVenta.getBtnAceptar().setEnabled(false);
                        setAndPersist(false);
                    } catch (MessageException ex) {
                        jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        Logger.getLogger(SucursalController.class.getName()).log(Level.ERROR, null, ex);
                    } finally {
                        jdFacturaVenta.getBtnFacturar().setEnabled(true);
                        jdFacturaVenta.getBtnAceptar().setEnabled(true);
                    }
                }
            });
            jdFacturaVenta.getBtnFacturar().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        jdFacturaVenta.getBtnFacturar().setEnabled(false);
                        jdFacturaVenta.getBtnAceptar().setEnabled(false);
                        setAndPersist(true);
                    } catch (MessageException ex) {
                        jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    } catch (Exception ex) {
                        jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                        Logger.getLogger(SucursalController.class.getName()).log(Level.ERROR, null, ex);
                    } finally {
                        jdFacturaVenta.getBtnFacturar().setEnabled(true);
                        jdFacturaVenta.getBtnAceptar().setEnabled(true);
                    }
                }
            });
        } catch (MessageException ex) {
            Logger.getLogger(this.getClass()).log(Level.ERROR, null, ex);
        }
        // seteando datos de FacturaCompra
        jdFacturaVenta.getCbCliente().addItem(selectedFacturaVenta.getCliente());
        Sucursal s = selectedFacturaVenta.getSucursal();
        ComboBoxWrapper<Sucursal> cbw = new ComboBoxWrapper<Sucursal>(s, s.getId(), s.getNombre());
        jdFacturaVenta.getCbSucursal().addItem(cbw);
        jdFacturaVenta.getCbListaPrecio().addItem(selectedFacturaVenta.getListaPrecios());
        jdFacturaVenta.setDcFechaFactura(selectedFacturaVenta.getFechaVenta());
        if (selectedFacturaVenta.getRemito() != null) {
            jdFacturaVenta.setTfRemito(JGestionUtils.getNumeracion(selectedFacturaVenta.getRemito(), false));
        }

        //Los tipos de factura se tienen q cargar antes, sinó modifica el Nº de factura y muestra el siguiente
        //y no el de Factura seleccionada
        cargarComboTiposFacturas(selectedFacturaVenta.getCliente());
        jdFacturaVenta.setTfFacturaCuarto(UTIL.AGREGAR_CEROS(selectedFacturaVenta.getSucursal().getPuntoVenta(), 4));
        jdFacturaVenta.setTfFacturaOcteto(UTIL.AGREGAR_CEROS(selectedFacturaVenta.getNumero(), 8));
        jdFacturaVenta.setTfNumMovimiento(String.valueOf(selectedFacturaVenta.getMovimientoInterno()));
        jdFacturaVenta.getCbCaja().addItem(selectedFacturaVenta.getCaja());
        UTIL.loadComboBox(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
        UTIL.setSelectedItem(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormaPago(selectedFacturaVenta.getFormaPago()));
        if (selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CHEQUE
                || selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CONTADO_CHEQUE) {
            jdFacturaVenta.getLabelDias().setText("N°");
            jdFacturaVenta.setTfDias(selectedFacturaVenta.getCheque().getNumero().toString());
        } else if (selectedFacturaVenta.getFormaPagoEnum() == Valores.FormaPago.CTA_CTE) {
            if (selectedFacturaVenta.getDiasCtaCte() != null) {
                jdFacturaVenta.setTfDias(selectedFacturaVenta.getDiasCtaCte().toString());
            }
        }
        // <editor-fold defaultstate="collapsed" desc="Carga de DetallesVenta">
        List<DetalleVenta> lista = selectedFacturaVenta.getDetallesVentaList();
        DefaultTableModel dtm = (DefaultTableModel) jdFacturaVenta.getjTable1().getModel();
        for (DetalleVenta detallesVenta : lista) {
            Iva iva = detallesVenta.getProducto().getIva();
            if (iva == null) {
                Producto findProducto = (Producto) DAO.findEntity(Producto.class, detallesVenta.getProducto().getId());
                iva = findProducto.getIva();
                if (iva == null || iva.getIva() == null) {
                    throw new MessageException("No se pudo recuperar toda la información relacionada al produto:"
                            + "\nCódigo:" + findProducto.getCodigo()
                            + "\nNombre:" + findProducto.getNombre()
                            + "\nIva: " + iva.getId());
                }
            }
            double productoConIVA = detallesVenta.getPrecioUnitario()
                    + UTIL.getPorcentaje(detallesVenta.getPrecioUnitario(), iva.getIva());
            //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
            Logger.getLogger(this.getClass()).debug(detallesVenta);
            dtm.addRow(new Object[]{
                        null,
                        detallesVenta.getProducto().getCodigo(),
                        detallesVenta.getProducto().getNombre() + "(" + detallesVenta.getProducto().getIva().getIva() + ")",
                        detallesVenta.getCantidad(),
                        detallesVenta.getPrecioUnitario(),
                        UTIL.PRECIO_CON_PUNTO.format(productoConIVA),
                        detallesVenta.getDescuento(),
                        UTIL.PRECIO_CON_PUNTO.format((detallesVenta.getCantidad() * productoConIVA) - detallesVenta.getDescuento()),
                        null,
                        detallesVenta.getProducto().getId(),
                        null
                    });
        }// </editor-fold>

        //totales
        jdFacturaVenta.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getImporte() - (selectedFacturaVenta.getIva10() + selectedFacturaVenta.getIva21())));
        jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getIva10()));
        jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getIva21()));
        jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getImporte()));
        jdFacturaVenta.modoVista();

        //viendo si se habilita el botón FACTURAR
        if ((!selectedFacturaVenta.getAnulada())
                && selectedFacturaVenta.getNumero() == 0
                && selectedFacturaVenta.getTipo() == 'I') {
            //si NO está anulada
            //es decir que es un movimiento interno, y puede ser cambiado a FACTURA VENTA
            jdFacturaVenta.getBtnFacturar().setEnabled(true);
        }

        if (paraAnular) {
            jdFacturaVenta.hidePanelCambio();
            jdFacturaVenta.getBtnCancelar().setVisible(false);
            jdFacturaVenta.getBtnAceptar().setVisible(false);
            jdFacturaVenta.getBtnFacturar().setVisible(false);
            jdFacturaVenta.getBtnAnular().setVisible(paraAnular);
            jdFacturaVenta.getCheckFacturacionElectronica().setVisible(false);
        }
        jdFacturaVenta.addActionAndKeyListener(this);
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setVisible(true);
    }

    private void anular(FacturaVenta factura) throws MessageException {
        Caja oldCaja = factura.getCaja();
        List<Caja> cajasPermitidasList = new CajaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true);
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
            JOptionPane.showMessageDialog(jdFacturaVenta, ex.getMessage());
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
        JDABM abm = new JDABM(true, null, panel);
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
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_FacturaVenta_I.jasper", "Comprobante venta");
        r.addEntidad();
        r.addLogo();
        r.addParameter("FACTURA_ID", facturaVenta.getId());
        if (facturaVenta.getCheque() != null) {
            r.addParameter("CHEQUE", UTIL.AGREGAR_CEROS(facturaVenta.getCheque().getNumero(), 12));
        }
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
        facturaVenta.setTipo(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
        facturaVenta.setNumero(jpaController.getNextNumeroFactura(s, facturaVenta.getTipo()));
        facturaVenta.setMovimientoInterno(0);
        DAO.doMerge(facturaVenta);
        if (facturaVenta.getFormaPago() == Valores.FormaPago.CONTADO.getId()) {
            //si NO es al CONTADO no tiene un registro de DetalleCajaMovimiento
            new CajaMovimientosController().actualizarDescripcion(facturaVenta);
        }
        imprimirFactura(facturaVenta);
        jdFacturaVenta.getBtnFacturar().setEnabled(false);
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
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta, sb.toString(), "Confirmación de modificaciónes", JOptionPane.YES_NO_OPTION)) {
                String mensaje = edit(cambiaCaja, cambiaFormaPago, editedFacturaVenta);
                if (facturar) {
                    imprimirFactura(editedFacturaVenta);
                } else if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta,
                        "¿Imprimir comprobante?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
                    imprimirMovimientoInterno(editedFacturaVenta);
                }
                jdFacturaVenta.showMessage("Modificaciones realizadas.\n" + mensaje, null, JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            throw new MessageException("No han realizado modificaciones en el comprobante");
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
        } else if (EL_OBJECT.getFormaPagoEnum().equals(Valores.FormaPago.CHEQUE)) {
            throw new MessageException("Modificación de Forma de pago con CHEQUE no soportadas aún");
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
            ChequeTerceros cheque = null;
            if (newFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CHEQUE)
                    || newFacturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CONTADO_CHEQUE)) {
                //entity Cheque persisted..
                cheque = getChequeToBind(newFacturaVenta);
            }
            jpaController.create(newFacturaVenta);
            //refreshing the entity from DB
            newFacturaVenta = (FacturaVenta) DAO.findEntity(FacturaVenta.class, newFacturaVenta.getId());
            if (cheque != null) {
                cheque.setBoundId(newFacturaVenta.getId().longValue());
                new ChequeTercerosJpaController().edit(cheque);
                newFacturaVenta.setCheque(cheque);
            }
            if (newFacturaVenta.getRemito() != null) {
                remitoToFacturar.setFacturaVenta(newFacturaVenta);
                new RemitoController().edit(remitoToFacturar);
            }

            //actualiza Stock
            new StockJpaController().updateStock(newFacturaVenta);
            //asiento en caja..
            registrarVentaSegunFormaDePago(newFacturaVenta);
            if (conFactura) {
                if (jdFacturaVenta.getCheckFacturacionElectronica().isSelected()) {
                    AFIPWSController afipWS = new AFIPWSController();
                    afipWS.showSetting(newFacturaVenta);
                }
                imprimirFactura(newFacturaVenta);
            } else {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta,
                        "¿Imprimir comprobante?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
                    imprimirMovimientoInterno(newFacturaVenta);
                }
            }
            limpiarPanel();
        } else {
            if (conFactura) {
                if (EL_OBJECT.getTipo() == 'I') {
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta,
                            "¿Cambiar factura Movimiento interno Nº" + EL_OBJECT.getMovimientoInterno()
                            + " por Factura Venta \"" + jdFacturaVenta.getCbFacturaTipo().getSelectedItem() + "\" Nº"
                            + getNextNumeroFacturaConGuion(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)) + "?", "Facturación - Venta", JOptionPane.OK_CANCEL_OPTION)) {
                        cambiarMovimientoInternoToFactura(EL_OBJECT);
                    }
                } else {
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta,
                            "La Factura Nº" + EL_OBJECT.getNumero()
                            + " ya fue impresa.\n¿Volver a imprimir?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
                        imprimirFactura(EL_OBJECT);
                    }
                }
            } else {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(jdFacturaVenta,
                        "¿Re-Imprimir comprobante?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
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
        jdFacturaVenta.setTfRemito("Sin Remito");
        remitoToFacturar = null;
        if (jdFacturaVenta.getModoUso() == 1) {
            jdFacturaVenta.setTfNumMovimiento(jpaController.getNextMovimientoInterno().toString());
            Sucursal s = getSelectedSucursalFromJDFacturaVenta();
            setNumeroFactura(s, jpaController.getNextNumeroFactura(s, jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
        }
    }

    /**
     * Limpiar la dtm de detalles de venta. refresca los totales (gravado,
     * IVA's, ...) selectedProducto = null y la info sobre este
     */
    void borrarDetalles() {
        UTIL.limpiarDtm(jdFacturaVenta.getDTM());
        refreshResumen(jdFacturaVenta);
        selectedProducto = null;
        setInformacionDeProducto(jdFacturaVenta, selectedProducto);
        jdFacturaVenta.setLabelCodigoNoRegistradoVisible(false);
        jdFacturaVenta.getCbCliente().requestFocus();
    }

    JDFacturaVenta getContenedor() {
        return jdFacturaVenta;
    }

    private void initBuscadorRemito(Cliente clienteSeleccionado) {
        RemitoController remitoController = new RemitoController();
        remitoController.initBuscadorToFacturar(jdFacturaVenta, clienteSeleccionado);
        remitoToFacturar = remitoController.getSelectedRemito();
        if (remitoToFacturar != null) {
            jdFacturaVenta.setTfRemito(JGestionUtils.getNumeracion(remitoToFacturar, false));
            if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(jdFacturaVenta, "Desea cargar el detalle del remito a la Factura?",
                    "Volcar detalle", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                cargarRemitoToDetallesFactura(remitoToFacturar);
            }
        } else {
            jdFacturaVenta.setTfRemito("Sin Remito");
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
            jdFacturaVenta.setTfCantidad(cantidad.toString());
            setInformacionDeProducto(jdFacturaVenta, selectedProducto);
            try {
                addProductoToDetails();
            } catch (MessageException ex) {
                jdFacturaVenta.showMessage(ex.getMessage(), "DUPLICADO", 2);
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
            jdFacturaVenta.setVisibleEstrellita(Boolean.FALSE);
        } else {
            ListaPrecios toCatalogoWeb = new ListaPreciosJpaController().findListaPreciosParaCatalogo();
            if (toCatalogoWeb == null) {
                jdFacturaVenta.setVisibleEstrellita(Boolean.FALSE);
            } else {
                jdFacturaVenta.setVisibleEstrellita(toCatalogoWeb.equals(listaPrecios));
            }
        }
    }

    /**
     * Levanta la GUI para la creación de un ChequeTercero ligado a la factura
     *
     * @param newFacturaVenta
     * @return
     * @throws MessageException
     */
    private ChequeTerceros getChequeToBind(FacturaVenta newFacturaVenta) throws MessageException {
        ChequeTercerosJpaController ch = new ChequeTercerosJpaController();
        ch.getABMCheque(newFacturaVenta).setVisible(true);
        ChequeTerceros cheque = ch.getChequeTerceroInstance();
        if (cheque == null) {
            throw new MessageException("Creación de cheque cancelada.");
        }
        cheque.setBound(DetalleCajaMovimientosJpaController.FACTU_VENTA);
        return cheque;
    }

    Sucursal getSelectedSucursalFromJDFacturaVenta() {
        @SuppressWarnings("unchecked")
        ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) jdFacturaVenta.getCbSucursal().getSelectedItem();
        return cbw.getEntity();
    }

    public void unlockedABM(JFrame owner) throws MessageException {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA_NUMERACION_MANUAL);
        unlockedNumeracion = true;
        initFacturaVenta(owner, true, this, 1, false, true);
        jdFacturaVenta.setTfFacturaOcteto(null);
        jdFacturaVenta.getBtnAceptar().setEnabled(false);
        jdFacturaVenta.setNumeroFacturaEditable(true);
        jdFacturaVenta.setLocationRelativeTo(owner);
        jdFacturaVenta.setVisible(true);


    }

    private FacturaVenta findBySucursal(char tipo, Sucursal sucursal, Integer numero) {
        return jpaController.find(tipo, sucursal, numero);

    }
}
