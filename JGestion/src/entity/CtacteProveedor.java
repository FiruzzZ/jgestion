package entity;

import generics.UTIL;
import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "ctacte_proveedor")
@NamedQueries({
   @NamedQuery(name = "CtacteProveedor.findAll", query = "SELECT c FROM CtacteProveedor c"),
   @NamedQuery(name = "CtacteProveedor.findById", query = "SELECT c FROM CtacteProveedor c WHERE c.id = :id"),
   @NamedQuery(name = "CtacteProveedor.findByFactura", query = "SELECT c FROM CtacteProveedor c WHERE c.factura.id = :idFactura")
})
public class CtacteProveedor implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "dias", nullable = false)
   private short dias;
   @Basic(optional = false)
   @Column(name = "importe", nullable = false)
   private double importe;
   @Basic(optional = false)
   @Column(name = "estado", nullable = false)
   private short estado;
   @Basic(optional = false)
   @Column(name = "entregado", nullable = false)
   private double entregado;
   @Basic(optional = false)
   @Column(name = "fecha_carga", nullable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
   @Temporal(TemporalType.TIMESTAMP)
   private Date fechaCarga;
   @JoinColumn(name = "factura", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private FacturaCompra factura;

   public CtacteProveedor() {
   }

   public CtacteProveedor(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public short getDias() {
      return dias;
   }

   public void setDias(short dias) {
      this.dias = dias;
   }

   public double getImporte() {
      return importe;
   }

   public void setImporte(double importe) {
      this.importe = importe;
   }

   public short getEstado() {
      return estado;
   }

   public void setEstado(short estado) {
      this.estado = estado;
   }

   public double getEntregado() {
      return entregado;
   }

   public void setEntregado(double entregado) {
      this.entregado = entregado;
   }

   public Date getFechaCarga() {
      return fechaCarga;
   }

   public void setFechaCarga(Date fechaCarga) {
      this.fechaCarga = fechaCarga;
   }

   public FacturaCompra getFactura() {
      return factura;
   }

   public void setFactura(FacturaCompra factura) {
      this.factura = factura;
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
      if (!(object instanceof CtacteProveedor)) {
         return false;
      }
      CtacteProveedor other = (CtacteProveedor) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return UTIL.AGREGAR_CEROS(String.valueOf(this.getFactura().getNumero()), 12);
   }
}
