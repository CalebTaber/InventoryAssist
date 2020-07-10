import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Transaction {

    private File log;
    private int ID;
    private DateTime date;
    private HashMap<Integer, Integer> contents; // This will be a map of product ID numbers and quantities (in withdrawal transactions, the quantity must be negative)

    private String destination;
    // TODO add destination of transaction

    public Transaction(int ID, String destination, File logFile, DateTime date) {
        this.ID = ID;
        this.destination = destination;
        this.date = date;
        log = logFile;

        contents = new HashMap<>();
    }

    public void addItem(int ID, int quantity) {
        contents.put(ID, quantity);
    }

    public void print() {
        System.out.println("CONTENTS OF TRANSACTION " + ID);
        for (int i : contents.keySet()) {
            System.out.println(InventoryAssist.lookup(i).getName() + " X " + contents.get(i));
        }
    }

    public HashMap<Integer, Integer> getContents() {
        return contents;
    }

    public Pane getPane() {
        VBox root = new VBox();
        root.getChildren().add(new Label("#" + ID + " to " + destination + ", completed " + date.toString()));

        for (int i : contents.keySet()) {
            BorderPane bp = new BorderPane();

            bp.setLeft(new Label(InventoryAssist.lookup(i).getName()));
            bp.setRight(new Label(String.valueOf(contents.get(i))));
            root.getChildren().add(bp);
        }

        return root;
    }

    public void log() {
        try {
            if (!log.exists()) log.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));
            writer.write(ID + "-" + destination + "@" + date.toString() + "{\n");
            writer.flush();
            for (int i : contents.keySet()) {
                writer.write(i + "," + contents.get(i) + "\n");
                writer.flush();
            }
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
