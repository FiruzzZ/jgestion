package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "nota_debito", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sucursal_id", "numero", "tipo"})
})
public class NotaDebito implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false, precision = 8)
    private int numero;
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
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "notaDebito", orphanRemoval = true)
    private List<DetalleNotaDebito> detalle;

    public NotaDebito() {
    }

    public NotaDebito(Integer id) {
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

    public boolean getAnulada() {
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<DetalleNotaDebito> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleNotaDebito> detalle) {
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
        if (!(object instanceof NotaDebito)) {
            return false;
        }
        NotaDebito other = (NotaDebito) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotaDebito{" + "id=" + id + ", numero=" + numero + ", fechaNotaDebito=" + fechaNotaDebito + ", fechaCarga=" + fechaCarga + ", importe=" + importe + ", observacion=" + observacion + ", gravado=" + gravado + ", noGravado=" + noGravado + ", iva10=" + iva10 + ", iva21=" + iva21 + ", otrosIvas=" + otrosIvas + ", anulada=" + anulada + ", impuestosRecuperables=" + impuestosRecuperables + ", tipo=" + tipo + ", usuario=" + usuario + ", sucursal=" + sucursal + ", cliente=" + cliente + ", detalle=" + detalle + '}';
    }
}
