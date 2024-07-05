import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;

public class SOSIDE extends JFrame {
    private JTextArea queryArea;
    private JTextArea outputArea;
    private JFileChooser fileChooser;
    private Connection connection;
    private File recentFile;

    public SOSIDE() {
        setTitle("SOS IDE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set fullscreen mode
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);

        // Set the icon
        try {
            setIconImage(new ImageIcon("sos.jpg").getImage());
        } catch (Exception e) {
            System.out.println("Icon not found.");
        }

        // Toolbars for buttons similar to Android Studio
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton openFileButton = new JButton("Open File");
        JButton executeQueryButton = new JButton("Execute SQL");
        JButton connectDBButton = new JButton("Connect to Database");
        JButton backupDBButton = new JButton("Backup Database");
        JButton createTableButton = new JButton("Create Table");
        JButton createDatabaseButton = new JButton("Create Database");
        JButton createRecordButton = new JButton("Create Record");
        JButton newProjectButton = new JButton("New Project");
        JButton openProjectButton = new JButton("Open Project");
        JButton runButton = new JButton("Run");
        JButton debugButton = new JButton("Debug");
        JButton generateExecutableButton = new JButton("Generate Executable");

        toolBar.add(openFileButton);
        toolBar.add(connectDBButton);
        toolBar.add(executeQueryButton);
        toolBar.add(backupDBButton);
        toolBar.add(createTableButton);
        toolBar.add(createDatabaseButton);
        toolBar.add(createRecordButton);
        toolBar.add(newProjectButton);
        toolBar.add(openProjectButton);
        toolBar.add(runButton);
        toolBar.add(debugButton);
        toolBar.add(generateExecutableButton);

        // Panels for query and output areas
        queryArea = new JTextArea(10, 80);
        outputArea = new JTextArea(10, 80);
        fileChooser = new JFileChooser();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(queryArea),
                new JScrollPane(outputArea));
        splitPane.setResizeWeight(0.5);

        // Add components to the frame
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Add action listeners
        openFileButton.addActionListener(new OpenFileAction());
        connectDBButton.addActionListener(new ConnectDBAction());
        executeQueryButton.addActionListener(new ExecuteQueryAction());
        backupDBButton.addActionListener(new BackupDBAction());
        createTableButton.addActionListener(new CreateTableAction());
        createDatabaseButton.addActionListener(new CreateDatabaseAction());
        createRecordButton.addActionListener(new CreateRecordAction());
        newProjectButton.addActionListener(new NewProjectAction());
        openProjectButton.addActionListener(new OpenProjectAction());
        runButton.addActionListener(new RunAction());
        debugButton.addActionListener(new DebugAction());
        generateExecutableButton.addActionListener(new GenerateExecutableAction());

        loadRecentFile();
    }

    private void loadRecentFile() {
        try {
            File recent = new File("recent.txt");
            if (recent.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(recent));
                recentFile = new File(reader.readLine());
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveRecentFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("recent.txt"));
            writer.write(file.getAbsolutePath());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class OpenFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    queryArea.read(reader, null);
                    saveRecentFile(selectedFile);
                    outputArea.setText("Opened file: " + selectedFile.getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    outputArea.setText("Error opening file: " + ex.getMessage());
                }
            }
        }
    }

    private class ConnectDBAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String url = JOptionPane.showInputDialog("Enter Database URL:");
            String user = JOptionPane.showInputDialog("Enter Username:");
            String password = JOptionPane.showInputDialog("Enter Password:");
            try {
                connection = DriverManager.getConnection(url, user, password);
                outputArea.setText("Connected to the database.");
            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to connect to the database: " + ex.getMessage());
            }
        }
    }

    private class ExecuteQueryAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(queryArea.getText());
                    StringBuilder results = new StringBuilder();
                    int columns = resultSet.getMetaData().getColumnCount();
                    while (resultSet.next()) {
                        for (int i = 1; i <= columns; i++) {
                            results.append(resultSet.getString(i)).append(" ");
                        }
                        results.append("\n");
                    }
                    outputArea.setText(results.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.setText("Failed to execute query: " + ex.getMessage());
                }
            } else {
                outputArea.setText("Not connected to any database.");
            }
        }
    }

    private class BackupDBAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (connection != null) {
                try {
                    String backupDir = JOptionPane.showInputDialog("Enter Backup Directory:");
                    String backupCommand = "mysqldump -u root -p password --databases yourdatabase -r " + backupDir + "/backup.sql";
                    Process runtimeProcess = Runtime.getRuntime().exec(backupCommand);
                    int processComplete = runtimeProcess.waitFor();
                    if (processComplete == 0) {
                        outputArea.setText("Backup created successfully.");
                    } else {
                        outputArea.setText("Backup creation failed.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.setText("Failed to create backup: " + ex.getMessage());
                }
            } else {
                outputArea.setText("Not connected to any database.");
            }
        }
    }

    private class CreateTableAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    String tableName = JOptionPane.showInputDialog("Enter table name:");
                    String tableSchema = JOptionPane.showInputDialog("Enter table schema (e.g., id INT, name VARCHAR(100)):");
                    String createTableSQL = "CREATE TABLE " + tableName + " (" + tableSchema + ")";
                    statement.executeUpdate(createTableSQL);
                    outputArea.setText("Table " + tableName + " created successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.setText("Failed to create table: " + ex.getMessage());
                }
            } else {
                outputArea.setText("Not connected to any database.");
            }
        }
    }

    private class CreateDatabaseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    String databaseName = JOptionPane.showInputDialog("Enter database name:");
                    String createDatabaseSQL = "CREATE DATABASE " + databaseName;
                    statement.executeUpdate(createDatabaseSQL);
                    outputArea.setText("Database " + databaseName + " created successfully.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.setText("Failed to create database: " + ex.getMessage());
                }
            } else {
                outputArea.setText("Not connected to any database.");
            }
        }
    }

    private class CreateRecordAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (connection != null) {
                try (Statement statement = connection.createStatement()) {
                    String tableName = JOptionPane.showInputDialog("Enter table name:");
                    String columnValues = JOptionPane.showInputDialog("Enter column values (e.g., 1, 'John Doe'):");
                    String insertRecordSQL = "INSERT INTO " + tableName + " VALUES (" + columnValues + ")";
                    statement.executeUpdate(insertRecordSQL);
                    outputArea.setText("Record inserted successfully into table " + tableName + ".");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.setText("Failed to insert record: " + ex.getMessage());
                }
            } else {
                outputArea.setText("Not connected to any database.");
            }
        }
    }

    private class NewProjectAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            queryArea.setText("");
            outputArea.setText("New project created.");
        }
    }

    private class OpenProjectAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    queryArea.read(reader, null);
                    saveRecentFile(selectedFile);
                    outputArea.setText("Opened project: " + selectedFile.getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    outputArea.setText("Error opening project: " + ex.getMessage());
                }
            }
        }
    }

    private class RunAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            outputArea.setText("Run project functionality not implemented yet.");
        }
    }

    private class DebugAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            outputArea.setText("Debug project functionality not implemented yet.");
        }
    }

    private class GenerateExecutableAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            outputArea.setText("Generate executable functionality not implemented yet.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SOSIDE().setVisible(true));
    }
}
