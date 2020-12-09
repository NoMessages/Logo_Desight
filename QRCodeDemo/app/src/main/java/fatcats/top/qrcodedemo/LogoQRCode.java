package fatcats.top.qrcodedemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

public class LogoQRCode extends AppCompatActivity {


    @BindView(R.id.qr_content)
    EditText qrContent;
    @BindView(R.id.logo_img)
    ImageView logoImg;
    @BindView(R.id.gender_btn)
    Button genderBtn;
    @BindView(R.id.gen_qr_img)
    ImageView genQrImg;

    public static final int TAKE_PHOTO = 1;//拍照

    public static final int CHOOSE_PHOTO = 2;//从相册选择图片
    //二维码宽度
    public static final int QR_CODE_WIDTH = 500;
    //logo的尺寸不能高于二维码的20%.大于可能会导致二维码失效
    public static final int LOGO_WIDTH_MAX = QR_CODE_WIDTH / 5;
    //logo的尺寸不能小于二维码的10%，否则不搭
    public static final int LOGO_WIDTH_MIN = QR_CODE_WIDTH / 10;
    //定义黑色
    private static final int BLACK = 0xFF000000;
    //定义白色
    private static final int WHITE = 0xFFFFFFFF;

    private Bitmap genBitMap; //生成的二维码

    private Bitmap logoBitMap; //绘制的logo二维码

