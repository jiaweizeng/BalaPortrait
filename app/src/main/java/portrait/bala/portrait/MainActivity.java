package portrait.bala.portrait;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends ComponentActivity {

    //改变头像的标记位
    private ImageView headImage = null;

    private final int PERMISSION_CAMERA = 0;//读和相机权限
    private final int PERMISSION_READ = 1;//读取权限

    private final String picPermission = PermissionUtil.getReadImgPermission();
    private Uri fileUri;
    private boolean isToCrop = true;

    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private ActivityResultLauncher<Intent> albumLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        headImage = (ImageView) findViewById(R.id.imageView);
        Button buttonLocal = (Button) findViewById(R.id.buttonLocal);
        buttonLocal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkPicPermission();
            }
        });

        Button buttonCamera = (Button) findViewById(R.id.buttonCamera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkCameraPermission();//检查是否有权限
            }
        });
        cropLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    setImageToHeadView(result.getData().getData());
                });
        albumLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    try {
                        if (isToCrop) {
                            cropRawPhoto(result.getData().getData());
                        } else {
                            setImageToHeadView(result.getData().getData());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
        takePhotoLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    try {
                        if (isToCrop) {
                            cropRawPhoto(fileUri);
                        } else {
                            setImageToHeadView(fileUri);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    // 从本地相册选取图片作为头像
    private void choseHeadImageFromGallery() {
        Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
        intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
        albumLauncher.launch(intentFromGallery);
    }

    // 启动手机相机拍摄照片作为头像
    private void choseHeadImageFromCameraCapture() {
        File file = buildTemporaryFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //如果是7.0以上，使用FileProvider，否则会报错
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileProvider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); //设置拍照后图片保存的位置
        }
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //设置图片保存的格式
        takePhotoLauncher.launch(intent);
    }

    private void checkCameraPermission() {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CAMERA);
        } else {
            choseHeadImageFromCameraCapture();
        }
    }

    private void checkPicPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, picPermission);
        if (permission == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {picPermission};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_READ);
        } else {
            choseHeadImageFromGallery();
        }
    }

    //权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choseHeadImageFromCameraCapture();
                }
                break;
            case PERMISSION_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choseHeadImageFromGallery();
                }
                break;
        }

    }

    /**
     * 裁剪原始的图片
     */
    public void cropRawPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);  //X方向上的比例
        intent.putExtra("aspectY", 1);  //Y方向上的比例
        intent.putExtra("outputX", 500); //裁剪区的宽
        intent.putExtra("outputY", 500);//裁剪区的高
        intent.putExtra("scale ", true); //是否保留比例
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.setDataAndType(uri, "image/*");

        // 7.0 使用 FileProvider 并赋予临时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        File temporaryFile = buildTemporaryFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createCropImageFile();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri = Uri.fromFile(temporaryFile));      //设置输出
        }
        cropLauncher.launch(intent);
    }

    public File buildTemporaryFile() {
        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES),
                    System.currentTimeMillis() + ".jpg");
        } else {
            file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        }
        return file;
    }

    public void createCropImageFile() {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            String fileName = "IMG_" + currentTimeMillis + "_CROP.jpg";
            File imgFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + fileName);
            // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imgFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取保存裁剪之后的图片数据，并设置头像部分的View
     */
    private void setImageToHeadView(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            headImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
