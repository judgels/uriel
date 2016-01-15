package org.iatoki.judgels.uriel.activity;

import org.iatoki.judgels.jophiel.OneEntityActivityKey;
import org.iatoki.judgels.jophiel.ThreeEntityActivityKey;
import org.iatoki.judgels.jophiel.TwoEntityActivityKey;

public final class UrielActivityKeys {

    public static final OneEntityActivityKey LOCK = new OneEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "LOCK";
        }

        @Override
        public String toString() {
            return "lock " + getEntity() + " " + getEntityName() + ".";
        }
    };

    public static final OneEntityActivityKey UNLOCK = new OneEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "UNLOCK";
        }

        @Override
        public String toString() {
            return "unlock " + getEntity() + " " + getEntityName() + ".";
        }
    };

    public static final OneEntityActivityKey REGISTER = new OneEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "REGISTER";
        }

        @Override
        public String toString() {
            return "register to " + getEntity() + " " + getEntityName() + ".";
        }
    };

    public static final OneEntityActivityKey UNREGISTER = new OneEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "UNREGISTER";
        }

        @Override
        public String toString() {
            return "unregister from " + getEntity() + " " + getEntityName() + ".";
        }
    };

    public static final OneEntityActivityKey START = new OneEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "Start";
        }

        @Override
        public String toString() {
            return "start " + getEntity() + " " + getEntityName() + ".";
        }
    };

    public static final TwoEntityActivityKey ANSWER = new TwoEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "ANSWER";
        }

        @Override
        public String toString() {
            return "answer " + getEntity() + " " + getEntityName() + " in " + getRefEntity() + " " + getRefEntityName() + ".";
        }
    };

    public static final ThreeEntityActivityKey SUBMIT = new ThreeEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "SUBMIT";
        }

        @Override
        public String toString() {
            return "submit " + getEntity() + " " + getEntityName() + " for " + getRefEntity() + " " + getRefEntityName() + " in " + getRefRefEntity() + " " + getRefRefEntityName() + ".";
        }
    };

    public static final ThreeEntityActivityKey REGRADE = new ThreeEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "REGRADE";
        }

        @Override
        public String toString() {
            return "regrade " + getEntity() + " " + getEntityName() + " in " + getRefEntity() + " " + getRefEntityName() + " in " + getRefRefEntity() + " " + getRefRefEntityName() + ".";
        }
    };

    public static final TwoEntityActivityKey REFRESH = new TwoEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "REFRESH";
        }

        @Override
        public String toString() {
            return "refresh " + getEntity() + " " + getEntityName() + " in " + getRefEntity() + " " + getRefEntityName() + ".";
        }
    };

    private UrielActivityKeys() {
        // prevent instantiation
    }
}
