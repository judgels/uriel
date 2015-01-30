package org.iatoki.judgels.uriel;

import org.iatoki.judgels.uriel.controllers.ApplicationController;
import org.iatoki.judgels.uriel.controllers.ContestController;
import org.iatoki.judgels.uriel.controllers.UserRoleController;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestAnnouncementHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestClarificationHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestContestantHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.ContestProblemHibernateDao;
import org.iatoki.judgels.uriel.models.daos.hibernate.UserRoleHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestContestantDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestProblemDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import play.Application;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

public final class Global extends org.iatoki.judgels.commons.Global {

    private Map<Class, Controller> cache;

    @Override
    public void onStart(Application application) {
        cache = new HashMap<>();

        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        if (!cache.containsKey(controllerClass)) {
            if (controllerClass.equals(ApplicationController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ApplicationController applicationController = new ApplicationController(userRoleService);
                cache.put(ApplicationController.class, applicationController);
            } else if (controllerClass.equals(ContestController.class)) {
                ContestDao contestDao = new ContestHibernateDao();
                ContestAnnouncementDao contestAnnouncementDao = new ContestAnnouncementHibernateDao();
                ContestContestantDao contestContestantDao = new ContestContestantHibernateDao();
                ContestClarificationDao contestClarificationDao = new ContestClarificationHibernateDao();
                ContestProblemDao contestProblemDao = new ContestProblemHibernateDao();
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                ContestService contestService = new ContestServiceImpl(contestDao, contestAnnouncementDao, contestProblemDao, contestClarificationDao, contestContestantDao, userRoleDao);
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                ContestController contestController = new ContestController(contestService, userRoleService);
                cache.put(ContestController.class, contestController);
            } else if (controllerClass.equals(UserRoleController.class)) {
                UserRoleDao userRoleDao = new UserRoleHibernateDao();
                UserRoleService userRoleService = new UserRoleServiceImpl(userRoleDao);

                UserRoleController userRoleController = new UserRoleController(userRoleService);
                cache.put(UserRoleController.class, userRoleController);
            }
        }
        return controllerClass.cast(cache.get(controllerClass));
    }
}
