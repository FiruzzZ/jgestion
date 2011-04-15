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
@Table(name = "detalle_nota_credito")
@NamedQueries({
   @NamedQuery(name = "DetalleNotaCredito.findAll", query = "SELECT d FROM DetalleNotaCredito d"),
   @NamedQuery(name = "DetalleNotaCredito.findById", query = "SELECT d FROM DetalleNotaCredito d WHERE d.id = :id")
})
public class DetalleNotaCredito implements Serializable {

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
   @JoinColumn(name = "nota_credito", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private NotaCredito notaCredito;
   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Producto producto;

   public DetalleNotaCredito() {
   }

   public DetalleNotaCredito(Integer id) {
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

   public double getPrecioUnitario() {
      return precioUnitario;
   }

   public void setPrecioUnitario(double precioUnitario) {
      this.precioUnitario = precioUnitario;
   }

   public NotaCredito getNotaCredito() {
      return notaCredito;
   }

   public void setNotaCredito(NotaCredito notaCredito) {
      this.notaCredito = notaCredito;
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
      if (!(object instanceof DetalleNotaCredito)) {
         return false;
      }
      DetalleNotaCredito other = (DetalleNotaCredito) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.DetalleNotaCredito[id=" + id + ",Producto=" + producto.getNombre() + ", Cantidad=" + cantidad + ", PrecioU=" + precioUnitario + "]";
   }
}
