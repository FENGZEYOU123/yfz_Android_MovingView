package com.example.main_movingview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MovingView extends ConstraintLayout {
    private String TAG="移动组件：    ";
    private DisplayMetrics dm= new DisplayMetrics();
    private Context context;
    private WindowManager wm=null;

    private int MovingView_X;
    /**
     *  MovingView_X 记录组件的宽度
     **/

    private int MovingView_Y;
    /**
     *   MovingView_Y 记录组件的长度
     **/

    private float Finger_X;
    /**
     *  Finger_X 记录点击时手指基于组件坐标系的位置 X
     **/

    private float Finger_Y;
    /**
     *  Finger_Y 记录点击时手指基于组件坐标系的位置 Y
     **/

    //**************Android坐标系 是以屏幕左上角为(0,0)*******************//
    private float MovingView_Left;
    /**
     *  MovingView_Left 记录组件的最左边相对于 Android坐标系x 的位置
     **/

    private float MovingView_Right;
    /**
     *   MovingView_Right 记录组件的最右边相对于 Android坐标系x 的位置
     **/
    private float MovingView_Top;
    /**
     *  MovingView_Top 记录组件的最上边相对于 Android坐标系y 的位置
     **/

    private float MovingView_Bottom;
    /**
     *   MovingView_Bottom 记录组件的最下边相对于 Android坐标系y 的位置
     **/

    private float Screen_MAX_Height;
    /**
     *   Screen_MAX_Height 记录屏幕的最大长度  //确保组件不会超出
     **/
    private float Screen_MAX_Width;
    /**
     *   Screen_MAX_Width 记录屏幕的最大宽度   //确保组件不会超出
     **/



    private float Move_X;
    /**
     *   Move_X 移动距离X（手指移动的距离-手指第一次点击组件记录的值）
     **/
    private float Move_Y;
    /**
     *   Move_X 移动距离Y（手指移动的距离-手指第一次点击组件记录的值）
     **/


    public MovingView(Context context) {
        super(context);

    }
    public MovingView(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        this.context=context;

        wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics( dm );
        Screen_MAX_Height=dm.heightPixels;
        Screen_MAX_Width=dm.widthPixels;
        Log.d(TAG, "Screen_MAX_Height 屏幕最大长度 "+Screen_MAX_Height);
        Log.d(TAG, "Screen_MAX_Width 屏幕最大宽度 "+Screen_MAX_Width);
    }
    public MovingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  //拿到测量的组件值
        MovingView_X=getMeasuredWidth();  //记录组件宽度
        MovingView_Y=getMeasuredHeight();   //记录组件长度
        Log.d(TAG, "MovingView_X 组件宽度 "+MovingView_X);
        Log.d(TAG, "MovingView_Y 组件长度 "+MovingView_Y);
        Log.e(TAG, "-----------------------------------------");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.requestDisallowInterceptTouchEvent(true);//自己消耗掉事件，不向下传递
        int action=event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:   //当手指按下的时候，需要记录以下手指点击的位置相对于组件的坐标(以组件左上角计作(0,0))
                Finger_X=event.getX();
                Finger_Y=event.getY();
                Log.d(TAG, "Finger_X 手指点击相对组件宽度 "+Finger_X);
                Log.d(TAG, "Finger_Y 手指点击相对组件长度 "+Finger_Y);

                MovingView_Left=getLeft();
                MovingView_Top=getTop();
                MovingView_Right=getRight();
                MovingView_Bottom=getBottom();

                Log.d(TAG, "MovingView_Left 组件相对于 android位置 "+MovingView_Left);
                Log.d(TAG, "MovingView_Right 组件相对于 android位置 "+MovingView_Right);
                Log.d(TAG, "MovingView_Top 组件相对于 android位置 "+MovingView_Top);
                Log.d(TAG, "MovingView_Bottom 组件相对于 android位置 "+MovingView_Bottom);
                Log.e(TAG, "******************************************");

                break;

            case MotionEvent.ACTION_MOVE:  //当手指开始移动的时候
                //刷新组件位置，形成组件跟随手指移动的效果
                Move_X=event.getX()-Finger_X;  //记录移动的距离X
                Move_Y=event.getX()-Finger_Y;  //记录移动的距离Y
                Log.d(TAG, "Move_X 移动距离为: "+Move_X);
                Log.d(TAG, "Move_Y 移动距离为: "+Move_Y);
                Log.e(TAG, "....................................");
                if(Move_X<=0){  //如果移动为负数，那么代表已经超出了屏幕尺寸
                    Move_X=0;   //重置移动的 X 距离为0
                }

                if(Move_Y<=0){  //如果移动为负数，那么代表已经超出了屏幕尺寸
                    Move_X=0;   //重置移动的 Y 距离为0
                }


                //https://www.cnblogs.com/xyhuangjinfu/p/5435253.html view的layout原理文章
                this.layout((int)MovingView_Left,(int)MovingView_Top,(int)MovingView_Right,(int)MovingView_Bottom); //左，上，右，下
                    break;

            case MotionEvent.ACTION_UP:  //当手指上抬起（停止触屏屏幕）
                this.requestDisallowInterceptTouchEvent(false);  //取消自己消耗事件
                break;
        }


        return true;
    }
}
