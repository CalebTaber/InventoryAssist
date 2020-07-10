import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.io.*;
import java.util.HashMap;

public class Location {

    private File log; // The csv file that corresponds to this location
    private String name;
    private HashMap<Product, Integer> contents; // A map of products to quantities
    private boolean showGoals;

    public Location(String name, File log, boolean showGoals) {
        this.name = name;
        this.log = log;
        this.showGoals = showGoals;

        contents = new HashMap<>();

        try {
            buildMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transact(Transaction t, boolean negate) throws Exception {
        for (int id : t.getContents().keySet()) {
            Product p = InventoryAssist.lookup(id);

            if (negate) {
                if (contents.containsKey(p)) contents.put(p, contents.get(p) - t.getContents().get(id));
                else throw new Exception("Can't expend more than is in current stock"); // TODO replace with more graceful error
            } else {
                if (contents.containsKey(p)) contents.put(p, contents.get(p) + t.getContents().get(id));
                else contents.put(p, t.getContents().get(id));
            }
        }

        logContents();
    }

    private void buildMap() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(log));

        while (reader.ready()) {
            String line = reader.readLine();
            if (line.isBlank()) continue;
            String[] arr = line.split(",");
            contents.put(InventoryAssist.lookup(Integer.parseInt(arr[0])), Integer.parseInt(arr[1]));
        }

        reader.close();
    }

    public Pane getPane(int width, int height) {
        FlowPane root = new FlowPane();

        ObservableList<Pane> productsInStock = FXCollections.observableArrayList();

        for (Product p : contents.keySet()) {

            if (showGoals) {
                // TODO add pane for every product, showing its progress with respect to the stockGoal
                VBox box = new VBox(20);

                BorderPane top = new BorderPane();
                top.setLeft(new Label(p.getName()));
                top.setRight(new Label(String.valueOf(p.getID())));
                box.getChildren().add(top);

                BorderPane bottom = new BorderPane();
                ProgressBar pb = new ProgressBar(contents.get(p) / (double) p.getGoal());
                pb.setPrefWidth(width * 0.8);
                bottom.setLeft(pb);
                bottom.setRight(new Label(contents.get(p) + "/" + p.getGoal()));
                box.getChildren().add(bottom);

                productsInStock.add(box);
            } else {
                BorderPane bp = new BorderPane();
                bp.setLeft(new Label(p.getName()));

                HBox quantityPane = new HBox(width / 3);
                quantityPane.getChildren().addAll(new Label(String.valueOf(p.getID())), new Label(String.valueOf(contents.get(p))));
                bp.setRight(quantityPane);

                productsInStock.add(bp);
            }
        }

        ListView<Pane> productsList = new ListView<>(productsInStock);
        productsList.setPrefSize(width, height);
        root.getChildren().add(productsList);

        return root;
    }

    public void print() {
        System.out.println("\n" + name + " contents");
        for (Product p : contents.keySet()) {
            System.out.println(p.getName() + " X " + contents.get(p));
        }
    }

    public void logContents() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(log));

            for (Product p : contents.keySet()) {
                writer.write(p.getID() + "," + contents.get(p) + "\n");
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
