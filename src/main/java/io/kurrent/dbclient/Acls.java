package io.kurrent.dbclient;

/**
 * Access control list (ACL) utility class.
 */
public final class Acls {

    /**
     * Returns a default stream ACL.
     * @see StreamAcl
     * @return acl
     */
    public static StreamAcl newStreamAcl() {
        return new StreamAcl();
    }

    /**
     * Returns a default user ACL.
     * @see UserStreamAcl
     * @return acl
     */
    public static Acl newUserStreamAcl() {
        return UserStreamAcl.getInstance();
    }

    /**
     * Returns a default system ACL.
     * @see SystemStreamAcl
     * @return acl
     */
    public static Acl newSystemStreamAcl() {
        return SystemStreamAcl.getInstance();
    }
}
