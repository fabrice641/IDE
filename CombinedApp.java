import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
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
    private JButton downloadButton = new JButton("DOWNLOAD ISO");
    private JLabel statusLabel = new JLabel("Status: Idle");
    private JProgressBar progressBar = new JProgressBar();

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
        downloadButton.setBackground(Color.WHITE);
        downloadButton.addActionListener(e -> downloadIsoFile());
        progressBar.setPreferredSize(new Dimension(300, 25));
        progressBar.setStringPainted(true);

        panel.add(isoButton);
        panel.add(downloadButton);
        panel.add(statusLabel);
        panel.add(progressBar);
    }

    private void openIsoFile() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File isoFile = fileChooser.getSelectedFile();
            // Simulate installation process for demonstration
            flashIsoToUsb(isoFile.getAbsolutePath());
        }
    }

    private void downloadIsoFile() {
        String url = JOptionPane.showInputDialog(this, "Enter ISO URL:");
        if (url != null && !url.isEmpty()) {
            new Thread(() -> downloadIsoFromUrl(url)).start();
        }
    }

    private void downloadIsoFromUrl(String url) {
        try {
            updateStatusLabel("Downloading ISO...");
            progressBar.setIndeterminate(true);
            InputStream in = new URL(url).openStream();
            Path tempFile = Files.createTempFile("downloaded_iso", ".iso");
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            updateStatusLabel("Download complete. Flashing ISO...");
            progressBar.setIndeterminate(false);
            flashIsoToUsb(tempFile.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Failed to download ISO.");
            progressBar.setIndeterminate(false);
        }
    }

    private void flashIsoToUsb(String isoPath) {
        // Simulate flashing process for demonstration
        try {
            // Here you would typically call an external script/tool with admin rights to flash the USB
            updateStatusLabel("Creating bootable USB...");
            progressBar.setValue(0);
            for (int i = 1; i <= 100; i++) {
                Thread.sleep(30); // Simulate progress
                progressBar.setValue(i);
            }
            updateStatusLabel("Bootable USB created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Failed to create bootable USB.");
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
            displayEditorPane.setPage(pageUrl);
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
