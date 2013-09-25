package com.comic.seexian.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.comic.seexian.Loge;
import com.comic.seexian.R;

public class PullDownRefreashListView extends ListView {

	private static final float PULL_RESISTANCE = 1.7f;
	private static final int BOUNCE_ANIMATION_DURATION = 300;
	private static final int BOUNCE_ANIMATION_DELAY = 100;
	private static final float BOUNCE_OVERSHOOT_TENSION = 1.4f;

	private static enum State {
		PULL_TO_REFRESH, RELEASE_TO_REFRESH, REFRESHING
	}

	/**
	 * Interface to implement when you want to get notified of 'pull to refresh'
	 * events. Call setOnRefreshListener(..) to activate an OnRefreshListener.
	 */
	public interface OnRefreshListener {

		/**
		 * Method to be called when a refresh is requested
		 */
		public void onRefresh();
	}

	private static int measuredHeaderHeight;

	private boolean scrollbarEnabled;
	private boolean bounceBackHeader;
	private boolean lockScrollWhileRefreshing;
	private String pullToRefreshText;
	private String releaseToRefreshText;
	private String refreshingText;

	private float previousY;
	private int headerPadding;
	private boolean hasResetHeader;
	private State state;
	private View headerContainer;
	private View header;
	private TextView text;
	private TextView lastUpdatedTextView;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;
	private OnRefreshListener onRefreshListener;

	private View mRefreshHorizontalView;
	private View mRefreshHorizontalImage;
	private View mRefreshHorizontalProgress;
	private int mHalfSreenWidth;

	private float mScrollStartY;
	private final int IDLE_DISTANCE = 5;

	public PullDownRefreashListView(Context context) {
		super(context);
		init(context);
	}

	public PullDownRefreashListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullDownRefreashListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void addCustomView(View refreshHorizontalView,
			View refreshHorizontalImage, View refreshHorizontalProgress) {
		mRefreshHorizontalView = refreshHorizontalView;
		mRefreshHorizontalImage = refreshHorizontalImage;
		mRefreshHorizontalProgress = refreshHorizontalProgress;
		mRefreshHorizontalImage.setPadding(mHalfSreenWidth, 0, mHalfSreenWidth,
				0);
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	@Override
	public void setOnItemLongClickListener(
			OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	/**
	 * Activate an OnRefreshListener to get notified on 'pull to refresh'
	 * events.
	 * 
	 * @param onRefreshListener
	 *            The OnRefreshListener to get notified
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	/**
	 * @return If the list is in 'Refreshing' state
	 */
	public boolean isRefreshing() {
		return state == State.REFRESHING;
	}

	/**
	 * Default is false. When lockScrollWhileRefreshing is set to true, the list
	 * cannot scroll when in 'refreshing' mode. It's 'locked' on refreshing.
	 * 
	 * @param lockScrollWhileRefreshing
	 */
	public void setLockScrollWhileRefreshing(boolean lockScrollWhileRefreshing) {
		this.lockScrollWhileRefreshing = lockScrollWhileRefreshing;
	}

	public void setLastUpdatedText(String text) {
		lastUpdatedTextView.setText(text);
	}

	/**
	 * Explicitly set the state to refreshing. This is useful when you want to
	 * show the spinner and 'Refreshing' text when the refresh was not triggered
	 * by 'pull to refresh', for example on start.
	 */
	public void setRefreshing() {
		state = State.REFRESHING;
		scrollTo(0, 0);
		setUiRefreshing();
		setHeaderPadding(0);
	}

	/**
	 * Set the state back to 'pull to refresh'. Call this method when refreshing
	 * the data is finished.
	 */
	public void onRefreshComplete() {
		state = State.PULL_TO_REFRESH;
		resetHeader();

		mRefreshHorizontalView.setVisibility(View.GONE);
		mRefreshHorizontalImage.setVisibility(View.VISIBLE);
		mRefreshHorizontalProgress.setVisibility(View.GONE);
		mRefreshHorizontalImage.setPadding(mHalfSreenWidth, 0, mHalfSreenWidth,
				0);
	}

	/**
	 * Change the label text on state 'Pull to Refresh'
	 * 
	 * @param pullToRefreshText
	 *            Text
	 */
	public void setTextPullToRefresh(String pullToRefreshText) {
		this.pullToRefreshText = pullToRefreshText;
		if (state == State.PULL_TO_REFRESH) {
			text.setText(pullToRefreshText);
		}
	}

	/**
	 * Change the label text on state 'Release to Refresh'
	 * 
	 * @param releaseToRefreshText
	 *            Text
	 */
	public void setTextReleaseToRefresh(String releaseToRefreshText) {
		this.releaseToRefreshText = releaseToRefreshText;
		if (state == State.RELEASE_TO_REFRESH) {
			text.setText(releaseToRefreshText);
		}
	}

	/**
	 * Change the label text on state 'Refreshing'
	 * 
	 * @param refreshingText
	 *            Text
	 */
	public void setTextRefreshing(String refreshingText) {
		this.refreshingText = refreshingText;
		if (state == State.REFRESHING) {
			text.setText(refreshingText);
		}
	}

	private void init(Context context) {
		setVerticalFadingEdgeEnabled(false);

		headerContainer = (View) LayoutInflater.from(getContext()).inflate(
				R.layout.pull_to_refresh_header, null);
		header = (View) headerContainer.findViewById(R.id.ptr_id_header);
		text = (TextView) header.findViewById(R.id.ptr_id_text);
		lastUpdatedTextView = (TextView) header
				.findViewById(R.id.ptr_id_last_updated);

		pullToRefreshText = getContext()
				.getString(R.string.ptr_pull_to_refresh);
		releaseToRefreshText = getContext().getString(
				R.string.ptr_release_to_refresh);
		refreshingText = getContext().getString(R.string.ptr_refreshing);

		addHeaderView(headerContainer);
		setState(State.PULL_TO_REFRESH);
		scrollbarEnabled = isVerticalScrollBarEnabled();

		ViewTreeObserver vto = header.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new PTROnGlobalLayoutListener());

		super.setOnItemClickListener(new PTROnItemClickListener());
		super.setOnItemLongClickListener(new PTROnItemLongClickListener());

		mHalfSreenWidth = context.getResources().getDisplayMetrics().widthPixels / 2;
	}

	private void setHeaderPadding(int padding) {
		headerPadding = padding;

		MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) header
				.getLayoutParams();
		mlp.setMargins(0, Math.round(padding), 0, 0);
		header.setLayoutParams(mlp);
	}

