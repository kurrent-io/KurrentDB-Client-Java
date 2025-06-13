package io.kurrent.dbclient;
public class ServerVersion {
    private final int major;
    private final int minor;
    private final int patch;

    public ServerVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public boolean isGreaterThan(int major, int minor, int patch) {
        return !isLessThan(major, minor, patch) &&
                !equals(major, minor, patch);
    }

    public boolean equals(int major, int minor, int patch) {
        return this.major == major
                && this.minor == minor
                && this.patch == patch;
    }

    public boolean isLessThan(int major, int minor, int patch) {
        int cmp;
        if ((cmp = Integer.compare(this.major, major)) != 0)
            return cmp < 0;

        if ((cmp = Integer.compare(this.minor, minor)) != 0)
            return cmp < 0;

        if ((cmp = Integer.compare(this.patch, patch)) != 0)
            return cmp < 0;

        return false;
    }

    public boolean isLessOrEqualThan(int major, int minor) {
        int cmp;
        if ((cmp = Integer.compare(this.major, major)) != 0)
            return cmp < 0;

        if ((cmp = Integer.compare(this.minor, minor)) != 0)
            return cmp < 0;

        return true;
    }

    public boolean isGreaterOrEqualThan(int major, int minor) {
        int cmp;
        if ((cmp = Integer.compare(this.major, major)) != 0)
            return cmp > 0;

        if ((cmp = Integer.compare(this.minor, minor)) != 0)
            return cmp > 0;

        return true;
    }

    @Override
    public String toString() {
        return "ServerVersion{" +
                "major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                '}';
    }
}
