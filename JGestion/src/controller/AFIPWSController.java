package controller;

import afip.ws.jaxws.*;
import afip.ws.wsaa.WSAA;
import afip.ws.wsfe.AFIPClient;
import afip.ws.wsfe.exceptions.WSAFIPErrorResponseException;
import afip.ws.wsfe.gui.PanelWSFE;
import controller.exceptions.MessageException;
import entity.DetalleVenta;
import entity.FacturaElectronica;
import entity.FacturaVenta;
import gui.JDABM;
import gui.JDWSAASetting;
import gui.WSFEVetificacionPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class AFIPWSController {

    public static final String TICKET_ACCESS_XML_PATH = ".\\ws\\tar.xml";
    private AFIPClient aFIPClient;
    private JDABM jDABM;
    private WSFEVetificacionPanel panelWSFE;
    private String eventos;
    private org.w3c.dom.Document TA_XML;

    public AFIPWSController() throws ParserConfigurationException, SAXException, IOException, Exception {
        this(new File(AFIPWSController.TICKET_ACCESS_XML_PATH));
    }

    public AFIPWSController(File ticketAccessXMLFile) throws ParserConfigurationException, SAXException, IOException, Exception {
        if (ticketAccessXMLFile == null || !ticketAccessXMLFile.exists()) {
            throw new IllegalArgumentException("No se encontró el archivo de certificación de la AFIP.\n"
                    + "\nSi dispone de este archivo, haga una copia, pegue y renombrelo como se indica a continuación:"
                    + ticketAccessXMLFile.getAbsolutePath()
                    + "\nSino debe crear el suyo en la página de la AFIP.");
        }
        if (!AFIPClient.areServicesAvailable()) {
            throw new MessageException("Los servicios de la AFIP no se encuentran disponibles.");
        }
        TA_XML = getDocument(ticketAccessXMLFile);
        Calendar expirationTime = getExpirationTime(TA_XML);
        //chequea si el ticket está vigente
        if (expirationTime.before(new GregorianCalendar())) {
            String loginTicketResponse = null;
            try {
                loginTicketResponse = WSAA.getLoginTicketResponse(null, null, null);
            } catch (UnrecoverableKeyException ex) {
                throw new MessageException("No es posible crear el certificado porque contraseña del archivo PCKS#12 no es correcta"
                        + "\n" + ex.getMessage());
            }
            UTIL.createFile(loginTicketResponse, ticketAccessXMLFile.getCanonicalPath());
            Logger.getLogger(this.getClass()).trace("TRACE - new TicketAccess expirationTime" + expirationTime.getTime());
            TA_XML = getDocument(ticketAccessXMLFile);
        }
        aFIPClient = new AFIPClient(TA_XML);
    }

    /**
     * Parse the content of the given file as an XML document and return a new
     * DOM {@link org.w3c.dom.Document} object normalized
     * ({@link org.w3c.dom.Document#normalize()}). An IllegalArgumentException
     * is thrown if the File is null null.
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

    public JDialog initFE(JFrame owner) throws WSAFIPErrorResponseException {
//
//        panelWSFE.getBtnAddItem().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int cantidad;
//                double precioUnitario;
//
//                try {
//                    if (panelWSFE.getjTabbedPane1().getSelectedIndex() == 0) { //tab Producto
//                        Producto p = (Producto) panelWSFE.getCbProductos().getSelectedItem();
//                        if (p == null) {
//                            throw new IllegalArgumentException("Debe seleccionar un producto");
//                        }
//                        try {
//                            cantidad = Integer.parseInt(panelWSFE.getTfProductoCantidad().getText());
//                        } catch (Exception ex) {
//                            throw new IllegalArgumentException("cantidad no válida");
//                        }
//                        try {
//                            precioUnitario = Double.parseDouble(panelWSFE.getTfProductoPrecioUnit().getText());
//                        } catch (Exception ex) {
//                            throw new IllegalArgumentException("precio unitario no válida");
//                        }
//                        addItemToFE((Producto) panelWSFE.getCbProductos().getSelectedItem(),
//                                cantidad,
//                                Double.parseDouble(((IvaTipo) panelWSFE.getCbProductoAlicuotas().getSelectedItem()).getDesc().replaceAll("%", "")),
//                                precioUnitario);
//                    }
//                } catch (Exception ex) {
//                    JOptionPane.showMessageDialog(jDABM, ex.getMessage());
//                }
//            }
//
//            private void addItemToFE(Producto selectedProducto, int cantidad, double alicuota, double precioUnitario) {
//                DefaultTableModel dtm = UTIL.getDtm(panelWSFE.getTableDetalle());
//                double unitarioConIva = precioUnitario + UTIL.getPorcentaje(precioUnitario, alicuota);
//                dtm.addRow(new Object[]{
//                            selectedProducto.getIva().toString(),
//                            selectedProducto.getCodigo(),
//                            selectedProducto.getNombre() + "(" + selectedProducto.getIva().toString() + ")",
//                            cantidad,
//                            UTIL.PRECIO_CON_PUNTO.format(precioUnitario),
//                            UTIL.PRECIO_CON_PUNTO.format(unitarioConIva),
//                            null,
//                            UTIL.PRECIO_CON_PUNTO.format((cantidad * unitarioConIva)), //Total
//                            null,//Tipo de descuento
//                            selectedProducto.getId(),
//                            null
//                        });
//                refreshResumen();
//            }
//        });
//        panelWSFE.getBtnDropItem().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        });
//        panelWSFE.getBtnAddTributo().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        });
//        panelWSFE.getBtnDropTributo().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            }
//        });
//        panelWSFE.getBtnGetCotizAFIP().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    if (panelWSFE.getCbMonendas().getSelectedIndex() > 0) {
//                        Cotizacion cotizacion = aFIPClient.getCotizacion(((Moneda) panelWSFE.getCbMonendas().getSelectedItem()).getId());
//                        panelWSFE.getTfCotizacion().setText(String.valueOf(cotizacion.getMonCotiz()));
//                    } else {
//                        panelWSFE.getTfCotizacion().setText("1");
//                    }
//                } catch (WSAFIPErrorResponseException ex) {
//                    JOptionPane.showMessageDialog(jDABM, ex.getMessage(), "Error obteniendo cotización", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(AFIPWSController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
//        jDABM = new JDABM(owner, true, panelWSFE);
//        jDABM.getbAceptar().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    doFE(panelWSFE);
//                } catch (WSAFIPErrorResponseException ex) {
//                    jDABM.showMessage(ex.getMessage(), "AFIP FE Error", JOptionPane.ERROR_MESSAGE);
//                    Logger.getLogger(AFIPWSController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
//        jDABM.getbCancelar().addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                jDABM.dispose();
//            }
//        });
//        jDABM.setTitle("Facturación Electrónica AFIP");
        return jDABM;
    }

    private void refreshResumen() {
//        Double gravado = 0.0;
//        Double impTotal = 0.0;
////        Double iva21 = 0.0;
//        Double tribTotal = 0.0;
//        Double subTotal = 0.0;
//        DefaultTableModel detalle = (DefaultTableModel) panelWSFE.getTableDetalle().getModel();
//        DefaultTableModel tributos = (DefaultTableModel) panelWSFE.getTableTributos().getModel();
//        for (int index = 0; index < detalle.getRowCount(); index++) {
//            double cantidad = Double.valueOf(detalle.getValueAt(index, 3).toString());
//
//            // precioSinIVA + cantidad
//            gravado += (cantidad * Double.valueOf(detalle.getValueAt(index, 4).toString()));
//
//            // IVA's ++
//            impTotal += cantidad * UTIL.getPorcentaje(Double.valueOf(detalle.getValueAt(index, 4).toString()), 10.5);
//
//            subTotal += Double.valueOf(detalle.getValueAt(index, 7).toString());
//        }
//
//        for (int index = 0; index < tributos.getRowCount(); index++) {
//            tribTotal += Double.valueOf(tributos.getValueAt(index, 3).toString());
//        }
//
//        panelWSFE.getTfTotalNeto().setText(UTIL.PRECIO_CON_PUNTO.format(gravado));
//        panelWSFE.getTfTotalIVAs().setText(UTIL.PRECIO_CON_PUNTO.format(impTotal));
//        panelWSFE.getTfTotalTributos().setText(UTIL.PRECIO_CON_PUNTO.format(tribTotal));
//        panelWSFE.getTfTotal().setText(UTIL.PRECIO_CON_PUNTO.format(subTotal));
    }

    private void doFE(PanelWSFE panelFE) throws WSAFIPErrorResponseException {
//        IvaTipo iVATipo; //= (IvaTipo) panelFE.getCbAlicuotas().getSelectedItem();
//        long docNro = Long.parseLong(panelFE.getTfDocNumero().getText());
//        FECAERequest fECAERequest = new FECAERequest();
//        FECAECabRequest fECAECabRequest = new FECAECabRequest();
    }

    private Calendar getExpirationTime(org.w3c.dom.Document ta)
            throws SAXException, ParserConfigurationException {
        NodeList nodeLst = ta.getElementsByTagName("header");
        Node node = nodeLst.item(0);
        Element element = (Element) node;
        String XSDDateTime = AFIPClient.getTagValue("expirationTime", element);
        Calendar parseDate = DatatypeConverter.parseDateTime(XSDDateTime);
        System.out.println("TRACE: TicketAccess > expirationTime=" + parseDate.getTime());
        return parseDate;
    }

    public static JDialog initWSAA(JFrame jFrame) {
        JDWSAASetting jDWSAA = new JDWSAASetting(jFrame, true);
        return jDWSAA;
    }

    private FacturaElectronica invokeFE(FacturaVenta fv, ConceptoTipo conceptoTipo,
            CbteTipo cbteTipo, DocTipo docTipo, Moneda moneda, double monCotizacion,
            Date fechaServDesde, Date fechaServHasta, List<Tributo> tributoList)
            throws WSAFIPErrorResponseException, MessageException {
        List<IvaTipo> iVATipoList = getIVATipoList();
        FECAERequest fECAERequest = new FECAERequest();
        FECAECabRequest fECAECabRequest = new FECAECabRequest();

        fECAECabRequest.setCbteTipo(cbteTipo.getId());
        fECAECabRequest.setPtoVta(fv.getSucursal().getPuntoVenta().intValue());
        int ultimoCompActualizado;
        ultimoCompActualizado = aFIPClient.getUltimoCompActualizado(fv.getSucursal().getPuntoVenta().intValue(), new CbteTipo(fECAECabRequest.getCbteTipo()));



        ArrayOfFECAEDetRequest arrayOfFECAEDetRequest = new ArrayOfFECAEDetRequest();
        ArrayOfAlicIva arrayOfAlicIva = new ArrayOfAlicIva();


        HashMap<IvaTipo, Double> totalesAlicuotas = new HashMap<IvaTipo, Double>();
        //inicializa todos los totales
        for (IvaTipo o : iVATipoList) {
            totalesAlicuotas.put(o, 0.0);
            Logger.getLogger(this.getClass()).debug("IvaTipo =" + o.getDesc());
        }

        List<DetalleVenta> detallesVentaList = fv.getDetallesVentaList();
        //recorriendo el detalle de la facturaVenta
        for (DetalleVenta detalleVenta : detallesVentaList) {
            boolean unknownIVA = true;
            int cantidad = detalleVenta.getCantidad();
            Double precioUnitario = detalleVenta.getPrecioUnitario().doubleValue() - detalleVenta.getDescuento().doubleValue();
            Double productoIVA = Double.valueOf(detalleVenta.getProducto().getIva().getIva());
            //identificando el IVA de cada item del detalle
            for (IvaTipo o : iVATipoList) {
                Double AFIP_IVA = Double.valueOf(o.getDesc().replaceAll("%", ""));
                if (0 == (AFIP_IVA.compareTo(productoIVA))) {
                    //acumulando 
                    Double currentTotalIVA = totalesAlicuotas.get(o);
                    currentTotalIVA += cantidad * precioUnitario;
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

        //total de todos los IVA's
        double impIVA = 0;
        //se agregan las alic con impuesto > a 0
        for (IvaTipo ivaTipo : totalesAlicuotas.keySet()) {
            double totalAlic = totalesAlicuotas.get(ivaTipo);
            if (totalAlic > 0) {
                double totalImporte = totalAlic * (Double.valueOf(ivaTipo.getDesc().replaceAll("%", "")) / 100);
                AlicIva alic = new AlicIva();
                alic.setId(Integer.parseInt(ivaTipo.getId()));
                alic.setBaseImp(totalAlic);
                alic.setImporte(totalImporte);
                Logger.getLogger(this.getClass()).trace("IVA Tipo: " + ivaTipo.getDesc() + " | " + alic.toString());
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
        if (tributoList != null && !tributoList.isEmpty()) {
            arrayOfTributo = new ArrayOfTributo();
            for (Tributo tributo : tributoList) {
                arrayOfTributo.getTributo().add(tributo);
                impTributos += tributo.getImporte();
            }
        }

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
        detalle.setImpIVA(impIVA);
        if (fechaServDesde != null && fechaServHasta != null) {
            detalle.setFchServDesde(getXMLDate(fechaServDesde));
            detalle.setFchServHasta(getXMLDate(fechaServHasta));
            detalle.setFchVtoPago(getXMLDate(fv.getFechaVenta()));
        }
        detalle.setMonId(moneda.getId());
        detalle.setMonCotiz(monCotizacion);
        //array's....
        detalle.setCbtesAsoc(arrayOfCbteAsoc);
        detalle.setTributos(arrayOfTributo);
        detalle.setIva(arrayOfAlicIva);
        detalle.setOpcionales(null);
        Logger.getLogger(this.getClass()).trace(detalle.toString());
        arrayOfFECAEDetRequest.getFECAEDetRequest().add(detalle);

        fECAECabRequest.setCantReg(arrayOfFECAEDetRequest.getFECAEDetRequest().size());
        fECAERequest.setFeCabReq(fECAECabRequest);
        fECAERequest.setFeDetReq(arrayOfFECAEDetRequest);

        FECAEResponse fECAE = aFIPClient.invokeFECAESolicitar(fECAERequest);
        FECAECabResponse feCabResp = fECAE.getFeCabResp();
        FECAEDetResponse fEDetalle = fECAE.getFeDetResp().getFECAEDetResponse().get(0);
        FacturaElectronica facturaElectronica = new FacturaElectronica(null,
                feCabResp.getFchProceso(), feCabResp.getResultado(),
                fEDetalle.getConcepto(), feCabResp.getCbteTipo(), fv.getId());
        facturaElectronica.setCae(fEDetalle.getCAE());
        StringBuilder sb;
        if (fEDetalle.getObservaciones() != null
                && fEDetalle.getObservaciones().getObs() != null
                && !fEDetalle.getObservaciones().getObs().isEmpty()) {
            sb = new StringBuilder("Observaciones");
            for (Obs obs : fEDetalle.getObservaciones().getObs()) {
                sb.append("\n").append(obs.getCode()).append(": ").append(obs.getMsg());
            }
            facturaElectronica.setObservaciones(sb.toString());
        }
        return facturaElectronica;
    }

    public void getFEComprobante(int ptoVenta, long cbteNro, int cbteTipo) {
        FECompConsultaReq fec = new FECompConsultaReq();
        fec.setPtoVta(ptoVenta);
        fec.setCbteNro(cbteNro);
        fec.setCbteTipo(cbteTipo);
        FECompConsultaResponse o = aFIPClient.getComprobante(fec);
        FECompConsResponse comprobanteResp = o.getResultGet();
        Logger.getLogger(this.getClass()).trace(comprobanteResp.toString());

    }

    private void checkErrorsAndEvents(ArrayOfErr errors, ArrayOfEvt events)
            throws WSAFIPErrorResponseException {
        StringBuilder sbErrors = null;
        StringBuilder sbEvents = null;
        if (errors != null && !errors.getErr().isEmpty()) {
            sbErrors = new StringBuilder("Errors:" + errors.getErr().size());
            for (Err err : errors.getErr()) {
                sbErrors.append("\ncode:").append(err.getCode()).append(", msg:").append(err.getMsg());
            }
        }
        if (events != null && !events.getEvt().isEmpty()) {
            sbEvents = new StringBuilder("Events:" + events.getEvt().size());
            for (Evt evt : events.getEvt()) {
                sbEvents.append("\ncode:").append(evt.getCode()).append(", msg:").append(evt.getMsg());
            }
        }
        eventos = sbEvents != null ? sbEvents.toString() : null;
        if (sbErrors != null) {
            if (sbEvents != null) {
                sbErrors.append("----------------\n").append(sbEvents.toString());
            }
            throw new WSAFIPErrorResponseException(sbErrors.toString());
        }

    }

    public List<CbteTipo> getComprobantesTipoList() throws WSAFIPErrorResponseException {
        CbteTipoResponse o = aFIPClient.getComprobantesTipoList();
        checkErrorsAndEvents(o.getErrors(), o.getEvents());
        return o.getResultGet().getCbteTipo();
    }

    public List<ConceptoTipo> getConceptoTipoList() throws WSAFIPErrorResponseException {
        ConceptoTipoResponse o = aFIPClient.getConceptoTipoList();
        checkErrorsAndEvents(o.getErrors(), o.getEvents());
        return o.getResultGet().getConceptoTipo();
    }

    public List<DocTipo> getDocumentosTipoList() throws WSAFIPErrorResponseException {
        DocTipoResponse o = aFIPClient.getDocumentosTipoList();
        checkErrorsAndEvents(o.getErrors(), o.getEvents());
        return o.getResultGet().getDocTipo();
    }

    public List<IvaTipo> getIVATipoList() throws WSAFIPErrorResponseException {
        IvaTipoResponse o = aFIPClient.getIVATipoList();
        checkErrorsAndEvents(o.getErrors(), o.getEvents());
        return o.getResultGet().getIvaTipo();
    }

    String getEventos() {
        return eventos;
    }

    /**
     * Convert a Date to a formatted XML Date String (yyyyMMdd)
     *
     * @param fecha To be converted
     * @return A XML Date String formatted. If <tt>fecha</tt> == null, returns
     * null.
     */
    private String getXMLDate(Date fecha) {
        if (fecha == null) {
            return null;
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(fecha);
        int year = gc.get(Calendar.YEAR) * 10000;
        int month = (gc.get(Calendar.MONTH) + 1) * 100;
        int day = (gc.get(Calendar.DAY_OF_MONTH));
        return String.valueOf(year + month + day);
    }

    public JDialog showSetting(final FacturaVenta fv) throws WSAFIPErrorResponseException, MessageException {
        if (panelWSFE == null) {
            panelWSFE = new WSFEVetificacionPanel();
            //implement del botón para obtener cotización de moneda según AFIP
            panelWSFE.getBtnGetCotizAFIP().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Cotizacion cotizacion = aFIPClient.getCotizacion(((Moneda) panelWSFE.getCbMonendas().getSelectedItem()).getId());
                        panelWSFE.getTfCotizacion().setText(String.valueOf(cotizacion.getMonCotiz()));
                    } catch (WSAFIPErrorResponseException ex) {
                        JOptionPane.showMessageDialog(panelWSFE, ex.getMessage(), "Error obteniendo cotización", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(this.getClass()).error(ex);
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
//        UTIL.loadComboBox(panelWSFE.getCbTributos(), tributoTipoList, false);
        CbteTipo ct = null;
        if (String.valueOf(fv.getTipo()).equalsIgnoreCase("a")) {
            ct = new CbteTipo(1);
        } else if (String.valueOf(fv.getTipo()).equalsIgnoreCase("b")) {
            ct = new CbteTipo(6);
        } else if (String.valueOf(fv.getTipo()).equalsIgnoreCase("c")) {
            ct = new CbteTipo(11);
        }
        UTIL.setSelectedItem(panelWSFE.getCbComprobantes(), ct);
        panelWSFE.getTfTotalNeto().setText(UTIL.PRECIO_CON_PUNTO.format(fv.getGravado()));
        panelWSFE.getTfTotalIVAs().setText(UTIL.PRECIO_CON_PUNTO.format(fv.getImporte() - fv.getGravado()));
        panelWSFE.getTfTotalTributos().setText(UTIL.PRECIO_CON_PUNTO.format(0));
        panelWSFE.getTfTotal().setText(UTIL.PRECIO_CON_PUNTO.format(fv.getImporte()));
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
                Date fechaServDesde = panelWSFE.getDcServDesde();
                Date fechaServHasta = panelWSFE.getDcServHasta();
                List<Tributo> tributoList = null;
                try {
                    invokeFE(fv, conceptoTipo, cbteTipo, docTipo, moneda, cotizacion, fechaServDesde, fechaServHasta, tributoList);
                } catch (WSAFIPErrorResponseException ex) {
                    JOptionPane.showMessageDialog(jDABM1, ex.getMessage(), "Web Service AFIP Error", JOptionPane.ERROR_MESSAGE);
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(jDABM1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(jDABM1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(this.getClass()).error(ex);
                }
            }
        });
        jDABM1.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jDABM1.dispose();
            }
        });

        return jDABM1;
    }
}