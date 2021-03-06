package edu.cascadia.bookmarked;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;


/**
 * A fragment representing a list of Items.
 * <p>
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class BookListFragment extends ListFragment {

    //private final static String bookURI = "bookmarked/book/getallbooks";
    private final static String book4SaleURI = "bookmarked/book/getbooksforsale";
    private final static String bookWantedURI = "bookmarked/book/getbookswanted";
    private final static String book4SaleByUsernameURI = "bookmarked/book/getbooksforsalebyusername";
    private final static String bookWantedbyusernameURI = "bookmarked/book/getbookswantedbyusername";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "UserID";

    private String listType;

    private String userID="";

    private BooksAdapter listAdapter;

    private OnFragmentInteractionListener mListener;
    // Progress Dialog Object
    private ProgressDialog prgDialog;


    private BookListItem bookListItem;
    private String serverURI;

    public static BookListFragment newInstance(String param1, String param2) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookListFragment() {
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onMyPostingBookClicked(bookListItem.ITEMS.get(position), listType);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        if (getArguments() != null) {
            listType = getArguments().getString(ARG_PARAM1);
            userID = getArguments().getString(ARG_PARAM2);
        }

        if (!isNetworkAvailable()) {
            Utility.beep();
            showNoNetworkWarning();
            return;
        }

        // load data and call web service if first time get here
        if (listAdapter == null) {
            bookListItem = new BookListItem();

            listAdapter = new BooksAdapter(getActivity(), (ArrayList<BookItem>) bookListItem.ITEMS);

            listAdapter.setForSaleListItem(listType.contains("sell"));

            setListAdapter(listAdapter);
            invokeWS();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoNetworkWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("No network connection!");

        // set dialog message
        alertDialogBuilder
                .setMessage("Please connect to the network and refresh the list")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // do nothing
                            }
                        }
                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            serverURI = Utility.getServerAddress(activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onMyPostingBookClicked(BookItem bookItem, String listType);
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     */
    private void invokeWS(){
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        String hostAddress = "http://" + serverURI + "/";
        String wsURL;
        //wsURL = hostAddress + book4SaleURI;
        if (listType.equals("wanted-view")) {
            wsURL = hostAddress + bookWantedURI;
        } else if (listType.equals("sell-view")) {
            wsURL = hostAddress + book4SaleURI;
        } else if (listType.equals("my-sell-list")){
            wsURL = hostAddress + book4SaleByUsernameURI;
        } else {
            wsURL = hostAddress + bookWantedbyusernameURI;
        }

        RequestParams requestParams = new RequestParams();
        if (listType.startsWith("my")) {
            requestParams.add("username", userID);
        }

        System.out.println("Getting " + wsURL);

        client.get(wsURL, requestParams, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                String toastMsg;
                if (listType.contains("sell")) {
                    toastMsg = "Books for sale queried successfully";
                } else {
                    toastMsg = "Books wanted queried successfully";
                }
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONArray jsonArray = new JSONArray(response);

                    // reset the listAdapter
                    listAdapter.clear();
                    try {
                        getArray(jsonArray);
                        listAdapter.notifyDataSetChanged();
                    } catch (ParseException e) {
                        System.out.println("Exception e:" + e.getMessage());
                        e.printStackTrace();
                    }

                    // When the JSON response has status boolean value assigned with true
                    if (response.length() > 0) {
                        // Display successfully registered message using Toast
                        //Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
                    }
                    // Else display error message
					/* else {
						System.out.println(obj.getString("error_msg"));
						Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
					} */
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.json_exception), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.http_404_error), Toast.LENGTH_SHORT).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.http_500_error), Toast.LENGTH_SHORT).show();
                }
                // When Http response code other than 404, 500
                else {
                    System.out.println("error:" + error.getMessage());
                    Toast.makeText(getActivity(), getResources().getString(R.string.unexpected_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getArray(JSONArray jsonArr) throws ParseException {

        try {
            for (int k = 0; k < jsonArr.length(); k++) {

                try {
                    if (jsonArr.getJSONObject(k) instanceof JSONObject) {
                        //System.out.println("BOOK " + k + ":");
                        addBookToAdapter(jsonArr.getJSONObject(k));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception ex) {
            System.out.println("Exception in getArray:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addBookToAdapter(JSONObject jsonObject) throws ParseException {

        try {
            String isbn = jsonObject.getString("isbn");
            String title = jsonObject.getString("title");

            BookItem bookItem = new BookItem(isbn, title, jsonObject.toString());

            listAdapter.add(bookItem);
        } catch (JSONException e) {
            System.out.println("Exception in addBookToAdapter. e:" + e.getMessage());
            e.printStackTrace();
        }

    }

    public void refreshList() {
        // for now, just reload the data from backend
        // should be optimized later by adding only to the adapter
        listAdapter.clear();
        invokeWS();
    }
}
