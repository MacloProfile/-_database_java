public class Main {
    public static void main(String[] args) {
        try {
            FileDatabase database = new FileDatabase("database.txt");
            GuiManager guiManager = new GuiManager(database);
            guiManager.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
