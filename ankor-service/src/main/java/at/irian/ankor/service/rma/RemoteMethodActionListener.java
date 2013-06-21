package at.irian.ankor.service.rma;

import at.irian.ankor.action.Action;
import at.irian.ankor.application.BeanResolver;
import at.irian.ankor.el.BeanResolverELContext;
import at.irian.ankor.event.ActionListener;
import at.irian.ankor.ref.Ref;
import at.irian.ankor.ref.RefFactory;
import at.irian.ankor.ref.el.ELRefContext;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import java.util.Map;

/**
* @author MGeiler (Manfred Geiler)
*/
public class RemoteMethodActionListener implements ActionListener {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoteMethodActionListener.class);

    private final ExpressionFactory expressionFactory;
    private final RefFactory refFactory;

    public RemoteMethodActionListener(ExpressionFactory expressionFactory, RefFactory refFactory) {
        this.expressionFactory = expressionFactory;
        this.refFactory = refFactory;
    }

    @Override
    public void processAction(Ref modelContext, Action action) {
        if (action instanceof RemoteMethodAction) {
            processMethodAction(modelContext, (RemoteMethodAction) action);
        }
    }

    private void processMethodAction(Ref modelContext, RemoteMethodAction action) {

        Object result;
        try {
            result = executeMethod(modelContext, action.getMethodExpression(), action.getParams());
        } catch (Exception e) {
            handleError(modelContext, action, e);
            return;
        }

        handleResult(modelContext, action.getResultPath(), result);

        if (action.isAutoRefreshActionContext()) {
            modelContext.setValue(modelContext.getValue());
        }

        fireCompleteAction(modelContext, action.getCompleteAction());
    }

    private void handleResult(Ref modelContext, String resultPath, Object result) {
        if (resultPath != null) {
            ELRefContext refContext = (ELRefContext) modelContext.refContext();
            Ref resultRef = refFactory.ref(resultPath, refContext.withModelContext(modelContext));
            resultRef.setValue(result);
        }
    }

    private void fireCompleteAction(Ref actionContext, Action completeAction) {
        if (completeAction != null) {
            actionContext.fire(completeAction);
        }
    }

    private void handleError(Ref actionContext, RemoteMethodAction action, Exception e) {
        Action errorAction = action.getErrorAction();
        if (errorAction != null) {
            actionContext.fire(errorAction);
        } else {
            LOG.error("Error executing " + action, e);
        }
    }

    private Object executeMethod(Ref modelContext, String methodExpression, final Map<String, Object> params) {
        ELRefContext refContext = (ELRefContext) modelContext.refContext();
        ELContext modelContextELContext = refContext.withModelContext(modelContext).getELContext();

        ELContext executionELContext;
        if (params != null) {
            executionELContext = new BeanResolverELContext(modelContextELContext, new BeanResolver() {
                @Override
                public Object resolveByName(String beanName) {
                    return params.get(beanName);
                }
            });
        } else {
            executionELContext = modelContextELContext;
        }

        ValueExpression ve = expressionFactory.createValueExpression(executionELContext,
                                                                     "#{" + methodExpression + "}",
                                                                     Object.class);
        return ve.getValue(executionELContext);
    }

}
