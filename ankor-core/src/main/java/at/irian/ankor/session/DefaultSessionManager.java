package at.irian.ankor.session;

import at.irian.ankor.context.ModelContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * todo  timeout functionality
 *
 * @author Manfred Geiler
 */
public class DefaultSessionManager implements SessionManager {
    //private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSessionManager.class);

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();
    private final SessionFactory sessionFactory;

    public DefaultSessionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Session getOrCreate(ModelContext modelContext, RemoteSystem remoteSystem) {
        String sessionId = getSessionIdFrom(modelContext, remoteSystem);
        Session session;
        session = sessionMap.get(sessionId);
        if (session == null) {
            synchronized (sessionMap) {
                session = sessionMap.get(sessionId);
                if (session == null) {
                    session = createAndInitSession(modelContext, remoteSystem);
                    sessionMap.put(sessionId, session);
                }
            }
        }
        return session;
    }

    @Override
    public Collection<Session> getAllFor(ModelContext modelContext) {
        Collection<Session> result = null;
        for (Session session : sessionMap.values()) {
            if (session.getModelContext().equals(modelContext)) {
                if (result == null) {
                    result = Collections.singleton(session);
                } else {
                    if (result.size() == 1) {
                        result = new ArrayList<Session>(result);
                    }
                    result.add(session);
                }
            }
        }
        return result == null ? Collections.<Session>emptyList() : result;
    }

    private String getSessionIdFrom(ModelContext modelContext, RemoteSystem remoteSystem) {
        return remoteSystem.getId() + "_" + modelContext.getId();
    }

    protected Session createAndInitSession(ModelContext modelContext, RemoteSystem remoteSystem) {
        Session session = sessionFactory.create(modelContext, remoteSystem);
        session.init();
        return session;
    }

    @Override
    public void invalidate(Session session) {
        sessionMap.values().remove(session);
        session.close();
    }

}
