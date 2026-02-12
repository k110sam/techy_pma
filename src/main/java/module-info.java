module org.sam.projectmanager.techy_pma {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens org.sam.projectmanager.techy_pma to javafx.fxml;
    exports org.sam.projectmanager.techy_pma;
    exports org.sam.projectmanager.techy_pma.controllers;
    opens org.sam.projectmanager.techy_pma.controllers to javafx.fxml;
}