/*
 * JFP.java
 *
 * Created on 19/11/2009, 15:32:19
 */

package gui;

import controller.*;
import controller.exceptions.MessageException;
import entity.Usuario;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author FiruzzzzzZ
 */
public class JFP extends javax.swing.JFrame {
   private final String VERSION = "JGestion 1.0207";
   private final String ICON_IMAGE = "/iconos/kf.png";
   public static Usuario CURRENT_USER;

   static {
      try {
//         javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
         javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//         javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      } catch (UnsupportedLookAndFeelException ex) {
         JOptionPane.showMessageDialog(null, "El Sistema Operativo no soporta el L&F predeterminado de la aplicación.");
         ex.printStackTrace();
      } catch (Exception e) {
         System.out.println("se pudrió el LookAndFeel");
      }
   }

   /** Creates new form JFP */
   public JFP() {
      loginUser();
      initComponents();
      jMenuItem11.setVisible(false); // Menú -> Datos Generales -> Contribuyente
      // Menú -> Datos Generales -> Contribuyente
      this.setIconImage(java.awt.Toolkit.getDefaultToolkit()
                              .createImage(getClass().getResource(ICON_IMAGE)));
      this.setTitle(VERSION);
      this.setLocation((int) (this.getLocation().getX() + 200),
                       (int) (this.getLocation().getY() + 100));
   }

