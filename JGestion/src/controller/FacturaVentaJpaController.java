package controller;

import generics.UTIL;
import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import generics.AutoCompleteComboBox;
import java.awt.event.FocusAdapter;
import javax.swing.JFrame;

/**
 * Clase (Ventana) usada para crear FacturaVenta, Remitos, Presupuestos
 * @author FiruzzZ
 */
public class FacturaVentaJpaController implements ActionListener, KeyListener {

   public static final String CLASS_NAME = FacturaVenta.class.getSimpleName();
   private JDFacturaVenta jdFacturaVenta;
   private FacturaVenta EL_OBJECT;
   private Producto selectedProducto;
   private ListaPrecios listaPrecios;
   private JDBuscadorReRe buscador;
   private final int LIMITE_DE_ITEMS = 14; // <--- cantidad de items en detalleFactura
   private boolean MODO_VISTA = false;
   private Remito remitoToFacturar;
   /**
    * Solo usada para reasignación de Caja, cuando se anula una Factura.
    */
   private CajaMovimientos cajaMovToAsentarAnulacion;

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
    */
   public void initFacturaVenta(JFrame frame, boolean modal, Object listener, int factVenta1_Presup2_Remito3, boolean setVisible) throws MessageException {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      jdFacturaVenta = new JDFacturaVenta(frame, modal, factVenta1_Presup2_Remito3);
      UTIL.getDefaultTableModel(jdFacturaVenta.getjTable1(),
              new String[]{"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Precio con IVA", "Desc", "Sub total", "TipoDescuento"},
              new int[]{1, 70, 180, 10, 30, 30, 30, 30, 1});
      //esconde de la vista del usuario la columna IVA y Tipo de Descuento
      UTIL.hideColumnsTable(jdFacturaVenta.getjTable1(), new int[]{0, 8});
      jdFacturaVenta.getBtnBuscarRemito().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            initBuscadorRemito();
         }
      });
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
               addProductoToList();
            } catch (MessageException ex) {
               jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdFacturaVenta.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
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
      //porque cuando es FALSE, se va usar en MODO_VISTA (Factura, Remito o Presupuesto)
      //por lo tanto no es necesario cargar todos los combos
      if (setVisible) {
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
         jdFacturaVenta.setDcFechaFactura(new java.util.Date());
         UTIL.loadComboBox(jdFacturaVenta.getCbCliente(), new ClienteJpaController().findClienteEntities(), false);
         UTIL.loadComboBox(jdFacturaVenta.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), false);
         UTIL.loadComboBox(jdFacturaVenta.getCbListaPrecio(), new ListaPreciosJpaController().findListaPreciosEntities(), false);
         UTIL.loadComboBox(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
         jdFacturaVenta.getCbUsuario().addItem(UsuarioJpaController.getCurrentUser());
         jdFacturaVenta.getCbListaPrecio().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               if (!listaPrecios.equals((ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem())) {
                  borrarDetalles();
                  listaPrecios = (ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem();
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

         if (factVenta1_Presup2_Remito3 == 1) {
            try {
               cargarComboTiposFacturas((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
            } catch (ClassCastException ex) {
               throw new MessageException("Debe crear un Cliente para poder realizar una Facturas venta, Presupuestos o Remitos.");
            }
            jdFacturaVenta.getCbCliente().addActionListener(new ActionListener() {

               @Override
               public void actionPerformed(ActionEvent e) {
                  try {
                     cargarComboTiposFacturas((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
                  } catch (ClassCastException ex) {
                     //No hay entity.Cliente
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
                     Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
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
                     Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
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
         listaPrecios = (ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem();
      } catch (ClassCastException ex) {
         //no hay listaPrecios..............!!!");
      }
      jdFacturaVenta.setLocation(jdFacturaVenta.getOwner().getX() + 100, jdFacturaVenta.getY() + 50);
      jdFacturaVenta.setVisible(setVisible);
   }

   private void buscarProducto(String codigoProducto) {
      selectedProducto = new ProductoJpaController().findProductoByCodigo(codigoProducto);
      setInformacionDeProducto(jdFacturaVenta, selectedProducto);
   }

   private void addProductoToList() throws MessageException {
      if (selectedProducto == null) {
         throw new MessageException("Seleccione un producto");
      }
      if (jdFacturaVenta.getDTM().getRowCount() >= LIMITE_DE_ITEMS) {
         throw new MessageException("La factura no puede tener mas de " + LIMITE_DE_ITEMS + " items.");
      }
      if (listaPrecios == null) {
         throw new MessageException("Debe elegir una lista de precios antes."
                 + "\nSi no existe ninguna debe crearla en el Menú -> Productos -> Lista de precios.");
      }

      int cantidad;
      double precioUnitario; // (precioUnitario + margen unitario + margen listaPrecios)
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
      } catch (NumberFormatException ex) {
         throw new MessageException("Precio Unitario no válido");
      }// </editor-fold>

      // <editor-fold defaultstate="collapsed" desc="ctrl tfDescuento">
      try {
         //por si deja el TextField de Descuento vacío
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
            } // cuando es por un monto fijo ($)
            else if ((jdFacturaVenta.getCbDesc().getSelectedIndex() == 1) && (descuentoUnitario > precioUnitario)) {
               throw new MessageException("El descuento (" + descuentoUnitario + ")no puede ser superior al precio venta (" + precioUnitario + ")");
            }

         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Descuento no válido");
      }// </editor-fold>

      // el descuento se hace sobre el precioUnitario SIN IVA!!!!
      descuentoUnitario = getMargen(precioUnitario, jdFacturaVenta.getCbDesc().getSelectedIndex() + 1, descuentoUnitario);

      //adiciona el descuento
      precioUnitario -= descuentoUnitario;

      //el IVA se calcula sobre (precioUnitario - descuentos)
      double unitarioConIva = precioUnitario + UTIL.getPorcentaje(precioUnitario, selectedProducto.getIva().getIva());

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
                 (descuentoUnitario == 0) ? -1 : (jdFacturaVenta.getCbDesc().getSelectedIndex() + 1)//Tipo de descuento
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
    * Setea la info del Producto en <code>gui.JDFacturaVenta</code>
    * @param contenedor
    * @param selectedProducto entity Producto
    */
   private void setInformacionDeProducto(JDFacturaVenta contenedor, Producto selectedProducto) {
      if (selectedProducto != null) {
         contenedor.setLabelCodigoNoRegistradoVisible(false);
         contenedor.setTfProductoCodigo(selectedProducto.getCodigo());
         UTIL.setSelectedItem(contenedor.getCbProductos(), selectedProducto.getNombre());
         contenedor.setTfProductoIVA(selectedProducto.getIva().getIva().toString());

         //agrega el margen de ganancia individual del Producto
         Double precioUnitario = selectedProducto.getPrecioVenta();
         precioUnitario += getMargen(precioUnitario,
                 selectedProducto.getTipomargen(),
                 selectedProducto.getMargen());

         //agrega el margen de ganancia según la ListaPrecio
         precioUnitario += getMargenSegunListaPrecio(precioUnitario);
         contenedor.setTfPrecioUnitario(UTIL.PRECIO_CON_PUNTO.format(precioUnitario));
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
      javax.swing.table.DefaultTableModel dtm = contenedor.getDTM();
      for (int i = 0; i < dtm.getRowCount(); i++) {
         double cantidad = Double.valueOf(dtm.getValueAt(i, 3).toString());

         // precioSinIVA + cantidad
         gravado += (cantidad * Double.valueOf(dtm.getValueAt(i, 4).toString()));

         // IVA's ++
         if (dtm.getValueAt(i, 0).toString().equalsIgnoreCase("10.5")) {
            iva10 += cantidad * UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 4).toString()), 10.5);
         } else if (dtm.getValueAt(i, 0).toString().equalsIgnoreCase("21.0")) {
            iva21 += cantidad * UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 4).toString()), 21);
         }

         //Descuento++
         //si el subtotal es > 0... para no dar resultados negativos!!!
         if (Double.valueOf(dtm.getValueAt(i, 7).toString()) >= 0) {
            desc += Double.valueOf(dtm.getValueAt(i, 6).toString());
         }

         subTotal += Double.valueOf(dtm.getValueAt(i, 7).toString());
      }

      contenedor.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado));
      contenedor.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(iva10));
      contenedor.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(iva21));
      contenedor.setTfTotalDesc(UTIL.PRECIO_CON_PUNTO.format(desc));
      System.out.println("subTotal=" + subTotal);
      if (subTotal < 0) {
         subTotal = 0.0;
      }
      contenedor.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(subTotal));
      contenedor.setTfCambio(UTIL.PRECIO_CON_PUNTO.format(subTotal));
   }

   /**
    * Calcula el margen de ganancia/descuento de ganancia/perdida sobre el monto
    * @param monto cantidad monetaria sobre la cual se harán los cálculos
    * @param tipo Indica como se aplicará el margen al monto. 1 = % (porcentaje), 2 = $ (monto fijo), if (tipo > 2 || 1 > tipo) RETURN null!
    * @param margen monto fijo o porcentual
    * @return adiviná!
    */
   public static Double getMargen(double monto, int tipoDeMargen, double margen) {
      if (margen == 0) {
         return 0.0;
      }

      double total = 0.0;
      switch (tipoDeMargen) {
         case 1: { // margen en %
            total = ((monto * margen) / 100);
            break;
         }
         case 2: {  // margen en $ (monto fijo).. no hay mucha science...
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

   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyPressed(KeyEvent e) {
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
            } catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
               ex.printStackTrace();
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
         throw new MessageException("Bravo!.. seleccionó un Remito que ya fue asociado a una Factura.\nFactura Nº" + remitoToFacturar.getFacturaVenta().toString());
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
      newFacturaVenta.setListaPrecios(listaPrecios);
      newFacturaVenta.setUsuario((Usuario) jdFacturaVenta.getCbUsuario().getSelectedItem());
      newFacturaVenta.setCaja((Caja) jdFacturaVenta.getCbCaja().getSelectedItem());
      newFacturaVenta.setRemito(remitoToFacturar);

      //setting fields
      newFacturaVenta.setGravado(Double.valueOf(jdFacturaVenta.getTfGravado()));
      newFacturaVenta.setIva10(Double.valueOf(jdFacturaVenta.getTfTotalIVA105()));
      newFacturaVenta.setIva21(Double.valueOf(jdFacturaVenta.getTfTotalIVA21()));
      newFacturaVenta.setDescuento(Double.valueOf(jdFacturaVenta.getTfTotalDesc()));
      newFacturaVenta.setImporte(Double.valueOf(jdFacturaVenta.getTfTotal()));
      newFacturaVenta.setDetallesVentaList(new ArrayList<DetalleVenta>());

      if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CONTADO)) {
         newFacturaVenta.setFormaPago((short) Valores.FormaPago.CONTADO.getId());
         newFacturaVenta.setDiasCtaCte((short) 0);
      } else if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
         newFacturaVenta.setFormaPago((short) Valores.FormaPago.CTA_CTE.getId());
         newFacturaVenta.setDiasCtaCte(Short.parseShort(jdFacturaVenta.getTfDias()));
      }

      /// carga de detalleVenta
      DetalleVenta dv;
      for (int i = 0; i < dtm.getRowCount(); i++) {
         dv = new DetalleVenta();
         dv.setProducto(new ProductoJpaController().findProductoByCodigo(
                 dtm.getValueAt(i, 1).toString()));
         dv.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
         dv.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
         dv.setDescuento(Double.valueOf(dtm.getValueAt(i, 6).toString()));
         dv.setTipoDesc(Integer.valueOf(dtm.getValueAt(i, 8).toString()));
         newFacturaVenta.getDetallesVentaList().add(dv);
      }
      //persistiendo
      create(newFacturaVenta);
      newFacturaVenta = findFacturaVenta(newFacturaVenta.getId());
      if (newFacturaVenta.getRemito() != null) {
         remitoToFacturar.setFacturaVenta(newFacturaVenta);
         new RemitoJpaController().edit(remitoToFacturar);
      }

      //actualiza Stock
      try {
         new StockJpaController().updateStock(newFacturaVenta);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
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
    * Agarra el número, le agrega los 0 (ceros) a la izq hasta completar los 12
    * dígitos (#### ########) y setea en la GUI {@link JDFacturaVenta}
    * @param numero
    */
   void setNumeroFactura(Long numero) {
      String factuString = UTIL.AGREGAR_CEROS(numero.toString(), 12);
      jdFacturaVenta.setTfFacturaCuarto(factuString.substring(0, 4));
      jdFacturaVenta.setTfFacturaOcteto(factuString.substring(4));
   }

   /**
    * Calcula el margen de ganancia sobre el precioUnitario según la {@link ListaPrecios}
    * seleccionada
    * @param precioUnitario sobre el cual se va calcular el margen
    * @return el margen de ganancia según el precioUnitario
    */
   private Double getMargenSegunListaPrecio(Double precioUnitario) {
      double margen = 0.0;
      if (listaPrecios.getMargenGeneral()) {
         margen = listaPrecios.getMargen();
      } else {
         boolean encontro = false;
         Rubro rubro = selectedProducto.getRubro();
         Rubro subRubro = null;
         if (selectedProducto.getSubrubro() != null) {
            subRubro = selectedProducto.getSubrubro();
         }

         List<DetalleListaPrecios> l = listaPrecios.getDetalleListaPreciosList();
         //si no se encuentra el Rubro en el DetalleListaPrecio, margen permanecerá en 0
         for (DetalleListaPrecios d : l) {
            double margenLista = 0.0;
            //si el Rubro del Producto coincide con un Rubro de la ListaPrecios
            if (d.getRubro().getNombre().equals(rubro.getNombre())) {
               encontro = true;
               margenLista = d.getMargen();
            } else {
               //si el subRubro coincide con algún Rubro definido en la ListaPrecios
               if (!encontro && subRubro != null) {
                  if (d.getRubro().getNombre().equals(subRubro.getNombre())) {
                     margenLista = d.getMargen();
                  }
               }
            }

            if (margenLista > margen) {
               margen = margenLista;
            }
         }
      }
      return ((precioUnitario * margen) / 100);
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
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.VENTA);
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
                  setDatosFactura(EL_OBJECT, paraAnular);
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
      cargarDtmBuscador(query);
   }

   private void setDatosFactura(FacturaVenta selectedFacturaVenta, final boolean paraAnular) {
      try {
         initFacturaVenta(null, true, this, 1, false);
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
                  Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
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
                  Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
               } finally {
                  jdFacturaVenta.getBtnFacturar().setEnabled(true);
                  jdFacturaVenta.getBtnAceptar().setEnabled(true);
               }
            }
         });
      } catch (MessageException ex) {
         Logger.getLogger(FacturaVentaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      jdFacturaVenta.setLocationRelativeTo(buscador);
      // seteando datos de FacturaCompra
      jdFacturaVenta.getCbCliente().addItem(selectedFacturaVenta.getCliente());
      jdFacturaVenta.getCbSucursal().addItem(selectedFacturaVenta.getSucursal());
      jdFacturaVenta.getCbListaPrecio().addItem(selectedFacturaVenta.getListaPrecios());  //<---
      jdFacturaVenta.getCbUsuario().addItem(selectedFacturaVenta.getUsuario());           //<---
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
      UTIL.setSelectedItem(jdFacturaVenta.getCbFormaPago(), Valores.FormaPago.getFormasDePago(selectedFacturaVenta.getFormaPago()));
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
         Logger.getLogger(FacturaVentaJpaController.class.getName()).log(Level.SEVERE, null, ex);
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
         //sinó que va ir directo a la opción de imprimir
         FacturaVenta newestFacturaVenta = setAndPersist(false);
         if (0 == JOptionPane.showConfirmDialog(jdFacturaVenta,
                 "¿Imprimir comprobante?", CLASS_NAME, JOptionPane.OK_CANCEL_OPTION)) {
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
//         jdFacturaVenta = null;
      }
   }

   /**
    * Borra el detalle de la factura
    * Actualiza Movimiento interno y/o Número de factura (según corresponda)
    */
   private void limpiarPanel() {
      borrarDetalles();
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
      jdFacturaVenta.setTfRemito("Sin Remito");
      remitoToFacturar = null;
      jdFacturaVenta.setLabelCodigoNoRegistradoVisible(false);
   }

   JDFacturaVenta getContenedor() {
      return jdFacturaVenta;
   }

   private void initBuscadorRemito() {
      RemitoJpaController remitoController = new RemitoJpaController();
      remitoController.initBuscadorToFacturar(jdFacturaVenta, false, (Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
      remitoToFacturar = remitoController.getSelectedRemito();
      if (remitoToFacturar != null) {
         jdFacturaVenta.setTfRemito(remitoToFacturar.toString());
      } else {
         jdFacturaVenta.setTfRemito("Sin Remito");
      }
   }
}
