package jgestion.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "productos_web", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"producto"})
})
@NamedQueries({
   @NamedQuery(name = "ProductosWeb.findAll", query = "SELECT p FROM ProductosWeb p"),
   @NamedQuery(name = "ProductosWeb.findById", query = "SELECT p FROM ProductosWeb p WHERE p.id = :id")
})
public class ProductosWeb implements Serializable {

   private static final long serialVersionUID = 1L;
   //Estados del Producto en la tabla
   public static final short ALTA = 1;
   public static final short BAJA = 3;
   public static final short MODIFICADO = 2;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "precio", nullable = false)
   private double precio;
   @Basic(optional = false)
   @Column(name = "destacado", nullable = false)
   private boolean destacado;
   @Basic(optional = false)
   @Column(name = "oferta", nullable = false)
   private boolean oferta;
   @Column(name = "inicio_oferta")
   @Temporal(TemporalType.DATE)
   private Date inicioOferta;
   @Column(name = "fin_oferta")
   @Temporal(TemporalType.DATE)
   private Date finOferta;
   @Basic(optional = false)
   @Column(name = "estado", nullable = false)
   private short estado;
   @Basic(optional = false)
   @Column(name = "chequeado", nullable = false)
   private short chequeado;
   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false, unique = true)
   @OneToOne
   private Producto producto;

   public ProductosWeb() {
   }

   public ProductosWeb(Integer id) {
      this.id = id;
   }

   public ProductosWeb(Integer id, double precio, boolean destacado, boolean oferta, short estado, short chequeado) {
      this.id = id;
      this.precio = precio;
      this.destacado = destacado;
      this.oferta = oferta;
      this.estado = estado;
      this.chequeado = chequeado;
   }

   public double getPrecio() {
      return precio;
   }

   public void setPrecio(double precio) {
      this.precio = precio;
   }

   public boolean getDestacado() {
      return destacado;
   }

   public void setDestacado(boolean destacado) {
      this.destacado = destacado;
   }

   public boolean getOferta() {
      return oferta;
   }

   public void setOferta(boolean oferta) {
      this.oferta = oferta;
   }

   public Date getInicioOferta() {
      return inicioOferta;
   }

   public void setInicioOferta(Date inicioOferta) {
      this.inicioOferta = inicioOferta;
   }

   public Date getFinOferta() {
      return finOferta;
   }

   public void setFinOferta(Date finOferta) {
      this.finOferta = finOferta;
   }

   public short getEstado() {
      return estado;
   }

   public void setEstado(short estado) {
      this.estado = estado;
   }

   public short getChequeado() {
      return chequeado;
   }

   public void setChequeado(short chequeado) {
      this.chequeado = chequeado;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
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
      if (!(object instanceof ProductosWeb)) {
         return false;
      }
      ProductosWeb other = (ProductosWeb) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

    @Override
    public String toString() {
        return "ProductosWeb{" + "id=" + id + ", precio=" + precio + ", destacado=" + destacado + ", oferta=" + oferta + ", inicioOferta=" + inicioOferta + ", finOferta=" + finOferta + ", estado=" + estado + ", chequeado=" + chequeado + ", producto=" + producto + '}';
    }

   
}
