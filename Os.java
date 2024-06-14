import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

public class Os {

    public static JFrame os;
    public static JMenuBar menu;
    public static JFileChooser fileChooser;
    public static JButton homeButton;
    public static JButton aboutButton;
    public static JButton whereButton;
    public static JButton contactButton;
    public static JButton isoButton;
    public static JLabel statusLabel;

    public static void main(String[] args) {

        // Creazione del JFrame
        os = new JFrame("OS INSTALLER");
        os.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        os.setSize(1000, 1000);
        os.setLayout(new FlowLayout(30));

        // Creazione e configurazione della JMenuBar
        menu = new JMenuBar();
        menu.setBackground(Color.BLACK);
        os.setJMenuBar(menu); // Aggiunta della JMenuBar al JFrame

        // Creazione e configurazione dei bottoni
        homeButton = new JButton("HOME");
        homeButton.setBackground(Color.WHITE);
        os.add(homeButton, BorderLayout.NORTH); // Aggiunta del bottone Home in alto

        aboutButton = new JButton("CHI SIAMO");
        aboutButton.setBackground(Color.WHITE);
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(os, "Sono Fabris Vulpio e sono un programmatore Java. Questo è il mio software per montare e rendere eseguibile un sistema operativo su chiavetta USB.");
            }
        });
        os.add(aboutButton, BorderLayout.SOUTH); // Aggiunta del bottone Chi Siamo in basso

        whereButton = new JButton("DOVE SIAMO");
        whereButton.setBackground(Color.WHITE);
        whereButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(os, "Siamo a Bari, via Colonnello De Cristoforis 8, Spazio 13.");
            }
        });
        os.add(whereButton, BorderLayout.NORTH); // Aggiunta del bottone Dove Siamo a sinistra

        contactButton = new JButton("CONTATTI");
        contactButton.setBackground(Color.WHITE);
        contactButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    URI uri = new URI("https://api.whatsapp.com/send?phone=+39 3471462385&text=Siamo a a Bari, Via Colonnello de Cristoforis 8, Spazio 13");
                    Desktop.getDesktop().browse(uri);
                } catch (URISyntaxException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        os.add(contactButton, BorderLayout.NORTH); // Aggiunta del bottone Contatti a destra

        // Creazione e configurazione del bottone per aprire il file .iso
        isoButton = new JButton("OPEN ISO");
        isoButton.setBackground(Color.WHITE);
        isoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File isoFile = fileChooser.getSelectedFile();
                    JFileChooser usbChooser = new JFileChooser();
                    usbChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int usbResult = usbChooser.showOpenDialog(null);
                    if (usbResult == JFileChooser.APPROVE_OPTION) {
                        File usbDrive = usbChooser.getSelectedFile();
                        try {
                            updateStatusLabel("Creating bootable USB...");
                            createBootableUSB(isoFile, usbDrive);
                            updateStatusLabel("Bootable USB created successfully.");
                        } catch (IOException ex) {
                            updateStatusLabel("Error: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        menu.add(isoButton); // Aggiunta del bottone ISO alla JMenuBar

        // Creazione e configurazione del JLabel per mostrare lo stato
        statusLabel = new JLabel("Status: Idle");
        os.add(statusLabel, BorderLayout.CENTER); // Aggiunta del JLabel al centro

        // Visualizzazione del JFrame
        os.setVisible(true);
    }

    private static void createBootableUSB(File isoFile, File usbDrive) throws IOException {
        // Costruzione del comando per creare l'unità avviabile
        String command = String.format("dd if=%s of=%s bs=4M status=progress", isoFile.getAbsolutePath(), usbDrive.getAbsolutePath());

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            process.waitFor();
        } catch (InterruptedException | IOException ex) {
            throw new IOException("Failed to create bootable USB", ex);
        }
    }

    private static void updateStatusLabel(String text) {
        statusLabel.setText("Status: " + text);
    }
}
