package entity;

import utilities.general.UTIL;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "factura_compra", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"numero", "proveedor"})
})
@NamedQueries({
   @NamedQuery(name = "FacturaCompra.findAll", query = "SELECT f FROM FacturaCompra f"),
   @NamedQuery(name = "FacturaCompra.findByNumeroProveedor", query = "SELECT f FROM FacturaCompra f WHERE f.numero = :numero AND f.proveedor.id = :proveedor"),
   @NamedQuery(name = "FacturaCompra.findById", query = "SELECT f FROM FacturaCompra f WHERE f.id = :id"),
   @NamedQuery(name = "FacturaCompra.findByNumero", query = "SELECT f FROM FacturaCompra f WHERE f.numero = :numero")
})
public class FacturaCompra implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "numero", nullable = false)
   private long numero;
   @Basic(optional = false)
   @Column(name = "tipo", nullable = false)
   private char tipo;
   @Basic(optional = false)
   @Column(name = "importe", nullable = false)
   private double importe;
   @Basic(optional = false)
   @Column(name = "fecha_compra", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date fechaCompra;
   @Basic(optional = false)
   @Column(name = "fechaalta", nullable = false, insertable=false, updatable=false, columnDefinition="timestamp with time zone DEFAULT now()")
   @Temporal(TemporalType.TIMESTAMP)
   private Date fechaalta;
   @Basic(optional = false)
   @Column(name = "forma_pago", nullable = false)
   private short formaPago;
   @Column(name = "remito")
   private Long remito;
   @Basic(optional = false)
   @Column(name = "actualiza_stock", nullable = false)
   private boolean actualizaStock;
   @Basic(optional = false)
   @Column(name = "factura_cuarto", nullable = false)
   private short facturaCuarto;
   @Basic(optional = false)
   @Column(name = "factura_octeto", nullable = false)
   private int facturaOcteto;
   @Basic(optional = false)
   @Column(name = "perc_iva", nullable = false)
   private double percIva;
   @Basic(optional = false)
   @Column(name = "perc_dgr", nullable = false)
   private double percDgr;
   @Basic(optional = false)
   @Column(name = "iva10", nullable = false)
   private double iva10;
   @Basic(optional = false)
   @Column(name = "iva21", nullable = false)
   private double iva21;
   @Basic(optional = false)
   @Column(name = "movimiento_interno", nullable = false)
   private int movimientoInterno;
   @Column(name = "dias_cta_cte")
   private Short diasCtaCte;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "factura", fetch=FetchType.EAGER)
   private List<DetalleCompra> detalleCompraList;
   @JoinColumn(name = "proveedor", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Proveedor proveedor;
   @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Sucursal sucursal;
   @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Usuario usuario;
   @JoinColumn(name = "caja", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Caja caja;
   @Basic(optional = false)
   @Column(name = "anulada", nullable = false)
   private boolean anulada;

   public FacturaCompra() {
   }

   public FacturaCompra(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public long getNumero() {
      return numero;
   }

   public void setNumero(long numero) {
      this.numero = numero;
   }

   public char getTipo() {
      return tipo;
   }

   public void setTipo(char tipo) {
      this.tipo = tipo;
   }

   public double getImporte() {
      return importe;
   }

   public void setImporte(double importe) {
      this.importe = importe;
   }

   public Date getFechaCompra() {
      return fechaCompra;
   }

   public void setFechaCompra(Date fechaCompra) {
      this.fechaCompra = fechaCompra;
   }

   public Date getFechaalta() {
      return fechaalta;
   }

   public int getFormaPago() {
      return formaPago;
   }

   public void setFormaPago(short formaPago) {
      this.formaPago = formaPago;
   }

   public Long getRemito() {
      return remito;
   }

   public void setRemito(Long remito) {
      this.remito = remito;
   }

   public boolean getActualizaStock() {
      return actualizaStock;
   }

   public void setActualizaStock(boolean actualizaStock) {
      this.actualizaStock = actualizaStock;
   }

   public short getFacturaCuarto() {
      return facturaCuarto;
   }

   public void setFacturaCuarto(short facturaCuarto) {
      this.facturaCuarto = facturaCuarto;
   }

   public int getFacturaOcteto() {
      return facturaOcteto;
   }

   public void setFacturaOcteto(int facturaOcteto) {
      this.facturaOcteto = facturaOcteto;
   }

   public double getPercIva() {
      return percIva;
   }

   public void setPercIva(double percIva) {
      this.percIva = percIva;
   }

   public double getPercDgr() {
      return percDgr;
   }

   public void setPercDgr(double percDgr) {
      this.percDgr = percDgr;
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

   public int getMovimientoInterno() {
      return movimientoInterno;
   }

   public void setMovimientoInterno(int movimientoInterno) {
      this.movimientoInterno = movimientoInterno;
   }

   public Short getDiasCtaCte() {
      return diasCtaCte;
   }

   public void setDiasCtaCte(Short diasCtaCte) {
      this.diasCtaCte = diasCtaCte;
   }

   public List<DetalleCompra> getDetalleCompraList() {
      return detalleCompraList;
   }

   public void setDetalleCompraList(List<DetalleCompra> detalleCompraList) {
      this.detalleCompraList = detalleCompraList;
   }

   public Proveedor getProveedor() {
      return proveedor;
   }

   public void setProveedor(Proveedor proveedor) {
      this.proveedor = proveedor;
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

   public Caja getCaja() {
      return caja;
   }

   public void setCaja(Caja caja) {
      this.caja = caja;
   }

   public boolean getAnulada() {
      return anulada;
   }

   public void setAnulada(boolean anulada) {
      this.anulada = anulada;
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
      if (!(object instanceof FacturaCompra)) {
         return false;
      }
      FacturaCompra other = (FacturaCompra) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return UTIL.AGREGAR_CEROS(String.valueOf(this.getNumero()), 12);
   }
}
