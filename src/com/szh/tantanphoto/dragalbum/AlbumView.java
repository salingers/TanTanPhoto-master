package com.szh.tantanphoto.dragalbum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.szh.tantanphoto.dragalbum.entity.PhotoItem;
import com.szh.tantanphoto.dragalbum.util.ImageUtils;
import com.szh.tantanphoto.dragalbum.util.StringUtils;

/**
 * 模仿探探 相册View
 * 
 * @author szh QQ1095897632
 * 
 */
public class AlbumView extends ViewGroup implements OnTouchListener {
	/**
	 * View集合
	 */
	private List<View> views = new ArrayList<View>();
	/**
	 * PhotoItem集合
	 */
	private List<PhotoItem> images = new ArrayList<PhotoItem>();

	/**
	 * 一个图片处理类
	 */
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	/**
	 * 图片的配置信息
	 */
	private DisplayImageOptions mImageOptions;
	/**
	 * 动画是否结束标记
	 */
	private boolean mAnimationEnd = true;

	/**
	 * 第一个最大的view的宽高
	 */
	private int mItmeOne;
	/**
	 * item 其余宽高
	 */
	private int ItemWidth;

	private int hidePosition = -1;
	/**
	 * 根据数据 获取的 最大可拖拽移动换位的范围
	 */
	private int maxSize;
	/**
	 * 当前控件 距离屏幕 顶点 的高度
	 */
	private int mTopHeight = -1;
	/**
	 * 每个item之间的间隙
	 */
	public int padding = -1;

	/**
	 * 正在拖拽的view的position(第几个子类)
	 */
	private int mDragPosition;
	/**
	 * 按下的X点
	 */
	private int mDownX;
	/**
	 * 按下的Y点
	 */
	private int mDownY;
	/**
	 * 是否点下显示镜像(是否是点击)
	 */
	private boolean isOnItemClick = false;

	/**
	 * 刚开始拖拽的item对应的View
	 */
	private View mStartDragItemView = null;

	/**
	 * 用于拖拽的镜像，这里直接用一个ImageView
	 */
	private ImageView mDragImageView;

	/**
	 * 我们拖拽的item对应的Bitmap
	 */
	private Bitmap mDragBitmap;

	/**
	 * 点击item的宽的中点
	 */
	private int dragPointX;
	/**
	 * 点击item的高的中点
	 */
	private int dragPointY;
	/**
	 * x坐标移动的距离
	 */
	private int dragOffsetX;
	/**
	 * y坐标移动的距离
	 */
	private int dragOffsetY;

	private GridLayout RootView;

	/**
	 * 为了兼容小米那个日了狗的系统 就不用 WindowManager了
	 * 
	 * @param rootView
	 */
	public void setRootView(GridLayout rootView) {
		System.out.println("222222222222222222---将布局里面的一个GridLayout给传进来了");
		RootView = rootView;
	}

	public AlbumView(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.out.println("4444444444444444444444----配置图片信息和初始化padding值");
		mImageOptions = ImageUtils.getFaceVideoOptions();
		padding = dp2px(4, context);
	}

