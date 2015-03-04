package jgestion.entity;

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

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "nota_debito_proveedor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proveedor_id", "numero"})})
@NamedQueries({
    @NamedQuery(name = "NotaDebitoProveedor.findAll", query = "SELECT n FROM NotaDebitoProveedor n")})
public class NotaDebitoProveedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12)
    private Long numero;
    @Basic(optional = false)
    @Column(name = "fecha_nota_debito", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaNotaDebito;
    @Column(name = "fecha_carga", nullable = false, updatable = false, insertable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Column(length = 200)
    private String observacion;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal gravado;
    @Basic(optional = false)
    @Column(name = "no_gravado", nullable = false, precision = 12, scale = 2)
    private BigDecimal noGravado;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal iva10;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal iva21;
    @Basic(optional = false)
    @Column(name = "otros_ivas", nullable = false, precision = 12, scale = 2)
    private BigDecimal otrosIvas;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean anulada;
    @Basic(optional = false)
    @Column(name = "impuestos_recuperables", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuestosRecuperables;
    @Basic(optional = false)
    @Column(nullable = false)
    private char tipo;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @ManyToOne
    private Remesa remesa;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Proveedor proveedor;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "notaDebitoProveedor", orphanRemoval = true)
    private List<DetalleNotaDebitoProveedor> detalle;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Date getFechaNotaDebito() {
        return fechaNotaDebito;
    }

    public void setFechaNotaDebito(Date fechaNotaDebito) {
        this.fechaNotaDebito = fechaNotaDebito;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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

    public BigDecimal getOtrosIvas() {
        return otrosIvas;
    }

    public void setOtrosIvas(BigDecimal otrosIvas) {
        this.otrosIvas = otrosIvas;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public BigDecimal getImpuestosRecuperables() {
        return impuestosRecuperables;
    }

    public void setImpuestosRecuperables(BigDecimal impuestosRecuperables) {
        this.impuestosRecuperables = impuestosRecuperables;
    }

    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Remesa getRemesa() {
        return remesa;
    }

    public void setRemesa(Remesa remesa) {
        this.remesa = remesa;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public List<DetalleNotaDebitoProveedor> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleNotaDebitoProveedor> detalle) {
        this.detalle = detalle;
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
        if (!(object instanceof NotaDebitoProveedor)) {
            return false;
        }
        NotaDebitoProveedor other = (NotaDebitoProveedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotaDebitoProveedor{" + "id=" + id + ", numero=" + numero + ", fechaNotaDebito=" + fechaNotaDebito + ", fechaCarga=" + fechaCarga + ", importe=" + importe + ", observacion=" + observacion + ", gravado=" + gravado + ", noGravado=" + noGravado + ", iva10=" + iva10 + ", iva21=" + iva21 + ", otrosIvas=" + otrosIvas + ", anulada=" + anulada + ", impuestosRecuperables=" + impuestosRecuperables + ", tipo=" + tipo + ", usuario=" + usuario.getId() + ", remesa=" + (remesa != null ? remesa.getId() : null) + ", proveedor=" + (proveedor != null ? proveedor.getId() : null) + ", detalle=" + (detalle != null ? detalle.size() : null) + '}';
    }
}
