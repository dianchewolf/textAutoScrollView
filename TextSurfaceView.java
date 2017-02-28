
/**
 * Created by monkey on 17/1/12.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TextSurfaceView extends SurfaceView implements Callback {

    /**
     * 是否滚动
     */
    private boolean isMove = true;
    /**
     * 移动方向
     */
    private int orientation = 0;
    /**
     * 向左移动
     */
    public final static int MOVE_LEFT = 0;
    /**
     * 向右移动
     */
    public final static int MOVE_RIGHT = 1;
    /**
     * 向上移动
     */
    public final static int MOVE_TOP = 2;
    /**
     * 向下移动
     */
    public final static int MOVE_BOTTOM = 3;
    /**
     * static state
     */
    private int staticOri = 0;
    public final static int STATIC_LEFT = 1;
    public final static int STATIC_CENTER = 0;
    public final static int STATIC_RIGHT = 2;
    /**
     * 移动速度　1.5s　移动一次
     */
    private long speed = 100;
    /**
     * 字幕内容
     */
    private String content = "";

    /**
     * 字幕背景色
     */
    private int bgColor = Color.BLACK;

    /**
     * 字幕透明度　默认：255
     */
    private int bgalpha = 255;

    /**
     * 字体颜色 　默认：白色 (#FFFFFF)
     */
    private int fontColor = Color.RED;

    /**
     * 字体透明度　默认：不透明(255)
     */
    private int fontAlpha = 255;

    /**
     * 字体大小 　默认：30
     */
    private float fontSize = 30f;
    /**
     * 容器
     */
    private SurfaceHolder mSurfaceHolder;

    /**
     * 内容滚动位置起始坐标
     */
    private float x = 0;
    /**
     * 内容滚动位置起始坐标
     */
    private float y = 0;

    /**
     * 文字内容宽度
     */
    private float textContentWidth = 0;
    private float textHeigth = 0;
    private float textFontHeight = 0;
    private int repeatCount = 0;
    /**
     * 内容
     */
    private ArrayList<String> stringLines = new ArrayList<>();
    private ScheduledExecutorService scheduledExecutorService = null;
    private float viewWidth =0;
    /**
     * @param context <see>默认滚动</see>
     */
    public TextSurfaceView(Context context) {
        super(context);
        init();
    }

    public TextSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        //设置画布背景不为黑色　继承Sureface时这样处理才能透明
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        //背景色
        setBackgroundColor(bgColor);
        //设置透明
