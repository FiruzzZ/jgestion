package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import javax.persistence.UniqueConstraint;

/**
 * Una CajaMovimiento es una referencia a registros de movimientos contables de
 * una {@link Caja} (Ingresos, Egresos), los cuales son agrupados por una
 * instancia de esta clase.
 * Debe existir una CajaMovimiento "activa" (es decir sin fechaCierre,
 * montoCierre ni usuarioCierre) por cada Caja.estado == true.
 * @author FiruzzZ
 */
@Entity
@Table(name = "caja_movimientos", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"caja", "fecha_apertura"}),
   @UniqueConstraint(columnNames = {"caja", "fecha_cierre"})
})
@NamedQueries({
   @NamedQuery(name = "CajaMovimientos.findAll", query = "SELECT c FROM CajaMovimientos c"),
   @NamedQuery(name = "CajaMovimientos.findById", query = "SELECT c FROM CajaMovimientos c WHERE c.id = :id")
})
public class CajaMovimientos implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "fecha_apertura", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fechaApertura;
   @Column(name = "fecha_cierre")
   @Temporal(TemporalType.DATE)
   private Date fechaCierre;
   @Basic(optional = false)
   @Column(name = "sistema_fecha_apertura", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
   @Temporal(TemporalType.TIMESTAMP)
   private Date sistemaFechaApertura;
   @Column(name = "sistema_fecha_cierre", insertable = false, columnDefinition = "timestamp with time zone")
   @Temporal(TemporalType.TIMESTAMP)
   private Date sistemaFechaCierre;
   @Basic(optional = false)
   @Column(name = "monto_apertura", nullable = false)
   private double montoApertura;
   @Column(name = "monto_cierre", precision = 17, scale = 17)
   private Double montoCierre;
   @JoinColumn(name = "usuario_cierre", referencedColumnName = "id")
   @ManyToOne
   private Usuario usuarioCierre;
   @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Caja caja;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "cajaMovimientos", fetch = FetchType.EAGER)
   private List<DetalleCajaMovimientos> detalleCajaMovimientosList;

   public CajaMovimientos() {
   }

   public CajaMovimientos(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Date getFechaApertura() {
      return fechaApertura;
   }

   public void setFechaApertura(Date fechaApertura) {
      this.fechaApertura = fechaApertura;
   }

   public Date getFechaCierre() {
      return fechaCierre;
   }

   public void setFechaCierre(Date fechaCierre) {
      this.fechaCierre = fechaCierre;
   }

   public Date getSistemaFechaApertura() {
      return sistemaFechaApertura;
   }

   public void setSistemaFechaApertura(Date sistemaFechaApertura) {
      this.sistemaFechaApertura = sistemaFechaApertura;
   }

   public Date getSistemaFechaCierre() {
      return sistemaFechaCierre;
   }

   public void setSistemaFechaCierre(Date sistemaFechaCierre) {
      this.sistemaFechaCierre = sistemaFechaCierre;
   }

   public double getMontoApertura() {
      return montoApertura;
   }

   public void setMontoApertura(double montoApertura) {
      this.montoApertura = montoApertura;
   }

   public Double getMontoCierre() {
      return montoCierre;
   }

   public void setMontoCierre(Double montoCierre) {
      this.montoCierre = montoCierre;
   }

   public Usuario getUsuarioCierre() {
      return usuarioCierre;
   }

   public void setUsuarioCierre(Usuario usuarioCierre) {
      this.usuarioCierre = usuarioCierre;
   }

   public Caja getCaja() {
      return caja;
   }

   public void setCaja(Caja caja) {
      this.caja = caja;
   }

   public List<DetalleCajaMovimientos> getDetalleCajaMovimientosList() {
      return detalleCajaMovimientosList;
   }

   public void setDetalleCajaMovimientosList(List<DetalleCajaMovimientos> detalleCajaMovimientosList) {
      this.detalleCajaMovimientosList = detalleCajaMovimientosList;
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
      if (!(object instanceof CajaMovimientos)) {
         return false;
      }
      CajaMovimientos other = (CajaMovimientos) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   /**
    * Muestra el NOMBRE DE LA CAJA a la cual está relacionada la entity CajaMovimiento y el ID.
    * <code>CajaMovimiento.getCaja().getNombre()</code>
    *
    */
   public String toString() {
      return this.getCaja().getNombre() + " (" + this.getId() + ")";
   }
}