	/**
	 * 滑动时的X点
	 */
	int moveX;
	/**
	 * 滑动时的Y点
	 */
	int moveY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		System.out.println("66666666666666666666666666666---点击item触发触摸事件");
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHandler.removeCallbacks(mDragRunnable);

			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			mDragPosition = pointToPosition(mDownX, mDownY);
			/**
			 * 判断获取的这个组件是否超出可以滑动的组件范围，如果超出了将分发事件
			 */
			if (mDragPosition > maxSize) {
				return super.dispatchTouchEvent(ev);
			}
			/**
			 * 判断触摸的组件是否符合范围，不符合 将分发事件
			 */
			if (mDragPosition == -1) {
				return super.dispatchTouchEvent(ev);
			}
			/**
			 * 根据position获取该item所对应的View
			 */
			mStartDragItemView = getChildAt(mDragPosition);
			/**
			 * 设置不销毁此View的cach
			 */
			mStartDragItemView.setDrawingCacheEnabled(true);
			/**
			 * 获取此View的BitMap对象
			 */
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
					.getDrawingCache());
			/**
			 * 销毁cache
			 */
			mStartDragItemView.destroyDrawingCache();

			dragPointX = mStartDragItemView.getWidth() / 2;
			dragPointY = mStartDragItemView.getHeight() / 2;
			dragOffsetX = (int) (ev.getRawX() - mDownX);
			dragOffsetY = (int) (ev.getRawY() - mDownY);
			/**
			 * 将多线程加入消息队列并延迟50毫秒执行
			 */
			mHandler.postDelayed(mDragRunnable, 50);
			break;
		case MotionEvent.ACTION_MOVE:
			moveX = (int) ev.getX();
			moveY = (int) ev.getY();
			if (mDragImageView != null) {
				onDragItem(moveX - dragPointX + dragOffsetX, moveY - dragPointY
						+ dragOffsetY - mTopHeight);
				onSwapItem(moveX, moveY);
			}
			break;
		case MotionEvent.ACTION_UP:
			onStopDrag();
			mHandler.removeCallbacks(mDragRunnable);
			break;
		case MotionEvent.ACTION_CANCEL:
			onStopDrag();
			mHandler.removeCallbacks(mDragRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private Handler mHandler = new Handler();

	/**
	 * 用来处理是否为长按的Runnable
	 */
	private Runnable mDragRunnable = new Runnable() {
		@Override
		public void run() {
			System.out.println("777777777777777777777777777");
			/**
			 * 根据我们按下的点显示item镜像
			 */
			System.out.println(isOnItemClick);
			if (isOnItemClick)
				return;
			createDragImage();
			mStartDragItemView.setVisibility(View.GONE);
		}

	};

	private Rect mTouchFrame;

	/**
	 * 判断按下的位置是否在Item上 并返回Item的位置,即是第几的一个item
	 * {@link AbsListView #pointToPosition(int, int)}
	 */
	public int pointToPosition(int x, int y) {
		System.out.println("888888888888888888888888888888");
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}
		int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 创建拖动的镜像
	 * 
	 * @param bitmap
	 * @param downX
	 *            按下的点相对父控件的X坐标
	 * @param downY
	 *            按下的点相对父控件的X坐标
	 */
	private void createDragImage() {
		System.out.println("9999999999999999999999999999999999");
		int[] location = new int[2];
		mStartDragItemView.getLocationOnScreen(location);
		float drX = location[0];
		float drY = location[1] - mTopHeight;
		/**
		 * 创建一个ImageView并将你点击的那一个item的Bitmap存进去
		 */
		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(mDragBitmap);
		RootView.addView(mDragImageView);

		int drH = (int) (ItemWidth * 0.8);
		float w = mStartDragItemView.getWidth();
		final float scale = drH / w;
		createTranslationAnimations(mDragImageView, drX,
				mDownX - dragPointX + dragOffsetX, drY,
				mDownY - dragPointY + dragOffsetY - mTopHeight, scale, scale)
				.setDuration(200).start();
	}

	/**
	 * 从界面上面移除拖动镜像
	 */
	private void removeDragImage() {
		System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		if (mDragImageView != null) {
			RootView.removeView(mDragImageView);
			mDragImageView = null;
			if (mStartDragItemView != null)
				mStartDragItemView.setVisibility(View.VISIBLE);
		}
	}

	class resultSetListenerAdapter implements AnimatorListener {
		View mStartDragItemView, mDragImageView;
		boolean isStart;

		public resultSetListenerAdapter(View mStartDragItemView,
				View mDragImageView, boolean isStart) {
			System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
			// TODO Auto-generated constructor stub
			this.mStartDragItemView = mStartDragItemView;
			this.mDragImageView = mDragImageView;
			this.isStart = isStart;
		}

		@Override
		public void onAnimationStart(android.animation.Animator animation) {
			System.out.println("ccccccccccccccccccccccccccccccc");
			// TODO Auto-generated method stub
			if (isStart) {
				mStartDragItemView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onAnimationEnd(android.animation.Animator animation) {
			System.out.println("ddddddddddddddddddddddddddddddddddd");
			// TODO Auto-generated method stub
			if (!isStart) {
				mStartDragItemView.setVisibility(View.VISIBLE);
				RootView.removeView(mDragImageView);
				mDragImageView = null;
			}
		}

		@Override
		public void onAnimationCancel(android.animation.Animator animation) {
			// TODO Auto-generated method stub
			System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		}

		@Override
		public void onAnimationRepeat(android.animation.Animator animation) {
			// TODO Auto-generated method stub
			System.out.println("ffffffffffffffffffffffffffffffff");
		}

	}

	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * 
	 * @param x
	 * @param y
	 */
	private void onDragItem(int X, int Y) {
		System.out.println("ggggggggggggggggggggggggggggggggggg");
		if (mDragImageView != null) {
			ViewHelper.setTranslationX(mDragImageView, X);
			ViewHelper.setTranslationY(mDragImageView, Y);
		}
	}

	/**
	 * 交换item
	 * 
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
		if (mDragImageView != null) {
			/**
			 * 获取移动时经过的坐标是第几个item
			 */
			int tempPosition = pointToPosition(moveX, moveY);
			/**
			 * 如果这个大于最大的那个那么就不做为什么，如果小于则判断是否是自己那一个View，是否不存在，交换动画是否执行完毕
			 */
			if (tempPosition > maxSize) {
				return;
			}
			if (tempPosition != mDragPosition && tempPosition != -1
					&& mAnimationEnd) {
				animateReorder(mDragPosition, tempPosition);
			}
		}
	}

	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 * 
	 * @param moveY
	 * @param moveX
	 */
	private void onStopDrag() {
		System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiii");
		removeDragImage();
		hidePosition = -1;
	}

	/**
	 * 获取当前控件 距离屏幕 顶点 的高度
	 * 
	 * @param context
	 * @return
	 */
	private int getTopHeight(Context context) {
		System.out.println("jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
		int statusHeight = 0;
		Rect ViewRect = new Rect();
		getGlobalVisibleRect(ViewRect);
		statusHeight = ViewRect.top;
		if (0 == statusHeight) {
			Rect localRect = new Rect();
			getWindowVisibleDisplayFrame(localRect);
			statusHeight = localRect.top;
			if (0 == statusHeight) {
				Class<?> localClass;
				try {
					localClass = Class.forName("com.android.internal.R$dimen");
					Object localObject = localClass.newInstance();
					int i5 = Integer.parseInt(localClass
							.getField("status_bar_height").get(localObject)
							.toString());
					statusHeight = context.getResources()
							.getDimensionPixelSize(i5);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 注意 如果上边方法 未能 获取成功 那么 请根据个人 应用情况 加上相应的值
			// 比如 +45 我加的是一个 大概Title 的值
			// 如果当前控件 上边 有其他控件 请加上其他控件的高度
			statusHeight += dp2px(45, context);
		}

		return statusHeight;
	}

	public int getItemWidth() {
		System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
		return ItemWidth;
	}

	public void setImages(List<PhotoItem> images) {
		System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLL---将一个PhotoItem集合传进来了");
		this.images = images;
		initUI();
	}

	public List<PhotoItem> getImages() {
		System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
		List<PhotoItem> temimages = new ArrayList<PhotoItem>();
		for (PhotoItem Item : images) {
			if (!StringUtils.isEmpty(Item.hyperlink)) {
				temimages.add(Item);
			}
		}
		return temimages;
	}

	/**
	 * 初始化View集合，并向views里面添加数据
	 */
	public void initUI() {
		System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnnnnn----开始给view集合添加数据");
		/**
		 * 清空集合
		 */
		views.clear();
		/**
		 * 清楚所有的view对象
		 */
		removeAllViews();
		for (int i = 0; i < images.size(); i++) {
			ImageView view = new ImageView(getContext());
			/**
			 * 给ImageView设置填充父类布局的属性
			 */
			view.setScaleType(ScaleType.FIT_XY);
			if (!StringUtils.isEmpty(images.get(i).hyperlink)) {
				maxSize = i;
			}
			mImageLoader.displayImage(images.get(i).hyperlink, view,
					mImageOptions);
			views.add(view);
			addView(view);
		}
		initListener();
	}

	private void initListener() {
		System.out
				.println("ooooooooooooooooooooooo----从给Views取出View对象设置tag和触摸事件");
		for (int i = 0; i < views.size(); i++) {
			View view = views.get(i);
			view.setTag(i);
			view.setOnTouchListener(this);
		}
	}

	/**
	 * 创建移动动画
	 * 
	 * @param view
	 *            动画执行的View
	 * @param startX
	 *            动画开始的X坐标
	 * @param endX
	 *            结束时的X坐标
	 * @param startY
	 *            开始时的Y坐标
	 * @param endY
	 *            结束时的Y坐标
	 * @return 返回一个动画集合
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY) {
		System.out.println("pppppppppppppppppppppppppppppppppppppp");
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(ObjectAnimator.ofPropertyValuesHolder(view,
				PropertyValuesHolder.ofFloat("translationX", startX, endX),
				PropertyValuesHolder.ofFloat("translationY", startY, endY)));
		return animSetXY;
	}

	/**
	 * 缩放动画加平移动画
	 * 
	 * @param view
	 *            将要执行动画的View组件
	 * @param startX
	 *            开始时的X坐标
	 * @param endX
	 *            结束时的X坐标
	 * @param startY
	 *            开始时的Y坐标
	 * @param endY
	 *            结束时的Y坐标
	 * @param scaleX
	 *            X轴的缩放比例
	 * @param scaleY
	 *            Y轴的缩放比列
	 * @return 返回一个动画集合
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY, float scaleX, float scaleY) {
		System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(ObjectAnimator.ofPropertyValuesHolder(view,
				PropertyValuesHolder.ofFloat("translationX", startX, endX),
				PropertyValuesHolder.ofFloat("translationY", startY, endY),
				PropertyValuesHolder.ofFloat("scaleX", 1.0f, scaleX),
				PropertyValuesHolder.ofFloat("scaleY", 1.0f, scaleY)));
		return animSetXY;
	}

	public boolean IsOneTwo(int Position) {
		System.out.println("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
		return Position == 1 || Position == 0;
	}

	@SuppressWarnings("unchecked")
	public void swap(List<?> List, int index1, int index2) {
		System.out.println("sssssssssssssssssssssssssssssssss");
		List<Object> rawList = (java.util.List<Object>) List;
		rawList.set(index2, rawList.set(index1, rawList.get(index2)));
	}

	/**
	 * item的交换动画效果
	 * 
	 * @param oldPosition
	 *            正在拖拽的那一个View的编号
	 * @param newPosition
	 *            当前触摸到的那个组件的编号
	 */
	public void animateReorder(int oldPosition, int newPosition) {
		System.out.println("ttttttttttttttttttttttttttttttttt");
		/**
		 * 判断触摸到的坐标的那一个View的编号是否大于现在正在拖拽的那一个坐标
		 */
		boolean isForward = newPosition > oldPosition;
		final List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition + 1; pos <= newPosition; pos++) {
				View view = getChildAt(pos);
				if (pos == 1) {
					float h = view.getWidth() / 2;
					float mSpacing = padding / 2;
					float w = getChildAt(0).getWidth();
					float scale = w / view.getWidth();
					resultList.add(createTranslationAnimations(view, 0,
							-(view.getWidth() + padding + mSpacing + h), 0, h
									+ mSpacing, scale, scale));
					swap(images, pos, pos - 1);
				}
				if (pos == 2) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0,
							-(view.getWidth() + padding)));
					swap(images, pos, pos - 1);
				}
				if (pos == 3) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0,
							-(view.getWidth() + padding)));
					swap(images, pos, pos - 1);
				}
				if (pos == 4) {
					resultList.add(createTranslationAnimations(view, 0,
							view.getWidth() + padding, 0, 0));
					swap(images, pos, pos - 1);
				}
				if (pos == 5) {
					resultList.add(createTranslationAnimations(view, 0,
							view.getWidth() + padding, 0, 0));
					swap(images, pos, pos - 1);
				}
			}
		} else {
			for (int pos = newPosition; pos < oldPosition; pos++) {
				View view = getChildAt(pos);
				if (pos == 0) {
					float h = getChildAt(1).getWidth() / 2;
					float mSpacing = padding / 2;
					float w = getChildAt(0).getWidth();
					float scale = getChildAt(1).getWidth() / w;
					resultList.add(createTranslationAnimations(view, 0,
							getChildAt(1).getWidth() + padding + mSpacing + h,
							0, -(h + mSpacing), scale, scale));
				}
				if (pos == 1) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0,
							view.getWidth() + padding));
				}
				if (pos == 2) {
					resultList.add(createTranslationAnimations(view, 0, 0, 0,
							view.getWidth() + padding));
				}
				if (pos == 3) {
					resultList.add(createTranslationAnimations(view, 0,
							-(view.getWidth() + padding), 0, 0));
				}
				if (pos == 4) {
					resultList.add(createTranslationAnimations(view, 0,
							-(view.getWidth() + padding), 0, 0));
				}
			}
			for (int i = oldPosition; i > newPosition; i--) {
				swap(images, i, i - 1);
			}
		}

		hidePosition = newPosition;
		resultSet = new AnimatorSet();
		/**
		 * 给动画填充动画集
		 */
		resultSet.playTogether(resultList);
		/**
		 * 设置动画时间
		 */
		resultSet.setDuration(150);
		/**
		 * 设置其播放模式
		 */
		resultSet.setInterpolator(new OvershootInterpolator(1.6f));
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuu");
				// TODO Auto-generated method stub
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvv");
				// TODO Auto-generated method stub
				if (!mAnimationEnd) {
					initUI();
					resultSet.removeAllListeners();
					resultSet.clone();
					resultSet = null;
					mDragPosition = hidePosition;
				}
				mAnimationEnd = true;
			}
		});
		resultSet.start();
		resultList.clear();
	}

	AnimatorSet resultSet = null;
	/**
	 * 点击Item事件
	 */
	OnItemClickListener clickListener;

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		System.out
				.println("wwwwwwwwwwwwwwwwwwwwwwwwww---给AlbumView对象设置一个点击item事件");
		// TODO Auto-generated method stub
		clickListener = onItemClickListener;
	}

	public interface OnItemClickListener {
		public void ItemClick(View view, int position, boolean Photo);

	}

	/**
	 * 触摸时的x点坐标
	 */
	int ItemDownX;
	/**
	 * 触摸时Y点坐标
	 */
	int ItemDownY;
	/**
	 * 触摸开始时系统时间
	 */
	long strTime;

	/**
	 * 此方法用于判断是否是点击事件还是滑动
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			ItemDownX = (int) event.getX();
			ItemDownY = (int) event.getY();
			strTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_UP:
			/**
			 * 获取点击的是第几个item
			 */
			int mDragPosition = (Integer) v.getTag();
			/**
			 * 判断是点击的item是否超出了范围，如果超出了则响应点击事件
			 */
			if (mDragPosition <= maxSize) {
				/**
				 * 获取触摸时距离widget的原点的X坐标
				 */
				int moveX = (int) event.getX();
				int moveY = (int) event.getY();
				/**
				 * 计算按下去和抬起来X点坐标的绝对值
				 */
				float abslMoveDistanceX = Math.abs(moveX - ItemDownX);
				float abslMoveDistanceY = Math.abs(moveY - ItemDownY);
				/**
				 * 如果X和Y的绝对值都小雨2.0并且抬起的时间和按下的时间差小于50，并且点击事件不等于null,
				 * 则将触发item的点击事件，改变为点下的状态。并传出这个VIEW和下标。 否则改变为没有点下状态
				 */
				if (abslMoveDistanceX < 2.0 && abslMoveDistanceY < 2.0
						&& (System.currentTimeMillis() - strTime) < 50) {
					if (clickListener != null) {
						isOnItemClick = true;
						clickListener.ItemClick(getChildAt(mDragPosition),
								mDragPosition, true);
					} else {
						isOnItemClick = false;
					}
				} else {
					isOnItemClick = false;
				}
			} else {
				/**
				 * 如果不为null则响应点击事件并改为点击，否则改为没点击
				 */
				if (clickListener != null) {
					isOnItemClick = true;
					clickListener.ItemClick(getChildAt(mDragPosition),
							mDragPosition, false);
				} else {
					isOnItemClick = false;
				}
			}
			break;
		}
		return true;
	}

	// public void onResume() {
	// System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
	// initUI();
	// }
	//
	// public void onPause() {
	// System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzz");
	// mImageLoader.clearMemoryCache();
	// }
	//
	// public void onDestroy() {
	// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
	//
	// }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		System.out.println("##################################---绘制子View的位置");
		// TODO Auto-generated method stub
		/**
		 * 获取父容器的宽度
		 */
		int Width = getMeasuredWidth();
		/**
		 * 容器的宽度/3分-item之间的间隙
		 */
		ItemWidth = Width / 3 - padding - (padding / 3);
		System.out.println(l + "-" + t + "-" + r + "-" + b);
		for (int i = 0, size = getChildCount(); i < size; i++) {
			View view = getChildAt(i);
			if (i == 0) {
				mItmeOne = ItemWidth * 2 + padding;
				l += padding;
				t += padding;
				view.layout(l, t, l + mItmeOne, t + mItmeOne);
				l += mItmeOne + padding;
			}
			if (i == 1) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				t += ItemWidth + padding;
			}
			if (i == 2) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				t += ItemWidth + padding;
			}
			if (i >= 3) {
				view.layout(l, t, l + ItemWidth, t + ItemWidth);
				l -= ItemWidth + padding;
			}
			/**
			 * 如果当前绘制的view与拖动的view的是一样则让其隐藏
			 */
			if (i == hidePosition) {
				view.setVisibility(View.GONE);
				mStartDragItemView = view;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		System.out
				.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$---开始计算mAlbumView对象的宽高（由于布局改变因此可能被调用多次）");
		// TODO Auto-generated method stub
		int resWidth = 0;
		int resHeight = 0;
		/**
		 * 根据传入的参数，分别获取测量模式和测量值
		 */
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int height = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		/**
		 * 如果宽或者高的测量模式非精确值
		 */
		if (widthMode != MeasureSpec.EXACTLY
				|| heightMode != MeasureSpec.EXACTLY) {
			/**
			 * 主要设置为背景图的高度
			 */
			resWidth = getSuggestedMinimumWidth();
			/**
			 * 如果未设置背景图片，则设置为屏幕宽高的默认值
			 */
			resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

			resHeight = getSuggestedMinimumHeight();
			/**
			 * 如果未设置背景图片，则设置为屏幕宽高的默认值
			 */
			resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
		} else {
			/**
			 * 如果都设置为精确值，则直接取小值；
			 */
			resWidth = resHeight = Math.min(width, height);
		}

		setMeasuredDimension(resWidth, resHeight);
	}

	/**
	 * 获得默认该layout的尺寸
	 * 
	 * @return
	 */
	private int getDefaultWidth() {
		System.out
				.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%---获取该容器的默认尺寸进行计算容器大小");
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
	}

	/**
	 * 像素转换，dp转换为px
	 * 
	 * @param dp
	 * @param context
	 * @return
	 */
	public int dp2px(int dp, Context context) {
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^---像素转换---");
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				context.getResources().getDisplayMetrics());
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (mTopHeight <= 0) {
				mTopHeight = getTopHeight(getContext());
			}
		}
	}

}