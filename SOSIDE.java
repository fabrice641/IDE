import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.nio.file.*;

public class SOSIDE extends JFrame {
    private JTextArea queryArea;
    private JTextArea outputArea;
    private JFileChooser fileChooser;
    private Connection connection;
    private File recentFile;
    private JDesktopPane desktopPane;
    private DragSource dragSource;
    private String sdkPath;

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
        JButton connectToServerButton = new JButton("Connect to Server");
        JButton newFragmentButton = new JButton("New Fragment");
        JButton newServiceButton = new JButton("New Service");

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
        toolBar.add(connectToServerButton);
        toolBar.add(newFragmentButton);
        toolBar.add(newServiceButton);

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

        desktopPane = new JDesktopPane();
        desktopPane.setDropTarget(new DropTarget(desktopPane, new ComponentDropTargetListener()));

        add(desktopPane, BorderLayout.CENTER);

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
        connectToServerButton.addActionListener(new ConnectToServerAction());
        newFragmentButton.addActionListener(new NewFragmentAction());
        newServiceButton.addActionListener(new NewServiceAction());

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

    private void createAndroidProject(String projectName) throws IOException {
        File projectDir = new File(projectName);
        if (!projectDir.exists() && projectDir.mkdirs()) {
            // Create directories
            new File(projectDir, "app/src/main/java/com/example/" + projectName).mkdirs();
            new File(projectDir, "app/src/main/res/layout").mkdirs();
            new File(projectDir, "app/src/main/res/values").mkdirs();
            new File(projectDir, "app/src/androidTest/java/com/example/" + projectName).mkdirs();
            new File(projectDir, "app/src/test/java/com/example/" + projectName).mkdirs();

            // Create build.gradle files and other necessary files
            String buildGradleProjectContent = "buildscript {\n" +
                    "    repositories {\n" +
                    "        google()\n" +
                    "        mavenCentral()\n" +
                    "    }\n" +
                    "    dependencies {\n" +
                    "        classpath 'com.android.tools.build:gradle:7.0.0'\n" +
                    "    }\n" +
                    "}\n" +
                    "allprojects {\n" +
                    "    repositories {\n" +
                    "        google()\n" +
                    "        mavenCentral()\n" +
                    "    }\n" +
                    "}";
            String buildGradleAppContent = "apply plugin: 'com.android.application'\n" +
                    "android {\n" +
                    "    compileSdkVersion 30\n" +
                    "    defaultConfig {\n" +
                    "        applicationId \"com.example." + projectName + "\"\n" +
                    "        minSdkVersion 16\n" +
                    "        targetSdkVersion 30\n" +
                    "        versionCode 1\n" +
                    "        versionName \"1.0\"\n" +
                    "    }\n" +
                    "    buildTypes {\n" +
                    "        release {\n" +
                    "            minifyEnabled false\n" +
                    "            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n" +
                    "dependencies {\n" +
                    "    implementation 'com.android.support:appcompat-v7:28.0.0'\n" +
                    "    testImplementation 'junit:junit:4.12'\n" +
                    "    androidTestImplementation 'com.android.support.test:runner:1.0.2'\n" +
                    "    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'\n" +
                    "}";
            String settingsGradleContent = "include ':app'";
            String mainActivityContent = "package com.example." + projectName + ";\n" +
                    "import android.os.Bundle;\n" +
                    "import androidx.appcompat.app.AppCompatActivity;\n" +
                    "public class MainActivity extends AppCompatActivity {\n" +
                    "    @Override\n" +
                    "    protected void onCreate(Bundle savedInstanceState) {\n" +
                    "        super.onCreate(savedInstanceState);\n" +
                    "        setContentView(R.layout.activity_main);\n" +
                    "    }\n" +
                    "}";
            String activityMainXmlContent = "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                    "    android:layout_width=\"match_parent\"\n" +
                    "    android:layout_height=\"match_parent\">\n" +
                    "    <TextView\n" +
                    "        android:layout_width=\"wrap_content\"\n" +
                    "        android:layout_height=\"wrap_content\"\n" +
                    "        android:text=\"Hello World!\"/>\n" +
                    "</RelativeLayout>";
            String colorsXmlContent = "<resources>\n" +
                    "    <color name=\"colorPrimary\">#3F51B5</color>\n" +
                    "    <color name=\"colorPrimaryDark\">#303F9F</color>\n" +
                    "    <color name=\"colorAccent\">#FF4081</color>\n" +
                    "</resources>";
            String stringsXmlContent = "<resources>\n" +
                    "    <string name=\"app_name\">" + projectName + "</string>\n" +
                    "</resources>";
            String stylesXmlContent = "<resources>\n" +
                    "    <style name=\"AppTheme\" parent=\"Theme.AppCompat.Light.DarkActionBar\">\n" +
                    "        <item name=\"colorPrimary\">@color/colorPrimary</item>\n" +
                    "        <item name=\"colorPrimaryDark\">@color/colorPrimaryDark</item>\n" +
                    "        <item name=\"colorAccent\">@color/colorAccent</item>\n" +
                    "    </style>\n" +
                    "</resources>";

            writeToFile(new File(projectDir, "build.gradle"), buildGradleProjectContent);
            writeToFile(new File(projectDir, "settings.gradle"), settingsGradleContent);
            writeToFile(new File(projectDir, "app/build.gradle"), buildGradleAppContent);
            writeToFile(new File(projectDir, "app/src/main/java/com/example/" + projectName + "/MainActivity.java"), mainActivityContent);
            writeToFile(new File(projectDir, "app/src/main/res/layout/activity_main.xml"), activityMainXmlContent);
            writeToFile(new File(projectDir, "app/src/main/res/values/colors.xml"), colorsXmlContent);
            writeToFile(new File(projectDir, "app/src/main/res/values/strings.xml"), stringsXmlContent);
            writeToFile(new File(projectDir, "app/src/main/res/values/styles.xml"), stylesXmlContent);

            outputArea.setText("New project created: " + projectName);
        } else {
            outputArea.setText("Project directory already exists or failed to create.");
        }
    }