//        getBackground().setAlpha(bgalpha);
    }


    /**
     * @param context
     * @param move    <see>是否滚动</see>
     */
    public TextSurfaceView(Context context, boolean move) {
        this(context);
        this.isMove = move;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        draw();
    }

    public void surfaceCreated(SurfaceHolder holder) {

        y = getHeight() / 2 - getFontHeight(this.fontSize) / 2 +5;
        viewWidth =getWidth();

        Paint paint = new Paint();
        if (!TextUtils.isEmpty(content)){
            getTextInfo();
        }
//            textContentWidth = paint.measureText(content);
//        textHeigth = getFontHeight(this.fontSize);

        if (isMove) {//滚动效果
//            JLog.i("surfaceCreated:",textContentWidth+"");

            if (orientation == MOVE_LEFT) {
                x = viewWidth;
            } else if (orientation == MOVE_RIGHT) {
                x = -(content.length() * 10);
            } else if (orientation == MOVE_TOP) {
                x = viewWidth / 2 - (textContentWidth) / 2;
                y = getHeight() / 2 - textHeigth / 2;
            } else {
                x = viewWidth / 2 - (textContentWidth) / 2;
                y = getHeight() / 2 - textHeigth / 2;
            }

            beginSchedule();
        } else {//不滚动只画一次
            draw();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        closeSchedule();
    }

    /**
     * 画图
     */
    private void draw() {
        //锁定画布
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (mSurfaceHolder == null || canvas == null) {
            return;
        }

        Paint paint = new Paint();
        //清屏
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        //锯齿
        paint.setAntiAlias(true);
        //字体
        paint.setTypeface(Typeface.SANS_SERIF);
        //字体大小
        paint.setTextSize(fontSize);
        //字体颜色
        paint.setColor(fontColor);
        //字体透明度
        paint.setAlpha(fontAlpha);
        viewWidth =getWidth();
        //滚动效果

        if (isMove) {
            //内容所占像素
            float conlen = textContentWidth;
            //组件宽度
            float w = viewWidth;
            //方向
            if (orientation == MOVE_LEFT) {//向左
                if (x < -conlen) {
                    x = w - 5;
                } else {
                    x -= 2;
                }
            } else if (orientation == MOVE_RIGHT) {//向右
			if (x >= w - 5) {
			    x = -conlen;
			} else {
			    x += 2;
			}
	    } else if (orientation == MOVE_TOP) {//向上
			x = w/ 2 - (conlen) / 2;
			if (y < -textHeigth) {
			    y = getHeight() - 5;
			} else {
			    y -= 2;
			}
	    } else if (orientation == MOVE_BOTTOM) {//向下
			if (y >= textHeigth) {
			    y = -getHeight() - 5;
			} else {
			    y += 2;
			}
		    }
		} else {
		    //内容所占像素
		    float conlen = textContentWidth;
		    //组件宽度
		    float w = viewWidth;

		    if (staticOri == STATIC_LEFT) {
			x = 0;
		    } else if (staticOri == STATIC_CENTER) {
			x = w / 2 - conlen / 2;
		    } else if (staticOri == STATIC_RIGHT) {
			x = w - conlen;
		    }
		}
		//画文字
	//        canvas.drawText(content, x, y, paint);
		for (int i = 0; i < stringLines.size(); i++) {

		    canvas.drawText(stringLines.get(i), x,
			    y+ textFontHeight * i, paint);
		}
		//解锁显示
		mSurfaceHolder.unlockCanvasAndPost(canvas);
	    }

	    private void closeSchedule() {
		if (scheduledExecutorService != null) {
		    scheduledExecutorService.shutdown();
		    scheduledExecutorService = null;
		}
	    }

	    private void beginSchedule() {
		if (scheduledExecutorService == null) {
		    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		    scheduledExecutorService.scheduleAtFixedRate(new ScrollTextThread(), 1000, 10, TimeUnit.MILLISECONDS);
		}
	    }

	    class ScrollTextThread implements Runnable {
		@Override
		public void run() {
		    // TODO Auto-generated method stub
		    synchronized (mSurfaceHolder) {
			draw();
		    }
		}
	    }


	    /******************************
	     * set get method
	     ***********************************/

	    private int getOrientation() {
		return orientation;
	    }

	    /**
	     * @param orientation <li>可以选择类静态变量</li>
	     *                    <li>1.MOVE_RIGHT 向右 </li>
	     *                    <li>2.MOVE_LEFT  向左 (默认)</li>
	     */
	    public void setOrientation(int orientation) {
		this.orientation = orientation;
	    }

	    /**
	     * @param staticOri <li>choose</li>
	     *                  <li> STATIC_LEFT STATIC_CENTER STATIC_RIGHT </li>
	     */
	    public void setStaticOri(int staticOri) {
		this.staticOri = staticOri;
	    }

	    private long getSpeed() {
		return speed;
	    }

	    /**
     * @param speed <li>速度以毫秒计算两次移动之间的时间间隔</li>
     *              <li>默认为 1500 毫秒</li>
     *              <li>现在速度没有使用</li>
     */
    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public boolean isMove() {
        return isMove;
    }

    /**
     * @param isMove <see>默认滚动</see>
     */
    public void setMove(boolean isMove) {
        if (this.isMove == isMove) {
            return;
        }
        this.isMove = isMove;
        if (isMove) {
            beginSchedule();
        } else {
            closeSchedule();
        }
    }

    public void tryDraw() {
        draw();
    }

    public void setContent(String content) {
        this.content = content;
        getTextInfo();
    }

    private void getTextInfo() {
        char ch;
        int istart = 0;
        int w=0;
        String tmpStr="";
        if(viewWidth ==0){
            stringLines.add(content);
            return;
        }
        float width =viewWidth;
        textFontHeight = getFontHeight(fontSize);
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        int count = content.length();
        stringLines.clear();
        float maxWidth =0;
        float tmpWidth =0;
        for (int i = 0; i < count; i++) {
            ch = content.charAt(i);
            float[] widths = new float[1];
            String str = String.valueOf(ch);
            paint.getTextWidths(str, widths);
            if (ch == '\n') {
                tmpStr =content.substring(istart, i);
                stringLines.add(tmpStr);
                tmpWidth =paint.measureText(tmpStr);
                maxWidth =maxWidth >tmpWidth ? maxWidth: tmpWidth;
                istart = i + 1;
                w=0;
            } else {
                w += (int) Math.ceil(widths[0]);
                if(w > (width-60)){
                    tmpStr =content.substring(istart, i);
                    stringLines.add(tmpStr);
                    tmpWidth =paint.measureText(tmpStr);
                    maxWidth =maxWidth >tmpWidth ? maxWidth: tmpWidth;
                    istart=i;
                    w=0;
                }else if (i == count - 1) {
                    tmpStr =content.substring(istart, count);
                    stringLines.add(tmpStr);
                    tmpWidth =paint.measureText(tmpStr);
                    maxWidth =maxWidth >tmpWidth ? maxWidth: tmpWidth;
                }

            }
        }
        textHeigth =textFontHeight *stringLines.size();
        textContentWidth =maxWidth;
        y = getHeight() / 2 - textHeigth / 2  +5;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = Color.parseColor(bgColor);
        if (mSurfaceHolder != null) {
            setZOrderOnTop(true);
//            setZOrderMediaOverlay(true);
            mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
            //背景色
            setBackgroundColor(this.bgColor);
        }
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        if (mSurfaceHolder != null) {
            setZOrderOnTop(true);
//            setZOrderMediaOverlay(true);
            mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
            //背景色
            setBackgroundColor(this.bgColor);
        }
    }

    public void setBgalpha(int bgalpha) {
        this.bgalpha = bgalpha;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = Color.parseColor(fontColor);
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public void setFontAlpha(int fontAlpha) {
        this.fontAlpha = fontAlpha;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }


    public int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

}
