package at.irian.ankor.service.test.animal;

import at.irian.ankor.action.Action;
import at.irian.ankor.action.ActionListener;
import at.irian.ankor.ref.Ref;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MGeiler (Manfred Geiler)
 */
public class AnimalSearchActionListener implements ActionListener {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnimalSearchActionListener.class);

    @Override
    public void processAction(Ref actionContext, Action action) {
        if (action.name().equals("search")) {
            Object container = actionContext.getValue();

            if (container instanceof AnimalSearchContainer) {
                LOG.info("Animal search action");
                AnimalSearchContainer animalSearchContainer = (AnimalSearchContainer) container;

                if (animalSearchContainer.getFilter().getName().equals("A*")) {

                    List<Animal> animals = new ArrayList<Animal>();
                    animals.add(new Animal("Adler", AnimalType.Bird));
                    animals.add(new Animal("Amsel", AnimalType.Bird));

                    actionContext.sub("resultList").setValue(animals);
                }
            }

        }
    }
}
