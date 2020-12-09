package fatcats.top.qrcodedemo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.MailTo;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    ImageView mainCode;
    public static final int CODE_WIDTH = 500;
    public static final int CODE_HEIGHT = 500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainCode = findViewById(R.id.main_code);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType,String> hintTypeStringMap = new HashMap<>();
        hintTypeStringMap.put(EncodeHintType.MARGIN,"0");
        hintTypeStringMap.put(EncodeHintType.CHARACTER_SET,"utf8");
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode("绘制二维码demo", BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_HEIGHT, hintTypeStringMap);
            int[] arr = new int[CODE_WIDTH * CODE_HEIGHT];

            for (int i = 0; i < CODE_WIDTH; i++) {
                for (int j = 0; j < CODE_HEIGHT; j++) {
                    if(bitMatrix.get(i,j)){
                        arr[ i * CODE_WIDTH + j] = Color.BLACK;
                    }else{
                        arr[ i * CODE_WIDTH + j] = Color.WHITE;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(arr, CODE_WIDTH, CODE_HEIGHT, Bitmap.Config.ARGB_8888);
            Glide.with(MainActivity.this).load(bitmap).into(mainCode);

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
}