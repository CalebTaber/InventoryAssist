import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class TransactionPane {

    private BorderPane root;
    private Product product;
    private TextField quantity;

    public TransactionPane(Product p) {
        product = p;

        root = new BorderPane();

        HBox left = new HBox(10);

        Button remove = new Button("X");
        remove.setFocusTraversable(false);
        remove.setOnAction(e -> {
            InventoryAssist.remove(root, this);
        });

        left.getChildren().addAll(remove, new Label(product.getName()));
        root.setLeft(left);

        HBox right = new HBox(180);

        Label id = new Label(String.valueOf(product.getID()));
        right.getChildren().add(id);

        quantity = new TextField();
        quantity.setPrefColumnCount(4);
        right.getChildren().add(quantity);

        root.setRight(right);
    }

    public int getQuantity() throws Exception {
        String s = quantity.getText();
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) throw new Exception("Quantity entered '" + s + "' is not a valid number");
        }

        return Integer.parseInt(s);
    }

    public Product getProduct() {
        return product;
    }

    public Pane getPane() {
        return root;
    }

}
