 package es.studium.pracitca2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MusicPlayerApp {

    // Atributos principales de la aplicación
    private JFrame frame; // Ventana principal
    private JTable table; // Tabla para mostrar los archivos de música
    private DefaultTableModel tableModel; // Modelo de la tabla para gestionar los datos
    private List<File> musicFiles; // Lista para almacenar los archivos de música encontrados
    private Player currentPlayer = null; // Reproductor actual de música
    private Thread currentThread = null; // Hilo que reproduce la música
    private boolean stopRequested = false; // Bandera para detener la reproducción

    // Constructor de la clase
    public MusicPlayerApp() {
        musicFiles = new ArrayList<>(); // Inicializa la lista de archivos de música
        initComponents(); // Inicializa los componentes gráficos de la interfaz
        searchMusicFiles(); // Busca archivos de música en el sistema
    }

    // Método para inicializar los componentes gráficos
    private void initComponents() {
        frame = new JFrame("Music Player"); // Crea la ventana principal
        frame.setSize(600, 400); // Tamaño de la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Acción al cerrar
        frame.setLayout(new BorderLayout()); // Usa BorderLayout para organizar los elementos

        // Configura la tabla para mostrar los nombres de los archivos
        tableModel = new DefaultTableModel(new Object[]{"File Name"}, 0); // Una columna: "File Name"
        table = new JTable(tableModel); // Crea la tabla con el modelo
        frame.add(new JScrollPane(table), BorderLayout.CENTER); // Agrega la tabla con barra de desplazamiento al centro

        // Panel inferior con botones para "Reproducir" y "Parar"
        JPanel panel = new JPanel();
        JButton playButton = new JButton("Reproducir");
        JButton stopButton = new JButton("Parar");
        panel.add(playButton); // Agrega el botón "Reproducir"
        panel.add(stopButton); // Agrega el botón "Parar"
        frame.add(panel, BorderLayout.SOUTH); // Coloca el panel en la parte inferior

        // Acción para el botón "Reproducir"
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow(); // Obtiene la fila seleccionada en la tabla
                if (selectedRow != -1) { // Verifica que se haya seleccionado una fila
                    playMusic(selectedRow); // Reproduce la música del archivo seleccionado
                }
            }
        });

        // Acción para el botón "Parar"
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopMusic(); // Detiene la reproducción de música
            }
        });

        // Centra la ventana en la pantalla
        frame.setLocationRelativeTo(null);
    }

    // Método para buscar archivos de música en el sistema
    private void searchMusicFiles() {
        File[] roots = File.listRoots(); // Obtiene las unidades de disco del sistema (C:, D:, etc.)
        for (File root : roots) {
            searchFilesInDirectory(root, new String[]{"mp3", "wav"}); // Busca archivos MP3 y WAV en cada unidad
        }
        updateTable(); // Actualiza la tabla con los archivos encontrados
    }

    // Método recursivo para buscar archivos en un directorio y sus subdirectorios
    private void searchFilesInDirectory(File dir, String[] extensions) {
        if (dir != null && dir.isDirectory()) { // Verifica si el archivo es un directorio
            File[] files = dir.listFiles(); // Obtiene la lista de archivos en el directorio
            if (files != null) { // Verifica que no sea null
                for (File file : files) {
                    if (file.isDirectory()) {
                        searchFilesInDirectory(file, extensions); // Busca recursivamente en los subdirectorios
                    } else {
                        for (String ext : extensions) {
                            if (file.getName().toLowerCase().endsWith(ext)) { // Verifica si el archivo tiene una extensión válida
                                musicFiles.add(file); // Agrega el archivo a la lista
                                break; // Sale del bucle de extensiones
                            }
                        }
                    }
                }
            }
        }
    }

    // Método para actualizar la tabla con los archivos encontrados
    private void updateTable() {
        for (File file : musicFiles) {
            tableModel.addRow(new Object[]{file.getName()}); // Agrega el nombre del archivo a la tabla
        }
    }

    // Método para reproducir música
    private void playMusic(int index) {
        File file = musicFiles.get(index); // Obtiene el archivo seleccionado en la tabla

        stopMusic(); // Detiene la reproducción actual si hay alguna en curso

        stopRequested = false; // Reinicia la bandera de parada
        currentThread = new Thread(() -> { // Crea un hilo para la reproducción
            try {
                FileInputStream fis = new FileInputStream(file); // Crea un flujo de entrada para el archivo
                currentPlayer = new Player(fis); // Inicializa el reproductor con el archivo
                while (!stopRequested) { // Mientras no se solicite detener
                    currentPlayer.play(1); // Reproduce un frame de audio
                }
                fis.close(); // Cierra el archivo cuando se detiene la reproducción
            } catch (JavaLayerException | java.io.IOException e) {
                e.printStackTrace(); // Muestra errores en la consola
            }
        });
        currentThread.start(); // Inicia el hilo de reproducción
    }

    // Método para detener la música
    private void stopMusic() {
        stopRequested = true; // Cambia la bandera para detener la reproducción
        if (currentPlayer != null) {
            currentPlayer.close(); // Cierra el reproductor
            currentPlayer = null;
        }
        if (currentThread != null) {
            currentThread.interrupt(); // Interrumpe el hilo de reproducción
            currentThread = null;
        }
    }

    // Método para iniciar la aplicación
    public void start() {
        frame.setVisible(true); // Hace visible la ventana principal
    }

    // Método principal para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MusicPlayerApp().start()); // Inicia la aplicación en el hilo de eventos de Swing
    }
}
