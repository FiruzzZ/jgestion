package entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "nota_credito_proveedor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proveedor", "numero"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "NotaCreditoProveedor.findAll", query = "SELECT n FROM NotaCreditoProveedor n")})
public class NotaCreditoProveedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false)
    private Long numero;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean anulada;
    /**
     * Antes se podía ir desacreditando en porciones una nota de crédito, ya no mas.
     * Cuando una nota de credito sea utilizada {@link #desacreditado} == {@link #importe}
     * @deprecated
     */
    @Deprecated
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal desacreditado;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, updatable = false, insertable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(name = "fecha_nota_credito_proveedor", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaNotaCredito;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal gravado;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Basic(optional = false)
    @Column(name = "impuestos_recuperables", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuestosRecuperables;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal iva10;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal iva21;
    @Basic(optional = false)
    @Column(name = "no_gravado", nullable = false, precision = 12, scale = 2)
    private BigDecimal noGravado;
    @Column(length = 250)
    private String observacion;
    @JoinColumn(name = "usuario", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @JoinColumn(name = "proveedor", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Proveedor proveedor;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "notaCreditoProveedor", orphanRemoval = true)
    private List<DetalleNotaCreditoProveedor> detalleNotaCreditoProveedorList;
    @JoinColumn(name = "remesa")
    @ManyToOne
    private Remesa remesa;

    public NotaCreditoProveedor() {
    }

    public NotaCreditoProveedor(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }
/**
     * Antes se podía ir desacreditando en porciones una nota de crédito, ya no mas.
     * Cuando una nota de credito sea utilizada {@link #desacreditado} == {@link #importe}
     * @return 
     * @deprecated
     */
    @Deprecated
    public BigDecimal getDesacreditado() {
        return desacreditado;
    }

    /**
     * Antes se podía ir desacreditando en porciones una nota de crédito, ya no mas.
     * Cuando una nota de credito sea utilizada {@link #desacreditado} == {@link #importe}
     * @param desacreditado 
     * @deprecated
     */
    @Deprecated
    public void setDesacreditado(BigDecimal desacreditado) {
        this.desacreditado = desacreditado;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public BigDecimal getGravado() {
        return gravado;
    }

    public void setGravado(BigDecimal gravado) {
        this.gravado = gravado;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public BigDecimal getImpuestosRecuperables() {
        return impuestosRecuperables;
    }

    public void setImpuestosRecuperables(BigDecimal impuestosRecuperables) {
        this.impuestosRecuperables = impuestosRecuperables;
    }

    public BigDecimal getIva10() {
        return iva10;
    }

    public void setIva10(BigDecimal iva10) {
        this.iva10 = iva10;
    }

    public BigDecimal getIva21() {
        return iva21;
    }

    public void setIva21(BigDecimal iva21) {
        this.iva21 = iva21;
    }

    public BigDecimal getNoGravado() {
        return noGravado;
    }

    public void setNoGravado(BigDecimal noGravado) {
        this.noGravado = noGravado;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Date getFechaNotaCredito() {
        return fechaNotaCredito;
    }

    public void setFechaNotaCredito(Date fechaNotaCredito) {
        this.fechaNotaCredito = fechaNotaCredito;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Remesa getRemesa() {
        return remesa;
    }

    public void setRemesa(Remesa remesa) {
        this.remesa = remesa;
    }

    @XmlTransient
    public List<DetalleNotaCreditoProveedor> getDetalleNotaCreditoProveedorList() {
        return detalleNotaCreditoProveedorList;
    }

    public void setDetalleNotaCreditoProveedorList(List<DetalleNotaCreditoProveedor> detalleNotaCreditoProveedorList) {
        this.detalleNotaCreditoProveedorList = detalleNotaCreditoProveedorList;
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
        if (!(object instanceof NotaCreditoProveedor)) {
            return false;
        }
        NotaCreditoProveedor other = (NotaCreditoProveedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotaCreditoProveedor{" + "id=" + id + ", numero=" + numero + ", anulada=" + anulada + ", desacreditado=" + desacreditado + ", fechaCarga=" + fechaCarga + ", fechaNotaCreditoProveedor=" + fechaNotaCredito + ", gravado=" + gravado + ", importe=" + importe + ", impuestosRecuperables=" + impuestosRecuperables + ", iva10=" + iva10 + ", iva21=" + iva21 + ", noGravado=" + noGravado + ", observacion=" + observacion + ", usuario=" + usuario + ", proveedor=" + proveedor + ", detalleNotaCreditoProveedorList=" + detalleNotaCreditoProveedorList + '}';
    }
}
