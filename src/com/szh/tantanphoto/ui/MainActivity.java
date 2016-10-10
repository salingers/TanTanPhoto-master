package com.szh.tantanphoto.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;

import com.szh.tantanphoto.R;
import com.szh.tantanphoto.dragalbum.AlbumView;
import com.szh.tantanphoto.dragalbum.AlbumView.OnItemClickListener;
import com.szh.tantanphoto.dragalbum.entity.PhotoItem;
import com.szh.tantanphoto.dragalbum.log.L;
import com.szh.tantanphoto.dragalbum.util.DemoUtils;

public class MainActivity extends Activity implements OnItemClickListener {
	private AlbumView mAlbumView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.out.println("11111111111111111---创建一个布局了");
		mAlbumView = getId(R.id.imageListView);
		mAlbumView.setRootView((GridLayout) getId(R.id.Rootlayout));
		mAlbumView.setImages(new DemoUtils().moarItems(6, getImageDate()));
		mAlbumView.setOnItemClickListener(this);
	}

	private List<PhotoItem> getImageDate() {
		System.out.println("给photoitem集合填充数据");
		// TODO Auto-generated method stub
		List<PhotoItem> mDataList = new ArrayList<PhotoItem>();
		PhotoItem item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head6;
		item.id = 1;
		item.sort = 1;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head7;
		item.id = 2;
		item.sort = 2;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head8;
		item.id = 3;
		item.sort = 3;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head9;
		item.id = 4;
		item.sort = 4;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head10;
		item.id = 5;
		item.sort = 5;
		mDataList.add(item);

		item = new PhotoItem();
		item.hyperlink = "drawable://" + R.drawable.head1;
		item.id = 6;
		item.sort = 6;
		mDataList.add(item);

		return mDataList;
	}

	/**
	 * 获取控件ID
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends View> T getId(int id) {
		return (T) findViewById(id);
	}

	@Override
	public void ItemClick(View view, int position, boolean Photo) {
		// TODO Auto-generated method stub
		// Photo 照片还是空格子
		L.out("position", position + ",Photo : " + Photo);
	}
}
