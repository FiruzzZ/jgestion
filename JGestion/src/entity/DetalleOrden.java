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
@Table(name = "detalle_orden")
@NamedQueries({
   @NamedQuery(name = "DetalleOrden.findAll", query = "SELECT d FROM DetalleOrden d"),
   @NamedQuery(name = "DetalleOrden.findById", query = "SELECT d FROM DetalleOrden d WHERE d.id = :id")
})
public class DetalleOrden implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "cantidad", nullable = false)
   private int cantidad;
   @JoinColumn(name = "orden", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Orden orden;
   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Producto producto;

   public DetalleOrden() {
   }

   public DetalleOrden(Integer id) {
      this.id = id;
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

   public Orden getOrden() {
      return orden;
   }

   public void setOrden(Orden orden) {
      this.orden = orden;
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
      if (!(object instanceof DetalleOrden)) {
         return false;
      }
      DetalleOrden other = (DetalleOrden) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.DetalleOrden[id=" + id + "]";
   }
}
