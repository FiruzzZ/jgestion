package controller;

import generics.UTIL;
import controller.exceptions.*;
import entity.*;
import java.awt.event.FocusEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import gui.JDABM;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import gui.PanelReasignacionDeCaja;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import generics.AutoCompleteComboBox;
import java.awt.event.FocusAdapter;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Clase (Ventana) usada para crear FacturaVenta, Remitos, Presupuestos
 * @author FiruzzZ
 */
public class FacturaVentaJpaController implements ActionListener, KeyListener {

   public static final String CLASS_NAME = FacturaVenta.class.getSimpleName();
   private JDFacturaVenta jdFacturaVenta;
   private static final String[] columnNames = {"IVA", "Cód. Producto", "Producto (IVA)", "Cantidad", "Precio U.", "Precio con IVA", "Desc", "Sub total", "TipoDescuento", "Producto.id", "HistorialOfertas.id"};
   private static final int[] columnWidths = {1, 70, 180, 10, 30, 30, 30, 30, 1, 1, 1};
   private static final Class[] columnClasses = {Object.class, Object.class, Object.class, String.class, String.class, String.class, String.class, String.class, Object.class, Object.class, Object.class};
   private FacturaVenta EL_OBJECT;
   /**
    * Para cuando se está usando {@link FacturaVentaJpaController#jdFacturaVenta}
    * Lleva el ctrl para saber cuando se seleccionó un Producto.
    */
   private Producto selectedProducto;
   private ListaPrecios selectedListaPrecios;
   private JDBuscadorReRe buscador;
   /**
    * Cantidad de item que puede contenedor el detalle.
    * Limitado por el tamaño del reporte (la factura pre-impresa)
    */
   public static final int LIMITE_DE_ITEMS = 14;
   private boolean MODO_VISTA = false;
   private Remito remitoToFacturar;
   /**
    * Solo usada para reasignación de Caja, cuando se anula una Factura.
    */
   private CajaMovimientos cajaMovToAsentarAnulacion;
   private HistorialOfertas productoEnOferta;

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   private void create(FacturaVenta facturaVenta) throws Exception {
      EntityManager em = null;
      try {
         em = DAO.getEntityManager();
         em.getTransaction().begin();
         List<DetalleVenta> detallesVentaListToPersist = facturaVenta.getDetallesVentaList();
         facturaVenta.setDetallesVentaList(new ArrayList<DetalleVenta>());
         em.persist(facturaVenta);
         em.getTransaction().commit();
         for (DetalleVenta detallesVenta : detallesVentaListToPersist) {
            detallesVenta.setFactura(facturaVenta);
            new DetalleVentaJpaController().create(detallesVenta);
         }
      } catch (Exception ex) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         throw ex;
      } finally {
         if (em != null) {
            if (em.isOpen()) {
               em.close();
            }
         }
      }
   }

   private void edit(FacturaVenta facturaVenta) throws NonexistentEntityException, Exception {
   }

   private void destroy(Integer id) throws NonexistentEntityException {
   }

   public List<FacturaVenta> findFacturaVentaEntities() {
      return findFacturaVentaEntities(true, -1, -1);
   }

   public List<FacturaVenta> findFacturaVentaEntities(int maxResults, int firstResult) {
      return findFacturaVentaEntities(false, maxResults, firstResult);
   }

   private List<FacturaVenta> findFacturaVentaEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from FacturaVenta as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public FacturaVenta findFacturaVenta(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return (FacturaVenta) DAO.findEntity(FacturaVenta.class, id);
      } finally {
         em.close();
      }
   }

   public int getFacturaVentaCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from " + CLASS_NAME + " as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   /**
    * Inicializa la GUI de Ventas {@link gui.JDFacturaVenta},
    * está misma es usada para realizar FacturasVenta, Presupestos y Remitos.
    * @param frame Papi de {@link gui.JDFacturaVenta}
    * @param modal bla bla...
    * @param listener Object encargado de manejar los Eventos de la GUI (Action, Mouse, Key, Focus)
    * @param factVenta1_Presup2_Remito3 Es para settear algunos Labels, TextFields
    * según la entidad que va usar la GUI.
    * @param setVisible Si la GUI debe hacerse visible cuando se invoca este método.
    * Se pone <code>false</code> cuando se va usar en MODO_VISTA, así 1ro se
    * settean los datos correspondiendtes a la entidad que la va utilizar y
    * luego se puede hacer visible.
    * @param loadDefaultData determina si se cargan los comboBox (listaprecios, cajas, clientes, productos, sucursales)
    * @throws MessageException Mensajes personalizados de alerta y/o información
    * para el usuario.
    */
   public void initFacturaVenta(JFrame frame, boolean modal, Object listener, int factVenta1_Presup2_Remito3, boolean setVisible, boolean loadDefaultData) throws MessageException {
      UsuarioJpaController.CHECK_PERMISO(PermisosJpaController.PermisoDe.VENTA);
      jdFacturaVenta = new JDFacturaVenta(frame, modal, factVenta1_Presup2_Remito3);
      UTIL.getDefaultTableModel(jdFacturaVenta.getjTable1(),
              columnNames,
              columnWidths,
              columnClasses);
      DefaultTableCellRenderer defaultTableCellRender = new DefaultTableCellRenderer();
      defaultTableCellRender.setHorizontalAlignment(JLabel.RIGHT);
      jdFacturaVenta.getjTable1().setDefaultRenderer(String.class, defaultTableCellRender);
      //esconde de la vista del usuario la columna IVA y Tipo de Descuento, Producto.id, HistorialOferta.id
      UTIL.hideColumnsTable(jdFacturaVenta.getjTable1(), new int[]{0, 8, 9, 10});
      jdFacturaVenta.setTfRemito("Sin Remito");
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
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.ERROR, null, ex);
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
            ClienteJpaController ctrl = new ClienteJpaController();
            ctrl.initContenedor(null, true);
            UTIL.loadComboBox(jdFacturaVenta.getCbCliente(), ctrl.findClienteEntities(), false);
         }
      });
      jdFacturaVenta.getBtnCancelar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            cancelarBtnAction();
         }
      });
      jdFacturaVenta.getTfProductoCodigo().addKeyListener(new KeyAdapter() {

         @Override
         public void keyReleased(KeyEvent e) {
            if (jdFacturaVenta.getTfProductoCodigo().getText().trim().length() > 0 && e.getKeyCode() == 10) {
               buscarProducto(jdFacturaVenta.getTfProductoCodigo().getText().trim());
            }
         }
      });
      jdFacturaVenta.getTfCambio().addKeyListener(new KeyAdapter() {

         @Override
         public void keyReleased(KeyEvent e) {
            if (jdFacturaVenta.getTfCambio().getText().length() > 0 && e.getKeyCode() == 10) {
               jdFacturaVenta.setCambio();
            }
         }
      });

      //porque cuando es FALSE, se va usar en MODO_VISTA (Factura, Remito o Presupuesto, NotaCredito)
      //por lo tanto no es necesario cargar todos los combos
      if (loadDefaultData) {
         // <editor-fold defaultstate="collapsed" desc="AutoCompleteComboBox">
         UTIL.loadComboBox(jdFacturaVenta.getCbProductos(), new ProductoJpaController().findProductoToCombo(), false);
         // must be editable!!!.................
         jdFacturaVenta.getCbProductos().setEditable(true);
         JTextComponent editor = (JTextComponent) jdFacturaVenta.getCbProductos().getEditor().getEditorComponent();
         editor.setDocument(new AutoCompleteComboBox(jdFacturaVenta.getCbProductos()));
         editor.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
               try {
                  String codigoProducto = ((Producto) jdFacturaVenta.getCbProductos().getSelectedItem()).getCodigo();
                  selectedProducto = new ProductoJpaController().findProductoByCodigo(codigoProducto);
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
               if (e.getKeyCode() == 10) {
                  try {
                     String codigoProducto = ((Producto) jdFacturaVenta.getCbProductos().getSelectedItem()).getCodigo();
                     selectedProducto = new ProductoJpaController().findProductoByCodigo(codigoProducto);
                     setInformacionDeProducto(jdFacturaVenta, selectedProducto);
                  } catch (ClassCastException ex) {
                     //imposible que pase esto.. cierto?
                  }
               }
            }
         });// </editor-fold>
         jdFacturaVenta.setDcFechaFactura(new Date());
         UTIL.loadComboBox(jdFacturaVenta.getCbCliente(), new ClienteJpaController().findClienteEntities(), false);
         UTIL.loadComboBox(jdFacturaVenta.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), false);
         UTIL.loadComboBox(jdFacturaVenta.getCbListaPrecio(), new ListaPreciosJpaController().findListaPreciosEntities(), false);
         UTIL.loadComboBox(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
         jdFacturaVenta.getCbListaPrecio().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               if (!selectedListaPrecios.equals((ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem())) {
                  borrarDetalles();
                  selectedListaPrecios = (ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem();
                  setIconoListaPrecios(selectedListaPrecios);
               }
            }
         });
         if (factVenta1_Presup2_Remito3 == 1) {
            try {
               cargarComboTiposFacturas((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
            } catch (ClassCastException ex) {
               throw new MessageException("Debe crear un Cliente para poder realizar una Facturas venta, Presupuestos o Remitos.");
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
                     setNumeroFactura(getNextNumeroFactura(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
                  }
               }
            });
            jdFacturaVenta.getBtnAceptar().addActionListener(new ActionListener() {

               @Override
               public void actionPerformed(ActionEvent e) {
                  try {
                     jdFacturaVenta.getBtnFacturar().setEnabled(false);
                     jdFacturaVenta.getBtnAceptar().setEnabled(false);
                     doMovimientoInterno();
                  } catch (MessageException ex) {
                     jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                  } catch (Exception ex) {
                     jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                     Logger.getLogger(SucursalJpaController.class.getName()).log(Level.ERROR, null, ex);
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
                     facturar();
                  } catch (MessageException ex) {
                     jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
                  } catch (Exception ex) {
                     jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                     Logger.getLogger(SucursalJpaController.class.getName()).log(Level.ERROR, null, ex);
                  } finally {
                     jdFacturaVenta.getBtnFacturar().setEnabled(true);
                     jdFacturaVenta.getBtnAceptar().setEnabled(true);
                  }
               }
            });
            //hasta que no elija el TIPO A,B,C no se sabe el nº
            jdFacturaVenta.setTfFacturaCuarto("");
            jdFacturaVenta.setTfFacturaOcteto("");
            UTIL.loadComboBox(jdFacturaVenta.getCbCaja(), new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);
            jdFacturaVenta.setTfNumMovimiento(getNextMovimientoInterno().toString());
            setNumeroFactura(getNextNumeroFactura(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
         } else if (factVenta1_Presup2_Remito3 == 2) {
            // y aca?...
         } else if (factVenta1_Presup2_Remito3 == 3) {
            jdFacturaVenta.getLabelNumMovimiento().setVisible(false);
            jdFacturaVenta.getTfNumMovimiento().setVisible(false);
            setNumeroFactura(((RemitoJpaController) listener).getNextNumero());
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
                  if (0 == JOptionPane.showOptionDialog(jdFacturaVenta, "- " + factu_compro + " Nº:" + UTIL.AGREGAR_CEROS(EL_OBJECT.getNumero() + "\n- Movimiento de Caja\n- Movimiento de Stock" + msg_extra_para_ctacte, 12), "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null)) {
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
      selectedProducto = new ProductoJpaController().findProductoByCodigo(codigoProducto);
      setInformacionDeProducto(jdFacturaVenta, selectedProducto);
   }

   private void addProductoToDetails(ListaPrecios lp, Producto producto) throws MessageException {
      selectedListaPrecios = lp;
      selectedProducto = producto;
      addProductoToDetails();
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
                    + " ser menor al mínimo establecido ($" + selectedProducto.getMinimoPrecioDeVenta() + ")"
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
         descuentoUnitario = GET_MARGEN(precioUnitario - selectedProducto.getMinimoPrecioDeVenta(),
                 jdFacturaVenta.getCbDesc().getSelectedIndex() + 1,
                 descuentoUnitario);

         if (!isPrecioVentaMinimoValido((precioUnitario - descuentoUnitario))) {
            throw new MessageException("El descuento deseado produce un precio de venta ($" + UTIL.PRECIO_CON_PUNTO.format(precioUnitario - descuentoUnitario) + ")"
                    + "\nmenor al mínimo establecido ($" + selectedProducto.getMinimoPrecioDeVenta() + ")"
                    + "\nPara poder realizar este descuento, debe cambiar este mínimo de venta ajustando el \"precio de venta\" del producto."
                    + "\n(Menú -> Productos -> ABM Productos -> Modificar)");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Descuento no válido");
      }// </editor-fold>

      //adiciona el descuento
      precioUnitario -= descuentoUnitario;
      double unitarioConIva;
      double iva = 0.0;

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
      org.apache.log4j.Logger.getLogger(FacturaVentaJpaController.class).log(org.apache.log4j.Level.TRACE,
              "precioU=" + precioUnitario + ", IVA=" + iva + ", descuento=" + descuentoUnitario + ", conIVA=" + unitarioConIva);
      //carga detallesVenta en tabla
      jdFacturaVenta.getDTM().addRow(new Object[]{
                 selectedProducto.getIva().toString(),
                 selectedProducto.getCodigo(),
                 selectedProducto.getNombre() + "(" + selectedProducto.getIva().toString() + ")",
                 cantidad,
                 UTIL.PRECIO_CON_PUNTO.format(precioUnitario),
                 UTIL.PRECIO_CON_PUNTO.format(unitarioConIva),
                 UTIL.PRECIO_CON_PUNTO.format(descuentoUnitario * cantidad),
                 UTIL.PRECIO_CON_PUNTO.format(((cantidad * unitarioConIva)) > 0 ? (cantidad * unitarioConIva) : 0.0), //subTotal
                 (descuentoUnitario == 0) ? -1 : (jdFacturaVenta.getCbDesc().getSelectedIndex() + 1),//Tipo de descuento
                 selectedProducto.getId(),
                 productoEnOferta
              });
      refreshResumen(jdFacturaVenta);
   }

   void deleteProductoFromLista(gui.JDFacturaVenta contenedor) {
      if (contenedor.getjTable1().getSelectedRow() >= 0) {
         contenedor.getDTM().removeRow(contenedor.getjTable1().getSelectedRow());
         refreshResumen(contenedor);
      }
   }

   /**
    * Setea la info del Producto en la instancia de {@link gui.JDFacturaVenta}
    * @param contenedor instancia de <code>gui.JDFacturaVenta</code>
    * @param selectedProducto entity Producto
    */
   private void setInformacionDeProducto(JDFacturaVenta contenedor, Producto selectedProducto) {
      if (selectedProducto != null) {
         contenedor.setLabelCodigoNoRegistradoVisible(false);
         contenedor.setTfProductoCodigo(selectedProducto.getCodigo());
         UTIL.setSelectedItem(contenedor.getCbProductos(), selectedProducto.getNombre());
         contenedor.setTfProductoIVA(selectedProducto.getIva().getIva().toString());
         Double precioUnitario = selectedProducto.getMinimoPrecioDeVenta();
         //buscamos si el producto está en oferta
         productoEnOferta = new HistorialOfertasJpaController().findOfertaVigente(selectedProducto);
         Logger.getLogger(CLASS_NAME).log(Level.DEBUG, "productoEnOferta.id=" + (productoEnOferta != null ? productoEnOferta.getId() : null));
         ListaPrecios listaPreciosParaCatalogo = new ListaPreciosJpaController().findListaPreciosParaCatalogo();
         //Cuando el producto NO está en oferta o cuando NO hay lista designada para CatalogoWeb
         if (productoEnOferta == null || listaPreciosParaCatalogo == null) {
            //agrega el margen de ganancia según la ListaPrecio
            precioUnitario += GET_MARGEN_SEGUN_LISTAPRECIOS(selectedListaPrecios, selectedProducto, null);
            contenedor.setTfPrecioUnitario(UTIL.PRECIO_CON_PUNTO.format(precioUnitario));
         } else {
            Logger.getLogger(CLASS_NAME).log(Level.DEBUG, "¡¡¡Producto ES OFERTA!!!");
            if (listaPreciosParaCatalogo.equals(selectedListaPrecios)) {
               Logger.getLogger(CLASS_NAME).log(Level.DEBUG, "¡¡¡LISTA PRECIOS [CW]!!!");
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
    * Actualiza los campos de montos de <code>gui.JD.FacturaVenta</code> según
    * los items en el detalle.
    * Gravado (SubTotal), Iva10, Iva21, Descuentos, Total y Cambio
    * @param contenedor
    */
   void refreshResumen(gui.JDFacturaVenta contenedor) {
      Double gravado = 0.0;
      Double iva10 = 0.0;
      Double iva21 = 0.0;
      Double desc = 0.0;
      Double subTotal = 0.0;
      DefaultTableModel dtm = contenedor.getDTM();
      for (int index = 0; index < dtm.getRowCount(); index++) {
         double cantidad = Double.valueOf(dtm.getValueAt(index, 3).toString());

         // precioSinIVA + cantidad
         gravado += (cantidad * Double.valueOf(dtm.getValueAt(index, 4).toString()));

         // IVA's ++
         if (dtm.getValueAt(index, 0).toString().equalsIgnoreCase("10.5")) {
            iva10 += cantidad * UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(index, 4).toString()), 10.5);
         } else if (dtm.getValueAt(index, 0).toString().equalsIgnoreCase("21.0")) {
            iva21 += cantidad * UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(index, 4).toString()), 21);
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
      contenedor.setTfTotalDesc(UTIL.PRECIO_CON_PUNTO.format(desc));
      if (subTotal < 0) {
         subTotal = 0.0;
      }
      contenedor.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(subTotal));
      contenedor.setTfCambio(UTIL.PRECIO_CON_PUNTO.format(subTotal));
   }

   /**
    * Calcula el margen monetario ganancia/perdida sobre el monto.
    * @param monto cantidad monetaria sobre la cual se hará el cálculo
    * @param tipoDeMargen Indica como se aplicará el margen al monto.
    * If <code>(tipo > 2 || 1 > tipo)</code> will return <code>null</code>.
    *    <lu>
    *     <li>1 = % (porcentaje)
    *     <li>2 = $ (monto fijo)
    *    <lu>
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
         return;
      }

   }

   private FacturaVenta setAndPersist(boolean conFactura) throws MessageException, Exception {

      // <editor-fold defaultstate="collapsed" desc="CONTROLES">
      if (jdFacturaVenta.getDcFechaFactura() == null) {
         throw new MessageException("Fecha de factura no válida");
      }

      try {
         Sucursal s = (Sucursal) jdFacturaVenta.getCbSucursal().getSelectedItem();
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

      if (remitoToFacturar != null && remitoToFacturar.getFacturaVenta() != null) {
         throw new MessageException("Bravo!.. seleccionó un Remito que ya fue asociado a una Factura."
                 + "\nFactura Nº" + remitoToFacturar.getFacturaVenta().toString());
      }

      //checkeando vigencia de la oferta de los productos a facturar COMO "oferta"
      HistorialOfertas ofertaToCheck;
      HistorialOfertasJpaController historialOfertasController = new HistorialOfertasJpaController();
      for (int rowIndex = 0; rowIndex < dtm.getRowCount(); rowIndex++) {
         ofertaToCheck = (HistorialOfertas) dtm.getValueAt(rowIndex, 10);
         if (ofertaToCheck != null
                 && !historialOfertasController.isOfertaVigente(ofertaToCheck.getId())) {
            throw new MessageException("¡La oferta del Producto:"
                    + "\n" + ofertaToCheck.getProducto().getCodigo()
                    + "\n" + ofertaToCheck.getProducto().getNombre()
                    + "\nNO SE ENCUENTRA MAS VIGENTE!.");
         }
      }
      // </editor-fold>

      //set entity.fields
      FacturaVenta newFacturaVenta = new FacturaVenta();
      newFacturaVenta.setAnulada(false);
      newFacturaVenta.setFechaVenta(jdFacturaVenta.getDcFechaFactura());
      if (conFactura) {
         newFacturaVenta.setNumero(Long.valueOf(jdFacturaVenta.getTfFacturaCuarto() + jdFacturaVenta.getTfFacturaOcteto()));
         newFacturaVenta.setFacturaCuarto(Short.valueOf(jdFacturaVenta.getTfFacturaCuarto()));
         newFacturaVenta.setFacturaOcteto(Integer.valueOf(jdFacturaVenta.getTfFacturaOcteto()));
         newFacturaVenta.setMovimientoInterno(0);
         newFacturaVenta.setTipo(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
      } else {
         newFacturaVenta.setNumero(0);
         newFacturaVenta.setFacturaCuarto((short) 0);
         newFacturaVenta.setFacturaOcteto(0);
         newFacturaVenta.setMovimientoInterno(Integer.valueOf(jdFacturaVenta.getTfNumMovimiento().getText()));
         newFacturaVenta.setTipo('I');
      }

      //setting entities
      newFacturaVenta.setCliente((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
      newFacturaVenta.setSucursal((Sucursal) jdFacturaVenta.getCbSucursal().getSelectedItem());
      newFacturaVenta.setUsuario(UsuarioJpaController.getCurrentUser());
      newFacturaVenta.setCaja((Caja) jdFacturaVenta.getCbCaja().getSelectedItem());
      newFacturaVenta.setListaPrecios(selectedListaPrecios);
      newFacturaVenta.setRemito(remitoToFacturar);

      //setting fields
      newFacturaVenta.setImporte(Double.valueOf(jdFacturaVenta.getTfTotal()));
      newFacturaVenta.setGravado(Double.valueOf(jdFacturaVenta.getTfGravado()));
      newFacturaVenta.setIva10(Double.valueOf(jdFacturaVenta.getTfTotalIVA105()));
      newFacturaVenta.setIva21(Double.valueOf(jdFacturaVenta.getTfTotalIVA21()));
      newFacturaVenta.setDescuento(Double.valueOf(jdFacturaVenta.getTfTotalDesc()));
      newFacturaVenta.setDetallesVentaList(new ArrayList<DetalleVenta>());

      if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CONTADO)) {
         newFacturaVenta.setFormaPago((short) Valores.FormaPago.CONTADO.getId());
         newFacturaVenta.setDiasCtaCte((short) 0);
      } else if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
         newFacturaVenta.setFormaPago((short) Valores.FormaPago.CTA_CTE.getId());
         newFacturaVenta.setDiasCtaCte(Short.parseShort(jdFacturaVenta.getTfDias()));
      }

      /// carga de detalleVenta
      DetalleVenta detalleVenta;
      ProductoJpaController productoController = new ProductoJpaController();
      for (int i = 0; i < dtm.getRowCount(); i++) {
         detalleVenta = new DetalleVenta();
         detalleVenta.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
         detalleVenta.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
         detalleVenta.setDescuento(Double.valueOf(dtm.getValueAt(i, 6).toString()));
         detalleVenta.setTipoDesc(Integer.valueOf(dtm.getValueAt(i, 8).toString()));
         detalleVenta.setProducto(productoController.findProducto((Integer) dtm.getValueAt(i, 9)));
         if (dtm.getValueAt(i, 10) != null) {
            detalleVenta.setOferta((HistorialOfertas) dtm.getValueAt(i, 10));
         }
         newFacturaVenta.getDetallesVentaList().add(detalleVenta);
      }
      //persistiendo
      create(newFacturaVenta);
      newFacturaVenta = findFacturaVenta(newFacturaVenta.getId());
      if (newFacturaVenta.getRemito() != null) {
         remitoToFacturar.setFacturaVenta(newFacturaVenta);
         new RemitoJpaController().edit(remitoToFacturar);
      }

      //actualiza Stock
      new StockJpaController().updateStock(newFacturaVenta);
      //asiento en caja..
      registrarVentaSegunFormaDePago(newFacturaVenta);

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
         case 3: { // CHEQUE
         }
         default: {
            System.out.println("// acá se pudre todo....");
         }
      }
   }

   private Integer getNextMovimientoInterno() {
      EntityManager em = getEntityManager();
      try {
         Object o = em.createQuery("SELECT MAX(o.movimientoInterno) FROM FacturaVenta o").getSingleResult();
         if (o == null) {
            return 1;
         } else {
            return 1 + Integer.valueOf(o.toString());
         }
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   /**
    * Agrega a <code>numero</code> tantos 0 (ceros) a la izq hasta completar los 12
    * dígitos (#### ########) y setea en la GUI {@link JDFacturaVenta}
    * @param numero
    */
   void setNumeroFactura(Long numero) {
      String factuString = UTIL.AGREGAR_CEROS(numero.toString(), 12);
      jdFacturaVenta.setTfFacturaCuarto(factuString.substring(0, 4));
      jdFacturaVenta.setTfFacturaOcteto(factuString.substring(4));
   }

   /**
    * Calcula el margen de ganancia sobre el monto según la {@link ListaPrecios}
    * seleccionada.
    * @param listaPrecios
    * @param producto {@link Producto} del cual se tomarán los {@link Rubro} y
    * Sub para determinar el margen de ganancia if 
    * {@link ListaPrecios#margenGeneral} == FALSE.
    * @param monto sobre el cual se va calcular el margen (suele incluir el
    * margen individual de ganancia de cada producto). Si es == null, se utilizará {@link Producto#precioVenta}
    * @return margen de ganancia (NO INCLUYE EL MONTO).
    */
   public static Double GET_MARGEN_SEGUN_LISTAPRECIOS(ListaPrecios listaPrecios, Producto producto, Double monto) {
      double margenFinal = 0.0;
      if (listaPrecios.getMargenGeneral()) {
         margenFinal = listaPrecios.getMargen();
      } else {
         boolean encontro = false;
         Rubro rubro = producto.getRubro();
         Rubro subRubro = producto.getSubrubro();
         List<DetalleListaPrecios> detalleListaPreciosList = listaPrecios.getDetalleListaPreciosList();
         //si no se encuentra el Rubro en el DetalleListaPrecio, margen permanecerá en 0
         for (DetalleListaPrecios dlp : detalleListaPreciosList) {
            double margenEncontrado = 0.0;
            //si el Rubro del Producto coincide con un Rubro de la ListaPrecios
            if (dlp.getRubro().equals(rubro)) {
               encontro = true;
               margenEncontrado = dlp.getMargen();
            } else {
               //si el subRubro coincide con algún Rubro definido en la ListaPrecios
               if (!encontro && subRubro != null) {
                  if (dlp.getRubro().equals(subRubro)) {
                     margenEncontrado = dlp.getMargen();
                  }
               }
            }
            if (margenEncontrado > margenFinal) {
               /*Puede que la listaPrecios tenga determinado margenes de ganancia
                * tanto para el Rubro como SubRubro de un Producto, entonces
                * solo tomamos el mayor de ellos para aplicarlo.
                */
               margenFinal = margenEncontrado;
            }
         }
      }
      Double montoDefinitivo = (monto == null ? producto.getPrecioVenta() : monto);
      return ((montoDefinitivo * margenFinal) / 100);
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

   public FacturaVenta findFacturaVenta(long numero, Cliente p) {
      return (FacturaVenta) DAO.getEntityManager().createNamedQuery("FacturaCompra.findByNumeroCliente").setParameter("numero", numero).setParameter("cliente", p.getId()).getSingleResult();
   }

   private void initBuscarProducto() throws DatabaseErrorException {
      ProductoJpaController p = new ProductoJpaController();
      p.initContenedor(null, true, false);
      UTIL.loadComboBox(jdFacturaVenta.getCbProductos(), new ProductoJpaController().findProductoToCombo(), false);
   }

   public void initBuscador(JFrame frame, final boolean modal, final boolean paraAnular) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.CHECK_PERMISO(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>

      buscador = new JDBuscadorReRe(frame, "Buscador - Facturas venta", modal, "Cliente", "Nº Factura");
      UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(buscador.getCbCaja(), new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
      UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"facturaID", "Nº factura", "Tipo", "Mov.", "Cliente", "Importe", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha (Sistema)"},
              new int[]{1, 60, 5, 10, 50, 50, 50, 80, 80, 50, 70});
      //escondiendo facturaID
      UTIL.hideColumnTable(buscador.getjTable1(), 0);
      buscador.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
               if (buscador.getjTable1().getSelectedRow() > -1) {
                  EL_OBJECT = findFacturaVenta(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                  setDatosEnUI(EL_OBJECT, paraAnular);
               }
            }
         }
      });
      if (paraAnular) {
         buscador.getCheckAnulada().setEnabled(false);
      }
      MODO_VISTA = true;
      buscador.setListeners(this);
      buscador.setLocationRelativeTo(frame);
      buscador.setVisible(true);
   }

   private void cargarDtmBuscador(String query) {
      DefaultTableModel dtm = buscador.getDtm();
      UTIL.limpiarDtm(dtm);
      List<FacturaVenta> l = DAO.getEntityManager().createNativeQuery(query, FacturaVenta.class).getResultList();
      for (FacturaVenta facturaVenta : l) {
         dtm.addRow(new Object[]{
                    facturaVenta.getId(), // <--- no es visible
                    UTIL.AGREGAR_CEROS(facturaVenta.getNumero(), 12),
                    facturaVenta.getTipo(),
                    facturaVenta.getMovimientoInterno(),
                    facturaVenta.getCliente(),
                    facturaVenta.getImporte(),
                    UTIL.DATE_FORMAT.format(facturaVenta.getFechaVenta()),
                    facturaVenta.getSucursal(),
                    facturaVenta.getCaja(),
                    facturaVenta.getUsuario(),
                    UTIL.DATE_FORMAT.format(facturaVenta.getFechaalta()) + " (" + UTIL.TIME_FORMAT.format(facturaVenta.getFechaalta()) + ")"
                 });
      }
   }

   private void armarQuery() throws MessageException {
      String query = "SELECT o.* FROM factura_venta o"
              + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected();

      long numero;
      //filtro por nº de ReRe
      if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
            query += " AND o.numero = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }

      //filtro por nº de factura
      if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
            query += " AND o.numero = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }
      if (buscador.getDcDesde() != null) {
         query += " AND o.fecha_venta >= '" + buscador.getDcDesde() + "'";
      }
      if (buscador.getDcHasta() != null) {
         query += " AND o.fecha_venta <= '" + buscador.getDcHasta() + "'";
      }
      if (buscador.getCbCaja().getSelectedIndex() > 0) {
         query += " AND o.caja = " + ((Caja) buscador.getCbCaja().getSelectedItem()).getId();
      }
      if (buscador.getCbSucursal().getSelectedIndex() > 0) {
         query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
      }

      if (buscador.getCbClieProv().getSelectedIndex() > 0) {
         query += " AND o.cliente = " + ((Cliente) buscador.getCbClieProv().getSelectedItem()).getId();
      }

      if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
         query += " AND o.forma_pago = " + ((Valores.FormaPago) buscador.getCbFormasDePago().getSelectedItem()).getId();
      }

      if (buscador.getTfFactu4().trim().length() > 0) {
         try {
            query += " AND o.movimiento_interno = " + Integer.valueOf(buscador.getTfFactu4());
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de movimiento no válido");
         }
      }
      query += " ORDER BY o.id";
      Logger.getLogger(FacturaVentaJpaController.class).log(Level.TRACE, "queryBuscador=" + query);
      cargarDtmBuscador(query);
   }

   private void setDatosEnUI(FacturaVenta selectedFacturaVenta, final boolean paraAnular) {
      try {
         initFacturaVenta(null, true, this, 1, false, false);
         jdFacturaVenta.getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                  jdFacturaVenta.getBtnFacturar().setEnabled(false);
                  jdFacturaVenta.getBtnAceptar().setEnabled(false);
                  doMovimientoInterno();
               } catch (MessageException ex) {
                  jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
               } catch (Exception ex) {
                  jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                  Logger.getLogger(SucursalJpaController.class.getName()).log(Level.ERROR, null, ex);
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
                  facturar();
               } catch (MessageException ex) {
                  jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
               } catch (Exception ex) {
                  jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
                  Logger.getLogger(SucursalJpaController.class.getName()).log(Level.ERROR, null, ex);
               } finally {
                  jdFacturaVenta.getBtnFacturar().setEnabled(true);
                  jdFacturaVenta.getBtnAceptar().setEnabled(true);
               }
            }
         });
      } catch (MessageException ex) {
         Logger.getLogger(FacturaVentaJpaController.class.getName()).log(Level.ERROR, null, ex);
      }
      jdFacturaVenta.setLocationRelativeTo(buscador);
      // seteando datos de FacturaCompra
      jdFacturaVenta.getCbCliente().addItem(selectedFacturaVenta.getCliente());
      jdFacturaVenta.getCbSucursal().addItem(selectedFacturaVenta.getSucursal());
      jdFacturaVenta.getCbListaPrecio().addItem(selectedFacturaVenta.getListaPrecios());
      jdFacturaVenta.setDcFechaFactura(selectedFacturaVenta.getFechaVenta());
      if (selectedFacturaVenta.getRemito() != null) {
         jdFacturaVenta.setTfRemito(selectedFacturaVenta.getRemito().toString());
      }

      //Los tipos de factura se tienen q cargar antes, sinó modifica el Nº de factura y muestra el siguiente
      //y no el de Factura seleccionada
      cargarComboTiposFacturas(selectedFacturaVenta.getCliente());
      String numFactura = UTIL.AGREGAR_CEROS(selectedFacturaVenta.getNumero(), 12);
      jdFacturaVenta.setTfFacturaCuarto(numFactura.substring(0, 4));
      jdFacturaVenta.setTfFacturaOcteto(numFactura.substring(4));
      jdFacturaVenta.setTfNumMovimiento(String.valueOf(selectedFacturaVenta.getMovimientoInterno()));
      jdFacturaVenta.getCbCaja().addItem(selectedFacturaVenta.getCaja());
      UTIL.loadComboBox(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
      UTIL.setSelectedItem(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormaPago(selectedFacturaVenta.getFormaPago()));
      if (selectedFacturaVenta.getDiasCtaCte() != null) {
         jdFacturaVenta.setTfDias(selectedFacturaVenta.getDiasCtaCte().toString());
      }
      // <editor-fold defaultstate="collapsed" desc="Carga de DetallesVenta">
      List<DetalleVenta> lista = selectedFacturaVenta.getDetallesVentaList();
      DefaultTableModel dtm = jdFacturaVenta.getDTM();
      for (DetalleVenta detallesVenta : lista) {
         double productoConIVA = detallesVenta.getPrecioUnitario() + UTIL.getPorcentaje(detallesVenta.getPrecioUnitario(), detallesVenta.getProducto().getIva().getIva());
         //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
         dtm.addRow(new Object[]{
                    null,
                    detallesVenta.getProducto().getCodigo(),
                    detallesVenta.getProducto(),
                    detallesVenta.getCantidad(),
                    detallesVenta.getPrecioUnitario(),
                    UTIL.PRECIO_CON_PUNTO.format(productoConIVA),
                    detallesVenta.getDescuento(),
                    UTIL.PRECIO_CON_PUNTO.format((detallesVenta.getCantidad() * productoConIVA) - detallesVenta.getDescuento())
                 });
      }// </editor-fold>

      //totales
      jdFacturaVenta.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getImporte() - (selectedFacturaVenta.getIva10() + selectedFacturaVenta.getIva21())));
      jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getIva10()));
      jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getIva21()));
      jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(selectedFacturaVenta.getImporte()));
      jdFacturaVenta.modoVista(); //   <----------------------

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
      }
      jdFacturaVenta.setListener(this);
      jdFacturaVenta.setLocationByPlatform(true);
      jdFacturaVenta.setVisible(true);

   }

   private void anular(FacturaVenta factura) throws MessageException {
      Caja oldCaja = factura.getCaja();
      List<Caja> cajasPermitidasList = new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true);
      if (cajasPermitidasList.isEmpty()) {
         throw new MessageException("No tiene acceso a ninguna Caja del sistema");
      }
      if (!cajasPermitidasList.contains(oldCaja)) {
         throw new MessageException("No tiene permiso para modificar la Caja " + oldCaja + ", con la que fue realizada la venta.");
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
         // disponible, se tiene que elegir OTAR caja (se muestra una GUI para esto)
         cmAbierta = initReAsignacionCajaMovimiento(cajasPermitidasList);
         if (cmAbierta == null) {
            throw new MessageException("Proceso de anulación CANCELADO");
         }
      }
      try {
         cmController.anular(factura, cmAbierta);
      } catch (Exception ex) {
         JOptionPane.showMessageDialog(jdFacturaVenta, ex.getMessage());
         Logger.getLogger(FacturaVentaJpaController.class.getName()).log(Level.ERROR, null, ex);
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
      r.printReport(false);
   }

   private void imprimirFactura(FacturaVenta facturaVenta) throws Exception {
      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_FacturaVenta_" + facturaVenta.getTipo() + ".jasper", "Factura Venta");
      r.addParameter("FACTURA_ID", facturaVenta.getId());
      if (facturaVenta.getRemito() != null) {
         r.addParameter("REMITO", facturaVenta.getRemito().toString());
      }
      r.printReport(true);
   }

   private Long getNextNumeroFactura(char letra) {
      EntityManager em = getEntityManager();
      Long next_factu = 100000001L;
      try {
         next_factu = 1 + (Long) em.createQuery("SELECT MAX(o.numero) FROM FacturaVenta o"
                 + " WHERE o.movimientoInterno = 0 AND o.tipo ='" + letra + "'").getSingleResult();
      } catch (NullPointerException ex) {
         System.out.println("pintó la 1ra Factura " + letra);
      } finally {
         em.close();
      }
      return next_factu;
   }

   private String getNextNumeroFacturaStringConGuion(char letra) {
      Long next_factu = getNextNumeroFactura(letra);
      String factuString = UTIL.AGREGAR_CEROS(next_factu.toString(), 12);
      factuString = factuString.substring(0, 4) + "-" + factuString.substring(4);
      return factuString;
   }

   private void cambiarMovimientoInternoToFactura(FacturaVenta facturaVenta) throws Exception {
      facturaVenta.setTipo(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
      facturaVenta.setNumero(getNextNumeroFactura(facturaVenta.getTipo()));
      facturaVenta.setMovimientoInterno(0);
      DAO.doMerge(facturaVenta);
      if (facturaVenta.getFormaPago() == Valores.FormaPago.CONTADO.getId()) {
         //si NO es al CONTADO no tiene un registro de DetalleCajaMovimiento
         new CajaMovimientosJpaController().actualizarDescripcion(facturaVenta);
      }
      imprimirFactura(facturaVenta);
      jdFacturaVenta.getBtnFacturar().setEnabled(false);
//      limpiarPanel();
   }

   private void facturar() throws MessageException, Exception {
      if (!MODO_VISTA) {
         //cuando se está en modo vista (factura seleccionada del buscador).. no va entrar ACA
         //sinó que va ir directo a la opción de imprimir   |
         imprimirFactura(setAndPersist(true));                // |
         limpiarPanel();                                 // |
      } else {                                           // |
         if (EL_OBJECT.getTipo() == 'I') {               // V
            if (0 == javax.swing.JOptionPane.showConfirmDialog(jdFacturaVenta, "¿Cambiar factura Movimiento interno Nº" + EL_OBJECT.getMovimientoInterno() + " por "
                    + "Factura Venta \"" + jdFacturaVenta.getCbFacturaTipo().getSelectedItem() + "\" Nº"
                    + getNextNumeroFacturaStringConGuion(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)) + "?", "Facturación - Venta", javax.swing.JOptionPane.OK_CANCEL_OPTION)) {
               cambiarMovimientoInternoToFactura(EL_OBJECT);
            }
         } else {
            if (0 == JOptionPane.showConfirmDialog(jdFacturaVenta, "La Factura Nº" + EL_OBJECT.getNumero() + " ya fue impresa.\n¿Volver a imprimir?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
               imprimirFactura(EL_OBJECT);
            }
         }
      }
   }

   private void doMovimientoInterno() throws MessageException, Exception {
      if (!MODO_VISTA) {
         //cuando se está en modo vista.. no va entrar ACA
         //sinó que va ir directo a la opción de imprimir (Re-imprimir)
         FacturaVenta newestFacturaVenta = setAndPersist(false);
         if (0 == JOptionPane.showConfirmDialog(jdFacturaVenta, "¿Imprimir comprobante?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
            imprimirMovimientoInterno(newestFacturaVenta);
         }
         limpiarPanel();
      } else {
         if (0 == JOptionPane.showConfirmDialog(jdFacturaVenta,
                 "¿Re-Imprimir comprobante?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
            imprimirMovimientoInterno(EL_OBJECT);
         }
      }
   }

   private void cancelarBtnAction() {
      if (MODO_VISTA) {
         jdFacturaVenta.dispose();
      } else {
         limpiarPanel();
         EL_OBJECT = null;
      }
   }

   /**
    * Borra el detalle de la factura
    * Actualiza Movimiento interno y/o Número de factura (según corresponda)
    */
   private void limpiarPanel() {
      borrarDetalles();
      jdFacturaVenta.setTfRemito("Sin Remito");
      remitoToFacturar = null;
      if (jdFacturaVenta.getModoUso() == 1) {
         jdFacturaVenta.setTfNumMovimiento(getNextMovimientoInterno().toString());
         setNumeroFactura(getNextNumeroFactura(jdFacturaVenta.getCbFacturaTipo().getSelectedItem().toString().charAt(0)));
      }
   }

   /**
    * Limpiar la dtm de detalles de venta.
    * refresca los totales (gravado, IVA's, ...)
    * selectedProducto = null y la info sobre este
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
      RemitoJpaController remitoController = new RemitoJpaController();
      remitoController.initBuscadorToFacturar(jdFacturaVenta, clienteSeleccionado);
      remitoToFacturar = remitoController.getSelectedRemito();
      if (remitoToFacturar != null) {
         jdFacturaVenta.setTfRemito(remitoToFacturar.toString());
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
         selectedProducto = p.findProductoById(producto.getId());
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
    * Verifica el precio mínimo de venta de un producto.
    * <p>Si el producto está en <b>OFERTA</b> NO SE APLICA la regla.
    * <p>Si el producto tiene precio de venta mínimo seteado se compara este con
    * precioUnitario.
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
}
