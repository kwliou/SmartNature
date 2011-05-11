package edu.berkeley.cs160.smartnature;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Encyclopedia extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener, Runnable {
	
	ResultAdapter adapter;
	ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
	String pName = "";
	String name = "";
	EditText search;
	
	/** Called when the activity is first created. */
	@Override @SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Window.FEATURE_PROGRESS
		setContentView(R.layout.encycl);
		
		Object previousData = getLastNonConfigurationInstance();
		if (previousData != null)
			resultList = (ArrayList<SearchResult>) previousData;
		
		adapter = new ResultAdapter(this, R.layout.search_list_item, resultList);
		getListView().setAdapter(adapter);
		getListView().setOnItemClickListener(this);
		Button searchBtn = (Button) findViewById(R.id.searchButton);
		searchBtn.setOnClickListener(this);
		search = (EditText) findViewById(R.id.searchText);
		Intent intent = getIntent();
	    
		if (previousData == null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
			name = getIntent().getStringExtra(SearchManager.QUERY);
			search.setText(name);
			searchBtn.performClick();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return resultList.isEmpty() ? null : resultList;
	}
	
	@Override
	public void run() {
		String searchText = ((EditText) findViewById(R.id.searchText)).getText().toString();
		String searchURL = "http://www.plantcare.com/encyclopedia/search.aspx?q=" + searchText;
		Element resultBox = null;
		try {
			Document doc = Jsoup.connect(searchURL).get();
			resultBox = doc.getElementById("searchEncyclopedia");
		} catch (Exception e) {
			e.printStackTrace();
			runOnUiThread(new Runnable() {
				@Override public void run() {
					setProgressBarIndeterminateVisibility(false);
					((TextView)findViewById(R.id.encycl_msg_title)).setText("Sorry!");
					((TextView)findViewById(R.id.encycl_msg_body)).setText("No internet connection found.");
					findViewById(R.id.encycl_msg).setVisibility(View.VISIBLE);
				}
			});
			return;
		}
		if (resultBox.child(1).attr("id").equals("_ctl0_mainHolder_noresults")) {
			runOnUiThread(new Runnable() {
				@Override public void run() {
					((TextView)findViewById(R.id.encycl_msg_title)).setText("No results found");
					((TextView)findViewById(R.id.encycl_msg_body)).setText("Please try refining your search.");
					findViewById(R.id.encycl_msg).setVisibility(View.VISIBLE);
					setProgressBarIndeterminateVisibility(false);
				}
			});
		} else {
			Elements results = resultBox.child(1).children();
			for (int i = 0; i < results.size(); i++) {
				Element next = results.get(i);
				String plantURL = "http://www.plantcare.com" + next.child(0).child(0).child(0).attr("src");
				String name = next.child(1).text();
				String altNames = next.child(2).text().replaceFirst("Also known as:", "Known as:");
				String linkURL = "http://www.plantcare.com/encyclopedia/" + next.child(1).attr("href");
				SearchResult result = new SearchResult(name, altNames, plantURL, linkURL);
				new Thread(new LoadBitmap(result, i == results.size() - 1)).start();
				resultList.add(result);
				invalidate();
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SearchResult result = resultList.get(position);
		Intent intent = new Intent(this, EncyclopediaResult.class);
		intent.putExtra("name", result.getName());
		intent.putExtra("linkURL", result.getLinkURL());
		startActivity(intent);
	}
	
	@Override
	public void onClick(View view) {
		String searchText = search.getText().toString().trim();
		if (!searchText.equals("")) {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
			resultList.clear();
			findViewById(R.id.encycl_msg).setVisibility(View.GONE);
			setProgressBarIndeterminateVisibility(true);
			new Thread(this).start();
		}
	}
	
	public void invalidate() {
		runOnUiThread(new Runnable() {
			@Override public void run() { adapter.notifyDataSetChanged(); }
		});
	}
	
	class LoadBitmap implements Runnable {
		private SearchResult result;
		private boolean last;
		LoadBitmap(SearchResult result, boolean last) { this.result = result; this.last = last; }
		@Override
		public void run() {
			try {
				result.setBitmap(BitmapFactory.decodeStream(new URL(result.getPicURL()).openStream()));
			} catch (IOException e) { e.printStackTrace(); }
			runOnUiThread(new Runnable() {
				@Override public void run() {
					if (last)
						setProgressBarIndeterminateVisibility(false);
					adapter.notifyDataSetChanged();
				}
			});
		}
	}
	
	class ResultAdapter extends ArrayAdapter<SearchResult> {
		
		private ArrayList<SearchResult> items;
		private LayoutInflater li;
		
		public ResultAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			this.items = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = li.inflate(R.layout.search_list_item, null);
			SearchResult s = items.get(position);
			((TextView) v.findViewById(R.id.name)).setText(s.getName());
			((TextView) v.findViewById(R.id.altNames)).setText(s.getAltNames());
			if (!s.getLinkURL().equals("")) {
				((TextView) v.findViewById(R.id.linkURL)).setText(s.getLinkURL());
				try {
					((ImageView) v.findViewById(R.id.searchPic)).setImageBitmap(s.getBitmap());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return v;
		}
	}

}