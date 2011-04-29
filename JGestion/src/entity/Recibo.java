
package entity;

import utilities.general.UTIL;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "recibo")
@NamedQueries({
   @NamedQuery(name = "Recibo.findAll", query = "SELECT r FROM Recibo r"),
   @NamedQuery(name = "Recibo.findById", query = "SELECT r FROM Recibo r WHERE r.id = :id"),
   @NamedQuery(name = "Recibo.findByEstado", query = "SELECT r FROM Recibo r WHERE r.estado = :estado")
})
public class Recibo implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
//   @GeneratedValue(strategy = GenerationType.IDENTITY) <-- NO ES AUTOGENERADO!
   private Long id;
   @Basic(optional = false)
   @Column(name = "fecha_carga", nullable = false, insertable=false, updatable=false, columnDefinition="timestamp with time zone NOT NULL DEFAULT now()")
   @Temporal(TemporalType.TIMESTAMP)
   private Date fechaCarga;
   @Basic(optional = false)
   @Column(name = "monto", nullable = false)
   private double monto;
   @Basic(optional = false)
   @Column(name = "fecha_recibo", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fechaRecibo;
   @Basic(optional = false)
   @Column(name = "estado", nullable = false)
   private boolean estado;
   @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Caja caja;
   @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Sucursal sucursal;
   @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Usuario usuario;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "recibo")
   private List<DetalleRecibo> detalleReciboList;

   public Recibo() {
   }

   public Recibo(Long id) {
      this.id = id;
   }

   public Date getFechaCarga() {
      return fechaCarga;
   }

   public double getMonto() {
      return monto;
   }

   public void setMonto(double monto) {
      this.monto = monto;
   }

   public Date getFechaRecibo() {
      return fechaRecibo;
   }

   public void setFechaRecibo(Date fechaRecibo) {
      this.fechaRecibo = fechaRecibo;
   }

   public boolean getEstado() {
      return estado;
   }

   public void setEstado(boolean estado) {
      this.estado = estado;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Caja getCaja() {
      return caja;
   }

   public void setCaja(Caja caja) {
      this.caja = caja;
   }

   public Sucursal getSucursal() {
      return sucursal;
   }

   public void setSucursal(Sucursal sucursal) {
      this.sucursal = sucursal;
   }

   public Usuario getUsuario() {
      return usuario;
   }

   public void setUsuario(Usuario usuario) {
      this.usuario = usuario;
   }

   public List<DetalleRecibo> getDetalleReciboList() {
      return detalleReciboList;
   }

   public void setDetalleReciboList(List<DetalleRecibo> detalleReciboList) {
      this.detalleReciboList = detalleReciboList;
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
      if (!(object instanceof Recibo)) {
         return false;
      }
      Recibo other = (Recibo) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return UTIL.AGREGAR_CEROS(this.getId(), 12);
   }

}
