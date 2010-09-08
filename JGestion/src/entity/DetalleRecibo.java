
package entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_recibo")
@NamedQueries({
   @NamedQuery(name = "DetalleRecibo.findAll", query = "SELECT d FROM DetalleRecibo d"),
   @NamedQuery(name = "DetalleRecibo.findById", query = "SELECT d FROM DetalleRecibo d WHERE d.id = :id"),
   @NamedQuery(name = "DetalleRecibo.findByFacturaVenta", query = "SELECT d FROM DetalleRecibo d WHERE d.facturaVenta = :facturaVenta")
})
public class DetalleRecibo implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "monto_entrega", nullable = false)
   private double montoEntrega;
   @Column(name = "observacion", length = 200)
   private String observacion;
   @JoinColumn(name = "factura_venta", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private FacturaVenta facturaVenta;
   @JoinColumn(name = "recibo", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Recibo recibo;
   @Column(nullable = false)
   private boolean anulado;

   public DetalleRecibo() {
   }

   public DetalleRecibo(Integer id) {
      this.id = id;
   }

   public DetalleRecibo(Integer id, double montoEntrega, boolean anulado) {
      this.id = id;
      this.montoEntrega = montoEntrega;
      this.anulado = anulado;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public double getMontoEntrega() {
      return montoEntrega;
   }

   public void setMontoEntrega(double montoEntrega) {
      this.montoEntrega = montoEntrega;
   }

   public String getObservacion() {
      return observacion;
   }

   public void setObservacion(String observacion) {
      this.observacion = observacion;
   }

   public FacturaVenta getFacturaVenta() {
      return facturaVenta;
   }

   public void setFacturaVenta(FacturaVenta facturaVenta) {
      this.facturaVenta = facturaVenta;
   }

   public Recibo getRecibo() {
      return recibo;
   }

   public void setRecibo(Recibo recibo) {
      this.recibo = recibo;
   }

   public boolean isAnulado() {
      return anulado;
   }

   public void setAnulado(boolean anulado) {
      this.anulado = anulado;
   }

   @Override
   public int hashCode() {
      int hash = 0;
      hash += (id != null ? id.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object object) {
      // TODO: Warning - this method won't work in the case the id fields are not set
      if (!(object instanceof DetalleRecibo)) {
         return false;
      }
      DetalleRecibo other = (DetalleRecibo) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.DetalleRecibo[id=" + id + "]";
   }
}
