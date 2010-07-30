package controller;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrador
 */
public final class Valores {

   /**
    * estados posibles de una CtaCte (Proveedor o Cliente)
    */
   public final static short PENDIENTE = 1, PAGADA = 2, ANULADA = 3;

   /**
    * Formas de pago usadas en operaciones Venta y Compras
    */
   public static enum FormaPago {
      CONTADO (1,"CONTADO")
      ,CTA_CTE (2, "CTA CTE")
//    ,CHEQUE (3, "CHEQUE")
      ;

      static FormaPago getFormasDePago(int formaPagoID) {
         FormaPago fp = null;
         for (FormaPago formaPago : formasDePago) {
            if(formaPago.getId() == formaPagoID) {
               System.out.println("1er return..");
               return formaPago;
            }
         }
         System.out.println("2do return...");
         return fp;
      }

      private final int id;
      private final String nombre;
      private static final List<FormaPago> formasDePago = new ArrayList<FormaPago>();

      static {
         for (FormaPago formaPago : FormaPago.values()) {
            formasDePago.add(formaPago);
         }
      }
      private FormaPago(int id, String nombre) {
         System.out.println("Constructor FormaPago("+id+", "+nombre+")");
         this.id = id;
         this.nombre = nombre;
      }

      public int getId() {
         return this.id;
      }

      public String getNombre() {
         return this.nombre;
      }

      public static List<FormaPago> getFormasDePago() {
         //retorna una copia de las formas de pago
         return new ArrayList<FormaPago>(formasDePago);
      }

      @Override
      public String toString() {
         return this.nombre;
      }

   }

   public enum CtaCteEstado {
      PENDIENTE (1,"PENDIENTE"),
      PAGADA (2,"PAGADA"),
      ANULADA (3,"ANULADA");

      private final short estado;
      private final String nombre;

      private CtaCteEstado(int i, String nombre) {
         this.estado = (short) i;
         this.nombre = nombre;
      }

      public short getEstado() {
         return this.estado;
      }

      @Override
      public String toString() {
         return this.nombre;
      }

//      public static CtaCteEstado getANULADA() {
//         return ANULADA;
//      }
//
//      public static CtaCteEstado getPAGADA() {
//         return PAGADA;
//      }
//
//      public static CtaCteEstado getPENDIENTE() {
//         return PENDIENTE;
//      }
//

   }//Enum CtaCteEstado

}
