import javax.swing.*;

public class bank {
    public static void main(String[] args) {

        Model model = new Model();
        View view = new View();
        new Controller(model, view);
        view.setVisible(true);

    }
}