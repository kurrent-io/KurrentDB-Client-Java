package io.kurrent.dbclient;

/**
 * Admin stream access control list (ACL).
 */
class SystemStreamAcl implements Acl {
    public static final String ACL_NAME = "$systemStreamAcl";
    private static final SystemStreamAcl SINGLETON = new SystemStreamAcl();

    private SystemStreamAcl() {}

    static SystemStreamAcl deserialize(String source) {
        SystemStreamAcl acl = null;

        if (source.equals(ACL_NAME))
            acl = SINGLETON;

        return acl;
    }

    static SystemStreamAcl getInstance() {
        return SINGLETON;
    }

    @Override
    public int hashCode() {
        return ACL_NAME.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof  SystemStreamAcl);
    }

    @Override
    public String toString() {
        return ACL_NAME;
    }
}
