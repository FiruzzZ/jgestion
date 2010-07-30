
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
import javax.persistence.Transient;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "presupuesto")
@NamedQueries({
   @NamedQuery(name = "Presupuesto.findAll", query = "SELECT p FROM Presupuesto p"),
   @NamedQuery(name = "Presupuesto.findById", query = "SELECT p FROM Presupuesto p WHERE p.id = :id")
})
public class Presupuesto implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Column(name = "fecha_creacion")
   @Temporal(TemporalType.DATE)
   private Date fechaCreacion;
   @Basic(optional = false)
   @Column(name = "importe", nullable = false)
   private double importe;
   @Basic(optional = false)
   @Column(name = "descuento", nullable = false)
   private double descuento;
   @Basic(optional = false)
   @Column(name = "iva10", nullable = false)
   private double iva10;
   @Basic(optional = false)
   @Column(name = "iva21", nullable = false)
   private double iva21;
   @Column(name = "hora_creacion")
   @Temporal(TemporalType.TIME)
   private Date horaCreacion;
   @Basic(optional = false)
   @Column(name = "forma_pago", nullable = false)
   private short formaPago;
   @Column(name = "dias")
   private Short dias;
   @JoinColumn(name="lista_precios", referencedColumnName= "id", nullable = false)
   @ManyToOne(optional = false)
   private ListaPrecios listaPrecios;
   @JoinColumn(name = "cliente", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Cliente cliente;
   @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Sucursal sucursal;
   @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Usuario usuario;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "presupuesto")
   private List<DetallePresupuesto> detallePresupuestoList;

   public Presupuesto() {
   }

   public Presupuesto(Integer id) {
      this.id = id;
   }

   public Presupuesto(Integer id, double importe, double descuento, double iva10, double iva21) {
      this.id = id;
//      this.fechaCreacion = fechaCreacion;
      this.importe = importe;
      this.descuento = descuento;
      this.iva10 = iva10;
      this.iva21 = iva21;
//      this.horaCreacion = horaCreacion;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Date getFechaCreacion() {
      return fechaCreacion;
   }

   public void setFechaCreacion(Date fechaCreacion) {
      this.fechaCreacion = fechaCreacion;
   }

   public double getImporte() {
      return importe;
   }

   public void setImporte(double importe) {
      this.importe = importe;
   }

   public double getDescuento() {
      return descuento;
   }

   public void setDescuento(double descuento) {
      this.descuento = descuento;
   }

   public double getIva10() {
      return iva10;
   }

   public void setIva10(double iva10) {
      this.iva10 = iva10;
   }

   public double getIva21() {
      return iva21;
   }

   public void setIva21(double iva21) {
      this.iva21 = iva21;
   }

   public ListaPrecios getListaPrecios() {
      return listaPrecios;
   }

   public void setListaPrecios(ListaPrecios listaPrecios) {
      this.listaPrecios = listaPrecios;
   }
   
   public Date getHoraCreacion() {
      return horaCreacion;
   }

   public void setHoraCreacion(Date horaCreacion) {
      this.horaCreacion = horaCreacion;
   }

   public Cliente getCliente() {
      return cliente;
   }

   public void setCliente(Cliente cliente) {
      this.cliente = cliente;
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

   public short getFormaPago() {
      return formaPago;
   }

   public void setFormaPago(short formaPago) {
      this.formaPago = formaPago;
   }

   public Short getDias() {
      return dias;
   }

   public void setDias(Short dias) {
      this.dias = dias;
   }

   public List<DetallePresupuesto> getDetallePresupuestoList() {
      return detallePresupuestoList;
   }

   public void setDetallePresupuestoList(List<DetallePresupuesto> detallePresupuestoList) {
      this.detallePresupuestoList = detallePresupuestoList;
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
      if (!(object instanceof Presupuesto)) {
         return false;
      }
      Presupuesto other = (Presupuesto) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return this.getId().toString();
   }

}
