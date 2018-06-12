package notifications.test.com.aadhardemo.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Venkatesh on 23-05-2017
 */

public class NetworkHelper {
    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    public static boolean isConnected(Context activity) {
        mContext = activity;
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
