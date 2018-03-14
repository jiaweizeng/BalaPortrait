package portrait.bala.portrait;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by zjw on 2018/3/8 0008.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        if (Build.VERSION.SDK_INT >= 18){

            builder.detectFileUriExposure();
        }
        StrictMode.setVmPolicy(builder.build());
       /* StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
    }
}
