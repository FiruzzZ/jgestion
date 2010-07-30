
package entity;

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
@Table(name = "detalle_caja_movimientos")
@NamedQueries({
   @NamedQuery(name = "DetalleCajaMovimientos.findAll", query = "SELECT d FROM DetalleCajaMovimientos d"),
   @NamedQuery(name = "DetalleCajaMovimientos.findById", query = "SELECT d FROM DetalleCajaMovimientos d WHERE d.id = :id")
})

public class DetalleCajaMovimientos implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "ingreso", nullable = false)
   private boolean ingreso;
   @Basic(optional = false)
   @Column(name = "tipo", nullable = false)
   private short tipo;
   @Basic(optional = false)
   @Column(name = "numero", nullable = false)
   private long numero;
   @Basic(optional = false)
   @Column(name = "monto", nullable = false)
   private double monto;
   @Basic(optional = false)
   @Column(name = "fecha", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fecha;
   @Basic(optional = false)
   @Column(name = "hora", nullable = false)
   @Temporal(TemporalType.TIME)
   private Date hora;
   @Basic(optional = false)
   @Column(name = "descripcion", nullable = false, length = 2147483647)
   private String descripcion;
   @JoinColumn(name = "caja_movimientos", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private CajaMovimientos cajaMovimientos;
   @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Usuario usuario;

   public DetalleCajaMovimientos() {
   }

   public DetalleCajaMovimientos(Integer id) {
      this.id = id;
   }

   public DetalleCajaMovimientos(Integer id, boolean ingreso, short tipo, long numero, double monto, Date fecha, Date hora, String descripcion) {
      this.id = id;
      this.ingreso = ingreso;
      this.tipo = tipo;
      this.numero = numero;
      this.monto = monto;
      this.fecha = fecha;
      this.hora = hora;
      this.descripcion = descripcion;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public boolean getIngreso() {
      return ingreso;
   }

   public void setIngreso(boolean ingreso) {
      this.ingreso = ingreso;
   }

   public short getTipo() {
      return tipo;
   }

   public void setTipo(short tipo) {
      this.tipo = tipo;
   }

   public long getNumero() {
      return numero;
   }

   public void setNumero(long numero) {
      this.numero = numero;
   }

   public double getMonto() {
      return monto;
   }

   public void setMonto(double monto) {
      this.monto = monto;
   }

   public Date getFecha() {
      return fecha;
   }

   public void setFecha(Date fecha) {
      this.fecha = fecha;
   }

   public Date getHora() {
      return hora;
   }

   public void setHora(Date hora) {
      this.hora = hora;
   }

   public String getDescripcion() {
      return descripcion;
   }

   public void setDescripcion(String descripcion) {
      this.descripcion = descripcion;
   }

   public CajaMovimientos getCajaMovimientos() {
      return cajaMovimientos;
   }

   public void setCajaMovimientos(CajaMovimientos cajaMovimientos) {
      this.cajaMovimientos = cajaMovimientos;
   }

   public Usuario getUsuario() {
      return usuario;
   }

   public void setUsuario(Usuario usuario) {
      this.usuario = usuario;
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
      if (!(object instanceof DetalleCajaMovimientos)) {
         return false;
      }
      DetalleCajaMovimientos other = (DetalleCajaMovimientos) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return this.getDescripcion();
   }

}
