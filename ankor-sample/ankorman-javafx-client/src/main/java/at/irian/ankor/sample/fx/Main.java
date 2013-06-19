package at.irian.ankor.sample.fx;

import at.irian.ankor.core.application.Application;
import at.irian.ankor.core.application.SimpleApplication;
import at.irian.ankor.core.server.SimpleAnkorServer;
import at.irian.ankor.sample.fx.app.App;
import at.irian.ankor.sample.fx.model.AnimalType;
import at.irian.ankor.sample.fx.view.AnimalSearchTab;
import at.irian.ankor.sample.fx.view.Tab;
import at.irian.ankor.sample.fx.view.ViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends javafx.application.Application {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        setupClientServer();

        primaryStage.setTitle("Ankor FX Sample");
        Pane myPane = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));

        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();
    }

    private void setupClientServer() {

        final Application serverApp = SimpleApplication.withModelType(ViewModel.class)
                .withBean("service", new ServiceBean());
        SimpleAnkorServer server = new SimpleAnkorServer(serverApp, "animalServer");
        server.init();

        Application clientApp = SimpleApplication.withModelType(ViewModel.class);
        SimpleAnkorServer client = new SimpleAnkorServer(clientApp, "animalClient");
        client.init();
        App.setInstance(clientApp);

        server.setRemoteServer(client);
        client.setRemoteServer(server);

        new Thread(new Runnable() {
            public void run() {
                int i = 0;
                //noinspection InfiniteLoopStatement
                for (;;) {
                    try {
                        Thread.sleep((i == 0 ? 5 : 1) * 1000);
                    } catch (InterruptedException e) {
                    }
                    serverApp.getRefFactory().rootRef().sub("tabs.A.model.filter.name").setValue("Eagle " + (++i));
                }
            }
        }).start();

    }

    @SuppressWarnings("UnusedDeclaration")
    public static class ServiceBean {

        public ViewModel init() {
            LOG.info("ServiceBean.init");
            ViewModel model = new ViewModel();
            model.setUserName("Toni Polster");
            return model;
        }

        public Tab createAnimalSearchTab(String tabId) {
            LOG.info("ServiceBean.openTab");
            Tab<AnimalSearchTab> tab = new Tab<AnimalSearchTab>(tabId);
            tab.setModel(new AnimalSearchTab());
            tab.getModel().getFilter().setName("Eagle");
            tab.getModel().getFilter().setType(AnimalType.Bird);
            return tab;
        }
    }
}