	private void applyRefreshImagePadding(MotionEvent ev) {

		int pointerCount = ev.getHistorySize();

		for (int p = 0; p < pointerCount; p++) {

			int historicalY = (int) ev.getHistoricalY(p);

			int padding = (int) ((historicalY - mScrollStartY) / 1.2);

			mRefreshHorizontalView.setVisibility(View.VISIBLE);
			if (!(padding > mHalfSreenWidth)) {
				mRefreshHorizontalImage.setPadding(mHalfSreenWidth - padding,
						0, mHalfSreenWidth - padding, 0);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (lockScrollWhileRefreshing
				&& (state == State.REFRESHING || getAnimation() != null
						&& !getAnimation().hasEnded())) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (getFirstVisiblePosition() == 0) {
				previousY = event.getY();
			} else {
				previousY = -1;
			}

			// Remember where have we started
			mScrollStartY = event.getY();

			break;

		case MotionEvent.ACTION_UP:
			if (previousY != -1
					&& (state == State.RELEASE_TO_REFRESH || getFirstVisiblePosition() == 0)) {
				switch (state) {
				case RELEASE_TO_REFRESH:
					setState(State.REFRESHING);
					bounceBackHeader();

					break;

				case PULL_TO_REFRESH:
					resetHeader();
					break;
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (previousY != -1 && getFirstVisiblePosition() == 0
					&& Math.abs(mScrollStartY - event.getY()) > IDLE_DISTANCE) {
				float y = event.getY();
				float diff = y - previousY;
				if (diff > 0)
					diff /= PULL_RESISTANCE;
				previousY = y;

				int newHeaderPadding = Math.max(
						Math.round(headerPadding + diff), -header.getHeight());

				if (newHeaderPadding != headerPadding
						&& state != State.REFRESHING) {
					setHeaderPadding(newHeaderPadding);
					applyRefreshImagePadding(event);

					if (state == State.PULL_TO_REFRESH && headerPadding > 0) {
						setState(State.RELEASE_TO_REFRESH);

					} else if (state == State.RELEASE_TO_REFRESH
							&& headerPadding < 0) {
						setState(State.PULL_TO_REFRESH);

					}
				}
			}

			break;
		}

		return super.onTouchEvent(event);
	}

	private void bounceBackHeader() {
		int yTranslate = state == State.REFRESHING ? header.getHeight()
				- headerContainer.getHeight() : -headerContainer.getHeight()
				- headerContainer.getTop() + getPaddingTop();

		TranslateAnimation bounceAnimation = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE,
				yTranslate);

		bounceAnimation.setDuration(BOUNCE_ANIMATION_DURATION);
		bounceAnimation.setFillEnabled(true);
		bounceAnimation.setFillAfter(false);
		bounceAnimation.setFillBefore(true);
		bounceAnimation.setInterpolator(new OvershootInterpolator(
				BOUNCE_OVERSHOOT_TENSION));
		bounceAnimation.setAnimationListener(new HeaderAnimationListener(
				yTranslate));

		startAnimation(bounceAnimation);

		mRefreshHorizontalView.setVisibility(View.VISIBLE);
		mRefreshHorizontalImage.setPadding(mHalfSreenWidth, 0, mHalfSreenWidth,
				0);
	}

	private void resetHeader() {
		if (getFirstVisiblePosition() > 0) {
			setHeaderPadding(-header.getHeight());
			setState(State.PULL_TO_REFRESH);
			return;
		}

		if (getAnimation() != null && !getAnimation().hasEnded()) {
			bounceBackHeader = true;
		} else {
			bounceBackHeader();
		}
	}

	private void setUiRefreshing() {
		text.setText(refreshingText);
		mRefreshHorizontalImage.setVisibility(View.GONE);
		mRefreshHorizontalProgress.setVisibility(View.VISIBLE);
		lastUpdatedTextView.setVisibility(View.GONE);
	}

	private void setState(State state) {
		this.state = state;
		switch (state) {
		case PULL_TO_REFRESH:
			text.setText(pullToRefreshText);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		case RELEASE_TO_REFRESH:
			text.setText(releaseToRefreshText);
			break;
		case REFRESHING:
			setUiRefreshing();
			if (onRefreshListener == null) {
				setState(State.PULL_TO_REFRESH);
			} else {
				onRefreshListener.onRefresh();
			}
			break;
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (!hasResetHeader) {
			if (measuredHeaderHeight > 0 && state != State.REFRESHING) {
				setHeaderPadding(-measuredHeaderHeight);
			}

			hasResetHeader = true;
		}
	}

	private class HeaderAnimationListener implements AnimationListener {

		private int height, translation;
		private State stateAtAnimationStart;

		public HeaderAnimationListener(int translation) {
			this.translation = translation;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			stateAtAnimationStart = state;

			android.view.ViewGroup.LayoutParams lp = getLayoutParams();
			height = lp.height;
			lp.height = getHeight() - translation;
			setLayoutParams(lp);

			if (scrollbarEnabled) {
				setVerticalScrollBarEnabled(false);
			}
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			setHeaderPadding(stateAtAnimationStart == State.REFRESHING ? 0
					: -measuredHeaderHeight - headerContainer.getTop());
			setSelection(0);

			android.view.ViewGroup.LayoutParams lp = getLayoutParams();
			lp.height = height;
			setLayoutParams(lp);

			if (scrollbarEnabled) {
				setVerticalScrollBarEnabled(true);
			}

			if (bounceBackHeader) {
				bounceBackHeader = false;

				postDelayed(new Runnable() {

					@Override
					public void run() {
						resetHeader();
					}
				}, BOUNCE_ANIMATION_DELAY);
			} else if (stateAtAnimationStart != State.REFRESHING) {
				setState(State.PULL_TO_REFRESH);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	}

	private class PTROnGlobalLayoutListener implements OnGlobalLayoutListener {

		@Override
		public void onGlobalLayout() {
			int initialHeaderHeight = header.getHeight();
			Loge.i("initialHeaderHeight = " + initialHeaderHeight);

			if (initialHeaderHeight > 0) {
				measuredHeaderHeight = initialHeaderHeight;

				if (measuredHeaderHeight > 0 && state != State.REFRESHING) {
					setHeaderPadding(-measuredHeaderHeight);
					requestLayout();
				}
			}

			getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
	}

	private class PTROnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				int position, long id) {
			hasResetHeader = false;

			if (onItemClickListener != null && state == State.PULL_TO_REFRESH) {
				// Passing up onItemClick. Correct position with the number of
				// header views
				onItemClickListener.onItemClick(adapterView, view, position
						- getHeaderViewsCount(), id);
			}
		}
	}

	private class PTROnItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view,
				int position, long id) {
			hasResetHeader = false;

			if (onItemLongClickListener != null
					&& state == State.PULL_TO_REFRESH) {
				// Passing up onItemLongClick. Correct position with the number
				// of header views
				return onItemLongClickListener.onItemLongClick(adapterView,
						view, position - getHeaderViewsCount(), id);
			}

			return false;
		}
	}
}
