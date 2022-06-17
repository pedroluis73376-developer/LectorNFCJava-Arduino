/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arras3888;

import comunicacionserial.ArduinoExcepcion;
import comunicacionserial.ComunicacionSerial_Arduino;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.management.StringValueExp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultCaret;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author pedro
 */
public class Main extends javax.swing.JFrame {

    // variables para saber la posicion de nuestro mouse
    int xMouse, yMouse;

    //variables para nuestras consultas SQL
    Statement metal = null;
    ResultSet metalResult = null;
    ArrayList metales = new ArrayList();

    Statement frente = null;
    ResultSet frenteResult = null;
    ArrayList frentes = new ArrayList();

    Statement vuelta = null;
    ResultSet vueltaResult = null;
    ArrayList vueltas = new ArrayList();

    Statement diametro = null;
    ResultSet diametroResult = null;
    ArrayList diametros = new ArrayList();

    Statement arras = null;
    ResultSet arrasResult = null;

    //variables para las tajetas nfc
    String[] frenteDatos;
    String[] vueltaDatos;
    String mensaje = "";

// Instancias la clase que hemos creado anteriormente
    private ConexionMySQL SQL = new ConexionMySQL();
// Llamas al método que tiene la clase y te devuelve una conexión
    private Connection conn = SQL.conectarMySQL();
    //declaramos nuestra instancia para comunicacion con el arduino
    ComunicacionSerial_Arduino conexion = new ComunicacionSerial_Arduino();
    SerialPortEventListener listen = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                //consola.append(String.valueOf(conexion.printMessage() + " "));
                if (conexion.isMessageAvailable()) {
                    mensaje = mensaje + conexion.printMessage();
                }

