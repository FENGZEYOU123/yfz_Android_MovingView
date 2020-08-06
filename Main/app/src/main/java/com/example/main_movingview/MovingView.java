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

/**
 *  编写者姓名：三水
 *  开始日期：2020年7月13日
 *  最后更新日期：2020年8月6日
 *
 *  功能介绍：
 *
 *
 *
 *
 *
 * **/
public class MovingView extends ConstraintLayout {
    private String TAG="移动组件：    ";
    private DisplayMetrics dm= new DisplayMetrics();
    private Context context;
    private WindowManager wm=null;


    private int inner =20;
    private int outside =40;
    /**
     *  ***吸附属性设置
     *  inner屏幕内部吸附距离
     *  outside屏幕外部吸附距离
     **/

    private double sb_dist = 0.5,more_slow=1.5;
    /**
     *  ***弹簧属性设置
     *  sb_dist弹簧距离限制,数字越小，组件能够超出屏幕的距离越小，越早开始压缩
     *  more_slow移动到弹簧距离限制后，再次放慢移动速率
     **/

    private boolean limited_innter = true;
    /**
     *  ***普通限制属性设置
     *  limited_innter是否限制view移动在屏幕内。true为限制，false为允许组件自由移出屏幕
     **/

    private int View_X_Width, View_Y_Hight;
    /**
     *  View_X_Width 记录组件的宽度
     *  View_Y_Hight 记录组件的长度
     **/

    //**************组件坐标系, 是以当前触摸组件左上角为(0,0)*******************//

    private int Finger_X,Finger_Y;
    /**
     *  Finger_X 记录点击时手指基于组件坐标系的位置 X
     *  Finger_Y 记录点击时手指基于组件坐标系的位置 Y
     **/

    //**************Android坐标系 是以屏幕左上角为(0,0)*******************//
    private int DisplayLeft,DisplayRight,DisplayTop,Display_Bottom;
    /**
     *  DisplayLeft 记录组件的最左边
     *  DisplayRight 记录组件的最右边
     *  DisplayTop 记录组件的最上边
     *  Display_Bottom 记录组件的最下边。
     *  相对于 Android坐标系x 的位置
     **/

    private int Screen_MAX_Hight,Screen_MAX_Width;
    /**
     *   Screen_MAX_Hight 记录屏幕的最大长度
     *   Screen_MAX_Width 记录屏幕的最大宽度
     *   //确保组件不会超出屏幕
     **/

    private  int Move_X_Distance,Move_Y_Distance;
    /**
     *   Move_X_Distance 移动距离X
     *   Move_X_Distance 移动距离Y
     *   （手指移动的距离-手指第一次点击组件记录的值）
     **/

    public MovingView(Context context) {
        super(context);

    }
    public MovingView(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        this.context=context;

        wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics( dm );
        Screen_MAX_Hight=dm.heightPixels;
        Screen_MAX_Width=dm.widthPixels;
        Log.d(TAG, "Screen_MAX_Hight 屏幕最大长度 "+Screen_MAX_Hight);
        Log.d(TAG, "Screen_MAX_Width 屏幕最大宽度 "+Screen_MAX_Width);
    }
    public MovingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  //拿到测量的组件值
        View_X_Width=getMeasuredWidth();  //记录组件宽度
        View_Y_Hight=getMeasuredHeight();   //记录组件长度
        Log.d(TAG, "View_X_Width 组件宽度 "+View_X_Width);
        Log.d(TAG, "View_Y_Hight 组件长度 "+View_Y_Hight);
        Log.e(TAG, "-----------------------------------------");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.requestDisallowInterceptTouchEvent(true);//自己消耗掉事件，不向下传递
        int action=event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:   //当手指按下的时候，需要记录以下手指点击的位置相对于组件的坐标(以组件左上角计作(0,0))
                Finger_X=(int)event.getX();
                Finger_Y=(int)event.getY();
                Log.d(TAG, "Finger_X 手指点击相对组件宽度 "+Finger_X);
                Log.d(TAG, "Finger_Y 手指点击相对组件长度 "+Finger_Y);
                Log.e(TAG, "******************************************");

                break;

            case MotionEvent.ACTION_MOVE:  //当手指开始移动的时候
                    //记录移动距离
                    Move_X_Distance = (int)event.getX() - Finger_X;  //记录移动的距离X
                    Move_Y_Distance = (int)event.getY() - Finger_Y;  //记录移动的距离Y
                    Log.d(TAG, "Move_X_Distance 移动距离为: " + Move_X_Distance);
                    Log.d(TAG, "Move_Y_Distance 移动距离为: " + Move_Y_Distance);
                    Log.e(TAG, "....................................");

                    //将要展示的组件的四个顶点位置
                    DisplayLeft = getLeft() + Move_X_Distance;
                    DisplayRight = DisplayLeft + View_X_Width;
                    DisplayTop = getTop() + Move_Y_Distance;
                    Display_Bottom = DisplayTop + View_Y_Hight;

//                  limited_in_Max_Screen(limited_innter);  //限制组件范围，不超过屏幕
                    attach_boundary();        //吸边 当组件靠近四边时会有吸附上去的效果
                    ios_spring_pop();         //模仿ios动画，允许移动超过屏幕，但不超过组件自身的1/2大小。且释放之后会自动回弹

