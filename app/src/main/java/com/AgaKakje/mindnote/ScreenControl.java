package com.AgaKakje.mindnote;



import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScreenControl {
    private DatabaseHelper m_db;
    public RelativeLayout m_parent;
    public int m_order;

    private Resources m_res;

    public Entity m_entity = null;
    public  MainActivity mainActivity;

    public ScreenControl(MainActivity  main, RelativeLayout parent, DatabaseHelper db, Resources res){

        m_db = db;
        m_parent =  parent;

        m_res = res;
        this.mainActivity = main;
    }



    public List<RotateZoomImageView> draw_current(){

        m_parent.removeAllViews();

        RotateZoomImageView im1;

        List<RotateZoomImageView> myList = new ArrayList<>();

        m_order = 0;

        Cursor cursor = m_db.getCurrentItem();
        if (cursor != null){
            if(cursor.moveToFirst()){
                do {
                    int order = cursor.getInt(1);
                    if (order >= m_order){
                        m_order = order + 1;
                    }
                    im1 = draw_image( cursor.getInt(0));
                    myList.add(im1);
                } while (cursor.moveToNext());
            }
        }

        return myList;

    }


    protected RotateZoomImageView draw_image( int item_id) {
        RotateZoomImageView iv;
        RelativeLayout playground = m_parent;

        Entity entity = m_db.get_entity(item_id);
        iv = new RotateZoomImageView(this, mainActivity, m_db, entity);

//        Bitmap batmapBitmap = BitmapFactory.decodeResource(getResources(), id);
        Bitmap batmapBitmap = get_file_name( entity.s_name);


        Bitmap rounded = getRoundedCornerBitmap(batmapBitmap, entity.i_color, 50, 10, mainActivity.getApplicationContext());


        Drawable d = new BitmapDrawable(rounded);

        iv.setImageDrawable(d);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(250, 250);
        lp.addRule(RelativeLayout.BELOW);

        lp.leftMargin = entity.i_leftMargin;
        lp.topMargin = entity.i_topMargin;
        lp.rightMargin = entity.i_rightMargin;
        lp.bottomMargin = entity.i_bottomMargin;


        iv.setLayoutParams(lp);

        iv.setScaleX(entity.f_scalediff);
        iv.setScaleY(entity.f_scalediff);

        iv.animate().rotationBy(entity.f_rotation).setDuration(0).setInterpolator(new LinearInterpolator()).start();


        playground.addView(iv);

        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return iv.onTouch(v, event);
            }
        });



        return  iv;
    }

    private Bitmap get_file_name(String name){
        File yourAppDir = new File(Environment.getExternalStorageDirectory()+ File.separator+"MindNotes"+File.separator+name);
        Bitmap imgBitmap = BitmapFactory.decodeFile(yourAppDir.getAbsolutePath());
        return  imgBitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color, int cornerDips, int borderDips, Context context) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) borderDips,
                context.getResources().getDisplayMetrics());
        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) cornerDips,
                context.getResources().getDisplayMetrics());
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) borderSizePx);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        return output;
    }

    public  long create_image( Bitmap bitmap){
        String name = m_db.GetNewName();
        File path = new File(Environment.getExternalStorageDirectory()+ File.separator+"MindNotes"+File.separator+name);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG,0,fileOutputStream);
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int parent = m_db.getCurent();

        Random rand = new Random();
        long time= System.currentTimeMillis();
        rand.setSeed(time);

        int hue = rand.nextInt(359);

        int color = Color.HSVToColor( new float[]{ hue, 61 , 90 } );

        int i_order;

        i_order = m_order;
        m_order = m_order + 1;

        Entity entity = new Entity(0, name ,parent,color,0,0,0,0,1,0, i_order);
        return m_db.InsertEmployee( entity );
    }
}
