/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redesii.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.text.MaskFormatter;
import redesii.bean.MensagemBean;
import redesii.bean.MensagemBean.Acao;
import redesii.service.ClienteSocket;

/**
 *
 * @author Plinio
 */
public class ClienteGUI extends javax.swing.JFrame {

    private Socket socket;
    private ClienteSocket cSocket;
    private MensagemBean msgBean;
    int aba;
    private HashMap componentMap;
    private static final Pattern mascara = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");;
    private final Color azulBt = new Color (99,137,160);
    //private final Color desabilitado = new Color (240,240,240);
    
    
    /**
     * Creates new form ClienteGUI
     */
    public ClienteGUI() {
        initComponents();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        try {
            MaskFormatter mascara = new MaskFormatter("###.###.###.###");
            //mascara.setPlaceholderCharacter('0');
            //192.168.25.2
            final JFormattedTextField formattedTf = new JFormattedTextField(mascara);
            formattedTf.setInputVerifier(new IPTextFieldVerifier());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //UIManager.put("Combobox.background", new ColorUIResource(azulBt));
        //mapearComponentes();
        //System.out.println(jTabbedPane1.getTabCount());
    }
    
    
    private class AnalisaSocket implements Runnable{
        
        private ObjectInputStream entrada;

        public AnalisaSocket(Socket socket) {
            try {
                entrada = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(ClienteGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
                
        @Override
        public void run() {
            MensagemBean msgBean = null;
            try {
                while ((msgBean = (MensagemBean) entrada.readObject()) != null){
                    Acao acao = msgBean.getAcao();
                    
                    switch (acao) {
                        case CONECTAR:
                            conectado(msgBean);
                            break;
                        case DESCONECTAR:
                            desconectado();
                            socket.close();
                            break;
                        case ENVIAR_PARA_UM:
                            receber(msgBean);
                            break;
                        case USUARIOS_ONLINE:
                            atualizarUsuariosOnline(msgBean);
                            break;
                        //case MUDAR_STATUS:
                        //    atualizarStatusUsuariosOnline(msgBean);
                        //    break;
                        default:
                            break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ClienteGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClienteGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private void conectado(MensagemBean msgBean){
        if(msgBean.getTexto().equals("impossível conexão")){
            this.tfNome.setText("");
            JOptionPane.showMessageDialog(this, "Impossível se conectar! \nTente novamente com outro nome.");
            return;
        }
        
        this.msgBean = msgBean;
        btConectar.setEnabled(false);
        tfNome.setEditable(false);
        btSair.setEnabled(true);
        btSair.setBackground(azulBt);
        taEnviada.setEnabled(true);
        taRecebidas.setEnabled(true);
        btEnviar.setEnabled(true);
        btEnviar.setBackground(azulBt);
        btLimpar.setEnabled(true);
        btLimpar.setBackground(azulBt);
        ipServidor.setEditable(false);
        porta.setEditable(false);
        status.setEnabled(true);
        novoChat.setEnabled(true);
        listaOnlines.setToolTipText("Selecione o usuário para msg privada");
        /*
        if (getComponentByName("jPanel5")== null){
            novoChat.setVisible(false);
        }
        */
        /*
        if (!jTabbedPane1.getTabComponentAt(0).getName().equals("Chat Principal")){
            novoChat.setVisible(false);
        }
        */
        /*
        aba = jTabbedPane1.getTabCount();
        for (int i=0; i<aba; i++){
            System.out.println(jTabbedPane1.getTitleAt(i));
        }
        */
        /*
        if(jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex()).equals("Chat Principal")){
            System.out.println(jTabbedPane1.getSelectedIndex());
        }
        */
        novoChat.setEnabled(true);
        
        JOptionPane.showMessageDialog(this, "Conexão realizada com sucesso!");
    }
    
    private void desconectado(){
        
        btConectar.setEnabled(true);
        tfNome.setEditable(true);
        btSair.setEnabled(false);
        taEnviada.setEnabled(false);
        taRecebidas.setEnabled(false);
        btEnviar.setEnabled(false);
        btLimpar.setEnabled(false);
        ipServidor.setEditable(true);
        porta.setEditable(true);
        status.setEnabled(false);
        novoChat.setEnabled(false);
        taRecebidas.setText("");
        taEnviada.setText("");
        listaOnlines.setModel(new DefaultListModel());
        listaOnlines.setToolTipText(null);
        
        JOptionPane.showMessageDialog(this, "Você saiu do chat!");
        
        //jTabbedPane1.remove(jTabbedPane1.getSelectedIndex());
    }
    
    private void receber(MensagemBean msgBean){
        if(msgBean.getNomePrivado().equals("todos")){
            taRecebidas.append(msgBean.getNome()+ ": " + msgBean.getTexto() +"\n");
        } else {
            taRecebidas.append("(Msg Privada) " + msgBean.getNome()+ ": " + msgBean.getTexto() +"\n");
        }
        
    }
    
    private void atualizarUsuariosOnline(MensagemBean msgBean){
        Set<String> nomes = msgBean.getUsuariosOnline();
        nomes.remove((String) msgBean.getNome());
        String[] arrayNomes = (String[]) nomes.toArray(new String[nomes.size()]);
        
        listaOnlines.setListData(arrayNomes);
        listaOnlines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaOnlines.setLayoutOrientation(JList.VERTICAL);
    }
    
    private void atualizarStatusUsuariosOnline(MensagemBean msgBean){
        listaOnlines.setCellRenderer(new CorStatus());
        atualizarUsuariosOnline(msgBean);
    }
    
    private boolean validaIP(String ip){
        return mascara.matcher(ip).matches();
    }
    
    private void adicionarAba(){
        jTabbedPane1.addTab("Chat", new ClienteGUI().jPanel5);
        //jTabbedPane1.add(new JLabel("Chat"));
    }
    
    private void mapearComponentes(){
        try{
            Component[] components = getContentPane().getComponents();
            System.out.println(this);
            for (Component component : components) {
            componentMap.put(component.getName(), component);
            System.out.println(component.getName());
            } 
        } catch (NullPointerException e){
            
        }
        
    }
    
    public Component getComponentByName(String name) {
        if (componentMap.containsKey(name)) {
                return (Component) componentMap.get(name);
        }
        else return null;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        tfNome = new javax.swing.JTextField();
        btConectar = new javax.swing.JButton();
        btSair = new javax.swing.JButton();
        porta = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        ipServidor = new javax.swing.JTextField();
        novoChat = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaOnlines = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taRecebidas = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        btEnviar = new javax.swing.JButton();
        btLimpar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        taEnviada = new javax.swing.JTextArea();
        status = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Client");
        setBackground(new java.awt.Color(52, 72, 84));

        jTabbedPane1.setBackground(new java.awt.Color(52, 72, 84));

        jPanel1.setBackground(new java.awt.Color(52, 72, 84));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13), new java.awt.Color(69, 163, 218))); // NOI18N

        tfNome.setToolTipText(null);
        tfNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfNomeActionPerformed(evt);
            }
        });

        btConectar.setBackground(new java.awt.Color(99, 137, 160));
        btConectar.setText("Conectar");
        btConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConectarActionPerformed(evt);
            }
        });

        btSair.setText("Sair");
        btSair.setEnabled(false);
        btSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSairActionPerformed(evt);
            }
        });

        porta.setToolTipText(null);
        porta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portaActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(130, 181, 211));
        jLabel1.setText("Username:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(130, 181, 211));
        jLabel2.setText("Endereço IP do Servidor:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(130, 181, 211));
        jLabel3.setText("Porta:");

        ipServidor.setToolTipText(null);

        novoChat.setBackground(new java.awt.Color(99, 137, 160));
        novoChat.setText("+");
        novoChat.setToolTipText("Entrar em novo chat");
        novoChat.setBorderPainted(false);
        novoChat.setEnabled(false);
        novoChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                novoChatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(novoChat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(ipServidor, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(porta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tfNome))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btSair, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btConectar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 13, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNome, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btSair, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(novoChat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(porta, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btConectar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(ipServidor, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        jPanel2.setBackground(new java.awt.Color(52, 72, 84));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Online", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13), new java.awt.Color(69, 163, 218))); // NOI18N

        listaOnlines.setToolTipText(null);
        jScrollPane3.setViewportView(listaOnlines);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(52, 72, 84));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Chat", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13), new java.awt.Color(69, 163, 218))); // NOI18N

        taRecebidas.setEditable(false);
        taRecebidas.setColumns(20);
        taRecebidas.setRows(5);
        taRecebidas.setEnabled(false);
        jScrollPane1.setViewportView(taRecebidas);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(52, 72, 84));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mensagem", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13), new java.awt.Color(69, 163, 218))); // NOI18N

        btEnviar.setText("Enviar");
        btEnviar.setEnabled(false);
        btEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEnviarActionPerformed(evt);
            }
        });

        btLimpar.setText("Limpar");
        btLimpar.setEnabled(false);
        btLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLimparActionPerformed(evt);
            }
        });

        taEnviada.setColumns(20);
        taEnviada.setRows(5);
        taEnviada.setEnabled(false);
        jScrollPane2.setViewportView(taEnviada);

        status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disponível", "Ocupado", "Ausente" }));
        status.setToolTipText(null);
        status.setEnabled(false);
        status.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                statusItemStateChanged(evt);
            }
        });
        status.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(btEnviar, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(status, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                        .addComponent(btEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 849, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 755, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        jTabbedPane1.addTab("Chat Principal", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Chat1");

        pack();
    }// </editor-fold>                        

    private void tfNomeActionPerformed(java.awt.event.ActionEvent evt) {                                       
        // TODO add your handling code here:
    }                                      

    private void btConectarActionPerformed(java.awt.event.ActionEvent evt) {                                           
        String nome = tfNome.getText();
        String ip;
        String port = porta.getText();
        if (validaIP(ipServidor.getText())){
            ip = ipServidor.getText();
            if (!nome.isEmpty() && !port.isEmpty() && !ip.isEmpty()){
            
            try {
            msgBean = new MensagemBean();
            msgBean.setAcao(Acao.CONECTAR);
            msgBean.setNome(nome);
            msgBean.setStatus(MensagemBean.Status.DISPONIVEL);
            msgBean.setNomePrivado("todos");
        
            
            cSocket = new ClienteSocket();
            socket = cSocket.conectar(ip, Integer.parseInt(port));

            new Thread(new AnalisaSocket(socket)).start();

            cSocket.enviar(msgBean);
            }   catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, digite um valor válido.\nUtilize apenas números.");
                desconectado();
                }    
            } else {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, digite um valor válido.\nUtilize apenas números e o formato: ###.###.###.###");
        }
    }                                          

    private void btSairActionPerformed(java.awt.event.ActionEvent evt) {                                       
        MensagemBean msgBean = new MensagemBean();
        msgBean.setNome(this.msgBean.getNome());
        msgBean.setAcao(Acao.DESCONECTAR);
        cSocket.enviar(msgBean);
        desconectado();
    }                                      

    private void btLimparActionPerformed(java.awt.event.ActionEvent evt) {                                         
        taEnviada.setText("");
    }                                        

    private void btEnviarActionPerformed(java.awt.event.ActionEvent evt) {                                         
        String texto = taEnviada.getText();
        String nome = msgBean.getNome();
        msgBean = new MensagemBean();
        
        if(listaOnlines.getSelectedIndex() >= 0){
            msgBean.setNomePrivado(listaOnlines.getSelectedValue());
            System.out.println(msgBean.getNomePrivado());
            msgBean.setAcao(Acao.ENVIAR_PARA_UM);
            listaOnlines.clearSelection();
        } else {
            msgBean.setAcao(Acao.ENVIAR_PARA_TODOS);
            msgBean.setNomePrivado("todos");
        }
        
        if(!texto.isEmpty()){
            msgBean.setNome(nome);
            msgBean.setTexto(texto);
            
            taRecebidas.append("Você: " + texto + "\n");
            
            cSocket.enviar(msgBean);
        }
        
        taEnviada.setText("");
    }                                        

    private void portaActionPerformed(java.awt.event.ActionEvent evt) {                                      
        // TODO add your handling code here:
    }                                     

    private void statusActionPerformed(java.awt.event.ActionEvent evt) {                                       
        // TODO add your handling code here:
    }                                      

    private void statusItemStateChanged(java.awt.event.ItemEvent evt) {                                        
        /*
        if(evt.getStateChange() == ItemEvent.SELECTED){
            int selecao = status.getSelectedIndex();
            switch(selecao){
                case 0:
                    msgBean.setStatus(MensagemBean.Status.DISPONIVEL);
                    cSocket.enviar(msgBean);
                    break;
                case 1:
                    msgBean.setStatus(MensagemBean.Status.OCUPADO);
                    cSocket.enviar(msgBean);
                    break;
                case 2:
                    msgBean.setStatus(MensagemBean.Status.AUSENTE);
                    cSocket.enviar(msgBean);
                    break;
            }
        }
        */
    }                                       

    private void novoChatActionPerformed(java.awt.event.ActionEvent evt) {                                         
        adicionarAba();
    }                                        

    // Variables declaration - do not modify                     
    private javax.swing.JButton btConectar;
    private javax.swing.JButton btEnviar;
    private javax.swing.JButton btLimpar;
    private javax.swing.JButton btSair;
    private javax.swing.JTextField ipServidor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList<String> listaOnlines;
    private javax.swing.JButton novoChat;
    private javax.swing.JTextField porta;
    private javax.swing.JComboBox<String> status;
    private javax.swing.JTextArea taEnviada;
    private javax.swing.JTextArea taRecebidas;
    private javax.swing.JTextField tfNome;
    // End of variables declaration                   

class CorStatus <String> extends JLabel implements ListCellRenderer {

    public CorStatus() {
        super();
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        
        System.out.println("teste");
        
        switch (msgBean.getStatus()) {
            case DISPONIVEL:
                setForeground(Color.green);
                break;
            case OCUPADO:
                setForeground(Color.yellow);
                break;
            case AUSENTE:
                setForeground(Color.red);
            default:
                break;
        }        
        return this;
    }

}

class IPTextFieldVerifier extends InputVerifier {
   @Override
   public boolean verify(JComponent input) {
      if (input instanceof JFormattedTextField) {
         JFormattedTextField ftf = (JFormattedTextField)input;
         AbstractFormatter formatter = ftf.getFormatter();
         if (formatter != null) {
            String text = ftf.getText();
            StringTokenizer st = new StringTokenizer(text, ".");
            while (st.hasMoreTokens()) {
               int value = Integer.parseInt((String) st.nextToken());
               if (value < 0 || value > 255) {
                  // to prevent recursive calling of the
                  // InputVerifier, set it to null and
                  // restore it after the JOptionPane has
                  // been clicked.
                  input.setInputVerifier(null);
                  JOptionPane.showMessageDialog(new Frame(), "Formato Inválido de IP!", "Error", JOptionPane.ERROR_MESSAGE);
                  input.setInputVerifier(this); 
                  return false;
               }
            }
            return true;
         }
      }
      return true;
   }
  
   @Override
   public boolean shouldYieldFocus(JComponent input) {
      return verify(input);
   }
}
}

