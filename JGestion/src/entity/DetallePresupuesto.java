
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
@Table(name = "detalle_presupuesto")
@NamedQueries({
   @NamedQuery(name = "DetallePresupuesto.findAll", query = "SELECT d FROM DetallePresupuesto d"),
   @NamedQuery(name = "DetallePresupuesto.findById", query = "SELECT d FROM DetallePresupuesto d WHERE d.id = :id")
})
public class DetallePresupuesto implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "cantidad", nullable = false)
   private int cantidad;
   @Basic(optional = false)
   @Column(name = "precio_unitario", nullable = false)
   private double precioUnitario;
   @Basic(optional = false)
   @Column(name = "tipo_desc", nullable = false)
   private int tipoDesc;
   @Basic(optional = false)
   @Column(name = "descuento", nullable = false)
   private double descuento;
   @JoinColumn(name = "presupuesto", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Presupuesto presupuesto;
   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Producto producto;

   public DetallePresupuesto() {
   }

   public DetallePresupuesto(Integer id) {
      this.id = id;
   }

   public DetallePresupuesto(Integer id, int cantidad, double precioUnitario, int tipoDesc, double descuento) {
      this.id = id;
      this.cantidad = cantidad;
      this.precioUnitario = precioUnitario;
      this.tipoDesc = tipoDesc;
      this.descuento = descuento;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public int getCantidad() {
      return cantidad;
   }

   public void setCantidad(int cantidad) {
      this.cantidad = cantidad;
   }

   public double getPrecioUnitario() {
      return precioUnitario;
   }

   public void setPrecioUnitario(double precioUnitario) {
      this.precioUnitario = precioUnitario;
   }

   public int getTipoDesc() {
      return tipoDesc;
   }

   public void setTipoDesc(int tipoDesc) {
      this.tipoDesc = tipoDesc;
   }

   public double getDescuento() {
      return descuento;
   }

   public void setDescuento(double descuento) {
      this.descuento = descuento;
   }

   public Presupuesto getPresupuesto() {
      return presupuesto;
   }

   public void setPresupuesto(Presupuesto presupuesto) {
      this.presupuesto = presupuesto;
   }

   public Producto getProducto() {
      return producto;
   }

   public void setProducto(Producto producto) {
      this.producto = producto;
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
      if (!(object instanceof DetallePresupuesto)) {
         return false;
      }
      DetallePresupuesto other = (DetallePresupuesto) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.DetallePresupuesto[id=" + id + "]";
   }

}
