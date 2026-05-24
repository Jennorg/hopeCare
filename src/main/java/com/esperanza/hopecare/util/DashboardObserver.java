package com.esperanza.hopecare.util;

import com.esperanza.hopecare.dao.DashboardDAO;
import com.esperanza.hopecare.util.EventBus;
import com.esperanza.hopecare.util.NuevaCitaEvent;
import javax.swing.SwingWorker;
import java.util.List;

public class DashboardObserver {
    private com.esperanza.hopecare.util.DashboardView view;
    private DashboardDAO dao;

    public DashboardObserver(com.esperanza.hopecare.util.DashboardView view) {
        this.view = view;
        this.dao = new DashboardDAO();
        
        EventBus.getInstance().register(NuevaCitaEvent.class, this::onNuevaCita);
    }
    
    private void onNuevaCita(NuevaCitaEvent event) {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return dao.obtenerCitasDelDia();
            }
            @Override
            protected void done() {
                try {
                    int citasHoy = get();
                    view.actualizarCitasDelDia(citasHoy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