                if (mensaje.contains(".")) {
                    if (mensaje.contains("Frente")) {

                        frenteDatos = mensaje.split("/");
                        Frente.setForeground(Color.BLACK);
                        Frente.setText(frenteDatos[2].replace(".", ""));
                        consola.append("\n Lectura de datos exitosa");
                    } else if (mensaje.contains("Vuelta")) {

                        vueltaDatos = mensaje.split("/");
                        Vuelta.setForeground(Color.BLACK);
                        Vuelta.setText(vueltaDatos[2].replace(".", ""));
                        consola.append("\n Lectura de datos exitosa");
                    } else {
                        if (mensaje.equals("Proceso Exitoso tarjeta formateada como NDEF.")) {
                            TimeUnit.SECONDS.sleep(1);
                            progreso.setValue(100);
                            JOptionPane.showMessageDialog(null, "Proceso Exitoso tarjeta formateada como NDEF");
                            formatoTxt2.setText("Proceso Completado");
                            TimeUnit.SECONDS.sleep(1);
                            progreso.setValue(0);
                        }
                        if (mensaje.equals("Proceso Realizado con exito la Etiqueta esta restaurada.")) {
                            TimeUnit.SECONDS.sleep(1);
                            progreso.setValue(100);
                            JOptionPane.showMessageDialog(null, "Proceso Realizado con exito la Etiqueta esta restaurada.");
                           limpiarTxt2.setText("Proceso Completado");
                            TimeUnit.SECONDS.sleep(1);
                            progreso.setValue(0);
                        }
                        if (mensaje.contains("Formato sin exito intente de nuevo.")) {
                            progreso.setValue(0);
                            formatoTxt2.setText("Ocurrio un error en el formato!!");
                            JOptionPane.showMessageDialog(null, "Intente primero Reiniciar la tarjeta ");
                        }
                        if (mensaje.equals("Proceso Exitoso Trata de Leer los Datos con un Dispositvo Movil.")) {
                            JOptionPane.showMessageDialog(null, "Tarjeta Grabada con Exito");
                        }
                        consola.append("\n");
                        consola.append(" " + mensaje);
                    }
                    mensaje = "";
                }

            } catch (SerialPortException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArduinoExcepcion ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    public Main() {
        initComponents();
        //iniciamos nuestro consola para el registro
        consola.append("Arras 3888:\n");
        viewEscaner();
        cargarDatos();

        try {
            conexion.arduinoRXTX("COM3", 9600, listen);

        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            consola.append("Fallo en la conexion con el modulo arduino");
            consola.append("\nReconectando Arduino.....");
            recargarArduino();

        }

    }

    //metodo para llamar la vista escabner
    public void viewEscaner() {
        dashboarTxt.setText("Escaner");
        escanner.setBackground(new Color(0, 255, 255));
        escaner.setVisible(true);
        Formato.setVisible(false);
        grabarEtiqueta.setVisible(false);
        Configuracion.setVisible(false);
    }
//metodo para llamar la vista grabar etiqueta

    public void viewGrabarEtiqueta() {
        dashboarTxt.setText("Grabar Etiqueta");
        grabar.setBackground(new Color(0, 255, 255));
        escaner.setVisible(false);
        Formato.setVisible(false);
        grabarEtiqueta.setVisible(true);
        Configuracion.setVisible(false);
    }

    //metodo para llamar la vista formato
    public void viewFormato() {
        dashboarTxt.setText("Formato");
        formato.setBackground(new Color(0, 255, 255));
        escaner.setVisible(false);
        Formato.setVisible(true);
        grabarEtiqueta.setVisible(false);
        Configuracion.setVisible(false);
        progreso.setValue(0);
       
    }
    public void viewConfiguracion(){
        dashboarTxt.setText("Configuracion");
        configuracion.setBackground(new Color(0, 255, 255));
        escaner.setVisible(false);
        Formato.setVisible(false);
        grabarEtiqueta.setVisible(false);
        Configuracion.setVisible(true);
    }

    //metodo para animaciones de cambio de vista
    public void menuBackground() {
        escanner.setBackground(new Color(81, 226, 245));
        grabar.setBackground(new Color(81, 226, 245));
        formato.setBackground(new Color(81, 226, 245));
        configuracion.setBackground(new Color(81, 226, 245));

    }

    //metodo para enviar datos al arduino e iniciar el metodo de escaner
    public void Escanear() throws SerialPortException {
        try {
            conexion.sendData("4");
        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            consola.append("\nNo es posible establecer conexion con el modulo Arduino");
        }
    }

    //metodo para enviar datos e iniciar el grabado en la etiqueta
    public void grabarEtiqueta(String datos) throws SerialPortException {
        try {
            conexion.sendData(datos);
        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            consola.append("\nNo es posible establecer conexion con el modulo Arduino");
        }
    }

    //metodo en el cargamos los datos y los arreglos en memoria 
    public void cargarDatos() {
        //cargaremos los datos en el sistema realizando varias consultas
        consola.append(String.valueOf(" " + conn));
        consola.append(String.valueOf("\n Cargando Datos......."));
        Metales();
        Frentes();
        Vueltas();
        Diametros();

    }

    //metodo para reestablecer la conexion con el arduino
    public void recargarArduino() {
        try {
            conexion.arduinoRXTX("COM3", 9600, listen);

        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            consola.append("Fallo en la Reconexion con el modulo arduino");
            consola.append("\nVerifique que el modulo Arduino este conectado correctamente.....");

        }

    }

    //metodo para generar la consulta a la base de datos y llenar el arreglo de nuestros metales
    public void Metales() {
        //cargamos los datos de nuestros metales
        try {
            metal = conn.createStatement();
            metalResult = metal.executeQuery("SELECT * FROM metals");

            if (metal.execute("SELECT * FROM metals")) {
                metalResult = metal.getResultSet();
                consola.append(String.valueOf("\n Cargando Lista de Metales......."));
                while (metalResult.next()) {
                    Metal.addItem(String.valueOf(metalResult.getObject("nombre_metal")));
                    metales.add(String.valueOf(metalResult.getObject("nombre_metal")));
                }
                consola.append(String.valueOf("\n Lista de Metales Cargada ......."));

                metal.close();
                metalResult.close();
            }
        } catch (SQLException ex) {
            consola.append("\nSQLException: " + ex.getMessage());
            consola.append("\nSQLState: " + ex.getSQLState());
            consola.append("\nVendorError: " + ex.getErrorCode());

        }
    }
//metodo para generar la consulta a la base de datos y llenar el arreglo de nuestros frentes

    public void Frentes() {
        //cargamos los datos de nuestros frentes
        try {
            frente = conn.createStatement();
            frenteResult = frente.executeQuery("SELECT * FROM frentes");

            if (frente.execute("SELECT * FROM frentes")) {
                frenteResult = frente.getResultSet();
                consola.append(String.valueOf("\n Cargando Lista de Frentes......."));
                while (frenteResult.next()) {
                    listaFrente.addItem(String.valueOf(frenteResult.getObject("nombre_frente")));
                    frentes.add(String.valueOf(frenteResult.getObject("nombre_frente")));
                }
                consola.append(String.valueOf("\n Lista de Frentes Cargada ......."));

                frente.close();
                frenteResult.close();
            }
        } catch (SQLException ex) {
            consola.append("\nSQLException: " + ex.getMessage());
            consola.append("\nSQLState: " + ex.getSQLState());
            consola.append("\nVendorError: " + ex.getErrorCode());

        }
    }
//metodo para generar la consulta a la base de datos y llenar el arreglo de nuestras vueltas

    public void Vueltas() {
        //cargamos los datos de nuestros frentes
        try {
            vuelta = conn.createStatement();
            vueltaResult = vuelta.executeQuery("SELECT * FROM vueltas");

            if (vuelta.execute("SELECT * FROM vueltas")) {
                vueltaResult = vuelta.getResultSet();
                consola.append(String.valueOf("\n Cargando Lista de Vueltas......."));
                while (vueltaResult.next()) {
                    listaVuelta.addItem(String.valueOf(vueltaResult.getObject("nombre_vuelta")));
                    vueltas.add(String.valueOf(vueltaResult.getObject("nombre_vuelta")));
                }
                consola.append(String.valueOf("\n Lista de Vueltas Cargada ......."));

                vuelta.close();
                vueltaResult.close();
            }
        } catch (SQLException ex) {
            consola.append("\nSQLException: " + ex.getMessage());
            consola.append("\nSQLState: " + ex.getSQLState());
            consola.append("\nVendorError: " + ex.getErrorCode());

        }

    }
//metodo para generar la consulta a la base de datos y llenar el arreglo de nuestros diametros

    public void Diametros() {
        //cargamos los datos de nuestros frentes
        try {
            diametro = conn.createStatement();
            diametroResult = diametro.executeQuery("SELECT * FROM sizes");

            if (diametro.execute("SELECT * FROM sizes")) {
                diametroResult = diametro.getResultSet();
                consola.append(String.valueOf("\n Cargando Lista de Tamaños......."));
                while (diametroResult.next()) {
                    diametros.add(String.valueOf(diametroResult.getObject("diametro")));
                }
                consola.append(String.valueOf("\n Lista de Tamaños Cargada ......."));

                diametro.close();
                diametroResult.close();
            }
        } catch (SQLException ex) {
            consola.append("\nSQLException: " + ex.getMessage());
            consola.append("\nSQLState: " + ex.getSQLState());
            consola.append("\nVendorError: " + ex.getErrorCode());

        }
        System.out.println(diametros);
    }

    //metodo para reiniciar el modelo de nuestra tabla
    public void modeloTabla() {
        TableColumn columna;
        columna = modelo.getColumnModel().getColumn(1);
        columna.setPreferredWidth(20);
        columna = modelo.getColumnModel().getColumn(2);
        columna.setPreferredWidth(20);
        columna = modelo.getColumnModel().getColumn(3);
        columna.setPreferredWidth(30);
        columna = modelo.getColumnModel().getColumn(7);
        columna.setPreferredWidth(20);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new javax.swing.JPanel();
        btnExit = new javax.swing.JPanel();
        exitTxt = new javax.swing.JLabel();
        escanner = new javax.swing.JPanel();
        icon_escanner = new javax.swing.JLabel();
        escannerTxt = new javax.swing.JLabel();
        grabar = new javax.swing.JPanel();
        grabar_icon = new javax.swing.JLabel();
        grabarTxt = new javax.swing.JLabel();
        formato = new javax.swing.JPanel();
        formato_icon = new javax.swing.JLabel();
        formatoTxt = new javax.swing.JLabel();
        configuracion = new javax.swing.JPanel();
        configuracion_icon = new javax.swing.JLabel();
        configuracionTxt = new javax.swing.JLabel();
        dashboardIcon = new javax.swing.JLabel();
        dashboarTxt = new javax.swing.JLabel();
        iconConected = new javax.swing.JLabel();
        conectedTxt = new javax.swing.JLabel();
        userTxt = new javax.swing.JLabel();
        iconProfile = new javax.swing.JLabel();
        bgProfile = new javax.swing.JLabel();
        banner = new javax.swing.JLabel();
        slide = new javax.swing.JPanel();
        header = new javax.swing.JPanel();
        desktop = new javax.swing.JPanel();
        panelConsola = new javax.swing.JScrollPane();
        consola = new javax.swing.JTextArea();
        Configuracion = new javax.swing.JPanel();
        Formato = new javax.swing.JPanel();
        realizarFormatoTxt = new javax.swing.JLabel();
        formatearBtn = new javax.swing.JButton();
        fomatearTxt = new javax.swing.JLabel();
        limpiarTxt = new javax.swing.JLabel();
        limpiarBtn = new javax.swing.JButton();
        progreso = new javax.swing.JProgressBar();
        formatoTxt2 = new javax.swing.JLabel();
        limpiarTxt2 = new javax.swing.JLabel();
        grabarEtiqueta = new javax.swing.JPanel();
        tipo = new javax.swing.JComboBox<>();
        tipoTxt = new javax.swing.JLabel();
        listaFrente = new javax.swing.JComboBox<>();
        listaVuelta = new javax.swing.JComboBox<>();
        Limpiar = new javax.swing.JButton();
        tipoTxt1 = new javax.swing.JLabel();
        grabarBtn = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        escaner = new javax.swing.JPanel();
        Escanear = new javax.swing.JButton();
        Frente = new javax.swing.JTextField();
        Vuelta = new javax.swing.JTextField();
        Buscar = new javax.swing.JButton();
        Metal = new javax.swing.JComboBox<>();
        metalTxt = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        modelo = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(new java.awt.Dimension(1000, 610));

        background.setBackground(new java.awt.Color(235, 235, 235));
        background.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnExit.setBackground(new java.awt.Color(73, 255, 255));
        btnExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExitMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnExitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnExitMouseExited(evt);
            }
        });

        exitTxt.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        exitTxt.setForeground(new java.awt.Color(204, 204, 204));
        exitTxt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        exitTxt.setText("X");
        exitTxt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        javax.swing.GroupLayout btnExitLayout = new javax.swing.GroupLayout(btnExit);
        btnExit.setLayout(btnExitLayout);
        btnExitLayout.setHorizontalGroup(
            btnExitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exitTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        btnExitLayout.setVerticalGroup(
            btnExitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exitTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        background.add(btnExit, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 0, 40, 30));

        escanner.setBackground(new java.awt.Color(81, 226, 245));
        escanner.setForeground(new java.awt.Color(204, 204, 204));
        escanner.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                escannerMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                escannerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                escannerMouseExited(evt);
            }
        });
        escanner.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        icon_escanner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/barcode-scanner_1.png"))); // NOI18N
        escanner.add(icon_escanner, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 32, 32));

        escannerTxt.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        escannerTxt.setText("Escaner");
        escanner.add(escannerTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 150, 32));

        background.add(escanner, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 190, 210, 50));

        grabar.setBackground(new java.awt.Color(81, 226, 245));
        grabar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grabarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                grabarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                grabarMouseExited(evt);
            }
        });
        grabar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        grabar_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/floppy-disk.png"))); // NOI18N
        grabar.add(grabar_icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 32, 32));

        grabarTxt.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        grabarTxt.setText("Grabar Etiqueta");
        grabar.add(grabarTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 150, 32));

        background.add(grabar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 260, 210, 50));

        formato.setBackground(new java.awt.Color(81, 226, 245));
        formato.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formatoMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formatoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formatoMouseExited(evt);
            }
        });
        formato.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        formato_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hard-drive.png"))); // NOI18N
        formato.add(formato_icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 32, 32));

        formatoTxt.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        formatoTxt.setText("Formato");
        formato.add(formatoTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 150, 32));

        background.add(formato, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 330, 210, 50));

        configuracion.setBackground(new java.awt.Color(81, 226, 245));
        configuracion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                configuracionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                configuracionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                configuracionMouseExited(evt);
            }
        });
        configuracion.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        configuracion_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/configuraciones.png"))); // NOI18N
        configuracion.add(configuracion_icon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 32, 32));

        configuracionTxt.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        configuracionTxt.setText("Configuracion");
        configuracion.add(configuracionTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 150, 32));

        background.add(configuracion, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 400, 210, 50));

        dashboardIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/applications.png"))); // NOI18N
        background.add(dashboardIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 0, 30, 70));

        dashboarTxt.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        dashboarTxt.setText("Dashboard");
        background.add(dashboarTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 0, 160, 70));

        iconConected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com.images/circle_green.png"))); // NOI18N
        background.add(iconConected, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 140, 13, 20));

        conectedTxt.setFont(new java.awt.Font("Roboto Light", 1, 12)); // NOI18N
        conectedTxt.setForeground(new java.awt.Color(255, 255, 255));
        conectedTxt.setText("Conectado");
        background.add(conectedTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 140, 80, 20));

        userTxt.setFont(new java.awt.Font("Roboto Light", 1, 14)); // NOI18N
        userTxt.setForeground(new java.awt.Color(255, 255, 255));
        userTxt.setText("User Name");
        background.add(userTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, 80, 30));

        iconProfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com.images/profile2.png"))); // NOI18N
        iconProfile.setText("jLabel4");
        background.add(iconProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 150, 110));

        bgProfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com.images/profile-bg.jpg"))); // NOI18N
        bgProfile.setText("jLabel3");
        background.add(bgProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 210, 170));

        banner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com.images/header.jpg"))); // NOI18N
        banner.setText("jLabel2");
        background.add(banner, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 790, 70));

        slide.setBackground(new java.awt.Color(81, 226, 245));

        javax.swing.GroupLayout slideLayout = new javax.swing.GroupLayout(slide);
        slide.setLayout(slideLayout);
        slideLayout.setHorizontalGroup(
            slideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 210, Short.MAX_VALUE)
        );
        slideLayout.setVerticalGroup(
            slideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        background.add(slide, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 210, 600));

        header.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                headerMouseDragged(evt);
            }
        });
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                headerMousePressed(evt);
            }
        });

        javax.swing.GroupLayout headerLayout = new javax.swing.GroupLayout(header);
        header.setLayout(headerLayout);
        headerLayout.setHorizontalGroup(
            headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 960, Short.MAX_VALUE)
        );
        headerLayout.setVerticalGroup(
            headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        background.add(header, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 960, 30));

        desktop.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelConsola.setBackground(new java.awt.Color(255, 255, 255));
        panelConsola.setAutoscrolls(true);
        panelConsola.setFont(new java.awt.Font("Roboto Light", 2, 11)); // NOI18N

        consola.setEditable(false);
        consola.setColumns(20);
        consola.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        consola.setRows(5);
        consola.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        consola.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                consolaCaretUpdate(evt);
            }
        });
        panelConsola.setViewportView(consola);

        desktop.add(panelConsola, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 399, 790, 140));

        Configuracion.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout ConfiguracionLayout = new javax.swing.GroupLayout(Configuracion);
        Configuracion.setLayout(ConfiguracionLayout);
        ConfiguracionLayout.setHorizontalGroup(
            ConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 790, Short.MAX_VALUE)
        );
        ConfiguracionLayout.setVerticalGroup(
            ConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        desktop.add(Configuracion, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 790, 400));

        Formato.setBackground(new java.awt.Color(255, 255, 255));
        Formato.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        realizarFormatoTxt.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        realizarFormatoTxt.setText("Realizar Formato:");
        Formato.add(realizarFormatoTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, 140, 30));

        formatearBtn.setText("Iniciar");
        formatearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatearBtnActionPerformed(evt);
            }
        });
        Formato.add(formatearBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 160, 90, 30));

        fomatearTxt.setFont(new java.awt.Font("Roboto Light", 0, 11)); // NOI18N
        fomatearTxt.setText("Nota: Mantener la etiqueta NFC sobre el lector antes de comenzar cualquier proceso");
        Formato.add(fomatearTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 450, 20));

        limpiarTxt.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        limpiarTxt.setText("Reiniciar Tarjeta:");
        Formato.add(limpiarTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 140, 30));

        limpiarBtn.setText("Iniciar");
        limpiarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpiarBtnActionPerformed(evt);
            }
        });
        Formato.add(limpiarBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 90, 30));

        progreso.setBackground(new java.awt.Color(0, 195, 242));
        progreso.setForeground(new java.awt.Color(51, 51, 51));
        Formato.add(progreso, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 380, 350, 20));

        formatoTxt2.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Formato.add(formatoTxt2, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 160, 360, 30));

        limpiarTxt2.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Formato.add(limpiarTxt2, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 60, 360, 30));

        desktop.add(Formato, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 790, 400));

        grabarEtiqueta.setBackground(new java.awt.Color(255, 255, 255));
        grabarEtiqueta.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tipo.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        tipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-selecionar-", "Frente", "Vuelta" }));
        tipo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tipoItemStateChanged(evt);
            }
        });
        grabarEtiqueta.add(tipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 40, 240, 34));

        tipoTxt.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tipoTxt.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tipoTxt.setText("Tipo:");
        grabarEtiqueta.add(tipoTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(242, 40, 50, 34));

        listaFrente.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        listaFrente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-seleccionar-" }));
        listaFrente.setAutoscrolls(true);
        grabarEtiqueta.add(listaFrente, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 100, 240, 34));

        listaVuelta.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        listaVuelta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-selecciona-" }));
        grabarEtiqueta.add(listaVuelta, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 100, 238, 34));

        Limpiar.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Limpiar.setText("Limpiar Campos");
        Limpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LimpiarActionPerformed(evt);
            }
        });
        grabarEtiqueta.add(Limpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 190, 140, 34));

        tipoTxt1.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        tipoTxt1.setText("Nombre:");
        grabarEtiqueta.add(tipoTxt1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 100, 70, 34));

        grabarBtn.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        grabarBtn.setText("Grabar Etiqueta");
        grabarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grabarBtnActionPerformed(evt);
            }
        });
        grabarEtiqueta.add(grabarBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 190, 140, 34));

        jTextField1.setFont(new java.awt.Font("Roboto Light", 0, 11)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("Nota: La etiqueta NFC debe tener el siguiente formato  NDEF puede realizar el proceso de formato  en la seccion de formato que se encuentra en el menu");
        jTextField1.setBorder(null);
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        grabarEtiqueta.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 375, 790, 20));

        desktop.add(grabarEtiqueta, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 790, 400));

        escaner.setBackground(new java.awt.Color(255, 255, 255));
        escaner.setEnabled(false);
        escaner.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Escanear.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Escanear.setText("Escanear Etiquetas");
        Escanear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EscanearActionPerformed(evt);
            }
        });
        escaner.add(Escanear, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 80, -1, -1));

        Frente.setEditable(false);
        Frente.setBackground(new java.awt.Color(255, 255, 255));
        Frente.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Frente.setForeground(new java.awt.Color(204, 204, 204));
        Frente.setText("Frente");
        Frente.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        escaner.add(Frente, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 290, 47));

        Vuelta.setEditable(false);
        Vuelta.setBackground(new java.awt.Color(255, 255, 255));
        Vuelta.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Vuelta.setForeground(new java.awt.Color(204, 204, 204));
        Vuelta.setText("Vuelta");
        Vuelta.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        escaner.add(Vuelta, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 10, 290, 47));

        Buscar.setFont(new java.awt.Font("Roboto Light", 0, 12)); // NOI18N
        Buscar.setText("Buscar Precio Arra");
        Buscar.setPreferredSize(new java.awt.Dimension(135, 23));
        Buscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuscarActionPerformed(evt);
            }
        });
        escaner.add(Buscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 80, -1, -1));

        Metal.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        Metal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Seleccione--" }));
        escaner.add(Metal, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, 240, -1));

        metalTxt.setFont(new java.awt.Font("Roboto Light", 0, 14)); // NOI18N
        metalTxt.setText("Metal:");
        escaner.add(metalTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 80, 26));

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        modelo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        modelo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Clave", "Peso", "Grosor", "Precio", "Vuelta", "Frente", "Metal", "Diametro"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        modelo.setGridColor(new java.awt.Color(204, 204, 204));
        jScrollPane1.setViewportView(modelo);
        if (modelo.getColumnModel().getColumnCount() > 0) {
            modelo.getColumnModel().getColumn(0).setPreferredWidth(60);
            modelo.getColumnModel().getColumn(1).setPreferredWidth(15);
            modelo.getColumnModel().getColumn(2).setPreferredWidth(20);
            modelo.getColumnModel().getColumn(3).setPreferredWidth(30);
            modelo.getColumnModel().getColumn(4).setPreferredWidth(40);
            modelo.getColumnModel().getColumn(5).setPreferredWidth(40);
            modelo.getColumnModel().getColumn(6).setPreferredWidth(25);
            modelo.getColumnModel().getColumn(7).setPreferredWidth(20);
        }

        escaner.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 770, 190));

        desktop.add(escaner, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 790, 400));

        background.add(desktop, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 70, 790, 540));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void headerMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headerMousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_headerMousePressed

    private void headerMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_headerMouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation((x - xMouse), (y - yMouse));
    }//GEN-LAST:event_headerMouseDragged

    private void btnExitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExitMouseEntered
        btnExit.setBackground(Color.red);
        exitTxt.setForeground(Color.white);
    }//GEN-LAST:event_btnExitMouseEntered

    private void btnExitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExitMouseExited
        btnExit.setBackground(new Color(75, 255, 255));
        exitTxt.setForeground(new Color(204, 204, 204));
    }//GEN-LAST:event_btnExitMouseExited

    private void btnExitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExitMouseClicked
        int result = JOptionPane.showConfirmDialog(null,
                "Desea Salir del Programa",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION)
            System.exit(0);
    }//GEN-LAST:event_btnExitMouseClicked

    private void EscanearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EscanearActionPerformed
        try {
            Escanear();
        } catch (SerialPortException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_EscanearActionPerformed

    private void BuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuscarActionPerformed
        //obtenemos los datos capturados

        int metal = Metal.getSelectedIndex();

        //realizamos nuestra consulta a la base de datos
        try {
            String query;
            if (metal == 0) {
                query = "SELECT * FROM arras WHERE frentes_id =" + frenteDatos[1] + " AND  vueltas_id=" + vueltaDatos[1];
            } else {
                query = "SELECT * FROM arras WHERE frentes_id =" + frenteDatos[1] + " AND  vueltas_id=" + vueltaDatos[1] + " AND metales_id=" + metal;

            }
            System.out.println(query);
            arras = conn.createStatement();
            arrasResult = arras.executeQuery(query);

            if (arras.execute(query)) {
                arrasResult = arras.getResultSet();
                consola.append(String.valueOf("\n Registros encontrados......."));
                //llenamos nuestra tabla con los datos obtenidos
                DefaultTableModel model = new DefaultTableModel();
                modelo.setModel(model);

                model.addColumn("Clave");
                model.addColumn("Peso");
                model.addColumn("Grosor");
                model.addColumn("Precio");
                model.addColumn("Vuelta");
                model.addColumn("Frente");
                model.addColumn("Metal");
                model.addColumn("Diametro");
                modeloTabla();
                Object[] fila = new Object[8];
                while (arrasResult.next()) {

                    fila[0] = String.valueOf(arrasResult.getObject("clave"));
                    fila[1] = String.valueOf(arrasResult.getObject("peso"));
                    fila[2] = String.valueOf(arrasResult.getObject("grosor"));
                    fila[3] = String.valueOf(arrasResult.getObject("precio"));
                    fila[4] = Vuelta.getText();
                    fila[5] = Frente.getText();
                    fila[6] = metales.get(Integer.parseInt(String.valueOf(arrasResult.getObject("metales_id"))) - 1);
                    fila[7] = diametros.get(Integer.parseInt(String.valueOf(arrasResult.getObject("size_id"))) - 1);
                    model.addRow(fila);
                }
                consola.append(String.valueOf("\n Mostrando Registros ......."));
                arras.close();
                arrasResult.close();
            }
        } catch (SQLException ex) {
            consola.append("\nSQLException: " + ex.getMessage());
            consola.append("\nSQLState: " + ex.getSQLState());
            consola.append("\nVendorError: " + ex.getErrorCode());

        }


    }//GEN-LAST:event_BuscarActionPerformed

    private void consolaCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_consolaCaretUpdate
        DefaultCaret caret = (DefaultCaret) consola.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }//GEN-LAST:event_consolaCaretUpdate

    private void escannerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_escannerMouseClicked
        // damos estilos al boton de escaner clikeado
        menuBackground();
        viewEscaner();

    }//GEN-LAST:event_escannerMouseClicked

    private void grabarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grabarMouseClicked
        menuBackground();
        viewGrabarEtiqueta();
    }//GEN-LAST:event_grabarMouseClicked

    private void escannerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_escannerMouseEntered
        // TODO add your handling code here:
        escanner.setBackground(new Color(0, 255, 255));

    }//GEN-LAST:event_escannerMouseEntered

    private void escannerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_escannerMouseExited
        if (!dashboarTxt.getText().equals(escannerTxt.getText())) {
            escanner.setBackground(new Color(81, 226, 245));
        }
    }//GEN-LAST:event_escannerMouseExited

    private void grabarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grabarMouseEntered
        // TODO add your handling code here:
        grabar.setBackground(new Color(0, 255, 255));
    }//GEN-LAST:event_grabarMouseEntered

    private void grabarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grabarMouseExited
        // TODO add your handling code here:
        if (!grabarTxt.getText().equals(dashboarTxt.getText())) {
            grabar.setBackground(new Color(81, 226, 245));
        }
    }//GEN-LAST:event_grabarMouseExited

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void tipoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tipoItemStateChanged
        String dato = String.valueOf(tipo.getSelectedItem());
        if (dato.equals("Frente")) {
            listaFrente.setVisible(true);
            listaVuelta.setVisible(false);
        } else {
            listaFrente.setVisible(false);
            listaVuelta.setVisible(true);
        }
    }//GEN-LAST:event_tipoItemStateChanged

    private void grabarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grabarBtnActionPerformed
        String datos = "1-";
        datos = datos + tipo.getSelectedItem().toString() + "/";

        if (datos.contains("Frente")) {
            datos = datos + listaFrente.getSelectedIndex() + "/" + listaFrente.getSelectedItem().toString();
            try {
                grabarEtiqueta(datos);
            } catch (SerialPortException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (datos.contains("Vuelta")) {
            datos = datos + listaVuelta.getSelectedIndex() + "/" + listaVuelta.getSelectedItem().toString();
            try {
                grabarEtiqueta(datos);
            } catch (SerialPortException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Asegurate de llenar todos los campos");
        }

    }//GEN-LAST:event_grabarBtnActionPerformed

    private void formatoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formatoMouseClicked
        // TODO add your handling code here:
        menuBackground();
        viewFormato();
        
    }//GEN-LAST:event_formatoMouseClicked

    private void formatoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formatoMouseEntered
        // TODO add your handling code here:
        formato.setBackground(new Color(0, 255, 255));
    }//GEN-LAST:event_formatoMouseEntered

    private void formatoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formatoMouseExited
        // TODO add your handling code here:
        if (!formatoTxt.getText().equals(dashboarTxt.getText())) {
            formato.setBackground(new Color(81, 226, 245));
        }
    }//GEN-LAST:event_formatoMouseExited

    private void LimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LimpiarActionPerformed
        tipo.setSelectedIndex(0);
        listaFrente.setSelectedIndex(0);
        listaVuelta.setSelectedIndex(0);
        
    }//GEN-LAST:event_LimpiarActionPerformed

    private void formatearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatearBtnActionPerformed
        // TODO add your handling code here:
        progreso.setValue(20);
        formatoTxt2.setText("realizando proceso...");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conexion.sendData("2");
            progreso.setValue(40);
        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            consola.append("\nNo es posible establecer conexion con el modulo Arduino");
        } catch (SerialPortException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }//GEN-LAST:event_formatearBtnActionPerformed

    private void limpiarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiarBtnActionPerformed
        // TODO add your handling code here:
        progreso.setValue(20);
        limpiarTxt2.setText("realizando proceso...");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            conexion.sendData("3");
            progreso.setValue(40);
        } catch (ArduinoExcepcion ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            consola.append("\nNo es posible establecer conexion con el modulo Arduino");
        } catch (SerialPortException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_limpiarBtnActionPerformed

    private void configuracionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configuracionMouseClicked
        menuBackground();
        viewConfiguracion();
    }//GEN-LAST:event_configuracionMouseClicked

    private void configuracionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configuracionMouseEntered
        // TODO add your handling code here:
        configuracion.setBackground(new Color(0, 255, 255));
    }//GEN-LAST:event_configuracionMouseEntered

    private void configuracionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configuracionMouseExited
        // TODO add your handling code here:
          if (!configuracionTxt.getText().equals(dashboarTxt.getText())) {
            configuracion.setBackground(new Color(81, 226, 245));
        }
    }//GEN-LAST:event_configuracionMouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {

            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Buscar;
    private javax.swing.JPanel Configuracion;
    private javax.swing.JButton Escanear;
    private javax.swing.JPanel Formato;
    private javax.swing.JTextField Frente;
    private javax.swing.JButton Limpiar;
    private javax.swing.JComboBox<String> Metal;
    private javax.swing.JTextField Vuelta;
    private javax.swing.JPanel background;
    private javax.swing.JLabel banner;
    private javax.swing.JLabel bgProfile;
    private javax.swing.JPanel btnExit;
    private javax.swing.JLabel conectedTxt;
    private javax.swing.JPanel configuracion;
    private javax.swing.JLabel configuracionTxt;
    private javax.swing.JLabel configuracion_icon;
    public static javax.swing.JTextArea consola;
    private javax.swing.JLabel dashboarTxt;
    private javax.swing.JLabel dashboardIcon;
    private javax.swing.JPanel desktop;
    private javax.swing.JPanel escaner;
    private javax.swing.JPanel escanner;
    private javax.swing.JLabel escannerTxt;
    private javax.swing.JLabel exitTxt;
    private javax.swing.JLabel fomatearTxt;
    private javax.swing.JButton formatearBtn;
    private javax.swing.JPanel formato;
    private javax.swing.JLabel formatoTxt;
    private javax.swing.JLabel formatoTxt2;
    private javax.swing.JLabel formato_icon;
    private javax.swing.JPanel grabar;
    private javax.swing.JButton grabarBtn;
    private javax.swing.JPanel grabarEtiqueta;
    private javax.swing.JLabel grabarTxt;
    private javax.swing.JLabel grabar_icon;
    private javax.swing.JPanel header;
    private javax.swing.JLabel iconConected;
    private javax.swing.JLabel iconProfile;
    private javax.swing.JLabel icon_escanner;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton limpiarBtn;
    private javax.swing.JLabel limpiarTxt;
    private javax.swing.JLabel limpiarTxt2;
    private javax.swing.JComboBox<String> listaFrente;
    private javax.swing.JComboBox<String> listaVuelta;
    private javax.swing.JLabel metalTxt;
    private javax.swing.JTable modelo;
    private javax.swing.JScrollPane panelConsola;
    private javax.swing.JProgressBar progreso;
    private javax.swing.JLabel realizarFormatoTxt;
    private javax.swing.JPanel slide;
    private javax.swing.JComboBox<String> tipo;
    private javax.swing.JLabel tipoTxt;
    private javax.swing.JLabel tipoTxt1;
    public javax.swing.JLabel userTxt;
    // End of variables declaration//GEN-END:variables
}
