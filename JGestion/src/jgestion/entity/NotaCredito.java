package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
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
 * @author Administrador
 */
@Entity
@Table(name = "nota_credito", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sucursal", "numero"})})
@NamedQueries({
    @NamedQuery(name = "NotaCredito.findAll", query = "SELECT n FROM NotaCredito n"),
    @NamedQuery(name = "NotaCredito.findById", query = "SELECT n FROM NotaCredito n WHERE n.id = :id"),
    @NamedQuery(name = "NotaCredito.findByNumero", query = "SELECT n FROM NotaCredito n WHERE n.numero = :numero")
})
public class NotaCredito implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false, precision = 8)
    private int numero;
    @Basic(optional = false)
    @Column(name = "fecha_nota_credito", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaNotaCredito;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, updatable = false, insertable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @Basic(optional = false)
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Column(name = "observacion", length = 250)
    private String observacion;
    @Basic(optional = false)
    @Column(name = "gravado", nullable = false)
    private double gravado;
    @Basic(optional = false)
    @Column(name = "no_gravado", nullable = false, precision = 12, scale = 2)
    private BigDecimal noGravado;
    @Basic(optional = false)
    @Column(name = "impuestos_recuperables", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuestosRecuperables;
    @Basic(optional = false)
    @Column(name = "iva10", nullable = false)
    private double iva10;
    @Basic(optional = false)
    @Column(name = "iva21", nullable = false)
    private double iva21;
    @Basic(optional = false)
    @Column(name = "anulada", nullable = false)
    private boolean anulada;
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente", referencedColumnName = "id", nullable = false)
    private Cliente cliente;
    @JoinColumn(name = "usuario", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @JoinColumn(name = "sucursal", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "notaCredito")
    private Collection<DetalleNotaCredito> detalleNotaCreditoCollection;
    /**
     * Antes se podía ir desacreditando en porciones una nota de crédito, ya no
     * mas. Cuando una nota de credito sea utilizada
     * {@link #desacreditado} == {@link #importe}
     *
     * @deprecated
     */
    @Deprecated
    @Column(name = "desacreditado", nullable = false, precision = 12, scale = 2)
    private BigDecimal desacreditado;
    @JoinColumn(name = "recibo")
    @ManyToOne
    private Recibo recibo;

    public NotaCredito() {
    }

    public NotaCredito(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Date getFechaNotaCredito() {
        return fechaNotaCredito;
    }

    public void setFechaNotaCredito(Date fechaNotaCredito) {
        this.fechaNotaCredito = fechaNotaCredito;
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

    public double getGravado() {
        return gravado;
    }

    public void setGravado(double gravado) {
        this.gravado = gravado;
    }

    public BigDecimal getNoGravado() {
        return noGravado;
    }

    public void setNoGravado(BigDecimal noGravado) {
        this.noGravado = noGravado;
    }

    public BigDecimal getImpuestosRecuperables() {
        return impuestosRecuperables;
    }

    public void setImpuestosRecuperables(BigDecimal impuestosRecuperables) {
        this.impuestosRecuperables = impuestosRecuperables;
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

    public boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Collection<DetalleNotaCredito> getDetalleNotaCreditoCollection() {
        return detalleNotaCreditoCollection;
    }

    public void setDetalleNotaCreditoCollection(Collection<DetalleNotaCredito> detalleNotaCreditoCollection) {
        this.detalleNotaCreditoCollection = detalleNotaCreditoCollection;
    }

    /**
     * Antes se podía ir desacreditando en porciones una nota de crédito, ya no
     * mas. Cuando una nota de credito sea utilizada
     * {@link #desacreditado} == {@link #importe}
     *
     * @return
     * @deprecated
     */
    @Deprecated
    public BigDecimal getDesacreditado() {
        return desacreditado;
    }

    /**
     * Antes se podía ir desacreditando en porciones una nota de crédito, ya no
     * mas. Cuando una nota de credito sea utilizada
     * {@link #desacreditado} == {@link #importe}
     *
     * @param desacreditado
     * @deprecated
     */
    @Deprecated
    public void setDesacreditado(BigDecimal desacreditado) {
        this.desacreditado = desacreditado;
    }

    public Recibo getRecibo() {
        return recibo;
    }

    public void setRecibo(Recibo recibo) {
        this.recibo = recibo;
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
        if (!(object instanceof NotaCredito)) {
            return false;
        }
        NotaCredito other = (NotaCredito) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.NotaCredito[id=" + id + ", número=" + numero + ", importe=" + importe + ",desacreditado=" + desacreditado + ", fechaNotaCredito=" + fechaNotaCredito + ", fechaCarga=" + fechaCarga + ", Cliente=" + cliente + ", Usuario=" + usuario + ", Sucursal=" + sucursal + ", detalle.size=" + detalleNotaCreditoCollection.size() + "]";
    }
}
