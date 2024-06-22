module com.upo.projekttt {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.upo.projekttt to javafx.fxml;
    exports com.upo.projekttt;
}