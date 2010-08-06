package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.CtacteProveedor;
import entity.Remesa;
import entity.Sucursal;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleRemesa;
import entity.FacturaCompra;
import entity.Proveedor;
import entity.UTIL;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrador
 */
public class RemesaJpaController implements ActionListener, MouseListener, FocusListener {
   private final String CLASS_NAME = "Remesa";
   private final String[] colsName = {"Factura","Observación","Entrega" };
   private final int[] colsWidth = {50,150,30};
   private JDReRe contenedor;
   private List<FacturaCompra> facturasList;
   private Remesa EL_OBJECT;
   private CtacteProveedor selectedCtaCte;
   private java.util.Date selectedFechaReRe = null;
   private JDBuscadorReRe buscador;
   private Remesa rereSelected;


//    public RemesaJpaController() {
//        emf = Persistence.createEntityManagerFactory("JGestionPU");
//    }
//    private EntityManagerFactory emf = null;

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Remesa remesa) throws PreexistingEntityException, Exception {
        if (remesa.getDetalleRemesaList() == null) {
            remesa.setDetalleRemesaList(new ArrayList<DetalleRemesa>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<DetalleRemesa> attachedDetalleRemesaList = new ArrayList<DetalleRemesa>();
            for (DetalleRemesa detalleRemesaListDetalleRemesaToAttach : remesa.getDetalleRemesaList()) {
                detalleRemesaListDetalleRemesaToAttach = em.merge(detalleRemesaListDetalleRemesaToAttach);
                attachedDetalleRemesaList.add(detalleRemesaListDetalleRemesaToAttach);
            }
            remesa.setDetalleRemesaList(attachedDetalleRemesaList);
            em.persist(remesa);
            for (DetalleRemesa detalleRemesaListDetalleRemesa : remesa.getDetalleRemesaList()) {
                Remesa oldRemesaOfDetalleRemesaListDetalleRemesa = detalleRemesaListDetalleRemesa.getRemesa();
                detalleRemesaListDetalleRemesa.setRemesa(remesa);
                detalleRemesaListDetalleRemesa = em.merge(detalleRemesaListDetalleRemesa);
                if (oldRemesaOfDetalleRemesaListDetalleRemesa != null) {
                    oldRemesaOfDetalleRemesaListDetalleRemesa.getDetalleRemesaList().remove(detalleRemesaListDetalleRemesa);
                    oldRemesaOfDetalleRemesaListDetalleRemesa = em.merge(oldRemesaOfDetalleRemesaListDetalleRemesa);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findRemesa(remesa.getId()) != null) {
                throw new PreexistingEntityException("Remesa " + remesa + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Remesa remesa) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Remesa persistentRemesa = em.find(Remesa.class, remesa.getId());
            List<DetalleRemesa> detalleRemesaListOld = persistentRemesa.getDetalleRemesaList();
            List<DetalleRemesa> detalleRemesaListNew = remesa.getDetalleRemesaList();
            List<String> illegalOrphanMessages = null;
            for (DetalleRemesa detalleRemesaListOldDetalleRemesa : detalleRemesaListOld) {
                if (!detalleRemesaListNew.contains(detalleRemesaListOldDetalleRemesa)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain DetalleRemesa " + detalleRemesaListOldDetalleRemesa + " since its remesa field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<DetalleRemesa> attachedDetalleRemesaListNew = new ArrayList<DetalleRemesa>();
            for (DetalleRemesa detalleRemesaListNewDetalleRemesaToAttach : detalleRemesaListNew) {
                detalleRemesaListNewDetalleRemesaToAttach = em.getReference(detalleRemesaListNewDetalleRemesaToAttach.getClass(), detalleRemesaListNewDetalleRemesaToAttach.getId());
                attachedDetalleRemesaListNew.add(detalleRemesaListNewDetalleRemesaToAttach);
            }
            detalleRemesaListNew = attachedDetalleRemesaListNew;
            remesa.setDetalleRemesaList(detalleRemesaListNew);
            remesa = em.merge(remesa);
            for (DetalleRemesa detalleRemesaListNewDetalleRemesa : detalleRemesaListNew) {
                if (!detalleRemesaListOld.contains(detalleRemesaListNewDetalleRemesa)) {
                    Remesa oldRemesaOfDetalleRemesaListNewDetalleRemesa = detalleRemesaListNewDetalleRemesa.getRemesa();
                    detalleRemesaListNewDetalleRemesa.setRemesa(remesa);
                    detalleRemesaListNewDetalleRemesa = em.merge(detalleRemesaListNewDetalleRemesa);
                    if (oldRemesaOfDetalleRemesaListNewDetalleRemesa != null && !oldRemesaOfDetalleRemesaListNewDetalleRemesa.equals(remesa)) {
                        oldRemesaOfDetalleRemesaListNewDetalleRemesa.getDetalleRemesaList().remove(detalleRemesaListNewDetalleRemesa);
                        oldRemesaOfDetalleRemesaListNewDetalleRemesa = em.merge(oldRemesaOfDetalleRemesaListNewDetalleRemesa);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = remesa.getId();
                if (findRemesa(id) == null) {
                    throw new NonexistentEntityException("The remesa with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Remesa remesa;
            try {
                remesa = em.getReference(Remesa.class, id);
                remesa.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The remesa with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<DetalleRemesa> detalleRemesaListOrphanCheck = remesa.getDetalleRemesaList();
            for (DetalleRemesa detalleRemesaListOrphanCheckDetalleRemesa : detalleRemesaListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Remesa (" + remesa + ") cannot be destroyed since the DetalleRemesa " + detalleRemesaListOrphanCheckDetalleRemesa + " in its detalleRemesaList field has a non-nullable remesa field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(remesa);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Remesa> findRemesaEntities() {
        return findRemesaEntities(true, -1, -1);
    }

    public List<Remesa> findRemesaEntities(int maxResults, int firstResult) {
        return findRemesaEntities(false, maxResults, firstResult);
    }

    private List<Remesa> findRemesaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Remesa as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Remesa findRemesa(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Remesa.class, id);
        } finally {
            em.close();
        }
    }

    public int getRemesaCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Remesa as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

   private Long getNextNumeroRemesa() {
      EntityManager em = getEntityManager();
      Long next_factu = 100000001L;
      try {
         next_factu = 1 + (Long) em.createQuery("SELECT MAX(o.id)" +
                                                " FROM "+CLASS_NAME+" o")
                                                .getSingleResult();
      } catch (NoResultException ex) {
         System.out.println("pintó la 1ra "+CLASS_NAME+"....NoResultEx");
      } catch (NullPointerException ex ) {
         System.out.println("pintó la 1ra "+CLASS_NAME+"....NullPointerEx");
      } finally { 
         if(em != null) em.close();
      }
      return next_factu;
    }

    public void initContenedor(java.awt.Frame frame, boolean modal) {
       // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.COMPRA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
      contenedor = new JDReRe(frame, modal);
      contenedor.setLocationRelativeTo(frame);
      //seteos de GUI --->
      contenedor.setTitle(CLASS_NAME);
      contenedor.getLabelReRe().setText("Nº "+CLASS_NAME);
      contenedor.getLabelClienteProveedor().setText("Proveedor");
      // <--- seteo de GUI
      try {
         UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
      //            UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      } catch (Exception ex) {
         Logger.getLogger(FacturaCompraJpaController.class.getName())
                          .log(Level.SEVERE, null, ex);
      }
      //set next nº Remesa
      setNextNumeroReRe();
      UTIL.loadComboBox(contenedor.getCbSucursal(),
                     new SucursalJpaController().findSucursalEntities(), false);
      UTIL.loadComboBox(contenedor.getCbCaja(),
                     new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);
      UTIL.loadComboBox(contenedor.getCbClienteProveedor(),
                     new ProveedorJpaController().findProveedorEntities(), true);
      UTIL.loadComboBox(contenedor.getCbCtaCtes(), null, false);

      contenedor.setListener(this);
      contenedor.setVisible(true);
    }

   public void mouseClicked(MouseEvent e) {
      if (buscador != null) {
         if(e.getClickCount() > 1) {
            setSelectedRemesa();
         }
      }
   }
   public void mousePressed(MouseEvent e) {}
   public void mouseEntered(MouseEvent e) {}
   public void mouseExited(MouseEvent e) {}
   public void mouseReleased(MouseEvent e) {}

   public void actionPerformed(ActionEvent e) {
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if(boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               checkConstraints();
               setEntityAndPersist();
               contenedor.showMessage(CLASS_NAME+" cargada..", CLASS_NAME, 1);
               limpiarDetalle();
               resetPanel();

            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName())
                            .log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("add")) {
             try {
                addEntregaToDetalle();
             } catch (MessageException ex) {
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
             } catch (Exception ex) {
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                Logger.getLogger(SucursalJpaController.class.getName())
                                .log(Level.SEVERE, null, ex);
             }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            delEntragaFromDetalle();
         } else if (boton.getName().equalsIgnoreCase("Print")) {

         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            resetPanel();
            limpiarDetalle();
         } else if(boton.getName().equalsIgnoreCase("buscarRERE")) {
            //inicializar buscador de Remesas
            initBuscadorReRe(contenedor, true);
            // a ver si eligió algo..
            if(rereSelected != null)
               setDatosCtaCte(rereSelected);

         } else if(boton.getName().equalsIgnoreCase("filtrarReRe")) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
               ex.printStackTrace();
            }
         } else if(boton.getName().equalsIgnoreCase("limpiarBuscadoR")) {
            // está en la GUI limpiarVentana();
         } else if(boton.getName().equalsIgnoreCase("anular")) {
            try {
               anularReRe(rereSelected);
               contenedor.showMessage("Remesa anulada", CLASS_NAME, 1);
               resetPanel();

            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
            }  catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
               ex.printStackTrace();
            }
         }
         return;
     // </editor-fold>

      } else if(e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
      // <editor-fold defaultstate="collapsed" desc="JComboBox">
         javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
         if (combo.getName() == null) {
            System.out.println("JComboBox.name = null");
            return;//chau...
         }

         if (combo.getName().equalsIgnoreCase("cbClienteProveedor")) {
            if (combo.getSelectedIndex() > 0) {
               cargarCtaCtes((Proveedor) combo.getSelectedItem());
            } else {
               //si no eligió nada.. vacia el combo de cta cte's
               UTIL.loadComboBox(contenedor.getCbCtaCtes(), null, false);
               limpiarDetalle();
            }

         } else if (combo.getName().equalsIgnoreCase("cbCtaCtes")) {
            try {
               setDatosCtaCte();
            } catch (NullPointerException ex) {
               //cuando no eligio una ctacte aún o el cliente/proveedor no tiene ninguna
            }
         }
      // </editor-fold>
      }
        
   }

   private void checkConstraints()  throws MessageException {
      if(contenedor.getDtm().getRowCount() < 1)
         throw new MessageException("No ha hecho ninguna entrega");

      if(contenedor.getDcFechaReRe() == null)
         throw new MessageException("Fecha de "+CLASS_NAME+" no válida");

   }

   private void setEntityAndPersist() {
      Remesa re = new Remesa();
      re.setId(Long.valueOf(contenedor.getTfCuarto() + contenedor.getTfOcteto()));
      re.setCaja((Caja) contenedor.getCbCaja().getSelectedItem());
      re.setSucursal((Sucursal) contenedor.getCbSucursal().getSelectedItem());
      re.setUsuario(UsuarioJpaController.getCurrentUser());
      re.setEstado(true);
      re.setFechaCarga(new java.util.Date());
      re.setFechaRemesa(contenedor.getDcFechaReRe());
      re.setHoraCarga(new java.util.Date());
      re.setMontoEntrega(Double.parseDouble(contenedor.getTfTotalPagado()));
      // 30% faster on ArrayList with initialCapacity
      re.setDetalleRemesaList(new ArrayList<DetalleRemesa>(contenedor.getDtm().getRowCount()));
      DefaultTableModel dtm = contenedor.getDtm();
      FacturaCompraJpaController fcc = new FacturaCompraJpaController();
      DetalleRemesa dr;
      for(int i = dtm.getRowCount() - 1; i > -1; i--) {
            dr = new DetalleRemesa();
            dr.setFacturaCompra(fcc.findFacturaCompra(
                    Long.parseLong(dtm.getValueAt(i, 0).toString()),
                    ((Proveedor)contenedor.getCbClienteProveedor().getSelectedItem())));
            dr.setObservacion(dtm.getValueAt(i, 1).toString());
            dr.setMontoEntrega(Double.parseDouble(dtm.getValueAt(i, 2).toString()));
            //dr.setRemesa(); <--- no hace falta...
            re.getDetalleRemesaList().add(dr);

            //actuliza saldo pagado de cada ctacte
            actualizarMontoEntrega(dr.getFacturaCompra(), dr.getMontoEntrega());
      }
      try {
         create(re);
      } catch (NoResultException ex) {
         ex.printStackTrace();
      } catch (PreexistingEntityException ex) {
         Logger.getLogger(RemesaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      } catch (Exception ex) {
         Logger.getLogger(RemesaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void actualizarMontoEntrega(FacturaCompra factu, double monto) {
      CtacteProveedor ctacte = new CtacteProveedorJpaController()
              .findCtacteProveedorByFactura(factu.getId());
      System.out.println("updatingMontoEntrega: CtaCte:"+ctacte.getId()
              +" -> Importe: "+ctacte.getImporte()+" Entregado:"+ctacte.getEntregado()+" + "+monto);

      ctacte.setEntregado(ctacte.getEntregado() + monto);
      if(ctacte.getImporte() == ctacte.getEntregado()) {
         ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getEstado());
         System.out.println("ctaCte PAGADA");
      }
      DAO.doMerge(ctacte);
   }

   private void limpiarDetalle() {
      limpiarDtmDetalle();
      contenedor.setTfImporte("0");
      contenedor.setTfEntrega("");
      contenedor.setTfObservacion("");
      contenedor.setTfSaldo("0");
      contenedor.setTfTotalPagado("0");
      selectedFechaReRe = null;
   }

   private void addEntregaToDetalle() throws MessageException {
      if(contenedor.getDcFechaReRe() == null)
         throw new MessageException("Debe especificar una fecha de "+CLASS_NAME+" antes");

      if(selectedCtaCte == null)
         throw new MessageException("No hay Factura seleccionada");

      if(contenedor.getDcFechaReRe().before(selectedCtaCte.getFechaCarga()))
         throw new MessageException("La fecha de la "+CLASS_NAME+" no puede ser anterior" +
                 "\n a la de la Cta Cte del Proveedor ("
                 +UTIL.DATE_FORMAT.format(selectedCtaCte.getFechaCarga())+")");

      // si ya se cargó un detalle de entrega
      // y sigue habiendo al menos UN detalle agregado (dtm no vacia)
      // ctrla que la fecha de ReRe siga siendo la misma
      if((selectedFechaReRe != null) && (contenedor.getDtm().getRowCount() > 0) &&
              (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(contenedor.getDcFechaReRe())))) {
                  throw new MessageException("La fecha de "+CLASS_NAME+" a sido cambiada" +
                          "\nAnterior: "+UTIL.DATE_FORMAT.format(selectedFechaReRe)
                          +"\nActual: "+UTIL.DATE_FORMAT.format(contenedor.getDcFechaReRe()));
      } else {
         selectedFechaReRe = contenedor.getDcFechaReRe();
      }
      FacturaCompra fc = selectedCtaCte.getFactura();
      double entrega;
      String observacion = contenedor.getTfObservacion();
      try {
         entrega = Double.parseDouble(contenedor.getTfEntrega());
         if( entrega <= 0)
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
         
//         if(entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) )
         if(entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) )
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");

      } catch (NumberFormatException e) {
         throw new MessageException("Monto de entrega no válido");
      }
      if(observacion.length() > 200)
         throw new MessageException("La Observación no puede superar los 200 caracteres");

      DefaultTableModel dtm = contenedor.getDtm();
      dtm.addRow(new Object[] {
         UTIL.AGREGAR_CEROS(String.valueOf(fc.getNumero()), 12),
         observacion,
         entrega
      });
      double totalEntregado = Double.valueOf(contenedor.getTfTotalPagado());
      contenedor.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format( totalEntregado + entrega));

   }

   private void delEntragaFromDetalle() {
      int selectedRow = contenedor.getjTable1().getSelectedRow();
      if(selectedRow > -1) {
         double entrega = Double.valueOf(contenedor.getDtm().getValueAt(selectedRow, 2).toString());
         double totalEntregado = Double.valueOf(contenedor.getTfTotalPagado());
         contenedor.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format( totalEntregado - entrega));
         contenedor.getDtm().removeRow(selectedRow);
      }
   }

   private void initBuscadorReRe(javax.swing.JDialog dialog, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.COMPRA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
      buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Proveedor", "Nº "+CLASS_NAME);
      buscador.setLocationRelativeTo(dialog);
      buscador.setListeners(this);
      UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorJpaController().findProveedorEntities(), true);
      UTIL.loadComboBox(buscador.getCbCaja(), new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      try {
         UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"Nº","Monto","Fecha","Sucursal","Caja","Usuario","Fecha/Hora (Sist)"},
              new int[]{50    ,30      ,40      ,50      ,50      ,50      ,70}
              );

      } catch (Exception ex) {
         Logger.getLogger(RemesaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      buscador.setVisible(true);
   }

   private void cargarCtaCtes(Proveedor proveedor) {
      limpiarDetalle();
      List<CtacteProveedor> ctacteProveedorPendientesList = new CtacteProveedorJpaController().
              findCtacteProveedorByProveedor(proveedor.getId(), Valores.PENDIENTE);
      UTIL.loadComboBox(contenedor.getCbCtaCtes(), ctacteProveedorPendientesList, false);

   }

   private void setDatosCtaCte() {
      try {
         selectedCtaCte = (CtacteProveedor)contenedor.getCbCtaCtes().getSelectedItem();
         contenedor.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
         contenedor.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
         contenedor.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(
                 selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
      } catch (ClassCastException ex) {
         selectedCtaCte = null;
         System.out.println("No se pudo caster a CtaCteProveedor -> "+ contenedor.getCbCtaCtes().getSelectedItem());
      }

   }

   private void limpiarDtmDetalle() {
      for( int i = contenedor.getDtm().getRowCount()-1; i > -1; i--) {
         contenedor.getDtm().removeRow(i);
      }
   }

   private void resetPanel() {
      contenedor.setDcFechaReRe(new java.util.Date());
      contenedor.getCbClienteProveedor().setSelectedIndex(0);
      setNextNumeroReRe();
      bloquearVentana(false);
   }

   private void armarQuery() throws MessageException {
      String query ="SELECT o.* FROM remesa o, proveedor p , caja c, detalle_remesa dr, factura_compra f, usuario u, sucursal s  " +
              " WHERE o.id = dr.remesa " +
              "   AND o.caja = c.id " +
//              "   AND o.usuario = u.id " +
              "   AND o.sucursal = s.id " +
              "   AND f.id = dr.factura_compra " +
              "   AND p.id = f.proveedor" +
              "   ";

      long numero;
      //filtro por nº de ReRe
      if(buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
            query += " AND o.id = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de "+CLASS_NAME+ " no válido");
         }
      }

      //filtro por nº de factura
      if(buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
            query += " AND f.numero = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de "+CLASS_NAME+ " no válido");
         }
      }
      if(buscador.getDcDesde() != null)
         query += " AND o.fecha_remesa >= '"+buscador.getDcDesde()+"'";
      if(buscador.getDcHasta() != null)
         query += " AND o.fecha_remesa <= '"+buscador.getDcHasta()+"'";
      if(buscador.getCbCaja().getSelectedIndex() > 0)
         query += " AND o.caja = "+((Caja)buscador.getCbCaja().getSelectedItem()).getId();
      if(buscador.getCbSucursal().getSelectedIndex() > 0)
         query += " AND o.sucursal = "+((Sucursal)buscador.getCbSucursal().getSelectedItem()).getId();
      if(buscador.isCheckAnuladaSelected())
         query += " AND o.estado = false";
      if(buscador.getCbClieProv().getSelectedIndex() > 0)
         query += " AND p.id = "+((Proveedor)buscador.getCbClieProv().getSelectedItem()).getId();

      query += " GROUP BY o.id, o.fecha_carga, o.hora_carga, o.monto_entrega, o.usuario, o.caja, o.sucursal, o.fecha_remesa, o.estado" +
              " ORDER BY o.id";
      System.out.println("QUERY: "+query);
      cargarDtmBuscador(query);
   }

   private void cargarDtmBuscador(String query) {
      buscador.dtmRemoveAll();
      DefaultTableModel dtm = buscador.getDtm();
      List<Remesa> l = DAO.getEntityManager().createNativeQuery(query, Remesa.class).getResultList();
      for (Remesa remesa : l) {
         dtm.addRow(new Object[] {
            remesa.getId(),
            remesa.getMontoEntrega(),
            UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()),
            remesa.getSucursal(),
            remesa.getCaja(),
            remesa.getUsuario(),
            UTIL.DATE_FORMAT.format(remesa.getFechaCarga())+" - "+ UTIL.TIME_FORMAT.format(remesa.getHoraCarga())
         });
      }
   }

   private void setSelectedRemesa() {
      int rowIndex = buscador.getjTable1().getSelectedRow();
      long remesaID = Long.valueOf(buscador.getjTable1().getValueAt(rowIndex, 0).toString());
      rereSelected = new RemesaJpaController().findRemesa(remesaID);
      if(rereSelected != null)
         buscador.dispose();


   }

   /**
    * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
    * detalles de la Remesa, imprimir y ANULAR, pero NO MODIFICAR
    * @param remesa
    */
   private void setDatosCtaCte(Remesa remesa) {
      bloquearVentana(true);
      String numero = UTIL.AGREGAR_CEROS(String.valueOf(remesa.getId()), 12);
      contenedor.setTfCuarto(numero.substring(0, 4));
      contenedor.setTfOcteto(numero.substring(4));
      
      //por no redundar en DATOOOOOOOOOSS...!!!
      Proveedor p = new FacturaCompraJpaController()
              .findFacturaCompra(remesa.getDetalleRemesaList().get(0)
              .getFacturaCompra().getId())
              .getProveedor();

      contenedor.setDcFechaReRe(remesa.getFechaRemesa());
      contenedor.setDcFechaCarga(remesa.getFechaCarga());

      //Uso los .toString por el 1er Item de los combos <Vacio> o <Elegir>
      // van a tirar error de ClassCastException
      UTIL.setSelectedItem(contenedor.getCbSucursal(), remesa.getSucursal().toString());
      UTIL.setSelectedItem(contenedor.getCbCaja(), remesa.getCaja().toString());
      UTIL.setSelectedItem(contenedor.getCbClienteProveedor(), p.toString());

      cargarDetalleReRe(remesa.getDetalleRemesaList());

      contenedor.setTfImporte(""); contenedor.setTfPagado(""); contenedor.setTfSaldo("");
      contenedor.setTfTotalPagado(String.valueOf(remesa.getMontoEntrega()));
   }

   private void cargarDetalleReRe(List<DetalleRemesa> detalleRemesaList) {
      limpiarDtmDetalle();
      DefaultTableModel dtm = contenedor.getDtm();
      for (DetalleRemesa r : detalleRemesaList) {
         dtm.addRow(new Object[] {
            UTIL.AGREGAR_CEROS(String.valueOf(r.getFacturaCompra().getNumero()), 12),
            r.getObservacion(),
            r.getMontoEntrega()
         });
      }
   }

   public void focusGained(FocusEvent e) {
      //..........
   }

   public void focusLost(FocusEvent e) {
      if(buscador != null) {
         if(e.getSource().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (JTextField) e.getSource();
            if(tf.getName().equalsIgnoreCase("tfocteto")) {
               if(buscador.getTfOcteto().length() > 0) {
                  buscador.setTfOcteto(UTIL.AGREGAR_CEROS(buscador.getTfOcteto(), 8));
               }
            } else if(tf.getName().equalsIgnoreCase("tfFactu8")) {

            }
         }
      }

   }

   private void bloquearVentana(boolean b) {
      contenedor.getbAnular().setEnabled(b);
      contenedor.getbImprimir().setEnabled(b);
      contenedor.getBtnADD().setEnabled(!b);
      contenedor.getBtnDEL().setEnabled(!b);
      contenedor.getbAceptar().setEnabled(!b);
      contenedor.getCbCtaCtes().setEnabled(!b);
      contenedor.getCbCaja().setEnabled(!b);
      contenedor.getCbSucursal().setEnabled(!b);
      contenedor.getCbClienteProveedor().setEnabled(!b);
      contenedor.getDcFechaReRe(!b);
   }

   private void setNextNumeroReRe() {
      Long nextRemesa = getNextNumeroRemesa();
      String factuString = UTIL.AGREGAR_CEROS(nextRemesa.toString(), 12);
      contenedor.setTfCuarto(factuString.substring(0, 4));
      contenedor.setTfOcteto(factuString.substring(4));
   }

   /**
    * La anulación de una Remesa, resta a <code>CtaCteProveedor.entregado</code>
    * los pagos/entregas (parciales/totales) realizados de cada DetalleRemesa y
    * cambia <code>Remesa.estado = false<code>
    * @throws MessageException
    * @throws IllegalOrphanException
    * @throws NonexistentEntityException
    */
   public void anularReRe(Remesa remesa) throws MessageException, Exception {
      EntityManager em = getEntityManager();
      if(remesa == null)
         throw new MessageException("Remesa is NULL");
      if(!remesa.getEstado())
         throw new MessageException("Esta "+CLASS_NAME+" ya está anulada");
      
      List<DetalleRemesa> detalleRemesaList = remesa.getDetalleRemesaList();
      CtacteProveedor ctaCteProveedor;
      try {
         em.getTransaction().begin();
         for (DetalleRemesa dr : detalleRemesaList) {
            //se resta la entrega ($) que implicaba este detalle con respecto a la factura
            ctaCteProveedor = new CtacteProveedorJpaController().findCtacteProveedorByFactura(dr.getFacturaCompra().getId());
            ctaCteProveedor.setEntregado(ctaCteProveedor.getEntregado() - dr.getMontoEntrega());
            // y si había sido pagada en su totalidad..
            if(ctaCteProveedor.getEstado() == Valores.CtaCteEstado.PAGADA.getEstado())
               ctaCteProveedor.setEstado(Valores.CtaCteEstado.PENDIENTE.getEstado());
            em.merge(ctaCteProveedor);
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         if(em.getTransaction().isActive())
            em.getTransaction().rollback();
         throw ex;
      } finally {
         if(em != null) em.close();
      }
      remesa.setEstado(false);
      DAO.doMerge(remesa);
   }

}