   private void loginUser() {
        if(this.isVisible())
            this.setVisible(false);
        new UsuarioJpaController().initLogin(this);
        if(UsuarioJpaController.getCurrentUser() == null){
            DAO.getEntityManager().close();
            Runtime.getRuntime().exit(0);
        } else {
           this.setVisible(true);
        }

    }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jLabel1 = new javax.swing.JLabel();
      jMenuBar1 = new javax.swing.JMenuBar();
      jMenu1 = new javax.swing.JMenu();
      jMenuItem23 = new javax.swing.JMenuItem();
      menuItemSalir = new javax.swing.JMenuItem();
      jMenu2 = new javax.swing.JMenu();
      jMenuItem2 = new javax.swing.JMenuItem();
      jMenuItem14 = new javax.swing.JMenuItem();
      jMenuItem5 = new javax.swing.JMenuItem();
      jMenuItem6 = new javax.swing.JMenuItem();
      jMenu11 = new javax.swing.JMenu();
      jMenuItem18 = new javax.swing.JMenuItem();
      jMenuItem34 = new javax.swing.JMenuItem();
      jMenuItem35 = new javax.swing.JMenuItem();
      jMenuItem36 = new javax.swing.JMenuItem();
      jMenu3 = new javax.swing.JMenu();
      jMenuItem4 = new javax.swing.JMenuItem();
      jMenuItem1 = new javax.swing.JMenuItem();
      jMenuItem3 = new javax.swing.JMenuItem();
      jMenuItem28 = new javax.swing.JMenuItem();
      jMenu5 = new javax.swing.JMenu();
      jMenuItem7 = new javax.swing.JMenuItem();
      jMenuItem17 = new javax.swing.JMenuItem();
      jMenuItem12 = new javax.swing.JMenuItem();
      jMenuItem20 = new javax.swing.JMenuItem();
      jMenu6 = new javax.swing.JMenu();
      jMenuItem8 = new javax.swing.JMenuItem();
      jMenu7 = new javax.swing.JMenu();
      jMenuItem9 = new javax.swing.JMenuItem();
      jMenu10 = new javax.swing.JMenu();
      jMenuItem25 = new javax.swing.JMenuItem();
      jMenuItem26 = new javax.swing.JMenuItem();
      jMenuItem32 = new javax.swing.JMenuItem();
      jMenu8 = new javax.swing.JMenu();
      jMenuItem30 = new javax.swing.JMenuItem();
      jMenuItem31 = new javax.swing.JMenuItem();
      jMenuItem21 = new javax.swing.JMenuItem();
      jMenu9 = new javax.swing.JMenu();
      jMenuItem22 = new javax.swing.JMenuItem();
      jMenuItem19 = new javax.swing.JMenuItem();
      jMenuItem27 = new javax.swing.JMenuItem();
      jMenuItem13 = new javax.swing.JMenuItem();
      jMenuItem11 = new javax.swing.JMenuItem();
      jSeparator1 = new javax.swing.JSeparator();
      jMenuItem10 = new javax.swing.JMenuItem();
      jMenuItem15 = new javax.swing.JMenuItem();
      jMenu4 = new javax.swing.JMenu();
      jMenuItem16 = new javax.swing.JMenuItem();
      jMenuItem24 = new javax.swing.JMenuItem();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
            formWindowClosing(evt);
         }
      });

      jLabel1.setText("<html>\n<b>F2</b> - Presupuesto\n<b>Alt + F3</b> - Cerrar sesión\n</html>");

      jMenu1.setMnemonic('a');
      jMenu1.setText("Archivo");

      jMenuItem23.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.ALT_MASK));
      jMenuItem23.setText("Cerrar sesión");
      jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem23ActionPerformed(evt);
         }
      });
      jMenu1.add(jMenuItem23);

      menuItemSalir.setText("Salir");
      menuItemSalir.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            menuItemSalirActionPerformed(evt);
         }
      });
      jMenu1.add(menuItemSalir);

      jMenuBar1.add(jMenu1);

      jMenu2.setMnemonic('v');
      jMenu2.setText("Ventas");

      jMenuItem2.setText("Facturacion");
      jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem2ActionPerformed(evt);
         }
      });
      jMenu2.add(jMenuItem2);

      jMenuItem14.setText("Recibos");
      jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem14ActionPerformed(evt);
         }
      });
      jMenu2.add(jMenuItem14);

      jMenuItem5.setText("Remito");
      jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem5ActionPerformed(evt);
         }
      });
      jMenu2.add(jMenuItem5);

      jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
      jMenuItem6.setText("Presupuesto");
      jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem6ActionPerformed(evt);
         }
      });
      jMenu2.add(jMenuItem6);

      jMenu11.setText("Buscar..");

      jMenuItem18.setText("Facturas");
      jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem18ActionPerformed(evt);
         }
      });
      jMenu11.add(jMenuItem18);

      jMenuItem34.setText("Recibos");
      jMenuItem34.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem34ActionPerformed(evt);
         }
      });
      jMenu11.add(jMenuItem34);

      jMenuItem35.setText("Remitos");
      jMenuItem35.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem35ActionPerformed(evt);
         }
      });
      jMenu11.add(jMenuItem35);

      jMenuItem36.setText("Presupuestos");
      jMenuItem36.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem36ActionPerformed(evt);
         }
      });
      jMenu11.add(jMenuItem36);

      jMenu2.add(jMenu11);

      jMenuBar1.add(jMenu2);

      jMenu3.setMnemonic('c');
      jMenu3.setText("Compras");

      jMenuItem4.setText("Facturas");
      jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem4ActionPerformed(evt);
         }
      });
      jMenu3.add(jMenuItem4);

      jMenuItem1.setText("Remesas");
      jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem1ActionPerformed(evt);
         }
      });
      jMenu3.add(jMenuItem1);

      jMenuItem3.setText("Orden de compra");
      jMenuItem3.setEnabled(false);
      jMenu3.add(jMenuItem3);

      jMenuItem28.setText("Buscar..");
      jMenuItem28.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem28ActionPerformed(evt);
         }
      });
      jMenu3.add(jMenuItem28);

      jMenuBar1.add(jMenu3);

      jMenu5.setMnemonic('r');
      jMenu5.setText("Productos");

      jMenuItem7.setText("ABM Productos");
      jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem7ActionPerformed(evt);
         }
      });
      jMenu5.add(jMenuItem7);

      jMenuItem17.setText("Marcas");
      jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem17ActionPerformed(evt);
         }
      });
      jMenu5.add(jMenuItem17);

      jMenuItem12.setText("Rubros");
      jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem12ActionPerformed(evt);
         }
      });
      jMenu5.add(jMenuItem12);

      jMenuItem20.setText("Lista de Precios");
      jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem20ActionPerformed(evt);
         }
      });
      jMenu5.add(jMenuItem20);

      jMenuBar1.add(jMenu5);

      jMenu6.setMnemonic('e');
      jMenu6.setText("Clientes");

      jMenuItem8.setText("ABM Cliente");
      jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem8ActionPerformed(evt);
         }
      });
      jMenu6.add(jMenuItem8);

      jMenuBar1.add(jMenu6);

      jMenu7.setMnemonic('s');
      jMenu7.setText("Proveedores");

      jMenuItem9.setText("ABM Proveedor");
      jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem9ActionPerformed(evt);
         }
      });
      jMenu7.add(jMenuItem9);

      jMenuBar1.add(jMenu7);

      jMenu10.setMnemonic('t');
      jMenu10.setText("Tesorería");

      jMenuItem25.setText("Cierre cajas");
      jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem25ActionPerformed(evt);
         }
      });
      jMenu10.add(jMenuItem25);

      jMenuItem26.setText("Movimento entre Cajas");
      jMenuItem26.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem26ActionPerformed(evt);
         }
      });
      jMenu10.add(jMenuItem26);

      jMenuItem32.setText("Movimientos varios");
      jMenuItem32.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem32ActionPerformed(evt);
         }
      });
      jMenu10.add(jMenuItem32);

      jMenu8.setText("Cta. Cte.");

      jMenuItem30.setText("Clientes");
      jMenuItem30.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem30ActionPerformed(evt);
         }
      });
      jMenu8.add(jMenuItem30);

      jMenuItem31.setText("Proveedores");
      jMenu8.add(jMenuItem31);

      jMenu10.add(jMenu8);

      jMenuItem21.setText("ABM Cajas");
      jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem21ActionPerformed(evt);
         }
      });
      jMenu10.add(jMenuItem21);

      jMenuBar1.add(jMenu10);

      jMenu9.setMnemonic('d');
      jMenu9.setText("Datos Generales");

      jMenuItem22.setText("Datos de la empresa");
      jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem22ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem22);

      jMenuItem19.setText("Sucursales");
      jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem19ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem19);

      jMenuItem27.setText("IVAs");
      jMenuItem27.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem27ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem27);

      jMenuItem13.setText("Unidades de medición");
      jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem13ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem13);

      jMenuItem11.setText("Contribuyentes");
      jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem11ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem11);
      jMenu9.add(jSeparator1);

      jMenuItem10.setText("Departamentos");
      jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem10ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem10);

      jMenuItem15.setText("Municipios");
      jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem15ActionPerformed(evt);
         }
      });
      jMenu9.add(jMenuItem15);

      jMenuBar1.add(jMenu9);

      jMenu4.setMnemonic('u');
      jMenu4.setText("Usuarios");

      jMenuItem16.setText("ABM Usuario");
      jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem16ActionPerformed(evt);
         }
      });
      jMenu4.add(jMenuItem16);

      jMenuItem24.setText("Cambiar contraseña");
      jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItem24ActionPerformed(evt);
         }
      });
      jMenu4.add(jMenuItem24);

      jMenuBar1.add(jMenu4);

      setJMenuBar(jMenuBar1);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(42, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(359, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
       new ProductoJpaController().initContenedor(this, true, false);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
       try {
          new ProveedorJpaController().initContenedor(this, true);
          refreshConnectionDB();
       } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, ex.getMessage());
          Logger.getLogger(JFP.class.getName()).log(Level.SEVERE, null, ex);
       }

    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
       new FacturaCompraJpaController().initJDFacturaCompra(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

   //Datos Gral -> Ubicación
    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
       new DepartamentoJpaController().initContenedor(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

   //Rubro->Proveedores
    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
       try {
          new RubroJpaController().initABM(this, true);
          refreshConnectionDB();
       } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, ex.getMessage());
       }
    }//GEN-LAST:event_jMenuItem12ActionPerformed

   //Rubro->Productos..    //Rubros->Clientes
   //abm unidades de medida
    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
       try {
          new UnidadmedidaJpaController().initABM(this, true);
          refreshConnectionDB();
       } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, ex.getMessage());
          Logger.getLogger(JFP.class.getName()).log(Level.SEVERE, null, ex);
       }
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
       new MarcaJpaController().initJD(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
       new SucursalJpaController().initContenedor(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
       new ContribuyenteJpaController().initABM(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
       new MunicipioJpaController().initContenedor(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
       new ClienteJpaController().initContenedor(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed
       new ListaPreciosJpaController().initContenedor(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
      try {
         new FacturaVentaJpaController().initFacturaVenta(this, true, null, 1, true);
      } catch (MessageException ex) {
         showError(ex.getMessage());
      }
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed
       new CajaJpaController().initABM(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
       try {
          new DatosEmpresaJpaController().initJD(this, true);
          refreshConnectionDB();
       } catch (Exception ex) {
          Logger.getLogger(JFP.class.getName()).log(Level.SEVERE, null, ex);
       }
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed
       new UsuarioJpaController().initCambiarPass(this);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem24ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
       new RemesaJpaController().initContenedor(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed
       new CajaMovimientosJpaController().initCierreCaja(this, false);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem25ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
       new ReciboJpaController().initContenedor(this, true, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed
       new CajaMovimientosJpaController().initCajaToCaja(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem26ActionPerformed

    private void jMenuItem27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem27ActionPerformed
       new IvaJpaController().initABM(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem27ActionPerformed

    private void menuItemSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSalirActionPerformed
       System.out.println("login OFF.........");
       cerrandoAplicacion();
    }//GEN-LAST:event_menuItemSalirActionPerformed

    private void jMenuItem28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem28ActionPerformed
      new FacturaCompraJpaController().initBuscador(this);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem28ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
      new UsuarioJpaController().initContenedor(this);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      System.out.println("formWindowClosing .....");
      cerrandoAplicacion();
    }//GEN-LAST:event_formWindowClosing

    private void jMenuItem32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem32ActionPerformed
      new CajaMovimientosJpaController().initMovimientosVarios(this, false);
      refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem32ActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
      UsuarioJpaController.cerrarSessionActual();
      loginUser();
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jMenuItem30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem30ActionPerformed
      new CtacteClienteJpaController().initResumenCtaCte(this, false);
      refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem30ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
      try {
         new PresupuestoJpaController().initPresupuesto(this, false, true);
      } catch (MessageException ex) {
         showError(ex.getMessage());
      }
      refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
      try {
         new RemitoJpaController().initRemito(this, false);
      } catch (MessageException ex) {
         showError(ex.getMessage());
      }
      refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
       new FacturaVentaJpaController().initBuscador(this, true);
       refreshConnectionDB();
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem36ActionPerformed
      new PresupuestoJpaController().initBuscador(this);
    }//GEN-LAST:event_jMenuItem36ActionPerformed

    private void jMenuItem34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem34ActionPerformed
      new ReciboJpaController().initBuscador(this, true);
    }//GEN-LAST:event_jMenuItem34ActionPerformed

    private void jMenuItem35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem35ActionPerformed
      new RemitoJpaController().initBuscador(this, true, true);
    }//GEN-LAST:event_jMenuItem35ActionPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel jLabel1;
   private javax.swing.JMenu jMenu1;
   private javax.swing.JMenu jMenu10;
   private javax.swing.JMenu jMenu11;
   private javax.swing.JMenu jMenu2;
   private javax.swing.JMenu jMenu3;
   private javax.swing.JMenu jMenu4;
   private javax.swing.JMenu jMenu5;
   private javax.swing.JMenu jMenu6;
   private javax.swing.JMenu jMenu7;
   private javax.swing.JMenu jMenu8;
   private javax.swing.JMenu jMenu9;
   private javax.swing.JMenuBar jMenuBar1;
   private javax.swing.JMenuItem jMenuItem1;
   private javax.swing.JMenuItem jMenuItem10;
   private javax.swing.JMenuItem jMenuItem11;
   private javax.swing.JMenuItem jMenuItem12;
   private javax.swing.JMenuItem jMenuItem13;
   private javax.swing.JMenuItem jMenuItem14;
   private javax.swing.JMenuItem jMenuItem15;
   private javax.swing.JMenuItem jMenuItem16;
   private javax.swing.JMenuItem jMenuItem17;
   private javax.swing.JMenuItem jMenuItem18;
   private javax.swing.JMenuItem jMenuItem19;
   private javax.swing.JMenuItem jMenuItem2;
   private javax.swing.JMenuItem jMenuItem20;
   private javax.swing.JMenuItem jMenuItem21;
   private javax.swing.JMenuItem jMenuItem22;
   private javax.swing.JMenuItem jMenuItem23;
   private javax.swing.JMenuItem jMenuItem24;
   private javax.swing.JMenuItem jMenuItem25;
   private javax.swing.JMenuItem jMenuItem26;
   private javax.swing.JMenuItem jMenuItem27;
   private javax.swing.JMenuItem jMenuItem28;
   private javax.swing.JMenuItem jMenuItem3;
   private javax.swing.JMenuItem jMenuItem30;
   private javax.swing.JMenuItem jMenuItem31;
   private javax.swing.JMenuItem jMenuItem32;
   private javax.swing.JMenuItem jMenuItem34;
   private javax.swing.JMenuItem jMenuItem35;
   private javax.swing.JMenuItem jMenuItem36;
   private javax.swing.JMenuItem jMenuItem4;
   private javax.swing.JMenuItem jMenuItem5;
   private javax.swing.JMenuItem jMenuItem6;
   private javax.swing.JMenuItem jMenuItem7;
   private javax.swing.JMenuItem jMenuItem8;
   private javax.swing.JMenuItem jMenuItem9;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JMenuItem menuItemSalir;
   // End of variables declaration//GEN-END:variables

   private void refreshConnectionDB() {
      System.out.println("refreshing (clear) .......................................DB");
      DAO.getEntityManager().clear();
   }

   private void cerrandoAplicacion() {
      System.out.println("cerrando DAO");
         DAO.getEntityManager().close();
      System.out.println("exit(0)");
         Runtime.getRuntime().exit(0);
   }

   private void showError(String message) {
      JOptionPane.showMessageDialog(this, message);
   }

}
