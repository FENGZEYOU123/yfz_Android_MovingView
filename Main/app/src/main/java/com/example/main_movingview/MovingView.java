package com.example.main_movingview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 *  编写者姓名：三水
 *  开始日期：2020年7月13日
 *  最后更新日期：2020年8月6日
 *
 *  功能介绍：可移动组件view，带有各种效果
 *
 *
 *      limited_in_Max_Screen(limited_open);  //限制组件范围，不超过屏幕. limited_open 为true的话 则限制，否则自由移动
 *      attach_boundary();          //吸边 当组件靠近四边时会有吸附上去的效果
 *      ios_spring_press();         //模仿ios动画-弹簧阻尼效果-压缩，允许移动超过屏幕，但不超过组件自身的1/2大小。且释放之后会自动回弹
 *      ios_spring_release();       //模仿ios动画-弹簧阻尼效果-释放，当组件在屏幕外，这时候抬起手指，则视为从弹簧压缩状态释放
 *      popup_W();popup_H();        //记录弹簧压缩并释放后，需要回弹的高度。 由 spring_open_release_popup 开关控制是否开启
 *
 *
 * **/
public class MovingView extends ConstraintLayout {
    private String TAG="移动组件：    ";
    private DisplayMetrics dm= new DisplayMetrics();
    private Context context;
    private WindowManager wm=null;

    Timer timer = new Timer();



    private double inner =15;
    private double outside =15;
    private boolean attach_open =true;
    private String move_Dir=null;
    /**
     *  ***吸附属性设置
     *  inner屏幕内部吸附距离
     *  outside屏幕外部吸附距离
     **/

    private double spring_dis = 15,more_slow=0.1;
    private boolean spring_open_press=true,spring_open_release=true,spring_open_release_popup=true;
    private double popup_W=0.0,popup_H=0.0,popup_rate=0.5;
    /**
     *  ***弹簧属性设置
     *   spring_open_press 开启压缩弹簧属性,spring_open_release 开启释放弹簧属性，spring_open_release_popup开启释放弹簧后popup属性
     *  spring_left弹簧距离限制,数字越小，组件能够超出屏幕的距离越小，越早开始压缩
     *  more_slow移动到弹簧距离限制后，再次放慢移动速率
     *  popup_W,popup_H弹簧释放release后将要弹起的W,H，跟组件超出屏幕边界多少有关， popup高度为其超出的 值 * (popup_rate)
     *  popup_rate 为弹起的倍率
     *
     **/

    private boolean limited_open = false;
    /**
     *  ***普通限制属性设置
     *  limited_innter是否限制view移动在屏幕内。true为限制，false为允许组件自由移出屏幕
     **/

    private double View_X_Width, View_Y_Hight;
    /**
     *  View_X_Width 记录组件的宽度
     *  View_Y_Hight 记录组件的长度
     **/

    //**************组件坐标系, 是以当前触摸组件左上角为(0,0)*******************//

    private double Finger_X,Finger_Y;
    /**
     *  Finger_X 记录点击时手指基于组件坐标系的位置 X
     *  Finger_Y 记录点击时手指基于组件坐标系的位置 Y
     **/

    //**************Android坐标系 是以屏幕左上角为(0,0)*******************//
    private double Display_Left,Display_Right,Display_Top,Display_Bottom;
    /**
     *  Display_Left 记录组件的最左边
     *  Display_Right 记录组件的最右边
     *  Display_Top 记录组件的最上边
     *  Display_Bottom 记录组件的最下边。
     *  相对于 Android坐标系x 的位置
     **/

    private double Screen_MAX_Hight,Screen_MAX_Width;
    /**
     *   Screen_MAX_Hight 记录屏幕的最大长度
     *   Screen_MAX_Width 记录屏幕的最大宽度
     *   //确保组件不会超出屏幕
     **/

