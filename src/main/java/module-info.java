module com.esperanza.hopecare {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires java.desktop;
    
    requires java.sql;

    opens com.esperanza.hopecare to javafx.fxml;
    opens com.esperanza.hopecare.main to javafx.fxml;

    opens com.esperanza.hopecare.modules.dashboard.ui to javafx.fxml;
    opens com.esperanza.hopecare.modules.Auth.view to javafx.fxml;

    opens com.esperanza.hopecare.modules.dashboard.model to javafx.base;
    opens com.esperanza.hopecare.common.model to javafx.base;
    opens com.esperanza.hopecare.modules.Auth.model to javafx.base;


    exports com.esperanza.hopecare;
    exports com.esperanza.hopecare.main;
}
