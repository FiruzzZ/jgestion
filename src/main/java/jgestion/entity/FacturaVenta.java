package jgestion.entity;

import jgestion.controller.Valores.FormaPago;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "factura_venta"
// cuando arregle el atributo movimiento_interno        
//, uniqueConstraints = {@UniqueConstraint(columnNames = {"tipo", "sucursal", "numero"})}
)
@NamedQueries({
    @NamedQuery(name = "FacturaVenta.findAll", query = "SELECT f FROM FacturaVenta f"),
    @NamedQuery(name = "FacturaVenta.findById", query = "SELECT f FROM FacturaVenta f WHERE f.id = :id",
            hints
            = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "FacturaVenta.findByNumero", query = "SELECT f FROM FacturaVenta f WHERE f.numero = :numero")
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
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Basic(optional = false)
    @Column(name = "fechaalta", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaalta;
    @Column(name = "descuento", precision = 12, scale = 2)
    private Double descuento;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false)
    private long numero;
    @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @Basic(optional = false)
    @Column(name = "iva10", nullable = false, precision = 12, scale = 2)
    private double iva10;
    @Basic(optional = false)
    @Column(name = "iva21", nullable = false, precision = 12, scale = 2)
    private double iva21;
    @Basic(optional = false)
    @Column(name = "movimiento_interno", nullable = false)
    private int movimientoInterno;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "factura", fetch = FetchType.EAGER)
    private List<DetalleVenta> detallesVentaList;
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
    @Column(name = "gravado", nullable = false, precision = 12, scale = 2)
    private BigDecimal gravado;
    @Basic(optional = false)
    @Column(name = "no_gravado", nullable = false, precision = 12, scale = 2)
    private BigDecimal noGravado;
    /**
     * 1= contado, 2 = ctacte
     */
    @Basic(optional = false)
    @Column(name = "forma_pago", nullable = false)
    private int formaPago;
    @Basic(optional = false)
    @Column(name = "dias_cta_cte", nullable = false)
    private Short diasCtaCte;
    @Basic(optional = false)
    @Column(name = "anulada", nullable = false)
    private boolean anulada;
    @JoinColumn(name = "remito", referencedColumnName = "id")
    @ManyToOne
    private Remito remito;
    @Transient
    private transient ChequeTerceros cheque;
    @Basic(optional = false)
    @Column(name = "diferencia_redondeo", nullable = false, precision = 10, scale = 2)
    private BigDecimal diferenciaRedondeo;
    @JoinColumn(name = "unidad_de_negocio_id")
    @ManyToOne
    private UnidadDeNegocio unidadDeNegocio;
    @JoinColumn(name = "cuenta_id")
    @ManyToOne
    private Cuenta cuenta;
    @JoinColumn(name = "subcuenta_id")
    @ManyToOne
    private SubCuenta subCuenta;
    @Column(length = 100)
    private String observacion;
    @JoinColumn(name = "vendedor_id")
    @ManyToOne
    private Vendedor vendedor;
    @Column(name = "venta_simple")
    private boolean ventaSimple;

    public FacturaVenta() {
    }

    @PostPersist
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

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Date getFechaalta() {
        return fechaalta;
    }

    public void setFechaalta(Date fechaalta) {
        this.fechaalta = fechaalta;
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

    public int getMovimientoInterno() {
        return movimientoInterno;
    }

    public void setMovimientoInterno(int movimientoInterno) {
        this.movimientoInterno = movimientoInterno;
    }

    public List<DetalleVenta> getDetallesVentaList() {
        return detallesVentaList;
    }

    public void setDetallesVentaList(List<DetalleVenta> detallesVentaList) {
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

    public BigDecimal getGravado() {
        return gravado;
    }

    public void setGravado(BigDecimal gravado) {
        this.gravado = gravado;
    }

    public BigDecimal getNoGravado() {
        return noGravado;
    }

    public void setNoGravado(BigDecimal noGravado) {
        this.noGravado = noGravado;
    }

    public int getFormaPago() {
        return formaPago;
    }

    public FormaPago getFormaPagoEnum() {
        return FormaPago.find(formaPago);
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago.getId();
    }

    public void setFormaPago(int formaPago) {
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

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public boolean getAnulada() {
        return anulada;
    }

    public ChequeTerceros getCheque() {
        return cheque;
    }

    public void setCheque(ChequeTerceros cheque) {
        this.cheque = cheque;
    }

    public BigDecimal getDiferenciaRedondeo() {
        return diferenciaRedondeo;
    }

    public void setDiferenciaRedondeo(BigDecimal diferenciaRedondeo) {
        this.diferenciaRedondeo = diferenciaRedondeo;
    }

    public UnidadDeNegocio getUnidadDeNegocio() {
        return unidadDeNegocio;
    }

    public void setUnidadDeNegocio(UnidadDeNegocio unidadDeNegocio) {
        this.unidadDeNegocio = unidadDeNegocio;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public SubCuenta getSubCuenta() {
        return subCuenta;
    }

    public void setSubCuenta(SubCuenta subCuenta) {
        this.subCuenta = subCuenta;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public boolean isVentaSimple() {
        return ventaSimple;
    }

    public void setVentaSimple(boolean ventaSimple) {
        this.ventaSimple = ventaSimple;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FacturaVenta other = (FacturaVenta) obj;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        if (this.tipo != other.tipo) {
            return false;
        }
        if (this.numero != other.numero) {
            return false;
        }
        if (!Objects.equals(this.sucursal, other.sucursal)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FacturaVenta{" + "id=" + id + ", tipo=" + tipo + ", fechaVenta="
                + fechaVenta + ", importe=" + importe + ", fechaalta=" + fechaalta
                + ", descuento=" + descuento + ", numero=" + numero
                + ", sucursal=" + sucursal.getId() + ", iva10=" + iva10 + ", iva21="
                + iva21 + ", movimientoInterno=" + movimientoInterno
                + ", cliente=" + cliente.getId() + ", listaPrecios=" + listaPrecios
                + ", usuario=" + usuario.getId() + ", caja=" + caja
                + ", gravado=" + gravado
                + ", noGravado=" + noGravado
                + ", diferenciaRedondeo=" + diferenciaRedondeo
                + ", formaPago=" + formaPago
                + ", diasCtaCte=" + diasCtaCte + ", anulada=" + anulada
                + ", remito=" + remito + ", cheque=" + cheque
                + ", ventaSimple=" + ventaSimple
                + "\n\tDetalle:" + (detallesVentaList != null
                        ? detallesVentaList.toString() : null)
                + '}';
    }
}
