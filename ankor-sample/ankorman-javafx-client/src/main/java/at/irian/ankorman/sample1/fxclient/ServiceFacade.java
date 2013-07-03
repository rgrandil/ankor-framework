package at.irian.ankorman.sample1.fxclient;

import at.irian.ankor.fx.app.ActionCompleteCallback;
import at.irian.ankor.fx.app.AppService;
import at.irian.ankor.ref.Ref;
import at.irian.ankor.ref.RefFactory;

/**
 * @author Thomas Spiegl
 */
public class ServiceFacade {

    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServiceFacade.class);

    private final AppService appService;
    private RefFactory refFactory;

    public ServiceFacade(AppService appService) {
        this.appService = appService;
        this.refFactory = appService.getRefFactory();
    }

    public void initApplication(ActionCompleteCallback cb) {
        Ref rootRef = refFactory.rootRef();
        appService.remoteMethod("service.init()")
                .inContext(rootRef)
                .withResultIn(rootRef)
                .onComplete(cb)
                .execute();
    }

    public void openTab(String tabId, TabType tabType, ActionCompleteCallback cb) {
        Ref tabsRef = refFactory.ref("root.tabs");
        appService.remoteMethod("service.openTab(contextRef, tabId, modelType)")
                .inContext(tabsRef)
                .setParam("tabId", tabId)
                .setParam("modelType", tabType.getModelType())
                        //.withResultIn(tabsRef.append(tabId))
                .onComplete(cb)
                .execute();
    }

    public void saveAnimal(Ref tabRef, ActionCompleteCallback cb) {
        appService.remoteMethod("service.saveAnimal(contextRef)")
                .inContext(tabRef.append("model"))
                .onComplete(cb)
                .execute();
    }

    public void saveAnimals(Ref tabRef, ActionCompleteCallback cb) {
        appService.remoteMethod("service.saveAnimals(context.animals.rows)")
                .inContext(tabRef.append("model"))
                .onComplete(cb)
                .execute();
    }
}
