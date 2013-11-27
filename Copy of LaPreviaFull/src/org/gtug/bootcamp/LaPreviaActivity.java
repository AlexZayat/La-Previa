package org.gtug.bootcamp;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.gtug.bootcamp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class LaPreviaActivity extends Activity implements OnClickListener {

	private List<String> mNames = new ArrayList<String>();
	private List<Float> mAmounts = new ArrayList<Float>();
	
	private List<String> mListViewData = new ArrayList<String>();
	private ArrayAdapter<String> mListViewAdapter;
	private int mItemToRemove;
	
	private String mCurrencySymbol;  	
	
	
	static final int DIALOG_EMPTY_NAME_ID = 0;
	static final int DIALOG_EMPTY_AMOUNT_ID = 1;
	static final int DIALOG_NO_ENTRIES_ID = 2;

	
	static final String PREFERENCES_NAMES = "names";
	static final String PREFERENCES_AMOUNTS = "amounts";

	static final String PREFERENCES_SPLIT_TOKEN = ";";
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		mCurrencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
		
		loadPreferences();
		
		ListView list = (ListView) findViewById(R.id.mylistview);
		mListViewAdapter = new ArrayAdapter<String>(this, R.layout.list_item, mListViewData);
		list.setAdapter(mListViewAdapter);

		Button addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(this);

		Button calculateButton = (Button) findViewById(R.id.calculateButton);
		calculateButton.setOnClickListener(this);

		Button cleanButton = (Button) findViewById(R.id.cleanButton);
		cleanButton.setOnClickListener(this);
		
		ListView lv = (ListView) findViewById(R.id.mylistview);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showRemoveDialog(position);
			}
		});
	}

	// @Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.calculateButton:
			onCalculateButtonClick(view);
			break;
		case R.id.cleanButton:
			onCleanButtonClick(view);
			break;
		case R.id.addButton:
			onAddButtonClick(view);
			break;
		}
	}
	
	
	private void onCalculateButtonClick(View view) {
		if (mListViewData.size() > 0) {
			showResultDialog();
		} else {
			showDialog(DIALOG_NO_ENTRIES_ID);
		}
	}
	
	
	private void onCleanButtonClick(View view) {
		mNames.clear();
		mAmounts.clear();
		mListViewData.clear();
		mListViewAdapter.notifyDataSetChanged();
		
		clearPreferences(); 
	}

	
	private void onAddButtonClick(View view) {
		EditText nameEditText = (EditText) findViewById(R.id.nameTextEdit);
		EditText amountEditText = (EditText) findViewById(R.id.amountTextEdit);

		

		String nameStr = nameEditText.getText().toString();
		String amountStr = amountEditText.getText().toString();

		if (nameStr.length() == 0) {
			showDialog(DIALOG_EMPTY_NAME_ID);
			return;
		} else if (amountStr.length() == 0) {
			showDialog(DIALOG_EMPTY_AMOUNT_ID);
			return;
		}

		

		mNames.add(nameStr);
		mAmounts.add(new Float(amountStr));

		mListViewData.add(nameStr + ": " + mCurrencySymbol + amountStr);
		mListViewAdapter.notifyDataSetChanged();
		
		
		
		savePreferences();

		

		nameEditText.setText("");
		amountEditText.setText("");
		nameEditText.requestFocus();
	}
	
	
	private void removeListItem(int position)  {
		mNames.remove(position);
		mAmounts.remove(position);
		mListViewData.remove(position);
		mListViewAdapter.notifyDataSetChanged();
		
		savePreferences();	
	}
			
	

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;

		switch (id) {
		case DIALOG_EMPTY_AMOUNT_ID:
			dialog = buildOkDialog(R.string.insertAmount);
			break;
		case DIALOG_EMPTY_NAME_ID:
			dialog = buildOkDialog(R.string.insertName);
			break;
		case DIALOG_NO_ENTRIES_ID:
			dialog = buildOkDialog(R.string.insertPeople);
			break;
		default:
			dialog = null;
		}

		return dialog;
	}



	private Dialog buildOkDialog(int messageId) {
		Dialog dialog;


		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(messageId)
				.setCancelable(false)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		dialog = builder.create();

		return dialog;
	}
	
	private void showRemoveDialog(int position) {
		mItemToRemove = position;

		Dialog dialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		String message = String.format(getString(R.string.remove), mListViewData.get(position));
		
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							removeListItem(mItemToRemove);
						}
					})
				.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});

		dialog = builder.create();

		dialog.show();
	}
	
	private void showResultDialog() {
		Dialog dialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		String message = getResultString();
		
				builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
						}
					});
					

		dialog = builder.create();

		dialog.show();
	}
		
	private String getResultString() {
		PaymentAlgorithm paymentAlg = new PaymentAlgorithm();
		List<PaymentAlgorithm.Payment> payments = paymentAlg.getResult(mAmounts);

		Float average = 0.0f;
		for (Float amount : mAmounts) {
			average += amount;
		}		
		average /= mAmounts.size();
		
		StringBuilder result = new StringBuilder();

		
		Formatter formatter = new Formatter();
		formatter.format(getString(R.string.average), mCurrencySymbol, average);
		result.append(formatter.toString());
		result.append("\n\n");
		
		
		for (PaymentAlgorithm.Payment p : payments) {
			formatter = new Formatter();
			formatter.format(getString(R.string.XPaysToY), 
					mNames.get(p.mPayer), 
					mCurrencySymbol, p.mAmount, 
					mNames.get(p.mPayee));
			result.append(formatter.toString());
			result.append("\n");
		}

		return result.toString();
	}
	/* Lo de abajo es lo que hace que no se pierdan los datos aunque hayamos cerrado el proceso de la previa */
	void savePreferences() {
		SharedPreferences.Editor preferences = getPreferences(MODE_PRIVATE).edit();
		StringBuilder namesBuilder = new StringBuilder();
		StringBuilder amountsBuilder = new StringBuilder();
		
		for (int i = 0; i < mNames.size() && i < mAmounts.size(); i++) {
			namesBuilder.append((i==0 ? "" : PREFERENCES_SPLIT_TOKEN) + mNames.get(i));
			amountsBuilder.append((i==0 ? "" : PREFERENCES_SPLIT_TOKEN) + mAmounts.get(i));
		}
		
		preferences.putString(PREFERENCES_NAMES, namesBuilder.toString());
		preferences.putString(PREFERENCES_AMOUNTS, amountsBuilder.toString());
		preferences.commit();
	}
	
	void loadPreferences() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		String prefNames = preferences.getString(PREFERENCES_NAMES, "");
		String prefAmounts = preferences.getString(PREFERENCES_AMOUNTS, "");

		if (prefNames.length() > 0 && prefAmounts.length() > 0) {
			String[] names = prefNames.split(PREFERENCES_SPLIT_TOKEN);
			String[] amounts = prefAmounts.split(PREFERENCES_SPLIT_TOKEN);
		
			for (int i = 0; i < names.length && i < amounts.length; ++i) {
				mNames.add(names[i]);
				mAmounts.add(new Float(amounts[i]));
				mListViewData.add(names[i] + ": " + mCurrencySymbol + amounts[i]);
			}
		}
	}
	
	void clearPreferences() {
		SharedPreferences.Editor preferences = getPreferences(MODE_PRIVATE).edit();
		preferences.clear();
		preferences.commit();
	}
	
}