    private Uri imageUri; //图片地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_q_r_code_demo);
        genQrImg = findViewById(R.id.gen_qr_img);
        logoImg = findViewById(R.id.logo_img);
        qrContent = findViewById(R.id.qr_content);
        genderBtn = findViewById(R.id.gender_btn);

//        logoBitMap = createBitmap(((BitmapDrawable) getDrawable(R.drawable.bg)).getBitmap()
//                , ((BitmapDrawable) getDrawable(R.drawable.logo)).getBitmap());
//        try {
//            genBitMap  = createQRBitmap(logoBitMap, "https://github.com/NoMessages/QRCode_Logo.git");
//            Glide.with(LogoQRCode.this).load(genBitMap).into(genQrImg);
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }

        logoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseDialog();
            }
        });

        genQrImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                imgChooseDialog();
                return false;
            }
        });
        genderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(qrContent.getText().toString().trim())) {
                    Toast.makeText(LogoQRCode.this, "请输入二维码内容哦~", Toast.LENGTH_SHORT).show();
                }else{
                    if(logoBitMap == null){
                        logoBitMap = createBitmap(((BitmapDrawable) getDrawable(R.drawable.bg)).getBitmap()
                                , ((BitmapDrawable) getDrawable(R.drawable.logo)).getBitmap());
                        logoImg.setImageBitmap(logoBitMap);
                    }
                    try {
                        genBitMap  = createQRBitmap(logoBitMap, qrContent.getText().toString().trim());
                        Glide.with(LogoQRCode.this).load(genBitMap).into(genQrImg);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /*
           生成二维码
    */

    public Bitmap createQRBitmap(Bitmap logoBitmap, String content) throws WriterException {

        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();
        //规整化图片
        int logoHaleWidth = logoWidth >= QR_CODE_WIDTH ? LOGO_WIDTH_MIN : LOGO_WIDTH_MAX;
        int logoHaleHeight = logoHeight >= QR_CODE_WIDTH ? LOGO_WIDTH_MIN : LOGO_WIDTH_MAX;
        // 将logo图片按martix设置的信息缩放
        Matrix matrix = new Matrix();
        float sx = (float) logoHaleWidth / logoWidth;
        float sy = (float) logoHaleHeight / logoHeight;
        matrix.setScale(sx, sy);
        //重新绘制Bitmap
        Bitmap matrixLogoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0, logoWidth, logoHeight, matrix, false);

        int mtLogoWidth = matrixLogoBitmap.getWidth();
        int mtLogoHidth = matrixLogoBitmap.getHeight();

        Map<EncodeHintType, Object> hintTypeStringMap = new HashMap<>();
        hintTypeStringMap.put(EncodeHintType.MARGIN, 2);//外边距
        hintTypeStringMap.put(EncodeHintType.CHARACTER_SET, "utf8");
        hintTypeStringMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//设置最高错误级别
        hintTypeStringMap.put(EncodeHintType.MAX_SIZE, LOGO_WIDTH_MAX);
        hintTypeStringMap.put(EncodeHintType.MIN_SIZE, LOGO_WIDTH_MIN);


        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH
                , QR_CODE_WIDTH, hintTypeStringMap);

        //绘制二维码数组
        int[] arr = new int[bitMatrix.getWidth() * bitMatrix.getHeight()];
            /*

                    二维码位置
                    Left: 屏幕宽度的一半 - 二维码宽度的一半
                    Right: 屏幕宽度的一半 + 二维码宽度的一半
                    logo长度： Right - Left = logoSize

             */
        for (int i = 0; i < bitMatrix.getHeight(); i++) {
            for (int j = 0; j < bitMatrix.getWidth(); j++) {
                /*
                            当坐标像素点恰好处于logo位置时，绘制logo  详情看图解
                 */
                if (j > bitMatrix.getWidth() / 2 - mtLogoWidth / 2 && j < bitMatrix.getWidth() / 2 + mtLogoWidth / 2
                        && i > bitMatrix.getHeight() / 2 - mtLogoHidth / 2 && i < bitMatrix.getHeight() / 2 + mtLogoHidth / 2) {
                    arr[i * bitMatrix.getWidth() + j] = matrixLogoBitmap.getPixel(j - bitMatrix.getWidth() / 2 + mtLogoWidth / 2
                            , i - bitMatrix.getHeight() / 2 + mtLogoHidth / 2);
                } else {
                    arr[i * bitMatrix.getWidth() + j] = bitMatrix.get(i, j) ? BLACK : WHITE;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(arr, bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);

        return bitmap;
    }

    /*
         创建Logo白底图片
     */

    public Bitmap createBitmap(Bitmap bgBitmap, Bitmap logoBitmap) {

        int bgWidth = bgBitmap.getWidth();
        int bgHeight = bgBitmap.getHeight();
        /*
            ThumbnailUtils 压缩logo为背景的 1/2
         */
        logoBitmap = ThumbnailUtils.extractThumbnail(logoBitmap, bgWidth / 2,
                bgHeight / 2, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        Bitmap canvasBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(canvasBitmap);
        //合成图片
        canvas.drawBitmap(bgBitmap, 0, 0, null);
        /*
                图片合成
         */
        canvas.drawBitmap(logoBitmap, 30, 30, null);
        canvas.save(); //保存
        canvas.restore();
        if (canvasBitmap.isRecycled()) {
            canvasBitmap.recycle();
        }
        return canvasBitmap;
    }

    /**
     * 长按二维码图片弹出选择框（保存或分享）
     */
    private void imgChooseDialog(){
        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(LogoQRCode.this);
        choiceBuilder.setCancelable(false);
        choiceBuilder
                .setTitle("选择")
                .setSingleChoiceItems(new String[]{"存储至手机", "分享"}, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://存储
                                        saveImg(genBitMap);
                                        break;
                                    case 1:// 分享
                                        shareImg(genBitMap);
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        choiceBuilder.create();
        choiceBuilder.show();
    }

    /**
     * 分享图片(直接将bitamp转换为Uri)
     * @param bitmap
     */
    private void  shareImg(Bitmap bitmap){
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");//设置分享内容的类型
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent = Intent.createChooser(intent, "分享");
        startActivity(intent);
    }

    /**
     * 保存图片至本地
     * @param bitmap
     */
    private void saveImg(Bitmap bitmap){
        String fileName = "qr_"+System.currentTimeMillis() + ".jpg";
        boolean isSaveSuccess = ImgStoreUtils.saveImageToGallery(LogoQRCode.this, bitmap,fileName);
        if (isSaveSuccess) {
            Toast.makeText(LogoQRCode.this, "图片已保存至本地", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LogoQRCode.this, "保存图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 弹出选择框（拍照或从相册选取图片）
     * @author xch
     */
    private void showChooseDialog() {
        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(this);
        choiceBuilder.setCancelable(false);
        choiceBuilder
                .setTitle("选择图片")
                .setSingleChoiceItems(new String[]{"拍照上传", "从相册选择"}, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://拍照
                                        takePhoto();
                                        break;
                                    case 1:// 从相册选择
                                        choosePhotoFromAlbum();
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        choiceBuilder.create();
        choiceBuilder.show();
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            imageUri = FileProvider.getUriForFile(LogoQRCode.this, "fatcats.top.qrcodedemo.fileprovider", outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 从相册选取图片
     */
    private void choosePhotoFromAlbum() {
        if (ContextCompat.checkSelfPermission(LogoQRCode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LogoQRCode.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    /**
     * /打开相册
     */
        private void openAlbum() {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "拒绝被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                            logoBitMap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            // 将拍摄的照片显示出来
                            logoImg.setImageBitmap(logoBitMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 4.4以后
     *
     * @param data
     */
    @SuppressLint("NewApi")
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }

    /**
     * 4.4版本以前，直接获取真实路径
     *
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

     /**
     * 显示图片
     * @param imagePath 图片路径
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
                Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();
                logoBitMap = BitmapFactory.decodeFile(imagePath);
                // 显示图片
            Bitmap bitmap = createBitmap(((BitmapDrawable) getDrawable(R.drawable.bg)).getBitmap(), logoBitMap);
            logoImg.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

}