/*
 * Nicholas Saney
 * 
 * Created: October 04, 2015
 * 
 * SampleServerSocket.java
 * SampleServerSocket class definition
 */

package chairosoft.sample_server_socket;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// import java.util.ArrayList;
// import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.border.Border;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class SampleServerSocket
{
    //
    // Constants
    //
    
    public static final int PORT = 5678;
    public static final int MAX_BACKLOG = 100;
    public static final Border BASIC_BORDER = BorderFactory.createLineBorder(Color.BLACK, 1);
    
    
    //
    // Static Variables
    //
    
    public static ServerSocket serverSocket = null;
    
    
    //
    // Main Method
    //
    
    /**
     * A sample of using a server socket.
     * @param args (ignored)
     * @throws Exception (because you never know)
     */
    public static void main(String[] args)
        throws Exception
    {
        // Local Address
        String localHostAddress = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Local address is: " + localHostAddress);
        
        // Acquire server socket on port
        serverSocket = new ServerSocket(PORT, MAX_BACKLOG);
        int localSocketPort = serverSocket.getLocalPort();
        System.out.println("Local port is: " + localSocketPort);
        
        // Create GUI
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Sample Server Socket");
        
        final JPanel parentPanel = new JPanel();
        parentPanel.setBackground(Color.WHITE);
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));
        parentPanel.setPreferredSize(new Dimension(600, 400));
        parentPanel.setFocusable(true);
        parentPanel.requestFocus(); // receiving key events
        
        parentPanel.add(new JLabel("Local address is: " + localHostAddress));
        parentPanel.add(new JLabel("Local port is: " + localSocketPort));
        
        JScrollPane scrollPane = new JScrollPane(parentPanel);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        frame.setResizable(false); // set this before pack() to avoid change in window size
        frame.pack();
        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
        
        // Listen for client connections 
        // (call serverSocket.close() to break out of this)
        while (true)
        {
            Socket clientSocket = serverSocket.accept();
            Thread handler = new Thread(() ->
            {
                try
                {
                    ClientConnectionUI clientUI = new ClientConnectionUI(clientSocket);
                    clientUI.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    parentPanel.add(clientUI.panel);
                    clientUI.start();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    System.exit(1);
                }
            });
            handler.start();
        }
    }
    
    
    //
    // non-main Static Methods
    //
    
    
    //
    // Static Inner Classes
    //
    
    public static class ClientConnectionUI
    {
        protected volatile boolean isOpen = true;
        public final Socket clientSocket;
        public final InputStream clientIn;
        public final OutputStream clientOut;
        public final JPanel panel;
        public final JButton removeButton;
        public final JButton closeButton;
        public final JTextArea textAreaIn;
        public final JTextArea textAreaOut;
        
        public ClientConnectionUI(Socket _clientSocket) throws IOException
        {
            // Client Socket
            this.clientSocket = _clientSocket;
            this.clientIn = this.clientSocket.getInputStream();
            this.clientOut = this.clientSocket.getOutputStream();
            
            // GUI
            this.panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            
            this.removeButton = new JButton("Remove");
            this.removeButton.setEnabled(false);
            this.removeButton.addActionListener(e -> 
            {
                if (!this.isOpen)
                {
                    Container parent = this.panel.getParent();
                    parent.remove(this.panel);
                    ((JComponent) parent).revalidate();
                    parent.repaint();
                }
            });
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 2;
            this.panel.add(this.removeButton, c);
            this.panel.setBorder(BASIC_BORDER);
            
            this.closeButton = new JButton("Close");
            this.closeButton.addActionListener(e -> 
            {
                this.close();
            });
            c.gridx = 1;
            c.gridy = 0;
            c.gridheight = 2;
            this.panel.add(this.closeButton, c);
            
            JLabel labelIn = new JLabel("Input");
            c.gridx = 2;
            c.gridy = 0;
            c.gridheight = 1;
            this.panel.add(labelIn, c);
            
            JLabel labelOut = new JLabel("Output");
            c.gridx = 2;
            c.gridy = 1;
            c.gridheight = 1;
            this.panel.add(labelOut, c);
            
            this.textAreaIn = new JTextArea();
            this.textAreaIn.setEnabled(false);
            this.textAreaIn.setBorder(BASIC_BORDER);
            c.gridx = 3;
            c.gridy = 0;
            c.gridheight = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            // JScrollPane scrollIn = new JScrollPane(this.textAreaIn);
            this.panel.add(this.textAreaIn, c);
            
            this.textAreaOut = new JTextArea();
            this.textAreaOut.setBorder(BASIC_BORDER);
            // this.textAreaOut.setEnabled(false);
            c.gridx = 3;
            c.gridy = 1;
            c.gridheight = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            // JScrollPane scrollOut = new JScrollPane(this.textAreaOut);
            this.panel.add(this.textAreaOut, c);
        }
        
        public void close()
        {
            if (this.isOpen)
            {
                this.isOpen = false;
                try
                {
                    this.clientSocket.close();
                    //ALICE wuz here
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    System.exit(1);
                }
                
                this.removeButton.setEnabled(true);
                this.closeButton.setEnabled(false);
                this.closeButton.setText("Closed");
                this.textAreaOut.setEnabled(false);
            }
        }
        
        public void start()
        {
            // output listener
            this.textAreaOut.addKeyListener(new KeyAdapter()
            {
                public void keyTyped(KeyEvent e)
                {
                    char keyChar = e.getKeyChar();
                    try
                    {
                        ClientConnectionUI.this.clientOut.write(keyChar);
                        ClientConnectionUI.this.clientOut.flush();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                        ClientConnectionUI.this.close();
                    }
                }
            });
            
            // input listener
            try
            {
                while (this.isOpen)
                {
                    int nextByte = this.clientIn.read();
                    if (nextByte < 0)
                    {
                        this.close();
                        break;
                    }
                    final char c = (char)nextByte;
                    String text = this.textAreaIn.getText();
                    this.textAreaIn.setText(text + c);
                }
            }
            catch (Exception ex)
            {
                if (this.isOpen)
                {
                    ex.printStackTrace();
                    this.close();
                }
            }
        }
    }
    
}