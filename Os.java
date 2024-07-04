import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class CombinedApp extends JFrame implements HyperlinkListener {

    // Components for Web Browser
    private JButton buttonBack = new JButton("<");
    private JButton buttonForward = new JButton(">");
    private JTextField locationTextField = new JTextField(35);
    private JEditorPane displayEditorPane = new JEditorPane();
    private ArrayList<String> pageList = new ArrayList<>();

    // Components for OS Installer
    private JFileChooser fileChooser;
    private JButton isoButton = new JButton("OPEN ISO");
    private JLabel statusLabel = new JLabel("Status: Idle");

    // Common Components
    private JTextField emailField = new JTextField(20);
    private JPasswordField emailPasswordField = new JPasswordField(20);

    public CombinedApp() {
        setTitle("Combined Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Web Browser Tab
        JPanel browserPanel = new JPanel(new BorderLayout());
        setupWebBrowser(browserPanel);
        tabbedPane.addTab("Web Browser", browserPanel);

        // OS Installer Tab
        JPanel osPanel = new JPanel(new BorderLayout());
        setupOSInstaller(osPanel);
        tabbedPane.addTab("OS Installer", osPanel);

        getContentPane().add(tabbedPane);
        pack();
        setLocationRelativeTo(null); // Center window on screen
    }

    private void setupWebBrowser(JPanel panel) {
        JPanel bttnPanel = new JPanel();
        bttnPanel.setBackground(Color.WHITE);
        buttonBack.addActionListener(e -> backAction());
        buttonBack.setEnabled(false);
        bttnPanel.add(buttonBack);
        buttonForward.addActionListener(e -> forwardAction());
        buttonForward.setEnabled(false);
        bttnPanel.add(buttonForward);

        locationTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionGo();
                }
            }
        });
        locationTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        bttnPanel.add(locationTextField);
        JButton bttnGo = new JButton("GO");
        bttnGo.addActionListener(e -> actionGo());
        bttnGo.setFont(new Font("Arial", Font.PLAIN, 14));
        bttnPanel.add(bttnGo);

        displayEditorPane.setContentType("text/html");
        displayEditorPane.setEditable(false);
        displayEditorPane.addHyperlinkListener(this);

        JScrollPane scrollPane = new JScrollPane(displayEditorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(bttnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Initialize browser with a default page
        try {
            URL defaultUrl = new URL("https://www.google.com");
            showPage(defaultUrl, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupOSInstaller(JPanel panel) {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
        isoButton.setBackground(Color.WHITE);
        isoButton.addActionListener(e -> openIsoFile());
        panel.add(isoButton);
        panel.add(statusLabel);
    }

    private void openIsoFile() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File isoFile = fileChooser.getSelectedFile();
            // Simulate installation process for demonstration
            updateStatusLabel("Creating bootable USB...");
            try {
                Thread.sleep(3000); // Simulate process
                updateStatusLabel("Bootable USB created successfully.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void backAction() {
        try {
            URL currentUrl = displayEditorPane.getPage();
            int pageIndex = pageList.indexOf(currentUrl.toString());
            if (pageIndex > 0) {
                showPage(new URL(pageList.get(pageIndex - 1)), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void forwardAction() {
        try {
            URL currentUrl = displayEditorPane.getPage();
            int pageIndex = pageList.indexOf(currentUrl.toString());
            if (pageIndex < pageList.size() - 1) {
                showPage(new URL(pageList.get(pageIndex + 1)), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionGo() {
        URL verifiedUrl = verifyUrl(locationTextField.getText());
        if (verifiedUrl != null) {
            showPage(verifiedUrl, true);
        } else {
            System.out.println("Invalid URL");
        }
    }

    private URL verifyUrl(String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            return null;
        }
        try {
            return new URL(url);
        } catch (Exception e) {
            return null;
        }
    }

    private void showPage(URL pageUrl, boolean addToList) {
        try {
            URLConnection connection = pageUrl.openConnection();
            connection.connect();

            InputStream input = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(input);
            StringBuilder builder = new StringBuilder();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
                builder.append(new String(buffer, 0, bytesRead));
            }

            displayEditorPane.setText(builder.toString());
            bis.close();

            if (addToList) {
                int listSize = pageList.size();
                if (listSize > 0) {
                    int pageIndex = pageList.indexOf(pageUrl.toString());
                    for (int i = listSize - 1; i > pageIndex; i--) {
                        pageList.remove(i);
                    }
                }
                pageList.add(pageUrl.toString());
            }

            locationTextField.setText(pageUrl.toString());
            updateButtons();
        } catch (Exception e) {
            System.out.println("Unable to load page: " + e.getMessage());
        }
    }

    private void updateButtons() {
        buttonBack.setEnabled(pageList.size() > 1);
        buttonForward.setEnabled(pageList.size() > 1);
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (event instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent linkEvent = (HTMLFrameHyperlinkEvent) event;
                HTMLDocument document = (HTMLDocument) displayEditorPane.getDocument();
                document.processHTMLFrameHyperlinkEvent(linkEvent);
            } else {
                showPage(event.getURL(), true);
            }
        }
    }

    private void updateStatusLabel(String text) {
        statusLabel.setText("Status: " + text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            CombinedApp app = new CombinedApp();
            app.setVisible(true);
        });
    }
}
