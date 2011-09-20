package entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_venta")
@NamedQueries({
   @NamedQuery(name = "DetalleVenta.findAll", query = "SELECT d FROM DetalleVenta d"),
   @NamedQuery(name = "DetalleVenta.findById", query = "SELECT d FROM DetalleVenta d WHERE d.id = :id")
})
public class DetalleVenta implements Serializable {

   private static final long serialVersionUID = 1L;
   private Integer id;
   private int cantidad;
   private Double precioUnitario;
   private int tipoDesc;
   /**
    * Descuento ya está multiplicado por {@link DetalleVenta#cantidad}
    */
   private Double descuento;
   private FacturaVenta factura;
   private Producto producto;
   private HistorialOfertas oferta;

   public DetalleVenta() {
   }

   public DetalleVenta(Integer id) {
      this.id = id;
   }

   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   @Basic(optional = false)
   @Column(name = "cantidad", nullable = false)
   public int getCantidad() {
      return cantidad;
   }

   public void setCantidad(int cantidad) {
      this.cantidad = cantidad;
   }

   @Basic(optional = false)
   @Column(name = "precio_unitario", nullable = false, precision = 9, scale = 2)
   public Double getPrecioUnitario() {
      return precioUnitario;
   }

   public void setPrecioUnitario(Double precioUnitario) {
      this.precioUnitario = precioUnitario;
   }

   @Basic(optional = false)
   @Column(name = "tipo_desc", nullable = false)
   public int getTipoDesc() {
      return tipoDesc;
   }

   public void setTipoDesc(int tipoDesc) {
      this.tipoDesc = tipoDesc;
   }

   @Basic(optional = false)
   @Column(name = "descuento", nullable = false, precision = 9, scale = 2)
   public Double getDescuento() {
      return descuento;
   }

   public void setDescuento(Double descuento) {
      this.descuento = descuento;
   }

   @JoinColumn(name = "factura", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   public FacturaVenta getFactura() {
      return factura;
   }

   public void setFactura(FacturaVenta factura) {
      this.factura = factura;
   }

   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   public Producto getProducto() {
      return producto;
   }

   public void setProducto(Producto producto) {
      this.producto = producto;
   }

   @JoinColumn(name = "oferta", referencedColumnName = "id")
   @ManyToOne
   public HistorialOfertas getOferta() {
      return oferta;
   }

   public void setOferta(HistorialOfertas oferta) {
      this.oferta = oferta;
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
      if (!(object instanceof DetalleVenta)) {
         return false;
      }
      DetalleVenta other = (DetalleVenta) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "Detalle: " + this.getId().toString();
   }
}