    private  double Move_X_Distance,Move_Y_Distance;
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
                Finger_X=(double)event.getX();
                Finger_Y=(double)event.getY();
                Log.d(TAG, "Finger_X 手指点击相对组件宽度 "+Finger_X);
                Log.d(TAG, "Finger_Y 手指点击相对组件长度 "+Finger_Y);
                Log.e(TAG, "******************************************");

                break;

            case MotionEvent.ACTION_MOVE:  //当手指开始移动的时候
//                Log.e(TAG, "onTouchEvent: ACTION_MOVE 移动 ");

                //记录移动距离
                    Move_X_Distance = (double)event.getX() - Finger_X;  //记录移动的距离X
                    Move_Y_Distance = (double)event.getY() - Finger_Y;  //记录移动的距离Y
                    Log.d(TAG, "Move_X_Distance 移动距离为: " + Move_X_Distance);
                    Log.d(TAG, "Move_Y_Distance 移动距离为: " + Move_Y_Distance);
                    Log.e(TAG, "....................................");

                    //将要展示的组件的四个顶点位置
                    Display_Left = getLeft() + Move_X_Distance;
                    Display_Right = getRight() + Move_X_Distance;
                    Display_Top = getTop() + Move_Y_Distance;
                    Display_Bottom = getBottom() + Move_Y_Distance;

                    //移动方向判断
                    move_Dir=  Move_Direction(Move_X_Distance,Move_Y_Distance);

                    limited_in_Max_Screen(limited_open);  //限制组件范围，不超过屏幕
                    attach_boundary(attach_open);          //吸边 当组件靠近四边时会有吸附上去的效果
                    ios_spring_press(spring_open_press);         //模仿ios动画-弹簧阻尼效果-压缩，允许移动超过屏幕，但不超过组件自身的1/2大小。且释放之后会自动回弹

                    // 刷新组件位置，形成组件跟随手指移动的效果
                    //https://www.cnblogs.com/xyhuangjinfu/p/5435253.html view的layout原理文章
                    this.layout( (int)Display_Left,  (int)Display_Top,  (int)Display_Right, (int)Display_Bottom); //左，上，右，下
            break;