    private void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
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
            String tableName = JOptionPane.showInputDialog("Enter Table Name:");
            String columns = JOptionPane.showInputDialog("Enter Columns (name type, ...):");
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE " + tableName + " (" + columns + ")");
                outputArea.setText("Table created: " + tableName);
            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to create table: " + ex.getMessage());
            }
        }
    }

    private class CreateDatabaseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String databaseName = JOptionPane.showInputDialog("Enter Database Name:");
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE " + databaseName);
                outputArea.setText("Database created: " + databaseName);
            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to create database: " + ex.getMessage());
            }
        }
    }

    private class CreateRecordAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String tableName = JOptionPane.showInputDialog("Enter Table Name:");
            String columns = JOptionPane.showInputDialog("Enter Columns (name, ...):");
            String values = JOptionPane.showInputDialog("Enter Values (value, ...):");
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")");
                outputArea.setText("Record inserted into: " + tableName);
            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to insert record: " + ex.getMessage());
            }
        }
    }

    private class NewProjectAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String projectName = JOptionPane.showInputDialog("Enter Project Name:");
            try {
                createAndroidProject(projectName);
            } catch (IOException ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to create project: " + ex.getMessage());
            }
        }
    }

    private class OpenProjectAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                outputArea.setText("Project opened: " + selectedFile.getAbsolutePath());
            }
        }
    }

    private class RunAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String projectPath = new JFileChooser().getCurrentDirectory().getAbsolutePath();
            try {
                Process process = Runtime.getRuntime().exec(sdkPath + "/platform-tools/adb install " + projectPath + "/app/build/outputs/apk/debug/app-debug.apk");
                process.waitFor();
                outputArea.setText("Project running.");
            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to run project: " + ex.getMessage());
            }
        }
    }

    private class DebugAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            outputArea.setText("Debugging project.");
            // Add logic to debug the project
        }
    }

    private class GenerateExecutableAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String projectPath = new JFileChooser().getCurrentDirectory().getAbsolutePath();
            try {
                Process process = Runtime.getRuntime().exec(sdkPath + "/build-tools/30.0.3/apksigner sign --ks my-release-key.jks --out " + projectPath + "/app-release.apk " + projectPath + "/app/build/outputs/apk/release/app-release-unsigned.apk");
                process.waitFor();
                outputArea.setText("Executable generated.");
            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.setText("Failed to generate executable: " + ex.getMessage());
            }
        }
    }

    private class ConnectToServerAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String serverAddress = JOptionPane.showInputDialog("Enter Server Address:");
            // Add logic to connect to the server
            outputArea.setText("Connected to server: " + serverAddress);
        }
    }

    private class NewFragmentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fragmentName = JOptionPane.showInputDialog("Enter Fragment Name:");
            // Add logic to create a new fragment
            outputArea.setText("New fragment created: " + fragmentName);
        }
    }

    private class NewServiceAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String serviceName = JOptionPane.showInputDialog("Enter Service Name:");
            // Add logic to create a new service
            outputArea.setText("New service created: " + serviceName);
        }
    }

    private class ComponentDropTargetListener implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            // Handle drag enter
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // Handle drag over
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // Handle drop action changed
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            // Handle drag exit
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : files) {
                        queryArea.read(new BufferedReader(new FileReader(file)), null);
                    }
                    dtde.dropComplete(true);
                    outputArea.setText("File(s) dropped.");
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                dtde.rejectDrop();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SOSIDE::new);
    }
}
