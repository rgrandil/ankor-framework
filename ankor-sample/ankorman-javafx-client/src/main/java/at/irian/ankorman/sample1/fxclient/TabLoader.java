package at.irian.ankorman.sample1.fxclient;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Thomas Spiegl
 */
public class TabLoader {

    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TabLoader.class);

    private final String tabId;
    private final TabType tabType;

    public TabLoader(TabType tabType, String tabId) {
        if (tabType == null) {
            throw new IllegalStateException("tabType is null");
        }
        this.tabType = tabType;
        this.tabId = tabId;
    }

    public void showTab(final TabPane tabPane) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Tab tab = (Tab) loader().load();
                    tabPane.getTabs().add(tab);
                    tabPane.getSelectionModel().select(tab);
                } catch (IOException e) {
                    throw new IllegalStateException("cannot load animal_search_tab.fxml", e);
                }
            }
        });
    }

    private FXMLLoader loader() {
        final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(tabType.getFxmlResource()));
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> aClass) {
                try {
                    return aClass.getConstructor(String.class).newInstance(tabId);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Cannot load tab for " + aClass.getName() + ": Constructor(String tabId) not found");
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("InvocationTargetException for " + aClass.getName());
                } catch (InstantiationException e) {
                    throw new IllegalStateException("InstantiationException for " + aClass.getName());
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("IllegalAccessException for " + aClass.getName());
                }
            }
        });
        return loader;
    }
}