            case MotionEvent.ACTION_UP:  //当手指上抬起（停止触屏屏幕）
                Log.e(TAG, "onTouchEvent: ACTION_UP 抬手 ");
                         //记录W超出边界值
                if(spring_open_release_popup){
                if(getLeft()<0||getRight()>Screen_MAX_Width){
                    popup_W= popup_W(getLeft(),getRight())*popup_rate;
                }
                        //记录H超出边界值
                if(getTop()<0||getBottom()>Screen_MAX_Hight){
                    popup_H= popup_H(getTop(),getBottom())*popup_rate;
                } }
                ios_spring_release(popup_W,popup_H);    //模仿ios动画-弹簧阻尼效果-释放，当组件在屏幕外，这时候抬起手指，则视为从弹簧压缩状态释放
                reset(); //重置数据
                break;
        }



        return true;
    }

    /**
     * 普通限制位置是否在屏幕内活动
     **/
    private  void limited_in_Max_Screen(boolean limited_inner){
        if(limited_inner) {  //如果是在边界内活动，则为true
            if (Display_Left < 0) {  //如果移动超出了最左边，那么代表已经超出了屏幕尺寸
                Display_Left = 0;   //重置移动的 X 距离为0
                Display_Right = Display_Left + View_X_Width;

            } else if (Display_Right > Screen_MAX_Width) {  //如果移动超出了最右边,那么代表已经超出了屏幕尺寸
                Display_Right = Screen_MAX_Width;
                Display_Left = Display_Right - View_X_Width;
            }

            if (Display_Top < 0) {  //如果移动为负数，那么代表已经超出了屏幕尺寸
                Display_Top = 0;   //重置移动的 Y 距离为0
                Display_Bottom = Display_Top + View_Y_Hight;
            } else if (Display_Bottom > Screen_MAX_Hight) {
                Display_Bottom = Screen_MAX_Hight;
                Display_Top = Display_Bottom - View_X_Width;
            }
        }
    }

    /**
     * ios弹簧方法-压缩
     **/
    private  void ios_spring_press(boolean spring_open_press){ //ios弹簧方法-压缩
        if(spring_open_press) {
            if (getLeft() < 0) {  //左边小于spring_dis距离的时候，开始放慢向左移动速度
                Display_Left = (double) (getLeft() + press_speed(more_slow, Move_X_Distance, getLeft()));
                Display_Right = Display_Left + View_X_Width;
                Log.d(TAG, "ios_spring_press: 小于左边spring_dis距离的:getLeft()  " + getLeft());

            } else if (getRight() > Screen_MAX_Width) {  //右边大于spring_dis+Screen距离的时候，开始放慢向右移动速度
                Display_Right = (double) (getRight() + press_speed(more_slow, Move_X_Distance, getRight()));
                Display_Left = Display_Right - View_X_Width;
                Log.d(TAG, "ios_spring_press: 小于右边spring_dis距离的:getRight()  " + getRight());

            }


            if (getTop() < 0) {  //上边小于spring_dis距离的时候，开始放慢向上移动速度
                Display_Top = (double) (getTop() + press_speed(more_slow, Move_Y_Distance, getTop()));
                Display_Bottom = Display_Top + View_Y_Hight;
                Log.d(TAG, "ios_spring_press: 小于上边spring_dis距离的:getTop()  " + getTop());

            } else if (getBottom() > Screen_MAX_Hight) {
                Display_Bottom = (double) (getBottom() + press_speed(more_slow, Move_Y_Distance, getBottom()));
                Display_Top = Display_Bottom - View_Y_Hight;
                Log.d(TAG, "ios_spring_press: 小于下边spring_dis距离的:getBottom()  " + getBottom());

            }

//           getLeft()<0: popup_W(Display_Left);
//            popup_H(Display_Top);
        }
    }

    //放慢速率随着组件view，超出屏幕边界越多越慢
    private double  press_speed(double more_slow,double Move_X_Distance,double get_view){
        double speed=0.0;
        if(get_view<0) {
            speed = 0.1*more_slow * (Move_X_Distance) * (double)(Math.sqrt(Math.abs(get_view)));
            Log.d(TAG, "press_speed: Display_Left" + "  " + Move_X_Distance+"     "+(double)(Math.sqrt(Math.abs(get_view))));
        }else{
            speed = 0.1*more_slow * (Move_X_Distance) * (double)(Math.sqrt(Math.abs(get_view-Screen_MAX_Width)) );
            Log.d(TAG, "press_speed: Display_Right" + "  " + Move_X_Distance+"   "+(double)(Math.sqrt(Math.abs(get_view-Screen_MAX_Width))));


        }
        return speed ;
    }

    //记录一下组件view超出边界的W
    private double popup_W(double Left,double Right){
        double W=0.0;
        if(Left<0){   //如果说超出左边W
            W= Math.abs(Left);
        }else if( Left>0 && Right>Screen_MAX_Width )  {  //如果说超出右边W
            W= Math.abs(Right-Screen_MAX_Width);
        }
        Log.d(TAG, "press_speed: 储存的W为   "+W  );
        return W ;
    }
    //记录一下组件view超出边界的H
    private double popup_H(double Top,double Bottom){
        double H=0.0;
        if(Top<0){   //如果说超出上边H
            H= Math.abs(Top);
        }else if( Top>0 && Bottom>Screen_MAX_Hight)  {  //如果说超出下边H
            H= Math.abs(Bottom-Screen_MAX_Hight);
        }

        Log.d(TAG, "press_speed: 储存的H为   "+H );
        return H ;
    }



    /**
     * ios弹簧方法-释放
     **/
    private  void ios_spring_release(double popup_W, double popup_H){ //ios弹簧方法-释放
        //当开启弹簧效果，且任意一边超出屏幕边界

//        timer.schedule(new TimerTask() {
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//            }
//
//        }, 100);

        while(spring_open_release&&(getLeft()<popup_W||getTop()<popup_H||getRight()>Screen_MAX_Width||getBottom()>Screen_MAX_Hight)) {
            if (getLeft() < popup_W) {  //左边超出屏幕边界
                Display_Left=getLeft()+1;
                Display_Right=Display_Left+View_X_Width;
                Log.d(TAG, "ios_spring_release: 释放左边   "+getLeft()+"   "+popup_W);

            } else if (getRight()>Screen_MAX_Width) {  //右边大于spring_dis+Screen距离的时候，开始放慢向右移动速度
               Display_Right=Screen_MAX_Width-popup_W;
               Display_Left=Display_Right-View_X_Width;
                Log.d(TAG, "ios_spring_release: 释放右边   "+getRight());

            }

            if (getTop() < popup_H) {  //左边超出屏幕边界
                Display_Top=getTop()+1;
                Display_Bottom=Display_Top+View_Y_Hight;
                Log.d(TAG, "ios_spring_release: 释放上边   "+getTop());

            } else if (getBottom()>Screen_MAX_Hight) {  //右边大于spring_dis+Screen距离的时候，开始放慢向右移动速度
                Display_Bottom =Screen_MAX_Hight-popup_H;
                Display_Top=Display_Bottom-View_Y_Hight;
                Log.d(TAG, "ios_spring_release: 释放下边   "+getBottom());

            }

            this.layout( (int)Display_Left,  (int)Display_Top,  (int)Display_Right, (int)Display_Bottom); //左，上，右，下

        }

    }


    /**
     * 吸边方法
     **/
    private void attach_boundary(boolean attach_open){
        if(attach_open) {
            if (Display_Left <= inner && Display_Left > 0) {  //左边吸边效果
                Display_Left = 0;
                Display_Right = Display_Left + View_X_Width;
                Log.d(TAG, "attach_boundary: 监测到左边碰到边");
                return;

            } else if (Screen_MAX_Width > Display_Right && Display_Right > Screen_MAX_Width - inner) {  //右边吸边效果
                Display_Right = Screen_MAX_Width;
                Display_Left = Display_Right - View_X_Width;
                Log.d(TAG, "attach_boundary: 监测到右边碰到边");
                return;
            }

            if (Display_Top <= inner && Display_Top > 0) {   //上边吸边效果
                Display_Top = 0;
                Display_Bottom = Display_Top + View_Y_Hight;
                Log.d(TAG, "attach_boundary: 监测到上边碰到边");
                return;

            } else if (Screen_MAX_Hight > Display_Bottom && Display_Bottom > Screen_MAX_Hight - inner) {  //下边吸边效果
                Display_Bottom = Screen_MAX_Hight;
                Display_Top = Display_Bottom - View_Y_Hight;
                Log.d(TAG, "attach_boundary: 监测到下边碰到边");
                return;
            }

        }

    }


    /**
     * 判断移动的方向
     **/
    private String Move_Direction(double Move_X,double Move_Y){
        String dir="";
        if(Move_Y==0){   //Y轴为0，那么肯定是在Y轴上移动
            if(Move_X==0){
                return dir;
            }else{ dir=(Move_X<0)? "West":"East"; }
        }else if (Move_X==0){  //X轴为0，那么肯定是在Y轴上移动
            if(Move_Y==0){
                return dir;
            }else{ dir=(Move_Y<0)? "North":"South"; }
        }else if (Move_X!=0&&Move_Y!=0){   //如果都不为0，那么肯定是XY倾斜的移动
            if(Move_X<0){
                dir=(Move_Y<0)? "toWest_fromNorth":"toWest_fromSouth"; //左上，左下
            }else {
                dir=(Move_Y<0)? "toEast_fromNorth":"toEast_fromSouth"; //右上，右下
            }
        }



            if(dir!="") { Log.e(TAG, "**********Move_Direction 移动方向为： " + dir); }
        return dir;

    }
    private void reset(){
        //重置数据
        popup_W=0.0;
        popup_H=0.0;
    }

}
