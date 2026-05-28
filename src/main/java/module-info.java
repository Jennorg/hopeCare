module com.esperanza.hopecare {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;

    exports com.esperanza.hopecare;
    exports com.esperanza.hopecare.controller;
    exports com.esperanza.hopecare.model;
    exports com.esperanza.hopecare.dao;
    exports com.esperanza.hopecare.service;
    exports com.esperanza.hopecare.util;

    opens com.esperanza.hopecare.controller to javafx.fxml;
    opens com.esperanza.hopecare.model to javafx.base;
}
