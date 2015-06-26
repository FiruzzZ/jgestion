package jgestion.controller;

import afip.ws.exception.WSAFIPErrorResponseException;
import afip.ws.wsaa.WSAA;

//entorno de test/homologación
//import afip.ws.jaxws.*;
//import afip.ws.wsfe.AFIPTestClient;

//entorno de producción
import afip.ws.produccion.fev1.*;
import afip.ws.wsfe.AFIPFEVClient;

import generics.CustomABMJDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.UnrecoverableKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaElectronica;
import jgestion.entity.FacturaVenta;
import jgestion.entity.NotaCredito;
import jgestion.entity.NotaDebito;
import jgestion.entity.Recibo;
import jgestion.gui.JDABM;
import jgestion.gui.PanelAFIPWSConsultarCbte;
import jgestion.gui.WSFEVerificacionPanel;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class AFIPWSController {

    /**
     * ./ws/
     */
    private static final String WS_FOLDER = "." + File.separator + "ws" + File.separator;
    private static final String TICKET_ACCESS_XML = WS_FOLDER + "tar.xml";
    private static final String WS_CLIENT_PROPERTIES = WS_FOLDER + "wsaa_client.properties";
    private static final Logger LOG = Logger.getLogger(AFIPWSController.class);

//    private AFIPTestClient aFIPClient;
    private AFIPFEVClient aFIPClient;
    private WSFEVerificacionPanel panelWSFE;
    private String eventos;
    private Document TA_XML;

    /**
     * Se encarga de instanciar un con un TickerAccess no expirado (si existiera uno pero ya
     * expirado, genera uno nuevo a través de {@link WSAA})
     *
     * @param pwdPKCS12
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws Exception
     */
    public AFIPWSController(String pwdPKCS12) throws ParserConfigurationException, SAXException, IOException, Exception {
        if (!aFIPClient.areServicesAvailable()) {
            throw new MessageException("Los servicios de la AFIP no se encuentran disponibles.");
        }
        boolean expired = true;
        File ticketAccessXMLFile = new File(TICKET_ACCESS_XML);
        if (ticketAccessXMLFile.exists()) {
            TA_XML = getDocument(ticketAccessXMLFile);
            Date expirationTime = WSAA.getExpirationTime(TA_XML);
            expired = expirationTime.before(JGestionUtils.getServerDate());
        }
        if (expired) {
            try {
                File f = new File(WS_CLIENT_PROPERTIES);
                if (!f.exists()) {
                    throw new MessageException("No se encontró el archivo de propiedades de WS: " + WS_CLIENT_PROPERTIES);
                }
                Properties config = new Properties();
                config.load(new FileInputStream(WS_CLIENT_PROPERTIES));
                //en el archivo de propiedad no se hace referencia a ningún path específico, solo al nombre del archivo PKCS#12
                config.setProperty("keystore", WS_FOLDER + config.getProperty("keystore"));
                WSAA wsaa = new WSAA(config);
                String tarXML = wsaa.getLoginTicketResponse(null, pwdPKCS12, null);
                UTIL.createFile(tarXML, ticketAccessXMLFile.getCanonicalPath());
            } catch (UnrecoverableKeyException ex) {
                throw new MessageException("No es posible crear el certificado porque la contraseña del archivo PCKS#12 no es correcta"
                        + "\n" + ex.getMessage());
            }
        }
        TA_XML = getDocument(ticketAccessXMLFile);
//        aFIPClient = new AFIPTestClient(TA_XML);
        aFIPClient = new AFIPFEVClient(TA_XML);
    }

    /**
     * Parse the content of the given file as an XML document and return a new DOM
     * {@link org.w3c.dom.Document} object normalized ({@link org.w3c.dom.Document#normalize()}). An
     * IllegalArgumentException is thrown if the File is null null.
     *
     * @param file
     * @return A new DOM Document object.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document getDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        TA_XML = documentBuilder.parse(file);
        TA_XML.normalize();
        return TA_XML;
    }

    /**
     *
     * @param ptoVta
     * @param cbteTipo para saber el ID usar {@link #getTipoComprobanteID(java.lang.Object)}
     * @return
     * @throws WSAFIPErrorResponseException
     */
    public int getUltimoCompActualizado(int ptoVta, int cbteTipo) throws WSAFIPErrorResponseException {
        CbteTipo cbteTipoo = new CbteTipo();
        cbteTipoo.setId(cbteTipo);
        return aFIPClient.getUltimoCompActualizado(ptoVta, cbteTipoo);
    }

    public FacturaElectronica requestCAE(FacturaVenta fv) throws WSAFIPErrorResponseException, MessageException {
        ConceptoTipo conceptoTipo = new ConceptoTipo();
        conceptoTipo.setId(3);//prod y serv
        DocTipo docTipo = new DocTipo();
        //80, CUIT, 20080725, NULL
        //86, CUIL, 20080725, NULL
        //87, CDI, 20080725, NULL
        //89, LE, 20080725, NULL
        //90, LC, 20080725, NULL
        //91, CI Extranjera, 20080725, NULL
        //92, en trámite, 20080725, NULL
        //93, Acta Nacimiento, 20080725, NULL
        //95, CI Bs. As. RNP, 20080725, NULL
        //96, DNI, 20080725, NULL
        docTipo.setId(80);
        Moneda moneda = new Moneda();
        moneda.setId("PES");
        CbteTipo cbteTipo = new CbteTipo();
        cbteTipo.setId(getTipoComprobanteID(fv));
        FacturaElectronica fe = invokeFE(fv, conceptoTipo, cbteTipo, docTipo, moneda, 1, new ArrayList<>());
        return fe;
    }

    private FacturaElectronica invokeFE(FacturaVenta fv, ConceptoTipo conceptoTipo,
            CbteTipo cbteTipo, DocTipo docTipo, Moneda moneda, double monCotizacion,
            List<Tributo> tributoList) throws WSAFIPErrorResponseException, MessageException {
        final int nextOne = aFIPClient.getUltimoCompActualizado(fv.getSucursal().getPuntoVenta(), cbteTipo) + 1;
        if (!Objects.equals(nextOne + "", fv.getNumero() + "")) {
            throw new MessageException("El número de Factura: " + fv.getNumero() + " no coincide con el esperado por AFIP: " + nextOne);
        }
        List<IvaTipo> iVATipoList = getIVATipoList();
        FECAERequest fECAERequest = new FECAERequest();
        FECAECabRequest fECAECabRequest = new FECAECabRequest();

        fECAECabRequest.setCbteTipo(cbteTipo.getId());
        fECAECabRequest.setPtoVta(fv.getSucursal().getPuntoVenta());

        HashMap<IvaTipo, BigDecimal> totalesAlicuotas = new HashMap<>();
        //inicializa todos los totales
        for (IvaTipo o : iVATipoList) {
            totalesAlicuotas.put(o, BigDecimal.ZERO);
        }

        List<DetalleVenta> detallesVentaList = fv.getDetallesVentaList();
        //recorriendo el detalle de la facturaVenta
        for (DetalleVenta detalleVenta : detallesVentaList) {
            boolean unknownIVA = true;
            int cantidad = detalleVenta.getCantidad();
            Double precioUnitario = detalleVenta.getPrecioUnitario().subtract(detalleVenta.getDescuento()).doubleValue();
            Double productoIVA = Double.valueOf(detalleVenta.getProducto().getIva().getIva());
            //identificando el IVA de cada item del detalle
            for (IvaTipo o : iVATipoList) {
                Double AFIP_IVA = Double.valueOf(o.getDesc().replaceAll("%", ""));
                if (0 == (AFIP_IVA.compareTo(productoIVA))) {
                    //acumulando 
                    BigDecimal currentTotalIVA = totalesAlicuotas.get(o);
                    currentTotalIVA = currentTotalIVA.add(BigDecimal.valueOf(cantidad * precioUnitario)).setScale(2, RoundingMode.HALF_UP);
                    totalesAlicuotas.put(o, currentTotalIVA);
                    unknownIVA = false;
                    break;
                }
            }
            if (unknownIVA) {
                //no existe la alicuota IVA que tiene el producto..
                throw new MessageException("El producto:"
                        + "\n(" + detalleVenta.getProducto().getCodigo() + ") "
                        + detalleVenta.getProducto().getNombre()
                        + "\n tiene una alícuota (IVA) \"" + productoIVA + "\" que no existe en los registros de la AFIP.");
            }
        }
        ArrayOfAlicIva arrayOfAlicIva = new ArrayOfAlicIva();
        //total de todos los IVA's
        double impIVA = 0;
        //se agregan las alic con importe > 0
        for (IvaTipo ivaTipo : totalesAlicuotas.keySet()) {
            BigDecimal totalAlic = totalesAlicuotas.get(ivaTipo);
            if (totalAlic.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalImporte = totalAlic.multiply(BigDecimal.valueOf((Double.valueOf(ivaTipo.getDesc().replaceAll("%", "")) / 100)))
                        .setScale(2, RoundingMode.HALF_UP);
                AlicIva alic = new AlicIva();
                alic.setId(Integer.parseInt(ivaTipo.getId()));
                alic.setBaseImp(totalAlic.doubleValue());
                alic.setImporte(totalImporte.doubleValue());
                LOG.trace("IVA Tipo: " + ivaTipo.getDesc() + " | " + alic.toString());
                arrayOfAlicIva.getAlicIva().add(alic);
                impIVA += alic.getImporte();
            }
        }

        //armando detalle
        //gravado (neto) + impuestos + tributos
        double impTotal = fv.getImporte();
        //importe NETO NO GRAVADO
        double impTotalConcepto = 0;
        //gravado (neto)
        double impNeto = fv.getGravado();
        double impExento = 0;
        double impTributos = 0;
//        double cotizacion = cotiz;

        ArrayOfCbteAsoc arrayOfCbteAsoc = null;
        ArrayOfTributo arrayOfTributo = null;
        if (!tributoList.isEmpty()) {
            arrayOfTributo = new ArrayOfTributo();
            for (Tributo tributo : tributoList) {
                arrayOfTributo.getTributo().add(tributo);
                impTributos += tributo.getImporte();
            }
        }
        ArrayOfFECAEDetRequest arrayOfFECAEDetRequest = new ArrayOfFECAEDetRequest();
        FECAEDetRequest detalle = new FECAEDetRequest();
        detalle.setConcepto(conceptoTipo.getId());
        detalle.setDocTipo(docTipo.getId());
        detalle.setDocNro(fv.getCliente().getNumDoc());
        detalle.setCbteDesde(fv.getNumero());
        detalle.setCbteHasta(fv.getNumero());
        detalle.setCbteFch(getXMLDate(fv.getFechaVenta()));
        detalle.setImpTotal(impTotal);
        detalle.setImpTotConc(impTotalConcepto);
        detalle.setImpNeto(impNeto);
        detalle.setImpOpEx(impExento);
        detalle.setImpTrib(impTributos);
        detalle.setImpIVA(BigDecimal.valueOf(impIVA).setScale(2, RoundingMode.HALF_UP).doubleValue());
        detalle.setFchServDesde(getXMLDate(fv.getFechaVenta()));
        detalle.setFchServHasta(getXMLDate(fv.getFechaVenta()));
        detalle.setFchVtoPago(getXMLDate(fv.getFechaVenta()));
        detalle.setMonId(moneda.getId());
        detalle.setMonCotiz(monCotizacion);
        //array's....
        detalle.setCbtesAsoc(arrayOfCbteAsoc);
        detalle.setTributos(arrayOfTributo);
        detalle.setIva(arrayOfAlicIva);
        detalle.setOpcionales(null);
        LOG.trace(detalle);
        arrayOfFECAEDetRequest.getFECAEDetRequest().add(detalle);

        fECAECabRequest.setCantReg(arrayOfFECAEDetRequest.getFECAEDetRequest().size());
        fECAERequest.setFeCabReq(fECAECabRequest);
        fECAERequest.setFeDetReq(arrayOfFECAEDetRequest);

        FECAEResponse fECAE = aFIPClient.invokeFECAESolicitar(fECAERequest);
        FECAECabResponse feCabResp = fECAE.getFeCabResp();
        FECAEDetResponse fEDetalle = fECAE.getFeDetResp().getFECAEDetResponse().get(0);
        LOG.info(feCabResp);
        LOG.info(fEDetalle);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat caeVtoformat = new SimpleDateFormat("yyyyMMdd");
        FacturaElectronica facturaElectronica = null;
        StringBuilder sb = new StringBuilder(100);
        if (fEDetalle.getObservaciones() != null
                && fEDetalle.getObservaciones().getObs() != null
                && !fEDetalle.getObservaciones().getObs().isEmpty()) {
            sb = new StringBuilder("");
            for (Obs obs : fEDetalle.getObservaciones().getObs()) {
                sb.append("code=").append(obs.getCode()).append(", msg=").append(obs.getMsg());
            }
        }
        if (feCabResp.getResultado().equalsIgnoreCase("R")) {
            throw new WSAFIPErrorResponseException(sb.toString());
        }
        try {
            facturaElectronica = new FacturaElectronica(null, feCabResp.getCbteTipo(),
                    Long.valueOf(fv.getNumero()).intValue(),
                    fv.getSucursal().getPuntoVenta(), sdf.parse(feCabResp.getFchProceso()),
                    feCabResp.getResultado(), fEDetalle.getConcepto(),
                    fEDetalle.getCAE(), caeVtoformat.parse(fEDetalle.getCAEFchVto()),
                    sb.toString().isEmpty() ? null : sb.toString());
        } catch (ParseException ex) {
            LOG.error("parseando xml date", ex);
        }
        return facturaElectronica;
    }

    public FacturaElectronica getFEComprobante(int ptoVenta, int cbteNro, int cbteTipo) throws WSAFIPErrorResponseException {
        FECompConsultaReq fec = new FECompConsultaReq();
        fec.setPtoVta(ptoVenta);
        fec.setCbteNro(cbteNro);
        fec.setCbteTipo(cbteTipo);
        FECompConsultaResponse o = aFIPClient.getComprobante(fec);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        FECompConsResponse res = o.getResultGet();
        String observaciones = null;
        if (res.getObservaciones() != null && res.getObservaciones().getObs() != null && !res.getObservaciones().getObs().isEmpty()) {
            observaciones = "";
            for (Obs ob : res.getObservaciones().getObs()) {
                observaciones += "code=" + ob.getCode() + ", msg=" + ob.getMsg();
            }
        }
        try {
            return new FacturaElectronica(null, cbteTipo, ptoVenta, cbteNro, sdf.parse(res.getFchProceso()), res.getResultado(), res.getConcepto(), res.getCodAutorizacion(), null, observaciones);
        } catch (ParseException ex) {
            LOG.error("parseando xml date=" + res.getFchProceso(), ex);
            throw new WSAFIPErrorResponseException("Fecha proceso no válida: " + res.getFchProceso());
        }
    }

    public List<CbteTipo> getComprobantesTipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getComprobantesTipoList();
    }

    public List<ConceptoTipo> getConceptoTipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getConceptoTipoList();
    }

    public List<DocTipo> getDocumentosTipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getDocumentosTipoList();
    }

    public List<IvaTipo> getIVATipoList() throws WSAFIPErrorResponseException {
        return aFIPClient.getIVATipoList();
    }

    String getEventos() {
        return eventos;
    }

    /**
     * Convert a Date to a formatted XML Date String (yyyyMMdd)
     *
     * @param fecha To be converted
     * @return A XML Date String formatted. If <tt>fecha</tt> == null, returns null.
     */
    private String getXMLDate(Date fecha) {
        if (fecha == null) {
            return null;
        }
        return new SimpleDateFormat("yyyyMMdd").format(fecha);
    }

    public JDialog showSetting(final FacturaVenta fv) throws WSAFIPErrorResponseException, MessageException {
        if (panelWSFE == null) {
            panelWSFE = new WSFEVerificacionPanel();
            panelWSFE.getBtnGetCotizAFIP().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Cotizacion cotizacion = aFIPClient.getCotizacion(((Moneda) panelWSFE.getCbMonendas().getSelectedItem()).getId());
                        panelWSFE.getTfCotizacion().setText(String.valueOf(cotizacion.getMonCotiz()));
                    } catch (WSAFIPErrorResponseException ex) {
                        JOptionPane.showMessageDialog(panelWSFE, ex.getMessage(), "Error obteniendo cotización", JOptionPane.ERROR_MESSAGE);
                        LOG.error(ex);
                    }
                }
            });
        }
        List<CbteTipo> comprobantesTipo = getComprobantesTipoList();
        List<ConceptoTipo> conceptoTipoList = getConceptoTipoList();
        List<DocTipo> docTipoList = getDocumentosTipoList();
        List<Moneda> monedaTipoList = aFIPClient.getMonedaTipoList();
        List<TributoTipo> tributoTipoList = aFIPClient.getTributoTipoList();

        //setting data panel
        UTIL.loadComboBox(panelWSFE.getCbComprobantes(), comprobantesTipo, false);
        UTIL.loadComboBox(panelWSFE.getCbConceptos(), conceptoTipoList, false);
        UTIL.loadComboBox(panelWSFE.getCbDocumentos(), docTipoList, false);
        UTIL.loadComboBox(panelWSFE.getCbMonendas(), monedaTipoList, false);
        UTIL.loadComboBox(panelWSFE.getCbTributos(), tributoTipoList, false);
        CbteTipo ct = new CbteTipo();
        ct.setId(getTipoComprobanteID(fv));
        final int lastOne = aFIPClient.getUltimoCompActualizado(fv.getSucursal().getPuntoVenta(), ct) + 1;
        panelWSFE.getTfCbteNumero().setText(lastOne + "");
        UTIL.setSelectedItem(panelWSFE.getCbComprobantes(), ct);
        panelWSFE.getTfTotalNeto().setText(UTIL.PRECIO_CON_PUNTO.format(fv.getGravado()));
        panelWSFE.getTfTotalIVAs().setText(UTIL.PRECIO_CON_PUNTO.format(fv.getImporte() - fv.getGravado()));
        panelWSFE.getTfTotalTributos().setText(UTIL.PRECIO_CON_PUNTO.format(0));
        panelWSFE.getTfTotal().setText(UTIL.PRECIO_CON_PUNTO.format(fv.getImporte()));
        panelWSFE.getDcFechaCbte().setDate(fv.getFechaVenta());
        final JDABM jDABM1 = new JDABM(null, null, true, panelWSFE);
        jDABM1.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CbteTipo cbteTipo = (CbteTipo) panelWSFE.getCbComprobantes().getSelectedItem();
                ConceptoTipo conceptoTipo = (ConceptoTipo) panelWSFE.getCbConceptos().getSelectedItem();
                DocTipo docTipo = ((DocTipo) panelWSFE.getCbDocumentos().getSelectedItem());
                Moneda moneda = (Moneda) panelWSFE.getCbMonendas().getSelectedItem();
                double cotizacion = moneda.getId().equalsIgnoreCase("PES") ? 1
                        : Double.valueOf(panelWSFE.getTfCotizacion().getText());
                List<Tributo> tributoList = new ArrayList<>();
                try {
                    FacturaElectronica fe = invokeFE(fv, conceptoTipo, cbteTipo, docTipo, moneda, cotizacion, tributoList);
                    System.out.println(fe.toString());
                } catch (WSAFIPErrorResponseException ex) {
                    JOptionPane.showMessageDialog(jDABM1, ex.getMessage(), "Web Service AFIP Error", JOptionPane.ERROR_MESSAGE);
                } catch (MessageException ex) {
                    ex.displayMessage(jDABM1);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(jDABM1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error(ex);
                }
            }
        });
        jDABM1.getbCancelar().addActionListener((ActionEvent e) -> jDABM1.dispose());
        return jDABM1;
    }

    /**
     * Campo CbteTipo sea: {@link FacturaElectronica#cbteTipo}
     *
     * @param o {@link FacturaVenta}, {@link Recibo}, {@link NotaCredito}, {@link NotaDebito}
     * @return
     */
    public static final int getTipoComprobanteID(Object o) {
        if (o instanceof FacturaVenta) {
            FacturaVenta fv = (FacturaVenta) o;
            if (String.valueOf(fv.getTipo()).equalsIgnoreCase("A")) {
                return 1;
            } else if (String.valueOf(fv.getTipo()).equalsIgnoreCase("B")) {
                return 6;
            } else if (String.valueOf(fv.getTipo()).equalsIgnoreCase("C")) {
                return 11;
            }
        }
        return 0;
    }

    public void consultarCAEs() throws WSAFIPErrorResponseException {
        final PanelAFIPWSConsultarCbte p = new PanelAFIPWSConsultarCbte();
        UTIL.loadComboBox(p.getCbSucursal(), JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales()), false);
        UTIL.loadComboBox(p.getCbCbte(), aFIPClient.getComprobantesTipoList(), false);
        CustomABMJDialog abm = new CustomABMJDialog(null, p, "Consultar Comprobantes", true, "Bla bla bla..");
        abm.getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FacturaElectronica feComprobante = getFEComprobante(
                            Integer.valueOf(p.getTfPtoVta().getText()),
                            Integer.valueOf(p.getTfNumero().getText()),
                            ((CbteTipo) p.getCbCbte().getSelectedItem()).getId());

                } catch (WSAFIPErrorResponseException ex) {
                    LOG.error(ex, ex);
                }
            }
        });
    }

    public List<Integer> getPuntoVentas() throws WSAFIPErrorResponseException {
        List<Integer> l = new ArrayList<>();
        for (PtoVenta pv : aFIPClient.getPtoVentaList()) {
            l.add((int) pv.getNro());
        }
        return l;
    }
}
