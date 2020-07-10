import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class Product {

    private int ID, stockGoal;
    private String name;
    private String[] keywords;
    private BorderPane root;

    public Product(int ID, String name, String[] keywords, int stockGoal) {
        this.ID = ID;
        this.name = name;
        this.keywords = keywords;
        this.stockGoal = stockGoal;

        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = keywords[i].toLowerCase();
        }

        // Create Pane
        root = new BorderPane();
        Label n = new Label(name);
        root.setLeft(n);

        Label i = new Label(String.valueOf(ID));
        root.setRight(i);
    }

    public int getID() {
        return ID;
    }

    public Pane getPane() {
        return root;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + "\t\t" + ID;
    }

    public int getGoal() {
        return stockGoal;
    }

    public boolean keywordsContain(String search) {
        search = search.toLowerCase();
        for (String s : keywords) {
            if (s.startsWith(search)) return true;
        }

        return false;
    }

}
