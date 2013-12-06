package at.irian.ankor.ref.el;

import at.irian.ankor.context.ModelContext;
import at.irian.ankor.delay.Scheduler;
import at.irian.ankor.el.ELSupport;
import at.irian.ankor.event.EventListeners;
import at.irian.ankor.path.PathSyntax;
import at.irian.ankor.path.el.SimpleELPathSyntax;
import at.irian.ankor.ref.RefContext;
import at.irian.ankor.ref.RefFactory;
import at.irian.ankor.ref.impl.RefContextImplementor;
import at.irian.ankor.viewmodel.ViewModelPostProcessor;
import at.irian.ankor.viewmodel.metadata.BeanMetadataProvider;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import java.util.List;

/**
 * @author Manfred Geiler
 */
public class ELRefContext implements RefContext, RefContextImplementor {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ELRefContext.class);

    private final ELSupport elSupport;
    private final ModelContext modelContext;
    private final List<ViewModelPostProcessor> viewModelPostProcessors;
    private final Scheduler scheduler;
    private final RefFactory refFactory;
    private final BeanMetadataProvider metadataProvider;

    protected ELRefContext(ELSupport elSupport,
                           ModelContext modelContext,
                           List<ViewModelPostProcessor> viewModelPostProcessors,
                           Scheduler scheduler,
                           RefFactory refFactory, BeanMetadataProvider metadataProvider) {
        this.elSupport = elSupport;
        this.modelContext = modelContext;
        this.viewModelPostProcessors = viewModelPostProcessors;
        this.scheduler = scheduler;
        this.refFactory = refFactory;
        this.metadataProvider = metadataProvider;
    }

    protected static ELRefContext create(ELSupport elSupport,
                                         ModelContext modelContext,
                                         List<ViewModelPostProcessor> viewModelPostProcessors,
                                         Scheduler scheduler,
                                         BeanMetadataProvider metadataProvider) {
        ELRefFactory refFactory = new ELRefFactory();
        ELRefContext refContext = new ELRefContext(elSupport,
                                                   modelContext,
                                                   viewModelPostProcessors,
                                                   scheduler,
                                                   refFactory,
                                                   metadataProvider);
        refFactory.setRefContext(refContext); // bi-directional relation - not nice but no idea by now how to make it nice...  ;-)
        return refContext;
    }

    @Override
    public RefFactory refFactory() {
        return refFactory;
    }

    ExpressionFactory getExpressionFactory() {
        return elSupport.getExpressionFactory();
    }

    ELContext createELContext() {
        return elSupport.getELContextFor(refFactory());
    }

    @Override
    public EventListeners eventListeners() {
        return modelContext.getEventListeners();
    }

    @Override
    public PathSyntax pathSyntax() {
        return SimpleELPathSyntax.getInstance();
    }

    @Override
    public List<ViewModelPostProcessor> viewModelPostProcessors() {
        return viewModelPostProcessors;
    }

    @Override
    public ModelContext modelContext() {
        return modelContext;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public BeanMetadataProvider metadataProvider() {
        return metadataProvider;
    }
}