                    // 刷新组件位置，形成组件跟随手指移动的效果
                    //https://www.cnblogs.com/xyhuangjinfu/p/5435253.html view的layout原理文章
                    this.layout( DisplayLeft,  DisplayTop,  DisplayRight, Display_Bottom); //左，上，右，下


            case MotionEvent.ACTION_UP:  //当手指上抬起（停止触屏屏幕）

                break;
        }



        return true;
    }

    /**
     * 普通限制位置是否在屏幕内活动
     **/
    private  void limited_in_Max_Screen(boolean limited_inner){
        if(limited_inner) {  //如果是在边界内活动，则为true
            if (DisplayLeft < 0) {  //如果移动超出了最左边，那么代表已经超出了屏幕尺寸
                DisplayLeft = 0;   //重置移动的 X 距离为0
                DisplayRight = DisplayLeft + View_X_Width;

            } else if (DisplayRight > Screen_MAX_Width) {  //如果移动超出了最右边,那么代表已经超出了屏幕尺寸
                DisplayRight = Screen_MAX_Width;
                DisplayLeft = DisplayRight - View_X_Width;
            }

            if (DisplayTop < 0) {  //如果移动为负数，那么代表已经超出了屏幕尺寸
                DisplayTop = 0;   //重置移动的 Y 距离为0
                Display_Bottom = DisplayTop + View_Y_Hight;
            } else if (Display_Bottom > Screen_MAX_Hight) {
                Display_Bottom = Screen_MAX_Hight;
                DisplayTop = Display_Bottom - View_X_Width;
            }
        }
    }

    /**
     * ios弹簧方法
     **/
    private  void ios_spring_pop(){ //ios弹簧方法

            if (DisplayLeft > (-sb_dist * View_X_Width ) && DisplayLeft <= -inner) {  //超出屏幕，但是不超过组件自身sb_dist*宽
                DisplayLeft = (int)(getLeft() + (-1) * Math.sqrt(-1 * Move_X_Distance / 3));
                DisplayRight = DisplayLeft + View_X_Width;
                Log.e(TAG, "ios_spring_pop: 左：在规定的 内 外 边界内");


            }else if (DisplayLeft<=(-sb_dist * View_X_Width)) {
                DisplayLeft = (int)(getLeft()-more_slow);
                DisplayRight = DisplayLeft + View_X_Width;
                Log.e(TAG, "ios_spring_pop: 左：超出规定的内 外 边界");


            }
            else if ((sb_dist*sb_dist * View_X_Width+Screen_MAX_Width )>DisplayRight && DisplayRight >= Screen_MAX_Width-inner)  {  //如果移动超出了最右边,那么代表已经超出了屏幕尺寸
                DisplayRight = (int)(Screen_MAX_Width+ Math.sqrt( Move_X_Distance / 3));
                DisplayLeft = DisplayRight - View_X_Width;
                if(DisplayRight==0||DisplayLeft==0){
                    Log.e(TAG, "报错:  "+DisplayRight+"   "+DisplayLeft );

                }
                Log.e(TAG, "ios_spring_pop: 右：在规定的 内 外 边界内");

            }
            else if (DisplayLeft>(sb_dist*sb_dist * View_X_Width+Screen_MAX_Width)) {
                DisplayRight = (int)(Screen_MAX_Width+more_slow);
                DisplayLeft = DisplayRight - View_X_Width;
                Log.e(TAG, "ios_spring_pop: 右：超出规定的内 外 边界");


            }



            if (DisplayTop < 0) {  //如果移动为负数，那么代表已经超出了屏幕尺寸
                DisplayTop = 0;   //重置移动的 Y 距离为0
                Display_Bottom = DisplayTop + View_Y_Hight;
            } else if (Display_Bottom > Screen_MAX_Hight) {
                Display_Bottom = Screen_MAX_Hight;
                DisplayTop = Display_Bottom - View_X_Width;
            }

    }

    /**
     * 吸边方法
     **/
    private void attach_boundary(){
        if(DisplayLeft<=inner&&DisplayLeft>=(-outside)){  //左边吸边效果
            DisplayLeft=0;
            DisplayRight= DisplayLeft + View_X_Width;
            Log.e(TAG, "attach_boundary: 监测到左边碰到边");
            return;

        }else if(Screen_MAX_Width+outside>DisplayRight&&DisplayRight > Screen_MAX_Width-inner){  //右边吸边效果
            DisplayRight=Screen_MAX_Width;
            DisplayLeft= DisplayRight - View_X_Width;
            Log.e(TAG, "attach_boundary: 监测到右边碰到边");
            return;
        }

        if (DisplayTop <=inner&&DisplayTop>=(-outside)) {   //上边吸边效果
            DisplayTop = 0;
            Display_Bottom = DisplayTop + View_Y_Hight;
            Log.e(TAG, "attach_boundary: 监测到上边碰到边");
            return;

        } else if (Screen_MAX_Hight+outside>Display_Bottom&&Display_Bottom > Screen_MAX_Hight-inner) {  //下边吸边效果
            Display_Bottom = Screen_MAX_Hight;
            DisplayTop = Display_Bottom - View_Y_Hight;
            Log.e(TAG, "attach_boundary: 监测到下边碰到边");
            return;
        }


    }

}
