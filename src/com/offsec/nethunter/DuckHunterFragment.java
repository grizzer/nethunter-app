package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DuckHunterFragment extends Fragment implements ActionBar.TabListener{

    TabsPagerAdapter TabsPagerAdapter;
    ViewPager mViewPager;
    static SharedPreferences sharedpreferences;

    final static CharSequence[] languages = {"American English", "French", "German", "Spanish", "Swedish", "Italian", "British English", "Russian", "Danish", "Norwegian", "Portugese", "Belgian"};

    private static final String ARG_SECTION_NUMBER = "section_number";

    public DuckHunterFragment() {

    }
    public static DuckHunterFragment newInstance(int sectionNumber) {
        DuckHunterFragment fragment = new DuckHunterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppNavHomeActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.duck_hunter, container, false);
        TabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pagerDuckHunter);
        mViewPager.setAdapter(TabsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {

                }
                getActivity().invalidateOptionsMenu();
            }
        });
        setHasOptionsMenu(true);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.duck_hunter, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            menu.findItem(R.id.duckPreviewRefresh).setVisible(false);
            menu.findItem(R.id.duckConvertUpdate).setVisible(true);
            menu.findItem(R.id.duckConvertConvert).setVisible(true);
        } else {
            menu.findItem(R.id.duckPreviewRefresh).setVisible(true);
            menu.findItem(R.id.duckConvertUpdate).setVisible(false);
            menu.findItem(R.id.duckConvertConvert).setVisible(false);
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.duckConvertUpdate:
                try {
                    File sdcard = Environment.getExternalStorageDirectory();
                    File myFile = new File(sdcard, DuckHunterConvertFragment.configFilePath);
                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    EditText source = (EditText) getActivity().findViewById(R.id.editSource);
                    myOutWriter.append(source.getText());
                    myOutWriter.close();
                    fOut.close();
                    ((AppNavHomeActivity) getActivity()).showMessage("Source updated");
                } catch (Exception e) {
                    ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
                }
                return true;
            case R.id.duckConvertConvert:
                int keyboardLayoutIndex = sharedpreferences.getInt("DuckHunterLanguageIndex", 0);
                String lang;
                switch (keyboardLayoutIndex) {
                    case 1:  lang = "fr";
                        break;
                    case 2:  lang = "de";
                        break;
                    case 3:  lang = "es";
                        break;
                    case 4:  lang = "sv";
                        break;
                    case 5:  lang = "it";
                        break;
                    case 6:  lang = "uk";
                        break;
                    case 7:  lang = "ru";
                        break;
                    case 8:  lang = "dk";
                        break;
                    case 9:  lang = "no";
                        break;
                    case 10:  lang = "pt";
                        break;
                    case 11:  lang = "be";
                        break;
                    default: lang = "us";
                        break;
                }
                String[] command = new String[1];
                command[0] = "su -c \"bootkali duck-hunt-convert " + lang + " /sdcard/" + DuckHunterConvertFragment.configFilePath + " /opt/" + DuckHunterPreviewFragment.configFileFilename + "\"";
                ShellExecuter exe = new ShellExecuter();
                exe.RunAsRoot(command);
                ((AppNavHomeActivity) getActivity()).showMessage("converting started");
                return true;
            case R.id.duckPreviewRefresh:
                TextView source = (TextView) getView().findViewById(R.id.source);
                source.setText(DuckHunterPreviewFragment.readFileForPreview());
                return true;
            case R.id.start_service:
                start();
                return true;
            case R.id.chooseLanguage:
                openLanguageDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void start() {
        String[] command = new String[1];
        command[0] = "su -c 'bootkali duck-hunt-run /opt/duckout.sh'";
        ShellExecuter exe = new ShellExecuter();
        exe.RunAsRoot(command);
        ((AppNavHomeActivity) getActivity()).showMessage("Attack started");
    }



    public void openLanguageDialog() {

        int keyboardLayoutIndex = sharedpreferences.getInt("DuckHunterLanguageIndex", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Language:");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setSingleChoiceItems(languages, keyboardLayoutIndex, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editor editor = sharedpreferences.edit();
                editor.putInt("DuckHunterLanguageIndex", which);
                editor.commit();
            }
        });
        builder.show();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public static class TabsPagerAdapter extends FragmentStatePagerAdapter {


        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 1:
                    return new DuckHunterPreviewFragment();
                default:
                    return new DuckHunterConvertFragment();
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "Preview";
                default:
                    return "Convert";
            }
        }
    }

    public static class DuckHunterConvertFragment extends Fragment implements View.OnClickListener{

        public static String configFilePath = "files/modules/duckconvert.txt";
        public static String loadFilePath = "files/scripts/ducky/";
        private static final int PICKFILE_RESULT_CODE = 1;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.duck_hunter_convert, container, false);

            TextView t2 = (TextView) rootView.findViewById(R.id.reference_text);
            t2.setMovementMethod(LinkMovementMethod.getInstance());

            EditText source = (EditText) rootView.findViewById(R.id.editSource);
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, configFilePath);
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                Log.e("Nethunter", "exception", e);
            }
            source.setText(text);

            Button b = (Button) rootView.findViewById(R.id.duckyLoad);
            Button b1 = (Button) rootView.findViewById(R.id.duckySave);
            b.setOnClickListener(this);
            b1.setOnClickListener(this);
            return rootView;
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.duckyLoad:
                    try {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File scriptsDir = new File(sdcard,loadFilePath);
                        if(!scriptsDir.exists()) scriptsDir.mkdirs();
                    } catch (Exception e) {
                        ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
                    }
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() +"/"+ loadFilePath);
                    intent.setDataAndType(selectedUri, "file/*");
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                    break;
                case R.id.duckySave:
                    try {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File scriptsDir = new File(sdcard,loadFilePath);
                        if(!scriptsDir.exists()) scriptsDir.mkdirs();
                    } catch (Exception e) {
                        ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
                    }
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    alert.setTitle("Name");
                    alert.setMessage("Please enter a name for your script.");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(getActivity());
                    alert.setView(input);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            if(value != null && value.length() >0){
                                //Save file (ask name)
                                File sdcard = Environment.getExternalStorageDirectory();
                                File scriptFile = new File(sdcard + File.separator + loadFilePath + File.separator +  value +".conf");
                                System.out.println(scriptFile.getAbsolutePath());
                                if(!scriptFile.exists()){
                                    try {
                                        EditText source = (EditText) getView().findViewById(R.id.editSource);
                                        String text = source.getText().toString();
                                        scriptFile.createNewFile();
                                        FileOutputStream fOut = new FileOutputStream(scriptFile);
                                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                        myOutWriter.append(text);
                                        myOutWriter.close();
                                        fOut.close();
                                        ((AppNavHomeActivity) getActivity()).showMessage("Script saved");
                                    } catch (Exception e) {
                                        ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
                                    }
                                }else{
                                    ((AppNavHomeActivity) getActivity()).showMessage("File already exists");
                                }
                            }else{
                                ((AppNavHomeActivity) getActivity()).showMessage("Wrong name provided");
                            }
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ///Do nothing
                        }
                    });
                    alert.show();
                    break;
                default:
                    ((AppNavHomeActivity) getActivity()).showMessage("Unknown click");
                    break;
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICKFILE_RESULT_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        String FilePath = data.getData().getPath();
                        EditText source = (EditText) getView().findViewById(R.id.editSource);
                        try {
                            String text = "";
                            BufferedReader br = new BufferedReader(new FileReader(FilePath));
                            String line;
                            while ((line = br.readLine()) != null) {
                                text += line + '\n';
                            }
                            br.close();
                            source.setText(text);
                            ((AppNavHomeActivity) getActivity()).showMessage("Script loaded");
                        } catch (Exception e) {
                            ((AppNavHomeActivity) getActivity()).showMessage(e.getMessage());
                        }
                        break;
                    }
                    break;

            }
        }
    } //end of class


    public static class DuckHunterPreviewFragment extends Fragment{

        public static String configFilePath = "/data/local/kali-armhf/opt/";
        public static String configFileFilename = "duckout.sh";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.duck_hunter_preview, container, false);
            TextView source = (TextView) rootView.findViewById(R.id.source);
            source.setText(readFileForPreview());
            return rootView;
        }



        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {

            }
        }

        public void onResume() {
            super.onResume();
        }

        public static String readFileForPreview()
        {
            File file = new File(configFilePath, configFileFilename);
            if(file.exists()) {
                StringBuilder text = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();
                } catch (IOException e) {
                    Log.e("Nethunter", "exception", e);
                }
                br.close();
            } catch (IOException e) {
                Log.e("Nethunter", "exception", e);
            }
            return text.toString();
        }
    }
}
