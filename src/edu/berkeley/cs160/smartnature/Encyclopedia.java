package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Encyclopedia extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener, Runnable {
	
	ResultAdapter adapter;
	ArrayList<SearchResult> resultList = new ArrayList<SearchResult>();
	String pName = "";
	String name = "";
	EditText search;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Window.FEATURE_PROGRESS
		setContentView(R.layout.encycl);
		adapter = new ResultAdapter(this, R.layout.search_list_item, resultList);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		((Button) findViewById(R.id.searchButton)).setOnClickListener(this);
		search = (EditText) findViewById(R.id.searchText);
		search.setText(name);
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
					findViewById(R.id.encycl_msg).setVisibility(View.VISIBLE);
				}
			});
			return;
		}
		resultList.clear();
		invalidate();
		if (resultBox.child(1).attr("id").equals("_ctl0_mainHolder_noresults")) {
			// Toast.makeText(Encyclopedia.this, "Sorry, no results were found.", Toast.LENGTH_SHORT).show();
			resultList.add(new SearchResult("No results found", "Please try refining your search", "", ""));
			invalidate();
		} else {
			Elements results = resultBox.child(1).children();
			for (int i = 0; i < results.size(); i++) {
				Element next = results.get(i);
				String plantURL = "http://www.plantcare.com" + next.child(0).child(0).child(0).attr("src");
				String name = next.child(1).text();
				String altNames = next.child(2).text().replaceFirst("Also known as:", "Known as:");
				String linkURL = "http://www.plantcare.com/encyclopedia/" + next.child(1).attr("href");
				resultList.add(new SearchResult(name, altNames, plantURL, linkURL));
				invalidate();
			}
		}
		runOnUiThread(new Runnable() {
			@Override public void run() { setProgressBarIndeterminateVisibility(false); }
		});
	}
	
	public void invalidate() {
		runOnUiThread(new Runnable() {
			@Override public void run() { adapter.notifyDataSetChanged(); }
		});
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent result = new Intent(Encyclopedia.this, EncyclopediaResult.class);
		String plantURL = ((TextView) view.findViewById(R.id.linkURL)).getText().toString();
		pName = ((TextView) view.findViewById(R.id.name)).getText().toString();
		result.putExtra("name", pName);
		result.putExtra("linkURL", plantURL);
		startActivity(result);
	}
	
	@Override
	public void onClick(View view) {
		String searchText = ((EditText) findViewById(R.id.searchText)).getText().toString();
		if (searchText.equals(""))
			Toast.makeText(this, "Please enter the plant you are looking for.", Toast.LENGTH_SHORT).show();
		else {
			findViewById(R.id.encycl_msg).setVisibility(View.GONE);
			setProgressBarIndeterminateVisibility(true);
			new Thread(this).start();
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