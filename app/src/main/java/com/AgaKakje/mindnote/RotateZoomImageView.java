package com.AgaKakje.mindnote;

import android.app.AlertDialog;
import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.View;
import android.graphics.drawable.BitmapDrawable;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

public class RotateZoomImageView extends AppCompatImageView implements View.OnTouchListener{
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;

    public DatabaseHelper m_db;
    public Entity m_entity;
    public ScreenControl m_screenControl;

    private long m_time;
    float scalediff;

    AlertDialog dialog;
    AlertDialog.Builder builder;

    private  ImageView m_view;

    private GestureDetector m_gestureDetector;
    private MainActivity mainActivity;
    public RotateZoomImageView(ScreenControl screenControl,MainActivity main, DatabaseHelper db, Entity entity) {

        super(main.getApplicationContext() );
        m_db = db;
        m_entity = entity;
        m_gestureDetector =  new GestureDetector(main.getApplicationContext(), new GestureListener());
        m_screenControl = screenControl;
        this.mainActivity = main;


    }

    /*
     * Use onSizeChanged() to calculate values based on the view's size.
     * The view has no size during init(), so we must wait for this
     * callback.
     */




    /*
     * Operate on two-finger events to rotate the image.
     * This method calculates the change in angle between the
     * pointers and rotates the image accordingly.  As the user
     * rotates their fingers, the image will follow.
     */
    RelativeLayout.LayoutParams parms;
    int startwidth;
    int startheight;
    float dx = 0, dy = 0, x = 0, y = 0;
    float angle = 0;



    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final ImageView view = (ImageView) v;
        m_view = view;
        ((BitmapDrawable) view.getDrawable()).setAntiAlias(true);
//        ((RoundedBitmapDrawable) view.getDrawable()).setAntiAlias(true);
//        view.getDrawable().setAntiAlias(true);
        boolean result = m_gestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                parms = (RelativeLayout.LayoutParams) view.getLayoutParams();
                startwidth = parms.width;
                startheight = parms.height;
                dx = event.getRawX() - parms.leftMargin;
                dy = event.getRawY() - parms.topMargin;
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    mode = ZOOM;
                }

                d = rotation(event);

                break;
            case MotionEvent.ACTION_UP:

                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;

                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {

                    x = event.getRawX();
                    y = event.getRawY();

                    parms.leftMargin = (int) (x - dx);
                    parms.topMargin = (int) (y - dy);

                    parms.rightMargin = 0;
                    parms.bottomMargin = 0;
                    parms.rightMargin = parms.leftMargin + (5 * parms.width);
                    parms.bottomMargin = parms.topMargin + (10 * parms.height);

                    view.setLayoutParams(parms);

                    m_entity.i_leftMargin = parms.leftMargin;
                    m_entity.i_topMargin = parms.topMargin;
                    m_entity.i_rightMargin = parms.rightMargin;
                    m_entity.i_bottomMargin = parms.bottomMargin;
                    if (scalediff != 0) {
                        m_entity.f_scalediff = scalediff;
                    }
                    m_entity.f_rotation = view.getRotation();
                    m_db.modify_Entity(m_entity);


                } else if (mode == ZOOM) {

                    if (event.getPointerCount() == 2) {

                        newRot = rotation(event);
                        float r = newRot - d;
                        angle = r;

                        x = event.getRawX();
                        y = event.getRawY();

                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            float scale = newDist / oldDist * view.getScaleX();
                            if (scale > 0.6) {
                                scalediff = scale;
                                view.setScaleX(scale);
                                view.setScaleY(scale);

                            }
                        }

                        view.animate().rotationBy(angle).setDuration(0).setInterpolator(new LinearInterpolator()).start();

                        x = event.getRawX();
                        y = event.getRawY();

                        parms.leftMargin = (int) ((x - dx) + scalediff);
                        parms.topMargin = (int) ((y - dy) + scalediff);

                        parms.rightMargin = 0;
                        parms.bottomMargin = 0;
                        parms.rightMargin = parms.leftMargin + (5 * parms.width);
                        parms.bottomMargin = parms.topMargin + (10 * parms.height);

                        view.setLayoutParams(parms);

                        m_entity.i_leftMargin = parms.leftMargin;
                        m_entity.i_topMargin = parms.topMargin;
                        m_entity.i_rightMargin = parms.rightMargin;
                        m_entity.i_bottomMargin = parms.bottomMargin;
                        m_entity.f_scalediff = scalediff;
                        m_entity.f_rotation = view.getRotation();
                        m_db.modify_Entity(m_entity);


                    }
                }
                break;
        }

        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{


        private static final String TAG = "tapListener";


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i(TAG, "onDoubleTouch");
            onDoubleTouch();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i(TAG, "onLongTouch");
            onLongTouch();
            super.onLongPress(e);
        }

        protected void onDoubleTouch(){

            m_db.setCurrent(m_entity.i_id);
            m_screenControl.draw_current();

        }

        protected void onLongTouch(){
            PopupMenu popupMenu = new PopupMenu(mainActivity, m_view);
            popupMenu.inflate(R.menu.popup_menu);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true);
            }



            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    int id = item.getItemId();

                    if (id == R.id.Up) {
                        m_entity.i_order = m_screenControl.m_order;
                        m_db.modify_Entity( m_entity );
                        m_screenControl.draw_current();

                        return true;
                    }

                    if (id == R.id.Delete) {
                        long time= System.currentTimeMillis();

                        if ( ( time - m_time ) < 10000 ){
                            m_db.delete(m_entity.i_id);
                            m_screenControl.draw_current();
                            File path = new File(Environment.getExternalStorageDirectory()+ File.separator+"MindNotes"+File.separator+m_entity.s_name);
                            boolean deleted = path.delete();
                        }else{
                            Toast toast = Toast.makeText(mainActivity.getApplicationContext(),
                                    "Delete again", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        m_time = time;


                        return true;
                    }

                    if (id == R.id.Cut) {
                        m_screenControl.m_entity = m_entity;
                        m_db.delete(m_entity.i_id);
                        m_screenControl.draw_current();
                        return true;
                    }

                    return true;

                }
            });


            popupMenu.show();



        }


    }
}