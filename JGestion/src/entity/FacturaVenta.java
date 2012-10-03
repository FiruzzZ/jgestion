package entity;

import controller.Valores.FormaPago;
import utilities.general.UTIL;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "factura_venta" //, uniqueConstraints = {@UniqueConstraint(columnNames = {"numero"})}
)
@NamedQueries({
    @NamedQuery(name = "FacturaVenta.findAll", query = "SELECT f FROM FacturaVenta f"),
    @NamedQuery(name = "FacturaVenta.findById", query = "SELECT f FROM FacturaVenta f WHERE f.id = :id",
    hints =
    @QueryHint(name = QueryHints.REFRESH, value = "true")),
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
    @Column(name = "importe", nullable = false, precision = 9, scale = 2)
    private Double importe;
    @Basic(optional = false)
    @Column(name = "fechaalta", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaalta;
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
    @Column(name = "gravado", nullable = false)
    private Double gravado;
    @Basic(optional = false)
    @Column(name = "no_gravado", nullable = false, precision = 12, scale = 2)
    private BigDecimal noGravado;
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

    public Double getGravado() {
        return gravado;
    }

    public void setGravado(Double gravado) {
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
        return FormaPago.getFormaPago(formaPago);
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
                + "\n\tDetalle:" + (detallesVentaList != null
                ? detallesVentaList.toString() : null)
                + '}';
    }
}
