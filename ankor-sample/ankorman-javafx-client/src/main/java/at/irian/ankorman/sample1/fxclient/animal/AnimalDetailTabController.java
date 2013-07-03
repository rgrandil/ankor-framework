package at.irian.ankorman.sample1.fxclient.animal;

import at.irian.ankor.fx.app.ActionCompleteCallback;
import at.irian.ankor.ref.Ref;
import at.irian.ankorman.sample1.fxclient.BaseTabController;
import at.irian.ankorman.sample1.model.animal.AnimalFamily;
import at.irian.ankorman.sample1.model.animal.AnimalType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;

import static at.irian.ankor.fx.binding.ValueBindingsBuilder.bindValue;
import static at.irian.ankorman.sample1.fxclient.App.facade;

/**
 * @author Thomas Spiegl
 */
public class AnimalDetailTabController extends BaseTabController {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnimalSearchTabController.class);

    @FXML
    protected TextInputControl name;
    @FXML
    protected Text nameStatus;
    @FXML
    protected ComboBox<AnimalType> type;
    @FXML
    protected ComboBox<AnimalFamily> family;

    public AnimalDetailTabController(String tabId) {
        super(tabId);
    }

    public void initialize() {
        Ref modelRef = getTabRef().append("model");
        Ref animalRef = modelRef.append("animal");
        Ref selItemsRef = modelRef.append("selectItems");

        bindValue(getTabRef().append("name"))
                .toTabText(tab)
                .createWithin(bindingContext);
        bindValue(animalRef.append("name"))
                .toInput(name)
                .withEditable(modelRef.append("editable"))
                .createWithin(bindingContext);
        bindValue(modelRef.append("nameStatus"))
                .toText(nameStatus)
                .createWithin(bindingContext);
        bindValue(animalRef.append("type"))
                .toInput(type)
                .withSelectItems(selItemsRef.append("types"))
                .createWithin(bindingContext);
        bindValue(animalRef.append("family"))
                .toInput(family)
                .withSelectItems(selItemsRef.append("families"))
                .createWithin(bindingContext);

        name.requestFocus();
    }

    @FXML
    protected void save(@SuppressWarnings("UnusedParameters") ActionEvent event) {
        facade().saveAnimal(getTabRef(), new ActionCompleteCallback() {
            public void onComplete() {
            }
        });
    }

}
