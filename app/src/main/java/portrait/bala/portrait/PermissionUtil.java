package portrait.bala.portrait;


/**
 * Created by zjw on 2023/12/26.
 */
public class PermissionUtil {

    public static final String READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    public static boolean isSdkInt33() {
        return getSdkInt() >= 33;
    }

    public static int getSdkInt() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static String getReadImgPermission() {
        String picPermission = PermissionUtil.READ_EXTERNAL_STORAGE;
        if (PermissionUtil.isSdkInt33()) {
            picPermission = PermissionUtil.READ_MEDIA_IMAGES;
        }
        return picPermission;
    }

}
