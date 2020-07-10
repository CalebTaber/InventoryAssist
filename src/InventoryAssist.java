import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class InventoryAssist extends Application {

    private final String CURRDIR = System.getProperty("user.dir") + System.getProperty("file.separator");
    private final File transactionLog = new File(CURRDIR + "transactions.txt");

    private static LinkedHashMap<Integer, Product> products;
    private LinkedHashMap<Product, Pane> productPaneMap;
    private LinkedHashMap<Pane, Product> paneProductMap;

    private ArrayList<Transaction> transactions;
    private static ArrayList<TransactionPane> transactionContents;
    private Location inventory, expended;

    private static ObservableList<Pane> logList, transferList;

    @Override
    public void init() throws Exception {
        // Set up products map
        products = new LinkedHashMap<>();
        BufferedReader productReader = new BufferedReader(new FileReader(new File(CURRDIR + "productIndex.csv")));

        productPaneMap = new LinkedHashMap<>();
        paneProductMap = new LinkedHashMap<>();
        while (productReader.ready()) {
            String[] info = productReader.readLine().split(",");
            String[] one = info[1].split(" ");
            String[] two = info[2].split(" ");
            String[] keywords = new String[one.length + two.length + 1];
            System.arraycopy(one, 0, keywords, 0, one.length);
            System.arraycopy(two, 0, keywords, one.length, two.length);
            keywords[keywords.length - 1] = info[0];

            Product p = new Product(Integer.parseInt(info[0]), info[1], keywords, Integer.parseInt(info[3]));

            products.put(p.getID(), p);
            productPaneMap.put(p, p.getPane());
            paneProductMap.put(p.getPane(), p);
        }

        productReader.close();

        transactions = new ArrayList<>();
        // Read previous transactions
        BufferedReader transactionReader = new BufferedReader(new FileReader(transactionLog));
        while (transactionReader.ready()) {
            String s = transactionReader.readLine();
            if (s.equals("}")) {

            }
            else if (s.isBlank()) continue;
            else if (s.contains("@")) {
                String[] split = s.split("@");
                String[] idDest = split[0].split("-");
                Transaction t = new Transaction(Integer.parseInt(idDest[0]), idDest[1], transactionLog, new DateTime(split[1].substring(0, split[1].length() - 1)));
                transactions.add(t);
            }
            else {
                String[] split = s.split(",");
                transactions.get(transactions.size() - 1).addItem(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            }
        }

        // Search list
        logList = FXCollections.observableArrayList(productPaneMap.values());

        // List of things to be transferred
        transactionContents = new ArrayList<>();
        transferList = FXCollections.observableArrayList();

        // Define locations
        inventory = new Location("Inventory", new File(CURRDIR + "inventory.csv"), true);
        expended = new Location("Expended", new File(CURRDIR + "expended.csv"), false);
    }

    @Override
    public void start(Stage stage) throws Exception {
        TabPane root = new TabPane();
        root.setPrefSize(650, 750);
        Scene scene = new Scene(root);

        // Create transfer tab GUI
        VBox transactionBox = new VBox(0);
        HBox controls = new HBox(5);
        controls.setPadding(new Insets(5, 0, 5, 0));
        controls.setAlignment(Pos.CENTER);

        // Controls
        Label moveTo = new Label("Move to...");

        ComboBox<String> locations = new ComboBox<>();
        locations.getItems().addAll("Current Stock", "Expended");

        Button transfer = new Button("Transfer");
        transfer.setOnAction(e -> {
            String selectedLocation = locations.getSelectionModel().getSelectedItem();
            DateTimeFormatter date = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

            Transaction transaction = new Transaction(transactions.size(), selectedLocation, new File(CURRDIR + "transactions.txt"), new DateTime(date.format(LocalDateTime.now())));
            for (TransactionPane tp : transactionContents) {
                try { // TODO replace with a more graceful error message
                    transaction.addItem(tp.getProduct().getID(), tp.getQuantity());
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
            transaction.print();

            try {
                if (selectedLocation.equals("Current Stock"))
                    inventory.transact(transaction, false);
                else if (selectedLocation.equals("Expended")) {
                    expended.transact(transaction, false);
                    inventory.transact(transaction, true);
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
            inventory.print();
            expended.print();

            transaction.log();
            transactions.add(transaction);
        });

        controls.getChildren().addAll(moveTo, locations, transfer);
        transactionBox.getChildren().add(controls);

        // Things to be transferred
        ListView<Pane> transferView = new ListView<>(transferList);
        transactionBox.getChildren().add(transferView);

        // Search bar
        TextField search = new TextField("Search...");
        search.setPrefWidth(root.getPrefWidth());
        search.setOnMouseClicked(e -> {
            if (search.getText().equals("Search...")) search.setText("");
        });
        search.setOnKeyPressed(e -> {
            if (search.getText().equals("Search...")) search.setText("");
        });
        search.setOnKeyTyped(e -> {
            ArrayList<Pane> panes = new ArrayList<>();
            for (Product p : productPaneMap.keySet()) {
                if (p.keywordsContain(search.getText())) panes.add(productPaneMap.get(p));
            }

            logList.clear();
            logList.addAll(panes);

            if (search.getText().isBlank()) search.setText("Search...");
        });
        transactionBox.getChildren().add(search);

        // Items to choose from
        ListView<Pane> log = new ListView<>(logList);
        log.setPrefWidth(root.getPrefWidth());
        log.setPrefHeight(root.getPrefHeight() / 2);
        log.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 2) {
                TransactionPane selected = new TransactionPane(paneProductMap.get(log.getSelectionModel().getSelectedItem()));
                transactionContents.add(selected);
                transferList.add(selected.getPane());
                // TODO disallow adding a product twice
            }
        });
        transactionBox.getChildren().add(log);

        Tab transferTab = new Tab("Transfer");
        transferTab.setContent(transactionBox);
        transferTab.setClosable(false);

        Tab inventoryTab = new Tab("Current Stock", inventory.getPane((int) root.getPrefWidth(), (int) root.getPrefHeight()));
        inventoryTab.setOnSelectionChanged(e -> {
            inventoryTab.setContent(inventory.getPane((int) root.getPrefWidth(), (int) root.getPrefHeight()));
        });
        inventoryTab.setClosable(false);

        Tab expendedTab = new Tab("Expended", expended.getPane((int) root.getPrefWidth(), (int) root.getPrefHeight()));
        expendedTab.setOnSelectionChanged(e -> {
            expendedTab.setContent(expended.getPane((int) root.getPrefWidth(), (int) root.getPrefHeight()));
        });
        expendedTab.setClosable(false);

        Tab transactionTab = new Tab("Transactions", new FlowPane());
        transactionTab.setOnSelectionChanged(e -> {
            ObservableList<Pane> transList = FXCollections.observableArrayList();

            for (Transaction t : transactions) {
                transList.add(t.getPane());
            }

            ListView<Pane> transListView = new ListView<>(transList);
            transactionTab.setContent(transListView);
        });
        transactionTab.setClosable(false);

        root.getTabs().addAll(transferTab, inventoryTab, expendedTab, transactionTab);

        stage.setScene(scene);
        stage.setTitle("Inventory Assist");
        stage.show();
    }

    public void stop() throws Exception {
        inventory.logContents();
        expended.logContents();
    }

    public static Product lookup(int ID) {
        return products.getOrDefault(ID, null);
    }

    public static void remove(Pane p, TransactionPane tp) {
        transferList.remove(p);
        transactionContents.remove(tp);
    }

}
