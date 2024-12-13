import javax.swing.*;
import java.io.*;
import java.util.*;

public class FileDatabase {
    private File dbFile;
    private final Map<String, Long> indexMap;

    public FileDatabase(String fileName) throws IOException {
        dbFile = new File(fileName);
        indexMap = new HashMap<>();
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        } else {
            loadIndex();
        }
    }

    private void loadIndex() throws IOException {
        indexMap.clear();
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
            String line;
            long position = 0;
            while ((line = raf.readLine()) != null) {
                String key = line.split(",")[1]; // Уникальный Identifier
                indexMap.put(key, position);
                position = raf.getFilePointer();
            }
        }
    }

    public void addRecord(String key, String record) throws IOException {
        if (indexMap.containsKey(key)) {
            throw new IllegalArgumentException("Record with this identifier already exists.");
        }
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            raf.seek(dbFile.length());
            long position = raf.getFilePointer();
            raf.writeBytes(record + "\n");
            indexMap.put(key, position);
        }
    }

    public List<String> searchByField(String field, String value) throws IOException {
        List<String> results = new ArrayList<>();
        int fieldIndex = getFieldIndex(field);

        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[fieldIndex].equalsIgnoreCase(value)) {
                    results.add(line);
                }
            }
        }
        return results;
    }

    public void deleteByField(String field, String value) throws IOException {
        File tempFile = new File(dbFile.getAbsolutePath() + ".tmp");
        int fieldIndex = getFieldIndex(field);

        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[fieldIndex].equalsIgnoreCase(value)) {
                    writer.write(line + "\n");
                } else if (field.equalsIgnoreCase("Identifier")) {
                    indexMap.remove(fields[1]);
                }
            }
        }
        dbFile.delete();
        tempFile.renameTo(dbFile);
    }

    public void clearDatabase() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbFile))) {
            writer.write("");
        }
        indexMap.clear();
    }

    public void openDatabase(String path) throws IOException {
        dbFile = new File(path);
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }
        loadIndex();
    }

    public void saveDatabase() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Database As");

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();

            if (!saveFile.getName().endsWith(".txt")) {
                saveFile = new File(saveFile.getAbsolutePath() + ".txt");
            }

            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = new FileOutputStream(saveFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            JOptionPane.showMessageDialog(null, "Database saved to: " + saveFile.getAbsolutePath());
        } else {
            JOptionPane.showMessageDialog(null, "Save operation cancelled.");
        }
    }

    public List<String> getAllRecords() throws IOException {
        List<String> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
        }
        return records;
    }

    public int getNextId() throws IOException {
        int maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int currentId = Integer.parseInt(line.split(",")[0]);
                maxId = Math.max(maxId, currentId);
            }
        }
        return maxId + 1;
    }

    private int getFieldIndex(String field) {
        return switch (field.toLowerCase()) {
            case "id" -> 0;
            case "identifier" -> 1;
            case "name" -> 2;
            case "age" -> 3;
            case "date of birth" -> 4;
            default -> throw new IllegalArgumentException("Invalid field: " + field);
        };
    }

    public boolean isIdentifierExists(int identifier) {
        return indexMap.containsKey(String.valueOf(identifier));
    }
}
