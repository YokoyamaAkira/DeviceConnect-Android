package org.deviceconnect.android.profile.api;


import android.content.Intent;

import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectApiSpecConstants;

public abstract class DConnectApi implements DConnectApiSpecConstants {

    private DConnectApiSpec mApiSpec;

    public String getInterface() {
        return null;
    }

    public String getAttribute() {
        return null;
    }

    public abstract Method getMethod();

    public DConnectApiSpec getApiSpec() {
        return mApiSpec;
    }

    public void setApiSpec(final DConnectApiSpec apiSpec) {
        mApiSpec = apiSpec;
    }

    public abstract boolean onRequest(final Intent request, final Intent response);

}
