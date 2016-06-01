package eu.inmite.apps.smsjizdenka.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.inmite.apps.smsjizdenka.R;
import eu.inmite.apps.smsjizdenka.dialog.DateDialogFragment;


public abstract class BaseFilterView extends LinearLayout {
    public static final long FILTER_ALL = -1;
    Context c;
    protected SelectedChangedListener mListener;
    protected long mSelected = FILTER_ALL;
    protected TextView vSelected;

    public BaseFilterView(Context context) {
        super(context);
    }

    public BaseFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BaseFilterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    protected void init() {
        View view = LayoutInflater.from(c).inflate(R.layout.view_filter, this, true);
        vSelected = (TextView)view.findViewById(R.id.selected);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        update();
    }

    public long getSelected() {
        return mSelected;

    }

    public void setSelected(long selected) {
        mSelected = selected;
        update();
    }

    public void setOnSelectedChangedListener(SelectedChangedListener listener) {
        mListener = listener;
    }

    protected abstract void showDialog();

    protected void showDialog(String title, boolean beginning) {
        long millis = mSelected;
        if (mSelected == FILTER_ALL) {
            millis = System.currentTimeMillis();
        }
        final DateDialogFragment dialogFragment = DateDialogFragment.newInstance(title,
                millis, beginning, new DateDialogFragment.DateSelectedListener() {
                    @Override
                    public void onDateSelected(long millis) {
                        if (mListener != null && mSelected != millis) {
                            mListener.onSelectedChanged(millis);
                        }
                        mSelected = millis;
                        update();
                    }
                });
        dialogFragment.show(((FragmentActivity)c).getSupportFragmentManager(), DateDialogFragment.TAG);
    }
    protected abstract void update();

    public interface SelectedChangedListener {
        public void onSelectedChanged(long selected);
    }
}
