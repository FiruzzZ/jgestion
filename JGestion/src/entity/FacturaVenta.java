package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "factura_venta" //, uniqueConstraints = {@UniqueConstraint(columnNames = {"numero"})}
)
@NamedQueries({
   @NamedQuery(name = "FacturaVenta.findAll", query = "SELECT f FROM FacturaVenta f"),
   @NamedQuery(name = "FacturaVenta.findById", query = "SELECT f FROM FacturaVenta f WHERE f.id = :id"),
   @NamedQuery(name = "FacturaVenta.findByNumero", query = "SELECT f FROM FacturaVenta f WHERE f.numero = :numero"),
   @NamedQuery(name = "FacturaVenta.findBySucursal", query = "SELECT f FROM FacturaVenta f WHERE f.sucursal = :sucursal"),
   @NamedQuery(name = "FacturaVenta.findByCaja", query = "SELECT f FROM FacturaVenta f WHERE f.caja = :caja")
})
public class FacturaVenta implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "tipo", nullable = false)
   private char tipo;
   @Basic(optional = false)
   @Column(name = "fecha_venta", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fechaVenta;
   @Basic(optional = false)
   @Column(name = "importe", nullable = false, precision = 9, scale = 2)
   private Double importe;
   @Column(name = "fechaalta")
   @Temporal(TemporalType.DATE)
   private Date fechaalta;
   @Basic(optional = false)
   @Column(name = "horaalta", nullable = false)
   @Temporal(TemporalType.TIME)
   private Date horaalta;
   @Column(name = "descuento", precision = 9, scale = 2)
   private Double descuento;
   @Basic(optional = false)
   @Column(name = "numero", nullable = false)
   private long numero;
   @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Sucursal sucursal;
   @Basic(optional = false)
   @Column(name = "iva10", nullable = false)
   private double iva10;
   @Basic(optional = false)
   @Column(name = "iva21", nullable = false)
   private double iva21;
   @Column(name = "factura_cuarto")
   private Short facturaCuarto;
   @Column(name = "factura_octeto")
   private Integer facturaOcteto;
   @Basic(optional = false)
   @Column(name = "movimiento_interno", nullable = false)
   private int movimientoInterno;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "factura", fetch=FetchType.EAGER)
   private List<DetallesVenta> detallesVentaList;
   @JoinColumn(name = "cliente", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Cliente cliente;
   @JoinColumn(name = "lista_precios", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private ListaPrecios listaPrecios;
   @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Usuario usuario;
   @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Caja caja;
   @Basic(optional = false)
   @Column(name = "gravado", nullable = false)
   private Double gravado;
   @Basic(optional = false)
   @Column(name = "forma_pago", nullable = false)
   private short formaPago;
   @Column(name = "dias_cta_cte")
   private Short diasCtaCte;
   @Basic(optional = false)
   @Column(name = "anulada", nullable = false)
   private boolean anulada;
   @JoinColumn(name = "remito", referencedColumnName = "id")
   @ManyToOne
   private Remito remito;

   public FacturaVenta() {
   }

   public FacturaVenta(Integer id) {
      this.id = id;
   }

   public FacturaVenta(Integer id, char tipo, Date fechaVenta, Double importe, long numero, Sucursal sucursal, double iva10, double iva21, int movimientoInterno, Caja caja, double gravado, short formaPago, Date fechaalta, Date horaalta, boolean anulada) {
      this.id = id;
      this.tipo = tipo;
      this.fechaVenta = fechaVenta;
      this.importe = importe;
      this.numero = numero;
      this.sucursal = sucursal;
      this.iva10 = iva10;
      this.iva21 = iva21;
      this.movimientoInterno = movimientoInterno;
      this.caja = caja;
      this.gravado = gravado;
      this.formaPago = formaPago;
      this.fechaalta = fechaalta;
      this.horaalta = horaalta;
      this.anulada = anulada;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public char getTipo() {
      return tipo;
   }

   public void setTipo(char tipo) {
      this.tipo = tipo;
   }

   public Date getFechaVenta() {
      return fechaVenta;
   }

   public void setFechaVenta(Date fechaVenta) {
      this.fechaVenta = fechaVenta;
   }

   public Double getImporte() {
      return importe;
   }

   public void setImporte(Double importe) {
      this.importe = importe;
   }

   public Date getFechaalta() {
      return fechaalta;
   }

   public void setFechaalta(Date fechaalta) {
      this.fechaalta = fechaalta;
   }

   public Date getHoraalta() {
      return horaalta;
   }

   public void setHoraalta(Date horaalta) {
      this.horaalta = horaalta;
   }

   public Double getDescuento() {
      return descuento;
   }

   public void setDescuento(Double descuento) {
      this.descuento = descuento;
   }

   public long getNumero() {
      return numero;
   }

   public void setNumero(long numero) {
      this.numero = numero;
   }

   public Sucursal getSucursal() {
      return sucursal;
   }

   public void setSucursal(Sucursal sucursal) {
      this.sucursal = sucursal;
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

   public Short getFacturaCuarto() {
      return facturaCuarto;
   }

   public void setFacturaCuarto(Short facturaCuarto) {
      this.facturaCuarto = facturaCuarto;
   }

   public Integer getFacturaOcteto() {
      return facturaOcteto;
   }

   public void setFacturaOcteto(Integer facturaOcteto) {
      this.facturaOcteto = facturaOcteto;
   }

   public int getMovimientoInterno() {
      return movimientoInterno;
   }

   public void setMovimientoInterno(int movimientoInterno) {
      this.movimientoInterno = movimientoInterno;
   }

   public List<DetallesVenta> getDetallesVentaList() {
      return detallesVentaList;
   }

   public void setDetallesVentaList(List<DetallesVenta> detallesVentaList) {
      this.detallesVentaList = detallesVentaList;
   }

   public Cliente getCliente() {
      return cliente;
   }

   public void setCliente(Cliente cliente) {
      this.cliente = cliente;
   }

   public ListaPrecios getListaPrecios() {
      return listaPrecios;
   }

   public void setListaPrecios(ListaPrecios listaPrecios) {
      this.listaPrecios = listaPrecios;
   }

   public Usuario getUsuario() {
      return usuario;
   }

   public void setUsuario(Usuario usuario) {
      this.usuario = usuario;
   }

   public Caja getCaja() {
      return caja;
   }

   public void setCaja(Caja caja) {
      this.caja = caja;
   }

   public Double getGravado() {
      return gravado;
   }

   public void setGravado(Double gravado) {
      this.gravado = gravado;
   }

   public short getFormaPago() {
      return formaPago;
   }

   public void setFormaPago(short formaPago) {
      this.formaPago = formaPago;
   }

   public Short getDiasCtaCte() {
      return diasCtaCte;
   }

   public void setDiasCtaCte(Short diasCtaCte) {
      this.diasCtaCte = diasCtaCte;
   }

   public Remito getRemito() {
      return remito;
   }

   public void setRemito(Remito remito) {
      this.remito = remito;
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
      if (!(object instanceof FacturaVenta)) {
         return false;
      }
      FacturaVenta other = (FacturaVenta) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      if (this.getMovimientoInterno() == 0) {
         return String.valueOf(this.getNumero());
      } else {
         return "I" + String.valueOf(this.getMovimientoInterno());
      }
   }

   public void setAnulada(boolean anulada) {
      this.anulada = anulada;
   }

   public boolean getAnulada() {
      return anulada;
   }
}
